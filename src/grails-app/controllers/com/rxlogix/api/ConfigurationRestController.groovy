package com.rxlogix.api

import com.rxlogix.Constants
import com.rxlogix.ReportExecutorService
import com.rxlogix.commandObjects.SaveCaseSeriesFromSpotfireCO
import com.rxlogix.config.*
import com.rxlogix.dto.ExecutionStatusDTO
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.enums.GroupType
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.signal.DrugClassification
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SignalQueryHelper
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.rest.RestfulController
import grails.transaction.Transactional
import org.hibernate.criterion.Order
import org.hibernate.sql.JoinType
import org.springframework.transaction.annotation.Propagation

class ConfigurationRestController extends RestfulController {

    def springSecurityService
    def configurationService
    def userService
    def alertService
    def productBasedSecurityService
    def pvsProductDictionaryService
    def viewInstanceService
    def adHocAlertService

    ConfigurationRestController() {
        super(Configuration)
    }

    def index() {
        def startTime=System.currentTimeSeconds();
        User currentUser = userService.getUser()
        Long workflowGroupId = currentUser.workflowGroup.id
        List<Long> groupIds = currentUser.groups?.collect { it.id }
        List alertList = []
        List<Configuration> configList
        def configurations
        Boolean isAggOrSingle = params.alertType in [Constants.AlertConfigType.SINGLE_CASE_ALERT,Constants.AlertConfigType.AGGREGATE_CASE_ALERT]
        int allTheColumns = 7
        Map filterMap = [:]
        Integer totalCount
        String searchValue = params["search[value]"]
        Integer offset = params.start  as Integer
        Integer max = params.length  as Integer
        (0..allTheColumns).each {
            if (params["columns[${it}][search][value]"]) {
                String key = params["columns[${it}][data]"]
                String value = params["columns[${it}][search][value]"]
                filterMap.put(key, value)
            }
        }
        def orderColumn = params["order[0][column]"]
        def orderColumnMap = [name: params["columns[${orderColumn}][data]"], dir: params["order[0][dir]"]]

        if (isAggOrSingle) {
            Closure aggregateCriteria = {
                eq("isDeleted", false)
                if (params.alertType.equals(Constants.AlertConfigType.AGGREGATE_CASE_ALERT) &&
                        (!SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION") && SpringSecurityUtils.ifAnyGranted("ROLE_FAERS_CONFIGURATION"))) {
                    sqlRestriction("UPPER(selected_data_source) LIKE UPPER('%faers%')")
                } else if (params.alertType.equals(Constants.AlertConfigType.AGGREGATE_CASE_ALERT) &&
                        (!SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION") && SpringSecurityUtils.ifAnyGranted("ROLE_VAERS_CONFIGURATION"))) {
                    sqlRestriction("UPPER(selected_data_source) LIKE UPPER('%vaers%')")
                } else if (params.alertType.equals(Constants.AlertConfigType.AGGREGATE_CASE_ALERT) &&
                        (!SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION") && SpringSecurityUtils.ifAnyGranted("ROLE_VIGIBASE_CONFIGURATION"))) {
                    sqlRestriction("UPPER(selected_data_source) LIKE UPPER('%vigibase%')")
                }else if (params.alertType.equals(Constants.AlertConfigType.AGGREGATE_CASE_ALERT) &&
                        (!SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION") && SpringSecurityUtils.ifAnyGranted("ROLE_JADER_CONFIGURATION"))) {
                    sqlRestriction("UPPER(selected_data_source) LIKE UPPER('%jader%')")
                } else if (params.alertType.equals(Constants.AlertConfigType.AGGREGATE_CASE_ALERT) &&
                        (SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION") && !SpringSecurityUtils.ifAnyGranted("ROLE_FAERS_CONFIGURATION") && !SpringSecurityUtils.ifAnyGranted("ROLE_VAERS_CONFIGURATION") && !SpringSecurityUtils.ifAnyGranted("ROLE_VIGIBASE_CONFIGURATION") && !SpringSecurityUtils.ifAnyGranted("ROLE_JADER_CONFIGURATION"))) {
                    sqlRestriction("UPPER(selected_data_source) LIKE UPPER('%pva%')")
                } else if(params.alertType.equals(Constants.AlertConfigType.SINGLE_CASE_ALERT)){
                    sqlRestriction("AGG_ALERT_ID is null")
                }

                sqlRestriction("""ID IN 
           (${SignalQueryHelper.user_configuration_sql(getUserService().getCurrentUserId(), workflowGroupId,
                        groupIds.join(","), params.alertType)}
           )""")
                if (!params.alertType.equals(Constants.AlertConfigType.EVDAS_ALERT)) {
                    sqlRestriction("IS_CASE_SERIES=0")
                }
                if (orderColumnMap.name != null) {
                    if (orderColumnMap.name?.equals('noOfExecution')) {
                        order('numOfExecutions', orderColumnMap.dir)
                    } else {
                        order(Order.(orderColumnMap.dir)(orderColumnMap.name).ignoreCase())
                    }
                } else {
                    order('numOfExecutions', 'desc')
                }

                if (searchValue) {
                    String esc_char = ""
                    if (searchValue.contains('_')) {
                        searchValue = searchValue.replaceAll("\\_", "!_%")
                        esc_char = "escape '!'"
                    } else if (searchValue.contains('%')) {
                        searchValue = searchValue.replaceAll("\\%", "!%%")
                        esc_char = "escape '!'"
                    }
                    searchValue = searchValue.replaceAll("'", "''")
                    def listUser = adHocAlertService.userList(searchValue, esc_char)*.id.join(",")
                    or {
                        sqlRestriction("UPPER(name) LIKE UPPER('%${searchValue}%') ${esc_char}")
                        if (listUser) {
                            sqlRestriction("PVUSER_ID in (${listUser})")
                        }
                    }
                }

                if (filterMap.size() > 0) {
                    filterMap.each { k, v ->
                        Map<String, String> dateMap = ["lastUpdated": "last_updated", "dateCreated": "DATE_CREATED"]
                        if (k in dateMap) {
                            sqlRestriction("UPPER(to_char(${dateMap.get(k)},'DD-MON-YYYY HH:MI:ss AM')) LIKE UPPER('%${EscapedILikeExpression.escapeString(v)}%')")
                        } else if (k == "noOfExecution") {
                            sqlRestriction("UPPER(NUM_OF_EXECUTIONS) LIKE UPPER('%${v}%')")
                        } else if (k == "createdBy") {
                            def listUser = adHocAlertService.userList(v)*.id.join(",")
                            if (listUser) {
                                sqlRestriction("PVUSER_ID in (${listUser})")
                            }
                        } else {
                            iLikeWithEscape(k, "%${EscapedILikeExpression.escapeString(v)}%")
                        }
                    }

                }
            }
            configList = Configuration.createCriteria().list([max:max,offset:offset],aggregateCriteria) as List<Configuration>
            configurations = briefProperties(configList)
            if (configurations) {
                alertList.addAll(configurations)
            }
            totalCount = Configuration.createCriteria().count {
                aggregateCriteria.delegate = delegate
                aggregateCriteria()
            } as Integer
        } else if (params.alertType.equals(Constants.AlertConfigType.EVDAS_ALERT)) {
            Closure evdasCriteria = {
                sqlRestriction("""ID IN 
           (${SignalQueryHelper.evdas_configuration_sql(getUserService().getCurrentUserId(), workflowGroupId,
                        groupIds.join(","))}  )""")
                isNull('integratedConfigurationId')
                eq("isDeleted", false)
                if (orderColumnMap.name != null) {
                    if (orderColumnMap.name?.equals('noOfExecution')) {
                        order('numOfExecutions', orderColumnMap.dir as String)
                    } else {
                        order(Order.(orderColumnMap.dir)(orderColumnMap.name).ignoreCase())
                    }
                } else {
                    order('numOfExecutions', 'desc')
                }
                if (searchValue) {
                    String esc_char = ""
                    if (searchValue.contains('_')) {
                        searchValue = searchValue.replaceAll("\\_", "!_")
                        esc_char = "escape '!'"
                    } else if (searchValue.contains('%')) {
                        searchValue = searchValue.replaceAll("\\%", "!%%")
                        esc_char = "escape '!'"
                    }
                    searchValue = searchValue.replaceAll("'", "''")
                    def listUser = adHocAlertService.userList(searchValue, esc_char)*.id.join(",")
                    or {
                        sqlRestriction("UPPER(name) LIKE UPPER('%${searchValue}%') ${esc_char}")
                        if (listUser) {
                            sqlRestriction("OWNER_ID in (${listUser})")
                        }
                    }
                }
                if (filterMap.size() > 0) {
                    filterMap.each { k, v ->
                        Map<String, String> dateMap = ["lastUpdated": "last_updated", "dateCreated": "DATE_CREATED"]
                        if (k in dateMap) {
                            sqlRestriction("UPPER(to_char(${dateMap.get(k)},'DD-MON-YYYY HH:MI:ss AM')) LIKE UPPER('%${EscapedILikeExpression.escapeString(v)}%')")
                        } else if (k == "noOfExecution") {
                            sqlRestriction("UPPER(NUM_OF_EXECUTIONS) LIKE UPPER('%${v}%')")
                        } else if (k == "createdBy") {
                            def listUser = adHocAlertService.userList(v)*.id.join(",")
                            if (listUser) {
                                sqlRestriction("OWNER_ID in (${listUser})")
                            }
                        } else {
                            iLikeWithEscape(k, "%${EscapedILikeExpression.escapeString(v)}%")
                        }
                    }
                }
            }
            List<EvdasConfiguration> evdasConfigurations = EvdasConfiguration.createCriteria().list([max: max, offset: offset], evdasCriteria) as List<EvdasConfiguration>
            if (evdasConfigurations) {
                alertList.add(briefEvdasProperties(evdasConfigurations))
            }
            totalCount = EvdasConfiguration.createCriteria().count {
                evdasCriteria.delegate = delegate
                evdasCriteria()
            } as Integer
        } else if (params.alertType.equals(Constants.AlertConfigType.LITERATURE_SEARCH_ALERT)) {
            Closure literatureConfigs = {
                sqlRestriction("""ID IN 
           (${SignalQueryHelper.literature_configuration_sql(getUserService().getCurrentUserId(), workflowGroupId,
                        groupIds.join(","))}  )""")
                eq("isDeleted", false)
                if (orderColumnMap.name != null) {
                    if (orderColumnMap.name?.equals('noOfExecution')) {
                        order('numOfExecutions', orderColumnMap.dir as String)
                    } else if (!orderColumnMap.name?.equals('description')) {
                        order(Order.(orderColumnMap.dir)(orderColumnMap.name).ignoreCase())
                    }
                } else {
                    order('numOfExecutions', 'desc')
                }
                if (searchValue) {
                    String esc_char = ""
                    if (searchValue.contains('_')) {
                        searchValue = searchValue.replaceAll("\\_", "!_")
                        esc_char = "escape '!'"
                    } else if (searchValue.contains('%')) {
                        searchValue = searchValue.replaceAll("\\%", "!%%")
                        esc_char = "escape '!'"
                    }
                    searchValue = searchValue.replaceAll("'", "''")
                    or {
                        def listUser = adHocAlertService.userList(searchValue, esc_char)*.id.join(",")
                        or {
                            sqlRestriction("UPPER(name) LIKE UPPER('%${searchValue}%') ${esc_char}")
                            if (listUser)
                                sqlRestriction("PVUSER_ID in (${listUser})")
                        }
                    }
                }
                if (filterMap.size() > 0) {
                    filterMap.each { k, v ->
                        Map<String, String> dateMap = ["lastUpdated": "last_updated", "dateCreated": "DATE_CREATED"]
                        if (k in dateMap) {
                            sqlRestriction("UPPER(to_char(${dateMap.get(k)},'DD-MON-YYYY HH:MI:ss AM')) LIKE UPPER('%${EscapedILikeExpression.escapeString(v)}%')")
                        } else if (k == "noOfExecution") {
                            sqlRestriction("UPPER(NUM_OF_EXECUTIONS) LIKE UPPER('%${v}%')")
                        } else if (k == "createdBy") {
                            def listUser = adHocAlertService.userList(v)*.id.join(",")
                            sqlRestriction("PVUSER_ID in (${listUser})")
                        } else {
                            iLikeWithEscape(k, "%${EscapedILikeExpression.escapeString(v)}%")
                        }
                    }
                }
            }
            List<LiteratureConfiguration> literatureConfigurations = LiteratureConfiguration.createCriteria().list([max:max,offset:offset],literatureConfigs)

            if (literatureConfigurations) {
                alertList.add(briefLiteratureProperties(literatureConfigurations))
            }
            totalCount = LiteratureConfiguration.createCriteria().count {
                literatureConfigs.delegate = delegate
                literatureConfigs()
            } as Integer
        }
        log.info("Total time taken to fetch the ${params.alertType} configurations is ${System.currentTimeSeconds()-startTime} seconds")
        render([aaData: alertList?.flatten(), recordsTotal: alertList?.flatten()?.size(), recordsFiltered: totalCount] as JSON)
    }

    private List briefProperties(List<Configuration> configurations) {
        configurations.collect {
            //isEdit for edit and copy both
            Boolean isEdit = false
            Boolean isView = false
            Boolean isDelete = false
            Boolean isRun = false
            Boolean isOwner = userService.getCurrentUserId() == it.owner.id
            Boolean isSharedExecuted = SpringSecurityUtils.ifAnyGranted("ROLE_CONFIGURATION_CRUD, ROLE_EXECUTE_SHARED_ALERTS")
            def numOfExecutions
            String userTimeZone = userService?.getUser()?.preference?.timeZone?:"UTC"
            if (it.type == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                if(it.aggAlertId){
                    numOfExecutions = 1
                }
                isEdit = SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN, ROLE_SINGLE_CASE_CONFIGURATION") || isOwner
                isRun = SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN, ROLE_SINGLE_CASE_CONFIGURATION, ROLE_EXECUTE_SHARED_ALERTS") || isOwner
                isView = SpringSecurityUtils.ifAnyGranted("ROLE_SINGLE_CASE_VIEWER") || isSharedExecuted || isOwner
                isDelete = SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN, ROLE_SINGLE_CASE_CONFIGURATION, ROLE_EXECUTE_SHARED_ALERTS") || isOwner
            } else if(it.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT){
                numOfExecutions = it.numOfExecutions
                isEdit = SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN, ROLE_AGGREGATE_CASE_CONFIGURATION, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION") || isOwner
                isRun = SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN, ROLE_AGGREGATE_CASE_CONFIGURATION, ROLE_EXECUTE_SHARED_ALERTS, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION") || isOwner
                isView = SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_VIEWER, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION") || isSharedExecuted || isOwner
                isDelete = SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN, ROLE_AGGREGATE_CASE_CONFIGURATION, ROLE_EXECUTE_SHARED_ALERTS, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION") || isOwner
            }
            [
                    id           : it.id,
                    name         : it.name,
                    description  : it.description,
                    noOfExecution: numOfExecutions ? numOfExecutions : it.numOfExecutions,
                    isPublic     : it.isPublic,
                    dateCreated  : DateUtil.StringFromDate(it.dateCreated,DateUtil.DATEPICKER_FORMAT_AM_PM_2.toString(),userTimeZone),
                    createdBy    : it.owner.fullName,
                    type         : it.type == Constants.AlertConfigType.SINGLE_CASE_ALERT ?
                            message(code: 'app.new.single.case.alert') : message(code: 'app.new.aggregate.case.alert'),
                    isAdhocRun   : it.adhocRun,
                    lastUpdated  : DateUtil.StringFromDate(it.lastUpdated,DateUtil.DATEPICKER_FORMAT_AM_PM_2.toString(),userTimeZone),
                    isView       : isView ,
                    isRun        : isRun ,
                    isDelete     : isDelete ,
                    isEdit       : isEdit,
                    masterConfigId: it.masterConfigId,
                    unscheduled   : it.isLatestMaster && !it.masterConfigId,
                    isRunnable     : it.masterConfigId == null || it.nextRunDate == null
            ]
        }
    }

    private List briefEvdasProperties(List<EvdasConfiguration> configurations) {
        configurations.collect {
            //isEdit for edit and copy both
            Boolean isEdit = false
            Boolean isView = false
            Boolean isDelete = false
            Boolean isRun = false
            Boolean isOwner = userService.getCurrentUserId() == it.owner.id
            Boolean isSharedExecuted = SpringSecurityUtils.ifAnyGranted("ROLE_CONFIGURATION_CRUD, ROLE_EXECUTE_SHARED_ALERTS")
            isEdit = SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN, ROLE_EVDAS_CASE_CONFIGURATION") || isOwner
            isRun = SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN, ROLE_EVDAS_CASE_CONFIGURATION, ROLE_EXECUTE_SHARED_ALERTS") || isOwner
            isView = SpringSecurityUtils.ifAnyGranted("ROLE_EVDAS_CASE_VIEWER") || isSharedExecuted || isOwner
            isDelete = SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN, ROLE_EVDAS_CASE_CONFIGURATION, ROLE_EXECUTE_SHARED_ALERTS") || isOwner
            [
                    id           : it.id,
                    name         : it.name,
                    description  : it.description,
                    noOfExecution: it.numOfExecutions,
                    isPublic     : true,
                    dateCreated  : it.dateCreated,
                    createdBy    : it.owner.fullName,
                    type         : message(code: 'app.new.evdas.alert'),
                    isAdhocRun   : it.adhocRun,
                    lastUpdated  : it.lastUpdated,
                    isView       : isView,
                    isEdit       : isEdit,
                    isDelete     : isDelete,
                    isRun        : isRun,
                    masterConfigId: null
            ]
        }
    }

    private List briefLiteratureProperties(List<LiteratureConfiguration> configurations) {
        configurations.collect {
            //isEdit for edit and copy both
            Boolean isEdit = false
            Boolean isView = false
            Boolean isDelete = false
            Boolean isRun = false
            Boolean isOwner = userService.getCurrentUserId() == it.owner.id
            Boolean isSharedExecuted = SpringSecurityUtils.ifAnyGranted("ROLE_CONFIGURATION_CRUD, ROLE_EXECUTE_SHARED_ALERTS")
            isEdit = SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN, ROLE_LITERATURE_CASE_CONFIGURATION") || isOwner
            isRun = SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN, ROLE_LITERATURE_CASE_CONFIGURATION, ROLE_EXECUTE_SHARED_ALERTS") || isOwner
            isView = SpringSecurityUtils.ifAnyGranted("ROLE_LITERATURE_CASE_VIEWER") || isSharedExecuted || isOwner
            isDelete = SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN, ROLE_LITERATURE_CASE_CONFIGURATION, ROLE_EXECUTE_SHARED_ALERTS") || isOwner
            [
                    id           : it.id,
                    name         : it.name,
                    description  : "",
                    noOfExecution: it.numOfExecutions,
                    tags         : "",
                    isPublic     : true,
                    dateCreated  : it.dateCreated,
                    createdBy    : it.owner.fullName,
                    type         : message(code: 'app.new.literature.search.alert'),
                    isAdhocRun   : false,
                    lastUpdated  : it.lastUpdated,
                    isView       : isView ,
                    isEdit       : isEdit,
                    isRun        : isRun,
                    isDelete     : isDelete,
                    masterConfigId: null
            ]
        }
    }

    def getExecutionStatus(def configuration) {
        ExecutionStatus.findByConfigIdAndReportVersion(configuration.id, configuration.numOfExecutions)
    }

    def executionStatus(String status) {
        ExecutionStatusDTO executionStatusDTO = createExecutionStatusDTO()
        Map result = [:]
        switch (status) {
            case ReportExecutionStatus.SCHEDULED.getKey():
                executionStatusDTO.executionStatus = ReportExecutionStatus.SCHEDULED
                result = configurationService.showExecutionsScheduled(executionStatusDTO)
                break
            case ReportExecutionStatus.COMPLETED.getKey():
                executionStatusDTO.executionStatus = ReportExecutionStatus.COMPLETED
                result = configurationService.generateMapForExecutionStatus(executionStatusDTO)
                break
            case ReportExecutionStatus.ERROR.getKey():
                executionStatusDTO.executionStatus = ReportExecutionStatus.ERROR
                result = configurationService.generateMapForExecutionStatus(executionStatusDTO)
                break
            default:
                executionStatusDTO.executionStatus = ReportExecutionStatus.GENERATING
                result = configurationService.generateMapForExecutionStatus(executionStatusDTO)
        }
        render(result as JSON)
    }

    ExecutionStatusDTO createExecutionStatusDTO(){
        User user = userService.getUser()
        ExecutionStatusDTO executionStatusDTO = new ExecutionStatusDTO()
        executionStatusDTO.alertType = params.alertType as AlertType
        executionStatusDTO.searchString = params.searchString
        executionStatusDTO.currentUser = user
        executionStatusDTO.max = params.getInt('length')
        executionStatusDTO.offset =  params.getInt('start')
        executionStatusDTO.sort = params.sort
        executionStatusDTO.direction = params.direction
        executionStatusDTO.workflowGroupId = user?.workflowGroup?.id
        setConfigrationDomain(executionStatusDTO)
        executionStatusDTO
    }

    private void setConfigrationDomain(ExecutionStatusDTO executionStatusDTO){
        switch (params.alertType){
            case AlertType.AGGREGATE_CASE_ALERT.name():
            case AlertType.SINGLE_CASE_ALERT.name():
                executionStatusDTO.configurationDomain = Configuration
                break
            case AlertType.EVDAS_ALERT.name():
                executionStatusDTO.configurationDomain = EvdasConfiguration
                break
            case AlertType.LITERATURE_SEARCH_ALERT.name():
                executionStatusDTO.configurationDomain = LiteratureConfiguration
                break
        }
    }


    def getUserScheduledConfigurationList() {
        User currentUser = userService.getUser()
        def c = Configuration.createCriteria()

        def scheduledConfigurations = c.list {
            and {
                eq('isDeleted', false)
                isNotNull('nextRunDate')
            }
        }
        return scheduledConfigurations
    }

    def getUserViewableConfigurationList() {
        def c = Configuration.createCriteria()

        List<Configuration> configurations = c.list {
            eq('isDeleted', false)
        }
        return configurations
    }

    def searchGenerics = {
        String searchTerm = params.term + "%"
        def allowedGenericNames = productBasedSecurityService.allowedGenericNamesForUser(userService.getUser())
        def genericsList = alertService.genericsList(searchTerm)
        if (genericsList) {
            def list = genericsList.findAll { item ->
                if (grailsApplication.config.pvsignal.product.based.security) {
                    allowedGenericNames.contains(item['id']?.toLowerCase())
                } else {
                    true
                }
            }
            respond list, [formats: ['json']]
        } else {
            respond([], [formats: ['json']])
        }
    }

    def fetchAllowedUsers(){
        def productList = params.productList.tokenize(',')
        def allowedUsers = []
        def userList = User.findAll()
        userList.each{User user ->
            def userFullName = user.fullName?.toLowerCase()
            def allowedProductsToUser = productBasedSecurityService.allAllowedProductForUser(user)
            if(allowedProductsToUser?.containsAll(productList) && !allowedUsers?.contains(userFullName)){
                allowedUsers?.push(['id' : user.id, 'fullName' : user.fullName])
            }
        }
        render allowedUsers as JSON
    }

    def fetchDrugClassification(String product){

        List drugClassificationList = []
        List dcList = []

        DrugClassification.withTransaction {
            drugClassificationList = DrugClassification."faers".findAllByClassification(product)
        }

        dcList = drugClassificationList.collect{
            ['className' : it.className]
        }?.unique{
            it.className
        }

        render dcList?.sort({it.className.toUpperCase()}) as JSON
    }

    List<Configuration> configurationList(Long userId, Long workflowGroupId, List<Long> groupIds, String type) {
        List<Configuration> configList = Configuration.createCriteria().list {
            executedConfigForViewAlert.delegate = delegate
            executedConfigForViewAlert(workflowGroupId,userId,groupIds)
            eq("isCaseSeries", false)
            if (type) {
                eq("type", type)
            }
            order("numOfExecutions","desc")
        } as List<Configuration>
        configList
    }

    Closure executedConfigForViewAlert = {Long workflowGroupId ,Long userId ,List<Long> groupIds ->
        createAlias("shareWithUser", "shareWithUser", JoinType.LEFT_OUTER_JOIN)
        createAlias("shareWithGroup", "shareWithGroup", JoinType.LEFT_OUTER_JOIN)
        eq("isDeleted", false)
        'or' {
            and {
                eq("workflowGroup.id", workflowGroupId)
                or {
                    eq("shareWithUser.id", userId)
                    if (groupIds) {
                        or {
                            groupIds.collate(1000).each {
                                'in'("shareWithGroup.id", groupIds)
                            }
                        }
                    }
                }
            }
            eq('owner.id',userId)
        }
    }

    def resumeAlertExecution(ExecutionStatus executionStatus) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            def configuration = configurationService.updateConfiguration(executionStatus)
            if((configuration instanceof Configuration && !configuration.masterConfigId) || !(configuration instanceof Configuration)) {
                executionStatus.executionStatus = ReportExecutionStatus.SCHEDULED
                if (configuration.hasProperty("templateQueries") && configuration.templateQueries?.size()) {
                    executionStatus.reportExecutionStatus = ReportExecutionStatus.SCHEDULED
                }
                if (configuration.hasProperty("spotfireSettings") && configuration.spotfireSettings) {
                    executionStatus.spotfireExecutionStatus = ReportExecutionStatus.SCHEDULED
                }
            }
            executionStatus.save(flush: true)
            if(configuration instanceof Configuration && configuration.masterConfigId){
                MasterConfiguration masterConfiguration = MasterConfiguration.get(configuration.masterConfigId)
                responseDTO.message = message(code: "app.label.resume.master.success", args: [masterConfiguration.name])
            }
        } catch (Exception e) {
            e.printStackTrace()
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.resume.error")
        }
        render(responseDTO as JSON)
    }

    def resumeReportExecution(ExecutionStatus executionStatus) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            Configuration configuration = Configuration.get(executionStatus.configId)
            configuration = configurationService.updateConfigurationAndExecutionStatus(configuration, executionStatus, Constants.Commons.RESUME_REPORT)
            if(configuration.masterConfigId){
                MasterConfiguration masterConfiguration = MasterConfiguration.get(configuration.masterConfigId)
                responseDTO.message = message(code: "app.label.resume.master.report.success", args: [masterConfiguration.name])
            } else {
                if(configuration.spotfireSettings && executionStatus.spotfireExecutionStatus == ReportExecutionStatus.ERROR){
                    executionStatus.spotfireExecutionStatus = ReportExecutionStatus.SCHEDULED
                }
                executionStatus.reportExecutionStatus = ReportExecutionStatus.SCHEDULED
                executionStatus.executionLevel = Constants.Commons.RESUME_REPORT
            }
            executionStatus.save(flush: true)
        } catch (Exception e) {
            e.printStackTrace()
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.resume.error")
        }
        render(responseDTO as JSON)
    }

    def resumeSpotfireExecution(ExecutionStatus executionStatus) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            Configuration configuration = Configuration.get(executionStatus.configId)
            configuration = configurationService.updateConfigurationAndExecutionStatus(configuration, executionStatus, Constants.Commons.RESUME_SPOTFIRE)
            if(configuration.masterConfigId){
                MasterConfiguration masterConfiguration = MasterConfiguration.get(configuration.masterConfigId)
                responseDTO.message = message(code: "app.label.resume.master.report.success", args: [masterConfiguration.name])
            } else {
                executionStatus.spotfireExecutionStatus = ReportExecutionStatus.SCHEDULED
                executionStatus.executionLevel = Constants.Commons.RESUME_SPOTFIRE
            }
            executionStatus.save(flush: true)
        } catch (Exception e) {
            e.printStackTrace()
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.resume.error")
        }
        render(responseDTO as JSON)
    }

    def saveCaseSeriesForSpotfire(SaveCaseSeriesFromSpotfireCO co) {
        ResponseDTO responseDTO = new ResponseDTO()
        try {
            if (!co.validate()) {
                log.warn(co.errors.allErrors?.toString())
                responseDTO.setErrorResponse(message(code: "case.series.no.case").toString())
            } else {
                User user = User.findByUsernameAndEnabled(co.user, true)
                if (!user) {
                    responseDTO.setErrorResponse(message(code: 'case.series.spotfire.user.not.exist'))
                } else {
                    viewInstanceService.saveTempView(user , co.caseNumbers)
                    log.info("Case Series Saved.")
                    responseDTO.message = message(code: 'case.series.saved').toString()
                }
            }
        } catch (Exception ex) {
            responseDTO.setErrorResponse(ex)
        }
        render(responseDTO as JSON)
    }
}
