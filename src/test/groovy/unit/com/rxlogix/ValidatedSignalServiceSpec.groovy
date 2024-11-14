package unit.com.rxlogix

import com.rxlogix.ActivityService
import com.rxlogix.AlertAttributesService
import com.rxlogix.CRUDService
import com.rxlogix.Constants
import com.rxlogix.UserService
import com.rxlogix.ValidatedSignalService
import com.rxlogix.config.Activity
import com.rxlogix.config.ActivityType
import com.rxlogix.config.ActivityTypeValue
import com.rxlogix.config.AllowedDictionaryDataCache
import com.rxlogix.config.Disposition
import com.rxlogix.config.Priority
import com.rxlogix.config.ProductDictionaryCache
import com.rxlogix.config.SafetyGroup
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.SignalStatusHistory
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import spock.lang.Ignore
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@TestFor(ValidatedSignalService)
@TestMixin(DomainClassUnitTestMixin)
@Mock([Disposition, Group, Priority, ValidatedSignal,User,SignalStatusHistory,ActivityType,Activity, ActivityService,UserService, AlertAttributesService, AllowedDictionaryDataCache, ProductDictionaryCache, SafetyGroup])
@ConfineMetaClassChanges([ValidatedSignalService])
class ValidatedSignalServiceSpec extends Specification {

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

    def setup() {
        priority = new Priority(value: "High", display: true, displayName: "High", reviewPeriod: 3, priorityOrder: 1)
        priority.save(flush:true)

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
        user.addToGroups(wfGroup)
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(flush:true)

        signalStatusHistory = new SignalStatusHistory(dateCreated: new Date(), statusComment: "Test Status Comment", signalStatus: "Signal Status",
                dispositionUpdated: true,performedBy: "Test User",id:1)
        signalStatusHistory.save()

        validatedSignal = new ValidatedSignal(
                name: "test_name",
                products: "test_products",
                endDate: new Date(),
                assignedTo: user,
                assignmentType: 'USER',
                modifiedBy: user.username,
                priority: priority,
                disposition: defaultSignalDisposition,
                createdBy: user.username,
                startDate: new Date(),
                id:1,
                genericComment: "Test notes",
                workflowGroup: wfGroup
        )
        validatedSignal.addToSignalStatusHistories(signalStatusHistory)
        validatedSignal.save(flush:true)

        safetyGroup = new SafetyGroup(name: "Test Safety", createdBy: user.name , dateCreated: new Date(), lastUpdated: new Date(), modifiedBy: user.name, allowedProd: "Test Product A, Test Product B, Test Product C", allowedProductList: ["Test Product A", "Test Product B", "Test Product C"], members: [user])
        safetyGroup.save(flush:true)

        productDictionaryCache = new ProductDictionaryCache(safetyGroup: safetyGroup)
        productDictionaryCache.save(flush:true)

        allowedDictionaryDataCache = new AllowedDictionaryDataCache(fieldLevelId: 1, label: "Product Name", isProduct: true, allowedData: "Test Product A, Test Product B, Test Product C", allowedDataIds: "123, 456, 789")
        allowedDictionaryDataCache.productDictionaryCache = productDictionaryCache
        allowedDictionaryDataCache.save(flush:true)
    }

    void "Test saveValidatedSignal"() {
        given: "A map with required parameters"
        ValidatedSignal validatedSignal1
        Map<String, String> map = ['productSelection': '{"1":[],"2":[],"3":[{"name":"Test Product d","id":"100022"}],"4":[],"5":[]}', 'eventSelection': '{"1":[],"2":[{"name":"Body temperature conditions (J)","id":"10005908"}],"3":[{"name":"Febrile disorders","id":"10016286"}],"4":[{"name":"Pyrexia","id":"10037660"}],"5":[],"6":[]}', 'name': 'Test Signal', 'topic': '', genericComment: '', 'detectedBy': 'Sahi', 'evaluationMethod': 'Claims data mining', 'signalEvaluationMethod': '', priority: '1', description: 'Description', reasonForEvaluation: '', commentSignalStatus: '', 'assignedToValue': 'User_1', initialDataSource: "Data mining - FAERS database"]

        def mockUserService = Mock(UserService)
        service.userService = mockUserService

        mockUserService.getUser() >> user
        service.metaClass.setDatesForSignal = { ValidatedSignal validatedSignal, Map<String, String> params -> }
        service.metaClass.setNonStringFields = { Map<String, String> params, ValidatedSignal validatedSignal -> }
        service.metaClass.bindTopicCategory = { ValidatedSignal validatedSignal, def signalTypeList -> }
        service.metaClass.bindActionTaken = { ValidatedSignal validatedSignal, def signalTypeList -> }
        service.metaClass.bindEvaluationMethod = { ValidatedSignal validatedSignal, def signalTypeList -> }
        service.metaClass.bindOutcomes = { ValidatedSignal validatedSignal, def signaloutcomeList -> }
        service.metaClass.bindLinkedSignals = { ValidatedSignal validatedSignal, def linkedSignalList -> }

        when:
        validatedSignal1 = service.saveValidatedSignal(map)

        then:
        validatedSignal1.disposition == defaultSignalDisposition
    }

