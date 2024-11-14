package com.rxlogix

import com.rxlogix.action.ActionService
import com.rxlogix.audit.AuditTrail
import com.rxlogix.audit.AuditTrailChild
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.config.ExecutedLiteratureConfiguration
import com.rxlogix.config.ExecutionStatus
import com.rxlogix.dto.AlertDTO
import com.rxlogix.dto.DashboardDTO
import com.rxlogix.dto.DueDateCountDTO
import com.rxlogix.dto.TriggeredReviewDTO
import com.rxlogix.enums.GroupType
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SignalQueryHelper
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.transform.Synchronized
import org.apache.commons.lang3.StringUtils
import org.hibernate.SQLQuery
import org.hibernate.Session

class DashboardService implements AlertUtil{

    def userService
    def productBasedSecurityService
    def singleCaseAlertService
    def aggregateCaseAlertService
    def alertService
    def sessionFactory
    def adHocAlertService
    def validatedSignalService
    def cacheService
    def dataSource
    def pvsProductDictionaryService
    def signalDataSourceService
    ActionService actionService
    def signalAuditLogService

    Map<String, List> generateProductsByStatusChart(String type) {
        Map<String, Map<String, Integer>> statusList = [:]
        Set<String> dispositionList = new HashSet<>()
        User currentUser = userService.getUser()
        Group workflowGroup = currentUser.workflowGroup
        List<Long> groupIdList = currentUser.groups.collect { it.id }

        Session session = sessionFactory.currentSession
        String sql_statement
        if (type == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
            sql_statement = SignalQueryHelper.singleCaseAlert_dashboard_by_status(currentUser.id, workflowGroup.id, groupIdList)
        } else {
            sql_statement = SignalQueryHelper.aggCaseAlert_dashboard_by_status(currentUser.id, workflowGroup.id, groupIdList)
        }
        SQLQuery sqlQuery = session.createSQLQuery(sql_statement)
        sqlQuery.list()?.each { row ->
            dispositionList.add(row[1])
            if (!statusList.containsKey(row[0])) {
                statusList.put(row[0].toString(), new HashMap<String, Integer>())
            }
            statusList.get(row[0]).put(row[1], row[2])
        }
        session.clear()
        session.flush()
        List<String> proList = []
        List productStateList = []
        if (statusList) {
            LinkedHashMap stateMap = [:]
            productStateList = fetchChartFormatData(statusList, proList, stateMap, dispositionList)
        }
        [productList: proList, productState: productStateList]
    }

    private List fetchChartFormatData(LinkedHashMap productMap, proList, LinkedHashMap stateMap, Set<String> requiredDispositions) {

        productMap.each { key, val ->
            proList.add(key)
            requiredDispositions.each {
                if (!stateMap.get(it)) {
                    stateMap.put(it, [val.get(it)])
                } else {
                    List list = stateMap.get(it)
                    list.add(val.get(it))
                    stateMap.put(it, list)
                }
            }
        }
        def productStateList = []
        stateMap.each { key, val ->
            def productStateMap = [:]
            productStateMap.put("name", key)
            productStateMap.put("data", val)
            productStateList.add(productStateMap)
        }
        productStateList
    }

    List<List<Map>> alertByDueDate() {
        User currentUser = userService.getUser()
        Group workflowgroup = currentUser.workflowGroup
        List<Long> groupIdList = currentUser.groups.collect { it.id }
        Map aggregateCountMap = [:]
        Map scaCountMap = [:]
        Map adhocCountMap = [:]
        Session session = sessionFactory.currentSession
        String dueDateWidgetQueryAggregate = SignalQueryHelper.agg_count_due_date(currentUser.id, workflowgroup.id, groupIdList)
        String dueDateWidgetQuerySCA = SignalQueryHelper.sca_count_due_date(currentUser.id, workflowgroup.id, groupIdList)
        String dueDateWidgetQueryAdhoc = SignalQueryHelper.caseAlert_dashboard_due_date(currentUser.id, workflowgroup.id, groupIdList)

        SQLQuery sqlQuery_aggregate = session.createSQLQuery(dueDateWidgetQueryAggregate)
        sqlQuery_aggregate.list().each { row ->
            aggregateCountMap.put(row[0] as String,row[1] as Long)
        }

        SQLQuery sqlQuery_sca = session.createSQLQuery(dueDateWidgetQuerySCA)
        sqlQuery_sca.list().each {row ->
            scaCountMap.put(row[0] as String,row[1] as Long)
        }

        SQLQuery sqlQuery_adhoc = session.createSQLQuery(dueDateWidgetQueryAdhoc)
        sqlQuery_adhoc.list().each {row ->
            adhocCountMap.put("OLD", row[0] as Long)
            adhocCountMap.put("NEW", row[1] as Long)
            adhocCountMap.put("CURRENT", row[2] as Long)
        }
        List<List<Map>> chartJson = prepareChartJsonMap(scaCountMap,aggregateCountMap,adhocCountMap)
        session.flush()
        session.clear()
        chartJson
    }

