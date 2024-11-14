class UrlMappings {
    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
        }
        "/policy"(controller: 'login', action: 'securityAndPrivacyPolicy')
        "/"(controller:"dashboard", action:"index")

        "403"(controller: "errors", action: "forbidden")
        "404"(controller: "errors", action: "notFound")
        "405"(controller: "errors", action: "notAllowed")
        "500"(controller: "errors", action: "serverError")

        "/login/$action?"(controller: "login")
        "/logout/$action?"(controller: "logout")
        "/keep-alive"(controller: 'user', action: 'keepAlive')

        "/console/$action?"(controller: "console")
        "/singleCaseAlert/caseSeriesCallback/${id}/${isCumulative}" (controller: "singleCaseAlert", action: "caseSeriesCallback")
        "/stopList/$action?"(controller: "emergingIssue")
        "/stopList/edit/${id}" (controller: "emergingIssue", action: "edit")
        "/stopList/delete/${id}" (controller: "emergingIssue", action: "delete")

        "/health/$id"(controller: 'health', action: 'index', accessKey: id)
        "/ping/$id"(controller: 'health', action: 'ping', accessKey: id)

        group "/adminPanel" ,{
            "/"(controller:'admin' ,action: 'index' )
            "/addNewUsers"(controller:'admin' ,action: 'addUsers' )
            "/downloadTemplate"(controller:'admin' ,action: 'downloadUserTemplate' )
        }

        group "/api", {
            "/templates"(resources: "reportTemplateRest")
            "/configurations"(resources: "configurationRest")
            "/templates/columns"(controller: "reportTemplateRest", action: "columns", method: "GET")
            "/reportBuilder"(controller: "reportTemplateRest", action: "runReport", method: "GET" )

            "/signalReport"(controller: "reportTemplateRest", action: "saveSignalReport", method: "POST" )

            "/queries"(resources: "queryRest")

            "/signal"(resources: "reportResultRest", excludes: ["update", "create"])
            "/reportData"(resources:"reportResultDataRest", excludes: ["index", "create", "save", "edit", "update"])
            "/singleCaseAlert"(resource: "singleCaseAlertRest")

            "/tags"(resources: "tagRest", excludes: ["delete", "show", "create", "save", "edit", "update"])

            "/tags/saveAlertCategories"(controller: "tagRest", action: "saveAlertCategories" , method: "POST")
            "/tags/saveGlobalCategories"(controller: "tagRest", action: "saveGlobalCategories", method: "POST" )

            "/adHocAlert"(resources: "adHocAlertRest")
            "/adHocAlert/importAlert"(controller: "adHocAlertRest", action: "importAlert", method: "POST" )
            "/adHocAlert/importDocument"(controller: "adHocAlertRest", action: "importDocument", method: "POST" )
            "/executedCaseSeries/saveCaseSeriesForSpotfire"(controller: "configurationRest", action: "saveCaseSeriesForSpotfire" , method: "POST")
            "/token/authentication"(controller: "batchRest", action: "authenticate" , method: "POST")
            "/batchlot/import"(controller: "batchRest", action: "importBatchLot" , method: "POST")
            "/runEtl"(controller: "batchRest", action: "runEtl" , method: "POST")
            "/productGroup/save"(controller: "productGroupRest", action: "save", method: "POST")
            "/productGroup/saveProductGroup"(controller: "productGroupRest", action: "saveProductGroup", method: "POST")
            "/productGroup/updateTemplateQuery"(controller: "productGroupRest", action: "updateTemplateQuery", method: "POST" )
            "/productGroup/syncProductGroup"(controller: "productGroupRest", action: "syncProductGroupUpdate", method: "POST")
            "/user/fetchUser"(controller: "userRest", action: "fetchUser", method: "GET")
            "/businessConfig/exportBusinessConfig"(controller: "businessConfig", action: "exportBusinessConfig", method: "GET")
            "/businessConfig/importBusinessConfig"(controller: "businessConfig", action: "importBusinessConfig", method: "POST")
        }

        group '/scim/v2', {
            '/Schemas'(controller: 'scimHome', action: 'schemas')
            "/Schemas/$id"(controller: 'scimHome', action: 'schemas')

            '/ResourceTypes'(controller: 'scimHome', action: 'resourceTypes') //TODO need to implement in plugin
            "/ResourceTypes/$id"(controller: 'scimHome', action: 'resourceTypes') //TODO need to implement in plugin

            '/ServiceProviderConfig'(controller: 'scimHome', action: 'serviceProviderConfig') //TODO need to implement in plugin

            '/ServiceConfiguration'(controller: 'scimHome', action: 'serviceProviderConfig') //TODO need to implement in plugin

            '/Users'(controller: 'scimUser') {
                action = [GET: 'index', POST: 'save']
            }
            "/Users/$id"(controller: 'scimUser') {
                action = [GET: 'show', DELETE: 'delete', PATCH: 'patch', PUT: 'update', POST: 'patch']
            }

            '/Groups'(controller: 'scimGroup') {
                action = [GET: 'index', POST: 'save']
            }
            "/Groups/$id"(controller: 'scimGroup') {
                action = [GET: 'show', DELETE: 'delete', PATCH: 'patch', PUT: 'update', POST: 'patch']
            }
        }
    }
}
