package com.rxlogix

import com.rxlogix.config.ActivityType
import com.rxlogix.config.ActivityTypeValue
import com.rxlogix.config.Configuration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.ProductEventHistory
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.hibernate.Session

@Transactional
class ProductEventHistoryService {

    def CRUDService
    def cacheService
    def sessionFactory
    def activityService
    def userService

    def preparePEHistoryOnExecution(Map peHistoryMap) {
        ProductEventHistory productEventHistory = new ProductEventHistory(peHistoryMap)
        productEventHistory.dateCreated = new Date()
        productEventHistory.lastUpdated = new Date()
        productEventHistory.executionDate = new Date()
        //Set the passed scores value if its execution flow.
        productEventHistory.prrValue = peHistoryMap.prrValue
        productEventHistory.rorValue = peHistoryMap.rorValue
        productEventHistory.ebgm = peHistoryMap.ebgm
        productEventHistory.eb05 = peHistoryMap.eb05
        productEventHistory.eb95 = peHistoryMap.eb95
        productEventHistory.justification = peHistoryMap.justification
        productEventHistory.isLatest = true
        productEventHistory.change = peHistoryMap.change
        productEventHistory.createdBy = peHistoryMap.createdBy
        productEventHistory.modifiedBy = peHistoryMap.modifiedBy
        productEventHistory.asOfDate = peHistoryMap.asOfDate
        productEventHistory.tagName = peHistoryMap.tagName
        productEventHistory
    }

