package com.rxlogix.util


import com.rxlogix.Constants
import com.rxlogix.dto.AlertDTO
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Holders

class SignalQueryHelper {

    /* used instead of User.getWorkflowGroup() cause performance optimization */
    static workflow_group_id_sql = { Long userId ->
        """
        SELECT 
            g.id FROM GROUPS g
        WHERE
            g.group_type = 'WORKFLOW_GROUP'
            AND g.id IN (
                SELECT
                    ug_s.group_id
                FROM
                    USER_GROUP_S ug_s 
                WHERE
                    ug_s.user_id = ${userId}
                UNION
                SELECT
                    ugm.group_id 
                FROM
                    USER_GROUP_MAPPING ugm 
                WHERE
                    ugm.user_id = ${userId}
            )
            AND rownum <= 1
        """
    }

    static single_case_and_single_archive_alert_list_sql = { Long signalId ->
        """
        WITH sca_ids AS (
            SELECT single_alert_id AS id FROM validated_single_alerts WHERE validated_signal_id = ${signalId}
        )
        SELECT * FROM (SELECT
            sca.id as id,
            sca.alert_configuration_id as alertConfigId,
            sca.name as alertName,
            sca.case_number as caseNumber,
            sca.case_id as caseId,
            sca.product_name as productName,
            sca.product_family as productFamily,
            sca.master_pref_term_all as masterPrefTermAll,
            sca.case_version as caseVersion,
            sca.follow_up_number as followUpNumber,
            sca.exec_config_id as execConfigId,
            d.display_name as disposition,
            sca.priority_id as priorityId,
            conf.is_standalone as isStandalone,
            0 as isArchived
        FROM
            SINGLE_CASE_ALERT sca
        INNER JOIN
            VALIDATED_SINGLE_ALERTS vsa ON sca.id = vsa.single_alert_id
        INNER JOIN
            VALIDATED_SIGNAL vs ON vsa.validated_signal_id = vs.id
        INNER JOIN
            DISPOSITION d ON sca.disposition_id = d.id
        LEFT OUTER JOIN
            RCONFIG conf ON sca.alert_configuration_id = conf.id
        WHERE
            vs.id = ${signalId}
            AND (vs.signal_status != 'Date Closed' OR (vs.milestone_completion_date IS NOT NULL
              AND sca.id IN (SELECT si.id FROM sca_ids si)
            ))
        ORDER BY
            sca.id DESC)
            
        UNION ALL
        
        SELECT * FROM (SELECT
            sca.id as id,
            sca.alert_configuration_id as alertConfigId,
            sca.name as alertName,
            sca.case_number as caseNumber,
            sca.case_id as caseId,
            sca.product_name as productName,
            sca.product_family as productFamily,
            sca.master_pref_term_all as masterPrefTermAll,
            sca.case_version as caseVersion,
            sca.follow_up_number as followUpNumber,
            sca.exec_config_id as execConfigId,
            d.display_name as disposition,
            sca.priority_id as priorityId,
            0 as isStandalone,
            1 as isArchived
        FROM
            ARCHIVED_SINGLE_CASE_ALERT sca
        INNER JOIN
            VALIDATED_ARCHIVED_SCA vsa ON sca.id = vsa.ARCHIVED_SCA_ID
        INNER JOIN
            VALIDATED_SIGNAL vs ON vsa.validated_signal_id = vs.id
        INNER JOIN
            DISPOSITION d ON sca.disposition_id = d.id
        WHERE
            vs.id = ${signalId}
        ORDER BY
            sca.id DESC)
        """
    }

    static agg_evdas_combined_alert_list_sql = { signalId, Long userId, dataSourcePva, String dataSourceEvdas, String searchTerm ->
        def integerSearchTermFragment = searchTerm.isInteger() ? """
            OR newCount1 = ${searchTerm}
            OR cumCount1 = ${searchTerm}
            OR newSeriousCount = ${searchTerm}
            OR cumSeriousCount = ${searchTerm}
        """ : """"""

        def doubleSearchTermFragment = searchTerm.isDouble() ? """
            OR prrValue = ${searchTerm}
            OR rorValue = ${searchTerm}
            OR ebgm = ${searchTerm}
            OR eb05 = ${searchTerm}
            OR eb95 = ${searchTerm}
        """ : """"""

        def searchTermFragment = searchTerm ? """
            AND LOWER(alertName) LIKE '%' || LOWER('${searchTerm}') || '%'
            OR LOWER(productName) LIKE '%' || LOWER('${searchTerm}') || '%'
            OR LOWER(soc) LIKE '%' || LOWER('${searchTerm}') || '%'
            OR LOWER(preferredTerm) LIKE '%' || LOWER('${searchTerm}') || '%'
            OR (
                LOWER(dataSource) LIKE '%' || LOWER('${searchTerm}') || '%'
            )
            OR (
                LOWER(disposition) LIKE '%' || LOWER('${searchTerm}') || '%'
            )
            ${integerSearchTermFragment}
            ${doubleSearchTermFragment}
        """ : """"""

        def aggAlertsColumns = """
            aa.id as id,
            aa.alert_configuration_id as alertConfigId,
            aa.name as alertName,
            aa.product_id as productId,
            aa.product_name as productName,
            aa.soc as soc,
            aa.pt as preferredTerm,
            aa.pt_code as ptCode,
            aa.exec_configuration_id as execConfigId,
            conf.selected_data_source as dataSourceValue,

            CASE conf.selected_data_source WHEN '%pva%' THEN '${dataSourcePva}' ELSE 
                /* replace pva in case of complex selected_data_source like pva,vaers or eudra,vaers,pva */
                DECODE(
                    INSTR(conf.selected_data_source,'pva,'), 
                    0, 
                    REPLACE(UPPER(conf.selected_data_source),',PVA',',${dataSourcePva}'),
                    REPLACE(UPPER(conf.selected_data_source),'PVA,','${dataSourcePva},')
                )
            END as dataSource,

            d.display_name as disposition,
    
            CASE 
                WHEN ex_conf.selected_data_source like '%pva%' THEN 
                    CASE WHEN aa.new_count != -1 THEN aa.new_count ELSE null END
                WHEN ex_conf.selected_data_source like '%faers%' THEN
                    TO_NUMBER(JSON_VALUE(aa.faers_columns, '\$.newCountFaers'))
                WHEN ex_conf.selected_data_source like '%vaers%' THEN
                    TO_NUMBER(JSON_VALUE(aa.vaers_columns, '\$.newCountVaers'))
                WHEN ex_conf.selected_data_source like '%vigibase%' THEN
                    TO_NUMBER(JSON_VALUE(aa.vigibase_columns, '\$.newCountVigibase'))
                WHEN ex_conf.selected_data_source like '%jader%' THEN
                    TO_NUMBER(JSON_VALUE(aa.jader_columns, '\$.newCountJader'))
                ELSE CASE WHEN aa.new_count != -1 THEN aa.new_count ELSE null END
            END as newCount1,
            
            CASE
                WHEN ex_conf.selected_data_source like '%pva%' THEN 
                    CASE WHEN aa.cumm_count != -1 THEN aa.cumm_count ELSE null END
                WHEN ex_conf.selected_data_source like '%faers%' THEN
                    TO_NUMBER(JSON_VALUE(aa.faers_columns, '\$.cummCountFaers'))
                WHEN ex_conf.selected_data_source like '%vaers%' THEN
                    TO_NUMBER(JSON_VALUE(aa.vaers_columns, '\$.cummCountVaers'))
                WHEN ex_conf.selected_data_source like '%vigibase%' THEN
                    TO_NUMBER(JSON_VALUE(aa.vigibase_columns, '\$.cummCountVigibase'))
                WHEN ex_conf.selected_data_source like '%jader%' THEN
                    /* jader_columns contains cumCountJader instead of cummCountJader */
                    TO_NUMBER(JSON_VALUE(aa.jader_columns, '\$.cumCountJader'))
                ELSE CASE WHEN aa.cumm_count != -1 THEN aa.cumm_count ELSE null END
            END as cumCount1,
            
            CASE 
                WHEN ex_conf.selected_data_source like '%pva%' THEN 
                    CASE WHEN aa.new_serious_count != -1 THEN aa.new_serious_count ELSE null END
                WHEN ex_conf.selected_data_source like '%faers%' THEN
                    TO_NUMBER(JSON_VALUE(aa.faers_columns, '\$.newSeriousCountFaers'))
                WHEN ex_conf.selected_data_source like '%vaers%' THEN
                    TO_NUMBER(JSON_VALUE(aa.vaers_columns, '\$.newSeriousCountVaers'))
                WHEN ex_conf.selected_data_source like '%vigibase%' THEN
                    TO_NUMBER(JSON_VALUE(aa.vigibase_columns, '\$.newSeriousCountVigibase'))
                WHEN ex_conf.selected_data_source like '%jader%' THEN
                    null
                ELSE CASE WHEN aa.new_serious_count != -1 THEN aa.new_serious_count ELSE null END
            END as newSeriousCount,
            
            CASE 
                WHEN ex_conf.selected_data_source like '%pva%' THEN 
                    CASE WHEN aa.cum_serious_count != -1 THEN aa.cum_serious_count ELSE null END
                WHEN ex_conf.selected_data_source like '%faers%' THEN
                    TO_NUMBER(JSON_VALUE(aa.faers_columns, '\$.cumSeriousCountFaers'))
                WHEN ex_conf.selected_data_source like '%vaers%' THEN
                    TO_NUMBER(JSON_VALUE(aa.vaers_columns, '\$.cumSeriousCountVaers'))
                WHEN ex_conf.selected_data_source like '%vigibase%' THEN
                    TO_NUMBER(JSON_VALUE(aa.vigibase_columns, '\$.cumSeriousCountVigibase'))
                WHEN ex_conf.selected_data_source like '%jader%' THEN
                    null
                ELSE CASE WHEN aa.cum_serious_count != -1 THEN aa.cum_serious_count ELSE null END
            END as cumSeriousCount,
            
            CASE 
                WHEN ex_conf.selected_data_source like '%pva%' THEN 
                    CASE WHEN aa.prr_value != -1.0 THEN aa.prr_value ELSE null END
                WHEN ex_conf.selected_data_source like '%faers%' THEN
                    TO_NUMBER(JSON_VALUE(aa.faers_columns, '\$.prrValueFaers'))
                WHEN ex_conf.selected_data_source like '%vaers%' THEN
                    TO_NUMBER(JSON_VALUE(aa.vaers_columns, '\$.prrValueVaers'))
                WHEN ex_conf.selected_data_source like '%vigibase%' THEN
                    TO_NUMBER(JSON_VALUE(aa.vigibase_columns, '\$.prrValueVigibase'))
                WHEN ex_conf.selected_data_source like '%jader%' THEN
                    TO_NUMBER(JSON_VALUE(aa.jader_columns, '\$.prrValueJader'))
                ELSE CASE WHEN aa.prr_value != -1.0 THEN aa.prr_value ELSE null END
            END as prrValue,
            
            CASE 
                WHEN ex_conf.selected_data_source like '%pva%' THEN 
                    CASE WHEN aa.ror_value != -1.0 THEN aa.ror_value ELSE null END
                WHEN ex_conf.selected_data_source like '%faers%' THEN
                    TO_NUMBER(JSON_VALUE(aa.faers_columns, '\$.rorValueFaers'))
                WHEN ex_conf.selected_data_source like '%vaers%' THEN
                    TO_NUMBER(JSON_VALUE(aa.vaers_columns, '\$.rorValueVaers'))
                WHEN ex_conf.selected_data_source like '%vigibase%' THEN
                    TO_NUMBER(JSON_VALUE(aa.vigibase_columns, '\$.rorValueVigibase'))
                WHEN ex_conf.selected_data_source like '%jader%' THEN
                    TO_NUMBER(JSON_VALUE(aa.jader_columns, '\$.rorValueJader'))
                ELSE CASE WHEN aa.ror_value != -1.0 THEN aa.ror_value ELSE null END
            END as rorValue,
            
            CASE 
                WHEN ex_conf.selected_data_source like '%pva%' THEN 
                    CASE WHEN aa.ebgm != -1.0 THEN aa.ebgm ELSE null END
                WHEN ex_conf.selected_data_source like '%faers%' THEN
                    TO_NUMBER(JSON_VALUE(aa.faers_columns, '\$.ebgmFaers'))
                WHEN ex_conf.selected_data_source like '%vaers%' THEN
                    TO_NUMBER(JSON_VALUE(aa.vaers_columns, '\$.ebgmVaers'))
                WHEN ex_conf.selected_data_source like '%vigibase%' THEN
                    TO_NUMBER(JSON_VALUE(aa.vigibase_columns, '\$.ebgmVigibase'))
                WHEN ex_conf.selected_data_source like '%jader%' THEN
                    TO_NUMBER(JSON_VALUE(aa.jader_columns, '\$.ebgmJader'))
                ELSE CASE WHEN aa.ebgm != -1.0 THEN aa.ebgm ELSE null END
            END as ebgm,
            
            CASE
                WHEN ex_conf.selected_data_source like '%pva%' THEN 
                    CASE WHEN aa.eb05 != -1.0 THEN aa.eb05 ELSE null END
                WHEN ex_conf.selected_data_source like '%faers%' THEN
                    TO_NUMBER(JSON_VALUE(aa.faers_columns, '\$.eb05Faers'))
                WHEN ex_conf.selected_data_source like '%vaers%' THEN
                    TO_NUMBER(JSON_VALUE(aa.vaers_columns, '\$.eb05Vaers'))
                WHEN ex_conf.selected_data_source like '%vigibase%' THEN
                    TO_NUMBER(JSON_VALUE(aa.vigibase_columns, '\$.eb05Vigibase'))
                WHEN ex_conf.selected_data_source like '%jader%' THEN
                    TO_NUMBER(JSON_VALUE(aa.jader_columns, '\$.eb05Jader'))
                ELSE CASE WHEN aa.eb05 != -1.0 THEN aa.eb05 ELSE null END
            END as eb05,
            
            CASE 
                WHEN ex_conf.selected_data_source like '%pva%' THEN 
                    CASE WHEN aa.eb95 != -1.0 THEN aa.eb95 ELSE null END
                WHEN ex_conf.selected_data_source like '%faers%' THEN
                    TO_NUMBER(JSON_VALUE(aa.faers_columns, '\$.eb95Faers'))
                WHEN ex_conf.selected_data_source like '%vaers%' THEN
                    TO_NUMBER(JSON_VALUE(aa.vaers_columns, '\$.eb95Vaers'))
                WHEN ex_conf.selected_data_source like '%vigibase%' THEN
                    TO_NUMBER(JSON_VALUE(aa.vigibase_columns, '\$.eb95Vigibase'))
                WHEN ex_conf.selected_data_source like '%jader%' THEN
                    TO_NUMBER(JSON_VALUE(aa.jader_columns, '\$.eb95Jader'))
                ELSE CASE WHEN aa.eb95 != -1.0 THEN aa.eb95 ELSE null END
            END as eb95,
            
            1 as isAggAlert
        """

        def evdasAlertsColumns = """
            ea.id as id,
            ea.alert_configuration_id as alertConfigId,
            ea.name as alertName,
            ea.substance_id as productId,
            ea.substance as productName,
            ea.soc as soc,
            ea.pt as preferredTerm,
            ea.pt_code as ptCode,
            ea.exec_configuration_id as execConfigId,
            '${dataSourceEvdas}' as dataSourceValue,
            '${dataSourceEvdas.toUpperCase()}' as dataSource,
            d.display_name as disposition,
    
            TO_NUMBER(ea.new_ev) as newCount1,
            TO_NUMBER(ea.total_ev) as cumCount1,
            TO_NUMBER(ea.new_serious) as newSeriousCount,
            TO_NUMBER(ea.total_serious) as cumSeriousCount,
            TO_NUMBER(null) as prrValue,
            TO_NUMBER(ea.ror_value) as rorValue,
            TO_NUMBER(null) as ebgm,
            TO_NUMBER(null) as eb05,
            TO_NUMBER(null) as eb95,
            
            0 as isAggAlert
        """

        """
        WITH agg_ids AS (
            SELECT agg_alert_id as id FROM VALIDATED_AGG_ALERTS WHERE validated_signal_id = ${signalId}
        ),
        evdas_ids AS (
            SELECT evdas_alert_id as id FROM VALIDATED_EVDAS_ALERTS WHERE validated_signal_id = ${signalId}
        ),
        workflow_group AS (
            ${SignalQueryHelper.workflow_group_id_sql(userId)}
        )

        /* AGG ALERTS */
        SELECT * FROM (
        
            SELECT * FROM (SELECT
                ${aggAlertsColumns},
                0 as isArchived
                
            FROM
                AGG_ALERT aa
                INNER JOIN
                    VALIDATED_AGG_ALERTS vaa ON aa.id = vaa.agg_alert_id
                INNER JOIN
                    VALIDATED_SIGNAL vs ON vaa.validated_signal_id = vs.id
                INNER JOIN
                    DISPOSITION d ON aa.disposition_id = d.id
                LEFT OUTER JOIN
                    RCONFIG conf ON aa.alert_configuration_id = conf.id
                LEFT OUTER JOIN
                    EX_RCONFIG ex_conf ON aa.exec_configuration_id = ex_conf.id
            WHERE
                vs.id = ${signalId}
                AND ex_conf.is_deleted = 0
                AND ex_conf.is_enabled = 1
                AND ex_conf.adhoc_run = 0
                AND ex_conf.workflow_group = (SELECT wg.id FROM workflow_group wg)
                AND (
                    vs.signal_status != 'Date Closed'
                    OR (vs.milestone_completion_date IS NOT NULL AND aa.id IN (SELECT ai.id FROM agg_ids ai))
                )
            ORDER BY aa.id DESC)
                
            UNION ALL    
                
            SELECT * FROM (SELECT
                ${aggAlertsColumns},
                1 as isArchived
                
            FROM
                ARCHIVED_AGG_ALERT aa
                INNER JOIN
                    VALIDATED_ARCHIVED_ACA vaa ON aa.id = vaa.archived_aca_id
                INNER JOIN
                    VALIDATED_SIGNAL vs ON vaa.validated_signal_id = vs.id
                INNER JOIN
                    DISPOSITION d ON aa.disposition_id = d.id
                LEFT OUTER JOIN
                    RCONFIG conf ON aa.alert_configuration_id = conf.id
                LEFT OUTER JOIN
                    EX_RCONFIG ex_conf ON aa.exec_configuration_id = ex_conf.id
            WHERE
                vs.id = ${signalId}
                AND ex_conf.is_deleted = 0
                AND ex_conf.is_enabled = 1
                AND ex_conf.adhoc_run = 0
                AND ex_conf.workflow_group = (SELECT wg.id FROM workflow_group wg)
            ORDER BY aa.id DESC)
        )
        WHERE
            1=1
            ${searchTermFragment}
                
        UNION ALL 
        
        /* EVDAS ALERTS */
        SELECT * FROM (
        
            SELECT * FROM (SELECT
                ${evdasAlertsColumns},
                0 as isArchived
                
            FROM
                EVDAS_ALERT ea
                INNER JOIN
                    VALIDATED_EVDAS_ALERTS vea ON ea.id = vea.evdas_alert_id
                INNER JOIN
                    VALIDATED_SIGNAL vs ON vea.validated_signal_id = vs.id
                INNER JOIN
                    DISPOSITION d ON ea.disposition_id = d.id
                LEFT OUTER JOIN
                    EVDAS_CONFIG conf ON ea.alert_configuration_id = conf.id
                LEFT OUTER JOIN
                    EX_EVDAS_CONFIG ex_conf ON ea.exec_configuration_id = ex_conf.id
            WHERE
                vs.id = ${signalId}
                AND ex_conf.is_deleted = 0
                AND ex_conf.is_enabled = 1
                AND ex_conf.adhoc_run = 0
                AND ex_conf.workflow_group = (SELECT wg.id FROM workflow_group wg)
                AND (
                    vs.signal_status != 'Date Closed'
                    OR (vs.milestone_completion_date IS NOT NULL AND ea.id IN (SELECT ei.id FROM evdas_ids ei))
                )
            ORDER BY ea.id DESC)
            
            UNION ALL
            
            SELECT * FROM (SELECT
                ${evdasAlertsColumns},
                1 as isArchived
                
            FROM
                ARCHIVED_EVDAS_ALERT ea
                INNER JOIN
                    VALIDATED_ARCH_EVDAS_ALERTS vea ON ea.id = vea.archived_evdas_alert_id
                INNER JOIN
                    VALIDATED_SIGNAL vs ON vea.validated_signal_id = vs.id
                INNER JOIN
                    DISPOSITION d ON ea.disposition_id = d.id
                LEFT OUTER JOIN
                    EVDAS_CONFIG conf ON ea.alert_configuration_id = conf.id
                LEFT OUTER JOIN
                    EX_EVDAS_CONFIG ex_conf ON ea.exec_configuration_id = ex_conf.id
            WHERE
                vs.id = ${signalId}
                AND ex_conf.is_deleted = 0
                AND ex_conf.is_enabled = 1
                AND ex_conf.adhoc_run = 0
                AND ex_conf.workflow_group = (SELECT wg.id FROM workflow_group wg)
            ORDER BY ea.id DESC)
        )    
        WHERE
            1=1
            ${searchTermFragment}
        """
    }

    static event_prod_info_sql = { cl, eventCode, eventCodeVal ->
        """
            SELECT
            COUNT (DISTINCT b.case_id) COUNT, a.case_num CASE_NUMBER, a.version_num VERSION, a.${eventCodeVal} EVENT_VAL
               FROM
            (SELECT caei.mdr_ae_pt_code, caei.mdr_ae_pt, caei.mdr_ae_llt_code,
                caei.mdr_ae_llt, caei.mdr_ae_hlt, caei.mdr_ae_hlt_code,
                caei.mdr_ae_hlgt_code, caei.mdr_ae_hlgt, caei.mdr_ae_soc_code,
                caei.mdr_ae_soc, ci.case_num, ci.case_id, ci.version_num
            FROM c_ae_identification caei JOIN c_identification_fu cifu
                ON caei.case_id = cifu.case_id
                AND caei.version_num = cifu.version_num
                AND caei.tenant_id = cifu.tenant_id
                AND caei.ae_rec_num = cifu.prim_ae_rec_num
                JOIN c_identification ci
                ON cifu.case_id = ci.case_id
                AND cifu.version_num = ci.version_num
                AND cifu.tenant_id = ci.tenant_id
                AND flag_primary_ae = 1
                AND mdr_ae_pt IS NOT NULL
                AND (ci.case_num, ci.version_num) IN (${cl})
            ) a, (
            SELECT caei.mdr_ae_pt_code, caei.mdr_ae_pt, caei.mdr_ae_llt_code,
                caei.mdr_ae_llt, caei.mdr_ae_hlt, caei.mdr_ae_hlt_code,
                caei.mdr_ae_hlgt_code, caei.mdr_ae_hlgt, caei.mdr_ae_soc_code,
                caei.mdr_ae_soc, ci.case_num, ci.case_id, ci.version_num
            FROM c_ae_identification caei JOIN c_identification ci
            ON caei.case_id = ci.case_id
            AND caei.version_num = ci.version_num
            AND caei.tenant_id = ci.tenant_id
            AND mdr_ae_pt IS NOT NULL
            AND (ci.case_num, ci.version_num) IN (${cl})) b
            WHERE a.${eventCode} = b.${eventCode}
            GROUP BY a.case_id, a.case_num, a.${eventCodeVal}, a.version_num       
        """
    }

    static faers_date_range = { "SELECT DECODE(SUBSTR(ETL_VALUE, 6, 7), 'Q1', '31-MAR-', 'Q2', '30-JUN-', 'Q3', '30-SEP-', 'Q4', '31-DEC-', '') " +
            "|| SUBSTR(ETL_VALUE, 1, 4) AS FAERS_DATE FROM PVR_ETL_CONSTANTS WHERE ETL_KEY = 'FAERS_PROCESSED_QUARTER'" }
    static jader_date_range = { "SELECT DECODE(SUBSTR(ETL_VALUE, 6, 7), 'Q4', '31-MAR-', 'Q1', '30-JUN-', 'Q2', '30-SEP-', 'Q3', '31-DEC-', '') " +
            "|| SUBSTR(ETL_VALUE, 1, 4) AS JADER_DATE FROM PVR_ETL_CONSTANTS WHERE ETL_KEY = 'JADER_PROCESSED_QUARTER'" }
    static vaers_date_range = { "select ETL_VALUE AS vaers_date  from pvr_etl_constants where etl_key = 'VAERS_LATEST_PROCESSED_DATE'" }
    static vigibase_date_range = {"SELECT DECODE(SUBSTR(ETL_VALUE, 6, 7), 'Q1', '31-MAR-', 'Q2', '30-JUN-', 'Q3', '30-SEP-', 'Q4', '31-DEC-', '') " +
            "|| SUBSTR(ETL_VALUE, 1, 4) AS VIGIBASE_DATE FROM PVR_ETL_CONSTANTS WHERE ETL_KEY = 'VIGIBASE_PROCESSED_QUARTER'"}

    static vigibase_date_range_display = { "select ETL_VALUE AS vigibase_date  from pvr_etl_constants where etl_key = 'VIGIBASE_LATEST_PROCESSED_DATE'" }
    static jader_date_range_display = { "select ETL_VALUE AS jader_date  from pvr_etl_constants where etl_key = 'JADER_LATEST_PROCESSED_DATE'" }
    static statification_enabled_ebgm = {"select count(1) from pvs_constants_ebgm where pvs_key like 'STR_%' and pvs_value=1"}
    static statification_enabled = {dataSource -> "select count(1) from VW_ADMIN_APP_CONFIG where CONFIG_KEY like 'STR_%' and JSON_VALUE(config_value,'\$.PVS_VALUE') = '1' and APPLICATION_NAME = '${dataSource}'"}

    static mining_variable_statification_enabled_ebgm = { keyId,dataSource -> "select count(1) from VW_ADMIN_APP_CONFIG where CONFIG_KEY='BS_${keyId}' and APPLICATION_NAME = '${dataSource}' and JSON_VALUE(CONFIG_VALUE,'\$.EBGM_STRATIFICATION') is not null"}
    static statification_enabled_data_Mining = {keyId,dataSource -> "select count(1) from VW_ADMIN_APP_CONFIG where CONFIG_KEY='BS_${keyId}' and APPLICATION_NAME = '${dataSource}' and JSON_VALUE(CONFIG_VALUE,'\$.PRR_STRATIFICATION') is not null"}
    static stratification_values_ebgm = {dataSource ->
        """
( SELECT
    'age' param,
    age_group param_value
FROM
    (
        ( SELECT
            age_group,
            1 is_custom
        FROM
            str_pvs_age_group_config
        UNION ALL
        SELECT
            'UNK' age_group,
            1 is_custom
        FROM
            dual
        )
        UNION ALL
        ( SELECT
            age_group
            || '>='
            || group_low
            || ' and <'
            || group_high
            || ' Years' age_group,
            0 is_custom
        FROM
            vw_lag_age_group
        UNION ALL
        SELECT
            'UNK' age_group,
            0 is_custom
        FROM
            dual
        )
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_AGE_GRP_STR' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            JSON_VALUE(config_value,'\$.PVS_VALUE') as pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'EBGM_STR_AGE_GROUP' and APPLICATION_NAME = '${dataSource}'
    ) = 1
)
UNION ALL
( SELECT
    'gender' param,
    gender
FROM
    (
        SELECT
            gender,
            0 is_custom
        FROM
            vw_lg_gender
        UNION ALL
        ( SELECT
            'Male' gender,
            1 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            'Female' gender,
            1 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            'Unknown' gender,
            1 is_custom
        FROM
            dual
        )
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_GENDER_STR' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            JSON_VALUE(config_value,'\$.PVS_VALUE') as pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'EBGM_STR_GENDER' and APPLICATION_NAME = '${dataSource}'
    ) = 1
)
UNION ALL
( SELECT
    'receipt_years' param,
    year
FROM
    (
        SELECT
            'All Years' year,
            0 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            year_group year,
            1 is_custom
        FROM
            pvs_ltst_date_config
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_LATEST_YEAR_GRP' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            JSON_VALUE(config_value,'\$.PVS_VALUE') as pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'EBGM_STR_LATEST_DATE' and APPLICATION_NAME = '${dataSource}'
    ) = 1
)
        """
    }
    static stratification_values_ebgm_jader = {dataSource ->
        """
( SELECT
    'age' param,
    age_group param_value
FROM
    (
        ( SELECT
            age_group,
            1 is_custom
        FROM
            str_pvs_age_group_config
        UNION ALL
        SELECT
            'UNK' age_group,
            1 is_custom
        FROM
            dual
        )
        UNION ALL
        ( SELECT
            age_group
            || '>='
            || group_low
            || ' and <'
            || group_high
            || ' Years' age_group,
            0 is_custom
        FROM
            vw_lag_age_group
        UNION ALL
        SELECT
            'UNK' age_group,
            0 is_custom
        FROM
            dual
        )
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_AGE_GRP_STR' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            JSON_VALUE(config_value,'\$.PVS_VALUE') as pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'EBGM_STR_AGE_GROUP' and APPLICATION_NAME = '${dataSource}'
    ) = 1
)
UNION ALL
( SELECT
    'gender' param,
    gender
FROM
    (
        SELECT
            gender,
            0 is_custom
        FROM
            vw_lg_gender
        UNION ALL
        ( SELECT
            '男' gender,
            1 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            '女性' gender,
            1 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            '未知' gender,
            1 is_custom
        FROM
            dual
        )
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_GENDER_STR' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            JSON_VALUE(config_value,'\$.PVS_VALUE') as pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'EBGM_STR_GENDER' and APPLICATION_NAME = '${dataSource}'
    ) = 1
)
UNION ALL
( SELECT
    'receipt_years' param,
    year
FROM
    (
        SELECT
            '通年' year,
            0 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            year_group year,
            1 is_custom
        FROM
            pvs_ltst_date_config
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_LATEST_YEAR_GRP' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            JSON_VALUE(config_value,'\$.PVS_VALUE') as pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'EBGM_STR_LATEST_DATE' and APPLICATION_NAME = '${dataSource}'
    ) = 1
)
        """
    }

    static stratification_values = { dataSource ->
        """
( SELECT
    'age' param,
    age_group param_value
FROM
    (
        ( SELECT
            age_group,
            1 is_custom
        FROM
            str_pvs_age_group_config
        UNION ALL
        SELECT
            'UNK' age_group,
            1 is_custom
        FROM
            dual
        )
        UNION ALL
        ( SELECT
            age_group
            || '>='
            || group_low
            || ' and <'
            || group_high
            || ' Years' age_group,
            0 is_custom
        FROM
            vw_lag_age_group
        UNION ALL
        SELECT
            'UNK' age_group,
            0 is_custom
        FROM
            dual
        )
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_AGE_GRP_STR' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            JSON_VALUE(config_value,'\$.PVS_VALUE') as pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'STR_AGE_GROUP' and APPLICATION_NAME = '${dataSource}' 
    ) = 1
)
UNION ALL
( SELECT
    'gender' param,
    gender
FROM
    (
        SELECT
            gender,
            0 is_custom
        FROM
            vw_lg_gender
        UNION ALL
        ( SELECT
            'Male' gender,
            1 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            'Female' gender,
            1 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            'Unknown' gender,
            1 is_custom
        FROM
            dual
        )
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_GENDER_STR' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'STR_GENDER' and APPLICATION_NAME = '${dataSource}' 
    ) = 1
)
UNION ALL
( SELECT
    'receipt_years' param,
    year
FROM
    (
        SELECT
            'All Years' year,
            0 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            year_group year,
            1 is_custom
        FROM
            pvs_ltst_date_config
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_LATEST_YEAR_GRP' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            JSON_VALUE(config_value,'\$.PVS_VALUE') as pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'STR_LATEST_DATE' and APPLICATION_NAME = '${dataSource}'
            
    ) = 1
)
        """
    }
    static stratification_values_jader = { dataSource ->
        """
( SELECT
    'age' param,
    age_group param_value
FROM
    (
        ( SELECT
            age_group,
            1 is_custom
        FROM
            str_pvs_age_group_config
        UNION ALL
        SELECT
            'UNK' age_group,
            1 is_custom
        FROM
            dual
        )
        UNION ALL
        ( SELECT
            age_group
            || '>='
            || group_low
            || ' and <'
            || group_high
            || ' Years' age_group,
            0 is_custom
        FROM
            vw_lag_age_group
        UNION ALL
        SELECT
            'UNK' age_group,
            0 is_custom
        FROM
            dual
        )
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_AGE_GRP_STR' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            JSON_VALUE(config_value,'\$.PVS_VALUE') as pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'STR_AGE_GROUP' and APPLICATION_NAME = '${dataSource}' 
    ) = 1
)
UNION ALL
( SELECT
    'gender' param,
    gender
FROM
    (
        SELECT
            gender,
            0 is_custom
        FROM
            vw_lg_gender
        UNION ALL
        ( SELECT
            '男' gender,
            1 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            '女性' gender,
            1 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            '未知' gender,
            1 is_custom
        FROM
            dual
        )
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_GENDER_STR' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'STR_GENDER' and APPLICATION_NAME = '${dataSource}' 
    ) = 1
)
UNION ALL
( SELECT
    'receipt_years' param,
    year
FROM
    (
        SELECT
            '通年' year,
            0 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            year_group year,
            1 is_custom
        FROM
            pvs_ltst_date_config
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_LATEST_YEAR_GRP' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            JSON_VALUE(config_value,'\$.PVS_VALUE') as pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'STR_LATEST_DATE' and APPLICATION_NAME = '${dataSource}'
            
    ) = 1
)
        """
    }

    static stratification_values_ebgm_data_Mining = { keyId,dataSource ->
        """
( SELECT
    'age' param,
    age_group param_value
FROM
    (
        ( SELECT
            age_group,
            1 is_custom
        FROM
            str_pvs_age_group_config
        UNION ALL
        SELECT
            'UNK' age_group,
            1 is_custom
        FROM
            dual
        )
        UNION ALL
        ( SELECT
            age_group
            || '>='
            || group_low
            || ' and <'
            || group_high
            || ' Years' age_group,
            0 is_custom
        FROM
            vw_lag_age_group
        UNION ALL
        SELECT
            'UNK' age_group,
            0 is_custom
        FROM
            dual
        )
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_AGE_GRP_STR' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            count(1)
        FROM
            pvs_batch_signal_constants
        WHERE
            key_id = ${keyId} AND
            ebgm_stratification LIKE '%AGE_GROUP%'
    ) = 1
)
UNION ALL
( SELECT
    'gender' param,
    gender
FROM
    (
        SELECT
            gender,
            0 is_custom
        FROM
            vw_lg_gender
        UNION ALL
        ( SELECT
            'Male' gender,
            1 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            'Female' gender,
            1 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            'Unknown' gender,
            1 is_custom
        FROM
            dual
        )
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_GENDER_STR' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            count(1)
        FROM
            pvs_batch_signal_constants
        WHERE
            key_id = ${keyId} AND
            ebgm_stratification LIKE '%GENDER%'
    ) = 1
)
UNION ALL
( SELECT
    'receipt_years' param,
    year
FROM
    (
        SELECT
            'All Years' year,
            0 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            year_group year,
            1 is_custom
        FROM
            pvs_ltst_date_config
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_LATEST_YEAR_GRP' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            count(1)
        FROM
            pvs_batch_signal_constants
        WHERE
            key_id = ${keyId} AND
            ebgm_stratification LIKE '%LATEST_DATE%'
    ) = 1
)
        """
    }

    static stratification_values_data_Mining = { keyId,dataSource ->
        """
( SELECT
    'age' param,
    age_group param_value
FROM
    (
        ( SELECT
            age_group,
            1 is_custom
        FROM
            str_pvs_age_group_config
        UNION ALL
        SELECT
            'UNK' age_group,
            1 is_custom
        FROM
            dual
        )
        UNION ALL
        ( SELECT
            age_group
            || '>='
            || group_low
            || ' and <'
            || group_high
            || ' Years' age_group,
            0 is_custom
        FROM
            vw_lag_age_group
        UNION ALL
        SELECT
            'UNK' age_group,
            0 is_custom
        FROM
            dual
        )
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_AGE_GRP_STR' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            count(1)
        FROM
            pvs_batch_signal_constants
        WHERE
            key_id = ${keyId} AND
            prr_stratification LIKE '%AGE_GROUP%'
    ) = 1
)
UNION ALL
( SELECT
    'gender' param,
    gender
FROM
    (
        SELECT
            gender,
            0 is_custom
        FROM
            vw_lg_gender
        UNION ALL
        ( SELECT
            'Male' gender,
            1 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            'Female' gender,
            1 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            'Unknown' gender,
            1 is_custom
        FROM
            dual
        )
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_GENDER_STR' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            count(1)
        FROM
            pvs_batch_signal_constants
        WHERE
            key_id = ${keyId} AND
            prr_stratification LIKE '%GENDER%'
    ) = 1
)
UNION ALL
( SELECT
    'receipt_years' param,
    year
FROM
    (
        SELECT
            'All Years' year,
            0 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            year_group year,
            1 is_custom
        FROM
            pvs_ltst_date_config
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_LATEST_YEAR_GRP' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            count(1)
        FROM
            pvs_batch_signal_constants
        WHERE
            key_id = ${keyId} AND
            prr_stratification LIKE '%LATEST_DATE%'
    ) = 1
)
        """
    }

    static stratification_subgroup_enabled = { dataSource ->
        "select count(1) from VW_ADMIN_APP_CONFIG WHERE CONFIG_KEY LIKE '%EBGM_SUBGROUP_%' AND JSON_VALUE(config_value,'\$.PVS_VALUE') = '1' and APPLICATION_NAME = '${dataSource}'"
    }

    static stratification_subgroup_values = { dataSource ->
        """
    (
    SELECT  'age_subgroup' param, age_group param_value
    FROM (
        SELECT  CASE
                    WHEN group_low IS NOT NULL THEN
                            age_group || '>='|| group_low || ' and <' || group_high || ' Years'
                END ||
                CASE
                    WHEN group_low IS NOT NULL AND cpc_age_group IS NOT NULL THEN
                        ' or '
                END ||
                CASE
                    WHEN cpc_age_group IS NOT NULL THEN
                        ' age group IN (' || RTRIM(SUBSTR(cpc_age_group, INSTR(UPPER(cpc_age_group), ' IN ') + 5), ')')|| ')'
                END age_group,
                1   is_custom
        FROM    pvs_age_group_config
        UNION ALL
        SELECT  age_group || '>=' || group_low || ' and <' || group_high || ' Years' age_group,
                0 is_custom
        FROM    vw_lag_age_group 
        )
    WHERE   is_custom = ( SELECT to_number(config_value) AS pvs_value FROM VW_ADMIN_APP_CONFIG WHERE config_key = 'FLAG_CSTM_AGE_GRP' and APPLICATION_NAME = '${dataSource}' )
)
UNION ALL
(
    SELECT  'gender_subgroup' param, gender param_value
    FROM (
        SELECT  gender, 0 is_custom
        FROM    vw_lg_gender
        UNION ALL
        (
        SELECT  label AS gender, 1 is_custom
        FROM    VW_PVS_GENDER_CONFIG_DISP
        )
    )
    WHERE   is_custom = ( SELECT to_number(config_value) AS pvs_value FROM VW_ADMIN_APP_CONFIG WHERE config_key = 'FLAG_CSTM_GENDER' and APPLICATION_NAME = '${dataSource}' )
)
UNION ALL
(
    SELECT 'country_subgroup' param, label param_value
    FROM    VW_PVS_COUNTRY_DISP
)
UNION ALL
(
    SELECT  'region_subgroup' param, label param_value
    FROM (
        SELECT  label, 'PVS_REGIONS_EEA_DESC' is_custom
        FROM    VW_PVS_REGIONS_EEA_DISP
        UNION ALL
        SELECT  label, 'PVS_REGIONS_DESC' is_custom
        FROM    VW_PVS_REGIONS_DISP
        )
    WHERE   is_custom = ( SELECT JSON_VALUE(config_value,'\$.PVS_STR_VIEW') as PVS_STR_VIEW FROM VW_ADMIN_APP_CONFIG WHERE config_key = 'EBGM_SUBGROUP_REGION' and APPLICATION_NAME = '${dataSource}')
)
        """
    }

    static stratification_subgroup_values_faers = { String dataSource ->
        """
(
    SELECT  'age_subgroup' param, age_group param_value
    FROM (
             SELECT  CASE
                         WHEN group_low IS NOT NULL THEN
                                 age_group || '>='|| group_low || ' and <' || group_high || ' Years'
                         END age_group,
                     1   is_custom
             FROM    pvs_age_group_config
             UNION ALL
             SELECT  age_group || '>=' || group_low || ' and <' || group_high || ' Years' age_group,
                     0 is_custom
             FROM    vw_lag_age_group
         )
    WHERE   is_custom = ( SELECT to_number(config_value) AS pvs_value FROM VW_ADMIN_APP_CONFIG WHERE config_key = 'FLAG_CSTM_AGE_GRP' and APPLICATION_NAME = '${dataSource}' )
)
UNION ALL
(
    SELECT  'gender_subgroup' param, gender param_value
    FROM (
             SELECT  gender, 0 is_custom
             FROM    vw_lg_gender
             UNION ALL
             (
                 SELECT  label AS gender, 1 is_custom
                 FROM    VW_PVS_GENDER_CONFIG_DISP
             )
         )
    WHERE   is_custom = ( SELECT to_number(config_value) AS pvs_value FROM VW_ADMIN_APP_CONFIG WHERE config_key = 'FLAG_CSTM_GENDER' and APPLICATION_NAME = '${dataSource}' )
)
UNION ALL
(
    SELECT 'country_subgroup' param, label param_value
    FROM    VW_PVS_COUNTRY_DISP
)
UNION ALL
(
    SELECT  'region_subgroup' param, label param_value
    FROM (
             SELECT  label, 'PVS_REGIONS_EEA_DESC' is_custom
             FROM    VW_PVS_REGIONS_EEA_DISP
             UNION ALL
             SELECT  label, 'PVS_REGIONS_DESC' is_custom
             FROM    VW_PVS_REGIONS_DISP
         )
    WHERE   is_custom = ( SELECT JSON_VALUE(config_value,'\$.PVS_STR_VIEW') as PVS_STR_VIEW FROM VW_ADMIN_APP_CONFIG WHERE config_key = 'EBGM_SUBGROUP_REGION' and APPLICATION_NAME = '${dataSource}')
)
        """
    }

    static case_info_sql = { cl, eventCode, eventCodeVal ->
        """
            SELECT DISTINCT ci.case_num case_number, ci.version_num case_version,
                lmp.product_name product_name, ce.mdr_ae_pt pt,
                cifu.case_labelness_desc listedness, cifu.case_outcome_desc,
                ls.seriousness seriousness,
                dv.rptd_result_assessment_desc determined_causality,
                dv.rptd_result_assessment_id reported_causality,
                cifu.flag_any_source_hcp hcp_flag,
                cifu.significant_counter follow_up                                   
            FROM c_identification ci,
                c_identification_fu cifu,
                c_prod_identification cp,
                c_ae_identification ce,
                vw_pud_seriousness ls,
                c_prod_ae_causality dv,
                vw_product lmp,
                vw_lcau_causality vlc
            WHERE ci.case_id = cp.case_id
            AND ci.tenant_id = cp.tenant_id
            AND ci.version_num = cp.version_num
            AND ci.case_id = cifu.case_id
            AND ci.tenant_id = cifu.tenant_id
            AND ci.version_num = cifu.version_num
            AND ci.case_id = ce.case_id
            AND ci.tenant_id = ce.tenant_id
            AND ci.version_num = ce.version_num
            AND ci.case_id = dv.case_id
            AND ci.tenant_id = dv.tenant_id
            AND ci.version_num = dv.version_num
            AND cp.prod_rec_num = dv.prod_rec_num
            AND ce.ae_rec_num = dv.ae_rec_num
            AND ls.ID(+) = ci.flag_serious
            AND cp.tenant_id = lmp.tenant_id(+)
            AND cp.prod_id_resolved = lmp.product_id(+)
            AND cp.flag_primary_prod = 1
            AND ce.${eventCode}               = '${eventCodeVal}'
            AND (ci.case_num, ci.version_num) IN (${cl})
        """
    }

    static evdas_wwid_case_and_version_sql = { wwid ->
        """
        SELECT
    ci.case_num,
    ci.version_num,
    ci.flag_master_case
FROM
    c_identification_src ci
WHERE
    ci.case_num IS NOT NULL
    AND ci.case_id = coalesce((
        SELECT DISTINCT
            scwwid.case_id AS case_id
        FROM
                 c_case_ww_identifier_src scwwid
            JOIN c_identification_src ci_src ON(ci_src.case_num IS NOT NULL
                                                AND ci_src.tenant_id = scwwid.tenant_id
                                                AND ci_src.case_id = scwwid.case_id
                                                AND ci_src.version_num = scwwid.version_num)
        WHERE
            TRIM(upper(scwwid.wwid)) = '${wwid.toString().toUpperCase().trim()}'
        FETCH FIRST 1 ROWS ONLY
    ),
                              (
                             SELECT DISTINCT
                                 ci1.case_id AS case_id
                             FROM
                                 c_identification_src ci1
                             WHERE
                                     TRIM(upper(ci1.worldwide_case_identifier)) = '${wwid.toString().toUpperCase().trim()}'
                                 AND ci1.case_num IS NOT NULL
                                 AND ci1.case_id IS NOT NULL
                             FETCH FIRST 1 ROWS ONLY
                         ),
                              (
                             SELECT DISTINCT
                                 ci1.case_id
                             FROM
                                      c_references_fu_src ci1
                                 JOIN c_identification_src ci_src ON(ci_src.case_num IS NOT NULL
                                                                     AND ci_src.tenant_id = ci1.tenant_id
                                                                     AND ci_src.case_id = ci1.case_id
                                                                     AND ci_src.version_num = ci1.version_num)
                             WHERE
                                     TRIM(upper(ci1.reference_num)) = '${wwid.toString().toUpperCase().trim()}'
                                 AND ci1.reference_type_desc IN('E2B Company #', 'E2B Authority #', 'E2B Company Number', 'E2B Authority Number'
                                 )
                             FETCH FIRST 1 ROWS ONLY
                         ))
ORDER BY
    version_num DESC
FETCH FIRST 1 ROWS ONLY
        """
    }

    static signal_agg_alerts_sql = { aggAlertIds ->
        "select validated_signal_id from validated_agg_alerts where agg_Alert_id in ($aggAlertIds)"
    }

    static signal_evdas_alerts_sql = { evdasAlertIds ->
        "select validated_signal_id from validated_evdas_alerts where EVDAS_ALERT_ID in ($evdasAlertIds)"
    }

    //This is for the calling with we are calculating only EBGM or Ebgm with PRR\ROR
    static ebgm_calling_sql = { id ->
        "SELECT * from PVS_APP_EB_FULL_CNT_${id} where PT_NAME is not null and PRODUCT_NAME is not null and ROW_COUNT_A <> 0"
    }

    //This will run when only PRR is configured and we don't need EBGM.
    static prr_calling_sql = { id ->
        "SELECT * from PVS_APP_EB_FULL_CNT_${id} where PT_NAME is not null and PRODUCT_NAME is not null"
    }

    static dss_calling_sql = { id ->
        "SELECT * from PVS_APP_DSS_FULL_CNT where PT_NAME is not null and PRODUCT_NAME is not null"
    }

    static select_auto_alert_sql = {
        "select FINISH_DATETIME from V_PVR_ETL_STATUS where UPPER(STATUS)='SUCCESS' "
    }

    static update_auto_alert_sql = { cases ->
        "UPDATE PVS_AUTO_ALERT_INCR_CASES SET DELETED_FLAG = 1 WHERE DELETED_FLAG = 0 and case_num in (" + cases + ")"
    }

    static alert_status_sql = { id ->
        "select CURRENT_STATUS from vw_check_alert_status where execution_id = ${id}"
    }


    static alert_db_progress_status_sql = { id ->
        "select PROGRESS_PCT, CURRENT_STATUS from VW_PVS_ALERT_PROGRESS where execution_id = ${id}"
    }


    static multiple_alert_db_progress_status_sql = { exConfigIds ->
        "select * from VW_PVS_ALERT_PROGRESS where execution_id in (${exConfigIds})"
    }

    static alert_resumption_limit_hours = {
        "select KEY_VALUE from pvs_app_constants where key_id='ALERT_RESUMPTION_LIMIT_HOURS'"
    }

    static select_etl_alert_sql = {
        "select FINISH_DATETIME from vw_pvs_etl_status"
    }

    static execution_status_detail = { id ->
        "select CONFIG_ID, execution_level, type from ex_status where id = ${id}"
    }

    static execution_completed_status_detail = {
        "SELECT ID FROM EX_STATUS WHERE ID IN (SELECT EX_STATUS_ID FROM APP_ALERT_PROGRESS_STATUS) AND EX_STATUS = 'COMPLETED'"
    }
    static check_prr_count_db = { executedConfigurationId ->
        "select * from PVS_PRR_ROR_ML_INFO where execution_id = ${executedConfigurationId}"
    }

    static criteria_sheet_count = { executedConfigurationId ->
        """
        SELECT
    execution_id,
    new_total_count,
    cumm_total_count,
    study_total_new_count,
    study_total_cumm_count
FROM
    (
        SELECT
            execution_id,
            COUNT(DISTINCT
                CASE
                    WHEN cumm_flag = 1 THEN
                        case_id
                END
            ) cumm_total_count,
            COUNT(DISTINCT
                CASE
                    WHEN cumm_flag = 1
                         AND date_range_type_flag = 1 THEN
                        case_id
                END
            ) new_total_count,
            COUNT(DISTINCT
                CASE
                    WHEN study_flag = 1 THEN
                        case_id
                END
            ) study_total_cumm_count,
            COUNT(DISTINCT
                CASE
                    WHEN study_flag = 1
                         AND date_range_type_flag = 1 THEN
                        case_id
                END
            ) study_total_new_count
        FROM
            pvs_case_drill_down
        WHERE
            execution_id IN (${executedConfigurationId.join(",")})
        GROUP BY
            execution_id
    )
"""
    }


    static agg_count_sql = { id, isEventGroup, exConfigIds,selectedDatasource ->
        """
            SELECT
                PRODUCT_NAME AS PRODUCT_NAME,
                PRODUCT_ID AS PRODUCT_ID,
                PT_NAME AS PT,
                PT_CODE AS PT_CODE,
                ${isEventGroup ? '':'SOC_NAME AS SOC,'}
                CUMM_SPONT_COUNT AS CUMM_SPON_COUNT,
                NEW_SPONT_COUNT AS NEW_SPON_COUNT,
                CUMM_STUDY_COUNT AS CUMM_STUDY_COUNT,
                NEW_STUDY_COUNT AS NEW_STUDY_COUNT,
                CUMM_SERIOUS_COUNT AS CUMM_SERIOUS_COUNT,
                NEW_SERIOUS_COUNT AS NEW_SERIOUS_COUNT,
                CUMM_FATAL_COUNT AS CUMM_FATAL_COUNT,
                NEW_FATAL_COUNT  AS NEW_FATAL_COUNT,
                FLAG_RECHAL AS POSITIVE_RECHALLENGE,
                FLAG_DECHAL AS POSITIVE_DECHALLENGE,
                FLAG_PREG AS PREGENENCY,
                FLAG_LABEL AS LISTED,
                FLAG_CONSERVATIVE_RELATEDNESS AS RELATEDNESS,
                NEW_COUNT AS NEW_COUNT,
                CUMM_COUNT AS CUMM_COUNT,
                NEW_PEDIA_COUNT AS NEW_PEDIA_COUNT,
                CUMM_PEDIA_COUNT AS CUMM_PEDIA_COUNT,              
                NEW_INTERACTING_COUNT AS NEW_INTERACTING_COUNT,
                CUMM_INTERACTING_COUNT AS CUMM_INTERACTING_COUNT,
                NEW_GERIA_COUNT AS NEW_GERIA_COUNT,
                CUMM_GERIA_COUNT AS CUMM_GERIA_COUNT,
                NEW_NON_SERIOUS_COUNT AS NEW_NON_SERIOUS_COUNT,
                CUMM_NON_SERIOUS_COUNT AS CUMM_NON_SERIOUS_COUNT,
                PROD_HIERARCHY_ID AS PROD_HIERARCHY_ID,
                EVENT_HIERARCHY_ID AS EVENT_HIERARCHY_ID,
                PROD_N_PERIOD AS PROD_N_PERIOD,
                PROD_N_CUMUL AS PROD_N_CUMUL,CUMM_NON_SERIOUS_COUNT AS CUMM_NON_SERIOUS_COUNT
                ${selectedDatasource == 'pva' ? ", HLT_NAME AS HLT_NAME, HLGT_NAME AS HLGT_NAME ,SMQ_NARROW_NAME AS SMQ_NARROW_NAME,#NEW_DYNAMIC_COUNT":""}
                ${exConfigIds? ", CHILD_EXECUTION_ID AS CHILD_EXECUTION_ID":""}
            FROM PVS_APP_AGG_COUNTS_${id} where PT_NAME IS NOT NULL
            ${exConfigIds? "and CHILD_EXECUTION_ID in (${exConfigIds.join(",")})":''}
        """
    }


    static agg_count_sql_sv = { id, isEventGroup, exConfigIds ,selectedDatasource->
        """
            SELECT
                a.PRODUCT_NAME AS PRODUCT_NAME,
                a.PRODUCT_ID AS PRODUCT_ID,
                a.PT_NAME AS PT,
                a.PT_CODE AS PT_CODE,
                ${isEventGroup ? '':'a.SOC_NAME AS SOC,'}
                a.CUMM_SPONT_COUNT AS CUMM_SPON_COUNT,
                a.NEW_SPONT_COUNT AS NEW_SPON_COUNT,
                a.CUMM_STUDY_COUNT AS CUMM_STUDY_COUNT,
                a.NEW_STUDY_COUNT AS NEW_STUDY_COUNT,
                a.CUMM_SERIOUS_COUNT AS CUMM_SERIOUS_COUNT,
                a.NEW_SERIOUS_COUNT AS NEW_SERIOUS_COUNT,
                a.CUMM_FATAL_COUNT AS CUMM_FATAL_COUNT,
                a.NEW_FATAL_COUNT  AS NEW_FATAL_COUNT,
                a.FLAG_RECHAL AS POSITIVE_RECHALLENGE,
                a.FLAG_DECHAL AS POSITIVE_DECHALLENGE,
                a.FLAG_PREG AS PREGENENCY,
                a.FLAG_LABEL AS LISTED,
                a.FLAG_CONSERVATIVE_RELATEDNESS AS RELATEDNESS,
                a.NEW_COUNT AS NEW_COUNT,
                a.CUMM_COUNT AS CUMM_COUNT,
                a.NEW_PEDIA_COUNT AS NEW_PEDIA_COUNT,
                a.CUMM_PEDIA_COUNT AS CUMM_PEDIA_COUNT,              
                a.NEW_INTERACTING_COUNT AS NEW_INTERACTING_COUNT,
                a.CUMM_INTERACTING_COUNT AS CUMM_INTERACTING_COUNT,
                a.NEW_GERIA_COUNT AS NEW_GERIA_COUNT,
                a.CUMM_GERIA_COUNT AS CUMM_GERIA_COUNT,
                a.NEW_NON_SERIOUS_COUNT AS NEW_NON_SERIOUS_COUNT,
                a.CUMM_NON_SERIOUS_COUNT AS CUMM_NON_SERIOUS_COUNT,
                a.PROD_HIERARCHY_ID AS PROD_HIERARCHY_ID,
                a.EVENT_HIERARCHY_ID AS EVENT_HIERARCHY_ID,
                b.NEW_COUNT as NEW_COUNT_FREQ_CALC,
                b.CUMM_COUNT as CUMM_COUNT_FREQ_CALC,
                a.PROD_N_PERIOD AS PROD_N_PERIOD,
                a.PROD_N_CUMUL AS PROD_N_CUMUL,
                b.CUMM_COUNT as CUMM_COUNT_FREQ_CALC
${selectedDatasource == 'pva' ? ", HLT_NAME AS HLT_NAME, HLGT_NAME AS HLGT_NAME ,SMQ_NARROW_NAME AS SMQ_NARROW_NAME,#NEW_DYNAMIC_COUNT":""}
                ${exConfigIds? ", CHILD_EXECUTION_ID AS CHILD_EXECUTION_ID":""}
            FROM PVS_APP_AGG_COUNTS_${id} a
            LEFT JOIN PVS_APP_AGG_CNT_SV_${id} b 
            ON a.PRODUCT_ID = b.PRODUCT_ID AND a.PT_CODE = b.PT_CODE
            where a.PT_NAME IS NOT NULL
            ${exConfigIds? "and a.CHILD_EXECUTION_ID in (${exConfigIds.join(",")})":''}
        """
    }

    static agg_count_sql_sv_faers = { id, isEventGroup, exConfigIds ,selectedDatasource->
        """
            SELECT
                a.PRODUCT_NAME AS PRODUCT_NAME,
                a.PRODUCT_ID AS PRODUCT_ID,
                a.PT_NAME AS PT,
                a.PT_CODE AS PT_CODE,
                ${isEventGroup ? '':'a.SOC_NAME AS SOC,'}
                a.CUMM_SPONT_COUNT AS CUMM_SPON_COUNT,
                a.NEW_SPONT_COUNT AS NEW_SPON_COUNT,
                a.CUMM_STUDY_COUNT AS CUMM_STUDY_COUNT,
                a.NEW_STUDY_COUNT AS NEW_STUDY_COUNT,
                a.CUMM_SERIOUS_COUNT AS CUMM_SERIOUS_COUNT,
                a.NEW_SERIOUS_COUNT AS NEW_SERIOUS_COUNT,
                a.CUMM_FATAL_COUNT AS CUMM_FATAL_COUNT,
                a.NEW_FATAL_COUNT  AS NEW_FATAL_COUNT,
                a.FLAG_PREG AS PREGENENCY,
                a.FLAG_LABEL AS LISTED,
                a.FLAG_CONSERVATIVE_RELATEDNESS AS RELATEDNESS,
                a.NEW_COUNT AS NEW_COUNT,
                a.CUMM_COUNT AS CUMM_COUNT,
                a.NEW_PEDIA_COUNT AS NEW_PEDIA_COUNT,
                a.CUMM_PEDIA_COUNT AS CUMM_PEDIA_COUNT,              
                a.NEW_INTERACTING_COUNT AS NEW_INTERACTING_COUNT,
                a.CUMM_INTERACTING_COUNT AS CUMM_INTERACTING_COUNT,
                a.NEW_GERIA_COUNT AS NEW_GERIA_COUNT,
                a.CUMM_GERIA_COUNT AS CUMM_GERIA_COUNT,
                a.NEW_NON_SERIOUS_COUNT AS NEW_NON_SERIOUS_COUNT,
                a.CUMM_NON_SERIOUS_COUNT AS CUMM_NON_SERIOUS_COUNT,
                a.PROD_HIERARCHY_ID AS PROD_HIERARCHY_ID,
                a.EVENT_HIERARCHY_ID AS EVENT_HIERARCHY_ID,
                b.NEW_COUNT as NEW_COUNT_FREQ_CALC,
                b.CUMM_COUNT as CUMM_COUNT_FREQ_CALC,
                a.PROD_N_PERIOD AS PROD_N_PERIOD,
                a.PROD_N_CUMUL AS PROD_N_CUMUL,
                b.CUMM_COUNT as CUMM_COUNT_FREQ_CALC
${selectedDatasource == 'pva' ? ", HLT_NAME AS HLT_NAME, HLGT_NAME AS HLGT_NAME ,SMQ_NARROW_NAME AS SMQ_NARROW_NAME,#NEW_DYNAMIC_COUNT":""}
                ${exConfigIds? ", CHILD_EXECUTION_ID AS CHILD_EXECUTION_ID":""}
            FROM PVS_APP_AGG_COUNTS_${id} a
            LEFT JOIN PVS_APP_AGG_CNT_SV_${id} b 
            ON a.PRODUCT_ID = b.PRODUCT_ID AND a.PT_CODE = b.PT_CODE
            where a.PT_NAME IS NOT NULL
            ${exConfigIds? "and a.CHILD_EXECUTION_ID in (${exConfigIds.join(",")})":''}
        """
    }

    static signal_detail_sql = {
        """
           select  JSON_VALUE(products,'\$."3"."name"')   as "Product Name", name as "Signal Name", INITIAL_DATA_SOURCE as "Initial Data Source",   (select b.DISPLAY_NAME  from Disposition b where b.ID =a.DISPOSITION_ID and rownum=1  ) as "Disposition",
(select c.value  from Priority c  where c.id = a.PRIORITY_ID and rownum=1) as "Priority" ,  case  trim(ASSIGNMENT_TYPE) when 'GROUP' then 'Group: '||  (   select e.name from  GROUPS e where e.id = a.ASSIGNED_TO_ID and rownum=1)  else
                                                                                                                   'User: ' ||(   select e.USERNAME from  pvuser e where e.id = a.ASSIGNED_TO_ID and rownum=1) end   as "Assigned To" ,
(select  (select  a2.name from SIGNAL_CATEGORY a2  where a2.id = a1.VALIDATED_SIGNAL_CATEGORY_ID and rownum=1 ) from VALIDATED_SIGNAL_CATEGORY a1 where a1.VALIDATED_SIGNAL_ID = a.id and rownum = 1) as "Signal Type",
to_char(START_DATE,'dd-MON-yyyy') as "Start Date", to_char(END_DATE,'dd-MON-yyyy') as "End Date"
from validated_signal a
        """
    }

    static signal_action_sql = { signalId ->
        """
            select 
            b.name as "Signal Name", 
            (select DISPLAY_NAME from ACTION_CONFIGURATIONS where id = a.config_id) as "Action Name", 
            (select DISPLAY_NAME from  action_types where id = TYPE_ID) "Action Type",
            (select e.USERNAME from  pvuser e where e.id = a.ASSIGNED_TO_ID) as "Assigned To", 
            (select g.NAME from  GROUPS g where g.id = a.ASSIGNED_TO_GROUP_ID) as "Assigned To Group", 
            ACTION_STATUS as "Status", to_char(CREATED_DATE,'dd-MON-yyyy')  as "Creation Date",
            to_char(a.DUE_DATE,'dd-MON-yyyy') as "Due Date", to_char(COMPLETED_DATE,'dd-MON-yyyy') as "Completion Date", 
            DETAILS as "Details", COMMENTS as "Comments",
            a.GUEST_ATTENDEE_EMAIL as "Guest Email"
            from actions a ,validated_signal b, validated_signal_actions c
            where b.id = ${signalId} and a.id = c.action_id and b.id = c.validated_signal_actions_id
        """
    }

    static evdas_query_sql = {
        "{call pkg_create_report_sql_evd.p_main_query(?,?)}"
    }

    static meeting_detail_sql = { signalId ->
        """
            select 
            validated_signal.name "Signal Name" , 
            Meeting_title "Title", 
            to_char(meeting_date,'DD-MON-YYYY HH24:MM:SS') "Meeting Date/Time",
            meeting_agenda "Agenda", 
            meeting_minutes  "Minutes" , 
            meeting.Modified_by  "Last Updated By",
            to_char(meeting.last_updated,'DD-MON-YYYY HH24:MM:SS') "Last Updated"
            from Meeting, validated_signal
            where meeting.validated_signal_id=validated_signal.id(+) 
            and validated_signal.id = ${signalId}
        """
    }

    static family_name_from_case_sql = { caseNum, caseVersion ->
        """
           SELECT DISTINCT family_name
           FROM c_identification ci LEFT JOIN c_prod_identification_fu cpi
                ON (    ci.tenant_id = cpi.tenant_id
                    AND ci.case_id = cpi.case_id
                    AND ci.version_num = cpi.version_num
                   )
                LEFT JOIN vw_family_name vfn
                ON (    cpi.prod_family_id_resolved = vfn.prod_family_id
                    AND cpi.tenant_id = vfn.tenant_id
                   )
				   where ci.case_num = '${caseNum}'
            and ci.version_num = ${caseVersion}
        """
    }

    static soc_pt_sql = { soc, langCode ->
        "SELECT pt_name, pt_code FROM pvr_md_pref_term_dsp pmpt JOIN pvr_md_soc_dsp pmsd on (pmpt.PT_SOC_CODE = pmsd.SOC_CODE) WHERE soc_code =  '${soc}' AND pmsd.lang_id = '${langCode}' and pmpt.lang_id = '${langCode}'"
    }

    static trend_analysis_sql = { startDate, endDate ->
        "{call p_trend_analysis(${startDate},${endDate}, ?, ?, ?, ?, ?, ?)}"
    }

    static add_case_sql = { List<String> cl ->
        String inClause = ''
        String csiSponsorStudyNumber
        String studyClassificationId

        if (Holders.config.custom.qualitative.fields.enabled) {
            csiSponsorStudyNumber = Constants.CustomRptFields.FDA_CSI_SPONSOR_STUDY_NUMBER
            studyClassificationId = Constants.CustomRptFields.FDA_STUDY_CLASSIFICATION_ID
        } else {
            csiSponsorStudyNumber = Constants.CustomRptFields.CSI_SPONSOR_STUDY_NUMBER
            studyClassificationId = Constants.CustomRptFields.STUDY_CLASSIFICATION_ID
        }

        if (cl && cl.size() <= 1000) {
            inClause = "(${cl.join(",")})"
        } else if (cl && cl.size() > 1000) {
            inClause = cl.collate(1000).join(" OR ci.CASE_NUM IN ").replace("[", "(").replace("]", ")")
        }

        """
WITH max_version AS (
                  SELECT
                      MAX(version_num) version_num,
                      case_id,
                      tenant_id
                  FROM
                      v_c_identification
                      where 
                      ( case_num IN ${ inClause })
                  GROUP BY
                      case_id,
                      tenant_id
              ), data AS (
                          SELECT DISTINCT
                              ci.case_id                     case_id,
                              ci.case_num                    case_num,
                              ci.version_num                 version_num,
                              ci.tenant_id                   tenant_id,
                  cpifu.prod_rec_num              prod_rec_num,
                  caei.ae_rec_num                gt_ae_rec_num,
                                VW_LH_HCP.HCP				   CsHcpFlag,
                              ci.source_type_desc            source_type_desc,
                              ci.date_first_receipt          date_first_receipt,
                  ci.txt_date_first_receipt      txt_date_first_receipt,
                              cifu.date_receipt              date_receipt,
                              cifu.case_outcome_desc         case_outcome_desc,
                              cpifu.prod_family_name         prod_family_name,
                              cifu.significant_counter       significant_counter,
                              decode(cifu.flag_ver_significant, 1, 'Yes', 'No') flag_ver_significant,
                              cpi.product_name               product_name,
                  caei.mdr_ae_pt                 prim_ae_pt,
                              cifu.prim_prod_name            prim_prod_name,
                              caei.ae_outcome_desc           ae_outcome_desc,
                              decode(ci.flag_serious, 1, 'Serious', 'Non Serious') flag_serious,
                              ci.occured_country_desc        occured_country_desc,
                              cpc.patient_age_group_desc     patient_age_group_desc,
                              cpc.patient_sex_desc           patient_sex_desc,
                              decode(cdifu.rechallenge_id, 3, 'N/A', 0, 'No',
                                     1, 'Yes', 2, 'Unk') rechallenge_id,
                              cifu.date_locked               date_locked,
                  cifu.txt_date_locked           txt_date_locked,
                              decode(ci.flag_serious_death, 1, 'Yes', 0, 'No',
                                     'Unk') flag_serious_death,
                                decode(ci.flag_combination_product, 1, 'Yes', NULL, '-',
                                       'No') flag_combination_product,
                              caei.mdr_ae_pt                 mdr_ae_pt,
                              cpi.prod_id_resolved           prod_id_resolved,
                              caei.ae_rec_num                ae_rec_num,
                                cifu.CASE_LABELNESS_DESC       listedness_text,
                                1 AS flag_master_case,
                              patient_age_onset_years        AS age_in_years,
                  csti.project_num            project_num,
                  csti.study_number           study_number,
                  cstdidfc.study_number        PreAnda_study_number,
                  cpat.tto_days              tto_days,
                  cls1_57.state_yn            as flag_susar,
                  F_GET_SUR_SER_UNL_REL('SAFETY',ci.tenant_id,ci.case_id,ci.version_num,cpifu.prod_rec_num,caei.AE_REC_NUM)                  sur
                          FROM
                              c_identification            ci
                              JOIN max_version              gqcl ON (ci.tenant_id = gqcl.tenant_id
                                                                      AND ci.version_num = gqcl.version_num
                                                                      AND ci.case_id = gqcl.case_id )
                              LEFT JOIN c_identification_fu         cifu ON ( ci.tenant_id = cifu.tenant_id
                                                                      AND ci.version_num = cifu.version_num
                                                                      AND ci.case_id = cifu.case_id )
                              LEFT JOIN c_patient_characteristics   cpc ON ( ci.tenant_id = cpc.tenant_id
                                                                           AND ci.version_num = cpc.version_num
                                                                           AND ci.case_id = cpc.case_id )
                              LEFT JOIN c_ae_identification         caei ON ( ci.tenant_id = caei.tenant_id
                                                                      AND ci.version_num = caei.version_num
                                                                      AND ci.case_id = caei.case_id )
                              LEFT JOIN c_prod_identification_fu    cpifu ON ( ci.tenant_id = cpifu.tenant_id
                                                                            AND ci.version_num = cpifu.version_num
                                                                            AND ci.case_id = cpifu.case_id )
                              LEFT JOIN c_prod_identification       cpi ON ( cpifu.tenant_id = cpi.tenant_id
                                                                       AND cpifu.version_num = cpi.version_num
                                                                       AND cpifu.case_id = cpi.case_id
                                                                       AND cpifu.prod_rec_num = cpi.prod_rec_num )
                              LEFT JOIN c_ae_identification_fu      caefu ON ( caefu.tenant_id = caei.tenant_id
                                                                          AND caefu.version_num = caei.version_num
                                                                          AND caefu.case_id = caei.case_id
                                                                          AND caefu.ae_rec_num = caei.ae_rec_num )
                              LEFT JOIN vw_product                  vpr ON ( cpi.prod_id_resolved = vpr.product_id )
                              LEFT JOIN c_drug_identification_fu    cdifu ON ( cpifu.tenant_id = cdifu.tenant_id
                                                                            AND cpifu.version_num = cdifu.version_num
                                                                            AND cpifu.case_id = cdifu.case_id
                                                                            AND cpifu.prod_rec_num = cdifu.prod_rec_num )
                              LEFT JOIN c_prod_devices_addl         cpda ON ( cpi.tenant_id = cpda.tenant_id
                                                                      AND cpi.case_id = cpda.case_id
                                                                      AND cpi.version_num = cpda.version_num
                                                                      AND cpi.prod_rec_num = cpda.prod_rec_num )
                  LEFT OUTER JOIN c_study_identification         csti ON ( csti.tenant_id = ci.tenant_id
                                                    AND csti.version_num = ci.version_num
                                                    AND csti.case_id = ci.case_id )
                  LEFT OUTER JOIN VW_PRE_ANDA_STUDY_NUMBER         cstdidfc ON ( cstdidfc.tenant_id = ci.tenant_id
                                                    AND cstdidfc.version_num = ci.version_num
                                                    AND cstdidfc.case_id = ci.case_id )
                  LEFT OUTER JOIN cdr_prod_ae_tto                cpat ON ( cpat.tenant_id = caei.tenant_id
                                               AND cpat.version_num = caei.version_num
                                               AND cpat.ae_rec_num = caei.ae_rec_num
                                               AND cpat.case_id = caei.case_id
                                               AND cpat.tenant_id = cpifu.tenant_id
                                               AND cpat.version_num = cpifu.version_num
                                               AND cpat.case_id = cpifu.case_id
                                               AND cpat.prod_rec_num = cpifu.prod_rec_num )
                  LEFT OUTER JOIN vw_clp_state_yn                cls1_57 ON ( cifu.flag_susar = cls1_57.id )
								LEFT OUTER JOIN VW_CASE_HCP cshcpflg ON ( cshcpflg.tenant_id = cifu.tenant_id
                                                                        AND cshcpflg.version_num = cifu.version_num
                                                                        AND cshcpflg.case_id = cifu.case_id )
								LEFT OUTER JOIN VW_LH_HCP ON ( VW_LH_HCP.HCP_ID = cshcpflg.FLAG_HCP )

                          WHERE
                              cpifu.flag_primary_prod = 1
                              AND caefu.flag_primary_ae = 1
                      ), clob_data AS (
                          SELECT
                              cdc.case_id         case_id,
                              cdc.version_num     version_num,
                              cdc.tenant_id       tenant_id,
                              cs.case_narrative   case_narrative,
                              replace(replace(replace(cdc.ae_pt, '!@##@!', CHR(13)
                                                                           || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') ae_pt,
                              replace(replace(replace(csda.company_susp_prod_all, '!@##@!', CHR(13)
                                                                                            || CHR(10)), '!@_@!', '-'), '!@.@!'
                                                                                            , ') ') company_susp_prod_all,
                              replace(replace(replace(csda.concomit_prod_all, '!@##@!', CHR(13)
                                                                                        || CHR(10)), '!@_@!', '-'), '!@.@!', ') '
                                                                                        ) concomit_prod_all,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('SAFETY','CHARACTERISTIC_ALL_CS',cdc.tenant_id,cdc.case_id,cdc.version_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as characteristic_all_cs,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('SAFETY','PAT_MED_COND',cdc.tenant_id,cdc.case_id,cdc.version_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as pat_med_cond_all,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('SAFETY','PAT_DRUG_HIST',cdc.tenant_id,cdc.case_id,cdc.version_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as coded_drug_name_all,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('SAFETY','CAUSE_OF_DEATH',cdc.tenant_id,cdc.case_id,cdc.version_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as cd_codd_reptd_all
                          FROM
                              cdr_clob            cdc
                              LEFT JOIN cdr_clob_prod_all   csda ON ( csda.case_id = cdc.case_id
                                                                    AND csda.version_num = cdc.version_num
                                                                    AND csda.tenant_id = cdc.tenant_id )
                              LEFT JOIN c_summary           cs ON ( cs.case_id = cdc.case_id
                                                          AND cs.version_num = cdc.version_num
                                                          AND cs.tenant_id = cdc.tenant_id )

                 where ( cdc.tenant_id,
                     cdc.case_id,
                     cdc.version_num ) IN (
                                  SELECT
                                      tenant_id,
                                      case_id,
                                      version_num
                                  FROM
                                      max_version
                              )
                      )
            ,clob_data_prod as(
            select gt.tenant_id  , gt.version_num  , gt.CASE_ID, gt.prod_rec_num,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('SAFETY','INDICATION',gt.tenant_id  , gt.CASE_ID, gt.version_num  , gt.prod_rec_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as ind_codd_reptd_all,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('SAFETY','LOT_NO_ALL',gt.tenant_id  ,gt.CASE_ID, gt.version_num  , gt.prod_rec_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as lot_no_all,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('SAFETY','THERAPY_DATES_ALL',gt.tenant_id  ,gt.CASE_ID, gt.version_num  , gt.prod_rec_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as therapy_dates_all,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('SAFETY','DOSE_DETAIL_ALL',gt.tenant_id  ,gt.CASE_ID, gt.version_num  , gt.prod_rec_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as dose_detail_all,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('SAFETY','ALL_PTS_SUR',gt.tenant_id  ,gt.CASE_ID, gt.version_num  , gt.prod_rec_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as all_pts_sur
            from (select distinct gt.tenant_id  , gt.version_num  , gt.CASE_ID, cpifu.prod_rec_num from max_version gt 
                  inner join c_prod_identification_fu    cpifu on gt.tenant_id = cpifu.tenant_id
                                AND gt.version_num = cpifu.version_num
                                AND gt.case_id = cpifu.case_id 
                  where cpifu.flag_primary_prod = 1
            
            ) gt                         
            )
                      SELECT
                          d.case_id                    "masterCaseId",
                          d.case_num                   "masterCaseNum",
                          d.version_num                "masterVersionNum",
                            d.CsHcpFlag                "CsHcpFlag",
                          d.source_type_desc           "masterRptTypeId",
                          d.date_first_receipt         "masterInitReptDate",
               d.txt_date_first_receipt     "ciTxtDateReceiptInitial",
                          d.date_receipt               "masterFollowupDate",
                          d.listedness_text            "assessListedness",
                          cd.company_susp_prod_all     "masterSuspProdList",
                          cd.concomit_prod_all         "masterConcomitProdList",
                          d.case_outcome_desc          "assessOutcome",
                          d.prod_family_name           "productFamilyId",
                          d.significant_counter        "masterFupNum",
                          d.flag_ver_significant       "masterFlagSt",
                          d.product_name               "productProductId",
                          d.prim_ae_pt                      "masterPrimEvtPrefTerm",
                          d.prim_prod_name             "masterPrimProdName",
                          d.ae_outcome_desc            "eventEvtOutcomeId",
                          d.flag_serious               "assessSeriousness",
                          d.occured_country_desc       "masterCountryId",
                          d.patient_age_group_desc     "patInfoAgeGroupId",
                          d.patient_sex_desc           "patInfoGenderId",
                          d.rechallenge_id             "prodDrugsPosRechallenge",
                          d.date_locked                "masterDateLocked",
               d.txt_date_locked         "cifTxtDateLocked",
                          d.flag_serious_death         "masterFatalFlag",
                            d.flag_combination_product   "deviceComboProduct",
                          d.mdr_ae_pt                  "eventPrefTerm",
                          d.prod_id_resolved           "cpiProdIdResolved",
                          cd.case_narrative            "narrativeNarrative",
                          cd.ae_pt                     "ccAePt",
                          NULL AS "cmadlflagEligibleLocalExpdtd",
                          NULL AS "caseMasterPvrUdText12",
                          NULL AS "${csiSponsorStudyNumber}",
                          NULL AS "${studyClassificationId}",
                          NULL AS "vwcpai1FlagCompounded",
                          NULL AS "csisender_organization",
                          NULL AS "cdrClobAeAllUdUdClob1",
                          age_in_years                 AS "casePatInfoPvrUdNumber2",
                          NULL AS "casProdDrugsPvrUdText20",
                          NULL AS "caseProdDrugsPvrUdNumber10",
                          NULL AS "caseProdDrugsPvrUdNumber11",
                          d.flag_master_case           "flagMasterCase",
               d.project_num            "vwstudyProtocolNum",
               d.PreAnda_study_number     "PreAndastudyStudyNum",
               d.tto_days              "dvProdEventTimeOnsetDays",
               d.flag_susar             "masterSusar",
               d.sur                  "ceSerUnlRel",
               cdp.ind_codd_reptd_all     "productIndCoddorReptd",
               cdp.lot_no_all              "productLotNoAllcs",
               cdp.all_pts_sur                 "masterPrefTermSurAll",
               cdp.therapy_dates_all         "productStartStopDateAllcs",
               cdp.dose_detail_all              "productDoseDetailAllcs",
               cd.characteristic_all_cs       "masterCharactersticAllcs",
               cd.pat_med_cond_all              "cprmConditionAll",
               cd.coded_drug_name_all        "ccMedHistDrugAll",
               cd.cd_codd_reptd_all          "ccCoddRptdCauseDeathAll"
                      FROM
                          data        d,
                          clob_data   cd,
               clob_data_prod cdp
                      WHERE
                          d.case_id = cd.case_id
                          AND d.version_num = cd.version_num
                          AND d.tenant_id = cd.tenant_id
                          AND d.case_id = cdp.case_id
                          AND d.version_num = cdp.version_num
                          AND d.tenant_id = cdp.tenant_id
                          AND d.prod_rec_num = cdp.prod_rec_num
  """

    }

    static add_case_sql_custom_col = { List<String> cl ->
        String inClause = ''
        String csiSponsorStudyNumber
        String studyClassificationId

        if (Holders.config.custom.qualitative.fields.enabled) {
            csiSponsorStudyNumber = Constants.CustomRptFields.FDA_CSI_SPONSOR_STUDY_NUMBER
            studyClassificationId = Constants.CustomRptFields.FDA_STUDY_CLASSIFICATION_ID
        } else {
            csiSponsorStudyNumber = Constants.CustomRptFields.CSI_SPONSOR_STUDY_NUMBER
            studyClassificationId = Constants.CustomRptFields.STUDY_CLASSIFICATION_ID
        }

        if (cl && cl.size() <= 1000) {
            inClause = "(${cl.join(",")})"
        } else if (cl && cl.size() > 1000) {
            inClause = cl.collate(1000).join(" OR ci.CASE_NUM IN ").replace("[", "(").replace("]", ")")
        }

        """

              WITH max_version AS (
                  SELECT
                      MAX(version_num) version_num,
                      case_id,
                      tenant_id
                  FROM
                      v_c_identification
                      where 
                      ( case_num IN ${inClause})
                  GROUP BY
                      case_id,
                      tenant_id
              ), data AS (
                          SELECT DISTINCT
                              ci.case_id                     case_id,
                              ci.case_num                    case_num,
                              ci.version_num                 version_num,
                              ci.tenant_id                   tenant_id,
                  cpifu.prod_rec_num              prod_rec_num,
                  caei.ae_rec_num                gt_ae_rec_num,
                                decode(ci.flag_medically_confirm, 1, 'Yes', 2, 'No',
                                        3, 'Unknown') flag_primary_source_hcp,
                               ci.source_type_desc            source_type_desc,
                              ci.date_first_receipt          date_first_receipt,
                  ci.txt_date_first_receipt      txt_date_first_receipt,
                              cifu.date_receipt              date_receipt,
                              cifu.case_outcome_desc         case_outcome_desc,
                              cpifu.prod_family_name         prod_family_name,
                              cifu.significant_counter       significant_counter,
                              decode(cifu.flag_ver_significant, 1, 'Yes', 'No') flag_ver_significant,
                              cpi.product_name               product_name,
                  caei.mdr_ae_pt                 prim_ae_pt,
                              cifu.prim_prod_name            prim_prod_name,
                              caei.ae_outcome_desc           ae_outcome_desc,
                              decode(ci.flag_serious, 1, 'Serious', 'Non Serious') flag_serious,
                              ci.occured_country_desc        occured_country_desc,
                              cpc.patient_age_group_desc     patient_age_group_desc,
                              cpc.patient_sex_desc           patient_sex_desc,
                              decode(cdifu.rechallenge_id, 3, 'N/A', 0, 'No',
                                     1, 'Yes', 2, 'Unk') rechallenge_id,
                              cifu.date_locked               date_locked,
                  cifu.txt_date_locked           txt_date_locked,
                              decode(ci.flag_serious_death, 1, 'Yes', 0, 'No',
                                     'Unk') flag_serious_death,
                                decode(ci.flag_combination_product, 1, 'Yes',
                                       NULL, '-', 'No') flag_combination_product,
                              caei.mdr_ae_pt                 mdr_ae_pt,
                              cpi.prod_id_resolved           prod_id_resolved,
                              caei.ae_rec_num                ae_rec_num,
                                cifu.CASE_LABELNESS_DESC       listedness_text,
                   vfct.case_type                                                case_type,
                  cma.ud_text_12                                                completeness_score,
                  CASE
                                     WHEN upper(cra.STUDY_TYPE_DESC) LIKE '%CLINICAL%TRIALS%'
                                          OR upper(cra.STUDY_TYPE_DESC) LIKE '%INDIVIDUAL%PATIENT%USE%'
                                          OR upper(cra.STUDY_TYPE_DESC) LIKE '%OTHER%STUDIES%'
                                          OR upper(cra.STUDY_TYPE_DESC) LIKE '%REPORT%FROM%AGGREGATE%ANALYSIS%' THEN
                                         cra.SPONSOR_STUDY_NUM
                                     ELSE
                                         NULL
                                 END AS primary_ind,
                                 CASE
                                     WHEN cra.STUDY_TYPE_DESC IS NOT NULL THEN
                                         cra.STUDY_TYPE_DESC
                                     ELSE
                                         NULL
                                 END AS study_type,
                                 decode(csi.sender_outsourced, 1, 'Yes', 2, 'No',
						  
							 
						  
                                        3, 'Unknown') AS comp_flag,
                                 csi.SENDER_ORGANIZATION_DESC      AS sender_organization,
                                 cpia.ud_number_2             AS age,
                                 cpda.ud_text_20              nda,
                                 cpda.ud_number_10            bla,
                                 cpda.ud_number_11            anda,
                                 1 AS flag_master_case,
                                patient_age_onset_years        AS age_in_years,
								csti.project_num 				project_num,
								csti.study_number				study_number,
								cstdidfc.study_number			PreAnda_study_number,
								cpat.tto_days					tto_days,
								cls1_57.state_yn				as flag_susar,
								F_GET_SUR_SER_UNL_REL('FDA',ci.tenant_id,ci.case_id,ci.version_num,cpifu.prod_rec_num,caei.AE_REC_NUM)                  sur
                          FROM
                              c_identification            ci
                              JOIN max_version              gqcl ON (ci.tenant_id = gqcl.tenant_id
                                                                      AND ci.version_num = gqcl.version_num
                                                                      AND ci.case_id = gqcl.case_id )
                              LEFT JOIN c_identification_fu         cifu ON ( ci.tenant_id = cifu.tenant_id
                                                                      AND ci.version_num = cifu.version_num
                                                                      AND ci.case_id = cifu.case_id )
                              LEFT JOIN c_patient_characteristics   cpc ON ( ci.tenant_id = cpc.tenant_id
                                                                           AND ci.version_num = cpc.version_num
                                                                           AND ci.case_id = cpc.case_id )
                              LEFT JOIN c_ae_identification         caei ON ( ci.tenant_id = caei.tenant_id
                                                                      AND ci.version_num = caei.version_num
                                                                      AND ci.case_id = caei.case_id )
                              LEFT JOIN cdr_conser_evt_label        ccel ON ( caei.tenant_id = ccel.tenant_id
                                                                       AND caei.version_num = ccel.version_num
                                                                       AND caei.case_id = ccel.case_id
                                                                       AND caei.ae_rec_num = ccel.ae_rec_num )
                              LEFT JOIN c_prod_identification_fu    cpifu ON ( ci.tenant_id = cpifu.tenant_id
                                                                            AND ci.version_num = cpifu.version_num
                                                                            AND ci.case_id = cpifu.case_id )
                              LEFT JOIN c_prod_identification       cpi ON ( cpifu.tenant_id = cpi.tenant_id
                                                                       AND cpifu.version_num = cpi.version_num
                                                                       AND cpifu.case_id = cpi.case_id
                                                                       AND cpifu.prod_rec_num = cpi.prod_rec_num )
                              LEFT JOIN c_ae_identification_fu      caefu ON ( caefu.tenant_id = caei.tenant_id
                                                                          AND caefu.version_num = caei.version_num
                                                                          AND caefu.case_id = caei.case_id
                                                                          AND caefu.ae_rec_num = caei.ae_rec_num )
                  LEFT JOIN c_master_addl              cma ON ( ci.tenant_id = cma.tenant_id
                                           AND ci.version_num = cma.version_num
                                           AND ci.case_id = cma.case_id )
                                 LEFT JOIN C_STUDY_IDENTIFICATION    cra ON ( ci.tenant_id = cra.tenant_id
                                             AND ci.version_num = cra.version_num
                                             AND ci.case_id = cra.case_id )
                  LEFT JOIN c_sender_info              csi ON ( ci.tenant_id = csi.tenant_id
                                           AND ci.case_id = csi.case_id
                                           AND ci.version_num = csi.version_num )
                  LEFT JOIN cdr_clob_ae_all_ud         csmq ON ( ci.tenant_id = csmq.tenant_id
                                                AND ci.case_id = csmq.case_id
                                                AND ci.version_num = csmq.version_num )
                  LEFT JOIN c_pat_info_addl            cpia ON ( ci.tenant_id = cpia.tenant_id
                                             AND ci.case_id = cpia.case_id
                                             AND ci.version_num = cpia.version_num )
                  LEFT JOIN c_prod_drugs_addl          cpda ON ( ci.tenant_id = cpda.tenant_id
                                               AND ci.case_id = cpda.case_id
                                               AND ci.version_num = cpda.version_num
                                               AND cpi.prod_rec_num = cpda.prod_rec_num )
                                  LEFT JOIN c_prod_devices_addl           cpdal ON ( ci.tenant_id = cpda.tenant_id
                                                                       AND ci.case_id = cpda.case_id
                                                                       AND ci.version_num = cpda.version_num
                                                                       AND cpi.prod_rec_num = cpda.prod_rec_num )
                              LEFT JOIN vw_product                  vpr ON ( cpi.prod_id_resolved = vpr.product_id )
                              LEFT JOIN c_drug_identification_fu    cdifu ON ( cpifu.tenant_id = cdifu.tenant_id
                                                                            AND cpifu.version_num = cdifu.version_num
                                                                            AND cpifu.case_id = cdifu.case_id
                                                                            AND cpifu.prod_rec_num = cdifu.prod_rec_num )
                  LEFT OUTER JOIN c_study_identification         csti ON ( csti.tenant_id = ci.tenant_id
                                                    AND csti.version_num = ci.version_num
                                                    AND csti.case_id = ci.case_id )
                  LEFT OUTER JOIN VW_PRE_ANDA_STUDY_NUMBER         cstdidfc ON ( cstdidfc.tenant_id = ci.tenant_id
                                                    AND cstdidfc.version_num = ci.version_num
                                                    AND cstdidfc.case_id = ci.case_id )
                  LEFT OUTER JOIN cdr_prod_ae_tto                cpat ON ( cpat.tenant_id = caei.tenant_id
                                               AND cpat.version_num = caei.version_num
                                               AND cpat.ae_rec_num = caei.ae_rec_num
                                               AND cpat.case_id = caei.case_id
                                               AND cpat.tenant_id = cpifu.tenant_id
                                               AND cpat.version_num = cpifu.version_num
                                               AND cpat.case_id = cpifu.case_id
                                               AND cpat.prod_rec_num = cpifu.prod_rec_num )
                  LEFT OUTER JOIN vw_clp_state_yn                cls1_57 ON ( cifu.flag_susar = cls1_57.id )
                  LEFT JOIN vw_fda_case_type           vfct ON ( ci.flag_eligible_local_expdtd = vfct.id )
                          WHERE
                              cpifu.flag_primary_prod = 1
                              AND caefu.flag_primary_ae = 1
                      ), clob_data AS (
                          SELECT
                              cdc.case_id         case_id,
                              cdc.version_num     version_num,
                              cdc.tenant_id       tenant_id,
                              cs.case_narrative   case_narrative,
                              replace(replace(replace(cdc.ae_pt, '!@##@!', CHR(13)
                                                                           || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') ae_pt,
                              replace(replace(replace(csda.company_susp_prod_all, '!@##@!', CHR(13)
                                                                                            || CHR(10)), '!@_@!', '-'), '!@.@!'
                                                                                            , ') ') company_susp_prod_all,
                              replace(replace(replace(csda.concomit_prod_all, '!@##@!', CHR(13)
                                                                                        || CHR(10)), '!@_@!', '-'), '!@.@!', ') '
                                                                                        ) concomit_prod_all,
                  CASE
                     WHEN csmq.ud_clob_1 IS NOT NULL THEN
                        replace(replace(csmq.ud_clob_1, '!@.@!', ''), '!@##@!',
                              ',')
                     ELSE
                        NULL
                  END                  AS smq_med,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('FDA','CHARACTERISTIC_ALL_CS',cdc.tenant_id,cdc.case_id,cdc.version_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as characteristic_all_cs,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('FDA','PAT_MED_COND',cdc.tenant_id,cdc.case_id,cdc.version_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as pat_med_cond_all,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('FDA','PAT_DRUG_HIST',cdc.tenant_id,cdc.case_id,cdc.version_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as coded_drug_name_all,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('FDA','CAUSE_OF_DEATH',cdc.tenant_id,cdc.case_id,cdc.version_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as cd_codd_reptd_all
                          FROM
                              cdr_clob            cdc
                              LEFT JOIN cdr_clob_prod_all   csda ON ( csda.case_id = cdc.case_id
                                                                    AND csda.version_num = cdc.version_num
                                                                    AND csda.tenant_id = cdc.tenant_id )
                              LEFT JOIN c_summary           cs ON ( cs.case_id = cdc.case_id
                                                          AND cs.version_num = cdc.version_num
                                                          AND cs.tenant_id = cdc.tenant_id )
                  LEFT JOIN cdr_clob_ae_all_ud  csmq ON ( cdc.tenant_id = csmq.tenant_id
                                                AND cdc.case_id = csmq.case_id
                                                   AND cdc.version_num = csmq.version_num )
                 where ( cdc.tenant_id,
                     cdc.case_id,
                     cdc.version_num ) IN (
                                  SELECT
                                      tenant_id,
                                      case_id,
                                      version_num
                                  FROM
                                      max_version
                              )
                      )
            ,clob_data_prod as(
            select gt.tenant_id  , gt.version_num  , gt.CASE_ID, gt.prod_rec_num,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('FDA','INDICATION',gt.tenant_id  , gt.CASE_ID, gt.version_num  , gt.prod_rec_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as ind_codd_reptd_all,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('FDA','LOT_NO_ALL',gt.tenant_id  ,gt.CASE_ID, gt.version_num  , gt.prod_rec_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as lot_no_all,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('FDA','THERAPY_DATES_ALL',gt.tenant_id  ,gt.CASE_ID, gt.version_num  , gt.prod_rec_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as therapy_dates_all,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('FDA','DOSE_DETAIL_ALL',gt.tenant_id  ,gt.CASE_ID, gt.version_num  , gt.prod_rec_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as dose_detail_all,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('FDA','ALL_PTS_SUR',gt.tenant_id  ,gt.CASE_ID, gt.version_num  , gt.prod_rec_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as all_pts_sur
            from (select distinct gt.tenant_id  , gt.version_num  , gt.CASE_ID, cpifu.prod_rec_num from max_version gt 
                  inner join c_prod_identification_fu    cpifu on gt.tenant_id = cpifu.tenant_id
                                AND gt.version_num = cpifu.version_num
                                AND gt.case_id = cpifu.case_id 
                  where cpifu.flag_primary_prod = 1
            
            ) gt                         
            )
                      SELECT
                          d.case_id                    "masterCaseId",
                          d.case_num                   "masterCaseNum",
                          d.version_num                "masterVersionNum",
                          d.flag_primary_source_hcp    "CsHcpFlag",
                          d.source_type_desc           "masterRptTypeId",
                          d.date_first_receipt         "masterInitReptDate",
               d.txt_date_first_receipt     "ciTxtDateReceiptInitial",
                          d.date_receipt               "masterFollowupDate",
                          d.listedness_text            "assessListedness",
                          cd.company_susp_prod_all     "masterSuspProdList",
                          cd.concomit_prod_all         "masterConcomitProdList",
                          d.case_outcome_desc          "assessOutcome",
                          d.prod_family_name           "productFamilyId",
                          d.significant_counter        "masterFupNum",
                          d.flag_ver_significant       "masterFlagSt",
                          d.product_name               "productProductId",
                          d.prim_ae_pt                      "masterPrimEvtPrefTerm",
                          d.prim_prod_name             "masterPrimProdName",
                          d.ae_outcome_desc            "eventEvtOutcomeId",
                          d.flag_serious               "assessSeriousness",
                          d.occured_country_desc       "masterCountryId",
                          d.patient_age_group_desc     "patInfoAgeGroupId",
                          d.patient_sex_desc           "patInfoGenderId",
                          d.rechallenge_id             "prodDrugsPosRechallenge",
                          d.date_locked                "masterDateLocked",
               d.txt_date_locked         "cifTxtDateLocked",
                          d.flag_serious_death         "masterFatalFlag",
                          d.mdr_ae_pt                  "eventPrefTerm",
                          d.prod_id_resolved           "cpiProdIdResolved",
                          cd.case_narrative            "narrativeNarrative",
                          cd.ae_pt                     "ccAePt",
                d.case_type "cmadlflagEligibleLocalExpdtd",
                d.completeness_score "caseMasterPvrUdText12",
                d.primary_ind "${csiSponsorStudyNumber}",
                d.study_type "${studyClassificationId}",
                d.comp_flag "vwcpai1FlagCompounded",
                d.sender_organization "csisender_organization",
                cd.smq_med "cdrClobAeAllUdUdClob1",
                             d.flag_combination_product  "deviceComboProduct",
                d.age "casePatInfoPvrUdNumber2",
                d.nda "casProdDrugsPvrUdText20",
                d.bla "caseProdDrugsPvrUdNumber10",
                d.anda "caseProdDrugsPvrUdNumber11",
                          d.flag_master_case           "flagMasterCase",
               d.project_num            "vwstudyProtocolNum",
               d.PreAnda_study_number     "PreAndastudyStudyNum",
               d.tto_days              "dvProdEventTimeOnsetDays",
               d.flag_susar             "masterSusar",
               d.sur                  "ceSerUnlRel",
               cdp.ind_codd_reptd_all     "productIndCoddorReptd",
               cdp.lot_no_all              "productLotNoAllcs",
               cdp.all_pts_sur                 "masterPrefTermSurAll",
               cdp.therapy_dates_all         "productStartStopDateAllcs",
               cdp.dose_detail_all              "productDoseDetailAllcs",
               cd.characteristic_all_cs       "masterCharactersticAllcs",
               cd.pat_med_cond_all              "cprmConditionAll",
               cd.coded_drug_name_all        "ccMedHistDrugAll",
               cd.cd_codd_reptd_all          "ccCoddRptdCauseDeathAll"
                      FROM
                          data        d,
                          clob_data   cd,
               clob_data_prod cdp
                      WHERE
                          d.case_id = cd.case_id
                          AND d.version_num = cd.version_num
                          AND d.tenant_id = cd.tenant_id
                          AND d.case_id = cdp.case_id
                          AND d.version_num = cdp.version_num
                          AND d.tenant_id = cdp.tenant_id
                          AND d.prod_rec_num = cdp.prod_rec_num;
  """
    }

    static product_summary_sql = { Integer productId, periodStartDate, periodEndDate, start, length, disposition, dataSrc, isOutputFormat, orderByCriteria, searchCriteria ->
        def dispositionCriteria = ""
        if (disposition) {
            dispositionCriteria = "AND aa.disposition_id IN (${disposition})"
        }
        def serverSideCriteria = ""
        if (!isOutputFormat) {
            serverSideCriteria = "OFFSET ${start} ROWS FETCH NEXT ${length} ROWS ONLY"
        }


        """
        SELECT * FROM
          (SELECT ID, validated_signal_id, ROWNUM rn,count(*) over () as filtered_count ,total_count, PT, DISPLAY_NAME, selected_data_source, NEW_SPON_COUNT,CUM_SPON_COUNT,EB95, EB05, COMMENTS,NAME,requested_by,product_id,pt_code,EXEC_CONFIGURATION_ID,ASSIGNED_TO_ID,generic_comment,ALERT_CONFIGURATION_ID
          FROM
             (SELECT ID, validated_signal_id, ROWNUM rn,count(*) over () as total_count , PT, DISPLAY_NAME, selected_data_source, NEW_SPON_COUNT,CUM_SPON_COUNT,EB95, EB05, COMMENTS,NAME,requested_by,product_id,pt_code,EXEC_CONFIGURATION_ID,ASSIGNED_TO_ID,generic_comment,ALERT_CONFIGURATION_ID
                FROM (SELECT DISTINCT ID, validated_signal_id, PT, DISPLAY_NAME, selected_data_source, NEW_SPON_COUNT,CUM_SPON_COUNT,EB95,EB05,COMMENTS,NAME,requested_by,product_id,pt_code,NULL AS EXEC_CONFIGURATION_ID,NULL AS ASSIGNED_TO_ID,generic_comment, NULL AS ALERT_CONFIGURATION_ID
                      FROM (SELECT ID, validated_signal_id,DISPLAY_NAME, last_updated, NAME, product_id,pt_code, PT, rn, selected_data_source, NEW_SPON_COUNT,CUM_SPON_COUNT,EB95, EB05, COMMENTS,requested_by,generic_comment
                            FROM (WITH max_per AS
                                   (SELECT DISTINCT vs.NAME,aa.product_id,aa.pt_code, aa.PT, aa.disposition_id,disp.DISPLAY_NAME,aa.period_start_date,aa.period_end_date,aa.product_name,aa.requested_by,
                                         MAX(aa.last_updated) OVER (PARTITION BY vaa.validated_signal_id, aa.product_id, aa.pt_code) last_updated,
                                         dbms_lob.substr(vs.generic_comment,dbms_lob.getlength(vs.generic_comment),1) generic_comment,rconf.selected_data_source,aa.NEW_SPON_COUNT, aa.EB05,
                                         ac.COMMENTS,aa.CUM_SPON_COUNT,aa.EB95
                                         FROM agg_alert aa 
                                         LEFT JOIN validated_agg_alerts vaa ON (aa.ID = vaa.agg_alert_id)
                                         LEFT JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID
                                         LEFT JOIN VALIDATED_ALERT_COMMENTS vac ON (vs.ID = vac.VALIDATED_SIGNAL_ID)
                                         JOIN RCONFIG rconf ON (aa.alert_configuration_id = rconf.id)
                                         LEFT JOIN DISPOSITION disp on (aa.disposition_id = disp.ID)
                                         LEFT JOIN ALERT_COMMENT AC on (AC.id = vac.COMMENT_ID)
                                         WHERE aa.product_id = ${productId}
                                         AND period_start_date = '${periodStartDate}'
                                         AND period_end_date = '${periodEndDate}'
                                         AND rconf.selected_data_source = '${dataSrc}'
                                         ${dispositionCriteria}
                                         )
                                         SELECT aa.ID,vaa.validated_signal_id, mp.DISPLAY_NAME,aa.last_updated, vs.NAME,aa.product_id,aa.requested_by,
                                             aa.pt_code,aa.PT,ROWNUM rn, mp.selected_data_source, aa.NEW_SPON_COUNT, aa.EB05,aa.CUM_SPON_COUNT,aa.EB95,
                                             mp.COMMENTS,dbms_lob.substr(vs.generic_comment,dbms_lob.getlength(vs.generic_comment),1) generic_comment
                                         FROM agg_alert aa 
                                         LEFT JOIN validated_agg_alerts vaa ON (aa.ID = vaa.agg_alert_id)
                                         LEFT JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID

                                         JOIN max_per mp ON ( mp.product_name = aa.product_name AND mp.period_start_date =aa.period_start_date
                                                        AND mp.period_end_date = aa.period_end_date AND mp.disposition_id = aa.disposition_id
                                                        AND mp.last_updated = aa.last_updated))
                                         WHERE validated_signal_id IS NOT NULL
                                  ORDER BY rn)
                              UNION 
                              SELECT DISTINCT ID, validated_signal_id, PT, DISPLAY_NAME, selected_data_source, NEW_SPON_COUNT, CUM_SPON_COUNT,EB95,EB05, COMMENTS,NAME,requested_by,product_id,pt_code,EXEC_CONFIGURATION_ID,ASSIGNED_TO_ID,NULL AS generic_comment,ALERT_CONFIGURATION_ID
                              FROM (SELECT ID, validated_signal_id,DISPLAY_NAME, last_updated, NAME, product_id,pt_code, PT, rn, selected_data_source, NEW_SPON_COUNT,CUM_SPON_COUNT,EB95, EB05, COMMENTS,requested_by,EXEC_CONFIGURATION_ID,ASSIGNED_TO_ID,ALERT_CONFIGURATION_ID
                                    FROM (WITH max_per AS
                                           (SELECT DISTINCT aa.NAME,aa.product_id,aa.pt_code, aa.PT, aa.disposition_id,disp.DISPLAY_NAME, aa.period_start_date,aa.period_end_date,aa.product_name,
                                             MAX(aa.last_updated) OVER (PARTITION BY aa.NAME, aa.product_id, aa.pt_code) last_updated , rconf.selected_data_source, aa.NEW_SPON_COUNT,aa.CUM_SPON_COUNT,aa.EB95, aa.EB05,  ac.COMMENTS,aa.requested_by,aa.ASSIGNED_TO_ID,aa.EXEC_CONFIGURATION_ID
                                             FROM agg_alert aa
                                             JOIN RCONFIG rconf ON (aa.alert_configuration_id = rconf.id)
                                             LEFT JOIN DISPOSITION disp on (aa.disposition_id = disp.ID)
                                             LEFT JOIN
                                             (SELECT AC.* , ROW_NUMBER() OVER (PARTITION BY PRODUCT_ID,PT_CODE ORDER BY AC.ID DESC) AS rn FROM ALERT_COMMENT AC)
                                              AC ON aa.product_id = AC.product_id and aa.pt_code = AC.pt_code AND AC.rn = 1
                                             WHERE aa.product_id = ${productId}
                                             AND period_start_date = '${periodStartDate}'
                                             AND period_end_date = '${periodEndDate}'
                                             AND rconf.selected_data_source = '${dataSrc}'
                                             ${dispositionCriteria}
                                             )
                                             SELECT aa.ID,vaa.validated_signal_id,mp.DISPLAY_NAME, aa.last_updated, aa.NAME,aa.product_id, aa.pt_code, aa.PT,ROWNUM rn, mp.selected_data_source
                                              , aa.NEW_SPON_COUNT, aa.EB05,aa.CUM_SPON_COUNT,aa.EB95, mp.COMMENTS,aa.requested_by,aa.EXEC_CONFIGURATION_ID,aa.ASSIGNED_TO_ID, aa.alert_configuration_id
                                              FROM agg_alert aa LEFT JOIN validated_agg_alerts vaa ON (aa.ID = vaa.agg_alert_id)
                                              JOIN max_per mp ON ( mp.product_name = aa.product_name AND mp.period_start_date = aa.period_start_date
                                                              AND mp.period_end_date = aa.period_end_date
                                                              AND mp.disposition_id = aa.disposition_id
                                                              AND mp.last_updated = aa.last_updated ))
                                             ORDER BY rn
                                    )
                                   WHERE validated_signal_id IS NULL))
                                   ${searchCriteria}
                                   ${orderByCriteria}
                                )
                                   ${serverSideCriteria}
               
        """

    }

    static product_summary_count = { Integer productId, periodStartDate, periodEndDate, disposition, dataSrc ->
        def dispositionCriteria = ""
        if (disposition) {
            dispositionCriteria = "AND aa.disposition_id IN (${disposition})"
        }
        """
         SELECT count(*) cnt
                FROM (SELECT DISTINCT ID, validated_signal_id, PT, DISPLAY_NAME, selected_data_source, NEW_SPON_COUNT, EB05,CUM_SPON_COUNT,EB95, COMMENTS,NAME,requested_by,product_id,pt_code,NULL AS EXEC_CONFIGURATION_ID,NULL AS ASSIGNED_TO_ID,generic_comment
                      FROM (SELECT ID, validated_signal_id,DISPLAY_NAME, last_updated, NAME, product_id,pt_code, PT, rn, selected_data_source, NEW_SPON_COUNT,CUM_SPON_COUNT,EB95, EB05, COMMENTS,requested_by,generic_comment
                            FROM (WITH max_per AS
                                   (SELECT DISTINCT vs.NAME,aa.product_id,aa.pt_code, aa.PT, aa.disposition_id,disp.DISPLAY_NAME,aa.period_start_date,aa.period_end_date,aa.product_name,aa.requested_by,
                                         MAX(aa.last_updated) OVER (PARTITION BY vaa.validated_signal_id, aa.product_id, aa.pt_code) last_updated,
                                         dbms_lob.substr(vs.generic_comment,dbms_lob.getlength(vs.generic_comment),1) generic_comment,rconf.selected_data_source,aa.NEW_SPON_COUNT, aa.EB05,
                                         ac.COMMENTS,aa.CUM_SPON_COUNT,aa.EB95
                                         FROM agg_alert aa 
                                         LEFT JOIN validated_agg_alerts vaa ON (aa.ID = vaa.agg_alert_id)
                                         LEFT JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID
                                         LEFT JOIN VALIDATED_ALERT_COMMENTS vac ON (vs.ID = vac.VALIDATED_SIGNAL_ID)
                                         JOIN RCONFIG rconf ON (aa.alert_configuration_id = rconf.id)
                                         LEFT JOIN DISPOSITION disp on (aa.disposition_id = disp.ID)
                                         LEFT JOIN ALERT_COMMENT AC on (AC.id = vac.COMMENT_ID)
                                         WHERE aa.product_id = ${productId}
                                         AND period_start_date = '${periodStartDate}'
                                         AND period_end_date = '${periodEndDate}'
                                         AND rconf.selected_data_source = '${dataSrc}'
                                         ${dispositionCriteria}
                                         )
                                         SELECT aa.ID,vaa.validated_signal_id, mp.DISPLAY_NAME,aa.last_updated, vs.NAME,aa.product_id,aa.requested_by,
                                             aa.pt_code,aa.PT,ROWNUM rn, mp.selected_data_source, aa.NEW_SPON_COUNT, aa.EB05,aa.CUM_SPON_COUNT,aa.EB95,
                                             mp.COMMENTS,dbms_lob.substr(vs.generic_comment,dbms_lob.getlength(vs.generic_comment),1) generic_comment
                                         FROM agg_alert aa 
                                         LEFT JOIN validated_agg_alerts vaa ON (aa.ID = vaa.agg_alert_id)
                                                                                  LEFT JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID

                                         JOIN max_per mp ON ( mp.product_name = aa.product_name AND mp.period_start_date =aa.period_start_date
                                                        AND mp.period_end_date = aa.period_end_date AND mp.disposition_id = aa.disposition_id
                                                        AND mp.last_updated = aa.last_updated))
                                         WHERE validated_signal_id IS NOT NULL
                                  ORDER BY rn)
                              UNION 
                              SELECT DISTINCT ID, validated_signal_id, PT, DISPLAY_NAME, selected_data_source, NEW_SPON_COUNT, CUM_SPON_COUNT,EB95,EB05, COMMENTS,NAME,requested_by,product_id,pt_code,EXEC_CONFIGURATION_ID,ASSIGNED_TO_ID,NULL AS generic_comment
                              FROM (SELECT ID, validated_signal_id,DISPLAY_NAME, last_updated, NAME, product_id,pt_code, PT, rn, selected_data_source, NEW_SPON_COUNT,CUM_SPON_COUNT,EB95, EB05, COMMENTS,requested_by,EXEC_CONFIGURATION_ID,ASSIGNED_TO_ID
                                    FROM (WITH max_per AS
                                           (SELECT DISTINCT aa.NAME,aa.product_id,aa.pt_code, aa.PT, aa.disposition_id,disp.DISPLAY_NAME, aa.period_start_date,aa.period_end_date,aa.product_name,
                                             MAX(aa.last_updated) OVER (PARTITION BY aa.NAME, aa.product_id, aa.pt_code) last_updated , rconf.selected_data_source, aa.NEW_SPON_COUNT,aa.CUM_SPON_COUNT,aa.EB95, aa.EB05,  ac.COMMENTS,aa.requested_by,aa.ASSIGNED_TO_ID,aa.EXEC_CONFIGURATION_ID
                                             FROM agg_alert aa
                                             JOIN RCONFIG rconf ON (aa.alert_configuration_id = rconf.id)
                                             LEFT JOIN DISPOSITION disp on (aa.disposition_id = disp.ID)
                                             LEFT JOIN
                                             (SELECT AC.* , ROW_NUMBER() OVER (PARTITION BY PRODUCT_ID,PT_CODE ORDER BY AC.ID DESC) AS rn FROM ALERT_COMMENT AC)
                                              AC ON aa.product_id = AC.product_id and aa.pt_code = AC.pt_code AND AC.rn = 1
                                             WHERE aa.product_id = ${productId}
                                             AND period_start_date = '${periodStartDate}'
                                             AND period_end_date = '${periodEndDate}'
                                             AND rconf.selected_data_source = '${dataSrc}'
                                             ${dispositionCriteria}
                                             )
                                             SELECT aa.ID,vaa.validated_signal_id,mp.DISPLAY_NAME, aa.last_updated, aa.NAME,aa.product_id, aa.pt_code, aa.PT,ROWNUM rn, mp.selected_data_source
                                              , aa.NEW_SPON_COUNT, aa.EB05,aa.CUM_SPON_COUNT,aa.EB95, mp.COMMENTS,aa.requested_by,aa.EXEC_CONFIGURATION_ID,aa.ASSIGNED_TO_ID
                                              FROM agg_alert aa LEFT JOIN validated_agg_alerts vaa ON (aa.ID = vaa.agg_alert_id)
                                              JOIN max_per mp ON ( mp.product_name = aa.product_name AND mp.period_start_date = aa.period_start_date
                                                              AND mp.period_end_date = aa.period_end_date
                                                              AND mp.disposition_id = aa.disposition_id
                                                              AND mp.last_updated = aa.last_updated ))
                                             ORDER BY rn
                                    )
                                   WHERE validated_signal_id IS NULL)
      """

    }

    static product_summary_evdas_sql = { productName, periodStartDate, periodEndDate, start, length, disposition, isOutputFormat, orderByCriteria, searchCriteria, dataSource ->
        def dispositionCriteria = ""
        if (disposition) {
            dispositionCriteria = "AND ea.disposition_id IN (${disposition})"
        }
        def serverSideCriteria = ""
        if (!isOutputFormat) {
            serverSideCriteria = "OFFSET ${start} ROWS FETCH NEXT ${length} ROWS ONLY"
        }

        """
          SELECT * FROM
          (SELECT ID, validated_signal_id, ROWNUM rn,count(*) over () as filtered_count ,total_count, PT, DISPLAY_NAME, NEW_SPONT AS NEW_SPON_COUNT,TOT_SPONT AS CUM_SPON_COUNT, COMMENTS,NAME,requested_by,substance_id AS PRODUCT_ID,pt_code,EXEC_CONFIGURATION_ID,ASSIGNED_TO_ID,generic_comment,selected_data_source,ALERT_CONFIGURATION_ID
       
          FROM 
          (SELECT ID, validated_signal_id, ROWNUM rn,count(*) over () as total_count ,PT, DISPLAY_NAME, NEW_SPONT,TOT_SPONT, COMMENTS,NAME,requested_by,substance_id,pt_code,EXEC_CONFIGURATION_ID,ASSIGNED_TO_ID,generic_comment,selected_data_source,ALERT_CONFIGURATION_ID
                FROM (SELECT DISTINCT ID, validated_signal_id,PT, DISPLAY_NAME, NEW_SPONT,TOT_SPONT, COMMENTS,NAME,requested_by,substance_id,pt_code,NULL AS EXEC_CONFIGURATION_ID,NULL AS ASSIGNED_TO_ID,generic_comment,'${
            dataSource
        }' AS selected_data_source, NULL AS ALERT_CONFIGURATION_ID
                      FROM (SELECT ID,validated_signal_id,DISPLAY_NAME,last_updated, NAME, substance_id,pt_code,PT, rn, NEW_SPONT,TOT_SPONT, COMMENTS,requested_by,generic_comment
                            FROM (WITH max_per AS
                                   (SELECT DISTINCT vs.NAME,ea.substance_id,ea.pt_code,ea.PT,ea.disposition_id,disp.DISPLAY_NAME,ea.period_start_date,ea.period_end_date,ea.substance,ea.requested_by,
                                         MAX(ea.last_updated) OVER (PARTITION BY vaa.validated_signal_id, ea.substance_id, ea.pt_code) last_updated,
                                          dbms_lob.substr(vs.generic_comment,dbms_lob.getlength(vs.generic_comment),1) generic_comment,ea.NEW_SPONT,
                                         ac.COMMENTS,ea.TOT_SPONT
                                         FROM evdas_alert ea 
                                         LEFT JOIN validated_evdas_alerts vaa ON (ea.ID = vaa.evdas_alert_id)
                                         LEFT JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID
                                         LEFT JOIN VALIDATED_ALERT_COMMENTS vac ON (vs.ID = vac.VALIDATED_SIGNAL_ID)
                                         LEFT JOIN DISPOSITION disp on (ea.disposition_id = disp.ID)
                                         LEFT JOIN ALERT_COMMENT AC on (AC.id = vac.COMMENT_ID)
                                         WHERE substance = '${productName}'
                                         AND period_start_date ='${periodStartDate}'
                                         AND period_end_date ='${periodEndDate}'
                                         ${dispositionCriteria}
                                   )
                                   SELECT ea.ID,vaa.validated_signal_id,mp.DISPLAY_NAME,ea.last_updated, vs.NAME,ea.substance_id, ea.requested_by,ea.pt_code,ea.PT,ROWNUM rn,
                                     ea.NEW_SPONT,ea.TOT_SPONT,
                                             mp.COMMENTS,dbms_lob.substr(vs.generic_comment,dbms_lob.getlength(vs.generic_comment),1) generic_comment
                                   FROM evdas_alert ea 
                                   LEFT JOIN validated_evdas_alerts vaa ON (ea.ID = vaa.evdas_alert_id)
                                   LEFT JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID

                                   JOIN max_per mp ON ( mp.substance = ea.substance AND mp.period_start_date =ea.period_start_date
                                                        AND mp.period_end_date = ea.period_end_date AND mp.disposition_id = ea.disposition_id
                                                        AND mp.last_updated = ea.last_updated))
                                   WHERE validated_signal_id IS NOT NULL
                                  ORDER BY rn)
                            UNION
                            SELECT DISTINCT ID, validated_signal_id, PT, DISPLAY_NAME, NEW_SPONT, TOT_SPONT, COMMENTS,NAME,requested_by,substance_id,pt_code,EXEC_CONFIGURATION_ID,ASSIGNED_TO_ID,NULL AS generic_comment,'${
            dataSource
        }' AS selected_data_source,ALERT_CONFIGURATION_ID
                            FROM (SELECT ID, validated_signal_id,DISPLAY_NAME,last_updated, NAME, substance_id,pt_code, PT,rn, NEW_SPONT,TOT_SPONT, COMMENTS,requested_by,EXEC_CONFIGURATION_ID,ASSIGNED_TO_ID,ALERT_CONFIGURATION_ID
                            FROM (WITH max_per AS
                                  (SELECT DISTINCT ea.NAME,ea.substance_id,ea.pt_code,ea.PT,ea.disposition_id,disp.DISPLAY_NAME,ea.period_start_date,ea.period_end_date,ea.substance,
                                   MAX(ea.last_updated) OVER (PARTITION BY ea.NAME, ea.substance_id, ea.pt_code) last_updated, ea.NEW_SPONT,ea.TOT_SPONT, ac.COMMENTS,ea.requested_by,ea.ASSIGNED_TO_ID,ea.EXEC_CONFIGURATION_ID
                                   FROM evdas_alert ea
                                   LEFT JOIN DISPOSITION disp on (ea.disposition_id = disp.ID)
                                   LEFT JOIN
                                             (SELECT AC.* , ROW_NUMBER() OVER (PARTITION BY PRODUCT_ID,PT_CODE ORDER BY AC.ID DESC) AS rn FROM ALERT_COMMENT AC)
                                              AC ON ea.substance_id = AC.product_id and ea.pt_code = AC.pt_code AND AC.rn = 1
                                   WHERE substance = '${productName}'
                                   AND period_start_date = '${periodStartDate}'
                                   AND period_end_date = '${periodEndDate}'
                                   ${dispositionCriteria}
                                  )
                                  SELECT ea.ID,vaa.validated_signal_id,mp.DISPLAY_NAME,ea.last_updated, ea.NAME,ea.substance_id, ea.pt_code,ROWNUM rn,ea.PT,
                                   ea.NEW_SPONT,ea.TOT_SPONT, mp.COMMENTS,ea.requested_by,ea.EXEC_CONFIGURATION_ID,ea.ASSIGNED_TO_ID, ea.alert_configuration_id
                                  FROM evdas_alert ea LEFT JOIN validated_evdas_alerts vaa ON (ea.ID = vaa.evdas_alert_id)
                                  JOIN max_per mp ON ( mp.substance = ea.substance AND mp.period_start_date = ea.period_start_date
                                                       AND mp.period_end_date = ea.period_end_date
                                                       AND mp.disposition_id = ea.disposition_id
                                                       AND mp.last_updated = ea.last_updated ))
                                  ORDER BY rn)
                                  WHERE validated_signal_id IS NULL))
                                  ${searchCriteria}
                                  ${orderByCriteria}
                                )
                                ${serverSideCriteria}
                                 
        """
    }

    static product_summary_evdas_count = { productName, periodStartDate, periodEndDate, disposition, dataSource ->
        def dispositionCriteria = ""
        if (disposition) {
            dispositionCriteria = "AND ea.disposition_id IN (${disposition})"
        }
        """
           SELECT count(*) cnt
                FROM (SELECT DISTINCT ID, validated_signal_id,PT, DISPLAY_NAME, NEW_SPONT,TOT_SPONT, COMMENTS,NAME,requested_by,substance_id,pt_code,NULL AS EXEC_CONFIGURATION_ID,NULL AS ASSIGNED_TO_ID,generic_comment,'${
            dataSource
        }' AS selected_data_source
                      FROM (SELECT ID,validated_signal_id,DISPLAY_NAME,last_updated, NAME, substance_id,pt_code,PT, rn, NEW_SPONT,TOT_SPONT, COMMENTS,requested_by,generic_comment
                            FROM (WITH max_per AS
                                   (SELECT DISTINCT vs.NAME,ea.substance_id,ea.pt_code,ea.PT,ea.disposition_id,disp.DISPLAY_NAME,ea.period_start_date,ea.period_end_date,ea.substance,ea.requested_by,
                                         MAX(ea.last_updated) OVER (PARTITION BY vaa.validated_signal_id, ea.substance_id, ea.pt_code) last_updated,
                                          dbms_lob.substr(vs.generic_comment,dbms_lob.getlength(vs.generic_comment),1) generic_comment,ea.NEW_SPONT,
                                         ac.COMMENTS,ea.TOT_SPONT
                                         FROM evdas_alert ea 
                                         LEFT JOIN validated_evdas_alerts vaa ON (ea.ID = vaa.evdas_alert_id)
                                         LEFT JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID
                                         LEFT JOIN VALIDATED_ALERT_COMMENTS vac ON (vs.ID = vac.VALIDATED_SIGNAL_ID)
                                         LEFT JOIN DISPOSITION disp on (ea.disposition_id = disp.ID)
                                         LEFT JOIN ALERT_COMMENT AC on (AC.id = vac.COMMENT_ID)
                                         WHERE substance = '${productName}'
                                         AND period_start_date ='${periodStartDate}'
                                         AND period_end_date ='${periodEndDate}'
                                         ${dispositionCriteria}      
                                   )
                                   SELECT ea.ID,vaa.validated_signal_id,mp.DISPLAY_NAME,ea.last_updated, vs.NAME,ea.substance_id, ea.requested_by,ea.pt_code,ea.PT,ROWNUM rn,
                                     ea.NEW_SPONT,ea.TOT_SPONT,
                                             mp.COMMENTS,dbms_lob.substr(vs.generic_comment,dbms_lob.getlength(vs.generic_comment),1) generic_comment
                                   FROM evdas_alert ea 
                                   LEFT JOIN validated_evdas_alerts vaa ON (ea.ID = vaa.evdas_alert_id)
                                   LEFT JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID

                                   JOIN max_per mp ON ( mp.substance = ea.substance AND mp.period_start_date =ea.period_start_date
                                                        AND mp.period_end_date = ea.period_end_date AND mp.disposition_id = ea.disposition_id
                                                        AND mp.last_updated = ea.last_updated))
                                   WHERE validated_signal_id IS NOT NULL
                                  ORDER BY rn)
                            UNION
                            SELECT DISTINCT ID, validated_signal_id, PT, DISPLAY_NAME, NEW_SPONT, TOT_SPONT, COMMENTS,NAME,requested_by,substance_id,pt_code,EXEC_CONFIGURATION_ID,ASSIGNED_TO_ID,NULL AS generic_comment,'${
            dataSource
        }' AS selected_data_source
                            FROM (SELECT ID, validated_signal_id,DISPLAY_NAME,last_updated, NAME, substance_id,pt_code, PT,rn, NEW_SPONT,TOT_SPONT, COMMENTS,requested_by,EXEC_CONFIGURATION_ID,ASSIGNED_TO_ID
                            FROM (WITH max_per AS
                                  (SELECT DISTINCT ea.NAME,ea.substance_id,ea.pt_code,ea.PT,ea.disposition_id,disp.DISPLAY_NAME,ea.period_start_date,ea.period_end_date,ea.substance,
                                   MAX(ea.last_updated) OVER (PARTITION BY ea.NAME, ea.substance_id, ea.pt_code) last_updated, ea.NEW_SPONT,ea.TOT_SPONT, ac.COMMENTS,ea.requested_by,ea.ASSIGNED_TO_ID,ea.EXEC_CONFIGURATION_ID
                                   FROM evdas_alert ea
                                   LEFT JOIN DISPOSITION disp on (ea.disposition_id = disp.ID)
                                   LEFT JOIN
                                             (SELECT AC.* , ROW_NUMBER() OVER (PARTITION BY PRODUCT_ID,PT_CODE ORDER BY AC.ID DESC) AS rn FROM ALERT_COMMENT AC)
                                              AC ON ea.substance_id = AC.product_id and ea.pt_code = AC.pt_code AND AC.rn = 1
                                   WHERE substance = '${productName}'
                                   AND period_start_date = '${periodStartDate}'
                                   AND period_end_date = '${periodEndDate}'
                                   ${dispositionCriteria}
                                  )
                                  SELECT ea.ID,vaa.validated_signal_id,mp.DISPLAY_NAME,ea.last_updated, ea.NAME,ea.substance_id, ea.pt_code,ROWNUM rn,ea.PT,
                                   ea.NEW_SPONT,ea.TOT_SPONT, mp.COMMENTS,ea.requested_by,ea.EXEC_CONFIGURATION_ID,ea.ASSIGNED_TO_ID
                                  FROM evdas_alert ea LEFT JOIN validated_evdas_alerts vaa ON (ea.ID = vaa.evdas_alert_id)
                                  JOIN max_per mp ON ( mp.substance = ea.substance AND mp.period_start_date = ea.period_start_date
                                                       AND mp.period_end_date = ea.period_end_date
                                                       AND mp.disposition_id = ea.disposition_id
                                                       AND mp.last_updated = ea.last_updated ))
                                  ORDER BY rn)
                                  WHERE validated_signal_id IS NULL) 
        """

    }

    static add_case_faers_sql = { List<String> cl ->
        String inClause = ''
        if (cl && cl.size() <= 1000) {
            inClause = "(${cl.join(",")})"
        } else if (cl && cl.size() > 1000) {
            inClause = cl.collate(1000).join(" OR case_num IN ").replace("[", "(").replace("]", ")")
        }
        """
        WITH max_version AS
     (SELECT   MAX (version_num) version_num, case_id
          FROM c_identification
      GROUP BY case_id)
       SELECT ci.case_id "masterCaseId_28", ci.case_num "masterCaseNum_0", ci.version_num "masterVersionNum_1",
       cifu.flag_primary_source_hcp "reportersHcpFlag_2",
       ci.source_type_desc "masterRptTypeId_3",
       ci.date_first_receipt "masterInitReptDate_4",
       cifu.date_receipt "masterFollowupDate_5",
       cdrc.ae_pt_all "masterPrefTermAll_6",
       cifu.case_outcome_desc "assessOutcome_7",
       vll.listedness_text "eventConserCoreListedness_8",
       cpifu.prod_family_name "productFamilyId_9",
       cifu.significant_counter "masterFupNum_10",
       DECODE (cifu.flag_ver_significant, 1, 'Yes', 'No') "masterFlagSt_11",
       vpr.product_name "productProductId_27",
       cifu.prim_evt_pref_term "masterPrimEvtPrefTerm_13",
       cifu.prim_prod_name "masterPrimProdName_14",
       caei.ae_outcome_desc "eventEvtOutcomeId_15",
       case when 
        ci.flag_serious_hosp = 1 or
        ci.flag_serious_other_med_imp = 1 or
        ci.flag_serious_death = 1 or
        ci.flag_serious_threat = 1 or
        ci.flag_serious_disable = 1 or
        ci.flag_serious_cong_anom = 1 or
        ci.flag_serious_int_req = 1
        then 'Serious' 
        else 'Non Serious'
        end as "assessSeriousness_16",
       csda.susp_prod_info "masterSuspProdAgg_17",
       cdrc.concomit_prod_all "masterConcomitProdList_32",
       ci.occured_country_desc "masterCountryId_19",
       cpc.patient_age_group_desc "patInfoAgeGroupId_20",
       cpc.patient_sex_desc "patInfoGenderId_21",
       cdifu.rechallenge_id "prodDrugsPosRechallenge_22",
       cifu.date_locked "masterDateLocked_23",
       decode(ci.flag_serious_death,1,'Yes', 'No') "masterFatalFlag_74",
       caei.mdr_ae_pt "eventPrefTerm_25",
       cpi.prod_id_resolved "cpiProdIdResolved_26",
       null as "narrativeNarrative_29",
       null as "ccAePt_30"
  FROM max_version mv JOIN c_identification ci
       ON (mv.version_num = ci.version_num AND mv.case_id = ci.case_id)
       LEFT JOIN c_identification_fu cifu
       ON (    ci.tenant_id = cifu.tenant_id
           AND ci.version_num = cifu.version_num
           AND ci.case_id = cifu.case_id
          )
       LEFT JOIN c_patient_characteristics cpc
       ON (    ci.tenant_id = cpc.tenant_id
           AND ci.version_num = cpc.version_num
           AND ci.case_id = cpc.case_id
          )
       LEFT JOIN c_ae_identification caei
       ON (    ci.tenant_id = caei.tenant_id
           AND ci.version_num = caei.version_num
           AND ci.case_id = caei.case_id
           AND mdr_ae_pt = prim_evt_pref_term
          )
       LEFT JOIN cdr_clob cdrc
       ON (    ci.tenant_id = cdrc.tenant_id
           AND ci.version_num = cdrc.version_num
           AND ci.case_id = cdrc.case_id
          )
       LEFT JOIN c_prod_identification_fu cpifu
       ON (    ci.tenant_id = cpifu.tenant_id
           AND ci.version_num = cpifu.version_num
           AND ci.case_id = cpifu.case_id
           AND prod_name_resolved = prim_prod_name
          )
       LEFT JOIN c_prod_identification cpi
       ON (    cpifu.tenant_id = cpi.tenant_id
           AND cpifu.version_num = cpi.version_num
           AND cpifu.case_id = cpi.case_id
           AND cpifu.prod_rec_num = cpi.prod_rec_num
          )
       LEFT JOIN vw_product vpr
       ON(cpi.prod_id_resolved=vpr.product_id) 
       LEFT JOIN c_drug_identification_fu cdifu
       ON (    cpifu.tenant_id = cdifu.tenant_id
           AND cpifu.version_num = cdifu.version_num
           AND cpifu.case_id = cdifu.case_id
           AND cpifu.prod_rec_num = cdifu.prod_rec_num
          )
       LEFT JOIN cdr_conser_evt_label ccel
       ON (    caei.tenant_id = ccel.tenant_id
           AND caei.version_num = ccel.version_num
           AND caei.case_id = ccel.case_id
           AND caei.ae_rec_num = ccel.ae_rec_num
          )
       LEFT JOIN c_susp_dose_agg csda
       ON (    ci.tenant_id = csda.tenant_id
           AND ci.version_num = csda.version_num
           AND ci.case_id = csda.case_id
          )
       LEFT JOIN vw_llist_listedness vll
       ON (ccel.conser_listedness = vll.listedness_id)
        WHERE case_num IN ${inClause}
        """
    }

    static child_case_max_veriosn = { caseNumber ->
        """
        select max(Version_num) MAX_VERSION from v_c_identification where case_num='${caseNumber}'
        group by case_num
        """
    }
    static default_followup_number = { caseNumber, version ,caseType ->
        """
        SELECT (case when significant_counter > 0 then (significant_counter -1) else significant_counter end) significant_counter
        FROM c_identification_fu
        cifu JOIN v_c_identification ci
        ON(cifu.tenant_id = ci.tenant_id
                AND cifu.case_id = ci.case_id
                AND cifu.version_num = ci.version_num
        )
        WHERE ci.case_num = '${caseNumber}' AND ci.version_num = ${version} AND ci.flag_Master_Case = ${caseType}
        """
    }

    static signal_alert_ids = { productIdAndPtCode, execConfigId, prevExecConfigId ->
        """
         with t1 as 
           (SELECT VSID,PRODUCT_ID,PT_CODE,rn 
           FROM (SELECT vs.ID VSID, PRODUCT_ID ,PT_CODE  ,row_number() OVER(PARTITION BY aa.PRODUCT_ID , aa.PT_CODE,aa.SOC, vs.ID ORDER by aa.ID) rn
                 FROM AGG_ALERT aa
                 JOIN validated_agg_alerts vaa ON (aa.ID = vaa.agg_alert_id)
                 JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID
                 WHERE (PRODUCT_ID ,PT_CODE,SOC,PT) IN (${productIdAndPtCode}) AND aa.EXEC_CONFIGURATION_ID = ${prevExecConfigId}
                 )
           where rn = '1'),
           t2 as 
                (select  aa.ID alertId,PRODUCT_ID ,PT_CODE
                 FROM AGG_ALERT aa
                 WHERE aa.EXEC_CONFIGURATION_ID = ${execConfigId} and (PRODUCT_ID ,PT_CODE,SOC,PT) IN (${productIdAndPtCode})
                 )
         select ALERTID,VSID from t1 join t2 on (t1.PRODUCT_ID = t2.PRODUCT_ID and t1.PT_CODE = t2.PT_CODE)
       
       
        """
    }

    static signal_ids_auto_routing = { productIdAndPtCode, execConfigId, prevExecConfigId ->
        """
           SELECT VSID
           FROM (SELECT vs.ID VSID, PRODUCT_ID ,PT_CODE  ,row_number() OVER(PARTITION BY aa.PRODUCT_ID , aa.PT_CODE,aa.SOC, vs.ID ORDER by aa.ID) rn
                 FROM AGG_ALERT aa
                 JOIN validated_agg_alerts vaa ON (aa.ID = vaa.agg_alert_id)
                 JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID
                 WHERE (PRODUCT_ID ,PT_CODE,SOC,PT) IN (${productIdAndPtCode}) AND aa.EXEC_CONFIGURATION_ID = ${prevExecConfigId})
           WHERE rn = '1'
        """
    }

    static signal_alert_ids_single = { caseNumberAndProductFamily, execConfigId, configId ->
        """
         with t1 as 
           (SELECT VSID,CASE_NUMBER,PRODUCT_FAMILY,rn 
           FROM (SELECT vs.ID VSID, CASE_NUMBER , PRODUCT_FAMILY ,row_number() OVER(PARTITION BY sca.CASE_NUMBER , sca.PRODUCT_FAMILY, vs.ID ORDER by sca.ID) rn
                 FROM SINGLE_CASE_ALERT sca
                 JOIN VALIDATED_SINGLE_ALERTS vsa ON (sca.ID = vsa.SINGLE_ALERT_ID)
                 JOIN VALIDATED_SIGNAL vs ON vs.ID = vsa.VALIDATED_SIGNAL_ID
                 WHERE (CASE_NUMBER ,PRODUCT_FAMILY) IN (${caseNumberAndProductFamily}) AND sca.alert_configuration_id = ${configId}
                 )
           where rn = '1'),
           t2 as 
                (select  sca.ID alertId,CASE_NUMBER,PRODUCT_FAMILY
                 FROM SINGLE_CASE_ALERT sca
                 WHERE sca.EXEC_CONFIG_ID = ${execConfigId} and (CASE_NUMBER ,PRODUCT_FAMILY) IN (${caseNumberAndProductFamily})
                 )
         select ALERTID,VSID from t1 join t2 on (t1.CASE_NUMBER = t2.CASE_NUMBER and t1.PRODUCT_FAMILY = t2.PRODUCT_FAMILY)
       
      
        """
    }

    static signal_alert_ids_evdas = { substanceIdAndPtCode, execConfigId, prevExecConfigId ->
        """
         with t1 as 
           (SELECT VSID,SUBSTANCE_ID,PT_CODE,rn 
           FROM (SELECT vs.ID VSID, SUBSTANCE_ID ,PT_CODE  ,row_number() OVER(PARTITION BY ea.SUBSTANCE_ID , ea.PT_CODE, vs.ID ORDER by ea.ID) rn
                 FROM EVDAS_ALERT ea
                 JOIN VALIDATED_EVDAS_ALERTS vea ON (ea.ID = vea.EVDAS_ALERT_ID)
                 JOIN VALIDATED_SIGNAL vs ON vs.ID = vea.VALIDATED_SIGNAL_ID
                 WHERE (SUBSTANCE_ID ,PT_CODE) IN (${substanceIdAndPtCode}) AND ea.EXEC_CONFIGURATION_ID = ${prevExecConfigId}
                 )
           where rn = '1'),
           t2 as 
                (select  ea.ID alertId,SUBSTANCE_ID ,PT_CODE
                 FROM EVDAS_ALERT ea
                 WHERE ea.EXEC_CONFIGURATION_ID = ${execConfigId} and (SUBSTANCE_ID ,PT_CODE) IN (${
            substanceIdAndPtCode
        })
                 )
         select ALERTID,VSID from t1 join t2 on (t1.SUBSTANCE_ID = t2.SUBSTANCE_ID and t1.PT_CODE = t2.PT_CODE)
       
       
        """
    }

    static signal_ids_evdas_auto_routing = { substanceIdAndPtCode, execConfigId, prevExecConfigId ->
        """
           SELECT VSID 
           FROM (SELECT vs.ID VSID, SUBSTANCE_ID ,PT_CODE  ,row_number() OVER(PARTITION BY ea.SUBSTANCE_ID , ea.PT_CODE, vs.ID ORDER by ea.ID) rn
                 FROM EVDAS_ALERT ea
                 JOIN VALIDATED_EVDAS_ALERTS vea ON (ea.ID = vea.EVDAS_ALERT_ID)
                 JOIN VALIDATED_SIGNAL vs ON vs.ID = vea.VALIDATED_SIGNAL_ID
                 WHERE (SUBSTANCE_ID ,PT_CODE) IN (${substanceIdAndPtCode}) AND ea.EXEC_CONFIGURATION_ID = ${prevExecConfigId})
           WHERE rn = '1'
        """
    }

    static signal_pecs = { List signalIdList ->
        String inCriteriaWhereClause = getDictProdNameINCriteria(signalIdList, 'vs.ID').toString()
        """
         With t1 as (
           SELECT VSID,rn 
           FROM (SELECT vs.ID VSID,row_number() OVER(PARTITION BY vaa.validated_signal_id, aa.PRODUCT_ID , aa.PT_CODE,aa.SOC, aa.alert_configuration_id ORDER by aa.ID) rn
                 FROM AGG_ALERT aa
                 JOIN validated_agg_alerts vaa ON (aa.ID = vaa.agg_alert_id)
                 JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID AND $inCriteriaWhereClause
                 JOIN EX_RCONFIG erc ON (erc.id = aa.EXEC_CONFIGURATION_ID)
                 where erc.is_Deleted = 0
                 and erc.is_Enabled = 1
                 and erc.adhoc_run = 0
                  
               )
           where rn = '1'
           union all
           SELECT VSID,rn 
                FROM (SELECT vs.ID VSID ,row_number() OVER(PARTITION BY vea.validated_signal_id, ea.SUBSTANCE_ID , ea.PT_CODE,ea.alert_configuration_id ORDER by ea.ID) rn
                      FROM EVDAS_ALERT ea
                      JOIN VALIDATED_EVDAS_ALERTS vea ON (ea.ID = vea.EVDAS_ALERT_ID)
                      JOIN VALIDATED_SIGNAL vs ON vs.ID = vea.VALIDATED_SIGNAL_ID AND $inCriteriaWhereClause
                      JOIN EX_EVDAS_CONFIG erc ON (erc.ID = ea.EXEC_CONFIGURATION_ID)
                      where erc.is_Deleted = 0
                      and erc.is_Enabled =1
                      and erc.adhoc_run = 0
                       )
                where rn = '1'
            )
            select t1.vsid as signalId,count(t1.vsid) as count 
            from t1
            group by t1.vsid
        """
    }
    static agg_archived_pecs = { List signalIdList ->
        String inCriteriaWhereClause = getDictProdNameINCriteria(signalIdList, 'vs.ID').toString()
        """
       SELECT VSID as signalId,count(*) as count
                FROM (SELECT vs.ID VSID ,row_number() OVER(PARTITION BY vaa.validated_signal_id, aa.PRODUCT_ID , aa.PT_CODE,aa.SOC, aa.alert_configuration_id ORDER by aa.ID) rn
                      FROM ARCHIVED_AGG_ALERT aa
                      JOIN VALIDATED_ARCHIVED_ACA vaa ON (aa.ID = vaa.ARCHIVED_ACA_ID)
                      JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID AND $inCriteriaWhereClause
                      JOIN EX_RCONFIG erc ON (erc.id = aa.EXEC_CONFIGURATION_ID)
                      where erc.is_Deleted = 0
                      and erc.is_Enabled =1
                      and erc.adhoc_run = 0
                      )
                WHERE rn ='1'
                group by VSID
             
        """
    }

    static signal_case_count = { List signalIdList ->
        String inCriteriaWhereClause = getDictProdNameINCriteria(signalIdList, 'vs.ID').toString()
        """
       SELECT VSID as signalId,count(*) as count
                FROM (SELECT vs.ID VSID ,row_number() OVER(PARTITION BY vea.validated_signal_id, ea.case_number , ea.product_family,ea.alert_configuration_id ORDER by ea.ID) rn
                      FROM SINGLE_CASE_ALERT ea
                      JOIN VALIDATED_SINGLE_ALERTS vea ON (ea.ID = vea.SINGLE_ALERT_ID)
                      JOIN VALIDATED_SIGNAL vs ON vs.ID = vea.VALIDATED_SIGNAL_ID AND $inCriteriaWhereClause
                      JOIN EX_RCONFIG erc ON (erc.id = ea.EXEC_CONFIG_ID)
                      where erc.is_Deleted = 0
                      and (erc.is_enabled =1 or erc.is_standalone = 1)
                      and erc.adhoc_run = 0
                      )
                WHERE rn ='1'
                group by VSID
             
        """
    }

    static signal_case_archived_count = { List signalIdList ->
        String inCriteriaWhereClause = getDictProdNameINCriteria(signalIdList, 'vs.ID').toString()
        """
       SELECT VSID as signalId,count(*) as count
                FROM (SELECT vs.ID VSID ,row_number() OVER(PARTITION BY vaa.validated_signal_id, aa.case_number , aa.product_family,aa.alert_configuration_id ORDER by aa.ID) rn
                      FROM ARCHIVED_SINGLE_CASE_ALERT aa
                      JOIN VALIDATED_ARCHIVED_SCA vaa ON (aa.ID = vaa.ARCHIVED_SCA_ID)
                      JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID AND $inCriteriaWhereClause
                      JOIN EX_RCONFIG erc ON (erc.id = aa.EXEC_CONFIG_ID)
                      where erc.is_Deleted = 0
                      and erc.is_Enabled =1
                      and erc.adhoc_run = 0
                      )
                WHERE rn ='1'
                group by VSID
             
        """
    }

    static signal_pec_validated_signal_count = { signalId ->
        """
           SELECT 'PVACOUNT' as abc ,count(*)
           FROM (SELECT vs.ID VSID,row_number() OVER(PARTITION BY vaa.validated_signal_id, aa.PRODUCT_ID , aa.PT_CODE,aa.SOC, aa.alert_configuration_id ORDER by aa.ID) rn
                 FROM AGG_ALERT aa
                 JOIN validated_agg_alerts vaa ON (aa.ID = vaa.agg_alert_id)
                 JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID
                 JOIN EX_RCONFIG erc ON (erc.id = aa.EXEC_CONFIGURATION_ID)
                 where erc.is_Deleted = 0
                 and erc.is_Enabled =1
                 and erc.adhoc_run = 0
                 and vs.ID = ${signalId}    
              )
              group by rn
              having rn ='1'
           union all
            
          SELECT 'EVDASCOUNT' as abc ,count(*)
                FROM (SELECT vs.ID VSID ,row_number() OVER(PARTITION BY vea.validated_signal_id, ea.SUBSTANCE_ID , ea.PT_CODE,ea.alert_configuration_id ORDER by ea.ID) rn
                      FROM EVDAS_ALERT ea
                      JOIN VALIDATED_EVDAS_ALERTS vea ON (ea.ID = vea.EVDAS_ALERT_ID)
                      JOIN VALIDATED_SIGNAL vs ON vs.ID = vea.VALIDATED_SIGNAL_ID
                      JOIN EX_EVDAS_CONFIG erc ON (erc.ID = ea.EXEC_CONFIGURATION_ID)
                      where erc.is_Deleted = 0
                      and erc.is_Enabled =1
                      and erc.adhoc_run = 0
                      and vs.ID = ${signalId}    
                       )
                group by rn
              having rn ='1'
              union all
            
          SELECT 'CASECOUNT' as abc ,count(*)
                FROM (SELECT vs.ID VSID ,row_number() OVER(PARTITION BY vea.validated_signal_id, ea.case_number , ea.product_family,ea.alert_configuration_id ORDER by ea.ID) rn
                      FROM SINGLE_CASE_ALERT ea
                      JOIN VALIDATED_SINGLE_ALERTS vea ON (ea.ID = vea.SINGLE_ALERT_ID)
                      JOIN VALIDATED_SIGNAL vs ON vs.ID = vea.VALIDATED_SIGNAL_ID
                      JOIN EX_RCONFIG erc ON (erc.id = ea.EXEC_CONFIG_ID)
                      where erc.is_Deleted = 0
                      and erc.is_Enabled =1
                      and erc.adhoc_run = 0
                      and vs.ID = ${signalId} 
                       )
                group by rn
              having rn ='1'
            
        """
    }


    static signal_concepts_map = { signalId ->
        """
      With t1 as (
           SELECT VSID,rn ,MEDICAL_CONCEPTS_ID,mname,'aggregateAlerts' as DATA_SOURCE
           FROM (SELECT aa.ID VSID,MEDICAL_CONCEPTS_ID,mc.name mname,row_number() OVER(PARTITION BY vaa.validated_signal_id, aa.PRODUCT_ID , aa.PT_CODE,aa.SOC, aa.alert_configuration_id ORDER by aa.ID) rn
                 FROM AGG_ALERT aa
                 JOIN validated_agg_alerts vaa ON (aa.ID = vaa.agg_alert_id)
                 JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID
                 JOIN AGG_SIGNAL_CONCEPTS ascs ON (aa.ID = ascs.AGG_ALERT_ID)
                 JOIN MEDICAL_CONCEPTS mc ON mc.ID = ascs.MEDICAL_CONCEPTS_ID
                 where vs.ID = ${signalId}
            
               )
           where rn = '1'
           union all
           SELECT VSID,rn ,MEDICAL_CONCEPTS_ID ,mname,'evdasAlerts' as DATA_SOURCE
                FROM (SELECT ea.ID VSID,MEDICAL_CONCEPTS_ID ,mc.name mname,row_number() OVER(PARTITION BY vea.validated_signal_id, ea.SUBSTANCE_ID , ea.PT_CODE,ea.alert_configuration_id ORDER by ea.ID) rn
                      FROM EVDAS_ALERT ea
                      JOIN VALIDATED_EVDAS_ALERTS vea ON (ea.ID = vea.EVDAS_ALERT_ID)
                      JOIN VALIDATED_SIGNAL vs ON vs.ID = vea.VALIDATED_SIGNAL_ID
                      JOIN EVDAS_SIGNAL_CONCEPTS ascs ON (ea.ID = ascs.EVDAS_ALERT_ID)
                      JOIN MEDICAL_CONCEPTS mc ON mc.ID = ascs.MEDICAL_CONCEPTS_ID
                       where vs.ID = ${signalId}
                       )
                where rn = '1'
                union all
           SELECT VSID,rn ,MEDICAL_CONCEPTS_ID ,mname,'singleCaseAlerts' as DATA_SOURCE
                FROM (SELECT ea.ID VSID,MEDICAL_CONCEPTS_ID ,mc.name mname,row_number() OVER(PARTITION BY vea.validated_signal_id, ea.product_family , ea.case_number,ea.alert_configuration_id ORDER by ea.ID) rn
                      FROM SINGLE_CASE_ALERT ea
                      JOIN VALIDATED_SINGLE_ALERTS vea ON (ea.ID = vea.SINGLE_ALERT_ID)
                      JOIN VALIDATED_SIGNAL vs ON vs.ID = vea.VALIDATED_SIGNAL_ID
                      JOIN SINGLE_SIGNAL_CONCEPTS ascs ON (ea.ID = ascs.SINGLE_CASE_ALERT_ID)
                      JOIN MEDICAL_CONCEPTS mc ON mc.ID = ascs.MEDICAL_CONCEPTS_ID
                       where vs.ID = ${signalId}
                       )
                where rn = '1'
           )
           select * from (
           select t1.mname,t1.DATA_SOURCE from t1
           )
           pivot(
              count(*) for DATA_SOURCE in ('aggregateAlerts' AggregateAlerts, 'singleCaseAlerts' SingleCaseAlerts, 'evdasAlerts'  EvdasAlerts )
           )
      
    """
    }

    static aggregateCaseAlert_attached_signals = {

        """
        SELECT DISTINCT vs.id,PRODUCT_ID ,PT_CODE 
        FROM AGG_ALERT aa
        JOIN validated_agg_alerts vaa ON (aa.ID = vaa.agg_alert_id)
        JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID
        
        """
    }

    static aggregateCaseAlert_signals_to_add = { productIdAndPtCode ->

        """
       SELECT aa.Id,PRODUCT_ID,PT_CODE
       FROM Agg_alert aa 
       LEFT JOIN validated_agg_alerts vaa ON (aa.ID = vaa.agg_alert_id)
       LEFT JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID
       where (aa.PRODUCT_ID , aa.PT_CODE) IN (${productIdAndPtCode}) 
       and vs.id is null
        
        """
    }

    static evdasCaseAlert_attached_signals = {

        """
        SELECT Distinct vs.ID VSID, SUBSTANCE_ID ,PT_CODE 
        FROM EVDAS_ALERT ea
        JOIN VALIDATED_EVDAS_ALERTS vea ON (ea.ID = vea.EVDAS_ALERT_ID)
        JOIN VALIDATED_SIGNAL vs ON vs.ID = vea.VALIDATED_SIGNAL_ID
        
        """
    }

    static evdasCaseAlert_signals_to_add = { productIdAndPtCode ->

        """
       SELECT ea.Id,SUBSTANCE_ID,PT_CODE
       FROM  EVDAS_ALERT ea 
       LEFT JOIN VALIDATED_EVDAS_ALERTS vea ON (ea.ID = vea.EVDAS_ALERT_ID)
       LEFT JOIN VALIDATED_SIGNAL vs ON vs.ID = vea.VALIDATED_SIGNAL_ID
       where (ea.SUBSTANCE_ID , ea.PT_CODE) IN (${productIdAndPtCode}) 
       and vs.id is null
        
        """
    }

    static caseAlert_dashboard_single_due_date = {Long currentUserId, Long workflowGrpId,List<Long> groupIdList ->
        """ 
        select 
        sum(case when TRUNC(aa.DUE_DATE) < TRUNC(CURRENT_TIMESTAMP) then 1 else 0 end) as PASTCOUNT,
        sum(case when TRUNC(aa.DUE_DATE) > TRUNC(CURRENT_TIMESTAMP) then 1 else 0 end) as FUTURECOUNT,
        sum(case when TRUNC(aa.DUE_DATE) = TRUNC(CURRENT_TIMESTAMP)  then 1 else 0 end) as CURRENTCOUNT
        FROM SINGLE_CASE_ALERT aa
        LEFT JOIN EX_RCONFIG rc ON (rc.id = aa.EXEC_CONFIG_ID)
        LEFT JOIN DISPOSITION disp ON (aa.disposition_id  = disp.ID)
        where (aa.ASSIGNED_TO_ID = ${currentUserId} OR aa.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")}) )
        AND rc.adhoc_run      = 0 AND rc.is_deleted = 0 AND rc.is_latest = 1  and rc.IS_ENABLED=1 AND aa.IS_CASE_SERIES = 0  
        AND disp.review_completed = 0 AND rc.workflow_group = ${workflowGrpId}
         """
    }
    static caseAlert_dashboard_agg_due_date = {Long currentUserId, Long workflowGrpId,List<Long> groupIdList ->
        """ 
        select 
        sum(case when TRUNC(aa.DUE_DATE) < TRUNC(CURRENT_TIMESTAMP) then 1 else 0 end) as PASTCOUNT,
        sum(case when TRUNC(aa.DUE_DATE) > TRUNC(CURRENT_TIMESTAMP) then 1 else 0 end) as FUTURECOUNT,
        sum(case when TRUNC(aa.DUE_DATE) = TRUNC(CURRENT_TIMESTAMP)  then 1 else 0 end) as CURRENTCOUNT
        FROM AGG_ALERT aa
        LEFT JOIN EX_RCONFIG rc ON (rc.id = aa.exec_configuration_id)
        LEFT JOIN DISPOSITION disp ON (aa.disposition_id  = disp.ID)
        where (aa.ASSIGNED_TO_ID = ${currentUserId} OR aa.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")}) )
        AND rc.adhoc_run      = 0 AND rc.is_deleted = 0 AND rc.is_latest = 1  and rc.IS_ENABLED=1
        AND disp.review_completed = 0 AND rc.workflow_group = ${workflowGrpId}
         """
    }
    static aggCaseAlert_dashboard_count_by_disposition = { Long currentUserId, Long workflowGroupId, List<Long> groupIdList ->

        """
       select disp.display_name wv ,count(aa.id) cnt
        from AGG_ALERT aa
        LEFT JOIN EX_RCONFIG rc ON (rc.id = aa.exec_configuration_id)
        LEFT JOIN DISPOSITION disp on (aa.disposition_id = disp.ID)
        where 
        (aa.ASSIGNED_TO_ID = ${currentUserId}
            OR aa.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")})
         )
        and rc.adhoc_run = 0 
        and disp.review_completed = 0 
        and rc.is_deleted = 0
        and rc.is_latest = 1
        and rc.workflow_group = ${workflowGroupId}
        group by disp.display_name,disp.id
        order by disp.id
        
        """
    }

    static aggCaseAlert_dashboard_by_disposition = { boolean isUser, Long dispositionId = null, List<Long> groupIdList = [], Long userId = null ->
        String assignedColumn = isUser ? "aa.ASSIGNED_TO_ID" : "aa.ASSIGNED_TO_GROUP_ID"
        """
        select disp.id,$assignedColumn, rc.workflow_group ,count(aa.id) cnt
        from AGG_ALERT aa
        LEFT JOIN EX_RCONFIG rc ON (rc.id = aa.exec_configuration_id)
        LEFT JOIN DISPOSITION disp on (aa.disposition_id = disp.ID)
        where
        rc.adhoc_run = 0 
        ${userId ? "AND aa.ASSIGNED_TO_ID = $userId" : ""}
        and rc.is_deleted = 0
        AND rc.is_latest = 1
        and disp.review_Completed = 0
        ${dispositionId ? "AND disp.id = $dispositionId" : ""}
        AND $assignedColumn IS NOT NULL
        ${groupIdList.size() ? "AND aa.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(',')})": ""}
        group by disp.id,$assignedColumn,rc.workflow_group
        """
    }
    static singleCaseAlert_dashboard_count_by_disposition = { Long currentUserId, Long workflowGroupId, List<Long> groupIdList ->

        """
       select disp.display_name wv ,count(sca.id) cnt
        from SINGLE_CASE_ALERT sca
        LEFT JOIN EX_RCONFIG rc ON (rc.id = sca.exec_config_id)
        LEFT JOIN DISPOSITION disp on (sca.disposition_id = disp.ID)
        where  
        (sca.ASSIGNED_TO_ID = ${currentUserId}
            OR sca.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")})
        )
        AND sca.IS_CASE_SERIES = 0
        and rc.adhoc_run = 0 
        and rc.is_deleted = 0
        AND rc.is_latest = 1
        and disp.closed = 0
        and rc.workflow_group = ${workflowGroupId}
        group by disp.display_name,disp.id
        order by disp.id
        """
    }
    static singleCaseAlert_dashboard_by_disposition = { boolean isUser, Long dispositionId = null, List<Long> groupIdList = [], Long userId = null ->
        String assignedColumn = isUser ? "sca.ASSIGNED_TO_ID" : "sca.ASSIGNED_TO_GROUP_ID"
        """
        select disp.id,$assignedColumn, rc.workflow_group ,count(sca.id) cnt
        from SINGLE_CASE_ALERT sca
        LEFT JOIN EX_RCONFIG rc ON (rc.id = sca.exec_config_id)
        LEFT JOIN DISPOSITION disp on (sca.disposition_id = disp.ID)
        where
        sca.IS_CASE_SERIES = 0
        ${userId ? "AND sca.ASSIGNED_TO_ID = $userId" : ""}
        and rc.adhoc_run = 0 
        and rc.is_deleted = 0
        AND rc.is_latest = 1
        and rc.is_enabled = 1
        and disp.review_Completed = 0
        ${dispositionId ? "AND disp.id = $dispositionId" : ""}
        AND $assignedColumn IS NOT NULL
        ${groupIdList.size() ? "AND sca.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(',')})": ""}
        group by disp.id,$assignedColumn,rc.workflow_group
        """
    }

    static singleCaseAlert_dashboard_due_date = { boolean isUser, Long dispositionId = null,List<Long> groupIdList = [], Long userId = null ->
        String assignedColumn = isUser ? "sca.ASSIGNED_TO_ID" : "sca.ASSIGNED_TO_GROUP_ID"
        """
        select to_char(TRUNC(due_date),'DD-MM-YYYY') as due_date, $assignedColumn,rc.workflow_group,count(TRUNC(due_date)) as cnt
        from SINGLE_CASE_ALERT sca
        JOIN EX_RCONFIG rc ON (rc.id = sca.exec_config_id)
        JOIN DISPOSITION disp ON (sca.disposition_id  = disp.ID)
        where  
        sca.IS_CASE_SERIES = 0
        ${userId ? "AND sca.ASSIGNED_TO_ID = $userId" : ""}
        and rc.adhoc_run = 0
        ${dispositionId ? "" : "AND disp.review_Completed = 0"}
        and rc.is_deleted = 0
        AND rc.is_latest = 1
        AND due_date is not null
        ${dispositionId ? "AND disp.id = $dispositionId" : ""}
        AND $assignedColumn IS NOT NULL
        ${groupIdList.size() ? "AND sca.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(',')})": ""}
        group by TRUNC(due_date), $assignedColumn,rc.workflow_group
        """
    }

    static aggCaseAlert_dashboard_due_date = { boolean isUser, Long dispositionId = null,List<Long> groupIdList = [], Long userId = null ->
        String assignedColumn = isUser ? "aa.ASSIGNED_TO_ID" : "aa.ASSIGNED_TO_GROUP_ID"
        """
        select to_char(TRUNC(due_date),'DD-MM-YYYY') as due_date, $assignedColumn,rc.workflow_group,count(TRUNC(due_date)) as cnt
        from AGG_ALERT aa
        JOIN EX_RCONFIG rc ON (rc.id = aa.exec_configuration_id)
        JOIN DISPOSITION disp ON (aa.disposition_id  = disp.ID)
        where  
        rc.adhoc_run = 0
        ${userId ? "AND aa.ASSIGNED_TO_ID = $userId" : ""}
        ${dispositionId ? "" : "AND disp.review_Completed = 0"}
        and rc.is_deleted = 0
        AND rc.is_latest = 1
        AND due_date is not null
        ${dispositionId ? "AND disp.id = $dispositionId" : ""}
        AND $assignedColumn IS NOT NULL
        ${groupIdList.size() ? "AND aa.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(',')})": ""}
        group by TRUNC(due_date), $assignedColumn,rc.workflow_group
        """
    }

    static dashboard_counts = { Long currentUserId, Long workflowGroupId,List<Long> groupIdList ->
        """
      SELECT 'evdas' AS evdas,COUNT(ea.id)
      FROM EVDAS_ALERT ea
      LEFT JOIN EX_EVDAS_CONFIG ec ON (ec.id = ea.exec_configuration_id)
      LEFT JOIN DISPOSITION disp ON (ea.disposition_id  = disp.ID)
      WHERE (ea.ASSIGNED_TO_ID = ${currentUserId}
            OR ea.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")})
         )
      AND ec.adhoc_run       = 0
      AND disp.review_completed = 0
      AND ec.is_deleted = 0
      AND ec.is_latest = 1
      AND ec.workflow_group = ${workflowGroupId}

        """

    }

    static aggCaseAlert_dashboard_by_status = {Long currentUserId, Long workflowGroupId, List<Long> groupIdList ->

        """
          select aa.product_name,dp.display_name as "Disposition", 
          count(distinct aa.product_id||aa.pt||aa.soc||aa.exec_configuration_id) AS "CNT"
          from   agg_alert aa 
          left join disposition dp on aa.disposition_id = dp.id 
          where dp.closed =0 and dp.REVIEW_COMPLETED=0 
            and (aa.ASSIGNED_TO_ID = ${currentUserId} OR aa.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")} )  )  
            and aa.exec_configuration_id in (select id from EX_RCONFIG where adhoc_run=0 and is_deleted=0 and is_latest=1 and workflow_group=${workflowGroupId}) 
            group by aa.product_name,dp.display_name
          order by 1
        """
    }

    static product_name_selection = {String viewName, String productColumn, String languageId ->
        "select "+ productColumn + " from "+ viewName+ " where LANG_ID = '"+ languageId + "'"
    }

    static dashboard_aggregate_counts = { Long currentUserId, Long workflowGroupId,List<Long> groupIdList ->
        """
        SELECT COUNT(aa.id) FROM AGG_ALERT aa
        LEFT JOIN EX_RCONFIG rc ON (rc.id = aa.exec_configuration_id)
        LEFT JOIN DISPOSITION disp ON (aa.disposition_id  = disp.ID)
        WHERE (aa.ASSIGNED_TO_ID = ${currentUserId} OR aa.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")}) )
        AND rc.adhoc_run = 0 AND rc.is_deleted = 0 AND rc.is_latest = 1  and rc.IS_ENABLED=1 and rc.SELECTED_DATA_SOURCE != 'jader'
        AND disp.review_completed = 0 AND rc.workflow_group = ${workflowGroupId}
        """
    }
    static dashboard_aggregate_counts2 = { Long currentUserId, Long workflowGroupId,List<Long> groupIdList ->
        """
        SELECT COUNT(aa.id) FROM AGG_ALERT aa
        LEFT JOIN EX_RCONFIG rc ON rc.id = aa.exec_configuration_id
        LEFT JOIN DISPOSITION disp ON aa.disposition_id  = disp.ID
        INNER JOIN (
        SELECT DISTINCT aa_id
        FROM (
            SELECT aa.id AS aa_id
            FROM AGG_ALERT aa
            WHERE aa.ASSIGNED_TO_ID = ${currentUserId}
            UNION
            SELECT aa.id AS aa_id
            FROM AGG_ALERT aa
            WHERE aa.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")})
            )
        ) filtered_aa ON aa.id = filtered_aa.aa_id
        WHERE rc.adhoc_run = 0 AND rc.is_deleted = 0 AND rc.is_latest = 1  and rc.IS_ENABLED=1 and rc.SELECTED_DATA_SOURCE != 'jader'
        AND disp.review_completed = 0 AND rc.workflow_group = ${workflowGroupId}
        """
    }
    static dashboard_aggregate_single_counts = { Long currentUserId, Long workflowGroupId,List<Long> groupIdList  ->
        """
        SELECT COUNT(sca.id) FROM SINGLE_CASE_ALERT sca
        LEFT JOIN EX_RCONFIG rc ON (rc.id = sca.exec_config_id)
        LEFT JOIN DISPOSITION disp ON (sca.disposition_id  = disp.ID) 
        WHERE  (sca.ASSIGNED_TO_ID = ${currentUserId} OR sca.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")}) ) 
            AND sca.IS_CASE_SERIES = 0 AND rc.adhoc_run = 0 and rc.IS_ENABLED=1
            AND rc.is_deleted = 0 AND rc.is_latest = 1  AND disp.review_completed = 0 
            AND rc.workflow_group = ${workflowGroupId}
        """
    }
    static dashboard_aggregate_single_counts2 = { Long currentUserId, Long workflowGroupId,List<Long> groupIdList  ->
        """
        SELECT COUNT(sca.id) FROM SINGLE_CASE_ALERT sca
        LEFT JOIN EX_RCONFIG rc ON rc.id = sca.exec_config_id
        LEFT JOIN DISPOSITION disp ON sca.disposition_id  = disp.ID
        INNER JOIN (
        SELECT DISTINCT sca_id
        FROM (
            SELECT sca.id AS sca_id
            FROM SINGLE_CASE_ALERT sca
            WHERE sca.ASSIGNED_TO_ID = ${currentUserId}
            UNION
            SELECT sca.id AS sca_id
            FROM SINGLE_CASE_ALERT sca
            WHERE sca.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")})
            )
        ) filtered_sca ON sca.id = filtered_sca.sca_id
        WHERE sca.IS_CASE_SERIES = 0 AND rc.adhoc_run = 0 and rc.IS_ENABLED=1
            AND rc.is_deleted = 0 AND rc.is_latest = 1  AND disp.review_completed = 0 
            AND rc.workflow_group = ${workflowGroupId}
        """
    }
    static dashboard_aggregate_evdas_counts = { Long currentUserId, Long workflowGroupId,List<Long> groupIdList  ->
        """
        SELECT COUNT(ea.id) FROM EVDAS_ALERT ea
        LEFT JOIN EX_EVDAS_CONFIG ec ON (ec.id = ea.exec_configuration_id)
        LEFT JOIN DISPOSITION disp ON (ea.disposition_id  = disp.ID) 
        WHERE (ea.ASSIGNED_TO_ID = ${currentUserId} OR ea.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")}) )
        AND ec.adhoc_run       = 0 AND ec.is_deleted = 0 AND ec.is_latest = 1 AND disp.review_completed = 0 
        AND ec.workflow_group = ${workflowGroupId}
        """
    }
    static dashboard_aggregate_evdas_counts2 = { Long currentUserId, Long workflowGroupId,List<Long> groupIdList  ->
        """
        SELECT COUNT(ea.id) FROM EVDAS_ALERT ea
        LEFT JOIN EX_EVDAS_CONFIG ec ON ec.id = ea.exec_configuration_id
        LEFT JOIN DISPOSITION disp ON ea.disposition_id  = disp.ID
        INNER JOIN (
        SELECT DISTINCT eva_id
        FROM (
            SELECT eva.id AS eva_id
            FROM EVDAS_ALERT eva
            WHERE eva.ASSIGNED_TO_ID = ${currentUserId}
            UNION
            SELECT eva.id AS eva_id
            FROM EVDAS_ALERT eva
            WHERE eva.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")})
            )
        ) filtered_eva ON ea.id = filtered_eva.eva_id
        WHERE ec.adhoc_run = 0 AND ec.is_deleted = 0 AND ec.is_latest = 1 AND disp.review_completed = 0 
        AND ec.workflow_group = ${workflowGroupId}
        """
    }

    static singleCaseAlert_dashboard_by_status = { Long currentUserId, Long workflowGroupId, List<Long> groupIdList ->

        """
          select sca.product_name,dp.display_name as "Disposition", 
          count(distinct sca.CASE_NUMBER||sca.product_family||sca.exec_config_id) AS "CNT"
          from  SINGLE_CASE_ALERT sca left join disposition dp on  sca.disposition_id = dp.id where dp.closed <> 1 and dp.REVIEW_COMPLETED=0
            and (sca.ASSIGNED_TO_ID =  ${currentUserId} OR sca.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")})) 
            and sca.IS_CASE_SERIES = 0 
            and sca.exec_config_id in (select id from EX_RCONFIG where adhoc_run=0 and is_deleted=0 and is_latest=1 and workflow_group=${workflowGroupId}) 
            group by sca.product_name,dp.display_name
          order by 1
        """
    }

    static list_agg_tag_name = {Long execConfigId,List<Long> reviewedDispIdList ->
        """
        select
          aca.id as id,
          LISTAGG(at.name, '@@@') WITHIN GROUP (ORDER BY aca.id) as tags
          from
            AGG_ALERT aca
          inner join
            DISPOSITION disposition
              on aca.disposition_id=disposition.id
          inner join
            AGG_ALERT_TAGS aaT
              on aca.id = aat.AGG_ALERT_ID
          inner join
            ALERT_TAG at
              on aat.ALERT_TAG_ID = at.id
          where
            aca.exec_configuration_id=${execConfigId}
            and not (aca.disposition_id in (${reviewedDispIdList.join(",").toString()}))    
          group by aca.id
        """
    }

    static list_single_case_tag_name = {Long execConfigId,List<Long> reviewedDispIdList ->
        """
        select
          sca.id as id,
          LISTAGG(at.tag_text, '@@@') WITHIN GROUP (ORDER BY sca.id) as tags
          from
            SINGLE_CASE_ALERT sca
          inner join
            DISPOSITION disposition
              on sca.disposition_id=disposition.id
          inner join
            SINGLE_GLOBAL_TAG_MAPPING saT
              on sca.id = sat.SINGLE_ALERT_ID
          inner join
            SINGLE_CASE_ALL_TAG at
              on sat.SINGLE_GLOBAL_ID = at.id
          where
            sca.exec_config_id=${execConfigId}
            and not (sca.disposition_id in (${reviewedDispIdList.join(",").toString()}))    
          group by sca.id
        """
    }

    static list_single_case_tag_name_bulk = {List<Long> alertIdList ->
        """
        select
           sca_id as id,
           LISTAGG(tag_text, '@@@') WITHIN GROUP (ORDER BY sca_id) as tags from (
           Select  sca.id as sca_id,at.tag_text as tag_text,row_number() over (partition by sca.id, tag_text order by sca.id) as rn
           from
            SINGLE_CASE_ALERT sca
            left join ex_rconfig ex
             on sca.EXEC_CONFIG_ID = ex.id 
          inner join
            SINGLE_CASE_ALL_TAG at
              on sca.case_id = at.case_id and (ex.PVR_CASE_SERIES_ID = at.case_Series_id or at.case_series_id is null)
          where
            sca.id in (${alertIdList.join(",").toString()})   
          ) 
          where rn = 1
          group by sca_id

        """
    }
//TODO archive check
    static update_literature_alert_level_disposition = {Map queryParameters ->
        String domainTableString = queryParameters.isArchived ? 'ARCHIVED_LITERATURE_ALERT' : 'LITERATURE_ALERT'
        String literatureAlertSql = "update ${domainTableString} set disposition_id = ${queryParameters.targetDispositionId} , disp_performed_by = '${queryParameters.dispPerformedBy}', is_disp_changed = ${queryParameters.isDispChanged} " +
        "where EX_LIT_SEARCH_CONFIG_ID = ${ queryParameters.execConfigId}" +
                "AND disposition_id not in (${queryParameters.reviewCompletedDispIdList.join(",")}) "
        literatureAlertSql
    }

    private static String getProdNameINCriteriaForSCA(List<String> allowedProducts) {
        String productNameCriteria = ""
        if (allowedProducts.size() > 1000) {
            List<List<String>> allowedProductsSubList = allowedProducts.collate(1000)
            productNameCriteria += "( sca.product_name IN (${allowedProductsSubList[0].join(",")})"
            allowedProductsSubList.remove(0)
            allowedProductsSubList.each {
                productNameCriteria += " OR sca.product_name IN (${it.join(",").toString()})"
            }
            productNameCriteria += ")"
        } else {
            productNameCriteria += "sca.product_name IN (${allowedProducts.join(",")})"
        }
        productNameCriteria
    }

    static String getProdNameINCriteria(List<String> allowedProducts) {
        String productNameCriteria = ""
        if (allowedProducts.size() > 1000) {
            List<List<String>> allowedProductsSubList = allowedProducts.collate(1000)
            productNameCriteria += "( aa.product_name IN (${allowedProductsSubList[0].join(",").toString()})"
            allowedProductsSubList.remove(0)
            allowedProductsSubList.each {
                productNameCriteria += " OR aa.product_name IN (${it.join(",").toString()})"
            }
            productNameCriteria += ")"
        } else {
            productNameCriteria += "aa.product_name IN (${allowedProducts.join(",").toString()})"
        }
        productNameCriteria
    }

    //TODO: Refactor this code to make sure that this is generically used.
    static StringBuilder getDictProdNameINCriteria(List allowedProducts, String columnName) {
        StringBuilder productNameCriteria = new StringBuilder()
        if (allowedProducts.size() > 1000) {
            List allowedProductsSubList = allowedProducts.collate(1000)
            productNameCriteria.append("( $columnName IN (${allowedProductsSubList[0].join(",").toString()})")
            allowedProductsSubList.remove(0)
            allowedProductsSubList.each {
                productNameCriteria.append(" OR $columnName IN (${it.join(",").toString()})")
            }
            productNameCriteria.append(")")
        } else {
            productNameCriteria.append("$columnName IN (${allowedProducts.join(",").toString()})")
        }
        productNameCriteria
    }

    static ebgm_strat_view_sql = {
        "SELECT JSON_VALUE(config_value,'\$.DSP_VIEW_NAME') as DSP_VIEW_NAME FROM VW_ADMIN_APP_CONFIG WHERE CONFIG_KEY LIKE '%STR_%' AND JSON_VALUE(config_value,'\$.PVS_VALUE') = '1' and APPLICATION_NAME = 'PVA-DB'" + ""
    }

    static ebgm_sub_group_view_sql = { String applicationName ->
        "SELECT JSON_VALUE(config_value,'\$.DSP_VIEW_NAME') as DSP_VIEW_NAME, JSON_VALUE(config_value,'\$.PVS_STR_COLUMN') as PVS_STR_COLUMN FROM VW_ADMIN_APP_CONFIG WHERE CONFIG_KEY LIKE '%EBGM_SUBGROUP_%' AND JSON_VALUE(config_value,'\$.PVS_VALUE') = '1' and APPLICATION_NAME = '${applicationName}'" + ""
    }
    static ebgm_other_sub_group_view_sql = {
        "SELECT JSON_VALUE(config_value,'\$.DSP_VIEW_NAME') as DSP_VIEW_NAME, JSON_VALUE(config_value,'\$.PVS_STR_COLUMN') as PVS_STR_COLUMN FROM VW_ADMIN_APP_CONFIG WHERE CONFIG_KEY LIKE '%EBGM_SUBGROUP_%' AND JSON_VALUE(config_value,'\$.PVS_VALUE') = '1' and CONFIG_KEY not in ('EBGM_SUBGROUP_AGE_GROUP','EBGM_SUBGROUP_GENDER') and APPLICATION_NAME = 'PVA-DB'" + ""
    }
    static other_sub_group_view_sql = {
        "SELECT JSON_VALUE(config_value,'\$.DSP_VIEW_NAME') as DSP_VIEW_NAME, JSON_VALUE(config_value,'\$.PVS_STR_COLUMN') as PVS_STR_COLUMN FROM VW_ADMIN_APP_CONFIG WHERE CONFIG_KEY LIKE '%PRR_SUBGROUP_%' AND JSON_VALUE(config_value,'\$.PVS_VALUE') = '1' and CONFIG_KEY not in ('EBGM_SUBGROUP_AGE_GROUP','EBGM_SUBGROUP_GENDER') and APPLICATION_NAME = 'PVA-DB'" + ""
    }
    static rel_ror_sub_group_enabled = {
        "select dbms_lob.substr(CONFIG_VALUE) as PVS_VALUE from VW_ADMIN_APP_CONFIG where CONFIG_KEY = 'PRR_ENABLE_RELATIVE_ROR' and APPLICATION_NAME = 'PVA-DB'"
    }

    static ebgm_strat_column_sql = { vwName ->
        "select * from ${vwName}"
    }

    static agg_on_demand_col_sql = {
        "select * from AGG_ON_DEM_RPT_FIELD_MAPPING where enabled = 1 order by id asc"
    }
    static distinct_advance_filter_new_column_list = { String columnName, String fieldName,String tableName,Long execConfigId ->
        "select distinct json_value($columnName, '\$.$fieldName') from $tableName where EXEC_CONFIGURATION_ID = $execConfigId"
    }
    static distinct_advance_filter_new_column_clob_list = { String columnName, String fieldName,String tableName,Long execConfigId,String term,Integer start,Integer length ->
        String values  = """
            select distinct json_value($columnName, '\$.$fieldName') from $tableName where upper(json_value($columnName, '\$.$fieldName')) like upper('%$term%')
        """
        if (execConfigId > 0) {
            values += " and exec_configuration_id=$execConfigId"
        }

        values += " OFFSET ${start} ROWS FETCH NEXT ${length} ROWS ONLY"
        values
    }
    static distinct_values_for_clob_columns = { String columnName, Long execConfigId, String joinTableName, String term,Integer start,Integer length ->
        String values = """
         select distinct DBMS_LOB.SUBSTR(alias.$columnName,32767)
          from SINGLE_CASE_ALERT alert
          inner join $joinTableName alias on alert.id=alias.SINGLE_ALERT_ID
          where   regexp_like(alias.$columnName, '$term',  'i')
          and alert.adhoc_run = 0
        """
        if (execConfigId > 0) {
            values += " and alert.exec_config_id=$execConfigId"
        }

        values += " OFFSET ${start} ROWS FETCH NEXT ${length} ROWS ONLY"
        values
    }
    static distinct_values_for_clob_columns_adhoc_alert = { String columnName, Long execConfigId, String joinTableName, String term,Integer start,Integer length ->
        String values = """
         select distinct DBMS_LOB.SUBSTR(alias.$columnName,32767)
          from SINGLE_CASE_ALERT alert
          inner join $joinTableName alias on alert.id=alias.SINGLE_ALERT_ID
          where   regexp_like(alias.$columnName, '$term',  'i')
          and alert.adhoc_run = 1
        """
        if (execConfigId > 0) {
            values += " and alert.exec_config_id=$execConfigId"
        }

        values += " OFFSET ${start} ROWS FETCH NEXT ${length} ROWS ONLY"
        values
    }

    static distinct_count_for_clob_columns = { String columnName, Long execConfigId, String joinTableName, String term ->
        String count = """
         select
          count(distinct DBMS_LOB.SUBSTR(alias.$columnName,32767))
          from SINGLE_CASE_ALERT alert
          inner join
          $joinTableName alias on alert.id=alias.SINGLE_ALERT_ID
          where regexp_like(alias.$columnName, '$term',  'i')
          and alert.adhoc_run = 0
        """
        if (execConfigId > 0) {
            count += " and alert.exec_config_id=$execConfigId"
        }
        count
    }

    static distinct_values_for_clob_on_demand_columns = { String columnName, Long execConfigId, String joinTableName, String term,Integer start,Integer length ->
        String subAliasColumn = ""
        if(columnName in ["PT", "CON_COMIT", "PRODUCT_NAME", "MED_ERROR"] ) {
            subAliasColumn = "SINGLE_ALERT_OD_ID"
        } else {
            subAliasColumn = "SINGLE_ALERT_ID"
        }
        String values = """
         select distinct DBMS_LOB.SUBSTR(alias.$columnName,32767)
          from SINGLE_ON_DEMAND_ALERT alert
          inner join $joinTableName alias on alert.id=alias.$subAliasColumn
          where   regexp_like(alias.$columnName, '$term',  'i')
           and alert.exec_config_id=$execConfigId
        """
        values += " OFFSET ${start} ROWS FETCH NEXT ${length} ROWS ONLY"
        values
    }

    static distinct_count_for_clob_on_demand_columns = { String columnName, Long execConfigId, String joinTableName, String term ->
        String subAliasColumn = ""
        if(columnName in ["PT", "CON_COMIT", "PRODUCT_NAME", "MED_ERROR"] ) {
            subAliasColumn = "SINGLE_ALERT_OD_ID"
        } else {
            subAliasColumn = "SINGLE_ALERT_ID"
        }
        String count = """
         select
          count(distinct DBMS_LOB.SUBSTR(alias.$columnName,32767))
          from SINGLE_ON_DEMAND_ALERT alert
          inner join
          $joinTableName alias on alert.id=alias.$subAliasColumn
          where regexp_like(alias.$columnName, '$term',  'i')
          and alert.exec_config_id=$execConfigId
        """
        count
    }
    static fetch_signal_list_query_product_group = { String pecProductList, boolean isAssociateClosedSignal,String productGroup ->
        String fetchQuery = ""
        if(isAssociateClosedSignal){
            fetchQuery = """
            select v1.id from VALIDATED_SIGNAL v1
            left join VALIDATED_SIGNAL_ALL_PRODUCT v2
            on v1.id = v2.VALIDATED_SIGNAL_ID
            where (v2.SIGNAL_ALL_PRODUCTS in (${pecProductList}) and v1.PRODUCTS is not null) or (v1.PRODUCT_GROUP_SELECTION ${searchLikeSql(productGroup)})
            order by v1.LAST_DISP_CHANGE desc
             """
        }else{
            fetchQuery = """
            select v1.id from VALIDATED_SIGNAL v1
            left join VALIDATED_SIGNAL_ALL_PRODUCT v2
            on v1.id = v2.VALIDATED_SIGNAL_ID
            where ((v2.SIGNAL_ALL_PRODUCTS in (${pecProductList}) and v1.PRODUCTS is not null) or (v1.PRODUCT_GROUP_SELECTION ${searchLikeSql(productGroup)}))
            and v1.SIGNAL_STATUS != 'Date Closed' order by v1.LAST_DISP_CHANGE desc
             """
        }
        fetchQuery
    }
    static fetch_signal_list_query_product = { String pecProductList, boolean isAssociateClosedSignal ->
        String fetchQuery = """
            select v1.id from VALIDATED_SIGNAL v1
            left join VALIDATED_SIGNAL_ALL_PRODUCT v2
            on v1.id = v2.VALIDATED_SIGNAL_ID
            where (${pecProductList}) in v2.SIGNAL_ALL_PRODUCTS 
        """
        if(isAssociateClosedSignal){
            fetchQuery = fetchQuery + "order by v1.LAST_DISP_CHANGE desc"
        }else{
            fetchQuery = fetchQuery + " and v1.SIGNAL_STATUS != 'Date Closed' order by v1.LAST_DISP_CHANGE desc"
        }
        fetchQuery
    }
    static final_signal_list_query_product_event = { String alertPt, String signalList, Long dispositionId ->
        String MatchQuery = """
            select v1.id from VALIDATED_SIGNAL v1
            where v1.ALL_EVENTS ${searchLikeSql(alertPt)}
            and (${signalList}) and v1.DISPOSITION_ID = ${dispositionId} and v1.EVENTS is not null
            order by v1.LAST_DISP_CHANGE desc
        """
        MatchQuery
    }
    static final_signal_list_query_product_event_all_pt_checked = { String alertPt, String signalList,Long dispositionId ->
        String MatchQuery = """
            select v1.id from VALIDATED_SIGNAL v1
            where v1.ALL_EVENTS_WITHOUT_HIERARCHY ${searchLikeSql(alertPt)}
            and (${signalList}) and v1.DISPOSITION_ID = ${dispositionId}
            order by v1.LAST_DISP_CHANGE desc
        """
        MatchQuery
    }
    static final_signal_list_query_product_event_smq_alert = { String alertPt, String signalList,Long dispositionId,boolean isSplitToPtLevel ->
        String MatchQuery = ""
        if(isSplitToPtLevel){
            MatchQuery = """
            select v1.id from VALIDATED_SIGNAL v1
            where ((v1.ALL_SMQS ${searchLikeSql(alertPt)}) or (v1.ALL_EVENTS ${searchLikeSql(alertPt)}))
            and ((${signalList}) and v1.DISPOSITION_ID = ${dispositionId})
            order by v1.LAST_DISP_CHANGE desc
        """
        }else{
            MatchQuery = """
            select v1.id from VALIDATED_SIGNAL v1
            where v1.ALL_SMQS ${searchLikeSql(alertPt)}
            and (${signalList}) and v1.DISPOSITION_ID = ${dispositionId}
            order by v1.LAST_DISP_CHANGE desc
        """
        }
        MatchQuery
    }
    static final_signal_list_query_product_event_smq_alert_event_group = { String alertPt, String signalList,Long dispositionId ->
        String MatchQuery = """
            select v1.id from VALIDATED_SIGNAL v1
            where v1.EVENT_GROUP_SELECTION like '%${alertPt}%'
            and (${signalList}) and v1.DISPOSITION_ID = ${dispositionId}
            order by v1.LAST_DISP_CHANGE desc
        """
        MatchQuery
    }
    static fetch_pt_from_smq = { Long smqId, Long termScope ->
        String MatchQuery = """
            select mpt.pt_name from meddra_smq_to_llt_pt_temp msmq
            join pvr_md_pref_term mpt on (nvl(msmq.pt_code,msmq.llt_code)=mpt.pt_code)
            where msmq.meddra_dict_id in (select MEDDRA_DICT_ID from vw_meddra_tenant_mapping) and mpt.meddra_dict_id in (select MEDDRA_DICT_ID from vw_meddra_tenant_mapping) and msmq.smq_parent = ${smqId} and msmq.term_scope = ${termScope}
        """
        MatchQuery
    }

    static fetch_pt_from_soc = { Long socCode ->
        String MatchQuery = """
            select pt_name from pvr_md_hierarchy where soc_code = ${socCode}
        """
        MatchQuery
    }

    static fetch_pt_from_hlt = { Long hltCode ->
        String MatchQuery = """
            select pt_name from pvr_md_hierarchy where hlt_code = ${hltCode}
        """
        MatchQuery
    }

    static fetch_pt_from_hlgt = { Long hlgtCode ->
        String MatchQuery = """
            select pt_name from pvr_md_hierarchy where hlgt_code = ${hlgtCode}
        """
        MatchQuery
    }
    static auto_routing_signal_name_for_audit_log = {String signalList ->
        String MatchQuery = """
            select name from VALIDATED_SIGNAL 
            where id in (${signalList})
        """
        MatchQuery
    }
    static add_activity_for_exec_config = {
        "INSERT INTO ex_rconfig_activities(EX_CONFIG_ACTIVITIES_ID,ACTIVITY_ID) VALUES(?,?)"
    }

    static add_activity_for_signal_memo = {
        "INSERT INTO VALIDATED_ALERT_ACTIVITIES(VALIDATED_SIGNAL_ID,ACTIVITY_ID) VALUES(?,?)"
    }

    static add_signalRMMs_for_signal = {
        "INSERT INTO SIGNAL_SIG_RMMS(SIG_RMM_ID,VALIDATED_SIGNAL_ID) VALUES(?,?)"
    }

    static case_history_change = { String caseAndConfigId ->
        """
         select case_number,config_id,JUSTIFICATION,CHANGE from (
               select case_number,config_id,JUSTIFICATION ,change,row_number() OVER(PARTITION BY case_number, config_id,change ORDER by last_updated desc) rn
               from case_history
               where change in ('DISPOSITION','PRIORITY') and (case_number,config_id) in ($caseAndConfigId))
               where rn = 1
         """
    }

    static product_event_history_change = { String productEventConfigIds ->
        """
         select product_name,event_name,config_id,JUSTIFICATION,DISPOSITION_ID,CHANGE from (
               select product_name,event_name,config_id,JUSTIFICATION,DISPOSITION_ID,change,row_number() OVER(PARTITION BY product_name,event_name, config_id,change ORDER by last_updated desc) rn
               from product_event_history
               where change in ('DISPOSITION','PRIORITY') and (product_name,event_name,config_id) in ($productEventConfigIds))
               where rn = 1
         """
    }

    static evdas_history_change = { Long configId ->
        """
         select product_name,event_name,config_id,JUSTIFICATION,CHANGE from (
               select product_name,event_name,config_id,JUSTIFICATION ,change,row_number() OVER(PARTITION BY product_name,event_name, config_id,change ORDER by last_updated desc) rn
               from evdas_history
               where change in ('DISPOSITION','PRIORITY') and config_id = $configId)
               where rn = 1
         """
    }

    static add_signal_for_agg_alert = { Boolean isArchived ->
        if(isArchived)
            "INSERT into VALIDATED_ARCHIVED_ACA(VALIDATED_SIGNAL_ID,ARCHIVED_ACA_ID) VALUES(?,?)"
        else
            "INSERT INTO VALIDATED_AGG_ALERTS(VALIDATED_SIGNAL_ID,AGG_ALERT_ID, DATE_CREATED) VALUES(?,?,?)"
    }

    static signal_action_count = { List signalIdList ->
        String inCriteriaWhereClause = getDictProdNameINCriteria(signalIdList, 'validated_signal_actions_id').toString()
        """
           Select validated_signal_actions_id AS SIGNALID,count(action_id) AS COUNT 
           from validated_signal_actions where $inCriteriaWhereClause
           group by validated_signal_actions_id
           """
    }

    static prev_actions_sql = { List prevAlertIdList ->
        String inCriteriaWhereClause = getDictProdNameINCriteria(prevAlertIdList, 'saa.SINGLE_CASE_ALERT_ID').toString()
        """
         SELECT SINGLE_CASE_ALERT_ID as alertId,
                 action_status as actionStatus,
                 alert_type as alertType,
                 assigned_to_id as assignedToId,
                 assigned_to_group_id as assignedToGroupId,
                 comments ,
                 completed_date as completedDate ,
                 config_id as configId ,
                 created_date as createdDate,
                 details,
                 due_date as dueDate,
                 guest_attendee_email as guestAttendeeEmail,
                 owner_id as ownerId,
                 type_id as typeId,
                 viewed
                 FROM SINGLE_ALERT_ACTIONS saa
                 INNER JOIN ACTIONS actions ON saa.ACTION_ID=actions.id
                 WHERE $inCriteriaWhereClause
         """
    }

    static quant_case_series_proc = { boolean isCumulative ->
        isCumulative ? "{call PKG_PVS_ALERT_EXECUTION.P_GET_QUANT_CASE_SERIES_CUM(?,?)}" : "{call PKG_PVS_ALERT_EXECUTION.P_GET_QUANT_CASE_SERIES_NEW(?,?)}"
    }

    static quant_last_review_sql = {exconfigIds ->
        String inCriteriaWhereClause = getDictProdNameINCriteria(exconfigIds, 'ex.id').toString()
        """
            SELECT acvt.suspect_product AS product, acvt.event_name AS event, dr.date_rng_end_absolute AS lastEndDate
            FROM ex_rconfig ex
                LEFT JOIN ex_alert_date_range dr ON ex.ex_alert_date_range_id = dr.id
                LEFT JOIN ex_rconfig_activities ex_acvt ON ex.id = ex_acvt.ex_config_activities_id
                LEFT JOIN activities acvt ON acvt.id = ex_acvt.activity_id
            WHERE $inCriteriaWhereClause
            ORDER BY ex.id DESC
        """
    }

    static qual_last_review_sql = {exconfigIds ->
        String inCriteriaWhereClause = getDictProdNameINCriteria(exconfigIds, 'ex.id').toString()
        """
            SELECT acvt.case_number AS caseNumber, dr.date_rng_end_absolute AS lastEndDate
            FROM ex_rconfig ex
                LEFT JOIN ex_alert_date_range dr ON ex.ex_alert_date_range_id = dr.id
                LEFT JOIN ex_rconfig_activities ex_acvt ON ex.id = ex_acvt.ex_config_activities_id
                LEFT JOIN activities acvt ON acvt.id = ex_acvt.activity_id
            WHERE $inCriteriaWhereClause
            ORDER BY ex.id DESC
        """
    }

    static evdas_cumulative_last_review_sql = {exconfigIds ->
        String inCriteriaWhereClause = getDictProdNameINCriteria(exconfigIds, 'ex.id').toString()
        """
            SELECT ACVT.SUSPECT_PRODUCT AS product, ACVT.EVENT_NAME AS event, EX.DATE_CREATED AS lastEndDate
            FROM EX_EVDAS_CONFIG EX
                LEFT JOIN EX_EVDAS_DATE_RANGE DR ON EX.DATE_RANGE_INFORMATION_ID = DR.ID
                LEFT JOIN EX_EVDAS_CONFIG_ACTIVITIES EX_ACTV ON EX.ID = EX_ACTV.EX_EVDAS_CONFIG_ID
                LEFT JOIN ACTIVITIES ACVT ON ACVT.ID = EX_ACTV.ACTIVITY_ID
            WHERE $inCriteriaWhereClause
            ORDER BY ex.id DESC
        """
    }

    static evdas_custom_last_review_sql = {exconfigIds ->
        String inCriteriaWhereClause = getDictProdNameINCriteria(exconfigIds, 'ex.id').toString()
        """
            SELECT ACVT.SUSPECT_PRODUCT AS product, ACVT.EVENT_NAME AS event, DR.DATE_RNG_END_ABSOLUTE AS lastEndDate
            FROM EX_EVDAS_CONFIG EX
                LEFT JOIN EX_EVDAS_DATE_RANGE DR ON EX.DATE_RANGE_INFORMATION_ID = DR.ID
                LEFT JOIN EX_EVDAS_CONFIG_ACTIVITIES EX_ACTV ON EX.ID = EX_ACTV.EX_EVDAS_CONFIG_ID
                LEFT JOIN ACTIVITIES ACVT ON ACVT.ID = EX_ACTV.ACTIVITY_ID
            WHERE $inCriteriaWhereClause
            ORDER BY ex.id DESC
        """
    }

    static list_literature_tag_name = {Long execConfigId,List<Long> reviewedDispIdList ->
        """
        select
          lit.id as id,
          LISTAGG(at.name, '@@@') WITHIN GROUP (ORDER BY lit.id) as tags
          from
            LITERATURE_ALERT lit
          inner join
            DISPOSITION disposition
              on lit.disposition_id=disposition.id
          inner join
            LITERATURE_ALERT_TAGS laT
              on lit.id = lat.LITERATURE_ALERT_ID
          inner join
            ALERT_TAG at
              on lat.ALERT_TAG_ID = at.id
          where
            lit.EX_LIT_SEARCH_CONFIG_ID=${execConfigId}
            and not (lit.disposition_id in (${reviewedDispIdList.join(",").toString()}))    
          group by lit.id
        """
    }


    static disable_all_constraints_on_table = { tableName ->
        "BEGIN " +
                "FOR c IN " +
                "(SELECT c.owner, c.table_name, c.constraint_name " +
                "FROM user_constraints c, user_tables t " +
                "WHERE c.table_name = '${tableName}' " +
                "AND c.status = 'ENABLED' " +
                "AND NOT (t.iot_type IS NOT NULL AND c.constraint_type = 'P') " +
                "ORDER BY c.constraint_type DESC) " +
                "LOOP " +
                "dbms_utility.exec_ddl_statement('alter table \"' || c.owner || '\".\"' || c.table_name || '\" disable constraint ' || c.constraint_name || ' cascade'); " +
                "END LOOP; " +
                "END; "
    }

    static enable_all_constraints_on_table = { tableName ->
        "BEGIN " +
                "FOR c IN " +
                "(SELECT c.owner, c.table_name, c.constraint_name " +
                "FROM user_constraints c, user_tables t " +
                "WHERE c.table_name = '${tableName}' " +
                "AND c.status = 'DISABLED' " +
                "ORDER BY c.constraint_type) " +
                "LOOP " +
                "dbms_utility.exec_ddl_statement('alter table \"' || c.owner || '\".\"' || c.table_name || '\" enable constraint ' || c.constraint_name ); " +
                "END LOOP; " +
                "END; "
    }

    static literature_archived_sql = { Long configId, Long exConfigId ->
        """declare
       lvc_sql VARCHAR2(32000);
       lvc_exec_sql VARCHAR2(32000);
       BEGIN
       SELECT listagg(column_name,',') within group (order by column_id) as cols
       INTO lvc_sql
       FROM USER_TAB_COLUMNS
       WHERE table_name = 'ARCHIVED_LITERATURE_ALERT';
       lvc_exec_sql := 'INSERT into ARCHIVED_LITERATURE_ALERT ('||lvc_sql||') SELECT '||lvc_sql||' FROM LITERATURE_ALERT WHERE lit_search_config_id = ${
            configId
        } and ex_lit_search_config_id = ${exConfigId}';
       execute immediate lvc_exec_sql;
      
       INSERT into VALIDATED_ARCHIVED_LIT_ALERTS(ARCHIVED_LIT_ALERT_ID,VALIDATED_SIGNAL_ID) SELECT ala.LITERATURE_ALERT_ID, ala.VALIDATED_SIGNAL_ID
       FROM VALIDATED_LITERATURE_ALERTS ala
        INNER JOIN LITERATURE_ALERT la
            ON ala.LITERATURE_ALERT_ID = la.ID
        WHERE lit_search_config_id = ${configId} and ex_lit_search_config_id = ${exConfigId};
      
       INSERT into ARCHIVED_LIT_CASE_ALERT_TAGS(ARCHIVED_LIT_ALERT_ID,PVS_ALERT_TAG_ID) SELECT ala.LITERATURE_ALERT_ID, ala.PVS_ALERT_TAG_ID
       FROM LITERATURE_CASE_ALERT_TAGS ala
        INNER JOIN LITERATURE_ALERT la
            ON ala.LITERATURE_ALERT_ID = la.ID
        WHERE lit_search_config_id = ${configId} and ex_lit_search_config_id = ${exConfigId};
      
       INSERT into ARCHIVED_LIT_ALERT_ACTIONS(ARCHIVED_LIT_ALERT_ID,ACTION_ID) SELECT ala.LITERATURE_ALERT_ID, ala.ACTION_ID
       FROM LIT_ALERT_ACTIONS ala
        INNER JOIN LITERATURE_ALERT la
            ON ala.LITERATURE_ALERT_ID = la.ID
        WHERE lit_search_config_id = ${configId} and ex_lit_search_config_id = ${exConfigId};
--      Move the attachments to Archived Literature Alert
        MERGE INTO attachment_link al
        USING (SELECT t1.reference_id as reference_id FROM attachment_link t1 left join LITERATURE_ALERT t2 on t1.reference_id = t2.id WHERE lit_search_config_id = ${
            configId
        } and ex_lit_search_config_id = ${exConfigId} and t1.reference_class='com.rxlogix.config.LiteratureAlert') conf
        ON (al.reference_id = conf.reference_id)
        WHEN matched THEN UPDATE SET al.reference_class='com.rxlogix.config.ArchivedLiteratureAlert';
      
        DELETE FROM VALIDATED_LITERATURE_ALERTS WHERE (LITERATURE_ALERT_ID) in
        (SELECT ala.LITERATURE_ALERT_ID FROM VALIDATED_LITERATURE_ALERTS ala
                INNER JOIN LITERATURE_ALERT la
            ON ala.LITERATURE_ALERT_ID = la.ID
        WHERE lit_search_config_id = ${configId} and ex_lit_search_config_id = ${exConfigId}
        );
        
        DELETE FROM LITERATURE_CASE_ALERT_TAGS WHERE (LITERATURE_ALERT_ID) in
        (SELECT ala.LITERATURE_ALERT_ID FROM LITERATURE_CASE_ALERT_TAGS ala
                INNER JOIN LITERATURE_ALERT la
            ON ala.LITERATURE_ALERT_ID = la.ID
        WHERE lit_search_config_id = ${configId} and ex_lit_search_config_id = ${exConfigId}
        );
       
        DELETE FROM LIT_ALERT_ACTIONS WHERE (LITERATURE_ALERT_ID) in
        (SELECT ala.LITERATURE_ALERT_ID FROM LIT_ALERT_ACTIONS ala
                INNER JOIN LITERATURE_ALERT la
            ON ala.LITERATURE_ALERT_ID = la.ID
        WHERE lit_search_config_id = ${configId} and ex_lit_search_config_id = ${exConfigId}
        );
        
        DELETE FROM LITERATURE_ALERT WHERE lit_search_config_id = ${configId} and ex_lit_search_config_id = ${
            exConfigId
        };
      
      exception when others
      then
      raise_application_error(-20001,sqlerrm||'->'||dbms_utility.format_error_backtrace);
      END;"""
    }

    static sca_archived_sql = { Long configId, Long exConfigId ->
        """declare
       lvc_sql VARCHAR2(32000);
       lvc_exec_sql VARCHAR2(32000);
       BEGIN
             SELECT listagg(column_name,',') within group (order by column_id) as cols
       INTO lvc_sql
       FROM USER_TAB_COLUMNS
       WHERE table_name = 'ARCHIVED_SINGLE_CASE_ALERT';
       lvc_exec_sql := 'INSERT into ARCHIVED_SINGLE_CASE_ALERT ('||lvc_sql||') SELECT '||lvc_sql||' FROM SINGLE_CASE_ALERT WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId}';
       execute immediate lvc_exec_sql;
      
       INSERT into VALIDATED_ARCHIVED_SCA(ARCHIVED_SCA_ID,VALIDATED_SIGNAL_ID) SELECT vsca.SINGLE_ALERT_ID, vsca.VALIDATED_SIGNAL_ID
       FROM VALIDATED_SINGLE_ALERTS vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
    
       INSERT into ARCHIVED_SCA_TAGS(SINGLE_ALERT_ID,PVS_ALERT_TAG_ID) SELECT vsca.SINGLE_ALERT_ID, vsca.PVS_ALERT_TAG_ID
       FROM SINGLE_CASE_ALERT_TAGS vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
     
       INSERT into ARCHIVED_SCA_PT(ARCHIVED_SCA_ID,ARCHIVED_SCA_PT,PT_LIST_IDX) SELECT vsca.SINGLE_ALERT_ID, vsca.SCA_PT, vsca.PT_LIST_IDX
       FROM SINGLE_ALERT_PT vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
     
     
       INSERT into ARCHIVED_SCA_CON_COMIT(ARCHIVED_SCA_ID,ALERT_CON_COMIT,CON_COMIT_LIST_IDX) SELECT vsca.SINGLE_ALERT_ID, vsca.ALERT_CON_COMIT, vsca.CON_COMIT_LIST_IDX
       FROM SINGLE_ALERT_CON_COMIT vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
     
       INSERT into ARCHIVED_SCA_SUSP_PROD(ARCHIVED_SCA_ID,SCA_PRODUCT_NAME,SUSPECT_PRODUCT_LIST_IDX) SELECT vsca.SINGLE_ALERT_ID, vsca.SCA_PRODUCT_NAME, vsca.SUSPECT_PRODUCT_LIST_IDX
       FROM SINGLE_ALERT_SUSP_PROD vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
      
       INSERT into ARCHIVED_SCA_MED_ERR_PT_LIST(ARCHIVED_SCA_ID,SCA_MED_ERROR,MED_ERROR_PT_LIST_IDX) SELECT vsca.SINGLE_ALERT_ID, vsca.SCA_MED_ERROR, vsca.MED_ERROR_PT_LIST_IDX
       FROM SINGLE_ALERT_MED_ERR_PT_LIST vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
     

       INSERT into ARCHIVED_SCA_ACTIONS(ARCHIVED_SCA_ID,ACTION_ID) SELECT vsca.SINGLE_CASE_ALERT_ID, vsca.ACTION_ID
       FROM SINGLE_ALERT_ACTIONS vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_CASE_ALERT_ID = sca.ID
             WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};


        INSERT into AR_SIN_ALERT_INDICATION_LIST(ARCHIVED_SCA_ID,SCA_INDICATION,indication_list_idx) 
        SELECT vsca.SINGLE_ALERT_ID, vsca.SCA_INDICATION, vsca.indication_list_idx
               FROM SINGLE_ALERT_INDICATION_LIST vsca
                INNER JOIN SINGLE_CASE_ALERT sca
                    ON vsca.SINGLE_ALERT_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};


        INSERT into AR_SIN_ALERT_CAUSE_OF_DEATH(ARCHIVED_SCA_ID,SCA_CAUSE_OF_DEATH,cause_of_death_list_idx) 
        SELECT vsca.SINGLE_ALERT_ID, vsca.SCA_CAUSE_OF_DEATH, vsca.cause_of_death_list_idx
               FROM SINGLE_ALERT_CAUSE_OF_DEATH vsca
                INNER JOIN SINGLE_CASE_ALERT sca
                    ON vsca.SINGLE_ALERT_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};

        INSERT into AR_SIN_ALERT_PAT_MED_HIST(ARCHIVED_SCA_ID,SCA_PAT_MED_HIST,patient_med_hist_list_idx) 
        SELECT vsca.SINGLE_ALERT_ID, vsca.SCA_PAT_MED_HIST, vsca.patient_med_hist_list_idx
               FROM SINGLE_ALERT_PAT_MED_HIST vsca
                INNER JOIN SINGLE_CASE_ALERT sca
                    ON vsca.SINGLE_ALERT_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};


        INSERT into AR_SIN_ALERT_PAT_HIST_DRUGS(ARCHIVED_SCA_ID,SCA_PAT_HIST_DRUGS,patient_hist_drugs_list_idx) 
        SELECT vsca.SINGLE_ALERT_ID, vsca.SCA_PAT_HIST_DRUGS, vsca.patient_hist_drugs_list_idx
               FROM SINGLE_ALERT_PAT_HIST_DRUGS vsca
                INNER JOIN SINGLE_CASE_ALERT sca
                    ON vsca.SINGLE_ALERT_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};


        INSERT into AR_SIN_ALERT_BATCH_LOT_NO(ARCHIVED_SCA_ID,SCA_BATCH_LOT_NO,batch_lot_no_list_idx) 
        SELECT vsca.SINGLE_ALERT_ID, vsca.SCA_BATCH_LOT_NO, vsca.batch_lot_no_list_idx
               FROM SINGLE_ALERT_BATCH_LOT_NO vsca
                INNER JOIN SINGLE_CASE_ALERT sca
                    ON vsca.SINGLE_ALERT_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};


        INSERT into AR_SIN_ALERT_CASE_CLASSIFI(ARCHIVED_SCA_ID,SCA_CASE_CLASSIFICATION,case_classification_list_idx) 
        SELECT vsca.SINGLE_ALERT_ID, vsca.SCA_CASE_CLASSIFICATION, vsca.case_classification_list_idx
               FROM SINGLE_ALERT_CASE_CLASSIFI vsca
                INNER JOIN SINGLE_CASE_ALERT sca
                    ON vsca.SINGLE_ALERT_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};


        INSERT into AR_SIN_ALERT_THERAPY_DATES(ARCHIVED_SCA_ID,SCA_THERAPY_DATES,therapy_dates_list_idx) 
        SELECT vsca.SINGLE_ALERT_ID, vsca.SCA_THERAPY_DATES, vsca.therapy_dates_list_idx
               FROM SINGLE_ALERT_THERAPY_DATES vsca
                INNER JOIN SINGLE_CASE_ALERT sca
                    ON vsca.SINGLE_ALERT_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};


        INSERT into AR_SIN_ALERT_DOSE_DETAILS(ARCHIVED_SCA_ID,SCA_DOSE_DETAILS,dose_details_list_idx) 
        SELECT vsca.SINGLE_ALERT_ID, vsca.SCA_DOSE_DETAILS, vsca.dose_details_list_idx
               FROM SINGLE_ALERT_DOSE_DETAILS vsca
                INNER JOIN SINGLE_CASE_ALERT sca
                    ON vsca.SINGLE_ALERT_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};

        INSERT into AR_SINGLE_ALERT_GENERIC_NAME(SINGLE_ALERT_ID,GENERIC_NAME,GENERIC_NAME_LIST_IDX) 
        SELECT vsca.SINGLE_ALERT_ID, vsca.GENERIC_NAME, vsca.GENERIC_NAME_LIST_IDX
               FROM SINGLE_ALERT_GENERIC_NAME vsca
                INNER JOIN SINGLE_CASE_ALERT sca
                    ON vsca.SINGLE_ALERT_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};

        INSERT into AR_SINGLE_ALERT_ALLPT_OUT_COME(SINGLE_ALERT_ID,ALLPTS_OUTCOME,ALLPTS_OUTCOME_LIST_IDX) 
        SELECT vsca.SINGLE_ALERT_ID, vsca.ALLPTS_OUTCOME, vsca.ALLPTS_OUTCOME_LIST_IDX
               FROM SINGLE_ALERT_ALLPT_OUT_COME vsca
                INNER JOIN SINGLE_CASE_ALERT sca
                    ON vsca.SINGLE_ALERT_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
        INSERT into ARCHIVED_SINGLE_ALERT_CROSS_REFERENCE_IND(SINGLE_ALERT_ID,CROSS_REFERENCE_IND,CROSS_REFERENCE_IND_LIST_IDX) 
        SELECT vsca.SINGLE_ALERT_ID, vsca.CROSS_REFERENCE_IND, vsca.CROSS_REFERENCE_IND_LIST_IDX
               FROM SINGLE_ALERT_CROSS_REFERENCE_IND vsca
                INNER JOIN SINGLE_CASE_ALERT sca
                    ON vsca.SINGLE_ALERT_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
     
       --      Move the attachments to Archived Single Case Alert
    
        MERGE INTO attachment_link al
        USING (SELECT t1.reference_id as reference_id FROM attachment_link t1 left join SINGLE_CASE_ALERT t2 on t1.reference_id = t2.id WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId} and t1.reference_class='com.rxlogix.signal.SingleCaseAlert') conf
        ON (al.reference_id = conf.reference_id)
        WHEN matched THEN UPDATE SET al.reference_class='com.rxlogix.signal.ArchivedSingleCaseAlert';
        UPDATE case_history
        SET archived_single_alert_id=single_alert_id,
            single_alert_id = null
        WHERE CONFIG_ID = ${configId} and EXEC_CONFIG_ID  = ${exConfigId};
     
       DELETE FROM VALIDATED_SINGLE_ALERTS WHERE (SINGLE_ALERT_ID) in (
       SELECT vsca.SINGLE_ALERT_ID
       FROM VALIDATED_SINGLE_ALERTS vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId}
        );
     
       DELETE FROM SINGLE_CASE_ALERT_TAGS WHERE (SINGLE_ALERT_ID) in (
       SELECT vsca.SINGLE_ALERT_ID
       FROM SINGLE_CASE_ALERT_TAGS vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId}
        );
      
       DELETE FROM SINGLE_ALERT_PT WHERE (SINGLE_ALERT_ID) in (
       SELECT vsca.SINGLE_ALERT_ID
       FROM SINGLE_ALERT_PT vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId}
        );
       DELETE FROM SINGLE_ALERT_CON_COMIT WHERE (SINGLE_ALERT_ID) in (
       SELECT vsca.SINGLE_ALERT_ID
       FROM SINGLE_ALERT_CON_COMIT vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId}
        );
       DELETE FROM SINGLE_ALERT_SUSP_PROD WHERE (SINGLE_ALERT_ID) in (
       SELECT vsca.SINGLE_ALERT_ID
       FROM SINGLE_ALERT_SUSP_PROD vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId}
        );
     
       DELETE FROM SINGLE_ALERT_MED_ERR_PT_LIST WHERE (SINGLE_ALERT_ID) in (
       SELECT vsca.SINGLE_ALERT_ID
       FROM SINGLE_ALERT_MED_ERR_PT_LIST vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId}
        );
        DELETE FROM SINGLE_ALERT_CROSS_REFERENCE_IND WHERE (SINGLE_ALERT_ID) in (
       SELECT vsca.SINGLE_ALERT_ID
       FROM SINGLE_ALERT_CROSS_REFERENCE_IND vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId}
        );
     
       DELETE FROM SINGLE_ALERT_ACTIONS WHERE (SINGLE_CASE_ALERT_ID) in (
       SELECT vsca.SINGLE_CASE_ALERT_ID
       FROM SINGLE_ALERT_ACTIONS vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_CASE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId});
    
        DELETE FROM single_case_alert WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
      exception when others
      then
     raise_application_error(-20001,sqlerrm||'->'||dbms_utility.format_error_backtrace);
        END;"""
    }

    static aca_archived_sql = { Long configId, Long exConfigId ->
        """declare
       lvc_sql VARCHAR2(32000);
       lvc_exec_sql VARCHAR2(32000);
       BEGIN
       SELECT listagg(column_name,',') within group (order by column_id) as cols
       INTO lvc_sql
       FROM USER_TAB_COLUMNS
       WHERE table_name = 'ARCHIVED_AGG_ALERT';
       lvc_exec_sql := 'INSERT into ARCHIVED_AGG_ALERT ('||lvc_sql||') SELECT '||lvc_sql||' FROM AGG_ALERT WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${exConfigId}';
       execute immediate lvc_exec_sql;
      
       INSERT into VALIDATED_ARCHIVED_ACA(ARCHIVED_ACA_ID,VALIDATED_SIGNAL_ID) SELECT vaca.AGG_ALERT_ID, vaca.VALIDATED_SIGNAL_ID
       FROM VALIDATED_AGG_ALERTS vaca
        INNER JOIN AGG_ALERT aca
            ON vaca.AGG_ALERT_ID = aca.ID
        WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${exConfigId};
      
       INSERT into ARCHIVED_AGG_CASE_ALERT_TAGS(AGG_ALERT_ID,PVS_ALERT_TAG_ID) SELECT vaca.AGG_ALERT_ID, vaca.PVS_ALERT_TAG_ID
       FROM AGG_CASE_ALERT_TAGS vaca
        INNER JOIN AGG_ALERT aca
            ON vaca.AGG_ALERT_ID = aca.ID
        WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${exConfigId};
     
       INSERT into ARCHIVED_ACA_ACTIONS(ARCHIVED_ACA_ID,ACTION_ID) SELECT vaca.AGG_ALERT_ID, vaca.ACTION_ID
       FROM AGG_ALERT_ACTIONS vaca
        INNER JOIN AGG_ALERT aca
            ON vaca.AGG_ALERT_ID = aca.ID
        WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${exConfigId};

--      Move the attachments to Archived Aggregate Case Alert
       
        MERGE INTO attachment_link al
        USING (SELECT t1.reference_id as reference_id FROM attachment_link t1 left join AGG_ALERT t2 on t1.reference_id = t2.id WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${exConfigId}  and t1.reference_class='com.rxlogix.signal.AggregateCaseAlert') conf
        ON (al.reference_id = conf.reference_id)
        WHEN matched THEN UPDATE SET al.reference_class='com.rxlogix.signal.ArchivedAggregateCaseAlert';
       
        UPDATE product_event_history
        SET archived_agg_case_alert_id=AGG_CASE_ALERT_ID,
            AGG_CASE_ALERT_ID = null
        WHERE CONFIG_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
      
        DELETE FROM VALIDATED_AGG_ALERTS WHERE (AGG_ALERT_ID) in (
        SELECT vaca.AGG_ALERT_ID
        FROM VALIDATED_AGG_ALERTS vaca
        INNER JOIN AGG_ALERT aca
            ON vaca.AGG_ALERT_ID = aca.ID
        WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${exConfigId}
        );
      
        DELETE FROM AGG_CASE_ALERT_TAGS WHERE (AGG_ALERT_ID) in (
        SELECT vaca.AGG_ALERT_ID
        FROM AGG_CASE_ALERT_TAGS vaca
        INNER JOIN AGG_ALERT aca
            ON vaca.AGG_ALERT_ID = aca.ID
        WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${exConfigId}
        );
     
        DELETE FROM AGG_ALERT_ACTIONS WHERE (AGG_ALERT_ID) in (
        SELECT vaca.AGG_ALERT_ID
        FROM AGG_ALERT_ACTIONS vaca
        INNER JOIN AGG_ALERT aca
            ON vaca.AGG_ALERT_ID = aca.ID
        WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${exConfigId}
        );
      
        DELETE FROM AGG_ALERT WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${exConfigId};
      exception when others
      then
      raise_application_error(-20001,sqlerrm||'->'||dbms_utility.format_error_backtrace);
        END;"""
    }

    static evdas_archived_sql = { Long configId, Long exConfigId ->
        """declare
       lvc_sql VARCHAR2(32000);
       lvc_exec_sql VARCHAR2(32000);
       BEGIN
       SELECT listagg(column_name,',') within group (order by column_id) as cols
       INTO lvc_sql
       FROM USER_TAB_COLUMNS
       WHERE table_name = 'ARCHIVED_EVDAS_ALERT';
       lvc_exec_sql := 'INSERT into ARCHIVED_EVDAS_ALERT ('||lvc_sql||') SELECT '||lvc_sql||' FROM EVDAS_ALERT WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${exConfigId}';
      execute immediate lvc_exec_sql;
     
       INSERT into VALIDATED_ARCH_EVDAS_ALERTS(ARCHIVED_EVDAS_ALERT_ID,VALIDATED_SIGNAL_ID) SELECT veva.EVDAS_ALERT_ID, veva.VALIDATED_SIGNAL_ID
       FROM VALIDATED_EVDAS_ALERTS veva
        INNER JOIN EVDAS_ALERT eva
            ON veva.EVDAS_ALERT_ID = eva.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${exConfigId};
     
       INSERT into ARCHIVED_EVDAS_ALERT_ACTIONS(ARCHIVED_EVDAS_ALERT_ID,ACTION_ID) SELECT veva.EVDAS_ALERT_ID, veva.ACTION_ID
       FROM EVDAS_ALERT_ACTIONS veva
        INNER JOIN EVDAS_ALERT eva
            ON veva.EVDAS_ALERT_ID = eva.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${exConfigId};

--      Move the attachments to Archived Evdas Alert
      
        MERGE INTO attachment_link al
        USING (SELECT t1.reference_id as reference_id FROM attachment_link t1 left join EVDAS_ALERT t2 on t1.reference_id = t2.id WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${exConfigId} and t1.reference_class='com.rxlogix.config.EvdasAlert') conf
        ON (al.reference_id = conf.reference_id)
        WHEN matched THEN UPDATE SET al.reference_class='com.rxlogix.config.ArchivedEvdasAlert';
       
        MERGE INTO evdas_history eh
        USING (SELECT t1.id as history_id, t1.EVDAS_ALERT_ID as alert_id FROM evdas_history t1 left join EVDAS_ALERT t2 on t1.EVDAS_ALERT_ID = t2.id WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${exConfigId}) conf
        ON (eh.id = conf.history_id)
        WHEN matched THEN UPDATE SET eh.ARCHIVED_EVDAS_ALERT_ID=conf.alert_id,eh.EVDAS_ALERT_ID = null;
       
        DELETE FROM VALIDATED_EVDAS_ALERTS WHERE (EVDAS_ALERT_ID) in
        (SELECT veva.EVDAS_ALERT_ID
        FROM VALIDATED_EVDAS_ALERTS veva
        INNER JOIN EVDAS_ALERT eva
            ON veva.EVDAS_ALERT_ID = eva.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${exConfigId});
       
        DELETE FROM EVDAS_ALERT_ACTIONS WHERE (EVDAS_ALERT_ID) in
        (SELECT veva.EVDAS_ALERT_ID
        FROM EVDAS_ALERT_ACTIONS veva
        INNER JOIN EVDAS_ALERT eva
            ON veva.EVDAS_ALERT_ID = eva.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${exConfigId});
      
        DELETE FROM EVDAS_ALERT WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${exConfigId};
      exception when others
      then
      raise_application_error(-20001,sqlerrm||'->'||dbms_utility.format_error_backtrace);
        END;"""
    }

    static signal_alert_ids_literature = { articleIdList, execConfigId, configId ->
        """
         with t1 as 
           (SELECT VSID,ARTICLE_ID,rn 
           FROM (SELECT vs.ID VSID, ARTICLE_ID  ,row_number() OVER(PARTITION BY la.ARTICLE_ID ,  vs.ID ORDER by la.ID) rn
                 FROM LITERATURE_ALERT la
                 JOIN VALIDATED_LITERATURE_ALERTS vla ON (la.ID = vla.LITERATURE_ALERT_ID)
                 JOIN VALIDATED_SIGNAL vs ON vs.ID = vla.VALIDATED_SIGNAL_ID
                 WHERE ARTICLE_ID IN (${articleIdList}) AND la.lit_Search_Config_ID = $configId
                 )
           where rn = '1'),
           t2 as 
                (select  la.ID alertId,ARTICLE_ID
                 FROM  LITERATURE_ALERT la
                 WHERE la.EX_LIT_SEARCH_CONFIG_ID= ${execConfigId} and ARTICLE_ID IN (${
                    articleIdList
                })
                 )
         select ALERTID,VSID from t1 join t2 on (t1.ARTICLE_ID = t2.ARTICLE_ID)
        
        """
    }

    static fetchGlobalTags = {domain ->
        """
             select tag_text as tagText , sub_tag_text as subTagText , global_Id as globalId  from pvs_global_tag where domain='${domain}'
        """
    }

    static fetchAlertTags = {domain ->
        """
             select tag_text as tagText , sub_tag_text as subTagText , alert_Id as alertId  from pvs_alert_tag where domain='${domain}'
        """
    }

    static literature_archivedAlertCat_sql = { oldExecConfigID, executedConfigId ->

        """
        SELECT
            la.id As LIT_ALERT_ID,
            at.id AS ALERT_TAG_ID
        FROM
            pvs_alert_tag               at
            INNER JOIN archived_literature_alert   ala ON ala.id = at.alert_id
            INNER JOIN literature_alert LA on ala.article_id = la.article_id
        WHERE
           ${getDictProdNameINCriteria(oldExecConfigID,'ala.ex_lit_search_config_id').toString()} and la.ex_lit_search_config_id=${executedConfigId}
        """
    }

    static aca_archivedAlertCat_sql = { oldExecConfigID, executedConfigId ->

        """
        SELECT
            agg.id   AS agg_alert_id,
            at.id    AS alert_tag_id,
            arch.disposition_id AS agg_disposition_id

        FROM
            pvs_alert_tag        at
            INNER JOIN archived_agg_alert   arch ON arch.id = at.alert_id
            INNER JOIN agg_alert            agg ON agg.product_id = arch.product_id
                                        AND agg.pt_code = arch.pt_code
        WHERE
            arch.exec_configuration_id = ${oldExecConfigID}
            AND agg.exec_configuration_id = ${executedConfigId}
         AND at.domain = 'Aggregate Case Alert'
        """
    }

    static sca_archivedAlertCat_sql = { oldExecConfigID, executedConfigId ->
        """
        SELECT
            sca.id   AS sca_alert_id,
            at.id    AS alert_tag_id,
            sca.badge AS sca_badge
        FROM
            pvs_alert_tag        at
            INNER JOIN archived_single_case_alert   arch ON arch.id = at.alert_id
            INNER JOIN single_case_alert            SCA ON sca.case_id = arch.case_id
        WHERE
            ${getDictProdNameINCriteria(oldExecConfigID,'arch.exec_config_id').toString()}
            AND sca.exec_config_id = ${executedConfigId}
            AND at.domain = 'Single Case Alert'
            """

    }

    static fetchETLCases = {
        """
            select * from CAT_CASES_REFRESH_HIST where is_Refreshed = 'N'
        """
    }

    static updateETLCases = { cases ->

        String query
        cases.eachWithIndex { caseIdString, index ->
            if (index == 0) {
                query = "UPDATE CAT_CASES_REFRESH_HIST SET is_Refreshed = 'Y' WHERE is_Refreshed = 'N' and ( CASE_ID in (" + caseIdString + ")"
            } else {
                query = query + " OR CASE_ID in (" + caseIdString + ") "
            }
        }
        query = query + ")"
        return query


    }

    static aca_allGlobalTags_sql = { configIds ->
        """
        SELECT DISTINCT
            aca.product_id   AS product_id,
            aca.pt_code    AS pt_code,
            aca.smq_code    AS smq,
            gt.tag_text AS tag
        FROM
            pvs_global_tag        gt
            INNER JOIN agg_alert   aca ON aca.global_identity_id = gt.global_id            
        WHERE
            aca.exec_configuration_id in (${configIds})
            AND gt.domain = 'Aggregate Case Alert'
            AND (gt.private_user is null OR  gt.private_user = '0')
            AND (gt.is_retained is null OR  gt.is_retained = 0)
            """
    }

    static deleteGlobalTags = { String globalCaseId ->
        """
            DELETE FROM PVS_GLOBAL_TAG WHERE GLOBAL_ID in ( ${globalCaseId} )
        """
    }

    static deleteGlobalTagsMapping = { String globalCaseId ->
        """
            DELETE FROM SINGLE_GLOBAL_TAGS WHERE GLOBAL_CASE_ID in ( ${globalCaseId} )
        """
    }

    static memo_reports_sql = { String productSelectionIds, String alertType ->
        """
            select id from ex_rconfig, json_table(PRODUCT_SELECTION,'\$.*[*]'
                                columns(ids NUMBER path '\$.id'))
                                t1 where t1.ids in ($productSelectionIds) 
                                and PRODUCT_SELECTION is not null and IS_DELETED=0 and IS_ENABLED=1 and adhoc_Run = 0 and IS_CASE_SERIES = 0
                                and type='$alertType'
                                UNION all
                                select id from ex_rconfig, json_table(PRODUCT_GROUP_SELECTION,'\$[*]'
                                columns(gids NUMBER path '\$.id'))
                                t2 where t2.gids in ($productSelectionIds) and PRODUCT_GROUP_SELECTION is not null and IS_DELETED=0 and IS_ENABLED=1 and adhoc_Run = 0 and IS_CASE_SERIES = 0
                                and type='$alertType'
        """
    }

    static aca_previousAlertTags_sql = { executedConfigId ->
        """
        SELECT
            aca.product_id   AS product_id,
            aca.pt_code    AS pt_code,
            aca.smq_code    AS smq,
            at.tag_text AS tag
        FROM
            pvs_alert_tag        at
            INNER JOIN agg_alert   aca ON aca.id = at.alert_id            
        WHERE
            at.exec_config_id = ${executedConfigId}
            AND at.domain = 'Aggregate Case Alert'
            AND (at.private_user is null OR  at.private_user = '0')
            AND (at.is_retained is null OR  at.is_retained = 0)

        UNION

        SELECT
            aca.product_id   AS product_id,
            aca.pt_code    AS pt_code,
            aca.smq_code    AS smq,
            at.tag_text AS tag
        FROM
            pvs_alert_tag        at
            INNER JOIN archived_agg_alert   aca ON aca.id = at.alert_id            
        WHERE
            at.exec_config_id = ${executedConfigId}
            AND at.domain = 'Aggregate Case Alert'
            AND (at.private_user is null OR  at.private_user = '0')
            AND (at.is_retained is null OR  at.is_retained = 0)
            """
    }

    static aca_allAlertTags_sql = { configIds ->
        """
        SELECT
            aca.product_id   AS product_id,
            aca.pt_code    AS pt_code,
            aca.smq_code    AS smq,
            at.tag_text AS tag
        FROM
            pvs_alert_tag        at
            INNER JOIN agg_alert   aca ON aca.id = at.alert_id            
        WHERE
            at.exec_config_id in (${configIds})
            AND at.domain = 'Aggregate Case Alert'
            AND (at.private_user is null OR  at.private_user = '0')
            AND (at.is_retained is null OR  at.is_retained = 0)
            
        UNION
        
        SELECT
            aca.product_id   AS product_id,
            aca.pt_code    AS pt_code,
            aca.smq_code    AS smq,
            at.tag_text AS tag
        FROM
            pvs_alert_tag        at
            INNER JOIN archived_agg_alert   aca ON aca.id = at.alert_id            
        WHERE
            at.exec_config_id in (${configIds})
            AND at.domain = 'Aggregate Case Alert'
            AND (at.private_user is null OR  at.private_user = '0')
            AND (at.is_retained is null OR  at.is_retained = 0)
            """
    }

    static configurations_firstX = { AlertDTO alertDTO, Long currentUserId , Long workflowGroupId, String groupIds ->
        """
         select * from (
             ${getConfigurations(alertDTO, currentUserId, workflowGroupId, groupIds)}
             ${getEvdasConfigurations(alertDTO, currentUserId, workflowGroupId, groupIds)} 
             ${getLiteratureConfigurations(alertDTO, currentUserId, workflowGroupId, groupIds)} 
         )
         ${searchSql(alertDTO.searchString)}  
         ${orderSql(alertDTO.sort, alertDTO.direction)}  
         OFFSET ${alertDTO.offset} ROWS FETCH NEXT ${alertDTO.max} ROWS ONLY    
        """
    }

    static count_configurations = { AlertDTO alertDTO, Long currentUserId , Long workflowGroupId, String groupIds ->
        """
         select count(*) from (
             ${getCountConfigurations(alertDTO, currentUserId, workflowGroupId, groupIds)}
             ${getCountEvdasConfigurations(alertDTO, currentUserId, workflowGroupId, groupIds)}
             ${getCountLiteratureConfigurations(alertDTO, currentUserId, workflowGroupId, groupIds)}
         )
        """
    }
    static filtered_count_configurations = {AlertDTO alertDTO, Long currentUserId , Long workflowGroupId, String groupIds ->
        """
         select count(*) from (
             ${getConfigurations(alertDTO, currentUserId, workflowGroupId, groupIds)}
             ${getEvdasConfigurations(alertDTO, currentUserId, workflowGroupId, groupIds)}
             ${getLiteratureConfigurations(alertDTO, currentUserId, workflowGroupId, groupIds)}
         )
         ${searchSql(alertDTO.searchString)}
        """
    }

    static String getConfigurations(AlertDTO alertDTO, Long currentUserId , Long workflowGroupId, String groupIds) {
        if(alertDTO.singleCaseRole || alertDTO.aggRole || SpringSecurityUtils.ifAnyGranted("ROLE_FAERS_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_VAERS_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_VIGIBASE_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_JADER_CONFIGURATION")) {
         return  """ 
           select ex1.*, case 
            when ex1.type='${Constants.AlertType.INDIVIDUAL_CASE_SERIES}' then TO_NUMBER( to_date(to_char(sca.min_due_date, 'yyyy-mm-dd'),'yyyy-mm-dd') - trunc(sysdate)) 
            when ex1.type='${Constants.AlertType.AGGREGATE_NEW}' then TO_NUMBER( to_date(to_char(aca.min_due_date, 'yyyy-mm-dd'),'yyyy-mm-dd') - trunc(sysdate)) 
            else NULL
          end as dueIn
          from (select er.id, er.name, er.adhoc_Run as adhocRun, er.date_created as dateCreated,
          TO_CLOB(er.PRODUCT_NAME) as product, TO_CLOB(er.PRODUCT_SELECTION) as productSelection, er.PRODUCT_GROUP_SELECTION as productGroupSelection, er.PRODUCT_DICTIONARY_SELECTION as productDictionarySelection, TO_CLOB(er.study_selection) as study, er.data_mining_variable as dataMiningVariable, EX_ALERT_DATE_RANGE.DATE_RNG_START_ABSOLUTE as dateRangeStart, EX_ALERT_DATE_RANGE.DATE_RNG_END_ABSOLUTE as dateRangeEnd,
          EX_ALERT_DATE_RANGE.id as dateRangeId,
          case
            when er.adhoc_run=0 then TO_NUMBER(er.REQUIRES_REVIEW_COUNT)
            else NULL
          end as requiresReview,     
          case
            when er.adhoc_run=0 then TO_NUMBER( to_date(to_char(er.review_due_date, 'yyyy-mm-dd'),'yyyy-mm-dd') - trunc(sysdate))
            else NULL
          end as dueIn2,          
          case
            when er.adhoc_Run=1 and er.type='${Constants.AlertConfigType.SINGLE_CASE_ALERT}' then '${Constants.AlertType.ICR_ADHOC}'
            when er.type='${Constants.AlertConfigType.SINGLE_CASE_ALERT}' then '${Constants.AlertType.INDIVIDUAL_CASE_SERIES}'
            when er.adhoc_Run=1 and er.type='${Constants.AlertConfigType.AGGREGATE_CASE_ALERT}' then '${Constants.AlertType.AGGREGATE_ADHOC}'
            when er.type='${Constants.AlertConfigType.AGGREGATE_CASE_ALERT}' then '${Constants.AlertType.AGGREGATE_NEW}'
          end as type
          from EX_RCONFIG er 
               JOIN EX_ALERT_DATE_RANGE on er.EX_ALERT_DATE_RANGE_ID=EX_ALERT_DATE_RANGE.id 
               where er.is_latest=1 and er.IS_ENABLED=1 and er.IS_CASE_SERIES=0
               ${roleSql(alertDTO)}
               and er.CONFIG_ID IN (${user_configuration_sql(currentUserId, workflowGroupId, groupIds, null, alertDTO.selectedFilterValues)})
               ${getReviewTypeQuery()}
               and (er.removed_users is null or er.removed_users not like '%,$currentUserId%')
          ) ex1
          LEFT JOIN (select exec_config_id, min(due_Date) as min_due_date from single_case_alert group by exec_config_id) sca on sca.exec_config_id = ex1.id and ex1.type = '${Constants.AlertType.INDIVIDUAL_CASE_SERIES}'
          LEFT JOIN (select exec_configuration_id, min(due_Date) as min_due_date from agg_alert group by exec_configuration_id) aca on aca.exec_configuration_id = ex1.id and ex1.type = '${Constants.AlertType.AGGREGATE_NEW}'
         
          
        """
        }
        ""
    }

    static String getReviewTypeQuery(){
        if(Holders.config.show.all.alerts.on.widget){
            """"""
        }else {
            """and (er.requires_review_count <> '0' OR er.adhoc_Run = 1)"""
        }
    }

    static String getCountConfigurations(AlertDTO alertDTO, Long currentUserId , Long workflowGroupId, String groupIds) {
        if(alertDTO.singleCaseRole || alertDTO.aggRole || SpringSecurityUtils.ifAnyGranted("ROLE_FAERS_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_VAERS_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_VIGIBASE_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_JADER_CONFIGURATION")) {
            return """ 
            select er.id
            from EX_RCONFIG er 
            LEFT JOIN EX_ALERT_DATE_RANGE on er.EX_ALERT_DATE_RANGE_ID=EX_ALERT_DATE_RANGE.id
            where er.is_latest=1 and er.IS_ENABLED=1 and er.IS_CASE_SERIES=0
               ${roleSql(alertDTO)}
               and er.CONFIG_ID IN (${user_configuration_sql(currentUserId, workflowGroupId, groupIds, null, [])})
               ${getReviewTypeQuery()}
               and (er.removed_users is null or er.removed_users not like '%,$currentUserId%')
        """
        }
        ""
    }

    static String getEvdasConfigurations(AlertDTO alertDTO, Long currentUserId , Long workflowGroupId, String groupIds) {
        if (alertDTO.evdasRole) {
           return  """
           ${(alertDTO.singleCaseRole || alertDTO.aggRole || SpringSecurityUtils.ifAnyGranted("ROLE_FAERS_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_VAERS_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_VIGIBASE_CONFIGURATION")) ? "UNION ALL" : ""}
           select ex2.*, TO_NUMBER( to_date(to_char(evdas.min_due_date, 'yyyy-mm-dd'),'yyyy-mm-dd') - trunc(sysdate)) as dueIn
            from (select evr.id, evr.name, evr.adhoc_Run as adhocRun, evr.date_created as dateCreated,
           TO_CLOB(evr.product_name) as product, TO_CLOB(evr.PRODUCT_SELECTION) as productSelection, evr.PRODUCT_GROUP_SELECTION as productGroupSelection, null as productDictionarySelection, null as study,null as dataMiningVariable, EX_EVDAS_DATE_RANGE.DATE_RNG_START_ABSOLUTE as dateRangeStart, EX_EVDAS_DATE_RANGE.DATE_RNG_END_ABSOLUTE as dateRangeEnd,
           EX_EVDAS_DATE_RANGE.id as dateRangeId,
          case
            when adhoc_run=0 then TO_NUMBER(evr.REQUIRES_REVIEW_COUNT)
            else NULL
          end as requiresReview,      
          case
            when adhoc_run =0 then TO_NUMBER( to_date(to_char(evr.review_due_date, 'yyyy-mm-dd'),'yyyy-mm-dd') - trunc(sysdate))
            else NULL
          end as dueIn2,            
          case
            when adhoc_Run=1 then '${Constants.AlertType.EVDAS_ADHOC}'
            else '${Constants.AlertType.EVDAS}'
          end as type
            from EX_EVDAS_CONFIG evr
            JOIN EX_EVDAS_DATE_RANGE on evr.DATE_RANGE_INFORMATION_ID=EX_EVDAS_DATE_RANGE.id
             where evr.is_latest=1 and evr.IS_ENABLED=1 
            and evr.CONFIG_ID IN (${evdas_configuration_sql(currentUserId, workflowGroupId, groupIds, alertDTO.selectedFilterValues)})
            ${getReviewTypeQueryForEvdas()}
            and (evr.removed_users is null or evr.removed_users not like '%,$currentUserId%')
           ) ex2
           LEFT JOIN (select exec_configuration_id, min(due_Date) as min_due_date from EVDAS_ALERT group by exec_configuration_id) evdas on evdas.exec_configuration_id = ex2.id

          
        """
        }
        ""
    }

    static String getReviewTypeQueryForEvdas(){
        if(Holders.config.show.all.alerts.on.widget){
            """"""
        }else {
            """and (evr.requires_review_count <> '0' OR evr.adhoc_Run = 1)"""
        }
    }

    static String getCountEvdasConfigurations(AlertDTO alertDTO, Long currentUserId , Long workflowGroupId, String groupIds) {
        if (alertDTO.evdasRole) {
            return """
           ${(alertDTO.singleCaseRole || alertDTO.aggRole || SpringSecurityUtils.ifAnyGranted("ROLE_FAERS_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_VAERS_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_VIGIBASE_CONFIGURATION")) ? "UNION ALL" : ""}
           select evr.id
            from EX_EVDAS_CONFIG evr 
            LEFT JOIN EVDAS_DATE_RANGE on evr.DATE_RANGE_INFORMATION_ID=EVDAS_DATE_RANGE.id
            where evr.is_latest=1 and evr.IS_ENABLED=1
            and evr.CONFIG_ID IN (${evdas_configuration_sql(currentUserId, workflowGroupId, groupIds, [])})            
            ${getReviewTypeQueryForEvdas()}
            and (evr.removed_users is null or evr.removed_users not like '%,$currentUserId%')
        """
        }
        ""
    }

    static String getCountLiteratureConfigurations(AlertDTO alertDTO, Long currentUserId , Long workflowGroupId, String groupIds) {
        if(alertDTO.literatureRole) {
            return """
            ${(alertDTO.singleCaseRole || alertDTO.aggRole || alertDTO.evdasRole || SpringSecurityUtils.ifAnyGranted("ROLE_FAERS_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_VAERS_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_VIGIBASE_CONFIGURATION")) ? "UNION ALL" : ""}
            select elit.id
            from EX_LITERATURE_CONFIG elit 
            LEFT JOIN EX_LITERATURE_DATE_RANGE ON elit.DATE_RANGE_INFORMATION_ID=EX_LITERATURE_DATE_RANGE.id 
            where elit.is_latest=1 and elit.IS_ENABLED=1 
            and elit.CONFIG_ID IN (${literature_configuration_sql(currentUserId, workflowGroupId, groupIds, [])})
            ${getReviewTypeQueryForLit()}
            and (elit.removed_users is null or elit.removed_users not like '%,$currentUserId%')
        """
        }
        ""
    }
    static String getReviewTypeQueryForLit(){
        if(Holders.config.show.all.alerts.on.widget){
            """"""
        }else {
            """and elit.requires_review_count <> '0'"""
        }
    }

    static String getLiteratureConfigurations(AlertDTO alertDTO, Long currentUserId , Long workflowGroupId, String groupIds) {
        if(alertDTO.literatureRole) {
         return """
           ${(alertDTO.singleCaseRole || alertDTO.aggRole || alertDTO.evdasRole || SpringSecurityUtils.ifAnyGranted("ROLE_FAERS_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_VAERS_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_VIGIBASE_CONFIGURATION")) ? "UNION ALL" : ""}
            select ex3.*, NULL as dueIn
            from (select elit.id, name, 0 as adhocRun, elit.date_created as dateCreated,
            TO_CLOB(elit.PRODUCT_NAME) product,TO_CLOB(elit.PRODUCT_SELECTION) as productSelection, elit.PRODUCT_GROUP_SELECTION as productGroupSelection, null as productDictionarySelection, null as study,null as dataMiningVariable, EX_LITERATURE_DATE_RANGE.DATE_RNG_START_ABSOLUTE as dateRangeStart, EX_LITERATURE_DATE_RANGE.DATE_RNG_END_ABSOLUTE as dateRangeEnd,
            EX_LITERATURE_DATE_RANGE.id as dateRangeId,
            TO_NUMBER(elit.REQUIRES_REVIEW_COUNT) as requiresReview,   
            NULL as dueIn2,
            '${Constants.AlertType.LITERATURE}' as type
            from EX_LITERATURE_CONFIG elit 
            JOIN EX_LITERATURE_DATE_RANGE ON elit.DATE_RANGE_INFORMATION_ID=EX_LITERATURE_DATE_RANGE.id 
            where elit.is_latest=1 and elit.IS_ENABLED=1 
             and elit.CONFIG_ID IN (${literature_configuration_sql(currentUserId, workflowGroupId, groupIds, alertDTO.selectedFilterValues)})
             ${getReviewTypeQueryForLit()}
             and (elit.removed_users is null or elit.removed_users not like '%,$currentUserId%')
           ) ex3
           
        """
        }
        ""
    }

    static String saveUserInfoInPvUserWebappTable(Long id, String email, String fullName, String username, String updatedByUsername, Date date){
        String sqlStatement = """
            INSERT INTO PVUSER_WEBAPP 
            (ID,EMAIL,FULL_NAME,USERNAME,LST_INS_UPD_USR,LST_INS_UPD_DATE)
            VALUES
            (${id},'${email}','${fullName}','${username}','${updatedByUsername}','${date.format(DateUtil.DATEPICKER_FORMAT_AM_PM_3)}')
        """
        return sqlStatement
    }
    static String saveGroupInfoInGroupsWebappTable(Long id, String name, String description,String groupType, String updatedByUsername, Boolean isActive, Date date){
        String sqlStatement = """
            INSERT INTO GROUPS_WEBAPP 
            (ID,NAME,DESCRIPTION,GROUP_TYPE,LST_INS_UPD_USR,IS_ACTIVE,LST_INS_UPD_DATE)
            VALUES
            (${id},'${name.replaceAll("'","''")}','${description?.replaceAll("'","''")}','${groupType}','${updatedByUsername}',${isActive?1:0},'${date.format(DateUtil.DATEPICKER_FORMAT_AM_PM_3)}')
        """
        return sqlStatement
    }
    static String updateUserInfoInPvUserWebappTable(Long id, String email, String fullName, String username, String updatedByUsername, Date date){
        String sqlStatement = """
            UPDATE PVUSER_WEBAPP 
            SET EMAIL = '${email}',
                FULL_NAME = '${fullName}',
                USERNAME = '${username}',
                LST_INS_UPD_USR = '${updatedByUsername}',
                LST_INS_UPD_DATE = '${date.format(DateUtil.DATEPICKER_FORMAT_AM_PM_3)}'
            WHERE
            ID = ${id}
        """
        return sqlStatement
    }

    static String updateGroupInfoInGroupsWebappTable(Long id, String name, String description,String groupType, String updatedByUsername, Boolean isActive, Date date){
        String sqlStatement = """
            UPDATE GROUPS_WEBAPP 
            SET NAME = '${name.replaceAll("'", "''")}',
                DESCRIPTION= '${description?.replaceAll("'", "''")}',
                GROUP_TYPE = '${groupType}',
                LST_INS_UPD_USR = '${updatedByUsername}',
                IS_ACTIVE = ${isActive?1:0},
                LST_INS_UPD_DATE = '${date.format(DateUtil.DATEPICKER_FORMAT_AM_PM_3)}'
            WHERE
            id = ${id}
        """
        return sqlStatement
    }

    static deleteUserInfoFRomPvUserWebApp(Long id){
        String sqlStatement = """DELETE FROM PVUSER_WEBAPP WHERE ID = ${id}"""
        return sqlStatement
    }
    static deleteGroupInfoFRomGroupsWebApp(Long id){
        String sqlStatement = """DELETE FROM GROUPS_WEBAPP WHERE ID = ${id}"""
        return sqlStatement
    }

    static String migrateAllUsersToPvUserWebappTable(List userList, Date date){
        String insertStatement = "Begin"
        userList.each {
            insertStatement += """ Insert into PVUSER_WEBAPP (ID,EMAIL,FULL_NAME,USERNAME,LST_INS_UPD_DATE) VALUES 
                                (${it.id},'${it.email}','${it.fullName}','${it.username}','${date.format(DateUtil.DATEPICKER_FORMAT_AM_PM_3)}');"""
        }
        insertStatement += "End;"
        return insertStatement
    }
    static String migrateAllGroupsToGroupsWebappTable(List groupList, Date date){
        String insertStatement = "Begin"
        groupList.each {
            insertStatement += """ Insert into GROUPS_WEBAPP (ID,NAME,DESCRIPTION,GROUP_TYPE,IS_ACTIVE,LST_INS_UPD_DATE) VALUES 
                                (${it.id},'${it.name.replaceAll("'", "''")}','${it.description?.replaceAll("'", "''")}','${it.groupType}','${it.isActive?1:0}','${date.format(DateUtil.DATEPICKER_FORMAT_AM_PM_3)}');"""
        }
        insertStatement += "End;"
        return insertStatement
    }

    static user_view_product_search_sql = { String searchString ->
        """
            select assignment.id from user_view_assignment assignment, json_table(PRODUCTS,'\$[*]'
                                                                       columns(name VARCHAR2 path '\$.name')) 
                                                                        t1 where UPPER(t1.name) LIKE UPPER('%${searchString}%')
        """
    }

    static String createFilterQueryLiterature(List<String> filterWithUsersAndGroups ) {
        String filterAlertsQuery = ""
        if(filterWithUsersAndGroups.size()) {
            filterAlertsQuery = " and ( "
            filterWithUsersAndGroups.each {it ->
                if(it.contains("User_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.ASSIGNED_TO_ID=${extractedId} or "
                }
                else if(it.contains("UserGroup_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.ASSIGNED_TO_GROUP_ID=${extractedId} or "
                }
                else if(it.contains("Mine_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.PVUSER_ID=${extractedId} or "
                }
                else if(it.contains("AssignToMe_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.ASSIGNED_TO_ID=${extractedId} or "
                    User loggedInUser = User.findById(extractedId as Long)
                    Set<Group> allGroupsContainsUser = Group.findAllUserGroupByUser(loggedInUser)
                    allGroupsContainsUser.each {
                        filterAlertsQuery += "this_.ASSIGNED_TO_GROUP_ID=${it.id} or "
                    }
                }
                else if(it.contains("SharedWithMe_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "sharewithu1_.id=${extractedId} or "
                    User loggedInUser = User.findById(extractedId as Long)
                    Set<Group> allGroupsContainsUser = Group.findAllUserGroupByUser(loggedInUser)
                    allGroupsContainsUser.each {
                        filterAlertsQuery += "sharewithg2_.id=${it.id} or "
                    }
                }
            }
            filterAlertsQuery = filterAlertsQuery.substring(0,filterAlertsQuery.length()-3)
            filterAlertsQuery += " )"
        }
        filterAlertsQuery
    }

    static String createFilterQuery(List<String> filterWithUsersAndGroups ) {
        String filterAlertsQuery = ""
        if(filterWithUsersAndGroups.size()) {
            filterAlertsQuery = " and ( "
            filterWithUsersAndGroups.each {it ->
                if(it.contains("User_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.ASSIGNED_TO_ID=${extractedId} or "
                }
                else if(it.contains("UserGroup_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.ASSIGNED_TO_GROUP_ID=${extractedId} or "
                }
                else if(it.contains("Mine_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.PVUSER_ID=${extractedId} or "
                }
                else if(it.contains("AssignToMe_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.ASSIGNED_TO_ID=${extractedId} or "
                    User loggedInUser = User.findById(extractedId as Long)
                    Set<Group> allGroupsContainsUser = Group.findAllUserGroupByUser(loggedInUser)
                    allGroupsContainsUser.each {
                        filterAlertsQuery += "this_.ASSIGNED_TO_GROUP_ID=${it.id} or "
                    }
                }
                else if(it.contains("SharedWithMe_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "sharewithu1_.id=${extractedId} or autosharew3_.id=${extractedId} or "
                    User loggedInUser = User.findById(extractedId as Long)
                    Set<Group> allGroupsContainsUser = Group.findAllUserGroupByUser(loggedInUser)
                    allGroupsContainsUser.each {
                        filterAlertsQuery += "sharewithg2_.id=${it.id} or autosharew4_.id=${it.id} or "
                    }
                }
            }
            filterAlertsQuery = filterAlertsQuery.substring(0,filterAlertsQuery.length()-3)
            filterAlertsQuery += " )"
        }
        filterAlertsQuery
    }

    static user_configuration_sql = { Long userId, Long workflowGroupId, String groupIds, String alertType= null, List<String> filterWithUsersAndGroups=[] ->
        """
    select
        this_.id as y0_ 
    from
        RCONFIG this_ 

    left outer join
        AUTO_SHARE_WITH_GROUP_CONFIG autosharew6_ 
            on this_.id=autosharew6_.CONFIG_ID 
    left outer join
        GROUPS autosharew4_ 
            on autosharew6_.AUTO_SHARE_WITH_GROUPID=autosharew4_.id 
    left outer join
        AUTO_SHARE_WITH_USER_CONFIG autosharew8_ 
            on this_.id=autosharew8_.CONFIG_ID 
    left outer join
        PVUSER autosharew3_ 
            on autosharew8_.AUTO_SHARE_WITH_USERID=autosharew3_.id 
    left outer join
        SHARE_WITH_GROUP_CONFIG sharewithg10_ 
            on this_.id=sharewithg10_.CONFIG_ID 
    left outer join
        GROUPS sharewithg2_ 
            on sharewithg10_.SHARE_WITH_GROUPID=sharewithg2_.id 
    left outer join
        SHARE_WITH_USER_CONFIG sharewithu12_ 
            on this_.id=sharewithu12_.CONFIG_ID 
    left outer join
        PVUSER sharewithu1_ 
            on sharewithu12_.SHARE_WITH_USERID=sharewithu1_.id 
    where
        (
          ( sharewithu1_.id=$userId 
            or autosharew3_.id=$userId
            or this_.PVUSER_ID=$userId
            ${groupSql(groupIds)} 
            ${autoGroupSql(groupIds)} )
            ${createFilterQuery(filterWithUsersAndGroups)}
        ) 
        ${alertType ? "and this_.type='$alertType' " : ""} 
        and this_.WORKFLOW_GROUP=$workflowGroupId
        """
    }

    static createfilterQueryForEvdas(List<String> filterWithUsersAndGroups) {
        String filterAlertsQuery = ""
        if(filterWithUsersAndGroups.size()) {
            filterAlertsQuery = " and ( "
            filterWithUsersAndGroups.each {it ->
                if(it.contains("User_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.ASSIGNED_TO_ID=${extractedId} or "
                }
                else if(it.contains("UserGroup_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.ASSIGNED_TO_GROUP_ID=${extractedId} or "
                }
                else if(it.contains("Mine_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.OWNER_ID=${extractedId} or "
                }
                else if(it.contains("AssignToMe_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.ASSIGNED_TO_ID=${extractedId} or "
                    User loggedInUser = User.findById(extractedId as Long)
                    Set<Group> allGroupsContainsUser = Group.findAllUserGroupByUser(loggedInUser)
                    allGroupsContainsUser.each {
                        filterAlertsQuery += "this_.ASSIGNED_TO_GROUP_ID=${it.id} or "
                    }
                }
                else if(it.contains("SharedWithMe_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "sharewithu1_.id=${extractedId} or "
                    User loggedInUser = User.findById(extractedId as Long)
                    Set<Group> allGroupsContainsUser = Group.findAllUserGroupByUser(loggedInUser)
                    allGroupsContainsUser.each {
                        filterAlertsQuery += "sharewithg2_.id=${it.id} or "
                    }
                }
            }
            filterAlertsQuery = filterAlertsQuery.substring(0,filterAlertsQuery.length()-3)
            filterAlertsQuery += " )"
        }
        filterAlertsQuery
    }

    static evdas_configuration_sql = { Long userId, Long workflowGroupId, String groupIds, List<String> selectedFilterValues=[] ->
        """
        select
         this_.id as y0_ 
    from
        EVDAS_CONFIG this_ 
    left outer join
        SHARE_WITH_GROUP_EVDAS_CONFIG sharewithg4_ 
            on this_.id=sharewithg4_.CONFIG_ID 
    left outer join
        GROUPS sharewithg2_ 
            on sharewithg4_.SHARE_WITH_GROUPID=sharewithg2_.id 
    left outer join
        SHARE_WITH_USER_EVDAS_CONFIG sharewithu6_ 
            on this_.id=sharewithu6_.CONFIG_ID 
    left outer join
        PVUSER sharewithu1_ 
            on sharewithu6_.SHARE_WITH_USERID=sharewithu1_.id 
    where
        (
          ( sharewithu1_.id=$userId
            or this_.OWNER_ID=$userId
            ${groupSql(groupIds)} )
            ${createfilterQueryForEvdas(selectedFilterValues)}
        ) 
        and this_.WORKFLOW_GROUP=$workflowGroupId

        """
    }

    static literature_configuration_sql = { Long userId, Long workflowGroupId, String groupIds, List<String> filterWithUsersAndGroups=[] ->
        """
        select
        this_.id as y0_ 
    from
        LITERATURE_CONFIG this_ 
    left outer join
        SHARE_WITH_GROUP_LITR_CONFIG sharewithg4_ 
            on this_.id=sharewithg4_.CONFIG_ID 
    left outer join
        GROUPS sharewithg2_ 
            on sharewithg4_.SHARE_WITH_GROUPID=sharewithg2_.id 
    left outer join
        SHARE_WITH_USER_LITR_CONFIG sharewithu6_ 
            on this_.id=sharewithu6_.CONFIG_ID 
    left outer join
        PVUSER sharewithu1_ 
            on sharewithu6_.SHARE_WITH_USERID=sharewithu1_.id 
    where
        (
          ( sharewithu1_.id=$userId
            or this_.PVUSER_ID=$userId
            ${groupSql(groupIds)} )
            ${createFilterQueryLiterature(filterWithUsersAndGroups)}
        ) 
        and this_.workflow_group_id=$workflowGroupId

        """
    }

    static String orderSql(String sort, String direction) {
        if(!sort) {
            "ORDER BY dateCreated desc NULLS LAST"
        } else if(sort in ["name", "type"]) {
            "ORDER BY UPPER($sort) $direction NULLS LAST"
        } else if(sort.equals("dateRange")) {
            "ORDER BY DATERANGESTART $direction, DATERANGEEND $direction"
        } else if (sort.equals("product")){
            "ORDER BY UPPER(dbms_lob.substr($sort)) $direction NULLS LAST"
        } else {
            "ORDER BY $sort $direction NULLS LAST"
        }
    }
    static String searchLikeSql(String searchString){
        if(searchString) {
            String esc_char = ""
            if (searchString.contains('_')) {
                searchString = searchString.replaceAll("\\_", "!_%")
                esc_char = "!"
            } else if (searchString.contains('%')) {
                searchString = searchString.replaceAll("\\%", "!%%")
                esc_char = "!"
            }
            if (esc_char) {
                return """
                 like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'
                """
            } else {
                return """
                 like '%${searchString.replaceAll("'", "''")}%'
            """
            }
        }
        ""
    }
    static String searchSql(String searchString) {
        if(searchString) {
            searchString = searchString.toLowerCase()
            String esc_char = ""
            if (searchString.contains('_')) {
                searchString = searchString.replaceAll("\\_", "!_%")
                esc_char = "!"
            } else if (searchString.contains('%')) {
                searchString = searchString.replaceAll("\\%", "!%%")
                esc_char = "!"
            }
            if (esc_char) {
                // TO DO:-search on study selection for more than one study selected
                return """
                 where lower(name) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'
                 or (select lower(replace(regexp_replace(TO_CLOB(product)||',','\\([0-9]+\\)\\s*\\([A-Z|[:space:]]+\\),',',',1,0,'i'),',','')) from dual) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'
                 or lower(type) like '%${searchString.replaceAll("'", "''")}%'
                 or dueIn like '%${searchString.replaceAll("'", "''")}%'
                 or requiresReview like '%${searchString.replaceAll("'", "''")}%'
                 or lower(dbms_lob.substr(TO_CLOB(study),dbms_lob.getlength(TO_CLOB(study)), 1)) like '%${searchString.replaceAll("'", "''")}%'
                 or lower(concat(concat(concat(dataMiningVariable,'('),(select lower(replace(regexp_replace(TO_CLOB(product)||',','\\([0-9]+\\)\\s*\\([A-Z|[:space:]]+\\),',',',1,0,'i'),',','')) from dual)),')')) like '%${searchString.replaceAll("'", "''")}%'
                """
            } else {
                // TO DO:-search on study selection for more than one study selected
                return """
                 where lower(name) like '%${searchString.replaceAll("'", "''")}%'
                 or (select lower(replace(regexp_replace(TO_CLOB(product)||',','\\([0-9]+\\)\\s*\\([A-Z|[:space:]]+\\),',',',1,0,'i'),',','')) from dual) like '%${searchString.replaceAll("'", "''")}%'
                 or lower(type) like '%${searchString.replaceAll("'", "''")}%'
                 or lower(dbms_lob.substr(TO_CLOB(study),dbms_lob.getlength(TO_CLOB(study)), 1)) like '%${searchString.replaceAll("'", "''")}%'
                 or dueIn like '%${searchString.replaceAll("'", "''")}%'
                 or requiresReview like '%${searchString.replaceAll("'", "''")}%'
                 or lower(concat(concat(concat(dataMiningVariable,'('),(select lower(replace(regexp_replace(TO_CLOB(product)||',','\\([0-9]+\\)\\s*\\([A-Z|[:space:]]+\\),',',',1,0,'i'),',','')) from dual)),')')) like '%${searchString.replaceAll("'", "''")}%'
            """
            }
        }
        ""
    }

    static String groupSql(String groupIds){
        if(groupIds) {
            return """
            or (
                sharewithg2_.id in (
                    $groupIds
                )
            ) 
            """
        }
        ""
    }

    static String autoGroupSql(String groupIds){
        if(groupIds) {
            return """
            or (
                autosharew4_.id in (
                    $groupIds
                )
            ) 
            """
        }
        ""
    }

    static String roleSql(AlertDTO alertDTO) {
        String roles = ""
        if (!alertDTO.singleCaseRole && !alertDTO.aggRole && SpringSecurityUtils.ifAnyGranted("ROLE_FAERS_CONFIGURATION")) {
            roles += appendRole(roles)
            roles += """ (er.type='Aggregate Case Alert' AND er.SELECTED_DATA_SOURCE like '%faers%') """
        }
        if (!alertDTO.singleCaseRole && !alertDTO.aggRole && SpringSecurityUtils.ifAnyGranted("ROLE_VAERS_CONFIGURATION")) {
            roles += appendRole(roles)
            roles += """ (er.type='Aggregate Case Alert' AND er.SELECTED_DATA_SOURCE like '%vaers%') """
        }
        if (!alertDTO.singleCaseRole && !alertDTO.aggRole && SpringSecurityUtils.ifAnyGranted("ROLE_VIGIBASE_CONFIGURATION")) {
            roles += appendRole(roles)
            roles += """ (er.type='Aggregate Case Alert' AND er.SELECTED_DATA_SOURCE like '%vigibase%') """
        }
        if (!alertDTO.singleCaseRole && !alertDTO.aggRole && SpringSecurityUtils.ifAnyGranted("ROLE_JADER_CONFIGURATION")) {
            roles += appendRole(roles)
            roles += """ (er.type='Aggregate Case Alert' AND er.SELECTED_DATA_SOURCE like '%jader%') """
        }
        if (!alertDTO.singleCaseRole && alertDTO.aggRole) {
            roles += appendRole(roles)
            roles += """ (er.type='Aggregate Case Alert') """
        } else if(alertDTO.singleCaseRole && !alertDTO.aggRole) {
            roles += appendRole(roles)
            roles += """ (er.type='Single Case Alert') """
        }
        if (roles != "") {
            return roles += " )"
        } else {
            return roles
        }
    }

    static String appendRole(String role) {
        String roles = ""
        if (role == "") {
            roles = "AND ( "
        } else {
            roles = "OR "
        }
        return roles
    }

    static fetchCaseComments = {
        """
            select TENANT_ID, CASE_NUMBER, VERSION_NUM, CASE_ID, FOLLOW_UP_NUM,
            COMMENT_TXT, CONFIG_ID, EXECUTION_ID, CREATED_BY, CREATED_DATE, UPDATED_BY, UPDATED_DATE, ALERT_NAME,
            PRODUCT_FAMILY, PRODUCT_NAME, PRODUCT_ID, EVENT_NAME, PT_CODE 
            from vw_alert_comment
        """
    }

    static executeMigrationScript = {
        """
            "{call PKG_COMMENT.P_PVS_COMMENTS_MIG()}"
        """
    }

    static insert_gtt_cumulative_case_series = { Long execConfigId ->
        """
            insert into GTT_FILTER_KEY_VALUES(CODE,TEXT)
            Select version_num, case_num from 
            ALRT_QRY_CASELST_QLC_$execConfigId
        """
    }

    static primary_suspect_sql = {
        """
              BEGIN
                   delete from GTT_QUERY_DETAILS;
                   delete from GTT_QUERY_SETS;
                   delete from GTT_REPORT_VAR_INPUT;
               INSERT INTO GTT_QUERY_SETS (SET_ID,CUSTOM_SQL,SET_OPERATOR,GROUP_ID,QUERY_FLAG,PARENT_GROUP_ID) values (1,null,null,0, 2,0);
               INSERT INTO GTT_QUERY_DETAILS (SET_ID,FIELD_ID,JAVA_VARIABLE,FIELD_OPERATOR,FIELD_VALUES,GROUP_ID,GROUP_OPERATOR,CUSTOM_INPUT,PARENT_GROUP_ID) values (1,null,null,null, null,1,'AND',null,0);
               INSERT INTO GTT_QUERY_DETAILS (SET_ID,FIELD_ID,JAVA_VARIABLE,FIELD_OPERATOR,FIELD_VALUES,GROUP_ID,GROUP_OPERATOR,CUSTOM_INPUT,PARENT_GROUP_ID,ADDL_PARAMS, IS_FIELD_COMPARE) values (1,1,'eventFirstEvent','EQUALS', '''Yes''',1,null,null,1,null, 0);
               END;
                    """
    }

    static deleted_cases_sql = {dataSource->
        "select to_number(JSON_VALUE(CONFIG_VALUE,'\$.KEY_VALUE')) as KEY_VALUE from VW_ADMIN_APP_CONFIG where CONFIG_KEY = 'ENABLE_REMOVE_DELETED_CASES' and APPLICATION_NAME ='${dataSource}'"
    }

    static case_history_sql = { String execConfigIds, String caseNumbers ->
        """
         select exec_config_id as execConfigId ,case_number as caseNumber,justification from (
              select exec_config_id,case_number,justification, 
              row_number() over (partition by exec_Config_Id, case_number order by last_updated desc) RNum
              from case_history
              where exec_config_id in ($execConfigIds) and case_number in ($caseNumbers)
         )
         where RNum=1
        """
    }

    static auto_shared_config_id_sql = { List configIds ->
        String result = ""
        String result1 = """
          SELECT CONFIG_ID as id FROM AUTO_SHARE_WITH_USER_ECONFIG """
        String result2 = """
          SELECT CONFIG_ID as id FROM AUTO_SHARE_WITH_GROUP_ECONFIG """
        String whereClause = ""
        configIds.collate(999).each{
            if(whereClause == ""){
                whereClause += "WHERE CONFIG_ID IN (${it.join(',')})"
            }
            else{
                whereClause += " OR CONFIG_ID IN  (${it.join(',')})"
            }

        }
        result = result1 + whereClause + " UNION " + result2 + whereClause
        return result

    }

    static date_range_sql = { List exDateRangeInfoIds ->
        String result = """
       select id, CAST(DATE_RNG_END_ABSOLUTE AS DATE) as dateRangeEndAbsolute , CAST(DATE_RNG_START_ABSOLUTE AS DATE) as dateRangeStartAbsolute , DATE_RNG_ENUM as dateRangeEnum
       from EX_ALERT_DATE_RANGE  """
        String whereClause = ""
        exDateRangeInfoIds.collate(999).each{
            if(whereClause == "") {
                whereClause += " where id in (${it.join(',')})"
            } else {
                whereClause += " or id in (${it.join(',')})"
            }
        }
        return result+whereClause
    }
    static signal_action_taken_sql = { List signalIdList ->
        String inCriteriaWhereClause = getDictProdNameINCriteria(signalIdList, 'validated_signal_id').toString()
        """
          select validated_signal_id as id,
                 action_taken_string as name 
          from validated_signal_action_taken 
          where $inCriteriaWhereClause
       """
    }

    static String signals_without_share_with =
        """
        select id from validated_signal where id not in ((select validated_signal_id from share_with_group_signal where share_with_groupid in
                                                          (select ID from GROUPS WHERE NAME IS NOT NULL)) 
                                                          union
                                                          (select validated_signal_id from SHARE_WITH_USER_SIGNAL where SHARE_WITH_USERID in
                                                          (select ID from PVUSER WHERE NAME IS NOT NULL)))
        """

    static signal_share_with_all = { String signalIds ->
        """
            INSERT INTO SHARE_WITH_GROUP_SIGNAL(SHARE_WITH_GROUPID,SHARE_WITH_GROUP_IDX,VALIDATED_SIGNAL_ID)
                (select (select ID from GROUPS WHERE NAME = 'All Users'),'0', ID from VALIDATED_SIGNAL where id in (${signalIds}))
        """
    }
    static date_range_sql_literature_alert = { String exDateRangeInfoIds ->
        """
       select
        id, DATE_RNG_END_ABSOLUTE as dateRangeEndAbsolute , DATE_RNG_START_ABSOLUTE as dateRangeStartAbsolute
      from
        EX_LITERATURE_DATE_RANGE
      where
        id in ($exDateRangeInfoIds)
        """
    }

    static date_range_sql_evdas_alert = { String exDateRangeInfoIds ->
        """
       select
        id, DATE_RNG_END_ABSOLUTE as dateRangeEndAbsolute , DATE_RNG_START_ABSOLUTE as dateRangeStartAbsolute
      from
        EX_EVDAS_DATE_RANGE
      where
        id in ($exDateRangeInfoIds)
        """
    }

    static batch_variables_sql = {
        "SELECT key_id, ui_label, use_case, paste_import_option,dic_level,dic_type,isautocomplete,validatable,DECODE_TABLE,DECODE_COLUMN FROM pvs_batch_signal_constants_dsp order by ui_label"
    }

    static find_meddra_field_sql={
        "SELECT key_id, ui_label, use_case,dic_level,dic_type,isautocomplete,validatable FROM pvs_batch_signal_constants_dsp where DECODE_TABLE like 'PVR%MD%'"
    }

    static batch_lot_status= {
        "SELECT vs FROM BatchLotStatus vs WHERE 1=1"
    }

    static batch_lot_status_with_columns = {
        "SELECT new Map(vs.batchId as batchId, vs.id as id , vs.batchDate as batchDate, vs.count as count,vs.validRecordCount as validRecordCount ,vs.invalidRecordCount as invalidRecordCount,vs.uploadedAt as uploadedAt,vs.addedBy as addedBy) FROM BatchLotStatus vs WHERE 1=1"
    }

    static batch_lot_status_update_to_started = {
        "update PVS_BS_APP_BATCH_LOT_STATUS set etl_start_date = sysdate where etl_status is null"
    }

    static batch_lot_status_update_to_completed = {
        "update PVS_BS_APP_BATCH_LOT_STATUS set etl_start_date = sysdate where etl_status = 'STARTED' "
    }

    static pvd_etl_status_completed = {
        "select etl_value from pvr_etl_constants where ETL_KEY = 'ETL_STATUS' and etl_value=3"
    }

    static batch_lot_status_count = {
        " SELECT count(vs.id) FROM BatchLotStatus vs WHERE 1 = 1 "
    }

    static product_groups_status = {
        "SELECT vs FROM ProductGroupStatus vs WHERE 1=1"
    }

    static product_groups_status_sql_qry = {
        "SELECT vs.id as id, vs.version as version, vs.unique_Identifier as uniqueIdentifier, vs.count as count, " +
                " vs.valid_Record_Count as validRecordCount, vs.invalid_Record_Count as invalidRecordCount, " +
                " vs.UPLOADED_DATE as uploadedAt, vs.added_By as addedBy , vs.is_Api_Processed as isApiProcessed " +
                " FROM PVS_BS_APP_PROD_GROUP_STATUS vs WHERE 1=1 "
    }

    static product_groups_status_with_columns = {
        "SELECT new Map(vs.uniqueIdentifier as uniqueIdentifier, vs.id as id , vs.count as count,vs.validRecordCount as validRecordCount ,vs.invalidRecordCount as invalidRecordCount,vs.uploadedAt as uploadedAt,vs.addedBy as addedBy) FROM ProductGroupStatus vs WHERE 1=1"
    }

    static product_groups_status_with_columns_sql_query = {
        "SELECT vs.id as id, vs.version as version, vs.unique_Identifier as uniqueIdentifier, vs.count as count, vs.valid_Record_Count as validRecordCount,  \n" +
                " vs.invalid_Record_Count as invalidRecordCount, vs.UPLOADED_DATE as uploadedAt, vs.added_By as addedBy , vs.is_Api_Processed as isApiProcessed " +
                " FROM PVS_BS_APP_PROD_GROUP_STATUS vs WHERE 1=1 "
    }

    static product_groups_status_count = {
        " SELECT count(vs.id) FROM ProductGroupStatus vs WHERE 1 = 1 "
    }

    static master_count_status_sql = { id ->
        "select CURRENT_STATUS from vw_check_counts_status where execution_id = ${id}"
    }

    static master_ebgm_status_sql = { id ->
        "select CURRENT_STATUS from vw_check_ebgm_status where execution_id = ${id}"
    }

    static master_prr_status_sql = { id ->
        "select CURRENT_STATUS from vw_check_prr_status where execution_id = ${id}"
    }

    static master_dss_status_sql = { id ->
        "select CURRENT_STATUS from vw_check_alert_status where execution_id = ${id}"
    }

    static batch_lot_status_sql= {
        "SELECT vs.id as id , vs.batch_Id as batchId, " +
                " vs.date_Range as dateRange, " +
                " vs.count as count,vs.valid_Record_Count as validRecordCount ,vs.invalid_Record_Count as invalidRecordCount,vs.uploaded_date as uploadedAt, " +
                " vs.added_By as addedBy, vs.is_Api_Processed isApiProcessed , vs.Etl_Status etlStatus FROM PVS_BS_APP_BATCH_LOT_STATUS vs WHERE 1=1"
    }

    static batch_lot_status_with_columns_sql = {
        "SELECT vs.id as id , vs.batch_Id as batchId, " +
                " vs.date_Range as dateRange, " +
                " vs.count as count,vs.valid_Record_Count as validRecordCount ,vs.invalid_Record_Count as invalidRecordCount,vs.uploaded_date as uploadedAt, " +
                " vs.added_By as addedBy, vs.is_Api_Processed isApiProcessed, vs.Etl_Status etlStatus FROM PVS_BS_APP_BATCH_LOT_STATUS vs WHERE 1=1"
    }

    static batch_lot_status_date_range_sql = {
        "select listagg(concat(concat(NVL(START_DATE,''),' - '), NVL(END_DATE,'')),', ') \n" +
                " within group (order by concat(concat(NVL(START_DATE,''),' - '), NVL(END_DATE,'')))\n" +
                " from (select DISTINCT START_DATE, END_DATE from PVS_BS_APP_BATCH_LOT_DATA where 1 = 1 "
    }

    static batch_lot_status_count_sql = {
        " SELECT count(vs.id) FROM PVS_BS_APP_BATCH_LOT_STATUS vs WHERE 1 = 1 "
    }

    static batch_lot_etl_status_by_id = { id ->
        " select count(*) from PVS_BS_APP_BATCH_LOT_STATUS where ETL_STATUS='COMPLETED' and id = ${id} "
    }

    static update_batch_lot_status_count = { bl ->
        "update PVS_BS_APP_BATCH_LOT_STATUS set VALID_RECORD_COUNT = ${bl.getValidRecordCount()}, INVALID_RECORD_COUNT= ${bl.getInvalidRecordCount()} where id = ${bl.getId()} "
    }

    static batch_lot_status_update_to_failed = { id ->
        "update PVS_BS_APP_BATCH_LOT_STATUS set ETL_STATUS='FAILED' where id = ${id} "
    }

    static batch_lot_etl_not_successful_count = {
        " select count(*) from PVS_BS_APP_BATCH_LOT_DATA where \n" +
                " batch_lot_id in (select id from PVS_BS_APP_BATCH_LOT_STATUS where ETL_START_DATE = " +
                " (select MAX(ETL_START_DATE) from PVS_BS_APP_BATCH_LOT_STATUS) ) \n" +
                " and  (ETL_STATUS != 'SUCCESS' or VALIDATION_ERROR is not null) "
    }

    static batch_lot_etl_last_successful_date = {
        "select max(ETL_START_DATE) from (\n" +
                "select STATUS.id, status.IS_ETL_PROCESSED, status.ETL_START_DATE , \n" +
                "(select count(*) from PVS_BS_APP_BATCH_LOT_DATA where \n" +
                "batch_lot_id =STATUS.id  and  (ETL_STATUS = 'SUCCESS' and VALIDATION_ERROR is null)) cnt, \n" +
                "(select count(*) from PVS_BS_APP_BATCH_LOT_DATA where \n" +
                "batch_lot_id =STATUS.id  and  (ETL_STATUS != 'SUCCESS' or VALIDATION_ERROR is not null)) invalid_cnt \n" +
                "from PVS_BS_APP_BATCH_LOT_STATUS STATUS \n" +
                "where status.ETL_START_DATE is not null \n" +
                ") successfull_etl where cnt>0 and invalid_cnt=0 "
    }

    static aggregate_alert_product_group_update = { configuration ->
        "update RCONFIG set product_group_selection='${configuration.productGroupSelection}' where id = ${configuration.id}"
    }

    static String orderRefSql(String sort, String direction){
        if(!sort){
            "order by rm.priority desc NULLS LAST"
        } else if(sort == "name"){
            "order by lower(ar.input_name) $direction NULLS LAST"
        } else if(sort == "date"){
            "order by rd.date_created $direction NULLS LAST"
        }
    }

    static String fetchRefSql(Integer max, Integer offset){
        if(max>0){
            "OFFSET ${offset} ROWS FETCH NEXT ${max} ROWS ONLY"
        } else{
            ""
        }
    }

    static searchReferencesSql(String searchString) {
        if (searchString) {
            searchString = searchString.toLowerCase()
            String esc_char = ""
            if (searchString.contains('_')) {
                searchString = searchString.replaceAll("\\_", "!_%")
                esc_char = "!"
            } else if (searchString.contains('%')) {
                searchString = searchString.replaceAll("\\%", "!%%")
                esc_char = "!"
            }
            if (esc_char) {
                return """
                and (lower(ar.input_name) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'
                or lower(ar.reference_link) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'
                or lower(rd.created_by) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}')
                """
            } else {
                return """
                and (lower(ar.input_name) like '%${searchString.replaceAll("'", "''")}%'
                or lower(ar.reference_link) like '%${searchString.replaceAll("'", "''")}%'
                or lower(rd.created_by) like '%${searchString.replaceAll("'", "''")}%')
            """
            }
        }
        ""
    }
    static get_references_sql = { userId, isPinned, sort, direction, max, offset, searchString ->
        """
        select rd.id as referenceId, rm.is_pinned as pinned, rm.priority as priority
        from REFERENCE_DETAILS rd
        LEFT JOIN USER_REFERENCES_MAPPING rm ON rd.id = rm.reference_id
        LEFT JOIN ATTACHMENT_REFERENCE ar ON rd.ATTACHMENT_ID = ar.id
        where rm.is_deleted = 0 and rm.is_pinned =${isPinned} and rm.user_id = ${userId}
        ${searchReferencesSql(searchString)}
        ${(orderRefSql(sort, direction))}
        ${(fetchRefSql(max, offset))}
        """

    }
    static get_ref_count_sql = { userId, searchString ->
        """
        select count(*)
        from REFERENCE_DETAILS rd
        LEFT JOIN USER_REFERENCES_MAPPING rm ON rd.id = rm.reference_id
        LEFT JOIN ATTACHMENT_REFERENCE ar ON rd.ATTACHMENT_ID = ar.id
        where rm.is_deleted = 0 and rm.user_id = ${userId}
        ${searchReferencesSql(searchString)}
        """

    }

    static remove_reference_mapping_to_users_and_groups = {Long referenceId,List<Long> userIdList ->
        """
            delete from USER_REFERENCES_MAPPING where REFERENCE_ID = ${referenceId} and USER_ID not in (${userIdList.join(",")})
        """
    }

    static get_user_id_from_group_sql = { groupId ->
        """
        select user_id as userId from user_group_s where group_id = ${groupId}
        """
    }


    static get_user_group_mapping_count_sql = { roleId, userId ->
        """
select count(1) as fieldCount from USER_GROUP_MAPPING ugm left join USER_GROUP_ROLE ugr on ugm.GROUP_ID=ugr.USER_GROUP_ID where ugr.ROLE_ID=${
            roleId
        } and ugm.USER_ID=" ${ userId } 

        """
    }

    static is_migration_required = {
        "select param_value from cat_parameters where param_key = 'IS_WEBAPP_MIG_REQUIRED' "
    }
    static delete_agg_global_tags = {
        '''DELETE FROM agg_global_tags WHERE pvs_global_tag_id IN (SELECT id FROM pvs_global_tag) '''
    }
    static delete_single_global_tags = {
        '''DELETE FROM SINGLE_GLOBAL_TAGS WHERE pvs_global_tag_id IN (SELECT id FROM pvs_global_tag) '''
    }
    static update_agg_alerts = {
        "UPDATE AGG_ALERT SET global_identity_id = NULL"
    }
    static update_archived_agg_alerts = {
        "UPDATE ARCHIVED_AGG_ALERT SET global_identity_id = NULL"
    }
    static update_single_alerts = {
        "UPDATE SINGLE_CASE_ALERT SET global_identity_id = NULL"
    }
    static update_archived_single_alerts = {
        // global_identity_id is nullable false hence setting -1
        "UPDATE ARCHIVED_SINGLE_CASE_ALERT SET global_identity_id = -1"
    }
    static disable_constraint_agg_table = {
        '''ALTER TABLE AGG_ALERT DISABLE CONstraint FK9wophmgrppuk00kumrk81waqb'''
    }
    static disable_constraint_archived_agg_table = {
        "ALTER TABLE ARCHIVED_AGG_ALERT DISABLE CONstraint FKedhgc4ecfee0ta6ixe5syha4e"
    }
    static disable_constraint_single_table = {
        '''ALTER TABLE SINGLE_CASE_ALERT DISABLE CONstraint FK8IC0IQI8EYNBXKKWXROC6IO1R'''
    }
    static disable_constraint_archived_single_table = {
        "ALTER TABLE ARCHIVED_SINGLE_CASE_ALERT DISABLE CONstraint FKQAXCHL4OFG9634TB244AW63E5"
    }
    static truncate_global_product_table = {
        'TRUNCATE TABLE GLOBAL_PRODUCT_EVENT'
    }
    static truncate_global_case_table = {
        'TRUNCATE TABLE GLOBAL_CASE'
    }
    static enable_constraint_agg_table = {
        'ALTER TABLE AGG_ALERT ENABLE CONstraint FK9wophmgrppuk00kumrk81waqb'
    }
    static enable_constraint_archived_agg_table = {
        'ALTER TABLE ARCHIVED_AGG_ALERT ENABLE CONstraint FKedhgc4ecfee0ta6ixe5syha4e'
    }
    static enable_constraint_single_table = {
        'ALTER TABLE SINGLE_CASE_ALERT ENABLE CONstraint FK8IC0IQI8EYNBXKKWXROC6IO1R'
    }
    static enable_constraint_archived_single_table = {
        'ALTER TABLE ARCHIVED_SINGLE_CASE_ALERT ENABLE CONstraint FKQAXCHL4OFG9634TB244AW63E5'
    }
    static insert_dummy_values_global_case = {
        'insert into GLOBAL_CASE(globalcaseid, version, case_id, version_num) values (-1,-1,-1,-1)'
    }
    static merge_agg_alerts = {
        '''MERGE INTO agg_alert a USING (
                                    SELECT
                                        *
                                    FROM
                                        global_product_event
                                )
                                b ON (( b.product_event_comb = a.product_id
                                                              || '-'
                                                              || a.pt_code
                                                              || '-'
                                                              || 'null')
                                        AND b.PRODUCT_KEY_ID = a.PROD_HIERARCHY_ID
                                        AND b.EVENT_KEY_ID   =   a.EVENT_HIERARCHY_ID)
                                WHEN MATCHED THEN UPDATE SET a.global_identity_id = globalproducteventid
                                WHERE
                             a.smq_code IS NULL  '''
    }
    static merge_single_alerts = {
        '''
        MERGE INTO SINGLE_CASE_ALERT a USING (
        SELECT
            *
        FROM
        global_case
        )
        b ON (b.CASE_ID = a.CASE_ID and b.VERSION_NUM = a.CASE_VERSION)
        WHEN MATCHED THEN UPDATE SET a.global_identity_id = b.GLOBALCASEID
        '''
    }
    static merge_agg_on_demand_alerts = {
        '''MERGE INTO agg_on_demand_alert a USING (
                                    SELECT
                                        *
                                    FROM
                                        global_product_event
                                )
                                b ON (( b.product_event_comb = a.product_id
                                                              || '-'
                                                              || a.pt_code
                                                              || '-'
                                                              || 'null')
                                        AND b.PRODUCT_KEY_ID = a.PROD_HIERARCHY_ID
                                        AND b.EVENT_KEY_ID   =   a.EVENT_HIERARCHY_ID)
                                WHEN MATCHED THEN UPDATE SET a.global_identity_id = globalproducteventid
                                WHERE
                             a.smq_code IS NULL  '''
    }
    static merge_single_on_demand_alerts = {
        '''
        MERGE INTO SINGLE_ON_DEMAND_ALERT a USING (
        SELECT
            *
        FROM
        global_case
        )
        b ON (b.CASE_ID = a.CASE_ID and b.VERSION_NUM = a.CASE_VERSION)
        WHEN MATCHED THEN UPDATE SET a.global_identity_id = b.GLOBALCASEID
        '''
    }
    static merge_archived_agg_alerts = {
        '''MERGE INTO ARCHIVED_AGG_ALERT a USING (
                                    SELECT
                                        *
                                    FROM
                                        global_product_event
                                )
                                b ON (( b.product_event_comb = a.product_id
                                                              || '-'
                                                              || a.pt_code
                                                              || '-'
                                                              || 'null' )
                                        AND b.PRODUCT_KEY_ID = a.PROD_HIERARCHY_ID
                                        AND b.EVENT_KEY_ID   =   a.EVENT_HIERARCHY_ID)
                                WHEN MATCHED THEN UPDATE SET a.global_identity_id = globalproducteventid
                                WHERE
                             a.smq_code IS NULL  '''
    }

    static merge_archived_single_alerts = {
        '''
        MERGE INTO ARCHIVED_SINGLE_CASE_ALERT a USING (
        SELECT
        *
        FROM
        global_case
        )
        b ON (b.CASE_ID = a.CASE_ID and b.VERSION_NUM = a.CASE_VERSION)
        WHEN MATCHED THEN UPDATE SET a.global_identity_id = b.GLOBALCASEID
        '''
    }

    static update_agg_alert_for_smq = {
       """UPDATE agg_alert tgt
            SET
            tgt.event_hierarchy_id = decode(tgt.smq_code, 1, 19, 18)
            WHERE
            tgt.smq_code IS NOT NULL
    """
    }


    static update_agg_on_demand_alert_for_smq = {
        """UPDATE agg_on_demand_alert tgt
            SET
            tgt.event_hierarchy_id = decode(tgt.smq_code, 1, 19, 18)
            WHERE
            tgt.smq_code IS NOT NULL
    """
    }

    static update_archived_agg_alert_for_smq = {
        """UPDATE archived_agg_alert tgt
            SET
            tgt.event_hierarchy_id = decode(tgt.smq_code, 1, 19, 18)
            WHERE
            tgt.smq_code IS NOT NULL
    """
    }

    static insert_agg_alerts = {
        """
        INSERT INTO AGG_GLOBAL_TAGS(PVS_GLOBAL_TAG_ID,GLOBAL_PRODUCT_EVENT_ID)
        SELECT ID, GLOBAL_ID FROM PVS_GLOBAL_TAG 
        """
    }
    static insert_single_alerts = {
        """
        INSERT INTO SINGLE_GLOBAL_TAGS(PVS_GLOBAL_TAG_ID,GLOBAL_CASE_ID)
        SELECT ID, GLOBAL_ID FROM PVS_GLOBAL_TAG
        """
    }
    static get_agg_sql = {
        """
        select id as id, product_id as productId, pt_code as ptCode, smq_code as smqCode, prod_hierarchy_id as prodHierarchyId, event_hierarchy_id as eventHierarchyId from agg_alert where global_identity_id is null
        """
    }
    static get_archived_agg_sql = {
        """
        select id as id, product_id as productId, pt_code as ptCode, smq_code as smqCode, prod_hierarchy_id as prodHierarchyId, event_hierarchy_id as eventHierarchyId from archived_agg_alert where global_identity_id is null
        """
    }
    static get_single_sql = {
        """
        select id as id, CASE_ID as caseId, COALESCE(CASE_VERSION,0) as versionNum from SINGLE_CASE_ALERT where global_identity_id is null
        """
    }
    static get_archived_single_sql = {
        """
        select id as id, CASE_ID as caseId, COALESCE(CASE_VERSION,0) as versionNum from ARCHIVED_SINGLE_CASE_ALERT where global_identity_id = -1
        """
    }
    static agg_count_due_date = { Long currentUserId, Long workflowGroupId,List<Long> groupIdList ->
        """
        SELECT IDENTIFICATION, COUNT(1) FROM (SELECT CASE WHEN TRUNC(DUE_DATE) < TRUNC(SYSDATE) THEN 'OLD' WHEN TRUNC(DUE_DATE) = TRUNC(SYSDATE) THEN 'CURRENT' WHEN TRUNC(DUE_DATE)> TRUNC(SYSDATE) THEN 'NEW' END as IDENTIFICATION FROM AGG_ALERT aa
        LEFT JOIN EX_RCONFIG rc ON (rc.id = aa.exec_configuration_id)
        LEFT JOIN DISPOSITION disp ON (aa.disposition_id  = disp.ID)
        WHERE (aa.ASSIGNED_TO_ID = ${currentUserId} OR aa.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")}) )
        AND rc.adhoc_run      = 0 AND rc.is_deleted = 0 AND rc.is_latest = 1  
        AND disp.review_completed = 0 AND rc.workflow_group = ${workflowGroupId} )
        GROUP BY IDENTIFICATION
        """
    }
    static sca_count_due_date = { Long currentUserId, Long workflowGroupId, List<Long> groupIdList ->
        """
        SELECT IDENTIFICATION, COUNT(1) FROM (SELECT CASE WHEN TRUNC(DUE_DATE) < TRUNC(SYSDATE) THEN 'OLD' WHEN TRUNC(DUE_DATE) = TRUNC(SYSDATE) THEN 'CURRENT' WHEN TRUNC(DUE_DATE)> TRUNC(SYSDATE) THEN 'NEW' END as IDENTIFICATION FROM SINGLE_CASE_ALERT sca
        LEFT JOIN EX_RCONFIG rc ON (rc.id = sca.exec_config_id)
        LEFT JOIN DISPOSITION disp ON (sca.disposition_id  = disp.ID) 
        WHERE  (sca.ASSIGNED_TO_ID = ${currentUserId} OR sca.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")}) ) 
            AND sca.IS_CASE_SERIES = 0 AND rc.adhoc_run = 0 
            AND rc.is_deleted = 0 AND rc.is_latest = 1  AND disp.review_completed = 0 
            AND rc.workflow_group = ${workflowGroupId} )
        GROUP BY IDENTIFICATION
        """
    }

    static String case_details_sections =
            """
        select id, section_name from case_details_section
        """

    static fetch_data_sheet_count = { searchedTerm="", String coreSheet, max, offset ->
        String whereClause = " where "
        String dataSheetClause = ""
        if(searchedTerm){
            dataSheetClause += " lower(display_col) like '%${searchedTerm.trim()}%' "
        }

        if (coreSheet && coreSheet == Constants.DatasheetOptions.CORE_SHEET) {
            if (dataSheetClause) {
                dataSheetClause += " and core_sheet = 1 "
            } else {
                dataSheetClause += " core_sheet = 1 "
            }
        }

        dataSheetClause = dataSheetClause ? whereClause + dataSheetClause : ""

        """
            select count(*) from vw_pvs_ds_pf_list 
            ${dataSheetClause}
            order by display_col asc
            //${fetchRefSql(max, offset)}
        """
    }


    static getListedNess = { executedId ->
        """
                  select Meddra_pt_code, Listedness_data from PVS_LISTEDNESS_${executedId}_AGG  
            """
    }

    static caseAlert_dashboard_due_date = {Long currentUserId, Long workflowGrpId,List<Long> groupIdList ->
        """ 
        select sum(case when TRUNC(due_date) < TRUNC(CURRENT_TIMESTAMP) then 1 else 0 end) as PASTCOUNT,
              sum(case when TRUNC(due_date) > TRUNC(CURRENT_TIMESTAMP) then 1 else 0 end) as FUTURECOUNT,
              sum(case when TRUNC(due_date) = TRUNC(CURRENT_TIMESTAMP)  then 1 else 0 end) as CURRENTCOUNT
        from ALERTS alert
        JOIN DISPOSITION disp ON (alert.disposition_id  = disp.ID)
        where  
        (alert.ASSIGNED_TO_ID = ${currentUserId}
            OR alert.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")})
        )
        AND disp.review_completed = 0
        AND alert.WORKFLOW_GROUP = ${workflowGrpId}
         """
    }

    static rpt_to_ui_label_table = {->
        """
            SELECT * FROM ICR_RPT_FIELD_MAPPING
        """
    }

    static rpt_to_ui_label_table_pvr = {->
        """
            SELECT * FROM PVR_RPT_FIELD_LABEL
        """
    }

    static disassociate_signal_from_pec = { String joinTableName, String joinColumnName, String alertIds ->
        """
delete from ${joinTableName} where ${joinColumnName} in (${alertIds})
"""
    }

    static disassociate_pec_from_signal = { String joinTableName, Long signalId ->
        """
delete from ${joinTableName} where VALIDATED_SIGNAL_ID = ${signalId}
"""
    }

    static delete_signal_by_ids = { String signalIdList ->
        """
            BEGIN
            DELETE FROM VALIDATED_SINGLE_ALERTS
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            
            DELETE FROM VALIDATED_LITERATURE_ALERTS
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_SIGNAL_ALL_PRODUCT
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_ADHOC_ALERTS
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_ALERT_ACTIVITIES
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_AGG_ALERTS
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_EVDAS_ALERTS
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_ARCHIVED_SCA
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_ARCHIVED_LIT_ALERTS
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_ARCHIVED_ACA
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_ARCH_EVDAS_ALERTS
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_ALERT_COMMENTS
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_ALERT_DOCUMENTS
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VAL_SIGNAL_TOPIC_CATEGORY
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_SIGNAL_GROUP
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VS_EVAL_METHOD
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_SIGNAL_RCONFIG
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VS_EVDAS_CONFIG
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM SIGNAL_LINKED_SIGNALS
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM SIGNAL_LINKED_SIGNALS
                WHERE LINKED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM SIGNAL_SIG_STATUS_HISTORY
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_SIGNAL_OUTCOMES
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM SHARE_WITH_USER_SIGNAL
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM SHARE_WITH_GROUP_SIGNAL
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM SIGNAL_SIG_RMMS
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            
            DELETE FROM SIGNAL_HISTORY
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );

            DELETE FROM VALIDATED_SIGNAL_ACTIONS
                WHERE VALIDATED_SIGNAL_ACTIONS_ID in ( ${signalIdList} );
           
            DELETE FROM SIGNAL_CHART
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );

            DELETE FROM VALIDATED_SIGNAL_ACTION_TAKEN
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );

            DELETE FROM MEETING_ATTACHMENTS where MEETING_ID in
            (select id from MEETING where VALIDATED_SIGNAL_ID in (${signalIdList}));

            DELETE FROM MEETING_ACTIONS where MEETING_ACTIONS_ID in
            (select id from MEETING where VALIDATED_SIGNAL_ID in (${signalIdList}));

            DELETE FROM MEETING_ACTIVITIES where  MEETING_ACTIVITIES_ID in
            (select id from MEETING where VALIDATED_SIGNAL_ID in (${signalIdList}));

            DELETE FROM MEETING_GUEST_ATTENDEE where MEETING_GUEST_ATTENDEE_ID in
            (select id from MEETING where VALIDATED_SIGNAL_ID in (${signalIdList}));

            DELETE FROM MEETING_PVUSER where MEETING_ATTENDEES_ID in
            (select id from MEETING where VALIDATED_SIGNAL_ID in (${signalIdList}));

            DELETE FROM MEETING
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
    
            DELETE FROM VALIDATED_SIGNAL WHERE id in ( ${signalIdList} );
            COMMIT;
                exception when others
          then
          raise_application_error(-20001,sqlerrm||'->'||dbms_utility.format_error_backtrace);
          END;
            
        """
    }
    static getExConfigIdsForMasterAlertQuery = { Long masterId, Boolean isLatest ->
        String query = "select id from ex_rconfig where config_id in (select id from rconfig where master_config_id =${masterId})"
        if(isLatest){
            query += " and is_latest =1"
        }
        query
    }
    static updateConfigurationDeletionStatus ={ exConfigIds ->
        """
         update rconfig
         set next_run_date = null,
         deletion_status = 'DELETION_IN_PROGRESS',
         deletion_In_Progress=1
         where id in (select CONFIG_ID from ex_rconfig where id in (${exConfigIds}))
        """
    }
    static configurationDeletionCompleted ={ exConfigIds ->
        """
         update rconfig
         set deletion_status = 'DELETED',
         deletion_In_Progress = 0
         where id in (select CONFIG_ID from ex_rconfig where id in (${exConfigIds}))
        """
    }
    static getCaseSeriesIds = { Long exConfigId ->
        "select pvr_case_series_id as id from ex_rconfig where id in (select distinct exec_config_id from single_case_alert where exec_config_id = ${exConfigId})"
    }

    static getReportId = { Long exConfigId ->
        "select report_id as id from ex_rconfig where id=${exConfigId}"
    }

    static get_manually_added_signal_count = {List exConfigIdList, String joinTable, String joinColumn ->
        """
        select count(*)
        from ${joinTable}
        where IS_CARRY_FORWARD is null
        and ${joinColumn} in (${exConfigIdList.join(",")})
        """
    }

    static get_carry_forward_signal_count = {List exConfigIdList, String joinTable, String joinColumn ->
        """
        select count(*)
        from ${joinTable}
        where IS_CARRY_FORWARD = 1
        and ${joinColumn} in (${exConfigIdList.join(",")})
        """
    }

    static delete_carry_forward_signals = {List exConfigIdList, String joinTable, String joinColumn ->
        """
        delete
        from ${joinTable}
        where IS_CARRY_FORWARD = 1
        and ${joinColumn} in (${exConfigIdList.join(",")})
        """
    }
    static validated_agg_alert_ids = { Long signalId, Date dateClosed ->
        "select agg_alert_id from VALIDATED_AGG_ALERTS where validated_signal_id = ${signalId}"
    }

    static validated_agg_alert_id_List = { List signalIds ->

        String whereClause = ""
        signalIds.collate(999).each{

            if(whereClause == "") {
                whereClause += " where agg.VALIDATED_SIGNAL_ID in (${it.join(',')})"
            } else {
                whereClause += " or agg.VALIDATED_SIGNAL_ID in (${it.join(',')})"
            }
        }
        """
            select
                agg_alert_id,
                validated_signal_id
                from
                VALIDATED_AGG_ALERTS agg
                inner join VALIDATED_SIGNAL vs on agg.VALIDATED_SIGNAL_ID = vs.id"""+whereClause

    }

    static validated_evdas_alert_ids_List = { List signalIds ->
        String whereClause = ""
        signalIds.collate(999).each{

            if(whereClause == "") {
                whereClause += " where ev.VALIDATED_SIGNAL_ID in (${it.join(',')})"
            } else {
                whereClause += " or ev.VALIDATED_SIGNAL_ID in (${it.join(',')})"
            }
        }
        """
            select
                evdas_alert_id,
                validated_signal_id
                from
                VALIDATED_EVDAS_ALERTS ev
                inner join VALIDATED_SIGNAL vs on ev.VALIDATED_SIGNAL_ID = vs.id"""+whereClause
    }

    static validated_evdas_alert_ids = { Long signalId, Date dateClosed ->
        "select evdas_alert_id from VALIDATED_EVDAS_ALERTS where validated_signal_id = ${signalId} "
    }

    static generic_comment_sql = { Long signalId ->
        "select GENERIC_COMMENT from VALIDATED_SIGNAL where ID = ${signalId}"
    }

    static single_case_attachments = { List<Long> configIdList ->
        String queryString = """
            select sca.CASE_NUMBER || '_' || sca.ALERT_CONFIGURATION_ID , sca.EXEC_CONFIG_ID
            from ATTACHMENT_LINK al
                     inner join SINGLE_CASE_ALERT sca
                                on
                                   al.REFERENCE_CLASS = 'com.rxlogix.signal.SingleCaseAlert' and
                                   al.REFERENCE_ID = sca.ID and
        """
        if(configIdList.size()>1000){
            List<List<Long>> configIdSubList = configIdList.collate(1000)
            queryString += "( sca.ALERT_CONFIGURATION_ID IN (${configIdSubList[0].join(",").toString()})"
            configIdSubList.remove(0)
            configIdSubList.each {
                queryString += " OR sca.ALERT_CONFIGURATION_ID IN (${it.join(",").toString()})"
            }
            queryString += ")"
        }else{
            queryString += " sca.ALERT_CONFIGURATION_ID in (${configIdList.join(",")})"
        }
        queryString
    }

    static archived_single_case_attachments = { List<Long> configIdList ->
        String queryString = """
            select asca.CASE_NUMBER || '_' || asca.ALERT_CONFIGURATION_ID , asca.EXEC_CONFIG_ID
            from ATTACHMENT_LINK al
                     inner join ARCHIVED_SINGLE_CASE_ALERT asca
                                on
                                   al.REFERENCE_CLASS = 'com.rxlogix.signal.ArchivedSingleCaseAlert' and
                                   al.REFERENCE_ID = asca.ID and
        """
        if(configIdList.size()>1000){
            List<List<Long>> configIdSubList = configIdList.collate(1000)
            queryString += "( asca.ALERT_CONFIGURATION_ID IN (${configIdSubList[0].join(",").toString()})"
            configIdSubList.remove(0)
            configIdSubList.each {
                queryString += " OR asca.ALERT_CONFIGURATION_ID IN (${it.join(",").toString()})"
            }
            queryString += ")"
        }else{
            queryString += " asca.ALERT_CONFIGURATION_ID in (${configIdList.join(",")})"
        }
        queryString
    }
    static delete_roles_from_user = {Long roleId ->
        """
        DELETE FROM PVUSERS_ROLES WHERE ROLE_ID = ${roleId}
        """
    }

    static evdas_activity_from_dashboard = { Long userId ->
        """    
            select id as id from activities act inner join ex_evdas_config_activities execa on act.id = execa.activity_id 
            where act.assigned_to_id =${userId}
        """
    }

    static agg_activity_from_dashboard = { Long userId ->
        """
            select  id as id from activities act inner join ex_rconfig_activities execa on act.id = execa.activity_id 
            where performed_by_id =${userId} and case_number is null and suspect_product is not null ORDER BY id DESC
        """
    }
    static evdas_activity_list = { Long exeAlertId ->
        """
            select distinct disposition.display_name from evdas_alert
            inner join disposition on evdas_alert.disposition_id = disposition.id 
            where evdas_alert.exec_configuration_id = ${exeAlertId}
        """
    }
    static agg_activity_list = {  Long exeAlertId ->
        """
            select distinct disposition.display_name from agg_alert 
             inner join disposition on agg_alert.disposition_id = disposition.id 
            where agg_alert.exec_configuration_id = ${exeAlertId}
        """
    }
    static singleCase_activity_list = { Long exeAlertId ->
        """
            select distinct disposition.display_name from single_case_alert
            inner join disposition on single_case_alert.disposition_id = disposition.id
            where single_case_alert.exec_config_id = ${exeAlertId}
        """
    }
    static context_setting_pvs_func_creation = { ->
        """
            create or replace FUNCTION f_security_management (
                p_schema VARCHAR2,
                p_obj    VARCHAR2
            ) RETURN VARCHAR2 AS
            
                lvc_predicate VARCHAR2(100);
                f_key         VARCHAR2(255);
                f_value VARCHAR2(255);
            BEGIN
            
                IF ( sys_context('PVD_SECURITY_FIELDS', 'ENCRYPTION_KEY') = 'PVR' OR sys_context('PVD_SECURITY_FIELDS', 'ENCRYPTION_KEY') = 'PVS'
                OR sys_context('PVD_SECURITY_FIELDS', 'ENCRYPTION_KEY') = 'PVA' OR sys_context('PVD_SECURITY_FIELDS', 'ENCRYPTION_KEY') = 'PVD'
                OR sys_context('PVD_SECURITY_FIELDS', 'ENCRYPTION_KEY') = 'PVCM' ) THEN
                    lvc_predicate := '1=1';
            ELSE
                    lvc_predicate := '1=2';
            END IF;
            
            RETURN lvc_predicate;
            END;
            
        """
    }

    static context_setting_pvs_drop_policies = { ->
        """
            BEGIN
            for i in (SELECT OBJECT_NAME, PF_OWNER, POLICY_NAME FROM USER_POLICIES WHERE POLICY_NAME = 'SCAPOLICY') LOOP
            BEGIN
            EXECUTE IMMEDIATE
                    'BEGIN
                        DBMS_RLS.DROP_POLICY(
                            object_schema => ''' || i.pf_owner || ''', -- Use pf_owner, not pf_user
                                    object_name   => ''' || i.object_name || ''',
                                    policy_name   => ''SCAPOLICY'');
                            END;';
            END;
            END LOOP;
            END;

        """
    }

    static context_setting_pvs_add_policy_SCA = { String schema ->
        """
            begin
            dbms_rls.add_policy(object_schema => '${schema}',
            object_name => 'SINGLE_CASE_ALERT',
            policy_name => 'SCAPOLICY',
            function_schema => '${schema}',
            policy_function => 'F_SECURITY_MANAGEMENT',
            sec_relevant_cols => 'DATE_OF_BIRTH',
            sec_relevant_cols_opt => dbms_rls.all_rows);
            end;
        """
    }

    static context_setting_pvs_add_policy_ArchivedSCA = { String schema ->
        """
            begin
            dbms_rls.add_policy(object_schema => '${schema}',
            object_name => 'ARCHIVED_SINGLE_CASE_ALERT',
            policy_name => 'SCAPOLICY',
            function_schema => '${schema}',
            policy_function => 'F_SECURITY_MANAGEMENT',
            sec_relevant_cols => 'DATE_OF_BIRTH',
            sec_relevant_cols_opt => dbms_rls.all_rows);
            end;
        """
    }

    static context_setting_pvs_replace_procedure_p_set_context_sec = {->
        """
            create or replace PROCEDURE p_set_context_sec (pi_owner VARCHAR2, pi_encryption_key VARCHAR2)
            IS
            BEGIN
            DBMS_SESSION.set_context(pi_owner, 'ENCRYPTION_KEY', pi_encryption_key);
            END;
        """
    }

    static context_setting_pvs_replace_procedure_p_set_context = {->
        """
            create or replace PROCEDURE p_set_context (pi_owner VARCHAR2, pi_encryption_key VARCHAR2)
            IS
            BEGIN
            EXECUTE IMMEDIATE 'CREATE OR REPLACE CONTEXT PVD_SECURITY_FIELDS USING p_set_context_sec';
            
            EXECUTE IMMEDIATE 'BEGIN p_set_context_sec(:owner, :encryption_key); END;'
                USING pi_owner, pi_encryption_key;
            
            COMMIT;
            END;
        """
    }

    static context_setting_pvs_p_set_context = {->
        """
            BEGIN
                p_set_context('PVD_SECURITY_FIELDS', 'PVS');
            END;
        """
    }

    static retrieve_justification_by_class_and_id = {String className, Long objectId->
        """
            select JUSTIFICATION from ACTION_JUSTIFICATION AJ
            where
            JSON_EXISTS (AJ.ATTRIBUTES_MAP, '\$.INSTANCES_INFO[*]?(@.id == ${objectId})')
            and
            POSTER_CLASS='${className}'
        """
    }

    static icr_data_clean_for_failed_execution = { Long configId, Long exConfigId, Long failedExConfigId ->
        """
            declare
       lvc_sql VARCHAR2(32000);
       lvc_exec_sql VARCHAR2(32000);
       BEGIN
             SELECT listagg(column_name,',') within group (order by column_id) as cols
       INTO lvc_sql
       FROM USER_TAB_COLUMNS
       WHERE table_name = 'SINGLE_CASE_ALERT'and column_name not in ('JSON_FIELD','REPORTER_QUALIFICATION','RISK_CATEGORY');
       lvc_exec_sql := 'INSERT into SINGLE_CASE_ALERT ('||lvc_sql||') SELECT '||lvc_sql||' FROM ARCHIVED_SINGLE_CASE_ALERT WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId}';
       execute immediate lvc_exec_sql;
       
       INSERT into VALIDATED_SINGLE_ALERTS(SINGLE_ALERT_ID,VALIDATED_SIGNAL_ID) SELECT vsca.ARCHIVED_SCA_ID, vsca.VALIDATED_SIGNAL_ID
       FROM VALIDATED_ARCHIVED_SCA vsca
        INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
            ON vsca.ARCHIVED_SCA_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
            
       INSERT into SINGLE_CASE_ALERT_TAGS(SINGLE_ALERT_ID,PVS_ALERT_TAG_ID) SELECT vsca.SINGLE_ALERT_ID, vsca.PVS_ALERT_TAG_ID
       FROM ARCHIVED_SCA_TAGS vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
            
        INSERT into SINGLE_ALERT_PT(SINGLE_ALERT_ID,SCA_PT,PT_LIST_IDX) SELECT vsca.ARCHIVED_SCA_ID, vsca.ARCHIVED_SCA_PT, vsca.PT_LIST_IDX
       FROM ARCHIVED_SCA_PT vsca
        INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
            ON vsca.ARCHIVED_SCA_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
            
        INSERT into SINGLE_ALERT_CON_COMIT(SINGLE_ALERT_ID,ALERT_CON_COMIT,CON_COMIT_LIST_IDX) SELECT vsca.ARCHIVED_SCA_ID, vsca.ALERT_CON_COMIT, vsca.CON_COMIT_LIST_IDX
       FROM ARCHIVED_SCA_CON_COMIT vsca
        INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
            ON vsca.ARCHIVED_SCA_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
            
            
         INSERT into SINGLE_ALERT_SUSP_PROD(SINGLE_ALERT_ID,SCA_PRODUCT_NAME,SUSPECT_PRODUCT_LIST_IDX) SELECT vsca.ARCHIVED_SCA_ID, vsca.SCA_PRODUCT_NAME, vsca.SUSPECT_PRODUCT_LIST_IDX
       FROM ARCHIVED_SCA_SUSP_PROD vsca
        INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
            ON vsca.ARCHIVED_SCA_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
            
         INSERT into SINGLE_ALERT_MED_ERR_PT_LIST(SINGLE_ALERT_ID,SCA_MED_ERROR,MED_ERROR_PT_LIST_IDX) SELECT vsca.ARCHIVED_SCA_ID, vsca.SCA_MED_ERROR, vsca.MED_ERROR_PT_LIST_IDX
       FROM ARCHIVED_SCA_MED_ERR_PT_LIST vsca
        INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
            ON vsca.ARCHIVED_SCA_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
            
         INSERT into SINGLE_ALERT_ACTIONS(SINGLE_CASE_ALERT_ID,ACTION_ID) SELECT vsca.ARCHIVED_SCA_ID, vsca.ACTION_ID
       FROM ARCHIVED_SCA_ACTIONS vsca
        INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
            ON vsca.ARCHIVED_SCA_ID = sca.ID
             WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
             
             
         INSERT into SINGLE_ALERT_INDICATION_LIST(SINGLE_ALERT_ID,SCA_INDICATION,indication_list_idx) 
        SELECT vsca.ARCHIVED_SCA_ID, vsca.SCA_INDICATION, vsca.indication_list_idx
               FROM AR_SIN_ALERT_INDICATION_LIST vsca
                INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
                    ON vsca.ARCHIVED_SCA_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
                    
                    
           INSERT into SINGLE_ALERT_CAUSE_OF_DEATH(SINGLE_ALERT_ID,SCA_CAUSE_OF_DEATH,cause_of_death_list_idx) 
        SELECT vsca.ARCHIVED_SCA_ID, vsca.SCA_CAUSE_OF_DEATH, vsca.cause_of_death_list_idx
               FROM AR_SIN_ALERT_CAUSE_OF_DEATH vsca
                INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
                    ON vsca.ARCHIVED_SCA_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
            
           INSERT into SINGLE_ALERT_PAT_MED_HIST(SINGLE_ALERT_ID,SCA_PAT_MED_HIST,patient_med_hist_list_idx) 
        SELECT vsca.ARCHIVED_SCA_ID, vsca.SCA_PAT_MED_HIST, vsca.patient_med_hist_list_idx
               FROM AR_SIN_ALERT_PAT_MED_HIST vsca
                INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
                    ON vsca.ARCHIVED_SCA_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
                    
            INSERT into SINGLE_ALERT_PAT_HIST_DRUGS(SINGLE_ALERT_ID,SCA_PAT_HIST_DRUGS,patient_hist_drugs_list_idx) 
        SELECT vsca.ARCHIVED_SCA_ID, vsca.SCA_PAT_HIST_DRUGS, vsca.patient_hist_drugs_list_idx
               FROM AR_SIN_ALERT_PAT_HIST_DRUGS vsca
                INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
                    ON vsca.ARCHIVED_SCA_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
                    
            INSERT into SINGLE_ALERT_BATCH_LOT_NO(SINGLE_ALERT_ID,SCA_BATCH_LOT_NO,batch_lot_no_list_idx) 
        SELECT vsca.ARCHIVED_SCA_ID, vsca.SCA_BATCH_LOT_NO, vsca.batch_lot_no_list_idx
               FROM AR_SIN_ALERT_BATCH_LOT_NO vsca
                INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
                    ON vsca.ARCHIVED_SCA_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
                    
             INSERT into SINGLE_ALERT_CASE_CLASSIFI(SINGLE_ALERT_ID,SCA_CASE_CLASSIFICATION,case_classification_list_idx) 
        SELECT vsca.ARCHIVED_SCA_ID, vsca.SCA_CASE_CLASSIFICATION, vsca.case_classification_list_idx
               FROM AR_SIN_ALERT_CASE_CLASSIFI vsca
                INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
                    ON vsca.ARCHIVED_SCA_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
                    
             INSERT into SINGLE_ALERT_THERAPY_DATES(SINGLE_ALERT_ID,SCA_THERAPY_DATES,therapy_dates_list_idx) 
        SELECT vsca.ARCHIVED_SCA_ID, vsca.SCA_THERAPY_DATES, vsca.therapy_dates_list_idx
               FROM AR_SIN_ALERT_THERAPY_DATES vsca
                INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
                    ON vsca.ARCHIVED_SCA_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
                    
                    
            INSERT into SINGLE_ALERT_DOSE_DETAILS(SINGLE_ALERT_ID,SCA_DOSE_DETAILS,dose_details_list_idx) 
        SELECT vsca.ARCHIVED_SCA_ID, vsca.SCA_DOSE_DETAILS, vsca.dose_details_list_idx
               FROM AR_SIN_ALERT_DOSE_DETAILS vsca
                INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
                    ON vsca.ARCHIVED_SCA_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
                    
                    
            INSERT into SINGLE_ALERT_GENERIC_NAME(SINGLE_ALERT_ID,GENERIC_NAME,GENERIC_NAME_LIST_IDX) 
        SELECT vsca.SINGLE_ALERT_ID, vsca.GENERIC_NAME, vsca.GENERIC_NAME_LIST_IDX
               FROM AR_SINGLE_ALERT_GENERIC_NAME vsca
                INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
                    ON vsca.SINGLE_ALERT_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};

        INSERT into SINGLE_ALERT_ALLPT_OUT_COME(SINGLE_ALERT_ID,ALLPTS_OUTCOME,ALLPTS_OUTCOME_LIST_IDX) 
        SELECT vsca.SINGLE_ALERT_ID, vsca.ALLPTS_OUTCOME, vsca.ALLPTS_OUTCOME_LIST_IDX
               FROM AR_SINGLE_ALERT_ALLPT_OUT_COME vsca
                INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
                    ON vsca.SINGLE_ALERT_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
                    
                   --      Move the attachments to Archived Single Case Alert
    
        MERGE INTO attachment_link al
        USING (SELECT t1.reference_id as reference_id FROM attachment_link t1 left join ARCHIVED_SINGLE_CASE_ALERT t2 on t1.reference_id = t2.id WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId} and t1.reference_class='com.rxlogix.signal.ArchivedSingleCaseAlert') conf
        ON (al.reference_id = conf.reference_id)
        WHEN matched THEN UPDATE SET al.reference_class='com.rxlogix.signal.SingleCaseAlert';
        UPDATE case_history
        SET single_alert_id=archived_single_alert_id,
            archived_single_alert_id = null
        WHERE CONFIG_ID = ${configId} and EXEC_CONFIG_ID  = ${exConfigId};
        
        
        
       DELETE FROM VALIDATED_ARCHIVED_SCA WHERE (ARCHIVED_SCA_ID) in (
       SELECT vsca.ARCHIVED_SCA_ID
       FROM VALIDATED_ARCHIVED_SCA vsca
        INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
            ON vsca.ARCHIVED_SCA_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId}
        );
        DELETE FROM ARCHIVED_SCA_TAGS WHERE (SINGLE_ALERT_ID) in (
       SELECT vsca.SINGLE_ALERT_ID
       FROM ARCHIVED_SCA_TAGS vsca
        INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId}
        );
        DELETE FROM ARCHIVED_SCA_PT WHERE (ARCHIVED_SCA_ID) in (
       SELECT vsca.ARCHIVED_SCA_ID
       FROM ARCHIVED_SCA_PT vsca
        INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
            ON vsca.ARCHIVED_SCA_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId}
        );
        
        DELETE FROM ARCHIVED_SCA_CON_COMIT WHERE (ARCHIVED_SCA_ID) in (
       SELECT vsca.ARCHIVED_SCA_ID
       FROM ARCHIVED_SCA_CON_COMIT vsca
        INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
            ON vsca.ARCHIVED_SCA_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId}
        );
        
        DELETE FROM ARCHIVED_SCA_SUSP_PROD WHERE (ARCHIVED_SCA_ID) in (
       SELECT vsca.ARCHIVED_SCA_ID
       FROM ARCHIVED_SCA_SUSP_PROD vsca
        INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
            ON vsca.ARCHIVED_SCA_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId}
        );
        
        DELETE FROM ARCHIVED_SCA_MED_ERR_PT_LIST WHERE (ARCHIVED_SCA_ID) in (
       SELECT vsca.ARCHIVED_SCA_ID
       FROM ARCHIVED_SCA_MED_ERR_PT_LIST vsca
        INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
            ON vsca.ARCHIVED_SCA_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId}
        );
        
        DELETE FROM ARCHIVED_SCA_ACTIONS WHERE (ARCHIVED_SCA_ID) in (
       SELECT vsca.ARCHIVED_SCA_ID
       FROM ARCHIVED_SCA_ACTIONS vsca
        INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
            ON vsca.ARCHIVED_SCA_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId});
    
        DELETE FROM ARCHIVED_single_case_alert WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
        DELETE FROM CASE_HISTORY where EXEC_CONFIG_ID = ${failedExConfigId};
        DELETE FROM ex_rconfig_activities where EX_CONFIG_ACTIVITIES_ID = ${failedExConfigId};
        DELETE
      FROM SINGLE_CASE_ALERT_TAGS
      WHERE PVS_ALERT_TAG_ID in (
        select ID
        from PVS_ALERT_TAG
        WHERE exec_config_id = ${failedExConfigId}
      );

      DELETE
      FROM PVS_GLOBAL_TAG
      where EXEC_CONFIG_ID = ${failedExConfigId}
        and DOMAIN = 'Single Case Alert'
        and CREATED_AT >
            (select DATE_CREATED from EX_RCONFIG where ID = ${failedExConfigId});
      
      DELETE
      FROM PVS_ALERT_TAG
      where EXEC_CONFIG_ID = ${failedExConfigId}
        AND DOMAIN = 'Single Case Alert';
      
      DELETE FROM SINGLE_CASE_ALERT WHERE EXEC_CONFIG_ID = ${failedExConfigId} and ALERT_CONFIGURATION_ID = ${configId};



      exception when others
      then
     raise_application_error(-20001,sqlerrm||'->'||dbms_utility.format_error_backtrace);
        END;           
        """
    }

    static agg_data_clean_for_failed_execution = { Long configId, Long exConfigId, Long failedExConfigId ->
        """
            
declare
       lvc_sql VARCHAR2(32000);
       lvc_exec_sql VARCHAR2(32000);
       BEGIN
       SELECT listagg(column_name,',') within group (order by column_id) as cols
       INTO lvc_sql
       FROM USER_TAB_COLUMNS
       WHERE table_name = 'AGG_ALERT';
       lvc_exec_sql := 'INSERT into AGG_ALERT ('||lvc_sql||') SELECT '||lvc_sql||' FROM ARCHIVED_AGG_ALERT WHERE ALERT_CONFIGURATION_ID = ${
            configId
        } and EXEC_CONFIGURATION_ID = ${exConfigId}';
       execute immediate lvc_exec_sql;
      
       INSERT into VALIDATED_AGG_ALERTS(AGG_ALERT_ID,VALIDATED_SIGNAL_ID) SELECT vaca.ARCHIVED_ACA_ID, vaca.VALIDATED_SIGNAL_ID
       FROM VALIDATED_ARCHIVED_ACA vaca
        INNER JOIN ARCHIVED_AGG_ALERT aca
            ON vaca.ARCHIVED_ACA_ID = aca.ID
        WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${exConfigId};
        
        INSERT into AGG_CASE_ALERT_TAGS(AGG_ALERT_ID,PVS_ALERT_TAG_ID)
 SELECT vaca.AGG_ALERT_ID, vaca.PVS_ALERT_TAG_ID
       FROM ARCHIVED_AGG_CASE_ALERT_TAGS vaca
        INNER JOIN ARCHIVED_AGG_ALERT aca
            ON vaca.AGG_ALERT_ID = aca.ID
        WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${exConfigId};
        
        INSERT into AGG_ALERT_ACTIONS(AGG_ALERT_ID,ACTION_ID) SELECT vaca.ARCHIVED_ACA_ID, vaca.ACTION_ID
       FROM ARCHIVED_ACA_ACTIONS vaca
        INNER JOIN ARCHIVED_AGG_ALERT aca
            ON vaca.ARCHIVED_ACA_ID = aca.ID
        WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${exConfigId};
        
MERGE INTO attachment_link al
USING (SELECT t1.reference_id as reference_id FROM attachment_link t1 left join ARCHIVED_AGG_ALERT t2 on t1.reference_id = t2.id WHERE ALERT_CONFIGURATION_ID = ${
            configId
        } and EXEC_CONFIGURATION_ID = ${exConfigId} and t1.reference_class='com.rxlogix.signal.ArchivedAggregateCaseAlert') conf
ON (al.reference_id = conf.reference_id)
WHEN matched THEN UPDATE SET al.reference_class='com.rxlogix.signal.AggregateCaseAlert';
       
        UPDATE product_event_history
        SET archived_agg_case_alert_id=null,
            AGG_CASE_ALERT_ID = AGG_CASE_ALERT_ID
        WHERE CONFIG_ID = ${configId} and EXEC_CONFIG_ID =${exConfigId};
      
      
       DELETE FROM VALIDATED_ARCHIVED_ACA WHERE (ARCHIVED_ACA_ID) in (
       SELECT vaca.ARCHIVED_ACA_ID
       FROM VALIDATED_ARCHIVED_ACA vaca
        INNER JOIN ARCHIVED_AGG_ALERT aca
            ON vaca.ARCHIVED_ACA_ID = aca.ID
        WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${exConfigId});
        
       
         DELETE FROM ARCHIVED_AGG_CASE_ALERT_TAGS WHERE (AGG_ALERT_ID) in (
       SELECT vaca.AGG_ALERT_ID
       FROM ARCHIVED_AGG_CASE_ALERT_TAGS vaca
        INNER JOIN ARCHIVED_AGG_ALERT aca
            ON vaca.AGG_ALERT_ID = aca.ID
        WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${exConfigId});
        
        DELETE FROM ARCHIVED_ACA_ACTIONS WHERE (ARCHIVED_ACA_ID) in (
        SELECT vaca.ARCHIVED_ACA_ID
       FROM ARCHIVED_ACA_ACTIONS vaca
        INNER JOIN ARCHIVED_AGG_ALERT aca
            ON vaca.ARCHIVED_ACA_ID = aca.ID
        WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${exConfigId}
        );
        
      
      DELETE FROM ARCHIVED_AGG_ALERT WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID =${exConfigId};
      DELETE FROM PRODUCT_EVENT_HISTORY WHERE EXEC_CONFIG_ID = ${failedExConfigId};
      DELETE FROM ex_rconfig_activities where EX_CONFIG_ACTIVITIES_ID = ${failedExConfigId};
        DELETE
      FROM AGG_GLOBAL_TAGS
      WHERE PVS_GLOBAL_TAG_ID in (select agt.PVS_GLOBAL_TAG_ID
                                  from AGG_GLOBAL_TAGS agt
                                         inner join PVS_GLOBAL_TAG pgt on agt.PVS_GLOBAL_TAG_ID = pgt.ID and
                                                                          pgt.EXEC_CONFIG_ID = ${failedExConfigId} and
                                                                          pgt.DOMAIN = 'Aggregate Case Alert'
                                    and agt.CREATION_DATE >
                                        (select DATE_CREATED from EX_RCONFIG where ID = ${failedExConfigId})
      );

      DELETE
      FROM AGG_CASE_ALERT_TAGS
      WHERE PVS_ALERT_TAG_ID in (
        select ID
        from PVS_ALERT_TAG
        WHERE exec_config_id = ${failedExConfigId}
      );

      DELETE
      FROM PVS_GLOBAL_TAG
      where EXEC_CONFIG_ID = ${failedExConfigId}
        and DOMAIN = 'Aggregate Case Alert'
        and CREATED_AT >
            (select DATE_CREATED from EX_RCONFIG where ID = ${failedExConfigId});

      DELETE
      FROM PVS_ALERT_TAG
      where EXEC_CONFIG_ID = ${failedExConfigId}
        AND DOMAIN = 'Aggregate Case Alert';   

      DELETE FROM AGG_ALERT WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID =${failedExConfigId};  
      exception when others
      then
      raise_application_error(-20001,sqlerrm||'->'||dbms_utility.format_error_backtrace);
        END;
"""
}
    static retrieve_case_details_sections_mapping_info = { ->
        """
            select cds.ID, cds.SECTION_NAME, cds.SECTION_POSITION, cds.UD_SECTION_NAME, cds.SECTION_KEY, cds.IS_FULL_TEXT, cdfm.UI_LABEl , cdfm.FIELD_VARIABLE
            from
                case_details_section cds,
                case_details_field_mapping cdfm
            where
                cds.section_name = cdfm.section_name
                and cds.flag_enable = 1
                order by cds.section_position

        """
    }
}
