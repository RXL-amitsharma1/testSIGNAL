package unit.com.rxlogix

import com.rxlogix.*
import com.rxlogix.cache.CacheService
import com.rxlogix.config.*
import com.rxlogix.dto.DashboardCountDTO
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.helper.NotificationHelper
import com.rxlogix.mart.MartTags
import com.rxlogix.signal.*
import com.rxlogix.spotfire.SpotfireService
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import org.joda.time.DateTime
import spock.lang.Ignore
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(AggregateCaseAlertController)
@TestMixin(DomainClassUnitTestMixin)
@Mock([AggregateCaseAlert, User, Group, ExecutedConfiguration, Priority, Justification,
        SignalStrategy,Configuration,   ValidatedSignalService,ActionConfiguration,
        AggregateCaseAlertService, ReportIntegrationService, ProductEventHistoryService,
        AlertDateRangeInformation, Disposition, CaseHistory,CaseHistoryService, CRUDService,
        DispositionService, AlertTag, ValidatedSignal, AlertTagService, ActivityService,
        Activity,Category,ReportTemplate,TemplateQuery, AlertService, UserService,
        ReportIntegrationService, ProductGroup, InboxLog, PvsGlobalTagService, ExecutionStatus,
        SubstanceFrequency, EvdasAlertService,ActivityType,ConfigurationService,
        ProductGroupService, CacheService, AggregateCaseAlertService, MartTags,
        Alert,ExecutedAlertDateRangeInformation,EmergingIssue,ReportIntegrationService,
        SpotfireService,DateRangeInformation, ArchivedAggregateCaseAlert,AggregateOnDemandAlert])
class AggregateCaseAlertControllerSpec extends Specification {

    Disposition disposition
    Disposition defaultDisposition
    Disposition autoRouteDisposition
    Configuration alertConfiguration
    Configuration configuration
    ExecutedAlertDateRangeInformation executedAlertDateRangeInformation
    ExecutedConfiguration executedConfiguration
    User user, newUser
    def attrMapObj
    AggregateCaseAlert alert
    Priority priority
    Priority priorityNew
    AlertTag alert_tag
    ExecutionStatus executionStatus
    ValidatedSignal validatedSignal
    DateRangeInformation dateRangeInformation
    AlertDateRangeInformation alertDateRangeInformation
    ReportTemplate reportTemplate
    TemplateQuery templateQuery
    AggregateCaseAlertService aggregateCaseAlertService
    Justification justification
    def caseHistoryService

