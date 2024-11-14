package com.rxlogix

import com.rxlogix.config.Disposition
import com.rxlogix.config.EvdasAlert
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.config.AlertType
import com.rxlogix.config.EVDASDateRangeInformation
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.util.DateUtil
import grails.validation.ValidationException
import grails.plugin.springsecurity.SpringSecurityUtils
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.enums.GroupType
import com.rxlogix.user.Group
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.User
import com.rxlogix.config.Priority
import grails.test.mixin.TestMixin
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Ignore
import spock.lang.Unroll
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.*

import javax.xml.bind.ValidationException
import java.text.ParseException

@TestMixin(GrailsUnitTestMixin)
@TestFor(EvdasAlertController)
@Mock([Priority,User,ValidatedSignal,Group,EvdasConfiguration,ExecutedEvdasConfiguration])
class EvdasAlertControllerSpec extends Specification {

    def setup() {
        Priority priority=new Priority( value:'testValue',displayName:'testDisplayName',reviewPeriod:2).save(failOnError:true)
        Group group1=new Group(name: 'testGroup',modifiedBy: 'user',createdBy: 'user',groupType:GroupType.WORKFLOW_GROUP, defaultSignalDisposition :Mock(Disposition)).save(failOnError: false)
        User user=new User(username:'test',fullName:'testFullName', createdBy:'user',modifiedBy:'user',groups:[group1]).save(failOnError: true)
        ValidatedSignal validatedSignal=new ValidatedSignal(products:'testProduct',name:'testName',priority:priority,workflowGroup:group1,assignmentType:'testAssignmentType',modifiedBy:'user',createdBy:'user').save(validate: false)
        EvdasConfiguration evdasConfiguration=new EvdasConfiguration(owner:user,name:'testName',priority:priority,createdBy:'user',modifiedBy:'user').save(validate: false)
        EvdasConfiguration evdasConfiguration1=new EvdasConfiguration(owner:user,name:'testName',priority:priority,createdBy:'user',modifiedBy:'user',nextRunDate:new Date(2020,06,02)).save(validate: false)
    }

    def cleanup() {
    }

    void "test index action "() {

        when:"call index action"
            controller.index()
        then:"It renders review view"
            response.status==200
            view=='/evdasAlert/review'
    }

