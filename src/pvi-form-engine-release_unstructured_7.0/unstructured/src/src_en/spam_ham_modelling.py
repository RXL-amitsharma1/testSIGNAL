import fasttext
import os
import sys
import logs
logger = logs.get_logger()
default_error_message = "Inside spam_ham_modelling.py - An error occured on line {}. Exception type: {} , Exception: {} "

class BiFasttext():
    def __init__(self, model_folder_path,threshold=60):
        email_model_name = "spam_ham_debiased_email_quantized.bin"
        sms_model_name = "spam_ham_debiased_sms_quantized.bin"
        self.threshold = threshold
        self.model_email = self.load(model_folder_path, email_model_name)
        self.model_sms = self.load(model_folder_path, sms_model_name)
    
    def predict(self, sentence, use_long=True, use_short=True):
        try:
            if use_long and not use_short:
                output = self.model_email.predict(sentence)
            elif not use_long and use_short:
                output = self.model_sms.predict(sentence)
            else:
                if len(sentence) > self.threshold:
                    output = self.model_email.predict(sentence)
                else:
                    output = self.model_sms.predict(sentence)
            return self.format_output(output)
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e
    
    def format_output(self, output):
        try:
            return {
                "label": output[0][0].strip("__label__"),
                "confidence_score": output[1][0]
            }
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e
    
    def load(self, folder, model):
        try:
            return fasttext.load_model(os.path.join(folder, model))
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e