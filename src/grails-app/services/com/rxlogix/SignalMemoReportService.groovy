package com.rxlogix

import com.rxlogix.signal.SignalNotificationMemo
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonSlurper
import java.util.Date
import org.apache.commons.validator.routines.EmailValidator

import java.util.regex.Pattern

@Transactional
class SignalMemoReportService {

    def cacheService
    def userService
    def validatedSignalService

    void generateAutoNotificationForSignal() {
        try {
            log.info("Job for Generating the Auto Notification For Signal Starts")
            Integer days = 0
            boolean dateCriteria = false
            Map params = [:]
            List<Map> paramsForConfig = []
            List<String> configNameListForMultipleConfig = []
            List<String> configNameListForSingleConfig = []
            List<ValidatedSignal> validatedSignalsForConfig = []
            List<ValidatedSignal> validatedSignalsForMultipleConfig = []
            List<String> criteriaListForConfig = []
            List signalSourceFromSignalForMultipleConfig = []
            List actionTakenFromSignalForMultipleConfig = []
            List signalOutcomeFromSignalForMultipleConfig = []
            List<Long> signalList = []
            String configDate = Holders.config.signal.autoNotification.validated.signal.config.date
            if (configDate) {
                signalList = ValidatedSignal.createCriteria().list {
                    projections {
                        property('id')
                    }
                    sqlRestriction("TRUNC(date_created) > '${configDate.substring(0, 7) + configDate.substring(9)}'")
                } as List<Long>
            }
            List<SignalNotificationMemo> signalNotificationMemoList = SignalNotificationMemo.list().sort { it.dateCreated }.reverse()
            signalList.each { Long signalId ->
                ValidatedSignal validatedSignal = ValidatedSignal.get(signalId)
                List<String> signalSourceFromSignal = validatedSignal.initialDataSource?.split("##") ?: []
                List<String> actionTakenFromSignal = validatedSignal?.actionTaken as List<String>
                List<String> signalOutcomeFromSignal = validatedSignal.signalOutcomes.collect { it.name } ?: []
                signalNotificationMemoList.each { SignalNotificationMemo signalNotificationMemo ->
                    String configName = signalNotificationMemo.configName
                    List<SignalNotificationMemo> memoList = signalNotificationMemoList.findAll { it.configName.toLowerCase() == configName.toLowerCase() }
                    if (memoList.size() == 1) {
                        List<String> signalOutcomeFromConfig = []
                        List<String> actionTakenFromConfig = []
                        List<String> signalSourceFromConfig = signalNotificationMemo.signalSource?.split(",") ?: []
                        if (signalNotificationMemo.triggerVariable == 'Signal Outcome') {
                            signalOutcomeFromConfig = signalNotificationMemo.triggerValue.split(",")
                        } else if (signalNotificationMemo.triggerVariable == 'Action Taken') {
                            actionTakenFromConfig = signalNotificationMemo.triggerValue.split(",")
                        } else {
                            days = signalNotificationMemo.triggerValue.toInteger()
                            dateCriteria = checkIfDateSatisfiesConfig(days, validatedSignal, signalNotificationMemo.triggerVariable)
                        }
                        params = prepareParamsForMemo(signalNotificationMemo, signalNotificationMemo.mailUsers, signalNotificationMemo.mailGroups, signalNotificationMemo.emailAddress)
                        signalSourceFromConfig.removeAll(["null"])
                        boolean matchSignalSource = signalSourceFromConfig == [] ? true : compareValuesBetweenSignalAndConfig(signalSourceFromSignal, signalSourceFromConfig)
                        boolean matchSignalOutcome = signalOutcomeFromConfig ? compareValuesBetweenSignalAndConfig(signalOutcomeFromSignal, signalOutcomeFromConfig) : false
                        boolean matchActionTaken = actionTakenFromConfig ? compareValuesBetweenSignalAndConfig(actionTakenFromSignal, actionTakenFromConfig) : false
                        if (isEligibleForReportGeneration(signalNotificationMemo, validatedSignal) && ((dateCriteria && matchSignalSource) || (matchSignalSource && matchSignalOutcome) || (matchSignalSource && matchActionTaken))
                                && (signalNotificationMemo.emailSubject && signalNotificationMemo.emailBody)) {
                            if(signalNotificationMemo.dateCreated <= validatedSignal.lastUpdated) {// Multiple rmm communication is getting creating
                                configNameListForSingleConfig.add(configName)
                                paramsForConfig.add(params)
                                validatedSignalsForConfig.add(validatedSignal)
                                criteriaListForConfig.add(prepareCriteriaForCommunication(signalNotificationMemo, validatedSignal))
                            }
                        }
                        dateCriteria = false
                    } else {
                        if(signalNotificationMemo.dateCreated <= validatedSignal.lastUpdated) {
                            configNameListForMultipleConfig.add(configName)
                            validatedSignalsForMultipleConfig.add(validatedSignal)
                            signalSourceFromSignalForMultipleConfig.add(signalSourceFromSignal)
                            actionTakenFromSignalForMultipleConfig.add(actionTakenFromSignal)
                            signalOutcomeFromSignalForMultipleConfig.add(signalOutcomeFromSignal)
                        }
                    }
                }
            }
            List checkRecord = []

            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            Integer signalMemoJobIntervalSize = Holders.config.signal.autoNotification.memo.report.job.interval/3600000l;
            cal.add(Calendar.HOUR, (signalMemoJobIntervalSize)*(-1));
            Date singalMemoJobInterval = cal.getTime();

            List updatedValidatedSignals = ValidatedSignal.findAllByLastUpdatedGreaterThanEquals(singalMemoJobInterval).findAll{
                Long dateCreated = it.dateCreated.getTime();
                Long lastUpdated = it.lastUpdated.getTime();
                (lastUpdated - dateCreated) > 60000;
            }

            validatedSignalService.removeDeletedSignalMemoReportsForUpdatedSignals(updatedValidatedSignals.signalRMMs);
            log.info("deleted signal memos removed from signal rmms table");

            checkRecord = persistRecordForSingleConfig(configNameListForSingleConfig, paramsForConfig, validatedSignalsForConfig, criteriaListForConfig, checkRecord)
            persistRecordForMultipleConfig(configNameListForMultipleConfig, validatedSignalsForMultipleConfig, signalSourceFromSignalForMultipleConfig, actionTakenFromSignalForMultipleConfig, signalOutcomeFromSignalForMultipleConfig, checkRecord)
        } catch (Throwable th) {
            log.error(th.getMessage(), th)
        }
    }

