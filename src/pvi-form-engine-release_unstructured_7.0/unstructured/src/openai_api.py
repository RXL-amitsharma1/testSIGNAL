from flask import Flask, request, jsonify
from openai import AzureOpenAI ,APIError , APIResponseValidationError ,APIConnectionError,RateLimitError 
from ast import literal_eval
import json
import re
import functools
import time
from concurrent.futures import ProcessPoolExecutor, wait
from fuzzywuzzy import fuzz
import sys
import signal
from datetime import datetime
import copy
import os
import tiktoken
encoding = tiktoken.encoding_for_model("gpt-3.5-turbo")
from os import path
parent_dir = path.dirname(path.dirname(path.abspath(__file__)))
sys.path.append(parent_dir)
from configs import global_config, languages_config, load_config#, load_updated_config
from utils import custom_sent_tokenize
import logs
logger = logs.get_logger()
default_error_message = "Inside openai_api.py - An error occurred on line {}. Exception type: {}, Exception :{} "
app_bio = Flask(__name__)
app_bio.config["DEBUG"] = False


credentials = load_config('chatgpt_credentials.yaml')


def num_tokens_from_string(s):
    num_tokens = len(encoding.encode(s))
    return num_tokens


def preprocess(text):
    try:
        text = re.sub(r'\n+', ' ', text)
        text = re.sub(r'\s+', ' ', text)
        return text.strip()
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e

class InvalidJson(Exception):
    def __init__(self, message=""):
        super().__init__(message)

class TimeOutException(Exception):
    def __init__(self, message=""):
        super().__init__(message)


class TokenLimitException(Exception):
    def __init__(self, message):
        super().__init__(message)


class CustomOpenAPIException(Exception):
    def __init__(self, message):
        super().__init__(message)

def retry_decorator(retries):
    def my_decorator(func):
        @functools.wraps(func)
        def my_wrapper(*args, **kwargs):
            error = None
            for i in range(1, retries + 1):
                try:
                    if i != 1:
                        time.sleep(credentials["sleep_before_retry"])
                    # print(f'Trying {i} time')
                    res = func(*args, **kwargs)
                    return res, i, "success"
                except APIError as e:
                    error = e
                    # print(f"OpenAI API returned an API Error: {e}")
                    logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
                except APIConnectionError as e:
                    error = e
                    # print(f"Failed to connect to OpenAI API: {e}")
                    logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
                except RateLimitError as e:
                    error = e
                    # print(f"OpenAI API request exceeded rate limit: {e}")
                    logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
                # except APIResponseValidationError as e:
                    # error = e
                #     # print(f"OpenAI API request exceeded rate limit: {e}")
                #     logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
                except InvalidJson as e:
                    error = e
                    logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))

            # return {"error": str(error)}, retries, "fail"
            raise CustomOpenAPIException(f"Max retries limit crossed: {str(error)}")
        return my_wrapper

    return my_decorator


@retry_decorator(retries=credentials["max_tries"])
def _call_azure_4k_finetuned(messages, max_token, token_exhaust_percent, validation_fun=None):
    try:
        logger.info(f"process_id: {os.getpid()} : " + "GPT 4k filetuned called: ")
        openai_model = credentials['azure_openai_4k_finetuned']['engine']
        client = AzureOpenAI(api_key=credentials['azure_openai_4k_finetuned']['api_key'], 
                azure_endpoint=credentials['azure_openai_4k_finetuned']['api_base'],
                api_version = credentials['azure_openai_4k_finetuned']['api_version']
               )
        # logger.info(f"{client.api_key}, {client._api_version},{openai_model}")
        completion = client.chat.completions.create(
                                                    # response_format={ "type": "json_object" },
                                                    model=openai_model,
                                                    messages=messages,
                                                    temperature=0,
                                                    max_tokens=2500,
                                                    n=1
                                                    )
        if validation_fun is not None:
            completion.choices[0].message.content = validation_fun(completion.choices[0].message.content , completion.usage.to_dict()["total_tokens"] , max_token, token_exhaust_percent)

        return completion

    except Exception as e:
        logger.error(
            f"process_id: {os.getpid()} : "
            + default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e)
        )
        raise e


