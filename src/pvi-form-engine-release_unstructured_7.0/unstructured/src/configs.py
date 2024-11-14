import sys
import os
from logs import get_logger
from configparser import ConfigParser
import importlib
import json
import yaml
import ast
import pandas as pd
import requests
from requests import ConnectTimeout , ReadTimeout


logger = get_logger()
default_error_message = "Inside configs.py - An error occured on line {}. Exception type: {} , Exception: {} "

config_dir = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "config")
model_dir_path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "models")
src_dir = os.path.dirname(os.path.abspath(__file__))


def load_config(filename, lang = None):
    try:
        # name = os.path.splitext(filename)[0]
        ext = os.path.splitext(filename)[-1]
        if lang is None:
            current_dir = config_dir
        else:
            current_dir = os.path.join(config_dir, f"config_{lang}")

        config_file_path = os.path.join(current_dir, f"{filename}")

        if ext == ".ini":
            config = ConfigParser()
            config.read(config_file_path)
            return config
        elif ext == ".pkl":
            current_dir = os.path.join(src_dir, f"src_{lang}")
            config_file_path = os.path.join(current_dir, "semantic_embeddings" , f"{filename}")
            # config_file_path = os.path.join(config_file_path, "semantic_embeddings")
            df = pd.read_pickle(config_file_path)
            return df
        elif ext == ".json":
            with open(config_file_path, 'r') as file:
                config_json = json.load(file)
            return config_json
        elif ext == ".yaml":
            with open(config_file_path, 'r') as file:
                config_yaml = yaml.safe_load(file)
            return config_yaml
        elif ext == ".csv":
            csv_file = pd.read_csv(config_file_path)
            return csv_file
        else:
            raise Exception("File type not supported")
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


global_config = load_config("config.ini", None)
model_config_json = load_config("model_config.json", None)
entity_json = load_config("entity_switches.json", "en")
pvi_ref_json = load_config("unstruct_reference_json.json", None)

languages_config = {}
languages = ast.literal_eval(global_config['LANGUAGE_SUPPORTED']['languages'])
for lang in languages:
    languages_config[lang] = load_config(f'config_file_{lang}.json', lang)