    DueDateCountDTO createDueDateCountDTO(){
        DueDateCountDTO dateCountDTO = new DueDateCountDTO()
        User user = userService.getUser()
        dateCountDTO.userId = user.id
        dateCountDTO.workflowGroupId = user.workflowGroup.id
        dateCountDTO.groupIdList = user.groups.collect { it.id }
        dateCountDTO.userDashboardCounts = UserDashboardCounts.get(user.id)
        dateCountDTO
    }

    void prepareDueDate(DueDateCountDTO dueDateCountDTO, boolean isCaseSeries) {
        if (dueDateCountDTO) {
            Date currentDate = new Date().clearTime()
            JsonSlurper jsonSlurper = new JsonSlurper()
            Integer pastCounts = 0
            Integer currentCounts = 0
            Integer futureCounts = 0
            String dueDateCounts = isCaseSeries ? dueDateCountDTO.userDashboardCounts?.userDueDateCaseCounts : dueDateCountDTO.userDashboardCounts?.userDueDatePECounts
            String dueDateGroupCounts = isCaseSeries ? dueDateCountDTO.userDashboardCounts?.groupDueDateCaseCounts : dueDateCountDTO.userDashboardCounts?.groupDueDatePECounts
            if (dueDateCounts) {
                Map dueDateCountMap = jsonSlurper.parseText(dueDateCounts) as Map
                pastCounts = dueDateCountMap.findAll { Date.parse("dd-MM-yyyy", it.key) < currentDate }.values().sum() ?: 0
                futureCounts =  dueDateCountMap.findAll { Date.parse("dd-MM-yyyy", it.key) > currentDate }.values().sum() ?: 0
                currentCounts =  dueDateCountMap.findAll { Date.parse("dd-MM-yyyy", it.key) == currentDate }.values().sum() ?: 0
            }
            if(dueDateGroupCounts) {
                jsonSlurper.parseText(dueDateGroupCounts).each {
                    pastCounts += it.value.entrySet().findAll { Date.parse("dd-MM-yyyy", it.key) < currentDate }.sum {it.value} ?: 0
                    futureCounts += it.value.entrySet().findAll { Date.parse("dd-MM-yyyy", it.key) > currentDate }.sum {it.value} ?: 0
                    currentCounts += it.value.entrySet().findAll { Date.parse("dd-MM-yyyy", it.key) == currentDate }.sum {it.value} ?: 0
                }
            }

            dueDateCountDTO.pastDateList.add(pastCounts)
            dueDateCountDTO.futureDateList.add(futureCounts)
            dueDateCountDTO.currentDateList.add(currentCounts)
        }
    }

    @Synchronized
    def saveDashboardConfiguration(String dashboardConfiguration) {
        User user = userService.getUser()
        user?.preference?.dashboardConfig = dashboardConfiguration
        user.save(flush: true)
    }

    Map<String, Integer> dashboardCounts(User user = null) {
        Map<String, Integer> alertMap = [:]
        User currentUser = user ?: userService.getUserFromCacheByUsername(userService.getCurrentUserName())
        alertMap.adhoc = adHocAlertService.getAssignedAdhocAlertList(currentUser)?.size()
        alertMap.actionItems = actionService.getActionDashboardCount(currentUser)
        alertMap.signals = validatedSignalService.getAssignedSignalList(currentUser)?.size()
        getAlertCounts(alertMap, currentUser)
        alertMap
    }

    Map<String, Integer> dashboardCountsByType(String caseType,User user = null) {
        Map<String, Integer> alertMap = [:]
        User currentUser = user ?: userService.getUserFromCacheByUsername(userService.getCurrentUserName())
        if(caseType=='adhoc') {
            alertMap.adhoc = adHocAlertService.getAssignedAdhocAlertList(currentUser)?.size()
        } else if(caseType=='actionItems') {
            alertMap.actionItems = actionService.getActionDashboardCount(currentUser)
        } else if(caseType=='signals') {
            alertMap.signals = validatedSignalService.getAssignedSignalList(currentUser)?.size()
        } else if(caseType=='aggregate' || caseType=='single' || caseType=='evdas') {
            getAlertCaseCounts(alertMap, caseType, currentUser);
        }
        alertMap
    }

