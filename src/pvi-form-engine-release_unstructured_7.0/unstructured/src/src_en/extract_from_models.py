from copy import deepcopy
from src_en.combine_model_output import combine
from logs import get_logger
from src.utils import get_coref_data, fix_age_unit, classify_parent, convert_str_key_to_tuple, \
    BiobertNegation, custom_sent_tokenize
from configs import global_config, model_config_json as config_json
from time import time


import sys
import requests
import pandas as pd
import concurrent.futures


bio_neg_obj = BiobertNegation()
logger = get_logger()
default_error_message = ("Inside unstructured_pipeline.py - An error occured on line {}. Exception type: {} , "
                         "Exception: {}")


def update_entity_spans(entities, coref_replacement):
    try:
        new_entities = deepcopy(entities)
        sorted_keys = sorted(coref_replacement, key=lambda k: coref_replacement[k][1][0])

        for k in sorted_keys:
            for ent in new_entities['entities']:
                if coref_replacement[k][1][0] <= ent['start']:
                    offset = len(k[0]) - len(coref_replacement[k][0])

                    if ent['entity_label'] == "PARENT_GENDER" and coref_replacement[k][1][0] <= ent['start'] < \
                            coref_replacement[k][1][1]:
                        label = classify_parent(k[0])
                        if label is None:
                            ent['entity_label'] = None
                        else:
                            _extra = []
                            for lbl in k[0].split():
                                index = lbl.find(label)
                                if index != -1:
                                    index = index + len("".join(_extra))
                                    ent['entity_text'] = lbl
                                    ent['start'] = k[1][0] + index
                                    ent['end'] = k[1][0] + index + len(lbl)
                                    break
                                else:
                                    _extra.append(lbl + " ")
                    else:
                        ent['start'] = ent['start'] + offset
                        ent['end'] = ent['end'] + offset

            # drop ent with parent_gender entity if org text does not mention text.
            new_entities['entities'] = [ent for ent in new_entities['entities'] if ent['entity_label'] is not None]
        return new_entities
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def get_model_response(url, sentence, sentence_id, default_dict, coref_replacement):
    """
    Function to get reponse from spacy and bert api parallelly
    """
    try:
        logger.debug(f"Fetching result from apis for sent_id :{sentence_id}")
        response = requests.post(url, data={"sentence": sentence, "sentence_id": sentence_id})
        model_output = response.json() if response.status_code == 200 else default_dict
        model_updated_output = update_entity_spans(model_output, coref_replacement)
        logger.debug(model_updated_output)
        return model_updated_output
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def extract_entities(sent_id, sentence, coref_replacement, pqc_flag=None):
    try:
        st = time()
        default_dict = {"entities": []}
        spacy_url = "http://{}:{}/predict/spacy".format(global_config["SPACY_API"]["URL"],
                                                        global_config["SPACY_API"]["PORT"])
        biobert_url = "http://{}:{}/predict/bert".format(global_config["BIOBERT_API"]["URL"],
                                                         global_config["BIOBERT_API"]["PORT"])
        pqc_url = "http://{}:{}/predict/pqc".format(global_config["SPACY_API"]["URL"],
                                                    global_config["SPACY_API"][
                                                        "PORT"])  #TODO correct it after confirmation

        if pqc_flag is not None:
            pqc_output = default_dict.copy()
            if config_json["pqc"]["switch"]:
                r = requests.post(pqc_url, data={"sentence": sentence, "sentence_id": sent_id})
                pqc_output = r.json() if r.status_code == 200 else pqc_output
            combined_output = combine(pqc_output)
        else:
            # spacy_output, bert_output = default_dict.copy(), default_dict.copy()
            entities = []
            if config_json["spacy"]["switch"] and config_json["bert"]["switch"]:
                url_list = [spacy_url, biobert_url]

                MAX_THREADS = len(url_list)  #number of maximum threads
                # result_list = []
                with concurrent.futures.ThreadPoolExecutor(max_workers=MAX_THREADS) as executor:
                    futures = [executor.submit(get_model_response, url, sentence, sent_id, default_dict.copy(),
                                               coref_replacement) for url in url_list]
                    for future in concurrent.futures.as_completed(futures):
                        try:
                            entities.extend(future.result()["entities"])
                        except Exception as e:
                            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
                            raise e

            elif config_json["spacy"]["switch"] and not config_json["bert"]["switch"]:
                r = requests.post(spacy_url, data={"sentence": sentence, "sentence_id": sent_id})
                _spacy_output = r.json() if r.status_code == 200 else default_dict.copy()
                spacy_output = update_entity_spans(_spacy_output, coref_replacement)
                entities.extend(spacy_output['entities'])
            elif config_json["bert"]["switch"] and not config_json["spacy"]["switch"]:
                stb = time()
                r = requests.post(biobert_url, data={"sentence": sentence, "sentence_id": sent_id})
                logger.debug(f"{time() - stb} secs spent in bert api request ")
                _bert_output = r.json() if r.status_code == 200 else default_dict.copy()
                bert_output = update_entity_spans(_bert_output, coref_replacement)
                entities.extend(bert_output['entities'])
        combined_output = combine(entities)
        logger.debug(f"Time taken by spacy and bert model to extract entites: {time() - st} secs")
        return combined_output
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def create_df_extract_ents(text):
    try:

        ls_extracted_ent_final = []
        # text = utils.data_cleaning(text)
        sentences = custom_sent_tokenize(str(text), 'en')
        logger.info(f"Number of sentences received in input : {len(sentences)}")
        if len(sentences) > 200:
            return {"code": 1, "message": "cannot process text, too long"}, 200, {}
        data, _debug, coref_replacements = get_coref_data(text)
        coref_replacements = convert_str_key_to_tuple(coref_replacements)
        _debug = convert_str_key_to_tuple(_debug)
        sentences_coref = custom_sent_tokenize(str(data), 'en')

        logger.info("Sentence tokenization completed!")
        for index, sentence_coref in enumerate(sentences_coref):
            sentence_coref = sentence_coref.strip()
            sent_id = index + 1
            # if not pqc_flag:
            ls_extracted_ent_final.extend(extract_entities(sent_id, sentence_coref, coref_replacements[index]))
            # else:
                # ls_extracted_ent_final.extend(
                    # extract_entities(sent_id, sentence_coref, coref_replacements[index], pqc_flag))
        # Partition entities based on "model" attribute
        biobert_entities = []
        non_biobert_entities = []
        for entity in ls_extracted_ent_final:
            (biobert_entities if entity["model"] == "biobert" else non_biobert_entities).append(entity)

        bio_bert_response = bio_neg_obj.negate_entities(biobert_entities)  #get negation for biobert result
        ls_extracted_ent_final = non_biobert_entities + bio_bert_response  #update negated bobert response
        df_extracted_ent = pd.json_normalize(ls_extracted_ent_final)
        logger.debug(df_extracted_ent.head())

        if not df_extracted_ent.empty:
            df_extracted_ent.loc[(df_extracted_ent["entity_label"] == "TEST_NAME"), "entity_label"] = "LAB_TEST_NAME" #TODO handle at same location where onset 
            df_extracted_ent.loc[(df_extracted_ent["entity_label"] == "EVENT_START_DATE"), "entity_label"] = "ONSET"
            df_extracted_ent.loc[(df_extracted_ent["entity_label"] == "GENDER"), "entity_label"] = \
                    df_extracted_ent.loc[(df_extracted_ent["entity_label"] == "GENDER"), "entity_label"].map("PAT_GENDER")


            df_extracted_ent = df_extracted_ent.sort_values(by="conf_score", ascending=False)
            df_extracted_ent.drop_duplicates(subset=df_extracted_ent.columns.difference(['conf_score']),
                                             keep='first', inplace=True)
            df_extracted_ent.sort_values(['sent_id', 'start'], ascending=[True, True], inplace=True)
            # df_extracted_ent = fix_age_unit(df_extracted_ent)   #tODO NEED CONFIRMAATION
        df_derived_entity = pd.DataFrame()
        return df_extracted_ent, sentences, df_derived_entity
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e
