from django.db import connections
import pandas as pd
import sys
import logging
from Decision_Support_System.DBController import DBController
from django.conf import settings
default_error_message = "Inside db_extraction.py - An error occurred on line {}. Exception type: {}, Exception: {} " \
                                "occurred while parallel processing."

class DBConnection:
    """
    Class for database connection
    read_table - returns a dataFrame from query
    execute_query - executes a data manipulation query
    close - used to close the connection created
    execute_count - executes the query for selection and returns count of records.Decision_Support_System.DBController import DBController
    """
    def __init__(self, connection_name):
        try:
            self.conn = connections[connection_name]
        except Exception as e:
            logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    def read_table(self, query):
        try:
            """executes the query and returns df"""
            return pd.read_sql(query, self.conn)
        except Exception as e:
            logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    def execute_query(self,query):
        try:
            """executes the query for insertion or stored procedure execution"""
            cur = self.conn.cursor()
            cur.execute(query)
            cur.close()
            self.conn.commit()
        except Exception as e:
            logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e
    
    def call_procedure(self, proc, params):
        try:
            """executes stored procedure on database"""
            cur = self.conn.cursor()
            cur.callproc(proc, params)
            cur.close()
        except Exception as e:
            logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    def close(self):
        try:
            """closes the connection created"""
            self.conn.close()
        except Exception as e:
            logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    def execute_count(self, query):
        try:
            """executes the query for selection and returns count of records"""
            cursor = self.conn.cursor()
            cursor.execute(query)
            count = cursor.fetchone()[0]
            cursor.close()
            return count
        except Exception as e:
            logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    def read_db_table_iter(self, query, chunk_size):
        try:
            df_iter = pd.read_sql(query, self.conn, chunksize=chunk_size)
            return df_iter
        except Exception as e:
            logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    def get_db_timestamp(self):
        try:
            _query = "with t as (select to_char(systimestamp,'TZH:TZM') as TIMEZONE from dual) " \
                     "select cast(cast(systimestamp as timestamp with time zone) at time zone TIMEZONE as timestamp) " \
                     "SYSTIMESTAMP from t"
            _df = pd.read_sql_query(_query, self.conn)
            start_timestamp = _df.iloc[0]["SYSTIMESTAMP"]
            return start_timestamp
        except Exception as e:
            logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e


def fetch_prev_data(prev_alert_ids, is_master, database='default', mode='chunks'):
    db = DBController(database)
    
    if is_master == 0:
        query = "SELECT PRODUCT_ID, PRODUCT_NAME, case when smq_code is not null then pt_code||'-'||smq_code else to_char(pt_code) end as PT_CODE,\
            PT, NEW_COUNT as prev_new_count, NEW_FATAL_COUNT as prev_fatal_count, NEW_SERIOUS_COUNT as prev_serious_count, \
            EB05 as prev_eb05_score from AGG_ALERT where exec_configuration_id IN({})".format(prev_alert_ids)
    else:
        query = "SELECT exec_configuration_id as child_execution_id, PRODUCT_ID, PRODUCT_NAME, \
            case when smq_code is not null then pt_code||'-'||smq_code else to_char(pt_code) end as PT_CODE, PT, NEW_COUNT as prev_new_count, \
            NEW_FATAL_COUNT as prev_fatal_count, NEW_SERIOUS_COUNT as prev_serious_count, \
            EB05 as prev_eb05_score from AGG_ALERT where exec_configuration_id IN({})".format(prev_alert_ids)
            
    if mode == 'chunks':
        chunks = pd.read_sql(query, db.engine_str(), chunksize=settings.DB_ROWS)
    else:
        chunks = pd.read_sql(query, db.engine_str())
    
    return chunks
    
def get_next_n_chunks(df_iter, n):
    chunks = []
    row_count = 0
    for df in df_iter:
        n -= 1
        row_count += df.shape[0]
        chunks.append((df))
        if n == 0:
            break
    return chunks, row_count

def trend_write_wrapper(dbc, table, chunk):
    dbc.create_write_table(chunk, table, chunksize=settings.DB_ROWS)

