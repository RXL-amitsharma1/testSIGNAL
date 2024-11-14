import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
from sentence_transformers import SentenceTransformer
from os import listdir, path
import sys
from configs import  model_config_json , model_dir_path, load_config, src_dir
import pandas as pd
from logs import get_logger
logger = get_logger()
default_error_message = "Inside semantic_coder.py - An error occured on line {}. Exception type: {} , Exception: {} "

model = SentenceTransformer(path.join(model_dir_path, model_config_json["semantic_model_path"]))


def similar_match(text, entity, code_list_dict):
    try:
        try:
            code_list = code_list_dict[entity.upper()]['embedding'].tolist()
        except KeyError:
            code_list = []
        text_embedding = model.encode([text])
        cosine_combo = cosine_similarity(text_embedding, code_list)
        score = max(cosine_combo[0])
        ix = np.where(cosine_combo == max(cosine_combo[0]))[1][0]
        # col_name = 'CODE_LIST_' + entity.upper()
        best_match = code_list_dict[entity.upper()][entity.upper()][ix]
        if score > 0.8:
            return best_match
        return np.nan
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e

def code(df,lang ,redis_obj):
    try:
        entities = redis_obj.fetch_data("Semantic_Validation" , lang)
        sematic_embeddings_path = path.join(
            path.join(path.join(src_dir, f"src_{lang}"), "semantic_embeddings"))
        onlyfiles = [file for file in listdir(sematic_embeddings_path)] # check extensions ? and isfile ?
        code_list_dict = {}
        for file in onlyfiles:
            ent_name = "_".join(file.split("_")[2:]).split(".")[0].upper()
            code_list_dict[ent_name] = load_config(file, lang)
        df["semantic_match"] = np.nan
        df.reset_index(inplace=True, drop=True)
        for ent in entities:
            for index, row in df.iterrows():
                if row['entity_label'].lower() == ent.lower():
                    input_text_context = redis_obj.fetch_data("semantic_context_map", lang)[ent.upper()]["input_string_context"]
                    best_match = similar_match(f"{input_text_context}+{row['entity_text']}", ent, code_list_dict)
                    default = redis_obj.fetch_data("semantic_context_map", lang)[ent.upper()]["default"]
                    if pd.isna(best_match) and default:
                        best_match = default
                    df['semantic_match'][index] = best_match
        return df
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e
