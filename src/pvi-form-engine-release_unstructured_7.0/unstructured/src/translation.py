import json
import uuid
import requests
import os
import sys
import ast
from google.cloud import translate
from logs import get_logger
from configs import config_dir, global_config as config
logger = get_logger()
ROOT_DIR = os.path.dirname(os.path.abspath(__file__))

default_error_message = "Inside translation.py - An error occured on line {}. Exception type: {} , Exception: {} "




def location_path(project_id, location):
    try:
        # might as well use an f-string, the new library supports python >=3.6
        return f"projects/{project_id}/locations/{location}"
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def google_lang_detection(sample, project_id):
    try:
        source_lan = ""
        res = []
        """
        Detecting the language of a text string

        Args:
        text The text string for performing language detection
        """
        #TODO set env variable only once
        os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = os.path.join(config_dir , "rx-google-translate-silicon-carver-259412-5a06d7b54235.json")
        client = translate.TranslationServiceClient()

        # TODO(developer): Uncomment and set the following variables
        # text = 'Hello, world!'
        # project_id = '[Google Cloud project_id ID]'
        parent = location_path(project_id, "global")

        response = client.detect_language(
            content=sample,
            parent=parent,
            mime_type='text/plain'  # mime types: text/plain, text/html
        )
        # Display list of detected languages sorted by detection confidence.
        # The most probable language is first.
        for language in response.languages:
            source_lan = format(language.language_code)
            # The language detected
            logger.debug(u"Language code: {}".format(language.language_code))
            # Confidence of detection result for this language
            logger.debug(u"Confidence: {}".format(language.confidence))
        if source_lan != "":
            return source_lan

        return res
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def google_translate_text(text, source_language, target_language, project_id):
    try:
        translated_data = []
        os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = os.path.join(config_dir , "rx-google-translate-silicon-carver-259412-5a06d7b54235.json")
        """
        Translating Text

        Args:
        text The content to translate in string format
        target_language Required. The BCP-47 language code to use for translation.
        """

        client = translate.TranslationServiceClient()
        # TODO(developer): Uncomment and set the following variables
        # text = 'Text you wish to translate'
        # target_language = 'fr'
        # project_id = '[Google Cloud Project ID]'
        contents = text

        parent = location_path(project_id, "global")

        response = client.translate_text(
            parent=parent,
            contents=[contents],
            mime_type='text/plain',  # mime types: text/plain, text/html
            source_language_code=source_language,
            target_language_code=target_language)
        for translation in response.translations:
            translated_data.append(translation.translated_text)

        return translated_data[0]
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def google_translate(data, input_lang, target_ln):
    try:
        project_id = config['GOOGLE']['project_id']
        languages_supported = ast.literal_eval(config['LANGUAGE_SUPPORTED']['languages'])
        if input_lang == 'unknown':
            input_lang = google_lang_detection(data, project_id)
        if input_lang in languages_supported:
            logger.info("Language is supported, hence no translation performed")
            return data, input_lang

        translation = google_translate_text(data, input_lang, target_ln, project_id)
        logger.info("Translation:  %s", translation)
        return translation, target_ln
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def microsoft_lang_detection(text, endpoint, lang_detect_path, headers):
    try:
        body = [{
            'text': text
        }]
        request = requests.post(endpoint + lang_detect_path, headers=headers, json=body)
        response = request.json()

        logger.debug(json.dumps(response, sort_keys=True, indent=2, ensure_ascii=False, separators=(',', ': ')))
        return response[0]['language']
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def microsoft_translate(text, input_lang, target_lang):
    try:
        key = config["MICROSOFT"]["Ocp-Apim-Subscription-Key"]
        endpoint = config["MICROSOFT"]["endpoint"]
        location = config["MICROSOFT"]["Ocp-Apim-Subscription-Region"]
        translate_path = config["MICROSOFT"]["translate_path"]
        lang_detect_path = config["MICROSOFT"]["lang_detect_path"]

        headers = {
            'Ocp-Apim-Subscription-Key': key,
            'Ocp-Apim-Subscription-Region': location,
            'Content-type': 'application/json',
            'X-ClientTraceId': str(uuid.uuid4())
        }
        languages_supported = ast.literal_eval(config['LANGUAGE_SUPPORTED']['languages'])
        if input_lang == 'unknown':
            input_lang = microsoft_lang_detection(text, endpoint, lang_detect_path, headers)
            logger.info(f"Language Detected as {input_lang}")
        if input_lang in languages_supported:
            logger.info("Language is supported, hence no translation performed")
            return text, input_lang
        params = {
            'api-version': '3.0',
            'from': input_lang,
            'to': target_lang
        }

        body = [{
            'text': text
        }]

        request = requests.post(endpoint + translate_path, params=params, headers=headers, json=body)
        response = request.json()

        logger.debug(json.dumps(response, sort_keys=True, ensure_ascii=False, indent=2, separators=(',', ': ')))
        logger.info("Translation:  %s", response[0]["translations"][0]["text"])
        return response[0]["translations"][0]["text"], target_lang
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def translate_text(text):
    try:
        lang = "en"

        if not config.getboolean('TRANSLATION', 'switch'):
            logger.info("Translation switch set to OFF.")
            return text, lang

        logger.info("Translation text...")
        translation = ""
        translator = config['TRANSLATION']['translator']
        input_lang = config['TRANSLATION']['input_lang']   #TODO confirmation of input langauge
        target_lang = config['TRANSLATION']['target_lang']

        if translator == "google":
            translation, is_language_supported, lang = google_translate(text, input_lang, target_lang)
        elif translator == "microsoft":
            translation, lang = microsoft_translate(text, input_lang, target_lang)

        return translation, lang     #make sure openai switch is always ON.
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


