package com.rxlogix

import com.rxlogix.audit.AuditTrail
import com.rxlogix.config.*
import com.rxlogix.dto.CommentDTO
import com.rxlogix.dto.RequestCommentDTO
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.signal.*
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.events.EventPublisher
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import groovyx.net.http.Method
import org.hibernate.FetchMode
import org.hibernate.Session
import org.hibernate.StaleStateException
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.criterion.Restrictions
import org.hibernate.jdbc.Work

import javax.sql.DataSource
import java.sql.*
import java.text.SimpleDateFormat
import java.util.Date

@Transactional
class AlertCommentService implements EventPublisher{

    def CRUDService
    def userService
    def activityService
    def cacheService
    def literatureActivityService
    def literatureAlertService
    def reportIntegrationService
    def signalDataSourceService
    def messageSource
    def dataSource
    def reportExecutorService
    def sessionFactory
    def signalAuditLogService

    def getComment(params) {
        def comment = null

        switch (params.alertType) {
            case Constants.AlertConfigType.SINGLE_CASE_ALERT:
                boolean isFaers = params.isFaers == "true"? true: false
                comment = listSingleCaseComments(params.caseId as Long, params.caseVersion as Integer, isFaers)
                break
            case Constants.AlertConfigType.AGGREGATE_CASE_ALERT:
                Configuration configuration
                if (params.configId) {
                    configuration = Configuration.get(params.configId as Long)
                }
                comment = listAggregateCaseComments(params.productId as BigInteger, params.eventName, configuration, params.executedConfigId as Long)
                break
            case Constants.AlertConfigType.LITERATURE_SEARCH_ALERT:
                comment = listLiteratureAlertComments(params.articleId)
                break
            case Constants.AlertConfigType.SIGNAL_MANAGEMENT:
                ValidatedSignal validatedSignal = ValidatedSignal.findById(params.validatedSignalId)
                comment = listSignalComments(validatedSignal)
                break
            case Constants.AlertConfigType.EVDAS_ALERT:
                EvdasConfiguration evdasConfiguration = EvdasConfiguration.get(params.configId as Long)
                ExecutedEvdasConfiguration executedEvdasConfiguration = ExecutedEvdasConfiguration.get(params.executedConfigId as Long)
                comment = listEvdasComments(params.productName, params.eventName, evdasConfiguration, executedEvdasConfiguration)
                break
            case Constants.AlertConfigType.AD_HOC_ALERT:
                AdHocAlert adHocAlert = AdHocAlert.findById(Long.parseLong(params.adhocAlertId))
                comment = listAdhocComments(adHocAlert)
        }

        User currentUserObj = userService.getUser()
        String currentUser = currentUserObj?.username
        String timeZone = currentUserObj?.preference?.timeZone
        comment = comment?.toDto(currentUser, timeZone)

        // get comment's history for aggregate alerts
        def commentHistoryList = null
        if (params.alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            commentHistoryList = listAggCommentsHistory(params.productId as Long, params.eventName, params.configId as Long, params.ptCode as Long)
        }

        return [comment : comment, commentHistoryList : commentHistoryList]
    }

    GlobalCaseCommentMapping listSingleCaseComments(Long caseId, Integer versionNum, boolean isFaers= false) {
        def domain = isFaers? GlobalCaseCommentMapping."faers":GlobalCaseCommentMapping."pva"
        def comment = domain.createCriteria().get {
            eq('caseId', caseId)
            eq('versionNum', versionNum)
            order('commentSeqNum', 'desc')
            maxResults(1)
        } as GlobalCaseCommentMapping
        if(comment){
            comment.comments = comment.comments?:""
        }
        comment
    }

    AlertComment listAggregateCaseComments(BigInteger productId, String eventName, Configuration configuration, Long exeConfigId) {
        AlertComment.createCriteria().get {
            eq('productId', productId)
            eq('eventName', eventName)
            eq('configId', configuration?.id)
            eq('exConfigId',exeConfigId)
            eq('alertType', Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
            order('lastUpdated', 'desc')
            maxResults(1)
        } as AlertComment
    }

    AlertComment listLiteratureAlertComments(String articleId) {
        AlertComment.createCriteria().get {
            eq('articleId', articleId)
            eq('alertType', Constants.AlertConfigType.LITERATURE_SEARCH_ALERT)
            order('lastUpdated', 'desc')
            maxResults(1)
        } as AlertComment
    }

    AlertComment getLatestComments(String productName, String eventName, Long configId, String type) {
        List<AlertComment> acList = cacheService.getAlertComments(productName, eventName, configId, type)?.sort { AlertComment ac1, AlertComment ac2 ->
            ac1.lastUpdated <=> ac2.lastUpdated
        }

        acList ? acList[0] : null
    }

    AlertComment getLatestSingleCaseAlertComment(String caseNumber, Long configId) {
        List<AlertComment> acList = cacheService.getSingleCaseAlertComments(caseNumber, configId)?.sort { AlertComment ac1, AlertComment ac2 ->
            ac1.lastUpdated <=> ac2.lastUpdated
        }
        acList ? acList[0] : null
    }

    AlertComment listEvdasComments(String productName, String eventName, EvdasConfiguration evdasConfiguration, ExecutedEvdasConfiguration executedEvdasConfiguration) {
        AlertComment.createCriteria().get {
            eq('productName', productName)
            eq('eventName', eventName)
            eq('configId', evdasConfiguration?.id)
            le('exConfigId', executedEvdasConfiguration?.id)
            order("exConfigId", "desc")
            order("id", "desc")
            maxResults(1)
        } as AlertComment
    }

    def listAdhocComments(AdHocAlert adHocAlert) {
        def list
        if(adHocAlert.comments) {
            list = adHocAlert.comments?.last()
        }
        return list
    }

    def listSignalComments(ValidatedSignal validatedSignal) {
        def list
        if(validatedSignal.comments) {
            list = validatedSignal.comments.last()
        }
        return list
    }

    def listTopicComments(Topic topic) {
        def list
        if(topic.comments) {
            list = topic.comments.last()
        }
        return list
    }

    def getExecConfigurationById(Long configId) {
        if(configId != null) {
            ExecutedConfiguration.get(configId)
        }
    }

    def getEvdasExecConfigurationById(Long configId) {
        if(configId != null) {
            ExecutedEvdasConfiguration.get(configId)
        }
    }
    def getLiteratureExecConfigurationById(Long configId) {
        if(configId != null) {
            ExecutedLiteratureConfiguration.get(configId)
        }
    }

    def getActivityByType(def type) {
        if(type != null) {
            ActivityType.findByValue(type)
        }
    }
    List<AlertComment> getAlertCommentList(AlertComment alertComment){
        AlertComment.createCriteria().list {
            ne("id", alertComment?.id)
            switch (alertComment.alertType){
                case Constants.AlertConfigType.AGGREGATE_CASE_ALERT:
                    eq('productId', alertComment?.productId)
                    eq('eventName', alertComment?.eventName)
                    eq('configId', alertComment?.configId)
                    eq('alertType', Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
                    break
                case Constants.AlertConfigType.EVDAS_ALERT:
                    eq('productName', alertComment?.productName)
                    eq('eventName', alertComment?.eventName)
                    eq('configId', alertComment?.configId)
                    eq('alertType', Constants.AlertConfigType.EVDAS_ALERT)
                    break
                case Constants.AlertConfigType.LITERATURE_SEARCH_ALERT:
                    eq('articleId', alertComment.articleId)
                    eq('alertType', Constants.AlertConfigType.LITERATURE_SEARCH_ALERT)
                    break
            }
        } as List<AlertComment>
    }

    def deleteComment(def comment) {
        def returnVal = false
        try {
            //In case the flow is coming from the validated signal.
            def validatedAlertMapping = ValidatedAlertCommentMapping.findAllByComment(comment)
            if (validatedAlertMapping) {
                validatedAlertMapping.each {
                    it.delete()
                }
            }
            List<AlertComment> commentList = getAlertCommentList(comment)
            if(commentList){
                commentList.each {it.delete()}
            }
            comment.delete()

            cacheService.deleteAlertComment(comment)

            returnVal = true
        } catch(Exception ex) {
            ex.printStackTrace()
        }
        returnVal
    }

    def deleteCommentForAlert(def comment) {
        def returnVal = false
        try {
            List<AlertComment> commentList = getAlertCommentList(comment)
            if(commentList){
                commentList.each {
                    it.delete()
                }
            }
            comment.delete()

            cacheService.deleteAlertComment(comment)

            returnVal = true
        } catch(Exception ex) {
            ex.printStackTrace()
        }
        returnVal
    }

    def alertCommentListForProductSummary(alertType, productName, eventList) {
        def alertCommentList = AlertComment.findAllByAlertTypeAndProductNameAndEventNameInList(alertType, productName, eventList)
        alertCommentList
    }

    def saveValidatedSignalComments(AlertComment comment, signalId) {
        ValidatedSignal validatedSignal = ValidatedSignal.findById(signalId)
        comment.createdBy = userService.getUser().username
        comment.modifiedBy = userService.getUser().username
        validatedSignal.addToComments(comment)
        try {
            CRUDService.update(validatedSignal)
            activityService.createActivityForSignal(validatedSignal, null, "Comment '" + comment.comments + "' is added",
                    ActivityType.findByValue(ActivityTypeValue.CommentAdded), null, userService.getUser(), [])
            cacheService.insertAlertCommentHistory(comment)
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex)
        }
    }

    void saveTopicComments(AlertComment comment, topicId) {
        Topic topic = Topic.findById(topicId)
        comment.createdBy = userService.getUser().username
        comment.modifiedBy = userService.getUser().username
        topic.addToComments(comment)
        try {
            CRUDService.update(topic)
            activityService.createActivityForSignal(topic, null, "Comment '" + comment.comments + "' is added",
                    ActivityType.findByValue(ActivityTypeValue.CommentAdded), null, userService.getUser(), [])
            cacheService.insertAlertCommentHistory(comment)
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex)
        }
    }

