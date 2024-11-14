from django.db import models
from datetime import datetime


class DSS_ACTIVITY_LOG(models.Model):
    activity_type = models.CharField(max_length=1000)
    suspect_product = models.CharField(max_length=1000)
    event = models.CharField(max_length=1000)
    description = models.CharField(max_length=1000)
    performed_by = models.CharField(max_length=1000)
    previous_value = models.CharField(max_length=1000)
    current_value = models.CharField(max_length=1000)
    time_stamp = models.DateTimeField()
    exec_configuration_id = models.IntegerField()
    alert_configuration_id = models.IntegerField()
    
    class Meta:
        db_table = 'dss_activity_log'
