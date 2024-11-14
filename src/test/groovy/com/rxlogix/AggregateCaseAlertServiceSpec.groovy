package com.rxlogix

import com.rxlogix.attachments.AttachmentLink
import com.rxlogix.cache.CacheService
import com.rxlogix.config.*
import com.rxlogix.dto.AlertDataDTO
import com.rxlogix.dto.AlertLevelDispositionDTO
import com.rxlogix.dto.DashboardCountDTO
import com.rxlogix.enums.*
import com.rxlogix.signal.*
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.AlertAsyncUtil
import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.util.Holders
import grails.web.mapping.LinkGenerator
import org.apache.commons.logging.Log
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Ignore
import org.slf4j.Logger
import spock.lang.Shared
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges
import com.rxlogix.user.Preference
import unit.com.rxlogix.PvsAlertTagServiceSpec

import javax.sql.DataSource
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@Mock([SubstanceFrequency, AggregateCaseAlert, Configuration, User, ExecutedConfiguration, Priority, Disposition, CaseHistory, ProductBasedSecurityService,
        WorkflowRule, CaseHistoryService, EvdasAlertService, AlertService, DataObjectService, Group, CacheService, ExecutionStatus,
        GlobalProductEvent, ExecutedTemplateQuery, ReportIntegrationService, AlertTag, UserService, AdvancedFilter, ViewInstance, ValidatedSignal,
        ProductEventHistory,BusinessConfiguration, AttachmentLink, EmailNotification])
@TestFor(AggregateCaseAlertService)
@TestMixin(GrailsUnitTestMixin)
@ConfineMetaClassChanges([ExecutedConfiguration])
class AggregateCaseAlertServiceSpec extends Specification {


    Disposition disposition
    Configuration alertConfiguration
    Configuration alertConfigurationFaers
    ExecutedConfiguration executedConfiguration
    User user
    def mockData
    def mockDateRange
    EvdasAlertService evdasAlertService
    @Shared
    AggregateCaseAlert aggregateCaseAlert_a
    @Shared
    AggregateCaseAlert aggregateCaseAlert_b
    GlobalProductEvent globalProductEvent
    List<Map> alertData = []
    Log logger
    ExecutionStatus executionStatus
    @Shared
    ViewInstance viewInstance
    AdvancedFilter advanceFilter
    Map<String,Long> otherDataSourcesExecIds = [:]
    def mockStratificationMap
    def setup() {
        logger = Mock(Log)
        service.log = logger as Log

        service.sessionFactory = Mock(SessionFactory)

        Priority priority = new Priority([displayName: "mockPriority", value: "mockPriority", display: true, defaultPriority: true, reviewPeriod: 1])
        priority.save(failOnError: true)
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, reviewCompleted: true, abbreviation: "vs")
        disposition.save(failOnError: true)


        Group wfGroup = new Group(name: "Default", createdBy: 'createdBy', modifiedBy: 'modifiedBy',
                groupType: GroupType.WORKFLOW_GROUP,
                defaultQualiDisposition: disposition,
                defaultQuantDisposition: disposition,
                defaultAdhocDisposition: disposition,
                defaultEvdasDisposition: disposition,
                defaultLitDisposition: disposition,
                defaultSignalDisposition: disposition,
                autoRouteDisposition: disposition,
                justificationText: "Update Disposition",
                forceJustification: true)

        wfGroup.save(flush: true)