    Map<String, Integer> getAlertCaseCounts(Map<String, Integer> alertMap, String caseType, User user = null) {
        User currentUser = user ?: userService.getUser()
        Group workflowgroup = currentUser.workflowGroup
        List<Long> groupIdList = currentUser.groups.collect { it.id }
        Sql sql
        try {
            sql = new Sql(dataSource)
            String dbCountQuery = ""
            if (caseType == 'aggregate') {
                dbCountQuery = SignalQueryHelper.dashboard_aggregate_counts2(currentUser.id, workflowgroup.id, groupIdList)
            } else if (caseType == 'single') {
                dbCountQuery = SignalQueryHelper.dashboard_aggregate_single_counts2(currentUser.id, workflowgroup.id, groupIdList)
            } else if (caseType == 'evdas') {
                dbCountQuery = SignalQueryHelper.dashboard_aggregate_evdas_counts2(currentUser.id, workflowgroup.id, groupIdList)
            }
            sql.eachRow(dbCountQuery, []) { row ->
                alertMap.put(caseType, row[0])
            }
        } catch(Exception e){
            e.printStackTrace()
        } finally {
            sql?.close()
        }

        print("After real time query caseType: "+caseType)
        alertMap
    }

    Map<String, Integer> getAlertCounts(Map<String, Integer> alertMap, User user = null) {
        User currentUser = user ?: userService.getUser()
        Group workflowgroup = currentUser.workflowGroup
        List<Long> groupIdList = currentUser.groups.collect { it.id }
        Sql sql = null
        try {
            sql = new Sql(dataSource)
            sql.eachRow(SignalQueryHelper.dashboard_counts(currentUser.id, workflowgroup.id, groupIdList), []) { row ->
                alertMap.put(row[0], row[1])
            }
            sql.close()
        } catch(Exception ex) {
            log.error(ex.printStackTrace())
        } finally {
            sql?.close()
        }
        getSCAAndAggCounts(currentUser, alertMap)
        alertMap
    }

    void getSCAAndAggCounts(User currentUser, Map<String, Integer> alertMap) {
        Integer groupCaseCounts = 0
        Integer userCaseCounts = 0
        Integer groupPECounts = 0
        Integer userPECounts = 0
        JsonSlurper jsonSlurper = new JsonSlurper()
        UserDashboardCounts userDashboardCounts = UserDashboardCounts.get(currentUser.id)

        if (userDashboardCounts) {
            if (userDashboardCounts.userDispCaseCounts) {
                userCaseCounts = jsonSlurper.parseText(userDashboardCounts.userDispCaseCounts).values().sum()
            }

            if (userDashboardCounts.groupDispCaseCounts) {
                jsonSlurper.parseText(userDashboardCounts.groupDispCaseCounts).each {
                    groupCaseCounts += it.value.entrySet().sum { it.value }
                }
            }

            if (userDashboardCounts.userDispPECounts) {
                userPECounts = jsonSlurper.parseText(userDashboardCounts.userDispPECounts).values().sum()
            }

            if (userDashboardCounts.groupDispPECounts) {
                jsonSlurper.parseText(userDashboardCounts.groupDispPECounts).each {
                    it.value?.entrySet()?.each {
                        if (it.value && it.value != []) {
                            groupPECounts += it.value
                        }
                    }
                }
            }
        }
        alertMap.single = (userCaseCounts?:0) + (groupCaseCounts?:0)
        alertMap.aggregate = (userPECounts?:0) + (groupPECounts?:0)
    }

    List<Map> generateReviewByStateChart(String type) {
        User currentUser = userService.getUser()
        Group workflowGroup = currentUser.workflowGroup
        List<Map> statusList = []
        List<Long> groupIdList = currentUser.groups.collect { it.id }

        Session session = sessionFactory.currentSession
        String sql_statement
        if (type == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
            sql_statement = SignalQueryHelper.singleCaseAlert_dashboard_count_by_disposition(currentUser.id, workflowGroup.id, groupIdList)
        } else {
            sql_statement = SignalQueryHelper.aggCaseAlert_dashboard_count_by_disposition(currentUser.id, workflowGroup.id, groupIdList)
        }
        SQLQuery sqlQuery = session.createSQLQuery(sql_statement)
        sqlQuery.list()?.each { row ->
            statusList.add([row[0], row[1]])
        }
        session.flush()
        session.clear()
        statusList
    }

    String productString(DashboardDTO dashboardDTO) {
        String finalName = ''
        try {
            if ((dashboardDTO.product != null && dashboardDTO.product != "") || (dashboardDTO.dataMiningVariable!=null && dashboardDTO.dataMiningVariable!="")) {
                finalName = !(dashboardDTO.type == (Constants.AlertType.AGGREGATE_ADHOC)) ?fetchProductName(dashboardDTO.product) :
                        (fetchProductName(dashboardDTO.product)? (dashboardDTO.dataMiningVariable? (dashboardDTO.dataMiningVariable + "(" + fetchProductName(dashboardDTO.product) + ")")
                                :fetchProductName(dashboardDTO.product)): dashboardDTO.dataMiningVariable)

                return finalName
            } else if (dashboardDTO.study != null && dashboardDTO.study != "") {
                return getAllProductNameFieldFromJson(dashboardDTO.study)
            }
        } catch (Exception ex) {
            ex.printStackTrace()
            return ""
        }
    }

