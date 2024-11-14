import pandas as pd
import math
from logs import get_logger
import sys
logger = get_logger()
default_error_message = "Inside entity_linker.py - An error occured on line {}. Exception type: {} , Exception: {} "
from configs import load_config
config = load_config("ent_link_config.ini")


def check_equal(pri_ent_df, sec_ent_df, n):
    try:
        if len(pri_ent_df) == len(sec_ent_df):
            for index in range(len(pri_ent_df)):
                pri_ind, sec_ind = pri_ent_df.iloc[index], sec_ent_df.iloc[index]
                avg_dist = abs(((pri_ind["start"] + pri_ind["end"]) / 2) - ((sec_ind["start"] + sec_ind["end"]) / 2))
                if avg_dist > n:
                    return False
            return True
        return False
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def look_ahead(filtered_df, index, pri_ent, n):
    try:
        mean_index = (filtered_df.iloc[index]["start"] + filtered_df.iloc[index]["end"]) / 2
        ent_label = filtered_df.iloc[index]["entity_label"]
        for ind in range(index + 1, len(filtered_df)):
            row = filtered_df.iloc[ind]
            label = row["entity_label"]
            if abs(mean_index - (row["start"] + row["end"]) / 2) <= n:
                if label == ent_label:
                    return -1
                if label == pri_ent:
                    return ind
            else:
                return -1
        return -1
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def add_new_pri_ent(sentence_linked_df, row):
    try:
        append_dict = {row["entity_label"]: row["entity_text"], "start": row["start"], "end": row["end"],
                    "sent_id": row["sent_id"], "conf_score": row["conf_score"]}
        if len(sentence_linked_df[sentence_linked_df["start"] == row["start"]]) == 0:
            sentence_linked_df = pd.concat([sentence_linked_df , pd.DataFrame([append_dict])] , ignore_index=True)
        return sentence_linked_df
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def generate_df(pri_ent, sec_ent, pri_ent_df, sec_ent_df, sentence_linked_df):
    try:
        for index, row in pri_ent_df.iterrows():
            # check if product already present
            prod_list = sentence_linked_df[sentence_linked_df["start"] == row["start"]].index.tolist()
            try:
                sec_row = sec_ent_df.iloc[index] #if len(sec_ent_df) != 1 else sec_ent_df.iloc[0] #for scenario if want to add single secondary entity to all primary entity
                append_dict = {pri_ent: row["entity_text"], "start": row["start"], "end": row["end"],
                        "additionalNotes": row["additionalNotes"],
                            sec_ent: sec_row["entity_text"], sec_ent + "_start": sec_row["start"],
                            sec_ent + "_end": sec_row["end"], sec_ent + "_conf_score": sec_row["conf_score"],
                            "sent_id": row["sent_id"], "conf_score": row["conf_score"], sec_ent + "_additionalNotes": sec_row["additionalNotes"], sec_ent + "_semantic_match": sec_row["semantic_match"]}
                if len(prod_list) > 0:
                    sentence_linked_df.iloc[prod_list[-1], sentence_linked_df.columns.get_loc(sec_ent)] = sec_row["entity_text"]
                    sentence_linked_df.iloc[prod_list[-1], sentence_linked_df.columns.get_loc(sec_ent + "_start")] = \
                        sec_row["start"]
                    sentence_linked_df.iloc[prod_list[-1], sentence_linked_df.columns.get_loc(sec_ent + "_end")] = \
                        sec_row["end"]
                    sentence_linked_df.iloc[prod_list[-1], sentence_linked_df.columns.get_loc(sec_ent + "_conf_score")] = \
                        sec_row["conf_score"]
                    sentence_linked_df.iloc[prod_list[-1], sentence_linked_df.columns.get_loc(sec_ent + "_additionalNotes")] = \
                        sec_row["additionalNotes"]
                    sentence_linked_df.iloc[prod_list[-1], sentence_linked_df.columns.get_loc(sec_ent + "_semantic_match")] = \
                        sec_row["semantic_match"]
                else:
                    sentence_linked_df = pd.concat([sentence_linked_df, pd.DataFrame([append_dict])] , ignore_index=True)
            except IndexError as e:
                logger.error("Index ERROR skipping secondary mapping")
                logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        return sentence_linked_df
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def link_two_ents(sentence_linked_df, pri_ent_row, sec_ent_row):
    try:
        pri_ent_name = pri_ent_row["entity_text"]
        sec_ent_name = sec_ent_row["entity_text"]
        pri_label = pri_ent_row["entity_label"]
        sec_label = sec_ent_row["entity_label"]
        append_dict = {pri_label: pri_ent_name, "start": pri_ent_row["start"], "end": pri_ent_row["end"],
                    "additionalNotes": pri_ent_row["additionalNotes"], "semantic_match": pri_ent_row["semantic_match"],
                    sec_label + "_start": sec_ent_row["start"], sec_label + "_end": sec_ent_row["end"],
                    sec_label + "_additionalNotes": sec_ent_row["additionalNotes"], sec_label + "_semantic_match": sec_ent_row["semantic_match"],
                    "sent_id": pri_ent_row["sent_id"], sec_label: sec_ent_name, "conf_score": pri_ent_row["conf_score"]}
        existing_list = sentence_linked_df[sentence_linked_df["start"] == pri_ent_row["start"]].index.tolist()
        if len(existing_list) == 0:
            sentence_linked_df = pd.concat([sentence_linked_df , pd.DataFrame([append_dict])] ,ignore_index=True)
        else:
            current_value = sentence_linked_df.iloc[existing_list[-1]][sec_label]
            if type(current_value) == float and math.isnan(current_value):
                sentence_linked_df.loc[existing_list[-1], sec_label] = sec_ent_name
                sentence_linked_df.loc[existing_list[-1], sec_label + "_start"] = sec_ent_row["start"]
                sentence_linked_df.loc[existing_list[-1], sec_label + "_end"] = sec_ent_row["end"]
                sentence_linked_df.loc[existing_list[-1], sec_label + "_additionalNotes"] = sec_ent_row["additionalNotes"]
                sentence_linked_df.loc[existing_list[-1], sec_label + "_semantic_match"] = sec_ent_row[
                    "semantic_match"]
            else:
                sentence_linked_df = pd.concat([sentence_linked_df , pd.DataFrame([append_dict])],ignore_index=True)
        return sentence_linked_df
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def generate_sentence_linked_df(filtered_df, pri_ent, last_ent, last_ent_index, sent_linked_df, n):
    try:
        last_pri_ent, last_pri_ent_index = -1, -1  # stores the avg index of last primary entity found
        for index in range(len(filtered_df)):
            row = filtered_df.iloc[index]
            mean_index = (row["start"] + row["end"]) / 2
            ent_label = row["entity_label"]
            if ent_label == pri_ent:
                last_pri_ent, last_pri_ent_index = mean_index, index
                sent_linked_df = add_new_pri_ent(sent_linked_df, row)
                continue
            if last_pri_ent != -1 and abs(last_pri_ent - mean_index) <= n:
                if last_ent[ent_label] <= last_pri_ent:
                    sent_linked_df = link_two_ents(sent_linked_df, filtered_df.iloc[last_pri_ent_index], row)
                else:
                    forward_index = look_ahead(filtered_df, index, pri_ent, n)
                    if forward_index == -1:
                        sent_linked_df = link_two_ents(sent_linked_df, filtered_df.iloc[last_pri_ent_index], row)
            else:
                forward_index = look_ahead(filtered_df, index, pri_ent, n)
                if forward_index != -1:
                    sent_linked_df = link_two_ents(sent_linked_df, filtered_df.iloc[forward_index], row)
            last_ent[ent_label], last_ent_index[ent_label] = mean_index, index
        return sent_linked_df
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def link_sentence_ents(info_attr, pri_ent, sec_ents, ent_df, n=60):
    try:
        """function to link the entities, pri_ent is main entity, sec_ent is sub entity,
        ent_df is dataframe of information on which logic will be applied to create a link"""
        # checking if there are equal num of primary ent and any of secondary ents
        equal_ents = []
        sent_linked_df = pd.DataFrame(columns=info_attr)
        for sec_ent in sec_ents:
            pri_ent_df = ent_df[(ent_df["entity_label"] == pri_ent)].reset_index(drop=True)
            sec_ent_df = ent_df[(ent_df["entity_label"] == sec_ent)].reset_index(drop=True)
            if not pri_ent_df.empty and not sec_ent_df.empty and check_equal(pri_ent_df, sec_ent_df, n):
                sent_linked_df = generate_df(pri_ent, sec_ent, pri_ent_df, sec_ent_df, sent_linked_df)
                equal_ents.append(sec_ent)
            # elif not pri_ent_df.empty and not sec_ent_df.empty and len(sec_ent_df)==1: #scenario where number of primary and secondary entities are not equal
            #     generate_df(pri_ent, sec_ent, pri_ent_df, sec_ent_df, sent_linked_df)
            #     print("p")

            # :TODO elif not equal entities only one test date and multiple test names
        # df after removing the entity labels which were matched in one-to-one mapping
        filtered_df = ent_df[~ent_df["entity_label"].isin(equal_ents)]

        # dictionaries to keep track of mean distance and index of other entities
        last_ent = {}
        last_ent_index = {}
        for e in sec_ents:
            last_ent[e] = -1
            last_ent_index[e] = -1  # stores avg index of last entity found

        sent_linked_df = generate_sentence_linked_df(filtered_df, pri_ent, last_ent, last_ent_index, sent_linked_df, n)

        return sent_linked_df
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def filter_df(linked_df, pri_ent, secondary_ents):
    try:
        linked_df.drop_duplicates(inplace=True, subset=[pri_ent] + secondary_ents)
        linked_df.reset_index(inplace=True, drop=True)
        frequencies = linked_df[pri_ent].value_counts()
        res_df = pd.DataFrame(columns=linked_df.columns)
        for key in frequencies.keys():
            if frequencies[key] > 1:
                intermediate_res = linked_df[linked_df[pri_ent] == key].dropna(how="all", subset=secondary_ents)
                res_df = pd.concat([res_df, intermediate_res])
            else:
                res_df = pd.concat([res_df, linked_df[linked_df[pri_ent] == key]])
        return res_df.sort_index().reset_index(drop=True)
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e

