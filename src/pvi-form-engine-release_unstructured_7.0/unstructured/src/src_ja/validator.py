import copy
import re
import sys
import math

import logs

from configs import  global_config, load_config
from src.utils import date_format_update

logger = logs.get_logger()
default_error_message = "Inside validator.py - An error occured on line {}. Exception type: {} , Exception: {} "


#dictionary having entity_label and validate json key mapping
val_config_dict = {
    # "SOURCE_TYPE" :"Source_Value" ,
                   "PAT_AGE_GRP" : "Age_Group",
                   "EVENT_SERIOUSSNESS":"Seriousness",
                   "PAT_GENDER" : "Gender",
                   "PARENT_GENDER" :"Gender"
                   }

def validate_from_json(entity , label_to_validate,redis_obj):
    """
    Validates entity value from validate json ,
    """
    try:
        # flag = False
        validate_list = redis_obj.fetch_data(val_config_dict[label_to_validate] , "ja")
        if isinstance(validate_list, list):
            for value in validate_list:
                if value in entity.lower():
                    return True ,entity
            else:
                return False , entity
        elif isinstance(validate_list , dict):
            for mapping in validate_list.keys():
                if entity.lower().strip() in validate_list[mapping]:
                    mapped_value = mapping.lower()
                    return True , mapped_value
            else:
                return False , entity
        else:
            logger.info(f"No value datatype matched from validate json")
            return False ,entity
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def pat_age_validate(entity, sentence ,redis_obj):
    try:
        if entity.strip()[0].isdigit():
            digits = re.findall(r'\d+', entity)[0]
            #validation
            AGE_RANGE = redis_obj.fetch_data("Age_Range","ja")
            if int(digits) < int(AGE_RANGE[0]) or int(digits) > int(AGE_RANGE[-1]):

                return False, entity
            #post process
            unit = re.sub(digits, "", entity.lower())
            for codelist_unit in redis_obj.fetch_data("Age_Unit","ja"):
                if unit in codelist_unit:
                    entity = f"{digits} {codelist_unit}"
                    return True, entity
            return False, entity
        else:
            return False, entity
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def pregnancy_validate(entity, sentence, redis_obj):
    try:
        st = sentence.find(entity.lower())
        end = st + len(entity.lower())
        if not end == len(sentence):
            preg_validate = [False if sentence[end + 1:].strip().startswith(x) else True for x in redis_obj.fetch_data("Pregnancy_Forward" , "ja")]
            return False if False in preg_validate else True
        return True
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def study_num_validate(entity):
    try:
        # ls_months = ["jan", "feb", "march", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"]
        ls_months = ["1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"]
        if entity.isalpha() or "%" in entity.lower():
            return False
        return False if True in [True if x in entity.lower() else False for x in ls_months] else True
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def dose_validate(entity):
    try:
        pat = re.compile(r"\d+")
        if pat.search(entity):
            return True
        return False
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def route_freq_formulation_validate(entity, label, redis_obj):
    try:
        codelist_data = redis_obj.fetch_data(label , "ja")
        if entity in map(str.lower , codelist_data):
            return True
        return False
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def validate_pat_height_and_weight(data, label, redis_obj):
    try:
        data = re.sub(r'\.{1,}', r'.', data)
        WEIGHT_UNIT = redis_obj.fetch_data("Weight_Unit" , "ja")
        HEIGHT_UNIT = redis_obj.fetch_data("Height_Unit" , "ja")
        if label in ["weight", "PAT_WEIGHT"]:
            validate_list = WEIGHT_UNIT
        elif label in ["height", "PAT_HEIGHT"]:
            validate_list = HEIGHT_UNIT
            if bool(re.search(r"\d+\s*(feet|ft)+\s*\d*\s*I*i*", data)): #converting feet to inch
                digits = re.findall(r"\d+", data)
                if len(digits) == 1:
                    data = str(float(digits[0]) * 12) + " inches"
                elif len(digits) == 2:
                    data = str(math.floor(float(".".join(digits)) * 12)) + " inches"
            else:
                data = re.sub(r"(\d+\\.*\d*)([a-zA-Z]+)", '\\1 \\2', data)

        else:
            validate_list = WEIGHT_UNIT + HEIGHT_UNIT

        value = re.findall(r'(\d+\.?\d*)', data)
        if len(value) == 0:
            value = None
        else:
            value = value[0]

        units = re.findall(r"[a-zA-Z]+", data)
        unit = None

        for unit_value in validate_list:
            if (unit_value in map(str.lower, units)) or (
                    unit_value + 's' in map(str.lower, units)):  # validating unit and handling case insensitivity
                unit = unit_value  #+  's'
                break

        if unit == None or value == None:
            return False, value, unit

        return True, value, unit
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e

