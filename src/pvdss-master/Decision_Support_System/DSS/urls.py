from django.urls import path
from . import views
from rest_framework_simplejwt import views as jwt_views

urlpatterns = [
    # Token generator
    path('api/token/', jwt_views.TokenObtainPairView.as_view(), name='token_obtain_pair'),
    path('api/token/refresh/', jwt_views.TokenRefreshView.as_view(), name='token_refresh'),
    # score calculation
    path('score/', views.Calculation.as_view(), name='dss-calc'),
    # network display
    path('network/', views.page_output, name='dss-display'),
    # Archived alerts
    path('period/', views.alert_period, name='dss-period'),
    path('record/', views.extract_record, name='dss-record'),
    # extracting activity table records
    path('activities_record/', views.activities_extract_record, name='dss-activity-record'),
    # extracting dataTable records (DSS result Tab)
    path('records/', views.extract_datatable_records, name='table-records'),
    # extracting selected record based on dataTable row click
    path('selected_record/', views.extract_selected_record, name='table-row-record'),
    # manual node submission form
    path('manual_node/', views.ManualSubmission.as_view(), name='manual-node') ,
    # review submission
    path('dss_review/', views.DssReview.as_view(), name='dss-review'),

    # sso endpoints handled
    # path('', views.login, name='dss-login'),

    path('detailed_history/', views.DetailedHistory.as_view(), name='dss-detailed_history'),
    # for application up status
    path('dss/', views.dss_display, name='dss-test'),
]
