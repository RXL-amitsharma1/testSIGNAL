from django.shortcuts import render
from rest_framework.response import Response
from rest_framework import status
from rest_framework.decorators import api_view
import json
import logging
from datetime import datetime
from django.conf import settings
import time
from rest_framework.views import APIView
# import ldap
# from django.shortcuts import redirect
# from django.views.decorators.csrf import csrf_exempt
# from django.http import HttpResponseRedirect
# from django.utils.decorators import method_decorator
# from onelogin.saml2.auth import OneLogin_Saml2_Auth
# from onelogin.saml2.utils import OneLogin_Saml2_Utils
from django.http import JsonResponse, HttpResponseNotFound
from django.template.defaulttags import register
import sys
from multiprocessing import Pool
from functools import partial
from Decision_Support_System.DBController import DBController

from .apps import DssConfig
from .score_calculation import calculate_score_and_save, error_callback, trend_data_etl
from .db_extraction import DBConnection
from .dss_network import Model
import pandas as pd
model = Model(DssConfig, remove_nodes=settings.DEACTIVATED_NODES)

ymd_format = '%Y-%m-%d'
dby_format = "%d-%b-%Y"
default_error_message = "Inside views.py - Error on line {}. Exception type: {}, Exception: {} "

class Calculation(APIView):
    """
    Fetched the data in chunks from DB performs calculation and write the data with score into DB
    Also save a meta information about the alert into DB.
    """

    def __init__(self):
        super(Calculation, self).__init__()

    def get_next_n_items(self, df_iter, n, part_no):
        chunks = []
        row_count = 0
        for df in df_iter:
            n -= 1
            row_count += df.shape[0]
            chunks.append((part_no, df))
            part_no+=1
            if n == 0:
                break
        return chunks, len(chunks), row_count

    def post(self, request):
        logging.info('***************Score Calculation started*****************')
        try:
            data = json.loads(request.body)
            alert_id = data['alert_id']
            is_master = data['is_master']
            prev_alert_ids = None
            if 'prev_alert_ids' in data.keys():
                prev_alert_ids = data['prev_alert_ids']
                prev_alert_ids = ','.join([str(_i) for _i in prev_alert_ids])
            
            if 'datasource' in data.keys():
                datasource = data['datasource']
            else:
                datasource = 'pva'

            logging.info(f"Received following parameters: {alert_id}, {datasource}, {is_master}, {prev_alert_ids}")
        except Exception as e:
            logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            return Response({'Message': 'Request parameter Missing', 'error' : default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e)}, status=status.HTTP_400_BAD_REQUEST)

        try:
            start_time = time.time()

            trend_flag = 0
            dbc = DBController(datasource)
            if prev_alert_ids:
                mart_connection = DBConnection(datasource)
                start_timestamp = mart_connection.get_db_timestamp()
                mart_connection.close()

                logging.info("Running previous alert data etl")
                _start_time = time.time()
                row_counts = trend_data_etl(prev_alert_ids, dbc, alert_id, is_master)
                trend_flag = 1
                _time_delta = time.time() - _start_time
                logging.info(f"Time taken in prev alert data etl: {_time_delta/60} min.")

                end_timestamp = start_timestamp + pd.Timedelta(f"{_time_delta} sec")
                mart_connection = DBConnection(datasource)
                mart_connection.call_procedure('p_pvs_logging',
                                               [start_timestamp, end_timestamp, alert_id, None,
                                                f'trend_data_etl for {alert_id} is successful. {start_timestamp}, {end_timestamp}',
                                                None, None, row_counts])
                mart_connection.close()

            _t = time.time()
            logging.info(f"Running DB procedure to create PVS_DSS_EVID_{alert_id}")
            mart_connection = DBConnection(datasource)
            mart_connection.call_procedure('p_pvs_dss_full',[alert_id, trend_flag])
            logging.info(f"Running DB procedure took: {round((time.time() - _t) / 60, 3)} min.")

            _t = time.time()
            start_timestamp = mart_connection.get_db_timestamp()
            query = f"SELECT * FROM PVS_DSS_EVID_{alert_id}"
            df_iter = mart_connection.read_db_table_iter(query, chunk_size=settings.ALERT_ROWS)
            part_no = 0
            row_counts = 0

            while True:
                chunks, c_count, _row_count = self.get_next_n_items(df_iter, settings.CORES_PER_ALERT, part_no)
                part_no+=c_count
                if c_count == 0:
                    break
                row_counts += _row_count
                pool = Pool(c_count)
                func = partial(calculate_score_and_save, alert_id, is_master, trend_flag, dbc)
                _ = pool.map(func, chunks, chunksize=1)
                pool.close()
                pool.join()

            if row_counts > 0:
                _time_delta = time.time() - _t
                end_timestamp = start_timestamp + pd.Timedelta(f"{_time_delta} sec")
                mart_connection.call_procedure('p_pvs_logging',
                                               [start_timestamp, end_timestamp, alert_id, None,
                                                f'pvs_dss_results_{alert_id} table created & populated. {start_timestamp}, {end_timestamp}',
                                                None, None, row_counts])
                logging.info(f"Calculating scores took: {round(_time_delta / 60, 3)} min.")
                # mart_connection.close()

                meta_str = json.dumps({"DEACTIVATED_NODES": settings.DEACTIVATED_NODES,
                                       "ALL_DEACTIVATED_NODES": model.deactivated_nodes,
                                       "ALL_DEACTIVATED_LABELS": [DssConfig.node_label_dict[node] for node in model.deactivated_nodes]})
                meta_table = f"PVS_DSS_META_INFO_{alert_id}"
                dbc.create_write_table(pd.DataFrame({'EXEC_CONFIG_ID':alert_id,'META_INFO':meta_str}, index=[0]),
                                       meta_table)
                logging.info(f"Inside Calculation - meta file saved for {alert_id}")
            else:
                logging.info(f"{row_counts} rows exists for {alert_id}. Nothing to calculate")
                logging.info(f"Total time taken for alert_id: {alert_id} is {(time.time() - start_time) / 60} minutes.")
                return Response({"message": "No PEC exist for DSS score calculation for alert_id: {alert_id}"},
                                status=status.HTTP_500_INTERNAL_SERVER_ERROR)

            logging.info(f"Total time taken for alert_id: {alert_id} is {(time.time()-start_time)/60} minutes.")
            return Response({"message": "Process Completed"}, status=status.HTTP_200_OK)
        except Exception as e:
            logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            return Response({'Message': "Exception type:{}, Exception value:{} writing the score in file ".format(type(e), e)},
                            status=status.HTTP_400_BAD_REQUEST)
        finally:
            if "mart_connection" in locals():
                mart_connection.close()
                logging.info("finally connections closed")

# def init_saml_auth(req):
#     """ Creating saml2.0 request for SSO"""
#     logging.info('Inside init_saml_auth function')
#     auth = OneLogin_Saml2_Auth(req, custom_base_path=settings.SAML_FOLDER)
#     return auth
#
#
# def prepare_django_request(request, params=''):
#     logging.info('Inside prepare_django_request function')
#     # If server is behind proxys or balancers use the HTTP_X_FORWARDED fields
#     result = {
#         'https': 'on' if settings.HTTPS == 'True' else 'off',
#         'http_host': request.META['HTTP_HOST'],
#         'script_name': request.META['PATH_INFO']+params,
#         'get_data': request.GET.copy(),
#         # Uncomment if using ADFS as IdP, https://github.com/onelogin/python-saml/pull/144
#         # 'lowercase_urlencoding': True,
#         'post_data': request.POST.copy()
#     }
#     if settings.HTTPS != 'True':
#         result['server_port'] = settings.SERVER_PORT
#
#     logging.info("Request created inside prepare_django_request {}".format(result))
#     return result
#

