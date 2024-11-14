package com.rxlogix.api

import com.rxlogix.ProductGroupStatusService
import com.rxlogix.commandObjects.DictionaryGroupStatusCO
import com.rxlogix.commandObjects.TokenAuthenticationCO
import com.rxlogix.config.Configuration
import com.rxlogix.config.ExecutedTemplateQuery
import com.rxlogix.config.TemplateQuery
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.user.User
import grails.converters.JSON
import grails.rest.RestfulController
import grails.util.Holders
import org.springframework.util.StringUtils

class ProductGroupRestController extends RestfulController {
    def reportIntegrationService
    def userService
    def productAssignmentService
    ProductGroupStatusService productGroupStatusService;


    ProductGroupRestController() {
        super(String)
    }
    def saveProductGroup(DictionaryGroupStatusCO dictionaryGroupStatus) {
        TokenAuthenticationCO tokenAuthenticationCO = new TokenAuthenticationCO(dictionaryGroupStatus.apiUsername,dictionaryGroupStatus.apiTocken )
        ResponseDTO responseDTO = authenticateUserTocken(tokenAuthenticationCO)
        if(responseDTO.status == true) {
            if(StringUtils.isEmpty(dictionaryGroupStatus.apiUsername)) {
                dictionaryGroupStatus.apiUsername=tokenAuthenticationCO.username
            }
            productGroupStatusService.saveProductGroupStatusAndAudit(dictionaryGroupStatus, dictionaryGroupStatus.apiUsername, dictionaryGroupStatus.id);
        }
        render(responseDTO as JSON)
    }
    def authenticateUserTocken(TokenAuthenticationCO commandObject) {
        ResponseDTO responseDTO = new ResponseDTO()
        try {
            if (!commandObject.validate()) {
                log.warn(commandObject.errors.allErrors?.toString())
                responseDTO.setErrorResponse("Api Token cannot be null and blank")
            } else {
                User user = userService.getUserByToken(commandObject)
                if (!user) {
                    responseDTO.setErrorResponse("Token is not valid, Authentication Failed")
                } else {
                    if(userService.authenticateToken(user,commandObject.apiToken)==true){
                        responseDTO.status = true
                        responseDTO.message = "Authentication is successful"
                    } else {
                        responseDTO.setErrorResponse("Token is not valid, Authentication Failed")
                    }
                }
            }
        } catch (Exception ex) {
            responseDTO.setErrorResponse(ex)
        }
        return responseDTO
    }
    def save() {
        String pvccUrl = Holders.config.pvcc.api.url
        String productGroupPath = Holders.config.pvcc.api.path.productGroup.save
        def result = [:]
        ResponseDTO responseDTO = new ResponseDTO(status: true, code: 200)
        result = reportIntegrationService.postData(pvccUrl, productGroupPath, request.JSON)
        render(result as JSON)
    }

    def updateTemplateQuery() {
        Map result = [status: 500, message: "fail"]
        try {
            def jsonContent = request.JSON
            def jsonString = reportIntegrationService.jsonToMap(jsonContent)
            Map templateQueryDetails = (Map) jsonString

            // Updating TemplateQuery and ExecutedTemplateQuery Table for template and query
            StringBuilder updateTQ = new StringBuilder()
            StringBuilder updateETQ = new StringBuilder()
            StringBuilder updateAlertQuery = new StringBuilder()
            if (templateQueryDetails.isTemplate) {
                updateETQ.append("Update ExecutedTemplateQuery set executedTemplateName = :name where executedTemplate = :id")
                updateTQ.append("Update TemplateQuery set templateName = :name where template = :id")
            } else {
                updateETQ.append("Update ExecutedTemplateQuery set executedQueryName = :name where executedQuery = :id")
                updateTQ.append("Update TemplateQuery set queryName = :name where query = :id")
                updateAlertQuery.append("Update Configuration set alertQueryName = :name where alertQueryId = :id")
            }
            TemplateQuery.executeUpdate(updateTQ.toString(), [name: templateQueryDetails.name, id: templateQueryDetails.id as Long])
            log.info("Successfully updated TemplateQuery table after template/Query name update")
            ExecutedTemplateQuery.executeUpdate(updateETQ.toString(), [name: templateQueryDetails.name, id: templateQueryDetails.id as Long])
            log.info("Successfully updated ExecutedTemplateQuery table after template/Query name update")
            Configuration.executeUpdate(updateAlertQuery.toString(), [name: templateQueryDetails.name, id: templateQueryDetails.id as Long])
            log.info("Successfully updated RCONFIG table after template/Query name update")
            result.status = 200
            result.message = "Success"
        } catch (Exception ex) {
            log.error(ex.getMessage())
        }
        render(result as JSON)
    }

    def syncProductGroupUpdate() {
        Map result = [status: 500, message: "fail"]
        try {
            def jsonContent = request.JSON
            def jsonString = jsonContent ? reportIntegrationService.jsonToMap(jsonContent) : null
            Map productGroupDetails = (Map) jsonString
            log.info("Request received from PVR, now updating the tables after product group update.")
            log.info("Data : ${productGroupDetails}")
            if (!productGroupDetails?.isTemplate) {
                productAssignmentService.updatePvsAppConfigForPGUpdate(false)
                productAssignmentService.notifyForPGUpdate(productGroupDetails)
            }
            result.status = 200
            result.message = "Success"
        } catch (Exception ex) {
            log.error("Some error occured while syncProductGroupUpdate and updatePvsAppConfigForPGUpdate")
            log.error(ex.printStackTrace())
            ex.printStackTrace()
        }
        render(result as JSON)

    }

}
