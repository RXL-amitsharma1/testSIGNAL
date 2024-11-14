from sentence_transformers import SentenceTransformer
import pandas as pd
import sys
import os
from logs import get_logger

logger = get_logger()
default_error_message = "Inside semantic_embedding.py - An error occured on line {}. Exception type: {} , Exception: {} "

from configs import model_config_json , model_dir_path ,languages ,src_dir ,load_config

model = SentenceTransformer(os.path.join(model_dir_path, model_config_json["semantic_model_path"]))


# api to get the latest code list ,uncomment when api is ready
"""
def get_latest_code_list(url=URL):
    myobj = {'somekey': 'somevalue'}
    latest_code_list = requests.post(url, data = myobj)
        
    return latest_code_list
"""


# Method to generate pickle file for first time
# def generate_pickle(language, sematic_path,redis_obj):
#     try:
#         CODE_LIST = redis_obj.fetch_data('Code_list', language)
#         for entity in CODE_LIST[0].keys(): #globals()[f'CODE_LIST_{language}']:
#             df = pd.DataFrame(columns=[entity, "embedding"])
#             code_list = CODE_LIST[0][entity] 
#             code_list = list(map(str.strip , code_list))#[each.strip() for each in code_list] # can use map function
#             df[entity] = code_list
#             # save_to_configs
#             save_path = os.path.join(sematic_path, f"CODE_LIST_{entity}".lower() + ".pkl") # need prefix here only
#             df.to_pickle(save_path)
#     except Exception as e:
#         logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
#         raise e

# Method to generate pickle file for first time
def generate_embedding_pickle(language, sematic_path, redis_obj):
    try:
        # To be uncommented when API will be integrated from PVI
        CODE_LIST = redis_obj.fetch_data('Code_list', language)
        SEMANTIC_CONTEXT_MAP =  redis_obj.fetch_data('semantic_context_map', language)

        for entity in CODE_LIST[0].keys():
            df = pd.DataFrame(columns=[entity, "embedding"])
            code_list = CODE_LIST[0][entity] 
            code_list = list(map(str.strip , code_list))#[each.strip() for each in code_list] # can use map function
            df[entity] = code_list
            code_list_context = SEMANTIC_CONTEXT_MAP[entity]["code_list_context"]

            # code_list = list(map(str.lower ,code_list)) #[each.lower() for each in code_list] # can do it in same loop or map function
            # code_list = [each.replace("/", " or ") for each in code_list]
            # code_list = [each.replace("-", " ") for each in code_list]
            copy_code = [0] * len(code_list)
            for j in range(len(code_list)):
                try:
                    copy_code[j] = model.encode([f"{code_list_context}+{code_list[j]}"])[0]
                except Exception as e:
                    logger.warning(f"Error while extracting embedding for term: '{copy_code[j]}'")
                    logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
                    raise e
            df["embedding"] = copy_code
            save_path = os.path.join(sematic_path, f"CODE_LIST_{entity}".lower() + ".pkl") # need prefix here only
            df.to_pickle(save_path)
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def update_semantic_embeddings(redis_obj):
    for lang in languages:
        sematic_embeddings_path = os.path.join(
            os.path.join(os.path.join(src_dir, f"src_{lang}"), "semantic_embeddings"))
        os.makedirs(sematic_embeddings_path, exist_ok=True)

        generate_embedding_pickle(lang, sematic_embeddings_path, redis_obj)