    void setup() {
        priority = new Priority([displayName: "mockPriority", value: "mockPriority", display: true, defaultPriority: true, reviewPeriod: 1])
        priority.save(failOnError: true)

        priorityNew = new Priority([displayName: "mockPriorityNew", value: "mockPriorityNew", display: true, defaultPriority: true, reviewPeriod: 1])
        priorityNew.save(failOnError: true)

        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VSS")
        disposition.save(flush: true, failOnError: true)

        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

        [defaultDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }
        Group wfGroup = new Group(name: "Default", createdBy: "ujjwal", modifiedBy: "ujjwal", groupType: GroupType.WORKFLOW_GROUP,
                defaultQualiDisposition: disposition,
                defaultQuantDisposition: disposition,
                defaultAdhocDisposition: disposition,
                defaultEvdasDisposition: disposition,
                defaultLitDisposition: disposition,
                defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(flush: true)

        //Prepare the mock user
        user = new User(id: '1', username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.enabled = true
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(failOnError: true)

        //Prepare the mock new User
        newUser = new User(id: '2', username: 'usernameNew', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        newUser.addToGroups(wfGroup)
        newUser.enabled = true
        newUser.preference.createdBy = "createdBy"
        newUser.preference.modifiedBy = "modifiedBy"
        newUser.preference.locale = new Locale("en")
        newUser.preference.isEmailEnabled = false
        newUser.metaClass.getFullName = { 'Fake Name' }
        newUser.metaClass.getEmail = { 'fake.email@fake.com' }
        newUser.save(failOnError: true)


        mockDomain(Priority, [
                [value: "Low", display: true, displayName: "Low", reviewPeriod: 3, priorityOrder: 1]
        ])

        dateRangeInformation = new DateRangeInformation(dateRangeStartAbsoluteDelta: 1, dateRangeEndAbsoluteDelta: 1,
                dateRangeEndAbsolute: new Date() + 1, dateRangeStartAbsolute: new Date())
        alertDateRangeInformation = new AlertDateRangeInformation(dateRangeEndAbsolute: new Date(), dateRangeStartAbsolute: new Date(),
                dateRangeEndAbsoluteDelta: 5, dateRangeStartAbsoluteDelta: 2, dateRangeEnum: DateRangeEnum.CUSTOM)
        Category category1 = new Category(name: "category1")
        category1.save(flush: true, failOnError: true)
        reportTemplate = new ReportTemplate(name: "repTemp1", description: "repDesc1", category: category1,
                owner: user, templateType: TemplateTypeEnum.TEMPLATE_SET, dateCreated: new Date(), lastUpdated: new Date(),
                createdBy: "username", modifiedBy: "username")
        reportTemplate.save(flush: true, failOnError: true)
        def now = new Date()
        def recurrenceJSON = /{"startDateTime":"$now","recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}/
        alertConfiguration = new Configuration(
                executing: false,
                template: reportTemplate,
                alertTriggerCases: 11,
                alertTriggerDays: 11,
                selectedDatasource: "pva",
                name: "test",
                spotfireSettings: "{\"type\":\"VACCINCE\",\"rangeType\":[\"PR_DATE_RANGE\",\"CUMULATIVE\"]}",
                productSelection: "Test Product A",
                type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,
                priority: priority,
                owner: user,
                isEnabled: true,
                alertDateRangeInformation: alertDateRangeInformation,
                nextRunDate: new DateTime(2015, 12, 15, 0, 0, 0).toDate(),
        )
        alertConfiguration.save(flush: true, failOnError: true)

        templateQuery = new TemplateQuery(template: 1L, query: 1L, templateName: "temp1", queryName: "query1",
                dateRangeInformationForTemplateQuery: dateRangeInformation, dateCreated: new Date(), lastUpdated: new Date(),
                createdBy: user.username, modifiedBy: user.username, report: alertConfiguration)
        templateQuery.save(flush: true, failOnError: true)

        configuration = new Configuration(
                executing: false,
                template: reportTemplate,
                alertTriggerCases: 11,
                alertTriggerDays: 11,
                missedCases: false,
                scheduleDateJSON: recurrenceJSON,
                selectedDatasource: "pva",
                name: "test",
                spotfireSettings: "{\"type\":\"VACCINCE\",\"rangeType\":[\"PR_DATE_RANGE\",\"CUMULATIVE\"]}",
                productSelection: "Test Product A",
                type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                createdBy: newUser.username, modifiedBy: newUser.username,
                assignedTo: newUser,
                priority: priority,
                owner: newUser,
                isEnabled: true,
                alertDateRangeInformation: alertDateRangeInformation,
                nextRunDate: new DateTime(2015, 12, 15, 0, 0, 0).toDate(),
        )

        executedAlertDateRangeInformation = new ExecutedAlertDateRangeInformation(dateRangeStartAbsoluteDelta: 2, dateRangeEndAbsoluteDelta: 5, dateRangeEnum: DateRangeEnum.LAST_YEAR,
                dateRangeStartAbsolute: Date.parse('dd-MMM-yyyy', '01-Jan-2014'), dateRangeEndAbsolute: Date.parse('dd-MMM-yyyy', '31-Dec-2014'))

        executedConfiguration = new ExecutedConfiguration(id: configuration.id, name: "test",
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(), executedAlertDateRangeInformation: executedAlertDateRangeInformation,
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: false, isEnabled: true, selectedDatasource: Constants.DataSource.FAERS,
                dateRangeType: DateRangeTypeCaseEnum.CASE_LOCKED_DATE,
                productSelection: "['testproduct2']", eventSelection: "['rash']", studySelection: "['test']",
                configSelectedTimeZone: "UTC",
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                limitPrimaryPath: true, spotfireSettings: "{\"type\":\"VACCINCE\",\"rangeType\":[\"PR_DATE_RANGE\",\"CUMULATIVE\"]}",
                type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                includeMedicallyConfirmedCases: true,
                excludeFollowUp: false, includeLockedVersion: true,
                adjustPerScheduleFrequency: true, groupBySmq: false,
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,configId: config?.id ?: configuration.id,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10 )
        executedConfiguration.save(validate:false)

        attrMapObj = ['masterFollowupDate_5' : new Date(),
                      'masterRptTypeId_3'    : "test type",
                      'masterInitReptDate_4' : new Date(),
                      'reportersHcpFlag_2'   : "true",
                      'masterProdTypeList_6' : "test",
                      'masterPrefTermAll_7'  : "test",
                      'assessOutcome'        : "Death",
                      'assessListedness_9'   : "test",
                      'assessAgentSuspect_10': "test"]

        alert_tag = new AlertTag(name: 'alert_tag', createdBy: user, dateCreated: new Date())
        alert_tag.save(flush: true)

        alert = new AggregateCaseAlert(
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
                alertTags: alert_tag
        )
        alert.save(failOnError: true)

        executionStatus = new ExecutionStatus(configId: alertConfiguration.id,
                startTime: new Date().getTime(),
                endTime: new Date().getTime(), executionStatus: ReportExecutionStatus.COMPLETED,
                reportVersion: 1, message: 'for testing purpose',
                owner: user, name: 'executionStatus', type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                nextRunDate: new Date())
        executionStatus.save(flush: true)

        SubstanceFrequency frequency = new SubstanceFrequency(name: 'Test Product', startDate: Date.parse('dd-MMM-yyyy', '01-Jan-2014'), endDate: Date.parse('dd-MMM-yyyy', '31-Dec-2014'),
                uploadFrequency: 'Yearly', miningFrequency: 'Yearly', frequencyName: "Yearly", alertType: "Aggregate Case Alert")
        frequency.save(flush: true)
        SubstanceFrequency frequency1 = new SubstanceFrequency(name: 'Test Product 1', startDate: Date.parse('dd-MMM-yyyy', '01-Jan-2014'), endDate: Date.parse('dd-MMM-yyyy', '31-Dec-2014'),
                uploadFrequency: 'Yearly', miningFrequency: 'Yearly', frequencyName: "Yearly", alertType: Constants.AlertConfigType.AGGREGATE_CASE_ALERT_FAERS)
        frequency1.save(flush: true)

        validatedSignal = new ValidatedSignal(name: "ValidatedSignal", assignedTo: user, assignmentType: "signalAssignment",
                createdBy: "username", disposition: disposition, modifiedBy: "username", priority: priority, products: "product1", workflowGroup: wfGroup)
        validatedSignal.save(flush: true, failOnError: true)

        Alert alert1 = new Alert(assignedTo: user, priority: priority)
        alert1.save(flush: true, failOnError: true)

        def alertServiceObj = Mock(AlertService)
        def allowedProducts = ["Calpol01", "Test Product AJ", "ALL-LIC-PROD", "Wonder Product"]
        def resultMapData = [totalCount: 1, totalFilteredCount: 1, resultList: [[alertName: "test case alert"]]]
        alertServiceObj.fetchAllowedProductsForConfiguration() >> allowedProducts
        alertServiceObj.getAlertFilterCountAndList(_) >> resultMapData
        alertServiceObj.isProductSecurity() >> true
        alertServiceObj.prepareFilterMap(_, _) >> [productName: 'productA', assigned: user.username, newDashboardFilter: true]
        alertServiceObj.prepareOrderColumnMap(_) >> [name: 'productA', dir: 'dir']
        alertServiceObj.getAlertFilterCountAndList(_) >> [totalCount: 10, totalFilteredCount: 20, resultList: [alert], fullCaseList: [alert]]
        alertServiceObj.getDistinctProductName(_, _, _) >> ['productA', 'productB']
        controller.alertService = alertServiceObj

        ProductEventHistoryService mockProductEventHistoryService = Mock(ProductEventHistoryService)
        mockProductEventHistoryService.batchPersistHistory(_) >> {
            return true
        }
        controller.productEventHistoryService = mockProductEventHistoryService

        UserService mockUserService = Mock(UserService)
        Preference preference = new Preference()
        preference.timeZone = "UTC"
        preference.isCumulativeAlertEnabled >> true
        mockUserService.getCurrentUserPreference() >> preference
        mockUserService.getUser() >> {
            return user
        }
        mockUserService.bindSharedWithConfiguration(configuration, "shared", true) >> {
            return configuration
        }
        mockUserService.assignGroupOrAssignTo(_, configuration) >> {
            return configuration
        }
        controller.userService = mockUserService
        controller.dispositionService.userService = mockUserService
        controller.aggregateCaseAlertService.userService = mockUserService

        ActionConfiguration actionConfiguration = new ActionConfiguration(value: 'PRV',
                displayName: 'Periodic review',
                isEmailEnabled: true,
                description: 'testing')
        ValidatedSignalService mockValidatedSignalService = Mock(ValidatedSignalService)
        mockValidatedSignalService.getActionConfigurationList(_) >> {
            return [actionConfiguration]
        }
        controller.validatedSignalService = mockValidatedSignalService
        SpotfireService mockSpotfireService = Mock(SpotfireService)
        mockSpotfireService.fetchAnalysisFileUrl(executedConfiguration) >> {
            return ['file1', 'file2']
        }
        controller.spotfireService = mockSpotfireService

        CacheService cacheService = Mock(CacheService)
        controller.cacheService = cacheService
        cacheService.getSubGroupMap() >> ["AGE_GROUP": [1: 'Adolescent', 2: 'Adult', 3: 'Child', 4: 'Elderly', 5: 'Foetus'], "GENDER": [6: 'AddedNew', 7: 'Confident', 8: 'Female', 9: 'MALE_NEW',]]

        this.aggregateCaseAlertService = Mock(AggregateCaseAlertService)

        this.justification = new Justification(name: "Test Justification", justification: "Test", feature : "feature")
        this.justification.save()
    }

    void "test create"() {
        setup:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        when:
        controller.create()
        then:
        response.status == 200
        model.configurationInstance.type == 'Aggregate Case Alert'
        model.priorityList[0]["value"] == "mockPriority"
        model.userList[0]["username"] == "username"
        model.action == "create"

    }

    void "test listByExecutedConfig without passed values"() {

        when:

        controller.listByExecutedConfig()

        then:
        response.status == 200
        response.json.recordsTotal == 0
        response.json.recordsFiltered == 0
    }

    void "test listByExecutedConfig with passed values"() {

        when:

        controller.listByExecutedConfig(false, 1, false)

        then:
        response.status == 200
        response.json.recordsTotal == 0
        response.json.recordsFiltered == 0
    }

    void "test for fetchSubGroupsMap"() {
        setup:
        controller.params.dataSource = "pva"

        when:
        controller.fetchSubGroupsMap()

        then:
        response.status == 200
        response.json.subGroupsMap.AGE_GROUP == ['Adolescent', 'Adult', 'Child', 'Elderly', 'Foetus']
        response.json.subGroupsMap.GENDER == ['AddedNew', 'Confident', 'Female', 'MALE_NEW']
        response.json.isAgeEnabled == true
        response.json.isGenderEnabled == true
    }

    void "test for fetchSubGroupsMap when datasource is faers"() {
        setup:
        controller.params.dataSource = "faers"

        when:
        controller.fetchSubGroupsMap()

        then:
        response.status == 200
        response.json.subGroupsMap == [:]
        response.json.isAgeEnabled == false
        response.json.isGenderEnabled == false
    }

    void "test editShare"() {
        when:
        controller.editShare()

        then:
        response.status == 302
        response.redirectedUrl == '/aggregateCaseAlert/review'
    }

    void "test fetchFaersDisabledColumnsIndexes"() {
        when:
        controller.fetchFaersDisabledColumnsIndexes()

        then:
        response.status == 200
        response.json.disabledIndexValues == [3, 5]
    }

    void "test changeAlertLevelDisposition in case of Exception"() {
        when:
        controller.changeAlertLevelDisposition(disposition, "text", executedConfiguration, false)

        then:
        response.status == 200
        response.json.status == false
        response.json.message == "app.label.disposition.change.error"
    }

    void "test changeAlertLevelDisposition in case of Rows Count equal to zero"() {
        setup:
        AggregateCaseAlertService mockAggregateCaseAlertService = Mock(AggregateCaseAlertService)
        mockAggregateCaseAlertService.changeAlertLevelDisposition(_) >> {
            return 0
        }
        controller.aggregateCaseAlertService = mockAggregateCaseAlertService

        when:
        controller.changeAlertLevelDisposition(disposition, "text", executedConfiguration, false)

        then:
        response.status == 200
        response.json.status == true
        response.json.message == "alert.level.review.completed"
    }


    void "test changeAlertLevelDisposition in case of Rows Count greater than zero"() {
        setup:
        AggregateCaseAlertService mockAggregateCaseAlertService = Mock(AggregateCaseAlertService)
        mockAggregateCaseAlertService.changeAlertLevelDisposition(_) >> {
            return 1
        }
        controller.aggregateCaseAlertService = mockAggregateCaseAlertService

        NotificationHelper notificationHelper = Mock(NotificationHelper)
        notificationHelper.pushNotification(_) >> {
            return true
        }
        controller.dispositionService.notificationHelper = notificationHelper


        when:
        controller.changeAlertLevelDisposition(disposition, "text", executedConfiguration, false)

        then:
        response.status == 200
        response.json.status == true
        response.json.message == "alert.level.review.completed"
    }

    void "test fetchAllFieldValues with default map"() {
        setup:

        this.aggregateCaseAlertService.fieldListAdvanceFilter(_) >> {
            return [[:]]
        }

        controller.aggregateCaseAlertService = this.aggregateCaseAlertService
        when:
        controller.fetchAllFieldValues()

        then:
        response.status == 200
        response.json.size() == 1
        response.json == [[:]]
    }

    void "test fetchAllFieldValues with value map"() {
        setup:
        List subGrpFieldList = [
                [name: "flags", display: "Flags", dataType: 'java.lang.String'],
                [name: "priority.id", display: "Priority", dataType: 'java.lang.String'],
                [name: "productName", display: "Product Name", dataType: 'java.lang.String']
        ]
        AggregateCaseAlertService aggregateCaseAlertService = Mock(AggregateCaseAlertService)
        aggregateCaseAlertService.fieldListAdvanceFilter(_) >> {
            return subGrpFieldList
        }

        controller.aggregateCaseAlertService = aggregateCaseAlertService
        when:
        controller.fetchAllFieldValues()

        then:
        response.status == 200
        response.json.size() == 3
        response.json == [
                [name: "flags", display: "Flags", dataType: 'java.lang.String'],
                [name: "priority.id", display: "Priority", dataType: 'java.lang.String'],
                [name: "productName", display: "Product Name", dataType: 'java.lang.String']
        ]
    }

    void "test fetchAllFieldValues with null coming from service"() {
        setup:

        AggregateCaseAlertService aggregateCaseAlertService = Mock(AggregateCaseAlertService)
        aggregateCaseAlertService.fieldListAdvanceFilter(_) >> {
            return null
        }

        controller.aggregateCaseAlertService = aggregateCaseAlertService
        when:
        controller.fetchAllFieldValues()

        then:
        thrown Exception
    }

    void "test fetchPossibleValues"() {
        setup:
        AlertService mockAlertService = Mock(AlertService)
        mockAlertService.preparePossibleValuesMap(_, _, _) >> {
            return [alertTags: [[id: alert_tag, text: alert_tag]]]
        }
        controller.alertService = mockAlertService

        when:
        controller.fetchPossibleValues(executedConfiguration.id)

        then:
        response.status == 200
        response.json.alertTags == [[id: alert_tag.name, text: alert_tag.name]]
        response.json.listed == [[id: "Yes", text: "Yes"], [id: "No", text: "No"]]
        response.json.positiveRechallenge == [[id: "Yes", text: "Yes"], [id: "No", text: "No"]]
    }

    void "test archivedAlert"() {
        setup:
        params.start = 1
        params.length = 2
        when:
        controller.archivedAlert(executedConfiguration.id)

        then:
        response.status == 200
    }

    void "test changeDisposition action on success"() {
        setup:
        String selectedRows = '[{"alert.id":1},{"alert.id":2}]'
        Disposition targetDisposition = Disposition.get(1)
        String justification = "change needed"
        String validatedSignalName = "ValidatedSignal2"
        String productJson = "product1"
        AggregateCaseAlertService mockAggregateCaseAlertService = Mock(AggregateCaseAlertService)
        mockAggregateCaseAlertService.changeDisposition([1L, 2L], targetDisposition, justification, validatedSignalName, productJson, false,1) >> {
            return true
        }
        controller.aggregateCaseAlertService = mockAggregateCaseAlertService

        when:
        controller.changeDisposition(selectedRows, targetDisposition, justification, validatedSignalName, productJson,1)

        then:
        JSON.parse(response.text).code == 200
        JSON.parse(response.text).status == false
    }

    void "test changeDisposition action when exception occurs"() {
        setup:
        String selectedRows = '[["alert.id":1],["alert.id":2]]'
        Disposition targetDisposition = Disposition.get(1)
        String justification = "change needed"
        String validatedSignalName = "ValidatedSignal2"
        String productJson = "product1"

        when:
        controller.changeDisposition(selectedRows, targetDisposition, justification, validatedSignalName, productJson,1)

        then:
        JSON.parse(response.text).code == 200
        JSON.parse(response.text).status == false
        JSON.parse(response.text).data == null
        JSON.parse(response.text).message == "app.label.disposition.change.error"
    }

    void "test modelData"() {
        setup:
        params.dataSource = "pva"
        params.selectedDataSource = "pva"
        params.signalId = "1"
        AggregateCaseAlertService mockAggregateCaseAlertService = Mock(AggregateCaseAlertService)
        mockAggregateCaseAlertService.checkProductNameListExistsForFAERS(_, _) >> {
            return true
        }
        mockAggregateCaseAlertService.getEnabledOptions()>>{
            return [enabledOptions:["PVA"], defaultSelected:""]
        }
        controller.aggregateCaseAlertService = mockAggregateCaseAlertService

        when:
        Map result = controller.modelData(alertConfiguration, "action")

        then:
        result.configurationInstance == alertConfiguration
        result.userList == [user, newUser]
        result.action == "action"
        result.spotfireEnabled == true
    }

    void "test fetchFreqName"() {
        when:
        controller.fetchFreqName("Yearly")

        then:
        response.status == 200
        response.json.frequency == 'Yearly'
    }

    void "test alertTagDetails"() {
        when:
        controller.alertTagDetails(alert.id, false)

        then:
        response.status == 200
        response.json.tagList == [alert_tag.name]
        response.json.alertTagList == [alert_tag.name]
    }

    void "test getSubstanceFrequency"() {
        setup:
        params.dataSource = "faers"

        when:
        controller.getSubstanceFrequency("Test Product 1")

        then:
        response.status == 200
        response.json.miningFrequency == 'Yearly'
    }

    void "test showCharts"() {
        setup:
        alert.alertConfiguration = configuration
        alert.save(validate:false)
        params.alertId = alert.id
        when:
        controller.showCharts()
        then:
        response.status == 200
        response.json.frequency == 'Yearly'
    }
    void "test showCharts when alertid absent"() {
        setup:
        alert.alertConfiguration = configuration
        alert.save(validate:false)
        params.alertId = null
        when:
        controller.showCharts()
        then:
        response.status == 200
        response.json.frequency == 'Yearly'
        response.json.studyCount == [1]
        response.json.isRor == null
        response.json.rorvalue == null
        response.json.sponCount == [1]
        response.json.prrValue == [1.0]
        response.json.fatalCount == [1]
        response.json.frequency == 'Yearly'

    }
    void "test fetchStratifiedScores in case of PRR"() {
        setup:
        AggregateCaseAlertService mockAggregateCaseAlertService = Mock(AggregateCaseAlertService)
        mockAggregateCaseAlertService.getDomainObject(_) >> {
            return AggregateCaseAlert
        }
        controller.aggregateCaseAlertService = mockAggregateCaseAlertService
        when:
        controller.fetchStratifiedScores(alert.id, Constants.Stratification_Fields.PRR, false)

        then:
        response.status == 200
        response.json.PRR == 1.0
        response.json."PRR(MH)" == "1"
    }

    void "test fetchStratifiedScores in case of ROR"() {
        when:
        controller.fetchStratifiedScores(alert.id, Constants.Stratification_Fields.ROR, false)

        then:
        response.status == 200
        response.json."ROR(MH)" == "1"
        response.json.ROR == 1.0
    }

    void "test fetchStratifiedScores in case of PRRLCI"() {
        when:
        controller.fetchStratifiedScores(alert.id, Constants.Stratification_Fields.PRRLCI, false)

        then:
        response.status == 200
        response.json."PRR(MH)" == "1"
        response.json.PRRLCI == 1.0
    }

    void "test fetchStratifiedScores in case of PRRUCI"() {
        when:
        controller.fetchStratifiedScores(alert.id, Constants.Stratification_Fields.PRRUCI, false)

        then:
        response.status == 200
        response.json."PRR(MH)" == "1"
        response.json.PRRUCI == 1.0
    }

    void "test fetchStratifiedScores in case of ROR05"() {
        when:
        controller.fetchStratifiedScores(alert.id, Constants.Stratification_Fields.RORLCI, false)

        then:
        response.status == 200
        response.json."ROR(MH)" == "1"
        response.json.RORLCI == 1.0
    }

    void "test fetchStratifiedScores in case of ROR95"() {
        when:
        controller.fetchStratifiedScores(alert.id, Constants.Stratification_Fields.RORUCI, false)

        then:
        response.status == 200
        response.json."ROR(MH)" == "1"
        response.json.ROR95 == 1.0
    }

    void "test fetchStratifiedScores in case of error"() {
        when:
        controller.fetchStratifiedScores(alert.id, null, false)

        then:
        response.status == 200
        response.json.ErrorMessage == "app.label.stratification.values.error"
    }

    void "test pecTreeJson"() {
        when:
        controller.pecTreeJson()

        then:
        response.status == 200
        JSON.parse(response.text) != null
    }

    void "test showTrendAnalysis faers true"() {
        when:
        params.alertId = alert.id
        controller.showTrendAnalysis(true)

        then:
        response.status == 302
        response.redirectedUrl == '/trendAnalysis/showTrendAnalysis?type=Aggregate+Case+Alert&isFaers=true'
    }
    void "test showTrendAnalysis not fears"() {
        when:
        params.alertId = alert.id
        controller.showTrendAnalysis(false)

        then:
        response.status == 302
        response.redirectedUrl == '/trendAnalysis/showTrendAnalysis?type=Aggregate+Case+Alert&isFaers=false'
    }
    void "test listConfig"() {
        when:
        controller.listConfig()

        then:
        response.status == 200
    }

    void "test getListWithoutFilter with Dashboard and true"() {
        when:
        params.callingScreen = Constants.Commons.DASHBOARD
        List result = controller.getListWithoutFilter(executedConfiguration, true)

        then:
        result == [alert]
    }

    void "test getListWithoutFilter with Dashboard and false"() {
        when:
        params.callingScreen = Constants.Commons.DASHBOARD
        List result = controller.getListWithoutFilter(executedConfiguration, false)

        then:
        result == [alert]
    }

    void "test getListWithoutFilter with Review and true"() {
        when:
        params.callingScreen = Constants.Commons.REVIEW
        List result = controller.getListWithoutFilter(executedConfiguration, true)

        then:
        result == [alert]
    }

    void "test getListWithoutFilter with Review and false"() {
        when:
        params.callingScreen = Constants.Commons.REVIEW
        List result = controller.getListWithoutFilter(executedConfiguration, false)

        then:
        result == [alert]
    }

    void "test getListWithoutFilter with TriggeredAlerts and true"() {
        when:
        params.callingScreen = Constants.Commons.TRIGGERED_ALERT
        List result = controller.getListWithoutFilter(executedConfiguration, true)

        then:
        result == [alert]
    }

    void "test getListWithoutFilter with TriggeredAlerts and false"() {
        when:
        params.callingScreen = Constants.Commons.TRIGGERED_ALERT
        List result = controller.getListWithoutFilter(executedConfiguration, false)

        then:
        result == [alert]
    }

    void "test getListWithoutFilter with ec and showClosed true"() {
        when:
        List result = controller.getListWithoutFilter(executedConfiguration, true)

        then:
        result == [alert]
    }

    void "test getListWithoutFilter without ec and showClosed true"() {
        when:
        List result = controller.getListWithoutFilter(null, true)

        then:
        result == [alert]
    }

    void "test getListWithoutFilter with ec and showClosed false"() {
        when:
        List result = controller.getListWithoutFilter(executedConfiguration, false)

        then:
        result == [alert]
    }

    void "test toggleFlag"() {
        when:
        params.id = alert.id
        controller.toggleFlag()

        then:
        response.status == 200
        response.json.flagged == true
        response.json.success == 'ok'
    }

    void "test toggleFlag in case of null id"() {
        when:
        params.id = null
        controller.toggleFlag()

        then:
        response.status == 404
    }

    void "test changeAssignedToGroup in case of Exception"() {
        setup:
        String selectedId = '["1"]'
        UserService mockUserService = Mock(UserService)
        mockUserService.getAssignedToName(alert) >> {
            return "Alert A"
        }
        mockUserService.getUserListFromAssignToGroup(alert) >> {
            return [user, newUser]
        }
        mockUserService.assignGroupOrAssignTo(alert) >> {
            return alert
        }
        controller.userService = mockUserService
        controller.aggregateCaseAlertService = [getActivityByType: { _ -> }]
        controller.aggregateCaseAlertService = [getDomainObject : { _ ->AggregateCaseAlert}]

        when:
        controller.changeAssignedToGroup(selectedId, "Assigned", false)

        then:
        response.status == 200
        response.json.status == false
        response.json.message == 'app.assignedTo.changed.fail'
    }

    void "test changeAssignedToGroup"() {
        setup:
        List newUserList = [user, newUser]
        List oldUserList = [user, newUser]
        String newEmailMessage = 'new email message'
        String oldEmailMessage = 'old email message'
        List emailDataList  = []
        Map peHistoryMap = [
                            "justification"   : '',
                            "change"          : Constants.HistoryType.ASSIGNED_TO,
                            cumFatalCount           : alert.cumFatalCount,
                            cumSeriousCount         : alert.cumSeriousCount,
                            cumSponCount            : alert.cumSponCount,
                            cumStudyCount           : alert.cumStudyCount,
                            newFatalCount           : alert.newFatalCount,
                            newSeriousCount         : alert.newSeriousCount,
                            newSponCount            : alert.newSponCount,
                            newStudyCount           : alert.newStudyCount,
                            positiveRechallenge     : alert.positiveRechallenge,
                            "productName"           : alert.productName,
                            "eventName"             : alert.pt,
                            "prrValue"              : alert.prrValue,
                            "rorValue"              : alert.rorValue,
                            "ebgm"                  : alert.ebgm,
                            "eb05"                  : alert.eb05,
                            "asOfDate"              : alert.periodEndDate,
                            "assignedTo"            : alert.assignedTo,
                            "assignedToGroup"       : alert.assignedToGroup,
                            "disposition"           : alert.disposition,
                            "eb95"                  : alert.eb95,
                            "executionDate"         : alert.dateCreated,
                            "createdBy"             : user?.fullName,
                            "modifiedBy"            : user?.fullName,
                            "aggCaseAlertObj"       : alert.id,
                            "archivedAggCaseAlertId":  null,
                            "aggCaseAlertId"        : alert.id,
                            "execConfigId"          : alert.executedAlertConfigurationId,
                            "configId"              : alert.alertConfigurationId,
                            "priority"              : alert.priority,
                            "isLatest"              : true,
                            "dueDate"               : alert.dueDate
        ]
        def dashboardCountDTO = (["dispCountKey" :Constants.UserDashboardCounts.USER_DISP_PECOUNTS,"dueDateCountKey":Constants.UserDashboardCounts.USER_DUE_DATE_PECOUNTS,"groupDispCountKey":Constants.UserDashboardCounts.GROUP_DISP_PECOUNTS,"groupDueDateCountKey":Constants.UserDashboardCounts.GROUP_DUE_DATE_PECOUNTS])
        String selectedId = '[1]'
        AlertService mockAlertService = Mock(AlertService)
        mockAlertService.prepareDashboardCountDTO(_) >> {
            return dashboardCountDTO
        }
        controller.alertService = mockAlertService
        ActivityService mockActivityService = Mock(ActivityService)
        mockActivityService.createActivity(_,_,_,_,_,_,_,_,_,_,_,_,_) >> {}
        controller.activityService = mockActivityService
        UserService mockUserService = Mock(UserService)
        mockUserService.getAssignToValue(alert) >> {
            return null
        }
        mockUserService.getAssignedToName(alert) >> {
            return "Alert A"
        }
        mockUserService.getUserListFromAssignToGroup(alert) >> {
            return [user, newUser]
        }
        mockUserService.assignGroupOrAssignTo("Assigned", alert) >> {
            return alert
        }
        mockUserService.generateEmailDataForAssignedToChange(newEmailMessage, newUserList, oldEmailMessage, oldUserList) >> {
            return [user: newUser, emailMessage: newEmailMessage]

        }
        controller.userService = mockUserService
        ActivityType activityType = new ActivityType(value: ActivityTypeValue.AssignedToChange)
        activityType.save(flush: true, failOnError: true)
        AggregateCaseAlertService mockAggregateCaseAlertService = Mock(AggregateCaseAlertService)
        mockAggregateCaseAlertService.getAlertConfigObject(executedConfiguration.name, executedConfiguration.owner) >> {
            return 1L
        }
        mockAggregateCaseAlertService.createPEHistoryMapForAssignedToChange(alert, configuration.id, false) >> {
            return peHistoryMap
        }
        mockAggregateCaseAlertService.getActivityByType(ActivityTypeValue.AssignedToChange) >> {
            return activityType
        }
        mockAggregateCaseAlertService.getDomainObject(false) >> {
            return AggregateCaseAlert
        }
        mockAggregateCaseAlertService.sendMailForAssignedToChange(emailDataList, alert, false) >> {}
        controller.aggregateCaseAlertService = mockAggregateCaseAlertService
        EmailNotificationService mockService = Mock(EmailNotificationService)
        mockService.emailNotificationWithBulkUpdate(_, _) >> {
            return true
        }
        controller.emailNotificationService = mockService
        executedConfiguration.configId = 2l
        executedConfiguration.save(validate:false)
        when:
        controller.changeAssignedToGroup(selectedId, "Constants.USER_GROUP_TOKEN", false)

        then:
        response.status == 200
    }

    void "test changePriorityOfAlert in case of Exception"() {
        setup:
        String selectedRows = '[["alert.id":1],["executedConfigObj.id":1],["configObj.id":1]]'

        when:
        controller.changePriorityOfAlert(selectedRows, priorityNew, "justification", false)

        then:
        response.status == 200
        response.json.status == false
        response.json.message == "app.label.priority.change.error"
    }

    void "test changePriority in case of Exception"() {
        when:
        String alertList = '[["alert.id":1]]'
        controller.changePriority(executedConfiguration.id, "newPriority", "justified", alertList)

        then:
        response.status == 400
    }

    void "test setNextRunDateAndScheduleDateJSON in case of null"() {
        when:
        controller.setNextRunDateAndScheduleDateJSON(configuration)

        then:
        configuration.nextRunDate == null
    }

    void "test setNextRunDateAndScheduleDateJSON"() {
        setup:
        ConfigurationService mockConfigurationService = Mock(ConfigurationService)
        mockConfigurationService.getNextDate(configuration) >> {
            return new DateTime(2015, 12, 15, 0, 0, 0).toDate()
        }
        controller.configurationService = mockConfigurationService

        when:
        controller.setNextRunDateAndScheduleDateJSON(configuration)

        then:
        configuration.nextRunDate == new DateTime(2015, 12, 15, 0, 0, 0).toDate()
    }

    void "test copy in case of null config"() {
        when:
        controller.copy(null)

        then:
        response != null
    }

    void "test copy"() {
        setup:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        alertConfiguration.alertDateRangeInformation = null
        alertConfiguration.save(validate:false)
        when:
        controller.copy(alertConfiguration)

        then:
        response.status == 200
        view == '/aggregateCaseAlert/create'
    }

    void "test viewExecutedConfig in case of null"() {
        when:
        controller.viewExecutedConfig(null)

        then:
        response != null
    }

    void "test viewExecutedConfig"() {
        setup:
        Map map_EBGM =["pva": [age: [], gender: [], receiptYear: [], ageSubGroup: [], genderSubGroup: [], "isEBGM": false]]
        Map map_PRR = ["pva": [age: [], gender: [], receiptYear: [], ageSubGroup: [], genderSubGroup: [], "isPRR": false]]
        AggregateCaseAlertService mockService = Mock(AggregateCaseAlertService)
        mockService.getStratificationVlauesDataMiningVariables(_,_)>> {
            return [map_EBGM: map_EBGM, map_PRR: map_PRR]
        }
        controller.aggregateCaseAlertService = mockService
        when:
        controller.viewExecutedConfig(executedConfiguration)

        then:
        response.status == 200
        view == '/aggregateCaseAlert/view'
        model.isExecuted == true
        model.modelMap.isDataMining == false
    }

    void "test delete in case of null"() {
        when:
        controller.delete(null)

        then:
        response != null
    }

    void "test delete in case of owner"() {
        setup:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }

        when:
        controller.delete(alertConfiguration)

        then:
        controller.flash.message == 'app.configuration.delete.success'
        response.status == 302
        response.redirectedUrl == '/configuration/index'
    }


    void "test delete in case of not owner"() {
        setup:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return false
        }

        when:
        controller.delete(configuration)

        then:
        controller.flash.warn == 'app.configuration.delete.fail'
        response.status == 302
        response.redirectedUrl == '/configuration/index'
    }

    void "test review"() {
        when:
        controller.review()

        then:
        response.status == 200
        view == '/aggregateCaseAlert/review'
    }

    void "test viewConfig in case of null id"() {
        when:
        params.id = null
        controller.viewConfig()

        then:
        controller.flash.error == "app.configuration.id.null"
        response.status == 302
        response.redirectedUrl == '/configuration/listAllResults'
    }

    void "test viewConfig in case of other params than result"() {
        when:
        params.id = 1
        params.from = 'notResult'
        controller.viewConfig()

        then:
        response.status == 302
        response.redirectedUrl == '/configuration/view/1'
    }

    void "test viewConfig"() {
        when:
        params.id = 1
        params.from = 'result'
        controller.viewConfig()

        then:
        response.status == 302
        response.redirectedUrl == '/configuration/viewExecutedConfig/1'
    }

    void "test populateModel"() {
        setup:
        ConfigurationService mockConfigurationService = Mock(ConfigurationService)
        mockConfigurationService.getNextDate(configuration) >> {
            return new DateTime(2015, 12, 15, 0, 0, 0).toDate()
        }
        controller.configurationService = mockConfigurationService
        UserService mockUserService = Mock(UserService)
        mockUserService.assignGroupOrAssignTo(_,_) >> {
            return configuration
        }
        controller.userService = mockUserService
        params.alertDateRangeInformation = alertDateRangeInformation
        params.productGroupSelection='{["name":"product group","id":1]}'
        params.eventGroupSelection='{["name":"event group","id":1]}'
        params.missedCases = false
        params.selectedDatasource = "pva"
        params.productSelection = "Test Product A"
        params.assignedToValue = "assigned"
        params.sharedWith = "shared"

        when:
        Configuration result = controller.populateModel(configuration)

        then:
        result.productGroupSelection == '{["name":"product group","id":1]}'
        result.eventGroupSelection == '{["name":"event group","id":1]}'
        result.missedCases == false
    }


    void "test listAllResults"() {
        setup:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }

        when:
        controller.listAllResults()

        then:
        response.status == 200
        view == '/aggregateCaseAlert/executionStatus'
    }

    void "test renderOnErrorScenario"() {
        when:
        params.previousAction = Constants.AlertActions.CREATE
        params.id = 1
        controller.renderOnErrorScenario(alertConfiguration)

        then:
        response.status == 200
        view == '/aggregateCaseAlert/edit'
    }

    void "test renderOnErrorScenario in case of no id"() {
        when:
        params.previousAction = Constants.AlertActions.CREATE
        params.id = null
        controller.renderOnErrorScenario(alertConfiguration)

        then:
        response.status == 200
        view == '/aggregateCaseAlert/create'
    }


    void "test runOnce in case of null id"() {
        when:
        params.id = null
        controller.runOnce()

        then:
        response != null
    }

    void "test runOnce in case of isEnabled true"() {
        when:
        params.id = 1
        controller.runOnce()

        then:
        response.status == 302
        response.redirectedUrl == '/aggregateCaseAlert/index'
        controller.flash.warn == 'app.configuration.run.exists'
    }

    void "test runOnce"() {
        setup:
        Configuration configurationNew = new Configuration(
                executing: false,
                template: reportTemplate,
                alertTriggerCases: 11,
                alertTriggerDays: 11,
                missedCases: false,
                selectedDatasource: "pva",
                name: "test",
                spotfireSettings: "{\"type\":\"VACCINCE\",\"rangeType\":[\"PR_DATE_RANGE\",\"CUMULATIVE\"]}",
                productSelection: "Test Product A",
                type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                createdBy: newUser.username, modifiedBy: newUser.username,
                assignedTo: newUser,
                priority: priority,
                owner: newUser,
                isEnabled: false,
                alertDateRangeInformation: alertDateRangeInformation,
                nextRunDate: null
        )
        configurationNew.save(flush: true)
        params.id = configurationNew.id

        when:
        controller.runOnce()

        then:
        response != null
    }

    void "test getRunOnceScheduledDateJson"() {
        when:
        String result = controller.getRunOnceScheduledDateJson()

        then:
        result.contains('{"name" :"UTC","offset" : "+00:00"}') == true
    }

    void "test edit in case of null instance"() {
        when:
        controller.edit(null)

        then:
        response != null
    }

    void "test details method"() {
        setup:
        def map = [viewdList: [], selectedViewInstance: null]
        ViewInstanceService viewInstanceServiceMock = Mock(ViewInstanceService)

        viewInstanceServiceMock.fetchViewsListAndSelectedViewMap(_, _) >> {
            return map
        }
        controller.viewInstanceService = viewInstanceServiceMock

        WorkflowRuleService workflowRuleService = Mock(WorkflowRuleService)
        workflowRuleService.fetchDispositionIncomingOutgoingMap() >> {
            return null
        }
        controller.workflowRuleService = workflowRuleService

        PriorityService priorityService = Mock(PriorityService)

        priorityService.listPriorityOrder() >> {
            return null
        }

        controller.priorityService = priorityService

        AlertService alertService = Mock(AlertService)

        alertService.listPriorityOrder() >> {
            return null
        }

        alertService.getActionTypeAndActionMap() >> {
            return [actionTypeList: null, actionPropertiesMap: null]
        }

        alertService.generateSearchableColumns(_,_,_) >> {
            List<Integer> filterIndex = []
            Map<Integer, String> filterIndexMap = [:]
            return [filterIndex, filterIndexMap]
        }

        controller.alertService = alertService

        aggregateCaseAlertService.fieldListAdvanceFilter(true,'AGGREGATE_CASE_ALERT') >> {
            return ["vaers"]
        }

        controller.aggregateCaseAlertService = aggregateCaseAlertService

        DispositionService dispositionService = Mock(DispositionService)
        dispositionService.getReviewCompletedDispositionList() >> {
            return ["ValidatedSignal"]
        }

        controller.dispositionService = dispositionService
        params.callingScreen = "review"
        when:
        controller.details()

        then:
        response.status == 302
        response.redirectedUrl == '/aggregateCaseAlert/index'
        model.fieldList == null
    }

    void "test fetchMiningVariables"(){
        setup:
        CacheService mockCache = Mock(CacheService)
        mockCache.getMiningVariables("pva")>>{
            return [GENDER: [label:"Gender"]]
        }
        controller.cacheService = mockCache
        String selectedDatasource = "pva"
        when:
        controller.fetchMiningVariables(selectedDatasource)
        then:
        response.status == 200
    }
}