# @csrf_exempt
# def login(request):
#     """
#     This function is used to handle SAML endpoints.
#
#     - acs : acs is present in request after logon service response is sent from IDP.
#             After successful authentication, session values are set and redirected to relay state
#             which is the url from where the page was called.
#     - slo : This endpoint can be used to logout user session from IDP. SP initiated logout.
#             IDP will send sls request to all the client,
#     - sls : This endpoint is called after session is logged out from idp. It clears the session
#             variables and redirects to main application
#     """
#
#     logging.info('Inside login function - used to handle saml endpoints')
#     req = prepare_django_request(request)
#     auth = init_saml_auth(req)
#
#     if 'acs' in req['get_data']:
#
#         logging.info('Inside login function - acs endpoint found')
#         request_id = None
#         if 'AuthNRequestID' in request.session:
#             request_id = request.session['AuthNRequestID']
#         auth.process_response(request_id=request_id)
#         errors = auth.get_errors()
#         not_auth_warn = not auth.is_authenticated()
#         logging.info('Inside login function - auth.is_authenticated {}'.format(not_auth_warn))
#
#         if not errors:
#             logging.info('Inside login function - acs endpoint found - no error - creating user session')
#             if 'AuthNRequestID' in request.session:
#                 del request.session['AuthNRequestID']
#             request.session['samlUserdata'] = auth.get_attributes()
#             request.session['samlNameId'] = auth.get_nameid()
#             request.session['samlNameIdFormat'] = auth.get_nameid_format()
#             request.session['samlNameIdNameQualifier'] = auth.get_nameid_nq()
#             request.session['samlNameIdSPNameQualifier'] = auth.get_nameid_spnq()
#             request.session['samlSessionIndex'] = auth.get_session_index()
#             try:
#                 logging.info('Inside login function - acs endpoint found - no error - extracting cn from ldap')
#
#                 con = ldap.initialize(settings.AUTH_LDAP_SERVER_URI)
#                 con.simple_bind_s(settings.AUTH_LDAP_BIND_DN, settings.AUTH_LDAP_BIND_PASSWORD)
#                 results = con.search_s(settings.AUTH_DC, ldap.SCOPE_SUBTREE, settings.LDAP_USERNAME_MAPPING+"="+auth.get_nameid())
#                 request.session['common_name'] = results[0][1][settings.LDAP_DISPLAY_PROPERTY][0].decode("utf-8")
#             except Exception as e:
#                 logging.info('Inside login function - acs endpoint found - no error - username not found, so setting username as common name')
#                 request.session['common_name'] = auth.get_nameid()
#                 logging.error('Error occurred while common_name extraction.' +"Exception type:{}, Exception value:{} occurred while name extraction.".format(type(e),e))
#
#             if 'RelayState' in req['post_data'] and OneLogin_Saml2_Utils.get_self_url(req) != req['post_data']['RelayState']:
#                 logging.info('Inside login function - acs endpoint found - calling relay state after session creation')
#                 logging.info("relay state {}".format(req['post_data']['RelayState']))
#                 return HttpResponseRedirect(auth.redirect_to(req['post_data']['RelayState']))
#         elif auth.get_settings().is_debug_active():
#             error_reason = auth.get_last_error_reason()
#             logging.info("error in acs endpoint - {}".format(error_reason))
#     elif 'slo' in req['get_data']:
#         logging.info('Inside login function - slo endpoint found')
#         name_id = session_index = name_id_format = name_id_nq = name_id_spnq = None
#         if 'samlNameId' in request.session:
#             name_id = request.session['samlNameId']
#         if 'samlSessionIndex' in request.session:
#             session_index = request.session['samlSessionIndex']
#         if 'samlNameIdFormat' in request.session:
#             name_id_format = request.session['samlNameIdFormat']
#         if 'samlNameIdNameQualifier' in request.session:
#             name_id_nq = request.session['samlNameIdNameQualifier']
#         if 'samlNameIdSPNameQualifier' in request.session:
#             name_id_spnq = request.session['samlNameIdSPNameQualifier']
#         logging.info('Inside login function - -slo endpoint - calling auth.logout for removing user session from IDP')
#         return HttpResponseRedirect(auth.logout(name_id=name_id, session_index=session_index, nq=name_id_nq, name_id_format=name_id_format, spnq=name_id_spnq))
#     elif 'sls' in req['get_data']:
#         logging.info('Inside login function - sls endpoint found')
#         request_id = None
#         if 'LogoutRequestID' in request.session:
#             request_id = request.session['LogoutRequestID']
#         dscb = lambda: request.session.flush()
#         logging.info('Inside login function - sls endpoint found - flushing session')
#         url = auth.process_slo(request_id=request_id, delete_session_cb=dscb)
#         errors = auth.get_errors()
#
#         if len(errors) == 0:
#             if url is not None:
#                 logging.info('Inside login function - sls endpoint found - redirecting to keycloak login page')
#                 return HttpResponseRedirect(url)
#             else:
#                 success_slo = True
#                 logging.info(
#                     'Inside login function - sls endpoint found - session already cleared by slo - redirecting to application, success_slo - {}'.format(success_slo))
#         elif auth.get_settings().is_debug_active():
#             error_reason = auth.get_last_error_reason()
#             logging.info("error in sls endpoint - {}".format(error_reason))
#
#     logging.info('Inside login function - no condition met - redirecting to application logout')
#     return redirect(settings.LOGOUT_REDIRECT)
#
#
# def login_ajax_session(function):
#     """
#     login_ajax_session is the decorator which would be included on every ajax request.
#     If user session is present then the ajax function is called, else a JSONresponse is
#     sent back as ajax response.
#     This response would would have two parameters
#     - login: 'true' - js code redirects to url parameter.
#     - url - main DSS output page url which calls login_session function and initiates login process
#
#     This needs to be added on every ajax request to perform authentication.
#     """
#
#     def check_login(request):
#         logging.info("Inside login_ajax_session decorator - called from ajax API's")
#
#         if 'samlUserdata' not in request.session.keys() and settings.SSO_FLAG == 'True':
#             logging.info("Inside login_ajax_session decorator - session does not exist - redirecting to main page")
#             """
#             Each ajax request has alert_id, archived flag, product name and event name.
#             Using product name and event name extract the id and send a new request to
#             page_output api which will call the login process
#             """
#             try:
#                 if request.method == 'GET':
#                     alert_id = request.GET['alert_id']
#                     archived = request.GET['archived']
#                     p = request.GET['p']
#                     e = request.GET['e']
#                     logging.info("Extracted alert_id, archived,p,e {},{},{},{} request"
#                                  "parameters".format(alert_id, archived, p, e))
#                 else:
#                     data = json.loads(request.body)
#                     alert_id = data['alert_id']
#                     archived = data['archived']
#                     p = data['p']
#                     e = data['e']
#                     logging.info("Extracted alert_id, archived,p,e {},{},{},{} request"
#                          "parameters".format(alert_id, archived, p, e))
#             except Exception as e:
#                 logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
#                 raise e
#
#             table_name = 'AGG_ALERT'
#             # comment for arch change
#             if archived == 'true':
#                 table_name = 'ARCHIVED_AGG_ALERT'
#             query = "SELECT ID from {} where exec_configuration_id = {} and "\
#                     "lower(product_name) = '{}' and lower(pt) = '{}'".format(table_name,
#                         alert_id, p.lower().replace("'", "''"), e.lower().replace("'", "''"))
#
#             appdb_connection = DBConnection('default')
#             try:
#                 # Object creation of db_connection class
#                 id_no = appdb_connection.execute_count(query)
#             except Exception as e:
#                 logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
#                 raise e
#             finally:
#                 appdb_connection.close()
#
#             # return login as true and url in ajax response which will refresh the url
#             if settings.HTTPS == 'True':
#                 redirect_url = 'https://'+request.META['HTTP_HOST']+'/network/?&alert_id='+str(alert_id)+'&row_id='+str(id_no)+'&archived='+str(archived)
#             else:
#                 redirect_url = 'http://'+request.META['HTTP_HOST']+'/network/?&alert_id='+str(alert_id)+'&row_id='+str(id_no)+'&archived='+str(archived)
#
#             return JsonResponse({'login': 'true', 'url': redirect_url})
#
#         else:
#             logging.info("Inside login_ajax_session decorator - exist - redirecting to calling ajax function")
#             return function(request)
#     return check_login
#
#
# def login_session(function):
#     """
#     login_session is the main decorator - which checks whether user session is present in request.
#     - If user session is not present, a request is sent to IDP using auth.login()
#     - If user session is present, request is redirected to calling function
#
#     Currently used as a decorator for page_output() API function, which is called from main pvsignal
#     application.
#     """
#
#     logging.info("Inside login_session decorator - called from main page_output API")
#
#     def check_login(request):
#
#         if 'samlUserdata' not in request.session.keys() and settings.SSO_FLAG.lower() == 'true':
#             logging.info("Inside login_session decorator - session does not exist - initiating login process")
#             req = prepare_django_request(request, params='?'+request.META['QUERY_STRING'])
#             auth = init_saml_auth(req)
#             return HttpResponseRedirect(auth.login())
#         else:
#             logging.info("Inside login_session decorator - request session present - redirecting back to API call")
#             return function(request)
#     return check_login