    @Unroll
    void "test create action"(){
        given:
            def mockEvdasAlertService=Mock(EvdasAlertService)
            mockEvdasAlertService.checkProductExistsForEvdas(_,_)>>{
                true
            }
            controller.evdasAlertService=mockEvdasAlertService
        when:"call create "
            params.signalId=signalIdValue
            controller.create()
        then:"It renders create view"
            response.status==200
            view=='/evdasAlert/create'
            model.action==Constants.AlertActions.CREATE
            model.userList[0].fullName=='testFullName'
            model.priorityList[0].displayName=='testDisplayName'
            model.configurationInstance!=null
            model.signalId==signalIdValue
        where:
            signalIdValue<<[null,'1']

    }
    @Unroll
    void "test save --Success"(){
        given:
            Group group1=new Group(name: 'testGroup',modifiedBy: 'user',createdBy: 'user',groupType:GroupType.WORKFLOW_GROUP)
            Priority priority2=new Priority( value:'testValue2',displayName:'testDisplayName2',reviewPeriod:2)
            EvdasConfiguration evdasConfiguration1=new EvdasConfiguration(name:'newTestName',priority:priority2,createdBy:'user',modifiedBy:'user')
            def mockUserService=Mock(UserService)
            2*mockUserService.getUser() >> {
                User user = new User(username:'test',fullName:'testFullName',groups: [group1])
                return user
            }
            mockUserService.bindSharedWithConfiguration(_,_,_)>>{

            }
            mockUserService.assignGroupOrAssignTo(_,_)>>{
                evdasConfiguration1
            }
            controller.userService=mockUserService
            def mockCRUDService=Mock(CRUDService)
            mockCRUDService.save(_)>>{
                return evdasConfiguration1
            }
            controller.CRUDService=mockCRUDService
        when:"call save method"
            params.id=idValue
            params.name='testName'
            params.priority=priority2
            params.productGroupSelection='{["name":"product group","id":1]}'
            params.eventGroupSelection='{["name":"event group","id":1]}'
            params.createdBy='user'
            params.modifiedBy='user'
            params.sharedWith=null
            params.signalId=null
            controller.save()
        then:"It redirects to view action"
            response.status==302
            response.redirectedUrl=='/evdasAlert/view'

        where:
        idValue<<[null,1]

    }
    @Unroll
    void "test save --Failed"(){
        given:
            Group group1=new Group(name: 'testGroup',modifiedBy: 'user',createdBy: 'user',groupType:GroupType.WORKFLOW_GROUP)
            Priority priority2=new Priority( value:'testValue2',displayName:'testDisplayName2',reviewPeriod:2)
            EvdasConfiguration evdasConfiguration1=new EvdasConfiguration(name:'newTestName',priority:priority2,createdBy:'user',modifiedBy:'user')
            def mockUserService=Mock(UserService)
            2*mockUserService.getUser() >> {
                User user = new User(username:'test',fullName:'testFullName',groups: [group1])
                return user
            }
            mockUserService.bindSharedWithConfiguration(_,_,_)>>{

            }
            mockUserService.assignGroupOrAssignTo(_,_)>>{
                evdasConfiguration1
            }
            controller.userService=mockUserService
            def mockCRUDService=Mock(CRUDService)
            mockCRUDService.save(_)>>{
                throw new ValidationException("validation failed")
            }
            controller.CRUDService=mockCRUDService
        when:
            params.id=1
            params.productGroupSelection='{["name":"product group","id":1]}'
            params.eventGroupSelection='{["name":"event group","id":1]}'
            params.name='testName'
            params.priority=priority2
            params.createdBy='user'
            params.modifiedBy='user'
            params.sharedWith=null
            params.signalId=null
            params.repeatExecution=repeatExecutionValue
            params.action=actionValue
            params.dateRangeEnum=DateRangeEnum.CUSTOM.name()
            params.dateRangeStartAbsolute=dateValue
            params.dateRangeEndAbsolute=dateValue
            controller.save()
        then:
            response.status==200
            view=='/evdasAlert/'+resultValue
            model.userList[0].fullName=='testFullName'
            model.priorityList[0].displayName=='testDisplayName'
            model.configurationInstance!=null
            model.startDateAbsoluteCustomFreq==null

        where:
            repeatExecutionValue | resultValue | actionValue | dateValue
            true                 | 'create'    | 'copy'      | null
            false                | 'create'    | 'copy'      | null
            true                 | 'edit'      | ''          | null
            false                | 'edit'      | ''          | null


    }

    void "test view,When EvdasConfiguration is not found "(){
        given:
            EvdasConfiguration evdasConfiguration=null
        when:
            controller.view(evdasConfiguration)
        then:
            response.status==302
            response.redirectedUrl=='/evdasAlert/index'

    }

    void "test view,When EvdasConfiguration found "(){
       given:
            User user1=new User()
            Priority priority1=new Priority()
            EvdasConfiguration evdasConfiguration=new EvdasConfiguration(owner:user1,name:'testName',priority:priority1,createdBy:'user',modifiedBy:'user')
            def mockUserService=Mock(UserService)
            mockUserService.getAssignedToName(_)>>{
            return 'assignedToUserName'
            }
            mockUserService.getCurrentUserId() >>{
                user1.id
            }
            mockUserService.getUser() >> {
                User user = new User(username:'test',fullName:'testFullName')
                return user
            }
            controller.userService=mockUserService
           SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
               return true
           }