    List getAlertHistoryList(String productName, String eventName, Long configId, Long executedConfigId) {
        String userTimezone = userService.getCurrentUserPreference()?.timeZone ?: Constants.UTC
        List alertHistoryList = []
        if (executedConfigId) {
            alertHistoryList = getCurrentAlertProductEventHistoryList(productName, eventName, configId, executedConfigId)
        } else {
            alertHistoryList = getOtherAlertsProductEventHistoryList(productName, eventName, configId)
        }
        List configIds = []
        if(!executedConfigId) {
            configIds = userService.getUserConfigurationsForCurrentUser(Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
        }

        List<Map> productEventHistoryList = alertHistoryList?.collect { (it.toDto(userTimezone, configIds)) }
        productEventHistoryList
    }

    //This will return the Review History for current Alert
    List<ProductEventHistory> getCurrentAlertProductEventHistoryList(String productName, String eventName, Long configId, Long executedConfigId = null) {
        Configuration configuration = Configuration.get(configId)
        Map queryParaters = [configId:configId, change:Constants.HistoryType.ASSIGNED_TO]
        StringBuilder hqlQuery = new StringBuilder()
        if (configuration.productGroupSelection) {
            String productId = configuration.getIdsForProductGroup(configuration.productGroupSelection)
            List productIdsList = productId.split(',').collect { it as Long }
            if (executedConfigId > 0) {
                ExecutedConfiguration execConfiguration = ExecutedConfiguration.get(executedConfigId)
                productId = execConfiguration.getIdsForProductGroup(execConfiguration.productGroupSelection)
                List productIds = productId.split(',').collect { it as Long }
                productIds.each {
                    if(!productIdsList.contains(it)) {
                        productIdsList.add(it)
                    }
                }
            }
            hqlQuery.append("from ProductEventHistory where productId in (:items) and configId=:configId and change is not null and change!=:change")
            queryParaters << [items: productIdsList]
        }
        else {
            hqlQuery.append("from ProductEventHistory where productName=:productName and configId=:configId and change is not null and change!=:change")
            queryParaters << [productName:productName]
        }
        if(configuration.eventGroupSelection && configuration.groupBySmq){
            String eventId = configuration.getIdsForEventGroup(configuration.eventGroupSelection)
            List eventIdsList = eventId.split(',').collect { it as Long }
            hqlQuery.append(" and eventId in (:eventIdList)")
            queryParaters << [eventIdList:eventIdsList]
        }else{
            hqlQuery.append(" and eventName=:eventName")
            queryParaters << [eventName:eventName]
        }
        if (executedConfigId > 0) {
            hqlQuery.append(" and execConfigId<=:executedConfigId")
            queryParaters << [executedConfigId:executedConfigId]
        }
        hqlQuery.append(" order by lastUpdated desc, id desc")
        List<ProductEventHistory> pEHistories = ProductEventHistory.executeQuery(hqlQuery.toString(), queryParaters)

        return removeAllPrivateHistory(pEHistories)
    }

    //This will return the Review History for other Alerts with same product And Event Name
    List<ProductEventHistory> getOtherAlertsProductEventHistoryList(String productName, String eventName, Long configId){
        Configuration configuration = Configuration.get(configId)
        Map queryParaters = [configId:configId, eventName:eventName, change:Constants.HistoryType.ASSIGNED_TO]
        StringBuilder hqlQuery = new StringBuilder()
        if (configuration.productGroupSelection) {
            String productId = configuration.getIdsForProductGroup(configuration.productGroupSelection)
            List productIdsList = productId.split(',').collect { it as Long }

            hqlQuery.append("from ProductEventHistory where productId in (:items) and eventName=:eventName and configId!=:configId and change is not null and change!=:change order by lastUpdated desc,id desc")
            queryParaters << [items: productIdsList]
        }
        else {
            String productId = configuration.getIdFieldFromJson(configuration.productSelection)
            List productIdsList = productId.split(',').collect{ it as Long}
            hqlQuery.append("from ProductEventHistory where productName=:productName and eventName=:eventName and productId in (:productId) and configId!=:configId and change is not null and change!=:change order by lastUpdated desc,id desc")
            queryParaters << [productName:productName,productId: productIdsList]
        }
        List<ProductEventHistory> pEHistories = ProductEventHistory.executeQuery(hqlQuery.toString(), queryParaters)
        return removeAllPrivateHistory(pEHistories)
    }

    Map getProductEventHistoryMap(ProductEventHistory productEventHistory){
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(productEventHistory.execConfigId)
        List allTagNames = productEventHistory.tagName? JSON.parse(productEventHistory.tagName) : []
        String userTimezone = userService.getCurrentUserPreference()?.timeZone ?: Constants.UTC
        String currentUsername = userService.getUser()?.username
        List tagNames = allTagNames.findAll{
            (it.privateUser == null || (it.privateUser!=null && it.privateUser == currentUsername))
        }.each{
            if(it.type == Constants.Commons.CASE_SERIES_TAG)
                it.name = it.name + ' (A)'
            else if(it.type == Constants.Commons.PRIVATE_TAG_GLOBAL)
                it.name = it.name + ' (P)'
            else if(it.type == Constants.Commons.PRIVATE_TAG_ALERT)
                it.name = it.name + ' (A)(P)'
        }?.unique()
        List allSubTagNames = productEventHistory.subTagName? JSON.parse(productEventHistory.subTagName) : []
        List subTagNames = allSubTagNames.findAll{
            (it.privateUser == null || (it.privateUser!=null && it.privateUser == currentUsername))
        }
        [
                alertName    : Configuration.findById(productEventHistory.configId)?.name,
                disposition  : productEventHistory.disposition?.displayName,
                priority     : productEventHistory.priority?.displayName,
                justification: productEventHistory.justification,
                timestamp    : new Date(DateUtil.toDateStringWithTime(productEventHistory.lastUpdated, userTimezone)).format(DateUtil.DATEPICKER_FORMAT_AM_PM).toString(),
                updatedBy    : productEventHistory.modifiedBy,
                alertTags    : tagNames?.name,
                alertSubTags : subTagNames?.name,
                reviewPeriod : DateUtil.toDateString(executedConfiguration.executedAlertDateRangeInformation.dateRangeStartAbsolute) +
                        " - " + DateUtil.toDateString(executedConfiguration.executedAlertDateRangeInformation.dateRangeEndAbsolute)
        ]
    }

    /**
     * The trend is calculated based on the current and its previous instance of history.
     * If the EBGM score for the PE history is greater than previous one then the trend is high otherwise the trend
     * is low.
     * @param productName
     * @param eventName
     */
    def getProductEventHistoryTrend(productName, eventName) {

        def peCriteria = ProductEventHistory.createCriteria()

        def peResult = peCriteria.list {
            eq("productName", productName)
            eq("eventName", eventName)
            order("id", "desc")
            maxResults(2)
        }
        if (peResult.size() > 1) {
            def firstEbgmScore = peResult[0]?.ebgm
            def secondEbgmScore = peResult[1]?.ebgm

            if (firstEbgmScore && secondEbgmScore) {
                def compareScore = Double.compare(firstEbgmScore, secondEbgmScore);

                if (compareScore > 0) {
                    return Constants.Commons.POSITIVE
                } else if (compareScore < 0) {
                    return Constants.Commons.NEGATIVE
                } else {
                    return Constants.Commons.EVEN
                }
            }
        }
        return Constants.Commons.EVEN
    }

    def getProductEventHistoryTrendPrr(productName, eventName) {

        def peCriteria = ProductEventHistory.createCriteria()

        def peResult = peCriteria.list {
            eq("productName", productName)
            eq("eventName", eventName)
            order("id", "desc")
            maxResults(2)
        }
        if (peResult.size() > 1) {
            def firstPrrScore = Double.parseDouble(peResult[0]?.prrValue)
            def secondPrrScore = Double.parseDouble(peResult[0]?.prrValue)

            if (firstPrrScore && secondPrrScore) {
                def compareScore = Double.compare(firstPrrScore, secondPrrScore);

                if (compareScore > 0) {
                    return Constants.Commons.POSITIVE
                } else if (compareScore < 0) {
                    return Constants.Commons.NEGATIVE
                } else {
                    return Constants.Commons.NEGATIVE
                }
            }
        }
        return Constants.Commons.EVEN
    }

    def getProductEventHistoryTrendCounts(productName, eventName) {

        def peCriteria = ProductEventHistory.createCriteria()

        def peResult = peCriteria.list {
            eq("productName", productName)
            eq("eventName", eventName)
            order("id", "desc")
            maxResults(2)
        }
        if (peResult.size() > 1) {
            def firstNewCount = peResult[0]?.newCount
            def secondNewCount = peResult[1]?.newCount
            if (firstNewCount && secondNewCount)
                if (secondNewCount * 1.5 < firstNewCount)
                    return Constants.Commons.POSITIVE
                else
                    return Constants.Commons.NEGATIVE
        }
        return Constants.Commons.EVEN
    }

    void batchPersistHistory(peHistoryMapList) {

        ProductEventHistory.withTransaction {
            def batch = []

            for(def peHistoryMap : peHistoryMapList) {
                batch += peHistoryMap
                if (batch.size() > Holders.config.signal.batch.size) {
                    Session session = sessionFactory.currentSession
                    for (def peHistory in batch) {
                        ProductEventHistory productEventHistory = preparePEHistoryOnExecution(peHistory)
                        productEventHistory.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                    batch.clear()
                }
            }

            if (batch) {
                try {
                    Session session = sessionFactory.currentSession
                    for (def peHistory in batch) {
                        ProductEventHistory productEventHistory = preparePEHistoryOnExecution(peHistory)
                        productEventHistory.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                } catch (Throwable th ) {
                    th.printStackTrace()
                }
            }
            log.info("History data is batch persisted.")
        }
    }

    void batchPersistPEHistory(List<ProductEventHistory> productEventHistoryList, def PEHistory) {

        ProductEventHistory.withTransaction {
            def batch = []

            for(ProductEventHistory productEventHistory : productEventHistoryList) {
                batch += productEventHistory
                if (batch.size() > Holders.config.signal.batch.size) {
                    Session session = sessionFactory.currentSession
                    for (ProductEventHistory peHistory in batch) {
                        peHistory.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                    PEHistory.add(batch*.id)
                    batch.clear()
                }
            }

            if (batch) {
                try {
                    Session session = sessionFactory.currentSession
                    for (ProductEventHistory peHistory in batch) {
                        peHistory.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                    PEHistory.add(batch*.id)
                } catch (Throwable th ) {
                    th.printStackTrace()
                }
            }
        }
    }

    void createActivityForJustificationChange(ProductEventHistory productEventHistory, String oldJustification) {
        AggregateCaseAlert alert = AggregateCaseAlert.get(productEventHistory.aggCaseAlertId)
        if(alert){
            User currentUser = userService.getUser()?:User.findByUsername("signaldev")
            String textInfo = productEventHistory.change == Constants.HistoryType.DISPOSITION ? "Disposition (${productEventHistory?.disposition?.displayName})" : productEventHistory.change == Constants.HistoryType.PRIORITY ? "Priority (${productEventHistory?.priority?.displayName})" : ''
            String details = "Justification for ${textInfo} changed from '$oldJustification' to '${productEventHistory.justification}'"
            ActivityType activityType = ActivityType.findByValue(ActivityTypeValue.JustificationChange)
            activityService.createActivity(alert.executedAlertConfiguration, activityType, currentUser, details, '', ['For Aggregate Alert'],
                    alert.productName, alert.pt, alert.assignedTo, null, alert.assignedToGroup)
        }
    }

    ProductEventHistory getPEHistoryByPEC(String productName, String eventName, Long configId) {
        ProductEventHistory.createCriteria().get {
            eq("productName", productName)
            eq("eventName", eventName)
            eq("configId", configId)
            order("lastUpdated", "desc")
            maxResults(1)
        } as ProductEventHistory

    }

    List<ProductEventHistory> removeAllPrivateHistory(List<ProductEventHistory> pEHistories) {
        List<ProductEventHistory> newPEHistories = pEHistories.findAll{
            String tags = it.tagsUpdated
            if(tags && tags != '[]') {
                List allTagNames = JSON.parse(tags)
                String currentUsername = userService.getUser().username
                String privateUserNameTag
                Integer count = 0
                allTagNames.each{tag->
                    if(tag.privateUser) {
                        count++
                        privateUserNameTag = tag.privateUser
                    }
                }
                count != allTagNames.size() || currentUsername == privateUserNameTag

            } else {
                true
            }
        }
        return newPEHistories
    }
}