    void saveAdhocAlertComments(AlertComment comment, alertId) {
        AdHocAlert adHocAlert = AdHocAlert.findById(alertId)
        comment.createdBy = userService.getUser().username
        comment.modifiedBy = userService.getUser().username
        adHocAlert.addToComments(comment)
        try {
            CRUDService.update(adHocAlert)
            activityService.create(alertId, ActivityType.findByValue(ActivityTypeValue.CommentAdded),
                    userService.getUser(), "Comment '" + comment.comments + "' is added", null)
            cacheService.insertAlertCommentHistory(comment)
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex)
        }
    }

    void saveLiteratureAlertComments(AlertComment comment, String oldComment, alertId) {

        def literatureAlert = LiteratureAlert.findById(alertId)
        if (!literatureAlert)
            literatureAlert = ArchivedLiteratureAlert.findById(alertId)
        comment.createdBy = userService.getUser().username
        comment.modifiedBy = userService.getUser().username
        comment.articleId = literatureAlert.articleId
        CRUDService.save(comment)

        try {
            if(comment.comments == '') {
                literatureActivityService.createLiteratureActivity(literatureAlert.exLitSearchConfig,literatureAlertService.getActivityByType(ActivityTypeValue.CommentRemoved),
                        userService.getUser(), "Comment '" + oldComment +"' is removed", "", literatureAlert.productSelection, literatureAlert.eventSelection, literatureAlert.assignedTo,literatureAlert.searchString, literatureAlert.articleId, literatureAlert.assignedToGroup)
            } else{
                literatureActivityService.createLiteratureActivity(literatureAlert.exLitSearchConfig,literatureAlertService.getActivityByType(ActivityTypeValue.CommentAdded),
                        userService.getUser(), "Comment '" + comment.comments + "' is added", "", literatureAlert.productSelection, literatureAlert.eventSelection, literatureAlert.assignedTo,literatureAlert.searchString, literatureAlert.articleId, literatureAlert.assignedToGroup)
            }

        } catch (Exception ex) {
            log.error(ex.getMessage())
        }
    }

    def createAlertComment(params) {
        boolean isSuccess = true
        Map<Long, Long> dtIndexCommentIdMap = [:]
        try {
            JsonSlurper jsonSlurper = new JsonSlurper()
            String alertList = params.caseJsonObjArray
            List alertListObj = jsonSlurper.parseText(alertList)
            Long configId
            Long executedConfigId
            List<Map> activityList = []
            Boolean isFaers
            Boolean isVaers
            Boolean isVigibase
            def alertType
            def caseIdToCommentMap
            if(alertListObj[0]?.configId){
                configId = Long.valueOf(alertListObj[0].configId)
                isFaers = Boolean.valueOf(alertListObj[0].isFaers)
                isVaers = Boolean.valueOf(alertListObj[0].isVaers)
                executedConfigId = Long.valueOf(alertListObj[0].executedConfigId)
                isVigibase = Boolean.valueOf(alertListObj[0].isVigibase)
                alertType = alertListObj[0].alertType
            }

            if (alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                //done for PVS-44967 performance impact is already discussed with product team
                def caseAndversionList = alertListObj?.collect {
                    [caseId: it.caseId as Long, versionNum: it.caseVersion as Integer]
                }
                List<GlobalCaseCommentMapping> globalCommentoldObjects = getGlobalCommentByCaseList(caseAndversionList, isFaers, false)
                caseIdToCommentMap = globalCommentoldObjects.collectEntries {
                    [(it.caseId): it.comments]
                }
            }

            List<CommentDTO> globalComments = []
            List<AlertComment> commentList = []
            Map<Long, String> oldCommentMap = [:]
            alertListObj?.each {
                AlertComment commentObj = (it.getAt('commentId') != null) ? AlertComment.get(it.getAt('commentId')) ?: new AlertComment() : new AlertComment()
                String oldComment = commentObj?.comments
                commentObj?.configId = configId
                commentObj?.alertType = it.getAt('alertType')
                commentObj?.caseNumber = it.getAt('caseNumber')
                commentObj?.comments = it.getAt('comments')?.length() == 8000 ? it.getAt('comments') + " " : it.getAt('comments')
                commentObj?.productFamily = it.getAt('productFamily')
                commentObj?.eventName = (it.getAt('eventName')) ?: (it.getAt('pt'))
                commentObj?.productName = it.getAt('productName')
                commentObj?.caseId = it.getAt('caseId') as Long
                commentObj?.exConfigId = it.getAt('executedConfigId') as Long
                commentObj?.productId = (it.getAt('productId') != 'null') ? it.getAt('productId') : null
                commentObj?.ptCode = (it.getAt('ptCode') != 'null') ? it.getAt('ptCode') : null
                commentObj?.commentTemplateId = it.getAt('commentTemplateId') != 'none' ? it.getAt('commentTemplateId') : null

                // use this for activities for single case alerts
                String oldGlobalComment = ""

                if (it.getAt('validatedSignalId')) {
                    saveValidatedSignalComments(commentObj, it.getAt('validatedSignalId'))
                } else if (it.getAt('adhocAlertId')) {
                    saveAdhocAlertComments(commentObj, it.getAt('adhocAlertId'))
                } else if (it.getAt('literatureAlertId')) {
                    saveLiteratureAlertComments(commentObj,oldComment, it.getAt('literatureAlertId'))
                } else {
                    if (it.getAt('alertType') == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                        commentObj.dataSource = isVaers ? "vaers" : isFaers ? "faers" : isVigibase ? "vigibase" : "pva"
                        def auditCategory = AuditTrail.Category.UPDATE.toString()
                        oldGlobalComment = caseIdToCommentMap.get(it.caseId as Long) ?: ""
                        if (oldGlobalComment == "" && commentObj?.comments != "") {
                            auditCategory = AuditTrail.Category.INSERT.toString()
                        } else if (commentObj?.comments == "") {
                            auditCategory = AuditTrail.Category.DELETE.toString()
                        }

                        globalComments.add(convertToCommentDTO(commentObj, it))
                        if ((oldGlobalComment != "" || commentObj?.comments != "") && oldGlobalComment != commentObj?.comments) {
                            signalAuditLogService.saveAuditTrailForComments(oldGlobalComment, commentObj?.comments, commentObj?.exConfigId,
                                    auditCategory, "(${it.getAt('caseNumber')}(${it.getAt('followUpNumber')}))")
                        }
                    }else if (it.getAt('alertType') == Constants.AlertConfigType.EVDAS_ALERT) {
                        commentObj = userService.setOwnershipAndModifier(commentObj) as AlertComment
                        if (it.getAt('commentId') == null) {
                            oldCommentMap.put(commentObj?.id, oldComment)
                        }
                        commentList.add(commentObj)
                    } else {
                        saveCommentForTriggeredAlerts(commentObj, it)
                    }
                    try {
                        oldComment = oldGlobalComment == "" ? oldComment : oldGlobalComment
                        Activity activity = createActivityForComment(commentObj, it, executedConfigId, oldComment)
                        activityList.add([execConfigId: it.getAt('executedConfigId'), activity: activity])
                    } catch (Exception exception) {
                        log.error(exception.getMessage())
                    }
                }
                dtIndexCommentIdMap.put(it.getAt('dtIndex') as Long,commentObj.id as Long)
            }
            if(commentList.size()>1){
                batchPersistAlertComments(commentList)
                commentList.each {AlertComment commentObj->
                    if(commentObj.id == null){
                        signalAuditLogService.saveAuditTrailForEvdasComments(oldCommentMap.getOrDefault(commentObj.id, ""), commentObj.comments, commentObj.exConfigId, AuditTrail.Category.INSERT.toString(),"(${commentObj.productName}(${commentObj.eventName}))" )
                    }
                    dtIndexCommentIdMap.put(
                            alertListObj.find({
                                it.eventName == commentObj.eventName && it.productName == commentObj.productName
                            })?.dtIndex as Long,
                            AlertComment.findByAlertTypeAndExConfigIdAndProductNameAndEventName('EVDAS Alert', commentObj.exConfigId, commentObj.productName, commentObj.eventName).id as Long
                    )
                }
            }else if(commentList.size()==1){
                AlertComment sAlertComment = commentList[0]
                CRUDService.save(sAlertComment)
                dtIndexCommentIdMap.put(
                        alertListObj.find({
                            it.eventName == sAlertComment.eventName && it.productName == sAlertComment.productName
                        })?.dtIndex as Long,
                        sAlertComment.id as Long
                )
            }
            if(globalComments.size() > 0){
                RequestCommentDTO requestCommentDTO = new RequestCommentDTO()
                requestCommentDTO.commentDTOS = globalComments
                requestCommentDTO.faersFlag = isFaers
                requestCommentDTO.vaersFlag = isVaers
                requestCommentDTO.vigibaseFlag = isVigibase
                isSuccess = saveGlobalCommentInMart(requestCommentDTO)
            }
            if (activityList && alertListObj[0].getAt('alertType') == Constants.AlertConfigType.EVDAS_ALERT) {
                notify 'evdas.activity.history.event.published', [activityList: activityList, isBulkUpdate: true]
            } else if (isSuccess && activityList && alertListObj[0].getAt('alertType') == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                notify 'activity.case.history.event.published', [activityList: activityList, isBulkUpdate: true]
            } else if (activityList) {
                notify 'activity.case.history.event.published', [activityList: activityList, isBulkUpdate: true]
            }
        } catch (Exception e) {
            isSuccess = false
            dtIndexCommentIdMap = [:]
            e.printStackTrace()
        }
        ["isSuccess": isSuccess, "dtIndexCommentIdMap": dtIndexCommentIdMap]
    }

    /**
     * Saves the comments for the alerts.
     * @param commentObj
     * @param alertObj
     * @return
     */
    def saveCommentForTriggeredAlerts(AlertComment commentObj, def alertObj) {

        //Save the alert comment
        CRUDService.save(commentObj)
        if (alertObj.getAt('alertType') == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
            cacheService.insertSingleCaseAlertCommentHistory(commentObj)
        } else if(alertObj.getAt('alertType') == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            if (AggregateCaseAlert.get((Integer) commentObj.caseId)){
                AggregateCaseAlert aca = AggregateCaseAlert.get((Integer) commentObj.caseId)
                AlertCommentHistory alertCommentHistory = new AlertCommentHistory()
                alertCommentHistory.aggAlertId = (Integer) commentObj.caseId
                alertCommentHistory.comments = commentObj.comments
                alertCommentHistory.eventName = commentObj.eventName
                alertCommentHistory.productName = commentObj.productName
                alertCommentHistory.configId = commentObj.configId
                alertCommentHistory.execConfigId=commentObj.exConfigId
                alertCommentHistory.productId = commentObj.productId
                alertCommentHistory.eventId = commentObj.ptCode
                // @ADDITION OF PRODUCT NAME AND EVENT NAME FOR FETCHING ALERT HISTORY AS PEC LEVEL
                alertCommentHistory.alertName = Configuration.get(commentObj.configId).name
                alertCommentHistory.period = getPeriodForCommentHistory(aca.executedAlertConfiguration)
                CRUDService.save(alertCommentHistory)
                aca.addToAlertCommentHistory(alertCommentHistory)
            }else if(ArchivedAggregateCaseAlert.get((Integer) commentObj.caseId)){
                ArchivedAggregateCaseAlert aca = ArchivedAggregateCaseAlert.get((Integer) commentObj.caseId)
                AlertCommentHistory alertCommentHistory = new AlertCommentHistory()
                alertCommentHistory.aggAlertId = (Integer) commentObj.caseId
                alertCommentHistory.comments = commentObj.comments
                // @ADDITION OF PRODUCT NAME AND EVENT NAME FOR FETCHING ALERT HISTORY AS PEC LEVEL
                alertCommentHistory.eventName = commentObj.eventName
                alertCommentHistory.productName = commentObj.productName
                alertCommentHistory.configId = commentObj.configId
                alertCommentHistory.productId = commentObj.productId
                alertCommentHistory.eventId = commentObj.ptCode
                alertCommentHistory.alertName = Configuration.get(commentObj.configId).name
                alertCommentHistory.period = getPeriodForCommentHistory(aca.executedAlertConfiguration)
                alertCommentHistory.execConfigId=commentObj.exConfigId
                CRUDService.save(alertCommentHistory)
                aca.addToAlertCommentHistory(alertCommentHistory)
            }
        }
    }

    def batchPersistAlertComments(List<AlertComment> commentList){
        String insertCommentQuery = "INSERT INTO ALERT_COMMENT(id, version, date_created, last_updated, config_Id, alert_type, case_number, comments, product_family, event_name, product_name, case_id, ex_config_id, product_id, pt_code, comment_template_id, created_By, modified_By) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
        String updateCommentQuery = "UPDATE ALERT_COMMENT SET last_updated = ?, config_Id = ?, alert_type = ?, case_number = ?, comments = ?, product_family = ?, event_name = ?, product_name = ?, case_id = ?, ex_config_id = ?, product_id = ?, pt_code = ?, comment_template_id = ?, modified_By = ? WHERE id = ?"

        Session session = sessionFactory.currentSession
        log.info("Batch insertion/update of alert comments started")
        Date start = new Date()
        session.doWork(new Work() {
            public void execute(Connection connection) throws SQLException {
                PreparedStatement insertStatement = null
                PreparedStatement updateStatement = null
                try {
                    insertStatement = connection.prepareStatement(insertCommentQuery)
                    updateStatement = connection.prepareStatement(updateCommentQuery)
                    int batchSize = Holders.config.signal.batch.size
                    int count = 0
                    commentList.each { AlertComment commentObj ->
                        if (commentObj.id == null) { // Insert new record
                            Long nextId = getNextSequenceValue(connection)
                            setInsertStatementParameters(insertStatement, nextId, commentObj)
                            insertStatement.addBatch()
                        } else { // Update existing record
                            setUpdateStatementParameters(updateStatement, commentObj)
                            updateStatement.setLong(15, commentObj.id)
                            updateStatement.addBatch()
                        }

                        count += 1
                        if (count == batchSize) {
                            insertStatement.executeBatch()
                            updateStatement.executeBatch()
                            count = 0
                            hasUpdates = false
                        }
                    }
                    insertStatement.executeBatch()
                    updateStatement.executeBatch()
                } catch (Exception e) {
                    log.error("Batch insert/update failed", e)
                    throw e
                } finally {
                    insertStatement.close()
                    updateStatement.close()
                    session.flush()
                    session.clear()
                }
            }
        })
        Date stop = new Date()
        TimeDuration td = TimeCategory.minus(stop, start)
        log.info("Batch insertion/update of alert comments completed in " + td)
    }

    private Long getNextSequenceValue(Connection connection) throws SQLException {
        String sequenceQuery = "SELECT alert_comment_sequence.NEXTVAL FROM ALERT_COMMENT"
        PreparedStatement sequenceStatement = connection.prepareStatement(sequenceQuery)
        ResultSet resultSet = sequenceStatement.executeQuery()
        Long nextId = null
        if (resultSet.next()) {
            nextId = resultSet.getLong(1)
        }
        resultSet.close()
        sequenceStatement.close()
        return nextId
    }

    private void setInsertStatementParameters(PreparedStatement statement, Long nextId, AlertComment commentObj) throws SQLException {
        statement.setLong(1, nextId)
        statement.setLong(2, 0)
        statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()))
        statement.setTimestamp(4, new Timestamp(System.currentTimeMillis()))
        statement.setLong(5, commentObj.configId)
        statement.setString(6, commentObj.alertType)
        statement.setString(7, commentObj.caseNumber)
        statement.setString(8, commentObj.comments)
        statement.setString(9, commentObj.productFamily)
        statement.setString(10, commentObj.eventName)
        statement.setString(11, commentObj.productName)
        statement.setLong(12, commentObj.caseId)
        statement.setLong(13, commentObj.exConfigId)
        statement.setObject(14, commentObj.productId)
        statement.setLong(15, commentObj.ptCode)
        statement.setString(16, commentObj.commentTemplateId)
        statement.setString(17, commentObj.createdBy)
        statement.setString(18, commentObj.modifiedBy)
    }

    private void setUpdateStatementParameters(PreparedStatement statement, AlertComment commentObj) throws SQLException {
        statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()))
        statement.setLong(2, commentObj.configId)
        statement.setString(3, commentObj.alertType)
        statement.setString(4, commentObj.caseNumber)
        statement.setString(5, commentObj.comments)
        statement.setString(6, commentObj.productFamily)
        statement.setString(7, commentObj.eventName)
        statement.setString(8, commentObj.productName)
        statement.setLong(9, commentObj.caseId)
        statement.setLong(10, commentObj.exConfigId)
        statement.setObject(11, commentObj.productId)
        statement.setLong(12, commentObj.ptCode)
        statement.setString(13, commentObj.commentTemplateId)
        statement.setString(14, commentObj.modifiedBy)
    }

    Activity createActivityForComment(AlertComment commentObj, alertObj, long executedConfigId,String oldComment) {
        //Prepare the data for activity log.
        def caseAlert
        if (commentObj.alertType == Constants.AlertConfigType.EVDAS_ALERT) {
            ExecutedEvdasConfiguration executedConfiguration = ExecutedEvdasConfiguration.get(executedConfigId)
            caseAlert = EvdasAlert.findByPtAndExecutedAlertConfiguration(commentObj.eventName, executedConfiguration)
            if (Objects.isNull(caseAlert)) {
                caseAlert = ArchivedEvdasAlert.findByPtAndExecutedAlertConfiguration(commentObj.eventName, executedConfiguration)
            }
        } else {
            ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(executedConfigId)
            if (commentObj.alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                caseAlert = SingleCaseAlert.findByCaseIdAndExecutedAlertConfiguration(alertObj.getAt('caseId') as long, executedConfiguration)
                if (Objects.isNull(caseAlert)) {
                    caseAlert = ArchivedSingleCaseAlert.findByCaseIdAndExecutedAlertConfiguration(alertObj.getAt('caseId') as long, executedConfiguration)
                }
            } else if (commentObj.alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
                caseAlert = AggregateCaseAlert.findByPtAndExecutedAlertConfiguration(commentObj.eventName, executedConfiguration)
                if (Objects.isNull(caseAlert)) {
                    caseAlert = ArchivedAggregateCaseAlert.findByPtAndExecutedAlertConfiguration(commentObj.eventName, executedConfiguration)
                }
            }
        }
        User loggedInUser = cacheService.getUserByUserId( userService.getCurrentUserId() )
        Map attrs = [ comments: commentObj.comments ]
        String header = '', body = ''

        if (commentObj.comments == '') {
            body = messageSource.getMessage("app.label.comment.alert.removed", [oldComment] as Object[], Locale.default)
            header = ActivityTypeValue.CommentRemoved.value
        } else if (oldComment != null) {
            body = messageSource.getMessage("app.label.comment.alert.updated", [oldComment, commentObj.comments] as Object[], Locale.default)
            header = ActivityTypeValue.CommentUpdated.value
        } else if (oldComment == null) {
            body = messageSource.getMessage("app.label.comment.alert.added", [commentObj.comments] as Object[], Locale.default)
            header = ActivityTypeValue.CommentAdded.value
        }

        Activity activity = activityService.createActivityBulkUpdate(cacheService.getActivityTypeByValue(header),
                loggedInUser, body,
                "", attrs, alertObj.getAt('productName'), (alertObj.getAt('eventName')) ?: (alertObj.getAt('pt')), caseAlert?.assignedTo, alertObj.caseNumber, caseAlert?.assignedToGroup)
        activity
    }

    def updateSignalComment(params) {
        boolean isSuccess = true

        def commentObj
        def caseAlert
        if(params.alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT){
            boolean isFaers = params.isFaers == "true"? true: false
            commentObj = listSingleCaseComments(params.caseId as Long, params.caseVersion as Integer, isFaers)
        }
        else{
            commentObj = AlertComment.get(params.id)
            commentObj.commentTemplateId = params.commentTemplateId != "none" ? params.commentTemplateId : null
        }
        def oldComment = commentObj.comments
        if(params.alertType != Constants.AlertConfigType.EVDAS_ALERT){
            commentObj.comments = params.comment == null ? "" : params.comment?.length() == 8000 ? params.comment + " " : params.comment
        }
        try {
            if(params.alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT){
                List<CommentDTO> globalComments = []
                CommentDTO commentDTO = convertToUpdateCommentDTO(commentObj, params)
                if(commentDTO)
                    globalComments.add(commentDTO)
                if(globalComments.size() > 0){
                    RequestCommentDTO requestCommentDTO = new RequestCommentDTO()
                    requestCommentDTO.commentDTOS = globalComments
                    boolean faersFlag = false
                    if(params.configId) {
                        Configuration config = Configuration.findById(params.long("configId"))
                        faersFlag = config.selectedDatasource == Constants.DataSource.FAERS ? true: false

                    }
                    requestCommentDTO.faersFlag = faersFlag
                    isSuccess = saveGlobalCommentInMart(requestCommentDTO)
                    String category = (commentObj.comments=='')?AuditTrail.Category.DELETE.toString():(oldComment=='')?AuditTrail.Category.INSERT.toString():AuditTrail.Category.UPDATE.toString()
                    if (isSuccess) {
                        signalAuditLogService.saveAuditTrailForComments((String) oldComment, (String) commentObj.comments, Long.parseLong(params.executedConfigId), (String) category, "(${params.get('caseNumber')}(${params.get('followUpNumber')}))")
                    }
                }
            }else {
                CRUDService.update(commentObj)
                if(params.alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT){
                    if(AggregateCaseAlert.get((Integer) commentObj.caseId ? commentObj.caseId : params.caseId as Integer)){
                        AggregateCaseAlert aca = AggregateCaseAlert.get((Integer) commentObj.caseId ? commentObj.caseId : params.caseId as Integer)
                        AlertCommentHistory alertCommentHistory = new AlertCommentHistory()
                        alertCommentHistory.aggAlertId = (Integer) commentObj.caseId ? commentObj.caseId : params.caseId as Integer
                        alertCommentHistory.comments = commentObj.comments?.length() == 8000 ? commentObj.comments + " " : commentObj.comments
                        // @ADDITION OF PRODUCT NAME AND EVENT NAME FOR FETCHING ALERT HISTORY AS PEC LEVEL
                        alertCommentHistory.eventName = commentObj.eventName
                        alertCommentHistory.productName = commentObj.productName
                        alertCommentHistory.configId = commentObj.configId
                        alertCommentHistory.productId = commentObj.productId
                        alertCommentHistory.eventId = commentObj.ptCode
                        alertCommentHistory.alertName = Configuration.get(commentObj.configId).name
                        alertCommentHistory.period = getPeriodForCommentHistory(aca.executedAlertConfiguration)
                        CRUDService.save(alertCommentHistory)
                        aca.addToAlertCommentHistory(alertCommentHistory)
                    }else{
                        ArchivedAggregateCaseAlert aca = ArchivedAggregateCaseAlert.get((Integer) commentObj.caseId ? commentObj.caseId : params.caseId as Integer)
                        AlertCommentHistory alertCommentHistory = new AlertCommentHistory()
                        alertCommentHistory.aggAlertId = (Integer) commentObj.caseId ? commentObj.caseId : params.caseId as Integer
                        alertCommentHistory.comments = commentObj.comments?.length() == 8000 ? commentObj.comments + " " : commentObj.comments
                        // @ADDITION OF PRODUCT NAME AND EVENT NAME FOR FETCHING ALERT HISTORY AS PEC LEVEL
                        alertCommentHistory.eventName = commentObj.eventName
                        alertCommentHistory.productName = commentObj.productName
                        alertCommentHistory.configId = commentObj.configId
                        alertCommentHistory.productId = commentObj.productId
                        alertCommentHistory.eventId = commentObj.ptCode
                        alertCommentHistory.alertName = Configuration.get(commentObj.configId).name
                        alertCommentHistory.period = getPeriodForCommentHistory(aca.executedAlertConfiguration)
                        CRUDService.save(alertCommentHistory)
                        aca.addToAlertCommentHistory(alertCommentHistory)
                    }
                }
            }
            if (params.validatedSignalId) {
                def validatedSignal = ValidatedSignal.findById(params.validatedSignalId)
                activityService.createActivityForSignal(validatedSignal, null, "Comment '" + oldComment + "' is updated with new comment '" + params.comment + "'",
                        ActivityType.findByValue(ActivityTypeValue.CommentUpdated), null, userService.getUser(), [:])
            } else if (params.literatureAlertId) {
                def literatureAlert
                literatureAlert = LiteratureAlert.findById(params.literatureAlertId)
                if (!literatureAlert)
                    literatureAlert = ArchivedLiteratureAlert.findById(params.literatureAlertId)
                literatureActivityService.createLiteratureActivity(literatureAlert?.exLitSearchConfig, literatureAlertService.getActivityByType(ActivityTypeValue.CommentUpdated),
                        userService.getUser(), "Comment '" + oldComment + "' is updated with new comment '" + params.comment + "'", "", literatureAlert.productSelection, literatureAlert.eventSelection, literatureAlert.assignedTo, literatureAlert.searchString, literatureAlert.articleId, literatureAlert.assignedToGroup)

            } else if (params.topicId) {
                def topic = Topic.findById(params.topicId)
                activityService.createActivityForTopic(topic, null, "Comment '" + oldComment + "' is updated with new comment '" + params.comment + "'",
                        ActivityType.findByValue(ActivityTypeValue.CommentUpdated), null, userService.getUser(), [:])
            } else if (params.adhocAlertId) {
                activityService.create(params.adhocAlertId, ActivityType.findByValue(ActivityTypeValue.CommentUpdated),
                        userService.getUser(), "Comment '" + oldComment + "' is updated with new comment '" + params.comment + "'", null)
            } else if (params.executedConfigId && isSuccess) {
                def executedConfigId = Long.parseLong(params.executedConfigId)
                def executedConfig

                if (params.alertType == Constants.AlertConfigType.EVDAS_ALERT) {
                    if(executedConfigId > commentObj.exConfigId) {
                        AlertComment newCommentObj = new AlertComment(commentObj.properties)
                        newCommentObj.alertName = params.alertName
                        newCommentObj.comments = params.comment == null ? "" : params.comment
                        newCommentObj.caseId = params.caseId as Long
                        newCommentObj.exConfigId = executedConfigId as Long
                        oldComment = commentObj.comments
                        CRUDService.save(newCommentObj)
                    }
                    else {
                        commentObj.comments = params.comment == null ? "" : params.comment
                        CRUDService.update(commentObj)
                    }
                    executedConfig = getEvdasExecConfigurationById(executedConfigId)
                    caseAlert = EvdasAlert.findByPtAndExecutedAlertConfiguration(commentObj.eventName, executedConfig)
                    if (Objects.isNull(caseAlert)) {
                        caseAlert = ArchivedEvdasAlert.findByPtAndExecutedAlertConfiguration(commentObj.eventName, executedConfig)
                    }
                    activityService.createActivityForEvdas(executedConfig, getActivityByType(ActivityTypeValue.CommentUpdated),
                            userService.getUser(), "Comment '" + oldComment + "' is updated with new comment '" + params.comment + "'",
                            "", [comments: params.comment], commentObj.productName, commentObj.eventName, caseAlert?.assignedTo, commentObj.caseNumber, caseAlert?.assignedToGroup)

                } else {
                    executedConfig = getExecConfigurationById(executedConfigId)
                    if (params.alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT){
                        caseAlert = SingleCaseAlert.findByCaseIdAndExecutedAlertConfiguration(params.caseId as long, executedConfig)
                        if (Objects.isNull(caseAlert)) {
                            caseAlert = ArchivedSingleCaseAlert.findByCaseIdAndExecutedAlertConfiguration(params.caseId as long, executedConfig)
                        }
                    }
                    else if (params.alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT){
                        caseAlert = AggregateCaseAlert.findByPtAndExecutedAlertConfiguration(commentObj.eventName, executedConfig)
                        if (Objects.isNull(caseAlert)) {
                            caseAlert = ArchivedAggregateCaseAlert.findByPtAndExecutedAlertConfiguration(commentObj.eventName, executedConfig)
                        }
                    }
                    if(commentObj.comments == ""){
                        activityService.createActivity(executedConfig, getActivityByType(ActivityTypeValue.CommentRemoved),
                                userService.getUser(), "Comment [" + oldComment + "] is removed",
                                "", [comments: commentObj.comments], commentObj.productName, commentObj.eventName, caseAlert?.assignedTo, commentObj.caseNumber, caseAlert?.assignedToGroup)

                    }else if(oldComment == ""){
                        activityService.createActivity(executedConfig, getActivityByType(ActivityTypeValue.CommentAdded),
                                userService.getUser(), "Comment '" + params.comment + "' is added",
                                "", [comments: commentObj.comments], commentObj.productName, commentObj.eventName, caseAlert?.assignedTo, commentObj.caseNumber, caseAlert?.assignedToGroup)
                    }else {
                        activityService.createActivity(executedConfig, getActivityByType(ActivityTypeValue.CommentUpdated),
                                userService.getUser(), "Comment '" + oldComment + "' is updated with new comment '" + params.comment + "'",
                                "", [comments: commentObj.comments], commentObj.productName, commentObj.eventName, caseAlert?.assignedTo, commentObj.caseNumber, caseAlert?.assignedToGroup)
                    }
                }

            }
        } catch (Exception e) {
            isSuccess = false
            log.error(e.getMessage(), e)
        }
        isSuccess
    }

    List<Map> getUpdatedCommentMap(String caseNumber, String versionNum, Boolean isFaers=false) {
        def domain = isFaers? GlobalCaseCommentMapping."faers":GlobalCaseCommentMapping."pva"
        def comment = domain.createCriteria().get {
            eq('caseNumber', caseNumber)
            eq('versionNum', versionNum as Integer)
            order('commentSeqNum', 'desc')
            maxResults(1)
        } as GlobalCaseCommentMapping
        [[comments: comment?comment.comments:""]]
    }

    def getAlertCommentByConfigIdList(Set alertExConfigIdList,String alertType) {
        List result = []
        Sql sql = new Sql(dataSource)
        try {
            def sqlQuery = """
        SELECT EVENT_NAME as eventName,
        PRODUCT_ID as productId,
        COMMENTS as comments,
        ID as id,
        PRODUCT_NAME as productName,
        EX_CONFIG_ID as exConfigId
        FROM
        ALERT_COMMENT
        WHERE EX_CONFIG_ID in ${alertExConfigIdList.size() > 1 ? '(' + (1..alertExConfigIdList.size()).collect {'?'}.join(',') + ')' : '?'}
        AND ALERT_TYPE = '${alertType}'
         """
            result = sql.rows(sqlQuery, alertExConfigIdList.toArray()).collect {
                [productId:it.productId as Long, eventName: it.eventName, comments: it.comments.getAsciiStream().getText(), id: it.id as Long, productName: it.productName, exConfigId: it.exConfigId]
            }
        } catch (Exception ex){
            ex.printStackTrace()
        } finally {
            sql?.close()
        }
        return result
    }

    def getGlobalCommentByCaseList(List<Map> caseVerionList, boolean isFaers = false, boolean orderRequired = true) {
        def domain = isFaers ? GlobalCaseCommentMapping.faers : GlobalCaseCommentMapping.pva
        List<GlobalCaseCommentMapping> result = []
        if (caseVerionList.isEmpty()) {
            return result
        }
        def batchSize = 500
        caseVerionList.collate(batchSize).each { batch ->
            def caseIdsBatch = batch.collect { it.caseId }
            def versionNumsBatch = batch.collect { it.versionNum }

            def batchResult = domain.createCriteria().list {
                'eq'('tenantId', 1)
                'in'('caseId', caseIdsBatch)
                'in'('versionNum', versionNumsBatch)

                if (orderRequired) {
                    order("commentSeqNum", "desc")
                }
                // Enable batch fetching
                fetchMode('comments', FetchMode.LAZY)

                //removed unwanted limit to batch
            }
            result.addAll(batchResult)
            sessionFactory.currentSession.clear()
        }
        result
    }

    List<AlertComment> getAlertCommentByArticleIdList(List<String> articleIdList) {
        AlertComment.createCriteria().list {
            or {
                articleIdList.collate(1000).each {
                    'in'('articleId', it)
                }
            }
            order("lastUpdated", "desc")
        } as List<AlertComment>
    }

    private CommentDTO convertToCommentDTO(AlertComment commentObj, def it){
        User user = userService.getUser()
        String timeZone = user?.preference?.timeZone
        CommentDTO globalComment = new CommentDTO()
        globalComment = globalComment.getCommentDtoObject(globalComment, commentObj, it)
        globalComment.updatedBy = user?.username
        globalComment.createdBy = user?.username
        globalComment.createdDate = DateUtil.stringFromDate(new Date(), SqlGenerationService.DATETIME_FMT, "UTC")
        globalComment.updatedDate = DateUtil.stringFromDate(new Date(), SqlGenerationService.DATETIME_FMT, "UTC")
        globalComment
    }

    private CommentDTO convertToUpdateCommentDTO(GlobalCaseCommentMapping commentObj, Map params){
        User user = userService.getUser()
        String timeZone = user?.preference?.timeZone
        CommentDTO globalComment = new CommentDTO()
        globalComment = globalComment.getUpdateCommentDtoObject(globalComment, commentObj, params)
        globalComment.updatedBy = user?.username
        globalComment.createdBy = user?.username
        globalComment.createdDate = DateUtil.stringFromDate(new Date(), SqlGenerationService.DATETIME_FMT, "UTC")
        globalComment.updatedDate = DateUtil.stringFromDate(new Date(), SqlGenerationService.DATETIME_FMT, "UTC")
        globalComment
    }

    private AlertComment persistGlobalComment(CommentDTO commentObj, def it){
        AlertComment globalComment = new AlertComment()
        globalComment.versionNum = commentObj.versionNum
        globalComment.createdBy = commentObj.createdBy
        globalComment.configId =  commentObj.configId
        globalComment.caseId = commentObj.caseId
        globalComment.alertType = it.getAt('alertType')
        globalComment.dateCreated = new Date()
        globalComment.exConfigId = commentObj.exConfigId
        globalComment.comments = it.getAt('comments')
        globalComment.caseNumber = it.getAt('caseNumber')
        globalComment.followUpNum = commentObj.followUpNum
        globalComment.syncFlag = false
        globalComment.dataSource = commentObj.dataSource
        globalComment.productId = commentObj.productId
        globalComment.productName = commentObj.productName
        globalComment.ptCode = commentObj.ptCode
        globalComment.productFamily = commentObj.productFamily
        globalComment.eventName = commentObj.eventName
        CRUDService.save(globalComment)
    }

    private boolean saveGlobalCommentInMart(RequestCommentDTO requestCommentDTO){
        HashMap resultMap = new HashMap()
        boolean result = false;
        try {
            def url = Holders.config.pvcc.api.url
            def path = Holders.config.pvcc.api.path.comment
            def query = JsonOutput.toJson(requestCommentDTO)
            resultMap = reportIntegrationService.postData(url, path, query, Method.POST)
            result = resultMap["result"]?.data?:false
        }catch(Exception ex){
            ex.printStackTrace()
        }
        return result
    }

    def getCommentHistory(params){
        String timezone = userService?.getCurrentUserPreference()?.timeZone
        def url = Holders.config.pvcc.api.url
        def path = Holders.config.pvcc.api.path.comment
        def query = [tenantId: 1, caseId: params.caseId, versionNum: params.versionNum, isFaers:params.isFaers, isHistory: true]
        Map result = reportIntegrationService.get(url, path, query)
        Map history = result.data ?: [:]
        User currentUser = userService.getUser()
        def response = history?.data?.collect{
            [alertName: it.alertName, caseNumber: params.caseNumber, caseVersion: it.versionNum, followUpNumber: it.followUpNum,
            oldCommentTxt: it.oldCommentTxt, newCommentTxt: it.newCommentTxt, updatedBy: getUserFullName(it.updatedBy),
             updatedDate: DateUtil.fromStringDateTimeZone(it.createdDate, timezone), caseId: it.caseId]
        }

    }

    def listAggCommentsHistory(Long productId, String eventName, Long configId, Long eventId) {
        String userTimezone = userService.getCurrentUserPreference()?.timeZone ?: Constants.UTC

        def list = AlertCommentHistory.createCriteria().list {
            eq('productId',productId as BigInteger)
            eq('configId',configId)
            'or'{
                eq('eventId',eventId as Integer)
                eq('eventName',eventName)
            }
        }.collect{it.toDto(userTimezone)}.sort({-it.id})

        return list
    }

    private String fromStringDate(String inputDate, User currentUser){
        Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(inputDate)
        String timeZone = currentUser.preference.timeZone
        return DateUtil.StringFromDate(date, DateUtil.DATEPICKER_FORMAT_AM_PM_2, timeZone)
    }

    private String getUserFullName(String userName){
        return userService.getUserByUsername(userName)?.getFullName()?: Constants.Commons.SYSTEM
    }

    private boolean updateCommentSyncSuccess(RequestCommentDTO requestCommentDTO){
        try {
            List<Long> commentIds = requestCommentDTO.commentDTOS*.commentId
            bulkUpdateCommentSync(commentIds)
        }catch(Exception ex){
            ex.printStackTrace()
            return false
        }
        return true
    }

    @Transactional
    void bulkUpdateCommentSync(List<Long> commentIdList) {
        commentIdList.collate(1000).each {
            AlertComment.executeUpdate("Update AlertComment set syncFlag = :synced " +
                    "where id in (:commentIdList) ",
                    [synced: true, commentIdList: it])
        }
    }

    void insertExistingCommentInDb(String db) {
        List<Map> existingComments =  getExistingCommentsForSCA(db)
        Sql sql = new Sql(signalDataSourceService.getReportConnection(db))
        try{
            if (existingComments) {
                log.debug("Existing comments Insertion Starts")
                sql.withBatch(500, "insert into ALERT_COMMENT_MIG(case_number, config_id, CREATED_BY, " +
                        "DATE_CREATED, LAST_UPDATED, MODIFIED_BY, DATA_SOURCE, COMMENTS, EVENT_NAME,PRODUCT_FAMILY," +
                        "PRODUCT_ID,PRODUCT_NAME,PT_CODE, ALERT_NAME) " +
                        "values (:val0, :val1, :val2, :val3, :val4, :val5, :val6, :val7, :val8, :val9, :val10, :val11, :val12, :val13)",
                        { preparedStatement ->
                            existingComments.each {
                                preparedStatement.addBatch(val0: it.caseNumber, val1: it.configId, val2: it.createdBy, val3: it.dateCreated,
                                        val4: it.lastUpdated, val5: it.modifiedBy, val6: it.dataSource, val7: it.comments,
                                        val8: it.eventName, val9: it.productFamily, val10: it.productId, val11: it.productName, val12: it.ptCode, val13: it.alertName)
                            }
                        })
                log.debug("Existing comments Insertion Completed")
            }
        }catch(Exception ex){
            ex.printStackTrace()
        }finally{
            sql?.close()
        }
    }

    private List getExistingCommentsForSCA(String db){
        List<Map> existingComments = AlertComment.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("caseNumber","caseNumber")
                property("configId","configId")
                property("createdBy","createdBy")
                property("dateCreated","dateCreated")
                property("lastUpdated","lastUpdated")
                property("modifiedBy","modifiedBy")
                property("dataSource","dataSource")
                property("comments","comments")
                property("caseId","caseId")
                property("ptCode","ptCode")
                property("productId","productId")
                property("productName","productName")
                property("eventName","eventName")
                property("productFamily","productFamily")
                property("alertName","alertName")

            }
            eq('dataSource', db)
            or {
                isNull('syncFlag')
                eq('syncFlag', false)
            }
            not{
                isNull('caseNumber')
            }
            eq('alertType', 'Single Case Alert')
        } as List<Map>
    }

    private void insertMartCommentsInPVS(List caseComments, String dbType) {
        Sql sql = new Sql(dataSource)
        try{
            if (caseComments) {
                log.info("Comments Insertion Starts")
                sql.withBatch(500, "insert into ALERT_COMMENT(id, version, alert_type, case_number, config_id, CREATED_BY, " +
                        "DATE_CREATED, LAST_UPDATED, MODIFIED_BY, DATA_SOURCE, COMMENTS, sync_flag,  ALERT_NAME, PRODUCT_FAMILY, " +
                        "PRODUCT_NAME, PRODUCT_ID,  EVENT_NAME, PT_CODE, case_id, version_num, follow_up_num) " +
                        "values (:val0, :val1, :val2, :val3, :val4, :val5, :val6, :val7, :val8, :val9, :val10, :val11, " +
                        ":val12, :val13, :val14, :val15, :val16, :v17, :val18, :val19, :val20)",
                        { preparedStatement ->
                            caseComments.each {
                                Long id
                                sql.eachRow("select HIBERNATE_SEQUENCE.nextval from dual", []) { row ->
                                    id = row[0]
                                }
                                preparedStatement.addBatch(val0: id, val1: 0, val2: Constants.AlertConfigType.SINGLE_CASE_ALERT, val3: it.caseNumber,
                                        val4: it.configId, val5: it.createdBy, val6: it.dateCreated,
                                        val7: it.lastUpdated, val8: it.modifiedBy, val9: dbType, val10: it.comments, val11: true,
                                        val12: it.alertName, val13: it.productFamily, val14: it.productName,
                                        val15: it.productId,  val16: it.eventName, val17: it.ptCode,
                                        val18: it.caseId,  val19: it.versionNum, val20: it.followUpNum)
                            }
                        })
                log.info("Comments Insertion Completed")
            }
        }catch(Exception ex){
            ex.printStackTrace()
        }finally{
            sql?.close()
        }
    }

    void runMigrationInDB(String db){
        Sql sql = null
        try {
            sql = new Sql(signalDataSourceService.getReportConnection(db))
            sql.call("{call P_PVS_COMMENTS_MIG()}")
        } catch (Exception ex) {
            log.error(ex.printStackTrace())
        } finally {
            sql?.close()
        }
    }

    def getPeriodForCommentHistory(ExecutedConfiguration executedConfiguration){
        String dateRange
        if(executedConfiguration.executedAlertDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE && executedConfiguration?.selectedDatasource == Constants.DataSource.FAERS){
            dateRange = DateUtil.toDateString(executedConfiguration.executedAlertDateRangeInformation.dateRangeStartAbsolute) +
                    " - " + (reportExecutorService.getFaersDateRange().faersDate).substring(13)
        } else if(executedConfiguration.executedAlertDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE && executedConfiguration?.selectedDatasource == Constants.DataSource.VAERS){
            dateRange = DateUtil.toDateString(executedConfiguration.executedAlertDateRangeInformation.dateRangeStartAbsolute) +
                    " - " + (reportExecutorService.getVaersDateRange(1).vaersDate).substring(13)
        } else {
            dateRange = DateUtil.toDateString(executedConfiguration.executedAlertDateRangeInformation.dateRangeStartAbsolute) +
                    " - " + DateUtil.toDateString(executedConfiguration.executedAlertDateRangeInformation.dateRangeEndAbsolute)
        }
        dateRange
    }

    def deleteBulkComments(params) {
        boolean isSuccess = true
        Map<Long, Long> dtIndexCommentIdMap = [:]

        try {
            JsonSlurper jsonSlurper = new JsonSlurper()
            List alertListObj = jsonSlurper.parseText(params.caseJsonObjArray) as List

            alertListObj.each {
                AlertComment comment = (it.getAt('commentId') != null) ? AlertComment.get(it.getAt('commentId')) ?: new AlertComment() : new AlertComment()

                if (deleteCommentForAlert(comment)) {
                    String commentStr = comment?.comments
                    createActivityForCommentRemoval(comment, commentStr, it)
                    cacheService.insertAlertCommentHistory(comment)
                }
                // commentId would be updated in case of bulk-delete
                dtIndexCommentIdMap.put(it.getAt('dtIndex') as Long,null)
            }
        } catch (Exception e) {
            isSuccess = false
            dtIndexCommentIdMap = [:]
            log.error("Error deleting bulk comments", e)
        }

        return ["isSuccess": isSuccess, "dtIndexCommentIdMap": dtIndexCommentIdMap]
    }

    private void createActivityForCommentRemoval(AlertComment comment, String commentStr, Map entityMap) {
        if (entityMap.adhocAlertId) {
            activityService.create(entityMap.adhocAlertId, ActivityType.findByValue(ActivityTypeValue.CommentRemoved),
                    userService.getUser(), "Comment [${commentStr}] is removed", null)
        } else if (entityMap.topicId) {
            Topic topic = Topic.findById(entityMap.topicId)
            activityService.createActivityForTopic(topic, null, "Comment [${commentStr}] is removed",
                    ActivityType.findByValue(ActivityTypeValue.CommentRemoved), null, userService.getUser(), [:])
        } else if (entityMap.executedConfigId) {
            Long executedConfigId = Long.parseLong(entityMap?.executedConfigId as String)
            if (entityMap.alertType == Constants.AlertConfigType.EVDAS_ALERT) {
                def executedConfig = getEvdasExecConfigurationById(executedConfigId)
                def caseAlert = EvdasAlert.findByPtAndExecutedAlertConfiguration(comment.eventName, executedConfig) ?: ArchivedEvdasAlert.findByPtAndExecutedAlertConfiguration(comment.eventName, executedConfig)
                activityService.createActivityForEvdas(executedConfig, getActivityByType(ActivityTypeValue.CommentRemoved),
                        userService.getUser(), "Comment [${commentStr}] is removed", "", [comments: comment.comments],
                        comment.productName, comment.eventName, caseAlert?.assignedTo, comment.caseNumber, caseAlert?.assignedToGroup)
            } else if (entityMap.alertType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
                def executedConfig = getLiteratureExecConfigurationById(executedConfigId)
                def literatureAlert = LiteratureAlert.findById(entityMap.literatureAlertId) ?: ArchivedLiteratureAlert.findById(entityMap.literatureAlertId)
                literatureActivityService.createLiteratureActivity(executedConfig, literatureAlertService.getActivityByType(ActivityTypeValue.CommentRemoved),
                        userService.getUser(), "Comment [${commentStr}] is removed", "", comment.productName, comment.eventName, userService.getUser(),
                        literatureAlert?.searchString, literatureAlert?.articleId)
            } else {
                def executedConfig = getExecConfigurationById(executedConfigId)
                def caseAlert
                if (AggregateCaseAlert.get((Integer) (comment.caseId ?: entityMap.caseId))) {
                    AggregateCaseAlert aca = AggregateCaseAlert.get((Integer) (comment.caseId ?: entityMap.caseId))
                    AlertCommentHistory alertCommentHistory = new AlertCommentHistory(
                            aggAlertId: (Integer) (comment.caseId ?: entityMap.caseId),
                            comments: "",
                            eventName: comment.eventName,
                            productName: comment.productName,
                            configId: comment.configId,
                            productId: comment.productId,
                            alertName: Configuration.get(comment.configId).name,
                            period: getPeriodForCommentHistory(aca.executedAlertConfiguration),
                            eventId: comment.ptCode
                    )
                    CRUDService.save(alertCommentHistory)
                    caseAlert = AggregateCaseAlert.findByPtAndExecutedAlertConfiguration(comment.eventName, executedConfig)
                } else {
                    ArchivedAggregateCaseAlert aca = ArchivedAggregateCaseAlert.get((Integer) (comment.caseId ?: entityMap.caseId))
                    AlertCommentHistory alertCommentHistory = new AlertCommentHistory(
                            aggAlertId: (Integer) (comment.caseId ?: entityMap.caseId),
                            comments: "",
                            eventName: comment.eventName,
                            productName: comment.productName,
                            configId: comment.configId,
                            productId: comment.productId,
                            alertName: Configuration.get(comment.configId).name,
                            period: getPeriodForCommentHistory(aca.executedAlertConfiguration)
                    )
                    CRUDService.save(alertCommentHistory)
                    caseAlert = ArchivedAggregateCaseAlert.findByPtAndExecutedAlertConfiguration(comment.eventName, executedConfig)
                }
                activityService.createActivity(executedConfig, getActivityByType(ActivityTypeValue.CommentRemoved),
                        userService.getUser(), "Comment [${commentStr}] is removed", "", [comments: comment.comments],
                        comment.productName, comment.eventName, caseAlert?.assignedTo, comment.caseNumber, caseAlert.assignedToGroup)
            }
        }
    }

}