        //Prepare the mock user
        user = new User(id: '1', username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(flush: true)

        alertConfiguration = new Configuration(
                executing: false,
                alertTriggerCases: 11,
                alertTriggerDays: 11,
                selectedDatasource: "pva",
                name: "test",
                productSelection: "Test Product A",
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,
                priority: priority,
                owner: user
        )
        AlertDateRangeInformation alertDateRangeInformation = new AlertDateRangeInformation()
        alertDateRangeInformation.dateRangeEnum = DateRangeEnum.CUMULATIVE
        alertDateRangeInformation.dateRangeStartAbsolute = new Date()
        alertDateRangeInformation.dateRangeEndAbsolute = new Date()
        alertConfiguration.alertDateRangeInformation = alertDateRangeInformation
        alertConfiguration.save(failOnError: true)

        alertConfigurationFaers = new Configuration(
                executing: false,
                alertTriggerCases: 11,
                alertTriggerDays: 11,
                selectedDatasource: "faers",
                name: "test",
                productSelection: "Test Product A",
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,
                priority: priority,
                owner: user
        )
        alertConfigurationFaers.save(failOnError: true)

        executionStatus = new ExecutionStatus(configId: alertConfiguration.id,
                startTime: new Date().getTime(),
                endTime: new Date().getTime(), executionStatus: ReportExecutionStatus.COMPLETED,
                reportVersion: 1, message: 'for testing purpose',
                owner: user, name: 'executionStatus', type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                nextRunDate: new Date())
        executionStatus.save(flush: true)

        executedConfiguration = new ExecutedConfiguration(name: "test", isLatest: true, adhocRun: false,
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(), type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: false, isEnabled: true, totalExecutionTime: 10,
                dateRangeType: DateRangeTypeCaseEnum.CASE_LOCKED_DATE,
                productSelection: "{\"1\":{\"name\":\"testVal1\",\"2\":\"testVal2\"}}", eventSelection: "['rash']", studySelection: "['test']",
                configSelectedTimeZone: "UTC",
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                limitPrimaryPath: true,
                includeMedicallyConfirmedCases: true,
                excludeFollowUp: false, includeLockedVersion: true,
                adjustPerScheduleFrequency: true,
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user, configId: alertConfiguration.id,
                pvrCaseSeriesId: 1,
                pvrCumulativeCaseSeriesId: 1,
                selectedDatasource: "pva",
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 1,dataMiningVariable: "Gender")
        ExecutedAlertDateRangeInformation executedAlertDateRangeInformation = new ExecutedAlertDateRangeInformation()
        executedAlertDateRangeInformation.dateRangeEnum = DateRangeEnum.CUMULATIVE
        executedAlertDateRangeInformation.dateRangeStartAbsolute = new Date()
        executedAlertDateRangeInformation.dateRangeEndAbsolute = new Date()
        executedConfiguration.executedAlertDateRangeInformation = executedAlertDateRangeInformation
        ExecutedDateRangeInformation executedDateRangeInfoTemplateQuery = new ExecutedDateRangeInformation(
                dateRangeEnum: DateRangeEnum.CUMULATIVE,
                dateRangeEndAbsolute: new Date(), dateRangeStartAbsolute: new Date()
        )
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(
                executedTemplate: 25l, createdBy: user.username, modifiedBy: user.username,
                executedDateRangeInformationForTemplateQuery: executedDateRangeInfoTemplateQuery, headerProductSelection: false,
                headerDateRange: false,
                blindProtected: false,
                privacyProtected: false,
                queryLevel: QueryLevelEnum.CASE
        )
        executedConfiguration.addToExecutedTemplateQueries(executedTemplateQuery)
        executedConfiguration.save(failOnError: true)

        SubstanceFrequency frequency = new SubstanceFrequency(name: 'Test Product', startDate: Date.parse('dd-MMM-yyyy', '01-Jan-2014'), endDate: Date.parse('dd-MMM-yyyy', '31-Dec-2014'),
                uploadFrequency: 'Yearly', miningFrequency: 'Yearly', frequencyName: "Yearly", alertType: "Aggregate Case Alert")
        frequency.save(flush: true)

        AlertTag alertTagA = new AlertTag(name: "testAlertA", createdBy: user, dateCreated: new Date())
        alertTagA.save(flush: true)

        AlertTag alertTagB = new AlertTag(name: "testAlertB", createdBy: user, dateCreated: new Date())
        alertTagB.save(flush: true)
        otherDataSourcesExecIds = ["Constants.DataSource.EUDRA":executedConfiguration.id,"Constants.DataSource.PVA":executedConfiguration.id,"Constants.DataSource.FAERS":executedConfiguration.id,"Constants.DataSource.VAERS":executedConfiguration.id]
        aggregateCaseAlert_a = new AggregateCaseAlert(
                alertConfiguration: alertConfiguration,
                executedAlertConfiguration: executedConfiguration,
                name: executedConfiguration.name,
                priority: priority,
                alertTags: alertTagA,
                disposition: disposition,
                assignedTo: user,
                detectedDate: executedConfiguration.dateCreated,
                productName: "Test Product A",
                soc: "BODY_SYS1",
                pt: 'Rash',
                hlt: 'TEST',
                hglt: 'TEST',
                llt: "INC_TERM2",
                newStudyCount: 1,
                cumStudyCount: 1,
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
                periodStartDate: new DateTime(2017, 7, 1, 0, 0, 0, DateTimeZone.forID('UTC')).toDate(),
                periodEndDate: new DateTime(2017, 12, 31, 0, 0, 0, DateTimeZone.forID('UTC')).toDate(),
                adhocRun: false,
                positiveRechallenge: "true",
                positiveDechallenge: "false",
                listed: "false",
                pregenency: "false",
                related: "false",
                flagged: false,
                format: "test",
                frequency: "Yearly",
                productId: 100083,
                ptCode: 10029404,
                newSponCount: 2,
                smqCode: 1213,
                eb05Str: "male:0.1, female:0.2, unknown:0.3",
                eb95Str: "male:0.1, female:0.2, unknown:0.3",
                ebgmStr: "male:0.1, female:0.2, unknown:0.3")
        globalProductEvent = new GlobalProductEvent()
        globalProductEvent.productEventComb = "100083-10029404"
        globalProductEvent.save(flush: true)
        aggregateCaseAlert_a.globalIdentity = globalProductEvent
        aggregateCaseAlert_a.save(failOnError: true)
        aggregateCaseAlert_b = new AggregateCaseAlert(
                alertConfiguration: alertConfiguration,
                executedAlertConfiguration: executedConfiguration,
                name: executedConfiguration.name,
                priority: priority,
                disposition: disposition,
                assignedTo: user,
                alertTags: alertTagB,
                detectedDate: executedConfiguration.dateCreated,
                productName: "Test Product A",
                soc: "BODY_SYS1",
                pt: 'Rash',
                hlt: 'TEST',
                hglt: 'TEST',
                llt: "INC_TERM2",
                newStudyCount: 1,
                cumStudyCount: 1,
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
                periodStartDate: new DateTime(2017, 7, 1, 0, 0, 0, DateTimeZone.forID('UTC')).toDate(),
                periodEndDate: new DateTime(2017, 12, 31, 0, 0, 0, DateTimeZone.forID('UTC')).toDate(),
                adhocRun: false,
                positiveRechallenge: "true",
                positiveDechallenge: "false",
                listed: "false",
                pregenency: "false",
                related: "false",
                flagged: false,
                format: "test",
                frequency: "Yearly",
                productId: 100083,
                ptCode: 10029404,
                newSponCount: 2,
                eb05Str: "male:0.1, female:0.2, unknown:0.3",
                eb95Str: "male:0.1, female:0.2, unknown:0.3",
                ebgmStr: "male:0.1, female:0.2, unknown:0.3")
        aggregateCaseAlert_b.save(flush: true)
        AggregateCaseAlert aggregateCaseAlert_c = new AggregateCaseAlert(frequency: "Yearly", productId: 100083, ptCode: 10029404, periodEndDate: Date.parse('dd-MMM-yyyy', '31-Dec-2016'), newSponCount: 2)
        aggregateCaseAlert_c.save(validate: false)
        AggregateCaseAlert aggregateCaseAlert_d = new AggregateCaseAlert(frequency: "Yearly", productId: 100083, ptCode: 10029404, periodEndDate: Date.parse('dd-MMM-yyyy', '31-Dec-2017'), newSponCount: 2, freqPriority: "Priority 6", disposition: disposition)
        aggregateCaseAlert_d.save(validate : false)
        String columnSeq = """
                {"1":{"containerView":1,"label":"Tags","name":"alertTags","listOrder":0,"seq":6},"2":{"containerView":1,"label":"Receipt Date","name":"caseInitReceiptDate","listOrder":1,"seq":7},"3":{"containerView":1,"label":"Product Name","name":"productName","listOrder":2,"seq":8},"4":{"containerView":1,"label":"PT","name":"pt","listOrder":3,"seq":9},"5":{"containerView":1,"label":"Listedness","name":"listedness","listOrder":4,"seq":10},"6":{"containerView":1,"label":"Outcome","name":"outcome","listOrder":5,"seq":11},"7":{"containerView":1,"label":"Signal / Topic","name":"signalsAndTopics","listOrder":6,"seq":12},
                "8":{"containerView":1,"label":"Disposition","name":"currentDisposition","listOrder":7,"seq":13},"9":{"containerView":1,"label":"Current Disposition","name":"disposition","listOrder":8,"seq":14},"10":{"containerView":1,"label":"Assigned To","name":"assignedToUser","listOrder":9,"seq":15},"11":{"containerView":1,"label":"Due In","name":"dueDate","listOrder":10,"seq":16},"12":{"containerView":3,"label":"Suspect Products","name":"suspProd","listOrder":9999,"seq":17},"13":{"containerView":3,"label":"Con Med","name":"conComit","listOrder":9999,"seq":18},
                "14":{"containerView":3,"label":"PT List","name":"masterPrefTermAll","listOrder":9999,"seq":19},"15":{"containerView":1,"label":"Serious","name":"serious","listOrder":14,"seq":20},"16":{"containerView":3,"label":"Report Type","name":"caseReportType","listOrder":9999,"seq":21},"17":{"containerView":3,"label":"HCP","name":"reportersHcpFlag","listOrder":9999,"seq":22},"18":{"containerView":3,"label":"Country","name":"country","listOrder":9999,"seq":23},"19":{"containerView":3,"label":"Age Group","name":"age","listOrder":9999,"seq":24},"20":{"containerView":3,"label":"Gender","name":"gender","listOrder":9999,"seq":25},
                "21":{"containerView":3,"label":"Positive Rechallenge","name":"rechallenge","listOrder":9999,"seq":26},"22":{"containerView":3,"label":"Locked Date","name":"lockedDate","listOrder":9999,"seq":27},"23":{"containerView":3,"label":"Death","name":"death","listOrder":9999,"seq":28},"24":{"containerView":3,"label":"Medication Error PTs","name":"medErrorsPt","listOrder":9999,"seq":37},"25":{"containerView":3,"label":"Age","name":"patientAge","listOrder":9999,"seq":29},"26":{"containerView":3,"label":"Case Type","name":"caseType","listOrder":9999,"seq":31},
                "27":{"containerView":3,"label":"Completeness Score","name":"completenessScore","listOrder":9999,"seq":32},"28":{"containerView":3,"label":"Primary IND#","name":"indNumber","listOrder":9999,"seq":33},"29":{"containerView":3,"label":"Application#","name":"appTypeAndNum","listOrder":9999,"seq":34},"30":{"containerView":3,"label":"Compounding Flag","name":"compoundingFlag","listOrder":9999,"seq":35},"31":{"containerView":3,"label":"Indications","name":"indications","listOrder":9999,"seq":30},"32":{"containerView":3,"label":"Medication Error PT Count","name":"medErrorPtCount","listOrder":9999,"seq":38}}
           """
        String filters = """
                {"1":"pyrexia"}
        """
        String sorting = """
                {"1":"asc"}
        """
        viewInstance = new ViewInstance(name: "viewInstance", alertType: "Single Case Alert", user: user, columnSeq: columnSeq, filters: filters, sorting: sorting)
        viewInstance.save(flush: true, failOnError: true)

        advanceFilter = new AdvancedFilter()
        advanceFilter.alertType = "Single Case Alert"
        advanceFilter.criteria = "{ ->\n" +
                "criteriaConditions('listedness','EQUALS','true')\n" +
                "}"
        advanceFilter.createdBy = "fakeuser"
        advanceFilter.dateCreated = new Date()
        advanceFilter.description = "test advanced filter"
        advanceFilter.JSONQuery = "{\"all\":{\"containerGroups\":[ {\"expressions\":[ {\"index\":\"0\",\"field\":\"listedness\",\"op\":\"EQUALS\",\"value\":\"true\"} ] }  ] } }"
        advanceFilter.lastUpdated = new Date()
        advanceFilter.modifiedBy = "fakeuser"
        advanceFilter.name = "ad listed 1"
        advanceFilter.user = user
        advanceFilter.save(validate:false)

        mockStratificationMap = ["pva": ["age":["01_below", "02_04", "05_12", "13_16", "17_45", "46_75", "76_85", "86_above"],
                                      "gender":["Male", "Female", "Other", "UNK", "Confident", "unknownup1", "AddedNew", "MALE_NEW", "Gender_Aut", "newgender", "transgende", "feb_gender", "TEST_UNK", "CopyConf", "mar-gender", "Transege", "pvd_auto"],
                                      "receiptYear":["All Years"], ageSubGroup:["Foetus>=0 and <0 Years", "Neonate>=1 and <2 Years", "Infant>=.1 and <1 Years", "Child>=2 and <13 Years", "Adolescent>=13 and <18 Years", "Adult>=18 and <69 Years", "Elderly>=69 and <199 Years", "test_age>=29 and <49 Years", "zz>=2.2 and <5.6 Years", "製品名はレヴァチオ錠製品名はレヴァチオ錠>=1 and <20 Years",
                                                                              "feb_agrp>=5 and <55 Years", "add_newgrp>=6 and <65 Years", "mar_agrp>=6 and <60 Years"],
                                      "genderSubGroup":["Male", "Female", "Other", "UNK", "Confident", "unknownup1", "AddedNew", "MALE_NEW", "Gender_Aut", "newgender", "transgende", "feb_gender", "TEST_UNK", "CopyConf", "mar-gender", "Transege", "pvd_auto"],
                                      "isEBGM":true, "isSubGroup":true],
                                 "faers": ["age":["01_below", "02_04", "05_12", "13_16", "17_45", "46_75", "76_85", "86_above"],
                                           "gender":["Male", "Female", "Other", "UNK", "Confident", "unknownup1", "AddedNew", "MALE_NEW", "Gender_Aut", "newgender", "transgende", "feb_gender", "TEST_UNK", "CopyConf", "mar-gender", "Transege", "pvd_auto"],
                                           "receiptYear":["All Years"], ageSubGroup:["Foetus>=0 and <0 Years", "Neonate>=1 and <2 Years", "Infant>=.1 and <1 Years", "Child>=2 and <13 Years", "Adolescent>=13 and <18 Years", "Adult>=18 and <69 Years", "Elderly>=69 and <199 Years", "test_age>=29 and <49 Years", "zz>=2.2 and <5.6 Years", "製品名はレヴァチオ錠製品名はレヴァチオ錠>=1 and <20 Years",
                                                                                     "feb_agrp>=5 and <55 Years", "add_newgrp>=6 and <65 Years", "mar_agrp>=6 and <60 Years"],
                                           "genderSubGroup":["Male", "Female", "Other", "UNK", "Confident", "unknownup1", "AddedNew", "MALE_NEW", "Gender_Aut", "newgender", "transgende", "feb_gender", "TEST_UNK", "CopyConf", "mar-gender", "Transege", "pvd_auto"],
                                           "isEBGM":true, "isSubGroup":true]
        ]

        mockData = ["PRODUCT_NAME": "Test Product", "PRODUCT_ID": 100083, "PT_CODE": 10029404, "NEW_SPON_COUNT": BigInteger.valueOf(5), "CUMM_SPON_COUNT": BigInteger.valueOf(13)]
        mockDateRange = [Date.parse('dd-MMM-yyyy', '01-Jan-2018'), Date.parse('dd-MMM-yyyy', '31-Dec-2018')]
        alertData.add(['PRODUCT_ID': 100083, "PT_CODE": "10029404"])
        alertData.add(['PRODUCT_ID': 100083, "PT_CODE": "10029405"])
        alertData.add(['PRODUCT_ID': 100083, "PT_CODE": "10029406"])

        DataObjectService mockDataObjectService = Mock(DataObjectService)
        mockDataObjectService.getStatsDataMap(_, _, _) >> {
            return [eb05: new Double(1), eb95: new Double(1), ebgm: new Double(2)]
        }
        mockDataObjectService.getStatsDataMapSubgrouping(_, _, _) >> {
            return [ebgmAge   : 'Adolescent : 3.99 ,Adult : 3.99 ,Child : 3.99 ,Elderly : 3.99 ', eb05Age: 'Adolescent : 5.99 ,Adult : 5.99 ,Child : 5.99 ,Elderly : 5.99 '
                    , eb95Age : 'Adolescent : 7.99 ,Adult : 7.99 ,Child : 7.99 ,Elderly : 7.99 ', eb05Gender: 'Confident : 3.99,CopyConf : 3.99,Female : 0.3361',
                    eb95Gender: 'Confident : 3.99,CopyConf : 3.99,Male : 0.2891', ebgmGender: 'Confident : 3.99,CopyConf : 3.99,Female : 0.2361']
        }
        mockDataObjectService.getProbDataMap(_, _, _) >> {
            return [prrValue: "1", prrLCI: "1", prrUCI: "1", prrStr: "1", prrStrLCI: "1", prrStrUCI: "1", prrMh: "1", rorValue: "1", rorLCI: "1",
                    rorUCI   : "1", rorStr: "1", rorStrLCI: "1", rorStrUCI: "1", rorMh: "1", chiSquare: 15]
        }
        service.dataObjectService = mockDataObjectService

        UserService userService = Mock(UserService)
        Preference preference = new Preference()
        preference.timeZone = "UTC"
        userService.getCurrentUserPreference() >> preference
        service.userService = userService

        ViewInstanceService mockViewInstanceService = Mock(ViewInstanceService)
        mockViewInstanceService.fetchSelectedViewInstance(_,_) >> {
            return viewInstance
        }
        service.viewInstanceService = mockViewInstanceService

    }

