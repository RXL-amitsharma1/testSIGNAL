package unit.com.rxlogix

import com.rxlogix.CRUDService
import com.rxlogix.EmergingIssueController
import com.rxlogix.EmergingIssueService
import com.rxlogix.UserService
import com.rxlogix.signal.EmergingIssue
import com.rxlogix.user.User
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Ignore
import spock.lang.Specification
import spock.util.mop.Use

@TestFor(EmergingIssueController)
@Mock([EmergingIssue, EmergingIssueService, User, CRUDService, UserService])
class EmergingIssueControllerSpec extends Specification{
    def setup(){
        User user=new User(username: "user1",fullName: "user1")
        user.save(validate:false)
        EmergingIssue emergingIssue1=new EmergingIssue(eventName: '{"3":[{"name":"event11"},{"name":"event12"}]}',createdBy: "createdBy1",dateCreated: new Date()-1)
        emergingIssue1.save(failOnError:true)
        EmergingIssue emergingIssue2=new EmergingIssue(eventName: '{"1":[{"name":"event21"},{"name":"event22"}]}',createdBy: "createdBy2",dateCreated: new Date())
        emergingIssue2.save(failOnError:true)
        SpringSecurityService springSecurityService=Mock(SpringSecurityService)
        springSecurityService.isLoggedIn()>>{
            return true
        }
        springSecurityService.principal>>{
            return user
        }
        controller.emergingIssueService.CRUDService.userService.springSecurityService=springSecurityService
    }
    def cleanup(){

    }
    void "test action index"(){
        when:
        controller.index()
        then:
        response.status==200
        view=="/emergingIssue/index"
        model.emergingIusseList.getClass()== EmergingIssue
        model.callingScreen=="index"
    }
    void "test list action when there are Emerging issue"(){
        when:
        controller.list()
        then:
        response.status==200
        JSON.parse(response.text).size()==2
        JSON.parse(response.text)[0].id==EmergingIssue.get(2).id
        JSON.parse(response.text)[1].id==EmergingIssue.get(1).id
    }
    void "test list action when there are no Emerging issue"(){
        setup:
        EmergingIssue.list().each{
            it.delete()
        }
        when:
        controller.list()
        then:
        response.status==200
        JSON.parse(response.text).size()==0
    }
    @Ignore
    void "test save action"(){
        given:
        String eventSelection='{"3":[{"name":"event1"},{"name":"event2"}]}'
        when:
        controller.save(null,null,null,null,eventSelection)
        then:
        response.status==302
        response.redirectedUrl=="/emergingIssue/index"
        EmergingIssue.get(3).eventName=='{"3":[{"name":"event1"},{"name":"event2"}]}'
    }
    void "test edit action"(){
        when:
        controller.edit(1L)
        then:
        response.status==200
        view=="/emergingIssue/index"
        model.callingScreen=="edit"
        model.emergingIusseList==EmergingIssue.get(1L)
    }
    void "test update action"(){
        given:
        String eventSelection='{"3":[{"name":"new event"},{"name":"new event"}]}'
        params.id=1L
        when:
        controller.update(null,null,null,null,eventSelection)
        then:
        response.status==302
        response.redirectedUrl=="/emergingIssue/index"
        EmergingIssue.get(1).eventName==eventSelection
    }
    void "test delete action"(){
        when:
        controller.delete(1L)
        then:
        response.status==200
        flash.message=="Important Issue deleted successfully"
        JSON.parse(response.text).status==true
    }
}
