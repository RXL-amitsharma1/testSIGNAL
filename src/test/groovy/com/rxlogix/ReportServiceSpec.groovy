package com.rxlogix

import com.rxlogix.config.DictionaryMapping
import com.rxlogix.config.Disposition
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.signal.ReportHistory
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.User
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.grails.async.factory.SynchronousPromise
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(ReportService)
@Mock([ReportService, DictionaryMapping, UserService, User, ReportHistory, ValidatedSignal, Disposition])
class ReportServiceSpec extends Specification {
    @Shared
    ValidatedSignal validatedSignal
    @Shared
    Disposition disposition
    @Shared
    ReportHistory reportHistory
    @Shared
    User userObj
    def setup() {
        userObj = new User()
        userObj.username = 'username'
        userObj.createdBy = 'createdBy'
        userObj.modifiedBy = 'modifiedBy'
        userObj.preference.createdBy = "createdBy"
        userObj.preference.modifiedBy = "modifiedBy"
        userObj.preference.locale = new Locale("en")
        userObj.preference.isEmailEnabled = false
        userObj.metaClass.getFullName = { "Fake Namer" }
        userObj.metaClass.getEmail = { 'fake.email@fake.com' }
        userObj.metaClass.isAdmin = { true }
        userObj.groups = []
        userObj.save(validate: false)
        reportHistory = new ReportHistory(reportName: "reportName", reportType: 'Memo Reports', dateCreated: new Date(),
                startDate: new Date(), endDate: new Date() + 10, productName: "APREMILAST", dataSource: "pva", updatedBy: userObj)
        reportHistory.save(flush:true, failOnError: true)
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, reviewCompleted: true, abbreviation: "vs")
        disposition.save(flush:true,failOnError:true)
        validatedSignal = new ValidatedSignal(
                name: "test_name",
                products: "test_products",
                endDate: new Date()+10,
                assignmentType: 'USER',
                disposition: disposition,
                productGroupSelection: '[{"name":"testing_AS (3)","id":"3"}]',
                startDate: new Date(),
                detectedDate: new Date(),
                id:1,
                genericComment: "Test notes",
        )
        validatedSignal.save(flush:true,validate:false)
        service.userService = [getUser: { return userObj }]
        service.productBasedSecurityService = [allAllowedProductForUser:{User user->}]
    }

    def cleanup() {
    }

    void "test fetchMatchingSignals for other report"() {
        setup:
        Map productSelectionMap =[productGroupSelectionIds:'3']
        Date startDate = new Date() -3
        Date endDate = new Date() +3
        when:
        List result = service.fetchMatchingSignals(startDate,endDate,productSelectionMap,false)
        then:
        result == [validatedSignal]
    }
    void "test fetchMatchingSignals for pbrer report"() {
        setup:
        Map productSelectionMap =[productGroupSelectionIds:'3']
        Date startDate = new Date() -3
        Date endDate = new Date() +3
        when:
        List result = service.fetchMatchingSignals(startDate,endDate,productSelectionMap,true)
        then:
        result == [validatedSignal]
    }
    void "test getProductSelectionIds"(){
        setup:
        Map productSelectionMap =[productSelection:[1,2]]
        when:
        String result = service.getProductSelectionIds(productSelectionMap)
        then:
        result == "'1','2'"
    }
    void "test getProductGroupSelectionIds"(){
        when:
        String result = service.getProductGroupSelectionIds('1,2')
        then:
        result == "'1','2'"
    }
    void "test generatePBRERReport"(){
        setup:
        Date startDate = new Date() -3
        Date endDate = new Date() +3
        Map productSelectionMap =[productGroupSelectionIds:'3']
        when:
        def result = service.generatePBRERReport(startDate,endDate,productSelectionMap,1L,'pva')
        then:
        result != null
    }
    void "test generateSignalStateReport"(){
        setup:
        Date startDate = new Date() -3
        Date endDate = new Date() +3
        Map productSelectionMap =[productGroupSelectionIds:'3']
        when:
        def result = service.generateSignalStateReport(startDate,endDate,productSelectionMap,1L,'pva')
        then:
        result != null
    }
    void "test generateProductActionsReport"(){
        setup:
        Date startDate = new Date() -3
        Date endDate = new Date() +3
        Map productSelectionMap =[productGroupSelectionIds:'3']
        when:
        def result = service.generateProductActionsReport(startDate,endDate,productSelectionMap,1L,'pva')
        then:
        result != null
    }
    void "test generateSignalSummaryReport"(){
        setup:
        Date startDate = new Date() -3
        Date endDate = new Date() +3
        Map productSelectionMap =[productGroupSelectionIds:'3']
        when:
        def result = service.generateSignalSummaryReport(startDate,endDate,productSelectionMap,1L,'pva')
        then:
        result != null
    }
}