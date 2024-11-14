package com.rxlogix.config

import com.rxlogix.*
import com.rxlogix.audit.AuditTrail
import com.rxlogix.cache.CacheService
import com.rxlogix.dto.AlertLevelDispositionDTO
import com.rxlogix.dto.AlertReviewDTO
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.helper.LinkHelper
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.signal.AlertComment
import com.rxlogix.signal.ArchivedSingleCaseAlert
import com.rxlogix.signal.GlobalArticle
import com.rxlogix.signal.LiteratureHistory
import com.rxlogix.signal.SystemConfig
import com.rxlogix.signal.UndoableDisposition
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SignalQueryHelper
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.events.EventPublisher
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonSlurper
import groovy.time.TimeCategory
import groovy.util.slurpersupport.GPathResult
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import groovyx.net.http.RESTClient
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.apache.commons.lang.StringUtils
import org.apache.http.ConnectionClosedException
import org.asynchttpclient.Param
import org.grails.datastore.mapping.query.Query
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.jdbc.Work
import org.hibernate.sql.JoinType
import org.joda.time.DateTimeZone
import org.springframework.transaction.annotation.Propagation

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.time.ZoneId
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

import static com.rxlogix.util.DateUtil.DEFAULT_DATE_FORMAT

@Transactional
class LiteratureAlertService implements Alertbililty, LinkHelper, EventPublisher, AlertUtil {

    def CRUDService
    def userService
    def configurationService
    def sessionFactory
    def emailService
    def messageSource
    def literatureActivityService
    def validatedSignalService
    def dynamicReportService
    def alertService
    def actionService
    def alertTagService
    def alertCommentService
    ActivityService activityService
    EmailNotificationService emailNotificationService
    DispositionService dispositionService
    def signalExecutorService
    def pvsAlertTagService
    def pvsGlobalTagService
    def dataObjectService
    LiteratureHistoryService literatureHistoryService
    def archiveService
    CacheService cacheService
    def spotfireService
    def undoableDispositionService
    CustomMessageService customMessageService
    def signalAuditLogService
    void createAlert(Long configId, Long executedConfigId, def alertData) throws Exception {

        try {
            if (alertData) {
                LiteratureConfiguration config = LiteratureConfiguration.get(configId)
                ExecutedLiteratureConfiguration executedConfig = ExecutedLiteratureConfiguration.get(executedConfigId)
                Map monthMap = ["01": "Jan", "02": "Feb", "03": "Mar", "04": "Apr", "05": "May", "06": "Jun", "07": "Jul", "08": "Aug", "09": "Sep", "10": "Oct", "11": "Nov", "12": "Dec"]
                Disposition defaultDisposition = executedConfig?.getOwner()?.getWorkflowGroup()?.defaultLitDisposition
                Integer workerCnt = Holders.config.signal.worker.count as Integer
                List<LiteratureAlert> resultData = []
                List alertDataList = []
                int alertDataSize = alertData.PubmedArticle.size() > 9000 ? 9000 : alertData.PubmedArticle.size()
                for (int idx = 0; idx < alertDataSize; idx++) {
                    alertDataList.add(alertData.PubmedArticle[idx])
                }

                log.info("Thread Starts")
                ExecutorService executorService = Executors.newFixedThreadPool(workerCnt)

                List<Long> newArticleIds = fetchNewArticles(alertDataList)
                pvsGlobalTagService.batchPersistGlobalArticles(newArticleIds)
                List<GlobalArticle> globalArticleList = fetchGlobalArticles(alertDataList)
                dataObjectService.setGlobalArticleList(executedConfig.id , globalArticleList)

                List<Future<LiteratureAlert>> futureList = alertDataList.collect { def data ->
                    executorService.submit({ ->
                        parallellyCreateLiteratureAlert(data, config, executedConfig, defaultDisposition, monthMap)
                    } as Callable)
                }
                futureList.each {
                    resultData.add(it.get())
                }
                executorService.shutdown()
                log.info("Thread Ends")
                batchPersistData(resultData, executedConfig, config)
                alertService.updateOldExecutedConfigurationsLiterature(config, executedConfig.id,ExecutedLiteratureConfiguration, resultData.countBy {it.dispositionId})
                persistValidatedSignalWithLiteratureAlert(config.id, executedConfig.id)
                dataObjectService.clearGlobalArticleMap(executedConfig.id)
                archiveService.moveDatatoArchive(executedConfig, LiteratureAlert, alertService.getLiteraturePrevExConfigIds(executedConfig, config.id))
            } else {
                log.error("There is no data to execute")
                dataObjectService.clearGlobalArticleMap(executedConfigId)
            }
        }
        catch (Throwable e) {
            dataObjectService.clearGlobalArticleMap(executedConfigId)
            log.error(e.printStackTrace())
            throw e
        }
    }

    LiteratureAlert parallellyCreateLiteratureAlert(GPathResult alertData, LiteratureConfiguration config, ExecutedLiteratureConfiguration executedConfig,
                                                    Disposition defaultDisposition,Map monthMap) {

        GPathResult medlineCitationData = alertData?.MedlineCitation
        GPathResult articleData = medlineCitationData?.Article
        String articleID = medlineCitationData?.PMID
        String publicationDate = getPublicationDate(articleData, monthMap, executedConfig.dateRangeInformation.getReportStartAndEndDate())
        String articleTitle = articleData?.ArticleTitle
        GPathResult authorsList = articleData?.AuthorList?.Author
        String articleAbstract = ""
        if(articleData?.Abstract?.AbstractText?.size() > 1){
            (0..articleData?.Abstract?.AbstractText?.size()-1).each{
                if(articleData?.Abstract?.AbstractText[it]['@Label'] != ""){
                    articleAbstract = articleAbstract + (articleData?.Abstract?.AbstractText[it]['@Label'] as String).toLowerCase().capitalize()+": "
                }
                articleAbstract = articleAbstract + articleData?.Abstract?.AbstractText[it] +"\n"
            }
        }else {
            articleAbstract = articleData?.Abstract?.AbstractText
        }
        int noOfAuthors = authorsList?.size() ?: 0
        String authorNames = getAuthorNames(noOfAuthors, authorsList) ?: ""
        List<LiteratureHistory> existingLiteratureHistoryList = LiteratureHistory.createCriteria().list {
            eq("litConfigId",config.id)
            order("lastUpdated", "desc")
        } as List<LiteratureHistory>

        LiteratureAlert literatureSearchAlert = new LiteratureAlert(
                litSearchConfig: config,
                exLitSearchConfig: executedConfig,
                dateCreated: executedConfig?.dateCreated ?: (new Date()),
                lastUpdated: executedConfig?.lastUpdated ?: (new Date()),
                name: config.name,
                assignedTo: config?.assignedTo ? config.assignedTo : null,
                assignedToGroup: config?.assignedToGroup ? config.assignedToGroup : null,
                disposition: defaultDisposition,
                priority: config.priority,
                productSelection : config.productSelection ? getNameFieldFromJson(config.productSelection):getGroupNameFieldFromJson(config.productGroupSelection),
                eventSelection: config.eventSelection ? getNameFieldFromJson(config.eventSelection):getGroupNameFieldFromJson(config.eventGroupSelection),
                searchString: config.searchString ?: Constants.Commons.DASH_STRING,
                articleId: articleID ? Integer.valueOf(articleID) : Constants.Commons.DASH_STRING,
                articleTitle: articleTitle ? String.valueOf(articleTitle) : Constants.Commons.DASH_STRING,
                articleAbstract: articleAbstract ? String.valueOf(articleAbstract) : Constants.Commons.DASH_STRING,
                articleAuthors: authorNames ? String.valueOf(authorNames) : Constants.Commons.DASH_STRING,
                publicationDate: publicationDate ? String.valueOf(publicationDate) : Constants.Commons.DASH_STRING
        )
        literatureSearchAlert.globalIdentity = dataObjectService.getGlobalArticleList(executedConfig.id , Long.valueOf(articleID) )
        LiteratureHistory existingLiteratureHistory = existingLiteratureHistoryList.find {it.articleId == Long.valueOf(articleID) && it.isLatest}
        if(existingLiteratureHistory){
            literatureSearchAlert.disposition = cacheService.getDispositionByValue(existingLiteratureHistory.currentDispositionId)
            if (existingLiteratureHistory.currentAssignedTo) {
                literatureSearchAlert.assignedTo = cacheService.getUserByUserId(existingLiteratureHistory.currentAssignedToId)
            } else {
                literatureSearchAlert.assignedToGroup = cacheService.getGroupByGroupId(existingLiteratureHistory.currentAssignedToGroupId)
            }
            literatureSearchAlert.priority = cacheService.getPriorityByValue(existingLiteratureHistory.currentPriorityId)
        }
        literatureSearchAlert
    }

    @Transactional
    void batchPersistData(List<LiteratureAlert> alertList, ExecutedLiteratureConfiguration executedConfig, LiteratureConfiguration config) {
        def time1 = System.currentTimeMillis()
        log.info("Now persisting the execution related data in a batch.")

        //Persist the alerts
        batchPersistLiteratureAlert(alertList)

        log.info("Persistance of execution related data in a batch is done.")
        def time2 = System.currentTimeMillis()
        log.info(((time2 - time1) / 1000) + " Secs were taken in the persistance of data for configuration " + executedConfig.name + "id is : " + executedConfig.id)
    }

    @Transactional
    def persistValidatedSignalWithLiteratureAlert(Long configId, Long exeConfigId) {
        List<LiteratureAlert> attachSignalAlertList = getAttachSignalAlertList(exeConfigId)

        if (attachSignalAlertList) {
            List<Map<String, String>> alertIdAndSignalIdList = new ArrayList<>()
            log.info("Now saving the signal across the PE.")
            List<String> articleList = attachSignalAlertList.collect {
                it.articleId.toString()
            }
            Session session = sessionFactory.currentSession
            String sql_statement = SignalQueryHelper.signal_alert_ids_literature(articleList.join(","), exeConfigId, configId)
            SQLQuery sqlQuery = session.createSQLQuery(sql_statement)
            sqlQuery.list().each { row ->
                alertIdAndSignalIdList.add([col2: row[0].toString(), col1: row[1].toString(), col3: '1'])
            }

            alertIdAndSignalIdList = alertIdAndSignalIdList.unique {
                [it.col2, it.col1]
            }
            String insertValidatedQuery = "INSERT INTO VALIDATED_LITERATURE_ALERTS(VALIDATED_SIGNAL_ID,LITERATURE_ALERT_ID,IS_CARRY_FORWARD) VALUES(?,?,?)"
            session.doWork(new Work() {
                public void execute(Connection connection) throws SQLException {
                    PreparedStatement preparedStatement = connection.prepareStatement(insertValidatedQuery)
                    def batchSize = Holders.config.signal.batch.size
                    int count = 0
                    try {
                        alertIdAndSignalIdList.each {
                            preparedStatement.setString(1, it.col1)
                            preparedStatement.setString(2, it.col2)
                            preparedStatement.setString(3, it.col3?:'0')
                            preparedStatement.addBatch()
                            count += 1
                            if (count == batchSize) {
                                preparedStatement.executeBatch()
                                count = 0
                            }
                        }
                        preparedStatement.executeBatch()
                    } catch (Exception e) {
                        e.printStackTrace()
                    } finally {
                        preparedStatement.close()
                        session.flush()
                        session.clear()
                    }
                }
            })
            log.info("Signal are saved across the system.")

        }

    }

