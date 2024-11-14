package com.rxlogix.signal

import com.rxlogix.CRUDService
import com.rxlogix.ConfigurationService
import com.rxlogix.Constants
import com.rxlogix.DynamicReportService
import com.rxlogix.UserService
import com.rxlogix.config.AlertDateRangeInformation
import com.rxlogix.config.Category
import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.config.Priority
import com.rxlogix.config.ReportTemplate
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.ImportConfigurationProcessState
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.transform.SourceURI
import org.grails.plugins.testing.GrailsMockMultipartFile
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(ImportConfigurationController)
@Mock([User, Group, UserService, Disposition, Configuration, AlertDateRangeInformation, Priority, ReportTemplate, Category, ImportConfigurationLog])
class ImportConfigurationControllerSpec extends Specification {

    User user
    Group wfGroup
    UserService userService
    Disposition disposition
    Configuration alertConfiguration
    Priority priority1
    CRUDService crudService
    File file
    String directory
    ImportConfigurationLog importConfigurationLog
    ImportConfigurationService importConfigurationService
    DynamicReportService dynamicReportServiceMocked


    def setup() {
        disposition = new Disposition(value: "ValidatedSignal1", displayName: "Validated Signal1", validatedConfirmed: true,
                abbreviation: "C")
        disposition.save(failOnError: true)

        wfGroup = new Group(name: "Default1", groupType: GroupType.WORKFLOW_GROUP, createdBy: 'createdBy',
                modifiedBy: 'modifiedBy',
                defaultQualiDisposition: disposition,
                defaultQuantDisposition: disposition,
                defaultAdhocDisposition: disposition,
                defaultEvdasDisposition: disposition,
                defaultLitDisposition: disposition,
                defaultSignalDisposition: disposition)

        user = new User(id: '1', username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.timeZone = "UTC"
        user.groups = [wfGroup]
        user.preference.locale = Locale.ENGLISH
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(failOnError: true)

        AlertDateRangeInformation alertDateRangeInformation1 = new AlertDateRangeInformation(dateRangeEndAbsolute: new Date() + 4, dateRangeStartAbsolute: new Date(),
                dateRangeEndAbsoluteDelta: 13, dateRangeStartAbsoluteDelta: 10, dateRangeEnum: DateRangeEnum.CUSTOM)
        Category category1 = new Category(name: "category1")
        category1.save(flush: true, failOnError: true)
        ReportTemplate reportTemplate1 = new ReportTemplate(name: "repTemp1", description: "repDesc1", category: category1,
                owner: user, templateType: TemplateTypeEnum.TEMPLATE_SET, dateCreated: new Date(), lastUpdated: new Date() + 4,
                createdBy: "username", modifiedBy: "username")
        reportTemplate1.save(flush: true, failOnError: true)
        priority1 = new Priority(value: "Low", display: true, displayName: "Low", reviewPeriod: 3, priorityOrder: 1)
        priority1.save(flush: true, failOnError: true)

        alertConfiguration = new Configuration(
                type: Constants.AlertConfigType.SINGLE_CASE_ALERT,
                productSelection: '{"3":[{"name":"product1"}]}',
                executing: false,
                template: reportTemplate1,
                priority: priority1,
                alertTriggerCases: "11",
                alertTriggerDays: "11",
                dateCreated: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                name: "test",
                assignedTo: user,
                createdBy: "username",
                modifiedBy: "username",
                owner: user,
                adhocRun: true,
                alertQueryId: 1L,
                alertQueryName: "AlertQuery1",
                alertDateRangeInformation: alertDateRangeInformation1,
                isTemplateAlert: true,
                isDeleted: false
        )
        alertConfiguration.metaClass.getProductType = { 'family' }
        alertConfiguration.save(flush: true, failOnError: true)

        importConfigurationLog = new ImportConfigurationLog(importFileName: "1614580907697.xlsx" , importedBy: user, importedDate: new Date(), status: ImportConfigurationProcessState.IN_READ, importConfType: Constants.AlertConfigType.SINGLE_CASE_ALERT)
        importConfigurationLog.save(validate:false)

        userService = Mock(UserService)
        userService.getUser() >> {
            return user
        }
        userService.assignGroupOrAssignTo(_,_,_)>>{
            return alertConfiguration
        }
        userService.user >>{
            return user
        }
        userService.bindSharedWithConfiguration(_, _, _, _) >> null
        controller.userService = userService

        crudService = Mock(CRUDService)
        crudService.save(_) >> alertConfiguration
        controller.CRUDService = crudService

        importConfigurationService = Mock(ImportConfigurationService)
        importConfigurationService.fetchAlertListByType(_,_)>>{
            return [aaData: [], recordsTotal: 0, recordsFiltered: 0]
        }
        importConfigurationService.updateScheduleDateAndNextRunDate(_,_)>>{

        }
        importConfigurationService.getSortedListData(_)>>{
            return []
        }
        controller.importConfigurationService = importConfigurationService
        @SourceURI
        URI sourceUri
        Path scriptLocation = Paths.get(sourceUri)
        directory = scriptLocation.toString().replace("ImportConfigurationServiceSpec.groovy", "testingFiles/1614580907697.xlsx")
        file=new File(directory)

        dynamicReportServiceMocked = Mock(DynamicReportService)
        dynamicReportServiceMocked.exportToExcelImportConfigurationList(_,_) >> {
            return file
        }
        controller.dynamicReportService = dynamicReportServiceMocked
    }

    def cleanup() {
    }

    void "test fetchAlertList"() {
        when:
        controller.fetchAlertList(Constants.AlertConfigType.SINGLE_CASE_ALERT)
        then:
        response.status == 200
    }
    void "test fetchAlertTemplateByType"(){
        when:
        def result = controller.fetchAlertTemplateByType(Constants.AlertConfigType.SINGLE_CASE_ALERT)
        then:
        response.status == 200
    }
    void "test createAlertFromTemplate"(){
        when:
        controller.createAlertFromTemplate(Constants.AlertConfigType.SINGLE_CASE_ALERT, 1L)
        then:
        response .status == 200
    }
    void "test editAlertName"(){
        when:
        controller.editAlertName("alertName", 1L)
        then:
        response.json.code == 200
        response.json.message == "Alert name Changed successfully"
    }
    void "test updateDateRangeForAlert"(){
        setup:
        controller.params.id = 1L
        controller.params.dateRangeEnum = DateRangeEnum.CUSTOM.name()
        when:
        controller.updateDateRangeForAlert()
        then:
        response.json.status == true
        response.json.message == "Alert Date Range updated successfully"
    }
    void "test updateDateRangeForAlert fail"(){
        when:
        controller.updateDateRangeForAlert()
        then:
        response.json.status == false
        response.json.message == "Alert does not exists."
    }
    void "test updateScheduleDateJSON"(){
        setup:
        controller.params.id = 1L
        when:
        controller.updateScheduleDateJSON()
        then:
        response.json.status == true
        response.json.message == "Alert updated successfully"
    }
    void "test updateScheduleDateJSON fail"(){
        when:
        controller.updateScheduleDateJSON()
        then:
        response.json.status == false
        response.json.message == "Alert does not exists."
    }
    void "test unScheduleAlert"(){
        setup:
        controller.params.id = 1L
        when:
        controller.unScheduleAlert()
        then:
        response.json.status == true
        response.json.message == "Alert Unscheduled Successfully!"
    }
    void "test unScheduleAlert fail"(){
        when:
        controller.unScheduleAlert()
        then:
        response.json.status == false
        response.json.message == "Alert does not exists."
    }
    void "test changeAssignedToGroup"(){
        when:
        controller.changeAssignedToGroup(1L, "assignToValue")
        then:
        response.json.status == true
    }
    void "test updateShareWithForConf Exception"(){
        when:
        controller.updateShareWithForConf(1L)
        then:
        response.json.status == false
    }
    void "test uploadFile incorrect file format"(){
        given:
        def file = new GrailsMockMultipartFile('file', 'someData'.bytes)
        request.addFile(file)
        when:
        controller.uploadFile()
        then:
        response.json.status == false
        response.json.message == "app.label.Configuration.upload.file.format.incorrect"
    }
    void "test uploadFile"(){
        given:
        importConfigurationService.checkFileFormat(_) >> {
            return true
        }
        importConfigurationService.saveImportConfigurationLog(_,_,_)>>{
            return importConfigurationLog
        }
        importConfigurationService.createDir(_)>>{

        }
        importConfigurationService.copyFile(_,_)>>{

        }
        controller.importConfigurationService = importConfigurationService
        def file = new GrailsMockMultipartFile('file', 'someData'.bytes)
        request.addFile(file)
        when:
        controller.uploadFile()
        then:
        response.json.status == true
        response.json.message == "app.label.Configuration.upload.inprogress"
    }
    void "test fetchImportConfigurationLog"(){
        when:
        controller.fetchImportConfigurationLog()
        then:
        response.json.importLogList.size() == 1
    }
    void "test renderImportConfigurationOutputType"(){
        when:
        controller.renderImportConfigurationOutputType(file, "1614580907697")
        then:
        response.contentType == "groovy charset=UTF-8"
    }
    void "test deleteAlertConfig failure"(){
        given:
        controller.params.id = 1L
        when:
        controller.deleteAlertConfig()
        then:
        response.json.status == false
        response.json.message == "app.common.error"
    }
    void "test deleteAlertConfig"(){
        given:
        ConfigurationService configurationService = Mock(ConfigurationService)
        configurationService.deleteConfig(_) >>{
            return alertConfiguration
        }
        controller.configurationService = configurationService
        controller.params.id = 1L
        user.metaClass.isAdmin = { true }
        when:
        controller.deleteAlertConfig()
        then:
        response.json.status == true
        response.json.message == "app.configuration.delete.success"
    }
    void "test updateProdSelection"(){
        given:
        controller.params.alertId = 1L
        controller.params.productSelection = "{}"
        when:
        controller.updateProdSelection()
        then:
        response.status == 200
        response.json.message == "app.configuration.update.success"
    }

}
