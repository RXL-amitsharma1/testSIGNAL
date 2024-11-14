import torch
import torch.nn as nn
from os import path
from flask import Flask, request , jsonify
import numpy as np
from nltk.corpus import stopwords
from transformers import BertTokenizer, BertModel, BertConfig
from copy import deepcopy
import sys
parent_dir = path.dirname(path.dirname(path.abspath(__file__)))
sys.path.append(parent_dir)
from utils import get_model_metadata ,classify_parent , get_coref_data ,convert_str_key_to_tuple ,BiobertNegation , custom_sent_tokenize
from utils_en import data_cleaning, preprocess_text
import logs
from time import time
from configs import global_config ,model_dir_path , model_config_json as config_json
app_bio = Flask(__name__)
app_bio.config["DEBUG"] = False
default_error_message = "Inside biobert_api.py - An error occured on line {}. Exception type: {} , Exception: {} "

loaded_models = None
logger = logs.get_logger()
neg_obj = BiobertNegation()


class EntityModel(nn.Module):
    def __init__(self, classes, base_model_config_path):
        super(EntityModel, self).__init__()
        self.classes = classes
        self.bert = BertModel(BertConfig.from_pretrained(base_model_config_path))
        self.out_tag = nn.Linear(768, len(self.classes))

    def forward(self, ids, mask, token_type_ids):
        try:
            output = self.bert(ids, attention_mask=mask, token_type_ids=token_type_ids, return_dict=True,
                               output_hidden_states=True)
            last_hidden_state = output["last_hidden_state"]
            tag = self.out_tag(last_hidden_state)
            return tag
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e