    List persistRecordForSingleConfig(List<String> configName, List<Map> params, List<ValidatedSignal> validatedSignals, List<String> criteria, List checkRecord) {
        if (params && validatedSignals && criteria) {
            validatedSignals.eachWithIndex { it, index ->
                String username = SignalNotificationMemo.findByConfigName(configName[index])?.updatedBy
                if (!checkRecord.contains(it.name) && params[index].sentTo) {
                    validatedSignalService.saveSignalMemoInCommunication(params[index], it, criteria[index], username)
                    log.info "Signal Memo Report has been created for Signal:${it.name} due to ConfigName: ${configName[index]}"
                    checkRecord.add(it.name)
                }
            }
        }
        checkRecord
    }

    void persistRecordForMultipleConfig(List<String> configName, List<ValidatedSignal> validatedSignals, List<List<String>> signalSource, List<List<String>> actionTaken, List<List<String>> signalOutcome, List checkRecord) {
        if (configName && validatedSignals && signalSource && actionTaken && signalOutcome) {
            validatedSignals.eachWithIndex { it, index ->
                if (!checkRecord.contains(it.name)) {
                    checkRecord = triggerReportInCaseOfMultipleConfigNames(configName[index], it, signalSource[index], actionTaken[index], signalOutcome[index], checkRecord) ?: []
                }
            }
        }
    }

