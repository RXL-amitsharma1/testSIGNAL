import json
import re
import joblib
import numpy as np

from dateutil.parser import parse
import sys

# from requests import ConnectTimeout

from logs import get_logger
from ast import literal_eval
import requests
import spacy
from negspacy.negation import Negex
from negspacy.termsets import termset
from spacy.training.example import Example
from copy import deepcopy
import time
from configs import model_dir_path, global_config
from datetime import datetime

logger = get_logger()
default_error_message = "Inside utils.py - An error occured on line {}. Exception type: {} , Exception: {} "

from nltk.tokenize.punkt import PunktSentenceTokenizer, PunktParameters

punkt_param = PunktParameters()

try:
    abbrev_list = set(literal_eval(global_config["PIPELINE_API"]["ABBREV_WORDS"]))
except Exception as e:
    logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
    abbrev_list = set(["no", "b.i.d"])
punkt_param.abbrev_types = abbrev_list  # adding 'no' in abbrevation to handle 'lot no. ' scenario
english_tokenizer = PunktSentenceTokenizer(punkt_param)

from konoha import SentenceTokenizer

japanese_tokenizer = SentenceTokenizer()


def my_converter(obj):
    try:
        if isinstance(obj, np.integer):
            return int(obj)
        elif isinstance(obj, np.floating):
            return float(obj)
        elif isinstance(obj, np.ndarray):
            return obj.tolist()
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def fix_age_unit(df):
    try:
        if "AGE_UNIT" in list(df.entity_label) and "PAT_AGE" in list(df.entity_label):
            age_unit = df.loc[df['entity_label'] == 'AGE_UNIT', 'entity_text'].values[0]
            age = df.loc[df['entity_label'] == 'PAT_AGE', 'entity_text'].values[0]
            age_end = df.loc[df['entity_label'] == 'AGE_UNIT', 'end'].values[0]
            df.loc[df['entity_label'] == 'PAT_AGE', 'entity_text'] = age + " " + age_unit
            df.loc[df['entity_label'] == 'PAT_AGE', 'end'] = age_end
        return df
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


# def data_cleaning(text):
#     try:
#         text = re.sub(r"\n+", "\n", text)
#         text_ls = text.split("\n")
#         for line in text_ls:
#             if line.startswith("Sent:") or line.startswith("To:") or line.startswith("To :") or line.startswith(
#                     "T o :") or line.startswith("T o:"):
#                 text_ls = text_ls[text_ls.index(line) + 1:]
#         text = "\n".join(text_ls)
#         text = text.replace("{", "(")
#         text = text.replace("}", ")")
#         data = re.sub(r"\s+", " ", text)
#         data = re.sub(r"\\\\", "", data)
#         return data
#     except Exception as e:
#         logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
#         raise e


