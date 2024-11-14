import json
import sys
import redis
from time import time
from logs import get_logger
logger = get_logger()
default_error_message = "Inside redis_client.py - An error occured on line {}. Exception type: {}  Exception: {}"




class RedisClient():
    def __init__(self ,host='localhost', port=6379, decode_responses=True) -> None:
        self.redis_client = redis.Redis(host=host, port=port, decode_responses=decode_responses)

    def save_to_redis(self, json_obj, lang):
        try:
            logger.info(f"Data uploading to redis started")
            st = time()
            for key , value in json_obj.items():
                value_new = json.dumps({"meta": type(value).__name__, "value": value})
                save_status = self.redis_client.set(f'{key}_{lang}', value_new)
                if save_status:
                    logger.info(f"Data saved for key :{key}_{lang}")
                else:
                    logger.info(f"Data saving failed for key :{key}_{lang}")

            
            logger.info(f"Time taken to upload data to Redis is :{time()-st} secs")

        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    def fetch_data(self, key, lang):
        try:
            st = time()
            data = self.redis_client.get(f"{key}_{lang}")
            value = json.loads(data).get("value")
            logger.info(f"Time taken to fetch data from Redis is: {time()-st} secs")
            return value
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e