    def cleanup() {
    }

    void "test printExecutionMessage when alertData present"() {
        when:
        service.printExecutionMessage(alertConfiguration, executedConfiguration, alertData)

        then:
        logger.info("\"Execution of Configuration took 10ms \" +\n" +
                "                \"for configuration test [C:${alertConfiguration.id}, EC: ${executedConfiguration.id}]. \" +\n" +
                "                \"It gave ${alertData} PE combinations\"")
        logger.info("Alert data save flow is complete.")
    }

    void "test printExecutionMessage when alertData absent"() {
        when:
        service.printExecutionMessage(alertConfiguration, executedConfiguration, null)

        then:
        logger.info("\"Execution of Configuration took 10ms \" +\n" +
                "                \"for configuration test [C:${alertConfiguration.id}, EC: ${executedConfiguration.id}]. \" +\n" +
                "                \"It gave 0 PE combinations\"")
        logger.info("Alert data save flow is complete.")
    }

    void "test setStatisticsScoresValues when smqCode is present"() {
        when:
        service.setStatisticsScoresValues(aggregateCaseAlert_a,null,false)

        then:
        aggregateCaseAlert_a.ebgm == new Double(2)
        aggregateCaseAlert_a.eb05 == new Double(1)
        aggregateCaseAlert_a.eb95 == new Double(1)
    }

