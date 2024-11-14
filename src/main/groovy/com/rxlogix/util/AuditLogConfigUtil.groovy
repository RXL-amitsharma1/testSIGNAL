package com.rxlogix.util

import com.rxlogix.Constants
import com.rxlogix.UserService
import com.rxlogix.config.AlertFieldService
import com.rxlogix.config.Configuration
import com.rxlogix.config.EvaluationReferenceType
import com.rxlogix.config.MasterConfiguration
import com.rxlogix.config.Meeting
import com.rxlogix.config.PriorityDispositionConfig
import com.rxlogix.dto.ActivityDTO
import com.rxlogix.enums.DataSourceEnum
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.QueryLevelEnum
import com.rxlogix.enums.TimeZoneEnum
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.signal.ProductTypeConfiguration
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.converters.JSON
import grails.util.Holders
import groovy.json.JsonSlurper
import net.fortuna.ical4j.model.property.RRule
import org.grails.web.json.JSONObject

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.List;

class AuditLogConfigUtil {

    static DateFormat defaultDateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss")
    static Map actionStatusMap = ["New": "New", "InProgress": "In Progress", "Closed": "Closed", "ReOpened": "Re-Opened", "Deleted": "Deleted"]
    static  def messageSource=Holders.applicationContext.getBean("messageSource")
    static Map brAttributeMap = [:]
    //ToDO: all method available here are also avilable in common utils because the methods of traits are not accessibe in Audit config file
    static def getProductSelectionValuesForAudit(String jsonString) {
        if (jsonString == "" || jsonString == null || jsonString == "null") {
            return ""
        }
        List dicList = PVDictionaryConfig.ProductConfig.views.collect {
            messageSource.getMessage(it.code, null, Locale.default)
        }
        Map productSelection = [:]
        dicList.eachWithIndex { value, index ->
            productSelection[index+1] = value
        }
        def jsonObj = null
        def data=[]
        if (jsonString) {
            jsonObj = parseString(jsonString)
            if (!jsonObj)
                return ""
            else {
                jsonObj.each { entry ->
                    if (entry.value && entry.value[0]?.genericName) {
                        data << "${entry.value[0].genericName} (Generic Name)" //[1:[], 2:[], 3:[], 4:[], 5:[[genericName:Test-Product 1]]] generic json for adhoc alert generic seletion PVS-56096
                    } else if (entry.value.name) {
                        def productView = productSelection.get(entry.key as Integer)
                        def names = entry.value.name.collect { "${it} (${productView})" }
                        data << names.join(", ")
                    }
                }
            }
        }
       return data.join(", ")
    }

    static String getAllEventNameFieldFromJsonForAudit(jsonString) {
        if(jsonString=="" || jsonString==null || jsonString == "null"){
            return ""
        }
        Map eventSelectionMap = [1: "SOC", 2: "HLGT", 3: "HLT", 4: "PT", 5: "LLT", 6: "Synonyms", 7: "SMQ Broad", 8: "SMQ Narrow"]
        List events = []
        def jsonObj = null
        if (jsonString) {
            jsonObj = parseString(jsonString)
            if (!jsonObj)
               return ""
            else {
                jsonObj.collect { map ->
                    if (map.value != []) {
                        events << (map.value.name.collect {
                            it + " (" + eventSelectionMap.get(map.key as Integer) + ")"
                        }.join(", "))
                    }
                }
            }
        }
        events.join(", ")
    }

    static def parseString(str) {
        try {
            def jsonSlurper = new JsonSlurper()
            jsonSlurper.parseText(str)
        } catch (all) {
            null
        }
    }

    static def getEventGroupFromJsonForAudit(jsonString) {
        if(jsonString=="" || jsonString==null || jsonString == "null"){
            return ""
        }
        JsonSlurper jsonSlurper = new JsonSlurper()
        def object = jsonSlurper.parseText(jsonString)
        def events = ''
        def size = object.size()
        object.each {
            map ->
                events = events + map.name.substring(0, map.name.lastIndexOf('(') - 1)
                size--
                if (size >= 1) {
                    events += ", "
                }
        }
        return events
    }