    void "test validateSignal with all required parameters"() {

        given: "A map with required parameters"

        UserService mockUserService = Mock(UserService)
        mockUserService.getUser() >> {
            return (User.get(1))
        }
        service.userService = mockUserService

        Map<String, String> map = ['products': '{"1":[],"2":[],"3":[{"name":"Test Product d","id":"100022"}],"4":[],"5":[]}', 'events': '{"1":[],"2":[{"name":"Body temperature conditions (J)","id":"10005908"}],"3":[{"name":"Febrile disorders","id":"10016286"}],' +
                '"4":[{"name":"Pyrexia","id":"10037660"}],"5":[],"6":[]}', 'name': 'Test Signal', 'topic': '', genericComment: '', 'detectedBy': 'Sahi', 'evaluationMethod': 'Claims data mining', 'signalEvaluationMethod': '', priority: Priority.findByValue('High'),
                                   description: 'Description', reasonForEvaluation: '', commentSignalStatus: '', assignedTo: User.findByUsername('test_user'), dueDate: new Date(), detectedDate: new Date(), initialDataSource: "Data mining - FAERS database",workflowGroup: wfGroup,assignmentType: 'USER',modifiedBy: 'modifiedBy',disposition: defaultDisposition,createdBy: 'createdBy']

        ValidatedSignal validatedSignal1 =
                new ValidatedSignal(map)
        validatedSignal1.save(flush:true)
        Boolean result = false

        when:
        result = service.validateSignal(validatedSignal1)

        then:
        result
    }

    void "test validateSignal without all required parameters"() {

        given: "A map without required parameters"

        UserService mockUserService = Mock(UserService)
        mockUserService.getUser() >> {
            return (User.get(1))
        }
        service.userService = mockUserService

        Map<String, String> map = ['products': '{"1":[],"2":[],"3":[{"name":"Test Product d","id":"100022"}],"4":[],"5":[]}', 'events': '{"1":[],"2":[{"name":"Body temperature conditions (J)","id":"10005908"}],"3":[{"name":"Febrile disorders","id":"10016286"}],"4":[{"name":"Pyrexia","id":"10037660"}],"5":[],"6":[]}', 'name': 'Test Signal', 'topic': '', genericComment: '', 'detectedBy': '', 'evaluationMethod': 'Claims data mining', 'signalEvaluationMethod': '', priority: Priority.findByValue('High'), description: 'Description', reasonForEvaluation: '', commentSignalStatus: '', assignedTo: User.findByUsername('test_user'), dueDate: new Date(), detectedDate: new Date(), initialDataSource: "Data mining - FAERS database"]

        ValidatedSignal validatedSignal = new ValidatedSignal(map)
        Boolean result = false

        when:
        result = service.validateSignal(validatedSignal)

        then:
        !result
    }


    void "test generateSignalHistory when it has StatusHistory"() {
        setup:
        AlertAttributesService mockAlertAttributeService = Mock(AlertAttributesService)
        mockAlertAttributeService.get(_) >> { return ['testA','testB'] }
        service.alertAttributesService = mockAlertAttributeService

        when:
        List<Map> signalStatusHistoryMapList = service.generateSignalHistory(validatedSignal)
        then:
        signalStatusHistoryMapList.size() == 1
        signalStatusHistoryMapList[0].signalStatus == 'Signal Status'
        signalStatusHistoryMapList[0].dispositionUpdated == true
        signalStatusHistoryMapList[0].performedBy == 'Test User'
        signalStatusHistoryMapList[0].statusComment == 'Test Status Comment'
        signalStatusHistoryMapList[0].isAddRow == true
    }

    void "test generateSignalHistory when StatusHistory is empty"() {
        setup:
        ValidatedSignal validatedSignalObj = new ValidatedSignal(
                name: "test_name",
                products: "test_products",
                endDate: new Date(),
                assignedTo: user,
                assignmentType: 'USER',
                modifiedBy: user.username,
                priority: priority,
                disposition: defaultSignalDisposition,
                createdBy: user.username,
                startDate: new Date(),
                genericComment: "Test notes"

        )
        when:
        List<Map> signalStatusHistoryMapList = service.generateSignalHistory(validatedSignalObj)
        then:
        signalStatusHistoryMapList.size() == 0
    }

