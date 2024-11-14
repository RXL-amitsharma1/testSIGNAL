from django.apps import AppConfig
from django.conf import settings
import os
import logging
import pandas as pd
from datetime import datetime
from .db_extraction import DBConnection
import sys
default_error_message = "Inside app.py - An error occurred on line {}. Exception type: {}, Exception: {} " \
                                "occurred while parallel processing."

class DssConfig(AppConfig):
    """
        Configuring project level variables:
        network - VariableElimination object based on concept.bif file
        terminal nodes - all the independent variables in network for which evidences would be provided
        non-terminal nodes - dependent nodes in network
        num_partitions - setting number of partitions for multiprocessing

        Following variables are configured from node_labels.csv,edge_labels.csv:
        node_label_dict - Labels of nodes
        node_description_dict - Description of nodes to be displayed on node click event
        state_label_dict - Labels of node states
        table_label_dict - Labels of table states
        input_nodes - nodes for which evidence would be provided from DB
        manual_nodes - nodes for which evidence can be selected from form
    """
    try:
        name = 'DSS'
        logging.info("Application configuration: Setting path of all configurable files")
        # Storing path of node and state label csv files
        node_label_file = os.path.join(settings.CONFIG_FILES, 'node_labels.csv')
        state_label_file = os.path.join(settings.CONFIG_FILES, 'state_labels.csv')
        rationale_table_structure = os.path.join(settings.CONFIG_FILES, 'rationale_table_structure.csv')
        mart_query_table_structure = os.path.join(settings.CONFIG_FILES, 'mart_table_data.csv')
        severity_rationale_dict = os.path.join(settings.CONFIG_FILES, 'severity_rationale_dict.csv')
        dss_threshold_file = os.path.join(settings.CONFIG_FILES, 'dss_threshold.csv')

        # Creating dict for node and state labels
        logging.info("Application configuration: Reading all configurable files")
        node_df = pd.read_csv(node_label_file, na_filter=False)
        state_df = pd.read_csv(state_label_file, na_filter=False)
        rationale_df = pd.read_csv(rationale_table_structure, na_filter=False)
        mart_table_df = pd.read_csv(mart_query_table_structure, na_filter=False, quotechar='"')
        severity_incr_rationale_df = pd.read_csv(severity_rationale_dict, na_filter=False)
        dss_threshold_df = pd.read_csv(dss_threshold_file, na_filter=False)

        logging.info("Application configuration: Creating dictionary of node and state labels")
        node_label_dict = dict(zip(node_df.node_id, node_df.node_label))
        label_node_dict = dict(zip(node_df.node_label, node_df.node_id))
        node_description_dict = dict(zip(node_df.node_id, node_df.description))
        state_label_dict = dict(zip(state_df.State, state_df.Labels))
        table_label_dict = dict(zip(state_df.State, state_df.Table_labels))
        dss_threshold_dict = dict(zip(dss_threshold_df.color, dss_threshold_df.value))

        logging.info("Application configuration: Creating dictionary to store relationship in rationale table")
        rationale_parent_dict = dict(zip(rationale_df.node_id, rationale_df.parent))
        rationale_order_dict = dict(zip(rationale_df.node_id, rationale_df.order))
        rationale_threshold_dict = dict(zip(rationale_df.node_id, rationale_df.potential_threshold))
        rationale_text_potential_yes = dict(zip(rationale_df.node_id, rationale_df.rationale_text_potential_yes))
        rationale_text_potential_no = dict(zip(rationale_df.node_id, rationale_df.rationale_text_potential_no))

        # creating dict for severity increase rationale
        severity_rationale_dict = dict(zip(severity_incr_rationale_df.severity_node, severity_incr_rationale_df.text))

        # Initializing terminal and non-terminal nodes list
        logging.info('Application configuration: Initializing terminal and non_terminal_nodes list.')
        terminal_nodes = []
        non_terminal_nodes = []
        node_effect = {}
        for node in list(node_df.node_id):
            if node_df[node_df['node_id'] == node]['position'].values[0] == 'Terminal':
                terminal_nodes.append(node)
            elif node_df[node_df['node_id'] == node]['position'].values[0] == 'Non_terminal':
                non_terminal_nodes.append(node)
            if node_df[node_df['node_id'] == node]['effect'].values[0] == 'Direct':
                node_effect[node] = 'direct'
            elif node_df[node_df['node_id'] == node]['effect'].values[0] == 'Inverse':
                node_effect[node] = 'inverse'

        # input_nodes and manual nodes are configured based on load_process column in node_labels.csv file
        # node_effect - direct or inverse on pec importance
        manual_nodes = []
        input_nodes = []
        threshold_based_nodes = {}
        for node in terminal_nodes:
            if node_df[node_df['node_id'] == node]['load_process'].values[0] == 'M':
                manual_nodes.append(node)
            elif node_df[node_df['node_id'] == node]['load_process'].values[0] == 'A':
                input_nodes.append(node)
            else:
                input_nodes.append(node)
                manual_nodes.append(node)

            if node_df[node_df['node_id'] == node]['threshold_key'].values[0] != '':
                threshold_based_nodes[node] = [[x.strip() for x in node_df[node_df['node_id'] == node]['threshold_key'].values[0].split(',')], [x.strip() for x in node_df[node_df['node_id'] == node]['threshold_value'].values[0].split(',')]]

        # creating select query from configurable csv file to extract evidenced values from mart DB
        logging.info("Application configuration: Creating evidence extraction query from mart")
        mart_evidence_extraction_query = 'SELECT '+mart_table_df['alias'][0].strip()+'.BASE_ID, '+mart_table_df['alias'][0].strip()+'.MEDDRA_PT_CODE, '
        for i in range(len(mart_table_df['columns'])):
            for _ in mart_table_df['columns'][i].split(' , '):
                mart_evidence_extraction_query = mart_evidence_extraction_query + _.strip() + ', '

        mart_evidence_extraction_query = mart_evidence_extraction_query[:-2] + ' from ' + mart_table_df['Table'][0].strip()+' '+ mart_table_df['alias'][0].strip()
        for i in range(1, len(mart_table_df['Table'])):
            mart_evidence_extraction_query = mart_evidence_extraction_query + ( ' left join '+ mart_table_df['Table'][i].strip() + ' ' + mart_table_df['alias'][i].strip() + \
                                                                                ' on ' + mart_table_df['alias'][0].strip()+'.BASE_ID = ' +  mart_table_df['alias'][i].strip()+'.BASE_ID and ' + mart_table_df['alias'][0].strip()+'.MEDDRA_PT_CODE = ' \
                                                                                + mart_table_df['alias'][i].strip()+'.MEDDRA_PT_CODE' )

        mart_evidence_extraction_query = (mart_evidence_extraction_query.replace("’", "'"))

        smq_mart_evidence_extraction_query = 'SELECT '+mart_table_df['alias'][0].strip()+'.BASE_ID, '+mart_table_df['alias'][0].strip()+'.MEDDRA_PT_CODE, '+mart_table_df['alias'][0].strip()+'.TERM_SCOPE, '
        for i in range(len(mart_table_df['columns'])):
            for _ in mart_table_df['columns'][i].split(' , '):
                smq_mart_evidence_extraction_query = smq_mart_evidence_extraction_query + _.strip() + ', '

        smq_mart_evidence_extraction_query = smq_mart_evidence_extraction_query[:-2] + ' from ' + mart_table_df['Table'][0].strip()+' '+ mart_table_df['alias'][0].strip()
        for i in range(1, len(mart_table_df['Table'])):
            smq_mart_evidence_extraction_query = smq_mart_evidence_extraction_query + ( ' left join '+ mart_table_df['Table'][i].strip() + ' ' + mart_table_df['alias'][i].strip() + \
                                                                                        ' on ' + mart_table_df['alias'][0].strip()+'.BASE_ID = ' +  mart_table_df['alias'][i].strip()+'.BASE_ID and ' + mart_table_df['alias'][0].strip()+'.MEDDRA_PT_CODE = ' \
                                                                                        + mart_table_df['alias'][i].strip()+'.MEDDRA_PT_CODE and ' +mart_table_df['alias'][0].strip()+'.TERM_SCOPE = ' + mart_table_df['alias'][i].strip()+'.TERM_SCOPE')

        smq_mart_evidence_extraction_query = (smq_mart_evidence_extraction_query.replace("’", "'"))

        # boolean keys which would be considered displaying rationale text for severity increase.
        boolean_severity = ['new_serious', 'new_serious_death', 'new_serious_threat', 'new_serious_med_imp', 'new_serious_int_req', 'new_serious_hosp', 'new_serious_prlg_host', 'new_serious_disbale', 'new_serious_cong_anom']
        # percentage value keys which would be considered displaying rationale text for severity increase.
        percent_severity = ['percent_serious', 'percent_serious_death', 'percent_serious_threat', 'percent_serious_med_imp', 'percent_serious_int_req', 'percent_serious_hosp', 'percent_serious_prlg_host', 'percent_serious_disbale', 'percent_serious_cong_anom']

        # Extracting DSS severity threshold configured in DB
        severity_threshold_query = "select pvs_value from pvs_constants where PVS_KEY = 'DSS_SEVERITY_THRESSHOLD'"
        martdb_connection = DBConnection('pva')
        severity_incr_threshold = int(martdb_connection.execute_count(severity_threshold_query))

        mart_boolean_columns =['evid_case_type', 'evid_countries', 'ime', 'dme', 'children', 'elderly', 'rechallenge', 'dechallenge', 'time_to_onset', 'severity_incr']
        mart_int_columns = ['cnt_country', 'cnt_source_type', 'cnt_child', 'cnt_elderly', 'cnt_rechal', 'cnt_dechal', 'cnt_tto', 'new_serious_death', 'new_serious_threat', 'new_serious_int_req', 'new_serious_med_imp', 'new_serious_hosp', 'new_serious_prlg_host', 'new_serious_disbale', 'new_serious_cong_anom', 'percent_serious_death', 'percent_serious', 'percent_serious_threat', 'percent_serious_int_req', 'percent_serious_med_imp', 'percent_serious_hosp', 'percent_serious_disbale', 'percent_serious_cong_anom', 'percent_serious_prlg_host']
        logging.info(f"CORES_PER_ALERT: {settings.CORES_PER_ALERT}")
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e
    finally:
        if "martdb_connection"in locals():
            martdb_connection.close()