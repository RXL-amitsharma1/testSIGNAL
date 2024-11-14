import spacy
import re
import time
from os import path
import sys
parent_dir = path.dirname(path.dirname(path.abspath(__file__)))
sys.path.append(parent_dir)
import logs
import os
from utils import convert_keys_to_str , custom_sent_tokenize
from flask import Flask ,jsonify ,request
from configs import global_config , model_dir_path

from fastcoref import LingMessCoref
logger = logs.get_logger()
default_error_message = "Inside parent_coreference_resolution.py - An error occured on line {}. Exception type: {} , Exception: {} "
app_coref = Flask(__name__)
app_coref.config["DEBUG"] = False


class Coref:
    _instance = None
    #for singleton implementation
    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(Coref, cls).__new__(cls)
            cls._instance._initialize()
        return cls._instance

    def _initialize(self):
        self.predictor =  LingMessCoref(os.path.join(model_dir_path , "coref_models"))
        # self._spacy = get_spacy_model('en_core_web_sm', pos_tags=True, parse=True, ner=False)
        self._spacy = spacy.load("en_core_web_sm")
        self._spacy.disable_pipe('ner')
        logger.info("Coref is loaded.")

    def preprocess(self, data):
        try:
            data = re.sub(r"\n+", " ", data)
            data = re.sub(r"\s+", " ", data)
            return data
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    def resolve(self, text):
        try:
            return self.predictor.predict(document = text)
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    def classify(self, text):
        try:
            if len(text)>25:
                return None

            text = text.lower()
            patient_phrases = ['patient', 'subject']
            parent_phrases = ['mother', 'parent', 'father']

            # is_patient = False
            # for pat_phrase in patient_phrases:
            #     if pat_phrase in text:
            #         is_patient = pat_phrase

            is_parent = False
            for par_phrase in parent_phrases:
                if par_phrase in text:
                    is_parent = par_phrase

            if is_parent:
                # return np.random.choice([f"patient's {is_parent}", is_parent])
                return f"patient's {is_parent}"
            # elif is_patient:
            #     # return np.random.choice([f"the {is_patient}", is_patient])
            #     return f"the {is_patient}"

            return None
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    # def process_coref(self, prediction):
    #     original_document = copy.deepcopy(prediction['document'])
    #     _debug = {}
    #     for _cls in prediction['clusters']:
    #         top_reference = _cls[0]
    #         other_references = _cls[1:]
    #         reference_phrase = ' '.join( prediction['document'][top_reference[0]: top_reference[1]+1])
    #         label = self.classify(reference_phrase)
    #
    #         replaced_phrase = ""
    #         if label:
    #             for ref in other_references:
    #                 for i in range(ref[0], ref[1]+1):
    #                     replaced_phrase = replaced_phrase + " " + prediction['document'][i]
    #                     if i == ref[0]:
    #                          prediction['document'][i] = label
    #                     else:
    #                          prediction['document'][i] = ''
    #
    #             _debug[(replaced_phrase, other_references)] = label
    #
    #     resolved_text = ' '.join(prediction['document'])
    #
    #     return self.preprocess(resolved_text), _debug, original_document, prediction['document']

    # def coref(self, text):
    #     s_time = time.time()
    #     text = self.preprocess(text)
    #     prediction = self.resolve(text)
    #     res_narrative, _debug, org_doc, pred_doc = self.process_coref(prediction)
    #     logger.info(f"Time taken in coref: {time.time()-s_time} secs.")
    #     return res_narrative, _debug, org_doc, pred_doc


    def get_fast_cluster_spans(self , doc, fast_clusters):
        """
        Function to replace string index with spacy token index in fastcotef cluster response.
        Params:
            doc[spacy.tokens.doc.Doc] --> spacy Doc object of the given input text for coref
            fast_clusters[List[List[tuple[int]]]] --> List of clusters returned from fastcoref model , containing index of string as datapoints.
        Returns:
            spacy_token_clusters[List] --> List of clusters having spacy token index as cluster datapoints.
        """
        try:
            spacy_token_clusters = []
            for cluster in fast_clusters:
                new_group = []
                for tuple in cluster:
                    (start, end) = tuple
                    span = doc.char_span(start, end)
                    new_group.append([span.start, span.end-1])
                spacy_token_clusters.append(new_group)
            return spacy_token_clusters
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    def replace_corefs(self, document, clusters):
        """
        Uses a list of coreference clusters to convert a spacy document into a
        string, where each coreference is replaced by its main mention.
        """
        try:
            _debug = {}
            # Original tokens with correct whitespace
            resolved = list(tok.text_with_ws for tok in document)

            for cluster in clusters:
                cluster_labels = []
                # The main mention is the first item in the cluster
                for data_point in cluster:
                    """Classifying all the points in cluster so handle scenario where pronoun occurs before noun.
                    """
                    mention_start, mention_end = data_point[0], data_point[1] + 1
                    mention_span = document[mention_start:mention_end]
                    label = self.classify(mention_span.text)
                    cluster_labels.append(label)
                if any(cluster_labels):
                    label = list(filter(lambda x: x is not None , cluster_labels))[0] #Asssuming label for points in single cluster will be same
                    offset = 0
                    for coref in cluster:
                        final_token = document[coref[1]]
                        replaced_phrase = resolved[coref[0]]
                        resolved[coref[0]] = label + final_token.whitespace_
                        replaced_with = label + final_token.whitespace_

                        # Mask out remaining tokens
                        for i in range(coref[0] + 1, coref[1] + 1):
                            replaced_phrase = replaced_phrase + resolved[i]
                            resolved[i] = ""

                        replaced_span = (document[coref[0]].idx, document[coref[0]].idx + len(replaced_phrase))
                        s2 = document[coref[0]].idx + offset
                        e2 = document[coref[0]].idx + offset + len(replaced_with)
                        replaced_with_span = (s2, e2)

                        offset = offset + (len(replaced_with) - len(replaced_phrase))

                        _debug[(replaced_phrase, replaced_span)] = (replaced_with, replaced_with_span)
                    break

            return "".join(resolved), _debug
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e


    def coref_resolved(self, document):
        try:
            s_time = time.time()
            document = self.preprocess(document)
            spacy_document = self._spacy(document)

            preds = self.predictor.predict([document] ,max_tokens_in_batch=10000 ) #default value of max_tokens = 10000
            clusters = preds[0].get_clusters(as_strings=False) if len(preds) > 0 else []

            coref_replacements = {}#None
            if not clusters:
                pass
            else:
                clusters = self.get_fast_cluster_spans(spacy_document ,clusters)
                document, coref_replacements = self.replace_corefs(spacy_document, clusters)
            logger.info(f"Time taken in coref: {time.time() - s_time} secs.")
            return document, coref_replacements
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    def get_coref_span_sentencewise(self, text, resolved_text, coref_replacements):
        try:
            start = 0
            _new_coref_replacements = {}
            for i, sent in enumerate(custom_sent_tokenize(resolved_text,'en')):
                _new_coref_replacements[i] = {}
                end = start + len(sent)

                assert resolved_text[start:end] == sent

                for k in coref_replacements:
                    d = coref_replacements[k]
                    if start <= d[1][0] <= end:
                        _new_coref_replacements[i][k] = (d[0], (d[1][0] - start, d[1][1] - start))

                if resolved_text[end :end+1] == " ": #to handle scenarios when there is space in naarative after sentence and no space after sentence
                    start = end + 1
                else:
                    start = end

            _new_coref_replacements_2 = {}
            start = 0
            for i, sent in enumerate(custom_sent_tokenize(text, 'en')):
                _new_coref_replacements_2[i] = {}
                end = start + len(sent)

                assert text[start:end] == sent

                for k in coref_replacements:
                    d = coref_replacements[k]
                    if start <= k[1][0] <= end:
                        _new_coref_replacements_2[i][(k[0], (k[1][0] - start, k[1][1] - start))] = \
                        _new_coref_replacements[i][k]
                if text[end :end+1] == " ": #to handle scenarios when there is space in naarative after sentence and no space after sentence
                    start = end + 1
                else:
                    start = end

            return _new_coref_replacements_2
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    def classify_parent(self, text):
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


@app_coref.route("/predict/coref" , methods=["POST"])
def resolve_coref():
    try:
        response = {"data" : "" , "_debug" :"" , "coref_replacements" :""}
        input_text = request.form.get("text" , "")
        if input_text:
            response["data"], response["_debug"] = coref.coref_resolved(input_text)
            response["coref_replacements"] = coref.get_coref_span_sentencewise(input_text, response["data"], response["_debug"] )
            response["_debug"] = convert_keys_to_str(response["_debug"])
            response["coref_replacements"] = convert_keys_to_str(response["coref_replacements"])
            return jsonify(response) ,200
        else:
            return jsonify(response) , 201

    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        return jsonify({"message" : e}) , 500

if __name__ == "__main__":
    coref = Coref()
    app_coref.run(host=global_config["COREF_API"]["URL"], port=global_config["COREF_API"]["PORT"], threaded=False)