    static def getGroupNameFieldFromJsonForAudit(jsonString) {
        if(jsonString=="" || jsonString==null || jsonString == "null"){
            return ""
        }
        def prdName = ""
        def jsonObj = null
        if (jsonString) {
            jsonObj = parseString(jsonString)
            if (!jsonObj)
                prdName = jsonString
            else {
                prdName = jsonObj.collect {
                    it.name.substring(0, it.name.lastIndexOf('(') - 1)
                }.join(",")
            }
        }
        prdName
    }

    static String getDataSourceForAudit(String dataSourceValue) {
        Map dataSourceMap = getDataSourceMapForAudit()
        List<String> dataSourceList = []
        dataSourceValue.split(',').each {
            dataSourceList.add(dataSourceMap.containsKey(it) ? dataSourceMap[it] : it)
        }
        dataSourceList.join(',')
    }

    static Map getDataSourceMapForAudit() {
        Holders.config.pvsignal.supported.datasource.call().collectEntries {
            String value = it.toString().toUpperCase()
            def enumVal = DataSourceEnum."$value"
            if (enumVal) {
                [(it): enumVal.value()]
            }
        }
    }

    static String getDrugTypeNamesForAudit(String idList) {

        String productRuleNames = ""
        def keyToNameMap = [
                'DRUG_SUSPECT_FAERS'               : 'Drug(F)-S',
                'DRUG_SUSPECT_CONCOMITANT_FAERS'   : 'Drug(F)-S+C',
                'VACCINE_SUSPECT_VAERS'            : 'Vaccine(VA)-S',
                'DRUG_SUSPECT_VIGIBASE'            : 'Drug(VB)-S',
                'DRUG_SUSPECT_CONCOMITANT_VIGIBASE': 'Drug(VB)-S+C',
                'VACCINE_SUSPECT_VIGIBASE'         : 'Vaccine(VB)-S',
                'SUSPECT'                          : 'Suspect',
                'SUSPECT_AND_CONCOMITANT'          : 'Suspect and Concomitant',
                'VACCINE'                          : 'Vaccine',
                'DRUG_SUSPECT_JADER'               : 'Drug(J)-S',
                'DRUG_SUSPECT_CONCOMITANT_JADER'   : 'Drug(J)-S+C',
        ]
        idList.split(',').each {
            if (it.isInteger()) {
                productRuleNames += (ProductTypeConfiguration.get(it) ? (ProductTypeConfiguration.get(it)?.name + ",") : "")
            } else {
                productRuleNames += (keyToNameMap[it] + ",")
            }
        }
        if (productRuleNames && productRuleNames.size() > 1) {
            productRuleNames = productRuleNames.substring(0, productRuleNames.size() - 1)
        }
    }

    static def getValueForDateEnums(def enumVal) {
        def dateEnumValueMap = [
                                    'CASE_LOCKED_DATE'      : 'Case Locked Date',
                                    'CASE_RECEIPT_DATE'     : 'Case Receipt Date',
                                    'SAFTEY_RECEIPT_DATE'   : 'Safety Receipt Date',
                                    'CREATION_DATE'         : 'Creation Date',
                                    'SUBMISSION_DATE'       : 'Submission Date',
                                    'EVENT_RECEIPT_DATE'    : 'Event Receipt Date',
                                    'LAST_X_DAYS'           : 'Last X Days',
                                    'LAST_X_WEEKS'          : 'Last X Weeks',
                                    'LAST_X_MONTHS'         : 'Last X Months',
                                    'LAST_X_YEARS'          : 'Last X Years',
                                    'VERSION_ASOF'          : 'Version As of',
                                    'CORE_SHEET'            : 'No',
                                    'ALL_SHEET'             : 'Yes',
                                    'LATEST_VERSION'        : 'Latest Version',
                                    'USER_GROUP'            : 'User Group',
                                    'WORKFLOW_GROUP'        : 'Workflow Group',
                                    'CUMULATIVE'            : 'Cumulative',
                                    'CUSTOM'                : 'Custom',
                                    'ALGORITHM'             : 'Algorithm',
                                    'COUNTS'                : 'Counts',
                                    'QUERY_CRITERIA'        : 'Query Criteria',
                                    'REVIEW_STATE'          : 'Last Review Disposition',
                                    'SIGNAL_REVIEW_STATE'   : 'Last Signal Review Disposition',
                                    'LAST_REVIEW_DURATION'  : 'Last Review Duration',
                                    'VACCINE'               : 'Vaccine',
                                    'DRUG'                  : 'Drug',
                                    'VACCINCE'              : 'Vaccine',
                                    'Date Closed'           : 'Closed'
        ]

        if (dateEnumValueMap.containsKey(enumVal)) {
            return dateEnumValueMap.get(enumVal)
        }
        return enumVal
    }

