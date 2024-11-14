import sys
import json
from flask import request, Flask, render_template

from utils import custom_sent_tokenize , remove_negated_ent
from os import path

parent_dir = path.dirname(path.dirname(path.abspath(__file__)))
sys.path.append(parent_dir)

from logs import get_logger
import prepare_json_unstructure
import post_process_json
import pandas as pd
import semantic_coder
import requests
import gc

from copy import deepcopy
from time import time
from  src_en.extract_from_models import create_df_extract_ents
from configs import global_config, pvi_ref_json, languages_config, \
    load_validator, load_utils, load_updated_config
from redis_client import RedisClient
redis_obj = RedisClient()
from case_clasifier import case_classification

app = Flask(__name__, static_folder='./docs/static',
            static_url_path='/static', template_folder="./docs/templates")
app.config["DEBUG"] = False

logger = get_logger()
default_error_message = ("Inside unstructured_pipeline.py - An error occured on line {}. Exception type: {} , "
                         "Exception: {}")

def create_df_extract_ents_chatgpt(narrative, language):
    try:
        sentences = custom_sent_tokenize(str(narrative), language)
        request_data = {"narrative": narrative, "language": language}

        r = requests.post("http://{}:{}/predict/openai".format(global_config["OPENAI_API"]["URL"],
                                                               global_config["OPENAI_API"]["PORT"]),
                          data=request_data)
        if r.status_code == 200:
            r = r.json()
            if r['code'] == 1:
                raise Exception(f"Exception: Call to openai_api.py failed: {r['message']}")
        else:
            raise Exception(f"Exception: Call to openai_api.py failed.")

        openai_output = r['entities']
        derived_entity = r['derived_entities']
        df_derived_entity = pd.json_normalize(derived_entity)
        df_extracted_ent = pd.json_normalize(openai_output)
        if not df_extracted_ent.empty:
            df_extracted_ent.loc[(df_extracted_ent["entity_label"] == "TEST_NAME"), "entity_label"] = "LAB_TEST_NAME"
            df_extracted_ent.loc[(df_extracted_ent["entity_label"] == "BATCH_LOT_NUM"), "entity_label"] = "DOSE_BATCH_LOT_NUM"
            df_extracted_ent.loc[(df_extracted_ent["entity_label"] == "EVENT_START_DATE"), "entity_label"] = "ONSET"
            df_extracted_ent = df_extracted_ent.sort_values(by="conf_score", ascending=False)
            df_extracted_ent.drop_duplicates(subset=df_extracted_ent.columns.difference(['conf_score']),
                                             keep='first', inplace=True)
            df_extracted_ent.sort_values(['sent_id', 'start'], ascending=[True, True], inplace=True)

        return df_extracted_ent, sentences, df_derived_entity
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def unstructured_prediction(text, language):
    try:
        """
        Unstructured prediction function
        """
        utils_module = load_utils(language) # loads language specific utils module
        text = utils_module.preprocess_text(text) # duplicate definitions in both utils
        logger.debug(f"text after preprocessing is {text}")

        if language == 'en' and languages_config[language]["openai"]["switch"] is False:
            df_extracted_ent, sentences, df_derived_entity = create_df_extract_ents(text)
        else:
            df_extracted_ent, sentences, df_derived_entity = create_df_extract_ents_chatgpt(text, language)

        if type(df_extracted_ent) is dict:  #when sentence limit is exceeded, 'code' sent is 1
            return df_extracted_ent

        _pvi_ref_json = deepcopy(pvi_ref_json)

        #return refer json when no entity is extracted
        if df_extracted_ent.empty:
            return _pvi_ref_json

        validator = load_validator(language)
        for index, row in df_extracted_ent.iterrows():
            df_extracted_ent = validator.validate(row['entity_label'], row['entity_text'], row['sentence'],
                                                  df_extracted_ent, index, redis_obj)

        # remove negated entities and keep non-negated entities
        df_extracted_ent = remove_negated_ent(df_extracted_ent)
        # semantic matching
        df_extracted_ent = semantic_coder.code(df_extracted_ent, language ,redis_obj)
        # populates json values
        json_final = prepare_json_unstructure.prepare_final_json(_pvi_ref_json, df_extracted_ent, text,
                                                                 sentences, df_derived_entity, language, redis_obj)
        # value set operation
        json_final = post_process_json.post_process(json_final, language)

        return json_final
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e
    finally:
        gc.collect()