@retry_decorator(retries=credentials["max_tries"])
def _call_azure_4k(messages, max_token, token_exhaust_percent, validation_fun=None):
    try:
        logger.info(f"process_id: {os.getpid()} : " + "GPT 3.5(4k) called: ")
        openai_model = credentials['azure_openai_4k']['engine']
        client = AzureOpenAI(api_key=credentials['azure_openai_4k']['api_key'], 
                azure_endpoint=credentials['azure_openai_4k']['api_base'],
                api_version = credentials['azure_openai_4k']['api_version']
               )
        # logger.info(f"{client.api_key}, {client._api_version},{openai_model}")
        completion = client.chat.completions.create(
                                                    # response_format={ "type": "json_object" },
                                                    model=openai_model,
                                                    messages=messages,
                                                    temperature=0,
                                                    max_tokens=2500,
                                                    n=1
                                                    )
        if validation_fun is not None:
            completion.choices[0].message.content = validation_fun(completion.choices[0].message.content , completion.usage.to_dict()["total_tokens"] , max_token, token_exhaust_percent)

        return completion
    except (Exception ) as e:#, InvalidJson )as e:
        logger.error(
            f"process_id: {os.getpid()} : "
            + default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e)
        )
        raise e


@retry_decorator(retries=credentials["max_tries"])
def _call_azure_16k(messages, max_token, token_exhaust_percent, validation_fun=None):
    try:
        logger.info(f"process_id: {os.getpid()} : " + "GPT (3.5)16k called: ")
        openai_model = credentials['azure_openai_16k']['engine']
        client = AzureOpenAI(api_key=credentials['azure_openai_16k']['api_key'], 
                azure_endpoint=credentials['azure_openai_16k']['api_base'],
                api_version = credentials['azure_openai_16k']['api_version']
               )
        # logger.info(f"{client.api_key}, {client._api_version},{openai_model}")
        completion = client.chat.completions.create(
                                                    # response_format={ "type": "json_object" },
                                                    model=openai_model,
                                                    messages=messages,
                                                    temperature=0,
                                                    max_tokens=8000,
                                                    n=1
                                                    )
        if validation_fun is not None:
            completion.choices[0].message.content = validation_fun(completion.choices[0].message.content , completion.usage.to_dict()["total_tokens"] , max_token, token_exhaust_percent)

        return completion
    except Exception as e:
        logger.error(
            f"process_id: {os.getpid()} : "
            + default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e)
        )
        raise e


@retry_decorator(retries=credentials["max_tries"])
def _call_azure_gpt4(messages, max_token, token_exhaust_percent, validation_fun=None):
    try:
        logger.info(f"process_id: {os.getpid()} : " + "GPT4 called: ")

        openai_model = credentials['azure_openai_gpt4']['engine']
        client = AzureOpenAI(api_key=credentials['azure_openai_gpt4']['api_key'], 
                azure_endpoint=credentials['azure_openai_gpt4']['api_base'],
                api_version = credentials['azure_openai_gpt4']['api_version']
               )
        # logger.info(f"{client.api_key}, {client._api_version},{openai_model}")
        completion = client.chat.completions.create(model=openai_model,
                                                    messages=messages,
                                                    # response_format={ "type": "json_object" },
                                                    temperature=0,
                                                    max_tokens=5000,
                                                    n=1
                                                )
        if validation_fun is not None:
            completion.choices[0].message.content = validation_fun(completion.choices[0].message.content , completion.usage.to_dict()["total_tokens"] , max_token, token_exhaust_percent)

        return completion
    except Exception as e:
        logger.error(
            f"process_id: {os.getpid()} : "
            + default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e)
        )
        raise e