    static def getUiLabelForBrAttribute(String attributeKey) {
        if(brAttributeMap.isEmpty()){
            // need to be initialized
            initializeBrAttributeMap()
        }
//        brAttributeMap=[:]
        if (brAttributeMap.containsKey(attributeKey)) {
            return brAttributeMap.get(attributeKey)
        } else if (attributeKey.endsWith("-Previous")) {
            // Extract the actual key
            String actualKey = attributeKey.substring(0, attributeKey.length() - "-Previous".length())
            // Check if the actual key exists in the map
            if (brAttributeMap.containsKey(actualKey)) {
                return "Previous " + brAttributeMap.get(actualKey)
            }
        }
        // Return default key if key not found or doesn't match any special format
        return attributeKey
    }

    static void initializeBrAttributeMap(){
        def alertFieldService = MiscUtil.getBean('alertFieldService')
        brAttributeMap << alertFieldService.fetchLabelConfigMap(Constants.SystemPrecheck.SAFETY, "")
        def evdasMap = Holders.config.businessConfiguration.attributes.evdas.clone() as Map
        // Iterating through algorithm list
        for (Object item : evdasMap.get("algorithm")) {
            Map<String, String> entry = (Map<String, String>) item;
            brAttributeMap.put(entry.get("key"), entry.get("value"));
        }

        // Iterating through counts list
        for (Object item : evdasMap.get("counts")) {
            Map<String, String> entry = (Map<String, String>) item;
            brAttributeMap.put(entry.get("key"), entry.get("value"));
        }
        brAttributeMap << alertFieldService.fetchLabelConfigMap(Constants.SystemPrecheck.VAERS, "Vaers")
        brAttributeMap << alertFieldService.fetchLabelConfigMap(Constants.SystemPrecheck.VIGIBASE, "Vigibase")
        brAttributeMap << alertFieldService.fetchLabelConfigMap(Constants.SystemPrecheck.FAERS, "Faers")
        brAttributeMap << alertFieldService.fetchLabelConfigMap(Constants.SystemPrecheck.JADER, "Jader")
    }

    static def getScheduleDateResolve(String scJson) {
        def userService=Holders.applicationContext.getBean("userService")
        if(scJson==null || scJson=="null"||scJson==""){
            return "NA"
        }
        String repeat=''
        JSONObject timeObject = JSON.parse(scJson)
        repeat=getReadableRecurrencePattern(timeObject?.recurrencePattern)
        def startDate = MiscUtil.getReadableStartDateTime(timeObject.startDateTime)
        String timeZone = timeObject.timeZone.name + userService.getGmtOffset(timeObject.timeZone.name)
        return "Start Date and Time: ${startDate} ,TimeZone: ${timeZone} ,Repeat: ${repeat} "
    }

    static def getAllowedProductCommaSeperated(String prodString) {
        return prodString.replaceAll('#(%)*#', ',')
    }

    static String getFilterCriteriaFromFilterJson(def filterCriteria, String filterString = "") {
        JSONObject filterObject
        if (filterCriteria.class == String) {
            filterObject = JSON.parse(filterCriteria) as JSONObject
        } else {
            filterObject = filterCriteria
        }
        if (filterObject.containsKey("expressions")) {
            if (filterObject.containsKey("keyword")) {
                filterString += "["
                for (JSONObject jsonObject : filterObject["expressions"]) {
                    filterString += "${getFilterCriteriaFromFilterJson(jsonObject, "")} ${filterObject["keyword"]} "
                }
                filterString = filterString.substring(0, filterString.length() - filterObject["keyword"].length() - 1)
                filterString += "]"
            } else {
                filterString += "[${getFilterCriteriaFromFilterJson(filterObject["expressions"][0], filterString)}]"
            }
        } else if (filterObject.containsKey("all")) {
            filterString += getFilterCriteriaFromFilterJson(filterObject["all"]["containerGroups"][0], filterString)
        } else {
            filterString += (filterObject["field"] + " " + filterObject["op"] + " " + filterObject["value"])
        }
        return filterString
    }