def link_ents(ent_df):
    try:
        """Function to receive all total extracted entities, link them by sentence_ids and return the linked data"""
        section = config['Entities_SubEntities']
        primary_ents = [key.upper() for key in section.keys()]
        result_list_of_df = {}
        char_diff_sec = config['Entities_Char_Difference']
        for pri_ent in primary_ents:
            try:
                n = int(char_diff_sec[pri_ent])
            except KeyError:
                n = 65
            secondary_ents = [entity.strip() for entity in section[pri_ent].split(",")]
            sec_ents_metadata = []
            for entity in secondary_ents:
                sec_ents_metadata.append(entity + "_start")
                sec_ents_metadata.append(entity + "_end")
                sec_ents_metadata.append(entity + "_conf_score")
                sec_ents_metadata.append(entity + "_additionalNotes")
                sec_ents_metadata.append(entity + "_semantic_match")
            info_attr = [pri_ent, "start", "sent_id", "end", "conf_score",
                        "additionalNotes", "semantic_match"] + secondary_ents + sec_ents_metadata
            linked_df = pd.DataFrame(columns=info_attr)
            relevant_ents = ent_df[ent_df["entity_label"].isin([pri_ent] + secondary_ents)]
            for name, group in relevant_ents.groupby("sent_id"):
                sentence_linked_df = link_sentence_ents(info_attr, pri_ent, secondary_ents,
                                                        group.sort_values(by="start").reset_index(drop=True), n)
                linked_df = pd.concat([linked_df, sentence_linked_df]).reset_index(drop=True)
            filtered_linked_df = filter_df(linked_df, pri_ent, secondary_ents)
            result_list_of_df[pri_ent] = filtered_linked_df
        return result_list_of_df
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e