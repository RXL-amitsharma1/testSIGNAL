package unit.com.rxlogix

import com.rxlogix.*
import com.rxlogix.cache.CacheService
import com.rxlogix.config.*
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.config.metadata.SourceTableMaster
import com.rxlogix.controllers.AlertController
import com.rxlogix.dto.AlertDataDTO
import com.rxlogix.enums.*
import com.rxlogix.signal.*
import com.rxlogix.spotfire.SpotfireService
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import groovy.json.JsonBuilder
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Ignore
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import java.text.SimpleDateFormat

@TestFor(SingleCaseAlertController)
@TestMixin(DomainClassUnitTestMixin)
@Mock([SingleCaseAlert, User, ExecutedConfiguration, Disposition, CaseHistory, WorkflowRule,
        CaseHistoryService, Configuration, CaseLineListingTemplate, ReportFieldGroup, SourceTableMaster,
        SourceColumnMaster, ReportField, ReportFieldInfo, ProductGroup, ReportFieldInfoList, AggregateCaseAlert,
        TemplateQuery, ProductGroupService, Group, CacheService, ReportIntegrationService, ExecutedAlertDateRangeInformation,
        ReportExecutorService, ConfigurationService, ValidatedSignalService, ValidatedSignal, Priority, ReportTemplate,
        CRUDService, UserService, AlertService, DateRangeInformation, ViewInstance, ViewInstanceService, ActionConfiguration,
        Justification, WorkflowRuleService, DispositionService, PriorityService, SafetyLeadSecurityService, ActionType,
        SpotfireService, SignalStrategy, UserGroupService, EmailService, ActivityType, Activity, EmailNotificationService,
        ReportFieldService,PvsGlobalTagService])
@ConfineMetaClassChanges([SpringSecurityUtils])
class SingleCaseAlertControllerSpec extends Specification {

    Disposition disposition
    Disposition defaultDisposition
    Disposition autoRouteDisposition
    Configuration alertConfiguration
    Configuration configuration_a
    Configuration configuration_b
    Configuration configuration_agg
    ExecutedConfiguration executedConfiguration
    ExecutedConfiguration executedConfiguration_a
    User user, newUser
    def attrMapObj
    SingleCaseAlert alert
    SingleCaseAlert sca_a
    CaseHistory caseHistoryObj
    Priority priority
    ValidatedSignal validatedSignal
    AlertDateRangeInformation alertDateRangeInformation1
    ViewInstance viewInstance1
    Justification justification1
    ActionType actionType
    ActionConfiguration actionConfiguration
    ActivityType activityType
    Group wfGroup
    AggregateCaseAlert aggAlert

    void setup() {
        activityType = new ActivityType(value: ActivityTypeValue.CaseAdded)
        activityType.save(flush: true, failOnError: true)
        actionConfiguration = new ActionConfiguration(displayName: 'test', value: 'test', description: "desc")
        actionConfiguration.save(flush: true, failOnError: true)
        actionType = new ActionType(description: 'test', displayName: 'test', value: 'test')
        justification1 = new Justification(/*caseAddition:"on",*/ name: "justification1", justification: "changed", feature:
                '{"feature":"feature1"}')
        justification1.save(flush: true, failOnError: true)
        viewInstance1 = new ViewInstance(name: "viewInstance", alertType: "Single Case Alert", user: user, columnSeq: "seq")
        viewInstance1.save(flush: true, failOnError: true)
        alertDateRangeInformation1 = new AlertDateRangeInformation(dateRangeEndAbsolute: new Date() + 4, dateRangeStartAbsolute: new Date(),
                dateRangeEndAbsoluteDelta: 13, dateRangeStartAbsoluteDelta: 10, dateRangeEnum: DateRangeEnum.CUSTOM)
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "vs")
        disposition.save(flush: true, failOnError: true)

        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

        [defaultDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }

        wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP, defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                createdBy: 'createdBy', modifiedBy: 'modifiedBy',
                defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(flush: true)