@api_view(['GET'])
def dss_display(request):
    logging.info("DSS Module Started.")
    response_data = {
        "message": "DSS Application is up!",
        "appVersion": settings.APP_VERSION
    }
    return JsonResponse(response_data)


# @login_session
@api_view(['GET', 'POST'])
def page_output(request):
    """
    page_output function is called when 'network' is mentioned in url.
    This API is called when the PEC scores are clicked in Quantitative review tab.
    This function handles
    - parameter extraction
    - DB connection
    - renders HTML page
    """
    
    logging.info('****************UI rendering process started*******************')
    start_time = time.time()

    logging.info('Inside page_output - Extracting parameters from url')
    # Extracting params from url
    try:
        alert_id = request.GET['alert_id']
        row_id = request.GET['row_id']
        archived = request.GET['archived']
        if "username" not in request.GET.keys() :
            return HttpResponseNotFound('<h2>Unauthorised</h2>')
        else:
            common_name = request.GET['username']
        
        logging.info("Inside page_output - Extracted alert_id, row_id,archived flag {},{},{} using GET "
                     "request".format(alert_id, row_id, archived))
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        response = HttpResponseNotFound('<h2>Request is not fulfilled!</h2>')
        response['X-Frame-Options'] = 'ALLOW-FROM *'
        return response
    """    
    # Extracting data from app DB
    logging.info('Inside page_output - Extracting data to render UI from Application DB')
    query = "SELECT DSS_SCORE, period_end_date,period_start_date, DSS_CONFIRMATION, DSS_COMMENTS " \
                "FROM AGG_ALERT where exec_configuration_id = "+alert_id+" and ID = "+row_id
    """
    # commented for arch change

    # table mapping based on archived flag
    if archived == 'false':
        query = "SELECT DSS_SCORE, period_end_date,period_start_date, DSS_CONFIRMATION, DSS_COMMENTS " \
                "FROM AGG_ALERT where exec_configuration_id = "+alert_id+" and ID = "+row_id
    else:
        query = "SELECT DSS_SCORE, period_end_date, period_start_date, DSS_CONFIRMATION, DSS_COMMENTS" \
                " FROM ARCHIVED_AGG_ALERT where exec_configuration_id = "+alert_id+" and ID = "+row_id

    # Object creation of db_connection class
    
    try:
        appdb_connection = DBConnection('default')
        record_df = appdb_connection.read_table(query)

        # converting column name to lower case
        record_df.columns = [column.lower() for column in record_df.columns]
        # record related to row_id (unique for prod event)
        logging.info('Inside page_output - Creating selected_record which stores the DSS_SCORES')
        selected_record = json.loads(str(record_df.dss_score.values[0]))
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        response = HttpResponseNotFound('<h2>Request is not fulfilled!</h2>')
        response['X-Frame-Options'] = 'ALLOW-FROM *'
        return response

    finally:
        if "appdb_connection" in locals():
            appdb_connection.close()

    selected_period = {'id': alert_id,
                       'text': datetime.strptime(str(datetime.date(record_df['period_start_date'].iloc[0])),
                                                 ymd_format).strftime(dby_format)
                               + " - " + datetime.strptime(str(datetime.date(record_df['period_end_date'].iloc[0])),
                                                           ymd_format).strftime(dby_format),
                       'title': archived}

    # comment and confirm for modal review
    comment = record_df.dss_comments.values[0]
    confirm = record_df.dss_confirmation.values[0]

    if comment is None:
        comment = ''
    if confirm is None:
        confirm = ''

    comment = comment.replace('\n', '\\n')

    logging.info("Inside page_output - Creating node_list and edge_list for vis.js graph")
    vis_node_list = []
    for node in model.name_to_id:
        if node in DssConfig.input_nodes:
            vis_node_list.append({'id': node,
                                  'label': DssConfig.node_label_dict[node] + '\n'
                                           + DssConfig.state_label_dict[(selected_record[node.lower()])]})
        else:
            vis_node_list.append({'id': node, 'label': DssConfig.node_label_dict[node]})

    vis_edge_list = model.edges


    logging.info("Checking ror_flag for ror/iror display")
    output_node_label_dict = DssConfig.node_label_dict.copy()
    output_rationale_text_potential_yes = DssConfig.rationale_text_potential_yes.copy()
    output_rationale_text_potential_no = DssConfig.rationale_text_potential_no.copy()
    try:
        if selected_record['ror_flag'] == 1:
            output_node_label_dict['ror'] = output_node_label_dict['ror'].replace('ROR','iROR')
            output_rationale_text_potential_yes['ror'] = output_rationale_text_potential_yes['ror'].replace('ROR','iROR')
            output_rationale_text_potential_no['ror'] = output_rationale_text_potential_no['ror'].replace('ROR','iROR')
    except Exception as e:
        logging.info("Error occured while setting ror/iror display value")

    # removing rationale_details from selected record
    selected_record.pop('rationale_details',-1)
    logging.info("Inside page_output - Creating context dictionary with all the values needed in UI")
    context = {'nodes': vis_node_list, 'edges': vis_edge_list,
               'terminal_nodes': DssConfig.input_nodes, 'non_terminal_nodes': DssConfig.non_terminal_nodes,
               'manual_nodes': DssConfig.manual_nodes, 'node_label_dict': (output_node_label_dict),
               'boolean_severity': DssConfig.boolean_severity,
               'percent_severity': DssConfig.percent_severity,
               'severity_incr_threshold': DssConfig.severity_incr_threshold,
               'state_dict': json.dumps(model.state_dict),
               'node_effect_dict': json.dumps(DssConfig.node_effect),
               'dss_threshold_dict': json.dumps(DssConfig.dss_threshold_dict),
               'threshold_details': json.dumps(DssConfig.threshold_based_nodes),
               'rationale_parent_dict': json.dumps(DssConfig.rationale_parent_dict),
               'rationale_order_dict': json.dumps(DssConfig.rationale_order_dict),
               'rationale_threshold_dict': json.dumps(DssConfig.rationale_threshold_dict),
               'severity_rationale_dict': json.dumps(DssConfig.severity_rationale_dict),
               'rationale_text_potential_yes': json.dumps(output_rationale_text_potential_yes),
               'rationale_text_potential_no': json.dumps(output_rationale_text_potential_no),
               'node_description_dict': json.dumps(DssConfig.node_description_dict),
               'state_label_dict': json.dumps(DssConfig.state_label_dict),
               'table_label_dict': json.dumps(DssConfig.table_label_dict),
               'selected_record': selected_record, 'archived_flag': archived, 'selected_period': selected_period,
               'alert_id': alert_id, 'archived': archived, 'common_name': common_name,
               'comment': comment, 'confirm': confirm}
    # time taken for complete page_rendering module
    end_time = time.time()
    total = end_time-start_time
    logging.info("Inside page_output - Total time taken for page rendering is {} secs".format(total))

    response = render(request, 'DSS/main_page.html', context)
    response['X-Frame-Options'] = 'ALLOW-FROM *'
    return response


