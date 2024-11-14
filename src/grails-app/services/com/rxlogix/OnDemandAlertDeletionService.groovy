package com.rxlogix


import com.rxlogix.config.Configuration
import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import grails.gorm.transactions.Transactional
import org.hibernate.criterion.CriteriaSpecification

@Transactional
class OnDemandAlertDeletionService {

    def grailsApplication
    def singleOnDemandAlertService
    def aggregateOnDemandAlertService
    def evdasOnDemandAlertService

    void startDeletingOnDemandAlert() {
        try {
            log.info("Job for deleting the On Demand Alert Starts")
            Integer timespan = grailsApplication.config.signal.timespan.deletion.ondemand.alert
            deletingSingleOnDemandAlert(timespan)
            deletingAggregateOnDemandAlert(timespan)
            deletingEvdasOnDemandAlert(timespan)
        } catch (Throwable th) {
            log.error(th.getMessage(), th)
        }
    }

    void deletingSingleOnDemandAlert(Integer timespan) {
        List<Map> onDemandConfigurationList = Configuration.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "id")
                property("dateCreated", "dateCreated")
            }
            eq("isDeleted", false)
            eq('adhocRun', true)
            eq("type", Constants.AlertConfigType.SINGLE_CASE_ALERT)
        } as List<Map>

        onDemandConfigurationList.each { Map configDataMap ->
            if (configDataMap.dateCreated.clearTime() <= (new Date().clearTime() - timespan)) {
                List<ExecutedConfiguration> executedConfigurationList = ExecutedConfiguration.findAllByConfigId(configDataMap.id as Long)
                if(executedConfigurationList){
                    executedConfigurationList.each {
                        singleOnDemandAlertService.deleteOnDemandAlert(it)
                    }
                }
            }
        }
    }

    void deletingAggregateOnDemandAlert(Integer timespan) {
        List<Map> onDemandConfigurationList = Configuration.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "id")
                property("dateCreated", "dateCreated")
            }
            eq("isDeleted", false)
            eq('adhocRun', true)
            eq("type", Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
        } as List<Map>

        onDemandConfigurationList.each { Map configDataMap ->
            if (configDataMap.dateCreated.clearTime() <= (new Date().clearTime() - timespan)) {
                List<ExecutedConfiguration> executedConfigurationList = ExecutedConfiguration.findAllByConfigId(configDataMap.id as Long)
                if(executedConfigurationList){
                    executedConfigurationList.each {
                        aggregateOnDemandAlertService.deleteOnDemandAlert(it)
                    }
                }
            }
        }
    }

    void deletingEvdasOnDemandAlert(Integer timespan) {
        List<Map> onDemandConfigurationList = EvdasConfiguration.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "id")
                property("dateCreated", "dateCreated")
            }
            eq("isDeleted", false)
            eq('adhocRun', true)
        } as List<Map>

        onDemandConfigurationList.each { Map configDataMap ->
            if (configDataMap.dateCreated.clearTime() <= (new Date().clearTime() - timespan)) {
                List<ExecutedEvdasConfiguration> executedEvdasConfigurationList = ExecutedEvdasConfiguration.findAllByConfigId(configDataMap.id as Long)
                if(executedEvdasConfigurationList){
                    executedEvdasConfigurationList.each {
                        evdasOnDemandAlertService.deleteOnDemandAlert(it)
                    }
                }
            }
        }
    }
}