    List triggerReportInCaseOfMultipleConfigNames(String configName, ValidatedSignal validatedSignal, List<String> signalSourceFromSignal, List<String> actionTakenFromSignal, List<String> signalOutcomeFromSignal, List checkRecord) {
        List signalSource = []
        List signalOutcome = []
        List actionTaken = []
        Integer days = 0
        boolean dateCriteria = true
        List userList = []
        List groupList = []
        String emailAddress
        List triggerVariable = []
        StringJoiner joinAddresses = new StringJoiner(",")
        List<SignalNotificationMemo> signalNotificationMemoList = SignalNotificationMemo.createCriteria().list {
            eq("configName", configName, [ignoreCase: true])
        }
        signalNotificationMemoList.each { SignalNotificationMemo signalNotificationMemo ->
            triggerVariable.add(signalNotificationMemo.triggerVariable)
            signalNotificationMemo.signalSource != null ? signalSource.add(signalNotificationMemo.signalSource?.split(",")) : ""
            if (signalNotificationMemo.triggerVariable == 'Signal Outcome') {
                signalOutcome.add(signalNotificationMemo.triggerValue.split(","))
            } else if (signalNotificationMemo.triggerVariable == 'Action Taken') {
                actionTaken.add(signalNotificationMemo.triggerValue.split(","))
            } else {
                days = signalNotificationMemo.triggerValue.toInteger()
                dateCriteria = dateCriteria && checkIfDateSatisfiesConfig(days, validatedSignal, signalNotificationMemo.triggerVariable)
            }
            userList.add(signalNotificationMemo?.mailUsers)
            groupList.add(signalNotificationMemo?.mailGroups)
            emailAddress = signalNotificationMemo?.emailAddress
            emailAddress ? joinAddresses.add(emailAddress) : ''
        }
        boolean matchSignalSource = signalSource?.flatten() == [] ? true : compareValuesBetweenSignalAndConfig(signalSourceFromSignal, signalSource?.flatten())
        boolean matchSignalOutcome = compareValuesBetweenSignalAndConfig(signalOutcomeFromSignal, signalOutcome.flatten())
        boolean matchActionTaken = compareValuesBetweenSignalAndConfig(actionTakenFromSignal, actionTaken.flatten())
        List<SignalNotificationMemo> configAccToTheCreationList = signalNotificationMemoList.sort { it.dateCreated }
        Map params = prepareParamsForMemo(configAccToTheCreationList[0], userList.unique()?.flatten(), groupList.unique()?.flatten(), joinAddresses.toString())
        List<String> configIds = signalNotificationMemoList.collect { it.id as String }
        boolean matchingConditions = matchingConditionsForMultipleConfig(triggerVariable, matchSignalSource, matchSignalOutcome, matchActionTaken, dateCriteria)
        if (isEligibleForReportGenerationForMultipleRecords(validatedSignal, configIds) && matchingConditions
                && (configAccToTheCreationList[0].emailSubject && configAccToTheCreationList[0].emailBody) && params.sentTo) {
            validatedSignalService.saveSignalMemoInCommunication(params, validatedSignal, prepareCriteriaForCommunicationForMultipleRecords(validatedSignal, configIds), configAccToTheCreationList[0]?.updatedBy)
            log.info "Signal Memo Report has been created for Signal:${validatedSignal.name} due to ConfigName: ${configName}"
            checkRecord.add(validatedSignal.name)
        }
        checkRecord
    }

    boolean matchingConditionsForMultipleConfig(List triggerVariable, boolean matchSignalSource, boolean matchSignalOutcome, boolean matchActionTaken, boolean dateCriteria) {
        boolean isMatched = matchSignalSource && dateCriteria
        if (triggerVariable.contains("Signal Outcome") && triggerVariable.contains("Action Taken")) {
            isMatched = isMatched && matchSignalOutcome && matchActionTaken
        } else if (triggerVariable.contains("Signal Outcome")) {
            isMatched = isMatched && matchSignalOutcome
        } else if (triggerVariable.contains("Action Taken")) {
            isMatched = isMatched && matchActionTaken
        }
        isMatched
    }

    String prepareCriteriaForCommunicationForMultipleRecords(ValidatedSignal validatedSignal, List<String> configIds) {
        String currentCriteria, newCriteria
        List<String> criteriaList = validatedSignal.signalRMMs.sort { it.dateCreated }.reverse().collect { it.criteria }
        currentCriteria = criteriaList?.find { it != null }
        Map newCriteriaMap = [(validatedSignal.id.toString()): configIds]
        if (currentCriteria) {
            JsonSlurper js = new JsonSlurper()
            Map criteriaMap = js.parseText(currentCriteria) as Map
            List<String> configIdsExist = criteriaMap.values().flatten() as List<String>
            configIdsExist = configIdsExist + configIds
            criteriaMap[validatedSignal.id.toString()] = configIdsExist
            newCriteria = (criteriaMap as JSON).toString()
        } else {
            newCriteria = (newCriteriaMap as JSON).toString()
        }
        newCriteria
    }

