package unit.com.rxlogix

import com.rxlogix.AdHocAlertService
import com.rxlogix.AlertService
import com.rxlogix.Constants
import com.rxlogix.DashboardService
import com.rxlogix.ProductBasedSecurityService
import com.rxlogix.UserDashboardCounts
import com.rxlogix.UserService
import com.rxlogix.ValidatedSignalService
import com.rxlogix.action.ActionService
import com.rxlogix.attachments.Attachment
import com.rxlogix.cache.CacheService
import com.rxlogix.config.*
import com.rxlogix.dto.TriggeredReviewDTO
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.SignalStatusHistory
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.Topic
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import grails.test.hibernate.HibernateSpec
import grails.testing.services.ServiceUnitTest
import groovy.json.JsonBuilder
import org.eclipse.jetty.websocket.common.SessionFactory
import org.grails.datastore.gorm.bootstrap.support.InstanceFactoryBean
import org.hibernate.Session
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Ignore
import spock.lang.Unroll

import javax.sql.DataSource
import javax.transaction.TransactionManager

class DashboardServiceSpec extends HibernateSpec implements ServiceUnitTest<DashboardService> {

    User user
    Group wfGroup
    AdHocAlert adHocAlertObj
    def attrMapObj
    Session session
    Action action
    Disposition disposition
    Priority priority
    UserService userService
    SingleCaseAlert alert
    AggregateCaseAlert aggAlert
    Disposition dispositionObj2
    ValidatedSignal validatedSignal
    SignalStatusHistory signalStatusHistory
    Disposition defaultDisposition
    Disposition autoRouteDisposition
    Configuration alertConfiguration
    ExecutedConfiguration executedConfiguration
    ExecutedConfiguration executedConfigurationNew
    Topic topic
    PVSState workFlowState
    TriggeredReviewDTO triggeredReviewDTO
    UserDashboardCounts userDashboardCounts
    ExecutedEvdasConfiguration executedEvdasConfiguration

    List<Class> getDomainClasses() { [Group, Topic, Configuration, SingleCaseAlert, Disposition, AggregateCaseAlert, Action, User, Priority,
                                      Attachment, ExecutedConfiguration, AdHocAlert, Disposition, SignalStatusHistory, ValidatedSignal, PVSState, UserDashboardCounts , ExecutedEvdasConfiguration] }

    static doWithSpring = {
        dataSource(InstanceFactoryBean, [:] as DataSource, DataSource)
    }

