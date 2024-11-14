import configparser
import os
import logging

logger = logging.getLogger(__name__)


BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
HOME_DIR = os.path.expanduser("~")

config = configparser.ConfigParser(interpolation=None)

# Config file name
config_file_path = os.path.join(HOME_DIR, '.Decision_Support_System/config/config.ini')
config_file_path_2 = os.path.join(HOME_DIR, '.Decision_Support_System/system.properties')

# Check if config file exists in Home directory else read default config file
try:
    if os.path.exists(config_file_path) and os.path.getsize(config_file_path) > 0:
        config.read([config_file_path, config_file_path_2])
        logger.info("Config File Loaded from Home directory")
    else:
        config.read([os.path.join(BASE_DIR, 'DSS/static/DSS/config/config.ini'),
                     os.path.join(os.path.dirname(BASE_DIR), 'system.properties')])
        logger.info("Config File Loaded from Project directory")
except Exception as e:
    logger.info(e)
    logger.info("Config File not Found")