def postprocessing(enc_tag, word_ids, target_labels, predicted_labels, predicted_probabilities):
    try:
        prev_wid = None
        new_target_labels = []
        new_predicted_labels = []
        new_probabilites = []

        for index, wid in enumerate(word_ids):
            if index == 0:
                continue

            if (wid == -200) or (prev_wid is not None and wid == -100):
                break

            if prev_wid is None or prev_wid != wid:
                new_target_labels.append(enc_tag.inverse_transform(np.array([target_labels[index], ]))[0])
                new_predicted_labels.append(enc_tag.inverse_transform(np.array([predicted_labels[index], ]))[0])
                new_probabilites.append(predicted_probabilities[index])
                prev_wid = wid

        return [new_target_labels, new_predicted_labels, new_probabilites]
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def process_sentence(sentence, tokenizer):
    try:
        text = sentence.split()
        tags = [0] * len(text)

        ids = []
        target_tag = []
        word_ids = []
        for i, s in enumerate(text):
            inputs = tokenizer.encode(
                s.lower(),
                add_special_tokens=False
            )
            input_len = len(inputs)
            ids.extend(inputs)
            target_tag.extend([tags[i]] * input_len)
            word_ids.extend([i] * input_len)

        max_len = min(512, len(ids) + 2)
        ids = ids[:max_len - 2]  # cut the ids if exceeds more than MAX_LEN
        target_tag = target_tag[:max_len - 2]  # same
        word_ids = word_ids[:max_len - 2]

        ids = [101] + ids + [102]
        target_tag = [0] + target_tag + [0]
        word_ids = [-100] + word_ids + [-100]

        mask = [1] * len(ids)
        token_type_ids = [0] * len(ids)

        padding_len = max_len - len(ids)

        ids = ids + ([0] * padding_len)
        mask = mask + ([0] * padding_len)
        token_type_ids = token_type_ids + ([0] * padding_len)
        target_tag = target_tag + ([0] * padding_len)
        word_ids = word_ids + ([-200] * padding_len)  # needed to keep all tensors/lists/dicts of equal length

        data = {
            "ids": torch.tensor(ids, dtype=torch.long),
            "mask": torch.tensor(mask, dtype=torch.long),
            "token_type_ids": torch.tensor(token_type_ids, dtype=torch.long),
            "target_tag": torch.tensor(target_tag, dtype=torch.long),
            "word_ids": torch.tensor(word_ids, dtype=torch.long),
        }

        return data
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def merge_entities(bert_output):
    try:
        merged_entities = []
        prev_ent = None
        for i, ent in enumerate(bert_output):
            if prev_ent and (ent["start"] - prev_ent["end"] == 1) and ent["entity_label"] == prev_ent["entity_label"]:
                prev_ent["end"] = ent["end"]
                prev_ent["entity_text"] = " ".join([prev_ent["entity_text"], ent["entity_text"]])
                prev_ent["conf_score"] = max(prev_ent["conf_score"], ent["conf_score"])
            else:
                merged_entities.append(ent)
                prev_ent = ent
        return merged_entities
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def prediction_bert(sentence, data, enc_tag, model, device=torch.device("cpu")):
    try:
        model.eval()

        with torch.no_grad():
            word_ids = data["word_ids"].detach().cpu().numpy().tolist()
            target_labels = data["target_tag"].detach().cpu().numpy().tolist()
            data.pop("word_ids")
            data.pop("target_tag")
            for k, v in data.items():
                data[k] = v.to(device).unsqueeze(0)

            logits = model(data["ids"], data["mask"], data["token_type_ids"])
            logits_soft = torch.softmax(logits, dim=2)
            logits_label = torch.argmax(logits, dim=2)
            predicted_probabilities = torch.max(logits_soft, dim=2).values.detach().cpu().numpy().tolist()
            predicted_labels = logits_label.detach().cpu().numpy().tolist()
            _, true_predicted, true_probs = postprocessing(enc_tag, word_ids, target_labels, predicted_labels[0],
                                                        predicted_probabilities[0])

            sentence_tokens = sentence.split()

            output = []
            start, end = 0, 0
            for index, p_tag in enumerate(true_predicted):
                end = start + len(sentence_tokens[index])
                if p_tag != "O":
                    label = p_tag.split("-", 1)[1]
                    if label == "EVENT_START_DATE":
                        label = "ONSET"
                    output.append({
                        "entity_label": label, "entity_text": sentence_tokens[index],
                        "start": start, "end": end, "conf_score": true_probs[index], "negated": None
                    })
                start = end + 1
        return output
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def load_model(model_path, enc_tag):
    try:
        """
        function to load each bert model
        """
        model = EntityModel(enc_tag.classes_,
                            path.join(model_dir_path, config_json["bert"]["base_biobert_path"], "config.json"))
        state_dict = torch.load(path.join(model_path, "model.bin"), map_location='cpu')
        model.load_state_dict(state_dict ,strict=False)
        return model, enc_tag
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def get_model_output(sentence, enc_tag, model):
    try:
        data = process_sentence(sentence, tokenizer)
        bert_output = prediction_bert(sentence, data, enc_tag, model)
        merged_output = merge_entities(bert_output)
        return merged_output
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def update_entity_spans(entities, coref_replacement):
    """
    Replaces coref entities with actual text from narrative.
    Params:
        entities[List] --> list of dictionary of entites extracted by models
        coref_replacement[dict] --> dictionary containing coref entity and original text of narrative
    Returns:
        new_entities[List] --> list of entites with updated start , end index.
    """
    try:
        new_entities = deepcopy(entities)
        sorted_keys = sorted(coref_replacement, key=lambda k: coref_replacement[k][1][0])

        for k in sorted_keys:
            for ent in new_entities:
                if coref_replacement[k][1][0] <= ent['start']:
                    offset = len(k[0]) - len(coref_replacement[k][0])

                    if ent['entity_label'] == "PARENT_GENDER" and coref_replacement[k][1][0] <= ent['start'] < \
                            coref_replacement[k][1][1]:
                        label = classify_parent(k[0]) # classifing "she"
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
                        ent['start'] = ent['start'] + offset if ent['start'] + offset >0 else 0
                        ent['end'] = ent['end'] + offset

        # drop ent with parent_gender entity if org text does not mention text.
        new_entities = [ent for ent in new_entities if ent['entity_label'] is not None]
        return new_entities
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def prepare_entities(sentence_id, sentence, model_outputs):
    try:
        entities = []
        stop_words = set(stopwords.words('english'))
        extra_words = ['Bot', 'bot', 'address', 'unknown', ""]
        stop_words = stop_words.union(set(extra_words))

        for model_output in model_outputs:
            for entity in model_output:
                if entity["entity_text"] in stop_words:
                    continue
                if entity["entity_label"] in [ "TEST_NAME","LAB_TEST_NAME"] and "," in entity["entity_text"] : #TODO: for other entities as well
                    search_start_index = entity["start"] #index after which comma splitted entity is searched
                    for entity_value in entity["entity_text"].strip(", ").split(","):
                        entity_value = entity_value.strip()
                        st_index = sentence.find(entity_value , search_start_index)
                        end_index =sentence.find(entity_value , search_start_index) + len(entity_value)
                        entities.append({"sent_id": sentence_id,
                            "entity_label": entity["entity_label"],
                            "entity_text": entity_value,
                            "original_entity_text": entity_value , #TODO: confirm if value should be as extracted my model with comma?
                            "start": st_index,
                            "end": end_index,
                            "conf_score": entity["conf_score"],
                            "negated": entity["negated"],
                            "sentence": sentence,
                            "model": "biobert"
                                })
                        search_start_index = end_index
                else:
                    entities.append({
                        "sent_id": sentence_id,
                        "entity_label": entity["entity_label"],
                        "entity_text": entity["entity_text"],
                        "original_entity_text": entity["entity_text"],
                        "start": entity["start"],
                        "end": entity["end"],
                        "conf_score": entity["conf_score"],
                        "negated": entity["negated"],
                        "sentence": sentence,
                        "model": "biobert"
                    })
        return entities
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