# @login_ajax_session
@api_view(['GET', 'POST'])
def extract_datatable_records(request):
    """
    This function is called when 'records' is mentioned in url.
    This request is made from ajax call from datatable, to render the dataTable and provide additional
    functionality like sorting, pagination
    This function handles
    - parameter extraction
    - DB connection
    - renders DataTable
    """
    logging.info('**************Extract records ajax call*************')
    start_time = time.time()

    header = ['product_name', 'pt', 'pec_importance']+DssConfig.input_nodes
    logging.info('Inside extract_datatable_records - Extracting parameters from url')
    # Extracting params from url
    try:
        alert_id = request.GET['alert_id']
        archived = request.GET['archived']
        start = request.GET['start']
        length = request.GET['length']
        search_phrase = request.GET['search[value]']
        # Below parameters are available based on sorting activity(optional)
        sort_col = request.GET.get('order[0][column]')
        sort_dir = request.GET.get('order[0][dir]')

        logging.info("Inside extract_datatable_records - Extracted alert_id,archived flag,start,length,search_phrase,"
                     "sort_col,sort_dir {},{},{},{},{},{},{} using GET "
                     "request".format(alert_id, archived, start, length, search_phrase, sort_col, sort_dir))
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e

    # Preprocessing of search term
    def preprocess_float(a):
        try:
            a = float(a)
            if int(a)==a:
                a = int(a)
        except Exception as e:
            pass
        return str(a)
    search_phrase = search_phrase.lower().replace("'","''").replace('(','\\(').replace(')','\\)').replace('%','\\%').replace('*','\\*')
    search_phrase=preprocess_float(search_phrase)

    logging.info('Inside extract_datatable_records - Extracting data from Application DB')

    table_name = 'AGG_ALERT'
    if archived == 'true': # table mapping based on archived flag
        table_name = 'ARCHIVED_AGG_ALERT'

    query = '''SELECT ID, DSS_SCORE, PRODUCT_NAME, PT, PEC_IMP_NUM_HIGH FROM <table_name> where exec_configuration_id = <alert_id> 
    and dss_score is not null'''
    if search_phrase:
        query = query +\
                ''' and (lower(PRODUCT_NAME) like '%<search_phrase>%' or lower(PT) like 
                '%<search_phrase>%' or PEC_IMP_NUM_HIGH like '%<search_phrase>%') '''
    query = query + ''' <order_phrase> offset <offset> rows fetch next <record_count> rows only'''

    order_phrase = ""
    if sort_col:
        sort_col = int(sort_col)
        if sort_col>=len(header):
            logging.info(f"sort_col: {sort_col}, but only {header} are applicable for sorting")
        elif header[sort_col] == "pec_importance":
            order_phrase = f'''ORDER BY PEC_IMP_NUM_HIGH {sort_dir}'''
        elif header[sort_col] in ['product_name' ,'pt']:
            order_phrase = f'''ORDER BY {header[sort_col]} {sort_dir}'''
        else:
            SORTING_COL_ALIAS =f'''REGEXP_SUBSTR(LOWER(dss_score),'"{header[sort_col]}": "[^""]*"') '''
            order_phrase = f'''ORDER BY cast(substr(substr(LOWER({SORTING_COL_ALIAS}),1,100),1, length(substr(LOWER({SORTING_COL_ALIAS}),1,100))-1) as varchar2(100)) {sort_dir}'''
        

    query = query.replace('<table_name>', table_name)\
    .replace('<search_phrase>', search_phrase)\
    .replace('<order_phrase>', order_phrase)\
    .replace('<alert_id>', alert_id)\
    .replace('<offset>', start)\
    .replace('<record_count>', length)\
    .replace('\n', ' ')

    count_query = f"SELECT COUNT(*) FROM {table_name} where exec_configuration_id = {alert_id} and dss_score is not null"

    filtered_count_query = '''SELECT COUNT(*) FROM <table_name> where exec_configuration_id = <alert_id> and dss_score is not null'''
    if search_phrase:
        filtered_count_query = filtered_count_query +\
                                ''' and (lower(PRODUCT_NAME) like '%<search_phrase>%' or lower(PT) like 
                                '%<search_phrase>%' or PEC_IMP_NUM_HIGH like '%<search_phrase>%') '''

    filtered_count_query = filtered_count_query.replace('<table_name>', table_name) \
        .replace('<search_phrase>', search_phrase) \
        .replace('<alert_id>', alert_id) \
        .replace('\n', ' ')

    try:
        appdb_connection = DBConnection('default')
        # Data extraction from DB
        app_df = appdb_connection.read_table(query)
        total_count = appdb_connection.execute_count(count_query)
        filtered_count = appdb_connection.execute_count(filtered_count_query)

        # converting column name to lower case
        app_df.columns = [column.lower() for column in app_df.columns]
        logging.info('Inside extract_datatable_records - Creating a list from the json extracted from App DB')
        dss_scores_json = []

        if filtered_count==0:
            return Response({'recordsTotal': 0, 'recordsFiltered': 0, 'data': []})

        # handling ROR/PRR configguration settings for old alerts on bayer env
        input_nodes_copy = DssConfig.input_nodes.copy()
        if 'prr' not in json.loads(str(app_df['dss_score'][0])).keys():
            input_nodes_copy.remove('prr')
            input_nodes_copy.append('ror')

        # Preparing dataTable records
        for i in range(app_df.shape[0]):
            json_dss_score = json.loads(str(app_df['dss_score'].iloc[i]))
            temp_list = [json_dss_score['product_name'],
                         json_dss_score['pt'],
                         str(json_dss_score['pec_importance'][0])]
            for nodes in input_nodes_copy:
                temp_list.append(DssConfig.table_label_dict[json_dss_score[nodes]])
            dss_scores_json.append(temp_list)

    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e
    finally:
        if "appdb_connection" in locals():
            appdb_connection.close()

    # time taken for complete extract_datatable_records module
    end_time = time.time()
    total = end_time-start_time
    logging.info("Inside extract_datatable_records - Total time taken for extract_datatable_records module is {} secs".format(total))

    return Response({'recordsTotal': total_count, 'recordsFiltered': filtered_count, 'data': dss_scores_json})


