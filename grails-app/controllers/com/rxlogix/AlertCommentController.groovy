package com.rxlogix

import com.rxlogix.config.ActivityType
import com.rxlogix.config.ActivityTypeValue
import com.rxlogix.config.ArchivedLiteratureAlert
import com.rxlogix.config.CommentTemplate
import com.rxlogix.config.Configuration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.LiteratureAlert
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.AlertComment
import com.rxlogix.signal.AlertCommentHistory
import com.rxlogix.signal.ArchivedAggregateCaseAlert
import com.rxlogix.signal.EmergingIssue
import com.rxlogix.signal.Topic
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.FileNameCleaner
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.json.JsonSlurper
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import java.text.DateFormat
import java.text.SimpleDateFormat

@Secured(["isAuthenticated()"])
class AlertCommentController {
    def alertCommentService
    def CRUDService
    def userService
    def activityService
    def cacheService
    def literatureAlertService
    def literatureActivityService
    def dynamicReportService
    def alertService
    def signalAuditLogService

    def listComments() {
        def commentInfo = alertCommentService.getComment(params)
        render(commentInfo as JSON)
    }

    def saveComment() {
        Map responseMap = alertCommentService.createAlertComment(params)
        String message;
        if(responseMap.isSuccess)
            message="Comment(s) added successfully."
        else
            message="An Error occurred while Processing request."
        render([success:responseMap.isSuccess,dtIndexIdMap:responseMap.dtIndexCommentIdMap,message:message] as JSON)
    }

    def updateComment() {
        def isSuccess = alertCommentService.updateSignalComment(params)
        String message;
        if(isSuccess)
            message="Comment(s) updated successfully."
        else
            message="An Error occurred while Processing request."
        render([success:isSuccess,message:message] as JSON)
    }