        when:
            params.viewSql=false
            controller.view(evdasConfiguration)
        then:
            response.status==200
            view=='/evdasAlert/view'


    }

    void "test viewExecutedConfig, When ExecutedEvdasConfiguration not found"(){
        given:
            ExecutedEvdasConfiguration executedConfiguration1= null
        when:
            controller.viewExecutedConfig(executedConfiguration1)
        then:
            response.status==302
            response.redirectedUrl=='/evdasAlert/index'

    }

    void "test changeDisposition , when result found changed disposition successfully"() {
        given:

        when:
            controller.changeDisposition("", disposition, "","","{\"3\":[{\"name\":\"test\",\"id\":\"1\"}]}",false,1)
        then:
            response.status==200
    }

    void "test revertDisposition , when result found disposition reverted successfully"() {
        given:

        when:
        controller.revertDisposition(1,"TEST JUSTIFICATION")
        then:
        response.status==200
    }

    void "test viewExecutedConfig, When ExecutedEvdasConfiguration found"(){
        given:
            ExecutedEvdasConfiguration executedConfiguration1= new ExecutedEvdasConfiguration()
            User user = new User(username:'test',fullName:'testFullName')
            def mockUserService=Mock(UserService)
            mockUserService.getUser() >> {

                return user
            }
            controller.userService=mockUserService
        when:
            controller.viewExecutedConfig(executedConfiguration1)
        then:
            response.status==200
            view=='/evdasAlert/view'
            model.configurationInstance==executedConfiguration1
            model.currentUser==user
            model.isExecuted==true

    }

    void "test edit, When ExecutedEvdasConfiguration not found"(){
        given:
            EvdasConfiguration evdasConfiguration= null
        when:
            controller.edit(evdasConfiguration)
        then:
            response.status==302
            response.redirectedUrl=='/evdasAlert/index'

    }
    Void"test copy, When configuration not found "(){
        given:
        EvdasConfiguration config=null
        when:
        controller.copy(config)
        then:
        response.status==302
        response.redirectedUrl=='/evdasAlert/index'
    }

    @Unroll
    Void"test copy, When configuration found "(){
        given:
            Priority priority=new Priority()
            User user=new User()
            EVDASDateRangeInformation evdasDateRangeInformation=new EVDASDateRangeInformation(dateRangeStartAbsolute:new Date(2020,06,03),dateRangeEndAbsolute:new Date(2020,06,07),dateRangeEnum:dateRangeType)
            EvdasConfiguration config=new EvdasConfiguration(owner:user,name:'testName',priority:priority,createdBy:'user',modifiedBy:'user',dateRangeInformation:evdasDateRangeInformation)

        when:
            controller.copy(config)
        then:
            response.status==200
            view=='/evdasAlert/create'
            model.action==Constants.AlertActions.COPY
            model.userList[0].fullName=='testFullName'
            model.priorityList[0].displayName=='testDisplayName'
            model.configurationInstance==config
            model.startDateAbsoluteCustomFreq==startDateAbsoluteCustomFreqResult
            model.endDateAbsoluteCustomFreq==endDateAbsoluteCustomFreqResult

        where:
        dateRangeType            | startDateAbsoluteCustomFreqResult              | endDateAbsoluteCustomFreqResult
        DateRangeEnum.CUMULATIVE | ''                                             | ''
        DateRangeEnum.CUSTOM     | DateUtil.toDateString1(new Date(2020, 06, 03)) | DateUtil.toDateString1(new Date(2020, 06, 07))
    }

    Void"test runOnce, When configuration not found "(){
        when:
            params.id=instanceId
            controller.runOnce()
        then:
            response.status==302
            flash.message!=null
            response.redirectedUrl=='/evdasAlert/index'
        where:
            instanceId<<[null,10]
    }

    Void"test runOnce, When configuration found and nextRunDate is not null "(){
        when:
            params.id=2
            controller.runOnce()
        then:
            response.status==302
            flash.warn!=null
            response.redirectedUrl=='/configuration/index'
    }

    Void"test runOnce, When configuration found and nextRunDate is null -- Success "(){
        given:
            EvdasConfiguration evdasConfiguration=EvdasConfiguration.get(1)
            def mockConfigurationService=Mock(ConfigurationService)
            mockConfigurationService.getNextDate(_)>>{
                return evdasConfiguration.nextRunDate=new Date()

            }
            controller.configurationService=mockConfigurationService
            def mockCRUDService=Mock(CRUDService)
            mockCRUDService.save(_)>>{
                return EvdasConfiguration.get(1)
            }
            controller.CRUDService=mockCRUDService
            def mockUserService=Mock(UserService)
            mockUserService.getUser()>>{
                new User()
            }
            controller.userService=mockUserService

        when:
            params.id=1
            controller.runOnce()
        then:
            response.status==302
            flash.message!=null
            response.redirectedUrl=='/configuration/executionStatus?alertType='+ AlertType.EVDAS_ALERT
    }

    Void"test delete, When configuration not found "(){
        when:
            EvdasConfiguration evdasConfiguration=null
            controller.delete(evdasConfiguration)
        then:
            response.status==302
            flash.message!=null
            response.redirectedUrl=='/evdasAlert/index'
    }
    



}