    static def getProdAssignName(String prodString) {
        def prodMap = JSON.parse(prodString)
        return prodMap?.'name'
    }
    // Added for PVS-64840
    static def getAlertTypeForAdvanceFilter(String alertType) {
        String dmvType = ""
        int index = 0
         if(alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT){
             return  "Aggregate Alert"
         }else if(alertType.contains(Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND)){
             if(alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND || alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND_FAERS || alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND_VAERS ||  alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND_VIGIBASE||  alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND_JADER) {
                 return "Aggregate Alert (Adhoc)"
             }
             else {
                 index = alertType?.lastIndexOf("-")
                 dmvType = alertType?.substring(index + 1).split("-")[0]
                 return "Aggregate Alert (Adhoc)-<${dmvType}>"
             }
         }else {
             return alertType
         }
    }
    static def getUserNameFromIdList(String idList) {
        if(idList=='[]' || idList==null){
            return ''
        }
        def newList = idList.substring(1, idList.length() - 1).tokenize(",").collect { it as Long }
        def UserNames = User.findAllByIdInList(newList)
        return UserNames
    }

    static def getGroupNameFromIdList(String idList) {

        if (idList == '[]') {
            return ""
        }

        def newList = idList.substring(1, idList.length() - 1).tokenize(",").collect { it as Long }
        def groupNames = Group.findAllByIdInList(newList)
        return groupNames
    }

    static def getGroupNameWithType(String groupNames) {
        def nameList = groupNames.substring(1, groupNames.length() - 1).tokenize(",").collect { it.trim() as String }
        def finalList = Group.findAllByNameInList(nameList).collect() {
            it.name + " (${getValueForDateEnums(it.groupType.toString())})"
        }
        return finalList
    }

    static def getReferenceNameFromId(String id) {
        if (id == null || id == "") {
            return ""
        }
        def referenceName = EvaluationReferenceType.get(id as Long)
        return referenceName.name
    }
    static String getFilterCriteriaFromRuleJson(def filterCriteria, String filterString = "") {
        try {
            def filterObject = filterCriteria instanceof String ? new JsonSlurper().parseText(filterCriteria) : filterCriteria

            if (filterObject?.expressions) {
                if (filterObject.keyword) {
                    filterString += "["
                    filterString += filterObject.expressions.collect { getFilterCriteriaFromRuleJson(it) }.join(" ${filterObject.keyword} ")
                    filterString += "]"
                } else {
                    filterString += "[${getFilterCriteriaFromRuleJson(filterObject.expressions[0], filterString)}]"
                }
            } else if (filterObject?.all) {
                if (filterObject.all.keyword == null) {
                    filterString += getFilterCriteriaFromRuleJson(filterObject.all.containerGroups[0], filterString)
                } else {
                    filterString += "["
                    filterString += filterObject.all.containerGroups.collect { getFilterCriteriaFromRuleJson(it) }.join(" ${filterObject.all.keyword} ")
                    filterString += "]"
                }
            } else {
                if(filterObject?.category!="QUERY_CRITERIA"){
                    filterString += "${getValueForDateEnums(filterObject.category)}: ${getUiLabelForBrAttribute(filterObject.attribute)} ${(filterObject.containsKey('percent') && filterObject.percent != "" && filterObject.percent != 'undefined') ? "Percent Value: ${filterObject.percent}%" : ""} ${filterObject.operator} ${getUiLabelForBrAttribute(filterObject.threshold)}" //percent modal changes added
                }
                filterString += getFilterCriteriaString(filterObject) //this is done to do some custom things for special scenarios
            }

            return filterString.replaceAll("null", "")
        } catch (Exception ex) {
            ex.printStackTrace()
            return filterCriteria
        }
    }