    def setup() {

        service.sessionFactory = sessionFactory

        priority = new Priority([displayName: "mockPriority", value: "mockPriority", display: true, defaultPriority: true, reviewPeriod: 1])
        priority.save(failOnError: true)

        disposition = new Disposition(value: "ValidatedSignal", displayName: "ValidatedSignal", validatedConfirmed: true,
                reviewCompleted: true, abbreviation: "vs")
        disposition.save(failOnError: true)
        dispositionObj2 = new Disposition(value: "ValidatedSignal2", displayName: "ValidatedSignal2", validatedConfirmed: true,
                reviewCompleted: true, abbreviation: "vs")
        dispositionObj2.save(failOnError: true)

        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

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

        user = new User(id: '1', username: 'test_user1', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name1' }
        user.metaClass.getEmail = { 'fake.email1@fake.com' }
        user.save(flush:true)

        action = new Action([details: "no comments", createdDate: new Date(),
                             dueDate: new Date(), assignedTo: user, actionStatus: "Closed"])

        adHocAlertObj = new AdHocAlert(
                productSelection: "something",
                detectedDate: new DateTime(2015, 12, 15, 0, 0, 0).toDate(),
                detectedBy: "Company",
                initialDataSource: "test",
                name: "Test Name2",
                alertVersion: 0,
                priority: priority,
                topic: "rash",
                disposition: disposition,
                assignedTo: user,
                createdBy: 'createdBy',
                modifiedBy: 'modifiedBy',
                owner : user
        )
        adHocAlertObj.save(failOnError: true,flush:true)

        alertConfiguration = new Configuration(
                executing: false,
                priority: priority,
                alertTriggerCases: "11",
                alertTriggerDays: "11",
                assignedTo: user,
                createdBy: 'createdBy',
                modifiedBy: 'modifiedBy',
                name : 'Test_2',
                owner : user,
                productSelection: 'something'
        )
        alertConfiguration.save(flush:true)

        signalStatusHistory = new SignalStatusHistory(dateCreated: new Date(), statusComment: "Test Status Comment", signalStatus: "Signal Status",
                dispositionUpdated: true,performedBy: "Test User",id:1)
        signalStatusHistory.save(flush:true)

        validatedSignal = new ValidatedSignal(
                name: "test_name",
                products: "test_products",
                endDate: new Date(),
                assignedTo: user,
                assignmentType: 'USER',
                modifiedBy: user.username,
                priority: priority,
                disposition: defaultDisposition,
                createdBy: user.username,
                startDate: new Date(),
                id:1,
                genericComment: "Test notes",
                workflowGroup: wfGroup
        )
        validatedSignal.addToSignalStatusHistories(signalStatusHistory)
        validatedSignal.save(flush:true)

        workFlowState = new PVSState(id:1L,value: "10",display: true,finalState: false,displayName: "display")
        workFlowState.save(flush:true)

        topic = new Topic(name: "topic1",products: "paracetamol",disposition: defaultDisposition, createdBy: 'createdBy', modifiedBy: 'modifiedBy',
                workflowState: workFlowState, startDate: new Date(),endDate: new Date(), assignedTo: user,priority:priority)
        topic.save(flush:true)

        executedConfiguration = new ExecutedConfiguration(name: "test",
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(),
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: true, isEnabled: true,isLatest: true,adhocRun: false,
                dateRangeType: DateRangeTypeCaseEnum.CASE_LOCKED_DATE,
                productSelection: "['testproduct2']", eventSelection: "['rash']", studySelection: "['test']",
                configSelectedTimeZone: "UTC",
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                limitPrimaryPath: true,
                includeMedicallyConfirmedCases: true,
                excludeFollowUp: false, includeLockedVersion: true,
                adjustPerScheduleFrequency: true,
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,
                type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                removedUsers: "100",
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10,configId:alertConfiguration?.id)

        executedConfiguration.save(failOnError: true)


        executedConfigurationNew = new ExecutedConfiguration(name: "test",
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(),
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: true, isEnabled: true,isLatest: true,adhocRun: false,
                dateRangeType: DateRangeTypeCaseEnum.CASE_LOCKED_DATE,
                productSelection: "['testproduct2']", eventSelection: "['rash']", studySelection: "['test']",
                configSelectedTimeZone: "UTC",
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                limitPrimaryPath: true,
                includeMedicallyConfirmedCases: true,
                excludeFollowUp: false, includeLockedVersion: true,
                adjustPerScheduleFrequency: true,
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,
                type: Constants.AlertConfigType.SINGLE_CASE_ALERT,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10,configId:alertConfiguration.id)

        executedConfigurationNew.save(failOnError: true)

        attrMapObj = ['masterFollowupDate_5' : new Date(),
                      'masterRptTypeId_3'    : "test type",
                      'masterInitReptDate_4' : new Date(),
                      'masterFollowupDate_5' : new Date(),
                      'reportersHcpFlag_2'   : "true",
                      'masterProdTypeList_6' : "test",
                      'masterPrefTermAll_7'  : "test",
                      'assessOutcome'        : "Death",
                      'assessListedness_9'   : "test",
                      'assessAgentSuspect_10': "test"]

        alert = new SingleCaseAlert(id: 1L,
                productSelection: "something",
                detectedDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                name: "Test Name",
                caseNumber: "1S01",
                caseVersion: 1,
                alertConfiguration: alertConfiguration,
                executedAlertConfiguration: executedConfiguration,
                reviewDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                priority: priority,
                disposition: disposition,
                assignedTo: user,
                productName: "Test Product A",
                pt: "Rash",
                createdBy: 'createdBy',
                modifiedBy: 'modifiedBy',
                dateCreated: executedConfiguration.dateCreated,
                lastUpdated: executedConfiguration.dateCreated,
                productFamily: "Test Product A",
                isNew: true,
                productId: 3982,
                followUpExists: true,
                attributesMap: attrMapObj,
                malfunction: "true",
                comboFlag: "true")
        alert.save(failOnError: true)

        aggAlert = new AggregateCaseAlert(
                alertConfiguration: alertConfiguration,
                executedAlertConfiguration: executedConfiguration,
                name: executedConfiguration.name,
                priority: priority,
                disposition: disposition,
                assignedTo: user,
                detectedDate: executedConfiguration.dateCreated,
                productName: "Test Product A",
                productId: 12321312,
                soc: "BODY_SYS1",
                pt: 'Rash',
                ptCode: 1421,
                hlt: 'TEST',
                hglt: 'TEST',
                llt: "INC_TERM2",
                newStudyCount: 1,
                cumStudyCount: 1,
                newSponCount: 1,
                cumSponCount: 1,
                newSeriousCount: 1,
                cumSeriousCount: 1,
                newFatalCount: 1,
                cumFatalCount: 1,
                prrValue: "1",
                prrLCI: "1",
                prrUCI: "1",
                prrStr: "1",
                prrStrLCI: "1",
                prrStrUCI: "1",
                prrMh: "1",
                rorValue: "1",
                rorLCI: "1",
                rorUCI: "1",
                rorStr: "1",
                rorStrLCI: "1",
                rorStrUCI: "1",
                rorMh: "1",
                pecImpHigh: "1",
                pecImpLow: "1",
                createdBy: 'createdBy',
                modifiedBy: 'modifiedBy',
                dateCreated: executedConfiguration.dateCreated,
                lastUpdated: executedConfiguration.dateCreated,
                eb05: new Double(1),
                eb95: new Double(1),
                ebgm: new Double(2),
                dueDate: new Date(),
                periodStartDate: new Date(),
                periodEndDate: new Date(),
                adhocRun: false,
                positiveRechallenge: "true",
                positiveDechallenge: "false",
                listed: "false",
                pregenency: "false",
                related: "false",
                flagged: false,
                format: "test",
                eb05Str: "male:0.1, female:0.2, unknown:0.3",
                eb95Str: "male:0.1, female:0.2, unknown:0.3",
                ebgmStr: "male:0.1, female:0.2, unknown:0.3"
        )
        aggAlert.save(failOnError: true)

        userService = Mock(UserService)
        Preference preference = new Preference()
        preference.timeZone = "UTC"
        userService.getCurrentUserPreference() >> preference
        userService.getUser() >> user
        userService.getUserFromCacheByUsername(_) >> user
        service.userService = userService

        triggeredReviewDTO =
                new TriggeredReviewDTO(userId: 1L,reviewedList: [disposition,dispositionObj2] , isProductSecurity: false,
                        requiresReviewedList: [disposition,dispositionObj2])

        AlertService mockAlertService = Mock(AlertService)
        mockAlertService.getDomainCountMap(_) >> {
            return [alertTotalCountList : 10 , alertNewCountList : 15]
        }
        UserDashboardCounts userDashboardCounts  = new UserDashboardCounts(userId: user.id)
        userDashboardCounts.save(flush:true)
        service.alertService = mockAlertService
        executedEvdasConfiguration = new ExecutedEvdasConfiguration(name: "test",
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(),
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: true, isEnabled: true,
                dateRangeType: DateRangeTypeCaseEnum.CASE_LOCKED_DATE,
                productSelection: "['testproduct2']", eventSelection: "['rash']", studySelection: "['test']",
                configSelectedTimeZone: "UTC",
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                limitPrimaryPath: true,
                includeMedicallyConfirmedCases: true,
                excludeFollowUp: false, includeLockedVersion: true,
                adjustPerScheduleFrequency: true,
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,configId: 1,
                removedUsers: "100",
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10)
        executedEvdasConfiguration.save(failOnError: true)

        service.transactionManager = getTransactionManager()

    }

    def cleanup() {
    }

    void "test generateProductsByStatusChart"(){
        when:
        Map result = service.generateProductsByStatusChart("Constants.AlertConfigType.SINGLE_CASE_ALERT")

        then:
        result.size() == 2
        result.productList == []
        result.productState == []
    }

    void "test alertByDueDate"(){
        when:
        List<List<Map>> result = service.alertByDueDate()

        then:
        result[0].name == "Due Date in Future"
        result[1].name == "Due Today"
        result[2].name == "Passed Due Date"
    }

    void "test createDueDateCountDTO"(){
        when:
        def result = service.createDueDateCountDTO()

        then:
        result.userId == user.id
        result.workflowGroupId == user.workflowGroup.id
    }

    void "test saveDashboardConfig"(){
        when:
        def result = service.saveDashboardConfiguration("dash")

        then:
        result == user
        result.preference.dashboardConfig == "dash"
    }

    void "test dashboardCounts"(){
        setup:
        AdHocAlertService mockAdhocAlertService = Mock(AdHocAlertService)
        mockAdhocAlertService.getAssignedAdhocAlertList(user).size() >> { return null }
        service.adHocAlertService = mockAdhocAlertService

        ActionService mockActionService = Mock(ActionService)
        mockActionService.getActionDashboardCount(user) >> { return 10 }
        service.actionService = mockActionService

        ValidatedSignalService mockValidatedSignalService = Mock(ValidatedSignalService)
        mockValidatedSignalService.getAssignedSignalList().size() >> { return null }
        service.validatedSignalService = mockValidatedSignalService

        service.metaClass.getAlertCounts = { ['test': 1] }
        when:
        Map alertMap = service.dashboardCounts()

        then:
        alertMap.adhoc == null
        alertMap.actionItems == 10
        alertMap.signals == null
    }

    void "test getAlertCounts"(){
        when:
        Map result = service.getAlertCounts([:])

        then:
        result.size() == 3
        result.single == 0
        result.aggregate == 0
        result.evdas == 0
    }

    void "test generateReviewByStateChart"(){
        setup:
        userService.getCurrentUserId() >> {user.id}
        service.alertService.mergeCountMaps(_,_) >> [key : disposition.id , value : 10]


        when:
        List result = service.generateReviewByStateChart("Constants.AlertConfigType.AGGREGATE_CASE_ALERT")

        then:
        result.size()==1
    }

    void "test getFirstXExecutedConfigurationList"(){
        when:
        Map result = service.getFirstXExecutedConfigurationList(["test"], [aggAlert.disposition.id.toString()] , [])

        then:
        result.size() == 0
    }

    void "test generateTriggeredView"(){
        setup:
        AlertService alertService = Mock(AlertService)
        alertService.userService.getUserFromCacheByUsername(_) >> user
        alertService.isProductSecurity() >> { return true }
        service.alertService = alertService

        ProductBasedSecurityService mockProductBasedSecurityService = Mock(ProductBasedSecurityService)
        mockProductBasedSecurityService.allAllowedProductForUser(_) >> ['Product A','Product B']
        alertService.productBasedSecurityService = mockProductBasedSecurityService

        CacheService mockCacheService = Mock(CacheService)
        mockCacheService.getProductsUsedSet() >> ['Product A','Product B']
        mockCacheService.getDispositionByReviewCompleted() >> [disposition]
        mockCacheService.getNotReviewCompletedDisposition() >> [dispositionObj2]
        service.cacheService = mockCacheService

        when:
        def result = service.generateTriggeredReview(1L)

        then:
        result.isProductSecurity
        result.userId == 1
        result.reviewedList == [disposition.id]
        result.requiresReviewedList == [dispositionObj2.id as String]
    }

    void "test generateEvdasReviewMap"(){
        setup:
        TriggeredReviewDTO triggeredReviewDTO1 = new TriggeredReviewDTO()

        when:
        Map result = service.generateEvdasReviewMap(executedEvdasConfiguration , triggeredReviewDTO1)

        then:
        result.name == executedEvdasConfiguration.name
    }

    void "test getSCAAndAggCounts"(){
        setup:
        Map alertMap = [:]

        Map caseCount = [:]
        caseCount.put(disposition.id as String , 10)
        Map pECount = [:]
        pECount.put(disposition.id as String , 10)


        UserDashboardCounts userDashboardCounts1 = UserDashboardCounts.findByUserId(user.id)
        userDashboardCounts1?.userDispCaseCounts = caseCount ? new JsonBuilder(caseCount).toPrettyString() :null
        userDashboardCounts1?.userDispPECounts = pECount ? new JsonBuilder(pECount).toPrettyString() : null
        userDashboardCounts1?.save(flush:true)

        when:
        service.getSCAAndAggCounts(user , alertMap)

        then:
        alertMap.single == 10
        alertMap.aggregate == 10

    }

    void "test deleteTriggeredAlert"(){
        setup:
        service.userService = [currentUserId:{ -> 200l }]
        when:
        service.deleteTriggeredAlert(executedConfiguration.id , Constants.AlertType.INDIVIDUAL_CASE_SERIES)
        then:
        executedConfiguration.removedUsers == "100,200"

    }
    void "test deleteTriggeredAlert for evdas"(){
        setup:
        service.userService = [currentUserId:{ -> 200l }]
        when:
        service.deleteTriggeredAlert(executedEvdasConfiguration.id , Constants.AlertType.EVDAS_ADHOC)
        then:
        executedEvdasConfiguration.removedUsers == "100,200"

    }



}