@retry_decorator(retries=credentials["max_tries"])
def _call_azure_gpt4o(messages, max_token, token_exhaust_percent, validation_fun=None):
    try:
        logger.info(f"process_id: {os.getpid()} : " + "GPT4o called: ")
        openai_model = credentials['azure_openai_gpt4O']['engine']
        client = AzureOpenAI(api_key=credentials['azure_openai_gpt4O']['api_key'], 
                azure_endpoint=credentials['azure_openai_gpt4O']['api_base'],
                api_version = credentials['azure_openai_gpt4O']['api_version']
               )
        # logger.info(f"{client.api_key}, {client._api_version},{openai_model}")
        completion = client.chat.completions.create(model=openai_model,
                                                    response_format={ "type": "json_object" },
                                                    messages=messages,
                                                    temperature=0,
                                                    max_tokens=max_token
                                                    )
        if validation_fun is not None:
            completion.choices[0].message.content = validation_fun(completion.choices[0].message.content , completion.usage.to_dict()["completion_tokens"] , max_token, token_exhaust_percent)

        return completion
    except Exception as e:
        logger.error(
            f"process_id: {os.getpid()} : "
            + default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e)
        )
        raise e


def alarm_handler(signum, frame):
    """
    This function is like a callback, it is called.
    Once alarm goes off.
    """
    raise TimeOutException("Timeout exception custom.")