    static String getFilterCriteriaString(def filterObject) {
        switch (filterObject.category) {
            case "SIGNAL_REVIEW_STATE":
                return "(Split Signals to PT Level: ${filterObject.splitSignalToPt == "true" ? "Yes" : "No"} " +
                        ",Associate PEC’s to Closed Signals: ${filterObject.assClosedSignal == "true" ? "Yes" : "No"} " +
                        ",Associate Multiple Signals to PEC’s: ${filterObject.assClosedSignal == "true" ? "Yes" : "No"})"

            case "QUERY_CRITERIA":
                return "Query Name: ${filterObject.queryName}"

            case "ALGORITHM":
                if (filterObject.containsKey("isProductSpecific")) {
                    return " (Product Specific: ${filterObject.isProductSpecific == "true" ? "Yes" : "No"})"
                }
                return ""
            default:
                return ""
        }
    }

    static def getLanguageFromLocale(String localeString){

        //this can be done generic
        if(localeString=="en"){
            return "English"
        }else if(localeString=="ja"){
            return "Japanese"
        }else {
            return localeString
        }
    }

    static String getDataSheetNames(String jsonString){
        def jsonObj = parseString(jsonString)
        List<String> list = []
        jsonObj.each { k, v ->
            list.add(v)
        }
        return list.join(",")
    }
    static def getDateListFormated(List<Date> dateList,String dateFormat="dd-MMM-yyyy"){
        List tempList=[]
        dateList.each { Date it ->
            tempList.add(it.format(dateFormat))
        }
        dateList = tempList
        return dateList.toString().replace("[", "").replace("]", "").replace(","," to")
    }

    static def getTagsFromRule(String tagString) {
        Map tagsMap = parseString(tagString) as Map
        List allTags = JSON.parse(tagsMap.tags)
        List finalTagList = allTags.collect() {
            String catName = it?.tagText
            String subCatName = it?.subTags ? it?.subTags.collect { it.replaceAll("\"", "####") }.join(',') : "" //done this to prevent conversion of " to / in join
            subCatName = subCatName.replaceAll("####", '"') // Revert the placeholder back to double quotes
            String privateStr = it?.private ? '(P)' : ''
            String alertStr = it?.alert ? '(A)' : ''
            if (subCatName.length() > 0) {
                return catName + privateStr + alertStr + " :" + subCatName
            }
            return catName + privateStr + alertStr
        }
        return finalTagList.toString()
    }
    static def getJustificationChanges(String value){
        if(value=="on"){
            return "Yes"
        }else{
            return "No"
        }
    }

   static def getRecurrencePattern(String jsonString) {
        def prdName = ""
        def jsonObj = null
        if (jsonString) {
            jsonObj = parseString(jsonString)
            if (!jsonObj)
                prdName = jsonString
            else {
                def prdVal = jsonObj.find {k,v->
                    if (k == "recurrencePattern") {
                        prdName = (v.split(";")[0]).split("=")[1]
                    }
                }
            }
        }
        prdName
    }