@app_bio.route('/predict/bert', methods=['POST'])
def main():
    try:
        st = time()
        logger.info("Bio bert api called")
        sentences, sent_ids = [], []
        coref_replacements = {}
        if "sentence" in request.form:
            sentence = request.form['sentence']
            # sentence = coref.coref(sentence)
            sentences.append(sentence)
            sent_ids.append(request.form['sentence_id'] if 'sentence_id' in request.form else 1)
        elif "narrative" in request.form:
            # sentences = custom_sent_tokenize(request.form["narrative"])
            narrative = request.form["narrative"]
            # narrative = coref.coref(narrative)
            narrative = preprocess_text(narrative)
            data, _debug, coref_replacements  = get_coref_data(narrative)
            coref_replacements = convert_str_key_to_tuple(coref_replacements)
            _debug = convert_str_key_to_tuple(_debug)
            sentences_coref = custom_sent_tokenize(str(data),'en')
            sentences = custom_sent_tokenize(narrative,'en')
            sent_ids = [sent_id + 1 for sent_id in range(len(sentences_coref))]

        logger.debug(sentences)
        output_json = {"entities": []}
        for sentence_id, sentence in zip(sent_ids, sentences):
            if "sentences_coref" in locals() and sentences_coref:
                output_all_models = [get_model_output(sentences_coref[int(sentence_id) -1], enc_tag, model) for model, enc_tag in loaded_models]
                logger.debug("Biobert prediction complete for '{}'".format((sentences_coref[int(sentence_id) -1])))
            else:
                output_all_models = [get_model_output(sentence, enc_tag, model) for model, enc_tag in loaded_models]
                logger.debug("Biobert prediction complete for '{}'".format(sentence))

            entities = prepare_entities(sentence_id, sentence, output_all_models )

            output_json["entities"].extend(update_entity_spans(entities, coref_replacements.get(int(sentence_id) - 1 , {})))
        if "narrative" in locals()  and config_json["bert"]["negation-switch"]:
            output_json["entities"] = neg_obj.negate_entities(output_json["entities"])
        logger.info(f"Bio bert api request completed in {time()-st} secs")
        return output_json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        logger.info(f"Bio bert api request completed in {time()-st} secs")
        return jsonify({"message": f"Error occured : {e}"}) , 500


if __name__ == "__main__":
    try:
        #TODO: remove model loading part from __main__ ,if import app_bio to other file for serving it in gunicorn if will load models
        tokenizer = BertTokenizer.from_pretrained(path.join(model_dir_path,
                                                            config_json["bert"]["base_biobert_path"], "vocab.txt"))
        models = get_model_metadata(config_json['bert']['models'], 'name')
        models = [model_dir_path + "/bert_models/" + name for name in models]
        enc_tags = get_model_metadata(config_json['bert']['models'], 'tags')
        logger.info("Fetched biobert metadata!")
        loaded_models = [load_model(model, enc_tag) for model, enc_tag in zip(models, enc_tags)]
        logger.info("Loaded all biobert models!")
        app_bio.run(host=global_config["BIOBERT_API"]["URL"], port=global_config["BIOBERT_API"]["PORT"])
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e