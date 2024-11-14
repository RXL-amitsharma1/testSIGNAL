from logging import getLogger
from logging.config import fileConfig
from os import path
from datetime import datetime
import os

def get_logger():
    folder_path = path.join(path.dirname(path.dirname(path.abspath(__file__))),"config")
    log_config_path = folder_path + "/log-config.ini"
    log_file_path = folder_path.replace("/config", "") + "/logs/unstructure.log"
    os.makedirs(os.path.dirname(log_file_path) , exist_ok=True)
    fileConfig(log_config_path, disable_existing_loggers=True, defaults={'logfilename': log_file_path})
    logger = getLogger("apiLogger")
    return logger