def try_load_json_dict(text):
    obj = None
    try:
        obj = json.loads(text)
    except Exception as e:
        logger.warning(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        try:
            obj = literal_eval(text)
        except Exception as e:
            logger.warning(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
    return obj

def response_validation(input_string , token_count , max_token , token_exhaust_percent):
    try:
        logger.info(f"process_id: {os.getpid()} : " + "validation function called")
        logger.debug(f"process_id: {os.getpid()} : " + str(input_string))
        input_string = re.sub('\n+', '', input_string)
        pattern = r'\$(.*?)\$'
        match = re.search(pattern, input_string, re.DOTALL)

        if match:
            json_str = match.group(1)

            try:
                logger.debug(f"process_id: {os.getpid()} : " + str(json_str))

                _json = try_load_json_dict(json_str)
                if isinstance(_json, dict):
                    return json.dumps(_json, ensure_ascii=False)
                else:
                    raise ValueError("gpt output is not a valid dict")

            except (json.JSONDecodeError, ValueError, SyntaxError) as e:
                # Incomplete RESPONSE
                logger.error(
                    f"process_id: {os.getpid()} : " + f"Json validataion failed."
                )
                if token_count > token_exhaust_percent * max_token :
                    logger.warning(f"process_id: {os.getpid()} : " + f"Token Limit crossed {token_exhaust_percent*100}% above: {token_count}")
                    logger.debug(
                        f"process_id: {os.getpid()} : "
                        + f"OpenAI output {input_string}"
                    )
                    raise TokenLimitException(f"Token Limit crossed {token_exhaust_percent*100}% above: {token_count}")
                else:
                    logger.info(f"process_id: {os.getpid()} : " + "InValid Json")
                    # raise TimeOutException("Not a valid json")
                    raise InvalidJson("Not a valid json") #("Not a valid json")
        else:
            logger.info(
                f"process_id: {os.getpid()} : "
                + "Incomplete RESPONSE, output/json is not enclosed between $ signs"
            )
            logger.debug(
                f"process_id: {os.getpid()} : "
                + f"Partial data received for validation : {input_string}"
            )
            try:
                _json = try_load_json_dict(input_string)
                if isinstance(_json , dict):
                    return json.dumps(_json, ensure_ascii=False)
                else:
                    raise ValueError("gpt output is not a valid dict")

            except (json.JSONDecodeError , ValueError, SyntaxError) as e:
                if token_count > token_exhaust_percent * max_token:
                    logger.warning(
                        f"process_id: {os.getpid()} : "
                        + f"Token Limit crossed {token_exhaust_percent*100}% above: {token_count}"
                    )
                    logger.debug(
                        f"process_id: {os.getpid()} : "
                        + f"OpenAI output {input_string}"
                    )
                    raise TokenLimitException(f"Token Limit crossed {token_exhaust_percent*100}% above: {token_count}")
                else:
                    logger.info(f"process_id: {os.getpid()} : " + "InValid Json")
                    raise InvalidJson("Not a valid json")
    except Exception as e:
        logger.error(
            f"process_id: {os.getpid()} : "
            + default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e)
        )
        raise e


def call_azure(messages, language):
    try:
        max_timeout = credentials['max_timeout']
        max_timeout_4k = credentials['max_timeout_4k']
        s_time = time.time()
        signal.signal(signal.SIGALRM, alarm_handler)

        if languages_config[language]["chatgpt_config"]['use_gpt4O']:
            # signal.signal(signal.SIGALRM, alarm_handler) # need to declare after every condition??
            signal.alarm(max_timeout)  # Will raise alarm after x seconds

            try:
                comp, retry, status = _call_azure_gpt4o(messages, max_token=4096, token_exhaust_percent = 0.95, validation_fun = response_validation)
                output_message = comp.choices[0].message.content
                # token_count = comp.usage.to_dict()['completion_tokens']
                # if token_count >= 4096:
                #     raise TokenLimitException(f"Token Limit crossed: {token_count}")
                return output_message, retry, status
            except (TimeOutException, TokenLimitException) as e:
                logger.error(
                    f"process_id: {os.getpid()} : "
                    + default_error_message.format(
                        sys.exc_info()[-1].tb_lineno, type(e), e
                    )
                )
                raise e
            except Exception as e:
                logger.error(f"process_id: {os.getpid()} : " + default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
                raise e
            finally:
                signal.alarm(0)
        elif languages_config[language]["chatgpt_config"]['use_gpt4']:
            # signal.signal(signal.SIGALRM, alarm_handler) # need to declare after every condition??
            signal.alarm(max_timeout)  # Will raise alarm after x seconds

            try:
                comp, retry, status = _call_azure_gpt4(messages, max_token = 8192, token_exhaust_percent = 0.95, validation_fun = response_validation)
                output_message = comp.choices[0].message.content
                # token_count = comp.usage.to_dict()['total_tokens']
                # if token_count >= 8000:
                #     raise TokenLimitException(f"Token Limit crossed: {token_count}")
                return output_message, retry, status
            except (TimeOutException, TokenLimitException) as e: #?? why excepting here?
                logger.error(
                    f"process_id: {os.getpid()} : "
                    + default_error_message.format(
                        sys.exc_info()[-1].tb_lineno, type(e), e
                    )
                )
                raise e
            except Exception as e:
                logger.error(f"process_id: {os.getpid()} : " + default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
                raise e
            finally:
                signal.alarm(0)
        elif languages_config[language]["chatgpt_config"]['use_finetuned']:
            # signal.signal(signal.SIGALRM, alarm_handler)
            signal.alarm(max_timeout_4k)  # Will raise alarm after x seconds

            try:
                comp, retry, status = _call_azure_4k_finetuned(messages)
                output_message = comp.choices[0].message.content
                # token_count = comp.usage.to_dict()['total_tokens']
                # if token_count >= 4096:
                #     raise TokenLimitException(f"Token Limit crossed: {token_count}")
                return output_message, retry, status
            except (TimeOutException, TokenLimitException) as e:
                logger.error(
                    f"process_id: {os.getpid()} : "
                    + default_error_message.format(
                        sys.exc_info()[-1].tb_lineno, type(e), e
                    )
                )
                raise e
            except Exception as e:
                logger.error(f"process_id: {os.getpid()} : " + default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
                raise e
            finally:
                signal.alarm(0)
        else:
            if not languages_config[language]["chatgpt_config"]['use_16k_only']:
                # signal.signal(signal.SIGALRM, alarm_handler)
                signal.alarm(max_timeout_4k)  # Will raise alarm after x seconds

                try:
                    comp, retry, status = _call_azure_4k(messages, max_token = 8192, token_exhaust_percent = 0.95, validation_fun = response_validation)
                    output_message = comp.choices[0].message.content
                    # token_count = comp.usage.to_dict()['total_tokens']
                    # if token_count >= 4096:
                    #     logger.info(f"OpenAI output : {output_message}")
                    #     raise TokenLimitException(f"Token Limit crossed: {token_count}")
                    return output_message, retry, status
                except (TimeOutException, TokenLimitException) as e:
                    logger.error(
                        f"process_id: {os.getpid()} : "
                        + default_error_message.format(
                            sys.exc_info()[-1].tb_lineno, type(e), e
                        )
                    )
                except Exception as e:
                    logger.error(f"process_id: {os.getpid()} : " + default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
                    raise e
                finally:
                    signal.alarm(0)

            e_time = time.time() # is it needed to outside os else block ?
            # signal.signal(signal.SIGALRM, alarm_handler)
            signal.alarm(max_timeout_4k)  # will raise alarm after x seconds
            try:
                logger.info(f"process_id: {os.getpid()} : " + "Trying 16k models ...")
                comp, retry, status = _call_azure_16k(messages, max_token = 16384, token_exhaust_percent = 0.95, validation_fun = response_validation)
                output_message = comp.choices[0].message.content

                return output_message, retry, status
            # except TimeOutException as e:
            #     logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            #     raise e
            except Exception as e:
                logger.error(f"process_id: {os.getpid()} : " + default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
                raise e
            finally:
                signal.alarm(0)
    except Exception as e:
        logger.error(f"process_id: {os.getpid()} : " + default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e

def find_best_match(phrase, sentences, entity, language):
    try:

        #  DIRECT SEARCH
        for idx, sent in enumerate(sentences):
            if phrase in sent:
                return idx
        phrase_sents = custom_sent_tokenize(phrase, language)
        best_metric = 0
        best_index = None
        if len(phrase_sents) == 1:
            # PARTIAL RATIO SEARCH
            for idx, sent in enumerate(sentences):
                metric = fuzz.partial_ratio(phrase, sent)
                if metric > best_metric:
                    best_metric = metric
                    best_index = idx
        elif len(phrase_sents) > 1:
            # DIRECT SEARCH
            for ps in phrase_sents:
                if entity in ps:
                    for idx, sent in enumerate(sentences):
                        if ps in sent:
                            return idx
            # PARTIAL RATIO SEARCH
            for ps in phrase_sents:
                for idx, sent in enumerate(sentences):
                    metric = fuzz.partial_ratio(ps, sent)
                    if metric > best_metric:
                        best_metric = metric
                        best_index = idx
        if best_index is None:
            logger.info(f"************: {best_index}, {best_metric}, {phrase}")
            logger.info(f"{sentences}")
        return best_index
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def format_openai_output(openai_output, sentences, langauge):
    try:
        select_ele = []
        derived_ele = []
        for i, ele in enumerate(openai_output):
            try:
                try:
                    ele['conf_score'] = float(ele["conf"])
                except ValueError as e:
                    ele['conf_score'] = 0.9
                    logger.error(f"Error converting to float: {e}, value: {ele['conf']}")
                ele["model"] = "openai"
                ele['entity_text'] = ele['entity_value']
                ele["sentence"] = ele["entity_sentence"]
                ele.pop('entity_value')
                ele.pop('entity_sentence')
                ele["negated"] = None
                ele.pop('conf')
                if ele['entity_text'] not in ["null", None, ''] \
                        and ele['sentence'] not in ["null", None, ''] \
                        and type(ele['entity_text']) == type(""):
                    phrase = ele['sentence']
                    map_entity_text = {'AUTOPSY_DONE': languages_config[langauge]["translation_mapping"]['autopsy'], 'PREGNANCY': languages_config[langauge]["translation_mapping"]['pregnancy']}
                    if ele['entity_text'] in ['True', 'true', True]:
                        ele['entity_text'] = map_entity_text[ele['entity_label']]
                    bestmatch_id = find_best_match(phrase, sentences, ele['entity_text'], langauge)
                    if bestmatch_id is not None:
                        ele['sent_id'] = bestmatch_id + 1
                        ele['sentence'] = sentences[ele['sent_id'] - 1]
                        ele['start'] = ele['sentence'].lower().find(ele['entity_text'].lower())
                    else:
                        ele["sent_id"] = -1
                        ele['start'] == -1
                    if ele['start'] == -1:
                        ele['end'] = -1
                        derived_ele.append(ele)
                        continue
                    ele['end'] = ele['start'] + len(ele['entity_text'])
                    select_ele.append(ele)
                elif (
                    ele["sentence"] in ["null", None, ""]
                    and ele["entity_text"] not in ["null", None, ""]
                    and type(ele["entity_text"]) == type("")
                ):
                    ele['sent_id'] = -1
                    ele['sentence'] = 'null'
                    ele['start'] = -1
                    ele['end'] = -1
                    derived_ele.append(ele)

            except Exception as e:
                logger.info(f"{ele}")
                logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
                raise e

        return select_ele, [len(select_ele), len(openai_output)], derived_ele
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def process_response(response):
    try:
        entities = []
        x = re.sub(r'"null"', "null", response)
        x = re.sub(r'\bnull\b', '"null"', x)
        x = re.sub('\n', '', x)
        x = re.findall(r"\[[^\[\]]*\]", x)
        for _x in x:
            try:
                ent = json.loads(_x)
                # print(ent)
                logger.info(f"process_id: {os.getpid()} : " + str(_x))
                temp_entity = {"entity_label": ent[0],
                                 "entity_value": ent[1],
                                 "entity_sentence": ent[2]}
                try:
                    temp_entity["conf"] = ent[3]
                except Exception as e:
                    logger.warning(
                        f"process_id: {os.getpid()} : "
                        + default_error_message.format(
                            sys.exc_info()[-1].tb_lineno, type(e), e
                        )
                    )
                    temp_entity["conf"] = 0.9
                entities.append(temp_entity)
            except IndexError as e:
                logger.warning(
                    f"process_id: {os.getpid()} : "
                    + default_error_message.format(
                        sys.exc_info()[-1].tb_lineno, type(e), e
                    )
                )
            except Exception as e:
                logger.error(
                    f"process_id: {os.getpid()} : "
                    + default_error_message.format(
                        sys.exc_info()[-1].tb_lineno, type(e), e
                    )
                )
        return entities
    except Exception as e:
        logger.error(
            f"process_id: {os.getpid()} : "
            + default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e)
        )
        raise e


def execute_and_wait(args):
    try:
        # executor = ProcessPoolExecutor(credentials['max_requests_openai'])
        # futures = []

        # for arg in args:
        #     futures.append(executor.submit(xrun_openai_query, arg[0], arg[1], arg[2], arg[3]))

        # while True:
        #     time.sleep(2)
        #     count_done = 0

        #     for f in futures:
        #         if f.done():
        #             count_done += 1
        #     if count_done == len(args):
        #         break

        # return futures

        futures = []
        with ProcessPoolExecutor(credentials["max_requests_openai"]) as executor:
            futures = [
                executor.submit(run_openai_query, arg[0], arg[1], arg[2], arg[3])
                for arg in args
            ]
            wait(futures, return_when="FIRST_EXCEPTION")
        return futures

    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def run_openai_query(narrative, prompt, process_response_as_json, language):
    try:
        messages = []
        messages.append(
            {
                "role": "user",
                "content": prompt.replace("<narrative>", narrative)
            }
        )
        # t1 = time.time()
        output_message, _retry, _status = call_azure(messages, language)
        # logger.info(f"Total tokens: {completion['usage']['total_tokens']}")
        # output_message = completion["choices"][0]["message"]["content"]
        if process_response_as_json:
            result = json.loads(output_message)
        else:
            result = process_response(output_message)
        return result, "success"
        # openai_output = []
        # if isinstance(result, list):
        #     openai_output = result
        # else:
        #     for key in result:
        #         openai_output.extend(result[key])
        # return openai_output, "success"
    except Exception as e:
        logger.error(f"process_id: {os.getpid()} : " + default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        logger.error(f"process_id: {os.getpid()} : " + f"{output_message}")
        raise e

def format_template(x):
    x = re.sub(r"\n+", " ", x)
    x = re.sub(r"\s+", " ", x)
    return x


def get_openai_output(narrative_text, language):
    try:
        template_name = languages_config[language]["templates"]
        arg1 = [
            (narrative_text, format_template(languages_config[language]["templates_library"][template_name]["1"]), True, language),
            (narrative_text, format_template(languages_config[language]["templates_library"][template_name]["2"]), False, language)
        ]

        futures = execute_and_wait(arg1)

        count_success = 0
        res_1 = []
        res_2 = []

        for f in futures:
            try:
                result = f.result()
                if result[1] == "success":
                    count_success += 1
                    if type(result[0]) is dict:
                        res_1 = result[0]
                    else:
                        res_2 = result[0]
            except Exception as e:
                logger.warning(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
                if type(e) == TokenLimitException:
                    raise e
                # print(f.exception())
                # raise e
                break

        logger.info(f"count_success: {count_success}")
        if count_success == len(arg1):
            final_output = []
            msg_3 = format_template(languages_config[language]["templates_library"][template_name]["3"])
            msg_3 = msg_3.replace("**PRODUCTS**", f"{res_1['PRODUCTS']}") if 'PRODUCTS' in res_1.keys() else msg_3.replace("**PRODUCTS**", f"{[]}")
            msg_3 = msg_3.replace("**PATIENT_PAST_DRUGS**", f"{res_1['PATIENT_PAST_DRUGS']}") if 'PATIENT_PAST_DRUGS' in res_1.keys() else msg_3.replace("**PATIENT_PAST_DRUGS**", f"{[]}")
            msg_3 = msg_3.replace("**PARENT_DRUG_HISTORIES**", f"{res_1['PARENT_DRUG_HISTORIES']}") if 'PARENT_DRUG_HISTORIES' in res_1.keys() else msg_3.replace("**PARENT_DRUG_HISTORIES**", f"{[]}")

            msg_4 = format_template(languages_config[language]["templates_library"][template_name]["4"])
            msg_4 = msg_4.replace("**LAB_TEST_NAMES**", f"{res_1['LAB_TEST_NAMES']}") if 'LAB_TEST_NAMES' in res_1.keys() else msg_4.replace("**LAB_TEST_NAMES**", f"{[]}")

            msg_5 = format_template(languages_config[language]["templates_library"][template_name]["5"])
            msg_5 = msg_5.replace("**DEATH_CAUSES**", f"{res_1['DEATH_CAUSES']}") if 'DEATH_CAUSES' in res_1.keys() else msg_5.replace("**DEATH_CAUSES**", f"{[]}")
            msg_5 = msg_5.replace("**MEDICAL_HISTORIES**", f"{res_1['MEDICAL_HISTORIES']}") if 'MEDICAL_HISTORIES' in res_1.keys() else msg_5.replace("**MEDICAL_HISTORIES**", f"{[]}")
            msg_5 = msg_5.replace("**EVENTS**", f"{res_1['EVENTS']}") if 'EVENTS' in res_1.keys() else msg_5.replace("**EVENTS**", f"{[]}")
            msg_5 = msg_5.replace("**PARENT_MEDICAL_HISTORIES**", f"{res_1['PARENT_MEDICAL_HISTORIES']}") if 'PARENT_MEDICAL_HISTORIES' in res_1.keys() else msg_5.replace("**PARENT_MEDICAL_HISTORIES**", f"{[]}")

            arg2 = [
                (narrative_text, msg_3, False, language),
                (narrative_text, msg_4, False, language),
                (narrative_text, msg_5, False, language),
            ]
            futures = execute_and_wait(arg2)

            count_success = 0
            _res = []

            for f in futures:
                try:
                    result = f.result()
                    if result[1] == "success":
                        count_success += 1
                        _res.extend(result[0])

                except Exception as e:
                    logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
                    # print(f.exception())
                    # raise e
                    if type(e) == TokenLimitException:
                        raise e
                    break

            final_output = _res
            final_output.extend(res_2)
            if count_success == len(arg2):
                return final_output
            else:
                raise Exception("Some openai call failed!")
        else:
            raise Exception("Some openai call failed!")
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def get_model_output(narrative_text, language):
    """
    maintains flow for prediction and processing of output
    """
    try:
        sentences = custom_sent_tokenize(narrative_text, language)
        
        # openai_output = get_openai_output(narrative_text, language)
        try:
            openai_output = get_openai_output(narrative_text, language)
        except Exception as e:
            if type(e) == TokenLimitException: #retry by breaking the 
                part_1 = " ".join(sentences[:(len(sentences) // 2)])
                part_2 = " ".join(sentences[(len(sentences) // 2):])
                openai_output = []
                openai_output.extend(get_openai_output(part_1, language))
                openai_output.extend(get_openai_output(part_2, language))
            else:
                raise e
                
        stime = time.perf_counter()
        format_output, count_entity, derived_entity = format_openai_output(copy.deepcopy(openai_output), sentences, language)
        logger.info(f"Time taken for source highlighting: {time.perf_counter() - stime}s")
        return openai_output, format_output, count_entity, derived_entity
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


# @app_bio.route('/refreshConfigs')
# def refresh_code_list():
# try:
# load_updated_config()
# return jsonify({"message": "Configs refreshed for OpenAI api"}), 200
# except Exception as e:
# logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
# raise e

@app_bio.route('/predict/openai', methods=['POST'])
def main():
    try:
        if "narrative" in request.form and "language" in request.form:
            s_time = time.time()
            narrative_text = request.form["narrative"]
            language = request.form['language']
            # logger.info(f'***************************** Identifier: {datetime.now().strftime("%m_%d_%Y_%H_%M_%S_%f")}')
            logger.info(f"Received Narrative: {narrative_text[:20]}...")

            input_token_count = num_tokens_from_string(narrative_text)
            logger.info(f"Number of tokens in narrative: {input_token_count}")
            if input_token_count > credentials['input_max_tokens']:
                return {"code": 1,
                        "message": f"Too long narrative, contains {input_token_count} tokens, more than {credentials['input_max_tokens']}"}

            openai_output, format_output, count_entity, derived_entity = get_model_output(narrative_text, language)

            output_json = {"code": 0,
                           "entities": format_output,
                           "all_entities": openai_output,
                           "derived_entities": derived_entity,
                           "selected_entity_count": count_entity[0],
                           "total_entity_count": count_entity[1]
                           }
            logger.info(f"Total time taken: {time.time()-s_time} seconds.")
            return output_json
        else:
            return {"code": 1, "message": "narrative or language parameter not found in request payload."}
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        return {"code": 1, "message": e.__str__()}


if __name__ == "__main__":
    # load_updated_config(False)
    app_bio.run(host=global_config["OPENAI_API"]["URL"], port=global_config["OPENAI_API"]["PORT"])