    Map getFirstXExecutedConfigurationList(AlertDTO alertDTO) {
        List<Map> triggeredReviewList = []
        Integer totalCount = 0
        Integer filteredCount = 0
        Session session = sessionFactory.currentSession
        try {
            User currentUser = userService.getUserFromCacheByUsername(userService.getCurrentUserName())
            String groupIds = currentUser.groups.findAll{it.groupType != GroupType.WORKFLOW_GROUP}.collect { it.id }.join(",")
            Group workflowGroup = currentUser.workflowGroup
            Long workflowGroupId = workflowGroup.id
            String sql_statement = SignalQueryHelper.configurations_firstX(alertDTO, currentUser.id, workflowGroupId, groupIds)
            String totalCountSql = SignalQueryHelper.count_configurations(alertDTO, currentUser.id, workflowGroupId, groupIds)
            String filterCountSql = SignalQueryHelper.filtered_count_configurations(alertDTO, currentUser.id, workflowGroupId, groupIds)
            totalCount = alertService.getResultListCount(totalCountSql, session)
            filteredCount = alertService.getResultListCount(filterCountSql, session)
            List<DashboardDTO> dashboardDTOList = alertService.getResultList(DashboardDTO.class as Class<DashboardDTO>, sql_statement, session)
            triggeredReviewList = dashboardDTOList.collect {
                def  exeConfig=(it.type==Constants.AlertType.EVDAS || it.type == Constants.AlertType.EVDAS_ADHOC)? ExecutedEvdasConfiguration.get(it.id):(it.type==Constants.AlertType.LITERATURE)?ExecutedLiteratureConfiguration.get(it.id):ExecutedConfiguration.get(it.id);
                Map dateRangeMap = [dateRangeStartAbsolute:it.dateRangeStart,dateRangeEndAbsolute:it.dateRangeEnd,id:it.dateRangeId]
                List<Map> exDateRangeInfoList = [dateRangeMap]
                String dateRange =(it.type==Constants.AlertType.EVDAS || it.type == Constants.AlertType.EVDAS_ADHOC || it.type==Constants.AlertType.LITERATURE)? getDateRangeFromExecutedConfiguration(exDateRangeInfoList, exeConfig?.dateRangeInformationId):getDateRangeFromExecutedConfiguration(exDateRangeInfoList, exeConfig?.executedAlertDateRangeInformationId);
                [
                        "name"              : it.name,
                        "product"           : getProducts(it),
                        "dueIn"             : it.dueIn || it.dueIn == 0 ? it.dueIn : Constants.Commons.DASH_STRING,
                        "id"                : it.id,
                        "type"              : it.type,
                        "requiresReview"    : it.adhocRun ? Constants.Commons.DASH_STRING : it.requiresReview,
                        "dateRange"         : dateRange,
                        "executionTime"     : ExecutionStatus.findByExecutedConfigId(it.id)?.endTime ?:0
                ]
            }
        }
        catch (Exception e) {
            e.printStackTrace()
        }
        [recordsTotal: totalCount, recordsFiltered: filteredCount, aaData: triggeredReviewList]
    }

    TriggeredReviewDTO generateTriggeredReview(Long userId){
        TriggeredReviewDTO triggeredReviewDTO = new TriggeredReviewDTO()
        if(alertService.isProductSecurity()){
            triggeredReviewDTO.allowedProducts = alertService.fetchAllowedProductsForConfiguration()
        }
        triggeredReviewDTO.isProductSecurity = alertService.isProductSecurity()
        triggeredReviewDTO.reviewedList = cacheService.getDispositionByReviewCompleted()*.id
        triggeredReviewDTO.requiresReviewedList = cacheService.getNotReviewCompletedDisposition().collect {it.id as String}
        triggeredReviewDTO.userId = userId
        triggeredReviewDTO
    }

    private getDateRangeFromExecutedConfiguration(List<Map> exDateRangeInfoList, Long execDateRangeInfoId) {
        Map dateRangeMap = exDateRangeInfoList.find { it.id == execDateRangeInfoId }
        if (dateRangeMap != null && dateRangeMap.isEmpty() == false) {
            DateUtil.toDateString1(dateRangeMap.dateRangeStartAbsolute) + " to " + DateUtil.toDateString1(dateRangeMap.dateRangeEndAbsolute)
        } else {
            "-"
        }
    }

