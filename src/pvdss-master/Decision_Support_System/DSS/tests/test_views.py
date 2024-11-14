from django.test import Client
import ast
import pandas as pd
import unittest
import math
import json
from unittest import mock
from django.urls import reverse
from difflib import SequenceMatcher
from DSS.score_calculation import parallelize_dataframe, calculate_score
from django.utils.datastructures import MultiValueDictKeyError
from rest_framework.test import APIClient
import os
import filecmp

content_type = "application/json"


class ViewTest(unittest.TestCase):

    def setUp(self):
        self.client = Client()
        self.calc_url = reverse('dss-calc')
        self.display_url = reverse('dss-display')
        self.period_url = reverse('dss-period')
        self.extract_record_url = reverse('dss-record')
        self.table_row_record_url = reverse('table-row-record')
        self.dss_review = reverse('dss-review')
        self.token_api = reverse('token_obtain_pair')
        self.input_df = pd.read_csv('./DSS/tests/input.csv')
        self.output_df = pd.read_csv('./DSS/tests/output.csv')
        self.agg_alert_df = pd.read_csv('./DSS/tests/agg_alert.csv')
        self.archived_agg_alert_df = pd.read_csv('./DSS/tests/archived_agg_alert.csv')
        self.evidence = pd.read_csv('./DSS/tests/pec.csv')
        self.mart_evidence_df = pd.read_csv('./DSS/tests/mart_evidence.csv')

    """ Need to enable in case token authentication is added
    def test_token_generation(self):
        response = self.client.post(self.token_api,
                                    {"username": "signaldev", "password": "signaldev"},
                                    content_type="application/json")
        self.assertEquals(response.status_code, 200)

        response = self.client.post(self.token_api,
                                    {"username": "test", "password": "test"},
                                    content_type="application/json")
        self.assertEquals(response.status_code, 401)
    """

    @mock.patch('DSS.views.DBConnection')
    def test_calculation(self, mock_dbconnection_class):
        # Testing score calculation api
        # Database values are loaded using mocking method and passed from csv

        # mocking database values
        folder_path = os.getcwd()
        folder_path = folder_path + "/DSS/tests/"

        fake_responses = [self.mart_evidence_df]
        mock_dbconnection_class.return_value.read_table.side_effect = fake_responses
        mock_dbconnection_class.return_value.execute_count.return_value = 1
        """
        API call for token generation
        response = self.client.post(self.token_api,
                                    {"username": "signaldev", "password": "signaldev"},
                                    content_type="application/json")
        token = response.data['access']
        # creating separate client with authorization header
        client = APIClient()
        client.credentials(HTTP_AUTHORIZATION="Bearer " + token)
        """

        with open("./DSS/tests/pemap.txt", 'r') as f:
            pemap = ast.literal_eval(f.read())

        # API request
        app_side_request_body = {
            "alert_id": "3",
            "prev_alert_id": 0,
            "smq_flag": False,
            "file_name": folder_path + 'test_run',
            "peMap": pemap
        }

        response = self.client.post(self.calc_url, json.dumps(app_side_request_body), content_type=content_type)
        self.assertEquals(response.status_code, 200)

        # comparing output file with expected file
        assert filecmp.cmp(folder_path + 'dss_output.txt', folder_path + 'test_run.txt')

    @mock.patch('DSS.views.DBConnection')
    def test_page_output(self, mock_dbconnection_class):
        # creating mock data
        selected_df = self.agg_alert_df[self.agg_alert_df['ID'] == 54][['DSS_SCORE', 'ID', 'PERIOD_START_DATE',
                                                                        'PERIOD_END_DATE', 'DSS_CONFIRMATION',
                                                                        'DSS_COMMENTS']]
        selected_df['PERIOD_START_DATE'] = pd.to_datetime(selected_df['PERIOD_START_DATE'])
        selected_df['PERIOD_END_DATE'] = pd.to_datetime(selected_df['PERIOD_END_DATE'])

        mock_dbconnection_class.return_value.read_table.return_value = selected_df

        # creating dummy session to bypass login function - session is flushed at the end
        session = self.client.session
        session['samlUserdata'] = 'yes'
        session['common_name'] = 'test'
        session.save()

        # API call
        response = self.client.get(self.display_url,
                                   {'alert_id': '3', 'archived': 'false', 'row_id': '54'},
                                   content_type=content_type)

        self.assertEquals(response.status_code, 200)
        self.assertEquals(response.templates[0].name, 'DSS/main_page.html')
        self.assertEquals(response.context['comment'], 'Check yes test')
        self.assertEquals(response.context['confirm'], 'yes')
        self.assertEquals(response.context['selected_period'],
                          {'id': '3', 'text': '01-Jan-2016 - 20-Dec-2018', 'title': 'false'})
        session.flush()

    @mock.patch('DSS.views.DBConnection')
    def test_page_output_archived(self, mock_dbconnection_class):
        # creating mock data
        selected_df = self.archived_agg_alert_df[self.archived_agg_alert_df['ID'] == 97][['DSS_SCORE', 'ID',
                                                                                          'PERIOD_START_DATE',
                                                                                          'PERIOD_END_DATE',
                                                                                          'DSS_CONFIRMATION',
                                                                                          'DSS_COMMENTS']]
        selected_df['PERIOD_START_DATE'] = pd.to_datetime(selected_df['PERIOD_START_DATE'])
        selected_df['PERIOD_END_DATE'] = pd.to_datetime(selected_df['PERIOD_END_DATE'])
        mock_dbconnection_class.return_value.read_table.return_value = selected_df

        # creating dummy session to bypass login function - session is flushed at the end
        session = self.client.session
        session['samlUserdata'] = 'yes'
        session['common_name'] = 'test'
        session.save()

        # API call
        response = self.client.get(self.display_url,
                                   {'alert_id': '8', 'archived': 'true', 'row_id': '97'},
                                   content_type=content_type)

        self.assertEquals(response.status_code, 200)
        self.assertEquals(response.templates[0].name, 'DSS/main_page.html')
        self.assertEquals(response.context['comment'], 'teset')
        self.assertEquals(response.context['confirm'], 'yes')
        self.assertEquals(response.context['selected_period'],
                          {'id': '8', 'text': '21-Dec-2009 - 20-Dec-2012', 'title': 'true'})

        response = self.client.get(self.display_url, {'alert_id': '9'}, content_type=content_type)
        self.assertEquals(response.status_code, 404)  # page not found
        session.flush()

    @mock.patch('DSS.views.DBConnection')
    def test_alert_period(self, mock_dbconnection_class):
        # Creating mock data
        df1 = self.agg_alert_df[['PERIOD_START_DATE', 'PERIOD_END_DATE',
                                 'EXEC_CONFIGURATION_ID']][self.agg_alert_df['ALERT_CONFIGURATION_ID'] == 3]
        df2 = self.archived_agg_alert_df[['PERIOD_START_DATE', 'PERIOD_END_DATE',
                                          'EXEC_CONFIGURATION_ID']][
            self.archived_agg_alert_df['ALERT_CONFIGURATION_ID'] == 3]
        df1.drop_duplicates(inplace=True)
        df1['PERIOD_START_DATE'] = pd.to_datetime(df1['PERIOD_START_DATE'])
        df1['PERIOD_END_DATE'] = pd.to_datetime(df1['PERIOD_END_DATE'])
        df1['ARCHIVED_FLAG'] = 'false'
        df2.drop_duplicates(inplace=True)
        df2['PERIOD_START_DATE'] = pd.to_datetime(df2['PERIOD_START_DATE'])
        df2['PERIOD_END_DATE'] = pd.to_datetime(df2['PERIOD_END_DATE'])
        df2['ARCHIVED_FLAG'] = 'true'
        appended_df = pd.concat([df1, df2])
        mock_dbconnection_class.return_value.read_table.return_value = appended_df

        # creating dummy session to bypass login function - session is flushed at the end
        session = self.client.session
        session['samlUserdata'] = 'yes'
        session['common_name'] = 'test'
        session.save()

        # testing parameter extraction
        with self.assertRaises(MultiValueDictKeyError):
            self.client.get(self.period_url, {'alert_id': '9'}, content_type=content_type)
            self.client.get(self.period_url, {'archived': 'true'}, content_type=content_type)
            self.client.get(self.period_url, {}, content_type=content_type)

        # API call
        response = self.client.get(self.period_url,
                                   {'alert_id': 3, 'archived': 'false', 'p': 'PARACETAMOL', 'e': 'FEVER'},
                                   content_type=content_type)
        self.assertEquals(response.status_code, 200)
        self.assertEquals(response.data['results'], [{'id': 9, 'text': '01-Jan-2016 to 20-Dec-2018', 'title': 'true'},
                                                     {'id': 8, 'text': '21-Dec-2011 to 20-Dec-2012', 'title': 'true'}])

        session.flush()

    @mock.patch('DSS.views.DBConnection')
    def test_extract_record(self, mock_dbconnection_class):
        # mocking the data
        prod_name = 'Test Product d'
        test_df = self.agg_alert_df[(self.agg_alert_df['EXEC_CONFIGURATION_ID'] == 3) &
                                    (self.agg_alert_df['PRODUCT_NAME'] == prod_name) &
                                    (self.agg_alert_df['PT'] == 'Arrhythmia')][['DSS_SCORE', 'DSS_COMMENTS',
                                                                                'DSS_CONFIRMATION']]
        mock_dbconnection_class.return_value.read_table.return_value = test_df

        # creating dummy session to bypass login function - session is flushed at the end
        session = self.client.session
        session['samlUserdata'] = 'yes'
        session['common_name'] = 'test'
        session.save()

        # Testing parameter extraction
        with self.assertRaises(MultiValueDictKeyError):
            self.client.get(self.extract_record_url, {'alert_id': '9'}, content_type=content_type)
            self.client.get(self.extract_record_url, {'archived': 'true'}, content_type=content_type)
            self.client.get(self.extract_record_url, {}, content_type=content_type)

        # API call
        response = self.client.get(self.extract_record_url,
                                   {'alert_id': 3, 'archived': 'false', 'p': prod_name, 'e': 'Arrhythmia'},
                                   content_type=content_type)
        self.assertEquals(response.status_code, 200)
        self.assertEquals(response.data['selected_record'],
                          json.loads(self.agg_alert_df[self.agg_alert_df['ID'] == 47]['DSS_SCORE'].values[0]))
        self.assertEquals(response.data['confirm'], 'no')
        self.assertEquals(response.data['comment'], "asd asdjkasjk test'a asd")
        self.assertEquals(response.data['nodes'],
                          [{'id': 'prr', 'label': 'PRR\nYes'}, {'id': 'ebgm', 'label': 'EBGM\nNo'},
                           {'id': 'trend', 'label': 'TREND\nNo'}, {'id': 'rechallenge', 'label': 'RECHALLENGE\nYes'},
                           {'id': 'dechallenge', 'label': 'DECHALLENGE\nYes'},
                           {'id': 'evid_countries', 'label': 'EVIDENCE COUNTRIES\nYes'},
                           {'id': 'evid_case_type', 'label': 'EVID CASE TYPE\nYes'}, {'id': 'dme', 'label': 'DME\nYes'},
                           {'id': 'ime', 'label': 'IME\nYes'}, {'id': 'listedness', 'label': 'LISTEDNESS\nYes'},
                           {'id': 'severity_incr', 'label': 'SEVERITY INCREASE\nYes'},
                           {'id': 'elderly', 'label': 'ELDERLY\nYes'}, {'id': 'children', 'label': 'CHILDREN\nYes'},
                           {'id': 'time_to_onset', 'label': 'TIME TO ONSET\nYes'}, {'id': 'sdr', 'label': 'SDR'},
                           {'id': 'strength', 'label': 'STRENGTH'}, {'id': 'bio_gradient', 'label': 'RE/DECHALLENGE'},
                           {'id': 'consistency', 'label': 'CONSISTENCY'}, {'id': 'specificity', 'label': 'SPECIFICITY'},
                           {'id': 'temporality', 'label': 'TEMPORALITY'},
                           {'id': 'special_population', 'label': 'SPECIAL POPULATION'},
                           {'id': 'other_factors', 'label': 'OTHER FACTORS'},
                           {'id': 'specif_consistency', 'label': 'SPECIF CONSISTENCY'},
                           {'id': 'bh_criteria', 'label': 'BH CRITERIA'},
                           {'id': 'pec_importance', 'label': 'DSS SCORE'}])
        session.flush()

    @mock.patch('DSS.views.DBConnection')
    def test_extract_selected_record(self, mock_dbconnection_class):
        # mocking the data
        test_df = self.agg_alert_df[(self.agg_alert_df['EXEC_CONFIGURATION_ID'] == 3) &
                                    (self.agg_alert_df['PRODUCT_NAME'] == 'Test Product d') &
                                    (self.agg_alert_df['PT'] == 'Arrhythmia')][['DSS_SCORE', 'DSS_COMMENTS',
                                                                                'DSS_CONFIRMATION']]
        mock_dbconnection_class.return_value.read_table.return_value = test_df

        # creating dummy session to bypass login function - session is flushed at the end
        session = self.client.session
        session['samlUserdata'] = 'yes'
        session['common_name'] = 'test'
        session.save()

        # Testing parameter extraction
        with self.assertRaises(MultiValueDictKeyError):
            self.client.get(self.table_row_record_url, {'alert_id': '9'}, content_type=content_type)
            self.client.get(self.table_row_record_url, {'archived': 'true', 'p': 'PARACETAMOL'},
                            content_type=content_type)
            self.client.get(self.table_row_record_url, {}, content_type=content_type)

        # API call
        response = self.client.get(self.table_row_record_url,
                                   {'alert_id': 3, 'archived': 'false', 'p': 'PARACETAMOL', 'e': 'FEVER'},
                                   content_type=content_type)
        self.assertEquals(response.status_code, 200)
        self.assertEquals(response.data['selected_record'],
                          json.loads(self.agg_alert_df[self.agg_alert_df['ID'] == 47]['DSS_SCORE'].values[0]))
        self.assertEquals(response.data['confirm'], 'no')
        self.assertEquals(response.data['comment'], "asd asdjkasjk test'a asd")

        session.flush()

    @mock.patch('DSS.views.DBConnection')
    def test_dss_review(self, mock_db_class):
        # creating dummy session to bypass login function - session is flushed at the end
        session = self.client.session
        session['samlUserdata'] = 'yes'
        session['common_name'] = 'test'
        session.save()

        response = self.client.post(self.dss_review, {'alert_id': '3', 'archived': 'false', 'p': 'PARACETAMOL',
                                                      'e': 'FEVER', 'confirm': 'yes', 'comment': 'test mock',
                                                      'common_name': 'test'},
                                    content_type=content_type)
        self.assertEquals(response.status_code, 200)
        self.assertEquals(response.data['data'], 'success')

        response = self.client.post(self.dss_review,
                                    {'alert_id': '4', 'archived': 'true', 'p': 'PARACETAMOL', 'e': 'FEVER',
                                     'confirm': '', 'comment': '', 'common_name': 'test'},
                                    content_type=content_type)
        self.assertEquals(response.status_code, 200)
        self.assertEquals(response.data['data'], 'success')
        session.flush()


if __name__ == '__main__':
    unittest.main()