    void "test setStatisticsScoresValues when smqCode is absent"() {
        when:
        service.setStatisticsScoresValues(aggregateCaseAlert_b,null,false)

        then:
        aggregateCaseAlert_b.ebgm == new Double(2)
        aggregateCaseAlert_b.eb05 == new Double(1)
        aggregateCaseAlert_b.eb95 == new Double(1)
    }
    void "test setStatisticsScoresValues when smqCode and VAERS Datasource is present"(){
        setup:
        Map vaersColumnMap = [productId             : 0,
                              ptCode                : 0,
                              smqCode                : 0,
                              newCountVaers          : 0,
                              cummCountVaers         : 0,
                              newSeriousCountVaers   : 0,
                              cumSeriousCountVaers   : 0,
                              ebgmVaers              : 0,
                              eb95Vaers              : 0,
                              eb05Vaers              : 0,
                              newGeriatricCountVaers : 0,
                              cumGeriatricCountVaers : 0,
                              newFatalCountVaers     : 0,
                              cumFatalCountVaers     : 0,
                              newPediatricCountVaers : 0,
                              cummPediatricCountVaers: 0,
                              prrValueVaers :0,
                              prrUCIVaers: 0,
                              prrLCIVaers:0,
                              rorValueVaers: 0,
                              rorLCIVaers:0,
                              rorUCIVaers: 0,
                              chiSquareVaers :0
        ]
        when:
        service.setStatisticsScoresValues(vaersColumnMap,null,false,"vaers")
        then:
        vaersColumnMap.ebgmVaers == new Double(2)
        vaersColumnMap.eb05Vaers == new Double(1)
        vaersColumnMap.eb95Vaers == new Double(1)
    }
    void "test setStatsSubgroupingScoresValues when smqCode is present"() {
        when:
        service.setStatsSubgroupingScoresValues(aggregateCaseAlert_a,null,false)

        then:
        aggregateCaseAlert_a.ebgmAge == 'Adolescent : 3.99 ,Adult : 3.99 ,Child : 3.99 ,Elderly : 3.99 '
        aggregateCaseAlert_a.eb05Age == 'Adolescent : 5.99 ,Adult : 5.99 ,Child : 5.99 ,Elderly : 5.99 '
        aggregateCaseAlert_a.eb95Age == 'Adolescent : 7.99 ,Adult : 7.99 ,Child : 7.99 ,Elderly : 7.99 '
        aggregateCaseAlert_a.ebgmGender == 'Confident : 3.99,CopyConf : 3.99,Female : 0.2361'
        aggregateCaseAlert_a.eb05Gender == 'Confident : 3.99,CopyConf : 3.99,Female : 0.3361'
        aggregateCaseAlert_a.eb95Gender == 'Confident : 3.99,CopyConf : 3.99,Male : 0.2891'
    }

    void "test setStatsSubgroupingScoresValues when smqCode is absent"() {
        when:
        service.setStatsSubgroupingScoresValues(aggregateCaseAlert_b,null,false)

        then:
        aggregateCaseAlert_b.ebgmAge == 'Adolescent : 3.99 ,Adult : 3.99 ,Child : 3.99 ,Elderly : 3.99 '
        aggregateCaseAlert_b.eb05Age == 'Adolescent : 5.99 ,Adult : 5.99 ,Child : 5.99 ,Elderly : 5.99 '
        aggregateCaseAlert_b.eb95Age == 'Adolescent : 7.99 ,Adult : 7.99 ,Child : 7.99 ,Elderly : 7.99 '
        aggregateCaseAlert_b.ebgmGender == 'Confident : 3.99,CopyConf : 3.99,Female : 0.2361'
        aggregateCaseAlert_b.eb05Gender == 'Confident : 3.99,CopyConf : 3.99,Female : 0.3361'
        aggregateCaseAlert_b.eb95Gender == 'Confident : 3.99,CopyConf : 3.99,Male : 0.2891'
    }

    void "test setPrrRorScoresValues when smqCode is present"() {
        when:
        service.setPrrRorScoresValues(aggregateCaseAlert_a,null,false,null,null)

        then:
        aggregateCaseAlert_a.prrValue == "1"
        aggregateCaseAlert_a.prrUCI == "1"
        aggregateCaseAlert_a.prrLCI == "1"
    }

    void "test setPrrRorScoresValues when smqCode is absent"() {
        when:
        service.setPrrRorScoresValues(aggregateCaseAlert_b,null,false,null,null)

        then:
        aggregateCaseAlert_b.prrValue == "1"
        aggregateCaseAlert_b.prrUCI == "1"
        aggregateCaseAlert_b.prrLCI == "1"
    }
    void "test setPrrRorScoresValues when smqCode is present and datasource is vaers"() {
        setup:
        Map vaersColumnMap = [productId             : 0,
                              ptCode                : 0,
                              smqCode                : 0,
                              newCountVaers          : 0,
                              cummCountVaers         : 0,
                              newSeriousCountVaers   : 0,
                              cumSeriousCountVaers   : 0,
                              ebgmVaers              : 0,
                              eb95Vaers              : 0,
                              eb05Vaers              : 0,
                              newGeriatricCountVaers : 0,
                              cumGeriatricCountVaers : 0,
                              newFatalCountVaers     : 0,
                              cumFatalCountVaers     : 0,
                              newPediatricCountVaers : 0,
                              cummPediatricCountVaers: 0,
                              prrValueVaers :0,
                              prrUCIVaers: 0,
                              prrLCIVaers:0,
                              rorValueVaers: 0,
                              rorLCIVaers:0,
                              rorUCIVaers: 0,
                              chiSquareVaers :0
        ]
        when:
        service.setPrrRorScoresValues(aggregateCaseAlert_a,null,false,null,vaersColumnMap)

        then:
        vaersColumnMap.prrValueVaers == "1"
        vaersColumnMap.prrUCIVaers == "1"
        vaersColumnMap.prrLCIVaers =="1"
        vaersColumnMap.rorValueVaers == "1"
        vaersColumnMap.rorLCIVaers == "1"
        vaersColumnMap.rorUCIVaers == "1"
        vaersColumnMap.chiSquareVaers == 15
    }
    void "test setPrrRorScoresValues when smqCode is absent and datasource is vaers"() {
        setup:
        Map vaersColumnMap = [productId             : 0,
                              ptCode                : 0,
                              smqCode                : 0,
                              newCountVaers          : 0,
                              cummCountVaers         : 0,
                              newSeriousCountVaers   : 0,
                              cumSeriousCountVaers   : 0,
                              ebgmVaers              : 0,
                              eb95Vaers              : 0,
                              eb05Vaers              : 0,
                              newGeriatricCountVaers : 0,
                              cumGeriatricCountVaers : 0,
                              newFatalCountVaers     : 0,
                              cumFatalCountVaers     : 0,
                              newPediatricCountVaers : 0,
                              cummPediatricCountVaers: 0,
                              prrValueVaers :0,
                              prrUCIVaers: 0,
                              prrLCIVaers:0,
                              rorValueVaers: 0,
                              rorLCIVaers:0,
                              rorUCIVaers: 0,
                              chiSquareVaers :0
        ]
        when:
        service.setPrrRorScoresValues(aggregateCaseAlert_b,null,false,null,vaersColumnMap)

        then:
        vaersColumnMap.prrValueVaers == "1"
        vaersColumnMap.prrUCIVaers == "1"
        vaersColumnMap.prrLCIVaers =="1"
        vaersColumnMap.rorValueVaers == "1"
        vaersColumnMap.rorLCIVaers == "1"
        vaersColumnMap.rorUCIVaers == "1"
        vaersColumnMap.chiSquareVaers == 15
    }
    void "test calcFreqPriority priority 1"() {
        when:
        def priority = service.calcFreqPriority(1, 1, 0, 0)
        then:
        priority == "Priority 1"
    }