        user = new User(id: '1', username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.addToGroups(wfGroup);
        user.save(flush: true,failOnError:true)

        newUser = new User(id: '2', username: 'username2', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        newUser.addToGroups(wfGroup)
        newUser.preference.createdBy = "createdBy"
        newUser.preference.modifiedBy = "modifiedBy"
        newUser.preference.locale = new Locale("en")
        newUser.preference.isEmailEnabled = false
        newUser.metaClass.getFullName = { 'Fake Name' }
        newUser.metaClass.getEmail = { 'fake.email@fake.com' }
        newUser.metaClass.isAdmin = { -> false }
        newUser.save(flush: true)

        priority = new Priority(value: "Low", display: true, displayName: "Low", reviewPeriod: 3, priorityOrder: 1)
        priority.save(flush: true, failOnError: true)
        alertConfiguration = new Configuration(
                name: "test",
                owner: user,
                modifiedBy: "signaldev",
                assignedTo: user,
                productSelection: "aspirin",
                executing: false,
                createdBy: "test",
                priority: priority,
                alertTriggerCases: "11",
                alertTriggerDays: "11")


        executedConfiguration = new ExecutedConfiguration(name: "test",
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
                assignedTo: user, configId: 1,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10)
        executedConfiguration.save(flush: true, failOnError: true)


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
                priority: alertConfiguration.priority,
                disposition: disposition,
                assignedTo: user,
                productName: "Test Product A",
                pt: "Rash",
                createdBy: config.assignedTo.username,
                modifiedBy: config.assignedTo.username,
                dateCreated: config.dateCreated,
                lastUpdated: config.dateCreated,
                productFamily: "Test Product A",
                isNew: true,
                productId: 3982,
                assignedToGroup: wfGroup,
                followUpExists: true,
                attributesMap: attrMapObj,
                comboFlag:'combo',
                malfunction: 'mal')
        alert.save(flush: true, failOnError: true)

        aggAlert = new AggregateCaseAlert(
                alertConfiguration: config,
                executedAlertConfiguration: executedConfiguration,
                name: executedConfiguration.name,
                priority: priority,
                disposition: disposition,
                assignedTo: user,
                detectedDate: executedConfiguration.dateCreated,
                productName: "Test Product",
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
                createdBy: config.assignedTo.username,
                modifiedBy: config.assignedTo.username,
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
                ebgmStr: "male:0.1, female:0.2, unknown:0.3",
                alertTags: alert.alertTags
        )
        aggAlert.save(failOnError: true)
        def obj = ["caseNumber" : "16US000684",
                   "caseVersion": "1",
                   "alertId"    : "1",
                   "event"      : "1.Rash",
                   "currentUser": "1"]

        def obj1 = new JsonBuilder(obj)
        def obj0 = ["caseNumber" : "16US000684",
                    "caseVersion": "1",
                    "alertId"    : "1",
                    "event"      : "1.Rash",
                    "currentUser": "1"]
        def obj2 = new JsonBuilder(obj0)
        controller.params.alertDetails = [obj1, obj2].toString()


        def singleCaseAlertServiceSpy = [
                getAlertById               : { return this.alert },
                updateSingleCaseAlertStates: { _1, _2, _3, _4 -> },
                getExecConfigurationById   : { return this.executedConfiguration },
                getActivityByType          : { _ -> }
        ] as SingleCaseAlertService

        controller.singleCaseAlertService = singleCaseAlertServiceSpy

        def userServiceSpy = [
                getUser: { this.user }
        ] as UserService
        controller.userService = userServiceSpy

        caseHistoryObj = new CaseHistory(
                "currentDisposition": alert.disposition,
                "currentAssignedTo": alert.assignedTo,
                "currentPriority": alert.priority,
                "caseNumber": alert.caseNumber,
                "caseVersion": alert.caseVersion,
                "productFamily": alert.productFamily,
                "followUpNumber": alert.followUpNumber,
                "isLatest": true)

        def caseHistoryServiceSpy = [
                saveCaseHistory     : {},
                getLatestCaseHistory: { String caseNumber, String productFamily -> return caseHistoryObj }
        ] as CaseHistoryService
        controller.caseHistoryService = caseHistoryServiceSpy

        def activityServiceSpy = [
                createActivity: { _1, _2, _3, _4, _5, _6, _7, _8, _9, _10 -> }
        ]
        controller.activityService = activityServiceSpy

        assert (SingleCaseAlert.count() == 1)

        validatedSignal = new ValidatedSignal(name: "ValidatedSignal", assignedTo: user, assignmentType: "signalAssignment",
                createdBy: "username", disposition: disposition, modifiedBy: "username", priority: priority, products: "product1", workflowGroup: wfGroup)
        validatedSignal.save(flush: true, failOnError: true)

        DateRangeInformation dateRangeInformation1 = new DateRangeInformation(dateRangeStartAbsoluteDelta: 1, dateRangeEndAbsoluteDelta: 1,
                dateRangeEndAbsolute: new Date() + 1, dateRangeStartAbsolute: new Date())
        configuration_a = new Configuration(
                id: 100001,
                name: 'case_series_config',
                assignedTo: user,
                productSelection: '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}',
                productGroupSelection: '{["name":"product group","id":1]}',
                eventGroupSelection: '{["name":"event group","id":1]}',
                owner: user,
                createdBy: user.username,
                modifiedBy: user.username,
                priority: priority,
                isCaseSeries: true,
                alertCaseSeriesId: 1L,
                alertCaseSeriesName: "case series a"
        )
        configuration_a.save(flush: true)
        configuration_b = new Configuration(
                id: 100004,
                name: 'case_series_config_b',
                assignedTo: user,
                productSelection: "[TestProduct]",
                owner: user,
                createdBy: user.username,
                modifiedBy: user.username,
                priority: priority,
                isCaseSeries: true
        )
        configuration_b.save(flush: true)

        configuration_agg = new Configuration(
                id: 200001,
                name: 'agg_config',
                assignedTo: user,
                productSelection: "[TestProduct]",
                owner: user,
                createdBy: user.username,
                modifiedBy: user.username,
                priority: priority,
                isCaseSeries: true
        )
        ReportFieldGroup fieldGroup = new ReportFieldGroup([name: "Case Information"])
        fieldGroup.save(flush: true, failOnError: true)
        SourceTableMaster argusTableMaster = new SourceTableMaster([tableName: "CASE_MASTER", caseJoinOrder: 1, caseJoinType: "E", tableAlias: "cm", tableType: "C"])
        argusTableMaster.save(flush: true, failOnError: true)
        SourceColumnMaster argusColumnMaster = new SourceColumnMaster([tableName: argusTableMaster, reportItem: "CM_CASE_NUM", columnName: "CASE_NUM", columnType: "V"])
        argusColumnMaster.save(flush: true, failOnError: true)

        ReportField field = new ReportField([dataType: String.class, transform: "test", isEudraField: false, name: "caseNumber", description: "This is the Case number", sourceColumn: argusColumnMaster, fieldGroup: fieldGroup])
        field.save(flush: true, failOnError: true)
        ReportFieldInfo reportFieldInfo = new ReportFieldInfo(reportField: field, argusName: "fakeName")
        ReportFieldInfoList reportFieldInfoList = new ReportFieldInfoList()
        reportFieldInfoList.addToReportFieldInfoList(reportFieldInfo).save(flush: true, failOnError: true)

        CaseLineListingTemplate templateNew = new CaseLineListingTemplate(id: 1L, templateType: TemplateTypeEnum.CASE_LINE, owner: user, name: 'Test template', createdBy: "normalUser", modifiedBy: "normalUser", columnList: reportFieldInfoList)

        def JSONQuery = """{ "all": { "containerGroups": [   
        { "expressions": [  { "index": "0", "field": "masterCaseNum", "op": "EQUALS", "value": "14FR000215" }  ] }  ] } }"""
        TemplateQuery templateQuery = new TemplateQuery(query: 1, template: 1L, report: configuration_a, dateRangeInformationForTemplateQuery: new DateRangeInformation(), createdBy: user.username, modifiedBy: user.username)
        templateQuery.save(flush: true, failOnError: true)
        configuration_agg.addToTemplateQueries(templateQuery)
        configuration_agg.save(flush: true)

        ExecutedAlertDateRangeInformation executedAlertDateRangeInformation = new ExecutedAlertDateRangeInformation(dateRangeStartAbsolute: new Date(),
                dateRangeEndAbsolute: new Date() + 2)
        executedAlertDateRangeInformation.save(validate: false)
        executedConfiguration_a = new ExecutedConfiguration(
                id: 100002,
                name: "case_series_config",
                executedAlertDateRangeInformation: executedAlertDateRangeInformation,
                owner: user,
                scheduleDateJSON: "{}",
                nextRunDate: new Date(),
                description: "test",
                dateCreated: new Date(),
                lastUpdated: new Date(),
                isPublic: true,
                isDeleted: false,
                isEnabled: true,
                dateRangeType: DateRangeTypeCaseEnum.CASE_LOCKED_DATE,
                productSelection: "['TestProduct']",
                eventSelection: "['rash']", studySelection: "['test']",
                configSelectedTimeZone: "UTC",
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                limitPrimaryPath: true,
                includeMedicallyConfirmedCases: true,
                excludeFollowUp: false,
                includeLockedVersion: true,
                adjustPerScheduleFrequency: true,
                createdBy: user.username,
                modifiedBy: user.username,
                assignedTo: user,
                executionStatus: ReportExecutionStatus.COMPLETED,
                numOfExecutions: 10,
                isCaseSeries: true,
                aggExecutionId: 200002,
                aggAlertId: 200003,
                configId: 2,
                aggCountType: "NEW_SER")
        executedConfiguration_a.save(flush: true)

        sca_a = new SingleCaseAlert(
                id: 100003,
                name: 'case_series_config',
                productSelection: "['TestProduct']",
                detectedDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                caseNumber: "1S00001",
                caseVersion: 1,
                alertConfiguration: configuration_a,
                executedAlertConfiguration: executedConfiguration_a,
                reviewDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                priority: configuration_a.priority,
                disposition: disposition,
                assignedTo: user,
                productName: "Test Product A",
                pt: "Rash",
                productId: 894239,
                isNew: true,
                followUpExists: false,
                createdBy: configuration_a.assignedTo.username,
                modifiedBy: configuration_a.assignedTo.username,
                dateCreated: configuration_a.dateCreated,
                lastUpdated: configuration_a.dateCreated,
                productFamily: "Test Product A",
                attributesMap: attrMapObj,
                isCaseSeries: true,
                comboFlag:'combo',
                malfunction: 'mal')
        sca_a.save(flush: true)
        SpringSecurityService springSecurityService = Mock(SpringSecurityService)
        springSecurityService.loggedIn >> {
            return true
        }
        springSecurityService.principal >> {
            return user
        }
        controller.userService.springSecurityService = springSecurityService
        controller.CRUDService.userService.springSecurityService = springSecurityService
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String s -> return true }
        EmailNotificationService emailNotificationService = Mock(EmailNotificationService)
        controller.emailNotificationService = emailNotificationService
        Configuration.metaClass.static.lock = Configuration.&get
        CacheService cacheService = Mock(CacheService)
        cacheService.getUserByUserId(_) >> {
            return user
        }
        cacheService.getGroupByGroupId(_) >> {
            return wfGroup
        }
        controller.cacheService = cacheService
        controller.userService.cacheService = cacheService
    }

    void "Test Change Priority"() {
        setup:
        controller.params.newValue = "Low"
        controller.params.justification = "Test"
        controller.params.executedConfigId = "4112"

        when:
        controller.changePriorityOfAlert()

        then:
        response.status == 200

    }

    void "Test Change Assign User"() {
        setup:
        controller.params.newValue = "2"
        controller.emailService = ["sendNotificationEmail": {}]
        controller.params.executedConfigId = "4112"
        def singleCaseAlertServiceSpy = [
                getAlertById               : { return this.alert },
                updateSingleCaseAlertStates: { _1, _2, _3, _4 -> },
                getExecConfigurationById   : { return this.executedConfiguration },
                getActivityByType          : { _ -> }
        ] as SingleCaseAlertService
        controller.singleCaseAlertService = singleCaseAlertServiceSpy

        when:
        controller.changeAssignedToGroup("4112", "2")

        then:
        response.status == 200
    }

    void "test revertDisposition when disposition reverted successfully"() {
        setup:

        when:
        controller.revertDisposition(1,"Reverted Justification")
        then:
        response.status == 200
    }

    void "test fetchRelatedCaseSeries"() {
        setup:
        def relatedCaseSeriesList = [[alertId     : "5362", name: "testCalpol", description: "-", criteria: "<b>Date Range</b> = 01-Jan-1900 - 31-May-2018", productSelection: "Calpol,Rx Calcium Test Product Name of Rx E2B-R3 Product Family exceeding 70 characters limit for the CSV file",
                                      lastExecuted: "31-May-2018"], [alertId: "6151", name: "testBcCalpol", description: "-", criteria: "<b>Date Range</b> = 01-Jan-1900 - 06-Jun-2018", productSelection: "Calpol", lastExecuted: "06-Jun-2018"]]

        controller.params.caseNumber = "17JP00000000001411"
        controller.singleCaseAlertService = [getRelatedCaseSeries: { caseNumber -> relatedCaseSeriesList }]
        when:
        controller.fetchRelatedCaseSeries()

        then:
        response.getJson()[0]["alertId"] == "5362"
        response.getJson()[0]["name"] == "testCalpol"
        response.getJson()[1]["alertId"] == "6151"
        response.getJson()[1]["name"] == "testBcCalpol"

    }

    void "test fetchRelatedCaseSeries when caseNumber is empty"() {
        setup:
        def relatedCaseSeriesList = []
        controller.params.caseNumber = ""
        controller.singleCaseAlertService = [getRelatedCaseSeries: { caseNumber -> relatedCaseSeriesList }]
        when:
        controller.fetchRelatedCaseSeries()

        then:
        response.getJson() == []

    }

    void "test create"() {
        when:
        controller.create()
        then:
        response.status == 200
        model.configurationInstance.type == 'Single Case Alert'
        model.priorityList[0]["value"] == "Low"
        model.userList[0]["username"] == "username"
        model.action == "create"
    }

    void "test saveCaseSeries"() {
        setup:
        Long executedConfigId = executedConfiguration_a.id
        SingleCaseAlertService mockSignalCaseAlertService = Mock(SingleCaseAlertService)
        mockSignalCaseAlertService.generateTempCaseSeries("new_config_series_name", executedConfigId) >> {
            return true
        }
        controller.singleCaseAlertService = mockSignalCaseAlertService

        when:
        controller.saveCaseSeries(executedConfigId, 'new_config_series_name')

        then:
        response.json.success == true
        executedConfiguration_a.name == 'new_config_series_name'
        !executedConfiguration_a.isCaseSeries
        configuration_a.name == 'new_config_series_name'
        !configuration_a.isCaseSeries
        sca_a.name == 'new_config_series_name'
        !sca_a.isCaseSeries
    }

    void "test saveCaseSeries error scenario"() {
        when:
        controller.saveCaseSeries(123456, 'new_name')

        then:
        notThrown(Exception)
        response.json.success == false
    }

    void "test caseSeriesDetails"() {
        setup:
        def exConfigId = executedConfiguration_a.id
        when:
        controller.caseSeriesDetails(200002, aggAlert.id, 'NEW_SER', 1234, 12345, 'New', 'Ser', 'SingleCaseAlert')
        then:
        response.status == 302
        response.redirectUrl == '/singleCaseAlert/details?callingScreen=review&configId=0&isFaers=&isCaseSeries=true&isCaseSeriesGenerating=false&productName=Test+Product&eventName=Rash'
    }

    void "test fetchAllFieldValues for faers true"() {
        when:
        controller.fetchAllFieldValues(true)

        then:
        response.getJson()[0]["name"] == "productName"

    }

    void "test fetchAllFieldValues for faers false"() {
        when:
        controller.fetchAllFieldValues(false)

        then:
        response.getJson()[0]["name"] == "flags"

    }

    @ConfineMetaClassChanges(AlertController)
    void "test for custom columns qualitative"() {
        setup:
        Boolean cumulative = false
        Long id = 43
        Boolean isFilterRequest = false
        AlertDataDTO alertDataDTO = new AlertDataDTO()
        alertDataDTO.dispositionFilters = ["filter1", "filter2"]
        def alertServiceObj = Mock(AlertService)
        def allowedProducts = ["Calpol01", "Test Product AJ", "ALL-LIC-PROD", "Wonder Product"]
        def resultMapData = [totalCount: 1, totalFilteredCount: 1, resultList: [[alertName       : "test case alert", caseType: "PERIODIC", completenessScore: 1.234, indNumber: "2402609084",
                                                                                 appTypeAndNumber: "DASD_434", compoundingFlag: "Yes", medErrorsPt: "some pts list", patientAge: 32, submitter: "TEVA"]], configId: id, fullCaseList: []]


        alertServiceObj.fetchAllowedProductsForConfiguration() >> allowedProducts
        alertServiceObj.getAlertFilterCountAndList(_) >> resultMapData
        controller.alertService = alertServiceObj
        UserService userService = Mock(UserService)
        SingleCaseAlertService singleCaseAlertService = Mock(SingleCaseAlertService)
        singleCaseAlertService.generateAlertDataDTO(_, _) >> alertDataDTO
        Preference preference = new Preference()
        preference.timeZone = "UTC"
        userService.getCurrentUserPreference() >> preference
        userService.getUser() >> new User(id: 20)
        controller.userService = userService
        controller.singleCaseAlertService = singleCaseAlertService
        AlertController.metaClass.getDispositionSet = { confId, df, df1 ->
            return null
        }

        when:
        controller.listByExecutedConfig(id, isFilterRequest)

        then:
        response.status == 200
        response.json.aaData[0].alertName == "test case alert"
        response.json.aaData[0].caseType == "PERIODIC"
        response.json.aaData[0].completenessScore == 1.234
        response.json.aaData[0].indNumber == "2402609084"
        response.json.aaData[0].submitter == "TEVA"
        response.json.aaData[0].patientAge == 32
        response.json.aaData[0].appTypeAndNumber == "DASD_434"
    }

    void "test index"() {
        when:
        controller.index()
        then:
        response.status == 200
        view == "/singleCaseAlert/index"
    }

    void "test view on success"() {
        when:
        controller.view(configuration_a)
        then:
        response.status == 200
        view == "/singleCaseAlert/view"
        model.configurationInstance == configuration_a
        model.currentUser == user
    }

    void "test view when configuration is null"() {
        when:
        controller.view(null)
        then:
        response.status == 302
        response.redirectedUrl == "/singleCaseAlert/index"
        flash.message == 'default.not.found.message'
    }

    void "test viewExecutedConfig on success"() {
        when:
        controller.viewExecutedConfig(executedConfiguration)
        then:
        response.status == 200
        view == "/singleCaseAlert/view"
        model.isExecuted == true
        model.configurationInstance == executedConfiguration
    }

    void "test viewExecutedConfig when executedConfiguration is null"() {
        when:
        controller.view(null)
        then:
        response.status == 302
        response.redirectedUrl == "/singleCaseAlert/index"
        flash.message == 'default.not.found.message'
    }

    void "test copy on success"() {
        when:
        controller.copy(configuration_a)
        then:
        response.status == 200
        view == "/singleCaseAlert/create"
        model.clone == true
    }

    void "test copy when copy is null"() {
        when:
        controller.copy(null)
        then:
        response.status == 302
        response.redirectedUrl == "/singleCaseAlert/index"
        flash.message == 'default.not.found.message'
    }

    void "test delete on success"() {
        when:
        controller.delete(configuration_a)
        then:
        configuration_a.isDeleted == true
        configuration_a.isEnabled == false
    }

    void "test delete when delete is null"() {
        when:
        controller.delete(null)
        then:
        response.status == 302
        response.redirectedUrl == "/singleCaseAlert/index"
        flash.message == 'default.not.found.message'
    }

    void "test edit on success"() {
        when:
        controller.edit(configuration_a)
        then:
        response.status == 200
        view == "/singleCaseAlert/edit"
        model.configurationInstance == configuration_a
    }

    void "test edit when reportExecutorService returns true"() {
        setup:
        controller.reportExecutorService.currentlyRunning = [configuration_a.id]
        when:
        controller.edit(configuration_a)
        then:
        response.status == 302
        response.redirectedUrl == "/singleCaseAlert/index"
        flash.warn == "app.configuration.running.fail"
    }

    void "test edit when configuration is not editable by current user"() {
        setup:
        UserService userService = Mock(UserService)
        userService.getUser() >> {
            return newUser
        }
        controller.userService = userService
        when:
        controller.edit(configuration_a)
        then:
        response.status == 302
        response.redirectedUrl == "/configuration/index"
        flash.warn == "app.configuration.edit.permission"
    }

    void "test save on success when content type is form"() {
        setup:
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.repeatExecution = "true"
        params.signalId = "${validatedSignal.id}"
        params.onOrAfterDate = sdf.format(new Date())
        params.reviewPeriod = "1"
        params.name = "config1"
        params.owner = user
        params.priority = priority
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.save()
        then:
        response.status == 302
        flash.message == 'default.created.message'
        response.redirectedUrl == "/singleCaseAlert/view/4"
        Configuration.get(4).isEnabled == true
    }

    void "test save on success when content type is all"() {
        setup:
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.repeatExecution = "true"
        params.signalId = "${validatedSignal.id}"
        params.onOrAfterDate = sdf.format(new Date())
        params.reviewPeriod = "1"
        params.name = "config1"
        params.owner = user
        params.priority = priority
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        request.contentType = ALL_CONTENT_TYPE
        when:
        controller.save()
        then:
        response.status == 201
        Configuration.get(4).isEnabled == true
    }

    void "test save on success when repeatExecution is false"() {
        setup:
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.repeatExecution = "false"
        params.signalId = "${validatedSignal.id}"
        params.onOrAfterDate = sdf.format(new Date())
        params.reviewPeriod = "1"
        params.name = "config1"
        params.owner = user
        params.priority = priority
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.save()
        then:
        response.status == 302
        flash.message == 'default.created.message'
        response.redirectedUrl == "/singleCaseAlert/view/4"
        Configuration.get(4).isEnabled == false
    }

    void "test save on success when review period is not given"() {
        setup:
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.repeatExecution = "true"
        params.signalId = "${validatedSignal.id}"
        params.onOrAfterDate = sdf.format(new Date())
        params.name = "config1"
        params.owner = user
        params.priority = priority
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.save()
        then:
        response.status == 302
        flash.message == 'default.created.message'
        response.redirectedUrl == "/singleCaseAlert/view/4"
        Configuration.get(4).isEnabled == true
    }

    void "test save when validation exception occurs"() {
        setup:
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.repeatExecution = "true"
        params.signalId = "${validatedSignal.id}"
        params.onOrAfterDate = sdf.format(new Date())
        params.reviewPeriod = "1"
        params.name = "config1"
        params.owner = user
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.save()
        then:
        response.status == 200
        view == "/singleCaseAlert/create"
    }

    void "test save when Exception occurs"() {
        setup:
        UserService userService = Mock(UserService)
        userService.assignGroupOrAssignTo(_, _) >> {
            throw new Exception()
        }
        controller.userService = userService
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.repeatExecution = true
        params.signalId = "${validatedSignal.id}"
        params.onOrAfterDate = sdf.format(new Date())
        params.reviewPeriod = "1"
        params.name = "config1"
        params.owner = user
        params.priority = priority
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.save()
        then:
        response.status == 200
        flash.error == "app.label.alert.error"
        view == "/singleCaseAlert/create"
    }

    void "test save when parsing of alertTriggerDays fails"() {
        setup:
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.alertTriggerDays = 1
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.repeatExecution = "true"
        params.signalId = "${validatedSignal.id}"
        params.onOrAfterDate = sdf.format(new Date())
        params.reviewPeriod = "1"
        params.name = "config1"
        params.owner = user
        params.priority = priority
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.save()
        then:
        response.status == 200
        flash.error == "app.label.threshold.trigger.days.invalid"
        view == "/singleCaseAlert/create"
    }

    void "test save when parsing of alertTriggerCases fails"() {
        setup:
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.alertTriggerDays = "1"
        params.alertTriggerCases = 1
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.repeatExecution = "true"
        params.signalId = "${validatedSignal.id}"
        params.onOrAfterDate = sdf.format(new Date())
        params.reviewPeriod = "1"
        params.name = "config1"
        params.owner = user
        params.priority = priority
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.save()
        then:
        response.status == 200
        flash.error == "app.label.threshold.trigger.cases.invalid"
        view == "/singleCaseAlert/create"
    }

    void "test run when content type is form"() {
        setup:
        controller.alertService = [renameExecConfig: { Long configId, String configNameOld, String configNameNew, Long ownerId, String alertType ->
            configuration_a.name = "newName"
            configuration_a.save(flush: true, failOnError: true)
        }]
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.id = configuration_a.id
        params.previousAction = 'create'
        params.name = "newName"
        params.excludeNonValidCases = true
        params.repeatExecution = "true"
        params.productGroupSelection='{["name":"product group","id":1]}'
        params.eventGroupSelection='{["name":"event group","id":1]}'
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.reviewPeriod = "1"
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        configuration_a.adhocRun = true
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.run()
        then:
        response.status == 302
        response.redirectedUrl == "/configuration/executionStatus?alertType=SINGLE_CASE_ALERT"
        flash.message == 'app.Configuration.RunningMessage'
    }

    void "test run when content type is not form"() {
        setup:
        controller.alertService = [renameExecConfig: { Long configId, String configNameOld, String configNameNew, Long ownerId, String alertType ->
            configuration_a.name = "newName"
            configuration_a.save(flush: true, failOnError: true)
        }]
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.id = configuration_a.id
        params.previousAction = 'create'
        params.name = "newName"
        params.excludeNonValidCases = true
        params.repeatExecution = "true"
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.reviewPeriod = "1"
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        configuration_a.adhocRun = true
        request.contentType = ALL_CONTENT_TYPE
        when:
        controller.run()
        then:
        response.status == 201
    }

    void "test run when params.id is not given"() {
        setup:
        controller.alertService = [renameExecConfig: { Long configId, String configNameOld, String configNameNew, Long ownerId, String alertType ->
            configuration_a.name = "newName"
            configuration_a.save(flush: true, failOnError: true)
        }]
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.previousAction = 'create'
        params.name = "newName"
        params.excludeNonValidCases = true
        params.repeatExecution = "true"
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.reviewPeriod = "1"
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params.owner = user
        params.priority = priority
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        configuration_a.adhocRun = true
        request.contentType = ALL_CONTENT_TYPE
        when:
        controller.run()
        then:
        response.status == 201
    }

    void "test run when parsing of alertTriggerDays fails"() {
        setup:
        controller.alertService = [renameExecConfig: { Long configId, String configNameOld, String configNameNew, Long ownerId, String alertType ->
            configuration_a.name = "newName"
            configuration_a.save(flush: true, failOnError: true)
        }]
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.previousAction = 'create'
        params.name = "newName"
        params.excludeNonValidCases = true
        params.repeatExecution = "true"
        params.alertTriggerDays = 1
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.reviewPeriod = "1"
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params.owner = user
        params.priority = priority
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        configuration_a.adhocRun = true
        request.contentType = ALL_CONTENT_TYPE
        when:
        controller.run()
        then:
        response.status == 200
        flash.error == "app.label.threshold.trigger.days.invalid"
        view == "/singleCaseAlert/create"
    }

    void "test run when parsing of alertTriggerCases fails"() {
        setup:
        controller.alertService = [renameExecConfig: { Long configId, String configNameOld, String configNameNew, Long ownerId, String alertType ->
            configuration_a.name = "newName"
            configuration_a.save(flush: true, failOnError: true)
        }]
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.previousAction = 'create'
        params.name = "newName"
        params.excludeNonValidCases = true
        params.repeatExecution = "true"
        params.alertTriggerDays = "1"
        params.alertTriggerCases = 1
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.reviewPeriod = "1"
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params.owner = user
        params.priority = priority
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        configuration_a.adhocRun = true
        request.contentType = ALL_CONTENT_TYPE
        when:
        controller.run()
        then:
        response.status == 200
        flash.error == "app.label.threshold.trigger.cases.invalid"
        view == "/singleCaseAlert/create"
    }

    void "test run when params.name is not given"() {
        setup:
        controller.alertService = [renameExecConfig: { Long configId, String configNameOld, String configNameNew, Long ownerId, String alertType ->
            configuration_a.name = "newName"
            configuration_a.save(flush: true, failOnError: true)
        }]
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.previousAction = 'create'
        params.excludeNonValidCases = true
        params.repeatExecution = "true"
        params.alertTriggerDays = "1"
        params.alertTriggerCases = 1
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.reviewPeriod = "1"
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params.owner = user
        params.priority = priority
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        configuration_a.adhocRun = true
        request.contentType = ALL_CONTENT_TYPE
        when:
        controller.run()
        then:
        response.status == 200
        flash.error == "app.label.alert.error"
        view == "/singleCaseAlert/create"
    }

    void "test run when validation exception occurs"() {
        setup:
        controller.alertService = [renameExecConfig: { Long configId, String configNameOld,
                                                       String configNameNew, Long ownerId, String alertType ->
            configuration_a.name = "newName"
            configuration_a.save(flush: true, failOnError: true)
        }]
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.id = configuration_a.id
        params.previousAction = 'create'
        params.name = "newName"
        params.excludeNonValidCases = true
        params.repeatExecution = "true"
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.reviewPeriod = "1"
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        configuration_a.adhocRun = true
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.run()
        then:
        response.status == 200
        view == "/singleCaseAlert/edit"
        flash.error == null
    }

    void "test run when exception occurs"() {
        setup:
        controller.userService = [assignGroupOrAssignTo: { String assignedTo, def domain -> throw new Exception() }]
        controller.alertService = [renameExecConfig: { Long configId, String configNameOld, String configNameNew,
                                                       Long ownerId, String alertType ->
            configuration_a.name = "newName"
            configuration_a.save(flush: true, failOnError: true)
        }]
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.id = configuration_a.id
        params.previousAction = 'create'
        params.name = "newName"
        params.excludeNonValidCases = true
        params.repeatExecution = "true"
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.reviewPeriod = "1"
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        configuration_a.adhocRun = true
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.run()
        then:
        response.status == 200
        view == "/singleCaseAlert/edit"
        flash.error == "app.label.alert.error"
    }

    void "test runOnce when content type is form"() {
        setup:
        params.id = configuration_a.id
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.runOnce()
        then:
        response.status == 302
        flash.message == 'app.Configuration.RunningMessage'
        response.redirectedUrl == "/configuration/executionStatus?alertType=SINGLE_CASE_ALERT"
    }

    void "test runOnce when content type is not form"() {
        setup:
        params.id = configuration_a.id
        request.contentType = ALL_CONTENT_TYPE
        when:
        controller.runOnce()
        then:
        response.status == 201
    }

    void "test runOnce when validation exception occurs"() {
        setup:
        configuration_a.priority = null
        params.id = configuration_a.id
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.runOnce()
        then:
        response.status == 200
        model != null
        view == "/singleCaseAlert/create"
    }

    void "test runOnce when exception occurs"() {
        setup:
        controller.configurationService = [getNextDate: { Configuration c -> throw new Exception() }]
        configuration_a.priority = null
        params.id = configuration_a.id
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.runOnce()
        then:
        response.status == 200
        flash.error == "app.label.alert.error"
        view == "/singleCaseAlert/edit"
    }

    void "test update when content type is form"() {
        setup:
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.id = configuration_a.id
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.productGroupSelection='{["name":"product group","id":1]}'
        params.eventGroupSelection='{["name":"event group","id":1]}'
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        params.assignedToValue = "User_${user.id}"
        params.repeatExecution = "true"
        params.onOrAfterDate = sdf.format(new Date())
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.update()
        then:
        response.status == 302
        response.redirectedUrl == "/singleCaseAlert/view/1"
        flash.message == 'default.updated.message'
    }

    void "test update when content type is not form"() {
        setup:
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.id = configuration_a.id
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        params.assignedToValue = "User_${user.id}"
        params.repeatExecution = "true"
        params.onOrAfterDate = sdf.format(new Date())
        request.contentType = ALL_CONTENT_TYPE
        when:
        controller.update()
        then:
        response.status == 200
    }

    void "test update when Exception occurs"() {
        setup:
        params.id = configuration_a.id
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        params.assignedToValue = "User_${user.id}"
        params.repeatExecution = "true"
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.update()
        then:
        response.status == 200
        flash.error == "app.label.alert.error"
        view == "/singleCaseAlert/edit"
    }

    void "test update when validation error occurs"() {
        setup:
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.id = configuration_a.id
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.repeatExecution = "true"
        params.onOrAfterDate = sdf.format(new Date())
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.update()
        then:
        response.status == 200
        view == "/singleCaseAlert/edit"
        flash.error == null
    }

    void "test update when repeatExecution is false"() {
        setup:
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.id = configuration_a.id
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        params.assignedToValue = "User_${user.id}"
        params.repeatExecution = "false"
        params.onOrAfterDate = sdf.format(new Date())
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.update()
        then:
        response.status == 302
        response.redirectedUrl == "/singleCaseAlert/view/1"
        flash.message == 'default.updated.message'
    }

    void "test update when parsing of alertTriggerDays fails"() {
        setup:
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.id = configuration_a.id
        params.alertTriggerDays = 1
        params.alertTriggerCases = "1"
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        params.assignedToValue = "User_${user.id}"
        params.repeatExecution = "true"
        params.onOrAfterDate = sdf.format(new Date())
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.update()
        then:
        response.status == 200
        view == "/singleCaseAlert/edit"
        flash.error == "app.label.threshold.trigger.days.invalid"
    }

    void "test update when parsing of alertTriggerCases fails"() {
        setup:
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.id = configuration_a.id
        params.alertTriggerDays = "1"
        params.alertTriggerCases = 1
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        params.assignedToValue = "User_${user.id}"
        params.repeatExecution = "true"
        params.onOrAfterDate = sdf.format(new Date())
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.update()
        then:
        response.status == 200
        view == "/singleCaseAlert/edit"
        flash.error == "app.label.threshold.trigger.cases.invalid"
    }

    void "test viewConfig when params.id and from both given"() {
        given:
        params.id = executedConfiguration.id
        params.from = "result"
        when:
        controller.viewConfig()
        then:
        response.status == 302
        response.redirectedUrl == "/configuration/viewExecutedConfig/${executedConfiguration.id}"
    }

    void "test viewConfig when only params.id given"() {
        given:
        params.id = executedConfiguration.id
        when:
        controller.viewConfig()
        then:
        response.status == 302
        response.redirectedUrl == "/configuration/view/${executedConfiguration.id}"
    }

    void "test viewConfig when neither params.id nor from given"() {
        when:
        controller.viewConfig()
        then:
        response.status == 302
        flash.error == "app.configuration.id.null"
        response.redirectedUrl == "/configuration/listAllResults"
    }

    void "test populateModel"() {
        given:
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        params.owner = user
        params.priority = priority
        params.productSelection = '{"3":[{"name":"Test Product A","id":"100004"}]}'
        params.productGroupSelection='{["name":"product group","id":1]}'
        params.eventGroupSelection='{["name":"event group","id":1]}'
        params.spotfireDaterange = 'lastXWeeks'
        params.enableSpotfire = true
        params.spotfireType = "DRUG"
        params.onOrAfterDate
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.onOrAfterDate = sdf.format(new Date())
        params.alertDateRangeInformation = alertDateRangeInformation1
        when:
        controller.populateModel(configuration_a)
        then:
        response.status == 200
        configuration_a.productSelection == '{"3":[{"name":"Test Product A","id":"100004"}]}'
        configuration_a.productGroupSelection == '{["name":"product group","id":1]}'
        configuration_a.eventGroupSelection == '{["name":"event group","id":1]}'
    }

    void "test enable when content type is form"() {
        given:
        params.id = configuration_a.id
        request.contentType = FORM_CONTENT_TYPE
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        when:
        controller.enable()
        then:
        response.status == 302
        response.redirectedUrl == "/singleCaseAlert/view/${configuration_a.id}"
        flash.message == "default.enabled.message"
    }

    void "test enable when content type is not form"() {
        given:
        params.id = configuration_a.id
        request.contentType = ALL_CONTENT_TYPE
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        when:
        controller.enable()
        then:
        response.status == 200
        model != null
        view == null
    }

    void "test enable when when validation exception occurs"() {
        given:
        params.id = configuration_a.id
        request.contentType = ALL_CONTENT_TYPE
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.onOrAfterDate = sdf.format(new Date())
        when:
        controller.enable()
        then:
        response.status == 200
        view == "/singleCaseAlert/edit"
        model != null
    }

    void "test enable when configuration not found"() {
        given:
        params.id = 1000L
        request.contentType = ALL_CONTENT_TYPE
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        when:
        controller.enable()
        then:
        response.status == 404
        model == [:]
        view == null
    }

    void "test disable when content type is form"() {
        given:
        params.id = configuration_a.id
        request.contentType = FORM_CONTENT_TYPE
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        when:
        controller.disable()
        then:
        response.status == 302
        response.redirectedUrl == "/singleCaseAlert/view/${configuration_a.id}"
        flash.message == "default.disabled.message"
    }

    void "test disable when content type is not form"() {
        given:
        params.id = configuration_a.id
        request.contentType = ALL_CONTENT_TYPE
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        when:
        controller.disable()
        then:
        response.status == 200
        model != [:]
    }

    void "test disable when configuration not found"() {
        given:
        params.id = 1000L
        request.contentType = ALL_CONTENT_TYPE
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        when:
        controller.disable()
        then:
        response.status == 404
        model == [:]
        view == null
    }

    void "test disable when validation exception occurs"() {
        given:
        params.id = configuration_a.id
        request.contentType = FORM_CONTENT_TYPE
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.onOrAfterDate = sdf.format(new Date())
        when:
        controller.disable()
        then:
        response.status == 200
        configuration_a.errors != null
        view == "/singleCaseAlert/edit"
    }

    void "test list"() {
        when:
        controller.list()
        then:
        response.status == 200
        JSON.parse(response.text).size() == 2
    }

    void "test previousCaseState"() {
        given:
        controller.caseHistoryService = [getSecondLatestCaseHistory: { String caseNumber, Configuration configuration ->
            return caseHistoryObj
        }, saveCaseHistory                                         : { Map caseHistoryMap ->
            CaseHistory caseHistory = new CaseHistory(configId: configuration_a.id, currentDisposition: disposition,
                    justification: "change needed", caseNumber: "1S01", caseVersion: 1, productFamily: "newFamily",
                    modifiedBy: user.username, followUpNumber: 1, currentPriority: priority)
            caseHistory.save(flush: true, failOnError: true)
        }]
        params.productFamily = configuration_a
        params.followUpNumber = "1"
        when:
        controller.previousCaseState("1S01", 1, 1, configuration_a.id)
        then:
        response.status == 200
        CaseHistory.findByProductFamily("newFamily") != null
    }

    void "test details"() {
        setup:
        controller.userService.cacheService = [getPreferenceByUserId: { Long userId ->
            return user.preference
        },getCurrentUserName: { -> user.name}]
        controller.viewInstanceService = [fetchViewsListAndSelectedViewMap: { String alertType, Long viewId ->
            return [viewdList: [], selectedViewInstance: viewInstance1]
        }]
        controller.viewInstanceService = [fetchSelectedViewInstance: { String alertType, Long viewId ->
            return viewInstance1
        }]
        ValidatedSignalService validatedSignalService = Mock(ValidatedSignalService)
        controller.validatedSignalService = validatedSignalService
        WorkflowRuleService workflowRuleService = Mock(WorkflowRuleService)
        controller.workflowRuleService = workflowRuleService
        CacheService cacheService = Mock(CacheService)
        controller.cacheService = cacheService
        controller.safetyLeadSecurityService.cacheService = cacheService
        UserService userService = Mock(UserService)
        userService.getUser() >> { return user }
        controller.dispositionService.userService = userService
        controller.alertService.actionService = [actionPropertiesJSON: { List<Map> actionTypeList -> }]
        params.configId = configuration_a.id
        params.isFaers = true
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        when:
        controller.details(true)
        then:
        response.status == 200
        view == "/singleCaseAlert/details"
        model.executedConfigId == configuration_a.id
        model.name == "test"
    }

    void "test changeAssignedTo"() {
        setup:
        params.alertDetails = """[{"alertId":"${alert.id}"}]"""
        params.newValue = "User_${newUser.id}"
        params.executedConfigId = "${executedConfiguration.id}"
        controller.cacheService = [getPriorityByValue: { Long id -> priority }, getDispositionByValue: { Long id -> disposition }]
        controller.singleCaseAlertService = [updateSingleCaseAlertStates: { SingleCaseAlert singleCaseAlert, Map map ->
            alert.assignedTo = newUser
        }, getExecConfigurationById                                     : { Long id -> executedConfiguration },
                                             getActivityByType          : { ActivityTypeValue type -> actionType }]
        when:
        controller.changeAssignedTo()
        then:
        response.status == 400
        alert.assignedTo == newUser
    }

    void "test changeAssignedToGroup on success"() {
        setup:
        controller.activityService = [createActivityBulkUpdate      : { ActivityType type, User loggedInUser, String details,
                                                                        String justification, def attrs, String product, String event,
                                                                        User assignedToUser, String caseNumber, Group assignToGroup ->
        },
                                      batchPersistBulkUpdateActivity: { List<Map> activityList -> }]
        params.isArchived = true
        AlertService alertService = Mock(AlertService)
        controller.alertService = alertService
        String selectedId = "[${alert.id}]"
        String assignedToValue = "UserGroup_${wfGroup.id}"
        when:
        controller.changeAssignedToGroup(selectedId, assignedToValue)
        then:
        response.status == 200
        JSON.parse(response.text).message == "app.assignedTo.changed.fail"

    }

    void "test changeAssignedToGroup when exception occurs"() {
        setup:
        String selectedId = "[${alert.id}]"
        String assignedToValue = "UserGroup_${wfGroup.id}"
        when:
        controller.changeAssignedToGroup(selectedId, assignedToValue)
        then:
        response.status == 200
        JSON.parse(response.text).message == "app.assignedTo.changed.fail"
        JSON.parse(response.text).status == false
    }

    void "test listByExecutedConfig"() {
        setup:
        controller.singleCaseAlertService = [generateAlertDataDTO: { Map params, Boolean isFilterRequest ->
            new AlertDataDTO(userId: user.id, dispositionFilters: ["filter"])
        }]
        AlertService alertService = Mock(AlertService)
        alertService.getAlertFilterCountAndList(_) >> {
            return [fullCaseList: [], resultList: [], totalCount: 10, totalFilteredCount: 2]
        }
        controller.alertService = alertService
        params.length = 10
        params.start = 1
        params.callingScreen = Constants.Commons.DASHBOARD
        when:
        controller.listByExecutedConfig(configuration_a.id, true)
        then:
        response.status == 200
        JSON.parse(response.text).recordsFiltered == 2
        JSON.parse(response.text).recordsTotal == 10
        JSON.parse(response.text).configId == configuration_a.id
    }

    void "test listByExecutedConfig when exception occurs"() {
        setup:
        params.length = 10
        params.start = -1
        params.callingScreen = Constants.Commons.DASHBOARD
        when:
        controller.listByExecutedConfig(configuration_a.id, true)
        then:
        response.status == 200
        JSON.parse(response.text).recordsFiltered == 0
        JSON.parse(response.text).recordsTotal == 0
        JSON.parse(response.text).configId == configuration_a.id
    }

    void "test fetchPossibleValues"() {
        setup:
        controller.alertService =[preparePossibleValuesMap:{def domainName, Map<String, List> possibleValuesMap, Long executedConfigId->}]
        controller.reportFieldService =[getSelectableValuesForFields:{->[:]}]
        controller.pvsGlobalTagService =[fetchTagsAndSubtags:{->},fetchTagsfromMart:{List<Map> codeValues->[]},fetchSubTagsFromMart:{List<Map> codeValues->[]}]
        when:
        controller.fetchPossibleValues(executedConfiguration.id)
        then:
        response.status == 200
        JSON.parse(response.text)."isSusar" == ["Yes","No"]
    }

    void "test modelData"(){
        when:
        Map result = controller.modelData(configuration_a,"copy")
        then:
        result.configurationInstance == configuration_a
        result.selectedCaseSeriesText == "case series a"
    }

    void "test fetchCaseSeries"(){
        setup:
        controller.reportIntegrationService = [get:{String url, String path, Map query->
            [status: 200, data: [result: [[ id: "id", text: "caseSeriesName"]],totalCount: 1]]}]
        when:
        controller.fetchCaseSeries("case",1,10)
        then:
        response.status == 200
        JSON.parse(response.text).totalCount == 1
    }
}

