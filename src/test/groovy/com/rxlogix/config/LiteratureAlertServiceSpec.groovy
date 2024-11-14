package com.rxlogix.config

import com.rxlogix.ActivityService
import com.rxlogix.AlertAttributesService
import com.rxlogix.SignalWorkflowService
import com.rxlogix.UserService
import com.rxlogix.ValidatedSignalService
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.SignalStatusHistory
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.user.UserGroupMapping
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges


@TestFor(LiteratureAlertService)
@TestMixin(DomainClassUnitTestMixin)
@Mock([Configuration, Disposition, Group, Priority, ValidatedSignal, User, SignalStatusHistory, ActivityType, Activity, ActivityService, UserService, AlertAttributesService, AllowedDictionaryDataCache, ProductDictionaryCache, SafetyGroup, UserGroupMapping, SignalWorkflowService, ValidatedSignalService])
@ConfineMetaClassChanges([ValidatedSignalService])
class LiteratureAlertServiceSpec extends Specification {

    User user
    Disposition defaultSignalDisposition
    Disposition defaultDisposition
    Disposition autoRouteDisposition
    Priority priority
    Group wfGroup
    ValidatedSignal validatedSignal
    SignalStatusHistory signalStatusHistory
    SafetyGroup safetyGroup
    AllowedDictionaryDataCache allowedDictionaryDataCache
    ProductDictionaryCache productDictionaryCache
    Configuration configuration_a

    def setup() {
        priority = new Priority(value: "High", display: true, displayName: "High", reviewPeriod: 3, priorityOrder: 1)
        priority.save(flush: true)
        defaultSignalDisposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VO")
        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")
        [defaultDisposition, defaultSignalDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }
        wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP,
                createdBy: 'createdBy', modifiedBy: 'modifiedBy',
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: defaultSignalDisposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(flush: true)
        user = new User(id: '1', username: 'test_user', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(flush: true)
        UserGroupMapping userGroupMapping = new UserGroupMapping(user: user, group: wfGroup)
        userGroupMapping.save(flush: true, failOnError: true)
        signalStatusHistory = new SignalStatusHistory(dateCreated: new Date(), statusComment: "Test Status Comment", signalStatus: "Signal Status",
                dispositionUpdated: true, performedBy: "Test User", id: 1)
        signalStatusHistory.save()
        ActivityType activityType = new ActivityType(value: ActivityTypeValue.StatusDate)
        activityType.save(flush: true)
        validatedSignal = new ValidatedSignal(
                name: "test_name",
                type: "Test type",
                products: "test_products",
                endDate: new Date(),
                assignedTo: user,
                assignmentType: 'USER',
                modifiedBy: user.username,
                priority: priority,
                disposition: defaultSignalDisposition,
                createdBy: user.username,
                startDate: new Date(),
                activities: [activityType],
                id: 1,
                genericComment: "Test notes",
                workflowGroup: wfGroup
        )
        validatedSignal.addToSignalStatusHistories(signalStatusHistory)
        validatedSignal.save(flush: true)
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
        safetyGroup = new SafetyGroup(name: "Test Safety", createdBy: user.username, dateCreated: new Date(), lastUpdated: new Date(), modifiedBy: user.username, allowedProd: "Test Product A, Test Product B, Test Product C", allowedProductList: ["Test Product A", "Test Product B", "Test Product C"], members: [user])
        safetyGroup.save(flush: true)
        productDictionaryCache = new ProductDictionaryCache(safetyGroup: safetyGroup)
        productDictionaryCache.save(flush: true)
        allowedDictionaryDataCache = new AllowedDictionaryDataCache(fieldLevelId: 1, label: "Product Name", isProduct: true, allowedData: "Test Product A, Test Product B, Test Product C", allowedDataIds: "123, 456, 789")
        allowedDictionaryDataCache.productDictionaryCache = productDictionaryCache
        allowedDictionaryDataCache.save(flush: true)
    }

    def "test prepareLiteratureAlertHQL with different filters and order columns"() {
        given:
        def filterMap = [
                'alertName'      : 'test_alert',
                'articleId'      : '12345',
                'title'          : 'Test Title',
                'authors'        : 'John Doe',
                'publicationDate': '2023',
                'disposition'    : 'Published',
                'assignedTo'     : 'John Group',
                'productName'    : 'Product_1',
                'eventName'      : 'Event_1',
                'signal'         : 'Signal_1'
        ]
        def orderColumnMap = [name: 'alertName', dir: 'asc']
        def isDispFilters = true
        def queryParameters = [:]
        def domainName = LiteratureAlert
        def userService = Mock(UserService)

        userService.getUser() >> user
        service.userService = userService

        when:
        def resultQuery = service.prepareLiteratureAlertHQL(filterMap, orderColumnMap, isDispFilters, queryParameters, domainName)

        then:
        resultQuery.contains("SELECT lsa FROM LiteratureAlert lsa")
    }

}