# @login_ajax_session
@api_view(['GET','POST'])
def activities_extract_record(request):
    """
    This function is called when 'activities_record' is mentioned in url.
    This request is made from ajax call from datatable under Activities tab,
    to render the dataTable and provide additional functionality like sorting, pagination
    This function handles
    - parameter extraction
    - DB connection
    - renders DataTable
    """
    logging.info('**************Activities extract records ajax call*************')
    start_time = time.time()
    header = ['activity_type', 'suspect_product', 'event', 'description', 'performed_by', 'time_stamp']

    logging.info('Inside activities_extract_record - Extracting parameters from url')
    # Extracting params from url
    try:
        alert_id = request.GET['alert_id']
        archived = request.GET['archived']
        start = request.GET['start']
        length = request.GET['length']
        search = request.GET['search[value]']
        # Below parameters are available based on sorting activity(optional)
        sort_col = request.GET.get('order[0][column]')
        sort_dir = request.GET.get('order[0][dir]')

        logging.info("Inside activities_extract_record - Extracted alert_id,archived flag,start,length,search,"
                     "sort_col,sort_dir {},{},{},{},{},{},{} using GET "
                     "request".format(alert_id, archived, start, length, search, sort_col, sort_dir))
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e

    # Extracting data from app DB
    logging.info('Inside activities_extract_record - Extracting data from Application DB')
    search = search.lower().replace("'", "''")

    if sort_col is None:
        query = """SELECT ACTIVITY_TYPE,SUSPECT_PRODUCT,EVENT,DESCRIPTION,PERFORMED_BY,
        to_char(cast(TIME_STAMP as timestamp),'DD-Mon-YYYY HH24:MI:SS') as TIME_STAMP from dss_activity_log 
        where EXEC_CONFIGURATION_ID = {} and (LOWER(ACTIVITY_TYPE) like '%{}%'
        or LOWER(SUSPECT_PRODUCT) like '%{}%' or LOWER(EVENT) like '%{}%'
        or LOWER(DESCRIPTION) like '%{}%' or LOWER(PERFORMED_BY) like '%{}%'
        or LOWER(to_char(cast(TIME_STAMP as timestamp),'DD-Mon-YYYY HH24:MI:SS'))  like '%{}%')
        offset {} rows fetch next {} rows 
        only""".format(alert_id, search, search, search, search, search, search,  start, length)
    else:
        sort_col = str(int(sort_col) + 1)
        query = """SELECT ACTIVITY_TYPE,SUSPECT_PRODUCT,EVENT,DESCRIPTION,PERFORMED_BY,
        to_char(cast(TIME_STAMP as timestamp),'DD-Mon-YYYY HH24:MI:SS') as TIME_STAMP1 from dss_activity_log 
        where EXEC_CONFIGURATION_ID = {} and (LOWER(ACTIVITY_TYPE) like '%{}%'
        or LOWER(SUSPECT_PRODUCT) like '%{}%' or LOWER(EVENT) like '%{}%'
        or LOWER(DESCRIPTION) like '%{}%' or LOWER(PERFORMED_BY) like '%{}%'
        or LOWER(to_char(cast(TIME_STAMP as timestamp),'DD-Mon-YYYY HH24:MI:SS'))  like '%{}%') order by {} {} 
        offset {} rows fetch next {} rows 
        only""".format(alert_id, search, search, search, search, search, search, header[int(sort_col)-1], sort_dir, start, length)
    
    count_query = "SELECT COUNT(*) FROM DSS_ACTIVITY_LOG where exec_configuration_id = {} ".format(alert_id)
    filtered_count_query = """SELECT COUNT(*) FROM DSS_ACTIVITY_LOG where EXEC_CONFIGURATION_ID = {}  and (LOWER(ACTIVITY_TYPE) like '%{}%'
                            or LOWER(SUSPECT_PRODUCT) like '%{}%' or LOWER(EVENT) like '%{}%'
                            or LOWER(DESCRIPTION) like '%{}%' or LOWER(PERFORMED_BY) like '%{}%'
                            or LOWER(to_char(cast(TIME_STAMP as timestamp),'DD-Mon-YYYY HH24:MI:SS'))  like '%{}%')  
                            """.format(alert_id, search, search, search, search, search, search)

    
    try:
        appdb_connection = DBConnection('default')
        # Data extraction from DB
        app_df = appdb_connection.read_table(query)
        total_count = appdb_connection.execute_count(count_query)
        filtered_count = appdb_connection.execute_count(filtered_count_query)

        # converting column name to lower case
        app_df.columns = [column.lower() for column in app_df.columns]
        logging.info('Inside activities_extract_record - Creating a list of dss_activity_log table values')
        dss_scores_json = []

        # Preparing dataTable records
        for i in range(app_df.shape[0]):
            temp_list = []
            for column_name in app_df.columns:
                if column_name.lower() == "DESCRIPTION".lower():
                    temp_list.append((app_df[column_name][i].replace('no', 'No').replace('yes', 'Yes'))) # make no & yes strings title-case
                else:
                    temp_list.append((app_df[column_name][i]))
            dss_scores_json.append(temp_list)

    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e
    finally:
        if "appdb_connection" in locals():
            appdb_connection.close()

    # time taken for complete activities_extract_record module
    end_time = time.time()
    total = end_time-start_time
    logging.info("Inside activities_extract_record - Total time taken for activities_extract_record module is {} secs".format(total))

    return Response({'recordsTotal': total_count, 'recordsFiltered': filtered_count, 'data': dss_scores_json})


