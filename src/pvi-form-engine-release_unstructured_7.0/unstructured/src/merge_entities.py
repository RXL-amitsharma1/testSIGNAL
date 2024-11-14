import numpy as np
import sys
from logs import get_logger
logger = get_logger()
default_error_message = "Inside merge_entities.py - An error occured on line {}. Exception type: {} , Exception: {} "


def process_string(string):
    try:
        string = string.lower().replace(" ", "")
        return string
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e



def compare_string(element1, element2):
    try:
        element1 = process_string(element1)
        element2 = process_string(element2)
        if element1 in element2 or element2 in element1:
            return True
        return False
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


def merge_duplicates(df_extracted):
    try:
        columns_len = len(df_extracted.columns)
        drop_index = []
        for index1 in range(len(df_extracted)):
            for index2 in range(len(df_extracted)):
                if index1 == index2 or index1 in drop_index:
                    continue
                if compare_string(df_extracted.iloc[index1, 0], df_extracted.iloc[index2, 0]):
                    for index3 in range(columns_len):
                        sec_ent_meta = ("_start", "_end", "_conf_score")
                        col_name = str(df_extracted.columns[index3])
                        if not any([s in col_name for s in sec_ent_meta]):
                            if df_extracted.iloc[index1, index3] in ("", np.nan, None):
                                df_extracted.iloc[index1, index3] = df_extracted.iloc[index2, index3]
                            else:
                                try:
                                    try:
                                        conf_1 = float(
                                            df_extracted.iloc[index1, df_extracted.columns.get_loc(col_name + "_conf_score")])
                                    except ValueError as e:
                                        logger.warning(f"Value error raised ,for {index1}")
                                        conf_1 = 0
                                    try:
                                        conf_2 = float(
                                            df_extracted.iloc[index2, df_extracted.columns.get_loc(col_name + "_conf_score")])
                                    except ValueError as e:
                                        logger.warning(f"Value error raised ,for {index2}")
                                        conf_2 = 0
                                    if conf_1 >= conf_2:
                                        choice = index1
                                    else:
                                        choice = index2
                                    df_extracted.iloc[index1, df_extracted.columns.get_loc(col_name)] = df_extracted.iloc[
                                        choice, df_extracted.columns.get_loc(col_name)]
                                    df_extracted.iloc[index1, df_extracted.columns.get_loc(col_name + "_start")] = \
                                        df_extracted.iloc[choice, df_extracted.columns.get_loc(col_name + "_start")]
                                    df_extracted.iloc[index1, df_extracted.columns.get_loc(col_name + "_end")] = \
                                        df_extracted.iloc[choice, df_extracted.columns.get_loc(col_name + "_end")]
                                    df_extracted.iloc[index1, df_extracted.columns.get_loc(col_name + "_conf_score")] = \
                                        df_extracted.iloc[choice, df_extracted.columns.get_loc(col_name + "_conf_score")]
                                except Exception as e:
                                    logger.warning(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
                                    pass
                    drop_index.append(index2)
        df_extracted = df_extracted.drop(drop_index, axis=0).reset_index().drop(["index"], axis=1)

        return df_extracted
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e