    boolean isEligibleForReportGenerationForMultipleRecords(ValidatedSignal validatedSignal, List<String> configIds) {
        String currentCriteria
        boolean isEligible = true
        List<String> criteriaList = validatedSignal.signalRMMs.sort { it.dateCreated }.reverse().collect { it.criteria }
        if (!criteriaList) {
            return true
        }
        currentCriteria = criteriaList?.find { it != null }
        if (currentCriteria) {
            JsonSlurper js = new JsonSlurper()
            Map criteria = js.parseText(currentCriteria) as Map
            List<String> configIdsExist = criteria.values().flatten() as List<String>
            if (configIdsExist.intersect(configIds)) {
                isEligible = false
            }
        }
        isEligible
    }

    boolean isEligibleForReportGeneration(SignalNotificationMemo signalNotificationMemo, ValidatedSignal validatedSignal) {
        String currentCriteria
        boolean isEligible = true
        List<String> criteriaList = validatedSignal.signalRMMs.sort { it.dateCreated }.reverse().collect { it.criteria }
        if (!criteriaList) {
            return true
        }
        currentCriteria = criteriaList?.find { it != null }
        if (currentCriteria) {
            JsonSlurper js = new JsonSlurper()
            Map criteria = js.parseText(currentCriteria) as Map
            List<String> configIdsExist = criteria.values().flatten() as List<String>
            if (configIdsExist.contains(signalNotificationMemo.id as String)) {
                isEligible = false
            }
        }
        isEligible
    }

    String prepareCriteriaForCommunication(SignalNotificationMemo signalNotificationMemo, ValidatedSignal validatedSignal) {
        String currentCriteria, newCriteria
        List<String> criteriaList = validatedSignal.signalRMMs.sort { it.dateCreated }.reverse().collect { it.criteria }
        currentCriteria = criteriaList?.find { it != null }
        Map newCriteriaMap = [(validatedSignal.id.toString()): [signalNotificationMemo.id.toString()]]
        if (currentCriteria) {
            JsonSlurper js = new JsonSlurper()
            Map criteriaMap = js.parseText(currentCriteria) as Map
            List<String> configIdsExist = criteriaMap.values().flatten() as List<String>
            configIdsExist.add(signalNotificationMemo.id.toString())
            criteriaMap[validatedSignal.id.toString()] = configIdsExist
            newCriteria = (criteriaMap as JSON).toString()
        } else {
            newCriteria = (newCriteriaMap as JSON).toString()
        }
        newCriteria
    }

    Map prepareParamsForMemo(SignalNotificationMemo signalNotificationMemo, List userList, List groupList, String emailAddress) {
        Map params = [:]
        StringJoiner joinAddresses = new StringJoiner(",")
        if (emailAddress) {
            joinAddresses.add(emailAddress)
        }
        params.subject = signalNotificationMemo.emailSubject
        params.body = signalNotificationMemo.emailBody
        if (userList) {
            userList.each { def user ->
                if (user.email) {
                    joinAddresses.add(user.email)
                }
            }
        }
        if (groupList) {
            groupList.each { def group ->
                List<User> userFromGroups = cacheService.getAllUsersFromCacheByGroup(group.id)
                userFromGroups.each { User user ->
                    if (user.email) {
                        joinAddresses.add(user.email)
                    }
                }
            }
        }
        List addresses = []
        if (joinAddresses.length()) {
            addresses = joinAddresses.toString().split(',')
            addresses = validEmailAddresses(addresses)
        }
        params.sentTo = addresses
        params
    }

    List validEmailAddresses(List addresses) {
        List properAddress = []
        addresses.removeAll([null])
        addresses = addresses.unique()
        addresses.each { String email ->
            String emailRegex = "^(.+)@(.+)\$"
            Pattern pat = Pattern.compile(emailRegex)
            if (pat.matcher(email).matches()) {
                properAddress.add(email)
            }
        }
        properAddress
    }