def remove_duplicate_products(product_json):
    try:
        for first_prod in product_json:
            for second_prod in product_json:
                if first_prod["license_value"].lower() == second_prod["license_value"].lower():
                    if str(first_prod).count("None") > str(second_prod).count("None"):
                        product_json.remove(first_prod)
                    elif str(first_prod).count("None") < str(second_prod).count("None"):
                        product_json.remove(second_prod)
                    break
        return product_json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def date_preprocess(dt_st):
    """
    preprocess function for date string processing ,
    removes extra spaces, replaces other separators with '-'
    """
    try:
        dt_st = re.sub(r"\s+", " ", dt_st)
        dt_st = re.sub(r"-?\s+?", "-", dt_st)
        dt_st = re.sub(r"[\?\$\#\^*,/.-]+", "-", dt_st).strip("- )(")
        return dt_st
    except Exception as e:
        # logger.warning(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        # given_date = re.sub(r'\D', '', given_date)
        # if given_date in ("continuuing"):
        #     return given_date
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def date_format_update(given_date):
    """
    function to convert date string in specific format("04-Aug-2000")
    """
    try:
        months = {1: 'Jan', 2: 'Feb', 3: 'Mar', 4: 'Apr', 5: 'May', 6: 'Jun',
                  7: 'Jul', 8: 'Aug', 9: 'Sep', 10: 'Oct', 11: 'Nov', 12: 'Dec'}
        default1 = datetime.strptime("29-02-1600", "%d-%m-%Y")
        default2 = datetime.strptime("01-01-1700", "%d-%m-%Y")
        given_date = date_preprocess(given_date)

        try:
            dt1 = parse(given_date, default=default1, fuzzy=True)
            dt2 = parse(given_date, default=default2, fuzzy=True)
        except ValueError as e:
            logger.warning("error in parsing date trying dayfirst")
            dt1 = parse(given_date, default=default1, fuzzy=True, dayfirst=True)
            dt2 = parse(given_date, default=default2, fuzzy=True, dayfirst=True)
        if dt1.year == dt2.year:
            year = dt1.year
        else:
            year = ""
        if dt1.month == dt2.month:
            month = months[dt1.month]
        else:
            month = ""

        if dt1.day == dt2.day:
            date = str(dt1.day) if len(str(dt1.day)) == 2 else f"0{str(dt1.day)}"
        else:
            date = ""

        return f"{date}-{month}-{year}".strip("- ")


    except ValueError as e:
        logger.warning("Ambiguity in date so trying parsing only year")
        date, year, month = "", "", ""
        partial_date_match = re.match(r"([A-Za-z\s-]*)?(\d+)-((\d{4})|(\d{2}))", given_date)  #examples 22-2022, 20-20

        if partial_date_match:
            date = partial_date_match.group(2)
            year = partial_date_match.group(3)
            return f"{year}".strip("- ")
        else:
            given_date = re.sub(r'\D', '', given_date)
            if given_date in ("continuuing"):
                return given_date
            else:
                return given_date.strip()
    except Exception as e:
        # logger.warning(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        # given_date = re.sub(r'\D', '', given_date)
        # if given_date in ("continuuing"):
        #     return given_date
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def convert_spacy_out_to_dict_format(output):
    try:
        for text, entity_dict in output:
            for index, entity in enumerate(entity_dict["entities"]):
                entity_dict["entities"][index] = {
                    "sent_id": entity[0],
                    "tag": entity[1],
                    "text": entity[2],
                    "start": entity[3],
                    "end": entity[4],
                    "conf_score": entity[5],
                    "sentence": entity[6]
                }
        return output
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def get_model_metadata(input_list: list, flag: str):
    try:
        """
        function to return list with all the model name and tags if switch value true
        """
        empty_list = []
        for each in input_list:
            for key, value in each.items():
                if value['switch']:
                    if flag == 'name':
                        empty_list.append(key)
                    elif flag == 'tags':
                        meta_data = joblib.load(model_dir_path + "/bert_models/{}/{}".format(key, value["metadata"]))
                        enc_tag = meta_data["enc_tag"]
                        empty_list.append(enc_tag)
                    elif flag == "conf":
                        with open(model_dir_path + "/spacy_models/{}/meta.json".format(key), "r") as f:
                            meta_json = json.load(f)
                            empty_list.append(meta_json["performance"]["ents_f"])
        return empty_list
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


# not being used currently...can be used in future
def date_format(date_string):
    try:
        date_pattern = re.compile(r"\d{1,2}\s*\/\s*\d{1,2}\s*\/\d{4}")
        months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
        if len(date_pattern.findall(date_string)) > 0:
            try:
                date_string = parse(date_pattern.findall(date_string)[0])
                day = date_string.day
                mon = months[date_string.month - 1]
                yr = date_string.year

                if int(day) < 10:
                    day = "0" + str(day)
                '''
                if int(mon) < 10:
                    mon = "0" + str(mon)
                '''

                date = str(day) + "-" + str(mon) + "-" + str(yr)

                return date
            except:
                return date_string

        else:
            return date_string
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def classify_parent(text):
    """
    Copied it from coref Class so that no instance of coref class is needed
    """
    try:
        text = text.lower()
        parent_phrases = ['mother', 'father']

        is_parent = None
        for par_phrase in parent_phrases:
            if par_phrase in text:
                is_parent = par_phrase
                break

        return is_parent
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def get_coref_data(text):
    try:
        coref_api_url = "http://{}:{}/predict/coref".format(global_config["COREF_API"]["URL"],
                                                            global_config["COREF_API"]["PORT"])
        response = requests.post(coref_api_url, data={"text": text})
        if response.status_code == 200:
            response = response.json()
            response["data"], response["_debug"], response["coref_replacements"] = response[
                "data"], convert_str_key_to_tuple(response["_debug"]), convert_str_key_to_tuple(
                response["coref_replacements"])
            return response["data"], response["_debug"], response["coref_replacements"]

        else:
            logger.error(f"Exception occured in api response :{response.json()['message']}")
            raise Exception("No response from coref api")

    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def convert_str_key_to_tuple(input_dict):
    """
    updated dictionary key datatype from str to tuple , so that is can be converted to expected datatype
    """
    try:
        for key in deepcopy(list(input_dict.keys())):
            if isinstance(key, str):
                try:
                    input_dict[literal_eval(key)] = input_dict[key]
                    del input_dict[key]
                except ValueError as e:
                    pass
            if isinstance(key, int):
                convert_str_key_to_tuple(input_dict[key])
        return input_dict
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def convert_keys_to_str(response):
    """
    updated dictionary key datatype from tuple to str , so that is can be converted to json
    """
    try:
        for res in deepcopy(list(response)):
            if isinstance(res, tuple):
                response[str(res)] = response[res]
                del response[res]
            elif isinstance(res, int):
                convert_keys_to_str(response[res])
        return response
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


