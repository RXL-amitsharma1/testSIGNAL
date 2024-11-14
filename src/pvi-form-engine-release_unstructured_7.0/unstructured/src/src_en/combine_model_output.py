import sys
import logs
logger = logs.get_logger()
default_error_message = "Inside combine_model_output.py - An error occured on line {}. Exception type: {} , Exception: {} "

from configs import entity_json

def remove_punctutions(entity):
    try:
        punctutions = ",."
        strip_text = entity["entity_text"].lstrip(punctutions)
        if len(strip_text) < len(entity["entity_text"]):
            entity["entity_text"] = strip_text
            entity["start"] = entity["start"]+1
        strip_text = entity["entity_text"].rstrip(punctutions)
        if len(strip_text) < len(entity["entity_text"]):
            entity["entity_text"] = strip_text
            entity["end"] = entity["end"]-1
        return entity
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def combine(entities):
    try:
        combined_entities , combined_output= [], []
        
        _combined_entities = entities
        for entity in _combined_entities:
            if entity_json[entity["entity_label"].upper()] == False or \
                    (str(entity_json[entity["entity_label"].upper()]).lower() != "both" and \
                    str(entity_json[entity["entity_label"].upper()]).lower() != entity["model"].lower()):
                pass
            else:
                combined_entities.append(entity)
        entities = []
        for element in combined_entities:
            element = remove_punctutions(element)
            found = False
            for index, final_element in enumerate(combined_output):
                if element["entity_label"] == final_element["entity_label"] or element["entity_label"] in final_element["entity_label"] \
                        or final_element["entity_label"] in element["entity_label"]:
                    if element["start"] == final_element["start"] and   element["sent_id"] == final_element["sent_id"]:
                        found = True
                        if element["end"] > final_element["end"]:
                            final_element["end"] = element["end"]
                            final_element["entity_text"] = element["entity_text"]
                            final_element["model"] = element["model"]
                    elif element["end"] == final_element["end"] and  element["sent_id"] == final_element["sent_id"]:
                        found = True
                        if element["start"] < final_element["start"]:
                            final_element["start"] = element["start"]
                            final_element["entity_text"] = element["entity_text"]
                            final_element["model"] = element["model"]
            if not found:
                combined_output.append(element)
       
        entities.extend(combined_output)
        logger.debug(entities)
        return entities
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e

