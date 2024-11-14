import copy
import pandas as pd
import sys
from logs import get_logger
logger = get_logger()
default_error_message = "Inside highlight_json_preparation.py - An error occured on line {}. Exception type: {} , Exception: {} "


def highlight_json_prep(df_extracted_ent, df_event, df_prod, df_test, df_mh, df_pd, df_parent_mh, df_parent_pd,
                        pvi_json):
    try:
        dic_ent_json = {}
        sent_json = {}
        dict_pat_json = {}
        # pat_sent_json = {}
        dict_entity = pvi_json['entitylocation']["entities"]["0"]

        df_sent = df_extracted_ent.drop_duplicates(subset=['sent_id'], keep='first')
        for i in range(0, len(df_sent)):
            sent_id = int(df_sent.iloc[i]["sent_id"])
            sent_json[sent_id] = df_sent.iloc[i]["sentence"].strip()

        # handling patient, study ,reporter entities
        df_pat_ent = df_extracted_ent.loc[df_extracted_ent.entity_label.isin(
            ['PAT_AGE', 'PAT_GENDER', 'PARENT_GENDER', 'PARENT_LMP', 'PREGNANCY', 'PAT_HEIGHT', 'PAT_WEIGHT', 'ETHNICITY',
            'REPORTER_OCCUPATION', 'REPORTER_COUNTRY', 'DEATH_DATE', 'AUTOPSY_DONE', 'AUTOPSY_RESULT', 'DEATH_CAUSE',
            'LAB_TEST_NAME', 'STUDY_NUM', 'STUDY_TYPE', 'SOURCE_TYPE', 'PAT_LMP', 'HOSP_START', 'HOSP_END'])]

        if len(df_pat_ent) > 0:
            for i in range(0, len(df_pat_ent)):
                current_entity = copy.deepcopy(dict_entity)
                view_id = df_pat_ent.iloc[i]["view_id"]
                current_entity["entity_label"] = df_pat_ent.iloc[i]["entity_label"]
                current_entity["sent_id"] = df_pat_ent.iloc[i]["sent_id"]
                current_entity["start"] = int(df_pat_ent.iloc[i]["start"])
                current_entity["end"] = int(df_pat_ent.iloc[i]["end"])
                current_entity["entity_text"] = df_pat_ent.iloc[i]["sentence"].strip()[
                                                current_entity["start"]: current_entity["end"]]
                dict_pat_json.update({view_id: current_entity})
                # pat_sent_json[view_id] = df_pat_ent.iloc[i]["sentence"]
            dic_ent_json.update(dict_pat_json)
            # sent_json.update(pat_sent_json)

        # handling event entities

        dict_event_json = {}
        event_sent_json = {}
        if len(df_event) > 0:
            df_event_ent = pd.DataFrame(columns=['sent_id', 'entity_label', 'entity_text', 'start', 'end'])
            # prepare dataframe containing all values received after entity linking
            df_desc_ent = df_event.loc[:, ['sent_id', 'DESC_REPTD', 'start', 'end']]
            df_desc_ent = df_desc_ent.loc[(df_desc_ent.DESC_REPTD != "")]
            df_desc_ent['entity_label'] = ['DESC_REPTD'] * len(df_desc_ent)
            df_desc_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_event_ent = df_event_ent.append(df_desc_ent)

            df_out_ent = df_event.loc[:, ['sent_id', 'EVENT_OUTCOME', 'EVENT_OUTCOME_start', 'EVENT_OUTCOME_end']]
            df_out_ent = df_out_ent.loc[(df_out_ent.EVENT_OUTCOME != "")]
            df_out_ent['entity_label'] = ['EVENT_OUTCOME'] * len(df_out_ent)
            df_out_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_event_ent = df_event_ent.append(df_out_ent)

            df_seriousness_ent = df_event.loc[:,
                                ['sent_id', 'EVENT_SERIOUSSNESS', 'EVENT_SERIOUSSNESS_start', 'EVENT_SERIOUSSNESS_end']]
            df_seriousness_ent = df_seriousness_ent.loc[(df_seriousness_ent.EVENT_SERIOUSSNESS != "")]
            df_seriousness_ent['entity_label'] = ['EVENT_SERIOUSSNESS'] * len(df_seriousness_ent)
            df_seriousness_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_event_ent = df_event_ent.append(df_seriousness_ent)

            df_onset_ent = df_event.loc[:, ['sent_id', 'ONSET', 'ONSET_start', 'ONSET_end']]
            df_onset_ent = df_onset_ent.loc[(df_onset_ent.ONSET != "")]
            df_onset_ent['entity_label'] = ['ONSET'] * len(df_onset_ent)
            df_onset_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_event_ent = df_event_ent.append(df_onset_ent)

            df_stop_date_ent = df_event.loc[:, ['sent_id', 'EVENT_END_DATE', 'EVENT_END_DATE_start', 'EVENT_END_DATE_end']]
            df_stop_date_ent = df_stop_date_ent.loc[(df_stop_date_ent.EVENT_END_DATE != "")]
            df_stop_date_ent['entity_label'] = ['EVENT_END_DATE'] * len(df_stop_date_ent)
            df_stop_date_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_event_ent = df_event_ent.append(df_stop_date_ent)

            df_stop_date_ent = df_event.loc[:, ['sent_id', 'EVENT_END_DATE', 'EVENT_END_DATE_start', 'EVENT_END_DATE_end']]
            df_stop_date_ent = df_stop_date_ent.loc[(df_stop_date_ent.EVENT_END_DATE != "")]
            df_stop_date_ent['entity_label'] = ['EVENT_END_DATE'] * len(df_stop_date_ent)
            df_stop_date_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_event_ent = df_event_ent.append(df_stop_date_ent)

            df_duration_ent = df_event.loc[:, ['sent_id', 'EVENT_DURATION', 'EVENT_DURATION_start', 'EVENT_DURATION_end']]
            df_duration_ent = df_duration_ent.loc[(df_duration_ent.EVENT_DURATION != "")]
            df_duration_ent['entity_label'] = ['EVENT_DURATION'] * len(df_duration_ent)
            df_duration_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_event_ent = df_event_ent.append(df_duration_ent)

            df_onset_ent = df_event.loc[:, ['sent_id', 'ONSET', 'ONSET_start', 'ONSET_end']]
            df_onset_ent = df_onset_ent.loc[(df_onset_ent.ONSET != "")]
            df_onset_ent['entity_label'] = ['ONSET'] * len(df_onset_ent)
            df_onset_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_event_ent = df_event_ent.append(df_onset_ent)
            """
            df_hosp_st_ent = df_event.loc[:,['sent_id','HOSP_START','HOSP_START_start','HOSP_START_end']]
            print(type(df_hosp_st_ent.HOSP_START))
            df_hosp_st_ent= df_hosp_st_ent.loc[(df_hosp_st_ent.HOSP_START != "")]
            df_hosp_st_ent['entity_label'] = ['HOSP_START']*len(df_hosp_st_ent)
            df_hosp_st_ent.columns =['sent_id','entity_text','start','end','entity_label']
            df_event_ent = df_event_ent.append(df_hosp_st_ent)

            df_hosp_end_ent = df_event.loc[:,['sent_id','HOSP_END','HOSP_END_start','HOSP_END_end']]
            df_hosp_end_ent= df_hosp_end_ent.loc[(df_hosp_st_ent.HOSP_END != "")]
            df_hosp_end_ent['entity_label'] = ['HOSP_END']*len(df_hosp_st_ent)
            df_hosp_end_ent.columns =['sent_id','entity_text','start','end','entity_label']
            df_event_ent = df_event_ent.append(df_hosp_end_ent)
            """
            df_event_ent = df_event_ent[['sent_id', 'entity_label', 'entity_text', 'start', 'end']]
            df_event_ent[['start', 'end']] = df_event_ent[['start', 'end']].astype(int)

            df_event_entities = df_event_ent.merge(df_extracted_ent.drop_duplicates(),
                                                on=['sent_id', 'entity_label', 'entity_text', 'start', 'end'],
                                                how='left')

            if len(df_event_entities) > 0:
                list_of_event_name = []
                for i in range(0, len(df_event_entities)):

                    current_entity = copy.deepcopy(dict_entity)
                    view_id = df_event_entities.iloc[i]["view_id"]
                    current_entity["entity_label"] = df_event_entities.iloc[i]["entity_label"]
                    current_entity["sent_id"] = df_event_entities.iloc[i]["sent_id"]
                    current_entity["start"] = int(df_event_entities.iloc[i]["start"])
                    current_entity["end"] = int(df_event_entities.iloc[i]["end"])
                    current_entity["entity_text"] = df_event_entities.iloc[i]["sentence"].strip()[
                                                    current_entity["start"]: current_entity["end"]]
                    if "1" in ["1" if current_entity["entity_text"] in ele else "0" for ele in list_of_event_name]:
                        continue
                    list_of_event_name.append(current_entity["entity_text"])
                    dict_event_json.update({view_id: current_entity})
                    # event_sent_json[view_id] = df_event_entities.iloc[i]["sentence"]

                dic_ent_json.update(dict_event_json)
                # sent_json.update(event_sent_json)

        # handling test entities
        dict_test_json = {}
        if len(df_test) > 0:
            df_test_ent = pd.DataFrame(columns=['sent_id', 'entity_label', 'entity_text', 'start', 'end'])
            # prepare dataframe containing all values received after entity linking
            df_name_ent = df_test.loc[:, ['sent_id', 'LAB_TEST_NAME', 'start', 'end']]
            df_name_ent = df_name_ent.loc[(df_name_ent.LAB_TEST_NAME != "")]
            df_name_ent['entity_label'] = ['LAB_TEST_NAME'] * len(df_name_ent)
            df_name_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_test_ent = df_test_ent.append(df_test_ent)

            df_result_ent = df_test.loc[:, ['sent_id', 'TEST_RESULT', 'TEST_RESULT_start', 'TEST_RESULT_end']]
            df_result_ent = df_result_ent.loc[(df_result_ent.TEST_RESULT != "")]
            df_result_ent['entity_label'] = ['TEST_RESULT'] * len(df_result_ent)
            df_result_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_test_ent = df_test_ent.append(df_result_ent)

            df_date_ent = df_test.loc[:, ['sent_id', 'TEST_DATE', 'TEST_DATE_start', 'TEST_DATE_end']]
            df_date_ent = df_date_ent.loc[(df_date_ent.TEST_DATE != "")]
            df_date_ent['entity_label'] = ['TEST_DATE'] * len(df_date_ent)
            df_date_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_test_ent = df_test_ent.append(df_date_ent)

            df_test_ent = df_test_ent[['sent_id', 'entity_label', 'entity_text', 'start', 'end']]
            df_test_ent[['start', 'end']] = df_test_ent[['start', 'end']].astype(int)
            df_test_entities = df_test_ent.merge(df_extracted_ent.drop_duplicates(),
                                                on=['sent_id', 'entity_label', 'entity_text', 'start', 'end'],
                                                how='left')

            if len(df_test_entities) > 0:
                for i in range(0, len(df_test_entities)):
                    current_entity = copy.deepcopy(dict_entity)
                    view_id = df_test_entities.iloc[i]["view_id"]
                    current_entity["entity_label"] = df_test_entities.iloc[i]["entity_label"]
                    current_entity["sent_id"] = df_test_entities.iloc[i]["sent_id"]
                    current_entity["start"] = int(df_test_entities.iloc[i]["start"])
                    current_entity["end"] = int(df_test_entities.iloc[i]["end"])
                    current_entity["entity_text"] = df_test_entities.iloc[i]["sentence"].strip()[
                                                    current_entity["start"]: current_entity["end"]]
                    dict_test_json.update({view_id: current_entity})
                    # event_sent_json[view_id] = df_event_entities.iloc[i]["sentence"]

                dic_ent_json.update(dict_event_json)

        # handling prod entities
        dict_prod_json = {}
        prod_sent_json = {}

        if len(df_prod) > 0:
            df_prod_ent = pd.DataFrame(columns=['sent_id', 'entity_label', 'entity_text', 'start', 'end'])

            df_prodname_ent = df_prod.loc[:, ['sent_id', 'PRODUCT_NAME', 'start', 'end']]
            df_prodname_ent = df_prodname_ent.loc[(df_prodname_ent.PRODUCT_NAME != "")]
            df_prodname_ent['entity_label'] = ['PRODUCT_NAME'] * len(df_prodname_ent)
            df_prodname_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_prod_ent = df_prod_ent.append(df_prodname_ent)

            df_dose_ent = df_prod.loc[:, ['sent_id', 'DOSE', 'DOSE_start', 'DOSE_end']]
            df_dose_ent = df_dose_ent.loc[(df_dose_ent.DOSE != "")]
            df_dose_ent['entity_label'] = ['DOSE'] * len(df_dose_ent)
            df_dose_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_prod_ent = df_prod_ent.append(df_dose_ent)

            df_malfunctions = df_extracted_ent[df_extracted_ent.entity_label == "MALFUNCTIONS"]
            if len(df_malfunctions) > 0:
                df_malfunctions = df_malfunctions.reset_index(drop=True)
                df_malfunction_ent = df_malfunctions.loc[0, ['sent_id', 'entity_text', 'start', 'end']]
                df_malfunction_ent['entity_label'] = 'MALFUNCTIONS'
                df_malfunction_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
                df_prod_ent = df_prod_ent.append(df_malfunction_ent)

            df_freq_ent = df_prod.loc[:, ['sent_id', 'FREQ', 'FREQ_start', 'FREQ_end']]
            df_freq_ent = df_freq_ent.loc[(df_freq_ent.FREQ != "")]
            df_freq_ent['entity_label'] = ['FREQ'] * len(df_freq_ent)
            df_freq_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_prod_ent = df_prod_ent.append(df_freq_ent)

            df_route_ent = df_prod.loc[:, ['sent_id', 'ROUTE', 'ROUTE_start', 'ROUTE_end']]
            df_route_ent = df_route_ent.loc[(df_route_ent.ROUTE != "")]
            df_route_ent['entity_label'] = ['ROUTE'] * len(df_route_ent)
            df_route_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_prod_ent = df_prod_ent.append(df_route_ent)

            df_form_ent = df_prod.loc[:, ['sent_id', 'FORMULATION', 'FORMULATION_start', 'FORMULATION_end']]
            df_form_ent = df_form_ent.loc[(df_form_ent.FORMULATION != "")]
            df_form_ent['entity_label'] = ['FORMULATION'] * len(df_form_ent)
            df_form_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_prod_ent = df_prod_ent.append(df_form_ent)

            df_dose_st = df_prod.loc[:, ['sent_id', 'DOSE_START_DATE', 'DOSE_START_DATE_start', 'DOSE_START_DATE_end']]
            df_dose_st = df_dose_st.loc[(df_dose_st.DOSE_START_DATE != "")]
            df_dose_st['entity_label'] = ['DOSE_START_DATE'] * len(df_dose_st)
            df_dose_st.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_prod_ent = df_prod_ent.append(df_dose_st)

            df_dose_stop = df_prod.loc[:, ['sent_id', 'DOSE_STOP_DATE', 'DOSE_STOP_DATE_start', 'DOSE_STOP_DATE_end']]
            df_dose_stop = df_dose_stop.loc[(df_dose_stop.DOSE_STOP_DATE != "")]
            df_dose_stop['entity_label'] = ['DOSE_STOP_DATE'] * len(df_dose_stop)
            df_dose_stop.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_prod_ent = df_prod_ent.append(df_dose_stop)

            df_dur_ent = df_prod.loc[:, ['sent_id', 'DOSE_DURATION', 'DOSE_DURATION_start', 'DOSE_DURATION_end']]
            df_dur_ent = df_dur_ent.loc[(df_dur_ent.DOSE_DURATION != "")]
            df_dur_ent['entity_label'] = ['DOSE_DURATION'] * len(df_dur_ent)
            df_dur_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_prod_ent = df_prod_ent.append(df_dur_ent)

            df_action_ent = df_prod.loc[:, ['sent_id', 'ACTION_TAKEN', 'ACTION_TAKEN_start', 'ACTION_TAKEN_end']]
            df_action_ent = df_action_ent.loc[(df_action_ent.ACTION_TAKEN != "")]
            df_action_ent['entity_label'] = ['ACTION_TAKEN'] * len(df_action_ent)
            df_action_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_prod_ent = df_prod_ent.append(df_action_ent)

            df_batch_ent = df_prod.loc[:,
                        ['sent_id', 'DOSE_BATCH_LOT_NUM', 'DOSE_BATCH_LOT_NUM_start', 'DOSE_BATCH_LOT_NUM_end']]
            df_batch_ent = df_batch_ent.loc[(df_batch_ent.DOSE_BATCH_LOT_NUM != "")]
            df_batch_ent['entity_label'] = ['DOSE_BATCH_LOT_NUM'] * len(df_batch_ent)
            df_batch_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_prod_ent = df_prod_ent.append(df_batch_ent)

            df_indication_ent = df_prod.loc[:,
                                ['sent_id', 'PRODUCT_INDICATIONS', 'PRODUCT_INDICATIONS_start', 'PRODUCT_INDICATIONS_end']]
            df_indication_ent = df_indication_ent.loc[(df_indication_ent.PRODUCT_INDICATIONS != "")]
            df_indication_ent['entity_label'] = ['PRODUCT_INDICATIONS'] * len(df_indication_ent)
            df_indication_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_prod_ent = df_prod_ent.append(df_indication_ent)

            df_prod_ent = df_prod_ent[['sent_id', 'entity_label', 'entity_text', 'start', 'end']]
            df_prod_ent[['start', 'end']] = df_prod_ent[['start', 'end']].astype(int)
            df_prod_entities = df_prod_ent.merge(df_extracted_ent.drop_duplicates(),
                                                on=['sent_id', 'entity_label', 'entity_text', 'start', 'end'],
                                                how='left')

            if len(df_prod_entities) > 0:
                for i in range(0, len(df_prod_entities)):
                    current_entity = copy.deepcopy(dict_entity)
                    view_id = df_prod_entities.iloc[i]["view_id"]
                    current_entity["entity_label"] = df_prod_entities.iloc[i]["entity_label"]
                    current_entity["sent_id"] = df_prod_entities.iloc[i]["sent_id"]
                    current_entity["start"] = int(df_prod_entities.iloc[i]["start"])
                    current_entity["end"] = int(df_prod_entities.iloc[i]["end"])
                    current_entity["entity_text"] = df_prod_entities.iloc[i]["sentence"].strip()[
                                                    current_entity["start"]: current_entity["end"]]
                    dict_prod_json.update({view_id: current_entity})
                    # prod_sent_json[view_id] = df_prod_entities.iloc[i]["sentence"]

                dic_ent_json.update(dict_prod_json)
                # sent_json.update(prod_sent_json)
        # handling medical history entities
        dict_mh_json = {}
        if len(df_mh) > 0:
            df_mh_ent = pd.DataFrame(columns=['sent_id', 'entity_label', 'entity_text', 'start', 'end'])
            # prepare dataframe containing all values received after entity linking
            df_mh_name_ent = df_mh.loc[:, ['sent_id', 'MEDICAL_HISTORY', 'start', 'end']]
            df_mh_name_ent = df_mh_name_ent.loc[(df_mh_name_ent.MEDICAL_HISTORY != "")]
            df_mh_name_ent['entity_label'] = ['MEDICAL_HISTORY'] * len(df_mh_name_ent)
            df_mh_name_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_mh_ent = df_mh_ent.append(df_mh_name_ent)

            df_mh_start_ent = df_mh.loc[:, ['sent_id', 'MC_START_DATE', 'MC_START_DATE_start', 'MC_START_DATE_end']]
            df_mh_start_ent = df_mh_start_ent.loc[(df_mh_start_ent.MC_START_DATE != "")]
            df_mh_start_ent['entity_label'] = ['MC_START_DATE'] * len(df_mh_start_ent)
            df_mh_start_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_mh_ent = df_mh_ent.append(df_mh_start_ent)

            df_mh_end_ent = df_mh.loc[:, ['sent_id', 'MC_END_DATE', 'MC_END_DATE_start', 'MC_END_DATE_end']]
            df_mh_end_ent = df_mh_end_ent.loc[(df_mh_end_ent.MC_END_DATE != "")]
            df_mh_end_ent['entity_label'] = ['MC_END_DATE'] * len(df_mh_end_ent)
            df_mh_end_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_mh_ent = df_mh_ent.append(df_mh_end_ent)

            df_mh_ent = df_mh_ent[['sent_id', 'entity_label', 'entity_text', 'start', 'end']]
            df_mh_ent[['start', 'end']] = df_mh_ent[['start', 'end']].astype(int)
            df_mh_entities = df_mh_ent.merge(df_extracted_ent.drop_duplicates(),
                                            on=['sent_id', 'entity_label', 'entity_text', 'start', 'end'],
                                            how='left')

            if len(df_mh_entities) > 0:
                for i in range(0, len(df_mh_entities)):
                    current_entity = copy.deepcopy(dict_entity)
                    view_id = df_mh_entities.iloc[i]["view_id"]
                    current_entity["entity_label"] = df_mh_entities.iloc[i]["entity_label"]
                    current_entity["sent_id"] = df_mh_entities.iloc[i]["sent_id"]
                    current_entity["start"] = int(df_mh_entities.iloc[i]["start"])
                    current_entity["end"] = int(df_mh_entities.iloc[i]["end"])
                    current_entity["entity_text"] = df_mh_entities.iloc[i]["sentence"].strip()[
                                                    current_entity["start"]: current_entity["end"]]
                    dict_mh_json.update({view_id: current_entity})
                    # event_sent_json[view_id] = df_event_entities.iloc[i]["sentence"]

                dic_ent_json.update(dict_mh_json)
        # handling parent medical history entities
        """commenting parent medical and drug history highlighting code"""
        """
        dict_parent_mh_json={}
        if(len(df_parent_mh) > 0):
            df_parent_mh_ent = pd.DataFrame(columns = ['sent_id','entity_label','entity_text','start','end'])
            #prepare dataframe containing all values received after entity linking
            df_mh_name_ent = df_parent_mh.loc[:,['sent_id','PARENT_MED_HIST','start','end']]
            df_mh_name_ent = df_mh_name_ent.loc[(df_mh_name_ent.PARENT_MED_HIST != "")]
            df_mh_name_ent['entity_label'] = ['PARENT_MED_HIST']*len(df_mh_name_ent)
            df_mh_name_ent.columns =['sent_id','entity_text','start','end','entity_label']
            df_parent_mh_ent = df_parent_mh_ent.append(df_mh_name_ent)

            df_parent_mh_ent = df_parent_mh_ent[['sent_id','entity_label','entity_text','start','end']]
            df_parent_mh_ent[['start', 'end']] = df_parent_mh_ent[['start', 'end']].astype(int)
            df_parent_mh_entities = df_parent_mh_ent.merge(df_extracted_ent.drop_duplicates(), on=['sent_id','entity_label','entity_text','start','end'],
                            how='left')

            if(len(df_parent_mh_entities) > 0):
                for i in range(0, len(df_parent_mh_entities)):
                    current_entity =  copy.deepcopy(dict_entity)
                    view_id = df_parent_mh_entities.iloc[i]["view_id"]
                    current_entity["entity_label"] = df_parent_mh_entities.iloc[i]["entity_label"]
                    current_entity["sent_id"] = df_parent_mh_entities.iloc[i]["sent_id"]
                    current_entity["start"] = df_parent_mh_entities.iloc[i]["start"]
                    current_entity["end"] = df_parent_mh_entities.iloc[i]["end"]
                    current_entity["entity_text"] = df_parent_mh_entities.iloc[i]["sentence"].strip()[current_entity["start"] : current_entity["end"] ]
                    dict_parent_mh_json.update({view_id : current_entity})
                dic_ent_json.update(dict_parent_mh_json)
        """
        # handling past drug entities
        dict_pd_json = {}
        if len(df_pd) > 0:
            df_pd_ent = pd.DataFrame(columns=['sent_id', 'entity_label', 'entity_text', 'start', 'end'])
            # prepare dataframe containing all values received after entity linking
            df_pd_name_ent = df_pd.loc[:, ['sent_id', 'PAT_PAST_DRUG', 'start', 'end']]
            df_pd_name_ent = df_pd_name_ent.loc[(df_pd_name_ent.PAT_PAST_DRUG != "")]
            df_pd_name_ent['entity_label'] = ['PAT_PAST_DRUG'] * len(df_pd_name_ent)
            df_pd_name_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_pd_ent = df_pd_ent.append(df_pd_name_ent)

            df_pd_start_ent = df_pd.loc[:, ['sent_id', 'PAST_DRUG_START', 'PAST_DRUG_START_start', 'PAST_DRUG_START_end']]
            df_pd_start_ent = df_pd_start_ent.loc[(df_pd_start_ent.PAST_DRUG_START != "")]
            df_pd_start_ent['entity_label'] = ['PAST_DRUG_START'] * len(df_pd_start_ent)
            df_pd_start_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_pd_ent = df_pd_ent.append(df_pd_start_ent)

            df_pd_end_ent = df_pd.loc[:, ['sent_id', 'PAST_DRUG_END', 'PAST_DRUG_END_start', 'PAST_DRUG_END_end']]
            df_pd_end_ent = df_pd_end_ent.loc[(df_pd_end_ent.PAST_DRUG_END != "")]
            df_pd_end_ent['entity_label'] = ['PAST_DRUG_END'] * len(df_pd_end_ent)
            df_pd_end_ent.columns = ['sent_id', 'entity_text', 'start', 'end', 'entity_label']
            df_pd_ent = df_pd_ent.append(df_pd_end_ent)

            df_pd_ent = df_pd_ent[['sent_id', 'entity_label', 'entity_text', 'start', 'end']]
            df_pd_ent[['start', 'end']] = df_pd_ent[['start', 'end']].astype(int)
            df_pd_entities = df_pd_ent.merge(df_extracted_ent.drop_duplicates(),
                                            on=['sent_id', 'entity_label', 'entity_text', 'start', 'end'],
                                            how='left')
            if len(df_pd_entities) > 0:
                for i in range(0, len(df_pd_entities)):
                    current_entity = copy.deepcopy(dict_entity)
                    view_id = df_pd_entities.iloc[i]["view_id"]
                    current_entity["entity_label"] = df_pd_entities.iloc[i]["entity_label"]
                    current_entity["sent_id"] = df_pd_entities.iloc[i]["sent_id"]
                    current_entity["start"] = int(df_pd_entities.iloc[i]["start"])
                    current_entity["end"] = int(df_pd_entities.iloc[i]["end"])
                    current_entity["entity_text"] = df_pd_entities.iloc[i]["sentence"].strip()[
                                                    current_entity["start"]: current_entity["end"]]
                    dict_pd_json.update({view_id: current_entity})
                dic_ent_json.update(dict_pd_json)
        """            
        dict_parent_pd_json={}
        if(len(df_parent_pd) > 0):
            df_parent_pd_ent = pd.DataFrame(columns = ['sent_id','entity_label','entity_text','start','end'])
            #prepare dataframe containing all values received after entity linking
            df_pd_name_ent = df_parent_pd.loc[:,['sent_id','PARENT_DRUG_HIST','start','end']]
            df_pd_name_ent = df_pd_name_ent.loc[(df_pd_name_ent.PARENT_DRUG_HIST != "")]
            df_pd_name_ent['entity_label'] = ['PARENT_DRUG_HIST']*len(df_pd_name_ent)
            df_pd_name_ent.columns =['sent_id','entity_text','start','end','entity_label']
            df_parent_pd_ent = df_parent_pd_ent.append(df_pd_name_ent)

            df_parent_pd_ent = df_parent_pd_ent[['sent_id','entity_label','entity_text','start','end']]
            df_parent_pd_ent[['start', 'end']] = df_parent_pd_ent[['start', 'end']].astype(int)
            df_parent_pd_entities = df_parent_pd_ent.merge(df_extracted_ent.drop_duplicates(), on=['sent_id','entity_label','entity_text','start','end'],
                            how='left')

            if(len(df_parent_pd_entities) > 0):
                for i in range(0, len(df_parent_pd_entities)):
                    current_entity =  copy.deepcopy(dict_entity)
                    view_id = df_parent_pd_entities.iloc[i]["view_id"]
                    current_entity["entity_label"] = df_parent_pd_entities.iloc[i]["entity_label"]
                    current_entity["sent_id"] = df_parent_pd_entities.iloc[i]["sent_id"]
                    current_entity["start"] = df_parent_pd_entities.iloc[i]["start"]
                    current_entity["end"] = df_parent_pd_entities.iloc[i]["end"]
                    current_entity["entity_text"] = df_parent_pd_entities.iloc[i]["sentence"].strip()[current_entity["start"] : current_entity["end"] ]
                    dict_parent_pd_json.update({view_id : current_entity})
                dic_ent_json.update(dict_parent_pd_json)
        """
        pvi_json['entitylocation']['entities'] = dic_ent_json
        pvi_json['entitylocation']['sentences'] = sent_json
        return pvi_json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e