    @Transactional
    void deleteTriggeredAlert(Long id , String alertType) {
        String descString=""
        def execConfiguration
        if(alertType in [Constants.AlertType.EVDAS, Constants.AlertType.EVDAS_ADHOC]) {
            execConfiguration = ExecutedEvdasConfiguration.get(id)
        } else if (alertType == Constants.AlertType.LITERATURE) {
            execConfiguration = ExecutedLiteratureConfiguration.get(id)
        } else {
            execConfiguration = ExecutedConfiguration.get(id)
        }
        descString="Removed ${alertType} Alert"
        String removedUsers = execConfiguration.removedUsers ?: ""
        execConfiguration.removedUsers = "${removedUsers},${userService.currentUserId}"
        execConfiguration.save()
        createAuditLogForAlertRemove(execConfiguration,descString)
    }

    def createAuditLogForAlertRemove(def execConfig,String description){
        Map auditTrail = [:]
        auditTrail.category = AuditTrail.Category.UPDATE.toString()
        auditTrail.entityValue = "${description}: ${execConfig.name}"
        auditTrail.entityName = "dashboard"
        auditTrail.moduleName = "Dashboard: Alert Widget"
        auditTrail.description = description
        List<Map> auditChildMap = []

                Map entryMap = [:]
                entryMap.newValue = "No"
                entryMap.oldValue = "Yes"
                entryMap.propertyName = "Available on Dashboard"
                auditChildMap.add(entryMap)
        signalAuditLogService.createAuditLog(auditTrail, auditChildMap)
    }

    String fetchProductName(String productName) {
        String finalName = productName
        List finalNames = []
        List list = productName?.split(":")
        String firstName = list?list[0]:productName
        if (firstName?.endsWith("(Product Group)")) {
            firstName.split("\\(Product Group\\), ").each {
                String name = it
                if (name?.endsWith("(Product Group)"))
                    name = name?.substring(0,it.size()-15)
                finalNames.add(name?.substring(0, name?.lastIndexOf('(')))
            }
            finalName = finalNames?.join(", ")
            if(list.size() > 1){
                finalName = finalName + ":" + list[1]
            }
        }
        return finalName
    }

