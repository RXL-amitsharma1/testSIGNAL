package com.rxlogix

import com.rxlogix.attachments.AttachmentLink
import com.rxlogix.audit.AuditTrail
import com.rxlogix.cache.HazelcastService
import com.rxlogix.config.*
import com.rxlogix.dto.*
import com.rxlogix.enums.AdvancedFilterOperatorEnum
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.QueryOperatorEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.mapping.LmProduct
import com.rxlogix.signal.*
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.dto.reports.integration.ExecutedConfigurationSharedWithDTO
import com.rxlogix.util.DbUtil
import com.rxlogix.util.RelativeDateConverter
import com.rxlogix.util.SignalQueryHelper
import com.rxlogix.util.SignalUtil
import grails.converters.JSON
import grails.util.Pair
import org.apache.http.HttpStatus
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.transform.Synchronized
import groovy.util.slurpersupport.GPathResult
import org.apache.commons.lang3.StringUtils
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.criterion.Order
import org.hibernate.jdbc.Work
import org.hibernate.sql.JoinType
import org.hibernate.transform.Transformers
import org.hibernate.type.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.springframework.http.MediaType
import org.springframework.transaction.annotation.Propagation

import org.grails.datastore.mapping.query.*
import java.lang.reflect.Field
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import oracle.jdbc.OracleTypes
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

import java.util.zip.GZIPOutputStream

@Transactional
class AlertService implements Alertbililty {

    def grailsApplication
    def productBasedSecurityService
    def pvsProductDictionaryService
    def userService
    def singleCaseAlertService
    def validatedSignalService
    def dispositionService
    def priorityService
    def aggregateCaseAlertService
    def jaderAlertService
    def evdasAlertService
    def advancedFilterService
    def activityService
    def sessionFactory
    def productEventHistoryService
    def cacheService
    def caseHistoryService
    def evdasHistoryService
    def literatureActivityService
    def messageSource
    def dataSource
    def actionService
    def dataSource_pva
    def sqlGenerationService
    def spotfireService
    def reportIntegrationService
    def notificationHelper
    def masterExecutorService
    def childExecutorService
    AggregateOnDemandAlertService aggregateOnDemandAlertService
    EvdasOnDemandAlertService evdasOnDemandAlertService
    SingleOnDemandAlertService singleOnDemandAlertService
    def signalDataSourceService
    def dataObjectService
    LiteratureAlertService literatureAlertService
    def emergingIssueService

    def restAPIService
    def reportExecutorService
    def configurationService
    def dictionaryGroupService
    def springSecurityService
    def preCheckService
    def alertFieldService

    def signalAuditLogService
    def dataSheetService
    def archiveService
    def alertDeletionDataService
    HazelcastService hazelcastService
    def appAlertProgressStatusService

    Map detailUrlMap = ["Single Case Alert"   : [adhocRun: "sca_adhoc_reportRedirectURL", dataMiningRun: "sca_reportRedirectURL"],
                        "Aggregate Case Alert": [adhocRun: "aga_adhoc_reportRedirectURL", dataMiningRun: "aga_reportRedirectURL"]]
    private
    static List<String> doubleTypeColumn = ["eb05", "eb95", "ebgm", "prrValue", "prrLCI", "prrUCI", "rorValue", "rorLCI", "rorUCI",
                                            "newRc", "totRc", "newLit", "totalLit", "newPaed", "totPaed", "newGeria", "totGeria",
                                            "newEea", "totEea", "newMedErr", "totMedErr", "newHcp", "totHcp", "newSpont", "totSpont",
                                            "newObs", "totObs", "ratioRorPaedVsOthers", "ratioRorGeriatrVsOthers", "totSpontRest",
                                            "totSpontAsia", "totSpontJapan", "totSpontNAmerica", "totSpontEurope", "asiaRor", "restRor",
                                            "japanRor", "europeRor", "northAmericaRor", "chiSquare", "completenessScore", "patientAge", "pecImpNumLow", "pecImpNumHigh",
                                            "freqPeriod", "cumFreqPeriod", "reviewedFreqPeriod", "reviewedCumFreqPeriod", "aValue", "bValue", "cValue", "dValue", "eValue", "rrValue"
    ]

    private static Map<String, String> prrRorColumn =
            ["prrValue"               : "prr_value",
             "prrLCI"                 : "prr05",
             "prrUCI"                 : "prr95",
             "rorValue"               : "ror_value",
             "rorLCI"                 : "ror05",
             "rorUCI"                 : "ror95",
             "newRc"                  : "new_rc",
             "totRc"                  : "tot_rc",
             "newLit"                 : "new_lit",
             "totalLit"               : "total_lit",
             "newPaed"                : "new_paed",
             "totPaed"                : "tot_paed",
             "newGeria"               : "new_geria",
             "totGeria"               : "tot_geria",
             "newEea"                 : "new_eea",
             "totEea"                 : "tot_eea",
             "newMedErr"              : "new_med_err",
             "totMedErr"              : "tot_med_err",
             "newHcp"                 : "new_hcp",
             "totHcp"                 : "tot_hcp",
             "newObs"                 : "new_obs",
             "totObs"                 : "tot_obs",
             "newSpont"               : "new_spont",
             "totSpont"               : "tot_spont",
             "ratioRorPaedVsOthers"   : "RATIO_ROR_PAED_VS_OTHERS",
             "ratioRorGeriatrVsOthers": "RATIO_ROR_GERIATR_VS_OTHERS",
             "totSpontRest"           : "tot_spont_rest",
             "totSpontAsia"           : "tot_spont_asia",
             "totSpontJapan"          : "tot_spont_japan",
             "totSpontNAmerica"       : "TOT_SPONTNAMERICA",
             "totSpontEurope"         : "tot_spont_europe",
             "asiaRor"                : "asia_ror",
             "restRor"                : "rest_ror",
             "japanRor"               : "japan_ror",
             "europeRor"              : "europe_ror",
             "northAmericaRor"        : "north_america_ror",
            ]

    private static Map<String, String> dateColumn = [
            "dueDate"            : "due_date",
            "caseInitReceiptDate": "case_init_receipt_date",
            "lockedDate"         : "locked_date",
            "dispLastChange"     : "disp_last_change",
            "caseCreationDate"   : "case_creation_date",
            "dateOfBirth"        : "date_of_birth",
            "eventOnsetDate"     : "event_onset_date"
    ]

    private static Map<String, String> exportColumns = [
            caseInitReceiptDate   : "caseInitReceiptDate",
            productName           : "productName",
            pt                    : "pt",
            listedness            : "listedness",
            outcome               : "outcome",
            dueDate               : "dueDate",
            serious               : "serious",
            indication            : "indication",
            eventOutcome          : "eventOutcome",
            causeOfDeath          : "causeOfDeath",
            seriousUnlistedRelated: "seriousUnlistedRelated",
            patientAge            : "patientAge",
            conComit              : "conComit",
            age                   : "age",
            caseClassification    : "caseClassification",
            masterPrefTermAll     : "masterPrefTermAll",
            timeToOnset           : "timeToOnset",
            reportersHcpFlag      : "reportersHcpFlag",
            protocolNo            : "protocolNo",
            rechallenge           : "rechallenge",
            patientMedHist        : "patientMedHist",
            patientHistDrugs      : "patientHistDrugs",
            medErrorsPt           : "medErrorsPt",
            therapyDates          : "therapyDates",
            malfunction           : "malfunction",
            doseDetails           : "doseDetails",
            death                 : "death",
            region                : "region",
            country               : "country",
            gender                : "gender",
            suspProd              : "suspProd",
            initialFu             : "initialFu",
            isSusar               : "isSusar",
            caseReportType        : "caseReportType",
            lockedDate            : "lockedDate",
            batchLotNo            : "batchLotNo",
            comboFlag             : "comboFlag",
            caseType              : "caseType",
            completenessScore     : "completenessScore",
            indNumber             : "indNumber",
            appTypeAndNum         : "appTypeAndNum",
            compoundingFlag       : "compoundingFlag",
            submitter             : "submitter",
            preAnda               : "preAnda",
            justification         : "justification",
            dispPerformedBy       : "dispPerformedBy",
            dispLastChange        : "dispLastChange",
            primSuspProd          : "primSuspProd",
            primSuspPai           : "primSuspPai",
            paiAll                : "paiAll",
            allPt                 : "allPt",
            genericName           : "genericName",
            caseCreationDate      : "caseCreationDate",
            dateOfBirth           : "dateOfBirth",
            eventOnsetDate        : "eventOnsetDate",
            pregnancy             : "pregnancy",
            medicallyConfirmed    : "medicallyConfirmed",
            allPTsOutcome         : "allPTsOutcome",
            reporterQualification : "reporterQualification",
            crossReferenceInd     : "crossReferenceInd"
    ]

    def findByExecutedConfiguration(exeConfig) {
        if (exeConfig) {
            Alert.findAll("from Alert as a where a.executedAlertConfiguration.id=?",
                    [exeConfig.id])
        } else {
            null
        }
    }

    def findAdhocAlertById(id) {
        AdHocAlert.get(id)
    }

    def genericsList(String searchTerm) {
        LmProduct.withTransaction {
            def gnList = LmProduct.createCriteria().listDistinct {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property 'genericName', 'id'
                    property 'genericName', 'text'
                }
                ilike('genericName', searchTerm)
            }
            gnList.unique(textComparator())
        }
    }

    private Comparator textComparator() {
        return [
                equals : { delegate.equals(it) },
                compare: { first, second ->
                    first.text <=> second.text
                }
        ] as Comparator
    }

    List<AdHocAlert> filterByProductSecurity(List alertList) {
        def allowedGenericNames = productBasedSecurityService.allowedGenericNamesForUser(userService.getUserFromCacheByUsername(userService.getCurrentUserName())) as Set
        alertsByProductSecurity(alertList, allowedGenericNames, Holders.config.pvsignal.product.based.security)
    }

    def alertsByProductSecurity(alertList, allowedGenericNames, filterOn) {
        def alerts = []
        User user = userService.getUserFromCacheByUsername(userService.getCurrentUserName())
        List<String> allowedProductsIds = []
        if (filterOn) {
            allowedProductsIds = productBasedSecurityService.allAllowedProductIdsForUser(user)
        }
        alertList?.findAll { alert ->
            if (filterOn) {
                if (alert.productNameList) {
                    def prodSelectionType = getProductSelectionType(alert.productSelection)
                    if (prodSelectionType == 'product') {
                        List<String> alertProducts = getProductIdsFromProductSelection(alert.productSelection)
                        if (allowedProductsIds.intersect(alertProducts).size() > 0) {
                            alerts.add(alert)
                        }
                    } else if (prodSelectionType == 'generic') {
                        alert.productNameList && allowedGenericNames ?
                                !allowedGenericNames.disjoint(alert.productNameList as Collection) : false
                    }
                }
            } else {
                alerts.add(alert)
            }
        }
        alerts
    }

    Closure filterClosure = { Map filterMap, def domainName, Boolean isFaers = false, Long configId = null, Long execConfigId = null, Boolean isJader = false ->
        Map integerCountRelatedColumnMap = [
                'newSponCount'       : 'cumSponCount',
                'newSeriousCount'    : 'cumSeriousCount',
                'newFatalCount'      : 'cumFatalCount',
                'newStudyCount'      : 'cumStudyCount',
                'newEv'              : 'totalEv',
                'newSerious'         : 'totalSerious',
                'newFatal'           : 'totalFatal',
                'newCount'           : 'cummCount',
                'newPediatricCount'  : 'cummPediatricCount',
                'newInteractingCount': 'cummInteractingCount',
                'newGeriatricCount'  : 'cumGeriatricCount',
                'newNonSerious'      : 'cumNonSerious',
                'timeToOnset'        : 'timeToOnset',
                'newProdCount'       : 'cumProdCount',
        ]
        Map doubleStackedColumns = [
                "freqPeriod"        : "cumFreqPeriod",
                "reviewedFreqPeriod": "reviewedCumFreqPeriod",
                "prrLCI"            : "prrUCI",
                "rorLCI"            : "rorUCI"
        ]

        Map orILikeKeyMap = [
                'pecImpHigh': 'pecImpLow',
                'newEea'    : 'totEea',
                'newHcp'    : 'totHcp',
                'newMedErr' : 'totMedErr',
                'newAbuse'  : 'totAbuse',
                'newObs'    : 'totObs',
                'newOccup'  : 'totOccup',
                'newCt'     : 'totCt',
                'newRc'     : 'totRc',
                'newLit'    : 'totalLit',
                'newPaed'   : 'totPaed',
                'newGeria'  : 'totGeria',
                'newSpon'   : 'totSpon',
                'newSpont'  : 'totSpont'
        ]


        List evdasJsonColumnMap = ["dmeImeEvdas", "sdrEvdas", "rorValueEvdas", "hlgtEvdas", "hltEvdas", "smqNarrowEvdas", "impEventsEvdas", "totSpontEuropeEvdas",
                                   "newSpontEvdas", "sdrGeratrEvdas", "ratioRorGeriatrVsOthersEvdas", "totSpontNAmericaEvdas", "ratioRorPaedVsOthersEvdas",
                                   "sdrPaedEvdas", "totSpontRestEvdas", "totSpontAsiaEvdas", "totSpontJapanEvdas", "europeRorEvdas",
                                   "northAmericaRorEvdas", "japanRorEvdas", "asiaRorEvdas", "restRorEvdas", "changesEvdas", "listedEvdas"]


        Map faersCountColumnMap = [
                "newCountFaers"           : "cummCountFaers",
                "newSeriousCountFaers"    : "cumSeriousCountFaers",
                'newFatalCountFaers'      : 'cumFatalCountFaers',
                'newStudyCountFaers'      : 'cumStudyCountFaers',
                'eb05Faers'               : 'eb95Faers',
                'eb95Faers'               : 'eb05Faers',
                "rorLCIFaers"             : "rorUCIFaers",
                "newPediatricCountFaers"  : "cummPediatricCountFaers",
                "newInteractingCountFaers": "cummInteractingCountFaers"

        ]

        Map vaersCountColumnMap = [
                "newCountVaers"         : "cummCountVaers",
                "newSeriousCountVaers"  : "cumSeriousCountVaers",
                'newFatalCountVaers'    : 'cumFatalCountVaers',
                'newStudyCountFaers'    : 'cumStudyCountFaers',
                'eb05Vaers'             : 'eb95Vaers',
                'eb95Vaers'             : 'eb95Vaers',
                "rorLCIVaers"           : "rorUCIVaers",
                "prrLCIVaers"           : "prrUCIVaers",
                "newPediatricCountVaers": "cummPediatricCountVaers",
                "newGeriatricCountVaers": "cumGeriatricCountVaers"

        ]

        Map vigibaseCountColumnMap = [
                "newCountVigibase"         : "cummCountVigibase",
                "newSeriousCountVigibase"  : "cumSeriousCountVigibase",
                'newFatalCountVigibase'    : 'cumFatalCountVigibase',
                'newStudyCountVigibase'    : 'cumStudyCountVigibase',
                'eb05Vigibase'             : 'eb95Vigibase',
                'eb95Vigibase'             : 'eb95Vigibase',
                "rorLCIVigibase"           : "rorUCIVigibase",
                "prrLCIVigibase"           : "prrUCIVigibase",
                "newPediatricCountVigibase": "cummPediatricCountVigibase",
                "newGeriatricCountVigibase": "cumGeriatricCountVigibase"

        ]
        Map jaderCountColumnMap = [
                "newCountJader"         : "cumCountJader",
                "newSeriousCountJader"  : "cumSeriousCountJader",
                'newFatalCountJader'    : 'cumFatalCountJader',
                'newStudyCountJader'    : 'cumStudyCountJader',
                'eb05Jader'             : 'eb95Jader',
                'eb95Jader'             : 'eb95Jader',
                "rorLCIJader"           : "rorUCIJader",
                "prrLCIJader"           : "prrUCIJader",
                "newPediatricCountJader": "cumPediatricCountJader",
                "newGeriatricCountJader": "cumGeriatricCountJader"

        ]
        List faersCountColumnList = ["newSponCountFaers", "newStudyCountFaers", "prrValueFaers", "prrLCIFaers", "rorValueFaers",
                                     "ebgmFaers", "chiSquareFaers"]
        List vaersCountColumnList = ["prrValueVaers", "rorValueVaers", "ebgmVaers", "chiSquareVaers"]

        List vigibaseCountColumnList = ["prrValueVigibase", "rorValueVigibase", "ebgmVigibase", "chiSquareVigibase"]
        List jaderCountColumnList = ["prrValueJader", "rorValueJader", "ebgmJader", "chiSquareJader","aValueJader","bValueJader","cvalueJader","dValueJader","eValueJader","rrvalueJader"]
        List faersTextColumnList = ["impEventsFaers", "positiveRechallengeFaers", "positiveDechallengeFaers",
                                    "listedFaers", "relatedFaers", "pregenencyFaers", "trendTypeFaers"]

        Map evdasCountColumnMap = [
                newEvEvdas     : 'totalEvEvdas',
                newSeriousEvdas: 'totalSeriousEvdas',
                newFatalEvdas  : 'totalFatalEvdas'
        ]

        Map evdasLikeColumnMap = [
                newEeaEvdas   : 'totEeaEvdas',
                newHcpEvdas   : 'totHcpEvdas',
                newMedErrEvdas: 'totMedErrEvdas',
                newObsEvdas   : 'totObsEvdas',
                newRcEvdas    : 'totRcEvdas',
                newLitEvdas   : 'totalLitEvdas',
                newPaedEvdas  : 'totPaedEvdas',
                newGeriaEvdas : 'totGeriaEvdas'
        ]

        Map<String, String> dateMap = ["caseInitReceiptDate": "case_init_receipt_date", "lockedDate": "locked_date", "caseCreationDate": "case_creation_date", "dispLastChange": "disp_last_change"]

        Double doubleMinValue = -1.0d
        Integer integerMinValue = Integer.MIN_VALUE
        User currentUser = userService.getUser()
        String timeZone = currentUser?.preference?.timeZone ? currentUser?.preference?.timeZone : "UTC"
        Date dateTime = new Date(DateUtil.toDateString(new Date(), timeZone))
        Date currentDate = dateTime.clearTime()
        List subGroupingFields = cacheService.allOtherSubGroupColumnUIList(Constants.DataSource.PVA)?.values() as List
        List relSubGroupingFields = cacheService.relativeSubGroupColumnUIList(Constants.DataSource.PVA)?.values() as List
        List newFields = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT',true).collect{
            it.name
        }
        List newAdhocColumnList = alertFieldService.getAggOnDemandColumnList(Constants.DataSource.PVA,false,true)
        List newAdhocColumnNameList = newAdhocColumnList.collect{it.name}
        filterMap.each { k, v ->

            if (k == 'assessListedness') {
                k = 'listedness'
            }
            if (k == 'assessSeriousness') {
                k = 'serious'
            }
            if (k == 'disposition') {
                'disposition' {
                    iLikeWithEscape('displayName', "%${EscapedILikeExpression.escapeString(v)}%")
                }
            } else if (k == 'assigned') {
                'assignedTo' {
                    iLikeWithEscape('username', "%${EscapedILikeExpression.escapeString(v)}%")
                }
                'disposition' {
                    eq('closed', false)
                    eq('validatedConfirmed', false)
                }
            } else if (k == 'assignedToUser' || k == 'assignedTo') {
                createAlias("assignedTo", "at", JoinType.LEFT_OUTER_JOIN)
                createAlias("assignedToGroup", "atg", JoinType.LEFT_OUTER_JOIN)
                or {
                    iLikeWithEscape('at.fullName', "%${EscapedILikeExpression.escapeString(v)}%")
                    iLikeWithEscape('atg.name', "%${EscapedILikeExpression.escapeString(v)}%")
                }
            } else if (k == 'assignedToUserAndGroup') {
                List<Long> groupIds = User.get(v as Long)?.groups?.collect { it.id }
                'or' {
                    'eq'("assignedTo.id", v as Long)
                    if (groupIds?.size() > 0) {
                        or {
                            groupIds.collate(1000).each {
                                'in'('assignedToGroup.id', it)
                            }
                        }
                    }
                }
            } else if (k == 'signalsAndTopics') {
                createAlias("validatedSignals", "vs", JoinType.LEFT_OUTER_JOIN)
                iLikeWithEscape('vs.name', "%${EscapedILikeExpression.escapeString(v)}%")
            } else if (k == 'newDashboardFilter') {
                eq("isNew", v)
            }else if(k in newAdhocColumnNameList && domainName == AggregateOnDemandAlert){
                Map columnInfoMap = newAdhocColumnList.find{it.name == k}
                if(columnInfoMap.secondaryName) {
                    or {
                        sqlRestriction("to_number(json_value(NEW_COUNTS_JSON, '\$.$k')) = ${v}")
                        sqlRestriction("to_number(json_value(NEW_COUNTS_JSON, '\$.$columnInfoMap.secondaryName')) = ${v}")
                    }
                }else{
                    or {
                        sqlRestriction("UPPER(json_value(NEW_COUNTS_JSON, '\$.$k')) LIKE '%${EscapedILikeExpression.escapeString(v.toUpperCase())}%'")
                    }
                }
            } else if (integerCountRelatedColumnMap.containsKey(k)) {
                or {
                    eq(k, v.isInteger() ? v.toInteger() : integerMinValue)
                    eq(integerCountRelatedColumnMap.get(k), v.isInteger() ? v.toInteger() : integerMinValue)
                }
            } else if (orILikeKeyMap.containsKey(k)) {
                or {
                    iLikeWithEscape(k, "${EscapedILikeExpression.escapeString(v)}")
                    iLikeWithEscape(orILikeKeyMap.get(k), "${EscapedILikeExpression.escapeString(v)}")
                }
            } else if (k == 'eb05') {
                or {
                    eq(k, v.isDouble() ? v.toDouble() : doubleMinValue)
                    eq('eb95', v.isDouble() ? v.toDouble() : doubleMinValue)
                }

            } else if (doubleStackedColumns.containsKey(k)) {
                or {
                    eq(k, v?.isDouble() ? v?.toDouble() : doubleMinValue)
                    eq(doubleStackedColumns.get(k), v?.isDouble() ? v?.toDouble() : doubleMinValue)
                }

            } else if (k == 'prrLCI') {
                or {
                    eq(k, v?.isDouble() ? v?.toDouble() : doubleMinValue)
                    eq('prrLCI', v?.isDouble() ? v?.toDouble() : doubleMinValue)
                }

            } else if (k == 'rorLCI') {
                or {
                    eq(k, v?.isDouble() ? v?.toDouble() : doubleMinValue)
                    eq('rorLCI', v?.isDouble() ? v?.toDouble() : doubleMinValue)
                }

            } else if (k == 'rorValue' && domainName in [AggregateCaseAlert, AggregateOnDemandAlert]) {
                or {
                    eq(k, v.isDouble() ? v.toDouble() : doubleMinValue)
                    eq('rorValue', v.isDouble() ? v.toDouble() : doubleMinValue)
                }
            } else if (k == 'rorValue' && domainName in [EvdasAlert, EvdasOnDemandAlert]) {
                or {
                    eq(k, v)
                    eq('rorValue', v)
                }
            } else if (k == 'prrValue') {
                or {
                    eq(k, v.isDouble() ? v.toDouble() : doubleMinValue)
                    eq('prrValue', v.isDouble() ? v.toDouble() : doubleMinValue)
                }

            } else if (k == 'completenessScore') {
                or {
                    eq(k, v.isDouble() ? v.toDouble() : doubleMinValue)
                    eq('completenessScore', v.isDouble() ? v.toDouble() : doubleMinValue)
                }
            } else if (k == 'patientAge') {
                or {
                    eq(k, v.isDouble() ? v.toDouble() : doubleMinValue)
                    eq('patientAge', v.isDouble() ? v.toDouble() : doubleMinValue)
                    sqlRestriction("round(PATIENT_AGE,2) = ${v}")
                }
            } else if (k == 'chiSquare') {
                or {
                    eq(k, v.isDouble() ? v.toDouble() : doubleMinValue)
                    eq('chiSquare', v.isDouble() ? v.toDouble() : doubleMinValue)
                }
            } else if (k == 'aValue') {
                or {
                    eq(k, v.isDouble() ? v.toDouble() : doubleMinValue)
                    eq('aValue', v.isDouble() ? v.toDouble() : doubleMinValue)
                }
            } else if (k == 'bValue') {
                or {
                    eq(k, v.isDouble() ? v.toDouble() : doubleMinValue)
                    eq('bValue', v.isDouble() ? v.toDouble() : doubleMinValue)
                }
            } else if (k == 'cValue') {
                or {
                    eq(k, v.isDouble() ? v.toDouble() : doubleMinValue)
                    eq('cValue', v.isDouble() ? v.toDouble() : doubleMinValue)
                }
            } else if (k == 'dValue') {
                or {
                    eq(k, v.isDouble() ? v.toDouble() : doubleMinValue)
                    eq('dValue', v.isDouble() ? v.toDouble() : doubleMinValue)
                }
            } else if (k == 'eValue') {
                or {
                    eq(k, v.isDouble() ? v.toDouble() : doubleMinValue)
                    eq('eValue', v.isDouble() ? v.toDouble() : doubleMinValue)
                }
            } else if (k == 'rrValue') {
                or {
                    eq(k, v.isDouble() ? v.toDouble() : doubleMinValue)
                    eq('rrValue', v.isDouble() ? v.toDouble() : doubleMinValue)
                }
            }else if (k == 'pecImpNumLow') {
                or {
                    eq(k, v.isDouble() ? v.toDouble() : doubleMinValue)
                    eq('pecImpNumLow', v.isDouble() ? v.toDouble() : doubleMinValue)
                }
            } else if (k == 'dueDate') {
                between('dueDate', currentDate + (v.isInteger() ? v.toInteger() : 0), currentDate + (v.isInteger() ? v.toInteger() + 1 : 0))
            } else if (k in dateMap) {
                if (k == "dispLastChange") {
                    // Date is displayed in User time-zone
                    sqlRestriction("UPPER(to_char(${dateMap.get(k)} AT TIME ZONE '${timeZone}','DD-Mon-YYYY HH:MI:SS PM')) LIKE UPPER('%${EscapedILikeExpression.escapeString(v)}%')")
                } else if( k == "caseInitReceiptDate" && isJader){
                    sqlRestriction("UPPER(to_char(${dateMap.get(k)},'YYYY/MM/DD')) LIKE UPPER('%${EscapedILikeExpression.escapeString(v)}%')")
                } else {
                    sqlRestriction("UPPER(to_char(${dateMap.get(k)},'DD-MON-YYYY')) LIKE UPPER('%${EscapedILikeExpression.escapeString(v)}%')")
                }
            } else if (k == 'listed' && domainName in [EvdasAlert, EvdasOnDemandAlert]) {
                String value = '(?i)(.*)' + v + '(.*)'
                if ('yes'.matches(value)) {
                    eq('listedness', true)
                } else if ('no'.matches(value)) {
                    eq('listedness', false)
                } else if ('N/A'.matches(value)) {
                    isNull('listedness')
                }
            } else if (k.equals('productName') && domainName == AggregateCaseAlert) {
                or {
                    eq('productName', v)
                }
            } else if (k.equals('productName') && domainName == AggregateOnDemandAlert) {
                or {
                    eq('productName', v, [ignoreCase: true])
                }
            } else if (k.equals('listedness') && (domainName == SingleCaseAlert || domainName == SingleOnDemandAlert)) {
                or {
                    iLikeWithEscape(k, "${EscapedILikeExpression.escapeString(v)}%")
                }
            } else if (k.equals('serious') && (domainName == SingleCaseAlert || domainName == SingleOnDemandAlert)) {
                or {
                    iLikeWithEscape(k, "${EscapedILikeExpression.escapeString(v)}%")
                }

            } else if (faersCountColumnMap.containsKey(k)) {
                or {
                    sqlRestriction("json_value(faers_columns, '\$.$k') = ${v}")
                }
            } else if (k in newFields && domainName == AggregateCaseAlert) {
                if(k in ["hlt","hlgt","smqNarrow"]){
                    or {
                        sqlRestriction("UPPER(json_value(NEW_COUNTS_JSON, '\$.$k')) Like '%${EscapedILikeExpression.escapeString(v?.toString()?.toUpperCase())}%'")
                    }
                }else{
                    String newCount = k + ".new"
                    String cumCount = ""
                    if (k in ['newPediatricCount', 'newPediatricCountVaers', 'newPediatricCountVigibase', 'newPediatricCountFaers', 'newCount', 'newCountFaers', 'newCountVaers'
                              , 'newCountVigibase', 'newInteractingCount', 'newInteractingCountFaers', 'newInteractingCountVaers', 'newInteractingCountVigibase']) {
                        k = k.toString().replace("new", "cumm")
                        cumCount = k + ".cum"
                    } else {
                        cumCount = k + ".cum"
                    }

                    or {
                        sqlRestriction("to_number(json_value(NEW_COUNTS_JSON, '\$.$newCount')) = ${v}")
                        sqlRestriction("to_number(json_value(NEW_COUNTS_JSON, '\$.$cumCount')) = ${v}")
                    }
                }
            } else if (faersCountColumnList.contains(k)) {
                sqlRestriction("json_value(faers_columns, '\$.$k') = ${v}")
            } else if (isFaersStratificationColumn(k.toString())) {
                String columnName = k.substring(0, 4)
                String fieldName = k.substring(4, k.length() - 5)
                columnName = columnName?.equalsIgnoreCase("EBGM") ? "EBGM_" : columnName
                sqlRestriction("coalesce(to_number(regexp_substr(regexp_substr(concat(${columnName}GENDER_FAERS,${columnName}AGE_FAERS), '${fieldName} *: *\\d+(\\.\\d+)?'),'\\d+(\\.\\d+)?')),0) = $v")
            } else if(k in subGroupingFields ) {
                String columnName
                String fieldName
                if (k.toString()?.toUpperCase()?.startsWith("EBGM")) {
                    columnName = "EBGM_SUB_GROUP"
                    fieldName = k.toString() - "ebgm"
                } else if (k.toString()?.toUpperCase()?.startsWith("EB05")) {
                    columnName = "EB05_SUB_GROUP"
                    fieldName = k.toString() - "eb05"
                } else if (k.toString()?.toUpperCase()?.startsWith("EB95")) {
                    columnName = "EB95_SUB_GROUP"
                    fieldName = k - "eb95"
                } else if (k.toString()?.toUpperCase()?.startsWith("RORLCI")) {
                    columnName = "ROR_LCI_SUB_GROUP"
                    fieldName = k.toString() - "rorLci"
                } else if (k.toString()?.toUpperCase()?.startsWith("RORUCI")) {
                    columnName = "ROR_UCI_SUB_GROUP"
                    fieldName = k.toString() - "rorUci"
                }  else if (k.toString()?.toUpperCase()?.startsWith("ROR")) {
                    columnName = "ROR_SUB_GROUP"
                    fieldName = k.toString() - "ror"
                } else if (k.toString()?.toUpperCase()?.startsWith("PRRLCI")) {
                    columnName = "PRR_LCI_SUB_GROUP"
                    fieldName = k.toString() - "prrLci"
                } else if (k.toString()?.toUpperCase()?.startsWith("PRRUCI")) {
                    columnName = "PRR_UCI_SUB_GROUP"
                    fieldName = k - "prrUci"
                } else if (k.toString()?.toUpperCase()?.startsWith("PRR")) {
                    columnName = "PRR_SUB_GROUP"
                    fieldName = k.toString() - "prr"
                } else if(k.toString()?.toUpperCase()?.startsWith("CHISQUARE")){
                    columnName = "CHI_SQUARE_SUB_GROUP"
                    fieldName = k.toString() - "chiSquare"
                }
                fieldName = escapeSpecialCharacters(fieldName)
                sqlRestriction("coalesce(to_number(regexp_substr(regexp_substr(${columnName}, '\"${fieldName}\" *: *\\d+(\\.\\d+)?'),'\\d+(\\.\\d+)?')),0) = $v")
            } else if(k in relSubGroupingFields){
                String columnName
                String fieldName
                if(k.toString()?.toUpperCase()?.startsWith("RORLCI")){
                    columnName = "ROR_LCI_REL_SUB_GROUP"
                    fieldName = k.toString() - "rorLciRel"
                }else if(k.toString()?.toUpperCase()?.startsWith("RORUCI")){
                    columnName = "ROR_UCI_REL_SUB_GROUP"
                    fieldName = k.toString() - "rorUciRel"
                }else if(k.toString()?.toUpperCase()?.startsWith("ROR")){
                    columnName = "ROR_REL_SUB_GROUP"
                    fieldName = k.toString() - "rorRel"
                }
                fieldName = escapeSpecialCharacters(fieldName)
                sqlRestriction("coalesce(to_number(regexp_substr(regexp_substr(${columnName}, '\"${fieldName}\" *: *\\d+(\\.\\d+)?'),'\\d+(\\.\\d+)?')),0) = $v")
            } else if ((!(k?.toString()?.toUpperCase().contains("VIGIBASE")  || k?.toString()?.toUpperCase().contains("JADER")|| k?.toString()?.toUpperCase().contains("VAERS") || k?.toString()?.toUpperCase().contains("FAERS"))) && (k?.toString()?.toUpperCase()?.startsWith("EBGM") || k?.toString()?.toUpperCase()?.startsWith("EB05") || k.toString()?.toUpperCase()?.startsWith("EB95")) &&(!(k?.toString()?.equalsIgnoreCase("EBGM")))) {
                String columnName = k?.toString()?.substring(0, 4)
                String fieldName = k?.toString()?.substring(4)
                columnName = columnName?.equalsIgnoreCase("EBGM") ? "EBGM_" : columnName
                sqlRestriction("coalesce(to_number(regexp_substr(regexp_substr(concat(${columnName}GENDER,${columnName}AGE), '${fieldName} *: *\\d+(\\.\\d+)?'),'\\d+(\\.\\d+)?')),0) = $v")
            } else if (evdasCountColumnMap.containsKey(k)) {
                or {
                    sqlRestriction("json_value(evdas_columns, '\$.$k') = ${v}")
                    sqlRestriction("json_value(evdas_columns, '\$.${evdasCountColumnMap.get(k)}') = ${v}")
                }
            } else if (evdasJsonColumnMap.contains(k)) {
                sqlRestriction("UPPER(json_value(evdas_columns, '\$.$k')) LIKE '%${EscapedILikeExpression.escapeString(v.toUpperCase())}%'")
            } else if (evdasLikeColumnMap.containsKey(k)) {
                or {
                    sqlRestriction("UPPER(json_value(evdas_columns, '\$.$k')) LIKE '%${EscapedILikeExpression.escapeString(v.toUpperCase())}%'")
                    sqlRestriction("UPPER(json_value(evdas_columns, '\$.${evdasLikeColumnMap.get(k)}')) LIKE '%${EscapedILikeExpression.escapeString(v.toUpperCase())}%'")
                }
            } else if (k in doubleTypeColumn) {
                if (k == "pecImpNumHigh" || k =="ebgm") {
                    v = v as Double
                }
                or {
                    eq(k, v)
                }
            } else if (vaersCountColumnMap.containsKey(k)) {
                or {
                    sqlRestriction("json_value(vaers_columns, '\$.$k') = ${v}")
                    sqlRestriction("json_value(vaers_columns, '\$.${vaersCountColumnMap.get(k)}') = ${v}")
                }
            } else if (vigibaseCountColumnMap.containsKey(k)) {
                or {
                    sqlRestriction("json_value(vigibase_columns, '\$.$k') = ${v}")
                    sqlRestriction("json_value(vigibase_columns, '\$.${vigibaseCountColumnMap.get(k)}') = ${v}")
                }
            } else if (jaderCountColumnMap.containsKey(k)) {
                or {
                    sqlRestriction("json_value(jader_columns, '\$.$k') = ${v}")
                    sqlRestriction("json_value(jader_columns, '\$.${jaderCountColumnMap.get(k)}') = ${v}")
                }
            } else if (faersCountColumnList.contains(k)) {
                sqlRestriction("json_value(faers_columns, '\$.$k') = ${v}")
            } else if (vaersCountColumnList.contains(k)) {
                sqlRestriction("json_value(vaers_columns, '\$.$k') = ${v}")
            } else if (vigibaseCountColumnList.contains(k)) {
                sqlRestriction("json_value(vigibase_columns, '\$.$k') = ${v}")
            }else if (jaderCountColumnList.contains(k)) {
                sqlRestriction("json_value(jader_columns, '\$.$k') = ${v}")
            }  else if (faersTextColumnList.contains(k)) {
                sqlRestriction("UPPER(json_value(faers_columns, '\$.$k')) LIKE '%${EscapedILikeExpression.escapeString(v.toUpperCase())}%'")
            } else if (k == "impEvents") {
                String searchString = ''
                String esc_char = ""
                if (v) {
                    searchString = v.toLowerCase()
                    if (searchString.contains('_')) {
                        searchString = searchString.replaceAll("\\_", "!_%")
                        esc_char = "!"
                    } else if (searchString.contains('%')) {
                        searchString = searchString.replaceAll("\\%", "!%%")
                        esc_char = "!"
                    }
                }
                if (esc_char) {
                    or {
                        sqlRestriction("""lower(REPLACE(IMP_EVENTS, 'ime', '${
                            Holders.config.importantEvents.ime.abbreviation.toLowerCase()
                        }')) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'""")
                        sqlRestriction("""lower(REPLACE(IMP_EVENTS, 'dme', '${
                            Holders.config.importantEvents.dme.abbreviation.toLowerCase()
                        }')) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'""")
                        sqlRestriction("""lower(REPLACE(IMP_EVENTS, 'sm',  '${
                            Holders.config.importantEvents.specialMonitoring.abbreviation.toLowerCase()
                        }')) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'""")
                        sqlRestriction("""lower(REPLACE(IMP_EVENTS, 'ei',  '${
                            Holders.config.importantEvents.stopList.abbreviation.toLowerCase()
                        }')) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'""")
                    }
                } else {
                    or {
                        sqlRestriction("""lower(REPLACE(IMP_EVENTS, 'ime', '${
                            Holders.config.importantEvents.ime.abbreviation.toLowerCase()
                        }')) like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction("""lower(REPLACE(IMP_EVENTS, 'dme', '${
                            Holders.config.importantEvents.dme.abbreviation.toLowerCase()
                        }')) like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction("""lower(REPLACE(IMP_EVENTS, 'sm',  '${
                            Holders.config.importantEvents.specialMonitoring.abbreviation.toLowerCase()
                        }')) like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction("""lower(REPLACE(IMP_EVENTS, 'ei',  '${
                            Holders.config.importantEvents.stopList.abbreviation.toLowerCase()
                        }')) like '%${searchString.replaceAll("'", "''")}%'""")
                    }
                }

            } else if (k == Constants.AdvancedFilter.COMMENTS && domainName == SingleCaseAlert) {
                List commentList = alertCaseCommentsFilter(v as String, domainName, isFaers, configId)
                String inStr = getInStringForFilters(commentList, domainName)
                sqlRestriction("""({alias}.case_id,{alias}.case_version) in ${inStr}""")
            } else if (k == Constants.AdvancedFilter.COMMENT && domainName == AggregateCaseAlert) {
                List commentList = alertCaseCommentsFilter(v as String, domainName, isFaers, configId)
                String inStr = getInStringForFilters(commentList, domainName)
                sqlRestriction("""({alias}.product_id,{alias}.pt,{alias}.alert_configuration_id) in ${inStr}""")
            } else if (k == Constants.AdvancedFilter.COMMENT && domainName == EvdasAlert) {
                List commentList = alertCaseCommentsFilter(v as String, domainName, isFaers, configId)
                String inStr = getInStringForFilters(commentList, domainName)
                sqlRestriction("""({alias}.substance,{alias}.pt,{alias}.alert_configuration_id) in ${inStr}""")
            } else if (k == "rationale") {
                String rationale="rationale"
                String pecImpNumHigh="pec_imp_num_high"
                sqlRestriction(" lower(CONCAT(CONCAT(${rationale},' '),CONCAT(${pecImpNumHigh},'.0'))) like lower('%${v}%')")
            } else if (k == "soc") {
                def ec = (domainName == EvdasOnDemandAlert || domainName == EvdasAlert || domainName == ArchivedEvdasAlert)? ExecutedEvdasConfiguration.get(execConfigId):  ExecutedConfiguration.get(execConfigId)
                List filtersocs = []
                if(ec) {
                    List socfull = domainName.findAllByExecutedAlertConfiguration(ec)*.soc.unique()
                    socfull.each {
                        String fullsoc = dataObjectService.getAbbreviationMap(it)
                        if (fullsoc && fullsoc?.toLowerCase().contains(v.toLowerCase()))
                            filtersocs << it
                    }
                }

                or {
                    iLikeWithEscape(k, "%${EscapedILikeExpression.escapeString(v)}%")
                    if(filtersocs) {
                        sqlRestriction("soc in ('" + filtersocs.join("','") + "')")
                    }
                }
            } else if (k == 'alertTags' && (domainName == SingleCaseAlert || domainName == AggregateCaseAlert)) {
                Map aliasPropertiesMap = grailsApplication.config.advancedFilter.sqlRestrictionAliasTagMap
                String tagFieldName = "tags"
                //aggTags
                if(domainName == AggregateCaseAlert) {
                    tagFieldName = "aggTags"
                }
                String sqlRestrictionTagSql = generateSqlRestrictionTagSql(tagFieldName, "CONTAINS", v, "tag_text", aliasPropertiesMap)
                String sqlRestrictionSubTagSql = generateSqlRestrictionTagSql(tagFieldName, "CONTAINS", v, "sub_tag_text", aliasPropertiesMap)
                or {
                    sqlRestriction("${sqlRestrictionTagSql}")
                    sqlRestriction("${sqlRestrictionSubTagSql}")
                }
            } else if (k == 'alertTags' && (domainName == SingleOnDemandAlert || domainName == AggregateOnDemandAlert)) {
                Map aliasPropertiesMap = grailsApplication.config.advancedFilter.onDemand.sqlRestrictionAliasTagMap
                String tagFieldName = "tags"
                //aggTags
                if(domainName == AggregateOnDemandAlert) {
                    tagFieldName = "aggTags"
                }
                String sqlRestrictionTagSql = generateSqlRestrictionTagSql(tagFieldName, "CONTAINS", v, "tag_text", aliasPropertiesMap)
                String sqlRestrictionSubTagSql = generateSqlRestrictionTagSql(tagFieldName, "CONTAINS", v, "sub_tag_text", aliasPropertiesMap)
                or {
                    sqlRestriction("${sqlRestrictionTagSql}")
                    sqlRestriction("${sqlRestrictionSubTagSql}")
                }
            }  else if (k == 'alertTags' && (domainName == ArchivedSingleCaseAlert || domainName == ArchivedAggregateCaseAlert)) {
                Map aliasPropertiesMap = grailsApplication.config.advancedFilter.archived.sqlRestrictionAliasTagMap
                String tagFieldName = "tags"
                if(domainName == ArchivedAggregateCaseAlert) {
                    tagFieldName = "aggTags"
                }
                String sqlRestrictionTagSql = generateSqlRestrictionTagSql(tagFieldName, "CONTAINS", v, "tag_text", aliasPropertiesMap)
                String sqlRestrictionSubTagSql = generateSqlRestrictionTagSql(tagFieldName, "CONTAINS", v, "sub_tag_text", aliasPropertiesMap)

                or {
                    sqlRestriction("${sqlRestrictionTagSql}")
                    sqlRestriction("${sqlRestrictionSubTagSql}")
                }
            } else {
                if(Holders.config.custom.qualitative.fields.enabled) {
                    iLikeWithEscape(k, "${EscapedILikeExpression.escapeString(v)}%")
                }
                else{
                    iLikeWithEscape(k, "%${EscapedILikeExpression.escapeString(v)}%")
                }
            }
        }

    }

    List maxSeqCommentListAggregate(Long configId) {
        List commentList = AlertComment.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("productId", "productId")
                property("eventName", "eventName")
                property("configId", "configId")
                property("comments", "comments")

            }
            eq('alertType', Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
            isNotNull("productId")
            isNotNull("eventName")
            isNotNull("configId")
            if (configId) {
                eq("configId", configId)
            }
        } as List

        commentList
    }

    List maxSeqCommentListEvdas(Long configId) {
        List commentList = AlertComment.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("productName", "productName")
                property("eventName", "eventName")
                property("configId", "configId")
                property("comments", "comments")

            }
            eq('alertType', Constants.AlertConfigType.EVDAS_ALERT)
            isNotNull("productName")
            isNotNull("eventName")
            isNotNull("configId")
            if (configId) {
                eq("configId", configId)
            }
        } as List

        commentList
    }

    List maxSeqCommentList(Boolean isFaers) {
        def domain = isFaers ? GlobalCaseCommentMapping."faers" : GlobalCaseCommentMapping."pva"
        List commentList = domain.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("caseId", "caseId")
                property("versionNum", "versionNum")
                property("commentSeqNum", "commentSeqNum")
                property("comments", "comments")

            }
            order('commentSeqNum', 'desc')
        }

        List finalCommentList = []
        commentList.groupBy { it.caseId }.each { key, value ->
            value.groupBy { it.versionNum }.each { key2, value2 ->
                finalCommentList.addAll(value2.findAll { it.commentSeqNum == value2.commentSeqNum.max() })
            }
        }
        finalCommentList
    }

    List alertCaseCommentsFilter(String v, def domainName, Boolean isFaers = false, Long configId = null) {
        List finalCommentList = []
        switch (domainName) {
            case SingleCaseAlert:
                finalCommentList = maxSeqCommentList(isFaers)
                break
            case AggregateCaseAlert:
                finalCommentList = maxSeqCommentListAggregate(configId)
                break
            case EvdasAlert:
                finalCommentList = maxSeqCommentListEvdas(configId)
                break
        }
        finalCommentList = finalCommentList.findAll { it?.comments?.toUpperCase()?.contains(v?.toUpperCase()) }
        finalCommentList
    }

    String getInStringForFilters(List commentList, def domainName) {
        String inStr = "("
        Integer length = commentList.size()
        switch (domainName) {
            case SingleCaseAlert:
                if (!length) {
                    inStr += "(-1,-1)"
                }
                commentList.eachWithIndex { val, index ->
                    if (index != length - 1) {
                        inStr += """(${val?.caseId},${val?.versionNum}),"""
                    } else {
                        inStr += """(${val?.caseId},${val?.versionNum})"""
                    }
                }
                break
            case EvdasAlert:
            case AggregateCaseAlert:
                if (!length) {
                    inStr += "(-1,'',-1)"
                }
                commentList.eachWithIndex { val, index ->
                    val?.eventName = val?.eventName.replaceAll("'", "''")
                    if (index != length - 1) {
                        inStr += """(${
                            domainName == AggregateCaseAlert ? val?.productId : "'" + val?.productName + "'"
                        },'${val?.eventName}',${val?.configId}),"""
                    } else {
                        inStr += """(${
                            domainName == AggregateCaseAlert ? val?.productId : "'" + val?.productName + "'"
                        },'${val?.eventName}',${val?.configId})"""
                    }
                }
                break
        }
        inStr += ")"
        return inStr
    }

    List alertCommentsAdvanceFilter(List valuesList, AdvancedFilterOperatorEnum operatorEnum, String valueStr, def domainName,
                                    Boolean isScaFaers = false, Long configId) {

        List finalCommentList = []
        switch (domainName) {
            case SingleCaseAlert:
                finalCommentList = maxSeqCommentList(isScaFaers)
                break
            case AggregateCaseAlert:
                finalCommentList = maxSeqCommentListAggregate(configId)
                break
            case EvdasAlert:
                finalCommentList = maxSeqCommentListEvdas(configId)
        }
        switch (operatorEnum) {
            case AdvancedFilterOperatorEnum.NOT_EQUAL:
                finalCommentList = finalCommentList.findAll { !(valuesList.contains(it?.comments?.trim())) }
                break
            case AdvancedFilterOperatorEnum.EQUALS:
                finalCommentList = finalCommentList.findAll {
                    valuesList.every { comm -> it?.comments?.trim() == comm }
                }
                break
            case AdvancedFilterOperatorEnum.DOES_NOT_CONTAIN:
                finalCommentList = finalCommentList.findAll {
                    it?.comments ? !(it?.comments?.toUpperCase()?.contains(valueStr?.toUpperCase())) : false
                }
                break
            case AdvancedFilterOperatorEnum.DOES_NOT_START:
                finalCommentList = finalCommentList.findAll {
                    it?.comments ? it?.comments?.toUpperCase()?.indexOf(valueStr?.toUpperCase()) != 0 : false
                }
                break
            case AdvancedFilterOperatorEnum.DOES_NOT_END:
                Integer lenOfValue = valueStr.length() ?: 0
                finalCommentList = finalCommentList.findAll {
                    it?.comments ? it?.comments?.toUpperCase()?.lastIndexOf(valueStr?.toUpperCase()) != (it?.comments?.length() - lenOfValue) : false
                }
                break
            case AdvancedFilterOperatorEnum.CONTAINS:
                finalCommentList = finalCommentList.findAll {
                    it?.comments ? it?.comments?.toUpperCase()?.contains(valueStr?.toUpperCase()) : false
                }
                break
            case AdvancedFilterOperatorEnum.START_WITH:
                finalCommentList = finalCommentList.findAll {
                    it?.comments ? it?.comments?.toUpperCase()?.indexOf(valueStr?.toUpperCase()) == 0 : false
                }
                break
            case AdvancedFilterOperatorEnum.ENDS_WITH:
                Integer lenOfValue = valueStr.length() ?: 0
                finalCommentList = finalCommentList.findAll {
                    it?.comments ? it?.comments?.toUpperCase()?.lastIndexOf(valueStr?.toUpperCase()) == (it?.comments?.length() - lenOfValue) : false
                }
                break
            case AdvancedFilterOperatorEnum.IS_EMPTY:
                finalCommentList = finalCommentList.findAll { it?.comments }
                break
            case AdvancedFilterOperatorEnum.IS_NOT_EMPTY:
                finalCommentList = finalCommentList.findAll { it?.comments }
                break
        }
        finalCommentList
    }

    boolean isFaersStratificationColumn(String key) {
        (key.toUpperCase().startsWith("EBGM") || key.toUpperCase().startsWith("EB05") || key.toUpperCase().startsWith("EB95")) && key.toUpperCase().endsWith("FAERS")
    }

    Closure callingScreenClosure = { AlertDataDTO alertDataDTO ->
        if (alertDataDTO.params.callingScreen == Constants.Commons.DASHBOARD) {
            //Calling screen is dashboard
            'or' {
                eq("assignedTo.id", userService.getCurrentUserId())
                if (alertDataDTO.groupIdList.size() > 0) {
                    or {
                        alertDataDTO.groupIdList.collate(1000).each {
                            'in'('assignedToGroup.id', it)
                        }
                    }
                }
            }
            if (alertDataDTO.domainName in [SingleCaseAlert, ArchivedSingleCaseAlert]) {
                eq('isCaseSeries', false)
            }
            'executedAlertConfiguration' {
                eq('adhocRun', false)
                eq('isDeleted', false)
                eq('isLatest', true)
                eq('isEnabled', true)
                eq('workflowGroup.id', alertDataDTO.workflowGroupId)
                if(alertDataDTO.domainName == AggregateCaseAlert) {
                    ne('selectedDatasource', 'jader')
                }
            }
            'disposition' {
                eq('reviewCompleted', false)
            }
        } else if (alertDataDTO.cumulative) {
            or {
                alertDataDTO.execConfigIdList.collate(1000).each{
                    'in'("executedAlertConfiguration.id", it)
                }
            }
        } else if (alertDataDTO.params.callingScreen != Constants.Commons.TRIGGERED_ALERTS) {
            eq("executedAlertConfiguration.id", alertDataDTO.execConfigId)
        }
    }


    Closure orderClosure = { Map orderColumnMap ->
        String orderColumnJson = orderColumnMap.name ? orderColumnMap.name.endsWith("Vaers") ? 'VAERS_COLUMNS' : orderColumnMap.name.endsWith("Vigibase") ? 'VIGIBASE_COLUMNS' : orderColumnMap.name.endsWith("Jader") ? 'JADER_COLUMNS': orderColumnMap.name.endsWith("Faers") ? 'FAERS_COLUMNS' : 'EVDAS_COLUMNS' : ''
        List<String> listColName = ["caseNumber", "serious", "gender", "death", "productName", "name", "pt", "soc", "caseType", "completenessScore", "indNumber", "appTypeAndNum",
                                    "compoundingFlag", "submitter", "patientAge", "freqPriority", "trendType", "newSponCount", "newGeriatricCount", "newNonSerious",
                                    "newSeriousCount", "newFatalCount", "newStudyCount", "prrValue", "prrLCI", "rorValue", "rorLCI", "ebgm", "eb05", "pecImpHigh",
                                    "pecImpLow", "positiveRechallenge", "positiveDechallenge", "listed", "related", "impEvents", "pregenency", "dueDate", "outcome", "flags",
                                    "listedness", "substance", "newFatal", "newSerious", "newEv", "newLit", "sdr", "smqNarrow", "hlgt", "hlt", "newEea", "newHcp", "newMedErr", "newAbuse", "newOccup",
                                    "newObs", "newCt", "newRc", "newPaed", "newGeria", "newSpont", "roa1", "newRoa1", "totRoa1", "europeRor", "northAmericaRor", "japanRor", "asiaRor",
                                    "restRor", "allRor", "dmeIme", "newCount", "chiSquare", "badge", "comboFlag", "malfunction", "newPediatricCount", "newInteractingCount", "newGeriatricCount",
                                    "newNonSerious", "indication", "eventOutcome", "seriousUnlistedRelated",
                                    "timeToOnset", "initialFu", "protocolNo", "isSusar", "preAnda", "justification", "dispPerformedBy", "dispLastChange", "trendFlag", "newProdCount",
                                    "freqPeriod", "reviewedFreqPeriod", "pecImpNumHigh", "rationale", "pregnancy", "medicallyConfirmed",
                                    "dateOfBirth", "eventOnsetDate", "caseCreationDate", "lockedDate", "caseInitReceiptDate",
                                    "aValue", "bValue", "cValue", "dValue", "eValue", "rrValue","age","reporterQualification","riskCategory"]

        List<String> prevListColName = ["newEv", "newFatal", "newLit", "newSerious", "allRor"]
        boolean isPrevEVDAS = orderColumnMap.name ? orderColumnMap.name.length() > 4 : false
        orderColumnMap.name = (delegate.targetClass in [EvdasOnDemandAlert, EvdasAlert] && isPrevEVDAS && orderColumnMap.name.substring(4) in prevListColName) ? orderColumnMap.name.substring(4) : orderColumnMap.name

        Map aliasOrderMap = [
                'disposition': 'displayName',
                'assignedTo' : 'username',
        ]
        Map orderMap = [
                'priority': 'dueDate',
                'actions' : 'actionCount'
        ]
        Map<String, String> clobFields = ["causeOfDeath"      : "CAUSE_OF_DEATH", "patientMedHist": "PATIENT_MED_HIST",
                                          "patientHistDrugs"  : "PATIENT_HIST_DRUGS", "batchLotNo": "BATCH_LOT_NO",
                                          "caseClassification": "CASE_CLASSIFICATION", "conComit": "CON_COMIT", "genericName": "GENERIC_NAME", "allPTsOutcome": "ALLPTS_OUTCOME", "therapyDates": "therapy_dates", "allPt": "ALL_PT", "suspProd": "susp_prod", "masterPrefTermAll": "master_pref_term_all",
                                          "reportersHcpFlag"  : "reporters_hcp_flag", "doseDetails": "dose_details", "caseReportType": "case_report_type", "primSuspProd": "prim_susp_prod", "country": "country", "rechallenge": "rechallenge","crossReferenceInd":"CROSS_REFERENCE_IND"]

        List<String> jsonIntegerColumnsList = ["newCountFaers", "newEvEvdas", "newSeriousCountFaers", "eb05Faers", "newSponCountFaers",
                                               "newStudyCountFaers", "prrValueFaers", "prrLCIFaers", "rorValueFaers", "rorLCIFaers",
                                               "newPediatricCountFaers", "newInteractingCountFaers", "newGeriatricCountFaers", "newNonSeriousFaers", "newFatalCountFaers",
                                               "ebgmFaers", "chiSquareFaers", "newEeaEvdas", "newHcpEvdas", "newSeriousEvdas",
                                               "newMedErrEvdas", "newObsEvdas", "newFatalEvdas", "newRcEvdas", "newLitEvdas", "newPaedEvdas", "ratioRorPaedVsOthersEvdas",
                                               "newGeriaEvdas", "ratioRorGeriatrVsOthersEvdas", "newSpontEvdas", "totSpontEuropeEvdas", "totSpontNAmericaEvdas",
                                               "totSpontJapanEvdas", "totSpontAsiaEvdas", "totSpontRestEvdas", "europeRorEvdas", "northAmericaRorEvdas", "japanRorEvdas", "rorValueEvdas",
                                               "asiaRorEvdas", "restRorEvdas", "newEvEvdas", "newCountVaers", "cummCountVaers", "newFatalCountVaers", "cumFatalCountVaers", "newSeriousCountVaers", "cumSeriousCountVaers",
                                               "newGeriatricCountVaers", "cumGeriatricCountVaers", "newPediatricCountVaers", "cummPediatricCountVaers", "eb05Vaers", "ebgmVaers", "prrValueVaers", "prrLCIVaers",
                                               "rorValueVaers", "rorLCIVaers", "chiSquareVaers", "newCountVigibase", "cummCountVigibase", "newFatalCountVigibase", "cumFatalCountVigibase", "newSeriousCountVigibase", "cumSeriousCountVigibase",
                                               "newGeriatricCountVigibase", "cumGeriatricCountVigibase", "newPediatricCountVigibase", "cummPediatricCountVigibase", "eb05Vigibase", "ebgmVigibase", "prrValueVigibase", "prrLCIVigibase",
                                               "rorValueVigibase", "rorLCIVigibase", "chiSquareVigibase",
                                               "eb05Jader", "ebgmJader", "prrValueJader", "prrLCIJader","rorValueJader", "rorLCIJader", "chiSquareJader","aValueJader", "bValueJader", "cValueJader", "dValueJader", "eValueJader", "rrValueJader"]
        List<String> jsonStringColumnsList = ["impEventsFaers", "freqPriorityFaers", "positiveRechallengeFaers", "positiveDechallengeFaers",
                                              "listedFaers", "relatedFaers", "pregenencyFaers", "trendTypeFaers", "hlgtEvdas", "hltEvdas", "smqNarrowEvdas", "impEventsEvdas", "changesEvdas", "listedEvdas", "dmeImeEvdas", "sdrEvdas", "sdrGeratrEvdas", "sdrPaedEvdas"]
        Map optionalFields = ["newCount"         : ["new_count", "cumm_count"], "newSeriousCount": ["new_serious_count", "cum_serious_count"], "newFatalCount": ["new_fatal_count", "cum_fatal_count"], "eb05Faers": ["eb05Faers", "eb95Faers"], "newSponCount": ["new_spon_count", "cum_spon_count"],
                              "newGeriatricCount": ["new_geriatric_count", "cum_geriatric_count"], "newInteractingCount": ["new_interacting_count", "cumm_interacting_count"], "newPediatricCount": ["new_pediatric_count", "cumm_pediatric_count"], "newProdCount": ["prod_n_period", "prod_n_cumul"],
                              "newNonSerious"    : ["new_non_serious", "cum_non_serious"], "newStudyCount": ["new_study_count", "cum_study_count"], "newEv": ["new_ev", "total_ev"], "newFatal": ["new_fatal", "total_fatal"], "newPaed": ["new_paed", "tot_paed"], "newEea": ["new_eea", "tot_eea"], "newMedErr": ["new_med_Err", "tot_med_Err"],
                              "newHcp"           : ["new_hcp", "tot_hcp"], "newSpont": ["new_spont", "tot_spont"], "newGeria": ["new_geria", "tot_geria"], "newObs": ["new_obs", "tot_obs"], "newLit": ["new_lit", "tot_lit"], "newSerious": ["new_serious", "total_serious"], "newRc": ["new_rc", "tot_rc"]]
        Map selectedDataSourceOptionalField = ["newCountFaers"            : ["FAERS_COLUMNS, '\$.newCountFaers'", "FAERS_COLUMNS, '\$.cummCountFaers'"], "newEvEvdas": ["EVDAS_COLUMNS, '\$.newEvEvdas'", "EVDAS_COLUMNS, '\$.totalEvEvdas'"],
                                               "newSeriousCountFaers"     : ["FAERS_COLUMNS, '\$.newSeriousCountFaers'", "FAERS_COLUMNS, '\$.cumSeriousCountFaers'"], "newStudyCountFaers": ["FAERS_COLUMNS, '\$.newStudyCountFaers'", "FAERS_COLUMNS, '\$.cumStudyCountFaers'"], "newPediatricCountFaers": ["FAERS_COLUMNS, '\$.newPediatricCountFaers'", "FAERS_COLUMNS, '\$.cummPediatricCountFaers'"], "newFatalCountFaers": ["FAERS_COLUMNS, '\$.newFatalCountFaers'", "FAERS_COLUMNS, '\$.cumFatalCountFaers'"],
                                               "newGeriatricCountFaers"   : ["FAERS_COLUMNS, '\$.newGeriatricCountFaers'", "FAERS_COLUMNS, '\$.cumGeriatricCountFaers'"], "newNonSeriousFaers": ["FAERS_COLUMNS, '\$.newNonSeriousFaers'", "FAERS_COLUMNS, '\$.cumNonSeriousFaers'"],
                                               "newSponCountFaers"        : ["FAERS_COLUMNS, '\$.newSponCountFaers'", "FAERS_COLUMNS, '\$.cumSponCountFaers'"], "newInteractingCountFaers": ["FAERS_COLUMNS, '\$.newInteractingCountFaers'", "FAERS_COLUMNS, '\$.cummInteractingCountFaers'"],
                                               "newFatalCountVigibase"    : ["VIGIBASE_COLUMNS, '\$.newFatalCountVigibase'", "VIGIBASE_COLUMNS, '\$.cumFatalCountVigibase'"], "newGeriatricCountVigibase": ["VIGIBASE_COLUMNS, '\$.newGeriatricCountVigibase'", "VIGIBASE_COLUMNS, '\$.cumGeriatricCountVigibase'"],
                                               "newPediatricCountVigibase": ["VIGIBASE_COLUMNS, '\$.newPediatricCountVigibase'", "VIGIBASE_COLUMNS, '\$.cummPediatricCountVigibase'"], "newSeriousCountVigibase": ["VIGIBASE_COLUMNS, '\$.newSeriousCountVigibase'", "VIGIBASE_COLUMNS, '\$.cumSeriousCountVigibase'"],
                                               "newCountVigibase"         : ["VIGIBASE_COLUMNS, '\$.newCountVigibase'", "VIGIBASE_COLUMNS, '\$.cummCountVigibase'"], "newFatalEvdas": ["EVDAS_COLUMNS, '\$.newFatalEvdas'", "EVDAS_COLUMNS, '\$.totalFatalEvdas'"],
                                               "newSeriousEvdas"          : ["EVDAS_COLUMNS, '\$.newSeriousEvdas'", "EVDAS_COLUMNS, '\$.totalSeriousEvdas'"], "newLitEvdas": ["EVDAS_COLUMNS, '\$.newLitEvdas'", "EVDAS_COLUMNS, '\$.totalLitEvdas'"],
                                               "newFatalCountJader"    : ["JADER_COLUMNS, '\$.newFatalCountJader'", "JADER_COLUMNS, '\$.cumFatalCountJader'"], "newGeriatricCountJader": ["JADER_COLUMNS, '\$.newGeriatricCountJader'", "JADER_COLUMNS, '\$.cumGeriatricCountJader'"],
                                               "newPediatricCountJader": ["JADER_COLUMNS, '\$.newPediatricCountJader'", "JADER_COLUMNS, '\$.cumPediatricCountJader'"], "newSeriousCountJader": ["JADER_COLUMNS, '\$.newSeriousCountJader'", "JADER_COLUMNS, '\$.cumSeriousCountJader'"],
                                               "newCountJader"         : ["JADER_COLUMNS, '\$.newCountJader'", "JADER_COLUMNS, '\$.cumCountJader'"],
                                               "newSpontEvdas"            : ["EVDAS_COLUMNS, '\$.newSpontEvdas'", "EVDAS_COLUMNS, '\$.totSpontEvdas'"], "newPaedEvdas": ["EVDAS_COLUMNS, '\$.newPaedEvdas'", "EVDAS_COLUMNS, '\$.totPaedEvdas'"],
                                               "newObsEvdas"              : ["EVDAS_COLUMNS, '\$.newObsEvdas'", "EVDAS_COLUMNS, '\$.totObsEvdas'"], "newMedErrEvdas": ["EVDAS_COLUMNS, '\$.newMedErrEvdas'", "EVDAS_COLUMNS, '\$.totMedErrEvdas'"],
                                               "newRcEvdas"               : ["EVDAS_COLUMNS, '\$.newRcEvdas'", "EVDAS_COLUMNS, '\$.totRcEvdas'"], "newGeriaEvdas": ["EVDAS_COLUMNS, '\$.newGeriaEvdas'", "EVDAS_COLUMNS, '\$.totGeriaEvdas'"],
                                               "newFatalCountVaers"       : ["VAERS_COLUMNS, '\$.newFatalCountVaers'", "VAERS_COLUMNS, '\$.cumFatalCountVaers'"], "newSeriousCountVaers": ["VAERS_COLUMNS, '\$.newSeriousCountVaers'", "VAERS_COLUMNS, '\$.cumSeriousCountVaers'"], "newGeriatricCountVaers": ["VAERS_COLUMNS, '\$.newGeriatricCountVaers'", "VAERS_COLUMNS, '\$.cumGeriatricCountVaers'"],
                                               "newPediatricCountVaers"   : ["VAERS_COLUMNS, '\$.newPediatricCountVaers'", "VAERS_COLUMNS, '\$.cummPediatricCountVaers'"], "newCountVaers": ["VAERS_COLUMNS, '\$.newCountVaers'", "VAERS_COLUMNS, '\$.cummCountVaers'"],
                                               "newHcpEvdas"               : ["EVDAS_COLUMNS, '\$.newHcpEvdas'", "EVDAS_COLUMNS, '\$.totHcpEvdas'"], "newEeaEvdas" : ["EVDAS_COLUMNS, '\$.newEeaEvdas'", "EVDAS_COLUMNS, '\$.totEeaEvdas'"]]
        String nullsOrder = orderColumnMap.dir == "desc" ? "NULLS LAST" : "NULLS FIRST"
        List newFields = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT',true).collect{
            it.name
        }
        List newAdhocColumnList = alertFieldService.getAggOnDemandColumnList(Constants.DataSource.PVA,false,true)
        List newAdhocColumnNameList = newAdhocColumnList.collect{it.name}
        List subGroupingFields = cacheService.allOtherSubGroupColumnUIList(Constants.DataSource.PVA)?.values() as List
        List relSubGroupingFields = cacheService.relativeSubGroupColumnUIList(Constants.DataSource.PVA)?.values() as List
        if (aliasOrderMap.containsKey(orderColumnMap.name)) {
            "$orderColumnMap.name" {
                order("${aliasOrderMap.get(orderColumnMap.name)}", orderColumnMap.dir)
            }
        } else if (orderMap.containsKey(orderColumnMap.name)) {
            order(orderMap.get(orderColumnMap.name), orderColumnMap.dir)
        } else if (delegate.targetClass == AggregateOnDemandAlert && orderColumnMap.name.equals("pregenency")) {
            order("pregnancy", orderColumnMap.dir)
        } else if(orderColumnMap.name.toString() in newAdhocColumnNameList && delegate.targetClass == AggregateOnDemandAlert){
            Map columnInfoMap = newAdhocColumnList.find{it.name == orderColumnMap.name}
            if(columnInfoMap.secondaryName) {
                sqlRestriction("1=1 ORDER BY to_number(json_value(NEW_COUNTS_JSON, '\$.$orderColumnMap.name')) ${orderColumnMap.dir} ${nullsOrder},to_number(json_value(NEW_COUNTS_JSON, '\$.$columnInfoMap.secondaryName')) ${orderColumnMap.dir} ${nullsOrder},(PT) ${orderColumnMap.dir}")
            }else{
                sqlRestriction("1=1 ORDER BY json_value(NEW_COUNTS_JSON, '\$.$orderColumnMap.name') ${orderColumnMap.dir} ${nullsOrder}")
            }
        }
        else if (orderColumnMap.name.toString() in newFields && delegate.targetClass == AggregateCaseAlert) {
            if(orderColumnMap.name in ["hlt","hlgt","smqNarrow"]){
                sqlRestriction("1=1 ORDER BY json_value(NEW_COUNTS_JSON, '\$.$orderColumnMap.name') ${orderColumnMap.dir}")
            }else{
                String newCount = orderColumnMap.name + ".new"
                String cumCount = orderColumnMap.name + ".cum"
                sqlRestriction("1=1 ORDER BY to_number(json_value(NEW_COUNTS_JSON, '\$.$newCount'))  ${orderColumnMap.dir} ${nullsOrder} , to_number(json_value(NEW_COUNTS_JSON, '\$.$cumCount'))  ${orderColumnMap.dir} ${nullsOrder},(PT) ${orderColumnMap.dir}")
            }
        }
        else if (orderColumnMap.name in listColName) {
            if (delegate.targetClass in [EvdasOnDemandAlert, EvdasAlert, ArchivedEvdasAlert] && orderColumnMap.name.equals("listed")) {
                order("listedness", orderColumnMap.dir)
            } else if (delegate.targetClass in [EvdasOnDemandAlert, EvdasAlert, ArchivedEvdasAlert] && orderColumnMap.name.equals("newLit")) {
                sqlRestriction("1=1 ORDER BY CAST(NEW_LIT as int) ${orderColumnMap.dir} ${nullsOrder}")
            } else if (delegate.targetClass in [EvdasOnDemandAlert, EvdasAlert, ArchivedEvdasAlert] && orderColumnMap.name.equals("rorValue")) {
                sqlRestriction("1=1 ORDER BY NVL(CAST(ALL_ROR AS DECIMAL(18,2)), 0.0) ${orderColumnMap.dir} ${nullsOrder}")
            } else if (delegate.targetClass in [EvdasOnDemandAlert, EvdasAlert, ArchivedEvdasAlert] && orderColumnMap.name.equals("allRor")) {
                sqlRestriction("1=1 ORDER BY NVL(CAST(ALL_ROR AS DECIMAL(18,2)), 0.0) ${orderColumnMap.dir} ${nullsOrder}")
            } else if (orderColumnMap.name in ["impEvents"]) {
                String sl = Holders.config.importantEvents.stopList.abbreviation.toLowerCase()
                sqlRestriction(" 1=1 ORDER BY lower(REPLACE(IMP_EVENTS, 'ei', '${sl}')) ${orderColumnMap.dir}")
            } else if (orderColumnMap.name in ["caseNumber"] && orderColumnMap.isVaers) {
                sqlRestriction("1=1 ORDER BY to_number(CASE_NUMBER) ${orderColumnMap.dir} ${nullsOrder}")
            } else if (orderColumnMap.name in ["caseNumber"] && orderColumnMap.isVigibase) {
                sqlRestriction("1=1 ORDER BY to_number(CASE_NUMBER) ${orderColumnMap.dir} ${nullsOrder}")
            } else if (orderColumnMap.name in optionalFields) {
                sqlRestriction("1=1 ORDER BY to_number(${optionalFields.get((orderColumnMap.name))?.get(0)})  ${orderColumnMap.dir} ${nullsOrder}, to_number(${optionalFields.get((orderColumnMap.name))?.get(1)}) ${orderColumnMap.dir} ${nullsOrder},(PT) ${orderColumnMap.dir}")
            } else {
                order(Order.(orderColumnMap.dir)(orderColumnMap.name).ignoreCase()).order("id")
            }
        } else if (orderColumnMap.name in ["signalsAndTopics"]) {
            createAlias("validatedSignals", "vs", JoinType.LEFT_OUTER_JOIN)
            order(Order.(orderColumnMap.dir)('vs.name').ignoreCase())
        } else if(orderColumnMap.name in subGroupingFields ){
            String columnName
            String fieldName
            if(orderColumnMap.name?.toString()?.toUpperCase()?.startsWith("EBGM")){
                columnName = "EBGM_SUB_GROUP"
                fieldName = orderColumnMap.name - "ebgm"
            }else if(orderColumnMap.name.toString()?.toUpperCase()?.startsWith("EB05")){
                columnName = "EB05_SUB_GROUP"
                fieldName = orderColumnMap.name?.toString() - "eb05"
            }else if(orderColumnMap.name.toString()?.toUpperCase()?.startsWith("EB95")){
                columnName = "EB95_SUB_GROUP"
                fieldName = orderColumnMap.name?.toString() - "eb95"
            }else if(orderColumnMap.name.toString()?.toUpperCase()?.startsWith("RORLCI")){
                columnName = "ROR_LCI_SUB_GROUP"
                fieldName = orderColumnMap.name?.toString() - "rorLci"
            }else if(orderColumnMap.name.toString()?.toUpperCase()?.startsWith("RORUCI")){
                columnName = "ROR_UCI_SUB_GROUP"
                fieldName = orderColumnMap.name - "rorUci"
            }else if(orderColumnMap.name.toString()?.toUpperCase()?.startsWith("ROR")){
                columnName = "ROR_SUB_GROUP"
                fieldName = orderColumnMap.name?.toString() - "ror"
            }else if(orderColumnMap.name.toString()?.toUpperCase()?.startsWith("PRRLCI")){
                columnName = "PRR_LCI_SUB_GROUP"
                fieldName = orderColumnMap.name?.toString() - "prrLci"
            }else if(orderColumnMap.name.toString()?.toUpperCase()?.startsWith("PRRUCI")){
                columnName = "PRR_UCI_SUB_GROUP"
                fieldName = orderColumnMap.name?.toString() - "prrUci"
            }else if(orderColumnMap.name.toString()?.toUpperCase()?.startsWith("PRR")){
                columnName = "PRR_SUB_GROUP"
                fieldName = orderColumnMap.name?.toString() - "prr"
            }else if(orderColumnMap.name.toString()?.toUpperCase()?.startsWith("CHISQUARE")){
                columnName = "CHI_SQUARE_SUB_GROUP"
                fieldName = orderColumnMap.name?.toString() - "chiSquare"
            }
            fieldName = escapeSpecialCharacters(fieldName)
            sqlRestriction("1=1 ORDER BY coalesce(to_number(regexp_substr(regexp_substr(${columnName}, '\"${fieldName}\" *: *\\d+(\\.\\d+)?'),'\\d+(\\.\\d+)?')),0) ${orderColumnMap.dir} ${nullsOrder}")
        } else if(orderColumnMap.name in relSubGroupingFields){
            String columnName
            String fieldName
            if(orderColumnMap.name.toString()?.toUpperCase()?.startsWith("RORLCI")){
                columnName = "ROR_LCI_REL_SUB_GROUP"
                fieldName = orderColumnMap.name?.toString() - "rorLciRel"
            }else if(orderColumnMap.name.toString()?.toUpperCase()?.startsWith("RORUCI")){
                columnName = "ROR_UCI_REL_SUB_GROUP"
                fieldName = orderColumnMap.name?.toString() - "rorUciRel"
            }else if(orderColumnMap.name.toString()?.toUpperCase()?.startsWith("ROR")){
                columnName = "ROR_REL_SUB_GROUP"
                fieldName = orderColumnMap.name?.toString() - "rorRel"
            }
            fieldName = escapeSpecialCharacters(fieldName)
            sqlRestriction("1=1 ORDER BY coalesce(to_number(regexp_substr(regexp_substr(${columnName}, '\"${fieldName}\" *: *\\d+(\\.\\d+)?'),'\\d+(\\.\\d+)?')),0) ${orderColumnMap.dir} ${nullsOrder}")
        } else if (orderColumnMap.name in clobFields) {
            sqlRestriction(" 1=1 ORDER BY nvl(dbms_lob.substr(UPPER(${clobFields.get(orderColumnMap.name)})), '-') ${orderColumnMap.dir}")
        } else if (isFaersStratificationColumn(orderColumnMap.name.toString())) {
            String columnName = orderColumnMap.name.substring(0, 4)
            String fieldName = orderColumnMap.name.substring(4, orderColumnMap.name.length() - 5)
            columnName = columnName?.equalsIgnoreCase("EBGM") ? "EBGM_" : columnName
            if (fieldName.equalsIgnoreCase("")) {
                sqlRestriction("1=1 ORDER BY json_value(${orderColumnJson}, '\$.${orderColumnMap.name}') + 0 ${orderColumnMap.dir} ${nullsOrder}")
            } else {
                sqlRestriction("1=1 ORDER BY coalesce(to_number(regexp_substr(regexp_substr(concat(${columnName}GENDER_FAERS,${columnName}AGE_FAERS), '${fieldName} *: *\\d+(\\.\\d+)?'),'\\d+(\\.\\d+)?')),0) ${orderColumnMap.dir} ${nullsOrder}")
            }
        } else if ((orderColumnMap.name.toString()?.toUpperCase()?.startsWith("EBGM") && orderColumnJson != "VAERS_COLUMNS" && orderColumnJson != "VIGIBASE_COLUMNS" && orderColumnJson != "JADER_COLUMNS") || (orderColumnMap.name.toString()?.toUpperCase()?.startsWith("EB05") && orderColumnJson != "VAERS_COLUMNS" && orderColumnJson != "VIGIBASE_COLUMNS" && orderColumnJson != "JADER_COLUMNS") || orderColumnMap.name.toString()?.toUpperCase()?.startsWith("EB95")) {
            String columnName = orderColumnMap?.name?.toString()?.substring(0, 4)
            String fieldName = orderColumnMap?.name?.toString()?.substring(4)
            columnName = columnName?.equalsIgnoreCase("EBGM") ? "EBGM_" : columnName
            sqlRestriction("1=1 ORDER BY coalesce(to_number(regexp_substr(regexp_substr(concat(${columnName}GENDER,${columnName}AGE), '${fieldName} *: *\\d+(\\.\\d+)?'),'\\d+(\\.\\d+)?')),0) ${orderColumnMap.dir} ${nullsOrder}")
        } else if (orderColumnMap.name in selectedDataSourceOptionalField) {
            sqlRestriction("1=1 ORDER BY json_value(${selectedDataSourceOptionalField.get((orderColumnMap.name))?.get(0)} returning number)  ${orderColumnMap.dir} ${nullsOrder}, json_value(${selectedDataSourceOptionalField.get((orderColumnMap.name))?.get(1)} returning number) ${orderColumnMap.dir} ${nullsOrder},(PT) ${orderColumnMap.dir}")
        } else if (jsonIntegerColumnsList.contains(orderColumnMap.name)) {
            sqlRestriction("1=1 ORDER BY json_value(${orderColumnJson}, '\$.${orderColumnMap.name}') + 0 ${orderColumnMap.dir} ${nullsOrder}, last_updated DESC")
        } else if (jsonStringColumnsList.contains(orderColumnMap.name)) {
            sqlRestriction("1=1 ORDER BY json_value(${orderColumnJson}, '\$.${orderColumnMap.name}') ${orderColumnMap.dir} ${nullsOrder}")
        }else {
            order("lastUpdated", "desc")
        }
    }

    Closure criteriaConditions = { String fieldName, String operatorKey, String value, Boolean isScaFaers = false, Long configId = null ->
        if (fieldName == Constants.AdvancedFilter.CASE_SERIES) {
            fieldName = "caseNumber"
            value = getCaseOfSeries(value)
        }

        Boolean isProductGroupValue = value.contains('PG_')
        Boolean isEventGroupValue = value.contains('EG_')

        List valuesList = []
        if (isProductGroupValue || isEventGroupValue) {
            if (fieldName == "productName") {
                valuesList = getValuesListForClobFields(value)
                if(isProductGroupValue){
                    fieldName="productId"
                }
            }else if (fieldName == "pt") {
                fieldName="ptCode"
            } else if (fieldName == "suspProd" || fieldName == "conComit") {
                valuesList = getValuesListForClobFields(value)
            }
            valuesList = getGroupValueList(value, isProductGroupValue, isEventGroupValue, fieldName)
        } else {
            if (fieldName == "productName") {
                valuesList = getValuesListForClobFields(value)
                if(isProductGroupValue){
                    fieldName="productId"
                }
            } else {
                valuesList = getValuesList(value, fieldName)
            }
        }
        log.info('Values for field : ' + valuesList)
        if (fieldName.equals(Constants.Badges.FLAGS) && delegate.targetClass == SingleCaseAlert) {
            fieldName = Constants.Badges.BADGE_TEXT
        }
        if (delegate.targetClass == AggregateOnDemandAlert && fieldName.equalsIgnoreCase("pregenency")) {
            fieldName = "pregnancy"
        }

        Map<String, List<String>> aliasPropertiesMap = [
                'priority.id'       : ['priority', 'pr', '.displayName'],
                'disposition.id'    : ['disposition', 'disp', '.displayName'],
                'assignedTo.id'     : ['assignedTo', 'assigned', '.fullName'],
                'assignedToGroup.id': ['assignedToGroup', 'assignedGroup', '.name'],
        ]

        Map<String, Map<String, String>> sqlRestrictionAliasMap = grailsApplication.config.advancedFilter.sqlRestrictionAliasMap

        AdvancedFilterOperatorEnum operatorEnum = operatorKey as AdvancedFilterOperatorEnum

        Map<String, String> iLikePropertiesMap = [
                'CONTAINS'  : "%${value}%",
                'START_WITH': "${value}%",
                'ENDS_WITH' : "%${value}"
        ]
        Map<String, String> notILikePropertiesMap = [
                'DOES_NOT_CONTAIN': "%${value}%",
                'DOES_NOT_START'  : "${value}%",
                'DOES_NOT_END'    : "%${value}"
        ]

        if ((operatorEnum != AdvancedFilterOperatorEnum.NOT_EQUAL && operatorEnum != AdvancedFilterOperatorEnum.EQUALS && aliasPropertiesMap.containsKey(fieldName)) || (!fieldName.contains('id') && aliasPropertiesMap.containsKey(fieldName))) {
            List<String> aliasProperties = aliasPropertiesMap.get(fieldName)
            fieldName = aliasProperties[1] + aliasProperties[2]
            createAlias(aliasProperties[0], aliasProperties[1], JoinType.LEFT_OUTER_JOIN)
        }

        if (fieldName in [Constants.AdvancedFilter.COMMENTS, Constants.AdvancedFilter.COMMENT]) {
            List commentList = alertCommentsAdvanceFilter(valuesList, operatorEnum, value, delegate.targetClass, isScaFaers, configId)
            String inStr = getInStringForFilters(commentList, delegate.targetClass)
            if (operatorEnum in [AdvancedFilterOperatorEnum.IS_EMPTY]) {
                switch (delegate.targetClass) {
                    case SingleCaseAlert:
                        sqlRestriction(""" ({alias}.case_id,{alias}.case_version) not in  ${inStr} """)
                        break
                    case AggregateCaseAlert:
                        sqlRestriction("""({alias}.product_id,{alias}.pt,{alias}.alert_configuration_id) not in ${
                            inStr
                        }""")
                        break
                    case EvdasAlert:
                        sqlRestriction("""({alias}.substance,{alias}.pt,{alias}.alert_configuration_id) not in ${
                            inStr
                        }""")
                        break
                }
            } else {
                switch (delegate.targetClass) {
                    case SingleCaseAlert:
                        sqlRestriction("""({alias}.case_id,{alias}.case_version) in ${inStr}""")
                        break
                    case AggregateCaseAlert:
                        sqlRestriction("""({alias}.product_id,{alias}.pt,{alias}.alert_configuration_id) in ${inStr}""")
                        break
                    case EvdasAlert:
                        sqlRestriction("""({alias}.substance,{alias}.pt,{alias}.alert_configuration_id) in ${inStr}""")
                        break
                }
            }
        } else if (sqlRestrictionAliasMap.containsKey(fieldName)) {
            if (fieldName.equals(Constants.AdvancedFilter.SIGNAL) && delegate.targetClass == AggregateCaseAlert) {
                fieldName = Constants.AdvancedFilter.AGG_SIGNAL
            } else if (fieldName.equals(Constants.AdvancedFilter.SIGNAL) && delegate.targetClass == EvdasAlert) {
                fieldName = Constants.AdvancedFilter.EVDAS_SIGNAL
            }
            String sqlRestrictionSql = generateSqlRestrictionSql(operatorEnum, valuesList, iLikePropertiesMap, notILikePropertiesMap, sqlRestrictionAliasMap.get(fieldName))
            if (sqlRestrictionSql) {
                sqlRestriction(sqlRestrictionSql)
            }
        } else if (operatorEnum == AdvancedFilterOperatorEnum.NOT_EQUAL) {
            not {
                'or' {
                    valuesList.collate(1000).each {
                        'in'(fieldName, it)
                    }
                }
            }
        } else if (operatorEnum == AdvancedFilterOperatorEnum.EQUALS) {
            'or' {
                valuesList.collate(1000).each {
                    'in'(fieldName, it)
                }
            }
        } else if (notILikePropertiesMap.containsKey(operatorKey)) {
            if (operatorKey == 'DOES_NOT_CONTAIN' && (isProductGroupValue || isEventGroupValue)) {
                not {
                    'or' {
                        valuesList.each {
                            if(it.getClass() == Pair){
                                // Filtered Broad/Narrow Smq with smqCode
                                String smqCode = it.getaValue()
                                Integer ptCode = it.getbValue()
                                and{
                                    eq('smqCode', smqCode)
                                    'in'(fieldName, ptCode)
                                }
                            } else if (StringUtils.equalsIgnoreCase("suspProd", fieldName) || StringUtils.equalsIgnoreCase("conComit", fieldName)) {
                                log.info("fieldName" + fieldName + "It...." + it)
                                'in'(fieldName, String.valueOf(it))
                            } else {
                                'in'(fieldName, isProductGroupValue ? new BigInteger(it) : new Integer(it))
                            }
                        }
                    }
                }
            } else {
                not {
                    ilike(fieldName, notILikePropertiesMap.get(operatorKey))
                }
            }
        } else if (iLikePropertiesMap.containsKey(operatorKey)) {
            if (operatorKey == 'CONTAINS' && (isProductGroupValue || isEventGroupValue)) {
                'or' {
                    valuesList.each {
                        if(it.getClass() == Pair){
                            String smqCode = it.getaValue()
                            Integer ptCode = it.getbValue()
                            and{
                                eq('smqCode', smqCode)
                                'in'(fieldName, ptCode)
                            }
                        } else if (StringUtils.equalsIgnoreCase("suspProd", fieldName) || StringUtils.equalsIgnoreCase("conComit", fieldName)) {
                            log.info("fieldName" + fieldName + "It...." + it)
                            'in'(fieldName, String.valueOf(it))
                        } else {
                            'in'(fieldName, isProductGroupValue ? new BigInteger(it) : new Integer(it))
                        }
                    }
                }
            } else {
                and {
                    ilike(fieldName, iLikePropertiesMap.get(operatorKey))
                    ne(fieldName, 'undefined')
                }
            }
        } else if (operatorEnum == AdvancedFilterOperatorEnum.IS_EMPTY) {
            or {
                "${operatorEnum.value()}"(fieldName)
                eq(fieldName, 'undefined')
            }
        } else if (operatorEnum == AdvancedFilterOperatorEnum.IS_NOT_EMPTY) {
            and {
                "${operatorEnum.value()}"(fieldName)
                ne(fieldName, 'undefined')
            }
        }

    }

    Closure criteriaConditionsDate = { String fieldName, String operatorKey, String value ->

        AdvancedFilterOperatorEnum operatorEnum = operatorKey as AdvancedFilterOperatorEnum
        if (operatorKey in AdvancedFilterOperatorEnum.numericOperators*.name() || operatorKey == 'NOT_EQUAL') {
            QueryOperatorEnum queryOperatorEnum = operatorKey as QueryOperatorEnum
            String formatDate = null
            if (fieldName == 'dueDate') {
                formatDate = new Date().plus(Integer.parseInt(value)).format("dd-MMM-yy")
            } else {
                formatDate = Date.parse('MM/dd/yyyy', value).format("dd-MMM-yy")
            }
            sqlRestriction("TRUNC(${dateColumn.get(fieldName)}) ${queryOperatorEnum.value()} '${formatDate}'")
        } else if (operatorEnum == AdvancedFilterOperatorEnum.IS_EMPTY || operatorEnum == AdvancedFilterOperatorEnum.IS_NOT_EMPTY) {
            "${operatorEnum.value()}"(fieldName)
        } else if (operatorKey in AdvancedFilterOperatorEnum.valuelessOperators*.name()) {
            List<Date> startDates = RelativeDateConverter.(operatorEnum.value())(null, 1, "UTC")
            between(fieldName, startDates[0], startDates[1])
        } else if (operatorKey in AdvancedFilterOperatorEnum.numericValueDateOperators*.name()) {
            List<Date> startDates = RelativeDateConverter.(operatorEnum.value())(null, Integer.parseInt(value), "UTC")
            between(fieldName, startDates[0], startDates[1])
        }

    }

    Closure criteriaConditionsForCLOB = { String fieldName, String operatorKey, String value, Long exeConfigId = null ->
        Map<String, List<String>> aliasPropertiesMap = delegate.targetClass in [SingleOnDemandAlert, AggregateOnDemandAlert, EvdasOnDemandAlert] ? grailsApplication.config.advancedFilter.onDemand.aliasPropertiesMap : grailsApplication.config.advancedFilter.aliasPropertiesMap


        String sqlRestrictionClobSql = generateSqlRestrictionClobSql(fieldName, operatorKey, value, aliasPropertiesMap, exeConfigId, delegate.targetClass)
        log.info("Clob SQL: " + sqlRestrictionClobSql)
        if (sqlRestrictionClobSql) {
            sqlRestriction(sqlRestrictionClobSql)
        }
    }
    //Fix for PVS-55470 starts from here.
    Closure criteriaConditionsForNewColumns = { Boolean isCumCount,String fieldName, String operatorKey, String value ->
        QueryOperatorEnum operatorEnum = operatorKey as QueryOperatorEnum
        if(fieldName.contains("_cum")){
            isCumCount=true
            fieldName = fieldName.replaceAll("_cum", "")
        }
        String operatorField
        operatorField = operatorEnum.value()
        if (operatorEnum == QueryOperatorEnum.IS_EMPTY) {
            operatorField = "IS"
            value = "NULL"
        } else if (operatorEnum == QueryOperatorEnum.IS_NOT_EMPTY) {
            operatorField = "IS NOT"
            value = "NULL"
        }
        Map<String, String> iLikePropertiesMap = [
                'CONTAINS'  : "%${value}%",
                'START_WITH': "${value}%",
                'ENDS_WITH' : "%${value}"
        ]
        Map<String, String> notILikePropertiesMap = [
                'DOES_NOT_CONTAIN': "%${value}%",
                'DOES_NOT_START'  : "${value}%",
                'DOES_NOT_END'    : "%${value}"
        ]
        Map<String, String> isEmptyMap = [
                'IS_EMPTY'    : "IS NULL",
                'IS_NOT_EMPTY': "IS NOT NULL",
        ]
        List valuesList = getValuesListForClobFields(value)
        StringBuilder criteriaStringBuilder = new StringBuilder()
        if(fieldName in ["hlt","hlgt"]){
            //for new varchar field
            if (iLikePropertiesMap.containsKey(operatorKey)) {
                sqlRestriction("UPPER(json_value(NEW_COUNTS_JSON, '\$.$fieldName')) LIKE UPPER('${iLikePropertiesMap.get(operatorKey)}')")
            } else if (notILikePropertiesMap.containsKey(operatorKey)) {
                sqlRestriction("UPPER(json_value(NEW_COUNTS_JSON, '\$.$fieldName')) NOT LIKE UPPER('${notILikePropertiesMap.get(operatorKey)}')")
            } else if (operatorKey == "IS_EMPTY" || operatorKey == "IS_NOT_EMPTY") {
                sqlRestriction("json_value(NEW_COUNTS_JSON, '\$.$fieldName') ${isEmptyMap.get(operatorKey)}")
            }else{
                criteriaStringBuilder.setLength(0)
                String multipleFieldCondition = (operatorKey == "NOT_EQUAL" ? " AND" : " OR")
                valuesList.eachWithIndex{name,idx ->
                    idx == 0 ? criteriaStringBuilder.append("json_value(NEW_COUNTS_JSON, '\$.$fieldName') ${operatorField} '${name}'") : criteriaStringBuilder.append("${multipleFieldCondition} json_value(NEW_COUNTS_JSON, '\$.$fieldName') ${operatorField} '${name}'")
                }
                sqlRestriction(criteriaStringBuilder.toString())
            }
        }else if(fieldName in ["smqNarrow"]){
            //for new numbered format clob field
            String newOperatorValue
            if (iLikePropertiesMap.containsKey(operatorKey)) {
                newOperatorValue = iLikePropertiesMap.get(operatorKey)
                if(operatorKey == 'START_WITH'){
                    newOperatorValue = '1) ' + newOperatorValue
                }
                sqlRestriction("UPPER(json_value(NEW_COUNTS_JSON, '\$.$fieldName')) LIKE UPPER('${newOperatorValue}')")
            } else if (notILikePropertiesMap.containsKey(operatorKey)) {
                newOperatorValue = notILikePropertiesMap.get(operatorKey)
                if(operatorKey == 'DOES_NOT_START'){
                    newOperatorValue = '1) ' + newOperatorValue
                }
                sqlRestriction("UPPER(json_value(NEW_COUNTS_JSON, '\$.$fieldName')) NOT LIKE UPPER('${newOperatorValue}')")
            } else if (operatorKey == "IS_EMPTY" || operatorKey == "IS_NOT_EMPTY") {
                sqlRestriction("json_value(NEW_COUNTS_JSON, '\$.$fieldName') ${isEmptyMap.get(operatorKey)}")
            }else if(operatorKey == "EQUALS"){
                criteriaStringBuilder.setLength(0)
                valuesList.eachWithIndex{name,idx ->
                    idx == 0 ? criteriaStringBuilder.append("UPPER(json_value(NEW_COUNTS_JSON, '\$.$fieldName')) LIKE UPPER('%${name}%')") : criteriaStringBuilder.append(" OR UPPER(json_value(NEW_COUNTS_JSON, '\$.$fieldName')) LIKE UPPER('%${name}%')")
                }
                sqlRestriction(criteriaStringBuilder.toString())
            }else if(operatorKey == "NOT_EQUAL"){
                criteriaStringBuilder.setLength(0)
                valuesList.eachWithIndex{name,idx ->
                    idx == 0 ? criteriaStringBuilder.append("UPPER(json_value(NEW_COUNTS_JSON, '\$.$fieldName')) NOT LIKE UPPER('%${name}%')") : criteriaStringBuilder.append(" AND UPPER(json_value(NEW_COUNTS_JSON, '\$.$fieldName')) NOT LIKE UPPER('%${name}%')")
                }
                sqlRestriction(criteriaStringBuilder.toString())
            }
        } else {
            if (isCumCount) {
                fieldName = fieldName + ".cum"
            } else {
                fieldName = fieldName + ".new"
            }
            if (operatorKey == "IS_EMPTY" || operatorKey == "IS_NOT_EMPTY") {
                sqlRestriction("json_value(NEW_COUNTS_JSON, '\$.$fieldName') ${isEmptyMap.get(operatorKey)}")
            }else{
                // for new number fields
                sqlRestriction("json_value(NEW_COUNTS_JSON, '\$.$fieldName') ${operatorField} '${value}'")
            }
        }
    }
    //Fix for PVS-55470 ends here.


    Closure criteriaConditionsForNewColumnsAdhoc = { String fieldName, String operatorKey, String value ->
        QueryOperatorEnum operatorEnum = operatorKey as QueryOperatorEnum
        String operatorField
        operatorField = operatorEnum.value()
        if (operatorEnum == QueryOperatorEnum.IS_EMPTY) {
            operatorField = "IS"
            value = "NULL"
        } else if (operatorEnum == QueryOperatorEnum.IS_NOT_EMPTY) {
            operatorField = "IS NOT"
            value = "NULL"
        }
        Map<String, String> iLikePropertiesMap = [
                'CONTAINS'  : "%${value}%",
                'START_WITH': "${value}%",
                'ENDS_WITH' : "%${value}"
        ]
        Map<String, String> notILikePropertiesMap = [
                'DOES_NOT_CONTAIN': "%${value}%",
                'DOES_NOT_START'  : "${value}%",
                'DOES_NOT_END'    : "%${value}"
        ]
        Map<String, String> isEmptyMap = [
                'IS_EMPTY'    : "IS NULL",
                'IS_NOT_EMPTY': "IS NOT NULL",
        ]
        List valuesList = getValuesListForClobFields(value)
        StringBuilder criteriaStringBuilder = new StringBuilder()
        if(fieldName in ["hlt","hlgt"]){
            //for new varchar field
            if (iLikePropertiesMap.containsKey(operatorKey)) {
                sqlRestriction("UPPER(json_value(NEW_COUNTS_JSON, '\$.$fieldName')) LIKE UPPER('${iLikePropertiesMap.get(operatorKey)}')")
            } else if (notILikePropertiesMap.containsKey(operatorKey)) {
               sqlRestriction("UPPER(json_value(NEW_COUNTS_JSON, '\$.$fieldName')) NOT LIKE UPPER('${notILikePropertiesMap.get(operatorKey)}')")
            } else if (operatorKey == "IS_EMPTY" || operatorKey == "IS_NOT_EMPTY") {
                sqlRestriction("json_value(NEW_COUNTS_JSON, '\$.$fieldName') ${isEmptyMap.get(operatorKey)}")
            }else{
                criteriaStringBuilder.setLength(0)
                String multipleFieldCondition = (operatorKey == "NOT_EQUAL" ? " AND" : " OR")
                valuesList.eachWithIndex{name,idx ->
                    idx == 0 ? criteriaStringBuilder.append("json_value(NEW_COUNTS_JSON, '\$.$fieldName') ${operatorField} '${name}'") : criteriaStringBuilder.append("${multipleFieldCondition} json_value(NEW_COUNTS_JSON, '\$.$fieldName') ${operatorField} '${name}'")
                }
                sqlRestriction(criteriaStringBuilder.toString())
            }
        }else if(fieldName in ["smqNarrow"]){
            //for new numbered format clob field
            String newOperatorValue
            if (iLikePropertiesMap.containsKey(operatorKey)) {
                newOperatorValue = iLikePropertiesMap.get(operatorKey)
                if(operatorKey == 'START_WITH'){
                    newOperatorValue = '1) ' + newOperatorValue
                }
                sqlRestriction("UPPER(json_value(NEW_COUNTS_JSON, '\$.$fieldName')) LIKE UPPER('${newOperatorValue}')")
            } else if (notILikePropertiesMap.containsKey(operatorKey)) {
                newOperatorValue = notILikePropertiesMap.get(operatorKey)
                if(operatorKey == 'DOES_NOT_START'){
                    newOperatorValue = '1) ' + newOperatorValue
                }
                sqlRestriction("UPPER(json_value(NEW_COUNTS_JSON, '\$.$fieldName')) NOT LIKE UPPER('${newOperatorValue}')")
            } else if (operatorKey == "IS_EMPTY" || operatorKey == "IS_NOT_EMPTY") {
                sqlRestriction("json_value(NEW_COUNTS_JSON, '\$.$fieldName') ${isEmptyMap.get(operatorKey)}")
            }else if(operatorKey == "EQUALS"){
                criteriaStringBuilder.setLength(0)
                valuesList.eachWithIndex{name,idx ->
                    idx == 0 ? criteriaStringBuilder.append("UPPER(json_value(NEW_COUNTS_JSON, '\$.$fieldName')) LIKE UPPER('%${name}%')") : criteriaStringBuilder.append(" OR UPPER(json_value(NEW_COUNTS_JSON, '\$.$fieldName')) LIKE UPPER('%${name}%')")
                }
                sqlRestriction(criteriaStringBuilder.toString())
            }else if(operatorKey == "NOT_EQUAL"){
                criteriaStringBuilder.setLength(0)
                valuesList.eachWithIndex{name,idx ->
                    idx == 0 ? criteriaStringBuilder.append("UPPER(json_value(NEW_COUNTS_JSON, '\$.$fieldName')) NOT LIKE UPPER('%${name}%')") : criteriaStringBuilder.append(" AND UPPER(json_value(NEW_COUNTS_JSON, '\$.$fieldName')) NOT LIKE UPPER('%${name}%')")
                }
                sqlRestriction(criteriaStringBuilder.toString())
            }
        } else {
            // for new number fields
            sqlRestriction("json_value(NEW_COUNTS_JSON, '\$.$fieldName') ${operatorField} '${value}'")
        }
    }


    Closure criteriaConditionsCount = { String fieldName, String operatorKey, String value ->
        AdvancedFilterOperatorEnum operatorEnum = operatorKey as AdvancedFilterOperatorEnum
        def criteriaValue
        Boolean isIntegerEmpty = false
        def minValueUndefined = -1
        if (fieldName in doubleTypeColumn) {
            minValueUndefined = -1.0d
        }
        if (fieldName in doubleTypeColumn && !(operatorKey in AdvancedFilterOperatorEnum.emptyOperators*.name())) {
            criteriaValue = value as Double
        } else if (!(operatorKey in AdvancedFilterOperatorEnum.emptyOperators*.name())) {
            criteriaValue = value as Integer
        }

        if (operatorKey in QueryOperatorEnum.numericOperatorsAdvancedFilter*.name() && prrRorColumn.containsKey(fieldName)) {
            QueryOperatorEnum queryOperatorEnum = operatorKey as QueryOperatorEnum
            sqlRestriction("${prrRorColumn.get(fieldName)} ${queryOperatorEnum.value()} ${criteriaValue} AND ${prrRorColumn.get(fieldName)} <> -1")
        } else if (operatorEnum == AdvancedFilterOperatorEnum.NOT_EQUAL) {
            not {
                eq(fieldName, criteriaValue)
            }
        } else if (operatorEnum == AdvancedFilterOperatorEnum.EQUALS) {
            eq(fieldName, criteriaValue)
        } else if (operatorKey in AdvancedFilterOperatorEnum.numericOperators*.name()) {
            "${operatorEnum.value()}"(fieldName, criteriaValue)
        } else if (operatorEnum == AdvancedFilterOperatorEnum.IS_NOT_EMPTY) {
            and {
                "${operatorEnum.value()}"(fieldName)
                ne(fieldName, minValueUndefined)
            }
        } else if (operatorEnum == AdvancedFilterOperatorEnum.IS_EMPTY) {
            or {
                "${operatorEnum.value()}"(fieldName)
                eq(fieldName, minValueUndefined)
            }
        }
    }

    Closure criteriaConditionsForSubGroup = { String fieldName, String operatorKey, String value ->
        QueryOperatorEnum operatorEnum = operatorKey as QueryOperatorEnum
        String columnfield
        String operatorField
        List fieldNames = fieldName.split(':')
        String initialName = fieldNames[0].toString().toUpperCase()
        List ebgmOldSubGroupList = cacheService.getSubGroupColumns()?.flatten()
        if((initialName?.startsWith("EBGM") || initialName?.startsWith("EB05") || initialName?.startsWith("EB95")) && (ebgmOldSubGroupList.contains(fieldNames[1]))){
            columnfield = fieldNames[0]?.toString().equalsIgnoreCase('EBGM') ? "EBGM_" : fieldNames[0]
        }else {
            columnfield = fieldNames[0].toString().replaceAll("-", "_") + "_SUB_GROUP"
        }
        operatorField = operatorEnum.value()
        if (operatorEnum == QueryOperatorEnum.IS_EMPTY) {
            operatorField = "IS"
            value = "NULL"
        } else if (operatorEnum == QueryOperatorEnum.IS_NOT_EMPTY) {
            operatorField = "IS NOT"
            value = "NULL"
        }
        String fieldNameRegex = escapeSpecialCharacters(fieldNames[1] as String)
        if((initialName?.startsWith("EBGM") || initialName?.startsWith("EB05") || initialName?.startsWith("EB95")) && (ebgmOldSubGroupList.contains(fieldNames[1])) ){
            sqlRestriction("to_number(regexp_substr(regexp_substr(concat(${columnfield}GENDER,${columnfield}AGE), '${fieldNameRegex} *: *\\d+(\\.\\d+)?'),'\\d+(\\.\\d+)?')) ${operatorField} ${value}")
        }else {
            sqlRestriction("to_number(regexp_substr(regexp_substr(${columnfield}, '\"${fieldNameRegex}\" *: *\\d+(\\.\\d+)?'),'\\d+(\\.\\d+)?')) ${operatorField} ${value}")
        }
    }
    Closure criteriaConditionsForRelSubGroup = { String fieldName, String operatorKey, String value ->
        QueryOperatorEnum operatorEnum = operatorKey as QueryOperatorEnum
        String operatorField
        List fieldNames = fieldName.split(':')
        String columnField = fieldNames[0].toString().replaceAll("-", "_") + "EL_SUB_GROUP"
        operatorField = operatorEnum.value()
        if (operatorEnum == QueryOperatorEnum.IS_EMPTY) {
            operatorField = "IS"
            value = "NULL"
        } else if (operatorEnum == QueryOperatorEnum.IS_NOT_EMPTY) {
            operatorField = "IS NOT"
            value = "NULL"
        }
        String fieldNameRegex = escapeSpecialCharacters(fieldNames[1] as String)
        sqlRestriction("to_number(regexp_substr(regexp_substr(${columnField}, '\"${fieldNameRegex}\" *: *\\d+(\\.\\d+)?'),'\\d+(\\.\\d+)?')) ${operatorField} ${value}")

    }
    Closure criteriaConditionsForSubGroupFaers = { String fieldName, String operatorKey, String value ->
        QueryOperatorEnum operatorEnum = operatorKey as QueryOperatorEnum
        String columnfield
        String operatorField
        List fieldNames = fieldName.split(':')
        if (fieldNames[0].indexOf("EBGM")) {
            fieldNames[0] = fieldNames[0].minus("FAERS")
        }
        columnfield = fieldNames[0]?.toString().equalsIgnoreCase('EBGMFAERS') ? "EBGM_" : fieldNames[0]
        operatorField = operatorEnum.value()
        if (operatorEnum == QueryOperatorEnum.IS_EMPTY) {
            operatorField = "IS"
            value = "NULL"
        } else if (operatorEnum == QueryOperatorEnum.IS_NOT_EMPTY) {
            operatorField = "IS NOT"
            value = "NULL"
        }
        sqlRestriction("to_number(regexp_substr(regexp_substr(concat(${columnfield}GENDER_FAERS,${columnfield}AGE_FAERS), '${fieldNames[1]} *: *\\d+(\\.\\d+)?'),'\\d+(\\.\\d+)?')) ${operatorField} ${value}")
    }

    Closure criteriaConditionsForIntegratedReview = { String fieldName, String operatorKey, String value ->
        String fieldColumnJson = fieldName ? fieldName.endsWith("Vaers") ? 'VAERS_COLUMNS' : fieldName.endsWith("Faers") ? 'FAERS_COLUMNS' : fieldName.endsWith("Vigibase") ? 'VIGIBASE_COLUMNS' : fieldName.endsWith("Jader") ? 'JADER_COLUMNS' : 'EVDAS_COLUMNS' : ''
        QueryOperatorEnum operatorEnum = operatorKey as QueryOperatorEnum
        String operatorField = operatorEnum.value()
        if (operatorEnum == QueryOperatorEnum.IS_EMPTY) {
            operatorField = "IS"
            value = "NULL"
        } else if (operatorEnum == QueryOperatorEnum.IS_NOT_EMPTY) {
            operatorField = "IS NOT"
            value = "NULL"
        }
        Map<String, String> iLikePropertiesMap = [
                'CONTAINS'  : "%${value}%",
                'START_WITH': "${value}%",
                'ENDS_WITH' : "%${value}"
        ]
        Map<String, String> notILikePropertiesMap = [
                'DOES_NOT_CONTAIN': "%${value}%",
                'DOES_NOT_START'  : "${value}%",
                'DOES_NOT_END'    : "%${value}"
        ]
        Map<String, String> isEmptyMap = [
                'IS_EMPTY'    : "IS NULL",
                'IS_NOT_EMPTY': "IS NOT NULL",
        ]
        if (iLikePropertiesMap.containsKey(operatorKey)) {
            sqlRestriction("UPPER(json_value(${fieldColumnJson}, '\$.$fieldName')) LIKE UPPER('${iLikePropertiesMap.get(operatorKey)}')")
        } else if (notILikePropertiesMap.containsKey(operatorKey)) {
            sqlRestriction("UPPER(json_value(${fieldColumnJson}, '\$.$fieldName')) NOT LIKE UPPER('${notILikePropertiesMap.get(operatorKey)}')")
        } else if (operatorKey == "IS_EMPTY" || operatorKey == "IS_NOT_EMPTY") {
            sqlRestriction("json_value(${fieldColumnJson}, '\$.$fieldName') ${isEmptyMap.get(operatorKey)}")
        } else if (value.isNumber()) {
            sqlRestriction("json_value(${fieldColumnJson}, '\$.$fieldName') ${operatorField} ${value}")
        } else {
            sqlRestriction("json_value(${fieldColumnJson}, '\$.$fieldName') ${operatorField} '${value}'")
        }
    }

    Map getAlertFilterCountAndList(AlertDataDTO alertDataDTO, String callingScreen = null, Boolean isArchived = false) {
        Integer totalCount = 0
        Integer totalFilteredCount = 0
        List alertList = []
        List resultList = []
        List fullCaseList = []
        List<Disposition> openDispositions = getDispositionsForName(alertDataDTO.dispositionFilters)
        Closure advancedFilterClosure
        advancedFilterClosure = generateAdvancedFilterClosure(alertDataDTO, advancedFilterClosure)
        if (alertDataDTO.params.callingScreen == Constants.Commons.TRIGGERED_ALERTS && !alertDataDTO.params.adhocRun.toBoolean() && alertDataDTO.domainName == AggregateCaseAlert) {
            totalCount = 0
            totalFilteredCount = 0
        } else {
            if(!alertDataDTO.isFromExport){
                totalCount = getTotalCount(alertDataDTO)
            }
            alertList = generateAlertList(advancedFilterClosure, alertDataDTO, openDispositions)
            totalFilteredCount = alertList.totalCount

            if (alertDataDTO.isFullCaseList) {
                fullCaseList = alertList.collect { [caseNumber: it.caseNumber, caseVersion: it.caseVersion, alertId: it.id, followUpNumber: it.followUpNumber ?: 0] }
            }

            if (!alertList.isEmpty()) {
                resultList = getResultList(alertList, alertDataDTO, callingScreen, isArchived)
            }
        }

        [totalCount: totalCount, totalFilteredCount: totalFilteredCount, resultList: resultList, fullCaseList: fullCaseList]
    }


    Integer getTotalCount(AlertDataDTO alertDataDTO) {
        if (alertDataDTO.params.callingScreen == Constants.Commons.REVIEW && !(alertDataDTO.domainName in [EvdasAlert, ArchivedEvdasAlert]) && !alertDataDTO.params.isCaseSeries?.toBoolean()) {
            return alertDataDTO.executedConfiguration.dispCounts ? new JsonSlurper().parseText(alertDataDTO.executedConfiguration.dispCounts).values().sum() : 0
        } else {
            return alertDataDTO.domainName.createCriteria().get {
                projections {
                    count('id')
                }
                callingScreenClosure.delegate = delegate
                callingScreenClosure(alertDataDTO)
            }
        }
    }

    List getResultList(List alertList, AlertDataDTO alertDataDTO, String callingScreen, Boolean isArchived) {
        if (alertDataDTO.domainName in [ArchivedSingleCaseAlert, SingleCaseAlert]) {
            return singleCaseAlertService.getSingleCaseAlertList(alertList, alertDataDTO, callingScreen, isArchived)
        } else if (alertDataDTO.domainName in [ArchivedAggregateCaseAlert, AggregateCaseAlert]) {
            return alertDataDTO.isJader ? jaderAlertService.fetchResultAlertListJader(alertList, alertDataDTO, callingScreen) : aggregateCaseAlertService.fetchResultAlertList(alertList, alertDataDTO, callingScreen)
        } else if (alertDataDTO.domainName in [ArchivedEvdasAlert, EvdasAlert]) {
            return evdasAlertService.fetchEvdasAlertList(alertList, alertDataDTO)
        }
        return []
    }

    List getAlertFilterIdList(AlertDataDTO alertDataDTO) {
        List alertIdList = []
        List<Disposition> openDispositions = getDispositionsForName(alertDataDTO.dispositionFilters)
        Closure advancedFilterClosure
        advancedFilterClosure = generateAdvancedFilterClosure(alertDataDTO, advancedFilterClosure)
        if (!(alertDataDTO.params.callingScreen == Constants.Commons.TRIGGERED_ALERTS && !alertDataDTO.params.adhocRun.toBoolean() && alertDataDTO.domainName == AggregateCaseAlert)) {
            List alertList = generateAlertList(advancedFilterClosure, alertDataDTO, openDispositions)
            alertIdList = alertList*.id
        }

        return alertIdList
    }

    Map getExportedAlertData(AlertDataDTO alertDataDTO, String callingScreen = null) {
        List alertList = []
        List resultList = []
        List<Disposition> openDispositions = getDispositionsForName(alertDataDTO.dispositionFilters)
        Closure advancedFilterClosure
        advancedFilterClosure = generateAdvancedFilterClosure(alertDataDTO, advancedFilterClosure)
        if(callingScreen == Constants.Commons.DASHBOARD) {
            alertList = generateAlertList(advancedFilterClosure, alertDataDTO, openDispositions)
            resultList.addAll(singleCaseAlertService.getSingleCaseAlertListForExport(alertList, alertDataDTO, callingScreen))
        } else {
            while (alertList = generateAlertList(advancedFilterClosure, alertDataDTO, openDispositions)) {
                resultList.addAll(singleCaseAlertService.getSingleCaseAlertListForExport(alertList, alertDataDTO, callingScreen))
                alertDataDTO.start = alertDataDTO.start + alertList.size()
                //for gc
                alertList = []
            }
        }

        [resultList: resultList]
    }

    @Synchronized
    List generateAlertList(Closure advancedFilterClosure, AlertDataDTO alertDataDTO, List<Disposition> openDispositions) {
        log.info("Generating alert list started by User: " + alertDataDTO.userId)
        //Added code for pii policy
        DbUtil.piiPolicy(sessionFactory.currentSession)
        // end code for pii policy
        List alertList = []
        Closure criteria = {
            if (alertDataDTO.visibleColumnsList) {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property("id", "id")
                    property("executedAlertConfiguration.id", "executedAlertConfigurationId")
                    property("caseId", "caseId")
                    property("caseVersion", "caseVersion")
                    property("badge", "badge")
                    property("caseNumber", "caseNumber")
                    property("followUpNumber", "followUpNumber")
                    property("globalIdentity.id", "globalIdentityId")
                    property("priority.id", "priorityId")
                    property("disposition.id", "dispositionId")
                    property("assignedTo.id", "assignedToId")
                    property("assignedToGroup.id", "assignedToGroupId")
                    property("name", "alertName")
                    exportColumns.findAll { it.key in alertDataDTO.visibleColumnsList }.each { key, value ->
                        property(value, key)
                    }
                }
            }
            if (alertDataDTO.exportCaseNarrative) {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                property("caseNarrative", "caseNarrative")
            }
            if (alertDataDTO.isProjection) {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property('caseNumber', 'caseNumber')
                    property('caseVersion', 'caseVersion')
                    property('id', 'alertId')
                }
            }
            if (alertDataDTO.isCaseFormProjection) {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property('caseNumber', 'caseNumber')
                    property('caseVersion', 'caseVersion')
                    property('id', 'alertId')
                    property('followUpNumber', 'followUpNumber')
                    property('isDuplicate', 'isDuplicate')
                    property('caseId', 'caseId')
                }
            }
            if (alertDataDTO.clipBoardCases) {
                or {
                    alertDataDTO.clipBoardCases.collate(1000).each {
                        'in'('caseNumber', it)
                    }
                }
            }
            callingScreenClosure.delegate = delegate
            filterClosure.delegate = delegate
            orderClosure.delegate = delegate
            if (advancedFilterClosure) {
                criteriaConditions.delegate = delegate
                criteriaConditionsDate.delegate = delegate
                criteriaConditionsCount.delegate = delegate
                criteriaConditionsForCLOB.delegate = delegate
                criteriaConditionsForSubGroup.delegate = delegate
                criteriaConditionsForRelSubGroup.delegate = delegate
                criteriaConditionsForSubGroupFaers.delegate = delegate
                criteriaConditionsForIntegratedReview.delegate = delegate
                criteriaConditionsForNewColumns.delegate = delegate
                criteriaConditionsForNewColumnsAdhoc.delegate = delegate
                criteriaConditionsTags.delegate = delegate
                advancedFilterClosure.delegate = delegate
                advancedFilterClosure.criteriaConditions = criteriaConditions
                advancedFilterClosure.criteriaConditionsDate = criteriaConditionsDate
                advancedFilterClosure.criteriaConditionsCount = criteriaConditionsCount
                advancedFilterClosure.criteriaConditionsForCLOB = criteriaConditionsForCLOB
                advancedFilterClosure.criteriaConditionsForSubGroup = criteriaConditionsForSubGroup
                advancedFilterClosure.criteriaConditionsForRelSubGroup = criteriaConditionsForRelSubGroup
                advancedFilterClosure.criteriaConditionsForSubGroupFaers = criteriaConditionsForSubGroupFaers
                advancedFilterClosure.criteriaConditionsForIntegratedReview = criteriaConditionsForIntegratedReview
                advancedFilterClosure.criteriaConditionsForNewColumns = criteriaConditionsForNewColumns
                advancedFilterClosure.criteriaConditionsForNewColumnsAdhoc = criteriaConditionsForNewColumnsAdhoc
                advancedFilterClosure.criteriaConditionsTags = criteriaConditionsTags
            }
            callingScreenClosure(alertDataDTO)
            if (alertDataDTO.params.advancedFilterChanged && Boolean.parseBoolean(alertDataDTO.params.advancedFilterChanged)) {
                openDispositions += cacheService.getDispositionListById(alertDataDTO.advancedFilterDispositions)
                openDispositions.unique()
            } else {
                alertDataDTO.advancedFilterDispositions = openDispositions.intersect(cacheService.getDispositionListById(alertDataDTO.advancedFilterDispositions))
            }
            if (openDispositions.size() > 0) {
                or {
                    openDispositions.collate(1000).each {
                        'in'("disposition", it)
                    }
                }
            }
            and {
                filterClosure(alertDataDTO.filterMap, alertDataDTO.domainName, alertDataDTO.isFaers, alertDataDTO.configId,alertDataDTO.execConfigId, alertDataDTO.isJader)
                if (advancedFilterClosure) {
                    advancedFilterClosure()
                }
            }
            if (alertDataDTO.cumulative && alertDataDTO.orderColumnMap && alertDataDTO.orderColumnMap.name == 'lastUpdated') {
                alertDataDTO.orderColumnMap.put("name", "id")
                alertDataDTO.orderColumnMap.put("dir", "desc")
            }
            alertDataDTO.orderColumnMap.put("isVaers", alertDataDTO.isVaers)
            alertDataDTO.orderColumnMap.put("isVigibase", alertDataDTO.isVigibase)
            alertDataDTO.orderColumnMap.put("isJader", alertDataDTO.isJader)
            orderClosure(alertDataDTO.orderColumnMap)
        }
        alertList = alertDataDTO.domainName.createCriteria().list([max: alertDataDTO.length, offset: alertDataDTO.start], criteria)
        log.info("Generating alert list Ended ")
        alertList
    }

    @Synchronized
    List generateAlertListForOnDemandRuns(Closure advancedFilterClosure, AlertDataDTO alertDataDTO) {
        List alertList = []
        //Added code for pii policy
        DbUtil.piiPolicy(sessionFactory.currentSession)
        // end code for pii policy
        Closure criteria = {
            if (alertDataDTO.isProjection) {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property('caseNumber', 'caseNumber')
                    property('caseVersion', 'caseVersion')
                    property('id', 'alertId')
                }
            }
            if (alertDataDTO.isCaseFormProjection) {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property('caseNumber', 'caseNumber')
                    property('caseVersion', 'caseVersion')
                    property('id', 'alertId')
                    property('followUpNumber', 'followUpNumber')
                    property('isDuplicate', 'isDuplicate')
                    property('caseId', 'caseId')
                }
            }
            if (alertDataDTO.clipBoardCases) {
                or {
                    alertDataDTO.clipBoardCases.collate(1000).each {
                        'in'('caseNumber', it)
                    }
                }
            }
            callingScreenClosure.delegate = delegate
            filterClosure.delegate = delegate
            orderClosure.delegate = delegate
            if (advancedFilterClosure) {
                criteriaConditions.delegate = delegate
                criteriaConditionsDate.delegate = delegate
                criteriaConditionsCount.delegate = delegate
                criteriaConditionsForCLOB.delegate = delegate
                criteriaConditionsForSubGroup.delegate = delegate
                criteriaConditionsForRelSubGroup.delegate = delegate
                criteriaConditionsForSubGroupFaers.delegate = delegate
                criteriaConditionsForIntegratedReview.delegate = delegate
                criteriaConditionsForNewColumnsAdhoc.delegate = delegate
                criteriaConditionsTags.delegate = delegate
                advancedFilterClosure.delegate = delegate
                advancedFilterClosure.criteriaConditions = criteriaConditions
                advancedFilterClosure.criteriaConditionsDate = criteriaConditionsDate
                advancedFilterClosure.criteriaConditionsCount = criteriaConditionsCount
                advancedFilterClosure.criteriaConditionsForCLOB = criteriaConditionsForCLOB
                advancedFilterClosure.criteriaConditionsForSubGroup = criteriaConditionsForSubGroup
                advancedFilterClosure.criteriaConditionsForRelSubGroup = criteriaConditionsForRelSubGroup
                advancedFilterClosure.criteriaConditionsForSubGroupFaers = criteriaConditionsForSubGroupFaers
                advancedFilterClosure.criteriaConditionsForNewColumns = criteriaConditionsForNewColumns
                advancedFilterClosure.criteriaConditionsForNewColumnsAdhoc = criteriaConditionsForNewColumnsAdhoc
                advancedFilterClosure.criteriaConditionsTags = criteriaConditionsTags
            }
            callingScreenClosure(alertDataDTO)
            and {
                filterClosure(alertDataDTO.filterMap, alertDataDTO.domainName, alertDataDTO.isFaers, alertDataDTO.configId,alertDataDTO.execConfigId, alertDataDTO.isJader)
                if (advancedFilterClosure) {
                    advancedFilterClosure()
                }
            }
            if (alertDataDTO.cumulative && alertDataDTO.orderColumnMap && alertDataDTO.orderColumnMap.name == 'lastUpdated') {
                alertDataDTO.orderColumnMap.put("name", "id")
                alertDataDTO.orderColumnMap.put("dir", "desc")
            }
            orderClosure(alertDataDTO.orderColumnMap)
        }
        alertList = alertDataDTO.domainName.createCriteria().list([max: alertDataDTO.length, offset: alertDataDTO.start], criteria)
        alertList
    }

    private String addExtraValueInCriteria(AlertDataDTO alertDataDTO, String str) {
        if (!str) {
            return str
        }
        Integer idxCriteria = str.indexOf("criteriaConditions(")
        if (idxCriteria <= 0) {
            return str
        }
        String tempString = str.substring(idxCriteria)
        Integer idxEndCriteira = tempString.indexOf(")\n") + 1
        String stringToBeUpdated = tempString.substring(0, idxEndCriteira)
        String finalString = stringToBeUpdated.substring(0, stringToBeUpdated.lastIndexOf(")")) + ",${alertDataDTO.isFaers},${alertDataDTO.configId})"
        finalString = finalString.replace("criteriaConditions", Constants.TO_REPLACE_STRING)
        str = str.substring(0, idxCriteria) + finalString + str.substring(str.indexOf(stringToBeUpdated) + stringToBeUpdated.length())
        return addExtraValueInCriteria(alertDataDTO, str).replace(Constants.TO_REPLACE_STRING, "criteriaConditions")
    }

    String escapeSpecialCharacters(String name) {
        def specialCharacters = /[-\[\]{}()*+?.\\^$|]/
        return name.replaceAll(specialCharacters) { match -> '\\' + match}
    }

    void populateAdvancedFilterDispositions(AlertDataDTO alertDataDTO, String JSONQuery) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        Map object = jsonSlurper.parseText(JSONQuery)
        Map expressionObj = object.all.containerGroups[0]
        parseExpressionObj(expressionObj, alertDataDTO)
        alertDataDTO.advancedFilterDispName = cacheService.getDispositionListById(alertDataDTO.advancedFilterDispositions)*.displayName
    }

    void parseExpressionObj(Map expressionObj, AlertDataDTO alertDataDTO) {
        if (expressionObj.containsKey('expressions')) {
            for (int i = 0; i < expressionObj.expressions.size(); i++) {
                parseExpressionObj(expressionObj.expressions[i], alertDataDTO)
            }
        } else if (expressionObj) {
            if (expressionObj.field == Constants.AdvancedFilter.DISPOSITION_ID) {
                List valueList = []
                Set uniqueDispositions = []
                Set uniqueDispositionsByName = []
                def ec = null
                if (alertDataDTO.domainName in [AggregateCaseAlert, SingleCaseAlert]) {
                    ec = ExecutedConfiguration.get(alertDataDTO.execConfigId)
                } else if (alertDataDTO.domainName in [EvdasAlert, ArchivedEvdasAlert]) {
                    ec = ExecutedEvdasConfiguration.get(alertDataDTO.execConfigId)
                }
                alertDataDTO.domainName.findAllByExecutedAlertConfiguration(ec).each {
                    uniqueDispositions.add(it?.disposition?.id)
                    uniqueDispositionsByName.add(it?.disposition?.displayName)
                }
                switch (expressionObj.op) {
                    case Constants.AdvancedFilter.EQUALS:
                        valueList = expressionObj.value ? expressionObj.value.split(";").collect { it as Long } : []
                        break
                    case 'NOT_EQUAL':
                        expressionObj.value.split(';').collect { it as Long }.each {
                            uniqueDispositions.removeAll(it)
                        }
                        break
                    case 'DOES_NOT_CONTAIN':
                        uniqueDispositions = []
                        uniqueDispositionsByName?.each { disp ->
                            if (!disp.trim().toLowerCase().contains(expressionObj.value.trim().toLowerCase())) {
                                uniqueDispositions.add(Disposition.findByDisplayName(disp as String).id)
                            }
                        }
                        break
                    case 'CONTAINS':
                        uniqueDispositions = []
                        uniqueDispositionsByName?.each { disp ->
                            if (disp.trim().toLowerCase().contains(expressionObj.value.trim().toLowerCase())) {
                                uniqueDispositions.add(Disposition.findByDisplayName(disp as String).id)
                            }
                        }
                        break
                    case 'START_WITH':
                        uniqueDispositions = []
                        uniqueDispositionsByName?.each { disp ->
                            if (disp.trim().toLowerCase().startsWith(expressionObj.value.trim().toLowerCase())) {
                                uniqueDispositions.add(Disposition.findByDisplayName(disp as String).id)
                            }
                        }
                        break
                    case 'DOES_NOT_START':
                        uniqueDispositions = []
                        uniqueDispositionsByName?.each { disp ->
                            if (!disp.trim().toLowerCase().startsWith(expressionObj.value.trim().toLowerCase())) {
                                uniqueDispositions.add(Disposition.findByDisplayName(disp as String).id)
                            }
                        }
                        break
                    case 'ENDS_WITH':
                        uniqueDispositions = []
                        uniqueDispositionsByName?.each { disp ->
                            if (disp.trim().toLowerCase().endsWith(expressionObj.value.trim().toLowerCase())) {
                                uniqueDispositions.add(Disposition.findByDisplayName(disp as String).id)
                            }
                        }
                        break
                    case 'DOES_NOT_END':
                        uniqueDispositions = []
                        uniqueDispositionsByName?.each { disp ->
                            if (!disp.trim().toLowerCase().endsWith(expressionObj.value.trim().toLowerCase())) {
                                uniqueDispositions.add(Disposition.findByDisplayName(disp as String).id)
                            }
                        }
                        break
                    case 'IS_EMPTY':
                        uniqueDispositions = []
                        break
                    default:
                        uniqueDispositions
                        break
                }
                valueList = !valueList ? uniqueDispositions?.toList() : valueList
                alertDataDTO.advancedFilterDispositions.addAll(valueList)
            }
        }
    }

    Closure generateAdvancedFilterClosure(AlertDataDTO alertDataDTO, Closure advancedFilterClosure) {
        Long exeConfigId = alertDataDTO.execConfigId
        String criteria
        JsonSlurper jsonSlurper = new JsonSlurper()
        if (alertDataDTO.params.advancedFilterId) {
            AdvancedFilter advancedFilter = AdvancedFilter.get(alertDataDTO.params.advancedFilterId as Long)
            GroovyShell groovyShell = new GroovyShell()
            criteria = advancedFilter.criteria.toString()
            Map object = jsonSlurper.parseText(advancedFilter?.JSONQuery)
            Map expressionObj = object?.all?.containerGroups[0]?.expressions[0]
            if (criteria.contains("batchLotNo")) {
                criteria = criteria.replace("exConfigId", "${exeConfigId}")
            }
            if (criteria.contains("productName") && (expressionObj?.op == "CONTAINS" || expressionObj?.op == "DOES_NOT_CONTAIN")) {
                criteria = criteria.replace("productName", "productId")
            }
            String newCriteriaString = addExtraValueInCriteria(alertDataDTO, criteria)
            newCriteriaString = newCriteriaString.replace("exConfigId", "${exeConfigId}")
            advancedFilterClosure = groovyShell.evaluate(newCriteriaString) as Closure
            populateAdvancedFilterDispositions(alertDataDTO, advancedFilter.JSONQuery)
        } else if (alertDataDTO.params.queryJSON) {
            criteria = advancedFilterService.createAdvancedFilterCriteria(alertDataDTO.params.queryJSON, exeConfigId,alertDataDTO.domainName)
            criteria = criteria.replace("exConfigId", "${exeConfigId}")
            Map object = jsonSlurper.parseText(alertDataDTO?.params?.queryJSON)
            Map expressionObj = object?.all?.containerGroups[0]?.expressions[0]
            if (criteria.contains("productName") && (expressionObj?.op == "CONTAINS" || expressionObj?.op == "DOES_NOT_CONTAIN")) {
                criteria = criteria.replace("productName", "productId")
            }
            GroovyShell groovyShell = new GroovyShell()
            String newCriteriaString = addExtraValueInCriteria(alertDataDTO, criteria)
            advancedFilterClosure = groovyShell.evaluate(newCriteriaString) as Closure
            populateAdvancedFilterDispositions(alertDataDTO, alertDataDTO.params.queryJSON)
        }
        if (advancedFilterClosure) {
            log.info("Advance filter closure : " + criteria)
        }
        advancedFilterClosure
    }

    private List getValuesList(String value, String fieldName) {
        List valuesList = value.split(";")
        valuesList = valuesList.collect {
            if (fieldName in grailsApplication.config.advancedFilter.string.fields) {
                return it as String
            } else if (StringUtils.isNumeric(it)) {
                return it as Long
            } else if (StringUtils.equalsIgnoreCase(it, "true") || StringUtils.equalsIgnoreCase(it, "false")) {
                return Boolean.parseBoolean(it)
            } else if (fieldName == Constants.AdvancedFilter.ASSIGNED_TO_ID && value == Constants.AdvancedFilter.CURRENT_USER_ID) {
                return userService.getUser().id
            } else if (fieldName == Constants.AdvancedFilter.ASSIGNED_TO_GROUP_ID && value == Constants.AdvancedFilter.CURRENT_GROUP_ID) {
                return userService.getUser().groups*.id
            } else {
                return it as String
            }
        }
        valuesList?.flatten()
    }

    private List getValuesListForClobFields(String value) {
        List valuesList = value.split(";")
        valuesList
    }

    void preparePossibleValuesMap(def domainName, Map<String, List> possibleValuesMap, Long executedConfigId) {
        possibleValuesMap.put("priority.id", priorityService.listPriorityAdvancedFilter())
        List impEventsList = [[id: 'ime', text: Holders.config.importantEvents.ime.abbreviation.toUpperCase()], [id: 'dme', text: Holders.config.importantEvents.dme.abbreviation.toUpperCase()],
                              [id: 'ei', text: Holders.config.importantEvents.stopList.abbreviation.toUpperCase()], [id: 'sm', text: Holders.config.importantEvents.specialMonitoring.abbreviation.toUpperCase()]]
        possibleValuesMap.put("aggImpEventList", impEventsList)
        possibleValuesMap.put("evImpEventList", impEventsList)
        possibleValuesMap.put("disposition.id", dispositionService.listDispositionAdvancedFilter())
        possibleValuesMap.put("signal", validatedSignalService.listSignalAdvancedFilter())
        possibleValuesMap.put("assignedTo.id", getAllUsers())
        possibleValuesMap.put("assignedToGroup.id", getAllGroups())
        getDistinctValuesOfStringFields(domainName, possibleValuesMap, executedConfigId)
        getDistinctValuesOfListFields(domainName, possibleValuesMap, executedConfigId)
    }

    void preparePossibleValuesMapForOnDemand(def domainName, Map<String, List> possibleValuesMap, Long executedConfigId) {

        List impEventsList = [[id: 'ime', text: Holders.config.importantEvents.ime.abbreviation.toUpperCase()], [id: 'dme', text: Holders.config.importantEvents.dme.abbreviation.toUpperCase()],
                              [id: 'ei', text: Holders.config.importantEvents.stopList.abbreviation.toUpperCase()], [id: 'sm', text: Holders.config.importantEvents.specialMonitoring.abbreviation.toUpperCase()]]
        possibleValuesMap.put("aggImpEventList", impEventsList)
        possibleValuesMap.put("evImpEventList", impEventsList)
        getDistinctValuesOfStringFields(domainName, possibleValuesMap, executedConfigId)
        getDistinctValuesOfListFields(domainName, possibleValuesMap, executedConfigId)
    }

    List<Map> getAllUsers() {
        List<Map> userList = []
        userList.add([id: Constants.AdvancedFilter.CURRENT_USER_ID, text: Constants.AdvancedFilter.CURRENT_USER_TEXT])
        cacheService.getAllUsers().each { k, v ->
            userList.add([id: k, text: v.fullName])
        }
        userList
    }

    List<Map> getAllGroups() {
        List<Map> groupList = []
        groupList.add([id: Constants.AdvancedFilter.CURRENT_GROUP_ID, text: Constants.AdvancedFilter.CURRENT_GROUP_TEXT])
        groupList.add([id: "k", text: "v.name"])
        groupList.add([id: "key", text: "v.aname"])
        cacheService.getAllGroups().each { k, v ->
            groupList.add([id: k, text: v.name])
        }
        groupList
    }

    private void getDistinctValuesOfStringFields(
            def domainName, Map<String, List> possibleValuesMap, Long executedConfigId) {
        List<String> possibleFieldsList = null
        Boolean isSingleDashboard = domainName == SingleCaseAlert && executedConfigId < 0
        ExecutedConfiguration ec = ExecutedConfiguration.get(executedConfigId as Long)
        if (domainName == SingleCaseAlert) {
            possibleFieldsList = ['flags', 'productName', 'appTypeAndNum']
        } else if (domainName == SingleOnDemandAlert) {
            possibleFieldsList = ['productName', 'appTypeAndNum']
        } else if (domainName == AggregateCaseAlert) {
            possibleFieldsList = ['flags', 'productName', 'soc', 'trendType', 'freqPriority']
        } else if (domainName == AggregateOnDemandAlert) {
            possibleFieldsList = ['productName', 'soc']
        } else if (domainName == EvdasOnDemandAlert) {
            possibleFieldsList = ['substance', 'soc', 'hlgt', 'hlt', 'smqNarrow', 'changes', 'dmeIme']
        } else {
            possibleFieldsList = ['flags', 'substance', 'soc', 'hlgt', 'hlt', 'smqNarrow', 'changes', 'dmeIme']
        }
        User user = userService.getUser()
        List  groupIdList = user.groups.collect { it.id }
        if (domainName == AggregateCaseAlert) {
            def openDispositions = cacheService.getNotReviewCompletedAndClosedDisposition().collect { it.id }
            List integratedReviewColumnsString = AggregateCaseAlert.createCriteria().list {
                projections {
                    property('faersColumns')
                    property('evdasColumns')
                }
                if (executedConfigId > 0) {
                    eq("executedAlertConfiguration.id", executedConfigId)
                } else{
                    if (openDispositions.size() > 0) {
                        sqlRestriction("disposition_id in (${openDispositions.join(',')})")
                    }
                }
                or{
                    isNotNull('faersColumns')
                    isNotNull('evdasColumns')
                }
                or {
                    eq("assignedTo.id", userService.getCurrentUserId())
                    if (groupIdList.size() > 0) {
                        or {
                            groupIdList.collate(1000).each {
                                'in'('assignedToGroup.id', it)
                            }
                        }
                    }
                }
            }
            // start  - pvs-53809 changes for fetching advance filter possible values in loop instead of in once
            Map faersMap
            Map evdasMap
            List faersCols = ['freqPriorityFaers', 'impEventsFaers', "trendTypeFaers"]
            List evdasCols = ["dmeImeEvdas","hlgtEvdas", "hltEvdas", "smqNarrowEvdas",
                              "impEventsEvdas", "changesEvdas", "listedEvdas"]

            Map<String, Set<String>> integratedReviewStringCols = new HashMap<String, HashSet<String>>()
            for(def obj: integratedReviewColumnsString) {
                if(obj && obj[0]) {
                    faersMap = JSON.parse(obj[0]).findAll { key,value -> faersCols.contains(key)}
                }
                if(obj && obj[1]) {
                    evdasMap = JSON.parse(obj[1]).findAll { key, value -> evdasCols.contains(key)}
                }
                faersMap.each { key,value ->
                    if(key && value){
                        Set uniqueFaersList =  integratedReviewStringCols.get(key)
                        if(uniqueFaersList){
                            uniqueFaersList << value
                        } else {
                            uniqueFaersList = [value]
                        }
                        integratedReviewStringCols.put(key,uniqueFaersList)
                    }
                }
                evdasMap.each { key,value ->
                    if(key && value){
                        Set uniqueEvdasList =  integratedReviewStringCols.get(key)
                        if(uniqueEvdasList){
                            uniqueEvdasList << value
                        } else {
                            uniqueEvdasList = [value]
                        }
                        integratedReviewStringCols.put(key,uniqueEvdasList)
                    }
                }
            }
            possibleValuesMap << integratedReviewStringCols
            // end  - pvs-53809 changes for fetching advance filter possible values
        }
        if(!isSingleDashboard) {
            possibleFieldsList.each { String propertyName ->
                boolean isBadge = propertyName.equals(Constants.Badges.FLAGS) && domainName == SingleCaseAlert
                List<String> possibleValuesList = domainName.createCriteria().list {
                    projections {
                        if (isBadge) {
                            distinct(Constants.Badges.BADGE_TEXT)
                        } else {
                            distinct(propertyName)
                        }
                    }
                    if (executedConfigId > 0) {
                        eq("executedAlertConfiguration.id", executedConfigId)
                    } else {
                        'executedAlertConfiguration' {
                            eq('adhocRun', false)
                        }
                    }
                    if (isBadge) {
                        isNotNull(Constants.Badges.BADGE_TEXT)
                    } else {
                        isNotNull(propertyName)
                    }
                } as List<String>

                possibleValuesMap.put(propertyName, possibleValuesList.collect { [id: it, text: it] })
            }
            if(domainName == AggregateOnDemandAlert || domainName == AggregateCaseAlert) {
                List newColumnList = ['hlt', 'hlgt']
                    String tableName = ""
                    if (domainName == AggregateOnDemandAlert) {
                        tableName = "AGG_ON_DEMAND_ALERT"
                    } else if (domainName == AggregateCaseAlert) {
                        tableName = "AGG_ALERT"
                    }
                newColumnList.each { newProperty ->
                    Session session = sessionFactory.currentSession
                    try {
                        String sql = SignalQueryHelper.distinct_advance_filter_new_column_list("NEW_COUNTS_JSON", newProperty, tableName, executedConfigId)
                        SQLQuery sqlQuery = session.createSQLQuery(sql)
                        List<String> possibleValuesList = sqlQuery.list()
                        possibleValuesMap.put(newProperty, possibleValuesList.collect { [id: it, text: it] })
                    } catch (Exception ex) {
                        ex.printStackTrace()
                    } finally {
                        session.flush()
                        session.clear()
                    }
                }
            }
        }else{
            def SinglePossibleVal=singleCaseAlertService.getListListDataSql()
            def prodNames=SinglePossibleVal.productName
            def flags=SinglePossibleVal.flags
            def appTypeNum=SinglePossibleVal.appTypeNum
            possibleValuesMap.put("productName", prodNames.collect { [id: it, text: it] })
            possibleValuesMap.put("flags", flags.collect { [id: it, text: it] })
            possibleValuesMap.put("appTypeAndNum", appTypeNum.collect { [id: it, text: it] })
        }
    }


    private void getDistinctValuesOfListFields(
            def domainName, Map<String, List> possibleValuesMap, Long executedConfigId) {
        List<String> possibleFieldsList = null
        possibleFieldsList.each { String propertyName ->
            List<String> possibleValuesList = domainName.createCriteria().list {
                createAlias(propertyName, 'alias')
                projections {
                    distinct('alias.elements')
                }
                if (executedConfigId > 0) {
                    eq("executedAlertConfiguration.id", executedConfigId)
                } else {
                    'executedAlertConfiguration' {
                        eq('adhocRun', false)
                    }
                }
                isNotNull('alias.elements')
            } as List<String>
            possibleValuesMap.put(propertyName, possibleValuesList.collect { [id: it, text: it] })
        }
    }

    List<Disposition> getDispositionsForName(dispositionFilters) {
        List<Disposition> dispositionList = []
        if (dispositionFilters) {
            dispositionList = Disposition.findAllByDisplayNameInList(dispositionFilters)
        }
        dispositionList
    }

    Map prepareFilterMap(Map params, Integer totalColumns) {
        Map filterMap = [:]
        (0..totalColumns).each {
            if (params["columns[${it}][search][value]"]) {
                filterMap.put(params["columns[${it}][data]"], params["columns[${it}][search][value]"])
            }
        }
        if (params.dashboardFilter == 'assignedTo') {
            filterMap.put('assigned', userService.getCurrentUserName())
        }
        if (params.dashboardFilter == 'assignedToUser') {
            filterMap.put('assignedToUserAndGroup', userService.getCurrentUserId())
        }
        if (params.dashboardFilter == 'new') {
            filterMap.put('newDashboardFilter', true)
        }
        filterMap
    }

    Map prepareOrderColumnMap(Map params) {
        Map orderColumnMap = [:]
        def orderColumn = params["order[0][column]"]
        orderColumnMap = [name: params["columns[${orderColumn}][data]"], dir: params["order[0][dir]"]]
        orderColumnMap
    }

    Integer changeAlertLevelDisposition(Closure saveActivityAndHistory, AlertLevelDispositionDTO alertLevelDispositionDTO, Boolean isArchived = false) {
        alertLevelDispositionDTO.reviewCompletedDispIdList = getReviewCompletedDispositionList()
        alertLevelDispositionDTO.reviewCompletedDispIdList.add(alertLevelDispositionDTO.targetDisposition.id)
        alertLevelDispositionDTO.alertList = alertListByExecConfig(alertLevelDispositionDTO)
        Disposition targetDisposition = alertLevelDispositionDTO.targetDisposition
        List<Map> bulkUpdateDueDateDataList = []
        List distinctPriorityIds = alertLevelDispositionDTO.domainName.createCriteria().list {
            projections {
                groupProperty("priority.id")
            }
            eq("executedAlertConfiguration.id", alertLevelDispositionDTO.execConfigId)
        }
        Map<Long, Date> priorityDueMap = new HashMap()
        def reviewDate = new DateTime(new Date(), DateTimeZone.UTC)
        def dueDate = null
        distinctPriorityIds.each {
            Priority priority = Priority.get(it)
            PriorityDispositionConfig priorityDispositionConfig = priority?.dispositionConfigs?.find { dispositionConfig -> dispositionConfig.disposition == alertLevelDispositionDTO.targetDisposition }
            Integer reviewPeriod = priorityDispositionConfig?.reviewPeriod
            dueDate = reviewPeriod ? reviewDate.plusDays(reviewPeriod).toDate() : null
            priorityDueMap.put(priority?.id, dueDate)
        }
        Iterator<Map.Entry<Long, Date>> itr = priorityDueMap.entrySet().iterator()
        Map queryParameters = [:]
        reviewDate = reviewDate.toDate()
        Integer numberOfRowsUpdated = 0
        String currUserName = userService.getUser().fullName
        while (itr.hasNext()) {
            StringBuilder updateAlertLevelDispositionHQL = new StringBuilder()
            Map.Entry<Long, Date> entry = itr.next()
            queryParameters = [targetDispositionId: alertLevelDispositionDTO.targetDisposition.id, execConfigId: alertLevelDispositionDTO.execConfigId, priorityId: entry.getKey(), isDispChanged: true, dispPerformedBy: currUserName]
            if(!entry.getValue() && (targetDisposition.reviewCompleted || targetDisposition.closed)) {
                bulkUpdateDueDateDataList.add([isClosed: true, dueDate: null, priorityId: entry.getKey(), dueDateChange: true])
                updateAlertLevelDispositionHQL.append("update ${alertLevelDispositionDTO.domainName.getSimpleName()} set disposition.id=:targetDispositionId , dispPerformedBy =:dispPerformedBy, dueDate = null , isDispChanged =:isDispChanged , reviewDate = TO_DATE('${sqlGenerationService.convertDateToSQLDateTime(reviewDate)}', '${sqlGenerationService.DATETIME_FMT_ORA}')  where executedAlertConfiguration.id=:execConfigId AND priority.id=:priorityId")
            } else if(entry.getValue() == null) {
                bulkUpdateDueDateDataList.add([isClosed: false, dueDate: null, priorityId: entry.getKey(), dueDateChange: false])
                updateAlertLevelDispositionHQL.append("update ${alertLevelDispositionDTO.domainName.getSimpleName()} set disposition.id=:targetDispositionId , dispPerformedBy =:dispPerformedBy, isDispChanged =:isDispChanged , reviewDate = TO_DATE('${sqlGenerationService.convertDateToSQLDateTime(reviewDate)}', '${sqlGenerationService.DATETIME_FMT_ORA}')  where executedAlertConfiguration.id=:execConfigId AND priority.id=:priorityId ")
            }else {
                bulkUpdateDueDateDataList.add([isClosed: false, dueDate: dueDate, priorityId: entry.getKey(), dueDateChange: true])
                updateAlertLevelDispositionHQL.append("update ${alertLevelDispositionDTO.domainName.getSimpleName()} set disposition.id=:targetDispositionId , dispPerformedBy =:dispPerformedBy, isDispChanged =:isDispChanged , reviewDate = TO_DATE('${sqlGenerationService.convertDateToSQLDateTime(reviewDate)}', '${sqlGenerationService.DATETIME_FMT_ORA}') , dueDate = TO_DATE('${sqlGenerationService.convertDateToSQLDateTime(entry.getValue())}', '${sqlGenerationService.DATETIME_FMT_ORA}') where executedAlertConfiguration.id=:execConfigId AND priority.id = :priorityId")
            }
            if (alertLevelDispositionDTO.reviewCompletedDispIdList) {
                updateAlertLevelDispositionHQL.append(" AND disposition.id not in (:reviewCompletedDispIdList) ")
                queryParameters.put('reviewCompletedDispIdList', alertLevelDispositionDTO.reviewCompletedDispIdList)
            }
            def ec = alertLevelDispositionDTO.execConfig ?: alertLevelDispositionDTO.evdasalertConfiguration
            signalAuditLogService.createAuditLog([
                    entityName : Constants.AuditLog.domainToEntityMap.get(alertLevelDispositionDTO.domainName.getSimpleName()),
                    moduleName : Constants.AuditLog.domainToEntityMap.get(alertLevelDispositionDTO.domainName.getSimpleName()),
                    category   : AuditTrail.Category.UPDATE.toString(),
                    entityValue: ec.getInstanceIdentifierForAuditLog(),
                    description: "Changes for Alert Level Disposition"
            ] as Map, [[propertyName: "Alert Level Disposition", oldValue: "", newValue: targetDisposition.displayName],
                       [propertyName: "Justification", oldValue: "", newValue: alertLevelDispositionDTO.justificationText]] as List)

            numberOfRowsUpdated += alertLevelDispositionDTO.domainName.executeUpdate(updateAlertLevelDispositionHQL.toString(), queryParameters)
        }
        saveActivityAndHistory(alertLevelDispositionDTO, isArchived, bulkUpdateDueDateDataList)
        return numberOfRowsUpdated
    }

    //This is for the updation of Literature Alerts Disposition
    Integer changeLiteratureAlertLevelDisposition(Closure saveLiteratureActivity, AlertLevelDispositionDTO alertLevelDispositionDTO, Boolean isArchived = false) {
        alertLevelDispositionDTO.reviewCompletedDispIdList = getReviewCompletedDispositionList()
        alertLevelDispositionDTO.reviewCompletedDispIdList.add(alertLevelDispositionDTO.targetDisposition.id)
        alertLevelDispositionDTO.workflowGroupId = alertLevelDispositionDTO.loggedInUser?.workflowGroup?.id
        alertLevelDispositionDTO.alertList = alertList(alertLevelDispositionDTO)
        def ec = alertLevelDispositionDTO.execConfig ?: alertLevelDispositionDTO.evdasalertConfiguration
        signalAuditLogService.createAuditLog([
                entityName : Constants.AuditLog.domainToEntityMap.get(alertLevelDispositionDTO.domainName.getSimpleName()),
                moduleName : Constants.AuditLog.domainToEntityMap.get(alertLevelDispositionDTO.domainName.getSimpleName()),
                category   : AuditTrail.Category.UPDATE.toString(),
                entityValue: ec?.getInstanceIdentifierForAuditLog(),
                description: "Changes for Alert Level Disposition"
        ] as Map, [[propertyName: "Alert Level Disposition", oldValue: "", newValue: alertLevelDispositionDTO.targetDisposition.displayName],
                   [propertyName: "Justification", oldValue: "", newValue: alertLevelDispositionDTO.justificationText]] as List)
        saveLiteratureActivity(alertLevelDispositionDTO, isArchived)
        String currUserName = userService.getUser().fullName
        Map queryParameters = [targetDispositionId      : alertLevelDispositionDTO.targetDisposition.id,
                               execConfigId             : alertLevelDispositionDTO.execConfigId,
                               reviewCompletedDispIdList: alertLevelDispositionDTO.reviewCompletedDispIdList, id: alertLevelDispositionDTO.loggedInUser.id,
                               assignedToGroupList      : alertLevelDispositionDTO.assignedToGroup,
                               workflowGroupId          : alertLevelDispositionDTO.workflowGroupId, isArchived: isArchived, isDispChanged: 1, dispPerformedBy: currUserName]
        Sql sql = new Sql(dataSource)
        String sqlStatement = SignalQueryHelper.update_literature_alert_level_disposition(queryParameters)
        Integer updatedRowsCount = sql.executeUpdate(sqlStatement)
        sql.close()
        if (alertLevelDispositionDTO.targetDisposition.reviewCompleted && updatedRowsCount) {
            ExecutedLiteratureConfiguration.executeUpdate("Update ExecutedLiteratureConfiguration set requiresReviewCount='0' where id=:id",
                    [id: alertLevelDispositionDTO.execConfigId])
        }
        updatedRowsCount
    }

    List<Map> alertListByExecConfig(AlertLevelDispositionDTO alertLevelDispositionDTO) {

        Map<String, String> domainPropertyMap = getDomainPropertyMap(alertLevelDispositionDTO.domainName)

        List<Map> alertList = alertLevelDispositionDTO.domainName.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            createAlias("assignedTo", "assignedTo", JoinType.LEFT_OUTER_JOIN)
            createAlias("assignedToGroup", "assignedToGroup", JoinType.LEFT_OUTER_JOIN)
            projections {
                'disposition' {
                    property("id", 'dispositionId')
                    property("displayName", 'dispositionDisplayName')
                }
                property("assignedTo.id", 'assignedToId')
                property("assignedToGroup.id", 'assignedToGroupId')
                'priority' {
                    property("id", 'priorityId')
                }
                'alertConfiguration' {
                    property("productSelection", 'productSelection')
                    property("eventSelection", 'eventSelection')
                    property("id", 'alertConfigurationId')
                }
                'executedAlertConfiguration' {
                    property("id", 'executedAlertConfigurationId')
                }
                domainPropertyMap.each { key, value ->
                    property(key, value)
                }
            }
            eq('executedAlertConfiguration.id', alertLevelDispositionDTO.execConfigId)
            if (alertLevelDispositionDTO.reviewCompletedDispIdList) {
                not {
                    'in'('disposition.id', alertLevelDispositionDTO.reviewCompletedDispIdList)
                }
            }
        } as List<Map>
        alertList
    }

    List<Map> alertList(AlertLevelDispositionDTO alertLevelDispositionDTO) {

        Map<String, String> domainPropertyMap = getDomainPropertyMap(alertLevelDispositionDTO.domainName)

        List<Map> alertList = alertLevelDispositionDTO.domainName.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            createAlias("assignedTo", "assignedTo", JoinType.LEFT_OUTER_JOIN)
            createAlias("assignedToGroup", "assignedToGroup", JoinType.LEFT_OUTER_JOIN)
            projections {
                'disposition' {
                    property("displayName", 'dispositionDisplayName')
                }
                'litSearchConfig' {
                    property("productSelection", 'productSelection')
                    property("eventSelection", 'eventSelection')
                }
                domainPropertyMap.each { key, value ->
                    property(key, value)
                }
            }
            eq('exLitSearchConfig.id', alertLevelDispositionDTO.execConfigId)
            if (alertLevelDispositionDTO.reviewCompletedDispIdList) {
                not {
                    'in'('disposition.id', alertLevelDispositionDTO.reviewCompletedDispIdList)
                }
            }
        } as List<Map>
        alertList
    }

    private Map<String, String> getDomainPropertyMap(def domain) {
        Map domainPropertyMap = ["id": "id", "priority": "priority", "disposition": "disposition", "assignedTo": "assignedTo", "assignedToGroup": "assignedToGroup",
        ]
        if (domain in [AggregateCaseAlert, ArchivedAggregateCaseAlert]) {
            domainPropertyMap << ["productName": "productName", "prrValue": "prrValue", "rorValue": "rorValue", "ebgm": "ebgm", "eb05": "eb05", "eb95": "eb95", "periodEndDate": "periodEndDate", "pt": "pt", "dueDate": "dueDate"
            ]
        } else if (domain in [SingleCaseAlert, ArchivedSingleCaseAlert]) {
            domainPropertyMap << [
                    "caseNumber"   : "caseNumber", "pt": "pt", "followUpNumber": "followUpNumber",
                    "productFamily": "productFamily", "caseVersion": "caseVersion", "productName": "productName",
                    "dueDate"      : "dueDate"
            ]
        } else if (domain in [EvdasAlert, ArchivedEvdasAlert]) {
            domainPropertyMap << [
                    "periodEndDate": "periodEndDate", "substance": "substance", "pt": "pt", "dueDate": "dueDate"
            ]
        } else if (domain in [LiteratureAlert, ArchivedLiteratureAlert]) {
            domainPropertyMap << [
                    "searchString": "searchString", "articleId": "articleId", "exLitSearchConfig": "exLitSearchConfig", "litSearchConfig.id": "litSearchConfig"
            ]
        }
        domainPropertyMap
    }

    private List<Long> getReviewCompletedDispositionList() {
        List<Long> reviewCompletedDispIdList = Disposition.createCriteria().list {
            projections {
                property('id')
            }
            eq('reviewCompleted', true)
        } as List<Long>
        reviewCompletedDispIdList
    }

    Activity createActivityForBulkDisposition(Map alertMap, AlertLevelDispositionDTO alertLevelDispositionDTO) {
        alertMap.caseNumber = alertMap.containsKey("caseNumber") ? alertMap.get("caseNumber") : null
        alertMap.details = messageSource.getMessage("alert.level.disposition.change", [alertMap.dispositionDisplayName, alertLevelDispositionDTO.targetDisposition.displayName] as Object[], Locale.default)
        alertMap.attrs = [product: getNameFieldFromJson(alertMap.productSelection),
                          event  : getNameFieldFromJson(alertMap.eventSelection)]
        activityService.createActivityAlertLevelDisposition(alertMap, alertLevelDispositionDTO)
    }

    //Literature Specific
    LiteratureActivity createLiteratureActivityForBulkDisposition(Map alertMap, AlertLevelDispositionDTO alertLevelDispositionDTO) {
        alertMap.details = messageSource.getMessage("alert.level.disposition.change", [alertMap.dispositionDisplayName, alertLevelDispositionDTO.targetDisposition.displayName] as Object[], Locale.default)
        alertMap.attrs = [productName: getNameFieldFromJson(alertMap.productSelection),
                          pt  : getNameFieldFromJson(alertMap.eventSelection)]
        literatureActivityService.createLiteratureActivityAlertLevelDisposition(alertMap, alertLevelDispositionDTO)
    }

//Return List of JSON Objects with Tag Names
    String getAlertTagNames(Long id, Map<Long, String> tagsNameMap) {
        List tagList = []
        if (tagsNameMap.containsKey(id)) {
            String[] tagNames = tagsNameMap.get(id)?.split('@@@')
            tagNames.each { tagName ->
                Map tagObj = ["name": tagName]
                tagList.add(tagObj as JSON)
            }
        }
        tagList as String
    }

    void batchPersistExecConfigActivityMapping(Long exConfigId, List<Long> activityIdList, String insertExConfigActivityQuery) {
        Session session = sessionFactory.currentSession
        session.doWork(new Work() {
            void execute(Connection connection) throws SQLException {
                PreparedStatement preparedStatement = connection.prepareStatement(insertExConfigActivityQuery)
                int batchSize = Holders.config.signal.batch.size
                int count = 0
                try {
                    activityIdList.each {
                        preparedStatement.setLong(1, exConfigId)
                        preparedStatement.setLong(2, it)
                        preparedStatement.addBatch()
                        count += 1
                        if (count == batchSize) {
                            preparedStatement.executeBatch()
                            count = 0
                        }
                    }
                    preparedStatement.executeBatch()
                } catch (Exception e) {
                    log.error(e.getMessage())
                } finally {
                    preparedStatement.close()
                    session.flush()
                    session.clear()
                }
            }
        })
    }

    void persistActivityAndPEHistory(Map map) {
        updateDispCountsForExecutedConfiguration(map.execDispCountMap, map.prevDispCountMap)
        List<Map> activityExecConfIdsMap
        List<Long> activityIdList
        if (map.activityList) {
            String insertExConfigActivityQuery = "INSERT INTO ex_rconfig_activities(EX_CONFIG_ACTIVITIES_ID,ACTIVITY_ID) VALUES(?,?)"
            if (map.containsKey("isBulkUpdate")) {
                activityExecConfIdsMap = activityService.batchPersistBulkUpdateActivity(map.activityList)
                batchPersistBulkUpdateExecConfigActivityMapping(activityExecConfIdsMap, insertExConfigActivityQuery)
            } else {
                activityIdList = activityService.batchPersistAlertLevelActivity(map.activityList)
                batchPersistExecConfigActivityMapping(map.id, activityIdList, insertExConfigActivityQuery)
            }
        }
        List<Long> savedPEHistoryIds = []
        productEventHistoryService.batchPersistPEHistory(map.peHistoryList, savedPEHistoryIds)
        savedPEHistoryIds = savedPEHistoryIds.flatten()
        if(savedPEHistoryIds && map.peHistoryList[0]?.change == "DISPOSITION"){
            saveAlertCaseHistory(savedPEHistoryIds)
        }
        syncActivitiesAndPEHistoryTimestamp(activityExecConfIdsMap, activityIdList, savedPEHistoryIds)

    }

    void syncActivitiesAndPEHistoryTimestamp(List<Map> activityExecConfIdsMap = [],List<Long> activityIdList = [], List<Long> savedPEHistoryIds = []){
        try {
            List <Long> activitiesIds
            if(activityExecConfIdsMap){
                activitiesIds = activityExecConfIdsMap.collect{it.activityId}
            }
            if(activityIdList){
                activitiesIds = activityIdList
            }
            if(savedPEHistoryIds && activitiesIds){
                List<ProductEventHistory> savedPEHistory = ProductEventHistory.findAllByIdInList(savedPEHistoryIds)
                List<Activity> savedActivities = Activity.findAllByIdInList(activitiesIds)
                savedPEHistory.each{ def peh ->
                    Activity activity = savedActivities.find { def act ->
                        act.suspectProduct == peh.productName && act.eventName == peh.eventName
                    }
                    if (activity){
                        activity.timestamp = peh.dateCreated
                        activity.save(flush:true)
                    }

                }
            }
        } catch (Exception ex) {
            log.error('Error in syncing Case history and Activity timestamp.')
            ex.printStackTrace()
        }
    }

    void saveAlertCaseHistory(List<Long> savedPEHistoryIds) {
        def size = Holders.config.signal.batch.size

        Sql sql = null
        try {
            List<ProductEventHistory> savedPEHistory = ProductEventHistory.findAllByIdInList(savedPEHistoryIds)
            if(savedPEHistory){
                sql = new Sql(dataSource)
                sql.withBatch(size, "update AGG_ALERT set justification = :val0, disp_Last_Change = :val1, disp_Performed_By = :val2, is_Disp_Changed=:val3 " +
                        "where id = :val4".toString(), { preparedStatement ->
                    savedPEHistory.each { def obj ->
                        if (obj.aggCaseAlertId) {
                            preparedStatement.addBatch(val0: obj.justification, val1: obj.dateCreated?  new Timestamp(obj.dateCreated?.time):null,
                                    val2: obj.modifiedBy, val3: 1 , val4: obj.aggCaseAlertId)
                        }
                    }
                })
                sql.withBatch(size, "update ARCHIVED_AGG_ALERT set justification = :val0, disp_Last_Change = :val1, disp_Performed_By = :val2, is_Disp_Changed=:val3 " +
                        "where id = :val4".toString(), { preparedStatement ->
                    savedPEHistory.each { def obj ->
                        if (obj.archivedAggCaseAlertId) {
                            preparedStatement.addBatch(val0: obj.justification, val1: obj.dateCreated?  new Timestamp(obj.dateCreated?.time):null,
                                    val2: obj.modifiedBy, val3: 1 , val4: obj.archivedAggCaseAlertId)
                        }
                    }
                })

            }
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sql?.close()
        }
    }

    void persistActivityAndEvdasHistory(Map map) {
        updateDispCountsForExecutedConfigurationEvdas(map.execDispCountMap, map.prevDispCountMap)
        if (map.activityList) {
            String insertExConfigActivityQuery = "INSERT INTO EX_EVDAS_CONFIG_ACTIVITIES(EX_EVDAS_CONFIG_ID,ACTIVITY_ID) VALUES(?,?)"
            if (map.containsKey("isBulkUpdate")) {
                List<Map> activityExecConfIdsMap = activityService.batchPersistBulkUpdateActivity(map.activityList)
                batchPersistBulkUpdateExecConfigActivityMapping(activityExecConfIdsMap, insertExConfigActivityQuery)
            } else {
                List<Long> activityIdList = activityService.batchPersistAlertLevelActivity(map.activityList)
                batchPersistExecConfigActivityMapping(map.id, activityIdList, insertExConfigActivityQuery)
            }
        }
        evdasHistoryService.batchPersistEvdasHistory(map.evHistoryList)
    }

    void persistActivity(List activityList) {
        literatureActivityService.batchPersistAlertLevelActivity(activityList)
    }

    void persistLiteratureActivityAndHistories(Map map) {
        persistActivity(map.activityList)

        if (map.existingLiteratureHistories.size() > 0) {
            batchPersistAlertHistory(map.existingLiteratureHistories, LiteratureHistory)
        }
        batchPersistAlertHistory(map.literatureHistories, LiteratureHistory)
        List splittedData = []
        Integer workerCnt = Holders.config.signal.worker.count as Integer

        (0..workerCnt).each {
            splittedData << []
        }

        for (int idx = 0; idx < map.literatureHistories.size(); idx++) {
            LiteratureHistory history = map.literatureHistories[idx]
            splittedData[idx % workerCnt].add(history)
        }

    }

    void batchPersistAlertHistory(List histories, Class domain) throws Exception {
        domain.withTransaction {
            def batch = []
            for (def history : histories) {
                batch += history
                Session session = sessionFactory.currentSession
                if (batch.size() > Holders.config.signal.batch.size) {
                    for (def historyObj in batch) {
                        historyObj.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                    batch.clear()
                }
            }

            if (batch) {
                try {
                    Session session = sessionFactory.currentSession
                    for (def historyObj in batch) {
                        historyObj.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                } catch (Throwable th) {
                    th.printStackTrace()
                }
            }
        }
        log.info("Data is persisted.")
    }

    void persistActivityAndCaseHistories(Map map) {
        updateDispCountsForExecutedConfiguration(map.execDispCountMap, map.prevDispCountMap)
        if (map.activityList) {
            String insertExConfigActivityQuery = "INSERT INTO ex_rconfig_activities(EX_CONFIG_ACTIVITIES_ID,ACTIVITY_ID) VALUES(?,?)"
            if (map.containsKey("isBulkUpdate")) {
                List<Map> activityExecConfIdsMap = activityService.batchPersistBulkUpdateActivity(map.activityList)
                batchPersistBulkUpdateExecConfigActivityMapping(activityExecConfIdsMap, insertExConfigActivityQuery)
            } else {
                List<Long> activityIdList = activityService.batchPersistAlertLevelActivity(map.activityList)
                batchPersistExecConfigActivityMapping(map.id, activityIdList, insertExConfigActivityQuery)
            }
        }
        if (map.caseHistoryList) {
            persistCaseHistories(map)
        }
    }

    void updateDispCountsForExecutedConfiguration(Map<Long, Map<String, Integer>> execDispCountMap, Map<String, Integer> prevDispCountMap) {
        log.info("Update Disp Counts for Executed Configuration")
        List requiresReviewDispList = cacheService.getNotReviewCompletedDisposition().collect { it.id as String }
        if (execDispCountMap && prevDispCountMap) {
            JsonSlurper jsonSlurper = new JsonSlurper()
            Sql sql = new Sql(signalDataSourceService.getReportConnection("dataSource"))
            sql.withBatch(100, "UPDATE EX_RCONFIG SET disp_counts = :dispCounts, requires_review_count=:requiresReviewCount WHERE ID = :id", { preparedStatement ->
                execDispCountMap.each { Long execConfigId, Map<String, Integer> dispCountMap ->
                    ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(execConfigId)
                    Map prevDispStatusMap = jsonSlurper.parseText(executedConfiguration.dispCounts) as Map
                    Map prevDispCountMaps = [:]
                    prevDispCountMap.each { k, v ->
                        if (v.getClass() == java.util.LinkedHashMap) {
                            v.each { key, value ->
                                if (prevDispCountMaps.containsKey(key)) {
                                    prevDispCountMaps.put(key, prevDispCountMaps.get(key) + value)
                                } else {
                                    prevDispCountMaps.put(key, value)
                                }
                            }
                        }
                    }
                    Map resultMap = mergeDispReviewCountMaps(dispCountMap, prevDispStatusMap, prevDispCountMaps)
                    int requiresReviewCount = 0
                    Map requiresReviewCountMap = resultMap?.findAll { it.key in requiresReviewDispList }
                    if (requiresReviewCountMap) {
                        requiresReviewCount = requiresReviewCountMap.values().sum()
                    }
                    preparedStatement.addBatch(id: execConfigId, dispCounts: new JsonBuilder(resultMap.findAll {
                        it.value != 0
                    }).toPrettyString(),
                            requiresReviewCount: requiresReviewCount ? requiresReviewCount.toString() : "0")
                }
            })
        }
        log.info("Disp Counts for Executed Configuration updated")
    }

    void persistCaseHistories(Map map) {
        if (map.existingCaseHistoryList) {
            caseHistoryService.bulkUpdateCaseHistoryByHQL(map.existingCaseHistoryList)
        }
        caseHistoryService.batchPersistCaseHistory(map.caseHistoryList)
    }

    List<Map> getTotalCountList(def domainName, List<Long> execConfigIdList) {
        List<Map> totalCountList = []
        Sql sql
        String exConfigIds = execConfigIdList?.join(',')
        if(exConfigIds){
            try{
                sql = new Sql(signalDataSourceService.getReportConnection("dataSource"))
                String searchString
                if(domainName == SingleOnDemandAlert){
                    searchString = 'select count(*) as cnt, EXEC_CONFIG_ID as id from single_on_demand_alert where EXEC_CONFIG_ID in ('+exConfigIds+') group by EXEC_CONFIG_ID'
                } else {
                    searchString = 'select count(*) as cnt, exec_configuration_id as id from agg_on_demand_alert where exec_configuration_id in ('+exConfigIds+') group by exec_configuration_id'
                }
                sql.eachRow(searchString){row ->
                    totalCountList << [cnt:row.cnt, id:row.id]
                }
            } catch (Exception ex){
                ex.printStackTrace()
            } finally {
                sql?.close()
            }
        }
        totalCountList
    }

    Integer getTotalCountsForExecConfig(String execDispCounts) {
        execDispCounts ? new JsonSlurper().parseText(execDispCounts).values().sum() : 0
    }

    Integer getDispCountsForExecConfig(String execDispCounts, List<String> dispositionList) {
        execDispCounts ? new JsonSlurper().parseText(execDispCounts).findAll {
            it.key in dispositionList
        }.values().sum() ?: 0 : 0
    }

    List<Map> getDispositionCountList(def domainName, List<Long> execConfigIdList, List<Long> dispositionList) {
        List<Map> dispositionCountList = domainName.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                groupProperty("executedAlertConfiguration.id", "id")
                count("id", "cnt")
            }
            'in'("disposition.id", dispositionList)
            if (execConfigIdList) {
                or {
                    execConfigIdList.collate(1000).each {
                        'in'("executedAlertConfiguration.id", it)
                    }
                }
            }

        }
        dispositionCountList
    }

    List<Map> getAssignedToCountList(def domainName, List<Long> execConfigIdList, List<Long> dispositionList, Long userId) {
        List<Map> assignedToCountList = []
        List<Long> groupIds = User.get(userId)?.groups?.collect { it.id }
        assignedToCountList = domainName.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                groupProperty("executedAlertConfiguration.id", "id")
                count("id", "cnt")
            }
            'in'("disposition.id", dispositionList)
            'or' {
                'eq'("assignedTo.id", userId)
                if (groupIds?.size() > 0) {
                    or {
                        groupIds.collate(1000).each {
                            'in'('assignedToGroup.id', it)
                        }
                    }
                }
            }
            if (execConfigIdList) {
                or {
                    execConfigIdList.collate(1000).each {
                        'in'("executedAlertConfiguration.id", it)
                    }
                }
            }

        }
        assignedToCountList
    }

    List<String> fetchAllowedProductsForConfiguration() {
        User user = userService.getUserFromCacheByUsername(userService.getCurrentUserName())
        List<String> allowedProducts = productBasedSecurityService.allAllowedProductForUser(user)
        List<String> productUsedList = []
        if (allowedProducts) {
            productUsedList = allowedProducts.intersect(cacheService.getProductsUsedSet() as List)
        }
        productUsedList
    }

    List<Long> getListOfIdsWithProductSecurity(String type, Boolean isAdhocRun, Long workflowGroupId) {
        User user = userService.getUserFromCacheByUsername(userService.getCurrentUserName())
        List<String> configurationNameList = Configuration.createCriteria().list {
            projections {
                property("name")
            }
            'or' {
                eq('executing', true)
                eq('isDeleted', true)
            }
        }


        Map listOfIdsAndProductSelection = ExecutedConfiguration.createCriteria().list {
            projections {
                property("id")
                property("productSelection")
            }
            if (type) {
                eq("type", type)
            }
            eq("isLatest", true)
            eq("isDeleted", false)
            eq("isCaseSeries", false)
            eq("adhocRun", isAdhocRun)
            eq("isEnabled", true)
            eq("workflowGroup.id", workflowGroupId)
            if (configurationNameList.size() > 0) {
                not {
                    and {
                        configurationNameList.collate(1000).each {
                            'in'("name", it)
                        }
                    }
                }
            }

        }.inject([:]
                , { sum, nextItem ->
            if (sum[nextItem[1]]) {
                sum[nextItem[1]].add(nextItem[0] as Long)
            } else {
                sum[nextItem[1]] = [nextItem[0]]
            }

            sum
        })

        List<Long> list = []
        if (Holders.config.pvsignal.product.based.security) {
            List<String> allowedProductsToUser = productBasedSecurityService.allAllowedProductIdsForUser(user)
            if (allowedProductsToUser) {
                listOfIdsAndProductSelection.each { String productSelectionStr, List<Long> configIds ->
                    List<String> productNameList = getProductIdsFromProductSelection(productSelectionStr)
                    if (allowedProductsToUser.intersect(productNameList).size() > 0) {
                        list.addAll(configIds)
                    }
                }
            }
        } else {
            listOfIdsAndProductSelection.each { String productSelectionStr, List<Long> configIds ->
                list.addAll(configIds)
            }
        }

        list
    }

    List<String> getProductIdsFromProductSelection(String productSelection) {
        String prdName = getIdFieldFromJson(productSelection)
        List<String> prdList = []
        if (prdName) {
            prdList = prdName.tokenize(',')
        }
        return prdList
    }

    Map getActionTypeAndActionMap() {
        List<Map> actionTypeList = ActionType.list().collect {
            [id: it.id, value: it.value, text: it.displayName]
        }
        Map actionPropertiesMap = actionService.actionPropertiesJSON(actionTypeList)
        [actionTypeList: actionTypeList, actionPropertiesMap: actionPropertiesMap]
    }

    String generateEventJSON(String pt, Integer ptCode, String ptSelection) {
        String eventJson = null
        if (pt && ptCode) {
            Map eventMap = ["1": [], "2": [], "3": [], "4": [], "5": [], "6": [], "7": [], "8": []]
            eventMap[ptSelection] << [name: pt, id: ptCode.toString()]
            eventJson = new JsonBuilder(eventMap)
        }
        eventJson
    }

    String generateBulkEventJSON(List<String> pts, List<Integer> ptCodes, String ptSelection) {
        String eventJson = null
        if (pts?.size() > 0 && ptCodes?.size() > 0) {
            Map eventMap = ["1": [], "2": [], "3": [], "4": [], "5": [], "6": [], "7": [], "8": []]
            if (ptSelection != "") {
                for (int ptIndex = 0; ptIndex < pts?.size(); ptIndex++) {
                    eventMap[ptSelection] << [name: pts[ptIndex], id: ptCodes[ptIndex]?.toString()]
                }
            } else {
                for (int ptIndex = 0; ptIndex < pts?.size(); ptIndex++) {
                    String termCode = SignalUtil.getTermScopeFromSMQ(pts[ptIndex])
                    pts[ptIndex] = pts[ptIndex]?.substring(0, pts[ptIndex]?.lastIndexOf("(")) //Earlier pt variable was not correct so, corrected code with actual variable.
                    if (termCode?.toUpperCase() == "BROAD") {
                        eventMap["7"] << [name: pts[ptIndex], id: ptCodes[ptIndex]?.toString()]
                    } else {
                        eventMap["8"] << [name: pts[ptIndex], id: ptCodes[ptIndex]?.toString()]
                    }
                }
            }

            eventJson = new JsonBuilder(eventMap)
        }
        eventJson
    }

    String generateEventJSONForSMQ(String pt, Integer ptCode) {
        String eventJson = null
        if (pt && ptCode) {
            String termCode = SignalUtil.getTermScopeFromSMQ(pt)
            pt = pt.substring(0, pt.lastIndexOf("("))
            if (termCode?.toUpperCase() == "BROAD") {
                eventJson = generateEventJSON(pt, ptCode, "7")
            } else {
                eventJson = generateEventJSON(pt, ptCode, "8")
            }
        }
        eventJson
    }

    void batchPersistForDomain(List domainList, Class domainClz) throws Exception {
        Integer batchSize = Holders.config.signal.batch.size as Integer
        domainClz.withTransaction {
            List batch = []
            domainList.eachWithIndex { def domain, Integer index ->
                batch += domain
                domain.save(validate: false)
                if (index && index.mod(batchSize ?: 1500) == 0) { //added this as this property was not available at startup changelog due to CMT
                    Session session = sessionFactory.currentSession
                    session.flush()
                    session.clear()
                    batch.clear()
                }
            }
            if (batch) {
                Session session = sessionFactory.currentSession
                session.flush()
                session.clear()
                batch.clear()
            }
        }

        log.info("Data is persisted.")
    }

    List<Map> getAttachmentMap(List<Long> alertIdList, String referenceClass) {
        List<Map> results = AttachmentLink.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("referenceId", "alertId")
            }
            eq("referenceClass", referenceClass)
            or {
                alertIdList.collate(1000).each {
                    'in'('referenceId', it)
                }
            }
        } as List<Map>

    }

    void batchPersistForMapping(Session session, List<Map> alertIdAndSignalIdList, String insertQuery, Boolean isDate=false) {
        session.doWork(new Work() {
            public void execute(Connection connection) throws SQLException {
                PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)
                def batchSize = Holders.config.signal.batch.size
                int count = 0
                try {
                    alertIdAndSignalIdList.each {
                        preparedStatement.setString(1, it.col1)
                        preparedStatement.setString(2, it.col2)
                        if (isDate) {
                            preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()))
                        }
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
    }


    void persistActivitesForSignal(Long signalId, List<Activity> activityList) {
        List<Long> activityIdList = activityService.batchPersistAlertLevelActivity(activityList)
        String insertExConfigActivityQuery = "INSERT INTO VALIDATED_ALERT_ACTIVITIES(VALIDATED_SIGNAL_ID,ACTIVITY_ID) VALUES(?,?)"
        batchPersistExecConfigActivityMapping(signalId, activityIdList, insertExConfigActivityQuery)
    }

    void persistActivitiesForAutoRouteSignal(Map signalActivityMap) {
        String insertExConfigActivityQuery = "INSERT INTO VALIDATED_ALERT_ACTIVITIES(VALIDATED_SIGNAL_ID,ACTIVITY_ID) VALUES(?,?)"
        signalActivityMap?.each { signalId, activityList ->
            List<Long> activityIdList = activityService.batchPersistAlertLevelActivity(activityList.unique { a, b -> a.details <=> b.details })
            batchPersistExecConfigActivityMapping(signalId as Long, activityIdList, insertExConfigActivityQuery)
        }
    }

    List<Map> getAlertNameMapForAction(def domain, List<Long> actionList) {
        List<Map> alertNameMap = []
        alertNameMap = domain.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            createAlias("actions", "actions", JoinType.LEFT_OUTER_JOIN)
            projections {
                property("id", "id")
                property("name", "name")
                property("actions.id", "actionId")
            }
            'or' {
                actionList.collate(1000).each {
                    'in'("actions.id", it)
                }
            }

        } as List<Map>
        alertNameMap
    }

    List<Map> getAlertForAction(def domain, List<Long> actionList) {
        List<Map> alertList = []
        alertList = domain.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            createAlias("actions", "actions", JoinType.LEFT_OUTER_JOIN)
            createAlias("assignedTo", "assignedTo", JoinType.LEFT_OUTER_JOIN)
            createAlias("assignedToGroup", "assignedToGroup", JoinType.LEFT_OUTER_JOIN)
            projections {
                property("id", "id")
                property("name", "name")
                property("actions.id", "actionId")
                'disposition' {
                    property("displayName", "dispositionDisplayName")
                }
                'priority' {
                    property("displayName", "priorityDisplayName")
                }
                property("assignedTo.id", "assignedToId")
                property("assignedToGroup.id", "assignedToGroupId")
                property("productName", "productName")
            }
            'or' {
                actionList.collate(1000).each {
                    'in'("actions.id", it)
                }
            }
        }
        alertList
    }

    Boolean isProductSecurity() {
        Holders.config.pvsignal.product.based.security as Boolean
    }

    Map generateAlertReviewMap(AlertReviewDTO alertReviewDTO, Closure executedConfigReviewClosure) {
        def st = System.currentTimeSeconds()
        String esc_char = ""
        String searchString = alertReviewDTO.searchValue?.toLowerCase()
        List<Disposition> dispositions = Disposition.list()
        List<Integer> allDispIds = dispositions.collect {it.id}
        List<Integer> closedIds = dispositions.findAll {it-> it.reviewCompleted == true}.collect {it.id}
        List<ExecutedConfiguration> configurations = ExecutedConfiguration.createCriteria().list(max: alertReviewDTO.max, offset: alertReviewDTO.offset) {
            executedConfigReviewClosure.delegate = delegate
            executedConfigReviewClosure(alertReviewDTO)
            if (searchString) {
                if (searchString.contains('_')) {
                    searchString = searchString.replaceAll("\\_", "!_")
                    esc_char = "!"
                } else if (searchString.contains('%')) {
                    searchString = searchString.replaceAll("\\%", "!%%")
                    esc_char = "!"
                }
                if (esc_char) {
                    or {
                        sqlRestriction("""lower(name) like '%${searchString.replaceAll("'", "''")}%' escape '${
                            esc_char
                        }'""")
                        sqlRestriction("""lower(dbms_lob.substr(product_name,dbms_lob.getlength(product_name), 1)) like '%${
                            searchString.replaceAll("'", "''")
                        }%' escape '${esc_char}'""")
                        sqlRestriction("""lower(description) like '%${searchString.replaceAll("'", "''")}%' escape '${
                            esc_char
                        }'""")
                        sqlRestriction("""REPLACE(REPLACE(SELECTED_DATA_SOURCE, 'pva', 'safety db'), 'eudra', 'evdas') like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction("""lower(to_char(data_mining_variable)) like '%${
                            searchString.replaceAll("'", "''")
                        }%' escape '${esc_char}'""")
                        sqlRestriction("""lower(concat( concat( concat(data_mining_variable,'('), product_name),')')) like '%${
                            searchString.replaceAll("'", "''")
                        }%' escape '${esc_char}'""")
                    }
                } else {
                    or {
                        sqlRestriction("""lower(name) like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction("""lower(dbms_lob.substr(product_name,dbms_lob.getlength(product_name), 1)) like '%${
                            searchString.replaceAll("'", "''")
                        }%'""") sqlRestriction("""lower(description) like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction("""REPLACE(REPLACE(SELECTED_DATA_SOURCE, 'pva', 'safety db'), 'eudra', 'evdas') like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction("""lower(to_char(data_mining_variable)) like '%${
                            searchString.replaceAll("'", "''")
                        }%'""")
                        sqlRestriction("""lower(concat( concat( concat(data_mining_variable,'('), product_name),')')) like ('%${
                            searchString.replaceAll("'", "''")
                        }%')""")
                    }
                }
            }
            if(alertReviewDTO.orderProperty == 'pecCount' && alertReviewDTO.adhocRun == true) {
                sqlRestriction(orderByCount(alertReviewDTO.orderProperty, alertReviewDTO.direction))
            } else if(alertReviewDTO.orderProperty == 'closedPecCount') {
                List<String> orderByList2 = []
                closedIds.each {
                    orderByList2 <<  "NVL(JSON_VALUE(disp_counts, '\$.\"${it}\"'), 0)"
                }
                String orderByStr2 = orderByList2.join("+")
                sqlRestriction(" 1=1 ORDER BY ${orderByStr2} ${alertReviewDTO.direction == 'asc' ? Query.Order.Direction.ASC : Query.Order.Direction.DESC} , id ${Query.Order.Direction.ASC} " )
            } else if(alertReviewDTO.orderProperty == 'pecCount') {
                List<String> orderByList3 = []
                allDispIds.each {
                    orderByList3 <<  "NVL(JSON_VALUE(disp_counts, '\$.\"${it}\"'), 0)"
                }
                String orderByStr3 = orderByList3.join("+")
                sqlRestriction(" 1=1 ORDER BY ${orderByStr3} ${alertReviewDTO.direction == 'asc' ? Query.Order.Direction.ASC : Query.Order.Direction.DESC} , id ${Query.Order.Direction.ASC} ")
            } else if(alertReviewDTO.orderProperty == 'selectedDataSource'){
                sqlRestriction(" 1=1 ORDER BY lower(selected_data_source) ${alertReviewDTO.direction == 'asc' ? Query.Order.Direction.ASC : Query.Order.Direction.DESC}")
            } else {
                order(new Query.Order(alertReviewDTO.orderProperty,
                        alertReviewDTO.direction == "asc" ?
                                Query.Order.Direction.ASC : Query.Order.Direction.DESC).ignoreCase())
            }
        } as List<ExecutedConfiguration>
        alertReviewDTO.filterWithUsersAndGroups = []
        Integer totalCount = ExecutedConfiguration.createCriteria().count() {
            executedConfigReviewClosure.delegate = delegate
            executedConfigReviewClosure(alertReviewDTO)
        } as Integer
        Integer filteredCount = configurations.totalCount
        [configurationsList: configurations, totalCount: totalCount, filteredCount: filteredCount]
    }
    Map generateAlertReviewMapICR(AlertReviewDTO alertReviewDTO, Closure executedConfigReviewClosure) {
        def st = System.currentTimeSeconds()
        String esc_char = ""
        String searchString = alertReviewDTO.searchValue?.toLowerCase()
        List<Disposition> dispositions = Disposition.list()
        List<Integer> allDispIds = dispositions.collect {it.id}
        List<Integer> closedIds = dispositions.findAll {it-> it.reviewCompleted == true}.collect {it.id}

        List<ExecutedConfiguration> configurations = ExecutedConfiguration.createCriteria().list(max: alertReviewDTO.max, offset: alertReviewDTO.offset) {

            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "id")
                property("name", "name")
                property("selectedDatasource", "selectedDatasource")
                property("numOfExecutions", "numOfExecutions")
                property("description", "description")
                property("scheduleDateJSON", "scheduleDateJSON")
                property("productGroupSelection", "productGroupSelection")
                property("productSelection", "productSelection")
                property("studySelection", "studySelection")
                property("dispCounts", "dispCounts")
                property("newCounts", "newCounts")
                property("dateCreated", "dateCreated")
                property("lastUpdated", "lastUpdated")
                property("pvrCaseSeriesId", "pvrCaseSeriesId")
                property("priority.id", "priorityId")
                property("executedAlertDateRangeInformation.id", "executedAlertDateRangeInformationId")
            }

            executedConfigReviewClosure.delegate = delegate
            executedConfigReviewClosure(alertReviewDTO)
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
                        sqlRestriction("""lower({alias}.name) like '%${searchString.replaceAll("'", "''")}%' escape '${
                            esc_char
                        }'""")
                        sqlRestriction("""lower(dbms_lob.substr({alias}.product_name,dbms_lob.getlength({alias}.product_name), 1)) like '%${
                            searchString.replaceAll("'", "''")
                        }%' escape '${esc_char}'""")
                        sqlRestriction("""lower({alias}.description) like '%${searchString.replaceAll("'", "''")}%' escape '${
                            esc_char
                        }'""")
                        sqlRestriction("""lower({alias}.selected_data_source) like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction("""lower(to_char({alias}.data_mining_variable)) like '%${
                            searchString.replaceAll("'", "''")
                        }%' escape '${esc_char}'""")
                        if(alertReviewDTO.adhocRun) {
                            sqlRestriction("""lower({alias}.data_mining_variable || '(' || {alias}.product_name || ')') like '%${
                                searchString.replaceAll("'", "''")
                            }%' escape '${esc_char}'""")
                        }
                    }
                } else {
                    or {
                        sqlRestriction("""lower({alias}.name) like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction("""lower(dbms_lob.substr({alias}.product_name,dbms_lob.getlength({alias}.product_name), 1)) like '%${
                            searchString.replaceAll("'", "''")
                        }%'""") sqlRestriction("""lower({alias}.description) like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction("""lower({alias}.selected_data_source) like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction("""lower(to_char({alias}.data_mining_variable)) like '%${
                            searchString.replaceAll("'", "''")
                        }%'""")
                        if(alertReviewDTO.adhocRun) {
                            sqlRestriction("""lower({alias}.data_mining_variable || '(' || {alias}.product_name || ')') like ('%${
                                searchString.replaceAll("'", "''")
                            }%')""")
                        }
                    }
                }
            }

            if(alertReviewDTO.orderProperty == 'caseCount' && alertReviewDTO.adhocRun == true) {
                sqlRestriction(orderByCount(alertReviewDTO.orderProperty, alertReviewDTO.direction))
            } else if(alertReviewDTO.orderProperty == 'newCases' && alertReviewDTO.adhocRun == true) {
                sqlRestriction(orderByCount(alertReviewDTO.orderProperty, alertReviewDTO.direction))
            } else if(alertReviewDTO.orderProperty == 'pecCount' && alertReviewDTO.adhocRun == true) {
                sqlRestriction(orderByCount(alertReviewDTO.orderProperty, alertReviewDTO.direction))
            } else if(alertReviewDTO.orderProperty == 'caseCount') {
                List<String> orderByList = []
                allDispIds.each {
                    orderByList <<  "NVL(JSON_VALUE(disp_counts, '\$.\"${it}\"'), 0)"
                }
                String orderByStr = orderByList.join("+")
                sqlRestriction(" 1=1 ORDER BY ${orderByStr} ${alertReviewDTO.direction == 'asc' ? Query.Order.Direction.ASC : Query.Order.Direction.DESC} , id ${Query.Order.Direction.ASC} ")
            } else if(alertReviewDTO.orderProperty == 'closedCaseCount') {
                List<String> orderByList1 = []
                closedIds.each {
                    orderByList1 <<  "NVL(JSON_VALUE(disp_counts, '\$.\"${it}\"'), 0)"
                }
                String orderByStr1 = orderByList1.join("+")
                sqlRestriction(" 1=1 ORDER BY ${orderByStr1} ${alertReviewDTO.direction == 'asc' ? Query.Order.Direction.ASC : Query.Order.Direction.DESC} , id ${Query.Order.Direction.ASC} ")
            } else if(alertReviewDTO.orderProperty == 'closedPecCount') {
                List<String> orderByList2 = []
                closedIds.each {
                    orderByList2 <<  "NVL(JSON_VALUE(disp_counts, '\$.\"${it}\"'), 0)"
                }
                String orderByStr2 = orderByList2.join("+")
                sqlRestriction(" 1=1 ORDER BY ${orderByStr2} ${alertReviewDTO.direction == 'asc' ? Query.Order.Direction.ASC : Query.Order.Direction.DESC} , id ${Query.Order.Direction.ASC} ")
            } else if(alertReviewDTO.orderProperty == 'pecCount') {
                List<String> orderByList3 = []
                allDispIds.each {
                    orderByList3 <<  "NVL(JSON_VALUE(disp_counts, '\$.\"${it}\"'), 0)"
                }
                String orderByStr3 = orderByList3.join("+")
                sqlRestriction(" 1=1 ORDER BY ${orderByStr3} ${alertReviewDTO.direction == 'asc' ? Query.Order.Direction.ASC : Query.Order.Direction.DESC} , id ${Query.Order.Direction.ASC} ")
            } else if(alertReviewDTO.orderProperty == 'newCases') {
                sqlRestriction(" 1=1 ORDER BY new_counts ${alertReviewDTO.direction == 'asc' ? Query.Order.Direction.ASC : Query.Order.Direction.DESC} , id ${Query.Order.Direction.ASC} ")
            } else {
                order(new Query.Order(alertReviewDTO.orderProperty,
                        alertReviewDTO.direction == "asc" ?
                                Query.Order.Direction.ASC : Query.Order.Direction.DESC).ignoreCase())
            }
        } as List<Map>
        alertReviewDTO.filterWithUsersAndGroups = []
        Integer totalCount = ExecutedConfiguration.createCriteria().count() {
            executedConfigReviewClosure.delegate = delegate
            executedConfigReviewClosure(alertReviewDTO)
        } as Integer
        Integer filteredCount = configurations.totalCount
        [configurationsList: configurations, totalCount: totalCount, filteredCount: filteredCount]
    }

    String orderByCount(String ordering_property, String direction) {
        String orderJoin = ""
        String alertType = "Single Case Alert"
        if(ordering_property == "caseCount") {
            orderJoin = "left join (select sca.exec_config_id as exid, count(*) as case_count from SINGLE_ON_DEMAND_ALERT sca  group by sca.exec_config_id  ) scacount\n" +
                    "            on evr.id = scacount.exid"
            ordering_property = "nvl(case_count,0)"
        } else if(ordering_property == "newCases") {
            orderJoin = "left join (select sca.exec_config_id as exid, count(*) as new_cases from SINGLE_ON_DEMAND_ALERT sca where sca.is_new = 1  group by sca.exec_config_id  ) scacount\n" +
                    "            on evr.id = scacount.exid"
            ordering_property = "nvl(new_cases,0)"
        } else if(ordering_property == "pecCount") {
            orderJoin = "left join (select aca.exec_configuration_id as exid, count(*) as pec_count from AGG_ON_DEMAND_ALERT aca  group by aca.exec_configuration_id  ) acacount\n" +
                    "            on evr.id = acacount.exid"
            ordering_property = "nvl(pec_count,0)"
            alertType = "Aggregate Case Alert"
        }

        String evdasIdsSql = """ select evr.id as id from ex_rconfig evr
            ${orderJoin}
            where adhoc_run=1 and type = '${alertType}'

            order by ${ordering_property} ${direction?direction.toLowerCase():'asc'}        


         """
        List<Long> evdasIds = []

        try {
            Session session = sessionFactory.currentSession
            SQLQuery sqlQuery = session.createSQLQuery(evdasIdsSql)
            sqlQuery.addScalar("id", new LongType())
            evdasIds = sqlQuery.list()
            session.flush()
            session.clear()
        } catch(Exception ex) {
            ex.printStackTrace()
        }

        if(evdasIds) {
            return ("1=1 order by decode(id, ${evdasIds.collect { [it, evdasIds.indexOf(it)] }?.flatten()?.join(',')})")
        } else {
            return ""
        }
    }

    Map getAlertFilterCountAndListOnDemandRuns(AlertDataDTO alertDataDTO, Boolean isExport = false, String selectedCases = null) {
        List scaL = []
        List agaL = []
        Integer totalCount = 0
        Integer totalFilteredCount = 0
        List alertList = []
        List resultList = []
        List fullCaseList = []
        Integer length = alertDataDTO.params.length?.isInteger() ? alertDataDTO.params.length.toInteger() : 0
        Integer start = alertDataDTO.params.start?.isInteger() ? alertDataDTO.params.start.toInteger() : 0
        Closure advancedFilterClosure
        advancedFilterClosure = generateAdvancedFilterClosure(alertDataDTO, advancedFilterClosure)

        if (alertDataDTO.params.callingScreen == Constants.Commons.TRIGGERED_ALERTS && !alertDataDTO.params.adhocRun.toBoolean() && alertDataDTO.domainName == AggregateCaseAlert) {
            totalCount = 0
            totalFilteredCount = 0
        } else {
            if (!alertDataDTO.isFromExport) {
                totalCount = alertDataDTO.domainName.createCriteria().count {
                    callingScreenClosure.delegate = delegate
                    callingScreenClosure(alertDataDTO)
                }
            }

            Closure criteria = {
                callingScreenClosure.delegate = delegate
                filterClosure.delegate = delegate
                orderClosure.delegate = delegate
                callingScreenClosure(alertDataDTO)
                filterClosure(alertDataDTO.filterMap, alertDataDTO.domainName, alertDataDTO.isFaers, alertDataDTO.configId,alertDataDTO.execConfigId, alertDataDTO.isJader)
                orderClosure(alertDataDTO.orderColumnMap)
            }
            alertList = generateAlertListForOnDemandRuns(advancedFilterClosure, alertDataDTO)
            totalFilteredCount = alertList.totalCount
            if (alertDataDTO.isFullCaseList) {
                fullCaseList = alertDataDTO.domainName.createCriteria().list(criteria).collect {
                    [caseNumber: it.caseNumber, caseVersion: it.caseVersion, alertId: it.id, followUpNumber: it.followUpNumber]
                }
            }
        }
        if (alertDataDTO.domainName == SingleOnDemandAlert && alertList.size() > 0) {
            if (selectedCases && selectedCases != null) {
                scaL = singleCaseAlertService.listSelectedAlerts(selectedCases, alertDataDTO.domainName)
                resultList = singleOnDemandAlertService.getSingleCaseAlertList(scaL, alertDataDTO)
            } else {
                resultList = singleOnDemandAlertService.getSingleCaseAlertList(alertList, alertDataDTO)

            }
            if (alertDataDTO.isFullCaseList) {
                fullCaseList = alertList.collect {
                    [caseNumber: it.caseNumber, caseVersion: it.caseVersion, alertId: it.id, followUpNumber: it.followUpNumber]
                }
            }
        } else if (alertDataDTO.domainName == AggregateOnDemandAlert && alertList.size() > 0) {
            if (selectedCases && selectedCases != null) {
                agaL = aggregateCaseAlertService.listSelectedAlerts(selectedCases, alertDataDTO.domainName)
                resultList = aggregateOnDemandAlertService.fetchResultAlertList(agaL, alertDataDTO, isExport)
            } else {
                resultList = aggregateOnDemandAlertService.fetchResultAlertList(alertList, alertDataDTO, isExport)
            }
        } else if (alertDataDTO.domainName == EvdasOnDemandAlert && alertList.size() > 0) {
            resultList = evdasOnDemandAlertService.fetchEvdasOnDemandAlertList(alertList, alertDataDTO, isExport)
        }
        [totalCount: totalCount, totalFilteredCount: totalFilteredCount, resultList: resultList, fullCaseList: fullCaseList]
    }

    void addProductInCacheForSingleAlerts(Long execConfigId, def domain) {
        List<String> productNameList = []
        productNameList = domain.createCriteria().list {
            projections {
                distinct("productName")
            }
            eq("executedAlertConfiguration.id", execConfigId)
        } as List<String>

        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(execConfigId)
        if (executedConfiguration.getProductNameList()) {
            productNameList.addAll(executedConfiguration.getProductNameList())
        }

        if (productNameList.size() > 0) {
            cacheService.addInProductsUsedCache(productNameList as Set)
        }
    }
    Boolean isDeleteInProgress(Long configId, String alertType) {
        def deletion = AlertDeletionData.createCriteria().get {
            eq("configId",configId)
            eq("alertType",alertType)
            eq("deletionStatus", DeletionStatus.READY_TO_DELETE)
            eq("deletionCompleted",false)
        }
        return deletion != null
    }

    Boolean toggleAlertFlag(Long id, def domain) {
        def alert = domain.findById(id)
        Boolean orgValue = alert.flagged
        alert.flagged = !orgValue
        alert.save()
        alert.flagged
    }

    List getDistinctProductName(def domainName, AlertDataDTO alertDataDTO, String callingScreen) {

        List<String> productName = domainName.createCriteria().list {
            projections {
                distinct('productName')
            }
            if (callingScreen == Constants.Commons.DASHBOARD) {
                'eq'("assignedTo.id", alertDataDTO.userId)
            } else {
                if (alertDataDTO.masterExConfigId) {
                    'executedAlertConfiguration' {
                        eq("masterExConfigId", alertDataDTO.masterExConfigId)
                    }
                } else {
                    eq("executedAlertConfiguration.id", alertDataDTO.execConfigId)
                }
            }
            isNotNull('productName')
        } as List<String>
        productName
    }

    Configuration getAlertConfigObjectByType(ExecutedConfiguration ec) {
        Configuration.createCriteria().get {
            eq('id', ec.configId)
            eq('type', ec.type)
        }
    }

    EvdasConfiguration getAlertConfigObject(ExecutedEvdasConfiguration ec) {
        EvdasConfiguration.createCriteria().get {
            eq('id', ec.configId)
        }
    }

    Long fetchPreviousDispositionCount(Long execConfigId, String incomingDisposition, def domain) {
        Long count
        if (domain == LiteratureAlert || domain == ArchivedLiteratureAlert) {
            count = domain.createCriteria().count {
                if (execConfigId) {
                    eq("exLitSearchConfig.id", execConfigId)
                }
                'disposition' {
                    eq("displayName", incomingDisposition)
                }
            }
        } else {
            count = domain.createCriteria().count {
                if (execConfigId) {
                    eq("executedAlertConfiguration.id", execConfigId)
                }
                'disposition' {
                    eq("displayName", incomingDisposition)
                }
            }
        }
        return count
    }

    Long fetchPreviousDispositionCountDashboard(String incomingDisposition, def domain) {
        Long count = domain.createCriteria().count {
            'disposition' {
                eq("displayName", incomingDisposition)
            }
        }
        return count
    }


    Set getDispositionSetDashboard(Boolean isFilterRequest, AlertDataDTO alertDataDTO) {
        List dispositionSet = []
        List<Long> dispositionList = []
        dispositionList = alertDataDTO.domainName.executeQuery(prepareDashboardDispositionHQL(alertDataDTO.domainName),
                [id           : userService.getCurrentUserId(), groupIdList: alertDataDTO.groupIdList,
                 workflowGrpId: alertDataDTO.workflowGroupId])
        Disposition disposition
        dispositionSet = dispositionList.collect {
            disposition = cacheService.getDispositionByValue(it)
            if (alertDataDTO.params.dashboardFilter == 'total' || alertDataDTO.params.dashboardFilter == 'new') {
                [value: disposition.displayName, closed: false]
            } else if (alertDataDTO.params.dashboardFilter == 'underReview' || alertDataDTO.params.dashboardFilter == "assignedTo") {
                [value: disposition.displayName, closed: isFilterRequest ? true : disposition.closed || disposition.validatedConfirmed]
            } else {
                [value: disposition.displayName, closed: isFilterRequest ? true : disposition.closed]
            }
        }
        dispositionSet
    }

    String prepareDashboardDispositionHQL(def domain) {
        String dashboardDispositionHQL = """
          SELECT disposition.id from ${domain.getName()} alert 
            INNER JOIN alert.disposition disposition 
            INNER JOIN alert.executedAlertConfiguration executedal1_ 
            where (alert.assignedTo.id = :id or (alert.assignedToGroup.id in (:groupIdList))) 
                  and (executedal1_.adhocRun=0 and executedal1_.isDeleted=0 and executedal1_.isLatest=1 and
                       executedal1_.workflowGroup.id = :workflowGrpId) 
                  and (disposition.reviewCompleted=0)
        """
        if (domain in [ArchivedSingleCaseAlert, SingleCaseAlert]) {
            dashboardDispositionHQL += ' and alert.isCaseSeries=0'
        }
        dashboardDispositionHQL += """
            GROUP BY disposition.id
            having count(disposition.id) > 0
        """
        dashboardDispositionHQL
    }

    Set getDispositionSet(def execConfig, def domain, Boolean isFilterRequest, Map params) {

        List dispositionSet = []
        List<Disposition> dispositionList = []
        if (domain in [EvdasAlert, ArchivedEvdasAlert] || params.callingScreen != Constants.Commons.REVIEW || Boolean.valueOf(params.isCaseSeries)) {
            dispositionList = domain.createCriteria().list {
                projections {
                    distinct("disposition")
                }
                eq("executedAlertConfiguration.id", execConfig.id)
            } as List<Disposition>
        } else {
            if (execConfig.dispCounts) {
                Map dispositionCountMap = new JsonSlurper().parseText(execConfig.dispCounts) as Map
                dispositionList = dispositionCountMap.keySet().collect {
                    cacheService.getDispositionByValue(it as Long)
                }
            }
        }

        dispositionSet = dispositionList.collect {
            if (params.dashboardFilter == 'total' || params.dashboardFilter == 'new') {
                [value: it.displayName, closed: false, isClosed: it.reviewCompleted]
            } else if (params.dashboardFilter == 'underReview' || params.dashboardFilter == "assignedTo") {
                [value: it.displayName, closed: isFilterRequest ? true : it.closed || it.validatedConfirmed, isClosed: it.reviewCompleted]
            } else {
                [value: it.displayName, closed: it.closed ?: false, isClosed: it.reviewCompleted]
            }
        }

        dispositionSet
    }

    void bulkUpdateAssignedTo(String assignedToValue, List<Long> alertIdList, Long userId, Long groupId, def domain) {
        if (assignedToValue.startsWith(Constants.USER_GROUP_TOKEN)) {
            groupId = Long.valueOf(assignedToValue.replaceAll(Constants.USER_GROUP_TOKEN, ''))
        } else {
            userId = Long.valueOf(assignedToValue.replaceAll(Constants.USER_TOKEN, ''))
        }
        StringBuilder bulkUpdateAssignedToHQL = new StringBuilder()
        bulkUpdateAssignedToHQL.append("Update ${domain.getSimpleName()} set assignedTo.id = :userId,assignedToGroup.id = :groupId,followUpExists = 0 where id in (:alertIdList) ")
        SingleCaseAlert.executeUpdate(bulkUpdateAssignedToHQL.toString(),
                [userId: userId, groupId: groupId, alertIdList: alertIdList])
    }

    void batchPersistBulkUpdateExecConfigActivityMapping(List<Map> activityIdList, String insertExConfigActivityQuery) {
        Session session = sessionFactory.currentSession

        session.doWork(new Work() {
            void execute(Connection connection) throws SQLException {
                PreparedStatement preparedStatement = connection.prepareStatement(insertExConfigActivityQuery)
                int batchSize = Holders.config.signal.batch.size
                int count = 0
                try {
                    activityIdList.each {
                        preparedStatement.setLong(1, it.execConfigId as Long)
                        preparedStatement.setLong(2, it.activityId as Long)
                        preparedStatement.addBatch()
                        count += 1
                        if (count == batchSize) {
                            preparedStatement.executeBatch()
                            count = 0
                        }
                    }
                    preparedStatement.executeBatch()
                } catch (Exception e) {
                    log.error(e.getMessage())
                } finally {
                    preparedStatement.close()
                    session.flush()
                    session.clear()
                }
            }
        })
    }

    void batchPersistSignalMemoActivityMapping(List<Map> activityIdList, String add_activity_for_signal_memo) {
        Session session = sessionFactory.currentSession

        session.doWork(new Work() {
            void execute(Connection connection) throws SQLException {
                PreparedStatement preparedStatement = connection.prepareStatement(add_activity_for_signal_memo)
                int batchSize = Holders.config.signal.batch.size
                int count = 0
                try {
                    activityIdList.each {
                        preparedStatement.setLong(1, it.signalId as Long)
                        preparedStatement.setLong(2, it.activityId as Long)
                        preparedStatement.addBatch()
                        count += 1
                        if (count == batchSize) {
                            preparedStatement.executeBatch()
                            count = 0
                        }
                    }
                    preparedStatement.executeBatch()
                } catch (Exception e) {
                    log.error(e.getMessage())
                } finally {
                    preparedStatement.close()
                    session.flush()
                    session.clear()
                }
            }
        })
    }

    void batchPersistSignalRMMaMapping(List<Map> signalRMMsIdList, String add_signalRMMs_for_signal) {
        Session session = sessionFactory.currentSession

        session.doWork(new Work() {
            void execute(Connection connection) throws SQLException {
                PreparedStatement preparedStatement = connection.prepareStatement(add_signalRMMs_for_signal)
                int batchSize = Holders.config.signal.batch.size
                int count = 0
                try {
                    signalRMMsIdList.each {
                        preparedStatement.setLong(1, it.signalRMMsId as Long)
                        preparedStatement.setLong(2, it.signalId as Long)
                        preparedStatement.addBatch()
                        count += 1
                        if (count == batchSize) {
                            preparedStatement.executeBatch()
                            count = 0
                        }
                    }
                    preparedStatement.executeBatch()
                } catch (Exception e) {
                    log.error(e.getMessage())
                } finally {
                    preparedStatement.close()
                    session.flush()
                    session.clear()
                }
            }
        })
    }

    Date calcDueDateBulkUpdate(Date detectedDate, Priority priority) {
        DateTime theDetectedDate = new DateTime(detectedDate)
        DateTime theDueDate = theDetectedDate.plusDays(priority.reviewPeriod)
        return theDueDate.toDate()
    }

    void bulkUpdatePriority(List<Long> alertIdList, Long priorityId, Date dueDate, def domain) {
        SingleCaseAlert.executeUpdate("Update SingleCaseAlert set priority.id = :priorityId,dueDate = :dueDate,followUpExists = 0 " +
                "where id in (:alertIdList) ",
                [priorityId: priorityId, dueDate: dueDate, alertIdList: alertIdList])
    }

    void bulkUpdateDisposition(List<Long> alertIdList, Long dispositionId, def domain) {
        domain.executeUpdate("Update ${domain.getSimpleName()} set disposition.id = :dispositionId " +
                "where id in (:alertIdList) ",
                [dispositionId: dispositionId, alertIdList: alertIdList])
    }

    void persistActivityForEvdasComment(Map map) {
        if (map.activityList) {
            String insertExConfigActivityQuery = "INSERT INTO EX_EVDAS_CONFIG_ACTIVITIES(EX_EVDAS_CONFIG_ID,ACTIVITY_ID) VALUES(?,?)"
            List<Map> activityExecConfIdsMap = activityService.batchPersistBulkUpdateActivity(map.activityList)
            batchPersistBulkUpdateExecConfigActivityMapping(activityExecConfIdsMap, insertExConfigActivityQuery)
        }
    }

    //TODO: Once this is merged with 5.0, ExecutedConfigurations should be updated based on configId, No need of oldName and ownerId --AP
    void renameExecConfig(Long configId, String configNameOld, String configNameNew, Long ownerId, String alertType) {
        def exConfigDomain = null
        def alertDomain = null
        def exConfigDomainString = null
        def alertDomainString = null
        switch (alertType) {
            case Constants.AlertConfigType.SINGLE_CASE_ALERT:
                exConfigDomain = ExecutedConfiguration
                alertDomain = SingleCaseAlert
                exConfigDomainString = 'ExecutedConfiguration'
                alertDomainString = 'SingleCaseAlert'
                break
            case Constants.AlertConfigType.AGGREGATE_CASE_ALERT:
                exConfigDomain = ExecutedConfiguration
                alertDomain = AggregateCaseAlert
                exConfigDomainString = 'ExecutedConfiguration'
                alertDomainString = 'AggregateCaseAlert'
                break
            case Constants.AlertConfigType.EVDAS_ALERT:
                exConfigDomain = ExecutedEvdasConfiguration
                alertDomain = EvdasAlert
                exConfigDomainString = 'ExecutedEvdasConfiguration'
                alertDomainString = 'EvdasAlert'
                break
            case Constants.AlertConfigType.LITERATURE_SEARCH_ALERT:
                exConfigDomain = ExecutedLiteratureConfiguration
                alertDomain = LiteratureAlert
                exConfigDomainString = 'ExecutedLiteratureConfiguration'
                alertDomainString = 'LiteratureAlert'
                break
            default:
                log.info("Invalid Alert Type")
                break
        }

        StringBuilder exConfigDomainHQL = new StringBuilder()
        exConfigDomainHQL.append("Update ${exConfigDomainString} set name =:configNameNew where configId= :configId")
        String alertConfiguration = (alertType.equals(Constants.AlertConfigType.LITERATURE_SEARCH_ALERT)) ? 'litSearchConfig' : 'alertConfiguration'
        StringBuilder alertDomainHQL = new StringBuilder()
        alertDomainHQL.append("Update ${alertDomainString} set name=:configNameNew where ${alertConfiguration}.id=:configId")

        if (exConfigDomain && alertDomain) {
            exConfigDomain.executeUpdate(exConfigDomainHQL.toString(),
                    [configNameNew: configNameNew, configId: configId])
            alertDomain.executeUpdate(alertDomainHQL.toString(), [configNameNew: configNameNew, configId: configId])
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void updateOldExecutedConfigurations(def configuration, Long execConfigId, def domain) {
        StringBuilder HQL1 = new StringBuilder()
        HQL1.append("Update ${domain.getSimpleName()} set isLatest = 0 where name = :name and owner = :owner and id <> :execConfigId")

        StringBuilder HQL2 = new StringBuilder()
        HQL2.append("Update ${domain.getSimpleName()} set isLatest = 1,isEnabled = 1 where id = :execConfigId")

        domain.executeUpdate(HQL1.toString(),
                [name: configuration.name, owner: configuration.owner, execConfigId: execConfigId])

        domain.executeUpdate(HQL2.toString(), [execConfigId: execConfigId])
    }

    JSONObject updateSharedWithReport(Map params) {
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(Long.parseLong(params.executedConfigId))
        if (executedConfiguration?.reportId) {
            log.info("Sharing of report with users and groups : ${params.sharedWith} started")
            try {
                List<Long> groupIdsCurrent = []
                List<Long> userIdsCurrent = []
                List<String> sharedWithUsers = []
                List<String> sharedWithGroups = []
                params.sharedWith.flatten().each { String shared ->
                    if (shared.startsWith(Constants.USER_GROUP_TOKEN)) {
                        Group group = Group.get(Long.valueOf(shared.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                        if (group) {
                            sharedWithGroups.add(group.name)
                        }
                    } else if (shared.startsWith(Constants.USER_TOKEN)) {
                        User user = User.get(Long.valueOf(shared.replaceAll(Constants.USER_TOKEN, '')))
                        if (user) {
                            sharedWithUsers.add(user.username)
                        }
                    }
                }

                Long reportId = executedConfiguration.reportId
                ExecutedConfigurationSharedWithDTO executedConfigurationSharedWithDTO = new ExecutedConfigurationSharedWithDTO()
                executedConfigurationSharedWithDTO.exConfigId = reportId
                executedConfigurationSharedWithDTO.sharedWithUsers = sharedWithUsers
                executedConfigurationSharedWithDTO.sharedWithGroups = sharedWithGroups
                String url = Holders.config.pvreports.url
                String path = Holders.config.pvreports.updateReport.uri
                Map response = reportIntegrationService.postData(url, path, executedConfigurationSharedWithDTO)
                log.info("Shared Report Update API Response from PVR : ${response}")
                if (response.status == HttpStatus.SC_OK) {
                    if (response.result.status && response.result.data) {
                        log.info("Sharing of report with users : ${sharedWithUsers} and groups : ${sharedWithGroups}")
                    } else {
                        throw new Exception("Error in sharing Report in PVR")
                    }
                } else {
                    throw new Exception("Something unexpected happen in PVR: " + response.status)
                }
            }
            catch (Throwable th) {
                log.error("Error occurred in executeReport" + th.getMessage())
                throw th
            }

        }
    }

    Closure criteriaConditionsTags = { String fieldName, String operatorKey, String value, Long exConfigId = null ->
        Map<String, List<String>> aliasPropertiesMap = [:]
        String columnName = (fieldName == "tags") ? "tag_text" : "sub_tag_text"
        String suffixCurrentRun = ""
        if (fieldName == "currentRun") {
            columnName = "tag_text"
            suffixCurrentRun = "CurrentRun"
        }
        if (delegate.targetClass in [AggregateCaseAlert, AggregateOnDemandAlert]) {
            fieldName = "aggTags"
        } else if (delegate.targetClass in [SingleCaseAlert, SingleOnDemandAlert]) {
            fieldName = "tags"
        }

        if (delegate.targetClass in [SingleOnDemandAlert, AggregateOnDemandAlert]) {
            aliasPropertiesMap = grailsApplication.config.advancedFilter.onDemand.sqlRestrictionAliasTagMap
        } else if (delegate.targetClass in [SingleCaseAlert, AggregateCaseAlert]) {
            aliasPropertiesMap = grailsApplication.config.advancedFilter.sqlRestrictionAliasTagMap
        }

        String sqlRestrictionTagSql = ""

        AdvancedFilterOperatorEnum operatorEnum = operatorKey as AdvancedFilterOperatorEnum
        List emptyOperators = [AdvancedFilterOperatorEnum.IS_NOT_EMPTY, AdvancedFilterOperatorEnum.IS_EMPTY]

        if (suffixCurrentRun && !emptyOperators.contains(operatorEnum)) {
            fieldName = fieldName + suffixCurrentRun
            sqlRestrictionTagSql = generateSqlRestrictionCurrentRunSql(fieldName, operatorKey, value)
        } else {
            sqlRestrictionTagSql = generateSqlRestrictionTagSql(fieldName, operatorKey, value, columnName, aliasPropertiesMap, exConfigId)
        }

        if (sqlRestrictionTagSql) {
            sqlRestriction(sqlRestrictionTagSql)
        }
    }


    String setImpEventValue(String alertPt, String ptCode, String productName, productId, List eiList, Boolean isEvdas = false, Boolean isMultiIngredient = false, Boolean isIngredient = false) {
        String resultString = Constants.Commons.BLANK_STRING
        List eventNameArray = []
        if (alertPt && eiList) {
            for (Map ei : eiList) {
                if (ei.eventName && (ei.eventName*.toLowerCase().contains(alertPt?.toLowerCase()) || ei.eventName*.toLowerCase().contains(alertPt?.toLowerCase() - '(broad)' - "(narrow)"))) {
                    if (!isEvdas && ei.productName.find {
                        it.name?.toLowerCase().contains(productName?.toLowerCase()) && it.id == productId as String
                    }) {
                        if (isIngredient) {
                            if (ei.isMultiIngredient == isMultiIngredient) {
                                eventNameArray += [ei.dme, ei.ei, ei.ime, ei.sm].grep()
                            }
                        } else {
                            eventNameArray += [ei.dme, ei.ei, ei.ime, ei.sm].grep()
                        }
                    } else if (isEvdas && ei.productName.find {
                        it.name?.toLowerCase().equals(productName?.toLowerCase())
                    } && (ei.dataSourceDict?.contains(Constants.DataSource.EUDRA) || ei.dataSourceDict?.contains(Constants.DataSource.DATASOURCE_EUDRA))) {
                        eventNameArray += [ei.dme, ei.ei, ei.ime, ei.sm].grep()
                    }
                    if (ei.productName.size() == 0) {
                        eventNameArray += [ei.dme, ei.ei, ei.ime, ei.sm].grep()
                    }
                }
            }
        }
        if (eventNameArray) {
            String sl = Holders.config.importantEvents.stopList.abbreviation?.toLowerCase()
            Collections.replaceAll(eventNameArray, "ei", sl)
            Collections.sort(eventNameArray)
            Collections.replaceAll(eventNameArray, sl, "ei")
        }

        resultString = eventNameArray ? eventNameArray.join(",") : Constants.Commons.BLANK_STRING
        return resultString
    }

    List getEmergingIssueList() {
        EmergingIssue.getAll()?.collect { ei ->
            [
                    eventName     : ei.eventGroupSelection ? getGroupNameFieldFromJson(ei.eventGroupSelection)?.tokenize(',')*.toLowerCase() : emergingIssueService.getEventNameFieldJson(ei.eventName)*.toLowerCase(),
                    dme           : ei.dme ? 'dme' : Constants.Commons.BLANK_STRING,
                    ei            : ei.emergingIssue ? 'ei' : Constants.Commons.BLANK_STRING,
                    ime           : ei.ime ? 'ime' : Constants.Commons.BLANK_STRING,
                    sm            : ei.specialMonitoring ? 'sm' : Constants.Commons.BLANK_STRING,
                    productName   : emergingIssueService.fetchProductNameForMatching(ei.productSelection) + emergingIssueService.fetchPGNameForMatching(ei.productGroupSelection),
                    dataSourceDict: ei.dataSourceDict,
                    isMultiIngredient: ei.isMultiIngredient
            ]
        }
    }

    String generateSqlRestrictionSql(AdvancedFilterOperatorEnum operatorEnum, List valuesList, Map<String, String> iLikePropertiesMap, Map<String, String> notILikePropertiesMap, Map<String, String> aliasPropertiesMap) {
        String sqlRestrictionSql
        String whereClause
        String operator
        if (operatorEnum in [AdvancedFilterOperatorEnum.NOT_EQUAL, AdvancedFilterOperatorEnum.EQUALS]) {
            String values = valuesList.collect {
                "'${it.toString().replaceAll("'", "''").toUpperCase()}'"
            }.join(',')
            operator = operatorEnum == AdvancedFilterOperatorEnum.EQUALS ? Constants.AdvancedFilter.IN : Constants.AdvancedFilter.NOT_IN
            whereClause = "upper(${aliasPropertiesMap.columnName}) IN ($values)"
        } else if (iLikePropertiesMap.containsKey(operatorEnum.value())) {
            operator = Constants.AdvancedFilter.IN
            whereClause = "upper(${aliasPropertiesMap.columnName}) LIKE '${iLikePropertiesMap.get(operatorEnum.value()).toUpperCase()}'"
        } else if (notILikePropertiesMap.containsKey(operatorEnum.value())) {
            operator = Constants.AdvancedFilter.NOT_IN
            whereClause = "upper(${aliasPropertiesMap.columnName}) LIKE '${notILikePropertiesMap.get(operatorEnum.value()).toUpperCase()}'"
        } else {
            operator = operatorEnum == AdvancedFilterOperatorEnum.IS_EMPTY ? Constants.AdvancedFilter.NOT_IN : Constants.AdvancedFilter.IN
        }
        sqlRestrictionSql = """{alias}.id $operator (
                                            ${aliasPropertiesMap.subQuery}
                                            ${whereClause ? "WHERE $whereClause" : ''} )
                                   """
        sqlRestrictionSql
    }

    List getProductOrEventNamesFromJson(String value) {
        def nameList = []
        def jsonSlurper = new JsonSlurper()
        def parsedJson = jsonSlurper.parseText(value)

        parsedJson.each { outerKey, outerValue ->
            if (outerValue instanceof Map) {
                outerValue.each { innerKey, innerValue ->
                    if (innerValue instanceof List) {
                        innerValue.each { item ->
                            if (item instanceof Map && item.name) {
                                nameList.add(item.name)
                            }
                        }
                    }
                }
            }
        }
        return nameList
    }

    String generateSqlRestrictionClobSql(String fieldName, String operatorKey, String value, Map<String, List<String>> aliasPropertiesMap, Long exeConfigId = null, def domain) {

        Map<String, String> iLikePropertiesMap = [
                'CONTAINS'  : "%${value}%",
                'START_WITH': "${value}%",
                'ENDS_WITH' : "%${value}"
        ]
        Map<String, String> notILikePropertiesMap = [
                'DOES_NOT_CONTAIN': "%${value}%",
                'DOES_NOT_START'  : "${value}%",
                'DOES_NOT_END'    : "%${value}"
        ]
        StringBuilder criteriaStringBuilder = new StringBuilder()
        StringBuilder productOrEventGroupsValue = new StringBuilder()

        AdvancedFilterOperatorEnum operatorEnum = operatorKey as AdvancedFilterOperatorEnum
        String whereClause
        String operator
        //used when there is apostrophe in the search string
        def modifiedString

        Map SCAJsonFieldMap = grailsApplication.config.advancedFilter.singleCaseAlert.jsonFieldMap
        String tableName = domain == SingleCaseAlert ? "SINGLE_CASE_ALERT" : "SINGLE_ON_DEMAND_ALERT"
        Boolean isProductGroupValue = value.contains('PG_')
        Boolean isEventGroupValue = value.contains('EG_')
        value = isProductGroupValue ? value.replace('PG_', '') : value
        value = isEventGroupValue ? value.replace('EG_', '') : value
        List productOrEventGroupsValueList = []
        if (isProductGroupValue || isEventGroupValue) {

            productOrEventGroupsValue.setLength(0)
            value.split(';').each {
                def DictValues = dictionaryGroupService.groupDetails(it as Long, springSecurityService.principal?.username, true)?.data
                productOrEventGroupsValueList.add(getProductOrEventNamesFromJson(DictValues))
            }
        }
        productOrEventGroupsValueList = productOrEventGroupsValueList.flatten()
        log.info("All products or events list : " + productOrEventGroupsValueList)

        boolean isSearchWithJsonFieldEnabled = grailsApplication.config.advancedFilter.enable.with.JsonField as boolean
        if (SCAJsonFieldMap.containsValue(fieldName) && isSearchWithJsonFieldEnabled) {
            if (operatorEnum in [AdvancedFilterOperatorEnum.NOT_EQUAL, AdvancedFilterOperatorEnum.EQUALS]) {

                operator = operatorEnum == AdvancedFilterOperatorEnum.EQUALS ? Constants.AdvancedFilter.IN : Constants.AdvancedFilter.NOT_IN
                if (isProductGroupValue || isEventGroupValue) {
                    criteriaStringBuilder.setLength(0)
                    criteriaStringBuilder.append("{alias}.id ${operator} (select id from ${tableName} , JSON_table(json_field, '\$.${fieldName}[*]'\n" +
                            "columns(t varchar2 path '\$')) t1  where  ${exeConfigId ? ' exec_config_id=' + exeConfigId + '' : ''} ")

                    productOrEventGroupsValueList.eachWithIndex { name, idx ->
                        name = name.replaceAll("'", "''")
                        if (exeConfigId) {
                            idx == 0 ? criteriaStringBuilder.append(" ( AND UPPER(t1.t) = UPPER('${name}')") : criteriaStringBuilder.append(" OR UPPER(t1.t) = UPPER('${name}') ")
                        } else {
                            idx == 0 ? criteriaStringBuilder.append(" ( UPPER(t1.t) = UPPER('${name}')") : criteriaStringBuilder.append(" OR UPPER(t1.t) = UPPER('${name}') ")
                        }
                    }
                    criteriaStringBuilder.append("))")
                    return criteriaStringBuilder
                } else {
                    List valuesList = getValuesListForClobFields(value)
                    def newValueList = valuesList.collect {
                        "'${it.toString().replaceAll("'", "''").replaceAll("\n", "\r\n")}'"
                    }
                    criteriaStringBuilder.setLength(0)
                    criteriaStringBuilder.append("{alias}.id ${operator} (select id from ${tableName}  , JSON_table(json_field, '\$.${fieldName}[*]'\n" +
                            "columns(t varchar2 path '\$')) t1  where  ${exeConfigId ? ' exec_config_id=' + exeConfigId + '' : ''} ")

                    newValueList.eachWithIndex { name, idx ->
                        if (exeConfigId) {
                            idx == 0 ? criteriaStringBuilder.append(" AND ( UPPER(t1.t) = UPPER(${name})") : criteriaStringBuilder.append(" OR UPPER(t1.t) = UPPER(${name}) ")
                        } else {
                            idx == 0 ? criteriaStringBuilder.append(" ( UPPER(t1.t) = UPPER(${name})") : criteriaStringBuilder.append(" OR UPPER(t1.t) = UPPER(${name}) ")
                        }
                    }
                    criteriaStringBuilder.append("))")
                    return criteriaStringBuilder

                }
            } else if (iLikePropertiesMap.containsKey(operatorEnum.value())) {
                operator = Constants.AdvancedFilter.IN
                if (isProductGroupValue || isEventGroupValue) {
                    criteriaStringBuilder.setLength(0)
                    criteriaStringBuilder.append("{alias}.id ${operator} (select id from ${tableName}  , JSON_table(json_field, '\$.${fieldName}[*]'\n" +
                            "columns(t varchar2 path '\$')) t1  where  ${exeConfigId ? ' exec_config_id=' + exeConfigId + '' : ''} ")

                    productOrEventGroupsValueList.eachWithIndex { name, idx ->
                        name = name.replaceAll("'", "''")
                        if (exeConfigId) {
                            idx == 0 ? criteriaStringBuilder.append(" AND ( UPPER(t1.t) LIKE UPPER('%${name}%')") : criteriaStringBuilder.append(" OR UPPER(t1.t) LIKE UPPER('%${name}%') ")
                        } else {
                            idx == 0 ? criteriaStringBuilder.append(" ( UPPER(t1.t) LIKE UPPER('%${name}%')") : criteriaStringBuilder.append(" OR UPPER(t1.t) LIKE UPPER('%${name}%') ")
                        }
                    }
                    criteriaStringBuilder.append("))")
                    return criteriaStringBuilder
                } else {
                    modifiedString = iLikePropertiesMap.get(operatorEnum.value())?.replaceAll("'", "''")
                    return "{alias}.id ${operator} (select id from ${tableName}  , JSON_table(json_field, '\$.${fieldName}[*]'\n" +
                            "columns(t varchar2 path '\$')) t1  where UPPER(t1.t) LIKE '${modifiedString.toUpperCase()}' ${exeConfigId ? 'and exec_config_id=' + exeConfigId + '' : ''} )"
                }
            } else if (notILikePropertiesMap.containsKey(operatorEnum.value())) {
                operator = Constants.AdvancedFilter.NOT_IN
                if (isProductGroupValue || isEventGroupValue) {
                    criteriaStringBuilder.setLength(0)
                    criteriaStringBuilder.append("{alias}.id ${operator} (select id from ${tableName}  , JSON_table(json_field, '\$.${fieldName}[*]'\n" +
                            "columns(t varchar2 path '\$')) t1  where  ${exeConfigId ? ' exec_config_id=' + exeConfigId + '' : ''} ")

                    productOrEventGroupsValueList.eachWithIndex { name, idx ->
                        name = name.replaceAll("'", "''")
                        if (exeConfigId) {
                            idx == 0 ? criteriaStringBuilder.append(" AND ( UPPER(t1.t) LIKE UPPER('%${name}%')") : criteriaStringBuilder.append(" OR UPPER(t1.t) LIKE UPPER('%${name}%') ")
                        } else {
                            idx == 0 ? criteriaStringBuilder.append(" ( UPPER(t1.t) LIKE UPPER('%${name}%')") : criteriaStringBuilder.append(" OR UPPER(t1.t) LIKE UPPER('%${name}%') ")
                        }
                    }
                    criteriaStringBuilder.append("))")
                    return criteriaStringBuilder
                } else {
                    modifiedString = notILikePropertiesMap.get(operatorEnum.value()).replaceAll("'", "''")
                    return "{alias}.id ${operator} (select id from ${tableName}  , JSON_table(json_field, '\$.${fieldName}[*]'\n" +
                            "columns(t varchar2 path '\$')) t1  where UPPER(t1.t) LIKE '${modifiedString.toUpperCase()}' ${exeConfigId ? 'and exec_config_id=' + exeConfigId + '' : ''} )"
                }
            } else {
                operator = operatorEnum == AdvancedFilterOperatorEnum.IS_EMPTY ? Constants.AdvancedFilter.NOT_IN : Constants.AdvancedFilter.IN
                if (aliasPropertiesMap.get(fieldName)[1]) {
                    return """{alias}.id $operator (
                                            Select ${aliasPropertiesMap.get(fieldName)[2]} from ${
                        aliasPropertiesMap.get(fieldName)[1]
                    }
                                            ${whereClause ? "WHERE $whereClause" : ''} )
                               """
                } else {
                    whereClause = whereClause + (exeConfigId ? (" and exec_config_id = " + exeConfigId) : "")
                    return whereClause
                }
            }
        } else {
            if (operatorEnum in [AdvancedFilterOperatorEnum.NOT_EQUAL, AdvancedFilterOperatorEnum.EQUALS]) {
                String values
                if (isProductGroupValue || isEventGroupValue) {
                    values = productOrEventGroupsValueList.join(',').replaceAll("'", "''")
                } else {
                    List valuesList = getValuesListForClobFields(value)
                    values = valuesList.collect {
                        "'${it.toString().replaceAll("'", "''")}'"
                    }.join(',')
                }
                operator = operatorEnum == AdvancedFilterOperatorEnum.EQUALS ? Constants.AdvancedFilter.IN : Constants.AdvancedFilter.NOT_IN
                whereClause = "DBMS_LOB.SUBSTR(${aliasPropertiesMap.get(fieldName)[0]},32767) IN ($values)"
            } else if (iLikePropertiesMap.containsKey(operatorEnum.value())) {
                operator = Constants.AdvancedFilter.IN
                if (isProductGroupValue || isEventGroupValue) {
                    StringBuilder whereClauseBuilder = new StringBuilder()
                    productOrEventGroupsValueList.eachWithIndex { name, idx ->
                        name = name.replaceAll("'", "''")
                        idx == 0 ? whereClauseBuilder.append("UPPER(${aliasPropertiesMap.get(fieldName)[0]}) LIKE '${iLikePropertiesMap.get(operatorEnum.value()).toUpperCase()}'") :
                                whereClauseBuilder.append("OR UPPER(${aliasPropertiesMap.get(fieldName)[0]}) LIKE '${iLikePropertiesMap.get(operatorEnum.value()).toUpperCase()}'")
                    }
                    whereClause = whereClauseBuilder.toString()

                } else {
                    modifiedString = iLikePropertiesMap.get(operatorEnum.value()).replaceAll("'", "''")
                    whereClause = "UPPER(${aliasPropertiesMap.get(fieldName)[0]}) LIKE '${modifiedString.toUpperCase()}'"
                }
            } else if (notILikePropertiesMap.containsKey(operatorEnum.value())) {
                operator = Constants.AdvancedFilter.IN
                modifiedString = notILikePropertiesMap.get(operatorEnum.value()).replaceAll("'", "''")
                if (aliasPropertiesMap.get(fieldName)[1]) {
                    whereClause = "not exists \n" +
                            "(select 1 from ${aliasPropertiesMap.get(fieldName)[1]} sap2 \n" +
                            "    where sap2.${aliasPropertiesMap.get(fieldName)[2]} = {alias}.id\n" +
                            "    and UPPER(${aliasPropertiesMap.get(fieldName)[0]}) like '${modifiedString.toUpperCase()}')\n" +
                            "group by ${aliasPropertiesMap.get(fieldName)[2]}"
                } else {
                    whereClause = "UPPER(${aliasPropertiesMap.get(fieldName)[0]}) NOT LIKE '${modifiedString.toUpperCase()}'"
                }
            } else {
                operator = operatorEnum == AdvancedFilterOperatorEnum.IS_EMPTY ? Constants.AdvancedFilter.NOT_IN : Constants.AdvancedFilter.IN
            }
            if (aliasPropertiesMap.get(fieldName)[1]) {
                return """{alias}.id $operator (
                                            Select ${aliasPropertiesMap.get(fieldName)[2]} from ${
                    aliasPropertiesMap.get(fieldName)[1]
                }
                                            ${whereClause ? "WHERE $whereClause" : ''} )
                               """
            } else {
                whereClause = whereClause + (exeConfigId ? (" and exec_config_id = " + exeConfigId) : "")
            }
            return whereClause
        }
    }

    String generateSqlRestrictionTagSql(String fieldName, String operatorKey, String value, String columnName, Map<String, List<String>> aliasPropertiesMap , Long exConfigId = null) {

        Map<String, String> iLikePropertiesMap = [
                'CONTAINS'  : "%${value}%",
                'START_WITH': "${value}%",
                'ENDS_WITH' : "%${value}"
        ]
        Map<String, String> notILikePropertiesMap = [
                'DOES_NOT_CONTAIN': "%${value}%",
                'DOES_NOT_START'  : "${value}%",
                'DOES_NOT_END'    : "%${value}"
        ]

        AdvancedFilterOperatorEnum operatorEnum = operatorKey as AdvancedFilterOperatorEnum
        String whereClauseGlobal
        String whereClauseAlert
        String operator
        String username = userService.getUser()?.username

        if (operatorEnum in [AdvancedFilterOperatorEnum.NOT_EQUAL, AdvancedFilterOperatorEnum.EQUALS]) {
            List valuesList = getValuesListForClobFields(value)
            String values = valuesList.collect {
                "'${it.toString()?.replaceAll("'", "''")?.toUpperCase()}'"
            }.join(',')
            operator = operatorEnum == AdvancedFilterOperatorEnum.EQUALS ? Constants.AdvancedFilter.IN : Constants.AdvancedFilter.NOT_IN
            whereClauseGlobal = "UPPER(pgt.${columnName}) IN ($values)"
            whereClauseAlert = "UPPER(pat.${columnName}) IN ($values)"
        } else if (iLikePropertiesMap.containsKey(operatorEnum.value())) {
            operator = Constants.AdvancedFilter.IN
            whereClauseGlobal = "UPPER(pgt.${columnName}) LIKE '${iLikePropertiesMap.get(operatorEnum.value()).toUpperCase()}' "
            whereClauseAlert = "UPPER(pat.${columnName}) LIKE '${iLikePropertiesMap.get(operatorEnum.value()).toUpperCase()}'"
        } else if (notILikePropertiesMap.containsKey(operatorEnum.value())) {
            operator = Constants.AdvancedFilter.NOT_IN
            whereClauseGlobal = "UPPER(pgt.${columnName}) LIKE '${notILikePropertiesMap.get(operatorEnum.value()).toUpperCase()}' "
            whereClauseAlert = "UPPER(pat.${columnName}) LIKE '${notILikePropertiesMap.get(operatorEnum.value()).toUpperCase()}'"
        } else {
            operator = operatorEnum == AdvancedFilterOperatorEnum.IS_EMPTY ? Constants.AdvancedFilter.NOT_IN : Constants.AdvancedFilter.IN
            if (columnName == "sub_tag_text") {
                whereClauseGlobal = "UPPER(pgt.${columnName}) IS NOT NULL"
                whereClauseAlert = "UPPER(pat.${columnName}) IS NOT NULL"
            }
        }
        // PVS-53965 exec config column name for agg/icr alert
        String exConfigColumnName = fieldName == "tags" ? "exec_config_id=" : "exec_configuration_id="
        return """{alias}.id $operator (select id from ${
            aliasPropertiesMap[fieldName].columnName
        } where ${exConfigId ? exConfigColumnName + exConfigId  + " AND " : ""} global_identity_id in (${
            aliasPropertiesMap[fieldName].subQueryGlobal
        } AND (PRIVATE_USER IS NULL OR PRIVATE_USER ='${username}') ${
            whereClauseGlobal ? "WHERE $whereClauseGlobal" : ''
        }) union
                                            ${
            aliasPropertiesMap[fieldName].subQueryAlert
        } AND (PRIVATE_USER IS NULL OR PRIVATE_USER ='${username}')
                                            ${whereClauseAlert ? "WHERE $whereClauseAlert" : ''} )
                               """
    }

    String generateSqlRestrictionCurrentRunSql(String fieldName, String operatorKey, String value) {


        Map<String, List<String>> aliasPropertiesMap = grailsApplication.config.advancedFilter.sqlRestrictionAliasTagMap

        AdvancedFilterOperatorEnum operatorEnum = operatorKey as AdvancedFilterOperatorEnum
        String operator

        Map<String, String> iLikePropertiesMap = [
                'CONTAINS'  : "%${value}%",
                'START_WITH': "${value}%",
                'ENDS_WITH' : "%${value}"
        ]
        Map<String, String> notILikePropertiesMap = [
                'DOES_NOT_CONTAIN': "%${value}%",
                'DOES_NOT_START'  : "${value}%",
                'DOES_NOT_END'    : "%${value}"
        ]

        if (operatorEnum in [AdvancedFilterOperatorEnum.EQUALS] && value.toUpperCase() == "YES") {
            operator = Constants.AdvancedFilter.IN
        } else if (iLikePropertiesMap.containsKey(operatorEnum.value()) && value.toUpperCase() == "YES") {
            operator = Constants.AdvancedFilter.IN
        } else if (notILikePropertiesMap.containsKey(operatorEnum.value()) && value.toUpperCase() != "YES") {
            operator = Constants.AdvancedFilter.NOT_IN
        } else {
            operator = Constants.AdvancedFilter.NOT_IN
        }

        return """{alias}.id $operator (${aliasPropertiesMap[fieldName].subQueryGlobal} union
                                            ${aliasPropertiesMap[fieldName].subQueryAlert} )
                               """
    }

    def addTaskCompletionNotification(ExecutedConfiguration executedConfiguration, User currentUser = null, Map caseInfoMap) {
        List<User> notificationRecipients = currentUser ? [currentUser] : [executedConfiguration.assignedTo]
        try {
            Map content = [
                    aggExecutionId: caseInfoMap.aggExecutionId,
                    aggAlertId    : caseInfoMap.aggAlert.id,
                    aggCountType  : "$executedConfiguration.aggCountType",
                    productId     : caseInfoMap.productId,
                    ptCode        : caseInfoMap.ptCode,
                    type          : caseInfoMap.type,
                    typeFlag      : caseInfoMap.typeFlag,
                    isArchived    : caseInfoMap.isArchived
            ]
            String messageArgs = "$executedConfiguration.name"
            def url = getDetailsUrlMap(executedConfiguration.type, executedConfiguration.adhocRun)
            NotificationLevel status = NotificationLevel.INFO
            String message = "app.notification.completed"
            String inboxType = "Case Series Drilldown"
            InboxLog inboxLog
            notificationRecipients.each { User notificationRecipient ->
                inboxLog = new InboxLog(notificationUserId: notificationRecipient.id, level: status, message: message,
                        messageArgs: messageArgs, type: inboxType, subject: messageSource.getMessage(message, [messageArgs].toArray(),
                        notificationRecipient.preference.locale), content: content as String, createdOn: new Date(), inboxUserId: notificationRecipient.id, isNotification: true,
                        executedConfigId: executedConfiguration.id, detailUrl: url)
                inboxLog.save(flush: true, failOnError: true)
                notificationHelper.pushNotification(inboxLog)

            }
        } catch (Throwable e) {
            log.error("Error creating Notification: ${e.message}", e)
        }
    }

    boolean saveCaseSeriesInMart(List<Map> caseAndVersionNumberList, ExecutedConfiguration executedConfiguration, Long seriesId, boolean isCumulative = false, String selectedDataSource = null) {
        log.info("saveCaseSeriesInMart process started.")
        Sql sql
        Set<String> warnings = []
        Integer result = 0
        String dataSource = selectedDataSource ?: executedConfiguration.selectedDatasource
        def dataSourceObj = signalDataSourceService.getReportConnection(dataSource)
        try {
            sql = new Sql(dataSourceObj)
            Long startTime = System.currentTimeMillis()
            if (caseAndVersionNumberList) {
                sql.execute(sqlGenerationService.delPrevGTTSForCaseSeries())
                sql.withBatch(1000) { stmt ->
                    caseAndVersionNumberList.each { Map caseVersionMap ->
                        stmt.addBatch("insert into GTT_FILTER_KEY_VALUES(CODE,TEXT) values(${caseVersionMap.versionNumber},'${caseVersionMap.caseNumber?.replaceAll("'","''")}')")
                    }
                }
            }

            String endDate = isCumulative ? new Date().format(SqlGenerationService.DATE_FMT) : executedConfiguration?.executedAlertDateRangeInformation?.reportStartAndEndDate[1]?.format(SqlGenerationService.DATE_FMT)
            String dateRangeType = executedConfiguration.dateRangeType.value()
            String evaluateDateAs = executedConfiguration.evaluateDateAs
            String versionAsOfDate = executedConfiguration.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF ? executedConfiguration?.asOfVersionDate?.format(SqlGenerationService.DATE_FMT) : null
            Integer includeLockedVersion = executedConfiguration.includeLockedVersion ? 1 : 0
            String owner = getOwner(dataSource)
            sql.call("{?= call PKG_QUERY_HANDLER.f_save_cstm_case_series(?,?,?,?,?,?,?,?,?,?,?,?)}",
                    [Sql.NUMERIC, executedConfiguration.name, executedConfiguration.version, executedConfiguration.owner.id, seriesId, null, 1, endDate, dateRangeType, evaluateDateAs, versionAsOfDate, includeLockedVersion, owner]) { res ->
                result = res
            }
            if (result != 0) {
                warnings = sql.rows("SELECT * from GTT_REPORT_INPUT_PARAMS").collect { it.PARAM_KEY }
            }
            Long endTime = System.currentTimeMillis()
            log.info("Time taken to save the Case Series \'${executedConfiguration.name}\' in DB : " + ((endTime - startTime) / 1000) + "secs")
            log.info("Out of ${caseAndVersionNumberList.size()} cases ${(caseAndVersionNumberList - warnings.collect { "'${it}'" }).size()} are saved in DB")
            log.info("Cases not saved in DB : ${warnings}")
        } catch (SQLException e) {
            e.printStackTrace()
        } finally {
            sql?.close()
        }
        warnings.size() == 0
    }

    List<Map> fetchPrevPeriodSCAlerts(def domain, List<Long> prevExecConfigId) {
        List<Map> prevSingleCaseAlerts = []
        if (prevExecConfigId) {
            prevSingleCaseAlerts = domain.createCriteria().list {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    if (domain != SingleOnDemandAlert) {
                        property('dueDate', 'dueDate')
                    }
                    property('attributes', 'attributes')
                    property('caseNumber', 'caseNumber')
                    property('followUpNumber', 'followUpNumber')
                    property('initialFu', 'initialFu')
                    if (domain in [SingleCaseAlert, ArchivedSingleCaseAlert]) {
                        'disposition' {
                            property('id', 'dispositionId')
                        }
                        'priority' {
                            property('id', 'priorityId')
                        }
                        property("assignedTo.id", 'assignedToId')
                        property("assignedToGroup.id", 'assignedToGroupId')
                    }
                    property('id', 'id')
                }
                'or' {
                    prevExecConfigId.collate(1000).each {
                        'in'("executedAlertConfiguration.id", it)
                    }
                }
                order("id", "desc")
            } as List<Map>
        }
        prevSingleCaseAlerts.unique { it.caseNumber }
    }

    def fetchPrevExecConfigId(ExecutedConfiguration executedConfiguration, Configuration configuration, boolean isCaseSeriesId = false, boolean isCumulative = false) {
        List<Integer> reportVersions = ExecutionStatus.findAllByConfigIdAndExecutionStatusInListAndType(configuration?.id, [ReportExecutionStatus.COMPLETED, ReportExecutionStatus.DELIVERING], executedConfiguration.type).reportVersion.collect {
            (int) it
        }

        def execConfigIds = ExecutedConfiguration.createCriteria().list() {
            projections {
                property("id")
                if (isCaseSeriesId && executedConfiguration.type == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                    property("pvrCaseSeriesId")
                }
            }
            eq("configId", executedConfiguration.configId)
            'owner' {
                eq("id", executedConfiguration.owner.id)
            }

            eq("type", executedConfiguration.type)
            eq("adhocRun", executedConfiguration.adhocRun)
            eq("isDeleted", false)
            if (reportVersions.size() > 0) {
                or {
                    reportVersions.collate(999).each {
                        'in'('numOfExecutions', it)
                    }
                }
            }
            if (!isCumulative) {
                'executedAlertDateRangeInformation' {
                    'or' {
                        'ne'('dateRangeStartAbsolute', executedConfiguration.executedAlertDateRangeInformation.dateRangeStartAbsolute)
                        'ne'('dateRangeEndAbsolute', executedConfiguration.executedAlertDateRangeInformation.dateRangeEndAbsolute)
                    }
                }
            }
            ne("id", executedConfiguration.id)
            order("id", "desc")
        }
        execConfigIds
    }

    void saveLogsInFile(StringBuffer logStringBuilder, String filename) {
        // Constructing the file path
        String filePath = Holders.config.externalDirectory + "br_logs/"  + filename + ".gz";

        try {
            // Creating the file object
            File file = new File(filePath);

            // Creating directories if they don't exist
            file.getParentFile().mkdirs();

            // Writing string to the compressed file
            FileOutputStream fos = new FileOutputStream(file);
            GZIPOutputStream gzipOS = new GZIPOutputStream(fos);
            gzipOS.write(logStringBuilder.toString().getBytes());
            gzipOS.close();

            System.out.println("Log written to compressed file: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void setBadgeValueForSCA(def alert, boolean isPreviousReportingPeriod) {
        if (alert.isNew) {
            alert.badge = Constants.Badges.NEW
        } else if (isPreviousReportingPeriod) {
            alert.badge = Constants.Badges.PENDING_REVIEW
        }
    }

    List<?> getResultList(Class className, String sql, Session session) {
        SQLQuery sqlQuery = session.createSQLQuery(sql)
        addScalarProperties(sqlQuery, className)
        sqlQuery.setResultTransformer(Transformers.aliasToBean(className))
        session.flush()
        session.clear()
        sqlQuery.list()
    }

    Integer getResultListCount(String sql, Session session) {
        SQLQuery sqlQuery = session.createSQLQuery(sql)
        sqlQuery.uniqueResult() as Integer
    }

    void addScalarProperties(SQLQuery query, Class className) {
        Field[] fields = className.getDeclaredFields()
        fields.each {
            if (it.type == String) {
                query.addScalar(it.name, new StringType())
            } else if (it.type == Long) {
                query.addScalar(it.name, new LongType())
            } else if (it.type == Double) {
                query.addScalar(it.name, new DoubleType())
            } else if (it.type == Integer) {
                query.addScalar(it.name, new IntegerType())
            } else if (it.type == Boolean) {
                query.addScalar(it.name, new BooleanType())
            } else if (it.type == Date) {
                query.addScalar(it.name)
            }
        }
    }

    @Transactional
    String prepareUpdateCaseSeriesHql(boolean isCumulativeCaseSeries) {
        StringBuilder updateCaseSeriesHql = new StringBuilder("Update ExecutedConfiguration set pvrCaseSeriesId = :pvrCaseSeriesId ")
        if (isCumulativeCaseSeries) {
            updateCaseSeriesHql.append(" ,pvrCumulativeCaseSeriesId = :pvrCaseSeriesId")
        }
        updateCaseSeriesHql.append(" where id = :id").toString()
    }

    boolean isCumCaseSeriesReport(ExecutedConfiguration executedConfiguration) {
        return executedConfiguration.executedTemplateQueries?.any {
            it.executedDateRangeInformationForTemplateQuery.dateRangeEnum == DateRangeEnum.CUMULATIVE && executedConfiguration.executedAlertDateRangeInformation.dateRangeEnum != DateRangeEnum.CUMULATIVE
        }
    }

    boolean isCumCaseSeriesSpotfire(ExecutedConfiguration executedConfiguration) {
        boolean isSpotfireCummCaseSeries
        if (executedConfiguration.spotfireSettings) {
            SpotfireSettingsDTO settings = SpotfireSettingsDTO.fromJson(executedConfiguration.spotfireSettings)
            isSpotfireCummCaseSeries = settings.rangeType.any {
                it in [DateRangeEnum.CUMULATIVE_SAFETYDB, DateRangeEnum.CUMULATIVE] && executedConfiguration.executedAlertDateRangeInformation.dateRangeEnum != DateRangeEnum.CUMULATIVE
            }
        }
        isSpotfireCummCaseSeries
    }

    List<DateRangeEnum> dateRangeFaersSpotfire(ExecutedConfiguration executedConfiguration) {
        SpotfireSettingsDTO settings = SpotfireSettingsDTO.fromJson(executedConfiguration.spotfireSettings)
        settings.rangeType
    }

    List<DateRangeEnum> dateRangeVigibaseSpotfire(ExecutedConfiguration executedConfiguration) {
        SpotfireSettingsDTO settings = SpotfireSettingsDTO.fromJson(executedConfiguration.spotfireSettings)
        settings.rangeType
    }

    List<DateRangeEnum> dateRangeVaersSpotfire(ExecutedConfiguration executedConfiguration) {
        SpotfireSettingsDTO settings = SpotfireSettingsDTO.fromJson(executedConfiguration.spotfireSettings)
        settings.rangeType
    }

    void generateSpotfireReport(ExecutedConfiguration executedConfiguration, ExecutedConfiguration executedConfigurationFaers = null) {
        if (executedConfiguration.spotfireSettings) {
            spotfireService.generateSpotfireReport(executedConfiguration, executedConfigurationFaers)
        }
    }

    void generateReport(ExecutedConfiguration executedConfiguration) {
        if (executedConfiguration.executedTemplateQueries?.size() > 0) {
            reportIntegrationService.runReport(executedConfiguration.id)
        }
    }

    @Transactional
    void sendReportingErrorNotifications(ExecutedConfiguration executedConfiguration, boolean isGenerateReport, boolean isSpotfire) {
        log.error("Error occured while generating case series")
        if (isGenerateReport) {
            ExecutedConfiguration.executeUpdate("Update ExecutedConfiguration set reportExecutionStatus = :executionStatus where id = :id",
                    [executionStatus: ReportExecutionStatus.ERROR, id: executedConfiguration.id])
            reportIntegrationService.saveConfigurationAndReportNotificationAndActivity(executedConfiguration)
        }
        if (isSpotfire) {
            spotfireService.sendErrorNotification(executedConfiguration)
        }
    }

    @Transactional
    List getPrevExConfigIds(ExecutedConfiguration executedConfiguration, Long confiId, String type) {
        List<Integer> reportVersions = ExecutionStatus.findAllByConfigIdAndExecutionStatusInListAndType(confiId, [ReportExecutionStatus.COMPLETED, ReportExecutionStatus.DELIVERING], executedConfiguration.type).reportVersion.collect {
            (int) it
        }

        List result = ExecutedConfiguration.createCriteria().list() {
            projections {
                property("id")
            }
            eq("name", executedConfiguration.name)
            'owner' {
                eq("id", executedConfiguration.owner.id)
            }

            eq("type", executedConfiguration.type)
            eq("adhocRun", executedConfiguration.adhocRun)
            eq("isDeleted", false)
            eq("type", type)
            if (reportVersions.size() > 0) {
                or {
                    reportVersions.collate(999).each {
                        'in'('numOfExecutions', it)
                    }
                }
            }
            ne("id", executedConfiguration.id)
            order("id", "desc")
        }
        result
    }

    @Transactional
    List getEvdasPrevExConfigIds(ExecutedEvdasConfiguration executedConfiguration, Long confiId) {
        List<Integer> reportVersions = ExecutionStatus.findAllByConfigIdAndExecutionStatusInList(confiId, [ReportExecutionStatus.COMPLETED, ReportExecutionStatus.DELIVERING]).reportVersion.collect {
            (int) it
        }

        List result = ExecutedEvdasConfiguration.createCriteria().list() {
            projections {
                property("id")
            }
            eq("name", executedConfiguration.name)
            'owner' {
                eq("id", executedConfiguration.owner.id)
            }
            eq("adhocRun", executedConfiguration.adhocRun)
            eq("isDeleted", false)
            if (reportVersions.size() > 0) {
                or {
                    reportVersions.collate(999).each {
                        'in'('numOfExecutions', it)
                    }
                }
            }
            ne("id", executedConfiguration.id)
            order("id", "desc")
        }
        result
    }

    void fetchFirstExecutionDate(ExecutedConfiguration executedConfiguration, Configuration configuration) {
        if(!executedConfiguration) {
            return
        }
        List<Integer> reportVersions = ExecutionStatus.findAllByConfigIdAndExecutionStatusInListAndType(configuration.id, [ReportExecutionStatus.COMPLETED, ReportExecutionStatus.DELIVERING], executedConfiguration?.type).reportVersion.collect {
            (int) it
        }

        Date firstExecDate = ExecutedConfiguration.createCriteria().get() {
            projections {
                'executedAlertDateRangeInformation' {
                    property("dateRangeStartAbsolute")
                }
            }
            eq("name", executedConfiguration.name)
            'owner' {
                eq("id", executedConfiguration.owner.id)
            }

            eq("type", executedConfiguration.type)
            eq("adhocRun", executedConfiguration.adhocRun)
            eq("isDeleted", false)
            if (reportVersions.size() > 0) {
                or {
                    reportVersions.collate(999).each {
                        'in'('numOfExecutions', it)
                    }
                }
            }
            'executedAlertDateRangeInformation' {
                'or' {
                    'ne'('dateRangeStartAbsoluteDelta', executedConfiguration.executedAlertDateRangeInformation.dateRangeStartAbsoluteDelta)
                    'ne'('dateRangeEndAbsoluteDelta', executedConfiguration.executedAlertDateRangeInformation.dateRangeEndAbsoluteDelta)
                }
            }
            ne("id", executedConfiguration.id)
            order("id", "asc")
            maxResults(1)
        } as Date
        Date missedCaseStartDate
        Date startDate
        if (grailsApplication.config.missed.cases.include.start.date) {
            missedCaseStartDate = new Date().parse(DateUtil.DATETIME_FMT, grailsApplication.config.missed.cases.include.start.date + " 00:00:01")
        }
        if (firstExecDate) {
            startDate = missedCaseStartDate ? (firstExecDate.before(missedCaseStartDate) ? firstExecDate : missedCaseStartDate) : firstExecDate
        } else {
            startDate = executedConfiguration.executedAlertDateRangeInformation.reportStartAndEndDate[0]
            startDate = missedCaseStartDate ? (startDate.before(missedCaseStartDate) ? startDate : missedCaseStartDate) : startDate
        }
        dataObjectService.setFirstVersionExecMap(executedConfiguration.id, startDate)
    }

    def fetchAdvancedFilterDetails(AdvancedFilter advancedFilter) {
        String shared = (userService.currentUserId == advancedFilter.userId) ? '' : Constants.Commons.SHARED
        ['name': advancedFilter.name + shared, 'id': advancedFilter.id] as JSON
    }

    void bulkUpdateSuperseded(List<Long> alertIdList) {
        alertIdList.collate(1000).each {
            SingleCaseAlert.executeUpdate("Update SingleCaseAlert set supersededFlag = :superseded " +
                    "where id in (:alertIdList) ",
                    [superseded: "Review Not Required", alertIdList: it])
        }
    }

    def generateDomainName(boolean isArchived) {
        isArchived ? ArchivedSingleCaseAlert : SingleCaseAlert
    }

    List generateSearchableColumns(boolean isGroupBySmq, String callingScreen, String selectedDatasource = null) {
        List<String> availableDataSources = grailsApplication.config.pvsignal.supported.datasource.call()
        boolean isFaersEnabled = availableDataSources.contains(Constants.DataSource.FAERS)
        boolean isEvdasEnabled = availableDataSources.contains(Constants.DataSource.EUDRA)
        boolean isVaersEnabled = availableDataSources.contains(Constants.DataSource.VAERS)
        boolean isVigibaseEnabled = availableDataSources.contains(Constants.DataSource.VIGIBASE)
        boolean isPriorityEnabled = grailsApplication.config.alert.priority.enable
        boolean isDssEnabled = grailsApplication.config.statistics.enable.dss
        Map flagsMap = [
                prrFlag            : Holders.config.statistics.enable.prr,
                prrFlagFaers       : isFaersEnabled && Holders.config.statistics.faers.enable.prr,
                prrFlagVaers       : isVaersEnabled && Holders.config.statistics.vaers.enable.prr,
                prrFlagVigibase    : isVigibaseEnabled && Holders.config.statistics.vigibase.enable.prr,
                rorFlag            : Holders.config.statistics.enable.ror,
                rorFlagFaers       : isFaersEnabled && Holders.config.statistics.faers.enable.ror,
                rorFlagVaers       : isVaersEnabled && Holders.config.statistics.vaers.enable.ror,
                rorFlagVigibase    : isVigibaseEnabled && Holders.config.statistics.vigibase.enable.ror,
                ebgmFlag           : Holders.config.statistics.enable.ebgm,
                ebgmFlagFaers      : isFaersEnabled && Holders.config.statistics.faers.enable.ebgm,
                ebgmFlagVaers      : isVaersEnabled && Holders.config.statistics.vaers.enable.ebgm,
                ebgmFlagVigibase   : isVigibaseEnabled && Holders.config.statistics.vigibase.enable.ebgm,
                customFieldsEnabled: Holders.config.custom.qualitative.fields.enabled,
        ]
        List<Integer> filterIndex = []
        Map<Integer, String> filterIndexMap = [:]
        int prodColumns
        if (callingScreen == Constants.Commons.DASHBOARD) {
            prodColumns = isPriorityEnabled ? 5 : 4
        } else {
            prodColumns = isPriorityEnabled ? 4 : 3
        }

        List newFields = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', null).findAll {
            it.enabled == true && it.isNewColumn == false && it.type != "subGroup"
        }

        List subGroupFields = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', null).findAll {
            it.enabled == true && it.type == "subGroup"
        }

        List newDynamicFields = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', true).findAll {
            it.enabled == true && it.isNewColumn == true && it.type != "subGroup"
        }

        if (isGroupBySmq) {
            newFields.removeAll {
                it.name in ['hlt', 'hlgt', 'smqNarrow']
            }
        }
        List<Map> filterColumns = newFields + newDynamicFields + subGroupFields

        if (!isFaersEnabled) {
            filterColumns.removeAll {
                it.name.endsWith(Constants.ViewsDataSourceLabels.FAERS)
            }
        }

        if (!isVaersEnabled) {
            filterColumns.removeAll {
                it.name.endsWith(Constants.ViewsDataSourceLabels.VAERS)
            }
        }

        if (!isVigibaseEnabled) {
            filterColumns.removeAll {
                it.name.endsWith(Constants.ViewsDataSourceLabels.VIGIBASE)
            }
        }

        if (!isEvdasEnabled) {
            filterColumns.removeAll {
                it.name.endsWith(Constants.ViewsDataSourceLabels.EVDAS)
            }
        }
        if (!isDssEnabled) {
            filterColumns.removeIf {
                it.name in [Constants.AggregateAlertFields.RATIONALE, Constants.AggregateAlertFields.PEC_IMP_NUM_HIGH]
            }
        }
        if(!flagsMap.prrFlag){
            filterColumns.removeIf{ it.name in ["prrLCI", "prrValue"] }
        }
        if(!flagsMap.prrFlagFaers){
            filterColumns.removeIf{ it.name in ["prrLCIFaers", "prrValueFaers"] }
        }
        if(!flagsMap.prrFlagVaers){
            filterColumns.removeIf{ it.name in ["prrLCIVaers", "prrValueVaers"] }
        }
        if(!flagsMap.prrFlagVigibase){
            filterColumns.removeIf{ it.name in ["prrLCIVigibase", "prrValueVigibase"] }
        }
        if(!flagsMap.rorFlag){
            filterColumns.removeIf{ it.name in ["rorLCI", "rorValue"] }
        }
        if(!flagsMap.rorFlagFaers){
            filterColumns.removeIf{ it.name in ["rorLCIFaers", "rorValueFaers"] }
        }
        if(!flagsMap.rorFlagVaers){
            filterColumns.removeIf{ it.name in ["rorLCIVaers", "rorValueVaers"] }
        }
        if(!flagsMap.rorFlagVigibase){
            filterColumns.removeIf{ it.name in ["rorLCIVigibase", "rorValueVigibase"] }
        }
        if(!(flagsMap.prrFlag || flagsMap.rorFlag || flagsMap.ebgmFlag)){
            filterColumns.removeIf{ it.name in  ["aValue", "bValue", "cValue", "dValue"] }
        }
        if(!(flagsMap.prrFlag && flagsMap.rorFlag)){
            filterColumns.removeIf{ it.name in  ["chiSquare"] }
        }
        if(!(flagsMap.prrFlagFaers && flagsMap.rorFlagFaers)){
            filterColumns.removeIf{ it.name in  ["chiSquareFaers"] }
        }
        if(!(flagsMap.prrFlagVaers && flagsMap.rorFlagVaers)){
            filterColumns.removeIf{ it.name in  ["chiSquareVaers"] }
        }
        if(!(flagsMap.prrFlagVigibase && flagsMap.rorFlagVigibase)){
            filterColumns.removeIf{ it.name in  ["chiSquareVigibase"] }
        }
        if(!flagsMap.ebgmFlag){
            filterColumns.removeIf{ it.name in ["ebgm", "eb05","eValue", "rrValue"]}
        }
        if(!flagsMap.ebgmFlagFaers){
            filterColumns.removeIf{ it.name in ["ebgmFaers", "eb05Faers"]}
        }
        if(!flagsMap.ebgmFlagVaers){
            filterColumns.removeIf{ it.name in ["ebgmVaers", "eb05Vaers"]}
        }
        if(!flagsMap.ebgmFlagVigibase){
            filterColumns.removeIf{ it.name in ["ebgmVigibase", "eb05Vigibase"]}
        }

        int index = prodColumns-3

        filterColumns.each { Map columnMap ->
            if (columnMap.name in ["hlt", "hlgt", "smqNarrow"]) {
                columnMap.type = "text"
            }
            if (columnMap.isFilter?.toBoolean() && columnMap.name != 'name') {
                filterIndex.add(index)
                String type = (columnMap.type == 'subGroup' ? 'count' : columnMap.type)
                filterIndexMap.put(index, type)
                if(columnMap.name == 'productName'){
                    filterIndexMap.put(index, 'select')
                }
                index++
            } else if(columnMap.isFilter?.toBoolean() && columnMap.name == 'name'){
                if(callingScreen == Constants.Commons.DASHBOARD){
                    filterIndex.add(index)
                    filterIndexMap.put(index, 'select')
                    index++
                }
            }else if (columnMap.containsKey("isVisible")) {
                index++
            }
        }
        [filterIndex, filterIndexMap]
    }
    List generateSearchableColumnsJader(boolean isGroupBySmq){
        List<Integer> filterIndex = []
        Map<Integer, String> filterIndexMap = [:]
        List<Map> filterColumns = alertFieldService.getJaderColumnList(Constants.DataSource.JADER, isGroupBySmq)
        int index = 2
        filterColumns.each { Map columnMap ->
            if (columnMap.isFilter?.toBoolean()) {
                filterIndex.add(index)
                if(columnMap.name == 'productName'){
                    filterIndexMap.put(index,'select')
                }else if(columnMap.type == 'countStacked' || columnMap.type == 'score'){
                    filterIndexMap.put(index,'count')
                }else{
                    filterIndexMap.put(index, columnMap.type)
                }
                index++
            } else if (columnMap.containsKey("isVisible")) {
                index++
            }
        }
        [filterIndex, filterIndexMap]
    }

    List generateSearchableColumnsForOnDemandRuns(boolean isGroupBySmq, boolean isFaersEnabled, boolean isPvaEnabled, boolean isVaersEnabled, boolean isVigibaseEnabled,boolean isJaderEnabled,boolean isDataMining) {
        List<Integer> filterIndex = []
        Map<Integer, String> filterIndexMap = [:]
        List<Map> filterColumns = []
        if (isVaersEnabled) {
            filterColumns = alertFieldService.getAggOnDemandColumnList(Constants.DataSource.VAERS, isGroupBySmq)
        } else if (isVigibaseEnabled) {
            filterColumns = alertFieldService.getAggOnDemandColumnList(Constants.DataSource.VIGIBASE, isGroupBySmq)
        } else if (isFaersEnabled) {
            filterColumns = alertFieldService.getAggOnDemandColumnList(Constants.DataSource.FAERS, isGroupBySmq)
        } else if (isJaderEnabled) {
            filterColumns = alertFieldService.getAggOnDemandColumnList(Constants.DataSource.JADER, isGroupBySmq)
        } else {
            filterColumns = alertFieldService.getAggOnDemandColumnList(Constants.DataSource.PVA, isGroupBySmq)
        }
        int index = 1
        filterColumns.each { Map columnMap ->
            if (columnMap.isFilter?.toBoolean()) {
                filterIndex.add(index)
                if (columnMap.name in ["hlt", "hlgt", "smqNarrow"]) {
                    columnMap.type = "text"
                }
                if(columnMap.name == 'productName'){
                    filterIndexMap.put(index,'select')
                }else if(columnMap.type == 'countStacked' || columnMap.type == 'score'){
                    filterIndexMap.put(index,'count')
                }else{
                    filterIndexMap.put(index, columnMap.type)
                }
                index++
            } else if (columnMap.containsKey("isVisible")) {
                index++
            }
        }


        Map<String, Map> subGroupMap = cacheService.getSubGroupMap()
        Map<String,String> prrRorSubGroupMap =  cacheService.allOtherSubGroupColumnUIList(Constants.DataSource.PVA)
        Map<String,String> relativeSubGroupMap = cacheService.relativeSubGroupColumnUIList(Constants.DataSource.PVA)
        List subGroupColumnInfo = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT').findAll{it.type=="subGroup" && it.enabled== true}.collect{it.name}
        prrRorSubGroupMap?.values()?.retainAll(subGroupColumnInfo)
        relativeSubGroupMap?.values()?.retainAll(subGroupColumnInfo)
        if (isPvaEnabled && !isDataMining) {
            [Holders.config.subgrouping.ageGroup.name, Holders.config.subgrouping.gender.name].each {
                subGroupMap[it]?.each { key, value ->
                    String ebgmKey = "ebgm" + value
                    String eb05Key = "eb05" + value
                    String eb95Key = "eb95" + value
                    if(ebgmKey in subGroupColumnInfo){
                        filterIndex.add(index)
                        filterIndexMap.put(index, "count")
                        index++
                    }
                    if(eb05Key in subGroupColumnInfo){
                        filterIndex.add(index)
                        filterIndexMap.put(index, "count")
                        index++
                    }
                    if(eb95Key in subGroupColumnInfo){
                        filterIndex.add(index)
                        filterIndexMap.put(index, "count")
                        index++
                    }
                }
            }
            prrRorSubGroupMap?.each{key,value ->
                filterIndex.add(index)
                filterIndexMap.put(index, "count")
                index++
            }
            relativeSubGroupMap?.each{key,value ->
                filterIndex.add(index)
                filterIndexMap.put(index, "count")
                index++
            }
        }

        if (isFaersEnabled && !isDataMining) {
            [Holders.config.subgrouping.faers.ageGroup.name, Holders.config.subgrouping.faers.gender.name].each {
                subGroupMap[it]?.each { key,value ->
                    String ebgmKey = "ebgm" + value + "Faers"
                    String eb05Key = "eb05" + value + "Faers"
                    String eb95Key = "eb95" + value + "Faers"
                    if(ebgmKey in subGroupColumnInfo){
                        filterIndex.add(index)
                        filterIndexMap.put(index, "count")
                        index++
                    }
                    if(eb05Key in subGroupColumnInfo){
                        filterIndex.add(index)
                        filterIndexMap.put(index, "count")
                        index++
                    }
                    if(eb95Key in subGroupColumnInfo){
                        filterIndex.add(index)
                        filterIndexMap.put(index, "count")
                        index++
                    }
                }
            }
        }
        [filterIndex, filterIndexMap]
    }

    String getFaersDbUserName() {
        grailsApplication.config.dataSources.faers.username
    }

    String getVigibaseDbUserName() {
        grailsApplication.config.dataSources.vigibase.username
    }

    String getVaersDbUserName() {
        grailsApplication.config.dataSources.vaers.username
    }

    void saveAlertDataInFile(ArrayList<Map> alertList, String fileName) throws Exception {
        FileOutputStream fis
        ObjectOutputStream ois
        try {
            fis = new FileOutputStream(fileName)
            ois = new ObjectOutputStream(fis)
            ois.writeObject(alertList)
        } catch (Throwable throwable) {
            throw throwable
        } finally {
            fis?.close()
            ois?.close()
        }
    }

    List<Map> loadAlertDataFromFile(String filePath) throws Exception {
        FileInputStream fis
        ObjectInputStream ois
        List<Map> alertDataList = []
        try {
            File file = new File(filePath)
            if (file.exists()) {
                fis = new FileInputStream(filePath)
                ois = new ObjectInputStream(fis)
                alertDataList = (ArrayList<Map>) ois.readObject()
            } else {
                log.info("File not found: " + filePath)
            }
        } catch (Throwable throwable) {
            throw throwable
        } finally {
            fis?.close()
            ois?.close()
        }
        alertDataList
    }

    void saveLiteratureDataInFile(String alertData, String filePath) {
        try {
            File file = new File(filePath)
            file << alertData
        } catch (Throwable throwable) {
            throw throwable
        }
    }

    GPathResult loadLiteratureDataFromFile(String filePath) {
        try {
            File file = new File(filePath)
            return literatureAlertService.getParsedData(file.text)
        } catch (Throwable throwable) {
            throw throwable
        }
    }

    String exceptionString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter()
        throwable.printStackTrace(new PrintWriter(stringWriter))
        stringWriter.toString()
    }

    void createAlertFilesFolder() {
        File baseFolder = new File(grailsApplication.config.signal.alert.file as String)
        if (!baseFolder.exists()) {
            log.debug("Alert base folder not found, creating it.")
            baseFolder.mkdir()
        }
    }

    void executeIncompleteJobs() {
        log.info("Execute Incomplete jobs")
        List domain = [Configuration, EvdasConfiguration, LiteratureConfiguration]
        def nodeName = hazelcastService.getName()
        log.info("Alerts running on Node: ${nodeName} will be killed.")
        domain.each { def domainName ->
                try {
                    String hqlStatement = configurationService.prepareHqlForIncompleteJob(domainName)
                    List<ExecutionStatus> executionStatusList = ExecutionStatus.executeQuery(hqlStatement)
                    executionStatusList.each { ExecutionStatus executionStatus ->
                        if (StringUtils.isBlank(executionStatus?.nodeName) || StringUtils.equalsIgnoreCase(executionStatus?.nodeName, nodeName)) {
                            if (executionStatus?.executionStatus != ReportExecutionStatus.COMPLETED) {
                                executionStatus.executionStatus = ReportExecutionStatus.ERROR
                                executionStatus.stackTrace = "Failed due to application restart."
                            }
                            if (domainName.hasProperty("templateQueries") && domainName.templateQueries &&
                                    executionStatus?.reportExecutionStatus != ReportExecutionStatus.COMPLETED) {
                                executionStatus.reportExecutionStatus = ReportExecutionStatus.ERROR
                            }
                            if (domainName.hasProperty("spotfireSettings") && domainName.spotfireSettings &&
                                    executionStatus?.spotfireExecutionStatus != ReportExecutionStatus.COMPLETED) {
                                executionStatus.spotfireExecutionStatus = ReportExecutionStatus.ERROR
                            }
                            executionStatus?.save(failOnError: true, flush: true)
                            def config = domainName.findById(executionStatus.configId)
                            if (config?.executing) {
                                config.executing = false
                                config.isEnabled = false
                                config.numOfExecutions = config.numOfExecutions ?: 1
                                config.save(failOnError: true, flush: true)
                            }
                            String selectedDataSource = config.selectedDatasource
                            List<String> dataSourcesList = selectedDataSource?.split(',')
                            dataSourcesList.each { dataSource ->
                                // Need to create app alert progress status if alerts are killed on restart so that temp db tables can be deleted.
                                appAlertProgressStatusService.createAppAlertProgressStatus(executionStatus, executionStatus.executedConfigId, dataSource, 0, 3, System.currentTimeMillis(), AlertProgressExecutionType.DATASOURCE)
                            }
                            log.info("${executionStatus.name} Killed.")
                        }
                    }

            } catch (Exception ex) {
                log.error("Validation Error" + ex.printStackTrace())
            }
        }
    }

    void executeMasterIncompleteJobs() {
        log.info("Execute Master Incomplete jobs")
        List<MasterConfiguration> masterConfigs = MasterConfiguration.findAllByExecuting(true)
        masterConfigs.each { config ->
            try{
                List<MasterExecutedConfiguration> masterExecutedConfigurationList = MasterExecutedConfiguration.findAllByMasterConfigId(config.id)
                // execution status of all master executed configuration should be updated instead of latest one
                masterExecutedConfigurationList?.each { MasterExecutedConfiguration masterExecutedConfiguration ->
                    ExecutionStatus executionStatus = ExecutionStatus.findByConfigIdAndExecutedConfigId(config.id, masterExecutedConfiguration.id)
                    if (executionStatus) {
                        if (executionStatus?.executionStatus != ReportExecutionStatus.COMPLETED) {
                            executionStatus.executionStatus = ReportExecutionStatus.ERROR
                            executionStatus.stackTrace = "Failed due to application restart."
                        }

                        if (executionStatus.reportExecutionStatus == ReportExecutionStatus.GENERATING ||
                                executionStatus.reportExecutionStatus == ReportExecutionStatus.SCHEDULED) {
                            executionStatus.reportExecutionStatus = ReportExecutionStatus.ERROR
                        }

                        if (executionStatus.spotfireExecutionStatus == ReportExecutionStatus.GENERATING ||
                                executionStatus.spotfireExecutionStatus == ReportExecutionStatus.SCHEDULED) {
                            executionStatus.spotfireExecutionStatus = ReportExecutionStatus.ERROR
                        }
                        String dataSource = masterExecutedConfiguration.datasource
                        appAlertProgressStatusService.createAppAlertProgressStatus(executionStatus, executionStatus.executedConfigId, dataSource ?: "pva", 0, 3, System.currentTimeMillis(), AlertProgressExecutionType.DATASOURCE)
                    }
                }
                config.executing = false
                config.isEnabled = true
                config.isResume = false
                config.save(failOnError: true, flush: true)
            } catch (Exception ex) {
                log.error("Master Validation Error:", ex)
            }
        }
        masterExecutorService.manageMasterExecutionQueue()
        childExecutorService.manageRunningChildExecutionQueue()
    }

    void updateDashboardMismatchCounts() {

        log.info("Update Dashboard counts")
        try {
            Boolean updateFlag = Holders.config.dashboard.count.updateFlag ?: false
            if (!updateFlag) {
                log.info("Update dashboard counts Flag is disabled")
                return
            }

            Sql sql = new Sql(signalDataSourceService.getReportConnection("dataSource"))
            List<Map> userDispCaseCountList = []
            Map<Long, Integer> userDispCaseCountsMap = [:]
            List<Map> groupDispCaseCountList = []
            Map<Long, Map> groupDispCaseCountsMap = [:]
            List<Map> userDueDateCaseCountsList = []
            Map<String, Integer> userDueDateCaseCountsMap = [:]
            List<Map> dueDateGroupCaseCountList = []
            Map<Long, Map> dueDateGroupCaseCountsMap = [:]

            List<Map> userDispPECountList = []
            Map<Long, Integer> userDispPECountsMap = [:]
            List<Map> groupDispPECountList = []
            Map<Long, Map> groupDispPECountsMap = [:]
            List<Map> userDueDatePECountsList = []
            Map<String, Integer> userDueDatePECountsMap = [:]
            List<Map> dueDateGroupPECountList = []
            Map<Long, Map> dueDateGroupPECountsMap = [:]


            sql.eachRow(SignalQueryHelper.singleCaseAlert_dashboard_by_disposition(true), []) { row ->
                userDispCaseCountList.add([dispositionId: row[0], assignedToId: row[1], workflowGroupId: row[2], count: row[3]])
            }

            sql.eachRow(SignalQueryHelper.singleCaseAlert_dashboard_by_disposition(false), []) { row ->
                groupDispCaseCountList.add([dispositionId: row[0], assignedToGroupId: row[1], workflowGroupId: row[2], count: row[3]])
            }

            sql.eachRow(SignalQueryHelper.singleCaseAlert_dashboard_due_date(true), []) { row ->
                userDueDateCaseCountsList.add([due_date: row[0], assignedToId: row[1], workflowGroupId: row[2], count: row[3]])
            }

            sql.eachRow(SignalQueryHelper.singleCaseAlert_dashboard_due_date(false), []) { row ->
                dueDateGroupCaseCountList.add([due_date: row[0], assignedToGroupId: row[1], workflowGroupId: row[2], count: row[3]])
            }

            sql.eachRow(SignalQueryHelper.aggCaseAlert_dashboard_by_disposition(true), []) { row ->
                userDispPECountList.add([dispositionId: row[0], assignedToId: row[1], workflowGroupId: row[2], count: row[3]])
            }

            sql.eachRow(SignalQueryHelper.aggCaseAlert_dashboard_by_disposition(false), []) { row ->
                groupDispPECountList.add([dispositionId: row[0], assignedToGroupId: row[1], workflowGroupId: row[2], count: row[3]])
            }

            sql.eachRow(SignalQueryHelper.aggCaseAlert_dashboard_due_date(true), []) { row ->
                userDueDatePECountsList.add([due_date: row[0], assignedToId: row[1], workflowGroupId: row[2], count: row[3]])
            }

            sql.eachRow(SignalQueryHelper.aggCaseAlert_dashboard_due_date(false), []) { row ->
                dueDateGroupPECountList.add([due_date: row[0], assignedToGroupId: row[1], workflowGroupId: row[2], count: row[3]])
            }

            User.list().each { user ->

                UserDashboardCounts userDashboardCounts = UserDashboardCounts.get(user.id)
                if (userDashboardCounts) {
                    userDashboardCounts.delete(flush: true)
                }

                Group workflowgroup = user.workflowGroup
                List<Long> groupIdList = user.groups.findAll { it.groupType != GroupType.WORKFLOW_GROUP }.id
                groupIdList.each { id ->
                    Map dispCountMap = [:]
                    Map dueDateCountMap = [:]
                    Map dispPECountMap = [:]
                    Map dueDatePECountMap = [:]

                    groupDispCaseCountList.findAll {
                        it.assignedToGroupId == id && it.workflowGroupId == workflowgroup.id
                    }.each {
                        dispCountMap.put(it.dispositionId, it.count)
                    }

                    if (dispCountMap) {
                        groupDispCaseCountsMap.put(id, dispCountMap)
                    }

                    dueDateGroupCaseCountList.findAll {
                        it.assignedToGroupId == id && it.workflowGroupId == workflowgroup.id
                    }.each {
                        dueDateCountMap.put(it.due_date, it.count)
                    }

                    if (dueDateCountMap) {
                        dueDateGroupCaseCountsMap.put(id, dueDateCountMap)
                    }

                    groupDispPECountList.findAll {
                        it.assignedToGroupId == id && it.workflowGroupId == workflowgroup.id
                    }.each {
                        dispPECountMap.put(it.dispositionId, it.count)
                    }

                    if (dispPECountMap) {
                        groupDispPECountsMap.put(id, dispPECountMap)
                    }

                    dueDateGroupPECountList.findAll {
                        it.assignedToGroupId == id && it.workflowGroupId == workflowgroup.id
                    }.each {
                        dueDatePECountMap.put(it.due_date, it.count)
                    }

                    if (dueDatePECountMap) {
                        dueDateGroupPECountsMap.put(id, dueDatePECountMap)
                    }
                }

                userDispCaseCountList.findAll {
                    it.assignedToId == user.id && it.workflowGroupId == workflowgroup.id
                }.each {
                    userDispCaseCountsMap.put(it.dispositionId, it.count)
                }

                userDueDateCaseCountsList.findAll {
                    it.assignedToId == user.id && it.workflowGroupId == workflowgroup.id
                }.each {
                    userDueDateCaseCountsMap.put(it.due_date, it.count)
                }

                userDispPECountList.findAll {
                    it.assignedToId == user.id && it.workflowGroupId == workflowgroup.id
                }.each {
                    userDispPECountsMap.put(it.dispositionId, it.count)
                }

                userDueDatePECountsList.findAll {
                    it.assignedToId == user.id && it.workflowGroupId == workflowgroup.id
                }.each {
                    userDueDatePECountsMap.put(it.due_date, it.count)
                }

                sql.execute("""INSERT INTO 
                                                  user_dashboard_counts (user_id, user_disp_case_counts, group_disp_case_counts, user_due_date_case_counts, group_due_date_case_counts, 
                                                  user_disppecounts, group_disppecounts, user_due_datepecounts,group_due_datepecounts)
                                                  VALUES (${user.id}, ${
                    userDispCaseCountsMap ? new JsonBuilder(userDispCaseCountsMap).toPrettyString() : null
                },
                                                          ${
                    groupDispCaseCountsMap ? new JsonBuilder(groupDispCaseCountsMap).toPrettyString() : null
                },
                                                          ${
                    userDueDateCaseCountsMap ? new JsonBuilder(userDueDateCaseCountsMap).toPrettyString() : null
                },
                                                          ${
                    dueDateGroupCaseCountsMap ? new JsonBuilder(dueDateGroupCaseCountsMap).toPrettyString() : null
                },
                                                          ${
                    userDispPECountsMap ? new JsonBuilder(userDispPECountsMap).toPrettyString() : null
                },
                                                          ${
                    groupDispPECountsMap ? new JsonBuilder(groupDispPECountsMap).toPrettyString() : null
                },
                                                          ${
                    userDueDatePECountsMap ? new JsonBuilder(userDueDatePECountsMap).toPrettyString() : null
                },
                                                          ${
                    dueDateGroupPECountsMap ? new JsonBuilder(dueDateGroupPECountsMap).toPrettyString() : null
                })
                                           """)
                userDispCaseCountsMap.clear()
                groupDispCaseCountsMap.clear()
                userDueDateCaseCountsMap.clear()
                dueDateGroupCaseCountsMap.clear()
                userDispPECountsMap.clear()
                groupDispPECountsMap.clear()
                userDueDatePECountsMap.clear()
                dueDateGroupPECountsMap.clear()
            }
        } catch (Exception ex) {
            log.info("Error updating dashboard count:" + ex.printStackTrace())
        }


    }


    String productSelectionValue(def executedConfiguration, boolean isProductLevel = false) {
        List data = []
        if (executedConfiguration.productGroupSelection) {
            data.addAll(getGroupNameFieldFromJson(executedConfiguration.productGroupSelection).split(","))
        }
        if (executedConfiguration.productSelection) {
            if (isProductLevel && pvsProductDictionaryService.isLevelGreaterThanProductLevel(executedConfiguration)) {
                data.addAll(getCacheService().getUpperHierarchyProductDictionaryCache(executedConfiguration.id)?.split(","))
            } else {
                data.addAll(getAllProductNameFieldFromJson(executedConfiguration.productSelection).split(","))
            }
        }
        return data.join(", ")
    }

    String eventSelectionValue(def executedConfiguration) {
        List data = []
        if (executedConfiguration.eventGroupSelection) {
            data.addAll(getGroupNameFieldFromJson(executedConfiguration.eventGroupSelection).split(","))
        }
        if (executedConfiguration.eventSelection) {
            data.addAll(getNameFieldFromJson(executedConfiguration.eventSelection).split(","))
        }
        return data.join(", ")
    }

    String productSelectionSignal(ValidatedSignal signal) {
        List data = []
        if (signal.productGroupSelection) {
            data.addAll(getGroupNameFieldFromJson(signal.productGroupSelection).split(","))
        }
        if (signal.products) {
            data.addAll(getAllProductNameFieldFromJson(signal.products).split(","))
        }
        return data.join(", ")
    }

    String eventSelectionSignal(ValidatedSignal signal) {
        List data = []
        if (signal.eventGroupSelection) {
            data.addAll(getGroupNameFieldFromJson(signal.eventGroupSelection).split(","))
        }
        if (signal.events) {
            data.addAll(getAllProductNameFieldFromJson(signal.events).split(","))
        }
        return data.join(", ")
    }

    String eventSelectionSignalWithSmq(ValidatedSignal signal) {
        List data = []
        if (signal.eventGroupSelection) {
            data.addAll(getGroupNameFieldFromJson(signal.eventGroupSelection).split(","))
        }
        if (signal.events) {
            data.addAll(getEventNameFieldFromJsonWithSmq(signal.events))
        }
        return data ? data.join(", "):"-"
    }

    def getEventNameFieldFromJsonWithSmq(def jsonString) {
        Map smqMap = [7: Constants.EventFields.BROAD, 8: Constants.EventFields.NARROW]
        def jsonObj = null
        List prdList = []
        String prdName
        if (jsonString) {
            jsonObj = parseJsonString(jsonString)
            if (!jsonObj)
                prdName = jsonString
            else {
                def prdVal = jsonObj.findAll { k, v ->
                    v.find { it.containsKey('name') || it.containsKey('genericName') }
                }
                prdVal.each { k, v ->
                    if ((k as Integer) in [7, 8]) {
                        v.each {
                            prdList.add(it?.name + " " + smqMap.get((k as Integer)))
                        }
                    } else {
                        v.each {
                            prdList.add(it?.name)
                        }
                    }
                }
                prdList = prdList?.sort()
            }
        }
        prdList
    }

    def copyBulkUsers() {
        List userList = User.createCriteria().list {
            eq('enabled', true)
        }
        User currentUser = userService.getUser()
        Map jsonData = [:]
        userList.each { User user ->
            jsonData.put(user.username, ['fullName': user.fullName,
                                         'email'   : user.email,
                                         'lang'    : user.preference.locale.language,
                                         'timezone': user.preference.timeZone,
                                         'roles'   : user.getAuthorities().authority,
                                         'groups'  : user.groups.findAll {
                                             it.isActive && it.groupType != GroupType.WORKFLOW_GROUP
                                         }?.name,
                                         'type'    : user.type
            ])
        }
        String url = Holders.config.pvreports.url
        String path = Holders.config.pvreports.user.copyBulkUser.uri
        Map data = [userName: currentUser.username, jsonData: new JsonBuilder(jsonData)]
        Map res = restAPIService.post(url, path, data, MediaType.APPLICATION_FORM_URLENCODED)
        return res
    }

    def copyBulkGroups() {
        User currentUser = userService.getUser()
        List groupList = Group.createCriteria().list {
            eq('isActive', true)
            ne("groupType", GroupType.WORKFLOW_GROUP)
        }
        Map jsonMap = [:]
        groupList.each { Group group ->
            List<User> userList = User.createCriteria().list {
                'groups' {
                    eq("id", group.id)
                }
                eq("enabled", true)
            }
            jsonMap.put(group.name, [description: group.description, users: userList.username])
        }
        String url = Holders.config.pvreports.url
        String path = Holders.config.pvreports.user.copyBulkGroup.uri
        Map data = [userName: currentUser.username, jsonData: new JsonBuilder(jsonMap)]
        Map res = restAPIService.post(url, path, data, MediaType.APPLICATION_FORM_URLENCODED)
        return res
    }

    void generateCountsMap(List alertData, DashboardCountDTO dashboardCountDTO) {
        List<Long> closedDispositionIdList = cacheService.getDispositionByReviewCompleted()*.id
        alertData.each {
            if (it.assignedToId && !closedDispositionIdList.contains(it.dispositionId)) {
                updateDashboardCountMaps(dashboardCountDTO.userDispCountsMap, it.assignedToId, it.dispositionId.toString())
                if (it.dueDate) {
                    updateDashboardCountMaps(dashboardCountDTO.userDueDateCountsMap, it.assignedToId, DateUtil.stringFromDate(it.dueDate, "dd-MM-yyyy", "UTC"))
                }
            } else if (!closedDispositionIdList.contains(it.dispositionId)) {
                updateDashboardCountMaps(dashboardCountDTO.groupDispCountsMap, it.assignedToGroupId, it.dispositionId.toString())
                if (it.dueDate) {
                    updateDashboardCountMaps(dashboardCountDTO.groupDueDateCountsMap, it.assignedToGroupId, DateUtil.stringFromDate(it.dueDate, "dd-MM-yyyy", "UTC"))
                }
            }
            updateDashboardCountMaps(dashboardCountDTO.execDispCountMap, it.executedAlertConfigurationId, it.dispositionId.toString())
        }
    }

    void updateDashboardCountMaps(Map<Long, Map<String, Integer>> dashboardCountMap, Long dashboardCountMapId, String countMapId) {
        if (dashboardCountMapId && countMapId && countMapId != 'null') {
            Map<String, Integer> countMap = dashboardCountMap.get(dashboardCountMapId) ?: [:]
            countMap.put(countMapId, (countMap.get(countMapId) ?: 0) + 1)
            dashboardCountMap.put(dashboardCountMapId, countMap)
        }
    }

    void updateDashboardCountMapsForPrevAlert(Map<Long, Map<String, Integer>> dashboardCountMap, Long dashboardCountMapId, String countMapId) {
        if (dashboardCountMapId && countMapId) {
            Map<String, Integer> countMap = dashboardCountMap.get(dashboardCountMapId) ?: [:]
            countMap.put(countMapId, (countMap.get(countMapId) ?: 0) - 1)
            dashboardCountMap.put(dashboardCountMapId, countMap)
        }
    }

    void updateDashboardCountsForPrevAlert(List<Map> alertData, DashboardCountDTO dashboardCountDTO) {
        List<Long> closedDispositionIdList = cacheService.getDispositionByReviewCompleted()*.id
        alertData.each {
            if (it.assignedToId && !closedDispositionIdList.contains(it.dispositionId)) {
                updateDashboardCountMapsForPrevAlert(dashboardCountDTO.userDispCountsMap, it.assignedToId, it.dispositionId.toString())
                if (it.dueDate) {
                    updateDashboardCountMapsForPrevAlert(dashboardCountDTO.userDueDateCountsMap, it.assignedToId, DateUtil.stringFromDate(it.dueDate, "dd-MM-yyyy", "UTC"))
                }
            } else if (!closedDispositionIdList.contains(it.dispositionId)) {
                updateDashboardCountMapsForPrevAlert(dashboardCountDTO.groupDispCountsMap, it.assignedToGroupId, it.dispositionId.toString())
                if (it.dueDate) {
                    updateDashboardCountMapsForPrevAlert(dashboardCountDTO.groupDueDateCountsMap, it.assignedToGroupId, DateUtil.stringFromDate(it.dueDate, "dd-MM-yyyy", "UTC"))
                }
            }
        }
    }

    void updateDashboardCounts(DashboardCountDTO dashboardCountDTO, Closure mergeCountsClosure) {
        dashboardCountDTO.userDispCountsMap.each { Long assignedToId, Map<String, Integer> dispCountMap ->
            updateUserAssignedToCounts(assignedToId, dashboardCountDTO, dispCountMap, dashboardCountDTO.userDueDateCountsMap.get(assignedToId), mergeCountsClosure)
        }

        dashboardCountDTO.groupDispCountsMap.each { Long groupId, Map dispCountMap ->
            updateUserGroupCounts(groupId, dashboardCountDTO, dispCountMap, dashboardCountDTO.groupDueDateCountsMap.get(groupId), mergeCountsClosure)
        }
    }

    void updateUserGroupCounts(Long groupId, DashboardCountDTO dashboardCountDTO, Map dispStatusMap, Map dueDateGroupMap, Closure mergeCountsClosure) {
        List<User> userList = cacheService.getAllUsersFromCacheByGroup(groupId)
        userList.each { user ->
            UserDashboardCounts userDashboardCounts = UserDashboardCounts.get(user.id)
            if (userDashboardCounts != null) {
                Map groupStatusMap = userDashboardCounts."${dashboardCountDTO.groupDispCountKey}" ? new JsonSlurper().parseText(userDashboardCounts."${dashboardCountDTO.groupDispCountKey}") as Map : [:]
                if (groupStatusMap.containsKey(groupId.toString())) {
                    groupStatusMap.put(groupId.toString(), mergeCountsClosure(dispStatusMap, groupStatusMap.get(groupId.toString()) as Map))
                } else {
                    groupStatusMap.put(groupId.toString(), dispStatusMap)
                }

                Map groupDueDateMap = userDashboardCounts."${dashboardCountDTO.groupDueDateCountKey}" ? new JsonSlurper().parseText(userDashboardCounts."${dashboardCountDTO.groupDueDateCountKey}") as Map : [:]
                if (groupDueDateMap.containsKey(groupId.toString())) {
                    groupDueDateMap.put(groupId.toString(), mergeCountsClosure(dueDateGroupMap, groupDueDateMap.get(groupId.toString()) as Map))
                } else {
                    groupDueDateMap.put(groupId.toString(), dueDateGroupMap)
                }
                userDashboardCounts."${dashboardCountDTO.groupDispCountKey}" = new JsonBuilder(groupStatusMap).toPrettyString()
                userDashboardCounts."${dashboardCountDTO.groupDueDateCountKey}" = new JsonBuilder(groupDueDateMap).toPrettyString()
                userDashboardCounts.save()
            }
        }
    }

    void updateUserAssignedToCounts(Long assignedToId, DashboardCountDTO dashboardCountDTO, Map<String, Integer> newDispStatusMap, Map<String, Integer> dueDateCountMap, Closure mergeCountsClosure) {
        UserDashboardCounts userDashboardCounts = UserDashboardCounts.get(assignedToId)
        if (userDashboardCounts) {
            if (userDashboardCounts."${dashboardCountDTO.dispCountKey}") {
                Map resultMap = mergeCountsClosure(newDispStatusMap, new JsonSlurper().parseText(userDashboardCounts."${dashboardCountDTO.dispCountKey}") as Map)
                userDashboardCounts."${dashboardCountDTO.dispCountKey}" = getJSONValueForCounts(resultMap)
            } else {
                userDashboardCounts."${dashboardCountDTO.dispCountKey}" = new JsonBuilder(newDispStatusMap).toPrettyString()
            }

            if (userDashboardCounts."${dashboardCountDTO.dueDateCountKey}" && dueDateCountMap) {
                Map resultDueDateMap = mergeCountsClosure(dueDateCountMap, new JsonSlurper().parseText(userDashboardCounts."${dashboardCountDTO.dueDateCountKey}") as Map)
                userDashboardCounts."${dashboardCountDTO.dueDateCountKey}" = getJSONValueForCounts(resultDueDateMap)
            } else if (dueDateCountMap) {
                userDashboardCounts."${dashboardCountDTO.dueDateCountKey}" = new JsonBuilder(dueDateCountMap).toPrettyString()
            }

            userDashboardCounts.save()
        }
    }

    void updateAssignedToDashboardCounts(DashboardCountDTO dashboardCountDTO, Long userId, Long groupId) {
        updateDashboardCounts(dashboardCountDTO, updateDispReviewCountMaps)
        if (userId) {
            updateUserAssignedToCounts(userId, dashboardCountDTO, generateAssignedToChangesCountMap(dashboardCountDTO.userDispCountsMap, dashboardCountDTO.groupDispCountsMap),
                    generateAssignedToChangesCountMap(dashboardCountDTO.userDueDateCountsMap, dashboardCountDTO.groupDueDateCountsMap), mergeCountMaps)
        } else {
            updateUserGroupCounts(groupId, dashboardCountDTO, generateAssignedToChangesCountMap(dashboardCountDTO.userDispCountsMap, dashboardCountDTO.groupDispCountsMap),
                    generateAssignedToChangesCountMap(dashboardCountDTO.userDueDateCountsMap, dashboardCountDTO.groupDueDateCountsMap), mergeCountMaps)
        }
    }

    void updateDashboardCountsForDispChange(DashboardCountDTO dashboardCountDTO, boolean isTargetDispReviewed) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        dashboardCountDTO.userDispCountsMap.each { Long assignedToId, Map dispCountMap ->
            UserDashboardCounts userDashboardCounts = UserDashboardCounts.get(assignedToId)
            if (userDashboardCounts."${dashboardCountDTO.dispCountKey}") {
                Map prevDispStatusMap = jsonSlurper.parseText(userDashboardCounts."${dashboardCountDTO.dispCountKey}") as Map
                Map prevDispCountMap = dashboardCountDTO.prevDispCountMap.get(assignedToId) ?: dashboardCountDTO.prevDispCountMap
                Map resultMap = isTargetDispReviewed ? updateDispReviewCountMaps(prevDispCountMap, prevDispStatusMap) :
                        mergeDispReviewCountMaps(dispCountMap, prevDispStatusMap, prevDispCountMap)
                userDashboardCounts."${dashboardCountDTO.dispCountKey}" = new JsonBuilder(resultMap.findAll {
                    it.value != 0
                }).toPrettyString()
            } else if (!isTargetDispReviewed) {
                userDashboardCounts."${dashboardCountDTO.dispCountKey}" = new JsonBuilder(dispCountMap).toPrettyString()
            }

            Map dueDateCountMap = dashboardCountDTO.userDueDateCountsMap.get(assignedToId)
            if (userDashboardCounts."${dashboardCountDTO.dueDateCountKey}" && dueDateCountMap) {
                Map resultDueDateMap = updateDispReviewCountMaps(dueDateCountMap, new JsonSlurper().parseText(userDashboardCounts."${dashboardCountDTO.dueDateCountKey}") as Map)
                userDashboardCounts."${dashboardCountDTO.dueDateCountKey}" = new JsonBuilder(resultDueDateMap.findAll {
                    it.value != 0
                }).toPrettyString()
            }
            userDashboardCounts.save()
        }
    }

    void updateDashboardGroupCountsForDispChange(DashboardCountDTO dashboardCountDTO, boolean isTargetDispReviewed) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        dashboardCountDTO.groupDispCountsMap.each { Long assignedToGroupId, Map dispCountsMap ->
            Map dueDateCountMap = dashboardCountDTO.groupDueDateCountsMap.get(assignedToGroupId) ?: [:]
            List<User> userList = cacheService.getAllUsersFromCacheByGroup(assignedToGroupId)
            userList.each { user ->
                UserDashboardCounts userDashboardCounts = UserDashboardCounts.get(user.id)
                Map groupStatusMap = userDashboardCounts."${dashboardCountDTO.groupDispCountKey}" ? jsonSlurper.parseText(userDashboardCounts."${dashboardCountDTO.groupDispCountKey}") as Map : [:]
                if (groupStatusMap.containsKey(assignedToGroupId.toString())) {
                    Map<String, Integer> groupDispCountMap = groupStatusMap.get(assignedToGroupId.toString())
                    Map prevGroupDispCountMap = dashboardCountDTO.prevGroupDispCountMap.get(assignedToGroupId) ?: dashboardCountDTO.prevGroupDispCountMap
                    groupStatusMap.put(assignedToGroupId.toString(), isTargetDispReviewed ? updateDispReviewCountMaps(prevGroupDispCountMap, groupDispCountMap) :
                            mergeDispReviewCountMaps(dispCountsMap, groupDispCountMap, prevGroupDispCountMap))
                } else if (!isTargetDispReviewed) {
                    groupStatusMap.put(assignedToGroupId.toString(), dispCountsMap)
                }
                userDashboardCounts."${dashboardCountDTO.groupDispCountKey}" = new JsonBuilder(groupStatusMap).toPrettyString()

                Map groupDueDateMap = userDashboardCounts."${dashboardCountDTO.groupDueDateCountKey}" ? new JsonSlurper().parseText(userDashboardCounts."${dashboardCountDTO.groupDueDateCountKey}") as Map : [:]
                if (groupDueDateMap.containsKey(assignedToGroupId.toString()) && groupDueDateMap.get(assignedToGroupId.toString())) {
                    Map resultDueDateMap = updateDispReviewCountMaps(dueDateCountMap, groupDueDateMap.get(assignedToGroupId.toString())).findAll {
                        it.value != 0
                    }
                    resultDueDateMap ? groupDueDateMap.put(assignedToGroupId.toString(), resultDueDateMap) : groupDueDateMap.remove(assignedToGroupId.toString())
                    userDashboardCounts."${dashboardCountDTO.groupDueDateCountKey}" = groupDueDateMap ? new JsonBuilder(groupDueDateMap).toPrettyString() : null
                }

                userDashboardCounts.save()
            }
        }
    }

    Map prepareDispStatusMap(Map jsonMap) {
        Map dispStatusMap = [:]
        jsonMap.each {
            it.value.entrySet().each {
                dispStatusMap.put(it.key, (dispStatusMap.get(it.key) ?: 0) + it.value)
            }
        }
        dispStatusMap
    }

    Map mergeDispReviewCountMaps(Map newDispStatusMap, Map prevDispStatusMap, Map prevDispCountMap) {
        int prevReviewCounts = 0
        (newDispStatusMap.keySet() + prevDispStatusMap.keySet()).collectEntries {
            prevReviewCounts = 0
            if (prevDispStatusMap && prevDispCountMap) {
                if (prevDispStatusMap[it] && !prevDispCountMap.containsKey(it)) {
                    prevReviewCounts = prevDispStatusMap[it]
                } else if (prevDispStatusMap[it] && prevDispCountMap.containsKey(it)) {
                    prevReviewCounts = prevDispStatusMap[it] - prevDispCountMap[it]
                }
            }
            [(it): (newDispStatusMap[it] ?: 0) + prevReviewCounts]

        }
    }

    Map generateAssignedToChangesCountMap(Map userCountMap, Map groupCountMap) {
        mergeCountMaps(prepareDispStatusMap(userCountMap), prepareDispStatusMap(groupCountMap))
    }

    Closure mergeCountMaps = { Map countMap1, Map countMap2 ->
        List countMap1keysList = countMap1 ? countMap1.keySet() as List : []
        List countMap2keysList = countMap2 ? countMap2.keySet() as List : []
        (countMap1keysList + countMap2keysList)?.collectEntries {
            [(it): (countMap1?.get(it) ?: 0) + (countMap2?.get(it) ?: 0)]
        }
    }

    Closure updateDispReviewCountMaps = { Map newDispStatusMap, Map prevDispStatusMap ->
        prevDispStatusMap?.collectEntries { k, v -> [k, v - (newDispStatusMap[k] ?: 0)] }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void updateOldExecutedConfigurationWithDispCounts(def configuration, Long execConfigId, def domain, Map execConfigCountsMap,
                                                      Integer newCounts, String disabledDssNodes = null) {
        domain.executeUpdate("Update ExecutedConfiguration set isLatest = 0 where name = :name and " +
                " owner = :owner and id <> :execConfigId and type = :type",
                [name: configuration.name, owner: configuration.owner, execConfigId: execConfigId, type: configuration.type])
        List requiresReviewDispList = cacheService.getNotReviewCompletedDisposition().collect { it.id as String }
        int requiresReviewCount = 0
        Map requiresReviewCountMap = execConfigCountsMap?.findAll { it.key.toString() in requiresReviewDispList }
        if (requiresReviewCountMap) {
            requiresReviewCount = requiresReviewCountMap.values().sum()
        }
        domain.executeUpdate("Update ExecutedConfiguration set isLatest = 1,isEnabled = 1, requiresReviewCount = :requiresReviewCount,  dispCounts = :dispCounts, newCounts = :newCounts, disabledDssNodes = :disabledDssNodes where id = :execConfigId",
                [execConfigId: execConfigId, dispCounts: execConfigCountsMap ? new JsonBuilder(execConfigCountsMap).toPrettyString() : null,
                 newCounts   : newCounts, requiresReviewCount: requiresReviewCount ? requiresReviewCount.toString() : "0", disabledDssNodes: disabledDssNodes])
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void updateExecutedConfigurationWithAddCaseDispCounts(def configuration, Long execConfigId, def domain, Map execConfigCountsMap,
                                                          Integer newCounts, String disabledDssNodes = null) {
        List requiresReviewDispList = cacheService.getNotReviewCompletedDisposition().collect { it.id as String }
        int requiresReviewCount = 0
        Map requiresReviewCountMap = execConfigCountsMap?.findAll { it.key.toString() in requiresReviewDispList }
        if (requiresReviewCountMap) {
            requiresReviewCount = requiresReviewCountMap.values().sum()
        }
        domain.executeUpdate("Update ExecutedConfiguration set isLatest = 1,isEnabled = 1, requiresReviewCount = :requiresReviewCount,  dispCounts = :dispCounts, newCounts = :newCounts, disabledDssNodes = :disabledDssNodes where id = :execConfigId",
                [execConfigId: execConfigId, dispCounts: execConfigCountsMap ? new JsonBuilder(execConfigCountsMap).toPrettyString() : null,
                 newCounts   : newCounts, requiresReviewCount: requiresReviewCount ? requiresReviewCount.toString() : "0", disabledDssNodes: disabledDssNodes])
    }


    List getLiteraturePrevExConfigIds(ExecutedLiteratureConfiguration executedConfiguration, Long configId) {
        List<Integer> reportVersions = ExecutionStatus.findAllByConfigIdAndExecutionStatusInList(configId, [ReportExecutionStatus.COMPLETED, ReportExecutionStatus.DELIVERING]).reportVersion.collect {
            (int) it
        }

        List result = ExecutedLiteratureConfiguration.createCriteria().list() {
            projections {
                property("id")
            }
            eq("configId", configId)
            eq("isDeleted", false)
            if (reportVersions.size() > 0) {
                or {
                    reportVersions.collate(999).each {
                        'in'('numOfExecutions', it)
                    }
                }
            }
            ne("id", executedConfiguration.id)
            order("id", "desc")
        }
        result
    }

    DashboardCountDTO prepareDashboardCountDTO(boolean isCaseSeries) {
        DashboardCountDTO dashboardCountDTO = new DashboardCountDTO()
        if (isCaseSeries) {
            dashboardCountDTO.dispCountKey = Constants.UserDashboardCounts.USER_DISP_CASE_COUNTS
            dashboardCountDTO.dueDateCountKey = Constants.UserDashboardCounts.USER_DUE_DATE_CASE_COUNTS
            dashboardCountDTO.groupDispCountKey = Constants.UserDashboardCounts.GROUP_DISP_CASE_COUNTS
            dashboardCountDTO.groupDueDateCountKey = Constants.UserDashboardCounts.GROUP_DUE_DATE_CASE_COUNTS
        } else {
            dashboardCountDTO.dispCountKey = Constants.UserDashboardCounts.USER_DISP_PECOUNTS
            dashboardCountDTO.dueDateCountKey = Constants.UserDashboardCounts.USER_DUE_DATE_PECOUNTS
            dashboardCountDTO.groupDispCountKey = Constants.UserDashboardCounts.GROUP_DISP_PECOUNTS
            dashboardCountDTO.groupDueDateCountKey = Constants.UserDashboardCounts.GROUP_DUE_DATE_PECOUNTS
        }
        dashboardCountDTO
    }

    boolean isUpdateDashboardCount(boolean isArchived, def alert) {
        !isArchived && !cacheService.getDispositionByValue(alert.dispositionId).reviewCompleted
    }

    String getJSONValueForCounts(Map countsMap) {
        Map resultMap = countsMap.findAll { it.value != 0 }
        resultMap ? new JsonBuilder(resultMap).toPrettyString() : null
    }

    def getDetailsUrlMap(type, adhocRun) {
        def urlMap = detailUrlMap.get(type)
        if (adhocRun) {
            urlMap.adhocRun
        } else {
            urlMap.dataMiningRun
        }
    }

    void updateDispCountsForExecutedConfigurationEvdas(Map<Long, Map<String, Integer>> execDispCountMap, Map<String, Integer> prevDispCountMap) {
        log.info("Update Disp Counts for Executed Evdas Configuration")
        List requiresReviewDispList = cacheService.getNotReviewCompletedDisposition().collect { it.id as String }
        if (execDispCountMap && prevDispCountMap) {
            JsonSlurper jsonSlurper = new JsonSlurper()
            Sql sql = new Sql(dataSource)
            sql.withBatch(100, "UPDATE EX_EVDAS_CONFIG SET disp_counts = :dispCounts, requires_review_count=:requiresReviewCount WHERE ID = :id", { preparedStatement ->
                execDispCountMap.each { Long execConfigId, Map<String, Integer> dispCountMap ->
                    ExecutedEvdasConfiguration executedConfiguration = ExecutedEvdasConfiguration.get(execConfigId)
                    Map prevDispStatusMap = jsonSlurper.parseText(executedConfiguration.dispCounts) as Map
                    Map resultMap = mergeDispReviewCountMaps(dispCountMap, prevDispStatusMap, prevDispCountMap)
                    int requiresReviewCount = 0
                    Map requiresReviewCountMap = resultMap?.findAll { it.key in requiresReviewDispList }
                    if (requiresReviewCountMap) {
                        requiresReviewCount = requiresReviewCountMap.values().sum()
                    }
                    preparedStatement.addBatch(id: execConfigId, dispCounts: new JsonBuilder(resultMap.findAll {
                        it.value != 0
                    }).toPrettyString(),
                            requiresReviewCount: requiresReviewCount ? requiresReviewCount.toString() : "0")
                }
            })
        }
        log.info("Disp Counts for Executed Evdas Configuration updated")
    }

    void updateReviewCountsForLiterature(Long execConfigId, Integer reviewCounts) {
        log.info("Update Review Counts for Executed Literature Configuration")
        ExecutedLiteratureConfiguration executedLiteratureConfiguration = ExecutedLiteratureConfiguration.get(execConfigId)
        Integer prevReviewCounts = executedLiteratureConfiguration.requiresReviewCount as Integer
        if (prevReviewCounts) {
            reviewCounts = prevReviewCounts - reviewCounts
        }
        executedLiteratureConfiguration.requiresReviewCount = reviewCounts > 0 ? reviewCounts.toString() : "0"
        log.info("Review Counts for Executed Literature Configuration updated")
    }

    void updateOldExecutedConfigurationsEvdasWithDispCount(def configuration, Long execConfigId, def domain, Map execConfigCountsMap) {
        domain.executeUpdate("Update ExecutedEvdasConfiguration set isLatest = 0 where name = :name and " +
                " owner = :owner and id <> :execConfigId",
                [name: configuration.name, owner: configuration.owner, execConfigId: execConfigId])
        List requiresReviewDispList = cacheService.getNotReviewCompletedDisposition().collect { it.id }
        int requiresReviewCount = 0
        Map requiresReviewCountMap = execConfigCountsMap?.findAll { it.key in requiresReviewDispList }
        if (requiresReviewCountMap) {
            requiresReviewCount = requiresReviewCountMap.values().sum()
        }
        domain.executeUpdate("Update ExecutedEvdasConfiguration set isLatest = 1,isEnabled = 1,dispCounts = :dispCounts, requiresReviewCount = :requiresReviewCount where id = :execConfigId",
                [execConfigId       : execConfigId, dispCounts: execConfigCountsMap ? new JsonBuilder(execConfigCountsMap).toPrettyString() : null,
                 requiresReviewCount: requiresReviewCount ? requiresReviewCount.toString() : "0"])
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void updateOldExecutedConfigurationsLiterature(def configuration, Long execConfigId, def domain, Map execConfigCountsMap) {
        domain.executeUpdate("Update ExecutedLiteratureConfiguration set isLatest = 0 where name = :name and " +
                " owner = :owner and id <> :execConfigId",
                [name: configuration.name, owner: configuration.owner, execConfigId: execConfigId])

        List requiresReviewDispList = cacheService.getNotReviewCompletedDisposition().collect { it.id }
        int requiresReviewCount = 0
        Map requiresReviewCountMap = execConfigCountsMap?.findAll { it.key in requiresReviewDispList }
        if (requiresReviewCountMap) {
            requiresReviewCount = requiresReviewCountMap.values().sum()
        }
        domain.executeUpdate("Update ExecutedLiteratureConfiguration set isLatest = 1,isEnabled = 1, requiresReviewCount = :requiresReviewCount where id = :execConfigId", [execConfigId: execConfigId, requiresReviewCount: requiresReviewCount ? requiresReviewCount.toString() : "0"])
    }

    String getCaseOfSeries(String value) {
        Sql sql = new Sql(dataSource_pva)
        List<String> caseDTOList = []
        Integer count = 0
        Integer totalCount = 0
        try {
            int index = 0
            sql.setResultSetType(ResultSet.TYPE_FORWARD_ONLY)
            value.split(';').each {
                Map resultMap = reportIntegrationService.fetchLatestCaseSeriesId(it as Long)
                sql.call("{?= call PKG_QUERY_HANDLER.f_get_cases_details(?,?,?,?,?,?,?)}",
                        [Sql.resultSet(OracleTypes.CURSOR), null, "case_num", "desc", 1, -1, resultMap.id, resultMap.caseSeriesOwner]) { cursorResults ->
                    cursorResults.eachRow { result ->
                        caseDTOList.add(result.case_num)
                    }
                }

                sql.call("{?= call PKG_QUERY_HANDLER.f_fetch_open_cases(?,?)}",
                        [Sql.resultSet(OracleTypes.CURSOR), resultMap.id, resultMap.caseSeriesOwner]) { cursorResults ->
                    cursorResults.eachRow { result ->
                        caseDTOList.add(result.case_num)
                    }
                }
            }
            return caseDTOList.join(";")
        } finally {
            sql.close()
        }
    }

    boolean isCumForLimitCaseSeries(ExecutedConfiguration executedConfiguration, boolean isLimitToCaseSeries) {
        return executedConfiguration.executedTemplateQueries?.any {
            it.executedDateRangeInformationForTemplateQuery.dateRangeEnum == DateRangeEnum.CUMULATIVE && isLimitToCaseSeries
        }
    }

    boolean isCumForLimitCaseSeriesSpotfire(ExecutedConfiguration executedConfiguration, boolean isLimitToCaseSeries) {
        boolean isSpotfireCummCaseSeries
        if (executedConfiguration.spotfireSettings) {
            SpotfireSettingsDTO settings = SpotfireSettingsDTO.fromJson(executedConfiguration.spotfireSettings)
            isSpotfireCummCaseSeries = settings.rangeType.any {
                it in [DateRangeEnum.CUMULATIVE_SAFETYDB, DateRangeEnum.CUMULATIVE] && isLimitToCaseSeries
            }
        }
        isSpotfireCummCaseSeries
    }

    JSONObject updateIsTempCaseSeries(Boolean isTemporary, isCaseSeries, Configuration config = null, Long id) {
        JSONArray showElements = new JSONArray()
        JSONObject result = new JSONObject()
        if (config) {
            JSONObject item = populateObject(isTemporary, isCaseSeries, config)
            item.caseSeriesId = id
            showElements.add(item)
        } else {
            ExecutedConfiguration.findAllByIsCaseSeriesAndPvrCaseSeriesIdIsNotNull(isCaseSeries).each {
                config = Configuration.get(it.configId)
                JSONObject item = populateObject(isTemporary, isCaseSeries, config)
                if (it.pvrCaseSeriesId) {
                    item.caseSeriesId = it.pvrCaseSeriesId
                    showElements.add(item)
                }
            }
        }

        result.put("caseSeriesDTOList", showElements)
        String url = Holders.config.pvreports.url
        String path = Holders.config.pvreports.caseSeries.updation.sharedWith
        Map response = reportIntegrationService.postData(url, path, result)
        log.info("Case Series API Response from PVR : ${response}")
    }

    JSONObject populateObject(Boolean isTemporary, isCaseSeries, Configuration config) {
        Set<String> users = config.getShareWithUsers()?.collect { it.username }
        Set<String> groups = config.getShareWithGroups()?.collect { it.name }
        Set<String> autoSharedUsers = []
        Set<String> autoSharedGroups = []
        if (config.autoShareWithGroup || config.autoShareWithUser) {
            autoSharedUsers = config.autoShareWithUser?.collect { it.username }
            autoSharedGroups = config.autoShareWithGroup?.collect { it.name }
        }
        if (autoSharedUsers && autoSharedUsers.size() > 0) {
            users += autoSharedUsers
        }
        if (autoSharedGroups && autoSharedGroups.size() > 0) {
            groups += autoSharedGroups
        }
        JSONObject item = new JSONObject()
        item.sharedWithUsers = null
        item.sharedWithGroups = null
        item.isTemporary = isTemporary
        if (users.size() > 0)
            item.sharedWithUsers = users as List
        if (groups.size() > 0)
            item.sharedWithGroups = groups as List

        return item
    }

    List<Long> fetchAutoSharedIds(List<Long> execConfigIdList) {
        Session session = sessionFactory.currentSession
        String sql = SignalQueryHelper.auto_shared_config_id_sql(execConfigIdList)
        SQLQuery sqlQuery = session.createSQLQuery(sql)
        sqlQuery.addScalar("id", new LongType())
        sqlQuery.list()
    }

    List<Map> fetchDateRangeInformationList(List<Long> exDateRangeInfoIdList) {
        Session session = sessionFactory.currentSession
        String sql = SignalQueryHelper.date_range_sql(exDateRangeInfoIdList)
        SQLQuery sqlQuery = session.createSQLQuery(sql)
        sqlQuery.addScalar("id", new LongType())
        sqlQuery.addScalar("dateRangeStartAbsolute", new DateType())
        sqlQuery.addScalar("dateRangeEndAbsolute", new DateType())
        sqlQuery.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
        sqlQuery.list()
    }

    List fetchDateRangeInformationMapListId(List<Long> exDateRangeInfoIdList) {
        Sql sql = new Sql(dataSource)
        List dateRangeMapList = []

        String sqlQuery = SignalQueryHelper.date_range_sql(exDateRangeInfoIdList)
        try {
            sql.eachRow(sqlQuery, []) { row ->
                if (row.id) {
                    dateRangeMapList.add([id                    : row.id, dateRangeEndAbsolute: row.dateRangeEndAbsolute,
                                          dateRangeStartAbsolute: row.dateRangeStartAbsolute, dateRangeEnum: row.dateRangeEnum])
                }
            }
            return dateRangeMapList
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            sql.close()
        }
    }

    List<Map> fetchDateRangeInformationListForAlerts(List<Long> exDateRangeInfoIdList, String alerType) {
        Session session = sessionFactory.currentSession
        String sql = null
        if (alerType == Constants.AlertType.EVDAS) {
            sql = SignalQueryHelper.date_range_sql_evdas_alert(exDateRangeInfoIdList.join(","))
        } else {
            sql = SignalQueryHelper.date_range_sql_literature_alert(exDateRangeInfoIdList.join(","))
        }
        SQLQuery sqlQuery = session.createSQLQuery(sql)
        sqlQuery.addScalar("id", new LongType())
        sqlQuery.addScalar("dateRangeStartAbsolute", new DateType())
        sqlQuery.addScalar("dateRangeEndAbsolute", new DateType())
        sqlQuery.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
        sqlQuery.list()
    }

    List<Map> getProductIdMap(def domainName, AlertDataDTO alertDataDTO, String callingScreen) {

        List groupIds = userService.getUser().groups.findAll { it.groupType != GroupType.WORKFLOW_GROUP }.collect {
            it.id
        }
        List<Map> productIdMapUser = domainName.createCriteria().list {
            projections {
                distinct('productName')
                property("executedAlertConfiguration.id", "executedAlertConfigurationId")
            }
            if (callingScreen == Constants.Commons.DASHBOARD) {
                'eq'("assignedTo.id", alertDataDTO.userId)
            } else {
                if (alertDataDTO.masterExConfigId) {
                    or {
                        'alertConfiguration' {
                            eq("id", alertDataDTO.configId)
                            'shareWithUser' {
                                'in'('id', [alertDataDTO.userId])
                            }
                        }
                        'executedAlertConfiguration' {
                            eq("masterExConfigId", alertDataDTO.masterExConfigId)
                        }
                    }
                } else {
                    eq("executedAlertConfiguration.id", alertDataDTO.execConfigId)
                }
            }
        } as List<Map>
        List<Map> productIdMapGroup = domainName.createCriteria().list {
            projections {
                distinct('productName')
                property("executedAlertConfiguration.id", "executedAlertConfigurationId")
            }
            if (callingScreen == Constants.Commons.DASHBOARD) {
                'eq'("assignedTo.id", alertDataDTO.userId)
            } else {
                if (alertDataDTO.masterExConfigId) {
                    or {
                        'alertConfiguration' {
                            eq("id", alertDataDTO.configId)
                            'shareWithGroup' {
                                'in'('id', groupIds)
                            }
                        }
                        'executedAlertConfiguration' {
                            eq("masterExConfigId", alertDataDTO.masterExConfigId)
                        }
                    }
                } else {
                    eq("executedAlertConfiguration.id", alertDataDTO.execConfigId)
                }
            }
        } as List<Map>
        (productIdMapUser + productIdMapGroup).findAll { it[0] != "undefined" }.unique()
    }

    List<Map> getProductIdMapForMasterConfig(AlertDataDTO alertDataDTO, String callingScreen) {

        List groupIds = userService.getUser().groups.findAll { it.groupType != GroupType.WORKFLOW_GROUP }.collect {
            it.id
        }
        List<Map> productIdMap = ExecutedConfiguration.createCriteria().list {
            projections {
                distinct('productName')
                property("id", "executedAlertConfigurationId")
                property("configId", "configId")

            }

            if (alertDataDTO.masterExConfigId) {
                eq("masterExConfigId", alertDataDTO.masterExConfigId)
            }

        } as List<Map>
        List configIds = productIdMap.collect { it[2] }
        List<Map> sharedConfigIds = Configuration.createCriteria().list {
            createAlias("shareWithUser", "shareWithUser", JoinType.LEFT_OUTER_JOIN)
            createAlias("shareWithGroup", "shareWithGroup", JoinType.LEFT_OUTER_JOIN)

            projections {
                distinct('id')
            }
            'or' {
                'eq'("owner.id", alertDataDTO.userId)
                'eq'("assignedTo.id", alertDataDTO.userId)
                if (groupIds?.size() > 0) {
                    or {
                        groupIds.collate(1000).each {
                            'in'('assignedToGroup.id', it)
                        }
                    }
                }
                eq("shareWithUser.id", alertDataDTO.userId)
                if (groupIds) {
                    or {
                        groupIds.collate(1000).each {
                            'in'("shareWithGroup.id", it)
                        }
                    }
                }
            }
            'or' {
                configIds.collate(1000).each {
                    'in'("id", it)
                }
            }
        }

        productIdMap.findAll { it[2] in sharedConfigIds && it[0] != "undefined" }.unique()

    }

    Map getSingleCaseFilterIndexes(Boolean isPriorityEnabled, Boolean isDashboardScreen, Boolean isCustomEnabled, Boolean isCaseSeries, Boolean adhocRun = false) {
        List<Map> viewMap = []
        if(isCaseSeries && adhocRun)
            viewMap = Holders.config.configurations.scaColumnOrderListDrilldown.clone() as List<Map>
        else
            viewMap = Holders.config.configurations.scaColumnOrderList.clone() as List<Map>
        // This addition is done in same order as our columns in the data table gsp.
        int fixedColumns = 2 + (isPriorityEnabled ? 1 : 0) + 1 + (isDashboardScreen ? 1 : 0) + 1
        List removeList = []
        if (!isCustomEnabled) {
            removeList.add(Constants.SingleCaseViewAvailability.ONLY_FDA)
        }
        viewMap.removeIf { it.availability in removeList }
        if(viewMap.find { it.name == 'dateOfBirth'}?.isFilter) {
            viewMap.find { it.name == 'dateOfBirth'}.isFilter = Holders.config.enable.show.dob
        }
        List indexList = []
        int count = fixedColumns - 1

        viewMap.each {
            it.isFilter ? indexList.add(++count) : count++
        }
        [fixedColumns: fixedColumns, indexList: indexList]
    }

    Map getSingleOnDemandFilterIndexes(Boolean isCustomEnabled) {
        List<Map> viewMap = Holders.config.configurations.scaColumnOrderListOnDemand.clone() as List<Map>
        int fixedColumns = 2
        List removeList = []
        if (!isCustomEnabled) {
            removeList.add(Constants.SingleCaseViewAvailability.ONLY_FDA)
        }
        viewMap.removeIf { it.availability in removeList }
        if(viewMap.find { it.name == 'dateOfBirth'}?.isFilter) {
            viewMap.find { it.name == 'dateOfBirth'}.isFilter = Holders.config.enable.show.dob
        }
        List indexList = []
        int count = fixedColumns - 1

        viewMap.each {
            it.isFilter ? indexList.add(++count) : count++
        }
        [fixedColumns: fixedColumns, indexList: indexList]
    }

    List getGroupValueList(String value, Boolean isProductGroupValue, Boolean isEventGroupValue, String fieldName) {
        List finalGroupValueList = []
        if (isProductGroupValue || isEventGroupValue) {
            value = isProductGroupValue ? value.replace('PG_', '') : value
            value = isEventGroupValue ? value.replace('EG_', '') : value
            List groupValueList = []
            value.split(';').each {
                def DictValues = dictionaryGroupService.groupDetails(it as Long, springSecurityService.principal?.username, true)?.data

                if (DictValues) {
                    JSON.parse(DictValues)?.values()?.each {
                        if(isEventGroupValue){
                            it?.values()?.eachWithIndex{events, idx ->
                                if(idx==6){
                                    // Broad SMQ has smqCode as 1
                                    events?.each{ event ->
                                        groupValueList.add(new Pair("1", event?.id as Integer))
                                    }
                                }else if(idx==7){
                                    // Narrow Smq event has 2
                                    events?.each{ event ->
                                        groupValueList.add(new Pair("2", event?.id as Integer))
                                    }
                                }else{
                                    groupValueList.add(events?.id)
                                }
                            }
                        } else {
                            if ((StringUtils.equalsIgnoreCase("suspProd", fieldName) || StringUtils.equalsIgnoreCase("conComit", fieldName)) && isProductGroupValue) {
                                groupValueList.add(it?.values()?.name)
                            } else {
                                groupValueList?.add(it?.values()?.id)
                            }
                        }
                    }
                }
            }
            finalGroupValueList = groupValueList.flatten().unique()
        }
        finalGroupValueList
    }

    private String getOwner(String dataSource) {
        log.info("getOwner datasource" + dataSource)
        if (StringUtils.isNotBlank(dataSource)) {
            dataSource = dataSource == Constants.DataSource.FAERS ? getFaersDbUserName() : dataSource == Constants.DataSource.VIGIBASE ? getVigibaseDbUserName() : dataSource == Constants.DataSource.VAERS ? getVaersDbUserName() : Constants.PVS_CASE_SERIES_OWNER
        } else {
            dataSource = Constants.PVS_CASE_SERIES_OWNER
        }
        log.info("Case Series datasource owner " + dataSource)
        return dataSource
    }

    def getDomainObjectByAlertType(String alertType) {
        if (alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT || alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            return Configuration
        } else if (alertType == Constants.AlertConfigType.EVDAS_ALERT) {
            return EvdasConfiguration
        } else if (alertType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
            return LiteratureConfiguration
        } else if (alertType == Constants.AlertConfigType.VALIDATED_SIGNAL) {
            return ValidatedSignal
        }
    }

    def getPosterClassName(String alertType) {
        if (alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT || alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            return Configuration.class.getName()
        } else if (alertType == Constants.AlertConfigType.EVDAS_ALERT) {
            return EvdasConfiguration.class.getName()
        } else if (alertType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
            return LiteratureConfiguration.class.getName()
        } else if (alertType == Constants.AlertConfigType.VALIDATED_SIGNAL) {
            return ValidatedSignal.class.getName()
        }
    }

    def getConfiguration(def domainName, Long configId) {
        return domainName.get(configId)
    }

    def getExecutedConfiguration(def executedDomainName, Long configId) {
        return executedDomainName.findByConfigIdAndIsLatest(configId, true)
    }

    def getArchivedExecutedConfigurations(Long id, def domain, def executedDomainName, String alertType) {
        def ec = executedDomainName.get(id)
        List executedConfigurationList = []
        if (ec) {
            def config = domain.findByNameAndOwner(ec.name, ec.owner)
            if (ec.configId) {
                //Fetching only completed execution status objects for alert. -PS
                List<Integer> reportVersions = ExecutionStatus.findAllByConfigIdAndExecutionStatusInListAndType(ec.configId, [ReportExecutionStatus.COMPLETED, ReportExecutionStatus.DELIVERING], alertType).reportVersion.collect {
                    (int) it
                }

                executedConfigurationList = executedDomainName.createCriteria().list() {
                    eq("configId", ec.configId)
                    owner {
                        eq("id", ec.owner?.id)
                    }
                    if (alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT || alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
                        eq("type", alertType)
                        if (alertType != Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
                            eq("adhocRun", ec.adhocRun)
                        }
                        eq("isLatest", false)
                        eq("isEnabled", true)
                        eq("isDeleted", false)
                        //This check is used to fetch only completed Executed Configurations
                        if (reportVersions.size() > 0) {
                            or {
                                reportVersions.collate(999).each {
                                    'in'('numOfExecutions', it)
                                }
                            }
                        }
                        order("numOfExecutions", "desc")
                    }
                }
            }
            return executedConfigurationList
        }
    }

    def getAlertDomainName(String alertType, Boolean isArchived = false) {
        def alertDomain = null
        if (alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
            alertDomain = isArchived ? ArchivedSingleCaseAlert : SingleCaseAlert
        } else if (alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            alertDomain = isArchived ? ArchivedAggregateCaseAlert : AggregateCaseAlert
        } else if (alertType == Constants.AlertConfigType.EVDAS_ALERT) {
            alertDomain = isArchived ? ArchivedEvdasAlert : EvdasAlert
        } else if (alertType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
            alertDomain = isArchived ? ArchivedLiteratureAlert : LiteratureAlert
        } else if (alertType == Constants.AlertConfigType.AD_HOC_ALERT) {
            alertDomain = AdHocAlert
        }
        return alertDomain
    }


    boolean isPreCheckVerified(AlertType alertType, Configuration configuration) {
        List<String> preCheckList = new ArrayList<String>()
        String[] dataSources = configuration?.selectedDatasource?.split(",")
        boolean precheckEnabled = Holders.config.system.precheck.configuration.enable
        boolean success = Boolean.TRUE
        if (!precheckEnabled) {
            return success
        }
        switch (alertType) {
            case AlertType.SINGLE_CASE_ALERT:
                preCheckList = SystemPreConfig.findAllByOptionalAndAlertTypeInList(false, [AlertType.SINGLE_CASE_ALERT, 'ALL'])?.collect {
                    it.name
                }
                break;

            case AlertType.AGGREGATE_CASE_ALERT:
                preCheckList = SystemPreConfig.findAllByOptionalAndAlertTypeInList(false, [AlertType.AGGREGATE_CASE_ALERT, 'ALL'])?.collect {
                    it.name
                }
                break;

        }
        for (int i = 0; i < preCheckList?.size(); i++) {
            if (!preCheckService.addPrecheckIfEnabled(preCheckList?.get(i))) {
                preCheckList?.remove(preCheckList?.get(i))
            }
        }
        boolean containsEvdas = (dataSources.contains("eudra") || dataSources.contains("evdas"))
        boolean containsSafety = (dataSources.contains("pva") || dataSources.contains("safety"))
        if (!containsEvdas) {
            preCheckList?.removeAll([Constants.SystemPrecheck.ERMR_FOLDER, Constants.SystemPrecheck.EVDAS_FOLDER, Constants.SystemPrecheck.CASE_LINE_LISTING_FOLDER])
            List<String> evdasMartPrechecks = SystemPreConfig.findAllByDbTypeOrName(Constants.SystemPrecheck.EVDAS, Constants.SystemPrecheck.EVDAS).collect {
                it.name
            }
            preCheckList?.removeAll(evdasMartPrechecks)
        }
        if (!containsSafety) {
            preCheckList?.removeAll([Constants.SystemPrecheck.DSS])
        }
        for (int p = 0; p < preCheckList?.size(); p++) {
            SystemPreConfig mandatoryPrecheckdetails = SystemPreConfig.findByName(preCheckList.get(p))
            for (int k = 0; k < dataSources.length; k++) {
                if (dataSources[k]?.toLowerCase().equals("pva") || dataSources[k]?.toLowerCase().equals("safety")) {
                    dataSources[k] = Constants.SystemPrecheck.SAFETY
                }
                if (dataSources[k]?.toLowerCase().equals("eudra") || dataSources[k]?.toLowerCase().equals("evdas")) {
                    dataSources[k] = Constants.SystemPrecheck.EVDAS
                }

                if (Objects.nonNull(mandatoryPrecheckdetails)) {
                    if (!mandatoryPrecheckdetails?.running && mandatoryPrecheckdetails?.appType.equals("application")) {
                        success = Boolean.FALSE;
                        break;
                    }
                    if (!mandatoryPrecheckdetails?.running && dataSources[k]?.toString().toLowerCase().contains(mandatoryPrecheckdetails?.dbType?.toLowerCase())) {
                        success = Boolean.FALSE;
                        break;
                    }
                }
            }
        }
        return success
    }

    Map getOnlyActiveDataSheets(def configuration,Boolean isEvdas = false) {

        if(!configuration.selectedDataSheet) {
            return [:]
        }
        Map dataSheetsMap = JSON.parse(configuration.selectedDataSheet)
        Map activeDataSheetsMap = [:]
        Boolean isProductGroup = false
        List currentActiveDatasheetList = []
        String productDictionarySelection = ''
        if (configuration.productGroupSelection) {
            productDictionarySelection = configuration.productGroupSelection
            isProductGroup = true
        } else {
            productDictionarySelection = configuration.productSelection
        }
        
        if (configuration.selectedDatasource?.contains(Constants.DataSource.PVA)) {
            currentActiveDatasheetList = dataSheetService.fetchDataSheets(productDictionarySelection, Constants.DatasheetOptions.ALL_SHEET, isProductGroup)
        } else {
            //Removed the check of EVDAS as it is of no use after addition of faers datasource since same function is called in both blocks with same params
            currentActiveDatasheetList = dataSheetService.getAllActiveDatasheetsList("", Constants.DatasheetOptions.ALL_SHEET)
        }

        List currentActiveDatasheetListIds = currentActiveDatasheetList.collect {it-> it.id as String}
        dataSheetsMap?.each { key, value ->
            if(currentActiveDatasheetListIds.contains(key as String)) {
                activeDataSheetsMap.put(key,value)
            }
        }
        return activeDataSheetsMap
    }

    protected void piiPolicy(Session session) {
        if (Objects.nonNull(session)) {
            try {
                session.createSQLQuery("CALL P_SET_CONTEXT(:param1, :param2)")
                        .setParameter("param1", "PVD_SECURITY_FIELDS")
                        .setParameter("param2", "PVS")
                        .executeUpdate()
                session.flush()
                session.clear()
            } catch (Exception ex) {
                log.error("PII policy failed.", ex)
            }
        }
    }
    List getDataSourceSubstring(String dataSource){
        List substrings = []

        for (int i = 0; i < dataSource.length(); i++) {
            for (int j = i + 1; j <= dataSource.length(); j++) {
                substrings.add((dataSource.substring(i, j)).toLowerCase())
            }
        }

        substrings
    }

    void updateUndoDispDueDate(String alertType, Long alertId, List undoableDispositionIdList, Date dueDate) {
        UndoableDisposition undoableDisposition = UndoableDisposition.createCriteria().get {
            eq('objectId', alertId)
            eq('objectType', alertType)
            order('dateCreated', 'desc')
            maxResults(1)
        }
        if (undoableDisposition && undoableDisposition.isEnabled) {
            undoableDisposition.prevDueDate = dueDate
            undoableDispositionIdList.add(undoableDisposition)
        }
    }

    boolean checkAlertSharedToCurrentUser(ExecutedConfiguration exConfig) {
        //checking is current loggged-in user have access to the alert for icr,agg and case-series
        if (!exConfig) {
            return true;
        }
        Configuration config = Configuration.get(exConfig?.configId)
        User user = userService.getUser()
        Set<Group> groups = Group.findAllUserGroupByUser(user)
        Group getAssignedToGroup = groups.find { group -> group.id == exConfig?.assignedToGroup?.id }
        Boolean isAutoShare = false
        Boolean isValidAutoShare = false
        if (exConfig.hasProperty("autoShareWithUser") || exConfig.hasProperty("autoShareWithGroup")) {
            isAutoShare = true
            isValidAutoShare = exConfig.autoShareWithUser?.contains(user) || exConfig?.autoShareWithGroup?.find { group -> groups*.id?.contains(group?.id) }
        }
        if (exConfig?.owner != user && exConfig?.assignedTo != user && !exConfig?.assignedToGroup?.members?.contains(user) && !(isAutoShare && isValidAutoShare) && getAssignedToGroup == null && user.id != exConfig?.assignedTo?.id && !config?.getShareWithUsers()?.contains(user) && !config?.getShareWithGroups()?.findAll { groups*.id.contains(it.id) }) {
            return true
        }
        return false
    }

    boolean roleAuthorised(String alertType = null, String selectedDatasource = null) {
        User user = userService.getUser()
        Set<String> roles = user?.getEnabledAuthorities()?.collect { it?.authority }
        List authorisedRoles = []
        if (alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
            authorisedRoles = ['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_ADMIN', 'ROLE_DEV', 'ROLE_SINGLE_CASE_REVIEWER', 'ROLE_SINGLE_CASE_VIEWER', 'ROLE_VIEW_ALL']
        } else if (alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            authorisedRoles = ['ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_ADMIN', 'ROLE_DEV', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIEW_ALL']
            if (selectedDatasource?.toLowerCase()?.contains("faers")) {
                authorisedRoles = authorisedRoles + ['ROLE_FAERS_CONFIGURATION']
            } else if (selectedDatasource?.toLowerCase()?.contains("vaers")) {
                authorisedRoles = authorisedRoles + ['ROLE_VAERS_CONFIGURATION']
            } else if (selectedDatasource?.toLowerCase()?.contains("vigibase")) {
                authorisedRoles = authorisedRoles + ['ROLE_VIGIBASE_CONFIGURATION']
            } else if (selectedDatasource?.toLowerCase()?.contains("jader")) {
                authorisedRoles = authorisedRoles + ['ROLE_JADER_CONFIGURATION']
            }
        } else if (alertType == Constants.AlertConfigType.EVDAS_ALERT) {
            authorisedRoles = ['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_ADMIN', 'ROLE_DEV', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL']
        } else if (alertType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
            authorisedRoles = ['ROLE_LITERATURE_CASE_CONFIGURATION', 'ROLE_ADMIN', 'ROLE_DEV', 'ROLE_LITERATURE_CASE_REVIEWER', 'ROLE_LITERATURE_CASE_VIEWER', 'ROLE_VIEW_ALL']
        } else if (alertType == Constants.AlertConfigType.AD_HOC_ALERT) {
            authorisedRoles = ['ROLE_AD_HOC_CRUD', 'ROLE_ADMIN', 'ROLE_DEV', 'ROLE_VIEW_ALL']
        }
        boolean roleAuthorised = false
        for (int i = 0; i < authorisedRoles?.size(); i++) {
            if (roles?.contains(authorisedRoles?.get(i))) {
                roleAuthorised = true
                break
            }
        }
        return roleAuthorised
    }

    boolean checkAlertSharedToCurrentUserEvdas(ExecutedEvdasConfiguration exConfig) {
        //checking is current loggged-in user have access to the evdas alert
        if (!exConfig) {
            return true;
        }
        EvdasConfiguration config = EvdasConfiguration.get(exConfig?.configId)
        User user = userService.getUser()
        Set<Group> groups = Group.findAllUserGroupByUser(user)
        Group getAssignedToGroup = groups.find { group -> group.id == exConfig?.assignedToGroup?.id }

        if (exConfig?.owner != user && exConfig?.assignedTo != user && !exConfig?.assignedToGroup?.members?.contains(user) && getAssignedToGroup == null && user.id != exConfig?.assignedTo?.id && !config?.getShareWithUsers()?.contains(user) && !config?.getShareWithGroups()?.each { groups?.contains(it) }) {
            return true
        }
        return false
    }

    void dataCleanUpForFailedExecution(ExecutedConfiguration executedConfiguration, Configuration configuration) {
        def dataExists
        List<Long> prevExecConfigId = fetchPrevExecConfigId(executedConfiguration, configuration, false, true) as List<Long>
        Long prevExecutionId = (prevExecConfigId ? prevExecConfigId[0] : 0) as Long
        if (executedConfiguration.type == 'Single Case Alert') {
            dataExists = (SingleCaseAlert.findByExecutedAlertConfiguration(executedConfiguration) || ArchivedSingleCaseAlert.findByExecutedAlertConfiguration(ExecutedConfiguration.get(prevExecutionId)))
        } else if (executedConfiguration.type == 'Aggregate Case Alert') {
            dataExists = (AggregateCaseAlert.findByExecutedAlertConfiguration(executedConfiguration)|| ArchivedAggregateCaseAlert.findByExecutedAlertConfiguration(ExecutedConfiguration.get(prevExecutionId)))
        }
        if (dataExists) {
            Sql sql
            try {
                log.info("Data Cleanup Started For Failed Execution ${executedConfiguration.id}, previous execution id restored ${prevExecConfigId}, config id is ${configuration.id} and type is ${configuration.type}")
                sql = new Sql(signalDataSourceService.getReportConnectionWithDisabledAutoCommit('dataSource'))
                String sqlQuery
                if (configuration.type == "Single Case Alert") {
                    archiveService.moveAttachementFromArchive(SingleCaseAlert, configuration.id, prevExecutionId, sql)
                    sqlQuery = SignalQueryHelper.icr_data_clean_for_failed_execution(configuration.id, prevExecutionId, executedConfiguration.id)
                    if (prevExecConfigId.size() > 0) {
                        alertDeletionDataService.revertIsLatestForCaseHistory(prevExecConfigId, configuration.id)
                    }
                } else if (configuration.type == "Aggregate Case Alert") {
                    archiveService.moveAttachementFromArchive(AggregateCaseAlert, configuration.id, prevExecutionId, sql)
                    sqlQuery = SignalQueryHelper.agg_data_clean_for_failed_execution(configuration.id, prevExecutionId, executedConfiguration.id)
                }
                sql.execute(sqlQuery)
                sql.connection.commit()
                log.info("Data Clean up of ${configuration.name} failed execution completed.")
            } catch (Exception ex) {
                sql.connection.rollback()
                ex.printStackTrace()
            } finally {
                sql?.close()
            }

        }

    }

}