    void "test calcFreqPriority priority 2"() {
        when:
        def priority = service.calcFreqPriority(5, 13, 2, 2)
        then:
        priority == "Priority 2"
    }

    void "test calcFreqPriority priority 3"() {
        when:
        def priority = service.calcFreqPriority(3, 10, 2, 2)
        then:
        priority == "Priority 3"
    }

    void "test calcFreqPriority priority 4"() {
        when:
        def priority = service.calcFreqPriority(2, 8, 1, 1.25)
        then:
        priority == "Priority 4"
    }

    void "test calcFreqPriority priority 5"() {
        when:
        def priority = service.calcFreqPriority(3, 11, 4, 2)
        then:
        priority == "Priority 5"
    }

    void "test calcFreqPriority priority 6"() {
        when:
        def priority = service.calcFreqPriority(1, 9, 3, 2)
        then:
        priority == "Priority 6"
    }

    void "test calcFreqPriority priority 7"() {
        when:
        def priority = service.calcFreqPriority(0, 1, 1, 0.25)
        then:
        priority == "Priority 7"
    }

    void "test setTrendType noTrend"() {
        when:
        def trendType = service.setTrendType("Priority 7", "Priority 7")
        then:
        trendType == "No Trend"
    }

    void "test setTrendType emergingTrend"() {
        when:
        def trendType = service.setTrendType("Priority 2", "Priority 7")
        then:
        trendType == "Emerging Trend"
    }

    void "test setTrendType continuingTrend"() {
        when:
        def trendType = service.setTrendType("Priority 2", "Priority 1")
        then:
        trendType == "Continuing Trend"
    }

    @Ignore
    void "test fetchPriorityTrendMap"() {
        setup:
        service.evdasAlertService = [populatePossibleDateRanges: { def date, def frequency ->
            return [['01-Jan-2014', '31-Dec-2014'], ['01-Jan-2015', '31-Dec-2015'], ['01-Jan-2016', '31-Dec-2016'], ['01-Jan-2017', '31-Dec-2017'], ['01-Jan-2018', '31-Dec-2018']]
        }]

        when:
        def priorityTrendMap = service.fetchPriorityTrendMap(mockData, mockDateRange)

        then:
        priorityTrendMap.freqPriority == "Priority 2"
        priorityTrendMap.trendType == "Emerging Trend"
    }

    @Ignore
    void "test setFlagsForAlert:New"() {
        setup:
        mockData = ["PRODUCT_NAME": "Test Product", "PRODUCT_ID": 100082, "PT_CODE": 10029402]
        when:
        def flag = service.setFlagsForAlert(mockData, mockDateRange[1], executedConfiguration)
        then:
        flag == "New"
    }

    @Ignore
    void "test setFlagsForAlert:Previously Reviewed"() {
        when:
        def flag = service.setFlagsForAlert(mockData, mockDateRange[1], executedConfiguration)
        then:
        flag == "Previously Reviewed"
    }

    @Ignore
    void "test getAggregateAlertListForProductSummary"() {
        setup:
        def productName = "Test Product A"
        def selectedDatasource = "pva"
        def dispositionValue = [disposition]
        def periodStartDate = new DateTime(2017, 7, 1, 0, 0, 0, DateTimeZone.forID('UTC')).toDate()
        def periodEndDate = new DateTime(2017, 12, 31, 0, 0, 0, DateTimeZone.forID('UTC')).toDate()
        when:
        def aggCaseAlertList = service.getAggregateAlertListForProductSummary(productName, periodStartDate, periodEndDate, dispositionValue, selectedDatasource)
        then:
        aggCaseAlertList.size() == 1
    }

    def "test getTagsNameList() method with reviewCompletedDispIdList null value"() {
        given:
        Long execConfigId = 5
        List<Long> reviewCompletedDispIdList = null

        when:
        Map<Long, String> response = service.getTagsNameList(execConfigId, reviewCompletedDispIdList)

        then:
        response == [:]
    }

    def "test createProductEventHistoryForBulkDisposition() method"() {
        given:
        AlertLevelDispositionDTO alertLevelDispositionDTO = new AlertLevelDispositionDTO(execConfigId: 15, loggedInUser: user)
        service.alertService = [
                getAlertTagNames: { id, tagNameMap ->
                    []
                }
        ]

        when:
        ProductEventHistory productEventHistory = service.createProductEventHistoryForBulkDisposition([:], [:], alertLevelDispositionDTO)

        then:
        productEventHistory.execConfigId == 15


    }

    def "test createBasePEHistoryMap() method"() {
        given:
        service.userService = [getUser: { return user }]

        when:
        Map peHistoryMap = service.createBasePEHistoryMap(aggregateCaseAlert_a, [customProperty: 'customProperty'])

        then:
        peHistoryMap.customProperty == 'customProperty'

    }

    void "test fetchNewProductEvent() "() {
        when:
        List<Long> newPEC = service.fetchNewProductEvent([PVA:alertData])

        then:
        assert newPEC.size() == 3

    }

    void "test generateCaseSeriesForQuantAlert "() {
        given:
        service.metaClass.generateCaseVersionList = { Long execConfigId, String dataSource, boolean isCumulative -> [] }
        service.alertService = [saveCaseSeriesInMart: { List<Map> caseAndVersionNumberList, ExecutedConfiguration executedConfiguration, Long seriesId, boolean isCumulative, String dataSource -> true }]
        service.signalDataSourceService = [getDataSource: { String selectedDataSource -> }]
        when:
        boolean isCaseSavedInMart = service.generateCaseSeriesForQuantAlert(executedConfiguration, true)
        then:
        isCaseSavedInMart
    }

    void "test generateCaseSeriesForQuantAlert if case series is not generated "() {
        given:
        service.metaClass.generateCaseVersionList = { Long execConfigId, String dataSource, boolean isCumulative -> }
        service.alertService = [saveCaseSeriesInMart: { List<Map> caseAndVersionNumberList, ExecutedConfiguration executedConfiguration, Long seriesId, boolean isCumulative, String dataSource -> false }]
        service.signalDataSourceService = [getDataSource: { String selectedDataSource -> }]
        when:
        boolean isCaseSavedInMart = service.generateCaseSeriesForQuantAlert(executedConfiguration, true)
        then:
        !isCaseSavedInMart
    }

    void "test invokeReportingForQuantAlert"() {
        given:
        GroovyMock(ExecutedConfiguration, global: true)
        ExecutedConfiguration.executeUpdate(_) >> 1
        service.singleCaseAlertService = [generateExecutedCaseSeries: { ExecutedConfiguration executedConfiguration, boolean isTemporary -> 5l }]
        service.alertService = [saveCaseSeriesInMart: { List<Map> caseAndVersionNumberList, ExecutedConfiguration executedConfiguration, Long, boolean isCumulative -> true }]
        service.reportIntegrationService = [runReport: { Long execConfigId -> }]
        when:
        service.invokeReportingForQuantAlert(executedConfiguration.id, 1L)
        then:
        thrown Exception
    }

