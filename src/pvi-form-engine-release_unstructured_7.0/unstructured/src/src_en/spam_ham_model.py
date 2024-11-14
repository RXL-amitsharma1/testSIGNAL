import re
import os
from spacy.lang.en.stop_words import STOP_WORDS
import spacy
import sys
import logs
logger = logs.get_logger()
default_error_message = "Inside spam_ham_model.py - An error occured on line {}. Exception type: {} , Exception: {} "




class SpamIdentification:
    def __init__(self, model_folder_path):
        self.model_email = self.load(model_folder_path, "spam_ham_en")
        self.nlp = spacy.load('en_core_web_sm')
        self.nlp.disable_pipes('ner', 'tagger', 'parser', 'attribute_ruler')

    def predict(self, text):
        try:
            preprocessed_text = self.preprocess(text)
            logger.debug(f"Pre Processed Text: \n {preprocessed_text}")
            doc = self.model_email(preprocessed_text)
            return doc.cats
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    def preprocess(self, text):
        try:
            text = text.lower()
            text = re.sub(r'[^A-Za-z0-9.]+', ' ', text)
            text = re.sub(r'\s+', " ", text)
            doc = self.nlp(text)
            tokens = [token.lemma_ for token in doc if token.text.lower() not in STOP_WORDS]
            text = " ".join(tokens)
            return text
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    def load(self, folder, model):
        try:
            return spacy.load(os.path.join(folder, model))
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e
