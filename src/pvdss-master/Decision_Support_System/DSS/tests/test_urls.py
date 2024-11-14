from django.test import SimpleTestCase
from django.urls import reverse, resolve
from DSS.views import page_output, Calculation, ManualSubmission, alert_period, extract_record, \
    extract_datatable_records, extract_selected_record


class TestUrls(SimpleTestCase):

    def test_display_url_is_resolved(self):
        url = reverse('dss-display')
        self.assertEquals(resolve(url).func, page_output)

    def test_calc_url_is_resolved(self):
        url = reverse('dss-calc')
        self.assertEquals(resolve(url).func.view_class, Calculation)

    def test_alert_period_url_is_resolved(self):
        url = reverse('dss-period')
        self.assertEquals(resolve(url).func, alert_period)

    def test_alert_records_url_is_resolved(self):
        url = reverse('dss-record')
        self.assertEquals(resolve(url).func, extract_record)

    def test_data_table_records_url_is_resolved(self):
        url = reverse('table-records')
        self.assertEquals(resolve(url).func, extract_datatable_records)

    def test_selected_record_url_is_resolved(self):
        url = reverse('table-row-record')
        self.assertEquals(resolve(url).func, extract_selected_record)

    def test_manual_node_form_url_is_resolved(self):
        url = reverse('manual-node')
        self.assertEquals(resolve(url).func.view_class, ManualSubmission)