    void "test prepareUpdateCaseSeriesHql"() {
        expect:
        updateSql == service.prepareUpdateCaseSeriesHql(isCumulative)
        where:
        isCumulative || updateSql
        true         || "Update ExecutedConfiguration set pvrCumulativeCaseSeriesId = :pvrCaseSeriesId  where id = :id"
        false        || "Update ExecutedConfiguration set pvrCaseSeriesId = :pvrCaseSeriesId  where id = :id"
    }

    void "test generateReportsInBackground"() {
        setup:
        service.metaClass.generateCaseSeriesForQuantAlert = { ExecutedConfiguration executedConfiguration, boolean isCumulative -> true }
        AlertService mockAlertService = Mock(AlertService)
        mockAlertService.generateReport(_) >> {
            return null
        }
        mockAlertService.generateSpotfireReport(_) >> {
            return null
        }
        service.alertService = mockAlertService

        when:
        boolean result = service.generateReportsInBackground(executedConfiguration.id,1L)

        then:
        result
    }

    void "test fetchAggCaseAlertsForBulkOperations"() {
        when:
        List aggCaseAlertList = service.fetchAggCaseAlertsForBulkOperations(AggregateCaseAlert ,[aggregateCaseAlert_a.id, aggregateCaseAlert_b.id])

        then:
        aggCaseAlertList == [aggregateCaseAlert_a, aggregateCaseAlert_b]
    }

    void "test generateCaseSeriesForReporting"() {
        setup:
        List dateRangeEnumList = ["PR_DATE_RANGE_FAERS"]
        Session session = Mock(Session)
        service.sessionFactory.getCurrentSession() >> session
        AlertService mockAlertService = Mock(AlertService)
        mockAlertService.dateRangeFaersSpotfire(_) >> {
            return dateRangeEnumList
        }
        service.alertService = mockAlertService
        GroovyMock(ExecutedConfiguration, global: true)
        ExecutedConfiguration.executeUpdate(_) >> 1
        executedConfiguration.spotfireSettings = "{\"type\":\"VACCINCE\",\"rangeType\":[\"PR_DATE_RANGE\",\"CUMULATIVE\"]}"
        service.singleCaseAlertService = [generateExecutedCaseSeries: { ExecutedConfiguration executedConfiguration, boolean isTemporary -> 5l }]
        service.sessionFactory.getCurrentSession() >> session
        expect:
        service.generateCaseSeriesForReporting(executedConfiguration, isGenerateReport, isSpotfire, isFaersSpotfire) == result

        where:
        isGenerateReport | isSpotfire | isFaersSpotfire || result
        true             | true       | true            || true
        false            | true       | false           || true
        true             | false      | true            || true
        false            | false      | false           || true
    }

    void "test fetchGlobalProductEvent"() {
        setup:
        GlobalProductEvent globalProductEvent = new GlobalProductEvent(productEventComb: "100083-10029404-null")
        globalProductEvent.save(flush: true)

        when:
        List result = service.fetchGlobalProductEvent([PVA:alertData])

        then:
        result[0] == globalProductEvent
    }

    void "test fetchPECfromdata"() {
        when:
        List<Map> result = service.fetchPECfromdata([PVA:alertData])

        then:
        result[0] == [productEventComb:"100083-10029404-null", productKeyId:null, eventKeyId:null]
        result[1] == [productEventComb:"100083-10029405-null", productKeyId:null, eventKeyId:null]
        result[2] == [productEventComb:"100083-10029406-null", productKeyId:null, eventKeyId:null]
    }

    void "test fetchNewProductEvent"() {
        setup:
        GlobalProductEvent globalProductEvent = new GlobalProductEvent(productEventComb: "100083-10029404-null")
        globalProductEvent.save(flush: true)

        when:
        List result = service.fetchNewProductEvent([PVA:alertData])

        then:
        result.size() == 3
        result[0] == [productEventComb:"100083-10029404-null", productKeyId:null, eventKeyId:null]
        result[1] == [productEventComb:"100083-10029405-null", productKeyId:null, eventKeyId:null]
        result[2] == [productEventComb:"100083-10029406-null", productKeyId:null, eventKeyId:null]
    }

    void "test fetchLastExecutionOfAlert"() {
        when:
        ExecutedConfiguration result = service.fetchLastExecutionOfAlert(executedConfiguration)

        then:
        result == executedConfiguration
    }

    void "test checkProductNameListExistsForFAERS"() {
        setup:
        ProductBasedSecurityService mockProductBasedSecurityService = Mock(ProductBasedSecurityService)
        mockProductBasedSecurityService.checkProductExistsForFAERS(_) >> {
            return true
        }
        mockProductBasedSecurityService.checkIngredientExistsForFAERS(_) >> {
            return true
        }
        service.productBasedSecurityService = mockProductBasedSecurityService

        expect:
        service.checkProductNameListExistsForFAERS(prodDictSelection, productNameList) == result

        where:
        prodDictSelection | productNameList          || result
        "3"               | ['ProductA', 'ProductB'] || true
        "1"               | ['ProductA', 'ProductB'] || true
        "4"               | ['ProductA', 'ProductB'] || false
        "5"               | ['ProductA', 'ProductB'] || false
    }

    void "test listSelectedAlerts"() {
        when:
        List result = service.listSelectedAlerts("1,2",AggregateCaseAlert)

        then:
        result.size() == 2
        result[0] == aggregateCaseAlert_a
        result[1] == aggregateCaseAlert_b
    }

    void "test getPreviousPeriodAggregateAlertListForProductSummary"() {
        when:
        List result = service.getPreviousPeriodAggregateAlertListForProductSummary(100083, new DateTime(2017, 7, 1, 0, 0, 0, DateTimeZone.forID('UTC')).toDate(), new DateTime(2017, 12, 31, 0, 0, 0, DateTimeZone.forID('UTC')).toDate(),
                [disposition], "pva", ['Rash'])

        then:
        result.size() == 1
        result[0] == aggregateCaseAlert_a
    }

    void "test setDefaultWorkflowStates"() {
        when:
        CacheService cacheService = Mock(CacheService)
        cacheService.getDispositionConfigsByPriority() >> []
        service.cacheService = cacheService
        service.setDefaultWorkflowStates(aggregateCaseAlert_a, alertConfiguration, executedConfiguration)

        then:
        aggregateCaseAlert_a.priority == alertConfiguration.priority
        aggregateCaseAlert_a.assignedTo == alertConfiguration.assignedTo
    }

    void "test getPreviousAlertMap"() {
        when:
        Map result = service.getPreviousAlertMap(executedConfiguration)

        then:
        result == ["Test Product A-Rash": aggregateCaseAlert_a]
    }

    void "test saveRequestByForAggCaseAlert"() {
        setup:
        Map params = [:]
        params.productName = 'Test Product A'
        params.eventName = 'Rash'
        params.requestedBy = "user"
        service.CRUDService = [saveWithoutAuditLog: { AggregateCaseAlert aggregateCaseAlert -> }]

        when:
        service.saveRequestByForAggCaseAlert(params)

        then:
        aggregateCaseAlert_a.requestedBy == "user"
        aggregateCaseAlert_b.requestedBy == "user"
    }

    def "test getColumnListForExcelExport" () {
        given:
        viewInstance = new ViewInstance([name: 'dummyView', alertType: 'test', filters: 'test filter', tempColumnSeq : '{"1":{"containerView":1,"label":"Test","name":"test","listOrder":0,"seq":0}}'])
        when:
        List columnList = service.getColumnListForExcelExport('test', 0l)
        then:
        assert columnList.size() == 1
    }
    def "test " () {
        given:
        grailsApplication.config.agaColumnExcelExportMap = [['Test Count/Test Cum count' : 'testCount']]
        when:
        List tempCoulmnList = service.getLabelNameList('Test Count/Test Cum count', 'testCount')
        then:
        assert tempCoulmnList.size() == 2
    }

