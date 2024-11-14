package unit.com.rxlogix

import com.rxlogix.AdvancedFilterController
import com.rxlogix.AdvancedFilterService
import com.rxlogix.Constants
import com.rxlogix.UserService
import com.rxlogix.ViewInstanceService
import com.rxlogix.config.AdvancedFilter
import com.rxlogix.user.User
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Ignore
import spock.lang.Specification

@Mock([User, AdvancedFilter, AdvancedFilterService, ViewInstanceService, UserService])
@TestFor(AdvancedFilterController)
@TestMixin(GrailsUnitTestMixin)

class AdvancedFilterControllerSpec extends Specification {
    AdvancedFilter advanceFilter

    User user
    UserService mockUserService
    ViewInstanceService mockViewInstanceService
    AdvancedFilterService advancedFilterService

    def setup() {

        user = new User(id: '1', username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(flush: true)

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
        advanceFilter.name = "fake filter 1"
        advanceFilter.user = user
        advanceFilter.shareWithUser = [user]
        advanceFilter.save(validate: false)

        mockUserService = Mock(UserService)

        mockUserService.getUser() >> {
            return user
        }
        controller.userService = mockUserService

        mockViewInstanceService = Mock(ViewInstanceService)
        mockViewInstanceService.isViewFilterSharingAllowed(_,_,_) >> {
            return false
        }
        mockUserService.bindSharedWithConfiguration(_,_,_)>>{

        }
        controller.viewInstanceService = mockViewInstanceService

        advancedFilterService = Mock(AdvancedFilterService)
        advancedFilterService.createAdvancedFilterCriteria(_)>>{
            return "{ ->\n" +
                    "criteriaConditionsForSubGroup('EBGM:Confident','EQUALS',\"153\")\n" +
                    "}"
        }
        advancedFilterService.getValidInvalidValues(_,_,_,_)>>{
            return ["validValues": [1,2],
                    "invalidValues" :[3,4]
            ]
        }
        advancedFilterService.getDuplicates(_) >> {
            List list = ["1"]
            return list.toSet()
        }
        controller.advancedFilterService = advancedFilterService
    }

    def cleanup() {
    }

    void "test save"(){
        setup:
        user.metaClass.isAdmin={false}
        controller.params.filterId = advanceFilter.id as Long
        controller.params.name = "fake filter 2"
        controller.params.alertType = "Single Case Alert"
        controller.params.createdBy = "fakeuser"
        controller.params.dateCreated = new Date()
        controller.params.description = "test advanced filter"
        controller.params.JSONQuery = "{\"all\":{\"containerGroups\":[ {\"expressions\":[ {\"index\":\"0\",\"field\":\"listedness\",\"op\":\"EQUALS\",\"value\":\"true\"} ] }  ] } }"
        controller.params.lastUpdated = new Date()
        controller.params.modifiedBy = "fakeuser"
        controller.userService = [getUser: { return user }]
        controller.params.miningVariable = [:]

        when:
        controller.save()

        then:
        AdvancedFilter.countByName("fake filter 2") == 0
        response.json.status == true
        response.json.message == null
        response.json.data == [id:1, text:"fake filter 1(S)"]
    }

    void "test delete"(){
        setup:
        controller.params.id = AdvancedFilter.findByName("fake filter 1").getId()
        controller.userService = [getUser: { return userObj }]

        when:
        controller.delete()

        then:
        AdvancedFilter.countByName("fake filter 1") == 1
        response.json.status == true
    }
    void "test validateValue()"(){
        setup:
        controller.params.selectedField = "selectedField"
        controller.params.values = ""
        when:
        controller.validateValue()
        then:
        response.json.success == false
    }
    void "test validateValue() success"(){
        setup:
        controller.params.selectedField = "selectedField"
        controller.params.values = "a;b;c"
        when:
        controller.validateValue()
        then:
        response.json.success == true
    }
    void "test fetchAjaxAdvancedFilterSearch for comments in single case alert"(){
        setup:
        controller.advancedFilterService=[getAjaxFilterData:{String term, int offset, int max, Long executedConfigId,
                                                             String field, def domainName, Map filterMap->[jsonData:["comm1","comm2"],
                                                                                                           possibleValuesListSize:2]}]
        String alertType = Constants.AlertConfigType.SINGLE_CASE_ALERT
        Long executedConfigId = 0l
        String term = "comm"
        int page =0
        int max = 30
        String field = Constants.AdvancedFilter.COMMENTS
        when:
        controller.fetchAjaxAdvancedFilterSearch(executedConfigId, term, page, max, field, alertType)
        then:
        response.json == [list:["comm1", "comm2"], totalCount:2]
    }
    void "test fetchAjaxAdvancedFilterSearch for comments in aggregate alert"(){
        setup:
        controller.advancedFilterService=[getAjaxFilterData:{String term, int offset, int max, Long executedConfigId,
                                                             String field, def domainName, Map filterMap->[jsonData:["comm1","comm2"],
                                                                                                           possibleValuesListSize:2]}]
        String alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
        Long executedConfigId = 0l
        String term = "comm"
        int page =0
        int max = 30
        String field = Constants.AdvancedFilter.COMMENT
        when:
        controller.fetchAjaxAdvancedFilterSearch(executedConfigId, term, page, max, field, alertType)
        then:
        response.json == [list:["comm1", "comm2"], totalCount:2]
    }
    void "test fetchAjaxAdvancedFilterSearch for comments in evdas alert"(){
        setup:
        controller.advancedFilterService=[getAjaxFilterData:{String term, int offset, int max, Long executedConfigId,
                                                             String field, def domainName, Map filterMap->[jsonData:["comm1","comm2"],
                                                                                                           possibleValuesListSize:2]}]
        String alertType = Constants.AlertConfigType.EVDAS_ALERT
        Long executedConfigId = 0l
        String term = "comm"
        int page =0
        int max = 30
        String field = Constants.AdvancedFilter.COMMENT
        when:
        controller.fetchAjaxAdvancedFilterSearch(executedConfigId, term, page, max, field, alertType)
        then:
        response.json == [list:["comm1", "comm2"], totalCount:2]
    }
}
