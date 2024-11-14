import spacy
from flask import Flask, request
from nltk.corpus import stopwords
from os import path
import sys
parent_dir = path.dirname(path.dirname(path.abspath(__file__)))
sys.path.append(parent_dir)
from utils import get_model_metadata , custom_sent_tokenize
from utils_en import data_cleaning
from time import time
from configs import global_config , model_config_json as config_json , model_dir_path

import logs
from negspacy.negation import Negex




default_error_message = "Inside spacy_api.py - An error occured on line {}. Exception type: {} , Exception: {} "

app_spacy = Flask(__name__)
app_spacy.config["DEBUG"] = False


pqc_models = None
loaded_models = None


logger = logs.get_logger()


def prediction_spacy(sentence, model, default_confidence):
    try:
        doc = model(sentence)
        output = []
        for ent in doc.ents:
            output.append({
                "tag": ent.label_,
                "text": ent.text,
                "start": ent.start_char,
                "end": ent.end_char,
                "conf_score": extract_confidence_score(ent, doc.spans["labeled_spans"], default_confidence),
                "negated": ent._.negex if config_json["spacy"]["negation-switch"] else None
            })
            if output[-1]["tag"] == "EVENT_START_DATE":
                output[-1]["tag"] = "ONSET"
        return output
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e

def extract_confidence_score(ent, spans, default_confidence):
    try:
        for index, span in enumerate(spans):
            if ent.label_ == span.label_ and (ent.text in span.text or span.text in ent.text):
                return float(spans.attrs["scores"][index])
        return float(default_confidence)
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e

def get_model_output(sentence, model, default_confidence):
    """
    maintains flow for prediction and processing of output
    """
    try:
        spacy_output = prediction_spacy(sentence, model, default_confidence)
        return spacy_output
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def load_model(model):
    try:
        return spacy.load(model)
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
                if entity["text"] in stop_words:
                    continue
                if entity["tag"] == "LAB_TEST_NAME" and "," in entity["text"] : #TODO: for other entities as well
                    search_start_index = entity["start"] #index after which comma splitted entity is searched
                    for entity_value in entity["text"].strip(", ").split(","):
                        entities.append({"sent_id": sentence_id,
                            "entity_label": entity["tag"],
                            "entity_text": entity_value.strip(),
                            "original_entity_text": entity_value.strip() , #TODO: confirm if value should be as extracted my model with comma?
                            "start": sentence.find(entity_value , search_start_index),
                            "end": sentence.find(entity_value , search_start_index) + len(entity_value),
                            "conf_score": entity["conf_score"],
                            "negated": entity["negated"],
                            "sentence": sentence,
                            "model": "spacy"
                                })
                        search_start_index = sentence.find(entity_value , search_start_index) + len(entity_value)
                else:
                    entities.append({
                        "sent_id": sentence_id,
                        "entity_label": entity["tag"],
                        "entity_text": entity["text"],
                        "original_entity_text": entity["text"],
                        "start": entity["start"],
                        "end": entity["end"],
                        "conf_score": entity["conf_score"],
                        "negated": entity["negated"],
                        "sentence": sentence,
                        "model": "spacy"
                    })
        return entities
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e

@app_spacy.route('/predict/spacy', methods=['POST'])
def main():
    try:
        st = time()
        sentences, sent_ids = [], []
        if "sentence" in request.form:
            sentence = request.form['sentence']
            sentences.append(sentence)
            sent_ids.append(request.form['sentence_id'] if 'sentence_id' in request.form else 1)
        elif "narrative" in request.form:

            sentences = data_cleaning(request.form["narrative"])
            sentences = custom_sent_tokenize(request.form["narrative"],'en')
            sent_ids = [sent_id + 1 for sent_id in range(len(sentences))]

        output_json = {"entities": []}
        global loaded_models
        for sentence_id, sentence in zip(sent_ids, sentences):
            if loaded_models is None:
                models = get_model_metadata(config_json['spacy']['models'], 'name')
                models = [model_dir_path + "/spacy_models/" + name for name in models]
                conf_scores = get_model_metadata(config_json['spacy']['models'], 'conf')
                logger.info("Fetched spacy metadata!")
                loaded_models = [load_model(model) for model in models]
                logger.info("Loaded all spacy models!")
                output_all_models = [get_model_output(sentence, model, conf) for model, conf in
                                    zip(loaded_models, conf_scores)]
            else:
                conf_scores = get_model_metadata(config_json['spacy']['models'], 'conf')
                output_all_models = [get_model_output(sentence, model, conf) for model, conf in
                                    zip(loaded_models, conf_scores)]

            entities = prepare_entities(sentence_id, sentence, output_all_models)
            output_json["entities"].extend(entities)
            logger.debug("Spacy prediction complete for '{}'".format(sentence))
        logger.info(f"Bio bert api request completed in {time()-st} secs")
        return output_json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e

@app_spacy.route('/predict/pqc', methods=['POST'])
def main_pqc():
    try:
        sentence = request.form['sentence']
        sentence_id = request.form['sentence_id'] if 'sentence_id' in request.form else 1
        global pqc_models
        if pqc_models is None:
            models = get_model_metadata(config_json['pqc']['models'], 'name')
            models = [model_dir_path + "/pqc_models/" + name for name in models]
            conf_scores = get_model_metadata(config_json['pqc']['models'], 'conf')
            pqc_models = [load_model(model) for model in models]
            output_all_models = [get_model_output(sentence, model, conf) for model, conf in zip(pqc_models, conf_scores)]
            logger.debug("Spacy prediction complete for '{}'".format(sentence))
        else:
            conf_scores = get_model_metadata(config_json['pqc']['models'], 'conf')
            output_all_models = [get_model_output(sentence, model, conf) for model, conf in zip(pqc_models, conf_scores)]
            logger.debug("Spacy prediction complete for '{}'".format(sentence))

        extracted_entities = prepare_entities(sentence_id, sentence, output_all_models)
        return extracted_entities
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e

if __name__ == "__main__":
    app_spacy.run(host=global_config["SPACY_API"]["URL"], port=global_config["SPACY_API"]["PORT"])