# @login_ajax_session
@api_view(['GET', 'POST'])
def alert_period(request):
    """
    alert function is called when 'period' is mentioned in url.
    This function is called using ajax, when the drop-down to select the alert period is opened

    Parameters:
    alert_id : exec_configuration_id
    archived : boolean(true or false)

    Output:
    drop_down_item: List with each drop-down element having id and text
    """

    logging.info('************Alert period drop-down element extraction started*********')
    start_time = time.time()

    logging.info('Inside alert_period - Extracting parameters from url')
    # Extracting params from url
    try:
        alert_id = request.GET['alert_id']
        archived = request.GET['archived']
        p = request.GET['p']
        e = request.GET['e']
        logging.info("Inside alert_period - Extracted alert_id,archived flag {},{},{},{} using GET request".format(alert_id, archived,p,e))

    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e

    logging.info('Inside alert_period - Extracting periods based on alert_id and archive flag')


    """
    query = select distinct period_end_date, period_start_date, exec_configuration_id, 'false' as archived_flag from agg_alert
    where alert_configuration_id = (select alert_configuration_id from agg_alert where exec_configuration_id= {} and LOWER(product_name) = '{}' and LOWER(pt) = '{}') and dss_score is not null.format(alert_id,p.lower().replace("'", "''"),e.lower().replace("'", "''"))
    """

    if archived == 'false':
        query = """
            select distinct period_end_date, period_start_date, exec_configuration_id, 'true' as archived_flag from archived_agg_alert where alert_configuration_id = (select alert_configuration_id from agg_alert where exec_configuration_id= """+alert_id+""" fetch next 1 rows only) and dss_score is not null and LOWER(product_name) = '"""+p.lower().replace("'", "''")+"""' and LOWER(pt) = '"""+e.lower().replace("'", "''")+"""'
            UNION
            (select distinct period_end_date, period_start_date, exec_configuration_id, 'false' as archived_flag from agg_alert where exec_configuration_id= """+ alert_id+""" and dss_score is not null and LOWER(product_name) = '"""+p.lower().replace("'", "''")+"""' and LOWER(pt) = '"""+e.lower().replace("'", "''")+"""' fetch next 1 rows only )"""
    else:
        query = """
            select distinct period_end_date, period_start_date, exec_configuration_id, 'false' as archived_flag from agg_alert where alert_configuration_id = (select alert_configuration_id from archived_agg_alert where exec_configuration_id= """+alert_id+""" fetch next 1 rows only) and dss_score is not null and LOWER(product_name) = '"""+p.lower().replace("'", "''")+"""' and LOWER(pt) = '"""+e.lower().replace("'", "''")+"""'
            UNION
            select distinct period_end_date, period_start_date, exec_configuration_id, 'true' as archived_flag from archived_agg_alert where alert_configuration_id = (select alert_configuration_id from archived_agg_alert where exec_configuration_id= """+alert_id +""" fetch next 1 rows only) and dss_score is not null and LOWER(product_name) = '"""+p.lower().replace("'", "''")+"""' and LOWER(pt) = '"""+e.lower().replace("'", "''")+"""'"""

    # Object creation of DBConnection class
    try:
        appdb_connection = DBConnection('default')
        period_df = appdb_connection.read_table(query)
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e
    finally:
        if "appdb_connection" in locals():
            appdb_connection.close()

    logging.info('Inside alert_period - Creating drop-down elements')
    drop_down_item = []
    for index, row in period_df.iterrows():
        if str(row['EXEC_CONFIGURATION_ID']) != str(alert_id):
            formatted_start_date = datetime.strptime(str(datetime.date(period_df['PERIOD_START_DATE'].iloc[index])), ymd_format).strftime(dby_format)
            formatted_end_date = datetime.strptime(str(datetime.date(period_df['PERIOD_END_DATE'].iloc[index])), ymd_format).strftime(dby_format)
            # formatted_end_time = datetime.time(period_df['PERIOD_END_DATE'].iloc[index]).strftime("%H:%M:%S")
            drop_down_item .append({'id': row['EXEC_CONFIGURATION_ID'],
                                    'text': f'{formatted_start_date} to {formatted_end_date}',
                                    'title': row['ARCHIVED_FLAG']})

    """
    To filter based on search term
    """
    if request.GET.get('q'):
        q = request.GET['q']
        drop_down_item = list(filter(lambda drop_down_element: q in drop_down_element['text'], drop_down_item))

    # time taken for complete alert_period module
    end_time = time.time()
    total = end_time-start_time
    logging.info("Inside alert_period - Total time taken for alert_period module is {} secs".format(total))

    return Response({'results': drop_down_item})


# @login_ajax_session
@api_view(['GET', 'POST'])
def extract_record(request):

    """ 
    This function is called on selecting the period from drop-down(#alert_period).
    
    Input parameters:
    alert_id:   exec_configuration_id of the selected alert period
    archived:   archived flag(value stored in drop down elements)
    p:  Product selected (for which network is shown)
    e:  event selected (for which network is shown)
    output :
    selected_records:   DSS_Score column record based on p,e
    nodes:  node list for network graph - This is prepared based on selected product event(p,e)
    edges:  edge list for network graph
    """
    logging.info("**************Inside extract_record**************")
    start_time = time.time()
    logging.info('Inside extract_record - Extracting parameters from url')
    # Extracting params from url
    try:
    
        alert_id = request.GET['alert_id']
        archived = request.GET['archived']
        p = request.GET['p']
        e = request.GET['e']
        
        logging.info("Inside extract_record - Extracted alert_id,archived flag,product and event  {},{},{} and {} using GET request".format(alert_id,archived,p,e))
    
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e

    # Extracting data from app DB
    logging.info('Inside extract_record - Extracting data(json) from Application DB')
    # table mapping based on archived flag
    table_name = 'AGG_ALERT'
    
    # commented for arch change
    if archived == 'true':
        table_name = 'ARCHIVED_AGG_ALERT'
    query = """SELECT DSS_SCORE, DSS_COMMENTS, DSS_CONFIRMATION FROM """+table_name+""" where exec_configuration_id = """+alert_id+""" and LOWER(product_name) = '"""+p.lower().replace("'", "''")+"""' and LOWER(pt) = '"""+e.lower().replace("'", "''")+"""'"""

    logging.info('Inside extract_record - Creating selected_record')
    """Case when p,e is not present"""
    selected_record = ''
    comment = ''
    confirm = ''

    # Object creation of DBConnection class
    try:
        appdb_connection = DBConnection('default')
        record_df = appdb_connection.read_table(query)

        # converting column name to lower case
        record_df.columns = [column.lower() for column in record_df.columns]
        if record_df.shape[0] != 0:
            selected_record = json.loads(str(record_df.dss_score.values[0]))
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        appdb_connection.close()
        raise e
    finally:
        if "appdb_connection" in locals():
            appdb_connection.close()

    logging.info('Inside extract_record - Creating selected_record based on the p,e passed in the url')
    if record_df.shape[0] != 0:
        # comment and confirm for modal review
        comment = record_df.dss_comments.values[0]
        confirm = record_df.dss_confirmation.values[0]
        if comment is None:
            comment = ''
        if confirm is None:
            confirm = ''
 
        logging.info("Inside extract_record - Creating node_list and edge_list for vis.js graph based on "
                     "selected product,event")
        vis_node_list = []
        for node in model.name_to_id:
            if node in DssConfig.input_nodes:
                vis_node_list.append({'id': node,
                                      'label': DssConfig.node_label_dict[node]+'\n' +
                                               DssConfig.state_label_dict[(selected_record[node.lower()])]})
            else:
                vis_node_list.append({'id': node, 'label': DssConfig.node_label_dict[node]})
        vis_edge_list = model.edges

    else:
        logging.info("Inside extract_record - Creating node_list and edge_list for vis.js graph if product and event "
                     "is not present for selected period")
        vis_node_list = []
        for node in model.name_to_id:
            vis_node_list.append({'id': node, 'label': DssConfig.node_label_dict[node]})
        vis_edge_list = model.edges

    # time taken for complete extract_record module
    end_time = time.time()
    total = end_time-start_time
    logging.info("Inside extract_record - Total time taken for extract_record module is {} secs".format(total))

    return Response({'selected_record': selected_record,
                     'nodes': vis_node_list, 'edges': vis_edge_list, 'comment': comment, 'confirm': confirm})


