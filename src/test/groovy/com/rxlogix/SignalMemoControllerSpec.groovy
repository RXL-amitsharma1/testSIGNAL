package com.rxlogix

import com.rxlogix.config.Disposition
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.SignalNotificationMemo
import com.rxlogix.signal.SignalWorkflowRule
import com.rxlogix.signal.SignalWorkflowState
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.util.Holders
import groovy.json.JsonBuilder
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@Mock([Disposition, User, Group, SignalNotificationMemo, AlertAttributesService, SignalMemoReportService])
@TestFor(SignalMemoReportController)
class SignalMemoControllerSpec extends Specification {

    User user
    Group wfGroup
    Disposition disposition
    Disposition defaultDisposition
    Disposition defaultSignalDisposition
    Disposition autoRouteDisposition
    SignalNotificationMemo signalNotificationMemo
    SignalNotificationMemo signalNotificationMemo_first

    def setup() {
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VO")
        disposition.save(flush: true, failOnError: true)
        defaultSignalDisposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VO")
        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

        [defaultDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }

        wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP,
                createdBy: 'createdBy', modifiedBy: 'modifiedBy',
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(flush: true, failOnError: true)

        //Save the  user
        user = new User(id: '1', fullName: 'Test_User', username: 'test_user', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(flush: true)

        signalNotificationMemo = new SignalNotificationMemo(configName: "Config 1", signalSource: "Data mining-Claims database", triggerVariable: "Detected Date", triggerValue: "0",
                emailSubject: "Well-Being", emailBody: "Hope you are doing good", mailUsers: [user], mailGroups: [wfGroup])
        signalNotificationMemo.save(flush: true)

        signalNotificationMemo_first = new SignalNotificationMemo(configName: "Config 2", signalSource: "Data mining-Faers database", triggerVariable: "Detected Date", triggerValue: "0",
                emailSubject: "Well-Being", emailBody: "Hope you are doing good", emailAddress: "def@gmail.com")
        signalNotificationMemo_first.save(flush: true)

        AlertAttributesService mockAlertAttributeService = Mock(AlertAttributesService)
        mockAlertAttributeService.get(_) >> { return ["ER", "CDMT"] }
        controller.alertAttributesService = mockAlertAttributeService

        SignalMemoReportService mockSignalMemoReportService = Mock(SignalMemoReportService)
        mockSignalMemoReportService.getUsersList([user],[wfGroup],"") >> { return [["name": user.fullName, "id": user.id], [["name": wfGroup.name, "id": wfGroup.id]]] }
        controller.signalMemoReportService = mockSignalMemoReportService

    }

    def "test index"(){
        when:
        controller.index()

        then:
        response.status == 200
        view == '/signalMemoReport/index'
    }

    def "test fetchSignalMemoConfig"(){
        when:
        controller.fetchSignalMemoConfig()

        then:
        response.status == 200
        response.contentType == 'application/json;charset=UTF-8'
        JSON.parse(response.text).memoConfigList.size() == 2
        JSON.parse(response.text).memoConfigList[0].id == signalNotificationMemo_first.id
    }

    def "test saveSignalMemoConfig"(){
        given:
        Map params = [configName: "Config 2", signalSource: "Data mining-Faers database", triggerVariable: "Detected Date", triggerValue: "0",
                      emailSubject: "Well-Being", emailBody: "Hope you are doing good", emailAddress: "def@gmail.com"]

        when:
        controller.saveSignalMemoConfig()

        then:
        response.status == 200
        noExceptionThrown()
    }

    def "test saveSignalMemoConfig in case of Exception"(){
        given:
        SignalMemoReportService mockSignalMemoReportService = Mock(SignalMemoReportService)
        mockSignalMemoReportService.saveSignalMemoConfig([:]) >> { throw new Exception() }
        controller.signalMemoReportService = mockSignalMemoReportService

        when:
        controller.saveSignalMemoConfig()

        then:
        response.status == 200
    }

    def "test deleteSignalMemoConfig"(){
        given:
        params.signalMemoId = signalNotificationMemo.id

        when:
        controller.deleteSignalMemoConfig()

        then:
        response.status == 200
        JSON.parse(response.text).status == true
        noExceptionThrown()
    }

    def "test checkIfConfigExistsWithConfigName in case no config found"(){
        given:
        params.configName = "Config 3"

        when:
        controller.checkIfConfigExistsWithConfigName()

        then:
        response.status == 200
        JSON.parse(response.text).status == false
        JSON.parse(response.text).data == null
    }

    def "test checkIfConfigExistsWithConfigName in case config is found"(){
        given:
        params.configName = "Config 2"

        when:
        controller.checkIfConfigExistsWithConfigName()

        then:
        response.status == 200
        JSON.parse(response.text).status == true
        JSON.parse(response.text).data == signalNotificationMemo_first.triggerVariable
    }

}
