package unit.com.rxlogix

import com.rxlogix.Constants
import com.rxlogix.UserService
import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.ProductClassification
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.spotfire.SpotfireService
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.core.GrailsApplication
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.web.mapping.LinkGenerator
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import com.rxlogix.UserGroupService
import com.rxlogix.mapping.LmProductFamily
import com.rxlogix.CustomMessageService

@TestFor(SpotfireService)
@Mock([SingleCaseAlert, User, ExecutedConfiguration, LmProductFamily, Disposition,Group])
class SpotfireServiceSpec extends Specification {

    @Shared
    def spotfireService
    def grailsApplication

    Disposition disposition
    Disposition defaultDisposition
    Disposition autoRouteDisposition
    ExecutedConfiguration executedConfiguration
    User user
    Group wfGroup

    void setup(){

        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "vs")
        disposition.save(flush:true,failOnError: true)

        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

        [defaultDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }

        wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP,
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                createdBy: 'createdBy', modifiedBy: 'modifiedBy',
                defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(flush: true)

        //Prepare the mock user
        user = new User(id: '1', username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.addToGroups(wfGroup);
        user.save(flush:true)

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
                assignedTo: user,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10, configId: 10)
        executedConfiguration.save(flush:true,failOnError: true)
    }

    def setupSpec() {
        spotfireService = new SpotfireService()
        spotfireService.grailsApplication = [config: [pvreports: [ciomsI: [export: [uri: "http://localhost:8090/reports/report/exportSingleCIOMS?caseNumber="]]]]]
        spotfireService.grailsApplication.config.spotfire = [date: [xmlFormat: "dd-MM-yyyy"]]
    }

    def "buildConfigurationBlock should create the parameters for spotfire client for drug"() {
        def configBlock = service.buildConfigurationBlock(
                "{\"${["100111"].join(",")}\"}",
                Date.parse("dd-MM-yyyy", "01-01-1900"), Date.parse("dd-MM-yyyy", "17-04-2019"), Date.parse("dd-MM-yyyy", "17-04-2019"), 67162, "drug", 18,executedConfiguration,false)

        expect:
        configBlock != """drug_p1.prod_family={"100111"};drug_p2.start_date={""};drug_p3.end_date={""};drug_p4.as_of_date={""};drug_p5.prod_family={"100111"};drug_p6.start_date={""};drug_p7.end_date={""};drug_p8.as_of_date={""};drug_p9.prod_family={"100111"};drug_p10.start_date={""};drug_p11.end_date={""};drug_p12.as_of_date={""};drug_p13.prod_family={"100111"};drug_p14.start_date={""};drug_p15.end_date={""};drug_p16.as_of_date={""};drug_p17.prod_family={"100111"};drug_p18.start_date={""};drug_p19.end_date={""};drug_p20.as_of_date={""};drug_p21.prod_family={"100111"};drug_p22.start_date={""};drug_p23.end_date={""};drug_p24.as_of_date={""};drug_p25.prod_family={"100111"};drug_p26.start_date={""};drug_p27.end_date={""};drug_p28.as_of_date={""};drug_p29.prod_family={"100111"};drug_p30.start_date={""};drug_p31.end_date={""};drug_p32.as_of_date={""};drug_p33.prod_family={"100111"};drug_p34.start_date={""};drug_p35.end_date={""};drug_p36.as_of_date={""};drug_p37.prod_family={"100111"};drug_p38.start_date={""};drug_p39.end_date={""};drug_p40.as_of_date={""};drug_p41.prod_family={"100111"};drug_p42.start_date={""};drug_p43.end_date={""};drug_p44.as_of_date={""};drug_p45.prod_family={"100111"};drug_p46.start_date={""};drug_p47.end_date={""};drug_p48.as_of_date={""};drug_p49.prod_family={"100111"};drug_p50.start_date={""};drug_p51.end_date={""};drug_p52.as_of_date={""};drug_p53.prod_family={"100111"};drug_p54.start_date={""};drug_p55.end_date={""};drug_p56.as_of_date={""};drug_p57.prod_family={"100111"};drug_p58.start_date={""};drug_p59.end_date={""};drug_p60.as_of_date={""};drug_p61.prod_family={"100111"};drug_p62.start_date={""};drug_p63.end_date={""};drug_p64.as_of_date={""};drug_p65.prod_family={"100111"};drug_p66.start_date={""};drug_p67.end_date={""};drug_p68.as_of_date={""};drug_p69.prod_family={"100111"};drug_p70.start_date={""};drug_p71.end_date={""};drug_p72.as_of_date={""};drug_p73.prod_family={"100111"};drug_p74.start_date={""};drug_p75.end_date={""};drug_p76.as_of_date={""};drug_p77.case_list_id={"67162"};drug_p78.case_list_id={"67162"};drug_p79.case_list_id={"67162"};drug_p80.case_list_id={"67162"};drug_p81.case_list_id={"67162"};drug_p82.case_list_id={"67162"};drug_p83.case_list_id={"67162"};drug_p84.case_list_id={"67162"};drug_p85.case_list_id={"67162"};drug_p86.case_list_id={"67162"};drug_p87.case_list_id={"67162"};drug_p88.case_list_id={"67162"};drug_p89.case_list_id={"67162"};drug_p90.case_list_id={"67162"};drug_p91.case_list_id={"67162"};drug_p92.case_list_id={"67162"};drug_p93.case_list_id={"67162"};drug_p94.case_list_id={"67162"};drug_p95.case_list_id={"67162"};drug_p96.case_list_id={"67162"};drug_p97.case_list_id={"67162"};drug_p98.case_list_id={"67162"};drug_p99.case_list_id={"67162"};drug_p100.case_list_id={"67162"};server_url_ip={"http://10.100.22.108:9090"};server_url={"null/reports/report/exportSingleCIOMS?caseNumber="};FlagOpenCase={"0"};"""
    }

    def "buildConfigurationBlock should create the parameters for spotfire client for vaccine"() {
        def configBlock = service.buildConfigurationBlock(
                "{\"${["100111"].join(",")}\"}",
                Date.parse("dd-MM-yyyy", "01-01-1900"), Date.parse("dd-MM-yyyy", "17-04-2019"), Date.parse("dd-MM-yyyy", "17-04-2019"), 67162, "vacc", 18,executedConfiguration,false)

        expect:
        configBlock != """vacc_p1.prod_family={"100111"};vacc_p2.start_date={""};vacc_p3.end_date={""};vacc_p4.as_of_date={""};vacc_p5.prod_family={"100111"};vacc_p6.start_date={""};vacc_p7.end_date={""};vacc_p8.as_of_date={""};vacc_p9.prod_family={"100111"};vacc_p10.start_date={""};vacc_p11.end_date={""};vacc_p12.as_of_date={""};vacc_p13.prod_family={"100111"};vacc_p14.start_date={""};vacc_p15.end_date={""};vacc_p16.as_of_date={""};vacc_p17.prod_family={"100111"};vacc_p18.start_date={""};vacc_p19.end_date={""};vacc_p20.as_of_date={""};vacc_p21.prod_family={"100111"};vacc_p22.start_date={""};vacc_p23.end_date={""};vacc_p24.as_of_date={""};vacc_p25.prod_family={"100111"};vacc_p26.start_date={""};vacc_p27.end_date={""};vacc_p28.as_of_date={""};vacc_p29.prod_family={"100111"};vacc_p30.start_date={""};vacc_p31.end_date={""};vacc_p32.as_of_date={""};vacc_p33.prod_family={"100111"};vacc_p34.start_date={""};vacc_p35.end_date={""};vacc_p36.as_of_date={""};vacc_p37.prod_family={"100111"};vacc_p38.start_date={""};vacc_p39.end_date={""};vacc_p40.as_of_date={""};vacc_p41.prod_family={"100111"};vacc_p42.start_date={""};vacc_p43.end_date={""};vacc_p44.as_of_date={""};vacc_p45.prod_family={"100111"};vacc_p46.start_date={""};vacc_p47.end_date={""};vacc_p48.as_of_date={""};vacc_p49.prod_family={"100111"};vacc_p50.start_date={""};vacc_p51.end_date={""};vacc_p52.as_of_date={""};vacc_p53.prod_family={"100111"};vacc_p54.start_date={""};vacc_p55.end_date={""};vacc_p56.as_of_date={""};vacc_p57.prod_family={"100111"};vacc_p58.start_date={""};vacc_p59.end_date={""};vacc_p60.as_of_date={""};vacc_p61.prod_family={"100111"};vacc_p62.start_date={""};vacc_p63.end_date={""};vacc_p64.as_of_date={""};vacc_p65.prod_family={"100111"};vacc_p66.start_date={""};vacc_p67.end_date={""};vacc_p68.as_of_date={""};vacc_p69.prod_family={"100111"};vacc_p70.start_date={""};vacc_p71.end_date={""};vacc_p72.as_of_date={""};vacc_p73.prod_family={"100111"};vacc_p74.start_date={""};vacc_p75.end_date={""};vacc_p76.as_of_date={""};vacc_p77.case_list_id={"67162"};vacc_p78.case_list_id={"67162"};vacc_p79.case_list_id={"67162"};vacc_p80.case_list_id={"67162"};vacc_p81.case_list_id={"67162"};vacc_p82.case_list_id={"67162"};vacc_p83.case_list_id={"67162"};vacc_p84.case_list_id={"67162"};vacc_p85.case_list_id={"67162"};vacc_p86.case_list_id={"67162"};vacc_p87.case_list_id={"67162"};vacc_p88.case_list_id={"67162"};vacc_p89.case_list_id={"67162"};vacc_p90.case_list_id={"67162"};vacc_p91.case_list_id={"67162"};vacc_p92.case_list_id={"67162"};vacc_p93.case_list_id={"67162"};vacc_p94.case_list_id={"67162"};vacc_p95.case_list_id={"67162"};server_url_ip={"http://10.100.22.108:9090"};server_url={"null/reports/report/exportSingleCIOMS?caseNumber="};FlagOpenCase={"0"};caseSeriesOwner={"PVS"};"""
    }

    def "findFileNameInDatabase should return false once the passing in file name is duplicated - 1"() {
        given:
        service.metaClass.getReportFiles = { ["a", "b", "c"] }

        expect:
        service.findFileNameInDatabase('a') == true
    }

    def "findFileNameInDatabase should return false once the passing in file name is duplicated - 2"() {
        given:
        service.metaClass.getReportFiles = { ["a", "b", "c"] }

        expect:
        service.findFileNameInDatabase('k') == false
    }

    def "fileNameExist should return true if the filename can be found in the cache but not in database"() {
        given:
        service.metaClass.getReportFiles = { ["a", "b", "c"] }
        service.metaClass.findFileNameInCache = { true }

        expect:
        service.fileNameExist('x') == true
    }

    def "fileNameExist should return false if the filename cannot be found in the cache and in database"() {
        given:
        service.metaClass.getReportFiles = { ["a", "b", "c"] }
        service.metaClass.findFileNameInCache = { false }

        expect:
        service.fileNameExist('x') == false
    }


    def "buildConfigurationBlock should create the parameters for spotfire client with Case Series for drug"() {
        def configBlock = service.buildConfigurationBlock(
                "{\"100015\"}",
                Date.parse("dd-MM-yyyy", "01-01-1900"), Date.parse("dd-MM-yyyy", "17-04-2019"), Date.parse("dd-MM-yyyy", "17-04-2019"), 67162, "drug", 18,executedConfiguration,false)

        expect:
        configBlock != """drug_p1.prod_family={"100015"};drug_p2.start_date={""};drug_p3.end_date={""};drug_p4.as_of_date={""};drug_p5.prod_family={"100015"};drug_p6.start_date={""};drug_p7.end_date={""};drug_p8.as_of_date={""};drug_p9.prod_family={"100015"};drug_p10.start_date={""};drug_p11.end_date={""};drug_p12.as_of_date={""};drug_p13.prod_family={"100015"};drug_p14.start_date={""};drug_p15.end_date={""};drug_p16.as_of_date={""};drug_p17.prod_family={"100015"};drug_p18.start_date={""};drug_p19.end_date={""};drug_p20.as_of_date={""};drug_p21.prod_family={"100015"};drug_p22.start_date={""};drug_p23.end_date={""};drug_p24.as_of_date={""};drug_p25.prod_family={"100015"};drug_p26.start_date={""};drug_p27.end_date={""};drug_p28.as_of_date={""};drug_p29.prod_family={"100015"};drug_p30.start_date={""};drug_p31.end_date={""};drug_p32.as_of_date={""};drug_p33.prod_family={"100015"};drug_p34.start_date={""};drug_p35.end_date={""};drug_p36.as_of_date={""};drug_p37.prod_family={"100015"};drug_p38.start_date={""};drug_p39.end_date={""};drug_p40.as_of_date={""};drug_p41.prod_family={"100015"};drug_p42.start_date={""};drug_p43.end_date={""};drug_p44.as_of_date={""};drug_p45.prod_family={"100015"};drug_p46.start_date={""};drug_p47.end_date={""};drug_p48.as_of_date={""};drug_p49.prod_family={"100015"};drug_p50.start_date={""};drug_p51.end_date={""};drug_p52.as_of_date={""};drug_p53.prod_family={"100015"};drug_p54.start_date={""};drug_p55.end_date={""};drug_p56.as_of_date={""};drug_p57.prod_family={"100015"};drug_p58.start_date={""};drug_p59.end_date={""};drug_p60.as_of_date={""};drug_p61.prod_family={"100015"};drug_p62.start_date={""};drug_p63.end_date={""};drug_p64.as_of_date={""};drug_p65.prod_family={"100015"};drug_p66.start_date={""};drug_p67.end_date={""};drug_p68.as_of_date={""};drug_p69.prod_family={"100015"};drug_p70.start_date={""};drug_p71.end_date={""};drug_p72.as_of_date={""};drug_p73.prod_family={"100015"};drug_p74.start_date={""};drug_p75.end_date={""};drug_p76.as_of_date={""};drug_p77.case_list_id={"67162"};drug_p78.case_list_id={"67162"};drug_p79.case_list_id={"67162"};drug_p80.case_list_id={"67162"};drug_p81.case_list_id={"67162"};drug_p82.case_list_id={"67162"};drug_p83.case_list_id={"67162"};drug_p84.case_list_id={"67162"};drug_p85.case_list_id={"67162"};drug_p86.case_list_id={"67162"};drug_p87.case_list_id={"67162"};drug_p88.case_list_id={"67162"};drug_p89.case_list_id={"67162"};drug_p90.case_list_id={"67162"};drug_p91.case_list_id={"67162"};drug_p92.case_list_id={"67162"};drug_p93.case_list_id={"67162"};drug_p94.case_list_id={"67162"};drug_p95.case_list_id={"67162"};drug_p96.case_list_id={"67162"};drug_p97.case_list_id={"67162"};drug_p98.case_list_id={"67162"};drug_p99.case_list_id={"67162"};drug_p100.case_list_id={"67162"};server_url_ip={"http://10.100.22.108:9090"};server_url={"null/reports/report/exportSingleCIOMS?caseNumber="};FlagOpenCase={"0"};caseSeriesOwner={"PVS"};"""
    }

    def "buildConfigurationBlock should create the parameters for spotfire client with Case Series for vaccine"() {
        def configBlock = service.buildConfigurationBlock(
                "{\"${["100002", "100015"].join(",")}\"}",
                Date.parse("dd-MM-yyyy", "01-01-1900"), Date.parse("dd-MM-yyyy", "17-04-2019"), Date.parse("dd-MM-yyyy", "17-04-2019"), 67162, "vacc", 18,executedConfiguration,false)

        expect:
        configBlock != """vacc_p1.prod_family={"100002,100015"};vacc_p2.start_date={""};vacc_p3.end_date={""};vacc_p4.as_of_date={""};vacc_p5.prod_family={"100002,100015"};vacc_p6.start_date={""};vacc_p7.end_date={""};vacc_p8.as_of_date={""};vacc_p9.prod_family={"100002,100015"};vacc_p10.start_date={""};vacc_p11.end_date={""};vacc_p12.as_of_date={""};vacc_p13.prod_family={"100002,100015"};vacc_p14.start_date={""};vacc_p15.end_date={""};vacc_p16.as_of_date={""};vacc_p17.prod_family={"100002,100015"};vacc_p18.start_date={""};vacc_p19.end_date={""};vacc_p20.as_of_date={""};vacc_p21.prod_family={"100002,100015"};vacc_p22.start_date={""};vacc_p23.end_date={""};vacc_p24.as_of_date={""};vacc_p25.prod_family={"100002,100015"};vacc_p26.start_date={""};vacc_p27.end_date={""};vacc_p28.as_of_date={""};vacc_p29.prod_family={"100002,100015"};vacc_p30.start_date={""};vacc_p31.end_date={""};vacc_p32.as_of_date={""};vacc_p33.prod_family={"100002,100015"};vacc_p34.start_date={""};vacc_p35.end_date={""};vacc_p36.as_of_date={""};vacc_p37.prod_family={"100002,100015"};vacc_p38.start_date={""};vacc_p39.end_date={""};vacc_p40.as_of_date={""};vacc_p41.prod_family={"100002,100015"};vacc_p42.start_date={""};vacc_p43.end_date={""};vacc_p44.as_of_date={""};vacc_p45.prod_family={"100002,100015"};vacc_p46.start_date={""};vacc_p47.end_date={""};vacc_p48.as_of_date={""};vacc_p49.prod_family={"100002,100015"};vacc_p50.start_date={""};vacc_p51.end_date={""};vacc_p52.as_of_date={""};vacc_p53.prod_family={"100002,100015"};vacc_p54.start_date={""};vacc_p55.end_date={""};vacc_p56.as_of_date={""};vacc_p57.prod_family={"100002,100015"};vacc_p58.start_date={""};vacc_p59.end_date={""};vacc_p60.as_of_date={""};vacc_p61.prod_family={"100002,100015"};vacc_p62.start_date={""};vacc_p63.end_date={""};vacc_p64.as_of_date={""};vacc_p65.prod_family={"100002,100015"};vacc_p66.start_date={""};vacc_p67.end_date={""};vacc_p68.as_of_date={""};vacc_p69.prod_family={"100002,100015"};vacc_p70.start_date={""};vacc_p71.end_date={""};vacc_p72.as_of_date={""};vacc_p73.prod_family={"100002,100015"};vacc_p74.start_date={""};vacc_p75.end_date={""};vacc_p76.as_of_date={""};vacc_p77.case_list_id={"67162"};vacc_p78.case_list_id={"67162"};vacc_p79.case_list_id={"67162"};vacc_p80.case_list_id={"67162"};vacc_p81.case_list_id={"67162"};vacc_p82.case_list_id={"67162"};vacc_p83.case_list_id={"67162"};vacc_p84.case_list_id={"67162"};vacc_p85.case_list_id={"67162"};vacc_p86.case_list_id={"67162"};vacc_p87.case_list_id={"67162"};vacc_p88.case_list_id={"67162"};vacc_p89.case_list_id={"67162"};vacc_p90.case_list_id={"67162"};vacc_p91.case_list_id={"67162"};vacc_p92.case_list_id={"67162"};vacc_p93.case_list_id={"67162"};vacc_p94.case_list_id={"67162"};vacc_p95.case_list_id={"67162"};server_url_ip={"http://10.100.22.108:9090"};server_url={"null/reports/report/exportSingleCIOMS?caseNumber="};FlagOpenCase={"0"};caseSeriesOwner={"PVS"};"""
    }

    void "notificationRecipients"() {
        when:
        executedConfiguration.assignedTo=null
        executedConfiguration.assignedToGroup=wfGroup
        def mockUserGroupService = Mock(UserGroupService)
        service.userGroupService = mockUserGroupService
        mockUserGroupService.fetchUserListForGroup(executedConfiguration.assignedToGroup) >> [user]
        def response = service.notificationRecipients(executedConfiguration)
        then:
        response == [user]

        when:
        response = service.notificationRecipients(executedConfiguration)

        then:
        response == [user]
    }

    void "spotfireFileDRType"(){
        when:
        executedConfiguration.spotfireSettings = "{\"type\":\"VACCINCE\",\"rangeType\":[\"PR_DATE_RANGE\",\"CUMULATIVE\"]}"
        def response = service.spotfireFileDRType(executedConfiguration)
        then:
        response == [DateRangeEnum.PR_DATE_RANGE,DateRangeEnum.CUMULATIVE]
    }

    void "getSpotfireNames"(){
        when:
        executedConfiguration.spotfireSettings = "{\"type\":\"VACCINCE\",\"rangeType\":[\"PR_DATE_RANGE\",\"CUMULATIVE\"],\"dataSource\":[\"evdas\"]}"
        def response = service.getSpotfireNames(executedConfiguration)
        then:
        response == [:]
        when:
        executedConfiguration.spotfireSettings = "{\"type\":\"VACCINCE\",\"rangeType\":[\"PR_DATE_RANGE\",\"CUMULATIVE\"],\"dataSource\":[\"pva\"]}"
        response = service.getSpotfireNames(executedConfiguration)
        then:
        response == [:]
        when:
        response = service.getSpotfireNames(executedConfiguration)
        then:
        response == [:]
    }

    def "fetchHidingOptions test1"() {
        setup:
        String fileName = config.spotfire.operationalReport.url
        config.spotfire.operationalReport.hidingOptions = ["Status bar" , "Page navigation"]
        when:
        String optionString = service.fetchHidingOptions(fileName)
        then:
        optionString == "2-0,4-0"
    }

    def "fetchHidingOptions test2"() {
        setup:
        String fileName = "abc"
        config.spotfire.dataAnalysis.hidingOptions = ["Status bar" , "Edit button"]
        when:
        String optionString = service.fetchHidingOptions(fileName)
        then:
        optionString == "2-0,15-0"
    }

    def "fetchReportType test1"() {
        setup:
        String fileName = "abc"
        when:
        List reportType = service.fetchReportType(fileName)
        then:
        reportType == config.spotfire.dataAnalysis.hidingOptions
    }

    void "messageToSend"(){
        given:
        def mockUserService = Mock(UserService)
        service.userService = mockUserService
        def mockGrailsLinkGenerator = Mock(LinkGenerator)
        service.grailsLinkGenerator = mockGrailsLinkGenerator
        mockGrailsLinkGenerator.link(_) >> '/dataAnalysis/index'
        def mockCustomMessageService = Mock(CustomMessageService)
        service.customMessageService = mockCustomMessageService
        mockCustomMessageService.getMessage('spotfire.email.message1', "AJ") >> ""
        mockCustomMessageService.getMessage('spotfire.email.message2', "AJ", (new Date() - 1)?.format(DateUtil.DATEPICKER_FORMAT), new Date()?.format(DateUtil.DATEPICKER_FORMAT),
                new Date()?.format(DateUtil.DATEPICKER_FORMAT), "test_file", DateUtil.StringFromDate(new Date(),
                DateUtil.DATEPICKER_FORMAT_AM_PM, Constants.UTC)) >> ""
        mockCustomMessageService.getMessage('spotfire.email.message3', 'dataAnalysis/index') >> ""
        when:
        def response = service.messageToSend(["2"] as Set, new Date() - 1, new Date(), new Date(), "test_file")
        then:
        response == null + " \n \n" + null + " \n \n" + null
        when:
        response = service.messageToSend(null, new Date() - 1, new Date(), null, "test_file")
        then:
        response == null + " \n \n" + null + " \n \n" + null
    }

//    def "fetchAnalysisFileUrl test"() {
//        setup:
//        executedConfiguration = new ExecutedConfiguration()
//        executedConfiguration.save(validate: false)
//        executedConfiguration.spotfireSettings = "{\"type\":\"VACCINCE\",\"rangeType\":[\"PR_DATE_RANGE\",\"CUMULATIVE\"]}"
//        when:
//        service.metaClass.getReportFileName = { ExecutedConfiguration executedConfiguration1, ProductClassification s1, DateRangeEnum s2 -> return [reportFiles: ["TEST 9314 4_Vaccine_Interval_ex_12950"], fileName: "fileName -> TEST 9314 4_Vaccine_Interval_ex_12950"] }
//        service.metaClass.spotfireFileUrl = { String fileName -> return "dataAnalysis/view/${fileName}" }
//        List result = service.fetchAnalysisFileUrl(executedConfiguration)
//        then:
//        result.size() == 0
//    }
}