    void "test getPreviousAlertTags"() {
        when:
        Session session = Mock(Session)
        service.sessionFactory.getCurrentSession() >> session
        Map result = service.getPreviousAlertTags(executedConfiguration)

        then:
        result == [:]
    }

    void "test getAllPreviousTags"() {
        when:
        Session session = Mock(Session)
        service.sessionFactory.getCurrentSession() >> session
        Map result = service.getAllPreviousTags(executedConfiguration, "")

        then:
        result == [:]
    }
    @Ignore
    void "test fetchAllPrevExecutionOfAlert"() {
        when:
        def result = service.fetchAllPrevExecutionOfAlert(executedConfiguration)

        then:
        result == [executedConfiguration]
    }
    void "test getAggregateCaseAlertCriteriaData()"(){
        Map params = [viewId:viewInstance.id as Long, advancedFilterId: advanceFilter.id as Long]
        service.metaClass.getStratificationVlaues = { String str ->
            return mockStratificationMap
        }
        when:
        def resultList = service.getAggregateCaseAlertCriteriaData(executedConfiguration, params)
        then:
        resultList.size()==26
    }
    void "test saveAlertCaseHistory"(){
        when:
        service.saveAlertCaseHistory(aggregateCaseAlert_a, "just", "signaldev")
        then:
        aggregateCaseAlert_a.justification == "just"
        aggregateCaseAlert_a.dispPerformedBy == "signaldev"
    }

    void "test getExecConfigIdForTrendFlag"(){
        when:
        List<Long> execIds = service.getExecConfigIdForTrendFlag(executedConfiguration)
        then:
        execIds.size() == 0
    }

    void "test setTrendFlag"(){
        setup:
        CacheService mockCacheService = Mock(CacheService)
        mockCacheService.getUserByUserName(_) >> {
            return user
        }
        List<Long> prevExecIds = service.getExecConfigIdForTrendFlag(executedConfiguration)
        List<Long> reviewedDispositions = mockCacheService.getDispositionByReviewCompleted()*.id
        service.cacheService = mockCacheService
        grailsApplication.config.signal.agg.trend.flag.fields = []
        grailsApplication.config.signal.agg.trend.flag.logic = ""
        when:
        service.setTrendFlag(aggregateCaseAlert_a, prevExecIds, reviewedDispositions)
        then:
        aggregateCaseAlert_a.trendFlag == "{}"
    }

    void "test changeDisposition"(){
        setup:
        ValidatedSignalService mockValidatedSignalService = Mock(ValidatedSignalService)
        CacheService mockCacheService = Mock(CacheService)
        CustomMessageService mockCustomMessageService = Mock(CustomMessageService)
        AlertService mockAlertService = Mock(AlertService)
        ActivityService mockActivityService = Mock(ActivityService)
        mockAlertService.prepareDashboardCountDTO(_)>>{
            DashboardCountDTO dashboardCountDTO = new DashboardCountDTO()
            dashboardCountDTO.dispCountKey = Constants.UserDashboardCounts.USER_DISP_PECOUNTS
            dashboardCountDTO.dueDateCountKey = Constants.UserDashboardCounts.USER_DUE_DATE_PECOUNTS
            dashboardCountDTO.groupDispCountKey = Constants.UserDashboardCounts.GROUP_DISP_PECOUNTS
            dashboardCountDTO.groupDueDateCountKey = Constants.UserDashboardCounts.GROUP_DUE_DATE_PECOUNTS
            return dashboardCountDTO
        }
        service.metaClass.fetchAggCaseAlertsForBulkOperations={def domain, List<Long> aggCaseAlertIdList -> return [aggregateCaseAlert_a]}
        service.validatedSignalService = mockValidatedSignalService
        service.cacheService = mockCacheService
        service.customMessageService = mockCustomMessageService
        service.alertService = mockAlertService
        service.activityService = mockActivityService
        grailsApplication.config.alert.validatedDateDispositions = []
        when:
        Map result = service.changeDisposition([AggregateCaseAlert.list()], disposition, "","","{\"3\":[{\"name\":\"test\",\"id\":\"1\"}]}",false,1)
        then:
        result.alertDueDateList.size()==1
    }
    void "test revertDisposition"(){
        setup:
        ValidatedSignalService mockValidatedSignalService = Mock(ValidatedSignalService)
        CacheService mockCacheService = Mock(CacheService)
        CustomMessageService mockCustomMessageService = Mock(CustomMessageService)
        AlertService mockAlertService = Mock(AlertService)
        ActivityService mockActivityService = Mock(ActivityService)
        mockAlertService.prepareDashboardCountDTO(_)>>{
            DashboardCountDTO dashboardCountDTO = new DashboardCountDTO()
            dashboardCountDTO.dispCountKey = Constants.UserDashboardCounts.USER_DISP_PECOUNTS
            dashboardCountDTO.dueDateCountKey = Constants.UserDashboardCounts.USER_DUE_DATE_PECOUNTS
            dashboardCountDTO.groupDispCountKey = Constants.UserDashboardCounts.GROUP_DISP_PECOUNTS
            dashboardCountDTO.groupDueDateCountKey = Constants.UserDashboardCounts.GROUP_DUE_DATE_PECOUNTS
            return dashboardCountDTO
        }
        service.metaClass.fetchAggCaseAlertsForBulkOperations={def domain, List<Long> aggCaseAlertIdList -> return [aggregateCaseAlert_a]}
        service.validatedSignalService = mockValidatedSignalService
        service.cacheService = mockCacheService
        service.customMessageService = mockCustomMessageService
        service.alertService = mockAlertService
        service.activityService = mockActivityService
        grailsApplication.config.alert.validatedDateDispositions = []
        when:
        Map result = service.revertDisposition(1,"test justification")
        then:
        result.alertDueDateList.size()==1
    }

    void "test undonePEHistory"(){
        setup:

        when:
        def result = service.undonePEHistory(aggregateCaseAlert_a)
        then:
        !result
    }

    void "test createActivityForUndoAction"() {
        setup:

        when:
        Activity activity = service.createActivityForUndoAction(aggregateCaseAlert_a,"test justification")
        then:
        activity

    }

    void "test setWorkflowMgmtStates"(){
        setup:
        CacheService mockCacheService = Mock(CacheService)
        service.cacheService = mockCacheService
        when:
        service.setWorkflowMgmtStates(aggregateCaseAlert_a, alertConfiguration, executedConfiguration)
        then:
        aggregateCaseAlert_a.assignedTo == alertConfiguration.assignedTo || aggregateCaseAlert_a.assignedTo == executedConfiguration.assignedTo
        aggregateCaseAlert_a.assignedToGroup == alertConfiguration.assignedToGroup || aggregateCaseAlert_a.assignedToGroup == executedConfiguration.assignedToGroup
    }

    void "test setWorkflowMgmtStates if existingLatestDispositionPEHistory and existingProductEventHistory"(){
        setup:
        Priority priority = new Priority([displayName: "mockPriorityTest", value: "mockPriorityTest", display: true, defaultPriority: true, reviewPeriod: 1])
        priority.save(failOnError: true,flush:true)
        ProductEventHistory peh1 = new ProductEventHistory(productName: "p1", eventName: "e1" , configId: 1l , execConfigId: 1l ,
                change: Constants.HistoryType.ASSIGNED_TO , priority: priority,
                disposition: disposition , prrValue: '0' , rorValue: '0' , eb05: 0.1 , ebgm: 0.2 ,
                eb95:0.3 , asOfDate: new Date())
        peh1.id = 1L
        peh1.save(flush:true)
        CacheService mockCacheService = Mock(CacheService)
        DataObjectService mockDataObjectService = Mock(DataObjectService)
        mockDataObjectService.getPEHistoryByConfigId(_)>>{
            return peh1
        }
        mockDataObjectService.getLatestDispositionPEHistory(_)>>{
            return peh1
        }
        service.cacheService = mockCacheService
        service.dataObjectService = mockDataObjectService
        when:
        service.setWorkflowMgmtStates(aggregateCaseAlert_a, alertConfiguration, executedConfiguration)
        then:
        aggregateCaseAlert_a.assignedTo == alertConfiguration.assignedTo || aggregateCaseAlert_a.assignedTo == executedConfiguration.assignedTo
        aggregateCaseAlert_a.assignedToGroup == alertConfiguration.assignedToGroup || aggregateCaseAlert_a.assignedToGroup == executedConfiguration.assignedToGroup
    }