# def convert_gender_seriousness(value):
#     mapped_value = ""
#     for key in validator_json:
#         if isinstance(validator_json[key], dict):
#             for mapping in validator_json[key]:
#                 if value.lower().strip() in validator_json[key][mapping]:
#                     mapped_value = mapping.lower()
#     return True, mapped_value


def convert_study_type(study_type):
    try:
        # study_type = study_type.replace("(study", "study")
        return True, study_type
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def validate_test_result(test_result):
    try:
        unit = ""
        testnotes = copy.deepcopy(test_result)
        if test_result == "":
            return [True, "", "", ""]

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
        return [True, testnotes, unit, test_result]
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def validate_lot_num(lot_num_entity):
    """
    remove junk from lot number entity and
    """
    try:
        lot_num_entity = re.sub(r"(lot[\s+]?(number|no)?[.]?|batch[\s+]?(number|no)?[.]?|batch|lot)" ,"" ,lot_num_entity).strip(")( ")

        include_hash_flag = global_config.getboolean("PIPELINE_API","INCLUDE_LOT_HASH")
        if "#" in lot_num_entity:
            if not include_hash_flag:
                lot_num_entity = lot_num_entity[lot_num_entity.find("#")+1:].strip(")(") #+1 to exclude "#"
            else:
                lot_num_entity = lot_num_entity[lot_num_entity.find("#"):].strip(")(")

        return True, lot_num_entity
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def validate_and_convert_date(date_str):
    date_str = date_str.strip()
    date_str = re.sub(r'[年月日]', '-', date_str).strip()
    value = date_format_update(date_str)
    if value == date_str:
        return False, date_str
    else:
        return True, value


def validate(label, entity, sentence, df, index, redis_obj):
    try:
        df = copy.deepcopy(df)
        if 'additionalNotes' not in df.columns:
            df['additionalNotes'] = None
            df['unit'] = None
            df['value'] = None
        sentence = sentence.lower()
        if label in ["FREQ","ROUTE","FORMULATION"]:
            if route_freq_formulation_validate(entity.lower(), label ,redis_obj):
                return df
        elif label == "DOSE":
            if dose_validate(entity):
                return df
        elif label in ["STUDY_NUM"]:
            if study_num_validate(entity):
                return df
        elif label in ["PREGNANCY", "PAT_STAT_PREG"]:
            if pregnancy_validate(entity, sentence, redis_obj):
                df.loc[index, 'entity_text'] = "yes"
                return df
        elif label in val_config_dict.keys(): #["PAT_GENDER", "PARENT_GENDER", "EVENT_SERIOUSSNESS" ,"SOURCE_TYPE", "PAT_AGE_GRP"]:
            flag, entity = validate_from_json(entity, label, redis_obj)
            if flag:
                df.loc[index, 'entity_text'] = entity
                return df
        elif label == "PAT_AGE":
            flag, entity = pat_age_validate(entity, sentence ,redis_obj)
            if flag:
                df.loc[index, 'entity_text'] = entity
                return df
        elif label in ["PAT_WEIGHT", "PAT_HEIGHT"]:
            flag, value, unit = validate_pat_height_and_weight(entity, label ,redis_obj)
            if flag:
                df.loc[index, 'unit'] = unit
                df.loc[index, 'value'] = value
                return df
        elif label == "STUDY_TYPE":
            flag, entity = convert_study_type(entity)
            if flag:
                df.loc[index, 'entity_text'] = entity
                return df
        elif label == "TEST_RESULT":
            flag, testnotes, unit, test_result_value = validate_test_result(entity)
            if flag:
                df.loc[index, 'unit'] = unit
                df.loc[index, 'value'] = test_result_value
                df.loc[index, 'additionalNotes'] = testnotes
                return df
        elif label == "DOSE_BATCH_LOT_NUM":
            flag, value = validate_lot_num(entity)
            if flag:
                df.loc[index, 'entity_text'] = value
                return df
        elif "START" in label or "END_" in label or "_END" in label or "DATE" in label or "PAT_LMP" == label or "ONSET" == label:
            flag, value = validate_and_convert_date(entity)
            if flag:
                df.loc[index, 'entity_text'] = value
                return df
            else:
                return df
        else:
            return df
        df.loc[index, 'additionalNotes'] = entity
        return df
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e
