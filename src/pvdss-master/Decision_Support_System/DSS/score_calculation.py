from .apps import DssConfig
import pandas as pd
import numpy as np
import json
import logging
import re
import sys
from django.conf import settings
from .dss_network import Model
from .db_extraction import fetch_prev_data, get_next_n_chunks, trend_write_wrapper
from functools import partial
import multiprocessing as mp
import time

value_regex = r'=([a-zA-Z0-9_]+)'
cores_per_alert = min(settings.CORES_PER_ALERT, settings.CPU)
partition_size = settings.PARTITION_SIZE
default_error_message = "Inside score_calculation.py - An error occurred on line {}. Exception type: {}, Exception: {} " \
                                "occurred while parallel processing."

logger = logging.getLogger()
class NpEncoder(json.JSONEncoder):
    """Encoder used to convert numpy data type inside json.dumps function"""
    def default(self, obj):
        if isinstance(obj, np.integer):
            return int(obj)
        elif isinstance(obj, np.floating):
            return float(obj)
        elif isinstance(obj, np.ndarray):
            return obj.tolist()
        else:
            return super(NpEncoder, self).default(obj)


def get_trend_value(row, is_eb05_valid, eb05_threshold_for_trend, \
                  percent_eb05_incr_threshold, other_nodes_thresholds):
    try:
        conds = []
        # checking for eb05
        if is_eb05_valid:
            conds.append(row['eb05_score']>=eb05_threshold_for_trend and \
                        row['percent_eb05_incr']>=percent_eb05_incr_threshold)
        # checking for threshold nodes for trend
        conds.extend([ True if row[_key]>=_threshold else False \
                        for _key, _threshold in other_nodes_thresholds.items()])

        return 'yes' if any(conds)==True else 'no'
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def trend_calculation(df_input):
    """
        trend_calculation checks whether there is an increasing trend as compared to previous period.
        It computes percentage increase of new count, fatal count, serious count and eb05 score.
        At present it is configured to check just for serious count and eb05 score
        
        Current condition for trend to be yes: 
        (eb05 score >= 2 and percentage increase in eb05 score >=50) or 
        (percentage increase in serious count >=50)

        We can configure it in node_labels.csv file
    """
    try:
        df_input['percent_count_incr'] = df_input.apply(lambda row: round(((row['new_count'] - row['prev_new_count']) * 100 / (row['prev_new_count'])), 2) if row['prev_new_count'] != 0 else ((row['new_count'] - row['prev_new_count']) * 100 / 1), axis=1)
        df_input['percent_fatal_count_incr'] = df_input.apply(lambda row: round(((row['new_fatal_count'] - row['prev_fatal_count']) * 100 / (row['prev_fatal_count'])),2) if row['prev_fatal_count'] != 0 else ((row['new_fatal_count'] - row['prev_fatal_count']) * 100 / 1), axis=1)
        df_input['percent_serious_count_incr'] = df_input.apply(lambda row: round(((row['new_serious_count'] - row['prev_serious_count']) * 100 / (row['prev_serious_count'])), 2) if row['prev_serious_count'] != 0 else ((row['new_serious_count'] - row['prev_serious_count']) * 100 / 1), axis=1)
        df_input['percent_eb05_incr'] = df_input.apply(lambda row: round(((row['eb05_score'] - row['prev_eb05_score']) * 100 / (row['prev_eb05_score'])), 2) if row['prev_eb05_score'] != 0 else ((row['eb05_score'] - row['prev_eb05_score']) * 100 / 1), axis=1)

        # Calculating trend based on criteria specified in node_labels.csv file
        node = 'trend'
        eb05_threshold_for_trend = None
        percent_eb05_incr_threshold = None
        other_nodes_thresholds = {}
        for i, _key in enumerate(DssConfig.threshold_based_nodes[node][0]):
            if _key == "eb05_score":
                eb05_threshold_for_trend = float(DssConfig.threshold_based_nodes[node][1][i])
            elif _key == "percent_eb05_incr":
                percent_eb05_incr_threshold = float(DssConfig.threshold_based_nodes[node][1][i])
            else:
                other_nodes_thresholds[_key] = float(DssConfig.threshold_based_nodes[node][1][i])

        is_eb05_valid = eb05_threshold_for_trend is not None and percent_eb05_incr_threshold is not None
        if not is_eb05_valid:
            logging.info(f"eb05 not valid for trend calculation.")

        df_input[node] = df_input.apply(lambda row: get_trend_value(row, is_eb05_valid, eb05_threshold_for_trend, \
                                                                    percent_eb05_incr_threshold,
                                                                    other_nodes_thresholds),
                                        axis=1)
        return df_input
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e