    def deleteComment() {
        AlertComment comment
        def caseAlert
        if(params.alertType != Constants.AlertConfigType.SINGLE_CASE_ALERT){
            comment = AlertComment.get(params.id)
        }
        if(params.alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT){
            def isSuccess = alertCommentService.updateSignalComment(params)
            render(contentType: 'text/json', text: ['success': isSuccess] as JSON)
        } else if (alertCommentService.deleteComment(comment)) {
            String commentStr = comment?.comments
            if (params.validatedSignalId) {
                ValidatedSignal validatedSignal = ValidatedSignal.findById(params.validatedSignalId)
                activityService.createActivityForSignal(validatedSignal, null, "Comment [" + commentStr + "] is removed",
                        ActivityType.findByValue(ActivityTypeValue.CommentRemoved), null, userService.getUser(), [])
            } else if (params.adhocAlertId) {
                activityService.create(params.adhocAlertId, ActivityType.findByValue(ActivityTypeValue.CommentRemoved),
                        userService.getUser(), "Comment [" + commentStr + "] is removed", null)
            } else if (params.topicId) {
                Topic topic = Topic.findById(params.topicId)
                activityService.createActivityForTopic(topic, null, "Comment [" + commentStr + "] is removed",
                        ActivityType.findByValue(ActivityTypeValue.CommentRemoved), null, userService.getUser(), [:])
            } else if (params.executedConfigId) {
                Long executedConfigId = Long.parseLong(params.executedConfigId)
                def executedConfig
                if (params.alertType == Constants.AlertConfigType.EVDAS_ALERT) {
                    executedConfig = alertCommentService.getEvdasExecConfigurationById(executedConfigId)
                    caseAlert = EvdasAlert.findByPtAndExecutedAlertConfiguration(comment.eventName, executedConfig)
                    activityService.createActivityForEvdas(executedConfig, alertCommentService.getActivityByType(ActivityTypeValue.CommentRemoved),
                            userService.getUser(), "Comment [" + commentStr + "] is removed",
                            "", [comments: comment.comments], comment.productName, comment.eventName, caseAlert?.assignedTo, comment.caseNumber, caseAlert?.assignedToGroup)
                } else if(params.alertType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
                    executedConfig = alertCommentService.getLiteratureExecConfigurationById(executedConfigId)
                    def literatureAlert = LiteratureAlert.findById(params.literatureAlertId)
                    if (!literatureAlert) {
                        literatureAlert = ArchivedLiteratureAlert.findById(params.literatureAlertId)
                    }
                    literatureActivityService.createLiteratureActivity(executedConfig,literatureAlertService.getActivityByType(ActivityTypeValue.CommentRemoved),
                            userService.getUser(), "Comment [" + commentStr + "] is removed", "", comment.productName, comment.eventName, userService.getUser(),literatureAlert?.searchString, literatureAlert?.articleId)
                } else {
                    executedConfig = alertCommentService.getExecConfigurationById(executedConfigId)
                    if(AggregateCaseAlert.get((Integer) (comment.caseId ? comment.caseId : params.caseId ))){
                        AggregateCaseAlert aca = AggregateCaseAlert.get((Integer) (comment.caseId ? comment.caseId : params.caseId ))
                        AlertCommentHistory alertCommentHistory = new AlertCommentHistory()
                        alertCommentHistory.aggAlertId = (Integer) (comment.caseId ? comment.caseId : params.caseId )
                        alertCommentHistory.comments = ""
                        // @ADDITION OF PRODUCT NAME AND EVENT NAME FOR FETCHING ALERT HISTORY AS PEC LEVEL
                        alertCommentHistory.eventName = comment.eventName
                        alertCommentHistory.productName = comment.productName
                        alertCommentHistory.configId = comment.configId
                        alertCommentHistory.productId = comment.productId
                        alertCommentHistory.alertName = Configuration.get(comment.configId).name
                        alertCommentHistory.period = alertCommentService.getPeriodForCommentHistory(aca.executedAlertConfiguration)
                        alertCommentHistory.eventId = comment.ptCode
                        CRUDService.save(alertCommentHistory)
                        //aca.addToAlertCommentHistory(alertCommentHistory)
                        caseAlert = AggregateCaseAlert.findByPtAndExecutedAlertConfiguration(comment.eventName, executedConfig)
                    }else{
                        ArchivedAggregateCaseAlert aca = ArchivedAggregateCaseAlert.get((Integer) (comment.caseId ? comment.caseId : params.caseId ))
                        AlertCommentHistory alertCommentHistory = new AlertCommentHistory()
                        alertCommentHistory.aggAlertId = (Integer) (comment.caseId ? comment.caseId : params.caseId )
                        alertCommentHistory.comments = ""
                        // @ADDITION OF PRODUCT NAME AND EVENT NAME FOR FETCHING ALERT HISTORY AS PEC LEVEL
                        alertCommentHistory.eventName = comment.eventName
                        alertCommentHistory.productName = comment.productName
                        alertCommentHistory.configId = comment.configId
                        alertCommentHistory.productId = comment.productId
                        alertCommentHistory.alertName = Configuration.get(comment.configId).name
                        alertCommentHistory.period = alertCommentService.getPeriodForCommentHistory(aca.executedAlertConfiguration)
                        CRUDService.save(alertCommentHistory)
                        //aca.addToAlertCommentHistory(alertCommentHistory)
                        caseAlert = ArchivedAggregateCaseAlert.findByPtAndExecutedAlertConfiguration(comment.eventName, executedConfig)
                    }
                    activityService.createActivity(executedConfig, alertCommentService.getActivityByType(ActivityTypeValue.CommentRemoved),
                            userService.getUser(), "Comment [" + commentStr + "] is removed",
                            "", [comments: comment.comments], comment.productName, comment.eventName, caseAlert?.assignedTo, comment.caseNumber, caseAlert.assignedToGroup)

                }

            }

            cacheService.insertAlertCommentHistory(comment)
            render(contentType: 'text/json', text: ['success': true] as JSON)
        } else {
            render(contentType: 'text/json', text: ['success': false] as JSON)
        }
    }

    def deleteBulkComments(){
        Map responseMap = alertCommentService.deleteBulkComments(params)
        String message;
        if(responseMap.isSuccess)
            message="Comment(s) deleted successfully."
        else
            message="An Error occurred while Processing request."
        render([success:responseMap.isSuccess,dtIndexIdMap:responseMap.dtIndexCommentIdMap,message:message] as JSON)
    }