# @login_ajax_session
@api_view(['GET', 'POST'])
def extract_selected_record(request):
    """ 
    This function is called on dataTable row click event. The details related to the clicked
    product-event are returned.
    Input parameters:
    alert_id:   exec_configuration_id of the selected alert period
    archived:   archived flag(value stored in drop down elements)
    p:  Product selected (for which network is shown)
    e:  event selected (for which network is shown)

    output - 
    selected_records:   DSS_Score column record based on p,e
    """
    logging.info("**************Inside extract_selected_record**************")
    start_time = time.time()
    logging.info('Inside extract_selected_record - Extracting parameters from url')
    # Extracting params from url
    try:
        alert_id = request.GET['alert_id']
        archived = request.GET['archived']
        p = request.GET['p']
        e = request.GET['e']

        logging.info("Inside extract_selected_record -  Extracted alert_id,archived flag,product and "
                     "event  {},{},{} and {} using GET request".format(alert_id, archived, p, e))
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e
    
    # Extracting data from app DB
    logging.info('Inside extract_selected_record - Extracting data(json) from Application DB')
    # table mapping based on archived flag
    table_name = 'AGG_ALERT'
    
    # comment for 4.7 arch change
    if archived == 'true':
        table_name = 'ARCHIVED_AGG_ALERT'

    query = """SELECT DSS_SCORE, DSS_COMMENTS, DSS_CONFIRMATION FROM """+table_name+""" where exec_configuration_id = """+alert_id+""" and LOWER(product_name) = '"""+p.lower().replace("'", "''")+"""' and LOWER(pt) = '"""+e.lower().replace("'", "''")+"""'"""

    try:
        appdb_connection = DBConnection('default')
        # Object creation of DBConnection class
        record_df = appdb_connection.read_table(query)

        record_df.columns = [column.lower() for column in record_df.columns]
        logging.info('Inside extract_selected_record - Creating selected_record')
        selected_record = json.loads(str(record_df.dss_score.values[0]))
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e
    finally:
        if "appdb_connection" in locals():
            appdb_connection.close()

    # comment and confirm for modal review
    comment = record_df.dss_comments.values[0]
    confirm = record_df.dss_confirmation.values[0]
    if comment is None:
        comment = ''
    if confirm is None:
        confirm = ''

    # time taken for complete extract_selected_record module
    end_time = time.time()
    total = end_time-start_time
    logging.info("Inside extract_selected_record - Total time taken for extract_selected_record module is {} secs".format(total))

    return Response({'selected_record': selected_record, 'comment': comment, 'confirm': confirm})


class ManualSubmission(APIView):
    """
    This function is called on on clicking the submit button in Evidence form.
    It extracts the manual nodes values selected in checkbox and calculates score for the selected product event
    Input parameters:
    request body
        check_box values - has yes/no values for all the nodes (request.POST.get('manual_node_name') would give yes/no)
        record - has selected_record i.e a json having product_name, pt , evidence values, non_terminal scores
    output -
    selected_records:   New DSS Score calculated based on manual nodes
    form_submitted_node: All the manual nodes which were present in request body(selected checkbox)
    """

    def __init__(self):
        super(ManualSubmission, self).__init__()

    # @method_decorator(login_ajax_session, name='post')
    def post(self, request):
        
        logging.info("**************Inside form ManualSubmission**************")
        start_time = time.time()
        data = json.loads(request.body)
        record = json.loads(data['record'])
        evidence_dict = {}

        logging.info("Inside ManualSubmission - Creating evidence list based on form submission")
        for node in DssConfig.input_nodes:
            if node in record.keys():
                evidence_dict[node] = record[node]
        for node in DssConfig.manual_nodes:

            if node in data.keys():
                evidence_dict[node] = data[node]
            else:
                evidence_dict.pop(node,None)
                record.pop(node,None)
        
        logging.info("Inside ManualSubmission - Score calculation and record output")
        result = model.get_inference(evidence_dict)

        output_record = record
        output_record.update(evidence_dict)
        # add scores for non terminal nodes
        for node in model.non_terminal_nodes:
            output_record[node] = [round(_*100, 1) for _ in result[node]]

        # time taken for complete ManualSubmission module
        end_time = time.time()
        total = end_time-start_time
        logging.info("Inside ManualSubmission - Total time taken for ManualSubmission module is {} secs".format(total))

        return Response({'selected_record': output_record, 'form_submitted_node': evidence_dict.keys()})


class DssReview(APIView):

    """
    This is called on form submission as an ajax request. Called when save button is clicked on confirmation modal
    to stored the comments and confirmation in backend tables.
    Input:
    alert_id
    archived - helps to choose table agg_alert or archived agg_alert
    p - product name
    e - event name
    confirm - user provided confirmation(yes/no) - to be stored in DSS_CONFIRMATION
    comments - user comments - to be stored in DSS_COMMENTS

    Output:
    Data is stored in respective table in DSS_COMMENTS,DSS_CONFIRMATION COLUMNS.
    In case of error, Internal server error alert is shown in UI
    """
    def __init__(self):
        super(DssReview, self).__init__()

    def execute_update_query(self, query):
        appdb_connection = DBConnection('default')
        try:
            appdb_connection.execute_query(query)
        except Exception as e:
            logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e
        finally:
            appdb_connection.close()

    # @method_decorator(login_ajax_session, name='post')
    def post(self, request):
        
        logging.info("**************Inside DSS Confirmation ajax call**************")
        start_time = time.time()
        logging.info("Inside DssReview - Extracting parameter")
        try:
            data = json.loads(request.body)
            
            p = data['p']
            e = data['e']
            archived = data['archived']
            confirm = data['confirm']
            comment = data['comment']
            alert_id = str(data['alert_id'])
            common_name = str(data['common_name'])
        except Exception as e:
            logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

        if confirm is None:
            confirm = ''

        table_name = 'AGG_ALERT'

        # comment for 4.7 architeture
        if archived == 'true':
            table_name = 'ARCHIVED_AGG_ALERT'

        logging.info("Inside DssReview - Extracting the comments and confirmation stored in "
                     "agg_alert/archived_agg_alert table")
        extract_query = "SELECT DSS_COMMENTS,DSS_CONFIRMATION,ALERT_CONFIGURATION_ID FROM "+table_name+" where EXEC_CONFIGURATION_ID = "+alert_id+" and LOWER(product_name) = '"+p.lower().replace("'", "''")+"' and LOWER(pt) = '"+e.lower().replace("'", "''")+"'"
        appdb_connection = DBConnection('default')
        try:
            record = appdb_connection.read_table(extract_query)
        except Exception as e:
            logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e
        finally:
            appdb_connection.close()

        record.columns = [column.lower() for column in record.columns]
        db_stored_comment = record.dss_comments.values[0]
        db_stored_confirm = record.dss_confirmation.values[0]
        alert_conf_id = record.alert_configuration_id.values[0]

        if db_stored_comment is None:
            db_stored_comment = ''
        if db_stored_confirm is None:
            db_stored_confirm = ''

        logging.info("Inserting the changes in DSS activity log table")
        comment_description = ''
        confirm_description = ''

        if db_stored_comment != comment:
            comment_description = "Assessment comments updated from '" + db_stored_comment + "' to '" + comment + "'"
            if db_stored_confirm == '':
                comment_description = 'Assessment comments added - ' + comment
        if db_stored_confirm != confirm:
            confirm_description = "Confirmation updated from '" + db_stored_confirm + "' to '" + confirm + "'"
            if db_stored_confirm == '':
                confirm_description = 'Confirmation added - ' + confirm

        confirm_insert_query = """INSERT INTO DSS_ACTIVITY_LOG(ACTIVITY_TYPE, SUSPECT_PRODUCT, EVENT, DESCRIPTION, PERFORMED_BY,
                          EXEC_CONFIGURATION_ID, ALERT_CONFIGURATION_ID, PREVIOUS_VALUE, CURRENT_VALUE,time_stamp)
                          VALUES('Confirmation updated', '{}','{}','{}','{}', {},'{}','{}',
                          '{}',to_date('{}','yyyy-mm-dd hh24:mi:ss'))""".format(p.replace("'", "''"), e.replace("'", "''"),
                                         confirm_description.replace("'", "''"), common_name.replace("'", "''"),
                                         alert_id, alert_conf_id,
                                         db_stored_confirm.replace("'", "''"), confirm.replace("'", "''"),datetime.now().strftime("%Y-%m-%d %H:%M:%S"))

        comment_insert_query = """INSERT INTO DSS_ACTIVITY_LOG(ACTIVITY_TYPE, SUSPECT_PRODUCT, EVENT, DESCRIPTION, PERFORMED_BY,
                          EXEC_CONFIGURATION_ID, ALERT_CONFIGURATION_ID, PREVIOUS_VALUE, CURRENT_VALUE,time_stamp)
                          VALUES('Assessment comments updated', '{}','{}','{}','{}', {},'{}','{}',
                          '{}',to_date('{}','yyyy-mm-dd hh24:mi:ss'))""".format(p.replace("'", "''"), e.replace("'", "''"),
                                         comment_description.replace("'", "''"), common_name.replace("'", "''"),
                                         alert_id, alert_conf_id,
                                         db_stored_comment.replace("'", "''"), comment.replace("'", "''"),datetime.now().strftime("%Y-%m-%d %H:%M:%S"))

        try:
            appdb_connection = DBConnection('default')
            if db_stored_confirm != confirm:
                appdb_connection.execute_query(confirm_insert_query)
            if db_stored_comment != comment:
                appdb_connection.execute_query(comment_insert_query)
        except Exception as e:
            logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e
        finally:
            if "appdb_connection" in locals():
                appdb_connection.close()

        logging.info("Inside DssReview - Updating DSS_CONFIRMATION/DSS_COMMENTS columns in table "+table_name)
        update_query = "UPDATE "+table_name+" set DSS_COMMENTS = '"+comment.replace("'", "''")+"',DSS_CONFIRMATION = '"+confirm +"' where EXEC_CONFIGURATION_ID = "+alert_id+" and LOWER(product_name) = '"+p.lower()+"' and LOWER(pt) = '"+e.lower()+"'"
        self.execute_update_query(update_query)

        # time taken for complete DSSReview module
        end_time = time.time()
        total = end_time-start_time
        logging.info("Inside DssReview - Total time taken for DSSReview module is {} secs".format(total))

        return Response({'data': 'success'})