    @Transactional
    def dashboardCountSync() {
        Map<Long, Map<String, Integer>> oldDashboardCountData = new HashMap<>()
        Sql sql
        try {

            List<Map> userDispCaseCountList = []
            Map<Long, Integer> userDispCaseCountsMap = [:]
            List<Map> groupDispCaseCountList = []
            Map<Long, Map> groupDispCaseCountsMap = [:]
            List<Map> userDueDateCaseCountsList = []
            Map<String, Integer> userDueDateCaseCountsMap = [:]
            List<Map> dueDateGroupCaseCountList = []
            Map<Long, Map> dueDateGroupCaseCountsMap = [:]

            List<Map> userDispPECountList = []
            Map<Long, Integer> userDispPECountsMap = [:]
            List<Map> groupDispPECountList = []
            Map<Long, Map> groupDispPECountsMap = [:]
            List<Map> userDueDatePECountsList = []
            Map<String, Integer> userDueDatePECountsMap = [:]
            List<Map> dueDateGroupPECountList = []
            Map<Long, Map> dueDateGroupPECountsMap = [:]

            sql = new Sql(dataSource)


            sql.eachRow(SignalQueryHelper.singleCaseAlert_dashboard_by_disposition(true), []) { row ->
                userDispCaseCountList.add([dispositionId: row[0], assignedToId: row[1], workflowGroupId: row[2], count: row[3]])
            }

            sql.eachRow(SignalQueryHelper.singleCaseAlert_dashboard_by_disposition(false), []) { row ->
                groupDispCaseCountList.add([dispositionId: row[0], assignedToGroupId: row[1], workflowGroupId: row[2], count: row[3]])
            }

            sql.eachRow(SignalQueryHelper.singleCaseAlert_dashboard_due_date(true), []) { row ->
                userDueDateCaseCountsList.add([due_date: row[0], assignedToId: row[1], workflowGroupId: row[2], count: row[3]])
            }

            sql.eachRow(SignalQueryHelper.singleCaseAlert_dashboard_due_date(false), []) { row ->
                dueDateGroupCaseCountList.add([due_date: row[0], assignedToGroupId: row[1], workflowGroupId: row[2], count: row[3]])
            }

            sql.eachRow(SignalQueryHelper.aggCaseAlert_dashboard_by_disposition(true), []) { row ->
                userDispPECountList.add([dispositionId: row[0], assignedToId: row[1], workflowGroupId: row[2], count: row[3]])
            }

            sql.eachRow(SignalQueryHelper.aggCaseAlert_dashboard_by_disposition(false), []) { row ->
                groupDispPECountList.add([dispositionId: row[0], assignedToGroupId: row[1], workflowGroupId: row[2], count: row[3]])
            }

            sql.eachRow(SignalQueryHelper.aggCaseAlert_dashboard_due_date(true), []) { row ->
                userDueDatePECountsList.add([due_date: row[0], assignedToId: row[1], workflowGroupId: row[2], count: row[3]])
            }

            sql.eachRow(SignalQueryHelper.aggCaseAlert_dashboard_due_date(false), []) { row ->
                dueDateGroupPECountList.add([due_date: row[0], assignedToGroupId: row[1], workflowGroupId: row[2], count: row[3]])
            }

            User.list().each { user ->

                UserDashboardCounts userDashboardCounts = UserDashboardCounts.get(user.id)
                if (userDashboardCounts) {
                    Map<String, Integer> oldDashboardCountMap = dashboardCounts(user)
                    oldDashboardCountData.put(user.id, oldDashboardCountMap)
                    sql.execute("""DELETE FROM USER_DASHBOARD_COUNTS WHERE USER_ID = ${user.id}""")
                }

                Group workflowgroup = user.workflowGroup
                List<Long> groupIdList = user.groups.findAll { it.groupType != GroupType.WORKFLOW_GROUP }.id
                groupIdList.each { id ->
                    Map dispCountMap = [:]
                    Map dueDateCountMap = [:]
                    Map dispPECountMap = [:]
                    Map dueDatePECountMap = [:]

                    groupDispCaseCountList.findAll { it.assignedToGroupId == id && it.workflowGroupId == workflowgroup.id }.each {
                        dispCountMap.put(it.dispositionId, it.count)
                    }

                    if (dispCountMap) {
                        groupDispCaseCountsMap.put(id, dispCountMap)
                    }

                    dueDateGroupCaseCountList.findAll { it.assignedToGroupId == id && it.workflowGroupId == workflowgroup.id }.each {
                        dueDateCountMap.put(it.due_date, it.count)
                    }

                    if (dueDateCountMap) {
                        dueDateGroupCaseCountsMap.put(id, dueDateCountMap)
                    }

                    groupDispPECountList.findAll { it.assignedToGroupId == id && it.workflowGroupId == workflowgroup.id }.each {
                        dispPECountMap.put(it.dispositionId, it.count)
                    }

                    if (dispPECountMap) {
                        groupDispPECountsMap.put(id, dispPECountMap)
                    }

                    dueDateGroupPECountList.findAll { it.assignedToGroupId == id && it.workflowGroupId == workflowgroup.id }.each {
                        dueDatePECountMap.put(it.due_date, it.count)
                    }

                    if (dueDatePECountMap) {
                        dueDateGroupPECountsMap.put(id, dueDatePECountMap)
                    }
                }

                userDispCaseCountList.findAll { it.assignedToId == user.id && it.workflowGroupId == workflowgroup.id }.each {
                    userDispCaseCountsMap.put(it.dispositionId, it.count)
                }

                userDueDateCaseCountsList.findAll { it.assignedToId == user.id && it.workflowGroupId == workflowgroup.id }.each {
                    userDueDateCaseCountsMap.put(it.due_date, it.count)
                }

                userDispPECountList.findAll { it.assignedToId == user.id && it.workflowGroupId == workflowgroup.id }.each {
                    userDispPECountsMap.put(it.dispositionId, it.count)
                }

                userDueDatePECountsList.findAll { it.assignedToId == user.id && it.workflowGroupId == workflowgroup.id }.each {
                    userDueDatePECountsMap.put(it.due_date, it.count)
                }

                sql.execute("""INSERT INTO 
                                                  user_dashboard_counts (user_id, user_disp_case_counts, group_disp_case_counts, user_due_date_case_counts, group_due_date_case_counts, 
                                                  user_disppecounts, group_disppecounts, user_due_datepecounts,group_due_datepecounts)
                                                  VALUES (${user.id}, ${userDispCaseCountsMap ? new JsonBuilder(userDispCaseCountsMap).toPrettyString() : null},
                                                          ${groupDispCaseCountsMap ? new JsonBuilder(groupDispCaseCountsMap).toPrettyString() : null},
                                                          ${userDueDateCaseCountsMap ? new JsonBuilder(userDueDateCaseCountsMap).toPrettyString() : null},
                                                          ${dueDateGroupCaseCountsMap ? new JsonBuilder(dueDateGroupCaseCountsMap).toPrettyString() : null},
                                                          ${userDispPECountsMap ? new JsonBuilder(userDispPECountsMap).toPrettyString() : null},
                                                          ${groupDispPECountsMap ? new JsonBuilder(groupDispPECountsMap).toPrettyString() : null},
                                                          ${userDueDatePECountsMap ? new JsonBuilder(userDueDatePECountsMap).toPrettyString() : null},
                                                          ${dueDateGroupPECountsMap ? new JsonBuilder(dueDateGroupPECountsMap).toPrettyString() : null})
                                           """)
                userDispCaseCountsMap.clear()
                groupDispCaseCountsMap.clear()
                userDueDateCaseCountsMap.clear()
                dueDateGroupCaseCountsMap.clear()
                userDispPECountsMap.clear()
                groupDispPECountsMap.clear()
                userDueDatePECountsMap.clear()
                dueDateGroupPECountsMap.clear()


            }
        } catch (Exception ex) {
            ex.printStackTrace()
            throw ex
        } finally {
            sql.close()
        }
        oldDashboardCountData
    }

