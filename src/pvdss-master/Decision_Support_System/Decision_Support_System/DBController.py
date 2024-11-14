from django.conf import settings
import logging, sys
import cx_Oracle as db
from sqlalchemy import create_engine, types
import time
import numpy as np
# db.init_oracle_client(lib_dir='/Users/tanuj/Downloads/instantclient_19_8')

logger = logging.getLogger(__name__)
default_error_message = "Inside DBController.py - An error occurred on line {}. Exception type: {}, Exception: {} " \
                        "occurred while processing."

def get_connection_string(connection):
    details = settings.DATABASES[connection]
    return details

class DBController:
    """
    DBController class based on cx_Oracle as django.db.backends.oracle
    lacks controlled write operations.

    """
    def __init__(self, connection):
        details = get_connection_string(connection)
        self.user = details['USER']
        self.password = details['PASSWORD']
        self.host = details['HOST']
        self.port = details['PORT']
        self.name = details['NAME']

    def connect(self):
        self.conn = db.connect(user=self.user, password=self.password, dsn=self.dsn())

    def dsn(self):
        return db.makedsn(host=self.host, port=self.port, service_name=self.name)

    def engine_str(self):
        return 'oracle+cx_oracle://' + self.user + ':' + self.password + '@%s' % self.dsn()

    def create_write_table(self, df, table, index=False, operation='append', chunksize=100000):
        """write_table function in DBController class
        writing data to oracle table

        Args:
            df (_type_): DataFrame
            table (_type_): string
            index (bool, optional): Bool Defaults to False.
            operation (str, optional):string -> 'fail'/'append'/'replace'. Defaults to 'replace'.
            chunksize (int, optional): number. Defaults to 100000.

        Raises:
            e: _description_
        """
        engine = create_engine(self.engine_str())
        try:
            en_con=engine.connect()
            try:
                dtyp = {c: types.VARCHAR(int(np.nan_to_num(df.head(0)[c].str.len().max())) + 10000) for c in
                        df.columns[df.head(0).dtypes == 'object'].tolist()}
                df.head(0).to_sql(table.lower(), en_con, index=index, if_exists='fail', chunksize=chunksize,
                                  dtype=dtyp)
            except:
                pass

            dtyp = {c: types.VARCHAR(int(np.nan_to_num(df[c].str.len().max())) + 10000) for c in
                    df.columns[df.dtypes == 'object'].tolist()}
            df.to_sql(table.lower(), en_con, index=index, if_exists=operation, chunksize=chunksize,
                      dtype=dtyp)
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e
        finally:
            en_con.close()
            engine.dispose()

    def close(self):
        try:
            """closes the connection created"""
            self.conn.close()
        except Exception as e:
            logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    def call_procedure(self, proc_name, parameters):
        try:
            self.connect()
            cur = self.conn.cursor()
            cur.callproc(proc_name, parameters)
            cur.close()
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e
        finally:
            self.close()