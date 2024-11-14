package com.rxlogix


import com.rxlogix.config.QueryExpressionValue
import com.rxlogix.config.ReportField
import com.rxlogix.config.ReportFieldGroup
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.config.metadata.SourceTableMaster
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Shared
import spock.lang.Specification
@TestFor(QueryService)
class QueryServiceSpec extends Specification {
    public static final String TEST_TZ = "UTC"
    public static final TimeZone ORIGINAL_TZ = TimeZone.getDefault()
    public static final user = "unitTest"
    def JSONQuery = """{ "all": { "containerGroups": [   { "expressions": [  
            { "index": "0", "field": "masterCaseNum", "op": "EQUALS", "value": "14FR000215" }  ] }  ] } }"""

    void "test for getQueryListForBusinessConfiguration()" () {
        setup:
        ReportIntegrationService reportIntegrationService = Mock(ReportIntegrationService)
        service.reportIntegrationService = reportIntegrationService
        Map queryOutput = [queryList:[[id:1 , name:'Date Filter' , owner:'Signaldev' , name:'Date Filter'] , [id:1 , name:'Date Filter' , owner:'Signaldev' , name:'Date Filter']]]
        reportIntegrationService.getQueryList("", 0, 9999, true) >> queryOutput

        when:
        List queryList = service.getQueryListForBusinessConfiguration()

        then:
        queryList.size() == 1
    }
}
