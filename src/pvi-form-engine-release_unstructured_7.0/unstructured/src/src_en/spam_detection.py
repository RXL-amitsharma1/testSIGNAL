import sys
from src_en.spam_ham_model import SpamIdentification
import logs
from configs import model_dir_path, global_config

logger = logs.get_logger()
default_error_message = "Inside spam_detection.py - An error occured on line {}. Exception type: {} , Exception: {} "

spam_model = SpamIdentification(model_dir_path + "/spam_ham_models")
try:
    SPAM_HAM_THRESHOLD = float(global_config["PIPELINE_API"]["SPAM_THRESHOLD"])

except ValueError as e:
    logger.warning("Failed to get spam threshold from config setting default value")
    SPAM_HAM_THRESHOLD = 0.6


def spam_identification(pvi_json, text):
    try:
        spam_result = spam_model.predict(text)
        logger.info("spam results: %s", spam_result)
        if spam_result['1'] > SPAM_HAM_THRESHOLD:   #here '1' represents spam
            pvi_json["code"] = 4
            pvi_json["message"] = "spam"
            pvi_json["spam_acc"] = spam_result['1']
        return pvi_json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        return pvi_json