    boolean compareValuesBetweenSignalAndConfig(List signalValue, List configValue) {
        if (signalValue == [] && configValue == []) {
            return true
        } else if (signalValue.intersect(configValue)) {
            return true
        }
        return false
    }

    boolean checkIfDateSatisfiesConfig(Integer noOfDays, ValidatedSignal validatedSignal, String triggerVariable) {
        try {
            List<Map> signalHistoryList = validatedSignalService.generateSignalHistory(validatedSignal)
            Map dateHistoryMap = [:]
            signalHistoryList.each {
                dateHistoryMap.put(it.signalStatus, it.dateCreated)
            }
            switch (triggerVariable) {
                case "Detected Date":
                    if (validatedSignal.detectedDate && (validatedSignal.detectedDate.clearTime() + noOfDays <= new Date().clearTime()))
                        return true
                    break
                case "${triggerVariable}":
                    if (dateHistoryMap["${triggerVariable}"] && (dateHistoryMap["${triggerVariable}"] + noOfDays).clearTime() <= new Date().clearTime())
                        return true
                    break
            }
            return false
        } catch (Exception ex) {
            log.error(ex.printStackTrace())
            return false
        }
    }

    boolean saveSignalMemoConfig(Map params) {
        SignalNotificationMemo signalNotificationMemo = new SignalNotificationMemo()
        if (params.signalMemoId) {
            signalNotificationMemo = SignalNotificationMemo.get(params.signalMemoId as Long)
        } else {
            signalNotificationMemo.dateCreated = new Date()
        }
        signalNotificationMemo.updatedBy = userService.getUser()?.username
        signalNotificationMemo.configName = params.configName.trim()
        signalNotificationMemo.signalSource = params.signalSource == Constants.NULL_STRING ? null : params.signalSource
        signalNotificationMemo.triggerVariable = params.triggerVariable
        signalNotificationMemo.triggerValue = params.triggerValue
        signalNotificationMemo.emailSubject = params.emailSubject
        signalNotificationMemo.emailBody = params.emailBody
        bindAddressToSignalMemo(params.mailAddresses as String, signalNotificationMemo)
        if(signalNotificationMemo.emailAddress && !checkAndBindValidEmailAddresses(signalNotificationMemo)){
            return false
        }
        signalNotificationMemo.save()
    }

    boolean validateEmailAddresses(String emailAddresses){
        boolean isEmailValid = true
        EmailValidator emailValidator = EmailValidator.getInstance()
        List<String> recepients = emailAddresses.split(',')
        for (String recepient in recepients){
            if(recepient.contains(Constants.USER_TOKEN) || recepient.contains(Constants.USER_GROUP_TOKEN)) {
                continue
            }
            else if(emailValidator.isValid(recepient)){
                continue
            }
            else {
                isEmailValid = false
            }
        }
        isEmailValid
    }

    void bindAddressToSignalMemo(String mailAddresses, SignalNotificationMemo signalNotificationMemo) {
        List<String> emails = []
        signalNotificationMemo.mailUsers = []
        signalNotificationMemo.mailGroups = []
        signalNotificationMemo.emailAddress = ''
        List<String> addresses = mailAddresses?.split(',')
        addresses.each { String address ->
            if (address.contains(Constants.USER_TOKEN)) {
                Long userId = address.replace(Constants.USER_TOKEN, "") as Long
                signalNotificationMemo.addToMailUsers(User.get(userId))
            } else if (address.contains(Constants.USER_GROUP_TOKEN)) {
                Long groupId = address.replace(Constants.USER_GROUP_TOKEN, "") as Long
                signalNotificationMemo.addToMailGroups(Group.get(groupId))
            } else {
                emails.add(address)
            }
        }
        signalNotificationMemo.emailAddress = emails.unique().collect { it }?.join(',')
    }

    boolean checkAndBindValidEmailAddresses(SignalNotificationMemo signalNotificationMemo) {
        boolean isEmailMatched = true
        StringJoiner joinAddresses = new StringJoiner(",")
        List<String> addresses = signalNotificationMemo.emailAddress.split(',')
        addresses.removeAll([null])
        addresses = addresses.unique()
        addresses.each { String email ->
            String emailRegex = "^(.+)@(.+)\$"
            Pattern pat = Pattern.compile(emailRegex)
            isEmailMatched = isEmailMatched && pat.matcher(email).matches()
            if (isEmailMatched) {
                joinAddresses.add(email)
            }
        }
        signalNotificationMemo.emailAddress = joinAddresses.toString()
        return isEmailMatched
    }