def initiate_trend_calculation(df):
    try:
        df_no_trend = df[df.prev_new_count.isna()]
        df_trend = df[~df.prev_new_count.isna()]

        df_no_trend['trend'] = 'no'
        df_no_trend['prev_new_count'] = 'New'
        df_no_trend['prev_fatal_count'] = 'New'
        df_no_trend['prev_serious_count'] = 'New'
        df_no_trend['prev_eb05_score'] = 'New'
        del df
        if df_trend.shape[0] > 0:
            df_trend = trend_calculation(df_trend)
        return pd.concat([df_trend, df_no_trend])
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e

def create_evidence_dict(df_row):
    """Creates the evidence dict in the format required by pgmpy network"""
    try:
        evidence_dict = {}

        # creating input node and evidence mapping
        for node in DssConfig.input_nodes:
            if df_row[node.lower()].lower() in ['yes', 'no']:
                evidence_dict[node] = df_row[node.lower()].lower()
            else:
                evidence_dict[node] = 'no'
        return evidence_dict
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def create_json_object_node_info(df_row, result):
    try:
        json_object = df_row.to_dict()

        # add scores for non terminal nodes
        for node in DssConfig.non_terminal_nodes:
            json_object[node] = [round(_ * 100, 1) for _ in result[node]]

        # updating input nodes having error
        for node in DssConfig.input_nodes:
            if df_row[node.lower()].lower() in ['yes', 'no']:
                json_object[node] = df_row[node.lower()].lower()
            else:
                json_object[node] = 'no'
        return json_object
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def potential_signal_calc_terminal_nodes(json_object, node):
    try:
        # Called from calculate_score function - checks whether a terminal node is potential signal for modal display
        if (json_object[node].lower() == 'yes' and DssConfig.node_effect[node].lower() == 'direct') or \
                (json_object[node].lower() == 'no' and DssConfig.node_effect[node].lower() == 'inverse'):
            return 'Yes'
        else:
            return 'No'
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def potential_signal_calc_nonterminal_nodes(json_object, node):
    try:
        # Called from calculate_score function - checks whether a non terminal node is potential signal for modal display
        if (json_object[node][0] >= float(DssConfig.rationale_threshold_dict[node]) and (
                DssConfig.node_effect[node].lower()) == 'direct') or \
                (json_object[node][0] <= float(DssConfig.rationale_threshold_dict[node]) and (
                        DssConfig.node_effect[node].lower() == 'inverse')):
            return 'Yes'
        else:
            return 'No'
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def potential_signal_calc(json_object, node):
    # calculates whether its a potential signal for application side modal display
    try:

        # Based on inverse or direct relationship, we are deciding whether its a potential signal
        if type(json_object[node]) != list:
            return potential_signal_calc_terminal_nodes(json_object, node)
        elif type(json_object[node]) == list:
            return potential_signal_calc_nonterminal_nodes(json_object, node)
        else:
            return None

    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        return None


def score_confidence_calc(json_object, node):
    try:
        # Setting score confidence from DSS score using direct or inverse relationship
        if type(json_object[node]) == list:

            if (json_object[node][0] >= float(DssConfig.rationale_threshold_dict[node]) and DssConfig.node_effect[
                node] == 'direct') or \
                    (json_object[node][0] >= float(DssConfig.rationale_threshold_dict[node]) and DssConfig.node_effect[
                        node] == 'inverse'):
                return str(json_object[node][0]).rstrip('0').rstrip('.') + '%'
            else:
                return str(json_object[node][1]).rstrip('0').rstrip('.') + '%'
        else:
            return None
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        return None    