    List<LiteratureAlert> getAttachSignalAlertList(Long exeConfigId) {
        List<LiteratureAlert> attachSignalAlertList = LiteratureAlert.createCriteria().list {
            eq("exLitSearchConfig.id", exeConfigId)
            'disposition' {
                eq("validatedConfirmed", true)
            }
            createAlias("validatedSignals", "vs", JoinType.LEFT_OUTER_JOIN)
            isNull('vs.id')
        } as List<LiteratureAlert>
        attachSignalAlertList
    }

    void batchPersistLiteratureAlert(alertList) {

        LiteratureAlert.withTransaction {
            def batch = []
            for (LiteratureAlert alert : alertList) {
                batch += alert
                if (batch.size() > Holders.config.signal.batch.size) {
                    Session session = sessionFactory.currentSession
                    for (LiteratureAlert alertIntance in batch) {
                        //Validate false is required to make sure that additional grails related check is not added to db.
                        alertIntance.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                    batch.clear()
                }
            }

            if (batch) {
                try {
                    Session session = sessionFactory.currentSession
                    for (LiteratureAlert alertIntance in batch) {
                        //Validate false is required to make sure that additional grails related check is not added to db.
                        alertIntance.save(validate: true, failOnError: true)
                    }
                    session.flush()
                    session.clear()
                    batch.clear()
                } catch (Throwable th) {
                    th.printStackTrace()
                }
            }
            log.info("Alert data is batch persisted.")
        }
    }

    private String getAuthorNames(int noOfAuthors, def authorsList) {
        String authorNames = " "

        noOfAuthors.times {
            def author = authorsList[it]
            authorNames += author.ForeName + " " + author.LastName
        }
        return authorNames
    }

    LiteratureConfiguration persistConfiguration(LiteratureConfiguration configurationInstance, Map params) {
        configurationInstance.owner = configurationInstance?.owner ?: userService.getUser()
        setStartAndEndDate(configurationInstance, params)
        //Set the workflow group from the logged in user.
        configurationInstance.workflowGroup = userService.getUser().workflowGroup
        configurationInstance = (LiteratureConfiguration) CRUDService.save(configurationInstance)
        return configurationInstance
    }

    private void setNextRunDateAndScheduleDateJSON(LiteratureConfiguration configurationInstance) {
        try {
            if (configurationInstance.scheduleDateJSON && configurationInstance.isEnabled) {
                configurationInstance.nextRunDate = configurationService.getNextDate(configurationInstance)
            } else {
                configurationInstance.nextRunDate = null
            }
        }catch(Exception e){
            configurationInstance.scheduleDateJSON = null
        }
    }

    private void setStartAndEndDate(LiteratureConfiguration configurationInstance, Map params) {
        if (configurationInstance?.dateRangeInformation?.dateRangeEnum == DateRangeEnum.CUSTOM) {

            try {
                configurationInstance?.dateRangeInformation?.dateRangeStartAbsolute = DateUtil.getEndDate(params?.dateRangeStartAbsolute, configurationInstance?.configSelectedTimeZone)
                configurationInstance?.dateRangeInformation?.dateRangeEndAbsolute = DateUtil.getEndDate(params?.dateRangeEndAbsolute, configurationInstance?.configSelectedTimeZone)
            } catch (Exception e) {
                configurationInstance?.dateRangeInformation?.dateRangeStartAbsolute = null
                configurationInstance?.dateRangeInformation?.dateRangeEndAbsolute = null
            }
        } else {
            configurationInstance?.dateRangeInformation?.dateRangeStartAbsolute = null
            configurationInstance?.dateRangeInformation?.dateRangeEndAbsolute = null
        }
    }

    Map fetchIdListForLiteratureData(LiteratureConfiguration configurationInstance) {
        Map ret = [:]
        String url = Holders.config.app.literature.url
        String path = Holders.config.app.literature.id.uriPath
        String apiKey = Holders.config.app.literature.api.key
        String termValue = getTermForAPI(configurationInstance)
        Map dateRangeMap = getStartAndEndDateForLiterature(configurationInstance)
        //TODO : We have put a cap of result of 1 lacs in the result.
        Map query = [db: 'pubmed', term: termValue, mindate: dateRangeMap?.mindate, maxdate: dateRangeMap?.maxdate, datetype: 'pdat', retmode: 'JSON', retmax: 100000, usehistory: 'y']
        if (apiKey) {
            query.put("api_key", apiKey)
        }
        RESTClient endpoint = new RESTClient(url)
        if (Holders.config.literature.proxy.enabled) {
            log.info("Proxy is Enabled")
            endpoint.setProxy(Holders.config.literature.proxy.url, Holders.config.literature.proxy.port, Holders.config.literature.proxy.scheme)
        }
        endpoint.handler.failure = { resp -> ret = [status: resp.status] }
        log.info("Starting the first API call...")
        try {
            def resp = endpoint.get(
                    path: path,
                    query: query
            )
            log.info("Ending the first API call...")
            if (resp.status == 200) {
                ret = [status: resp.status, data: resp.data]
                log.debug("Response is: " + ret)
            }
            if (ret?.data?.esearchresult) {
                Map data = ret.data.esearchresult
                log.info("Number of records received by API call are :  ${data.idlist.size()}")
                return ["webEnv": data?.webenv, "queryKey": data?.querykey]
            }
        } catch (Throwable th) {
            log.error(th.printStackTrace())
        } finally {
            endpoint.shutdown()
        }
        return [:]
    }

    def fetchDataForIds(Map data, Integer count=0) {
        String url = Holders.config.app.literature.url
        String uriPath = Holders.config.app.literature.data.uriPath
        String apiKey = Holders.config.app.literature.api.key

        Map query = [db: 'pubmed', WebEnv: data?.webEnv, query_key: data?.queryKey, retmode: 'xml']
        if (apiKey) {
            query.put("api_key", apiKey)
        }
        def ret = [:]

        HTTPBuilder http = new HTTPBuilder(url)
        if (Holders.config.literature.proxy.enabled) {
            log.info("Proxy is Enabled")
            http.setProxy(Holders.config.literature.proxy.url, Holders.config.literature.proxy.port, Holders.config.literature.proxy.scheme)
        }
        def result = null
        log.info("Starting the second API call...")
       try {
            http.request(Method.POST, ContentType.TEXT) {
                uri.path = uriPath
                uri.query = query
                response.success = { resp, reader ->
                    ret.status = resp.status
                    log.info("The status response is : " + resp.status)
                    result = reader.text
                }
                response.failure = { resp -> ret = [status: resp.status] }
            }
            log.info("Ending the second API call...")
            if (ret.status == 200) {
                return result
            }
            else {
                log.info("API CALL FAILED.")
                return Constants.API_CALL_FAILED_MESSAGE
            }
       } catch (ConnectionClosedException cce) {
           // trying to fetch data multiple times if response failed occasionally
           log.error("Connection Closed Exception catched for data ${data}")
           if (count < 5) {
               return fetchDataForIds(data, count + 1)
           }else{
               throw cce
           }
       } catch (Throwable th) {
            log.error(th.printStackTrace())
            throw th
        } finally {
           http.shutdown()
        }
        return null

    }

    def getParsedData(def result) {
        def alertData = null
        if (result) {
            XmlSlurper parser = new XmlSlurper()
            parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
            parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            log.info("Starting to parse XML Data")
            alertData = parser.parseText(result)
            log.info("Finished parsing XML Data")
        }
        return alertData
    }

    String getTermForAPI(LiteratureConfiguration config) {
        List result = []
        List<String> prdList
        if(config.productSelection){
            prdList = config.getProductNameList()
        } else {
            String prdName = config.getNameFieldFromJson(spotfireService.getJsonForGroup(config.getIdsForProductGroup()))
            if (prdName) {
                prdList = prdName.tokenize(',')
            }
        }
        prdList ? result.add("("+prdList.join(" OR ")+")") : ""
        List<String> eventList
        if(config.eventSelection) {
            eventList = config.getEventSelectionList()
        } else {
            String eventName = config.getNameFieldFromJson(spotfireService.getJsonForGroup(config.getIdsForEventGroup()))
            if (eventName) {
                eventList = eventName.tokenize(',')
            }
        }

        eventList ? result.add("("+eventList.join(" OR ")+")") : ""
        config.searchString ? result.add(config.searchString) : ""
        String term = result[0]
        for (int i = 1; i < result.size(); i++) {
            term += " AND ${result[i]}"
        }
        return term
    }


    Map fetchLiteratureSearchAlertResultMap(Map params, DataTableSearchRequest searchRequest, Long configId, Boolean isArchived = false) {
        Map resultMap = [:]
        Map filterMap = [:]
        def dispFiltersList = []
        int allTheColumns = 13
        (0..allTheColumns).each {
            if (searchRequest.searchParam.columns[it].search.value) {
                String key = searchRequest.searchParam.columns[it].data
                String value = searchRequest.searchParam.columns[it].search.value
                filterMap.put(key, value)
            }
        }
        List<String> openDisposition = Disposition.findAllByClosedAndReviewCompleted(false, false).collect {
            it.displayName
        }
        def escapedFilters = null
        if (params.filters) {
            if(params.filters.startsWith("[") && params.filters != "[]"){
                def slurper = new JsonSlurper()
                escapedFilters = slurper.parseText(params.filters)
                if(escapedFilters) {
                    dispFiltersList = new ArrayList(escapedFilters)
                }
            } else {
                dispFiltersList = params.filters.split(",")
            }

        } else {
            dispFiltersList = openDisposition
        }
        Map orderColumnMap = [name: searchRequest.searchParam.orderBy(), dir:searchRequest?.searchParam?.orderDir()]

        resultMap.filters = filterList(configId, isArchived)
        if (!params.containsKey('filters') || params.filters) {
            resultMap.resultList = literatureAlertListAssignedToUserOrGroup(dispFiltersList, filterMap, orderColumnMap, searchRequest.pageSize(), searchRequest.searchParam.start, configId, params)
            resultMap.filteredCount = getLiteratureAlertFilteredCount(configId, dispFiltersList, filterMap, isArchived)
        } else {
            resultMap.resultList = []
            resultMap.filteredCount = 0
        }
        resultMap.resultCount = getLiteratureAlertTotalCount(configId, isArchived)
        resultMap
    }

    List<Map> literatureAlertListAssignedToUserOrGroup(def dispFiltersList, Map filterMap, Map orderColumnMap, Integer max, Integer offset,Long configId, Map params) {
        def startTime = System.currentTimeMillis()
        def domainName
        Disposition defaultLitDisposition = userService.getUser().workflowGroup.defaultLitDisposition
        cacheService.setDefaultDisp(Constants.AlertType.LITERATURE, defaultLitDisposition.id as Long)
        boolean isDispFilters = dispFiltersList.size() > 0
        Map queryParameters = [configId: configId]
        if(params.isArchived == "true") {
            queryParameters.put("isLatest", false)
            domainName = ArchivedLiteratureAlert
        }
        else {
            queryParameters.put("isLatest", true)
            domainName = LiteratureAlert
        }

        String literatureAlertHQL = prepareLiteratureAlertHQL(filterMap, orderColumnMap, isDispFilters, queryParameters, domainName)

        if (isDispFilters) {
            queryParameters.put("dispList", dispFiltersList)
        }
        List literatureSearchAlertList = domainName.executeQuery(literatureAlertHQL, queryParameters, [offset: offset, max: max])
        List<Map> literatureSearchAlertDTO = []
        List<Long> alertIdList = literatureSearchAlertList.collect { it.id }
        List<String> articleIdList = literatureSearchAlertList.collect { it.articleId as String}
        List<Map> alertValidatedSignalList = validatedSignalService.getAlertValidatedSignalList(alertIdList, domainName)
        List<Map> alertTagNameList = pvsAlertTagService.getAllAlertSpecificTags(alertIdList , Constants.AlertType.LITERATURE_ALERT)
        List<Map> globalTagNameList = pvsGlobalTagService.getAllGlobalTags(literatureSearchAlertList.collect{it.globalIdentityId} , Constants.AlertType.LITERATURE_ALERT)
        List<Long> undoableAlertIdList =  undoableDispositionService.getUndoableAlertList(alertIdList, Constants.AlertType.LITERATURE)
        List<AlertComment> alertCommentList = alertCommentService.getAlertCommentByArticleIdList(articleIdList)
        ExecutorService executorService = signalExecutorService.threadPoolForLitListExec()
        List<Future> futureList = literatureSearchAlertList.collect { def literatureAlert ->
            executorService.submit({ ->
                UndoableDisposition undoableDisposition = null
                List<Map> litAlertTags = alertTagNameList.findAll{it.alertId == literatureAlert.id}
                List<Map> globalTags = globalTagNameList.findAll{it.globalId == literatureAlert.globalIdentityId }
                globalTags = globalTags.unique(false) { a, b ->
                    a.tagText <=> b.tagText
                }
                List<Map> allTags = litAlertTags + globalTags
                List<Map> tagNameList = allTags.sort{tag1 , tag2 -> tag1.priority <=> tag2.priority}

                List<Map> validatedSignals = alertValidatedSignalList.findAll {
                    it.id == literatureAlert.id
                }?.collect { [name: it.name + "(S)", signalId: it.signalId,disposition: it.disposition] }

                AlertComment commentObj = alertCommentList.find {
                    it.articleId == literatureAlert.articleId as String
                }
                String comment = commentObj?.comments ?: null
                Boolean isAttached = false
                Boolean isAttachedToCurrentAlert = literatureAlert.attachments as boolean
                if (isAttachedToCurrentAlert) {
                    isAttached = true
                }
                literatureAlert.toDto(tagNameList, validatedSignals,false, comment, isAttached, undoableAlertIdList.contains(literatureAlert.id)?:false,commentObj?.id)
            } as Callable)
        }
        futureList.each {
            literatureSearchAlertDTO.add(it.get())
        }
        cacheService.removeDefaultDisp(Constants.AlertType.LITERATURE)
        def endTime = System.currentTimeMillis()
        log.info("Got ${literatureSearchAlertDTO.size()} alerts in time: " + (endTime - startTime) / 1000 + " sec")
        literatureSearchAlertDTO
    }

    List<Long> getAlertIdsForAttachments(Long alertId, boolean isArchived = false){
        def domain = getDomainObject(isArchived)
        List<Long> litAlertList = []
        List<Long> archivedAlertIds = []
        def litAlert
        ArchivedLiteratureAlert.withTransaction {
            litAlert = domain.findById(alertId.toInteger())
            archivedAlertIds = ExecutedLiteratureConfiguration.findAllByConfigId(litAlert.litSearchConfig.id).collect {
                it.id
            }
            litAlertList = ArchivedLiteratureAlert.createCriteria().list {
                projections {
                    property('id')
                }
                eq('articleId', litAlert?.articleId)
                if (archivedAlertIds){
                    or {
                        archivedAlertIds.collate(1000).each{
                            'in'('exLitSearchConfig.id', it)
                        }
                    }
                }
            } as List<Long>

            archivedAlertIds = litAlertList.findAll {
                ArchivedLiteratureAlert.get(it).exLitSearchConfig.id < litAlert.exLitSearchConfig.id
            }
        }
        archivedAlertIds + litAlert.id
    }

    boolean checkAttachmentsForAlert(List<Long> alertIds){
        boolean  isAttached = false
        ArchivedLiteratureAlert.withTransaction {
            alertIds.each { Long litAlertId ->
                def litAlert = ArchivedLiteratureAlert.get(litAlertId) ?: LiteratureAlert.get(litAlertId)
                if (litAlert.attachments)
                    isAttached = true
            }
        }
        isAttached
    }

    def fetchPreviousLiteratureAlerts(List prevExecConfigIdList){
        def prevAlertsLiteratureAlerts
        ArchivedLiteratureAlert.withTransaction {
            prevAlertsLiteratureAlerts = ArchivedLiteratureAlert.createCriteria().list {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property('id','id')
                    property('articleId','articleId')
                }
                if (prevExecConfigIdList){
                    or {
                        prevExecConfigIdList.collate(1000).each{
                            'in'('exLitSearchConfig.id', it)
                        }
                    }
                }
            } as List<Long>
        }
        return prevAlertsLiteratureAlerts
    }