    void "test generateSignalHistory when ValidatedSignal is null"() {
        setup:
        ValidatedSignal validatedSignalObj = null
        when:
        List<Map> signalStatusHistoryMapList = service.generateSignalHistory(validatedSignalObj)
        then:
        signalStatusHistoryMapList.size() == 0
    }

    void "test saveActivityForSignalHistory"() {
        setup:
        ActivityType activityType = new ActivityType(value: ActivityTypeValue.StatusDate)
        activityType.save()
        UserService userService = Mock(UserService)
        userService.getCurrentUserId() >> 1
        service.userService = userService
        String details = "Details"
        when:
        service.saveActivityForSignalHistory(Constants.SYSTEM_USER,validatedSignal,details)
        then:
        validatedSignal.activities.size() == 1

    }

    void "test saveActivityForSignalHistory when ValidatedSignal is null"() {
        setup:
        ActivityType activityType = new ActivityType(value: ActivityTypeValue.StatusDate)
        activityType.save()
        ValidatedSignal validatedSignalObj = null
        service.userService = mockService(UserService)
        when:
        service.saveActivityForSignalHistory(Constants.SYSTEM_USER,validatedSignalObj,null)
        then:
        validatedSignalObj == null

    }

    void "test saveActivityForSignalHistory when Activity type is null"() {
        setup:
        service.userService = mockService(UserService)
        when:
        service.saveActivityForSignalHistory(Constants.SYSTEM_USER,validatedSignal,null)
        then:
        validatedSignal.activities == null
    }


    void "test saveSignalStatusHistory"() {
        setup:
        Map params = [signalId:"1",statusComment:"Test Status Comment",signalStatus:"Assessment Date",dateCreated: new Date()]
        UserService userServiceMock = Mock(UserService)
        userServiceMock.getUserFromCacheByUsername(_) >> user
        service.userService = userServiceMock
        CRUDService crudServiceMock = Mock(CRUDService)
        crudServiceMock.update(_) >> validatedSignal
        service.CRUDService = crudServiceMock
        ActivityType activityType = new ActivityType(value: ActivityTypeValue.StatusDate)
        activityType.save()
        when:
        service.saveSignalStatusHistory(params,true)
        then:
        validatedSignal.signalStatusHistories.size() == 2
        validatedSignal.activities.size() == 1
    }

    void "test saveSignalStatusHistory in edit mode"() {
        setup:
        Map params = [signalId:"1","signalHistoryId":"1",statusComment:"Test Status Comment Edit",signalStatus:"Assessment Date",dateCreated: new Date()]
        UserService userServiceMock = Mock(UserService)
        userServiceMock.getUserFromCacheByUsername(_) >> user
        service.userService = userServiceMock
        CRUDService crudServiceMock = Mock(CRUDService)
        crudServiceMock.update(_) >> validatedSignal
        service.CRUDService = crudServiceMock
        when:
        service.saveSignalStatusHistory(params,true)
        then:
        signalStatusHistory.statusComment == "Test Status Comment Edit"
    }

    void "test saveSignalStatusHistory when isDispositionUpdated is false"() {
        setup:
        Map params = [signalId:"1",statusComment:"Test Status Comment",signalStatus:"Assessment Date",dateCreated: new Date()]
        UserService userServiceMock = Mock(UserService)
        userServiceMock.getUserFromCacheByUsername(_) >> user
        service.userService = userServiceMock
        CRUDService crudServiceMock = Mock(CRUDService)
        crudServiceMock.update(_) >> validatedSignal
        service.CRUDService = crudServiceMock
        when:
        service.saveSignalStatusHistory(params,false)
        then:
        validatedSignal.signalStatusHistories.size() == 2
    }

    @Ignore
    void "test getStringIdList" () {
        given:
        String str = "ID_0, ID_1, ID_2"
        when:
        List<String> list = service.getStringIdList(str)
        then:
        list == ["ID_0", "ID_1", "ID_3"]
    }

    @Ignore
    void "test getAllowedProductIds" () {
        given:
        SafetyGroup safetyGroup1 = safetyGroup
        service.getStringIdList(_) >> ["Test A"]
        when:
        List<String> allowedProductIds = service.getAllowedProductIds(safetyGroup1)
        then:
        allowedProductIds == ["Test A"]
    }

}