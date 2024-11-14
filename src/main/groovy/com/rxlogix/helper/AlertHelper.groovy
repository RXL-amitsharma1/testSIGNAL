package com.rxlogix.helper

import com.rxlogix.config.EvaluationReferenceType
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.util.Holders

class AlertHelper {
    def static composeDetailRowsForDisplay(alert, timezone) {
        def attributes = [
                    [
                            ["Alert Details", "name", "2"], ["Product/Generic Name", "productSelection", "2"], ["Topic Name", "topic", "2"], ["Event", "eventSelection", "2"],
                            ["Detected By", "detectedBy", "2"], [Holders.config.alert.adhoc.custom.fields.detected.date, "detectedDate", "2"]
                    ],
                    [
                            ['Initial Datasource', 'initialDataSource', "2"], ['No of ICSR', 'numberOfICSRs', "2"], ["Issue Previously Tracked", "issuePreviouslyTracked", "2"],
                            ['Population Specific', 'populationSpecific', "2"], ['Report Type', 'reportType', "2"], ["Reference Type", "refType", "2"]
                    ],
                    [
                            ['Indication', 'indication', "2"], ["Formulation", "formulations", "2"], ['Device Related', 'deviceRelated', "2"], ['Country', 'countryOfIncidence', "2"],
                            ['Assigned To', 'assignedTo', "2"], ['Public', 'publicAlert', "2"]
                    ],
            ]

        if(Holders.config.alert.adhoc.custom.fields.enabled == true) {
            attributes.add([
                    ['Population Specific', 'populationSpecific', '2'], ['Evaluation Methods', 'evaluationMethods', "2"], ['Aggregate Report Start Date', 'aggReportStartDate', "2"],
                    ['Aggregate Report End Date', 'aggReportEndDate', "2"], ['Last Decision Date', 'lastDecisionDate', "2"],['Share With', 'shareWith', '2']
            ])
        } else{
            attributes.add([
                    ['Population Specific', 'populationSpecific', '2'],['Share With', 'shareWith', '2']
            ])
        }

        attributes.add([['Comments', 'notes', "12"]])
        attributes.add([['Description', 'description', "12"]])

        if(Holders.config.alert.adhoc.custom.fields.enabled == true) {
            attributes.add([

                            ['Signal Status', 'haSignalStatus', "2"], ['Date Closed', 'haDateClosed', "2"], ['Action Taken', 'actionTaken', "4"]
            ])
            attributes.add([['Comments on Signal Status', 'commentSignalStatus', '12']])
        }


        def tmp = attributes.collect {
            it.collect { item ->

                def value = null

                if (alert.hasProperty("${item[1]}")) {
                    value = alert."${item[1]}"
                } else {
                    value = alert.getAttr("${item[1]}")
                }

                def align = "left"
                def key = "${item[0]}"
                if(key == 'Assigned To') {
                    value = alert.assignedTo ? alert.assignedTo?.fullName : alert.assignedToGroup?.name
                }
                if(key == 'Share With') {
                    value = alert?.shareWithUser + alert?.shareWithGroup
                }
                if (key == 'Product/Generic Name') {
                    if(alert.productSelection){
                        value = alert.getNameFieldFromJson(alert.productSelection)
                    } else {
                        value = alert.getGroupNameFieldFromJson(alert.productGroupSelection)
                    }
                }

                if (key == 'Event') {
                    if(alert.eventSelection){
                        value = alert.getNameFieldFromJson(alert.eventSelection)
                    } else {
                        value = alert.getGroupNameFieldFromJson(alert.eventGroupSelection)
                    }
                }

                if (key == 'Shared with Group') {
                    if (value) {
                        def strVal = value.toString()
                        value = strVal.substring(1, strVal.size() - 1)
                    } else {
                        value = null
                    }
                }

                if (key == 'Device Related') {
                    if (!value)
                        value = "No"
                }

                if (key == 'Action Taken') {
                    value = alert?.actionTaken ? alert.actionTaken.join(", ") : ""
                }

                if(key == 'Reference Type' && alert) {
                    value = alert.refType ? EvaluationReferenceType.get(alert.refType as Long).name : "-"
                }

                if (value instanceof Date) {
                    value = DateUtil.toDateString(value, timezone)
                } else if (value instanceof User) {
                    value = value.getFullName()
                } else if (value instanceof Boolean) {
                    value = value ? 'Yes' : 'No'
                } else if (value instanceof List<String>) {
                    value = value.join(',')
                }
                [item[0], item[1], value, align, item[2]]
            }
        }

        tmp
    }

}