def get_codelist_data(language="en" , raise_on_failure=False):
    """
    Fetch codelist data from pvcm app api.
    read url , header token from config.ini file
    """
    try:
        logger.info("Fetching codelist data")
        service_url = global_config["GET_CODE_LIST"]["PVCM_SERVICE_URL"]
        codelist_endpoint = global_config["GET_CODE_LIST"]["CODELIST_ENDPOINT"]
        if service_url.endswith("/"):
            url = service_url + codelist_endpoint
        else:
            url = service_url + "/" + codelist_endpoint
        url = url + f"?language={language}" if not url.endswith("/") else url[:-1] + f"?language={language}"
        header = {"PVI_PUBLIC_TOKEN": global_config["GET_CODE_LIST"]["PVI_PUBLIC_TOKEN"]}
        response = requests.get(url=url, headers=header, timeout=int(global_config["GET_CODE_LIST"]["API_TIME_OUT"]))
        if response.status_code == 200:
            logger.info("Fetched codelist data")
            return response.json()["result"]
        else:
            logger.error("code list api response not received")
            return None
    except (ConnectTimeout ,ReadTimeout) as e:
        logger.error("code list api response not received timeout error")
        if raise_on_failure:
            raise e
        return None
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def update_validator_config(language, redis_obj, raise_on_failure=False):
    try:
        latest_code_list = get_codelist_data(language, raise_on_failure)
        if latest_code_list is None: #todo comment it 
            logger.error("Error in fetching Code List API")
            #TO DELETED
            # validator_json = load_config('validator_config.json', language)
            # redis_obj.save_to_redis(validator_json , language)
            return None
        code_list_mapping = {'weightUnits': 'Weight_Unit',
                          'heightUnits': 'Height_Unit',
                          'ageGroups': 'Age_Group',
                          'ageUnits': 'Age_Unit',
                          'sourceTypes': 'SOURCE_TYPE',
                          'studyTypes': 'STUDY_TYPE',
                          'reporterTypes': 'REPORTER_OCCUPATION',
                          'outcomes': 'EVENT_OUTCOME',
                          'race': 'ETHNICITY',
                          'ethnicGroup': 'ETHNIC_GROUP',
                          'route': 'ROUTE',
                          'dosageFrequency': 'FREQ',
                          'dosageForm': 'FORMULATION'
                          }
        code_list = {}
        for key, value in code_list_mapping.items():
            new_values = []
            for item in latest_code_list.get(key, {}):
                new_values.append(item.get('text'))
            code_list[value] = new_values
        validator_json = load_config('validator_config.json', language)
        validator_json['Weight_Unit'] = code_list['Weight_Unit']
        validator_json['Height_Unit'] = code_list['Height_Unit']
        validator_json['Age_Group'] = code_list['Age_Group']
        validator_json['Age_Unit'] = code_list['Age_Unit']
        validator_json['Code_list'][0]['SOURCE_TYPE'] = code_list['SOURCE_TYPE']
        validator_json['Code_list'][0]['STUDY_TYPE'] = code_list['STUDY_TYPE']
        validator_json['Code_list'][0]['REPORTER_OCCUPATION'] = code_list['REPORTER_OCCUPATION']
        validator_json['Code_list'][0]['EVENT_OUTCOME'] = code_list['EVENT_OUTCOME']
        validator_json['Code_list'][0]['ETHNICITY'] = code_list['ETHNICITY']
        validator_json['Code_list'][0]['ETHNIC_GROUP'] = code_list['ETHNIC_GROUP']
        validator_json["FREQ"] = code_list['FREQ']
        validator_json["ROUTE"] = code_list['ROUTE']
        validator_json["FORMULATION"] = code_list['FORMULATION']

        current_dir = os.path.join(config_dir, f"config_{language}")
        validator_config_file_path = os.path.join(current_dir, 'validator_config.json')
        with open(validator_config_file_path, 'w') as file:
            json.dump(validator_json, file, ensure_ascii=False, indent=2)

        #update to redis
        redis_obj.save_to_redis(validator_json , language)

    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e
# validator_config_ja :()

def load_updated_config(redis_obj, raise_on_failure=False):
    try:
        for lang in languages:
            update_validator_config(lang, redis_obj, raise_on_failure)

    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def load_validator(language):
    try:
        validator_module_name = f'src_{language}.validator'
        if validator_module_name not in globals():
            validator_module = importlib.import_module(validator_module_name)            
            globals()[validator_module_name] = validator_module
        else:
            validator_module = globals()[validator_module_name]
        return validator_module
    except ModuleNotFoundError as e:
        logger.error(f"Module src_{language}.validator not found.")
        raise e
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def load_utils(language):
    try:
        util_module_name = f'src_{language}.utils_{language}'
        if util_module_name not in globals():
            utils_module = importlib.import_module(util_module_name)
            globals()[util_module_name] = utils_module
        else:
            utils_module = globals()[util_module_name]
        return utils_module
    except ModuleNotFoundError as e:
        logger.error(f"Module src_{language}.utils not found.")
        raise e
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


# def load_spam_detection(language):
#     try:
#         # spam_detection_module_name = "src_en.spam_detection_en"
#         spam_detection_module_name = f'src_{language}.spam_detection'
#         if spam_detection_module_name not in globals():
#             spam_module = importlib.import_module(spam_detection_module_name)
#             globals()[spam_detection_module_name] = spam_module
#         else:
#             spam_module = globals()[spam_detection_module_name]
#         return spam_module
#     except ModuleNotFoundError as e:
#         logger.error(f"Module src_{language}.spam_detection not found.")
#         raise e
#     except Exception as e:
#         logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
#         raise e