def rationale_creation_potential_no(json_object, node):
    try:
        regex_match = re.findall(value_regex, DssConfig.rationale_text_potential_no[node])
        if regex_match:
            temp_str = DssConfig.rationale_text_potential_no[node].replace('=', '')
            for i in regex_match:
                if str(json_object[i]) == 'New':
                    temp_str = re.sub(r',.*' + i + ".*", '', temp_str)
                else:
                    temp_str = temp_str.replace(i, str(float(json_object[i])).rstrip('0').rstrip('.'), 1)

            return temp_str.replace('\n', '<br>')
        else:
            return DssConfig.rationale_text_potential_no[node].replace('\n', '<br>')
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def rationale_creation_potential_yes(json_object, node):
    try:
        regex_match = re.findall(value_regex, DssConfig.rationale_text_potential_yes[node])

        if regex_match:
            temp_str = DssConfig.rationale_text_potential_yes[node].replace('=', '')
            for i in regex_match:
                if str(json_object[i]) == 'New':
                    temp_str = re.sub(r',.*' + i + ".*", '', temp_str)
                else:
                    temp_str = temp_str.replace(i, str(float(json_object[i])).rstrip('0').rstrip('.'), 1)
            rationale = temp_str.replace('\n', '<br>')
        else:
            rationale = DssConfig.rationale_text_potential_yes[node].replace('\n', '<br>')

        # Explicity handling rationale for severity increase
        if node == 'severity_incr':
            rationale = rationale_creation_serverity_increase(json_object, rationale)
        return rationale
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def rationale_creation_serverity_increase_percent_params(indx, percent_params, json_object):
    # Setup of percentage rationale in severity increase
    try:
        regex_match = re.findall(value_regex,
                                 DssConfig.severity_rationale_dict[DssConfig.percent_severity[indx]])

        if regex_match:
            temp_str = DssConfig.severity_rationale_dict[DssConfig.percent_severity[indx]].replace('=', '')
            for i in regex_match:
                if str(json_object[i]) == 'New':
                    temp_str = re.sub(r',.*' + i + ".*", '', temp_str)
                else:
                    temp_str = temp_str.replace(i, str(float(json_object[i])).rstrip('0').rstrip('.'), 1)

            percent_params = percent_params + temp_str + ', '
        else:
            percent_params = percent_params + DssConfig.severity_rationale_dict[
                DssConfig.percent_severity[indx]] + ', '
        return percent_params
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def rationale_creation_serverity_increase(json_object, rationale):
    # Incase of severity increase the rationale could change based on the condition being used
    try:
        boolean_params = 'New seriousness type observed: <br>'
        percent_params = 'Increase in existing seriousness: <br>'
        for i in range(len(DssConfig.boolean_severity)):
            if int(json_object[DssConfig.boolean_severity[i]]) == 1:
                boolean_params = boolean_params + DssConfig.severity_rationale_dict[
                    DssConfig.boolean_severity[i]] + ', '
        if boolean_params != 'New seriousness type observed: <br>':
            rationale = rationale.replace('\n', '<br>') + '<br>' + boolean_params[:-2].replace('\n','<br>')

        for indx in range(len(DssConfig.percent_severity)):
            if float(json_object[DssConfig.percent_severity[indx]]) >= DssConfig.severity_incr_threshold:
                percent_params = rationale_creation_serverity_increase_percent_params(indx, percent_params, json_object)
        if percent_params != 'Increase in existing seriousness: <br>':
            rationale = rationale.replace('\n', '<br>') + '<br>' + percent_params[:-2].replace('\n', '<br>')
        return rationale
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def rationale_creation(temp_dict, json_object, node):
    # Function used to create rationale column of modal
    try:
        if temp_dict['potential_signal'].lower() == 'yes':
            return rationale_creation_potential_yes(json_object, node)
        else:
            return rationale_creation_potential_no(json_object, node)

    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        return None


def parent_mapping(node):
    # This function is used to set the parent in modal
    try:
        if DssConfig.rationale_parent_dict[node] == '':
            return None
        else:
            return DssConfig.node_label_dict[DssConfig.rationale_parent_dict[node]]
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        return None