    List<Map> filterList(Long configId, Boolean isArchived = false) {
        String domainName = isArchived ? "ArchivedLiteratureAlert" : "LiteratureAlert"
        String dispositionListHQL = prepareDispositionHQL(domainName)
        List<Disposition> dispositionList = Disposition.executeQuery(dispositionListHQL, [configId: configId])
        List<Map> filterMap = dispositionList.collect {
            [value: it.displayName, closed: it.closed,isClosed:it.reviewCompleted]
        }
        filterMap

    }

    def prepareDispositionHQL(String domainName) {
        StringBuilder dispositionQuery = new StringBuilder()
        dispositionQuery.append("SELECT Distinct(disp) from Disposition disp, ${domainName}  lsa ")
        dispositionQuery.append("where disp = lsa.disposition and lsa.exLitSearchConfig.id = :configId")
        dispositionQuery.toString()

    }

    //Using HQL as we have to perform sorting on CLOB Column
    String prepareLiteratureAlertHQL(Map filterMap, Map orderColumnMap, boolean isDispFilters, Map queryParameters, def domainName) {
        Long workflowGroupId = userService?.user?.workflowGroup?.id
        StringBuilder searchAlertQuery = new StringBuilder()
        searchAlertQuery.append("SELECT lsa FROM ${domainName.getSimpleName()} lsa LEFT OUTER JOIN lsa.assignedTo atUser ")
        searchAlertQuery.append("LEFT OUTER JOIN lsa.assignedToGroup atGroup ")
        searchAlertQuery.append("LEFT OUTER JOIN lsa.exLitSearchConfig elsc ")
        searchAlertQuery.append("WHERE lsa.exLitSearchConfig.id = :configId and elsc.isLatest = :isLatest and elsc.isDeleted = 0 and elsc.workflowGroup.id = ${workflowGroupId}")

        filterMap.each { k, v ->
            searchAlertQuery.append(" AND ")
            if (k == 'alertName') {
                if (v?.contains('_')) {
                    searchAlertQuery.append("lower(lsa.name) like :name escape '!'=")
                    queryParameters.put('name', "%${EscapedILikeExpression.escapeString(v)}%")
                } else {
                    searchAlertQuery.append("lower(lsa.name) like :name ")
                    queryParameters.put('name', "%${v.toLowerCase()}%")
                }
            } else if (k == 'articleId') {
                searchAlertQuery.append("str(lsa.articleId) like :articleId ")
                queryParameters.put('articleId', "%${v}%")
            } else if (k == 'title') {
                if (v?.contains('_')) {
                    searchAlertQuery.append("lower(lsa.articleTitle) like :articleTitle escape '!'")
                    queryParameters.put('articleTitle', "%${EscapedILikeExpression.escapeString(v)}%")
                } else {
                    searchAlertQuery.append("lower(lsa.articleTitle) like :articleTitle ")
                    queryParameters.put('articleTitle', "%${v.toLowerCase()}%")
                }
            } else if (k == 'authors') {
                if (v?.contains('_')) {
                    searchAlertQuery.append("lower(lsa.articleAuthors) like :articleAuthors escape '!'")
                    queryParameters.put('articleAuthors', "%${EscapedILikeExpression.escapeString(v)}%")
                } else {
                    searchAlertQuery.append("lower(lsa.articleAuthors) like :articleAuthors ")
                    queryParameters.put('articleAuthors', "%${v.toLowerCase()}%")
                }
            } else if (k == 'publicationDate') {
                searchAlertQuery.append("lower(lsa.publicationDate) like :publicationDate ")
                queryParameters.put('publicationDate', "%${v.toLowerCase()}%")
            } else if (k == 'disposition') {
                searchAlertQuery.append("lower(lsa.disposition.displayName) like :disposition ")
                queryParameters.put('disposition', "%${v.toLowerCase()}%")
            } else if (k == 'assignedTo') {
                searchAlertQuery.append("( lower(atUser.fullName) like :userName OR lower(atGroup.name) like :userName ) ")
                queryParameters.put('userName', "%${v.toLowerCase()}%")
            } else if (k == 'productName') {
                if (v?.contains('_')) {
                    searchAlertQuery.append("lower(lsa.productSelection) like :productSelection escape '!'")
                    queryParameters.put('productSelection', "%${EscapedILikeExpression.escapeString(v)}%")
                } else {
                    searchAlertQuery.append("lower(lsa.productSelection) like :productSelection ")
                    queryParameters.put('productSelection', "%${v.toLowerCase()}%")
                }
            } else if (k == 'eventName') {
                if (v?.contains('_')) {
                    searchAlertQuery.append("lower(lsa.eventSelection) like :eventSelection escape '!'")
                    queryParameters.put('eventSelection', "%${EscapedILikeExpression.escapeString(v)}%")
                } else {
                    searchAlertQuery.append("lower(lsa.eventSelection) like :eventSelection ")
                    queryParameters.put('eventSelection', "%${v.toLowerCase()}%")
                }
            } else if (k == 'signal') {
                if (v?.contains('_')) {
                    searchAlertQuery.append("lsa.id in (select distinct(lsa1.id) from ${domainName.getSimpleName()}  lsa1 Left OUTER JOIN lsa1.validatedSignals signal where lower(signal.name) like :signal escape '!') ")
                    queryParameters.put('signal', "%${EscapedILikeExpression.escapeString(v)}%")
                } else {
                    searchAlertQuery.append("lsa.id in (select distinct(lsa1.id) from ${domainName.getSimpleName()}  lsa1 Left OUTER JOIN lsa1.validatedSignals signal where lower(signal.name) like :signal) ")
                    queryParameters.put('signal', "%${v.toLowerCase()}%")
                }
            }
        }
        if (isDispFilters) {
            searchAlertQuery.append(" AND ")
            searchAlertQuery.append("lsa.disposition.displayName in (:dispList) ")
        }
        if (orderColumnMap.name == 'alertName') {
            searchAlertQuery.append("ORDER BY lsa.name ${orderColumnMap.dir}")
        } else if (orderColumnMap.name == 'articleId') {
            searchAlertQuery.append("ORDER BY str(lsa.articleId) ${orderColumnMap.dir}")
        } else if (orderColumnMap.name == 'title') {
            searchAlertQuery.append("ORDER BY dbms_lob.substr(lsa.articleTitle, dbms_lob.getlength(lsa.articleTitle), 1) ${orderColumnMap.dir}")
        } else if (orderColumnMap.name == 'authors') {
            searchAlertQuery.append("ORDER BY dbms_lob.substr(lsa.articleAuthors, dbms_lob.getlength(lsa.articleAuthors), 1) ${orderColumnMap.dir}")
        } else if (orderColumnMap.name == 'publicationDate') {
            StringBuilder temp = new StringBuilder()
            temp.append("ORDER BY CASE WHEN lsa.publicationDate LIKE '___-____' THEN TO_DATE('01-'||lsa.publicationDate, 'DD-Mon-YYYY') WHEN lsa.publicationDate LIKE '____' THEN TO_DATE('01-Dec-'||lsa.publicationDate, 'DD-Mon-YYYY') WHEN lsa.publicationDate LIKE '-' THEN TO_DATE('31-Dec-9999', 'DD-Mon-YYYY') WHEN lsa.publicationDate LIKE '__-___-____' THEN TO_DATE(lsa.publicationDate, 'DD-Mon-YYYY')")
            temp.append("END ${orderColumnMap.dir}")
            searchAlertQuery.append(temp.toString())
        } else if (orderColumnMap.name == 'disposition') {
            searchAlertQuery.append("ORDER BY lsa.disposition.displayName ${orderColumnMap.dir}")
        } else if (orderColumnMap.name == 'assignedTo') {
            searchAlertQuery.append("ORDER BY atUser.fullName ${orderColumnMap.dir}, atGroup.name ${orderColumnMap.dir}")
        } else if (orderColumnMap.name == 'productName') {
            searchAlertQuery.append("ORDER BY dbms_lob.substr(lsa.productSelection, dbms_lob.getlength(lsa.productSelection), 1) ${orderColumnMap.dir}")
        } else if (orderColumnMap.name == 'eventName') {
            searchAlertQuery.append("ORDER BY dbms_lob.substr(lsa.eventSelection, dbms_lob.getlength(lsa.eventSelection), 1) ${orderColumnMap.dir}")
        } else if (orderColumnMap.name == 'actions') {
            searchAlertQuery.append("ORDER BY lsa.actionCount ${orderColumnMap.dir}")
        } else {
            searchAlertQuery.append("ORDER BY lsa.lastUpdated desc")
        }
        searchAlertQuery.toString()
    }