    def saveAuditLog(Map oldDashboardCountData) {

        try {
            User.list().each { user ->
                UserDashboardCounts userDashboardCounts = UserDashboardCounts.get(user.id)
                if (userDashboardCounts) {
                    Map<String, Integer> oldDashboardCountMap = oldDashboardCountData.get(user.id)
                    Map<String, Integer> newDashboardCountMap = dashboardCounts(user)


                    boolean isDispCountsChanged = false
                    List<AuditTrailChild> auditTrailChildList = new ArrayList<>()

                    if (oldDashboardCountMap?.adhoc != newDashboardCountMap?.adhoc) {
                        isDispCountsChanged = true
                        AuditTrailChild auditTrailChild = new AuditTrailChild()
                        auditTrailChild.propertyName = "AD HOC review count changed"
                        auditTrailChild.oldValue = oldDashboardCountMap.adhoc
                        auditTrailChild.newValue = newDashboardCountMap.adhoc
                        auditTrailChildList.add(auditTrailChild)
                    }
                    if (oldDashboardCountMap?.actionItems != newDashboardCountMap?.actionItems) {
                        isDispCountsChanged = true
                        AuditTrailChild auditTrailChild = new AuditTrailChild()
                        auditTrailChild.propertyName = "Action items count"
                        auditTrailChild.oldValue = oldDashboardCountMap.actionItems
                        auditTrailChild.newValue = newDashboardCountMap.actionItems
                        auditTrailChildList.add(auditTrailChild)
                    }
                    if (oldDashboardCountMap?.signals != newDashboardCountMap?.signals) {
                        isDispCountsChanged = true
                        AuditTrailChild auditTrailChild = new AuditTrailChild()
                        auditTrailChild.propertyName = "Signals count"
                        auditTrailChild.oldValue = oldDashboardCountMap.signals
                        auditTrailChild.newValue = newDashboardCountMap.signals
                        auditTrailChildList.add(auditTrailChild)
                    }
                    if (oldDashboardCountMap?.evdas != newDashboardCountMap?.evdas) {
                        isDispCountsChanged = true
                        AuditTrailChild auditTrailChild = new AuditTrailChild()
                        auditTrailChild.propertyName = "EVDAS case review count"
                        auditTrailChild.oldValue = oldDashboardCountMap.evdas
                        auditTrailChild.newValue = newDashboardCountMap.evdas
                        auditTrailChildList.add(auditTrailChild)
                    }
                    if (oldDashboardCountMap?.single != newDashboardCountMap?.single) {
                        isDispCountsChanged = true
                        AuditTrailChild auditTrailChild = new AuditTrailChild()
                        auditTrailChild.propertyName = "Individual case review count"
                        auditTrailChild.oldValue = oldDashboardCountMap.single
                        auditTrailChild.newValue = newDashboardCountMap.single
                        auditTrailChildList.add(auditTrailChild)
                    }
                    if (oldDashboardCountMap?.aggregate != newDashboardCountMap?.aggregate) {
                        isDispCountsChanged = true
                        AuditTrailChild auditTrailChild = new AuditTrailChild()
                        auditTrailChild.propertyName = "Aggregate case review count"
                        auditTrailChild.oldValue = oldDashboardCountMap.aggregate
                        auditTrailChild.newValue = newDashboardCountMap.aggregate
                        auditTrailChildList.add(auditTrailChild)
                    }

                    if (isDispCountsChanged && auditTrailChildList.size() > 0) {
                        AuditTrail auditTrail = new AuditTrail()
                        auditTrail.category = AuditTrail.Category.UPDATE.toString()
                        auditTrail.applicationName = "PV Signal"
                        auditTrail.entityId = user.id
                        auditTrail.entityName = "Dashboard counts changed for user ${user.username} "
                        auditTrail.username = "System"
                        auditTrail.save(flush: true)
                        log.info('Audit log saved for user : ' + user)

                        for (AuditTrailChild auditTrailChild : auditTrailChildList) {
                            auditTrailChild.auditTrail = auditTrail
                            auditTrailChild.save(flush: true)
                        }
                    }

                }

            }
        } catch (Exception ex) {
            log.error('Error occurred during saving audit log.')
            ex.printStackTrace()
            throw ex
        }

    }
    List<List<Map>> prepareChartJsonMap(Map scaCountMap,Map aggregateCountMap,Map adhocCountMap){
        List futureDateList = prepareDateList(scaCountMap,aggregateCountMap,adhocCountMap,"NEW")
        List pastDateList = prepareDateList(scaCountMap,aggregateCountMap,adhocCountMap,"OLD")
        List currentDateList = prepareDateList(scaCountMap,aggregateCountMap,adhocCountMap,"CURRENT")

        List<List<Map>> chartJson = [
                [name: "Due Date in Future", data: futureDateList],
                [name: "Due Today", data: currentDateList],
                [name: "Passed Due Date", data: pastDateList]
        ]
        chartJson
    }
    List prepareDateList(Map scaCountMap,Map aggregateCountMap, Map adhocCountMap, String key){
        List genericDateList = []
        genericDateList.add(scaCountMap.get(key))
        genericDateList.add(aggregateCountMap.get(key))
        genericDateList.add(adhocCountMap.get(key))
        genericDateList
    }