class DetailedHistory(APIView):

    """
    For displaying history in modal
    """
    def __init__(self):
        super(DetailedHistory, self).__init__()

    # @method_decorator(login_ajax_session, name='post')
    def post(self, request):
        
        logging.info("**************Inside DetailedHistory ajax call**************")
        start_time = time.time()
        logging.info("Inside DetailedHistory - Extracting parameter")
        try:
            data = json.loads(request.body)
            
            p = data['p']
            e = data['e']
            archived = data['archived']
            alert_id = str(data['alert_id'])
            
        except Exception as e:
            logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

        table_name = 'AGG_ALERT'

        # comment for demo architecture(4.7)
        if archived == 'true':
            table_name = 'ARCHIVED_AGG_ALERT'

        # 5.0 logic - Extracting previous records
        query = """
        select DSS_SCORE,period_end_date,period_start_date  from
            (select DSS_SCORE,period_end_date,period_start_date,date_created from agg_alert 
                where alert_configuration_id = 
                    (select alert_configuration_id from {} 
                    where exec_configuration_id = {} and 
                    lower(product_name) = '{}' and lower(PT) = '{}') 
                and date_created <=(select date_created from {} 
                    where exec_configuration_id = {} and lower(product_name) = '{}' and lower(PT) = '{}')
                and lower(product_name) = '{}' and lower(PT) = '{}' and DSS_SCORE is not null
    
        union all

            (select DSS_SCORE,period_end_date,period_start_date,date_created from archived_agg_alert 
                where alert_configuration_id = (select alert_configuration_id from {} 
                    where exec_configuration_id = {} and lower(product_name) = '{}' and lower(PT) = '{}')
                and date_created <= (select date_created from {} 
                    where exec_configuration_id = {} and lower(product_name) = '{}' and lower(PT) = '{}')
                and lower(product_name) = '{}' and lower(PT) = '{}' and DSS_SCORE is not null
                order by date_created fetch next 5 rows only)
        ) 
        order by date_created desc fetch next 5 rows only

        """.format(table_name, alert_id, p.lower().replace("'", "''"), e.lower().replace("'", "''"),
                    table_name, alert_id, p.lower().replace("'", "''"), e.lower().replace("'", "''"),
                    p.lower().replace("'", "''"), e.lower().replace("'", "''"),
                    table_name, alert_id, p.lower().replace("'", "''"), e.lower().replace("'", "''"),
                    table_name, alert_id, p.lower().replace("'", "''"), e.lower().replace("'", "''"),
                    p.lower().replace("'", "''"), e.lower().replace("'", "''")
                )

        """ 4.7 Same table archived query
        query = 
        select DSS_SCORE,period_end_date,period_start_date from {} 
                where alert_configuration_id = 
                    (select alert_configuration_id from {} 
                    where exec_configuration_id = {} and 
                    lower(product_name) = '{}' and lower(PT) = '{}') 
                and date_created <=(select date_created from {} 
                    where exec_configuration_id = {} and lower(product_name) = '{}' and lower(PT) = '{}')
                and lower(product_name) = '{}' and lower(PT) = '{}' and DSS_SCORE is not null
        order by date_created desc fetch next 5 rows only

        .format(table_name, table_name, alert_id, p.lower().replace("'", "''"), e.lower().replace("'", "''"),
                    table_name, alert_id, p.lower().replace("'", "''"), e.lower().replace("'", "''"),
                    p.lower().replace("'", "''"), e.lower().replace("'", "''")
                )
        """

        # Object creation of db_connection class
        
        try:
            appdb_connection = DBConnection('default')
            logging.info("Inside DetailedHistory - Extracting data from application DB")
            record_df = appdb_connection.read_table(query)

            # converting column name to lower case
            record_df.columns = [column.lower() for column in record_df.columns]
            
            # creating a dict of period and dss_score column
            output_period_dict = {}
            for i in range(record_df.shape[0]):
                key = datetime.strptime(str(datetime.date(record_df['period_start_date'].iloc[i])), ymd_format).strftime(dby_format) + \
                      " - " + \
                      datetime.strptime(str(datetime.date(record_df['period_end_date'].iloc[i])), ymd_format).strftime(dby_format)
                output_period_dict[key] = json.loads(str(record_df.dss_score.iloc[i]))
            
        except Exception as e:
            logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            return Response({'Message': "Exception type:{}, Exception value:{} occurred while "
                                        "extracting data from application db.".format(type(e), e)},
                            status=status.HTTP_400_BAD_REQUEST)
        finally:
            if "appdb_connection" in locals():
                appdb_connection.close()
        # time taken for complete DSSReview module
        end_time = time.time()
        total = end_time-start_time
        logging.info("Inside DetailedHistory - Total time taken for DetailedHistory module is {} secs".format(total))
        return Response({'data': output_period_dict})


# This function provides functionality to access dictionary by keys in Django template
@register.filter
def get_item(dictionary, key):
    return dictionary.get(key)