    Integer getLiteratureAlertTotalCount(Long configId, Boolean isArchived = false) {
        Integer totalCount = 0
        Group workflowGroup = userService.getUser().workflowGroup
        def domain = getDomainObject(isArchived)
        totalCount = domain.createCriteria().count {
            eq('exLitSearchConfig.id', configId)
            'exLitSearchConfig' {
                eq('workflowGroup', workflowGroup)
            }
        }
        totalCount
    }

    Integer getLiteratureAlertFilteredCount(Long configId, def dispFilterList, Map filterMap, Boolean isArchived = false) {
        Integer filteredCount = 0
        Group workflowGroup = userService.getUser().workflowGroup
        def domain = getDomainObject(isArchived)
        filteredCount = domain.createCriteria().count {
            eq('exLitSearchConfig.id', configId)
            'exLitSearchConfig' {
                eq('workflowGroup', workflowGroup)
            }
            //If filter maps are coming then we prepare the filter map.
            and {
                filterMap.each { k, v ->
                    if (k == 'alertName') {
                        ilike('name', '%' + v + '%')
                    } else if (k == 'title') {
                        ilike('articleTitle', '%' + v + '%')
                    } else if (k == 'articleId') {
                        sqlRestriction "cast( article_id AS char( 256 ) ) like '%${v}%'"
                    } else if (k == 'authors') {
                        ilike('articleAuthors', '%' + v + '%')
                    } else if (k == 'publicationDate') {
                        ilike('publicationDate', '%' + v + '%')
                    } else if (k == 'disposition') {
                        'disposition' {
                            ilike('displayName', '%' + v + '%')
                        }
                    } else if (k == 'assignedTo') {
                        createAlias("assignedTo", "at", JoinType.LEFT_OUTER_JOIN)
                        createAlias("assignedToGroup", "atg", JoinType.LEFT_OUTER_JOIN)
                        or {
                            ilike('at.fullName', '%' + v + '%')
                            ilike('atg.name', '%' + v + '%')
                        }
                    } else if (k == 'productName') {
                        ilike('productSelection', '%' + v + '%')
                    } else if (k == 'eventName') {
                        ilike('eventSelection', '%' + v + '%')
                    }

                }
                if (dispFilterList.size() > 0) {
                    'disposition' {
                        'in'('displayName', dispFilterList)
                    }
                }
            }
        }
        filteredCount
    }

    //Fetching Configuration Object from ExecutedConfiguration Id
    LiteratureConfiguration getAlertConfigObject(Long executedConfigId) {
        LiteratureAlert.findByExLitSearchConfig(ExecutedLiteratureConfiguration.get(executedConfigId))?.litSearchConfig
    }

    def updateLiteratureSearchAlertStates(def alert, Map map, LiteratureConfiguration literatureConfiguration, Boolean isArchived = false) {
        def domain = getDomainObject(isArchived)
        domain.findAllByArticleIdAndLitSearchConfig(alert.articleId, literatureConfiguration).each {
            switch (map.change) {
                case Constants.HistoryType.DISPOSITION:
                    alert.disposition = map.disposition
                    break
                case Constants.HistoryType.ASSIGNED_TO:
                    alert.assignedTo = map.assignedTo
                    alert.assignedToGroup = map.assignedToGroup
                    break
                case Constants.HistoryType.PRIORITY:
                    alert.priority = map.priority
                    break
            }
            alert.save(flush: true)
        }
    }

    def sendMailOfAssignedToAction(List<User> oldUserList, List<User> newUserList, User currUser, def alert, Boolean isArchived = false, String newUserName) {
        List sentEmailList = []
        String alertLink = createHref("LiteratureAlert", "details", [callingScreen: "review", configId: alert.exLitSearchConfig.id, isArchived: isArchived])
        //Send email to assigned User
        String newMessage = messageSource.getMessage('app.email.case.assignment.literature.message.newUser', null, Locale.default)
        String oldMessage = messageSource.getMessage('app.email.case.assignment.literature.message.oldUser', null, Locale.default)
        List emailDataList = userService.generateEmailDataForAssignedToChange(newMessage, newUserList, oldMessage, oldUserList)
        emailDataList.each { Map emailMap ->
            if (!sentEmailList.count { it == emailMap.user.email }) {
                emailNotificationService.mailHandlerForAssignedToLiterature(emailMap.user, alert, alertLink, emailMap.emailMessage, newUserName)
                sentEmailList << emailMap.user.email
            }
        }

    }

    @Transactional
    Map changeDisposition(String selectedRows, Disposition targetDisposition, String validatedSignalName,
                              String justification, Boolean isArchived,signalId) {
        ValidatedSignal validatedSignal;
        def domain = getDomainObject(isArchived)
        Boolean attachedSignalData = false
        User loggedInUser = userService.user
        List selectedRowsList = JSON.parse(selectedRows)
        boolean bulkUpdate = selectedRowsList.size() > 1
        Long execConfigId
        Integer reviewCounts = 0
        boolean isReviewCompleted = targetDisposition.reviewCompleted
        List<UndoableDisposition> undoableDispositionList =[]
        selectedRowsList.each { Map<String, Long> selectedRow ->
            def alert = domain.get(selectedRow["alert.id"])
            execConfigId = alert?.exLitSearchConfigId
            if (alert) {
                if (!alert?.disposition?.isValidatedConfirmed()) {
                    Disposition previousDisposition = alert.disposition
                    Map dispDataMap = [objectId: alert.id, objectType: Constants.AlertType.LITERATURE, prevDispositionId: previousDisposition.id,
                                       currDispositionId: targetDisposition.id, prevDispPerformedBy: alert.dispPerformedBy]
                    UndoableDisposition undoableDisposition = undoableDispositionService.createUndoableObject(dispDataMap)

                    undoableDispositionList.add(undoableDisposition)
                    alert.disposition = targetDisposition
                    alert.customAuditProperties = ["justification": justification]
                    alert.isDispChanged = true
                    alert.dispPerformedBy = loggedInUser.fullName
                    if(emailNotificationService.emailNotificationWithBulkUpdate(bulkUpdate, Constants.EmailNotificationModuleKeys.DISPOSITION_CHANGE_LITERATURE)) {
                        emailNotificationService.mailHandlerForDispChangeLiterature(alert, previousDisposition, isArchived)
                    }
                    createActivityForDispositionChange(alert, previousDisposition, targetDisposition, justification, loggedInUser)
                    literatureHistoryService.saveLiteratureArticleHistory(literatureHistoryService.getLiteratureHistoryMap(null,alert,Constants.HistoryType.DISPOSITION,justification),isArchived)
                }
                else if(alert?.disposition.isValidatedConfirmed()){
                    if(justification){
                        justification = justification.replace('.', ' ') + "-- "+ customMessageService.getMessage("validatedObservation.justification.article", "${validatedSignalName}")
                    }
                    else
                        justification = customMessageService.getMessage("validatedObservation.justification.article", "${validatedSignalName}")
                    literatureHistoryService.saveLiteratureArticleHistory(literatureHistoryService.getLiteratureHistoryMap(null,alert,Constants.HistoryType.DISPOSITION,justification),isArchived)
                }
                if(isReviewCompleted) {
                    reviewCounts +=1
                }
                List<String> validatedDateDispositions=Holders.config.alert.validatedDateDispositions;
                boolean isValidatedDate=false;
                if (validatedSignalName) {
                    Disposition defaultSignalDisposition = loggedInUser?.getWorkflowGroup()?.defaultSignalDisposition
                    String defaultValidatedDate=Holders.config.signal.defaultValidatedDate
                    isValidatedDate = validatedDateDispositions.contains(defaultSignalDisposition.value);
                    validatedSignal = validatedSignalService.attachAlertToSignal(validatedSignalName, alert.exLitSearchConfig.productSelection, alert, Constants.AlertConfigType.LITERATURE_SEARCH_ALERT, defaultSignalDisposition, null, signalId, isValidatedDate)
                    boolean enableSignalWorkflow = SystemConfig.first()?.enableSignalWorkflow
                        Integer dueIn
                        boolean dueInStartEnabled = Holders.config.dueInStart.enabled
                        String dueInStartPoint = Holders.config.dueInStartPoint.field ?: Constants.WorkFlowLog.VALIDATION_DATE
                        String previousDueDate=DateUtil.fromDateToString(validatedSignal.actualDueDate,DEFAULT_DATE_FORMAT)
                        if (enableSignalWorkflow) {
                            dueIn = dueInStartEnabled ? validatedSignalService.calculateDueIn(validatedSignal.id, dueInStartPoint) : validatedSignalService.calculateDueIn(validatedSignal.id, validatedSignal.workflowState)
                        } else {
                            dueIn = dueInStartEnabled ? validatedSignalService.calculateDueIn(validatedSignal.id, dueInStartPoint) : validatedSignalService.calculateDueIn(validatedSignal.id, defaultValidatedDate)
                        }
                    // Entry should only created when new signal is created
                        if (dueIn != null && SystemConfig.first().displayDueIn && signalId==null) {
                            validatedSignalService.saveSignalStatusHistory([signalStatus: "Due Date", statusComment: "Due date has been updated.",
                                                                            signalId    : validatedSignal.id, "createdDate":null], true)
                        }
                    signalAuditLogService.createAuditLog([
                            entityName: "Signal: Literature Review Observations",
                            moduleName: "Signal: Literature Review Observations",
                            category: AuditTrail.Category.INSERT.toString(),
                            entityValue: "${validatedSignalName}: Article associated",
                            username: userService.getUser().username,
                            fullname: userService.getUser().fullName
                    ] as Map, [[propertyName: "Article associated", oldValue: "", newValue: "${alert.articleId}"]] as List)

                    attachedSignalData = true
                }
                alert.save()
            }
        }
        if(execConfigId && isReviewCompleted && !isArchived) {
            alertService.updateReviewCountsForLiterature(execConfigId, reviewCounts)
        }
        if(selectedRowsList.size() > 0 && !isArchived) {
            notify 'push.disposition.changes', [undoableDispositionList:undoableDispositionList]
        }
        Map signal=validatedSignalService.fetchSignalDataFromSignal(validatedSignal,null,null);
        [attachedSignalData: attachedSignalData,signal:signal]
    }
    def undoneLiteratureHistory(LiteratureAlert literatureAlert) {
        log.info("Marking previous Literature history as Undone")
        ExecutedLiteratureConfiguration ec = ExecutedLiteratureConfiguration.get(literatureAlert.exLitSearchConfig.id as Long)
        LiteratureHistory literatureHistory = LiteratureHistory.createCriteria().get{
            eq('searchString', literatureAlert.searchString)
            eq('change', Constants.HistoryType.DISPOSITION)
            eq('articleId', literatureAlert.articleId as Long)
            eq('litExecConfigId', literatureAlert.exLitSearchConfig.id as Long)
            eq('litConfigId', literatureAlert.litSearchConfig.id as Long)
            order('lastUpdated', 'desc')
            maxResults(1)
        } as LiteratureHistory
        if (literatureHistory) {
            literatureHistory.isUndo = true
            CRUDService.save(literatureHistory)
            log.info("Successfully marked previous Literature History as Undone for literatureAlert alert: ${literatureAlert?.id}")
        }
    }

