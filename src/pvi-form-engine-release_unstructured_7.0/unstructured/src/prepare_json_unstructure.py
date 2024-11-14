# from highlight_json_preparation import highlight_json_prep
from merge_entities import merge_duplicates
import entity_linker
import pandas as pd
import copy
import numpy
import sys
import re
from logs import get_logger
logger = get_logger()
default_error_message = "Inside prepare_json_unstructure.py - An error occured on line {}. Exception type: {}  Exception: {}"

from utils import convert_values_to_none
from configs import languages_config, load_validator



def update_current_additionalnote(current_additionalnote, key, hw_data):
    try:
        if current_additionalnote is None:
            current_additionalnote = f'{key} : {hw_data}'
        else:
            new_additionalnotes = f'{key} : {hw_data}'
            current_additionalnote = f'{current_additionalnote}, {new_additionalnotes}'
        return current_additionalnote
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def add_additionalnote(pvi_json, key, hw_data, main_entity):
    try:
        main_entity_data = pvi_json.get(main_entity, {})
        if isinstance(pvi_json[main_entity] , list): #main_entity == 'sourceType':
            current_additionalnote = pvi_json[main_entity][0]['additionalNotes']
        else:
            current_additionalnote = main_entity_data.get('additionalNotes')

        current_additionalnote = update_current_additionalnote(current_additionalnote, key, hw_data)
        if isinstance(pvi_json[main_entity] , list):
            pvi_json[main_entity][0]['additionalNotes'] = current_additionalnote
        elif isinstance(pvi_json[main_entity] , dict):
            pvi_json[main_entity]['additionalNotes'] = current_additionalnote
        return pvi_json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def add_additionalnote_item(given_json, key, hw_data):
    try:
        current_additionalnote = given_json.get('additionalNotes')  #todo check condition if key present or not / .get
        current_additionalnote = update_current_additionalnote(current_additionalnote, key, hw_data)
        given_json['additionalNotes'] = current_additionalnote
        return given_json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def populate_patient_age(pvi_json, df): #, lang):
    try:
        if "PAT_AGE" in df.entity_label.unique():
            pvi_json['patient']['age']['ageType'] = "PATIENT_ON_SET_AGE"
            if df.loc[df.entity_label == "PAT_AGE", "additionalNotes"].iloc[0] is not None:
                addToadditionalNote = df.loc[(df.entity_label == "PAT_AGE"), "additionalNotes"].iloc[0]
                pvi_json = add_additionalnote(pvi_json, "PAT_AGE", addToadditionalNote, 'patient')
            else:
                pvi_json['patient']['age']['inputValue'] = df.loc[df.entity_label == "PAT_AGE", "entity_text"].iloc[0]
                pvi_json['patient']['age']['inputValue_viewid'] = df.loc[(df.entity_label == "PAT_AGE"), "view_id"].iloc[0]
                pvi_json['patient']['age']['inputValue_acc'] = df.loc[(df.entity_label == "PAT_AGE"), "conf_score"].iloc[0]
        elif 'PAT_AGE_GRP' in df.entity_label.unique():
            pvi_json['patient']['age']['ageType'] = "PATIENT_AGE_GROUP"
            if df.loc[(df.entity_label == "PAT_AGE_GRP"), "additionalNotes"].iloc[0] is not None:
                addToadditionalNote = df.loc[(df.entity_label == "PAT_AGE_GRP"), "additionalNotes"].iloc[0]
                pvi_json = add_additionalnote(pvi_json, "PAT_AGE_GRP", addToadditionalNote, 'patient')
            else:
                pvi_json['patient']['age']['inputValue'] = \
                df.loc[(df.entity_label == "PAT_AGE_GRP"), "entity_text"].iloc[0]
                pvi_json['patient']['age']['inputValue_viewid'] = \
                df.loc[(df.entity_label == "PAT_AGE_GRP"), "view_id"].iloc[0]
                pvi_json['patient']['age']['inputValue_acc'] = \
                df.loc[(df.entity_label == "PAT_AGE_GRP"), "conf_score"].iloc[0]
        return pvi_json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def populate_patient_details(pvi_json, df):
    try:
        patient_dict = {'gender': 'PAT_GENDER', 'ethnicity': 'ETHNICITY', 'pregnancy': 'PREGNANCY', 'lmpDate': 'PAT_LMP'}
        for entity in patient_dict.keys():
            if patient_dict[entity] in df.entity_label.unique():
                if df.loc[(df.entity_label == patient_dict[entity]), "additionalNotes"].iloc[0] is not None:
                    addToadditionalNote = df.loc[(df.entity_label == patient_dict[entity]), "additionalNotes"].iloc[0]
                    pvi_json = add_additionalnote(pvi_json, patient_dict[entity], addToadditionalNote, 'patient')
                else:
                    pvi_json['patient'][entity] = df.loc[(df.entity_label == patient_dict[entity]), "entity_text"].iloc[0]
                    pvi_json['patient'][entity + '_viewid'] = \
                        df.loc[(df.entity_label == patient_dict[entity]), "view_id"].iloc[0]
                    pvi_json['patient'][entity + '_acc'] = \
                        df.loc[(df.entity_label == patient_dict[entity]), "conf_score"].iloc[0]
        return pvi_json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def populate_parent_gender_and_lmp_date(pvi_json, df):
    try:
        parent_dict = {'gender': 'PARENT_GENDER', 'lmpDate': 'PARENT_LMP_DATE'}
        for entity in parent_dict.keys():
            if parent_dict[entity] in df.entity_label.unique():
                if df.loc[(df.entity_label == parent_dict[entity]), "additionalNotes"].iloc[0] is not None:
                    addToadditionalNote = df.loc[(df.entity_label == parent_dict[entity]), "additionalNotes"].iloc[0]
                    pvi_json = add_additionalnote(pvi_json, parent_dict[entity], addToadditionalNote, 'patient')
                else:
                    pvi_json['parent'][entity] = df.loc[(df.entity_label == parent_dict[entity]), "entity_text"].iloc[0]
                    pvi_json['parent'][entity + '_viewid'] = \
                    df.loc[(df.entity_label == parent_dict[entity]), "view_id"].iloc[0]
                    pvi_json['parent'][entity + '_acc'] = \
                    df.loc[(df.entity_label == parent_dict[entity]), "conf_score"].iloc[0]
        return pvi_json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def populate_pat_height_and_weight(pvi_json, df):
    try:
        patient_dict = {'height': 'PAT_HEIGHT', 'weight': 'PAT_WEIGHT'}
        for entity in patient_dict.keys():
            if patient_dict[entity] in df.entity_label.unique():
                if df.loc[(df.entity_label == patient_dict[entity]), "additionalNotes"].iloc[0] is not None:
                    addToadditionalNote = df.loc[(df.entity_label == patient_dict[entity]), "additionalNotes"].iloc[0]
                    pvi_json = add_additionalnote(pvi_json, entity, addToadditionalNote, 'patient')
                else:
                    pvi_json['patient'][entity] = df.loc[(df.entity_label == patient_dict[entity]), "value"].iloc[0]
                    pvi_json['patient'][entity + '_acc'] = \
                        df.loc[(df.entity_label == patient_dict[entity]), "conf_score"].iloc[0]
                    pvi_json['patient'][entity + 'Unit'] = df.loc[(df.entity_label == patient_dict[entity]), "unit"].iloc[0]
                    pvi_json['patient'][entity + 'Unit_acc'] = \
                        df.loc[(df.entity_label == patient_dict[entity]), "conf_score"].iloc[0]
                    pvi_json['patient'][entity + '_viewid'] = \
                        df.loc[(df.entity_label == patient_dict[entity]), "view_id"].iloc[0]
        return pvi_json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def populate_autopsy_and_death_details(pvi_json, df, language):
    try:
        if 'AUTOPSY_DONE' in df.entity_label.unique():
            if df.loc[(df.entity_label == "AUTOPSY_DONE"), "additionalNotes"].iloc[0] is not None:
                addToadditionalNote = df.loc[(df.entity_label == "AUTOPSY_DONE"), "additionalNotes"].iloc[0]
                pvi_json = add_additionalnote(pvi_json, "autopsyDone", addToadditionalNote, 'deathDetail')
            else:
                if df.loc[(df.entity_label == "AUTOPSY_DONE"), "entity_text"].iloc[0].lower().startswith(languages_config[language]["translation_mapping"]['autopsy']):
                    pvi_json['deathDetail']['autopsyDone'] = "yes"
                    pvi_json['deathDetail']['autopsyDone_viewid'] = \
                        df.loc[(df.entity_label == "AUTOPSY_DONE"), "view_id"].iloc[0]
                    pvi_json['deathDetail']['autopsyDone_acc'] = \
                        df.loc[(df.entity_label == "AUTOPSY_DONE"), "conf_score"].iloc[0]
        if 'DEATH_DATE' in df.entity_label.unique():
            if df.loc[(df.entity_label == "DEATH_DATE"), "additionalNotes"].iloc[0] is not None:
                addToadditionalNote = df.loc[(df.entity_label == "DEATH_DATE"), "additionalNotes"].iloc[0]
                pvi_json = add_additionalnote(pvi_json, "deathDate", addToadditionalNote, 'deathDetail')
            else:
                pvi_json['deathDetail']['deathDate']['date'] = df.loc[(df.entity_label == "DEATH_DATE"), "entity_text"].iloc[0]
                pvi_json['deathDetail']['deathDate']['date_viewid'] = \
                    df.loc[(df.entity_label == "DEATH_DATE"), "view_id"].iloc[0]
                pvi_json['deathDetail']['deathDate']['date_acc'] = \
                    df.loc[(df.entity_label == "DEATH_DATE"), "conf_score"].iloc[0]
        if 'DEATH_CAUSE' or 'AUTOPSY_RESULT' in df.entity_label.unique():
            df_death_causes = df[(df.entity_label == 'DEATH_CAUSE') | (df.entity_label == 'AUTOPSY_RESULT')]
            df_death_causes.drop_duplicates(subset='entity_text', keep="first", inplace=True)
            death_cause_json = []
            if len(df_death_causes) > 0:
                for index in range(len(df_death_causes)):
                    current_cause = {"causeType": None, "reportedReaction": None,
                                    "reportedReaction_acc": None}  # copy.deepcopy(pvi_json['deathDetail']['deathCauses'][0])
                    current_cause['seq_num'] = index + 1
                    current_cause['reportedReaction'] = df_death_causes.iloc[index]['entity_text']
                    current_cause['reportedReaction_acc'] = df_death_causes.iloc[index]['conf_score']
                    current_cause['reportedReaction_viewid'] = df_death_causes.iloc[index]['view_id']
                    current_cause['causeType_acc'] = '1.0'
                    if df_death_causes.iloc[index]['entity_label'] == 'DEATH_CAUSE':
                        current_cause['causeType'] = 'Death Cause'
                    else:
                        current_cause['causeType'] = 'Autopsy Determined Cause'
                    death_cause_json.append(current_cause)
            pvi_json['deathDetail']['deathCauses'] = death_cause_json
        return pvi_json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def populate_study_details(pvi_json, df):
    try:
        study_dict = {'studyType': 'STUDY_TYPE', 'studyNumber': 'STUDY_NUM'}
        for entity in study_dict.keys():
            if study_dict[entity] in df.entity_label.unique():
                if df.loc[(df.entity_label == study_dict[entity]), "additionalNotes"].iloc[0] is not None:
                    addToadditionalNote = df.loc[(df.entity_label == study_dict[entity]), "additionalNotes"].iloc[0]
                    pvi_json = add_additionalnote(pvi_json, entity, addToadditionalNote, 'study')
                else:
                    pvi_json['study'][entity] = df.loc[(df.entity_label == study_dict[entity]), "entity_text"].iloc[0]
                    pvi_json['study'][entity + '_viewid'] = df.loc[(df.entity_label == study_dict[entity]), "view_id"].iloc[0]
                    pvi_json['study'][entity + '_acc'] = df.loc[(df.entity_label == study_dict[entity]), "conf_score"].iloc[0]
                    if entity == 'studyType':
                        try:
                            coded_study_type = df.loc[(df.entity_label == study_dict[entity]), "semantic_match"].iloc[0]
                        except IndexError as e:
                            logger.warning(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
                            coded_study_type = numpy.nan
                        if type(coded_study_type) != float:
                            pvi_json['study']['studyType'] = coded_study_type
        return pvi_json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def populate_source_type(pvi_json, df):
    try:
        if 'SOURCE_TYPE' in df.entity_label.unique():
            if df.loc[(df.entity_label == "SOURCE_TYPE"), "additionalNotes"].iloc[0] is not None:
                addToadditionalNote = df.loc[(df.entity_label == "SOURCE_TYPE"), "additionalNotes"].iloc[0]
                pvi_json = add_additionalnote(pvi_json, "value", addToadditionalNote, 'sourceType')
            else:
                try:
                    coded_source_type = df.loc[(df.entity_label == "SOURCE_TYPE"), "semantic_match"].iloc[0]
                except IndexError:
                    coded_source_type = numpy.nan
                if type(coded_source_type) != float:
                    pvi_json["sourceType"][0]["value"] = coded_source_type
                else:
                    pvi_json["sourceType"][0]["value"] = df.loc[(df.entity_label == "SOURCE_TYPE"), "entity_text"].iloc[0]
                pvi_json["sourceType"][0]["value_acc"] = df.loc[(df.entity_label == "SOURCE_TYPE"), "conf_score"].iloc[0]
                pvi_json["sourceType"][0]["value_viewid"] = df.loc[(df.entity_label == "SOURCE_TYPE"), "view_id"].iloc[0]
        return pvi_json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def set_rep_groupid(df_rep, entity_label):
    try:
        df_rep.loc[df_rep.entity_label == entity_label, "groupid"] = [index + 1 for index in range(
            len(df_rep.loc[df_rep.entity_label == entity_label]))]
        return df_rep
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def get_rep_value(df_rep, entity_label, index):
    try:
        if len(df_rep.loc[(df_rep.entity_label == entity_label) & (df_rep.groupid == index)]["entity_text"]) > 0:
            fields = ['entity_text', 'conf_score', 'view_id']
            text, score, view = [
                df_rep.loc[(df_rep.entity_label == entity_label) & (df_rep.groupid == index)][field].iloc[0] for field in
                fields]
            return str(text).strip(), score, view
        return "", "", ""
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def populate_reporter_details(pvi_json, df):
    try:
        reporter_json = []
        col_list = ["entity_label", "entity_text", "view_id", "conf_score"]
        if len(df.loc[(df.entity_label.isin(["REPORTER_COUNTRY", "REPORTER_OCCUPATION"])), col_list]) > 0:
            df_rep = df.loc[(df.entity_label.isin(["REPORTER_COUNTRY", "REPORTER_OCCUPATION"])), col_list]
            try:
                coded_occupation = df.loc[(df.entity_label == "REPORTER_OCCUPATION"), "semantic_match"].iloc[0]
            except IndexError:
                coded_occupation = numpy.nan
            if type(coded_occupation) != float and str(coded_occupation).lower() not in ["nan", "none"]:
                df_rep.loc[(df_rep.entity_label == 'REPORTER_OCCUPATION'), ['entity_text']] = coded_occupation
            # Drop duplicate reporter value
            df_rep = df_rep.drop_duplicates(['entity_label'], keep='last')
            df_rep["groupid"] = ""
            df_rep = set_rep_groupid(df_rep, "REPORTER_COUNTRY")
            df_rep = set_rep_groupid(df_rep, "REPORTER_OCCUPATION")
            for index in range(max(df_rep.entity_label.value_counts())):
                current_rep = copy.deepcopy(pvi_json['reporters'][0])
                current_rep["country"], current_rep["country_acc"], current_rep["country_viewid"] = \
                    get_rep_value(df_rep, "REPORTER_COUNTRY", index + 1)
                current_rep["qualification"], current_rep["qualification_acc"], current_rep["qualification_viewid"] = \
                    get_rep_value(df_rep, "REPORTER_OCCUPATION", index + 1)
                if len(str(current_rep["qualification_acc"])) > 1 and str(current_rep["qualification"]) in ["nan", "None"]:
                    current_rep["qualification"] = ""
                reporter_json.append(current_rep)
            pvi_json['reporters'] = reporter_json
        return pvi_json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def populate_malfunctions(prod, malfunctions, malfunctions_conf, malfunction_view_id):
    try:
        prod["devices"][0]['malfunctions'] = malfunctions
        prod["devices"][0]['malfunctions_acc'] = malfunctions_conf
        prod["devices"][0]['malfunctions_view_id'] = malfunction_view_id
        prod['product_type'][0]['value'] = "Device"
        return prod
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def get_prod_malfunctions(df):
    try:
        malfunctions = ""
        malfunctions_conf = ""
        malfunction_view_id = None
        df_malfunctions = df[df.entity_label == 'MALFUNCTIONS']
        if len(df_malfunctions) > 0:
            for index in range(len(df_malfunctions)):
                malfunctions = malfunctions + ", " + df_malfunctions.iloc[index]['entity_text']
            malfunctions_conf = df_malfunctions.iloc[0]['conf_score']
            malfunction_view_id = df_malfunctions.iloc[0]['view_id']
            malfunctions = malfunctions.strip(",")
        return malfunctions, malfunctions_conf, malfunction_view_id
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def get_prod_seq_df(df):
    try:
        df_seq = pd.DataFrame(columns=["PRODUCT_NAME"], data=df["PRODUCT_NAME"].str.lower().unique())
        df_seq["SEQ_NUM"] = list(range(len(df_seq)))
        df_seq["SEQ_NUM"] = df_seq["SEQ_NUM"].array.astype(int).tolist()
        return df_seq
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def populate_default_values(entity, value_type):
    try:
        default_values_dict = {
            'PRODUCT_NAME': {'_acc': None, '_viewid': None}, 'DRUG_ROLE': {'_acc': 0.99, '_viewid': 0},
            'FORMULATION': {'_acc': None, '_viewid': None}, 'DOSE': {'_acc': None, '_viewid': None},
            'FREQ': {'_acc': None, '_viewid': None}, 'ROUTE': {'_acc': None, '_viewid': None},
            'DOSE_START_DATE': {'_acc': None, '_viewid': None}, 'DOSE_STOP_DATE': {'_acc': None, '_viewid': None},
            'DOSE_DURATION': {'_acc': None, '_viewid': None}, 'DOSE_BATCH_LOT_NUM': {'_acc': None, '_viewid': None},
            'ACTION_TAKEN': {'_acc': None, '_viewid': None}, 'PRODUCT_INDICATIONS': {'_acc': None, '_viewid': None},
            'DESC_REPTD': {'_acc': None, '_viewid': None}, 'EVENT_OUTCOME': {'_acc': None, '_viewid': None},
            'EVENT_SERIOUSSNESS': {'_acc': None, '_viewid': None}, 'ONSET': {'_acc': None, '_viewid': None},
            'EVENT_END_DATE': {'_acc': None, '_viewid': None}, 'EVENT_DURATION': {'_acc': None, '_viewid': None},
            'LAB_TEST_NAME': {'_acc': None, '_viewid': None}, 'TEST_RESULT': {'_acc': None, '_viewid': None},
            'TEST_DATE': {'_acc': None, '_viewid': None},'TEST_NOTES': {'_acc': None, '_viewid': None}, 'MEDICAL_HISTORY': {'_acc': None, '_viewid': None},
            'MC_START_DATE': {'_acc': None, '_viewid': None}, 'MC_END_DATE': {'_acc': None, '_viewid': None},
            'PAT_PAST_DRUG': {'_acc': None, '_viewid': None}, 'PAST_DRUG_START': {'_acc': None, '_viewid': None},
            'PAST_DRUG_END': {'_acc': None, '_viewid': None}
        }
        if entity in default_values_dict.keys():
            return default_values_dict[entity][value_type]
        else:
            return None
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def populate_license_role_dosage_value(prod, df, row, language):
    try:
        prod_dict = {'license_value': 'PRODUCT_NAME', 'role_value': 'DRUG_ROLE', 'dosageForm_value': 'FORMULATION'}
        prod = populate_given_dict(prod_dict, prod, df, row)
        prod['role_value'] = prod['role_value'] if prod['role_value'] else languages_config[language]["translation_mapping"]["product role"]['suspect']
        return prod
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def populate_dose_informations(prod, df, row):
    try:
        prod_dict = {'dose_inputValue': 'DOSE', 'frequency_value': 'FREQ', 'route_value': 'ROUTE',
                    'startDate': 'DOSE_START_DATE', 'endDate': 'DOSE_STOP_DATE', 'duration': 'DOSE_DURATION',
                    'customProperty_batchNumber_value': 'DOSE_BATCH_LOT_NUM'}
        for entity in prod_dict.keys():
            if row[prod_dict[entity]+"_additionalNotes"] != "":
                addToadditionalNote = row[prod_dict[entity]+"_additionalNotes"]
                prod = add_additionalnote_item(prod, entity, addToadditionalNote)
            else:
                prod['doseInformations'][0][entity] = row[prod_dict[entity]]
                prod['doseInformations'][0][entity + '_acc'] = df.loc[((df.entity_label == prod_dict[entity]) & (
                        df.entity_text == row[prod_dict[entity]])), "conf_score"].iloc[0] \
                    if row[prod_dict[entity]] else populate_default_values(prod_dict[entity], '_acc')
                prod['doseInformations'][0][entity + '_viewid'] = df.loc[((df.entity_label == prod_dict[entity]) & (
                        df.entity_text == row[prod_dict[entity]])), "view_id"].iloc[0] \
                    if row[prod_dict[entity]] else populate_default_values(prod_dict[entity], '_viewid')
        prod['doseInformations'][0]['description'] = (str(row["DOSE"]) + " " + str(row["FREQ"])).strip()
        return prod
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def populate_indications(prod, df, row):
    try:
        prod_dict = {'reportedReaction': 'PRODUCT_INDICATIONS'}
        for entity in prod_dict.keys():
            if row[prod_dict[entity]+"_additionalNotes"] != "":
                addToadditionalNote = row[prod_dict[entity]+"_additionalNotes"]
                prod = add_additionalnote_item(prod, entity, addToadditionalNote)
            else:
                prod['indications'][0][entity] = row[prod_dict[entity]]
                prod['indications'][0][entity + '_acc'] = df.loc[((df.entity_label == prod_dict[entity]) & (
                        df.entity_text == row[prod_dict[entity]])), "conf_score"].iloc[0] \
                    if row[prod_dict[entity]] else populate_default_values(prod_dict[entity], '_acc')
                prod['indications'][0][entity + '_viewid'] = df.loc[((df.entity_label == prod_dict[entity]) & (
                        df.entity_text == row[prod_dict[entity]])), "view_id"].iloc[0] \
                    if row[prod_dict[entity]] else populate_default_values(prod_dict[entity], '_viewid')
        return prod
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def populate_action_taken(prod, df, row):
    try:
        prod_dict = {'actionTaken': 'ACTION_TAKEN'}
        for entity in prod_dict.keys():
            if row[prod_dict[entity]+"_additionalNotes"] != "":
                addToadditionalNote = row[prod_dict[entity]+"_additionalNotes"]
                prod = add_additionalnote_item(prod, entity, addToadditionalNote)
            else:
                prod[entity]['value'] = row[prod_dict[entity]]
                prod[entity]['value_acc'] = df.loc[((df.entity_label == prod_dict[entity]) & (
                        df.entity_text == row[prod_dict[entity]])), "conf_score"].iloc[0] \
                    if row[prod_dict[entity]] else populate_default_values(prod_dict[entity], '_acc')
                prod[entity]['value_viewid'] = df.loc[((df.entity_label == prod_dict[entity]) & (
                        df.entity_text == row[prod_dict[entity]])), "view_id"].iloc[0] \
                    if row[prod_dict[entity]] else populate_default_values(prod_dict[entity], '_viewid')
        return prod
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def populate_product(pvi_json, df, df_prod, language):
    try:
        # generate unique seq for product
        products = []
        df_seq = get_prod_seq_df(df_prod)
        malfunctions, malfunctions_conf, malfunction_view_id = get_prod_malfunctions(df)
        for index, row in df_prod.iterrows():
            prod = copy.deepcopy(pvi_json['products'][0])
            prod['seq_num'] = df_seq.loc[(df_seq.PRODUCT_NAME == row["PRODUCT_NAME"].lower()),
                                        "SEQ_NUM"].array.astype(int).tolist()[0]
            prod['product_type'][0]['value'] = "Drug"
            prod['product_type'][0]['value_acc'] = "1"
            if malfunctions != "":
                prod = populate_malfunctions(prod, malfunctions, malfunctions_conf, malfunction_view_id)
            prod = populate_license_role_dosage_value(prod, df, row, language)
            prod = populate_dose_informations(prod, df, row)
            prod = populate_indications(prod, df, row)
            prod = populate_action_taken(prod, df, row)
            products.append(prod)
        # product_json = remove_duplicate_products(product_json)
        pvi_json['products'] = products
        return pvi_json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def entity_linking(df):
    try:
        output_df_list = []
        dict_ent_linked = entity_linker.link_ents(df)
        ent_list = ['PRODUCT_NAME', 'DESC_REPTD', 'LAB_TEST_NAME', 'MEDICAL_HISTORY', 'PAT_PAST_DRUG',
                    'PARENT_MED_HIST', 'PARENT_DRUG_HIST']
        for entity in ent_list:
            temp_df = dict_ent_linked[entity]
            temp_df.fillna('', inplace=True)
            output_df_list.append(merge_duplicates(temp_df))
        return output_df_list
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def populate_event_seriousness(event, df, row):
    try:
        if row['EVENT_SERIOUSSNESS_additionalNotes'] != "":
            addToadditionalNote = row['EVENT_SERIOUSSNESS_additionalNotes']
            event = add_additionalnote_item(event, 'seriousnesses', addToadditionalNote)
        else:
            event['seriousnesses'][0]['value'] = row['EVENT_SERIOUSSNESS']
            event['seriousnesses'][0]['value_acc'] = \
                df.loc[((df.entity_label == 'EVENT_SERIOUSSNESS') &
                        (df.entity_text == event['seriousnesses'][0]['value'])), 'conf_score'].iloc[0] if row[
                    'EVENT_SERIOUSSNESS'] else populate_default_values('EVENT_SERIOUSSNESS', '_acc')
            event['seriousnesses'][0]['value_viewid'] = \
                df.loc[((df.entity_label == 'EVENT_SERIOUSSNESS') &
                        (df.entity_text == event['seriousnesses'][0]['value'])), 'view_id'].iloc[0] if row[
                    'EVENT_SERIOUSSNESS'] else populate_default_values('EVENT_SERIOUSSNESS', '_viewid')
        return event
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def populate_event(pvi_json, df, df_event):
    try:
        events = []
        for index in range(len(df_event)):
            row = df_event.iloc[index]
            event = copy.deepcopy(pvi_json['events'][0])
            event['seq_num'] = index + 1
            event_dict = {'reportedReaction': 'DESC_REPTD', 'reactionCoded': 'DESC_REPTD', 'outcome': 'EVENT_OUTCOME',
                        'startDate': 'ONSET', 'endDate': 'EVENT_END_DATE', 'duration': 'EVENT_DURATION'}
            event = populate_given_dict(event_dict, event, df, row)
            event['reportedReaction'] = event['reportedReaction'].strip()
            event = populate_event_seriousness(event, df, row)
            events.append(event)
        pvi_json['events'] = events
        return pvi_json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e

def validate_test_result(test_result):
    try:
        unit = ""
        testnotes = copy.deepcopy(test_result)
        if test_result == "":
            return ["", "", ""]

        pattern = re.compile(r'((<=|>=|=|>|<)\s*)?\d+(\.\d+)?(,\d+)?%*\s*(and|-)?\s*((<=|>=|=|>|<)\s*)?(\d+)?(\.\d+)?(,\d+)?')
        result_match = re.search(pattern, test_result)
        if result_match:
            test_result = result_match.group().replace(",", "")
            unit = re.sub(pattern, " ", testnotes).strip()
            testnotes = ""
        else:
            test_result = ""
            testnotes = testnotes.strip().strip(";").strip()
        unit_slash_pattern = r"\s*/\s*"
        unit = re.sub(unit_slash_pattern, "/", unit).strip() if unit else None
        return testnotes, unit, test_result.strip()
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def process_test_result(test_name, test_result, df, row, language, sentences, redis_obj):    #TOCHECK: units change specific to language
    """
    Splits test result and test result unit based on regex , if regex pattern is not matched then the test result value is passed to testNotes,
    After that based on conditions it is further passed to test assessment and test notes fields of json.
    """
    try:
        test_notes = row["TEST_NOTES"]
        sentence = sentences[int(row['sent_id'])- 1]
        pattern = r"(?<=\d)?\s?to\s?(?=\d)|\bto\b"
        result = re.split(pattern, test_result, flags=re.IGNORECASE)
        notes = ["increase", "decrease"]
        if len(result)>1:
            if any(True if note in test_notes.lower() else False for note in notes):
                test_notes = sentence[row["TEST_NOTES_start"]:row["TEST_RESULT_end"]]
                df.loc[((df.entity_label == "TEST_NOTES") &
                        (df.start == row["TEST_NOTES_start"]) & (df.entity_text == row['TEST_NOTES'])), "end"] = row[
                    "TEST_RESULT_end"]
                test_result = result[-1]
            else:
                test_result = " - ".join(result)

        additionalNote, unit, value = validate_test_result(test_result)

        test_notes = (test_notes+"\n"+additionalNote.strip()).strip()

        value = value if value else None
        unit = unit if unit else None
        return [test_notes, unit, value, df]
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def populate_test_section(pvi_json, df, df_test, language, sentences, redis_obj):
    """
    creates lab test section according to json exepected by PVCM application
    """
    try:
        tests = []
        try:
            for index in range(len(df_test)):
                row = df_test.iloc[index]
                test = copy.deepcopy(pvi_json['tests'][0])
                test['seq_num'] = index + 1
                test_dict = {'testName': 'LAB_TEST_NAME', 'testResult': 'TEST_RESULT', 'startDate': 'TEST_DATE', 'testNotes':'TEST_NOTES'}
                test = populate_given_dict(test_dict, test, df, row)
                test['testNotes'], test['testResultUnit'], test['testResult'], df = \
                    process_test_result(test['testName'], row['TEST_RESULT'], df, row ,language, sentences ,redis_obj)
                # elif conf score to result and unit key
                if test['testResult'] is None:
                    test["testResult_acc"] = None
                    test["testResult_viewid"] = None
                    test["testResultUnit"] = None

                tests.append(test)
            pvi_json['tests'] = tests if len(tests) != 0 else [convert_values_to_none(pvi_json["tests"][0])]
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        return pvi_json, df
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def populate_medical_history(pvi_json, df, df_mh):
    try:
        medical_histories = []
        med_uni = []
        for index in range(len(df_mh)):
            row = df_mh.iloc[index]
            if not row['MEDICAL_HISTORY'].lower() in med_uni:
                med_his = copy.deepcopy(pvi_json['patient']['medicalHistories'][0])
                med_uni.append(row['MEDICAL_HISTORY'].lower())
                med_his['seq_num'] = index + 1
                mh_dict = {'reportedReaction': 'MEDICAL_HISTORY', 'startDate': 'MC_START_DATE', 'endDate': 'MC_END_DATE'}
                med_his = populate_given_dict(mh_dict, med_his, df, row)
                medical_histories.append(med_his)
        pvi_json['patient']['medicalHistories'] = medical_histories
        return pvi_json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def populate_past_drug_histories(pvi_json, df, df_pd):
    try:
        past_drug_histories = []
        for index in range(len(df_pd)):
            row = df_pd.iloc[index]
            past_drug = copy.deepcopy(pvi_json['patient']['pastDrugHistories'][0])
            past_drug['seq_num'] = index + 1
            pd_dict = {'drugName': 'PAT_PAST_DRUG', 'startDate': 'PAST_DRUG_START', 'endDate': 'PAST_DRUG_END'}
            past_drug = populate_given_dict(pd_dict, past_drug, df, row)
            past_drug_histories.append(past_drug)
        pvi_json['patient']['pastDrugHistories'] = past_drug_histories
        return pvi_json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def populate_parent_med_history(pvi_json,df,df_parent_mh=""):
    try:
        medical_histories = []
        med_uni = []
        col_list = ["entity_label", "entity_text", "view_id", "conf_score"]

        if len(df.loc[(df.entity_label.isin(["PARENT_MED_HIST"])), col_list]) > 0:
            df_parent_mh = df.loc[(df.entity_label.isin(["PARENT_MED_HIST"])), col_list]

        for index in range(len(df_parent_mh)):
            row = df_parent_mh.iloc[index]
            if not row['entity_text'].lower() in med_uni:
                med_his = copy.deepcopy(pvi_json['parent']['medicalHistories'][0])
                med_uni.append(row['entity_text'].lower())
                med_his["reportedReaction"] = row["entity_text"]
                med_his["reportedReaction_acc"] = row["conf_score"]
                med_his["reportedReaction_viewId"] = row["view_id"]
                medical_histories.append(med_his)
        pvi_json['parent']['medicalHistories'] = medical_histories
        return pvi_json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def populate_parent_past_drug_histories(pvi_json, df, df_parent_pd=""):
    try:
        past_drug_hist = []
        drug_uni = []
        col_list = ["entity_label", "entity_text", "view_id", "conf_score"]

        if len(df.loc[(df.entity_label.isin(["PARENT_DRUG_HIST"])), col_list]) > 0:
            df_parent_pd = df.loc[(df.entity_label.isin(["PARENT_DRUG_HIST"])), col_list]

        for index in range(len(df_parent_pd)):
            row = df_parent_pd.iloc[index]
            if not row['entity_text'].lower() in drug_uni:
                past_drug = copy.deepcopy(pvi_json['parent']['pastDrugHistories'][0])
                drug_uni.append(row['entity_text'].lower())
                past_drug["drugName"] = row["entity_text"]
                past_drug["drugName_acc"] = row["conf_score"]
                past_drug["drugName_viewId"] = row["view_id"]
                past_drug_hist.append(past_drug)
        pvi_json['parent']['pastDrugHistories'] = past_drug_hist
        return pvi_json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def populate_given_dict(given_dict, given_json, df, row):
    try:
        for entity in given_dict.keys():
            pri_ent = ["PRODUCT_NAME", "DESC_REPTD", "LAB_TEST_NAME","MEDICAL_HISTORY","PAT_PAST_DRUG","PARENT_MED_HIST","PARENT_DRUG_HIST"]
            if (given_dict[entity] not in pri_ent) and (row[given_dict[entity]+"_additionalNotes"] != ""):
                addToadditionalNote = row[given_dict[entity]+"_additionalNotes"]
                given_json = add_additionalnote_item(given_json, entity, addToadditionalNote)
            else:
                if (given_dict[entity] not in pri_ent) and (row[given_dict[entity]+"_semantic_match"] != ""):
                    given_json[entity] = row[given_dict[entity]+"_semantic_match"]
                else:
                    given_json[entity] = row[given_dict[entity]]
                given_json[entity + '_acc'] = df.loc[((df.entity_label == given_dict[entity]) &
                                                    (df.entity_text == row[given_dict[entity]])), "conf_score"].iloc[0] if \
                    row[given_dict[entity]] else populate_default_values(given_dict[entity], '_acc')
                given_json[entity + '_viewid'] = df.loc[((df.entity_label == given_dict[entity]) &
                                                        (df.entity_text == row[given_dict[entity]])), "view_id"].iloc[0] if \
                    row[given_dict[entity]] else populate_default_values(given_dict[entity], '_viewid')
        return given_json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def highlight_json_prep(df_extracted_ent, text, sentences):
    try:
        dic_ent_json = {}
        sent_json = {}
        df_sent = df_extracted_ent.drop_duplicates(subset=['sent_id'], keep='first')
        for i in range(0, len(df_sent)):
            sent_id = int(df_sent.iloc[i]["sent_id"])
            if sent_id != -1:
                sent_json[sent_id] = sentences[sent_id-1].strip()
        if len(df_extracted_ent) > 0:
            for i in range(0, len(df_extracted_ent)):
                current_entity = {}
                view_id = df_extracted_ent.iloc[i]["view_id"]
                current_entity["entity_label"] = df_extracted_ent.iloc[i]["entity_label"]
                current_entity["sent_id"] = df_extracted_ent.iloc[i]["sent_id"]
                current_entity["start"] = int(df_extracted_ent.iloc[i]["start"])
                current_entity["end"] = int(df_extracted_ent.iloc[i]["end"])
                current_entity["entity_text"] = df_extracted_ent.iloc[i]["entity_text"]
                current_entity["model"] = df_extracted_ent.iloc[i]["model"]
                dic_ent_json.update({view_id: current_entity})
        return {'entities': dic_ent_json, 'sentences': sent_json}
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def populate_pvi_json(pvi_json, df, language):
    try:
        pvi_json = populate_patient_age(pvi_json, df)#, language)
    except:
        pass
    try:
        pvi_json = populate_patient_details(pvi_json, df)
    except:
        pass
    try:
        pvi_json = populate_parent_gender_and_lmp_date(pvi_json, df)
    except:
        pass
    try:
        pvi_json = populate_pat_height_and_weight(pvi_json, df)
    except:
        pass
    try:
        pvi_json = populate_autopsy_and_death_details(pvi_json, df, language)
    except:
        pass
    try:
        pvi_json = populate_study_details(pvi_json, df)
    except:
        pass
    try:
        pvi_json = populate_source_type(pvi_json, df)
    except:
        pass
    try:
        pvi_json = populate_reporter_details(pvi_json, df)
    except:
        pass
    try:
        pvi_json = populate_parent_med_history(pvi_json, df)
    except:
        pass
    try:
        pvi_json = populate_parent_past_drug_histories(pvi_json, df)
    except:
        pass
    return pvi_json


def prepare_final_json(pvi_json, df, text, sentences, df_derived_entity, language , redis_obj):
    try:
        # Adding view id
        df.insert(0, 'view_id', range(0, len(df)))
        df['view_id'] = df['view_id'].apply(str)
        df['sent_id'] = df['sent_id'].apply(str)
        pvi_json = populate_pvi_json(pvi_json, df, language)

        # calling entity linking code for products and events ---------------------------------------------
        df_prod, df_event, df_test, df_mh, df_pd, df_parent_mh, df_parent_pd = entity_linking(df)

        if len(df_prod) > 0:
            pvi_json = populate_product(pvi_json, df, df_prod, language)

        if len(df_event) > 0:
            pvi_json = populate_event(pvi_json, df, df_event)

        if len(df_test) > 0:
            pvi_json, df = populate_test_section(pvi_json, df, df_test, language, sentences, redis_obj)

        if len(df_mh) > 0:
            pvi_json = populate_medical_history(pvi_json, df, df_mh)

        if len(df_pd) > 0:
            pvi_json = populate_past_drug_histories(pvi_json, df, df_pd)

        if len(df_derived_entity):
            maxClm = len(df)
            df_derived_entity.insert(0, 'view_id', range(maxClm, maxClm+len(df_derived_entity)))
            df_derived_entity['view_id'] = df_derived_entity['view_id'].apply(str)
            df_derived_entity['sent_id'] = df_derived_entity['sent_id'].apply(str)
            df = pd.concat([df, df_derived_entity], ignore_index=True, sort=False)

        pvi_json["entitylocation"] = highlight_json_prep(df, text, sentences)

        return pvi_json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e
