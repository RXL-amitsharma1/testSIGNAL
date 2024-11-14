import sys
import json
from openai_api import _call_azure_gpt4o , response_validation ,credentials , TimeOutException
import timeout_decorator
from configs import languages_config

import logs
logger = logs.get_logger()
default_error_message = "Inside case_classifier.py - An error occurred on line {}. Exception type: {}, Exception :{} "

@timeout_decorator.timeout(credentials["max_timeout"] , timeout_exception=TimeOutException , use_signals=False)
def case_classification(narrative):
    try:
        prompt = languages_config["en"]["templates_library"][languages_config["en"]["template_case_classification"]]

        messages = [{"role":"user", 
                     "content": prompt.replace("<narrative>", narrative)}]

        comp, retry, status = _call_azure_gpt4o(messages, max_token = 4096, token_exhaust_percent = 0.95, validation_fun=response_validation)
        res = comp.choices[0].message.content
        res = json.loads(res)
        
        res["categories"] = res.get("categories", "").split("-")
        res["categories"] = list(map(lambda x :x.upper().strip(), res["categories"])) #Change to upper case and strip
        res["categories"] = list(filter(lambda x : x in ["AE","MI","SPAM","PQC","OTHER_MEDICAL"] , res["categories"])) #filter only possible values
        if len(res["categories"]) == 0:
            raise Exception("No supported case Categories was identified")

        try:
            if res.get("categories_acc"):
                res["categories_acc"] = float(res["categories_acc"])
        except ValueError as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            res["categories_acc"] = 0.85

        return res    
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        return json.loads(str({"categories" :[]}))