    @Transactional
    def revertDisposition(Long id, String justification) {
        log.info("Reverting Dispostion Started for Literature alert")
        Boolean dispositionReverted = false
        String oldDispName = ""
        String targetDisposition = ""
        LiteratureAlert literatureAlert = LiteratureAlert.get(id)
        UndoableDisposition undoableDisposition = UndoableDisposition.createCriteria().get {
            eq('objectId', id as Long)
            eq('objectType', Constants.AlertType.LITERATURE)
            order('dateCreated', 'desc')
            maxResults(1)
        }

        if (literatureAlert && undoableDisposition?.isEnabled) {
            try {

                Disposition oldDisp = cacheService.getDispositionByValue(literatureAlert.disposition?.id)
                oldDispName = oldDisp?.displayName
                Disposition newDisposition = cacheService.getDispositionByValue(undoableDisposition.prevDispositionId)
                targetDisposition = newDisposition?.displayName

                Long execConfigId
                Integer reviewCounts = 0
                boolean isReviewCompleted = newDisposition.reviewCompleted

                if(isReviewCompleted) {
                    reviewCounts +=1
                }
                execConfigId = literatureAlert?.exLitSearchConfigId
                undoableDisposition.isUsed = true
                // saving state before undo for activity: 60067
                def prevUndoDisposition = literatureAlert.disposition
                def prevUndoDispPerformedBy = literatureAlert.dispPerformedBy

                literatureAlert.disposition = newDisposition
                literatureAlert.dispPerformedBy = undoableDisposition.prevDispPerformedBy
                literatureAlert.undoJustification = justification

                def activityMap = [
                        'Disposition': [
                                'previous': prevUndoDisposition ?: "",
                                'current': literatureAlert.disposition ?: ""
                        ],
                        'Performed By': [
                                'previous': prevUndoDispPerformedBy ?: "",
                                'current': literatureAlert.dispPerformedBy ?: ""
                        ]
                ]

                String activityChanges = activityMap.collect { k, v ->
                    def previous = v['previous'] ?: ""
                    def current = v['current'] ?: ""
                    if (previous != current) {
                        "$k changed from \'$previous\' to \'$current\'"
                    } else {
                        null
                    }
                }.findAll().join(', ')

                CRUDService.update(literatureAlert)
                CRUDService.update(undoableDisposition)

                UndoableDisposition.executeUpdate("Update UndoableDisposition set isEnabled=:isEnabled where objectId=:id and objectType=:type", [isEnabled: false, id: id, type: Constants.AlertType.LITERATURE])
                undoneLiteratureHistory(literatureAlert)
                literatureHistoryService.saveLiteratureArticleHistory(literatureHistoryService.getLiteratureHistoryMap(null,literatureAlert,Constants.HistoryType.UNDO_ACTION,justification,true),false)

                createActivityForUndoAction(literatureAlert, justification, activityChanges)
                if(execConfigId && isReviewCompleted ) {
                    alertService.updateReviewCountsForLiterature(execConfigId, reviewCounts)
                }
                dispositionReverted=true
                log.info("Dispostion reverted successfully for Literature alert Id: " + id)
            } catch (Exception ex) {
                ex.printStackTrace()
                log.error("some error occoured while reverting disposition")
            }
        }
        [attachedSignalData: null, incomingDisposition: oldDispName, targetDisposition: targetDisposition, dispositionReverted:dispositionReverted]
    }

    def createActivityForUndoAction(LiteratureAlert literatureAlert, String justification, String activityChanges) {
        log.info("Creating Activity for reverting disposition")
        //ActivityType activityType = cacheService.getActivityTypeByValue(ActivityTypeValue.UndoAction.value)
        ActivityType activityType = ActivityType.findByValue(ActivityTypeValue.UndoAction)
        String changeDetails = Constants.ChangeDetailsUndo.UNDO_DISPOSITION_CHANGE + " with " + activityChanges
        User loggedInUser = userService.user
        String productName = getNameFieldFromJson(literatureAlert.litSearchConfig.productSelection)
        String eventName = getNameFieldFromJson(literatureAlert.litSearchConfig.eventSelection)

        literatureActivityService.createLiteratureActivity(literatureAlert.exLitSearchConfig,activityType, loggedInUser, changeDetails, justification,
                productName, eventName, literatureAlert.assignedTo, literatureAlert.searchString, literatureAlert.articleId, literatureAlert.assignedToGroup)

    }

    void createActivityForDispositionChange(def literatureAlert, Disposition previousDisposition, Disposition targetDisposition,
                                            String justification, User loggedInUser) {
        ActivityType activityType = ActivityType.findByValue(ActivityTypeValue.DispositionChange)
        String changeDetails  = "Disposition changed from '$previousDisposition' to '$targetDisposition'"
        String productName = getNameFieldFromJson(literatureAlert.litSearchConfig.productSelection)
        String eventName = getNameFieldFromJson(literatureAlert.litSearchConfig.eventSelection)

        literatureActivityService.createLiteratureActivity(literatureAlert.exLitSearchConfig,activityType, loggedInUser, changeDetails, justification,
                productName, eventName, literatureAlert.assignedTo, literatureAlert.searchString, literatureAlert.articleId, literatureAlert.assignedToGroup)
    }

    List getLiteratureActivityList(Long configId) {

        List<Map> literatureActivityListMap = LiteratureActivity.createCriteria().list() {
            eq("executedConfiguration.id", configId)
        }.collect { LiteratureActivity literatureActivity ->
            literatureActivity.toDto()
        }
        return literatureActivityListMap
    }

    File getLiteratureActivityExportedFile(Map params) {
        List selectedActivities = []
        List<Map> literatureActivityList = getLiteratureActivityList(params.getLong("configId"))
        literatureActivityList.each {
            it.articleId = it.articleId + ""
            it['type'] = activityService.breakActivityType(it['type'] as String)
            if(it.justification){
                it.details = it.details + "-- with Justification '" + it.justification + "'"
            }
        }
        literatureActivityList = literatureActivityList?.sort {
            -it.activity_id
        }
        File reportFile = dynamicReportService.createLiteratureActivityReport(new JRMapCollectionDataSource(literatureActivityList), params)
        reportFile
    }

    def listSelectedAlerts(String alerts, def domainName) {
        String[] alertList = alerts.split(",")
        alertList.collect {
            domainName.findById(Integer.valueOf(it))
        }
    }

    List<Map> getExportedList(Map params) {
        def domain = params.boolean('isArchived') ? ArchivedLiteratureAlert : LiteratureAlert
        Disposition defaultLitDisposition = userService.getUser().workflowGroup.defaultLitDisposition
        cacheService.setDefaultDisp(Constants.AlertType.LITERATURE, defaultLitDisposition.id as Long)
        List literatureAlertList =[]
        if(params.selectedCases){
            literatureAlertList = listSelectedAlerts(params.selectedCases,domain)
        }else{
            List filters = []
            if (params.dispositionFilters) {
                filters = Eval.me(params.dispositionFilters)
            }
            if(filters) {
                literatureAlertList = domain.createCriteria().list() {
                    eq('exLitSearchConfig.id', params.long("configId"))
                    or {
                        'disposition' {
                            if (filters) {
                                    'in'('displayName', filters)
                            }
                        }
                    }
                    and {
                        params.each { k, v ->
                            if (k == 'alertName') {
                                ilike('name', '%' + v + '%')
                            } else if (k == 'title') {
                                ilike('articleTitle', '%' + v + '%')
                            } else if (k == 'articleId') {
                                sqlRestriction "cast( article_id AS char( 256 ) ) like '%${v}%'"
                            } else if (k == 'authors') {
                                ilike('articleAuthors', '%' + v + '%')
                            } else if (k == 'publicationDate') {
                                ilike('publicationDate', '%' + v + '%')
                            } else if (k == 'disposition') {
                                'disposition' {
                                    ilike('displayName', '%' + v + '%')
                                }
                            } else if (k == 'assignedTo') {
                                createAlias("assignedTo", "at", JoinType.LEFT_OUTER_JOIN)
                                createAlias("assignedToGroup", "atg", JoinType.LEFT_OUTER_JOIN)
                                or {
                                    ilike('at.fullName', '%' + v + '%')
                                    ilike('atg.name', '%' + v + '%')
                                }
                            } else if (k == 'productName') {
                                ilike('productSelection', '%' + v + '%')
                            } else if (k == 'eventName') {
                                ilike('eventSelection', '%' + v + '%')
                            } else if (k == 'signal') {
                                createAlias("validatedSignals", "vs", JoinType.LEFT_OUTER_JOIN)
                                iLikeWithEscape('vs.name', "%${EscapedILikeExpression.escapeString(v)}%")
                            }
                        }
                    }
                    //added by Amrendra Kumar, bug/PVS-4720 end

                } as List
            }
        }
        List<Long> alertIdList = literatureAlertList.collect{it.id}
        List<String> articleIdList = literatureAlertList.collect { it.articleId as String}
        List<Map> alertValidatedSignalList = validatedSignalService.getAlertValidatedSignalList(alertIdList,domain)
        List<Map> alertTagNameList = pvsAlertTagService.getAllAlertSpecificTags(alertIdList , Constants.AlertConfigType.LITERATURE_SEARCH_ALERT)
        List<Map> globalTagNameList = pvsGlobalTagService.getAllGlobalTags(literatureAlertList.collect{it.globalIdentityId}, Constants.AlertType.LITERATURE_ALERT)
        List<AlertComment> alertCommentList = alertCommentService.getAlertCommentByArticleIdList(articleIdList)
        List<Map> litActivityListMap = []
        ExecutorService executorService = signalExecutorService.threadPoolForLitListExec()
        List<Future> futureList = literatureAlertList.collect {def literatureAlert ->
            executorService.submit({ ->
                List<Map> litAlertTags = alertTagNameList.findAll{it.alertId == literatureAlert.id}
                List<Map> globalTags = globalTagNameList.findAll{it.globalId == literatureAlert.globalIdentityId }
                globalTags = globalTags.unique(false) { a, b ->
                    a.tagText <=> b.tagText
                }
                List<Map> allTags = litAlertTags + globalTags
                List<String> tagNameList = allTags.collect{tag->
                    if(tag.subTagText == null) {
                        tag.tagText + tag.privateUser + tag.tagType
                    }
                    else{
                        String subTags = tag.subTagText.split(";").join("(S);")
                        tag.tagText + tag.privateUser + tag.tagType + " : " + subTags + " (S)"
                    }
                }
                List validatedSignals = alertValidatedSignalList.findAll {
                    it.id == literatureAlert.id
                }?.collect { [name: it.name + "(S)", signalId: it.signalId] }

                String comment = alertCommentList.find {
                    it.articleId == literatureAlert.articleId as String
                }?.comments ?: null

                literatureAlert.toDto(tagNameList,validatedSignals,true,comment)
            } as Callable)
        }
        futureList.each {
            litActivityListMap.add(it.get())
        }
        return litActivityListMap
    }

