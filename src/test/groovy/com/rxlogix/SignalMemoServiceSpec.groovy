package com.rxlogix

import com.rxlogix.cache.CacheService
import com.rxlogix.config.Disposition
import com.rxlogix.config.Priority
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.SignalNotificationMemo
import com.rxlogix.signal.SignalStatusHistory
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Shared
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@Mock([Disposition, User, Group, SignalNotificationMemo, SignalStatusHistory, Priority, ValidatedSignal, ValidatedSignalService,
        SignalMemoReportService, CacheService])
@TestFor(SignalMemoReportService)
class SignalMemoServiceSpec extends Specification {

    @Shared
    User user
    @Shared
    Group wfGroup
    Priority priority
    Disposition disposition
    Disposition defaultDisposition
    Disposition defaultSignalDisposition
    Disposition autoRouteDisposition
    @Shared
    SignalNotificationMemo signalNotificationMemo
    SignalNotificationMemo signalNotificationMemo_first
    ValidatedSignal validatedSignal
    SignalStatusHistory signalStatusHistory

    def setup() {
        priority = new Priority(value: "High", display: true, displayName: "High", reviewPeriod: 3, priorityOrder: 1)
        priority.save(flush: true)
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
        user = new User(id: 1L, fullName: 'Test_User', username: 'test_user', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
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

        signalStatusHistory = new SignalStatusHistory(dateCreated: new Date(), statusComment: "Test Status Comment", signalStatus: "Signal Status",
                dispositionUpdated: true, performedBy: "Test User", id: 1)
        signalStatusHistory.save()

        validatedSignal = new ValidatedSignal(
                name: "test_name",
                products: "test_products",
                endDate: new Date(),
                detectedDate: new Date() - 2,
                assignedTo: user,
                assignmentType: 'USER',
                modifiedBy: user.username,
                priority: priority,
                disposition: defaultSignalDisposition,
                createdBy: user.username,
                startDate: new Date(),
                id: 1,
                genericComment: "Test notes",
                workflowGroup: wfGroup
        )
        validatedSignal.addToSignalStatusHistories(signalStatusHistory)
        validatedSignal.save(flush: true)

        CacheService mockCacheService = Mock(CacheService)
        mockCacheService.getUserByUserId(user.id) >> { return user }
        mockCacheService.getGroupByGroupId(wfGroup.id) >> { return wfGroup }
        mockCacheService.getAllUsersFromCacheByGroup(wfGroup.id) >> { return [user] }
        service.cacheService = mockCacheService
    }

    void "test compareValuesBetweenSignalAndConfig"() {
        expect:
        service.compareValuesBetweenSignalAndConfig(signalList, configList) == result

        where:
        signalList     | configList | result
        []             | []         | true
        ["ET", "CDMT"] | ["ET"]     | true
        ["ET"]         | ["CDMT"]   | false
    }

    void "test checkIfDateSatisfiesConfig"() {
        setup:
        ValidatedSignalService mockValidatedSignalService = Mock(ValidatedSignalService)
        mockValidatedSignalService.generateSignalHistory(validatedSignal) >> {
            return [['signalStatus': 'Assessment Date', 'dateCreated': new Date().clearTime() - 2], ['signalStatus': 'Date Closed', 'dateCreated': new Date().clearTime() - 2],
                    ['signalStatus': 'Validation Date', 'dateCreated': new Date().clearTime() - 2], ['signalStatus': 'Confirmation Date', 'dateCreated': new Date().clearTime() - 2]]
        }
        service.validatedSignalService = mockValidatedSignalService

        expect:
        service.checkIfDateSatisfiesConfig(days, validatedSignal, triggerVariable) == result

        where:
        days | triggerVariable     | result
        2    | "Assessment Date"   | true
        3    | "Assessment Date"   | false
        2    | "Validation Date"   | true
        3    | "Validation Date"   | false
        2    | "Confirmation Date" | true
        3    | "Confirmation Date" | false
        2    | "Date Closed"       | true
        3    | "Date Closed"       | false
        2    | "Detected Date"     | true
        3    | "Detected Date"     | false
    }

    void "test saveSignalMemoConfig"() {
        setup:
        Map params = [configName  : "Config 2", signalSource: "Data mining-Faers database", triggerVariable: "Detected Date", triggerValue: "0",
                      emailSubject: "Well-Being", emailBody: "Hope you are doing good", emailAddress: "def@gmail.com"]

        when:
        service.saveSignalMemoConfig(params)

        then:
        SignalNotificationMemo.list().size() == 3
    }

    void "test saveSignalMemoConfig in case of Updation of record"() {
        setup:
        Map params = [signalMemoId: signalNotificationMemo.id, configName: "Config 2", signalSource: "Data mining-Faers database", triggerVariable: "Detected Date", triggerValue: "0",
                      emailSubject: "Well-Being", emailBody: "Hope you are doing good", emailAddress: "def@gmail.com"]

        when:
        service.saveSignalMemoConfig(params)

        then:
        SignalNotificationMemo.list().size() == 2
    }

    void "bindAddressToSignalMemo"() {
        setup:
        String mailAddresses = "xyz@gmail.com, User_${user.id}, UserGroup_${wfGroup.id}"

        when:
        service.bindAddressToSignalMemo(mailAddresses, signalNotificationMemo)

        then:
        signalNotificationMemo.emailAddress == 'xyz@gmail.com'
        signalNotificationMemo.mailUsers == [user]
        signalNotificationMemo.mailGroups == [wfGroup]
    }

    void "test getUsersList"() {
        expect:
        service.getUsersList(users, groups, addresses) == result

        where:
        users  | groups    | addresses       | result
        []     | []        | null            | [["name": '', "id": '']]
        [user] | []        | ""              | [["name": user.fullName, "id": 'User_' + user.id], ["name": '', "id": '']]
        []     | [wfGroup] | ""              | [["name": wfGroup.name, "id": 'UserGroup_' + wfGroup.id], ["name": '', "id": '']]
        []     | []        | "xyz@gmail.com" | [["name": "xyz@gmail.com", "id": "xyz@gmail.com"]]
        [user] | [wfGroup] | "xyz@gmail.com" | [["name": user.fullName, "id": 'User_' + user.id], ["name": wfGroup.name, "id": 'UserGroup_' + wfGroup.id], ["name": "xyz@gmail.com", "id": "xyz@gmail.com"]]
    }

    void "test prepareParamsForMemo"() {
        expect:
        service.prepareParamsForMemo(signalNotificationMemo, userList, groupList, emailAddress)['subject'] == result['subject']
        service.prepareParamsForMemo(signalNotificationMemo, userList, groupList, emailAddress)['body'] == result['body']

        where:
        userList | groupList | emailAddress    | result
        []       | []        | "xyz@gmail.com" | [subject: signalNotificationMemo.emailSubject, body: signalNotificationMemo.emailBody, sentTo: ["xyz@gmail.com"]]
        [user]   | []        | "xyz@gmail.com" | [subject: signalNotificationMemo.emailSubject, body: signalNotificationMemo.emailBody, sentTo: ["xyz@gmail.com, fake.email@fake.com"]]
        [user]   | [wfGroup] | "xyz@gmail.com" | [subject: signalNotificationMemo.emailSubject, body: signalNotificationMemo.emailBody, sentTo: ["xyz@gmail.com, fake.email@fake.com"]]
    }

}
