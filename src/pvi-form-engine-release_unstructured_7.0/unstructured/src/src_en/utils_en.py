import re
import logs
import sys
from utils import process_text_ner
logger = logs.get_logger()
default_error_message = "Inside src_en.utils.py - An error occurred on line {}. Exception type: {}, Exception :{} "

def data_cleaning(text):
    try:
        text = text.replace("{", "(")
        text = text.replace("}", ")")
        text = re.sub(r"\\\\", "", text)
        return text
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e

def preprocess_text(text):
    try:
        text = process_text_ner(text)
        text = data_cleaning(text)
        return text
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e