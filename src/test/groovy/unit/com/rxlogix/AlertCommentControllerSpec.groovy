package unit.com.rxlogix

import com.rxlogix.AlertCommentController
import com.rxlogix.AlertCommentService
import com.rxlogix.signal.AlertCommentHistory
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import spock.lang.Ignore
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(AlertCommentController)
@TestMixin(DomainClassUnitTestMixin)
@Ignore
class AlertCommentControllerSpec extends Specification {
    def setup() {
        AlertCommentHistory alertCommentHistory = new AlertCommentHistory(aggAlertId: 1, comments: "comment", alertName: "test alert", period: "2022")
        alertCommentHistory.save(failOnError: true)
    }

    def cleanup() {
    }


    void "test listComments"() {
        given:
        def mockListComment = [id           : 1,
                               comments     : "Test",
                               productName  : "Test Product AJ",
                               eventName    : "Rash",
                               caseNumber   : "1US",
                               productFamily: "Test Product AJ",
                               alertType    : "EVDAS Alert",
                               dateCreated  : "01-Jun-2018",
                               dateUpdated  : "01-Jun-2018",
                               createdBy    : "user",
                               editable     : true]
        def mockAlertCommentService = Mock(AlertCommentService)
        mockAlertCommentService.getComment(_) >> mockListComment
        controller.alertCommentService = mockAlertCommentService
        when:
        controller.listComments()
        then:
        response.getJson()["comments"] == "Test"
        response.getJson()["productName"] == "Test Product AJ"
        response.getJson()["eventName"] == "Rash"
        response.getJson()["productFamily"] == "Test Product AJ"
        response.getJson()["alertType"] == "EVDAS Alert"
    }

    void "test listComments when there is no comment"() {
        given:
        def mockAlertCommentService = Mock(AlertCommentService)
        mockAlertCommentService.getComment(_) >> null
        controller.alertCommentService = mockAlertCommentService
        when:
        controller.listComments()
        then:
        response.status == 200

    }

    void "test saveComment"() {
        given:
        def mockAlertCommentService = Mock(AlertCommentService)
        mockAlertCommentService.createAlertComment(_) >> true
        controller.alertCommentService = mockAlertCommentService
        when:
        controller.saveComment()
        then:
        response.getJson()["success"] == true
    }

    void "test saveComment if not success"() {
        given:
        def mockAlertCommentService = Mock(AlertCommentService)
        mockAlertCommentService.createAlertComment(_) >> false
        controller.alertCommentService = mockAlertCommentService
        when:
        controller.saveComment()
        then:
        response.getJson()["success"] == false
    }

    void "test updateComment"() {
        given:
        def mockAlertCommentService = Mock(AlertCommentService)
        mockAlertCommentService.updateSignalComment(_) >> true
        controller.alertCommentService = mockAlertCommentService
        when:
        controller.updateComment()
        then:
        response.getJson()["success"] == true
    }

    void "test updateComment if not success"() {
        given:
        def mockAlertCommentService = Mock(AlertCommentService)
        mockAlertCommentService.updateSignalComment(_) >> false
        controller.alertCommentService = mockAlertCommentService
        when:
        controller.updateComment()
        then:
        response.getJson()["success"] == false
    }

    void "test listAggCommentsHistory "() {
        when:
        controller.listAggCommentsHistory(1)
        then:
        println response.json
    }

    void "test getBulkCheck"() {
        when:
        controller.getBulkCheck()
        then:
        println "result"
    }

}