# required only for old bert models
# def update_bert_model(old_path, num_labels, new_path):
#     from transformers import BertForTokenClassification
#     import torch
#     from collections import OrderedDict
#
#     def change(st):
#         new_st = OrderedDict()
#         for k, v in st.items():
#             name = k[7:]
#             new_st[name] = v
#         return new_st
#
#     model = BertForTokenClassification.from_pretrained("biobert_v1.1_pubmed", num_labels=num_labels)
#     state_dict = torch.load(old_path, map_location=torch.device('cpu'))
#     if "height" in old_path or "source" in old_path:
#         new_state_dict = state_dict
#     else:
#         new_state_dict = change(state_dict)
#     model.load_state_dict(new_state_dict, strict=False)
#     torch.save(model.state_dict(), new_path)
#     print("Saved new model to", new_path)

class BiobertNegation:
    """
    Checks Negation in biobert extracted entities
    """

    def find_nonoverlapping_ranges(self, entities):
        """
        Return 2 entities lists:
        list_1 having entities with non-overlapping spans.
        list_2 having entities with overlapping spans.
        """
        try:
            sorted_ranges = entities
            non_overlapping = []
            overlapping = []
            current_range = sorted_ranges[0]

            for ent in sorted_ranges[1:]:
                start, end = ent['start_1'], ent['end_1']
                if start > current_range['end_1']:
                    non_overlapping.append(current_range)
                    current_range = ent
                else:
                    overlapping.append(ent)
            non_overlapping = [current_range] + non_overlapping
            return non_overlapping, overlapping
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    def check_negation(self, bert_output):
        """
        bert_output: complete list of entities by bert models
        Returns: complete list of entities with negation updated
        """
        try:
            bert_output = deepcopy(bert_output)
            terms = termset("en_clinical")
            nlp = spacy.blank("en")
            nlp.add_pipe("ner")

            ents, text, _, _ = self.update_offset(bert_output)

            entities = []
            for entity in ents:
                entities.append((entity["start_1"],
                                 entity["end_1"],
                                 entity["entity_label"]))

            train_sample = [(text, {"entities": entities})]
            nlp.begin_training()
            for itn in range(100):
                is_breaked = False
                for _text, annotations in train_sample:
                    doc = nlp.make_doc(_text)
                    exm = Example.from_dict(doc, annotations)
                    nlp.update([exm])
                    doc_predict = nlp(text)
                    if len(entities) == len(doc_predict.ents):
                        is_breaked = True
                        break
                if is_breaked:
                    # print(f"breaking at: {itn}")
                    break

            terms.add_patterns({"pseudo_negations": ["lot no"], })

            nlp.add_pipe('sentencizer')
            nlp.add_pipe("negex", config={"neg_termset": terms.get_patterns()})
            doc = nlp(text)
            for entity in bert_output:
                for ent in doc.ents:
                    if (entity["start_1"] == ent.start_char and entity["end_1"] == ent.end_char) or \
                            (entity["entity_text"] in ent.text or ent.text in entity["entity_text"]):
                        entity["negated"] = ent._.negex
            return bert_output
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    def update_offset(self, bert_output):
        """
        Given a list of entities, update start_1 & end_1 to match the correct index
        in text, created by joining all sentences
        Returns: list of entities with updated span under start_1, end_1 keys
        """
        try:
            ents = deepcopy(bert_output)
            sentid_to_sent = {}
            for x in ents:
                sentid_to_sent[x['sent_id']] = x['sentence']

            texts = []
            sentid_to_offset = {}
            offset = 0
            for sentid in sorted(sentid_to_sent.keys()):
                sentid_to_offset[sentid] = offset
                offset = offset + len(sentid_to_sent[sentid]) + 1
                texts.append(sentid_to_sent[sentid])

            text = " ".join(texts)

            for ent in ents:
                ent['start_1'] = ent['start'] + sentid_to_offset[ent['sent_id']]
                ent['end_1'] = ent['end'] + sentid_to_offset[ent['sent_id']]

            return ents, text, sentid_to_sent, sentid_to_offset
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    def negate_entities(self, bert_output):
        """
        Input: complete list of entities from all bert models
        Returns: Complete list of entities with negation updated
        """
        try:
            t1 = time.time()
            for ent in bert_output:
                ent['sent_id'] = int(ent['sent_id'])
            _bert_output, text, sentid_to_sent, sentid_to_offset = self.update_offset(bert_output)

            processed = []
            max_entities_per_iter = 10
            while _bert_output:
                _bert_output = sorted(_bert_output, key=lambda x: x['start_1'])
                non_overlapping, overlapping = self.find_nonoverlapping_ranges(_bert_output)
                if len(non_overlapping) > max_entities_per_iter:
                    y = non_overlapping[max_entities_per_iter:]
                    non_overlapping = non_overlapping[:max_entities_per_iter]
                    overlapping.extend(y)
                _bert_output = overlapping

                _processed = self.check_negation(non_overlapping)
                processed.extend(_processed)
            for ele in processed:
                if ele.get("start_1"):
                    del ele["start_1"]
                if ele.get("end_1"):
                    del ele["end_1"]
            logger.info(f"Time taken for identifying negation in bio bert response:{time.time() - t1}")
            return processed
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e