def ror_flag_update(temp_dict, json_object, node):
    # This function is used to map ROR/IROR mapping
    try:
        if node.lower() == 'ror' and json_object['ror_flag'] == 1:
            if temp_dict['pv_concept'] is not None and 'iROR' not in temp_dict['pv_concept']:
                temp_dict['pv_concept'] = temp_dict['pv_concept'].replace('ROR', 'iROR').replace('ror', 'iROR')
            if temp_dict['rationale'] is not None and 'iROR' not in temp_dict['rationale']:
                temp_dict['rationale'] = temp_dict['rationale'].replace('ROR', 'iROR').replace('ror', 'iROR')
            if temp_dict['parent'] is not None and 'iROR' not in temp_dict['parent']:
                temp_dict['parent'] = temp_dict['parent'].replace('ROR', 'iROR ').replace('ror', 'iROR')
        return temp_dict
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def calculate_score(df_input, is_master, part_no):

    """
    calculate_score function is called on each partition using multiprocessing pool.
    - Creates evidence and input nodes mapping
    - Calculates score for non-terminal nodes
    - Creates rationale to display in modal
    - Returns a df with column having score, evidence and rationale text in json format
    """
    try:
        model = Model(DssConfig, remove_nodes=settings.DEACTIVATED_NODES, print_logs=True if part_no==0 else False)

        grpby = df_input.groupby(by=model.terminal_nodes)
        n_groups = grpby.ngroups
        use_group_logic = False
        if n_groups < df_input.shape[0] * 0.95:
            use_group_logic = True
            res = []
            for grp in grpby.groups:
                df_grp = grpby.get_group(grp).copy()
                df_grp.loc[:, "score"] = json.dumps(model.get_inference(df_grp.iloc[0]), cls=NpEncoder)
                res.append(df_grp)
            df_input = pd.concat(res)#.sort_index()
        del grpby

        # logging.info(f"df_input shape: {df_input.shape}")
        if df_input.empty:
            if is_master == 0:
                df = pd.DataFrame(columns=['BASE_ID', 'PRODUCT_NAME', 'MEDDRA_PT_CODE', 'PT', 'DSS_SCORE', 'PEC_IMP_POS', 'PEC_IMP_NEG'])
            else:
                df = pd.DataFrame(columns=['CHILD_EXECUTION_ID', 'BASE_ID', 'PRODUCT_NAME', 'MEDDRA_PT_CODE', 'PT', 'DSS_SCORE', 'PEC_IMP_POS', 'PEC_IMP_NEG'])
            return df

        logging.info(f"Using use_group_logic: {use_group_logic}, for part_no: {part_no}, ngroups:{n_groups}, df_input: {df_input.shape}")

        df = pd.DataFrame.from_records(df_input.apply(lambda x: calculate_score_single_row(model, x, is_master, use_group_logic), axis=1).reset_index(drop=True))
        del df_input

        df.DSS_SCORE = df.DSS_SCORE.apply(lambda x: x.replace("'", r"\u0027"))
        return df
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def calculate_score_single_row(model, row, is_master, use_group_logic):
    try:
        if use_group_logic:
            result = json.loads(row["score"])
            row.drop("score", inplace=True)
        else:
            result = model.get_inference(row)

        # store node info in json_object for file creation
        json_object = create_json_object_node_info(row, result)

        # creating data for details tab(rationale) - for app side display
        """ 
        We are creating a list of dictionary, were each dictionary has rationale details for a node
        Nodes are picked from rationale_table_structure.csv file
        """
        details_list = []
        for node in DssConfig.rationale_parent_dict.keys():
            temp_dict = {}
            # Add node label in pv_concept key
            temp_dict['pv_concept'] = DssConfig.node_label_dict[node]
            temp_dict['potential_signal'] = potential_signal_calc(json_object, node)
            temp_dict['score_confidence'] = score_confidence_calc(json_object, node)
            # Creating rationale text based on yes/no value from potential_signal
            # Pattern with =* is replace with actual values present
            temp_dict['rationale'] = rationale_creation(temp_dict, json_object, node)
            temp_dict['parent'] = parent_mapping(node)
            temp_dict = ror_flag_update(temp_dict, json_object, node)

            details_list.append(temp_dict)

        json_object['rationale_details'] = details_list

        if is_master == 0:
            return_row = {'BASE_ID': row['base_id'],
                          'PRODUCT_NAME': row['product_name'],
                          'MEDDRA_PT_CODE': row['meddra_pt_code'],
                          'PT': row['pt'],
                          'DSS_SCORE': json.dumps(json_object, cls=NpEncoder).replace("'", r"\u0027"),
                          'PEC_IMP_POS': round(result['pec_importance'][0] * 100, 1),
                          'PEC_IMP_NEG': round(result['pec_importance'][1] * 100, 1),
                        }
        else:
            return_row = {'CHILD_EXECUTION_ID' : row['child_execution_id'],
                          'BASE_ID': row['base_id'],
                          'PRODUCT_NAME': row['product_name'],
                          'MEDDRA_PT_CODE': row['meddra_pt_code'],
                          'PT': row['pt'],
                          'DSS_SCORE': json.dumps(json_object, cls=NpEncoder).replace("'", r"\u0027"),
                          'PEC_IMP_POS': round(result['pec_importance'][0] * 100, 1),
                          'PEC_IMP_NEG': round(result['pec_importance'][1] * 100, 1),
                        }
        return return_row
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def threshold_based_calculation(df, DssConfig):
    try:
        # logging.info('Calculating evidence based on threshold')
        for node in DssConfig.threshold_based_nodes.keys():
            try:
                if node == 'trend':
                    continue
                df[node] = df.apply(lambda row: 'yes' if all([True if float(
                    row[DssConfig.threshold_based_nodes[node][0][i]]) >= float(
                    DssConfig.threshold_based_nodes[node][1][i]) \
                                                                  else False for i in range(
                    len(DssConfig.threshold_based_nodes[node][0]))]) else 'no', axis=1)
            except Exception as e:
                logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
                df[node] = 'no'

        df.fillna('New', inplace=True)
        return df
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def mp_write(function, dbc, table, chunks):
    try:
        row_counts = 0
        func = partial(function, dbc, table)
        pool = mp.Pool(settings.CORES_PER_ALERT)
        while True:
            df_iters, _row_count = get_next_n_chunks(chunks, settings.CORES_PER_ALERT)
            if len(df_iters):
                row_counts += _row_count
                pool.map(func, df_iters)
            else:
                break
        pool.close()
        return row_counts
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def trend_data_etl(prev_alert_ids, dbc, exec_id, is_master):
    try:
        logging.info(f'Extracting Previous alert data for {prev_alert_ids[:20]}... exec_ids')
        chunks = fetch_prev_data(prev_alert_ids, is_master, database='default', mode='chunks')
        table = f'pvs_dss_prev_data_{exec_id}'
        logging.info(f'Writing Previous alert data into mart for {prev_alert_ids[:20]}... exec_ids')
        row_count = mp_write(trend_write_wrapper, dbc, table, chunks)
        logging.info(f'Trend data populated to mart {prev_alert_ids[:20]}... exec_ids')
        return row_count
    except Exception as e:
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def error_callback(x):
    logging.info(x)


# import csv
def calculate_score_and_save(alert_id, is_master, trend_flag, dbc, data_chunk):
    try:
        _t1 = time.time()
        part_no, df = data_chunk
        df.columns = [column.lower() for column in df.columns]
        if trend_flag == 0:
            for col in settings.TREND_COLUMNS:
                df[col.lower()] = None

        df = initiate_trend_calculation(df)
        df = threshold_based_calculation(df, DssConfig)
        df = calculate_score(df, is_master, part_no)

        _t2 = time.time()
        # df.to_csv(f"/home/rohit/MYDRIVE/workspace/dss_space/debug/new_scores/df_{part_no}.csv", index=False, sep="|", quoting=csv.QUOTE_NONE)
        table = f"pvs_dss_results_{alert_id}"
        dbc.create_write_table(df, table, chunksize=settings.ALERT_ROWS)
        _t3 = time.time()

        logging.info(f'DSS scores calculated took {round((_t2-_t1)/60, 3)}min and uploaded into DB took {round((_t3-_t2)/60, 3)}min for {part_no}th chunk with {df.shape} shape')
    except Exception as e:
        logging.info(f'DSS scores calculation failed for part number : {part_no}')
        logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