    void "test getStratificationVlaues"(){
        when:
        Map result = service.getStratificationVlaues("pva")
        then:
        result.pva.isEBGM == false
        result.faers.isEBGM == false
    }

    void "test saveTagsForBusinessConfig"(){
        setup:
        PvsGlobalTagService mockPvsGlobalTagService = Mock(PvsGlobalTagService)
        PvsAlertTagService mockPvsAlertTagService = Mock(PvsAlertTagService)
        service.pvsGlobalTagService = mockPvsGlobalTagService
        service.pvsAlertTagService = mockPvsAlertTagService
        when:
        service.saveTagsForBusinessConfig(executedConfiguration.id)
        then:
        println(executedConfiguration)
    }

    void "test addProductInCache"(){
        when:
        service.addProductInCache(2)
        then:
        println(executedConfiguration)
    }
    @Ignore
    void "test batchPersistAggregateAlert"(){
        setup:
        BusinessConfigurationService mockBusinessConfigurationService = Mock(BusinessConfigurationService)
        service.businessConfigurationService = mockBusinessConfigurationService
        SignalExecutorService mockExecutorService = Mock(SignalExecutorService)
        service.signalExecutorService = mockExecutorService
        SingleCaseAlertService mockSingleCaseAlertService = Mock(SingleCaseAlertService)
        mockExecutorService.threadPoolForQuantListExec(_)>>{
            return Executors.newFixedThreadPool(8)
        }
        service.singleCaseAlertService = mockSingleCaseAlertService
        when:
        service.batchPersistAggregateAlert([aggregateCaseAlert_a], alertConfiguration, executedConfiguration)
        then:
        println(executedConfiguration)
    }

    void "test createAlert"() {
        setup:
        Map<String,List<Map>> alertData = ["pva":[],"faers":[],"eudra":[]]
        Map<String,Long> otherDataSourcesExecIds = ["pva":1,"faers":1,"eudra":1]
        PvsGlobalTagService mockPvsGlobalTagService = Mock(PvsGlobalTagService)
        service.pvsGlobalTagService = mockPvsGlobalTagService
        DataObjectService mockDataObjectService = Mock(DataObjectService)
        service.dataObjectService = mockDataObjectService
        ArchiveService mockArchiveService = Mock(ArchiveService)
        service.archiveService = mockArchiveService
        AlertService mockAlertService = Mock(AlertService)
        service.alertService = mockAlertService
        ActivityService mockActivityService = Mock(ActivityService)
        service.activityService = mockActivityService
        CacheService mockCacheService = Mock(CacheService)
        service.cacheService = mockCacheService
        SignalExecutorService executorService = Mock(SignalExecutorService)
        service.signalExecutorService = executorService
        ProductEventHistoryService mockProductEventHistoryService = Mock(ProductEventHistoryService)
        service.productEventHistoryService = mockProductEventHistoryService
        QueryService mockQueryService = Mock(QueryService)
        service.queryService = mockQueryService
        BusinessConfigurationService mockBusinessConfigurationService = Mock(BusinessConfigurationService)
        service.businessConfigurationService = mockBusinessConfigurationService
        SingleCaseAlertService mockSingleCaseAlertService = Mock(SingleCaseAlertService)
        def map = [:]
        service.singleCaseAlertService = mockSingleCaseAlertService
        service.metaClass.fetchAllPrevExecutionOfAlert = {ExecutedConfiguration ec ->
            return []
        }
        service.metaClass.persistValidatedSignalWithAggCaseAlert = {Long executedConfigId, String name ->
            return
        }
        service.metaClass.batchPersistAggregateAlert = {List alertList, Configuration config, ExecutedConfiguration executedConfig ->
            return map
        }
        Session session = Mock(Session)
        service.sessionFactory.getCurrentSession() >> session
        when:
        service.createAlert(alertConfiguration.id,executedConfiguration.id,alertData,otherDataSourcesExecIds)
        then:
        alertConfiguration.id==executedConfiguration.id
    }

    void "test persistValidatedSignalWithAggCaseAlert"() {

    }
    @Ignore
    void "test fetchResultAlertList"(){
        setup:
        List list = [aggregateCaseAlert_a]
        AlertDataDTO alertDataDTO=new AlertDataDTO(cumulative: true,domainName : AggregateCaseAlert, params: [isFaers: "true"])
        PvsGlobalTagService mockPvsGlobalTagService = Mock(PvsGlobalTagService)
        PvsAlertTagService mockPvsAlertTagService = Mock(PvsAlertTagService)
        service.pvsGlobalTagService = mockPvsGlobalTagService
        service.pvsAlertTagService = mockPvsAlertTagService
        ValidatedSignalService mockValidatedSignalService = Mock(ValidatedSignalService)
        service.validatedSignalService = mockValidatedSignalService
        AlertService mockAlertService = Mock(AlertService)
        service.alertService = mockAlertService
        AlertCommentService mockAlertCommentService = Mock(AlertCommentService)
        service.alertCommentService = mockAlertCommentService
        SignalExecutorService mockExecutorService = Mock(SignalExecutorService)
        mockExecutorService.threadPoolForQuantListExec(_)>>{
            return Executors.newFixedThreadPool(8)
        }
        println(Executors.newFixedThreadPool(8))
        service.signalExecutorService = mockExecutorService

        when:
        List result = service.fetchResultAlertList(list,alertDataDTO)
        then:
        result.size()>0
        result[0].id!=null
        result[0].globalId==12
        result[0].alertTags[0].alertId!=null
        result[0].alertTags[1].globalId==12
    }

    void "test getAlertConfigObjectFaers"(){
        when:
        def result = service.getAlertConfigObjectFaers("test",user)
        then:
        result == null
    }

    void "test getAlertConfigObjectFaers Faers"(){
        when:
        def result = service.getAlertConfigObjectFaers("test",user)
        then:
        result == null
    }

    void "test sendMailForAssignedToChange"(){
        setup:
        EmailNotificationService mockEmailNotificationService = Mock(EmailNotificationService)
        mockEmailNotificationService.mailHandlerForAssignedToACA(_)>>{
            return
        }
        service.emailNotificationService = mockEmailNotificationService
        LinkGenerator mockLinkGenerator = Mock(LinkGenerator)
        service.grailsLinkGenerator = mockLinkGenerator
        when:
        service.sendMailForAssignedToChange([], aggregateCaseAlert_a)
        then:
        EmailNotification.list().size() == 0
    }
    void "test getStratificationVlauesDataMiningVariables"(){
        setup:
        when:
        Map result = service.getStratificationVlauesDataMiningVariables(executedConfiguration.selectedDatasource, executedConfiguration.dataMiningVariable)
        then:
        result == [map_EBGM:[pva:[age:[], gender:[], receiptYear:[], ageSubGroup:[], genderSubGroup:[], isEBGM:false]], map_PRR:[pva:[age:[], gender:[], receiptYear:[], ageSubGroup:[], genderSubGroup:[], isPRR:false]]]
    }
}