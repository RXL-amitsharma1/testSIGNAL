import logs
import sys
from src.utils import process_text_ner 

logger = logs.get_logger()
default_error_message = "Inside utils.py - An error occurred on line {}. Exception type: {}, Exception :{} "


def preprocess_text(text):
    try:
        text = process_text_ner(text)
        return text
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e