    def listCommentsHistory() {
        def comment = alertCommentService.getCommentHistory(params)
        render((comment ? comment: []) as JSON)
    }

    def listAggCommentsHistory(Long productId, String eventName, Long configId, Long eventId) {
        def list = alertCommentService.listAggCommentsHistory(productId, eventName, configId, eventId)
        respond list , [formats: ['json']]
    }

    def getBulkCheck(){
        JsonSlurper jsonSlurper = new JsonSlurper()
        String alertList = params.caseJsonObjArray
        List alertListObj = jsonSlurper.parseText(alertList) as List
        boolean containsCommentTemplate=false
        alertListObj.each {
            Configuration configuration = Configuration.get(it.configId as Long)
            containsCommentTemplate |= (alertCommentService.listAggregateCaseComments(it.productId as BigInteger, it.eventName as String, configuration, it.executedConfigId as Long)?.commentTemplateId != null
                    && CommentTemplate.get(alertCommentService.listAggregateCaseComments(it.productId as BigInteger, it.eventName as String, configuration,  it.executedConfigId as Long)?.commentTemplateId))
        }
        render ([data: containsCommentTemplate] as JSON)
    }

    def exportCommentHistory(Long caseId){
        String searchString = params.searchField ?: null
        String esc_char = ""
        def alertCommentList
        def list = []
        String userTimezone = userService.getCurrentUserPreference()?.timeZone ?: Constants.UTC
        alertCommentList = AlertCommentHistory.createCriteria().list() {
                eq('aggAlertId', caseId as Integer)
                if(searchString) {
                searchString = searchString.toLowerCase()
                if (searchString.contains('_')) {
                    searchString = searchString.replaceAll("\\_", "!_%")
                    esc_char = "!"
                } else if (searchString.contains('%')) {
                    searchString = searchString.replaceAll("\\%", "!%%")
                    esc_char = "!"
                }
                if (esc_char) {
                    or {
                        sqlRestriction("""lower(comments) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'""")
                        sqlRestriction("""lower(alert_name) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'""")
                        sqlRestriction("""lower(modified_by) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'""")
                        sqlRestriction("""lower(period) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'""")
                        sqlRestriction("""lower(to_char((last_updated),'DD-MON-YYYY HH:MI:ss AM')) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'""")
                    }
                } else {
                    or {
                        sqlRestriction("""lower(comments) like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction("""lower(alert_name) like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction("""lower(modified_by) like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction("""lower(period) like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction("""lower(to_char((last_updated),'DD-MON-YYYY HH:MI:ss AM')) like '%${searchString.replaceAll("'", "''")}%'""")
                    }
                }
            }
        }
        if(alertCommentList){
           list =  alertCommentList.collect{
                [
                        id         : it.id,
                        aggAlertId : it.aggAlertId,
                        comments   : it.comments,
                        alertName  : it.alertName,
                        period     : it.period,
                        modifiedBy : User.findByUsername(it.modifiedBy)?.fullName,
                        lastUpdated: DateUtil.stringFromDate(it.lastUpdated, DateUtil.DEFAULT_DATE_TIME_FORMAT, userTimezone)
                ]
            }.sort({ -it.id })
        }
        def report = dynamicReportService.createCommentHistoryReport(list, params)
        renderReportOutputType(report,params)
        def domain = params.isArchived == true ? ArchivedAggregateCaseAlert.get(caseId as Long) : AggregateCaseAlert.get(caseId as Long)
        signalAuditLogService.createAuditForExport(params?.containsKey('criteriaSheetList') ? params.criteriaSheetList : null, domain.getInstanceIdentifierForAuditLog(), Constants.AuditLog.AGGREGATE_REVIEW + ": Comments", params, report.name)
    }

    private renderReportOutputType(File reportFile,def params) {
        String reportName = "Comment History" + DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        response.contentType = "${dynamicReportService.getContentType(params.outputFormat)}; charset=UTF-8"
        response.contentLength = reportFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(reportFile.bytes)
        response.outputStream.flush()
        params?.reportName = "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat"
    }
}
