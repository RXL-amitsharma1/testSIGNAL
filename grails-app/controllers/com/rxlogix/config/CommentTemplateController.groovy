package com.rxlogix.config

import com.rxlogix.CRUDService
import com.rxlogix.Constants
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.ArchivedAggregateCaseAlert
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.apache.http.util.TextUtils
import org.springframework.context.MessageSource
import grails.util.Holders
import org.springframework.util.StringUtils

import java.lang.reflect.Field
import java.util.regex.Matcher
import java.util.regex.Pattern

import java.util.regex.Matcher
import java.util.regex.Pattern

@Secured(["isAuthenticated()"])
class CommentTemplateController {

    CRUDService CRUDService
    def cacheService
    MessageSource messageSource
    def pvsAlertTagService
    def pvsGlobalTagService
    def alertFieldService
    def aggregateCaseAlertService
    def alertService



    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def index() {
        Map labelConfig = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', null, null, null).collectEntries {
            b -> [b.name, b.display]
        }

        List commentScoresList = aggregateCaseAlertService.fieldListAdvanceFilter('', 'AGGREGATE_CASE_ALERT').collect {
            [
                    name   : it.name,
                    enabled: it.enabled,
                    display: it.display
            ]
        }?.sort()

        commentScoresList.removeAll {
            (it.name.toString().contains("cum") || it.name.toString().contains("new") || it.enabled == false || it.name.toString() in ["disposition.id", "dispLastChange", "currentDisposition",
           "name", "assignedTo.id", "assignedToGroup.id", "currentRun", "flags", "subTags","justification","dispPerformedBy","comment","freqPeriod","reviewedFreqPeriod",
                    "freqPriority","freqPriorityFaers"
            ])
        }
        List commentCountList = alertFieldService.getCommentTemplateCount()?.sort()
        render(view: 'index', model: [commentScoresList: commentScoresList, commentCountList: commentCountList, labelConfig: labelConfig as JSON])
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def list() {
        def commentTemplateList = CommentTemplate.list().collect { it.toDto() }.sort({ -it.id })
        respond commentTemplateList, [formats: ['json']]
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def listOption() {
        def commentTemplateList = CommentTemplate.list().collect { it.toDto() }.sort({ it.name.toUpperCase()})
        render(commentTemplateList as JSON)
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def save() {
        params.templateName = params.templateName?.trim()?.replaceAll("\\s{2,}", " ")
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        try {
            if(params?.templateName?.toString()?.length()>255){
                responseDTO.status = false
                responseDTO.message = "The template name length is greater than 255 characters"
                render(responseDTO as JSON)
                return
            }
            if(TextUtils.isEmpty(params.templateName.trim())||TextUtils.isEmpty(params.comment.trim())){
                responseDTO.status = false
                responseDTO.message = "Please add all mandatory details"
                render(responseDTO as JSON)
                return
            }

            def commentTemplateInstance = CommentTemplate.findByName(params.templateName.trim())
            if(commentTemplateInstance){
                responseDTO.status = false
                responseDTO.message = "The template name is already in use, please use a different name"
                render(responseDTO as JSON)
                return
            }

            CommentTemplate commentTemplate = new CommentTemplate()
            commentTemplate.name = params.templateName.trim()
            commentTemplate.comments = params.comment?.length() == 8000 ? params.comment + " " : params.comment
            CRUDService.save(commentTemplate)
            responseDTO.message = "Comment Template saved successfully."

        } catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        } catch(Exception exp){
            responseDTO.status = false
            responseDTO.message = "Please fill all the required fields."
            log.error("${exp} Exception while saving Comment Template")
        }
        render(responseDTO as JSON);
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def delete(Long id) {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        CommentTemplate commentTemplate = CommentTemplate.get(id)
        try {
            CRUDService.delete(commentTemplate)
            responseDTO.message = "Comment Template ${commentTemplate.name} deleted successfully"
        }
        catch (Exception e) {
            responseDTO.message = "This template can not be deleted as it is being used in some safety observations/signals"
            responseDTO.status = false
        }
        render(responseDTO as JSON)
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def edit(Long id) {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        CommentTemplate commentTemplate = CommentTemplate.read(id)
        responseDTO.data = commentTemplate.toDto()
        render(responseDTO as JSON)
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def update(Long id) {
        params.templateName = params.templateName?.trim()?.replaceAll("\\s{2,}", " ")
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        try {
            if(TextUtils.isEmpty(params.templateName)||TextUtils.isEmpty(params.comment)){
                responseDTO.status = false
                responseDTO.message = "Please fill the required fields."
                render(responseDTO as JSON)
                return
            }

            CommentTemplate commentTemplate = CommentTemplate.get(id)
            def commentTemplateInstance = CommentTemplate.findByName(params.templateName.trim())
            if(commentTemplateInstance && params.templateName!= commentTemplate.name){
                responseDTO.status = false
                responseDTO.message = "The template name is already in use, please use a different name"
                render(responseDTO as JSON)
                return
            }

            commentTemplate.name = params.templateName.trim()
            commentTemplate.comments = params.comment?.length() == 8000 ? params.comment + " " : params.comment
            CRUDService.update(commentTemplate)
            responseDTO.message = "Comment Template updated successfully."
        } catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        } catch(Exception exp){
            responseDTO.status = false
            responseDTO.message = "Please fill all the required fields."
            log.error("${exp} Exception while saving Comment Template")
        }
        render(responseDTO as JSON);
    }

    def createCommentFromTemplate(){
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        CommentTemplate commentTemplate = CommentTemplate.findById(params.templateId as Long)
        def aggregateCaseAlert = AggregateCaseAlert.findById(params.acaId as Long) ? AggregateCaseAlert.findById(params.acaId as Long) : ArchivedAggregateCaseAlert.findById(params.acaId as Long)
        List<Map> categoryMap = pvsAlertTagService.getAllAlertSpecificTags([params.acaId as Long],  Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
        List<Map> globalTagNameList = pvsGlobalTagService.getAllGlobalTags([aggregateCaseAlert.globalIdentityId as Long], Constants.AlertConfigType.AGGREGATE_CASE_ALERT,  Constants.Commons.REVIEW)
        categoryMap.addAll(globalTagNameList)
        String categoryString = ""
        String subCategoryString = ""
        List<String> subCategoriesList
        if (categoryMap) {
            categoryMap.eachWithIndex { it, index ->
                if (index == categoryMap.size() - 1) {
                    if (it.subTagText != null) {
                        subCategoryString = ""
                        categoryString += it.tagText + " Category: "
                        subCategoriesList = it.subTagText.split(";").toList()
                        for (int idx = 0; idx < subCategoriesList.size(); idx++) {
                            if (idx == subCategoriesList.size() - 1) {
                                subCategoryString += subCategoriesList[idx] + '(s) '
                            } else {
                                subCategoryString += subCategoriesList[idx] + '(s), '
                            }
                        }
                        categoryString += subCategoryString
                    } else {
                        categoryString += it.tagText + " Category "

                    }

                } else {
                    if (it.subTagText != null) {
                        subCategoryString = ""
                        categoryString += it.tagText + " Category: "
                        subCategoriesList = it.subTagText.split(";").toList()
                        for (int idx = 0; idx < subCategoriesList.size(); idx++) {
                            subCategoryString += subCategoriesList[idx] + '(s), '
                        }
                        categoryString += subCategoryString
                    } else {
                        categoryString += it.tagText + " Category, "
                    }
                }

            }
        }

        Map acaProperty = aggregateCaseAlert.toDto()
        Map ebgmSubGroupMap = cacheService.getSubGroupKeyColumns()
        Map acaProperty1 =[:]

        Map propertyMap = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', null, null, null).findAll{it.type != 'subGroup'}.collectEntries {
            b -> [b.name, b.display]
        }
        propertyMap.remove("actions")
        Map subGroupPropertyMap = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', null, null, null).findAll{it.type == 'subGroup'}.collectEntries {
            b -> [b.name, b.display]
        }
        List ebgmOldSubGroupList = cacheService.getSubGroupColumns()?.flatten()
        List newFields = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', true).collect { it.name }
        List totalEvFields = ['newEvEvdas', 'newFatalEvdas', 'newLitEvdas', 'newSeriousEvdas']

        for (Map.Entry<String, Object> entry : acaProperty.entrySet()) {
            String newCount
            String cumCount
            String name
            if (newFields.contains(entry.getKey())) {
                if (entry.getKey().contains("new")) {
                    newCount = JSON.parse(entry.getValue()).new
                    cumCount = JSON.parse(entry.getValue()).cum
                    name = JSON.parse(entry.getValue()).name
                    entry.setValue(newCount)
                    acaProperty1.put(name, newCount)
                    acaProperty1.put(name.replace("new", "cum"), cumCount)
                }
            } else {
                acaProperty1.put(entry.getKey(), entry.getValue())
            }
        }
        acaProperty << acaProperty1

        String comment = commentTemplate.comments
        String rationalString=""
        rationalString += acaProperty.get("rationale").toString() == "-" ? " " : acaProperty.get("rationale").toString() + " "
        rationalString += acaProperty.get("pecImpNumHigh").toString()
        comment = comment.replace("<" + propertyMap.get("chiSquare") + ">", acaProperty.get("chiSquare").toString() != "-1.0" ? acaProperty.get("chiSquare").toString() : "-")
        comment = comment.replace("<" + propertyMap.get("alertTags") + ">", categoryString)
        comment = comment.replace("<" + propertyMap.get("pt").split("#OR")[0] + ">" + "/<" + propertyMap.get("pt").split("#OR")[1] + ">", acaProperty.get("preferredTerm").toString() ?: acaProperty.get("smqValue").toString())
        comment = comment.replace("<PT>", acaProperty.get("preferredTerm").toString() ?: acaProperty.get("smqValue").toString()) // PT added manually in Comment template
        comment = comment.replace("<Priority>", aggregateCaseAlert?.priority?.displayName?.toString()) // priority added manually
        comment = comment.replace("<"+propertyMap.get("dueDate")+">", acaProperty.get("dueIn").toString())
        comment = comment.replace("<"+propertyMap.get("signalsAndTopics")+">", aggregateCaseAlert?.validatedSignals?.toString() ?: "[]")
        comment = comment.replace("<"+propertyMap.get("rationale").split("#OR")[0]+propertyMap.get("rationale").split("#OR")[1]+">"  , rationalString)



        for (def it : propertyMap) {
            if (it.value.contains("/") && it.key.contains("new")) {
                String cumKey
                if (it.key in totalEvFields) {
                    cumKey = it.key.replace("new", "total")
                } else if (!(it.key in totalEvFields) && it.key.contains("Evdas")) {
                    cumKey = it.key.replace("new", "tot")
                } else if (it.key in ['newPediatricCount', 'newPediatricCountVaers', 'newPediatricCountVigibase', 'newPediatricCountFaers', 'newCount', 'newCountFaers', 'newCountVaers'
                                      , 'newCountVigibase', 'newInteractingCount', 'newInteractingCountFaers', 'newInteractingCountVaers', 'newInteractingCountVigibase']) {
                    cumKey = it.key.replace("new", "cumm")
                } else {
                    cumKey = it.key.replace("new", "cum")
                }
                comment = comment.replace("<" + it.value.split("/")[0] + ">", (acaProperty.get(it.key)?.toString() != "null" && acaProperty.get(it.key)?.toString() != "undefined" && acaProperty.get(it.key) != null) ? acaProperty.get(it.key).toString() : "-")
                comment = comment.replace("<" + it.value.split("/")[1] + ">", (acaProperty.get(cumKey)?.toString() != "null" && acaProperty.get(cumKey)?.toString() != "undefined" && acaProperty.get(cumKey) != null) ? acaProperty.get(cumKey).toString() : "-")
            } else if (it.value.contains("/") && !it.key.contains("new") && !it.value.contains("#OR") && !it.key.contains("dmeIme")) {
                String newCountKey=it.key
                String cumCountKey = ''
                if(it.key.contains("eb05")){
                    cumCountKey=it.key.replace("eb05","eb95")
                }else if(it.key.contains("prrLCI")){
                    cumCountKey=it.key.replace("prrLCI","prrUCI")
                }else{
                    cumCountKey=it.key
                }

                comment = comment.replace("<" + it.value.split("/")[0] + ">", (acaProperty.get(newCountKey)?.toString() != "null" && acaProperty.get(newCountKey)?.toString() != "undefined" && acaProperty.get(newCountKey) != null) ? acaProperty.get(newCountKey).toString() : "-")

                comment = comment.replace("<" + it.value.split("/")[1] + ">", (acaProperty.get(cumCountKey)?.toString() != "null" && acaProperty.get(cumCountKey)?.toString() != "undefined" && acaProperty.get(cumCountKey) != null) ? acaProperty.get(cumCountKey).toString() : "-")

            }else if (it.value.contains("#OR")) {
                String newCountKey = it.key
                String cumCountKey = it.key
                if(it.key.contains("rorValue")){
                    comment = comment.replace("<" + it.value.split("#OR")[0] + ">", (acaProperty.get(newCountKey)?.toString() != "null" && acaProperty.get(newCountKey)?.toString() != "undefined" && acaProperty.get(newCountKey) != null) ? acaProperty.get(newCountKey).toString() : "-")
                    comment = comment.replace("<" + it.value.split("#OR")[1] + ">", (acaProperty.get(cumCountKey)?.toString() != "null" && acaProperty.get(cumCountKey)?.toString() != "undefined" && acaProperty.get(cumCountKey) != null) ? acaProperty.get(cumCountKey).toString() : "-")

                }else if (it.key.contains("rorLCI")) {
                    cumCountKey = it.key.replace("rorLCI", "rorUCI")
                    comment = comment.replace("<" + it.value.split("#OR")[0].split("/")[0] + ">", (acaProperty.get(newCountKey)?.toString() != "null" && acaProperty.get(newCountKey)?.toString() != "undefined" && acaProperty.get(newCountKey) != null) ? acaProperty.get(newCountKey).toString() : "-")

                    comment = comment.replace("<" + it.value.split("#OR")[0].split("/")[1] + ">", (acaProperty.get(cumCountKey)?.toString() != "null" && acaProperty.get(cumCountKey)?.toString() != "undefined" && acaProperty.get(cumCountKey) != null) ? acaProperty.get(cumCountKey).toString() : "-")

                    comment = comment.replace("<" + it.value.split("#OR")[1].split("/")[0] + ">", (acaProperty.get(newCountKey)?.toString() != "null" && acaProperty.get(newCountKey)?.toString() != "undefined" && acaProperty.get(newCountKey) != null) ? acaProperty.get(newCountKey).toString() : "-")

                    comment = comment.replace("<" + it.value.split("#OR")[1].split("/")[1] + ">", (acaProperty.get(cumCountKey)?.toString() != "null" && acaProperty.get(cumCountKey)?.toString() != "undefined" && acaProperty.get(cumCountKey) != null) ? acaProperty.get(cumCountKey).toString() : "-")

                }

            } else {
                comment = comment.replace("<" + it.value + ">", (acaProperty.get(it.key)?.toString() != "null" && acaProperty.get(it.key)?.toString() != "undefined" && acaProperty.get(it.key) != null) ? acaProperty.get(it.key).toString() : "-")
            }
        }
        for(def it : subGroupPropertyMap){
            String ebgmfieldName = it.key.replace("ebgm","").replace("eb05","").replace("eb95","")
            if(ebgmfieldName in ebgmOldSubGroupList || it.key.toString().endsWith("Faers")){
                if(it.key.toString().endsWith("Faers")){
                    ebgmfieldName=ebgmfieldName.replace("Faers","")
                }
                String column  = ebgmSubGroupMap.get(ebgmfieldName)
                if(it.key.startsWith("ebgm")){
                    column = "ebgm"+column
                }else if(it.key.startsWith("eb05")){
                    column = "eb05"+column
                }else if(it.key.startsWith("eb95")){
                    column = "eb95"+column
                }
                if(it.key.toString().endsWith("Faers")){
                    column =column +"Faers"
                }
                String result = "-"
                String data = acaProperty.get(column)
                if(data && data != "-") {
                    ebgmfieldName = alertService.escapeSpecialCharacters(ebgmfieldName)
                    Pattern pattern = Pattern.compile("${ebgmfieldName} *: *(\\d+(\\.\\d+)?)")
                    Matcher matcher = pattern.matcher(data)
                    if (matcher.find()) {
                        result = matcher.group(1)
                    }
                }
                comment = comment.replace("<"+it.value+">",result)
            }else{
                if(comment.contains(it.value)){
                    String column  = subGroupColumn(it.key)
                    String data = acaProperty.get(column)
                    String result = "-"
                    if(data && data != "-"){
                        Pattern pattern = Pattern.compile("(\\(\\w.+\\))")
                        Matcher matcher = pattern.matcher(it.value)
                        if(matcher.find()) {
                            String matchWord = matcher.group(0)
                            if(matchWord.length() > 1) {
                                matchWord = matchWord.substring(1, matchWord.length() - 1)
                                matchWord = alertService.escapeSpecialCharacters(matchWord)
                                matchWord = "(" + matchWord + ")"
                            }
                            pattern = Pattern.compile("\"${matchWord}\" *: *(\\d+(\\.\\d+)?)")
                            matcher = pattern.matcher(data)
                            if (matcher.find()) {
                                result = matcher.group(2)
                            }
                        }
                    }
                    comment = comment.replace("<"+it.value+">",result)
                }
            }
        }
        comment = comment.toString()?.replaceAll("<BR>", " ")?.replaceAll("<br>", " ")
        responseDTO.data = comment
        render(responseDTO as JSON);
    }

    String subGroupColumn(String keyName){
        String columnName;
        switch (true) {
            case keyName.startsWith("ebgm"):
                columnName = "ebgmSubGroup"
                break
            case keyName.startsWith("eb05"):
                columnName = 'eb05SubGroup'
                break
            case keyName.startsWith("eb95"):
                columnName = 'eb95SubGroup'
                break
            case keyName.startsWith("prr"):
                columnName = 'prrSubGroup'
                break
            case keyName.startsWith("prrLci"):
                columnName = 'prrLciSubGroup'
                break
            case keyName.startsWith("prrUci"):
                columnName = 'prrUciSubGroup'
                break
            case keyName.startsWith("ror"):
                columnName = 'rorSubGroup'
                break
            case keyName.startsWith("rorLci"):
                columnName = 'rorLciSubGroup'
                break
            case keyName.startsWith("rorUci"):
                columnName = 'rorUciSubGroup'
                break
            case keyName.startsWith("rorRel"):
                columnName = 'rorRelSubGroup'
                break
            case keyName.startsWith("rorLciRel"):
                columnName = 'rorLciRelSubGroup'
                break
            case keyName.startsWith("rorUciRel"):
                columnName = 'rorUciRelSubGroup'
                break
            case keyName.startsWith("chiSquare"):
                columnName = 'chiSquareSubGroup'
                break
        }
        return columnName
    }
}