    private String getProducts(DashboardDTO dashboardDTO) {
        String product = ""
        List data = []
        try {
            if ((StringUtils.isBlank(dashboardDTO.product)) && (StringUtils.isBlank(dashboardDTO.dataMiningVariable))) {
                if (StringUtils.isNotBlank(dashboardDTO.study)) {
                    return getAllProductNameFieldFromJson(dashboardDTO.study)
                }
            }

            if (StringUtils.equalsIgnoreCase(dashboardDTO.type, Constants.AlertType.INDIVIDUAL_CASE_SERIES) || StringUtils.equalsIgnoreCase(dashboardDTO.type, Constants.AlertType.ICR_ADHOC)) {
                if (dashboardDTO.productGroupSelection) {
                    data.addAll(getGroupNameFieldFromJson(dashboardDTO.productGroupSelection).split(","))
                }
                if (dashboardDTO.productSelection) {
                    if (getPvsProductDictionaryService().isLevelGreaterThanProductLevel(dashboardDTO) && Holders.config.custom.qualitative.fields.enabled) {
                        data.addAll(getCacheService().getUpperHierarchyProductDictionaryCache(dashboardDTO.id)?.split(","))
                    } else {
                        data.addAll(getAllProductNameFieldFromJson(dashboardDTO.productSelection).split(","))
                    }
                }
                return data.sort().join(", ")
            }
            if (StringUtils.equalsIgnoreCase(dashboardDTO.type, Constants.AlertType.AGGREGATE_NEW)) {
                if (dashboardDTO.productSelection || dashboardDTO.productGroupSelection) {
                    return getProductSelection(dashboardDTO)
                }
            }
            if (StringUtils.equalsIgnoreCase(dashboardDTO.type, Constants.AlertType.AGGREGATE_ADHOC)) {
                if (dashboardDTO.dataMiningVariable) {
                    String miningVariable = dashboardDTO.dataMiningVariable
                    if (dashboardDTO.productSelection || dashboardDTO.productGroupSelection) {
                        product = miningVariable + "(" + getProductSelection(dashboardDTO) + ")"
                    } else {
                        product = miningVariable
                    }
                } else {
                    product = getProductSelection(dashboardDTO)
                }
                return product
            }
            if (StringUtils.equalsIgnoreCase(dashboardDTO.type, Constants.AlertType.EVDAS) || StringUtils.equalsIgnoreCase(dashboardDTO.type, Constants.AlertType.EVDAS_ADHOC)) {
                return getProductSelection(dashboardDTO)
            }
            if (StringUtils.equalsIgnoreCase(dashboardDTO.type, Constants.AlertType.LITERATURE)) {
                return getProductSelection(dashboardDTO)
            }
        } catch (Exception ex) {
            log.error("Exception occurred while generating products", ex)
            return ""
        }
        return product
    }

    private String getProductSelection(DashboardDTO dashboardDTO) {
        String productSelection = ""
        if (Holders.config.custom.qualitative.fields.enabled) {
            productSelection = (getPvsProductDictionaryService().isLevelGreaterThanProductLevel(dashboardDTO) ? getCacheService().getUpperHierarchyProductDictionaryCache(dashboardDTO.id) : getNameFieldFromJson(dashboardDTO.productSelection)) ?: getGroupNameFieldFromJson(dashboardDTO.productGroupSelection)
        } else {
            productSelection = getNameFieldFromJson(dashboardDTO.productSelection) ?: getGroupNameFieldFromJson(dashboardDTO.productGroupSelection)
        }
        return productSelection
    }
}