    static String splitCamelCase(String s) {
        if (s == null || s.length() < 1) return null
        String out = s
        if (out?.charAt(0)?.isUpperCase() && out.contains("_")) //assume that domain name was appended, remove it
            out = out.substring(out.indexOf("_"))
        while (out.contains("_")) {
            out = out.replaceFirst("_[a-z]", String.valueOf(Character.toUpperCase(out.charAt(out.indexOf("_") + 1))));
        }
        out = out.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                " "
        )
        if (out?.charAt(0)?.isLowerCase()) {
            out = "" + out.charAt(0).toUpperCase() + out.substring(1)
        }
        out
    }
    static def getMeetingNameFromId(String id) {
        if (id == null || id == "") {
            return ""
        }
        def meeting = Meeting.get(id as Long)
        return meeting?.meetingTitle
    }

    static getDateClosedConfigString(def val){
        if(val=="Yes"){
            return "Signal Workflow"
        }
        return "Signal Disposition"
    }

    static def changeStatusComments(def text){
        if (text == Constants.SignalHistory.SIGNAL_CREATED) {
            return " "
        }
        return text
    }

    static def getTimeZoneWithGmt(String timeZone) {
        def resGMTTimeZone = TimeZoneEnum.values().collect {
            [name: it?.timezoneId, display: ViewHelper.getMessage(it?.getI18nKey(), it?.getGmtOffset())]
        }.find { it.name == timeZone }
        return resGMTTimeZone?.display
    }

    static def getCreatedDateInGmt(def date){
        DateFormat gmtFormat = new SimpleDateFormat("dd-MM-YYYY hh:mm:ss a")
        return gmtFormat.format(date)+" (GMT)"
    }

    public static def getReadableRecurrencePattern(def recurrencePattern){
        //Need to use as it is from PVR
        try{
            if (recurrencePattern && Constants.Scheduler.RUN_ONCE.contains(recurrencePattern)) {
                return "Run Once"
            }
            def patternParts = recurrencePattern.split(';')
            int interval = 0, count = 0
            String frequency, endDate


            patternParts.each { part ->
                def key = part.split('=')[0]
                def value = part.split('=')[1]

                switch (key) {
                    case "INTERVAL":
                        interval = value as int
                        break

                    case "COUNT":
                        count = value as int
                        break

                    case "FREQ":
                        frequency = value.toString().toLowerCase()
                        break

                    case "UNTIL":
                        endDate = value
                        break
                }
            }

            String message = "Alert will be executed $frequency with an interval of $interval and this recurring process will "
            String untilMessage

            if (endDate){
                SimpleDateFormat dateFormatter = new SimpleDateFormat(Constants.DateFormat.BASIC_DATE)
                def d = dateFormatter.parse(endDate)
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy")
                dateFormat.applyPattern("dd-MMM-yyyy") // Adjusting the pattern
                def formattedDate = dateFormat.format(d)

                untilMessage = "continue until $formattedDate."
            }

            String countMessage = "occur a total of $count time(s)."
            if (count == 0) {
                countMessage = "ends never."
            }
            message += endDate ? untilMessage : countMessage

            // Adding recurrencePattern for complicated patterns
            message += "  (${recurrencePattern}) "

            return message
        }catch(Exception exception){
            exception.printStackTrace()
            return recurrencePattern
        }
    }

    public static getMasterConfigName(String id){
        return MasterConfiguration.get(id as Long)?.name
    }
    public static getConfigTemplateName(String id){
        return Configuration.get(id as Long)?.name
    }

    static def getActionStatusResolve(String actionStatus) {
        return actionStatusMap.getOrDefault(actionStatus, "New")
    }

    static def getMinuteHourFromMinutes(def minutes) {
        //taken from eeting controller activity
        return (minutes.toInteger() < 60) ? minutes.toString() + " min" : (minutes.toInteger()/60).toString() + " hour"
    }

    static def getAdjustmentEnumValue(def value){
        switch (value){
            case "ALERT_PER_SKIPPED_EXECUTION":
                "Auto-Adjust Date And Execute Alert For Every Skipped Execution"
                break
            case "SINGLE_ALERT_FOR_ALL_SKIPPED_EXECUTION":
                "Auto-Adjust Date And Execute A Single Alert For All Skipped Execution"
                break
            case "MANUAL_AUTO_ADJUSTMENT":
                "Disable Alert Execution And Enable Based On Manual Intervention"
                break
            default:
                "Disable Alert Execution And Enable Based On Manual Intervention"
                break
        }
    }

    static def getSpotfireData(def data) {
        if (data == null || data == "") {
            return "Generate Analysis :No"
        }
        def json = new groovy.json.JsonSlurper().parseText(data)
        def type = json.type
        def rangeType = json.rangeType.collect { messageSource.getMessage(DateRangeEnum.valueOf(it).getI18nKey(), null, Locale.default) }
        return "Analysis File Type: ${getValueForDateEnums(type)} ,Date Range: $rangeType"
    }
    static def getValueForQueryLevel(def data){
       def queryEnum=QueryLevelEnum."$data"
        if(queryEnum)
            return queryEnum.value()

        return data;
    }

}