    List<Map> getUsersList(List<User> usersAssigned, List<Group> groupsAssigned, String emailAddress) {
        List<Map> noAddresses = [["name": '', "id": '']]
        if (emailAddress == null && usersAssigned?.size() == 0 && groupsAssigned.size() == 0) {
            return noAddresses
        }
        List<Map> userList = []
        List<Map> emails = []
        List address = emailAddress?.split(',')
        address?.each {
            emails.add(["name": it, "id": it])
        }
        usersAssigned?.each {
            User user = cacheService.getUserByUserId(it.id as Long)
            if (user) {
                userList.add(["name": user.fullName, "id": "User_" + it.id])
            }
        }
        groupsAssigned?.each {
            Group group = cacheService.getGroupByGroupId(it.id as Long)
            if (group) {
                userList.add(["name": group.name, "id": "UserGroup_" + it.id])
            }
        }
        userList + emails
    }

    String getUsersString(List<User> usersAssigned, List<Group> groupsAssigned, String emailAddress){
        StringJoiner joinUsers = new StringJoiner(", ")
        usersAssigned?.each {
            User user = cacheService.getUserByUserId(it.id as Long)
            if (user) {
                joinUsers.add(user.fullName)
            }
        }
        groupsAssigned?.each {
            Group group = cacheService.getGroupByGroupId(it.id as Long)
            if (group) {
                joinUsers.add(group.name)
            }
        }
        if(emailAddress){
            joinUsers.add(emailAddress)
        }
        joinUsers.toString()
    }

    Map fetchSignalMemoConfig(Map params){
        List result = []
        Integer totalRecord = 0
        Integer filteredCount = 0
        List memoObjects = []
        SignalNotificationMemo.withTransaction {
            memoObjects = getRelatedSignalNotificationMemoObjects(params)
            filteredCount = memoObjects.totalCount
            memoObjects.each { SignalNotificationMemo memo ->
                result.add([id           : memo.id, configName: memo.configName, signalSource: memo.signalSource, triggerVariable: memo.triggerVariable, triggerValue: memo.triggerValue, dateCreated: memo.dateCreated,
                            mailAddresses: getUsersList(memo.mailUsers, memo.mailGroups, memo.emailAddress), addresses: getUsersString(memo.mailUsers, memo.mailGroups, memo.emailAddress), emailSubject: memo.emailSubject, emailBody: memo.emailBody])
            }
            totalRecord = SignalNotificationMemo.count()
        }
        return [aaData: result, recordsFiltered: filteredCount, recordsTotal:totalRecord]
    }

