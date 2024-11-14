import re
import sys
from logs import get_logger
from utils import convert_values_to_none
from configs import languages_config
logger = get_logger()
default_error_message = "Inside post_process_json.py - An error occured on line {}. Exception type: {} , Exception: {}"


def process_height_weight(json):
    try:
        new_tests = []
        if len(json["tests"]) > 0:
            for test in json["tests"]:
                if "height" in str(test["testName"]).lower():
                    if json["patient"]["height"] is None:
                        json["patient"]["height"] = test["testResult"]
                        json["patient"]["height_acc"] = test["testResult_acc"]
                        json["patient"]["height_viewid"] = test["testResult_viewid"]
                    if json["patient"]["heightUnit"] is None:
                        json["patient"]["heightUnit"] = test["testResultUnit"]
                        json["patient"]["heightUnit_acc"] = test["testResult_acc"]
                elif "weight" in str(test["testName"]).lower() or "weigh" in str(test["testName"]).lower():
                    if json["patient"]["weight"] is None:
                        json["patient"]["weight"] = test["testResult"]
                        json["patient"]["weight_acc"] = test["testResult_acc"]
                        json["patient"]["weight_viewid"] = test["testResult_viewid"]
                    if json["patient"]["weightUnit"] is None:
                        json["patient"]["weightUnit"] = test["testResultUnit"]
                        json["patient"]["weightUnit_acc"] = test["testResult_acc"]
                elif "lmp" in str(test["testName"]).lower() or "mother" in str(test["testName"]).lower():
                    continue
                else:
                    new_tests.append(test)
            json["tests"] = new_tests if len(new_tests) != 0 else [convert_values_to_none(json["tests"][0])]
        return json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def process_accuracy(json):
    try:
        if "united kingdom" in (str(json["reporters"][0]["country"])).lower():
            json["reporters"][0]["country"] = "United Kingdom of Great Britain"
        if len(json["products"]) > 0:
            for index in range(len(json["products"])):
                if json["products"][index]["doseInformations"][0]["description"] not in ["", None]:
                    json["products"][index]["doseInformations"][0]["description_acc"] = '0.99'
        return json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def process_ethnicity(json):
    try:
        json["patient"]["ethnicGroup"] = json["patient"].get("ethnicity", None)
        json["patient"]["ethnicGroup_acc"] = json["patient"].get("ethnicity_acc", None)
        json["patient"]["ethnicGroup_viewid"] = json["patient"].get("ethnicity_viewid", None)
        if json["patient"]["ethnicGroup"] and ("hispanic" in json["patient"]["ethnicGroup"].lower() or "latino" in json["patient"]["ethnicGroup"].lower()):
            json["patient"]["ethnicGroup"] = "Hispanic or Latino"
    except Exception as e:
        logger.warning(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        pass
    return json


def process_lab_test(json, language):
    """
    Remove lab test from json with autopsy as test name,
    add conf scores and viewids to the splitted data
    """
    try:
        test_section = []
        for test in json["tests"]:

            if test["testName"] and test["testName"].lower() != languages_config[language]["translation_mapping"]['autopsy']:
                if test['testNotes'] and not test['testResultUnit'] and not test['testResult']: #if regex not matched in json prepare file
                    test["testNotes_acc"], test["testResult_acc"] = test["testResult_acc"], None
                    test["testNotes_viewid"], test["testResult_viewid"] = test["testResult_viewid"], None

                #add conf score and viewid for resultunit
                if test["testResult"] and test["testResultUnit"] and (not test.get("testResultUnit_viewid", None) or not test.get("testResult_acc", None)):
                    test["testResultUnit_viewid"], test["testResultUnit_acc"] = test["testResult_viewid"], test["testResult_acc"]
                testassements = languages_config[language]["translation_mapping"]["testAssessment"]
                #TODO list
                assesment_pattern = '|'.join(fr"{testassements[key]}" for key in testassements) #possible values in test assesment codelist(labResultAssessments)
                match = re.search(assesment_pattern, str(test["testNotes"]), re.IGNORECASE)
                if match:
                    test["testAssesment"] = match.group().title() #convert to titlecase as present in PVCM codelist
                    test["testAssesment_acc"] = test.get("testNotes_acc", None)
                    test["testAssesment_viewid"] = test.get("testNotes_viewid", None)
                    test["testNotes"] = re.sub(assesment_pattern, "", test["testNotes"], flags=re.IGNORECASE).strip().strip(";").strip()
                    if not test["testNotes"]:
                        #testnotes setting as null if empty string after removing test assement
                        test["testNotes"], test["testNotes_acc"], test["testNotes_viewid"] = None, None, None

                # months = ["1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"]
                months = list(languages_config[language]["translation_mapping"]["months"].values())
                test['testName'] = None if any(month in test['testName'] for month in months) else test['testName']
                if test["testName"] and test["testName"].strip().strip("}: ;"):
                    test["testName"] = test["testName"].strip(": ;")
                    test["seq_num"] = len(test_section)+1
                    test_section.append(test)

        json["tests"] = test_section if len(test_section) != 0 else [convert_values_to_none(json["tests"][0])]
        return json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def post_process(json, language):
    try:
        json = process_height_weight(json)
        json = process_accuracy(json)
        json = process_ethnicity(json)
        json = process_lab_test(json, language)
        return json
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e