@app.route('/codelist/update')
def refresh_code_list():
    try:
        load_updated_config(redis_obj)
        update_semantic_embeddings(redis_obj)
        logger.info("Codelist saved to Redis, semantic embedding updated from '/codelist/update'")
        return {"message": "Configs refreshed for Unstructured"}, 200
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        return {"message": "Codelist refresh failed"}, 500


@app.route('/docs')
def swagger_ui():
    """
    Api endpoint to render swagger based ui for showing API documentations
    """
    try:
        app_name = app.name  # Sets title for html page
        base_url = app.static_url_path  #directoty consisting of css and js files  # Assuming your static files are served from /static directory
        config_json = {
            # "app_name": app_name,
            "dom_id": "#swagger-ui",
            "url": "/static/openapi.yml",  #documentation file path
            "layout": "StandaloneLayout",  #use not sure
            "deepLinking": True,  #use not sure
        }
        # Render the HTML template with dynamic variables
        #html ,css,js fetched from https://github.com/sveint/flask-swagger-ui
        return render_template("index.template.html", app_name=app_name, base_url=base_url,
                               config_json=json.dumps(config_json))
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e

@app.route('/unstruct/health' , methods=["GET"])
def health_check():
    try:
        return {"message": "Healthy"}, 200
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))

@app.route('/unstruct/live', methods=['POST'])
def main_function():
    try:
        """
        API end point function to call unstructured parsing module
        """
        logger.info("********* Request Received! *********")
        if "text" in request.form:
            text = request.form["text"]
        elif "file" in request.files:
            text = request.files["file"].read().decode("UTF-8")
        else:
            logger.info(f"Unable to identify text in the hit") #todo message
            return {"message": "Unable to identify text in the hit"}, 500
        
        logger.debug(f"text from form is: \n {text}")
        
        if not text:
            return {"message": "No valid text/Empty text"}, 204
        if "language" in request.form:
            language = request.form["language"]
        else:
            logger.info("Missing language parameter.")
            return {"message": "Missing language parameter."}, 204

        st = time()
        logger.info("Request validated. Proceeding to prediction.")
        spam_flag = False if "spam_flag" in request.headers and request.headers.get('spam_flag').lower() == 'false' else True

        case_categories = case_classification(text)

        if case_categories.get("categories") and "spam" in [x.lower() for x in case_categories.get("categories")] and spam_flag:
            case_categories["categories"] = ["SPAM"]
            case_categories["code"] = 4
            case_categories["message"] = "Spam"
            # case_categories["spam_acc"] = case_categories.get("categories_acc" , 0.85)
            case_categories["sourceLanguage"] = language
            return case_categories, 200
        else:
            if not spam_flag and "SPAM" in case_categories["categories"]:
                case_categories["categories"].remove("SPAM")

            output_json = unstructured_prediction(text, language)
            output_json["sourceLanguage"] = language

            product_exists = False if len(output_json.get("products" , [])) ==1 and not output_json.get("products" , [])[0].get("license_value") else True
            event_exists = False if len(output_json.get("events" , [])) == 1 and not output_json.get("events" , [])[0].get("reportedReaction") else True

            _case_categories = []
            for cat in case_categories.get("categories"): # OTHER_MEDICAL is meant to send
                if "ae" == cat.lower() and product_exists and event_exists:
                    _case_categories.append(cat)
                elif cat.lower() in [ "mi" , "pqc"] and product_exists:
                    _case_categories.append(cat)

            case_categories["categories"] = _case_categories
            if len(_case_categories) == 0:
                case_categories["categories_acc"] = None

            output_json.update(case_categories)

        logger.info(f"Time taken to process Request: {time() - st} secs")
        return output_json, 200

    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        logger.info(f"Time taken to process Request: {time() - st} secs")
        return {"message": "Error occured while extracting entities"}, 500


if __name__ == "__main__":
    load_updated_config(redis_obj, raise_on_failure=False)
    from semantic_embedding import update_semantic_embeddings
    update_semantic_embeddings(redis_obj)
    print("Codelist saved to Redis, semantic embedding updated from unstructure_pipeline")
    app.run(host=global_config["PIPELINE_API"]["URL"], port=global_config["PIPELINE_API"]["PORT"])