    List getRelatedSignalNotificationMemoObjects(Map params){
        String esc_char = ""
        String searchString = params."search[value]"
        if(searchString){
            searchString = searchString.trim().toLowerCase()
        }
        String direction = params."order[0][dir]"
        String columnNo = params."order[0][column]"
        List memoObjects = SignalNotificationMemo.createCriteria().list (offset: params.start, max: params.length){
            if (searchString) {
                if (searchString.contains('_')) {
                    searchString = searchString.replaceAll("\\_", "!_%")
                    esc_char = "!"
                } else if (searchString.contains('%')) {
                    searchString = searchString.replaceAll("\\%", "!%%")
                    esc_char = "!"
                }
                if(esc_char){
                    or {
                        sqlRestriction("""lower(config_name) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'""")
                        sqlRestriction("""lower(email_subject) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'""")
                        sqlRestriction("""lower(email_body) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'""")
                        sqlRestriction("""lower(email_address) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'""")
                    }
                } else {
                    or {
                        sqlRestriction("""lower(config_name) like '%${searchString.replaceAll("'", "''")}%' """)
                        sqlRestriction("""lower(signal_source) like '%${searchString.replaceAll("'", "''")}%' """)
                        sqlRestriction("""lower(trigger_variable) like '%${searchString.replaceAll("'", "''")}%' """)
                        sqlRestriction("""lower(trigger_value) like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction("""lower(email_subject) like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction("""lower(email_body) like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction("""lower(email_address) like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction(""" {alias}.id in (
                            Select SIGNAL_NOTIFICATION_MEMO_ID from MAIL_USERS_MEMO where USER_ID in
                            (Select id from PVUSER where UPPER(full_name) like UPPER('%${searchString}%')))
                            """)
                        sqlRestriction(""" {alias}.id in (
                            Select SIGNAL_NOTIFICATION_MEMO_ID from MAIL_GROUPS_MEMO where GROUP_ID in
                            (Select id from GROUPS where UPPER(name) like UPPER('%${searchString}%')))
                            """)
                    }
                }
            }

            if(columnNo == "0"){
                sqlRestriction(" 1=1 ORDER BY UPPER(CONFIG_NAME) ${direction}")
            } else if(columnNo == "1"){
                sqlRestriction(" 1=1 ORDER BY UPPER(SIGNAL_SOURCE) ${direction}")
            }  else if(columnNo == "2"){
                sqlRestriction(" 1=1 ORDER BY UPPER(TRIGGER_VARIABLE) ${direction}")
            }  else if(columnNo == "3"){
                sqlRestriction(" 1=1 ORDER BY UPPER(TRIGGER_VALUE) ${direction}")
            }  else if(columnNo == "4"){
                String ids = getSortedObjects(searchString, direction).join(',')
                sqlRestriction(" 1=1 ORDER BY instr('${ids}', id) ")
            } else if(columnNo == "5"){
                sqlRestriction(" 1=1 ORDER BY UPPER(EMAIL_SUBJECT) ${direction}")
            }  else if(columnNo == "6"){
                sqlRestriction(" 1=1 ORDER BY UPPER(EMAIL_BODY) ${direction}")
            }  else {
                order("dateCreated", "desc")
            }
        }
        memoObjects
    }

    List getSortedObjects(String searchString, String direction){
        String esc_char = ""
        List memoObjects = SignalNotificationMemo.createCriteria().list () {
            if (searchString) {
                if (searchString.contains('_')) {
                    searchString = searchString.replaceAll("\\_", "!_%")
                    esc_char = "!"
                } else if (searchString.contains('%')) {
                    searchString = searchString.replaceAll("\\%", "!%%")
                    esc_char = "!"
                }
                if (esc_char) {
                    or {
                        sqlRestriction("""lower(config_name) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'""")
                        sqlRestriction("""lower(email_subject) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'""")
                        sqlRestriction("""lower(email_body) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'""")
                        sqlRestriction("""lower(email_address) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'""")
                    }
                } else {
                    or {
                        sqlRestriction("""lower(config_name) like '%${searchString.replaceAll("'", "''")}%' """)
                        sqlRestriction("""lower(signal_source) like '%${searchString.replaceAll("'", "''")}%' """)
                        sqlRestriction("""lower(trigger_variable) like '%${searchString.replaceAll("'", "''")}%' """)
                        sqlRestriction("""lower(trigger_value) like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction("""lower(email_subject) like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction("""lower(email_body) like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction("""lower(email_address) like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction(""" {alias}.id in (
                                Select SIGNAL_NOTIFICATION_MEMO_ID from MAIL_USERS_MEMO where USER_ID in
                                (Select id from PVUSER where UPPER(full_name) like UPPER('%${searchString}%')))
                                """)
                        sqlRestriction(""" {alias}.id in (
                                Select SIGNAL_NOTIFICATION_MEMO_ID from MAIL_GROUPS_MEMO where GROUP_ID in
                                (Select id from GROUPS where UPPER(name) like UPPER('%${searchString}%')))
                                """)
                    }
                }
            }
        }
        List users = []
        memoObjects.each {
            users.add([id: it.id, addresses: getUsersString(it.mailUsers, it.mailGroups, it.emailAddress)])
        }
        if(direction == "asc"){
            users.sort{ a,b-> a.addresses.toLowerCase() <=> b.addresses.toLowerCase() }
        } else {
            users.sort{ a,b-> b.addresses.toLowerCase() <=> a.addresses.toLowerCase() }
        }
        users.collect { it.id }
    }
}