def custom_sent_tokenize(text, lang):
    try:
        if lang == 'en':
            return english_tokenizer.tokenize(text)
        elif lang == 'ja':
            return japanese_tokenizer.tokenize(text)
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def convert_values_to_none(sample_dict):
    """
    replace all values to null
    """
    try:
        for key, value in sample_dict.items():
            if isinstance(value, dict):
                convert_values_to_none(value)
            elif isinstance(value, list):
                for val in value:
                    convert_values_to_none(val)
            else:
                sample_dict[key] = None
        return sample_dict
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def remove_negated_ent(df):
    try:
        df = df.loc[(df.negated != True)]
        return df
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def process_text_ner(text):
    try:
        text = re.sub(r"\s*(\\n)*Subject\s*:\s*.*", "", text,flags=re.IGNORECASE)
        text = re.sub(r"\s*(\\n)*From\s*:\s*.*>", "", text,flags=re.IGNORECASE)
        text = re.sub(r"\s*(\\n)*Received\s*Date\s*:\s*.*", "", text,flags=re.IGNORECASE)
        text = re.sub(r"\s*(\\n)*T\s*o\s*:\s*.*>", "", text,flags=re.IGNORECASE)
        text = re.sub(r"\s*(\\n)*Sent\s*:\s*.*>", "", text, flags=re.IGNORECASE)
        text = re.sub(r"\n+", " ", text)
        text = re.sub(r"\s+", " ", text)
        patt = re.compile(
            r'\s*\n\s*[Kk]ind\s*[Rr]egards\s*\n|\s*\n\s*[Tt]hank\s*[Yy]ou\s*\n|\s*\n\s*[Bb]est\s*[Rr]egards\s*\n|\s*\n\s*[Yy]ours\s*[Ss]incerely\s*\n')
        a = re.search(patt, text)
        if a is not None:
            text = text[:a.span()[0]]
        return text
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e