    List getCriteriaList(Map params,ExecutedLiteratureConfiguration executedLiteratureConfiguration){
        String timeZone = userService.getCurrentUserPreference()?.timeZone
        String dateRange = DateUtil.toDateString(executedLiteratureConfiguration?.dateRangeInformation.dateRangeStartAbsolute) +
                " - " + DateUtil.toDateString(executedLiteratureConfiguration?.dateRangeInformation.dateRangeEndAbsolute)
        String date_range_type = executedLiteratureConfiguration?.dateRangeInformation?.dateRangeEnum.toString()
        String alertName=executedLiteratureConfiguration?.name
        def literatureConfig = LiteratureConfiguration.get(executedLiteratureConfiguration?.configId)
        String productSelection = executedLiteratureConfiguration?.productSelection ? ViewHelper.getDictionaryValues(executedLiteratureConfiguration, DictionaryTypeEnum.PRODUCT) : ViewHelper.getDictionaryValues(executedLiteratureConfiguration, DictionaryTypeEnum.PRODUCT_GROUP)


        List criteriaSheetList = [
                ['label': Constants.CriteriaSheetLabels.ALERT_NAME, 'value': alertName ? alertName : Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.PRIORITY, 'value': literatureConfig.priority?.displayName ?: ""],
                ['label': Constants.CriteriaSheetLabels.ASSIGNED_TO, 'value': executedLiteratureConfiguration.assignedTo?.name ?: executedLiteratureConfiguration.assignedToGroup?.name],
                ['label': 'Share With Users/Groups', 'value': getListOfShareWith(literatureConfig.shareWithUsers, literatureConfig.shareWithGroup)],
                ['label': Constants.CriteriaSheetLabels.PRODUCT, 'value': productSelection ? productSelection : Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.EVENT_SELECTION, 'value': executedLiteratureConfiguration.eventSelection ? getNameFieldFromJson(executedLiteratureConfiguration.eventSelection) : (getGroupNameFieldFromJson(executedLiteratureConfiguration.eventGroupSelection) ?: Constants.Commons.BLANK_STRING)],
                ['label': Constants.CriteriaSheetLabels.SEARCH_STRING, 'value': executedLiteratureConfiguration.searchString ?: ""],
                ['label': Constants.CriteriaSheetLabels.DATASOURCE, 'value': literatureConfig.selectedDatasource == 'pubmed' ? Constants.DataSource.PUB_MED : literatureConfig.selectedDatasource],
                ['label': Constants.CriteriaSheetLabels.DATE_RANGE, 'value': dateRange ? dateRange : Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.ARTICLE_COUNT, 'value': params.totalCount ?: Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.DATE_CREATED, 'value': DateUtil.stringFromDate(executedLiteratureConfiguration?.dateCreated, DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone)],
                ['label': Constants.CriteriaSheetLabels.REPORT_GENERATED_BY, 'value': userService.getUser().fullName ?: ""],
                ['label': Constants.CriteriaSheetLabels.DATE_EXPORTED, 'value': (DateUtil.stringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone) + userService.getGmtOffset(timeZone))]
        ]
        return criteriaSheetList
    }

    Map getStartAndEndDateForLiterature(LiteratureConfiguration configurationInstance){
        Map dateRangeMap = [:]
        List result = []
        LiteratureDateRangeInformation dateRangeInformation = configurationInstance.dateRangeInformation
        result = dateRangeInformation.getReportStartAndEndDate()
        String DATE_FORMAT = "yyyy/MM/dd";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        if(result){
            dateRangeMap.put("mindate", sdf.format(result[0]))
            dateRangeMap.put("maxdate", sdf.format(result[1]))
        }
        return dateRangeMap
    }

    Boolean isDateWithinRange(String dateString, Date startDate, Date endDate) {
        Date parsedDate = null
        List<String> dateFormats = [
                "yyyy",       // for '2023'
                "MM-yyyy",    // for '06-2023'
                "dd-MM-yyyy", // for '26-06-2023'
                "MMM-yyyy",   // for 'Jun-2023'
                "dd-MMM-yyyy" // for '26-Jun-2023'
        ]

        Calendar startCal = Calendar.getInstance()
        startCal.setTime(startDate)
        Calendar endCal = Calendar.getInstance()
        endCal.setTime(endDate)
        Calendar dateCal = Calendar.getInstance()

        for (String format : dateFormats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format)
                sdf.setLenient(false) // Set lenient to false to strictly parse dates
                parsedDate = sdf.parse(dateString)
                dateCal.setTime(parsedDate)

                if (format.equals("yyyy") && dateCal.get(Calendar.YEAR) >= startCal.get(Calendar.YEAR) && dateCal.get(Calendar.YEAR) <= endCal.get(Calendar.YEAR)) {
                    return true
                }

                if (format.equals("MM-yyyy") || format.equals("MMM-yyyy")) {
                    if (dateCal.get(Calendar.YEAR) == startCal.get(Calendar.YEAR) && dateCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR)) {
                        if (dateCal.get(Calendar.MONTH) >= startCal.get(Calendar.MONTH) && dateCal.get(Calendar.MONTH) <= endCal.get(Calendar.MONTH)) {
                            return true
                        }
                    }
                }

                if (format.equals("dd-MM-yyyy") || format.equals("dd-MMM-yy")) {
                    if (!parsedDate.before(startDate) && !parsedDate.after(endDate)) {
                        return true
                    }
                }

            } catch (Throwable th) {
            }
        }

        return false
    }
    String getPublicationDate(GPathResult data, Map monthMap, List<Date> dateRange){
        Date startDate
        startDate = dateRange[0]
        Date endDate
        endDate = dateRange[1]

        GPathResult pubDate = data?.Journal?.JournalIssue?.PubDate
        String medlineDate = pubDate?.MedlineDate?:""
        String publicationDate = ""
        String day = pubDate?.Day?(!(pubDate?.Day).equals("")? "${pubDate?.Day}-":""): ""
        String month = (pubDate?.Month)?: ""
        month = month?((month.length()==3)?"${month}-":"${monthMap.get(month)}-"):""
        String year = pubDate?.Year ? pubDate?.Year : ""
        publicationDate = day + month + year

        day = ""
        month = ""
        year = ""
        GPathResult artDate = data?.ArticleDate
        String articleDate = ""
        day = artDate?.Day?(!(artDate?.Day).equals("")? "${artDate?.Day}-":""): ""
        month = (artDate?.Month)?: ""
        month = month?((month.length()==3)?"${month}-":"${monthMap.get(month)}-"):""
        year = artDate?.Year ? artDate?.Year : ""
        articleDate = day + month + year

        return (publicationDate!="" && isDateWithinRange(publicationDate, startDate, endDate))?publicationDate:(articleDate!="" && isDateWithinRange(articleDate, startDate, endDate))?articleDate:medlineDate
    }

    LiteratureConfiguration delete(LiteratureConfiguration configurationInstance) {
        configurationInstance.isDeleted = true
        configurationInstance.isEnabled = false
        List<ExecutedLiteratureConfiguration> executedConfigurationList = ExecutedLiteratureConfiguration.findAllByConfigId(configurationInstance.id)
        if(executedConfigurationList){
            executedConfigurationList.each {
                it.isDeleted = true
                it.save(flush:true)
            }
        }
        configurationInstance.save(flush: true)
        return configurationInstance
    }

    Closure saveLiteratureActivity = { AlertLevelDispositionDTO alertLevelDispositionDTO, Boolean isArchived = false ->
        List<LiteratureActivity> activityList = []
        List<LiteratureHistory> literatureHistories = []
        List<LiteratureHistory> existingLiteratureHistories = []
        List<UndoableDisposition> undoableDispositionList = []
        alertLevelDispositionDTO.activityType = ActivityType.findByValue(ActivityTypeValue.DispositionChange)
        List<Long> groupIds = alertLevelDispositionDTO.loggedInUser.groups.findAll { it.groupType == GroupType.USER_GROUP }*.id
        alertLevelDispositionDTO.assignedToGroup = groupIds
        alertLevelDispositionDTO.literatureHistories = getExistingLiteratureHistoryList(alertLevelDispositionDTO.alertList, alertLevelDispositionDTO.execConfigId)
        Map<Long, String> tagsNameMap = getTagsNameList(alertLevelDispositionDTO.execConfigId, alertLevelDispositionDTO.reviewCompletedDispIdList)
        //This will create pool of threads to be executed in future
        ExecutorService executorService = Executors.newFixedThreadPool(20)
        List<Future> futureList = alertLevelDispositionDTO.alertList.collect { alertMap ->
            executorService.submit({ ->
                UndoableDisposition undoableDisposition = null
                LiteratureActivity activity = alertService.createLiteratureActivityForBulkDisposition(alertMap, alertLevelDispositionDTO)
                Map literatureHistoryAndExistingLiteratureHistoryMap = createLiteratureAndExistingLiteratureHistory(alertMap, tagsNameMap, alertLevelDispositionDTO)
                if(!isArchived){
                    Map dispDataMap = [objectId: alertMap.id, objectType: Constants.AlertType.LITERATURE, prevDispositionId: alertMap.disposition.id,
                                       currDispositionId: alertLevelDispositionDTO.targetDisposition.id, prevDispPerformedBy: alertMap.dispPerformedBy]
                    undoableDisposition = undoableDispositionService.createUndoableObject(dispDataMap)
                }
                [activity: activity, undoableDisposition: undoableDisposition?:null, literatureHistoryAndExistingLiteratureHistoryMap: literatureHistoryAndExistingLiteratureHistoryMap]
            } as Callable)
        }
        futureList.each {
            activityList.add(it.get()['activity'])
            literatureHistories.add(it.get()['literatureHistoryAndExistingLiteratureHistoryMap'].literatureHistories)
            undoableDispositionList.add(it.get()['undoableDisposition'])
            LiteratureHistory existingLiteratureHistory = it.get()['literatureHistoryAndExistingLiteratureHistoryMap'].existingLiteratureHistories
            if (existingLiteratureHistory) {
                existingLiteratureHistories.add(existingLiteratureHistory)
            }
        }
        executorService.shutdown()
        Map activityAndHistoryMap = [activityList           : activityList, literatureHistories: literatureHistories,
                                     existingLiteratureHistories: existingLiteratureHistories, id: alertLevelDispositionDTO.execConfigId]
        //To execute the given functionality in asynchronous environment
        notify 'literature.activity.event.published', activityAndHistoryMap
        undoableDispositionList.removeAll([null])
        if(undoableDispositionList.size()>0){
            notify 'push.disposition.changes', [undoableDispositionList:undoableDispositionList]
        }
    }

    Integer changeAlertLevelDisposition(AlertLevelDispositionDTO alertLevelDispositionDTO, Boolean isArchive = false) {
        alertService.changeLiteratureAlertLevelDisposition(saveLiteratureActivity, alertLevelDispositionDTO, isArchive)
    }

    void updateAutoRouteDisposition (def alertId, ResponseDTO responseDTO, Boolean isArchived = false) {
        def literatureAlert = isArchived ? ArchivedLiteratureAlert.get(alertId) : LiteratureAlert.get(alertId)
        if (literatureAlert) {
            Group workflowGroup = literatureAlert.exLitSearchConfig?.workflowGroup
            Boolean isAutoRouteDisposition = workflowGroup.autoRouteDisposition && (literatureAlert.disposition == workflowGroup.defaultLitDisposition)
            boolean isReviewCompleted = false
            if (isAutoRouteDisposition) {
                try {
                    isReviewCompleted = workflowGroup.autoRouteDisposition.reviewCompleted
                    Disposition previousDisposition = literatureAlert.disposition
                    literatureAlert.customAuditProperties = ["justification": workflowGroup.justificationText]
                    literatureAlert.disposition = workflowGroup.autoRouteDisposition
                    log.info("Update Auto Route Disposition from ${previousDisposition?.displayName} to ${literatureAlert.disposition?.displayName}")
                    String justification = workflowGroup.forceJustification ? workflowGroup.justificationText : ""
                    CRUDService.save(literatureAlert)
                    if(emailNotificationService.emailNotificationWithBulkUpdate(null, Constants.EmailNotificationModuleKeys.DISPOSITION_AUTO_ROUTE_LA)) {
                        emailNotificationService.mailHandlerForAutoRouteDispLA(literatureAlert, previousDisposition)
                    }
                    createActivityForDispositionChange(literatureAlert, previousDisposition, literatureAlert.disposition, justification, userService.user)
                    literatureHistoryService.saveLiteratureArticleHistory(literatureHistoryService.getLiteratureHistoryMap(null,literatureAlert,Constants.HistoryType.DISPOSITION,justification),isArchived)
                    responseDTO.status = true
                } catch (Throwable th) {
                    log.error(th.getMessage(),th)
                    responseDTO.message = "Some exception occured while updating Auto Route Disposition"
                }
            }
            if(isReviewCompleted && !isArchived) {
                alertService.updateReviewCountsForLiterature(literatureAlert.exLitSearchConfigId, 1)
            }
        }
    }

    List<Long> fetchNewArticles(def alertData) {
        List<Long> articleIds = fetchArticlesFromData(alertData)
        List oldArticleIds = GlobalArticle.createCriteria().list {
            projections {
                property('articleId')
            }
            or {
                articleIds?.collate(1000).each {
                    'in'("articleId", it)
                }
            }
        }
        return articleIds - oldArticleIds

    }

    List fetchGlobalArticles(def alertData) {
        List<Long> articleIds = fetchArticlesFromData(alertData)
        List globalArticles = GlobalArticle.createCriteria().list {
            or {
                articleIds?.collate(1000).each {
                    'in'("articleId", it)
                }
            }
        }
        return globalArticles
    }

    List<Long> fetchArticlesFromData(def alertData) {
        List<Long> articleIds = new ArrayList()
        for (def data : alertData) {
            def medlineCitationData = data?.MedlineCitation
            String articleID = medlineCitationData?.PMID
            articleID ? articleIds.add(Long.valueOf(articleID)) : null
        }

        return articleIds

    }

    @Transactional(propagation = Propagation.NEVER)
    List getFirstAndLastExecutionDate(LiteratureConfiguration configurationInstance, String timeZone, String firstExecutionDate, String lastExecutionDate) {
        List<ExecutedLiteratureConfiguration> executedConfiguration = ExecutedLiteratureConfiguration.findAllByConfigId(configurationInstance.id)
        if (executedConfiguration) {
            String firstExecutionStartDate
            String firstExecutionEndDate
            String lastExecutionStartDate
            String lastExecutionEndDate
            def length = executedConfiguration.size()
            def firstExecutionObject = executedConfiguration[0]
            getDateRangeExecutedLiteratureConfig(firstExecutionObject)
            def lastExecutionObject = executedConfiguration[length - 1]
            getDateRangeExecutedLiteratureConfig(lastExecutionObject)
            if (firstExecutionObject.dateRangeInformation.dateRangeStartAbsolute && firstExecutionObject.dateRangeInformation.dateRangeEndAbsolute) {
                firstExecutionStartDate = DateUtil.stringFromDate(firstExecutionObject.dateRangeInformation.dateRangeStartAbsolute, DateUtil.DATEPICKER_FORMAT, Constants.UTC)
                firstExecutionEndDate = DateUtil.stringFromDate(firstExecutionObject.dateRangeInformation.dateRangeEndAbsolute, DateUtil.DATEPICKER_FORMAT, Constants.UTC)
                firstExecutionDate = firstExecutionStartDate + "-" + firstExecutionEndDate
            }
            if (firstExecutionObject.dateRangeInformation.dateRangeStartAbsolute && firstExecutionObject.dateRangeInformation.dateRangeEndAbsolute) {
                lastExecutionStartDate = DateUtil.stringFromDate(lastExecutionObject.dateRangeInformation.dateRangeStartAbsolute, DateUtil.DATEPICKER_FORMAT, Constants.UTC)
                lastExecutionEndDate = DateUtil.stringFromDate(lastExecutionObject.dateRangeInformation.dateRangeEndAbsolute, DateUtil.DATEPICKER_FORMAT, Constants.UTC)
                lastExecutionDate = lastExecutionStartDate + "-" + lastExecutionEndDate
            }
        }
        [firstExecutionDate, lastExecutionDate]
    }

    @Transactional(propagation = Propagation.NEVER)
    Map generateResultMap(Map resultMap, DataTableSearchRequest searchRequest,String selectedAlertsFilter) {

        List literatureAlerts = []
        Map configList = generateAlertReviewMaps(createAlertReviewDTO(selectedAlertsFilter), executedConfigForLiterTypeAlert, searchRequest)
        String timeZone = getUserService().getCurrentUserPreference()?.timeZone
        resultMap.recordsTotal = configList?.totalCount
        if (configList?.configurationsList?.size() > 0) {
            configList.configurationsList?.each { ExecutedLiteratureConfiguration executedConfiguration ->

                String dateRange = getDateRangeExecutedLiteratureConfig(executedConfiguration)
                Map va = [
                        id                : executedConfiguration.id,
                        name              : executedConfiguration.name,
                        searchString      : executedConfiguration.searchString,
                        dateRange         : dateRange,
                        selectedDatasource: executedConfiguration?.selectedDatasource?.equalsIgnoreCase(Constants.DataSource.PUB_MED) ? Constants.DataSource.PUB_MED : executedConfiguration?.selectedDatasource,
                        dateCreated       : DateUtil.stringFromDate(executedConfiguration.dateCreated, DateUtil.DATEPICKER_FORMAT, timeZone),
                        lastUpdated       : DateUtil.stringFromDate(executedConfiguration.lastUpdated, DateUtil.DATEPICKER_FORMAT, timeZone),
                        IsShareWithAccess : getUserService().hasAccessShareWith()
                ]
                literatureAlerts.add(va)
            }
            resultMap = [aaData: literatureAlerts as Set, recordsTotal: configList.totalCount, recordsFiltered: configList.filteredCount]
        }
        resultMap
    }

    @Transactional(propagation = Propagation.NEVER)
    AlertReviewDTO createAlertReviewDTO(String filterWithUsersAndGroups = "") {

        Long workflowGroupId = getUserService().getUser().workflowGroup.id
        AlertReviewDTO alertReviewDTO = new AlertReviewDTO()
        alertReviewDTO.workflowGrpId = workflowGroupId
        alertReviewDTO.shareWithConfigs = getUserService().getUserLitConfigurations()
        alertReviewDTO.filterWithUsersAndGroups = (filterWithUsersAndGroups == "null" || filterWithUsersAndGroups == "") ? [] : filterWithUsersAndGroups?.substring(1,filterWithUsersAndGroups.length()-1).replaceAll("\"", "").split(",");
        alertReviewDTO
    }

    Closure executedConfigForLiterTypeAlert = { AlertReviewDTO alertReviewDTO ,DataTableSearchRequest searchRequest->

        eq("isLatest", true)
        eq("isDeleted", false)
        eq("isEnabled", true)
        String searchString = searchRequest?.searchParam?.search?.value;
        String esc_char = ""
        Group workflowGroup = userService.getUser().workflowGroup
        User currentUser = userService.getUser()
        String groupIds = currentUser.groups.findAll{it.groupType != GroupType.WORKFLOW_GROUP}.collect { it.id }.join(",")
        if(StringUtils.isNotBlank(searchString)){
            searchString = searchString.toLowerCase()
            if (searchString.contains('_')) {
                searchString = searchString.replaceAll("\\_", "!_%")
                esc_char = "!"
            } else if (searchString.contains('%')) {
                searchString = searchString.replaceAll("\\%", "!%%")
                esc_char = "!"
            }
            if (esc_char) {
                or {sqlRestriction("""lower(name) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'""")
                    sqlRestriction("""lower(search_string) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'""")
                    sqlRestriction("""lower(selected_data_source) like '%${searchString.replaceAll("'", "''")}%'""")
                }
            } else {
                or {sqlRestriction("""lower(name) like '%${searchString.replaceAll("'", "''")}%'""")
                    sqlRestriction("""lower(search_string) like '%${searchString.replaceAll("'", "''")}%'""")
                    sqlRestriction("""lower(selected_data_source) like '%${searchString.replaceAll("'", "''")}%'""")
                }
            }
        }
        sqlRestriction("""CONFIG_ID IN 
           (${SignalQueryHelper.literature_configuration_sql(getUserService().getCurrentUserId(), workflowGroup?.id,
                groupIds, alertReviewDTO.filterWithUsersAndGroups)}
           )""")
        if(StringUtils.isNotBlank(searchRequest?.orderBy())) {
            order(new Query.Order(searchRequest?.orderBy(),  Query.Order.Direction.valueOf(searchRequest?.searchParam?.orderDir().toUpperCase())).ignoreCase())
        }
    }

    @Transactional(propagation = Propagation.NEVER)
    Map generateAlertReviewMaps(AlertReviewDTO alertReviewDTO, Closure executedConfigReviewClosure,DataTableSearchRequest searchRequest) {

        List<ExecutedLiteratureConfiguration> configurations = ExecutedLiteratureConfiguration.createCriteria().list(max: searchRequest.pageSize(), offset: searchRequest.searchParam.start) {
            executedConfigReviewClosure.delegate = delegate
            executedConfigReviewClosure(alertReviewDTO,searchRequest)
        } as List<ExecutedLiteratureConfiguration>
        alertReviewDTO.filterWithUsersAndGroups = []
        Integer totalCount = ExecutedLiteratureConfiguration.createCriteria().count() {
            executedConfigReviewClosure.delegate = delegate
            executedConfigReviewClosure(alertReviewDTO,null)
        } as Integer

        Integer filteredCount = configurations?.totalCount
        [configurationsList: configurations, totalCount: totalCount, filteredCount: filteredCount]
    }

    @Transactional(propagation = Propagation.NEVER)
    private getDateRangeFromExecutedConfiguration(ExecutedLiteratureConfiguration c) {
        String dateRange = DateUtil.toDateString(c.dateRangeInformation.dateRangeStartAbsolute) + " to " +
                DateUtil.toDateString(c.dateRangeInformation.dateRangeEndAbsolute)
        return dateRange
    }

    @Transactional(propagation = Propagation.NEVER)
    boolean checkSearchCriteria(params) {
        if (params.searchString || params.productSelection || (params.productGroupSelection!="[]" && params.productGroupSelection) || params.eventSelection || (params.eventGroupSelection!="[]" && params.eventGroupSelection)) {
            return true
        }
        return false
    }

    @Transactional(propagation = Propagation.NEVER)
    Map getShareWithUserAndGroup(String exeConfigId){
        ExecutedLiteratureConfiguration executedConfiguration = ExecutedLiteratureConfiguration.get(exeConfigId)
        if (executedConfiguration) {
            LiteratureConfiguration config = LiteratureConfiguration.findByName(executedConfiguration.name)
            List<Map> users = config.getShareWithUsers()?.collect{[id: Constants.USER_TOKEN + it.id, name: it.fullName]}
            List<Map>  groups= config.getShareWithGroups()?.collect{[id: Constants.USER_GROUP_TOKEN + it.id, name: it.name]}
            return [users: users, groups: groups, all: users + groups]
        } else {
            log.info("Could not get the SharedWith User and Group as config id is not valid.")
        }
    }

    void editShareWith(def params) {
        if (params.sharedWith && params.executedConfigId) {
            ExecutedLiteratureConfiguration executedConfiguration = ExecutedLiteratureConfiguration.get(Long.parseLong(params.executedConfigId))
            LiteratureConfiguration config = LiteratureConfiguration.findByName(executedConfiguration.name)
            getUserService().bindSharedWithConfiguration(config, params.sharedWith, true)
            getCRUDService().update(config)
        } else {
            log.info("Could not edit share with as execution config id is not valid")
        }
    }

    void setDateRange(LiteratureConfiguration configurationInstance, Map params) {
        LiteratureDateRangeInformation dateRangeInformation = new LiteratureDateRangeInformation()
        dateRangeInformation.dateRangeEnum = params.dateRangeEnum[0]
        if (dateRangeInformation.dateRangeEnum == DateRangeEnum.CUSTOM) {
            if(params.dateRangeStartAbsolute != 'Invalid date' || params.dateRangeEndAbsolute != 'Invalid date'){
                dateRangeInformation.dateRangeStartAbsolute = new SimpleDateFormat("yyyy-MM-dd").parse(params.dateRangeStartAbsolute)
                dateRangeInformation.dateRangeEndAbsolute = new SimpleDateFormat("yyyy-MM-dd").parse( params.dateRangeEndAbsolute)
            }
            else{
                dateRangeInformation.dateRangeStartAbsolute = null
                dateRangeInformation.dateRangeEndAbsolute = null
            }
        }
        dateRangeInformation.relativeDateRangeValue = Integer.parseInt(params.relativeDateRangeValue)
        dateRangeInformation.literatureConfiguration = configurationInstance
        dateRangeInformation.save()
        configurationInstance.dateRangeInformation = dateRangeInformation
        configurationInstance.save(flush: true)
    }

    void updateAlertDateRange(LiteratureDateRangeInformation dateRangeInformation, Map params) {
        String timezone = Holders.config.server.timezone
        String dateRangeEnum = params.("alertDateRangeInformation.dateRangeEnum")
        if (dateRangeEnum) {
            dateRangeInformation?.dateRangeEnum = dateRangeEnum
            String startDateAbsolute = params.("alertDateRangeInformation.dateRangeStartAbsolute")
            String endDateAbsolute = params.("alertDateRangeInformation.dateRangeEndAbsolute")
            if (dateRangeEnum == DateRangeEnum.CUSTOM.name()) {
                //check for blank values in custom date
                try {
                    dateRangeInformation.dateRangeStartAbsolute = DateUtil.stringToDate(startDateAbsolute, 'dd-MMM-yyyy', timezone)
                    dateRangeInformation.dateRangeEndAbsolute = DateUtil.stringToDate(endDateAbsolute, 'dd-MMM-yyyy', timezone)
                } catch (Exception e) {
                    dateRangeInformation?.dateRangeStartAbsolute = null
                    dateRangeInformation?.dateRangeEndAbsolute = null
                }
            } else {
                dateRangeInformation?.dateRangeStartAbsolute = null
                dateRangeInformation?.dateRangeEndAbsolute = null
            }
        }
    }

    void deleteLiteratureConfig(LiteratureConfiguration configurationInstance) {
        configurationInstance.isDeleted = true
        configurationInstance.isEnabled = false
        configurationInstance.save(flush: true)
    }

    Integer changeAlertLevelDisposition(Disposition targetDisposition, String justificationText, def domain, ResponseDTO responseDTO, String alertName, Boolean isArchive = false , Map params) {
        AlertLevelDispositionDTO alertLevelDispositionDTO = dispositionService.populateAlertLevelDispositionDTO(targetDisposition, justificationText, domain, null, params.getLong("exConfigId"))
        alertLevelDispositionDTO.loggedInUser = userService.getUser()
        Integer updatedRowsCount = changeAlertLevelDisposition(alertLevelDispositionDTO, isArchive)
        return updatedRowsCount
    }

    void getExistingLiteratureHistoryList(List alertList, Long execConfigId) {
        List<Long> articleIds = alertList.collect { it.articleId as Long }
        List<LiteratureHistory> existingLiteratureHistories = LiteratureHistory.createCriteria().list {
            eq('litExecConfigId', execConfigId as Long)
            or {
                articleIds.collate(1000).each {
                    'in'("articleId", it)
                }
            }
            eq('isLatest', true)
        } as List<LiteratureHistory>
        existingLiteratureHistories
    }

    Map<Long, String> getTagsNameList(Long execConfigId, List<Long> reviewCompletedDispIdList) {
        Map<Long, String> tagsNameMap = [:]
        if (reviewCompletedDispIdList) {
            Session session = sessionFactory.currentSession
            String sql_statement = SignalQueryHelper.list_literature_tag_name(execConfigId, reviewCompletedDispIdList)
            SQLQuery sqlQuery = session.createSQLQuery(sql_statement)
            sqlQuery.list()?.each { row ->
                tagsNameMap.put(row[0], row[1])
            }
        }
        tagsNameMap
    }

    Map createLiteratureAndExistingLiteratureHistory(Map alertMap, Map<Long, String> tagsNameMap, AlertLevelDispositionDTO alertLevelDispositionDTO) {
        Map literatureHistoryMap = literatureHistoryMap(alertMap, tagsNameMap, alertLevelDispositionDTO)
        LiteratureHistory existingCaseHistory = alertLevelDispositionDTO.literatureHistories.find {
            it.articleId == literatureHistoryMap.articleId && it.litExecConfigId == literatureHistoryMap.litExecConfigId
        }
        LiteratureHistory history = new LiteratureHistory(literatureHistoryMap)
        history = populateCaseHistory(history, existingCaseHistory, literatureHistoryMap, alertLevelDispositionDTO)
        [literatureHistories: history, existingLiteratureHistories: existingCaseHistory]
    }

    Map literatureHistoryMap(Map alertMap, Map<Long, String> tagsNameMap, AlertLevelDispositionDTO alertLevelDispositionDTO) {
        Map literatureHistory = [
                "litConfigId"       : alertMap.litSearchConfig,
                "articleId"             : alertMap.articleId,
                "searchString"          : alertMap.searchString,
                "currentDisposition"    : alertLevelDispositionDTO.targetDisposition,
                "currentPriority"       : alertMap.priority,
                "currentAssignedTo"     : alertMap.assignedTo,
                "currentAssignedToGroup": alertMap.assignedToGroup,
                "justification"         : alertLevelDispositionDTO.justificationText,
                "litExecConfigId"       : alertLevelDispositionDTO.execConfigId,
                "change"                : Constants.HistoryType.DISPOSITION,
                "tagName"               : alertService.getAlertTagNames(alertMap.id, tagsNameMap),

        ]
        literatureHistory
    }

    LiteratureHistory populateCaseHistory(LiteratureHistory history, LiteratureHistory existingLiteratureHistory, Map literatureHistoryMap, AlertLevelDispositionDTO alertLevelDispositionDTO) {
        if (existingLiteratureHistory) {
            if (literatureHistoryMap.change != Constants.Commons.BLANK_STRING) {
                history.properties = existingLiteratureHistory.properties
                history.litConfigId = literatureHistoryMap.litSearchConfig
                history.litExecConfigId = literatureHistoryMap.litExecConfigId
                if (literatureHistoryMap.change == Constants.HistoryType.DISPOSITION) {
                    history.currentDisposition = literatureHistoryMap.currentDisposition
                    history.change = literatureHistoryMap.change
                }
                existingLiteratureHistory.isLatest = false
            }
        }
        history.justification = literatureHistoryMap.justification
        history.isLatest = true
        history.createdBy = alertLevelDispositionDTO.loggedInUser.username
        history.modifiedBy = alertLevelDispositionDTO.loggedInUser.username
        history
    }

    def getDomainObject(Boolean isArchived) {
        isArchived ? ArchivedLiteratureAlert : LiteratureAlert
    }

    @Transactional(propagation = Propagation.NEVER)
    String getDateRangeExecutedLiteratureConfig(ExecutedLiteratureConfiguration exConfig) {

        if (!exConfig?.dateRangeInformation.dateRangeEndAbsolute && !exConfig?.dateRangeInformation.dateRangeStartAbsolute) {
            DateRangeEnum dateRange = exConfig.dateRangeInformation.dateRangeEnum
            int dateRangeValue = exConfig.dateRangeInformation.relativeDateRangeValue
            ZoneId zoneId = ZoneId.of(DateTimeZone.forID(Holders.config.server.timezone).ID)
            if (DateRangeEnum.YESTERDAY.equals(dateRange) || DateRangeEnum.LAST_X_DAYS.equals(dateRange)) {
                setStartAndEndDateInConfig(exConfig, Date.from(DateUtil.convertToLocalDateViaInstant(exConfig.dateCreated).minusDays(dateRangeValue).atStartOfDay(zoneId).toInstant()))
            } else if (DateRangeEnum.LAST_WEEK.equals(dateRange) || DateRangeEnum.LAST_X_WEEKS.equals(dateRange)) {
                setStartAndEndDateInConfig(exConfig, Date.from(DateUtil.convertToLocalDateViaInstant(exConfig.dateCreated).minusWeeks(dateRangeValue).atStartOfDay(zoneId).toInstant()))
            } else if (DateRangeEnum.LAST_MONTH.equals(dateRange) || DateRangeEnum.LAST_X_MONTHS.equals(dateRange)) {
                setStartAndEndDateInConfig(exConfig, Date.from(DateUtil.convertToLocalDateViaInstant(exConfig.dateCreated).minusMonths(dateRangeValue).atStartOfDay(zoneId).toInstant()))
            } else if (DateRangeEnum.LAST_YEAR.equals(dateRange) || DateRangeEnum.LAST_X_YEARS.equals(dateRange)) {
                setStartAndEndDateInConfig(exConfig, Date.from(DateUtil.convertToLocalDateViaInstant(exConfig.dateCreated).minusYears(dateRangeValue).atStartOfDay(zoneId).toInstant()))
            }
        }
        getDateRangeFromExecutedConfiguration(exConfig)

    }

    void setStartAndEndDateInConfig(ExecutedLiteratureConfiguration exConfig, Date startDate) {
        use(TimeCategory) {
            exConfig.dateRangeInformation.dateRangeEndAbsolute = DateUtil.endOfDay(exConfig.dateCreated - 1)
        }
        exConfig.dateRangeInformation.dateRangeStartAbsolute = startDate
    }

    void dissociateLiteratureAlertFromSignal(def alert, Disposition targetDisposition, String justification, ValidatedSignal signal,
                                             Boolean isArchived) {
        Disposition previousDisposition = alert.disposition
        validatedSignalService.changeToInitialDisposition(alert, signal, targetDisposition)
        literatureHistoryService.saveLiteratureArticleHistory(literatureHistoryService.getLiteratureHistoryMap(null,alert,Constants.HistoryType.DISPOSITION,justification),isArchived)
        String changeDetails = "Disposition changed from '$previousDisposition' to '$targetDisposition' and dissociated from signal '$signal.name'"
        Map attrs = [product: getNameFieldFromJson(alert.litSearchConfig.productSelection),
                     event  : getNameFieldFromJson(alert.litSearchConfig.eventSelection)]
        activityService.createLiteratureActivity(alert.exLitSearchConfig, ActivityType.findByValue(ActivityTypeValue.DispositionChange), userService.getUser(),
                changeDetails, justification, attrs, attrs.product, attrs.event, alert.assignedTo, alert.articleId, alert.assignedToGroup, alert.searchString)
    }

}
