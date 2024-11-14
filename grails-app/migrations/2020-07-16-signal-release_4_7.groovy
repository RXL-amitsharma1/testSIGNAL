databaseChangeLog = {

    changeSet(author: "amit (generated)", id: "1594893043821-1") {
        createSequence(sequenceName: "agg_on_demand_sequence")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-2") {
        createSequence(sequenceName: "agg_sequence")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-3") {
        createSequence(sequenceName: "config_sequence")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-4") {
        createSequence(sequenceName: "evdas_config_sequence")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-5") {
        createSequence(sequenceName: "evdas_exec_config_sequence")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-6") {
        createSequence(sequenceName: "evdas_on_demand_sequence")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-7") {
        createSequence(sequenceName: "evdas_sequence")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-8") {
        createSequence(sequenceName: "exec_config_sequence")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-9") {
        createSequence(sequenceName: "hibernate_sequence")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-10") {
        createSequence(sequenceName: "sca_sequence")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-11") {
        createSequence(sequenceName: "single_on_demand_sequence")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-12") {
        createTable(tableName: "ACCESS_CONTROL_GROUP") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ACCESS_CONTROL_GROUPPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "DESCRIPTION", type: "VARCHAR2(200 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "LDAP_GROUP_NAME", type: "VARCHAR2(30 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(30 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-13") {
        createTable(tableName: "ACTIONS") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ACTIONSPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_status", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "alert_type", type: "VARCHAR2(255 CHAR)")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "comments", type: "VARCHAR2(4000 CHAR)")

            column(name: "completed_date", type: "TIMESTAMP")

            column(name: "config_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "details", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "due_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "guest_attendee_email", type: "VARCHAR2(255 CHAR)")

            column(name: "meeting_id", type: "VARCHAR2(255 CHAR)")

            column(name: "owner_id", type: "NUMBER(19, 0)")

            column(name: "type_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "viewed", type: "NUMBER(1, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-14") {
        createTable(tableName: "ACTION_CONFIGURATIONS") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ACTION_CONFIGURATIONSPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(255 CHAR)")

            column(name: "description_local", type: "VARCHAR2(255 CHAR)")

            column(name: "display_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "display_name_local", type: "VARCHAR2(255 CHAR)")

            column(name: "is_email_enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-15") {
        createTable(tableName: "ACTION_TEMPLATE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ACTION_TEMPLATEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_json", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(255 CHAR)")

            column(name: "guest_attendee_email", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-16") {
        createTable(tableName: "ACTION_TYPES") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ACTION_TYPESPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "description_local", type: "VARCHAR2(255 CHAR)")

            column(name: "display_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "display_name_local", type: "VARCHAR2(255 CHAR)")

            column(name: "value", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-17") {
        createTable(tableName: "ACTIVITIES") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ACTIVITIESPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "attributes", type: "CLOB")

            column(name: "case_number", type: "VARCHAR2(255 CHAR)")

            column(name: "client_name", type: "VARCHAR2(255 CHAR)")

            column(name: "DETAILS", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "display", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "event_name", type: "VARCHAR2(255 CHAR)")

            column(name: "guest_attendee_email", type: "VARCHAR2(255 CHAR)")

            column(name: "ip_address", type: "VARCHAR2(255 CHAR)")

            column(name: "JUSTIFICATION", type: "VARCHAR2(4000 CHAR)")

            column(name: "performed_by_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "private_user_name", type: "VARCHAR2(255 CHAR)")

            column(name: "suspect_product", type: "VARCHAR2(255 CHAR)")

            column(name: "timestamp", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "type_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "activities_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-18") {
        createTable(tableName: "ACTIVITY_TYPE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ACTIVITY_TYPEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-19") {
        createTable(tableName: "ADHOC_ALERT_ACTIONS") {
            column(name: "ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-20") {
        createTable(tableName: "ADHOC_ALERT_ACTION_TAKEN") {
            column(name: "ADHOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_TAKEN", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-21") {
        createTable(tableName: "ADHOC_ALERT_COMMENTS") {
            column(name: "ADHOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COMMENT_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-22") {
        createTable(tableName: "ADVANCED_FILTER") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ADVANCED_FILTERPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "alert_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "criteria", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(4000 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-23") {
        createTable(tableName: "AD_HOC_SIGNAL_CONCEPTS") {
            column(name: "AD_HOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-24") {
        createTable(tableName: "AD_HOC_TOPIC_CONCEPTS") {
            column(name: "AD_HOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-25") {
        createTable(tableName: "AEVAL_TYPE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "AEVAL_TYPEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(255 CHAR)")

            column(name: "description_local", type: "VARCHAR2(255 CHAR)")

            column(name: "display", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "display_name", type: "VARCHAR2(255 CHAR)")

            column(name: "display_name_local", type: "VARCHAR2(255 CHAR)")

            column(name: "value", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-26") {
        createTable(tableName: "AGG_ALERT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_count", type: "NUMBER(10, 0)")

            column(name: "adhoc_run", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_configuration_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "chi_square", type: "FLOAT(24)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "cum_fatal_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "cum_serious_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "cum_spon_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "cum_study_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "cumm_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "cumm_interacting_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "cumm_pediatric_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "detected_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "due_date", type: "TIMESTAMP")

            column(name: "eb05", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "eb05age", type: "CLOB")

            column(name: "eb05gender", type: "CLOB")

            column(name: "eb05str", type: "CLOB")

            column(name: "eb95", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "eb95age", type: "CLOB")

            column(name: "eb95gender", type: "CLOB")

            column(name: "eb95str", type: "CLOB")

            column(name: "ebgm", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "ebgm_age", type: "CLOB")

            column(name: "ebgm_gender", type: "CLOB")

            column(name: "ebgm_str", type: "CLOB")

            column(name: "exec_configuration_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "flagged", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "flags", type: "VARCHAR2(255 CHAR)")

            column(name: "format", type: "varchar2(8000 char)")

            column(name: "freq_priority", type: "VARCHAR2(255 CHAR)")

            column(name: "global_identity_id", type: "NUMBER(19, 0)")

            column(name: "imp_events", type: "VARCHAR2(255 CHAR)")

            column(name: "initial_disposition_id", type: "NUMBER(19, 0)")

            column(name: "initial_due_date", type: "TIMESTAMP")

            column(name: "is_new", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "listed", type: "VARCHAR2(255 CHAR)")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "new_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "new_fatal_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "new_interacting_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "new_pediatric_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "new_serious_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "new_spon_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "new_study_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "pec_imp_high", type: "VARCHAR2(255 CHAR)")

            column(name: "pec_imp_low", type: "VARCHAR2(255 CHAR)")

            column(name: "period_end_date", type: "TIMESTAMP")

            column(name: "period_start_date", type: "TIMESTAMP")

            column(name: "positive_dechallenge", type: "VARCHAR2(255 CHAR)")

            column(name: "positive_rechallenge", type: "VARCHAR2(255 CHAR)")

            column(name: "pregenency", type: "VARCHAR2(255 CHAR)")

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_id", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "prr05", type: "FLOAT(24)")

            column(name: "prr95", type: "FLOAT(24)")

            column(name: "prr_mh", type: "VARCHAR2(255 CHAR)")

            column(name: "PRR_STR", type: "CLOB")

            column(name: "prr_str05", type: "VARCHAR2(255 CHAR)")

            column(name: "prr_str95", type: "VARCHAR2(255 CHAR)")

            column(name: "prr_value", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "pt", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "pt_code", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "related", type: "VARCHAR2(255 CHAR)")

            column(name: "requested_by", type: "VARCHAR2(255 CHAR)")

            column(name: "review_date", type: "TIMESTAMP")

            column(name: "ror05", type: "FLOAT(24)")

            column(name: "ror95", type: "FLOAT(24)")

            column(name: "ror_mh", type: "VARCHAR2(255 CHAR)")

            column(name: "ROR_STR", type: "CLOB")

            column(name: "ror_str05", type: "VARCHAR2(255 CHAR)")

            column(name: "ror_str95", type: "VARCHAR2(255 CHAR)")

            column(name: "ror_value", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "smq_code", type: "VARCHAR2(255 CHAR)")

            column(name: "soc", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "trend_type", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-27") {
        createTable(tableName: "AGG_ALERT_ACTIONS") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-28") {
        createTable(tableName: "AGG_ALERT_IMP_EVENT_LIST") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AGA_IMP_EVENTS", type: "VARCHAR2(255 CHAR)")

            column(name: "agg_imp_event_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-29") {
        createTable(tableName: "AGG_ALERT_TAGS") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ALERT_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-30") {
        createTable(tableName: "AGG_CASE_ALERT_TAGS") {
            column(name: "PVS_ALERT_TAG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-31") {
        createTable(tableName: "AGG_GLOBAL_TAGS") {
            column(name: "PVS_GLOBAL_TAG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GLOBAL_PRODUCT_EVENT_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-32") {
        createTable(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_configuration_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "chi_square", type: "FLOAT(24)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "cum_fatal_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "cum_serious_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "cum_spon_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "cum_study_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "eb05", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "eb05age", type: "CLOB")

            column(name: "eb05gender", type: "CLOB")

            column(name: "eb95", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "eb95age", type: "CLOB")

            column(name: "eb95gender", type: "CLOB")

            column(name: "ebgm", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "ebgm_age", type: "CLOB")

            column(name: "ebgm_gender", type: "CLOB")

            column(name: "exec_configuration_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "flagged", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "listed", type: "VARCHAR2(255 CHAR)")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "new_fatal_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "new_serious_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "new_spon_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "new_study_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "period_end_date", type: "TIMESTAMP")

            column(name: "period_start_date", type: "TIMESTAMP")

            column(name: "positive_dechallenge", type: "VARCHAR2(255 CHAR)")

            column(name: "positive_rechallenge", type: "VARCHAR2(255 CHAR)")

            column(name: "pregnancy", type: "VARCHAR2(255 CHAR)")

            column(name: "product_id", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "prr05", type: "FLOAT(24)")

            column(name: "prr95", type: "FLOAT(24)")

            column(name: "prr_value", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "pt", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "pt_code", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "related", type: "VARCHAR2(255 CHAR)")

            column(name: "ror05", type: "FLOAT(24)")

            column(name: "ror95", type: "FLOAT(24)")

            column(name: "ror_value", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "smq_code", type: "VARCHAR2(255 CHAR)")

            column(name: "soc", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-33") {
        createTable(tableName: "AGG_SIGNAL_CONCEPTS") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-34") {
        createTable(tableName: "AGG_TOPIC_CONCEPTS") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-35") {
        createTable(tableName: "ALERTS") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ALERTSPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_configuration_id", type: "NUMBER(19, 0)")

            column(name: "alert_id", type: "NUMBER(19, 0)")

            column(name: "alert_version", type: "NUMBER(19, 0)")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "attributes", type: "CLOB")

            column(name: "case_number", type: "VARCHAR2(255 CHAR)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "detected_date", type: "TIMESTAMP")

            column(name: "disposition_id", type: "NUMBER(19, 0)")

            column(name: "due_date", type: "TIMESTAMP")

            column(name: "event_selection", type: "varchar2(8000 char)")

            column(name: "exec_config_id", type: "NUMBER(19, 0)")

            column(name: "flagged", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "notes", type: "VARCHAR2(4000 CHAR)")

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "review_date", type: "TIMESTAMP")

            column(name: "alert_type", type: "VARCHAR2(20 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "AGG_END_DATE", type: "TIMESTAMP")

            column(name: "AGG_START_DATE", type: "TIMESTAMP")

            column(name: "alert_rmp_rems_ref", type: "VARCHAR2(255 CHAR)")

            column(name: "comment_signal_status", type: "VARCHAR2(4000 CHAR)")

            column(name: "country_of_incidence", type: "VARCHAR2(255 CHAR)")

            column(name: "description", type: "VARCHAR2(4000 CHAR)")

            column(name: "detected_by", type: "VARCHAR2(255 CHAR)")

            column(name: "formulations", type: "VARCHAR2(255 CHAR)")

            column(name: "ha_date_closed", type: "TIMESTAMP")

            column(name: "ha_signal_status_id", type: "NUMBER(19, 0)")

            column(name: "indication", type: "VARCHAR2(255 CHAR)")

            column(name: "initial_data_source", type: "VARCHAR2(255 CHAR)")

            column(name: "initial_disposition_id", type: "NUMBER(19, 0)")

            column(name: "initial_due_date", type: "TIMESTAMP")

            column(name: "issue_previously_tracked", type: "NUMBER(1, 0)")

            column(name: "last_decision_date", type: "TIMESTAMP")

            column(name: "last_updated_note", type: "TIMESTAMP")

            column(name: "name", type: "VARCHAR2(4000 CHAR)")

            column(name: "note_modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "number_oficsrs", type: "NUMBER(10, 0)")

            column(name: "owner_id", type: "NUMBER(19, 0)")

            column(name: "product_dictionary_selection", type: "VARCHAR2(255 CHAR)")

            column(name: "product_selection", type: "VARCHAR2(4000 CHAR)")

            column(name: "public_alert", type: "NUMBER(1, 0)")

            column(name: "reason_for_delay", type: "VARCHAR2(255 CHAR)")

            column(name: "ref_type", type: "VARCHAR2(255 CHAR)")

            column(name: "reference_number", type: "VARCHAR2(255 CHAR)")

            column(name: "report_type", type: "VARCHAR2(255 CHAR)")

            column(name: "shared_with_id", type: "NUMBER(19, 0)")

            column(name: "study_selection", type: "VARCHAR2(4000 CHAR)")

            column(name: "topic", type: "VARCHAR2(4000 CHAR)")

            column(name: "WORKFLOW_GROUP", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-36") {
        createTable(tableName: "ALERT_COMMENT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ALERT_COMMENTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "article_id", type: "VARCHAR2(255 CHAR)")

            column(name: "case_number", type: "VARCHAR2(255 CHAR)")

            column(name: "COMMENTS", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "config_id", type: "NUMBER(19, 0)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "event_name", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "product_family", type: "VARCHAR2(255 CHAR)")

            column(name: "product_id", type: "NUMBER(10, 0)")

            column(name: "product_name", type: "VARCHAR2(255 CHAR)")

            column(name: "pt_code", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-37") {
        createTable(tableName: "ALERT_DATE_RANGE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ALERT_DATE_RANGEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_END_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_END_DELTA", type: "NUMBER(10, 0)")

            column(name: "DATE_RNG_ENUM", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_START_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_START_DELTA", type: "NUMBER(10, 0)")

            column(name: "RELATIVE_DATE_RNG_VALUE", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-38") {
        createTable(tableName: "ALERT_DOCUMENT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ALERT_DOCUMENTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_id", type: "NUMBER(19, 0)")

            column(name: "author", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "chronicle_id", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "comments", type: "VARCHAR2(255 CHAR)")

            column(name: "document_link", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "document_status", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "document_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "product_name", type: "VARCHAR2(4000 CHAR)")

            column(name: "start_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "status_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "target_date", type: "TIMESTAMP")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-39") {
        createTable(tableName: "ALERT_QUERY_VALUES") {
            column(name: "ALERT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY_VALUE_ID", type: "NUMBER(19, 0)")

            column(name: "QUERY_VALUE_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-40") {
        createTable(tableName: "ALERT_TAG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ALERT_TAGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by_id", type: "NUMBER(19, 0)")

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-41") {
        createTable(tableName: "ALLOWED_DICTIONARY_CACHE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ALLOWED_DICTIONARY_CACHEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "allowed_data", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "allowed_data_ids", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "field_level_id", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_product", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "label", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "product_dictionary_cache_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-42") {
        createTable(tableName: "ARCHIVED_ACA_ACTIONS") {
            column(name: "ARCHIVED_ACA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-43") {
        createTable(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ARCHIVED_AGG_ALERTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_count", type: "NUMBER(10, 0)")

            column(name: "adhoc_run", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_configuration_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "chi_square", type: "FLOAT(24)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "cum_fatal_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "cum_serious_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "cum_spon_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "cum_study_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "cumm_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "cumm_interacting_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "cumm_pediatric_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "detected_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "due_date", type: "TIMESTAMP")

            column(name: "eb05", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "eb05age", type: "CLOB")

            column(name: "eb05gender", type: "CLOB")

            column(name: "eb05str", type: "CLOB")

            column(name: "eb95", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "eb95age", type: "CLOB")

            column(name: "eb95gender", type: "CLOB")

            column(name: "eb95str", type: "CLOB")

            column(name: "ebgm", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "ebgm_age", type: "CLOB")

            column(name: "ebgm_gender", type: "CLOB")

            column(name: "ebgm_str", type: "CLOB")

            column(name: "exec_configuration_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "flagged", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "flags", type: "VARCHAR2(255 CHAR)")

            column(name: "format", type: "VARCHAR2(255 CHAR)")

            column(name: "freq_priority", type: "VARCHAR2(255 CHAR)")

            column(name: "global_identity_id", type: "NUMBER(19, 0)")

            column(name: "imp_events", type: "VARCHAR2(255 CHAR)")

            column(name: "initial_disposition_id", type: "NUMBER(19, 0)")

            column(name: "initial_due_date", type: "TIMESTAMP")

            column(name: "is_new", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "listed", type: "VARCHAR2(255 CHAR)")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "new_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "new_fatal_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "new_interacting_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "new_pediatric_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "new_serious_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "new_spon_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "new_study_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "pec_imp_high", type: "VARCHAR2(255 CHAR)")

            column(name: "pec_imp_low", type: "VARCHAR2(255 CHAR)")

            column(name: "period_end_date", type: "TIMESTAMP")

            column(name: "period_start_date", type: "TIMESTAMP")

            column(name: "positive_dechallenge", type: "VARCHAR2(255 CHAR)")

            column(name: "positive_rechallenge", type: "VARCHAR2(255 CHAR)")

            column(name: "pregenency", type: "VARCHAR2(255 CHAR)")

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_id", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "prr05", type: "FLOAT(24)")

            column(name: "prr95", type: "FLOAT(24)")

            column(name: "prr_mh", type: "VARCHAR2(255 CHAR)")

            column(name: "PRR_STR", type: "CLOB")

            column(name: "prr_str05", type: "VARCHAR2(255 CHAR)")

            column(name: "prr_str95", type: "VARCHAR2(255 CHAR)")

            column(name: "prr_value", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "pt", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "pt_code", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "related", type: "VARCHAR2(255 CHAR)")

            column(name: "requested_by", type: "VARCHAR2(255 CHAR)")

            column(name: "review_date", type: "TIMESTAMP")

            column(name: "ror05", type: "FLOAT(24)")

            column(name: "ror95", type: "FLOAT(24)")

            column(name: "ror_mh", type: "VARCHAR2(255 CHAR)")

            column(name: "ROR_STR", type: "CLOB")

            column(name: "ror_str05", type: "VARCHAR2(255 CHAR)")

            column(name: "ror_str95", type: "VARCHAR2(255 CHAR)")

            column(name: "ror_value", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "smq_code", type: "VARCHAR2(255 CHAR)")

            column(name: "soc", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "trend_type", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-44") {
        createTable(tableName: "ARCHIVED_AGG_CASE_ALERT_TAGS") {
            column(name: "PVS_ALERT_TAG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-45") {
        createTable(tableName: "ARCHIVED_EA_IMP_EVENT_LIST") {
            column(name: "ARCHIVED_EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EVDAS_IMP_EVENTS", type: "VARCHAR2(255 CHAR)")

            column(name: "ev_imp_event_list_idx", type: "NUMBER(10, 0)")
        }

    }

    changeSet(author: "amit (generated)", id: "1594893043821-46") {
        createTable(tableName: "ARCHIVED_EVDAS_ALERT") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ARCHIVED_EVDAS_ALERTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_count", type: "NUMBER(10, 0)")

            column(name: "adhoc_run", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_configuration_id", type: "NUMBER(19, 0)")

            column(name: "all_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "asia_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "attributes", type: "CLOB")

            column(name: "changes", type: "VARCHAR2(255 CHAR)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "detected_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DME_IME", type: "VARCHAR2(255 CHAR)")

            column(name: "due_date", type: "TIMESTAMP")

            column(name: "europe_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "exec_configuration_id", type: "NUMBER(19, 0)")

            column(name: "flagged", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "flags", type: "VARCHAR2(255 CHAR)")

            column(name: "format", type: "varchar2(8000 char)")

            column(name: "frequency", type: "VARCHAR2(255 CHAR)")

            column(name: "hlgt", type: "VARCHAR2(255 CHAR)")

            column(name: "hlt", type: "VARCHAR2(255 CHAR)")

            column(name: "imp_events", type: "VARCHAR2(255 CHAR)")

            column(name: "initial_disposition_id", type: "NUMBER(19, 0)")

            column(name: "initial_due_date", type: "TIMESTAMP")

            column(name: "is_new", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "japan_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "listedness", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "new_eea", type: "VARCHAR2(255 CHAR)")

            column(name: "new_ev", type: "NUMBER(10, 0)")

            column(name: "new_ev_link", type: "VARCHAR2(600 CHAR)")

            column(name: "new_fatal", type: "NUMBER(10, 0)")

            column(name: "new_geria", type: "VARCHAR2(255 CHAR)")

            column(name: "new_hcp", type: "VARCHAR2(255 CHAR)")

            column(name: "new_lit", type: "VARCHAR2(255 CHAR)")

            column(name: "new_med_err", type: "VARCHAR2(255 CHAR)")

            column(name: "new_obs", type: "VARCHAR2(255 CHAR)")

            column(name: "new_paed", type: "VARCHAR2(255 CHAR)")

            column(name: "new_rc", type: "VARCHAR2(255 CHAR)")

            column(name: "new_serious", type: "NUMBER(10, 0)")

            column(name: "new_spont", type: "VARCHAR2(255 CHAR)")

            column(name: "north_america_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "period_end_date", type: "TIMESTAMP")

            column(name: "period_start_date", type: "TIMESTAMP")

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "pt", type: "VARCHAR2(255 CHAR)")

            column(name: "pt_code", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ratio_ror_geriatr_vs_others", type: "VARCHAR2(255 CHAR)")

            column(name: "ratio_ror_paed_vs_others", type: "VARCHAR2(255 CHAR)")

            column(name: "requested_by", type: "VARCHAR2(255 CHAR)")

            column(name: "rest_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "review_date", type: "TIMESTAMP")

            column(name: "ror_value", type: "VARCHAR2(255 CHAR)")

            column(name: "sdr", type: "VARCHAR2(255 CHAR)")

            column(name: "sdr_geratr", type: "VARCHAR2(255 CHAR)")

            column(name: "sdr_paed", type: "VARCHAR2(255 CHAR)")

            column(name: "smq_narrow", type: "VARCHAR2(255 CHAR)")

            column(name: "soc", type: "VARCHAR2(255 CHAR)")

            column(name: "substance", type: "VARCHAR2(255 CHAR)")

            column(name: "substance_id", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "tot_eea", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_geria", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_hcp", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_med_err", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_obs", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_paed", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_rc", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_spont", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_spont_asia", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_spont_europe", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_spont_japan", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_spontnamerica", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_spont_rest", type: "VARCHAR2(255 CHAR)")

            column(name: "total_ev", type: "NUMBER(10, 0)")

            column(name: "total_ev_link", type: "VARCHAR2(600 CHAR)")

            column(name: "total_fatal", type: "NUMBER(10, 0)")

            column(name: "total_lit", type: "VARCHAR2(255 CHAR)")

            column(name: "total_serious", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-47") {
        createTable(tableName: "ARCHIVED_EVDAS_ALERT_ACTIONS") {
            column(name: "ARCHIVED_EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }

    }

    changeSet(author: "amit (generated)", id: "1594893043821-48") {
        createTable(tableName: "ARCHIVED_IMP_EVENT_LIST") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AGA_IMP_EVENTS", type: "VARCHAR2(255 CHAR)")

            column(name: "agg_imp_event_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-49") {
        createTable(tableName: "ARCHIVED_LITERATURE_ALERT") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ARCHIVED_LITERATURE_ALERTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_count", type: "NUMBER(10, 0)")

            column(name: "ARTICLE_ABSTRACT", type: "CLOB")

            column(name: "ARTICLE_AUTHORS", type: "CLOB")

            column(name: "article_id", type: "NUMBER(10, 0)")

            column(name: "ARTICLE_TITLE", type: "CLOB")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EVENT_SELECTION", type: "CLOB")

            column(name: "ex_lit_search_config_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "global_identity_id", type: "NUMBER(19, 0)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "lit_search_config_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PRODUCT_SELECTION", type: "CLOB")

            column(name: "PUBLICATION_DATE", type: "VARCHAR2(255 CHAR)")

            column(name: "search_string", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-50") {
        createTable(tableName: "ARCHIVED_LIT_ALERT_ACTIONS") {
            column(name: "ARCHIVED_LIT_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-51") {
        createTable(tableName: "ARCHIVED_LIT_CASE_ALERT_TAGS") {
            column(name: "PVS_ALERT_TAG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ARCHIVED_LIT_ALERT_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-52") {
        createTable(tableName: "ARCHIVED_SCA_ACTIONS") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-53") {
        createTable(tableName: "ARCHIVED_SCA_CON_COMIT") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ALERT_CON_COMIT", type: "CLOB")

            column(name: "con_comit_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-54") {
        createTable(tableName: "ARCHIVED_SCA_MED_ERR_PT_LIST") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_MED_ERROR", type: "CLOB")

            column(name: "med_error_pt_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-55") {
        createTable(tableName: "ARCHIVED_SCA_PT") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ARCHIVED_SCA_PT", type: "CLOB")

            column(name: "pt_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-56") {
        createTable(tableName: "ARCHIVED_SCA_SUSP_PROD") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_PRODUCT_NAME", type: "CLOB")

            column(name: "suspect_product_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-57") {
        createTable(tableName: "ARCHIVED_SCA_TAGS") {
            column(name: "PVS_ALERT_TAG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-58") {
        createTable(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ARCHIVED_SINGLE_CASE_ALERTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_count", type: "NUMBER(10, 0)")

            column(name: "adhoc_run", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "age", type: "VARCHAR2(255 CHAR)")

            column(name: "agg_alert_id", type: "VARCHAR2(255 CHAR)")

            column(name: "agg_count_type", type: "VARCHAR2(255 CHAR)")

            column(name: "agg_execution_id", type: "VARCHAR2(255 CHAR)")

            column(name: "alert_configuration_id", type: "NUMBER(19, 0)")

            column(name: "app_type_and_num", type: "VARCHAR2(255 CHAR)")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "attributes", type: "CLOB")

            column(name: "badge", type: "VARCHAR2(255 CHAR)")

            column(name: "case_id", type: "NUMBER(19, 0)")

            column(name: "case_init_receipt_date", type: "TIMESTAMP")

            column(name: "CASE_NARRATIVE", type: "CLOB")

            column(name: "case_number", type: "VARCHAR2(255 CHAR)")

            column(name: "case_report_type", type: "VARCHAR2(255 CHAR)")

            column(name: "case_type", type: "VARCHAR2(255 CHAR)")

            column(name: "case_version", type: "NUMBER(10, 0)")

            column(name: "completeness_score", type: "FLOAT(24)")

            column(name: "compounding_flag", type: "VARCHAR2(4000 CHAR)")

            column(name: "con_comit", type: "CLOB")

            column(name: "CON_MEDS", type: "CLOB")

            column(name: "country", type: "VARCHAR2(255 CHAR)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "death", type: "VARCHAR2(255 CHAR)")

            column(name: "detected_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "due_date", type: "TIMESTAMP")

            column(name: "exec_config_id", type: "NUMBER(19, 0)")

            column(name: "flagged", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "follow_up_exists", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "follow_up_number", type: "NUMBER(10, 0)")

            column(name: "gender", type: "VARCHAR2(255 CHAR)")

            column(name: "global_identity_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ind_number", type: "VARCHAR2(255 CHAR)")

            column(name: "initial_disposition_id", type: "NUMBER(19, 0)")

            column(name: "initial_due_date", type: "TIMESTAMP")

            column(name: "is_case_series", type: "NUMBER(1, 0)")

            column(name: "is_duplicate", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_new", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "listedness", type: "VARCHAR2(255 CHAR)")

            column(name: "locked_date", type: "TIMESTAMP")

            column(name: "master_pref_term_all", type: "VARCHAR2(2000 CHAR)")

            column(name: "MED_ERRORS_PT", type: "CLOB")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "outcome", type: "VARCHAR2(255 CHAR)")

            column(name: "patient_age", type: "FLOAT(24)")

            column(name: "period_end_date", type: "TIMESTAMP")

            column(name: "period_start_date", type: "TIMESTAMP")

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_family", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "product_id", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "pt", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "rechallenge", type: "VARCHAR2(255 CHAR)")

            column(name: "reporters_hcp_flag", type: "VARCHAR2(255 CHAR)")

            column(name: "review_date", type: "TIMESTAMP")

            column(name: "serious", type: "VARCHAR2(255 CHAR)")

            column(name: "submitter", type: "VARCHAR2(255 CHAR)")

            column(name: "superseded_flag", type: "VARCHAR2(255 CHAR)")

            column(name: "SUSP_PROD", type: "CLOB")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-59") {
        createTable(tableName: "ATTACHMENT_DESCRIPTION") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ATTACHMENT_DESCRIPTIONPK")
            }

            column(name: "attachment_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-60") {
        createTable(tableName: "BUSINESS_CONFIGURATION") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "BUSINESS_CONFIGURATIONPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "data_source", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(255 CHAR)")

            column(name: "enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_global_rule", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "product_dictionary_selection", type: "VARCHAR2(255 CHAR)")

            column(name: "PRODUCTS", type: "CLOB")

            column(name: "rule_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-61") {
        createTable(tableName: "CASE_COLUMN_JOIN_MAPPING") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "CASE_COLUMN_JOIN_MAPPINGPK")
            }

            column(name: "COLUMN_NAME", type: "VARCHAR2(40 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "IS_DELETED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MAP_COLUMN_NAME", type: "VARCHAR2(40 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "MAP_TABLE_NAME_ATM_ID", type: "VARCHAR2(40 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "TABLE_NAME_ATM_ID", type: "VARCHAR2(40 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-62") {
        createTable(tableName: "CASE_HISTORY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "CASE_HISTORYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "archived_single_alert_id", type: "NUMBER(19, 0)")

            column(name: "case_number", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "case_version", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "change", type: "VARCHAR2(255 CHAR)")

            column(name: "config_id", type: "NUMBER(19, 0)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "current_assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "current_assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "current_disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "current_priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "due_date", type: "TIMESTAMP")

            column(name: "exec_config_id", type: "NUMBER(19, 0)")

            column(name: "follow_up_number", type: "NUMBER(10, 0)")

            column(name: "is_latest", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "JUSTIFICATION", type: "VARCHAR2(4000 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "product_family", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "single_alert_id", type: "NUMBER(19, 0)")

            column(name: "sub_tag_name", type: "VARCHAR2(4000 CHAR)")

            column(name: "tag_name", type: "VARCHAR2(4000 CHAR)")

            column(name: "tags_updated", type: "VARCHAR2(4000 CHAR)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-63") {
        createTable(tableName: "CATEGORY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "CATEGORYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DEFAULT_NAME", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-64") {
        createTable(tableName: "CLL_TEMPLT") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY", type: "CLOB")

            column(name: "COLUMNS_RF_INFO_LIST_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COL_SHOW_DISTINCT", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COL_SHOW_TOTAL", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUPING_RF_INFO_LIST_ID", type: "NUMBER(19, 0)")

            column(name: "PAGE_BREAK_BY_GROUP", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RENAME_GROUPING", type: "CLOB")

            column(name: "RENAME_ROW_COLS", type: "CLOB")

            column(name: "ROW_COLS_RF_INFO_LIST_ID", type: "NUMBER(19, 0)")

            column(name: "SUPPRESS_COLUMN_LIST", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-65") {
        createTable(tableName: "COGNOS_REPORT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "COGNOS_REPORTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "DESCRIPTION", type: "VARCHAR2(1000 CHAR)")

            column(name: "IS_DELETED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "URL", type: "VARCHAR2(1000 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-66") {
        createTable(tableName: "COMMENTS") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "COMMENTSPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "commented_by_id", type: "NUMBER(19, 0)")

            column(name: "content", type: "VARCHAR2(255 CHAR)")

            column(name: "input_date", type: "TIMESTAMP")

            column(name: "parent_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-67") {
        createTable(tableName: "DATE_RANGE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "DATE_RANGEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_END_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_END_DELTA", type: "NUMBER(10, 0)")

            column(name: "DATE_RNG_ENUM", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_START_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_START_DELTA", type: "NUMBER(10, 0)")

            column(name: "RELATIVE_DATE_RNG_VALUE", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-68") {
        createTable(tableName: "DELIVERIES_EMAIL_USERS") {
            column(name: "DELIVERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EMAIL_USER", type: "VARCHAR2(255 CHAR)")

            column(name: "EMAIL_USER_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-69") {
        createTable(tableName: "DELIVERIES_RPT_FORMATS") {
            column(name: "DELIVERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_FORMAT", type: "VARCHAR2(255 CHAR)")

            column(name: "RPT_FORMAT_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-70") {
        createTable(tableName: "DELIVERIES_SHARED_WITHS") {
            column(name: "DELIVERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARED_WITH_ID", type: "NUMBER(19, 0)")

            column(name: "SHARED_WITH_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-71") {
        createTable(tableName: "DELIVERY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "DELIVERYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-72") {
        createTable(tableName: "DISPOSITION") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "DISPOSITIONPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "abbreviation", type: "VARCHAR2(3 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "closed", type: "NUMBER(1, 0)")

            column(name: "color_code", type: "VARCHAR2(255 CHAR)")

            column(name: "description", type: "VARCHAR2(255 CHAR)")

            column(name: "display", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "display_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "notify", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "reset_review_process", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "review_completed", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "validated_confirmed", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-73") {
        createTable(tableName: "DISPOSITION_RULES") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "DISPOSITION_RULESPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "approval_required", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(255 CHAR)")

            column(name: "display", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "incoming_disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_deleted", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "notify", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "signal_rule", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "target_disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-74") {
        createTable(tableName: "DISPO_RULES_TOPIC_CATEGORY") {
            column(name: "DISPOSITION_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TOPIC_CATEGORY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-75") {
        createTable(tableName: "DISPO_RULES_USER_GROUP") {
            column(name: "DISPOSITION_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "USER_GROUP_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-76") {
        createTable(tableName: "DISPO_RULES_WORKFLOW_GROUP") {
            column(name: "DISPOSITION_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "WORKFLOW_GROUP_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-77") {
        createTable(tableName: "DTAB_COLUMN_MEASURE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "DTAB_COLUMN_MEASUREPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COLUMNS_RFI_LIST_ID", type: "NUMBER(19, 0)")

            column(name: "SHOW_TOTAL_CUMULATIVE_CASES", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHOW_TOTAL_INTERVAL_CASES", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-78") {
        createTable(tableName: "DTAB_COL_MEAS_MEASURES") {
            column(name: "DTAB_COL_MEAS_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEASURE_ID", type: "NUMBER(19, 0)")

            column(name: "MEASURES_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-79") {
        createTable(tableName: "DTAB_MEASURE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "DTAB_MEASUREPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "CUSTOM_EXPRESSION", type: "VARCHAR2(255 CHAR)")

            column(name: "FROM_DATE", type: "TIMESTAMP")

            column(name: "TO_DATE", type: "TIMESTAMP")

            column(name: "date_range_count", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "PERCENTAGE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "SHOW_SUBTOTALS", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHOW_TOTAL", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHOW_TOTAL_AS_COLS", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHOW_TOTAL_ROWS", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEASURE_TYPE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-80") {
        createTable(tableName: "DTAB_TEMPLT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ROWS_RF_INFO_LIST_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHOW_TOTAL_CUMULATIVE_CASES", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHOW_TOTAL_INTERVAL_CASES", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-81") {
        createTable(tableName: "DTAB_TEMPLTS_COL_MEAS") {
            column(name: "DTAB_TEMPLT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COLUMN_MEASURE_ID", type: "NUMBER(19, 0)")

            column(name: "COLUMN_MEASURE_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-82") {
        createTable(tableName: "EMAIL_LOG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EMAIL_LOGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "message", type: "CLOB")

            column(name: "sent_on", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "sent_to", type: "VARCHAR2(4000 CHAR)")

            column(name: "subject", type: "VARCHAR2(4000 CHAR)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-83") {
        createTable(tableName: "EMAIL_NOTIFICATION") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EMAIL_NOTIFICATIONPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DEFAULT_VALUE", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_ENABLED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "KEY", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "MODULE_NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-84") {
        createTable(tableName: "ETL_SCHEDULE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ETL_SCHEDULEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "DISABLED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_INITIAL", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "REPEAT_INTERVAL", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "SCHEDULE_NAME", type: "VARCHAR2(20 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "START_DATETIME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-85") {
        createTable(tableName: "EVAL_REF_TYPE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EVAL_REF_TYPEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DESCRIPTION", type: "VARCHAR2(255 CHAR)")

            column(name: "DISPLAY", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-86") {
        createTable(tableName: "EVDAS_ALERT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_count", type: "NUMBER(10, 0)")

            column(name: "adhoc_run", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_configuration_id", type: "NUMBER(19, 0)")

            column(name: "all_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "asia_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "attributes", type: "CLOB")

            column(name: "changes", type: "VARCHAR2(255 CHAR)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "detected_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DME_IME", type: "VARCHAR2(255 CHAR)")

            column(name: "due_date", type: "TIMESTAMP")

            column(name: "europe_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "exec_configuration_id", type: "NUMBER(19, 0)")

            column(name: "flagged", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "flags", type: "VARCHAR2(255 CHAR)")

            column(name: "format", type: "varchar2(8000 char)")

            column(name: "frequency", type: "VARCHAR2(255 CHAR)")

            column(name: "hlgt", type: "VARCHAR2(255 CHAR)")

            column(name: "hlt", type: "VARCHAR2(255 CHAR)")

            column(name: "imp_events", type: "VARCHAR2(255 CHAR)")

            column(name: "initial_disposition_id", type: "NUMBER(19, 0)")

            column(name: "initial_due_date", type: "TIMESTAMP")

            column(name: "is_new", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "japan_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "listedness", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "new_eea", type: "VARCHAR2(255 CHAR)")

            column(name: "new_ev", type: "NUMBER(10, 0)")

            column(name: "new_ev_link", type: "VARCHAR2(600 CHAR)")

            column(name: "new_fatal", type: "NUMBER(10, 0)")

            column(name: "new_geria", type: "VARCHAR2(255 CHAR)")

            column(name: "new_hcp", type: "VARCHAR2(255 CHAR)")

            column(name: "new_lit", type: "VARCHAR2(255 CHAR)")

            column(name: "new_med_err", type: "VARCHAR2(255 CHAR)")

            column(name: "new_obs", type: "VARCHAR2(255 CHAR)")

            column(name: "new_paed", type: "VARCHAR2(255 CHAR)")

            column(name: "new_rc", type: "VARCHAR2(255 CHAR)")

            column(name: "new_serious", type: "NUMBER(10, 0)")

            column(name: "new_spont", type: "VARCHAR2(255 CHAR)")

            column(name: "north_america_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "period_end_date", type: "TIMESTAMP")

            column(name: "period_start_date", type: "TIMESTAMP")

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "pt", type: "VARCHAR2(255 CHAR)")

            column(name: "pt_code", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ratio_ror_geriatr_vs_others", type: "VARCHAR2(255 CHAR)")

            column(name: "ratio_ror_paed_vs_others", type: "VARCHAR2(255 CHAR)")

            column(name: "requested_by", type: "VARCHAR2(255 CHAR)")

            column(name: "rest_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "review_date", type: "TIMESTAMP")

            column(name: "ror_value", type: "VARCHAR2(255 CHAR)")

            column(name: "sdr", type: "VARCHAR2(255 CHAR)")

            column(name: "sdr_geratr", type: "VARCHAR2(255 CHAR)")

            column(name: "sdr_paed", type: "VARCHAR2(255 CHAR)")

            column(name: "smq_narrow", type: "VARCHAR2(255 CHAR)")

            column(name: "soc", type: "VARCHAR2(255 CHAR)")

            column(name: "substance", type: "VARCHAR2(255 CHAR)")

            column(name: "substance_id", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "tot_eea", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_geria", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_hcp", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_med_err", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_obs", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_paed", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_rc", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_spont", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_spont_asia", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_spont_europe", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_spont_japan", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_spontnamerica", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_spont_rest", type: "VARCHAR2(255 CHAR)")

            column(name: "total_ev", type: "NUMBER(10, 0)")

            column(name: "total_ev_link", type: "VARCHAR2(600 CHAR)")

            column(name: "total_fatal", type: "NUMBER(10, 0)")

            column(name: "total_lit", type: "VARCHAR2(255 CHAR)")

            column(name: "total_serious", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-87") {
        createTable(tableName: "EVDAS_ALERT_ACTIONS") {
            column(name: "EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-88") {
        createTable(tableName: "EVDAS_ALERT_IMP_EVENT_LIST") {
            column(name: "EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EVDAS_IMP_EVENTS", type: "VARCHAR2(255 CHAR)")

            column(name: "ev_imp_event_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-89") {
        createTable(tableName: "EVDAS_APPLICATION_SETTING") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EVDAS_APPLICATION_SETTINGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "CASE_LISTING_LOCKED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ERMR_LOCKED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-90") {
        createTable(tableName: "EVDAS_CONFIG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "adhoc_run", type: "NUMBER(1, 0)")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "BLANK_VALUES", type: "CLOB")

            column(name: "SELECTED_TIME_ZONE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "date_range_information_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(4000 CHAR)")

            column(name: "EVENT_SELECTION", type: "CLOB")

            column(name: "executing", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "frequency", type: "VARCHAR2(255 CHAR)")

            column(name: "is_deleted", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "next_run_date", type: "TIMESTAMP")

            column(name: "NUM_OF_EXECUTIONS", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "owner_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PRODUCT_SELECTION", type: "CLOB")

            column(name: "SUPER_QUERY_ID", type: "NUMBER(19, 0)")

            column(name: "QUERY_NAME", type: "VARCHAR2(255 CHAR)")

            column(name: "reference_number", type: "VARCHAR2(255 CHAR)")

            column(name: "SCHEDULE_DATE", type: "VARCHAR2(1024 CHAR)")

            column(name: "strategy_id", type: "NUMBER(19, 0)")

            column(name: "TOTAL_EXECUTION_TIME", type: "NUMBER(19, 0)")

            column(name: "WORKFLOW_GROUP", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-91") {
        createTable(tableName: "EVDAS_CONFIGURATION_GROUPS") {
            column(name: "EVDAS_CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-92") {
        createTable(tableName: "EVDAS_DATE_RANGE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EVDAS_DATE_RANGEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_END_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_END_DELTA", type: "NUMBER(10, 0)")

            column(name: "DATE_RNG_ENUM", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_START_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_START_DELTA", type: "NUMBER(10, 0)")

            column(name: "RELATIVE_DATE_RNG_VALUE", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-93") {
        createTable(tableName: "EVDAS_ON_DEMAND_ALERT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_configuration_id", type: "NUMBER(19, 0)")

            column(name: "all_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "asia_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "europe_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "exec_configuration_id", type: "NUMBER(19, 0)")

            column(name: "flagged", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "hlgt", type: "VARCHAR2(255 CHAR)")

            column(name: "hlt", type: "VARCHAR2(255 CHAR)")

            column(name: "japan_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "new_eea", type: "VARCHAR2(255 CHAR)")

            column(name: "new_ev", type: "NUMBER(10, 0)")

            column(name: "new_fatal", type: "NUMBER(10, 0)")

            column(name: "new_geria", type: "VARCHAR2(255 CHAR)")

            column(name: "new_hcp", type: "VARCHAR2(255 CHAR)")

            column(name: "new_lit", type: "VARCHAR2(255 CHAR)")

            column(name: "new_med_err", type: "VARCHAR2(255 CHAR)")

            column(name: "new_obs", type: "VARCHAR2(255 CHAR)")

            column(name: "new_paed", type: "VARCHAR2(255 CHAR)")

            column(name: "new_rc", type: "VARCHAR2(255 CHAR)")

            column(name: "new_serious", type: "NUMBER(10, 0)")

            column(name: "new_spont", type: "VARCHAR2(255 CHAR)")

            column(name: "north_america_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "period_end_date", type: "TIMESTAMP")

            column(name: "period_start_date", type: "TIMESTAMP")

            column(name: "pt", type: "VARCHAR2(255 CHAR)")

            column(name: "pt_code", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "rest_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "ror_value", type: "VARCHAR2(255 CHAR)")

            column(name: "sdr", type: "VARCHAR2(255 CHAR)")

            column(name: "smq_narrow", type: "VARCHAR2(255 CHAR)")

            column(name: "soc", type: "VARCHAR2(255 CHAR)")

            column(name: "substance", type: "VARCHAR2(255 CHAR)")

            column(name: "substance_id", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "tot_eea", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_geria", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_hcp", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_med_err", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_obs", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_paed", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_rc", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_spont", type: "VARCHAR2(255 CHAR)")

            column(name: "total_ev", type: "NUMBER(10, 0)")

            column(name: "total_fatal", type: "NUMBER(10, 0)")

            column(name: "total_lit", type: "VARCHAR2(255 CHAR)")

            column(name: "total_serious", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-94") {
        createTable(tableName: "EVDAS_SIGNAL_CONCEPTS") {
            column(name: "EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-95") {
        createTable(tableName: "EVDAS_TOPIC_CONCEPTS") {
            column(name: "EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-96") {
        createTable(tableName: "EX_ALERT_DATE_RANGE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EX_ALERT_DATE_RANGEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_END_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_END_DELTA", type: "NUMBER(10, 0)")

            column(name: "DATE_RNG_ENUM", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_START_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_START_DELTA", type: "NUMBER(10, 0)")

            column(name: "RELATIVE_DATE_RNG_VALUE", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-97") {
        createTable(tableName: "EX_ALERT_QUERY_VALUES") {
            column(name: "EX_ALERT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_QUERY_VALUE_ID", type: "NUMBER(19, 0)")

            column(name: "EX_QUERY_VALUE_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-98") {
        createTable(tableName: "EX_CLL_TEMPLT") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-99") {
        createTable(tableName: "EX_CUSTOM_SQL_TEMPLT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-100") {
        createTable(tableName: "EX_DATE_RANGE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EX_DATE_RANGEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_END_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_ENUM", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_START_ABSOLUTE", type: "TIMESTAMP")

            column(name: "EXECUTED_AS_OF", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "RELATIVE_DATE_RNG_VALUE", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-101") {
        createTable(tableName: "EX_DELIVERIES_EMAIL_USERS") {
            column(name: "EX_DELIVERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EMAIL_USER", type: "VARCHAR2(255 CHAR)")

            column(name: "EMAIL_USER_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-102") {
        createTable(tableName: "EX_DELIVERIES_RPT_FORMATS") {
            column(name: "EX_DELIVERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_FORMAT", type: "VARCHAR2(255 CHAR)")

            column(name: "RPT_FORMAT_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-103") {
        createTable(tableName: "EX_DELIVERIES_SHARED_WITHS") {
            column(name: "EX_DELIVERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARED_WITH_ID", type: "NUMBER(19, 0)")

            column(name: "SHARED_WITH_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-104") {
        createTable(tableName: "EX_DELIVERY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EX_DELIVERYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-105") {
        createTable(tableName: "EX_DTAB_TEMPLT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-106") {
        createTable(tableName: "EX_EVDAS_CONFIG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "adhoc_run", type: "NUMBER(1, 0)")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "BLANK_VALUES", type: "CLOB")

            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SELECTED_TIME_ZONE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "date_range_information_id", type: "NUMBER(19, 0)")

            column(name: "description", type: "VARCHAR2(4000 CHAR)")

            column(name: "EVENT_SELECTION", type: "CLOB")

            column(name: "executed_query", type: "NUMBER(19, 0)")

            column(name: "executed_query_name", type: "VARCHAR2(255 CHAR)")

            column(name: "execution_status", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "frequency", type: "VARCHAR2(255 CHAR)")

            column(name: "is_deleted", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_LATEST", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_public", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "next_run_date", type: "TIMESTAMP")

            column(name: "NUM_OF_EXECUTIONS", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "owner_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "priority_id", type: "NUMBER(19, 0)")

            column(name: "PRODUCT_SELECTION", type: "CLOB")

            column(name: "SCHEDULE_DATE", type: "VARCHAR2(1024 CHAR)")

            column(name: "total_execution_time", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "WORKFLOW_GROUP", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-107") {
        createTable(tableName: "EX_EVDAS_CONFIG_ACTIVITIES") {
            column(name: "EX_EVDAS_CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTIVITY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-108") {
        createTable(tableName: "EX_EVDAS_DATE_RANGE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EX_EVDAS_DATE_RANGEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_END_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_END_DELTA", type: "NUMBER(10, 0)")

            column(name: "DATE_RNG_ENUM", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_START_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_START_DELTA", type: "NUMBER(10, 0)")

            column(name: "RELATIVE_DATE_RNG_VALUE", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-109") {
        createTable(tableName: "EX_LITERATURE_CONFIG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EX_LITERATURE_CONFIGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "date_range_information_id", type: "NUMBER(19, 0)")

            column(name: "EVENT_SELECTION", type: "CLOB")

            column(name: "is_deleted", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_latest", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(200 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "NUM_OF_EXECUTIONS", type: "NUMBER(10, 0)")

            column(name: "PVUSER_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PRODUCT_SELECTION", type: "CLOB")

            column(name: "search_string", type: "VARCHAR2(255 CHAR)")

            column(name: "SELECTED_DATA_SOURCE", type: "VARCHAR2(255 CHAR)")

            column(name: "total_execution_time", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "workflow_group_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-110") {
        createTable(tableName: "EX_LITERATURE_DATE_RANGE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EX_LITERATURE_DATE_RANGEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_END_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_ENUM", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_START_ABSOLUTE", type: "TIMESTAMP")

            column(name: "literature_configuration_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RELATIVE_DATE_RNG_VALUE", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-111") {
        createTable(tableName: "EX_NCASE_SQL_TEMPLT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-112") {
        createTable(tableName: "EX_QUERY_EXP") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-113") {
        createTable(tableName: "EX_QUERY_VALUE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-114") {
        createTable(tableName: "EX_RCONFIG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "adhoc_run", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ADJUST_PER_SCHED_FREQUENCY", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "agg_alert_id", type: "VARCHAR2(255 CHAR)")

            column(name: "agg_count_type", type: "VARCHAR2(255 CHAR)")

            column(name: "agg_execution_id", type: "VARCHAR2(255 CHAR)")

            column(name: "QUERY_NAME", type: "VARCHAR2(255 CHAR)")

            column(name: "alert_rmp_rems_ref", type: "VARCHAR2(255 CHAR)")

            column(name: "alert_trigger_cases", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_trigger_days", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "apply_alert_stop_list", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AS_OF_VERSION_DATE", type: "TIMESTAMP")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "BLANK_VALUES", type: "CLOB")

            column(name: "case_series_execution_status", type: "VARCHAR2(255 CHAR)")

            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SELECTED_TIME_ZONE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(100)") {
                constraints(nullable: "false")
            }

            column(name: "CUM_CASE_SERIES_EXEC_STATUS", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RANGE_TYPE", type: "VARCHAR2(255 CHAR)")

            column(name: "DESCRIPTION", type: "VARCHAR2(4000 CHAR)")

            column(name: "drug_classification", type: "VARCHAR2(255 CHAR)")

            column(name: "DRUG_TYPE", type: "VARCHAR2(255 CHAR)")

            column(name: "EVALUATE_DATE_AS", type: "VARCHAR2(255 CHAR)")

            column(name: "EVENT_SELECTION", type: "CLOB")

            column(name: "EXCLUDE_FOLLOWUP", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EXCLUDE_NON_VALID_CASES", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_ALERT_DATE_RANGE_ID", type: "NUMBER(19, 0)")

            column(name: "EX_ALERT_QUERY_ID", type: "NUMBER(19, 0)")

            column(name: "GROUP_BY_SMQ", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "INCLUDE_LOCKED_VERSION", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "INCL_MEDICAL_CONFIRM_CASES", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_auto_trigger", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_case_series", type: "NUMBER(1, 0)")

            column(name: "IS_DELETED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_ENABLED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_latest", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_PUBLIC", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "limit_primary_path", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "missed_cases", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(100)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(200 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "NEXT_RUN_DATE", type: "TIMESTAMP")

            column(name: "NUM_OF_EXECUTIONS", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "on_or_after_date", type: "TIMESTAMP")

            column(name: "PVUSER_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "priority_id", type: "NUMBER(19, 0)")

            column(name: "product_dictionary_selection", type: "VARCHAR2(255 CHAR)")

            column(name: "PRODUCT_SELECTION", type: "CLOB")

            column(name: "pvr_case_series_id", type: "NUMBER(19, 0)")

            column(name: "CUMULATIVE_CASE_SERIES_ID", type: "NUMBER(19, 0)")

            column(name: "reference_number", type: "VARCHAR2(255 CHAR)")

            column(name: "repeat_execution", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "REPORT_EXECUTION_STATUS", type: "VARCHAR2(255 CHAR)")

            column(name: "REPORT_ID", type: "NUMBER(19, 0)")

            column(name: "review_due_date", type: "TIMESTAMP")

            column(name: "SCHEDULE_DATE", type: "VARCHAR2(1024 CHAR)")

            column(name: "SELECTED_DATA_SOURCE", type: "VARCHAR2(255 CHAR)")

            column(name: "SPOTFIRE_SETTINGS", type: "CLOB")

            column(name: "STUDY_SELECTION", type: "CLOB")

            column(name: "suspect_product", type: "NUMBER(1, 0)")

            column(name: "TOTAL_EXECUTION_TIME", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR2(255 CHAR)")

            column(name: "WORKFLOW_GROUP", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-115") {
        createTable(tableName: "EX_RCONFIGS_PROD_GRP") {
            column(name: "EXCONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PROD_GRP_ID", type: "NUMBER(19, 0)")

            column(name: "PROD_GRP_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-116") {
        createTable(tableName: "EX_SQL_VALUE") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-117") {
        createTable(tableName: "EX_STATUS") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EX_STATUSPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "END_TIME", type: "NUMBER(19, 0)")

            column(name: "executed_config_id", type: "NUMBER(19, 0)")

            column(name: "EX_STATUS", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "FREQUENCY", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "MESSAGE", type: "CLOB")

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "next_run_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "owner_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY_ID", type: "NUMBER(19, 0)")

            column(name: "RPT_VERSION", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SECTION_NAME", type: "VARCHAR2(255 CHAR)")

            column(name: "STACK_TRACE", type: "CLOB")

            column(name: "START_TIME", type: "NUMBER(19, 0)")

            column(name: "TEMPLATE_ID", type: "NUMBER(19, 0)")

            column(name: "type", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-118") {
        createTable(tableName: "EX_STATUSES_RPT_FORMATS") {
            column(name: "EX_STATUS_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_FORMAT", type: "VARCHAR2(255 CHAR)")

            column(name: "RPT_FORMAT_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-119") {
        createTable(tableName: "EX_STATUSES_SHARED_WITHS") {
            column(name: "EX_STATUS_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARED_WITH_ID", type: "NUMBER(19, 0)")

            column(name: "SHARED_WITH_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-120") {
        createTable(tableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES") {
            column(name: "EX_TEMPLT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_QUERY_VALUE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-121") {
        createTable(tableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES") {
            column(name: "EX_TEMPLT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_TEMPLT_VALUE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-122") {
        createTable(tableName: "EX_TEMPLT_QUERY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EX_TEMPLT_QUERYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "BLIND_PROTECTED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "EX_RCONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_DATE_RANGE_INFO_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_QUERY_ID", type: "NUMBER(19, 0)")

            column(name: "EX_QUERY_NAME", type: "VARCHAR2(255 CHAR)")

            column(name: "EX_TEMPLT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_TEMPLT_NAME", type: "VARCHAR2(255 CHAR)")

            column(name: "FOOTER", type: "VARCHAR2(1000 CHAR)")

            column(name: "HEADER", type: "VARCHAR2(255 CHAR)")

            column(name: "HEADER_DATE_RANGE", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "HEADER_PRODUCT_SELECTION", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "PRIVACY_PROTECTED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY_LEVEL", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "TITLE", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-123") {
        createTable(tableName: "EX_TEMPLT_VALUE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-124") {
        createTable(tableName: "FILE_ATTACHMENTS") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "FILE_ATTACHMENTSPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "app_type", type: "VARCHAR2(255 CHAR)")

            column(name: "name", type: "VARCHAR2(255 CHAR)")

            column(name: "source_attachments", type: "BLOB") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-125") {
        createTable(tableName: "GROUPS") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "GROUPSPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "allowed_prod", type: "CLOB")

            column(name: "auto_route_disposition_id", type: "NUMBER(19, 0)")

            column(name: "created_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "default_adhoc_disposition_id", type: "NUMBER(19, 0)")

            column(name: "default_evdas_disposition_id", type: "NUMBER(19, 0)")

            column(name: "default_lit_disposition_id", type: "NUMBER(19, 0)")

            column(name: "default_quali_disposition_id", type: "NUMBER(19, 0)")

            column(name: "default_quant_disposition_id", type: "NUMBER(19, 0)")

            column(name: "default_signal_disposition_id", type: "NUMBER(19, 0)")

            column(name: "description", type: "VARCHAR2(4000 CHAR)")

            column(name: "force_justification", type: "NUMBER(1, 0)")

            column(name: "group_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "is_active", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "justification_text", type: "VARCHAR2(4000 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "selected_datasource", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-126") {
        createTable(tableName: "GRP_ALERT_DISP") {
            column(name: "GRP_ALERT_DISP_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DISPOSITION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-127") {
        createTable(tableName: "GUEST_ATTENDEE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "GUEST_ATTENDEEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EMAIL", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-128") {
        createTable(tableName: "IMPORT_DETAIL") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "IMPORT_DETAILPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "input_identifier", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "log_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "message", type: "VARCHAR2(4000 CHAR)")

            column(name: "rec_num", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-129") {
        createTable(tableName: "IMPORT_LOG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "IMPORT_LOGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "end_time", type: "TIMESTAMP")

            column(name: "num_failed", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "num_succeeded", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "response", type: "VARCHAR2(255 CHAR)")

            column(name: "start_time", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-130") {
        createTable(tableName: "LITERATURE_ACTIVITY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "LITERATURE_ACTIVITYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "article_id", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "DETAILS", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "display", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "event_name", type: "VARCHAR2(255 CHAR)")

            column(name: "executed_configuration_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "guest_attendee_email", type: "VARCHAR2(255 CHAR)")

            column(name: "JUSTIFICATION", type: "VARCHAR2(4000 CHAR)")

            column(name: "performed_by_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "private_user_name", type: "VARCHAR2(255 CHAR)")

            column(name: "product_name", type: "VARCHAR2(255 CHAR)")

            column(name: "search_string", type: "VARCHAR2(255 CHAR)")

            column(name: "timestamp", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "type_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-131") {
        createTable(tableName: "LITERATURE_ALERT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "LITERATURE_ALERTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_count", type: "NUMBER(10, 0)")

            column(name: "ARTICLE_ABSTRACT", type: "CLOB")

            column(name: "ARTICLE_AUTHORS", type: "CLOB")

            column(name: "article_id", type: "NUMBER(10, 0)")

            column(name: "ARTICLE_TITLE", type: "CLOB")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EVENT_SELECTION", type: "CLOB")

            column(name: "ex_lit_search_config_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "global_identity_id", type: "NUMBER(19, 0)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "lit_search_config_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PRODUCT_SELECTION", type: "CLOB")

            column(name: "PUBLICATION_DATE", type: "VARCHAR2(255 CHAR)")

            column(name: "search_string", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-132") {
        createTable(tableName: "LITERATURE_ALERT_TAGS") {
            column(name: "LITERATURE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ALERT_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-133") {
        createTable(tableName: "LITERATURE_CASE_ALERT_TAGS") {
            column(name: "PVS_ALERT_TAG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "LITERATURE_ALERT_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-134") {
        createTable(tableName: "LITERATURE_CONFIG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "LITERATURE_CONFIGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "config_selected_time_zone", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "date_range_information_id", type: "NUMBER(19, 0)")

            column(name: "EVENT_SELECTION", type: "CLOB")

            column(name: "executing", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_deleted", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_latest", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "NEXT_RUN_DATE", type: "TIMESTAMP")

            column(name: "num_of_executions", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PVUSER_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PRODUCT_SELECTION", type: "CLOB")

            column(name: "repeat_execution", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCHEDULE_DATE", type: "VARCHAR2(1024 CHAR)")

            column(name: "search_string", type: "VARCHAR2(255 CHAR)")

            column(name: "SELECTED_DATA_SOURCE", type: "VARCHAR2(255 CHAR)")

            column(name: "total_execution_time", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "workflow_group_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-135") {
        createTable(tableName: "LITERATURE_DATE_RANGE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "LITERATURE_DATE_RANGEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_END_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_ENUM", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_START_ABSOLUTE", type: "TIMESTAMP")

            column(name: "RELATIVE_DATE_RNG_VALUE", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-136") {
        createTable(tableName: "LITERATURE_HISTORY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "LITERATURE_HISTORYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "article_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "change", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "current_assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "current_assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "current_disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "current_priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "is_latest", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "JUSTIFICATION", type: "VARCHAR2(4000 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "lit_config_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "lit_exec_config_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "search_string", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "sub_tag_name", type: "VARCHAR2(4000 CHAR)")

            column(name: "tag_name", type: "VARCHAR2(4000 CHAR)")

            column(name: "tags_updated", type: "VARCHAR2(4000 CHAR)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-137") {
        createTable(tableName: "LITERAURE_GLOBAL_TAGS") {
            column(name: "GLOBAL_ARTICLE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PVS_GLOBAL_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-138") {
        createTable(tableName: "LIT_ALERT_ACTIONS") {
            column(name: "LITERATURE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-139") {
        createTable(tableName: "MEDICAL_CONCEPTS") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "MEDICAL_CONCEPTSPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "NUMBER(1, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-140") {
        createTable(tableName: "MEETING") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "MEETINGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "duration", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_recurrence_meeting", type: "NUMBER(1, 0)")

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "linking_id", type: "VARCHAR2(255 CHAR)")

            column(name: "meeting_agenda", type: "VARCHAR2(4000 CHAR)")

            column(name: "meeting_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "meeting_minutes", type: "VARCHAR2(4000 CHAR)")

            column(name: "meeting_owner_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "meeting_status", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "meeting_title", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "SCHEDULAR_JSON", type: "CLOB")

            column(name: "time_zone", type: "VARCHAR2(255 CHAR)")

            column(name: "topic_id", type: "NUMBER(19, 0)")

            column(name: "validated_signal_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-141") {
        createTable(tableName: "MEETING_ATTACHMENTS") {
            column(name: "MEETING_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ATTACHMENTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-142") {
        createTable(tableName: "MISC_CONFIG") {
            column(name: "KEY_1", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "VALUE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-143") {
        createTable(tableName: "NONCASE_SQL_TEMPLT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COL_NAME_LIST", type: "VARCHAR2(2048 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "NON_CASE_SQL", type: "CLOB") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-144") {
        createTable(tableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES") {
            column(name: "NONCASE_SQL_TEMPLT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SQL_VALUE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-145") {
        createTable(tableName: "NOTIFICATION") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "NOTIFICATIONPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "detail_url", type: "VARCHAR2(255 CHAR)")

            column(name: "EC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "LVL", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "MESSAGE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "MSG_ARGS", type: "VARCHAR2(255 CHAR)")

            column(name: "NOTIFICATION_USER_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-146") {
        createTable(tableName: "PARAM") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "PARAMPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "LOOKUP", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "VALUE", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-147") {
        createTable(tableName: "PREFERENCE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "PREFERENCEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "DASHBOARD_CONFIG_JSON", type: "CLOB")

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "is_cumulative_alert_enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_email_enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "LOCALE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "TIME_ZONE", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-148") {
        createTable(tableName: "PRIORITY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "PRIORITYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "default_priority", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(255 CHAR)")

            column(name: "display", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "display_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "icon_class", type: "VARCHAR2(255 CHAR)")

            column(name: "priority_order", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "review_period", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-149") {
        createTable(tableName: "PRODUCT_DICTIONARY_CACHE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "PRODUCT_DICTIONARY_CACHEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "group_id", type: "NUMBER(19, 0)")

            column(name: "safety_group_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-150") {
        createTable(tableName: "PRODUCT_INGREDIENT_MAPPING") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "PRODUCT_INGREDIENT_MAPPINGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "other_data_source", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "PRODUCTS", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "PVA_PRODUCTS", type: "CLOB") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-151") {
        createTable(tableName: "PVDMS_APPLICATION_SETTINGS") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "PVDMS_APPLICATION_SETTINGSPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(20 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "DMS_INTEGRATION", type: "CLOB")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(20 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-152") {
        createTable(tableName: "PVS_STATE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "PVS_STATEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(255 CHAR)")

            column(name: "description_local", type: "VARCHAR2(255 CHAR)")

            column(name: "display", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "display_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "display_name_local", type: "VARCHAR2(255 CHAR)")

            column(name: "final_state", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "review_period", type: "NUMBER(10, 0)")

            column(name: "value", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-153") {
        createTable(tableName: "PVUSER") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "PVUSERPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACCOUNT_EXPIRED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACCOUNT_LOCKED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "BAD_PASSWORD_ATTEMPTS", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "email", type: "VARCHAR2(255 CHAR)")

            column(name: "ENABLED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "full_name", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "OUTLOOK_ACCESS_TOKEN", type: "VARCHAR2(2000 CHAR)")

            column(name: "OUTLOOK_REFRESH_TOKEN", type: "VARCHAR2(2000 CHAR)")

            column(name: "PASSWORD_EXPIRED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PREFERENCE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "USERNAME", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-154") {
        createTable(tableName: "PVUSERS_ROLES") {
            column(name: "role_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-155") {
        createTable(tableName: "PV_CONCEPT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "PV_CONCEPTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "signal_strategy_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-156") {
        createTable(tableName: "QUERY_EXP_VALUE") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "OPERATOR_ID", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "REPORT_FIELD_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-157") {
        createTable(tableName: "QUERY_VALUE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SUPER_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "query_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-158") {
        createTable(tableName: "RCONFIG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "adhoc_run", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ADJUST_PER_SCHED_FREQUENCY", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "agg_alert_id", type: "VARCHAR2(255 CHAR)")

            column(name: "agg_count_type", type: "VARCHAR2(255 CHAR)")

            column(name: "agg_execution_id", type: "VARCHAR2(255 CHAR)")

            column(name: "ALERT_DATA_RANGE_ID", type: "NUMBER(19, 0)")

            column(name: "ALERT_QUERY_ID", type: "NUMBER(19, 0)")

            column(name: "QUERY_NAME", type: "VARCHAR2(255 CHAR)")

            column(name: "alert_rmp_rems_ref", type: "VARCHAR2(255 CHAR)")

            column(name: "alert_trigger_cases", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_trigger_days", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "apply_alert_stop_list", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AS_OF_VERSION_DATE", type: "TIMESTAMP")

            column(name: "AS_OF_VERSION_DATE_DELTA", type: "NUMBER(10, 0)")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "BLANK_VALUES", type: "CLOB")

            column(name: "SELECTED_TIME_ZONE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(100)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RANGE_TYPE", type: "VARCHAR2(255 CHAR)")

            column(name: "DESCRIPTION", type: "VARCHAR2(4000 CHAR)")

            column(name: "drug_classification", type: "VARCHAR2(255 CHAR)")

            column(name: "DRUG_TYPE", type: "VARCHAR2(255 CHAR)")

            column(name: "EVALUATE_DATE_AS", type: "VARCHAR2(255 CHAR)")

            column(name: "EVENT_SELECTION", type: "CLOB")

            column(name: "EXCLUDE_FOLLOWUP", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EXCLUDE_NON_VALID_CASES", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EXECUTING", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_BY_SMQ", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "INCLUDE_LOCKED_VERSION", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "INCL_MEDICAL_CONFIRM_CASES", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_AUTO_TRIGGER", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_case_series", type: "NUMBER(1, 0)")

            column(name: "IS_DELETED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_ENABLED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_PUBLIC", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "limit_primary_path", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "missed_cases", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(100)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(200 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "NEXT_RUN_DATE", type: "TIMESTAMP")

            column(name: "NUM_OF_EXECUTIONS", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "on_or_after_date", type: "TIMESTAMP")

            column(name: "PVUSER_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_dictionary_selection", type: "VARCHAR2(255 CHAR)")

            column(name: "PRODUCT_SELECTION", type: "CLOB")

            column(name: "reference_number", type: "VARCHAR2(255 CHAR)")

            column(name: "repeat_execution", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "review_period", type: "NUMBER(10, 0)")

            column(name: "SCHEDULE_DATE", type: "VARCHAR2(1024 CHAR)")

            column(name: "SELECTED_DATA_SOURCE", type: "VARCHAR2(255 CHAR)")

            column(name: "SPOTFIRE_SETTINGS", type: "CLOB")

            column(name: "STUDY_SELECTION", type: "CLOB")

            column(name: "suspect_product", type: "number(1, 0)") {
                constraints(nullable: "true")
            }

            column(name: "guest_attendee_email", type: "NUMBER(1, 0)")

            column(name: "TEMPLATE_ID", type: "NUMBER(19, 0)")

            column(name: "TOTAL_EXECUTION_TIME", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR2(255 CHAR)")

            column(name: "WORKFLOW_GROUP", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-159") {
        createTable(tableName: "RCONFIGS_PROD_GRP") {
            column(name: "RCONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PROD_GRP_ID", type: "NUMBER(19, 0)")

            column(name: "PROD_GRP_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-160") {
        createTable(tableName: "RCONFIG_DISPOSITION") {
            column(name: "CONFIGURATION_DISPOSITION_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DISPOSITION_ID", type: "NUMBER(19, 0)")

            column(name: "RCONFIG_DISPOSITION_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-161") {
        createTable(tableName: "ROLE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ROLEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AUTHORITY", type: "VARCHAR2(50 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "DESCRIPTION", type: "VARCHAR2(200 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-162") {
        createTable(tableName: "RPT_ERROR") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "RPT_ERRORPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RCONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "EX_RCONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_TEMPLT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_STATUS", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "MESSAGE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_RESULT_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-163") {
        createTable(tableName: "RPT_FIELD") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "RPT_FIELDPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DATA_TYPE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_FORMAT", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "DESCRIPTION", type: "VARCHAR2(255 CHAR)")

            column(name: "DIC_LEVEL", type: "NUMBER(10, 0)")

            column(name: "DIC_TYPE", type: "VARCHAR2(255 CHAR)")

            column(name: "RPT_FIELD_GRPNAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "FIXED_WIDTH", type: "NUMBER(10, 0)")

            column(name: "ISAUTOCOMPLETE", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_DELETED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_EUDRAFIELD", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_TEXT", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "LIST_DOMAIN_CLASS", type: "VARCHAR2(255 CHAR)")

            column(name: "LMSQL", type: "CLOB")

            column(name: "NAME", type: "VARCHAR2(128 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "POST_QUERY_PROCEDURE", type: "VARCHAR2(255 CHAR)")

            column(name: "PRE_QUERY_PROCEDURE", type: "VARCHAR2(255 CHAR)")

            column(name: "PRE_REPORT_PROCEDURE", type: "VARCHAR2(255 CHAR)")

            column(name: "QUERY_SELECTABLE", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SOURCE_COLUMN_MASTER_ID", type: "VARCHAR2(80 CHAR)")

            column(name: "TEMPLT_CLL_SELECTABLE", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TEMPLT_DTCOL_SELECTABLE", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TEMPLT_DTROW_SELECTABLE", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TRANSFORM", type: "VARCHAR2(255 CHAR)")

            column(name: "WIDTH_PROPORTION_INDEX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-164") {
        createTable(tableName: "RPT_FIELD_GROUP") {
            column(name: "NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_DELETED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_EUDRAFIELD", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-165") {
        createTable(tableName: "RPT_FIELD_INFO") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "RPT_FIELD_INFOPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ADVANCED_SORTING", type: "VARCHAR2(2000 CHAR)")

            column(name: "ARGUS_NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "BLINDED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COMMA_SEPARATED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "CUSTOM_EXPRESSION", type: "VARCHAR2(255 CHAR)")

            column(name: "DATASHEET", type: "VARCHAR2(255 CHAR)")

            column(name: "RENAME_VALUE", type: "VARCHAR2(255 CHAR)")

            column(name: "RPT_FIELD_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RF_INFO_LIST_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "sort", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }

            column(name: "SORT_LEVEL", type: "NUMBER(10, 0)")

            column(name: "STACK_ID", type: "NUMBER(10, 0)")

            column(name: "SUPPRESS_REPEATING", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RF_INFO_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-166") {
        createTable(tableName: "RPT_FIELD_INFO_LIST") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "RPT_FIELD_INFO_LISTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-167") {
        createTable(tableName: "RPT_RESULT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "RPT_RESULTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_RESULT_DATA_ID", type: "NUMBER(19, 0)")

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "EX_TEMPLT_QUERY_ID", type: "NUMBER(19, 0)")

            column(name: "EX_STATUS", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "FILTER_VERSION_TIME", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "FREQUENCY", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "QUERY_ROWS", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY_TIME", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "REASSESS_TIME", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "REPORT_ROWS", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "REPORT_TIME", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RUN_DATE", type: "TIMESTAMP")

            column(name: "SCHEDULED_PVUSER_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SEQUENCE", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TEMPLT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TOTAL_TIME", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "VERSION_ROWS", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "FILTERED_VERSION_ROWS", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "VERSION_TIME", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-168") {
        createTable(tableName: "RPT_RESULT_DATA") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "RPT_RESULT_DATAPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "CROSS_TAB_SQL", type: "CLOB")

            column(name: "GTT_SQL", type: "CLOB")

            column(name: "HEADER_SQL", type: "CLOB")

            column(name: "QUERY_SQL", type: "CLOB")

            column(name: "REPORT_SQL", type: "CLOB")

            column(name: "VALUE", type: "BLOB")

            column(name: "VERSION_SQL", type: "CLOB")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-169") {
        createTable(tableName: "RPT_TEMPLT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "RPT_TEMPLTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "CATEGORY_ID", type: "NUMBER(19, 0)")

            column(name: "created_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "DESCRIPTION", type: "VARCHAR2(200 CHAR)")

            column(name: "factory_default", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "HASBLANKS", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_DELETED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_PUBLIC", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(200 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "ORIG_TEMPLT_ID", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PV_USER_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "REASSESS_LISTEDNESS", type: "VARCHAR2(255 CHAR)")

            column(name: "TEMPLATE_TYPE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-170") {
        createTable(tableName: "RPT_TEMPLTS_TAGS") {
            column(name: "RPT_TEMPLT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TAG_ID", type: "NUMBER(19, 0)")

            column(name: "TAG_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-171") {
        createTable(tableName: "RULE_INFORMATION") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "RULE_INFORMATIONPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_id", type: "NUMBER(19, 0)")

            column(name: "business_configuration_id", type: "NUMBER(19, 0)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "SQL_QUERY", type: "CLOB")

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "disposition_id", type: "NUMBER(19, 0)")

            column(name: "enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "format", type: "VARCHAR2(255 CHAR)")

            column(name: "is_break_after_rule", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_first_time_rule", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_single_case_alert_type", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "justification_text", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "medical_concepts", type: "VARCHAR2(255 CHAR)")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "RULES", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "rule_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "rule_rank", type: "NUMBER(10, 0)")

            column(name: "signal", type: "VARCHAR2(255 CHAR)")

            column(name: "tags", type: "VARCHAR2(4000 CHAR)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-172") {
        createTable(tableName: "SHARED_WITH") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SHARED_WITHPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_RCONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_DELETED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "STATUS", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_USER_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-173") {
        createTable(tableName: "SHARE_WITH_GROUP_ADHOC") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_GROUPID", type: "NUMBER(19, 0)")

            column(name: "share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-174") {
        createTable(tableName: "SHARE_WITH_GROUP_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_GROUPID", type: "NUMBER(19, 0)")

            column(name: "share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-175") {
        createTable(tableName: "SHARE_WITH_GROUP_EVDAS_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_GROUPID", type: "NUMBER(19, 0)")

            column(name: "share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-176") {
        createTable(tableName: "SHARE_WITH_GROUP_FILTER") {
            column(name: "ADVANCED_FILTER_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_ID", type: "NUMBER(19, 0)")

            column(name: "share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-177") {
        createTable(tableName: "SHARE_WITH_GROUP_LITR_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_GROUPID", type: "NUMBER(19, 0)")

            column(name: "share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-178") {
        createTable(tableName: "SHARE_WITH_GROUP_SIGNAL") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_GROUPID", type: "NUMBER(19, 0)")

            column(name: "share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-179") {
        createTable(tableName: "SHARE_WITH_GROUP_VIEW") {
            column(name: "VIEW_INSTANCE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_ID", type: "NUMBER(19, 0)")

            column(name: "share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-180") {
        createTable(tableName: "SHARE_WITH_USER_ADHOC") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_USERID", type: "NUMBER(19, 0)")

            column(name: "share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-181") {
        createTable(tableName: "SHARE_WITH_USER_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_USERID", type: "NUMBER(19, 0)")

            column(name: "share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-182") {
        createTable(tableName: "SHARE_WITH_USER_EVDAS_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_USERID", type: "NUMBER(19, 0)")

            column(name: "share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-183") {
        createTable(tableName: "SHARE_WITH_USER_FILTER") {
            column(name: "ADVANCED_FILTER_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "USER_ID", type: "NUMBER(19, 0)")

            column(name: "share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-184") {
        createTable(tableName: "SHARE_WITH_USER_LITR_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_USERID", type: "NUMBER(19, 0)")

            column(name: "share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-185") {
        createTable(tableName: "SHARE_WITH_USER_SIGNAL") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_USERID", type: "NUMBER(19, 0)")

            column(name: "share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-186") {
        createTable(tableName: "SHARE_WITH_USER_VIEW") {
            column(name: "VIEW_INSTANCE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "USER_ID", type: "NUMBER(19, 0)")

            column(name: "share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-187") {
        createTable(tableName: "SIGNAL_CHART") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SIGNAL_CHARTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "CHART_DATA", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "chart_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "exec_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "topic_id", type: "NUMBER(19, 0)")

            column(name: "validated_signal_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-188") {
        createTable(tableName: "SIGNAL_LINKED_SIGNALS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "LINKED_SIGNAL_ID", type: "NUMBER(19, 0)")

            column(name: "linked_signals_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-189") {
        createTable(tableName: "SIGNAL_OUTCOME") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SIGNAL_OUTCOMEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-190") {
        createTable(tableName: "SIGNAL_REPORT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SIGNAL_REPORTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_id", type: "NUMBER(19, 0)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "excel_report", type: "BLOB")

            column(name: "EXECUTED_ALERT_ID", type: "NUMBER(19, 0)")

            column(name: "IS_ALERT_REPORT", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_generating", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "link_url", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "pdf_report", type: "BLOB")

            column(name: "report_execution_status", type: "VARCHAR2(255 CHAR)")

            column(name: "REPORT_ID", type: "NUMBER(19, 0)")

            column(name: "report_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR2(255 CHAR)")

            column(name: "type_flag", type: "VARCHAR2(255 CHAR)")

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "word_report", type: "BLOB")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-191") {
        createTable(tableName: "SIGNAL_SIG_STATUS_HISTORY") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SIG_STATUS_HISTORY_ID", type: "NUMBER(19, 0)")

            column(name: "signal_status_histories_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-192") {
        createTable(tableName: "SIGNAL_STATUS_HISTORY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SIGNAL_STATUS_HISTORYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "disposition_updated", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "performed_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "signal_status", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "status_comment", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-193") {
        createTable(tableName: "SIGNAL_STRATEGY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SIGNAL_STRATEGYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "product_selection", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "start_date", type: "TIMESTAMP")

            column(name: "type", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-194") {
        createTable(tableName: "SINGLE_ALERT_ACTIONS") {
            column(name: "SINGLE_CASE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-195") {
        createTable(tableName: "SINGLE_ALERT_CON_COMIT") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ALERT_CON_COMIT", type: "CLOB")

            column(name: "con_comit_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-196") {
        createTable(tableName: "SINGLE_ALERT_MED_ERR_PT_LIST") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_MED_ERROR", type: "CLOB")

            column(name: "med_error_pt_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-197") {
        createTable(tableName: "SINGLE_ALERT_PT") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_PT", type: "CLOB")

            column(name: "pt_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-198") {
        createTable(tableName: "SINGLE_ALERT_SUSP_PROD") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_PRODUCT_NAME", type: "CLOB")

            column(name: "suspect_product_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-199") {
        createTable(tableName: "SINGLE_ALERT_TAGS") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ALERT_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-200") {
        createTable(tableName: "SINGLE_CASE_ALERT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_count", type: "NUMBER(10, 0)")

            column(name: "adhoc_run", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "age", type: "VARCHAR2(255 CHAR)")

            column(name: "agg_alert_id", type: "VARCHAR2(255 CHAR)")

            column(name: "agg_count_type", type: "VARCHAR2(255 CHAR)")

            column(name: "agg_execution_id", type: "VARCHAR2(255 CHAR)")

            column(name: "alert_configuration_id", type: "NUMBER(19, 0)")

            column(name: "app_type_and_num", type: "VARCHAR2(255 CHAR)")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "attributes", type: "CLOB")

            column(name: "badge", type: "VARCHAR2(255 CHAR)")

            column(name: "case_id", type: "NUMBER(19, 0)")

            column(name: "case_init_receipt_date", type: "TIMESTAMP")

            column(name: "CASE_NARRATIVE", type: "CLOB")

            column(name: "case_number", type: "VARCHAR2(255 CHAR)")

            column(name: "case_report_type", type: "VARCHAR2(255 CHAR)")

            column(name: "case_type", type: "VARCHAR2(255 CHAR)")

            column(name: "case_version", type: "NUMBER(10, 0)")

            column(name: "completeness_score", type: "FLOAT(24)")

            column(name: "compounding_flag", type: "VARCHAR2(4000 CHAR)")

            column(name: "con_comit", type: "CLOB")

            column(name: "CON_MEDS", type: "CLOB")

            column(name: "country", type: "VARCHAR2(255 CHAR)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "death", type: "VARCHAR2(255 CHAR)")

            column(name: "detected_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "due_date", type: "TIMESTAMP")

            column(name: "exec_config_id", type: "NUMBER(19, 0)")

            column(name: "flagged", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "follow_up_exists", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "follow_up_number", type: "NUMBER(10, 0)")

            column(name: "gender", type: "VARCHAR2(255 CHAR)")

            column(name: "global_identity_id", type: "NUMBER(19, 0)")

            column(name: "ind_number", type: "VARCHAR2(255 CHAR)")

            column(name: "initial_disposition_id", type: "NUMBER(19, 0)")

            column(name: "initial_due_date", type: "TIMESTAMP")

            column(name: "is_case_series", type: "NUMBER(1, 0)")

            column(name: "is_duplicate", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_new", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "listedness", type: "VARCHAR2(255 CHAR)")

            column(name: "locked_date", type: "TIMESTAMP")

            column(name: "master_pref_term_all", type: "VARCHAR2(2000 CHAR)")

            column(name: "MED_ERRORS_PT", type: "CLOB")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "outcome", type: "VARCHAR2(255 CHAR)")

            column(name: "patient_age", type: "DOUBLE precision")

            column(name: "period_end_date", type: "TIMESTAMP")

            column(name: "period_start_date", type: "TIMESTAMP")

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_family", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "product_id", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "pt", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "rechallenge", type: "VARCHAR2(255 CHAR)")

            column(name: "reporters_hcp_flag", type: "VARCHAR2(255 CHAR)")

            column(name: "review_date", type: "TIMESTAMP")

            column(name: "serious", type: "VARCHAR2(255 CHAR)")

            column(name: "submitter", type: "VARCHAR2(255 CHAR)")

            column(name: "superseded_flag", type: "VARCHAR2(255 CHAR)")

            column(name: "SUSP_PROD", type: "CLOB")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-201") {
        createTable(tableName: "SINGLE_CASE_ALERT_TAGS") {
            column(name: "PVS_ALERT_TAG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-202") {
        createTable(tableName: "SINGLE_CASE_ALL_TAG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SINGLE_CASE_ALL_TAGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "case_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "case_series_id", type: "NUMBER(19, 0)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "owner", type: "VARCHAR2(255 CHAR)")

            column(name: "tag_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "tag_text", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-203") {
        createTable(tableName: "SINGLE_GLOBAL_TAGS") {
            column(name: "PVS_GLOBAL_TAG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GLOBAL_CASE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-204") {
        createTable(tableName: "SINGLE_GLOBAL_TAG_MAPPING") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SINGLE_GLOBAL_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-205") {
        createTable(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "adhoc_run", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "age", type: "VARCHAR2(255 CHAR)")

            column(name: "alert_configuration_id", type: "NUMBER(19, 0)")

            column(name: "app_type_and_num", type: "VARCHAR2(255 CHAR)")

            column(name: "attributes", type: "CLOB")

            column(name: "badge", type: "VARCHAR2(255 CHAR)")

            column(name: "case_id", type: "NUMBER(19, 0)")

            column(name: "case_init_receipt_date", type: "TIMESTAMP")

            column(name: "CASE_NARRATIVE", type: "CLOB")

            column(name: "case_number", type: "VARCHAR2(255 CHAR)")

            column(name: "case_report_type", type: "VARCHAR2(255 CHAR)")

            column(name: "case_type", type: "VARCHAR2(255 CHAR)")

            column(name: "case_version", type: "NUMBER(10, 0)")

            column(name: "completeness_score", type: "DOUBLE precision")

            column(name: "compounding_flag", type: "VARCHAR2(4000 CHAR)")

            column(name: "con_comit", type: "CLOB")

            column(name: "CON_MEDS", type: "CLOB")

            column(name: "country", type: "VARCHAR2(255 CHAR)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "death", type: "VARCHAR2(255 CHAR)")

            column(name: "detected_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "exec_config_id", type: "NUMBER(19, 0)")

            column(name: "flagged", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "follow_up_exists", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "follow_up_number", type: "NUMBER(10, 0)")

            column(name: "gender", type: "VARCHAR2(255 CHAR)")

            column(name: "ind_number", type: "VARCHAR2(255 CHAR)")

            column(name: "is_duplicate", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_new", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "listedness", type: "VARCHAR2(255 CHAR)")

            column(name: "locked_date", type: "TIMESTAMP")

            column(name: "master_pref_term_all", type: "VARCHAR2(2000 CHAR)")

            column(name: "MED_ERRORS_PT", type: "CLOB")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "outcome", type: "VARCHAR2(255 CHAR)")

            column(name: "patient_age", type: "DOUBLE precision")

            column(name: "period_end_date", type: "TIMESTAMP")

            column(name: "period_start_date", type: "TIMESTAMP")

            column(name: "product_family", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "product_id", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "pt", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "rechallenge", type: "VARCHAR2(255 CHAR)")

            column(name: "reporters_hcp_flag", type: "VARCHAR2(255 CHAR)")

            column(name: "review_date", type: "TIMESTAMP")

            column(name: "serious", type: "VARCHAR2(255 CHAR)")

            column(name: "submitter", type: "VARCHAR2(255 CHAR)")

            column(name: "SUSP_PROD", type: "CLOB")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-206") {
        createTable(tableName: "SINGLE_SIGNAL_CONCEPTS") {
            column(name: "SINGLE_CASE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-207") {
        createTable(tableName: "SINGLE_TOPIC_CONCEPTS") {
            column(name: "SINGLE_CASE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-208") {
        createTable(tableName: "SOURCE_COLUMN_MASTER") {
            column(name: "REPORT_ITEM", type: "VARCHAR2(80 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "COLUMN_NAME", type: "VARCHAR2(40 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "COLUMN_TYPE", type: "VARCHAR2(1 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "CONCATENATED_FIELD", type: "VARCHAR2(1 CHAR)")

            column(name: "IS_DELETED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_EUDRAFIELD", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "LM_DECODE_COLUMN", type: "VARCHAR2(40 CHAR)")

            column(name: "LM_JOIN_COLUMN", type: "VARCHAR2(40 CHAR)")

            column(name: "LM_JOIN_EQUI_OUTER", type: "VARCHAR2(1 CHAR)")

            column(name: "LM_TABLE_NAME_ATM_ID", type: "VARCHAR2(40 CHAR)")

            column(name: "MIN_COLUMNS", type: "NUMBER(10, 0)")

            column(name: "PRIMARY_KEY_ID", type: "NUMBER(19, 0)")

            column(name: "TABLE_NAME_ATM_ID", type: "VARCHAR2(40 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-209") {
        createTable(tableName: "SOURCE_TABLE_MASTER") {
            column(name: "TABLE_NAME", type: "VARCHAR2(40 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "CASE_JOIN_ORDER", type: "NUMBER(10, 0)")

            column(name: "CASE_JOIN_EQUI_OUTER", type: "VARCHAR2(1 CHAR)")

            column(name: "HAS_ENTERPRISE_ID", type: "NUMBER(10, 0)")

            column(name: "IS_DELETED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_EUDRAFIELD", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TABLE_ALIAS", type: "VARCHAR2(10 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "TABLE_TYPE", type: "VARCHAR2(1 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "VERSIONED_DATA", type: "VARCHAR2(1 CHAR)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-210") {
        createTable(tableName: "SQL_TEMPLT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COLUMN_NAMES", type: "VARCHAR2(2048 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "SELECT_FROM_STMT", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "WHERE_STMT", type: "CLOB")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-211") {
        createTable(tableName: "SQL_TEMPLTS_SQL_VALUES") {
            column(name: "SQL_TEMPLT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SQL_VALUE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-212") {
        createTable(tableName: "SQL_TEMPLT_VALUE") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "FIELD", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-213") {
        createTable(tableName: "SQL_VALUE") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-214") {
        createTable(tableName: "STRATEGY_MEDICAL_CONCEPTS") {
            column(name: "SIGNAL_STRATEGY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-215") {
        createTable(tableName: "TAG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "TAGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by_id", type: "NUMBER(19, 0)")

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-216") {
        createTable(tableName: "TEMPLT_QRS_QUERY_VALUES") {
            column(name: "TEMPLT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY_VALUE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-217") {
        createTable(tableName: "TEMPLT_QRS_TEMPLT_VALUES") {
            column(name: "TEMPLT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TEMPLT_VALUE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-218") {
        createTable(tableName: "TEMPLT_QUERY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "TEMPLT_QUERYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "BLIND_PROTECTED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RANGE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "FOOTER", type: "VARCHAR2(1000 CHAR)")

            column(name: "HEADER", type: "VARCHAR2(255 CHAR)")

            column(name: "HEADER_DATE_RANGE", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "HEADER_PRODUCT_SELECTION", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "INDX", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "PRIVACY_PROTECTED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SUPER_QUERY_ID", type: "NUMBER(19, 0)")

            column(name: "QUERY_LEVEL", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "query_name", type: "VARCHAR2(255 CHAR)")

            column(name: "RCONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_TEMPLT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "template_name", type: "VARCHAR2(255 CHAR)")

            column(name: "TITLE", type: "VARCHAR2(255 CHAR)")

            column(name: "TEMPLT_QUERY_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-219") {
        createTable(tableName: "TEMPLT_VALUE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_TEMPLT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-220") {
        createTable(tableName: "TOPIC") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "TOPICPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "assigned_to_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "assignment_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "end_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "initial_data_source", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_dictionary_selection", type: "VARCHAR2(255 CHAR)")

            column(name: "PRODUCTS", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "start_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "strategy_id", type: "NUMBER(19, 0)")

            column(name: "workflow_state_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-221") {
        createTable(tableName: "TOPIC_ACTIVITIES") {
            column(name: "TOPIC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTIVITY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-222") {
        createTable(tableName: "TOPIC_ADHOC_ALERTS") {
            column(name: "ADHOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TOPIC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-223") {
        createTable(tableName: "TOPIC_AGG_ALERTS") {
            column(name: "TOPIC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-224") {
        createTable(tableName: "TOPIC_CATEGORY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "TOPIC_CATEGORYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-225") {
        createTable(tableName: "TOPIC_COMMENTS") {
            column(name: "TOPIC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COMMENT_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-226") {
        createTable(tableName: "TOPIC_GROUP") {
            column(name: "TOPIC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-227") {
        createTable(tableName: "TOPIC_SINGLE_ALERTS") {
            column(name: "TOPIC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-228") {
        createTable(tableName: "USER_GROUPS") {
            column(name: "group_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-229") {
        createTable(tableName: "USER_GROUP_MAPPING") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "USER_GROUP_MAPPINGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "group_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-230") {
        createTable(tableName: "VALIDATED_ADHOC_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ADHOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-231") {
        createTable(tableName: "VALIDATED_AGG_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-232") {
        createTable(tableName: "VALIDATED_ALERT_ACTIVITIES") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTIVITY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-233") {
        createTable(tableName: "VALIDATED_ALERT_COMMENTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COMMENT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-234") {
        createTable(tableName: "VALIDATED_ALERT_DOCUMENTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ALERT_DOCUMENT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-235") {
        createTable(tableName: "VALIDATED_ARCHIVED_ACA") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ARCHIVED_ACA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-236") {
        createTable(tableName: "VALIDATED_ARCHIVED_LIT_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ARCHIVED_LIT_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-237") {
        createTable(tableName: "VALIDATED_ARCHIVED_SCA") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-238") {
        createTable(tableName: "VALIDATED_ARCH_EVDAS_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ARCHIVED_EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-239") {
        createTable(tableName: "VALIDATED_EVDAS_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-240") {
        createTable(tableName: "VALIDATED_LITERATURE_ALERTS") {
            column(name: "LITERATURE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-241") {
        createTable(tableName: "VALIDATED_SIGNAL") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "VALIDATED_SIGNALPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "agg_report_end_date", type: "TIMESTAMP")

            column(name: "agg_report_start_date", type: "TIMESTAMP")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "assignment_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "comment_signal_status", type: "VARCHAR2(4000 CHAR)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "CLOB")

            column(name: "detected_by", type: "VARCHAR2(255 CHAR)")

            column(name: "detected_date", type: "TIMESTAMP")

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "due_date", type: "TIMESTAMP")

            column(name: "EVENTS", type: "CLOB")

            column(name: "generic_comment", type: "CLOB")

            column(name: "ha_date_closed", type: "TIMESTAMP")

            column(name: "ha_signal_status_id", type: "NUMBER(19, 0)")

            column(name: "include_in_aggregate_report", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "initial_data_source", type: "VARCHAR2(1000)")

            column(name: "last_decision_date", type: "TIMESTAMP")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_dictionary_selection", type: "VARCHAR2(255 CHAR)")

            column(name: "PRODUCTS", type: "CLOB")

            column(name: "REASON_FOR_EVALUATION", type: "VARCHAR2(4000)")

            column(name: "review_date", type: "TIMESTAMP")

            column(name: "REPORT_PREFERENCE", type: "VARCHAR2(255 CHAR)")

            column(name: "strategy_id", type: "NUMBER(19, 0)")

            column(name: "topic", type: "VARCHAR2(255 CHAR)")

            column(name: "WORKFLOW_GROUP", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-242") {
        createTable(tableName: "VALIDATED_SIGNAL_GROUP") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-243") {
        createTable(tableName: "VALIDATED_SIGNAL_OUTCOMES") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SIGNAL_OUTCOME_ID", type: "NUMBER(19, 0)")

            column(name: "signal_outcomes_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-244") {
        createTable(tableName: "VALIDATED_SIGNAL_RCONFIG") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "CONFIG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-245") {
        createTable(tableName: "VALIDATED_SINGLE_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-246") {
        createTable(tableName: "VALUE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "VALUEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-247") {
        createTable(tableName: "VALUES_PARAMS") {
            column(name: "VALUE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PARAM_ID", type: "NUMBER(19, 0)")

            column(name: "PARAM_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-248") {
        createTable(tableName: "VAL_SIGNAL_TOPIC_CATEGORY") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TOPIC_CATEGORY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-249") {
        createTable(tableName: "VS_EVAL_METHOD") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EVALUATION_METHOD", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-250") {
        createTable(tableName: "VS_EVDAS_CONFIG") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EVDAS_CONFIG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-251") {
        createTable(tableName: "WORKFLOWRULES_ACTION_TEMPLATES") {
            column(name: "WORKFLOW_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_TEMPLATE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-252") {
        createTable(tableName: "WORKFLOWRULES_GROUPS") {
            column(name: "workflow_rule_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "group_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "allowed_groups_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-253") {
        createTable(tableName: "WORKFLOWRULES_SIGNAL_CATEGORY") {
            column(name: "WORKFLOW_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SIGNAL_CATEGORY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-254") {
        createTable(tableName: "WORK_FLOW_RULES") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "WORK_FLOW_RULESPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "approve_required", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(255 CHAR)")

            column(name: "display", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "income_state_id", type: "NUMBER(19, 0)")

            column(name: "is_deleted", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "notify", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "signal_rule", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "target_state_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-255") {
        createTable(tableName: "WORK_FLOW_VARIABLES") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "WORK_FLOW_VARIABLESPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-256") {
        createTable(tableName: "WkFL_RUL_DISPOSITIONS") {
            column(name: "workflow_rule_id", type: "NUMBER(19, 0)")

            column(name: "disposition_id", type: "NUMBER(19, 0)")

            column(name: "allowed_dispositions_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-257") {
        createTable(tableName: "alert_stop_list") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "alert_stop_listPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "activated", type: "NUMBER(1, 0)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "date_deactivated", type: "TIMESTAMP")

            column(name: "EVENT_SELECTION", type: "CLOB")

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "PRODUCT_SELECTION", type: "CLOB")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-258") {
        createTable(tableName: "attachment") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "attachmentPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "attachment_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "content_type", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "ext", type: "VARCHAR2(255 CHAR)")

            column(name: "input_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "length", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "lnk_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "poster_class", type: "VARCHAR2(255 CHAR)")

            column(name: "poster_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "reference_link", type: "VARCHAR2(4000 CHAR)")

            column(name: "saved_name", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-259") {
        createTable(tableName: "attachment_link") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "attachment_linkPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "reference_class", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "reference_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-260") {
        createTable(tableName: "audit_child_log") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "audit_child_logPK")
            }

            column(name: "audit_trail_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "new_value", type: "CLOB")

            column(name: "old_value", type: "CLOB")

            column(name: "property_name", type: "VARCHAR2(500)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-261") {
        createTable(tableName: "audit_log") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "audit_logPK")
            }

            column(name: "application_name", type: "VARCHAR2(255 CHAR)")

            column(name: "category", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "entity_id", type: "VARCHAR2(255 CHAR)")

            column(name: "entity_name", type: "VARCHAR2(255 CHAR)")

            column(name: "entity_value", type: "VARCHAR2(2000)")

            column(name: "fullname", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "persisted_object_version", type: "NUMBER(19, 0)")

            column(name: "property_name", type: "VARCHAR2(255 CHAR)")

            column(name: "sent_on_server", type: "NUMBER(1, 0)")

            column(name: "uri", type: "VARCHAR2(255 CHAR)")

            column(name: "username", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-262") {
        createTable(tableName: "default_view_mapping") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "default_view_mappingPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "default_view_instance_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-263") {
        createTable(tableName: "emerging_issue") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "emerging_issuePK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "dme", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "emerging_issue", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EVENT_SELECTION", type: "CLOB")

            column(name: "ime", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "special_monitoring", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-264") {
        createTable(tableName: "evdas_file_process_log") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "evdas_file_process_logPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "data_type", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(255 CHAR)")

            column(name: "duplicate_data_handling", type: "NUMBER(10, 0)")

            column(name: "file_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "is_manual", type: "NUMBER(1, 0)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "processed_records", type: "NUMBER(10, 0)")

            column(name: "REASON", type: "CLOB")

            column(name: "record_end_date", type: "TIMESTAMP")

            column(name: "record_start_date", type: "TIMESTAMP")

            column(name: "saved_name", type: "VARCHAR2(255 CHAR)")

            column(name: "status", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "SUBSTANCES", type: "VARCHAR2(4000 CHAR)")

            column(name: "total_records", type: "NUMBER(10, 0)")

            column(name: "updated_by_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-265") {
        createTable(tableName: "evdas_history") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "evdas_historyPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "archived_evdas_alert_id", type: "NUMBER(19, 0)")

            column(name: "as_of_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "change", type: "VARCHAR2(255 CHAR)")

            column(name: "config_id", type: "NUMBER(19, 0)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "due_date", type: "TIMESTAMP")

            column(name: "evdas_alert_id", type: "NUMBER(19, 0)")

            column(name: "event_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "execution_date", type: "TIMESTAMP")

            column(name: "is_latest", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "JUSTIFICATION", type: "VARCHAR2(4000 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "new_eea", type: "VARCHAR2(255 CHAR)")

            column(name: "new_ev", type: "NUMBER(10, 0)")

            column(name: "new_fatal", type: "NUMBER(10, 0)")

            column(name: "new_geria", type: "VARCHAR2(255 CHAR)")

            column(name: "new_hcp", type: "VARCHAR2(255 CHAR)")

            column(name: "new_lit", type: "VARCHAR2(255 CHAR)")

            column(name: "new_med_err", type: "VARCHAR2(255 CHAR)")

            column(name: "new_obs", type: "VARCHAR2(255 CHAR)")

            column(name: "new_paed", type: "VARCHAR2(255 CHAR)")

            column(name: "new_rc", type: "VARCHAR2(255 CHAR)")

            column(name: "new_serious", type: "NUMBER(10, 0)")

            column(name: "new_spont", type: "VARCHAR2(255 CHAR)")

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "tot_eea", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_geria", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_hcp", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_med_err", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_obs", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_paed", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_rc", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_spont", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_spont_asia", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_spont_europe", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_spont_japan", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_spontnamerica", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_spont_rest", type: "VARCHAR2(255 CHAR)")

            column(name: "total_ev", type: "NUMBER(10, 0)")

            column(name: "total_fatal", type: "NUMBER(10, 0)")

            column(name: "total_lit", type: "VARCHAR2(255 CHAR)")

            column(name: "total_serious", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-266") {
        createTable(tableName: "ex_rconfig_activities") {
            column(name: "EX_CONFIG_ACTIVITIES_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTIVITY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-267") {
        createTable(tableName: "global_article") {
            column(autoIncrement: "true", name: "globalArticleId", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "global_articlePK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "article_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-268") {
        createTable(tableName: "global_case") {
            column(autoIncrement: "true", name: "globalCaseId", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "global_casePK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "case_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-269") {
        createTable(tableName: "global_product_event") {
            column(autoIncrement: "true", name: "globalProductEventId", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "global_product_eventPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_event_comb", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-270") {
        createTable(tableName: "inbox_log") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "inbox_logPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "content", type: "VARCHAR2(8000 CHAR)")

            column(name: "created_on", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "detail_url", type: "VARCHAR2(255 CHAR)")

            column(name: "executed_config_id", type: "NUMBER(19, 0)")

            column(name: "inbox_user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_deleted", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_notification", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_read", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "LVL", type: "VARCHAR2(255 CHAR)")

            column(name: "message", type: "VARCHAR2(255 CHAR)")

            column(name: "message_args", type: "VARCHAR2(255 CHAR)")

            column(name: "notification_user_id", type: "NUMBER(19, 0)")

            column(name: "subject", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-271") {
        createTable(tableName: "justification") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "justificationPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "feature", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "justification", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-272") {
        createTable(tableName: "meeting_actions") {
            column(name: "meeting_actions_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-273") {
        createTable(tableName: "meeting_activities") {
            column(name: "meeting_activities_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "activity_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-274") {
        createTable(tableName: "meeting_guest_attendee") {
            column(name: "meeting_guest_attendee_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "guest_attendee_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-275") {
        createTable(tableName: "meeting_pvuser") {
            column(name: "meeting_attendees_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-276") {
        createTable(tableName: "priority_disposition_config") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "priority_disposition_configPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "disposition_order", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "review_period", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "disposition_configs_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-277") {
        createTable(tableName: "product_event_history") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "product_event_historyPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "agg_case_alert_id", type: "NUMBER(19, 0)")

            column(name: "archived_agg_case_alert_id", type: "NUMBER(19, 0)")

            column(name: "as_of_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "change", type: "VARCHAR2(255 CHAR)")

            column(name: "config_id", type: "NUMBER(19, 0)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "cum_fatal_count", type: "NUMBER(10, 0)")

            column(name: "cum_serious_count", type: "NUMBER(10, 0)")

            column(name: "cum_spon_count", type: "NUMBER(10, 0)")

            column(name: "cum_study_count", type: "NUMBER(10, 0)")

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "due_date", type: "TIMESTAMP")

            column(name: "eb05", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "eb95", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "ebgm", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "event_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "exec_config_id", type: "NUMBER(19, 0)")

            column(name: "execution_date", type: "TIMESTAMP")

            column(name: "is_latest", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "JUSTIFICATION", type: "VARCHAR2(4000 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "new_count", type: "NUMBER(10, 0)")

            column(name: "new_fatal_count", type: "NUMBER(10, 0)")

            column(name: "new_serious_count", type: "NUMBER(10, 0)")

            column(name: "new_spon_count", type: "NUMBER(10, 0)")

            column(name: "new_study_count", type: "NUMBER(10, 0)")

            column(name: "positive_rechallenge", type: "VARCHAR2(255 CHAR)")

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "prr_value", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "ror_value", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "sub_tag_name", type: "VARCHAR2(4000 CHAR)")

            column(name: "tag_name", type: "VARCHAR2(4000 CHAR)")

            column(name: "tags_updated", type: "VARCHAR2(4000 CHAR)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-278") {
        createTable(tableName: "product_group") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "product_groupPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "classification", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "display", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "group_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "PRODUCTS", type: "CLOB") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-279") {
        createTable(tableName: "pvs_alert_tag") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "pvs_alert_tagPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_at", type: "TIMESTAMP")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "domain", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "is_master_category", type: "NUMBER(1, 0)")

            column(name: "mart_id", type: "NUMBER(19, 0)")

            column(name: "modified_at", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "priority", type: "NUMBER(10, 0)")

            column(name: "private_user", type: "VARCHAR2(255 CHAR)")

            column(name: "sub_tag_id", type: "NUMBER(19, 0)")

            column(name: "sub_tag_text", type: "VARCHAR2(255 CHAR)")

            column(name: "tag_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "tag_text", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-280") {
        createTable(tableName: "pvs_global_tag") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "pvs_global_tagPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_at", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "domain", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "global_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_master_category", type: "NUMBER(1, 0)")

            column(name: "mart_id", type: "NUMBER(19, 0)")

            column(name: "modified_at", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "priority", type: "NUMBER(10, 0)")

            column(name: "private_user", type: "VARCHAR2(255 CHAR)")

            column(name: "sub_tag_id", type: "NUMBER(19, 0)")

            column(name: "sub_tag_text", type: "VARCHAR2(255 CHAR)")

            column(name: "tag_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "tag_text", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-281") {
        createTable(tableName: "pvuser_safety_groups") {
            column(name: "safety_group_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-282") {
        createTable(tableName: "pvuser_user_department") {
            column(name: "user_user_departments_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_department_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-283") {
        createTable(tableName: "report_history") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "report_historyPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "data_source", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "end_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "is_report_generated", type: "NUMBER(1, 0)")

            column(name: "memo_report", type: "BLOB")

            column(name: "product_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "report_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "report_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "start_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "updated_by_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-284") {
        createTable(tableName: "safety_group") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "safety_groupPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "allowed_prod", type: "CLOB")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-285") {
        createTable(tableName: "signal_history") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "signal_historyPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "change", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "due_date", type: "TIMESTAMP")

            column(name: "group_id", type: "NUMBER(19, 0)")

            column(name: "is_latest", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "justification", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "validated_signal_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-286") {
        createTable(tableName: "specialpe") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "specialpePK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "special_events", type: "CLOB")

            column(name: "special_products", type: "CLOB")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-287") {
        createTable(tableName: "spotfire_notification_pvuser") {
            column(name: "recipients_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "USER_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-288") {
        createTable(tableName: "spotfire_notification_query") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "spotfire_notification_queryPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "configuration_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "executed_configuration_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "file_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "run_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-289") {
        createTable(tableName: "spotfire_session") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "spotfire_sessionPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "deleted", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "email", type: "VARCHAR2(255 CHAR)")

            column(name: "full_name", type: "VARCHAR2(255 CHAR)")

            column(name: "timestamp", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "token", type: "VARCHAR2(255 CHAR)")

            column(name: "username", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-290") {
        createTable(tableName: "substance_frequency") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "substance_frequencyPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "end_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "frequency_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "mining_frequency", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "start_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "upload_frequency", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-291") {
        createTable(tableName: "test_role") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "test_rolePK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "authority", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-292") {
        createTable(tableName: "test_saml_user") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "test_saml_userPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "account_expired", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "account_locked", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "email", type: "VARCHAR2(255 CHAR)")

            column(name: "enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "first_name", type: "VARCHAR2(255 CHAR)")

            column(name: "password", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "password_expired", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "username", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-293") {
        createTable(tableName: "test_user_role") {
            column(name: "role_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-294") {
        createTable(tableName: "topic_actions") {
            column(name: "topic_actions_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-295") {
        createTable(tableName: "user_department") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "user_departmentPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "department_name", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-296") {
        createTable(tableName: "user_group_s") {
            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "group_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-297") {
        createTable(tableName: "validated_signal_action_taken") {
            column(name: "validated_signal_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_taken_string", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-298") {
        createTable(tableName: "validated_signal_actions") {
            column(name: "validated_signal_actions_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-299") {
        createTable(tableName: "view_instance") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "view_instancePK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "advanced_filter_id", type: "NUMBER(19, 0)")

            column(name: "alert_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "column_seq", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "default_value", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "filters", type: "VARCHAR2(255 CHAR)")

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "sorting", type: "VARCHAR2(255 CHAR)")

            column(name: "temp_column_seq", type: "CLOB")

            column(name: "user_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-300") {
        createTable(tableName: "work_flow_rules_actions") {
            column(name: "workflow_rule_actions_id", type: "NUMBER(19, 0)")

            column(name: "action_id", type: "NUMBER(19, 0)")

            column(name: "actions_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-301") {
        createIndex(indexName: "IX_TOPIC_ADHOC_ALERTS", tableName: "TOPIC_ADHOC_ALERTS", unique: "true") {
            column(name: "ADHOC_ALERT_ID")

            column(name: "TOPIC_ID")
        }

        addPrimaryKey(columnNames: "ADHOC_ALERT_ID, TOPIC_ID", forIndexName: "IX_TOPIC_ADHOC_ALERTS", tableName: "TOPIC_ADHOC_ALERTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-302") {
        createIndex(indexName: "IX_TOPIC_AGG_ALERTS", tableName: "TOPIC_AGG_ALERTS", unique: "true") {
            column(name: "AGG_ALERT_ID")

            column(name: "TOPIC_ID")
        }

        addPrimaryKey(columnNames: "AGG_ALERT_ID, TOPIC_ID", forIndexName: "IX_TOPIC_AGG_ALERTS", tableName: "TOPIC_AGG_ALERTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-303") {
        createIndex(indexName: "IX_TOPIC_SINGLE_ALERTS", tableName: "TOPIC_SINGLE_ALERTS", unique: "true") {
            column(name: "SINGLE_ALERT_ID")

            column(name: "TOPIC_ID")
        }

        addPrimaryKey(columnNames: "SINGLE_ALERT_ID, TOPIC_ID", forIndexName: "IX_TOPIC_SINGLE_ALERTS", tableName: "TOPIC_SINGLE_ALERTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-304") {
        createIndex(indexName: "IX_ADHOC_ALERT", tableName: "VALIDATED_ADHOC_ALERTS", unique: "true") {
            column(name: "ADHOC_ALERT_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "ADHOC_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "IX_ADHOC_ALERT", tableName: "VALIDATED_ADHOC_ALERTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-305") {
        createIndex(indexName: "IX_VALIDATED_ADHOC_ALERTS", tableName: "VALIDATED_AGG_ALERTS", unique: "true") {
            column(name: "AGG_ALERT_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "AGG_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "IX_VALIDATED_ADHOC_ALERTS", tableName: "VALIDATED_AGG_ALERTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-306") {
        createIndex(indexName: "IX_ARCHIVED_ACA", tableName: "VALIDATED_ARCHIVED_ACA", unique: "true") {
            column(name: "ARCHIVED_ACA_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "ARCHIVED_ACA_ID, VALIDATED_SIGNAL_ID", forIndexName: "IX_ARCHIVED_ACA", tableName: "VALIDATED_ARCHIVED_ACA")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-307") {
        createIndex(indexName: "IX_ARCHIVED_LIT_ALERT", tableName: "VALIDATED_ARCHIVED_LIT_ALERTS", unique: "true") {
            column(name: "ARCHIVED_LIT_ALERT_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "ARCHIVED_LIT_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "IX_ARCHIVED_LIT_ALERT", tableName: "VALIDATED_ARCHIVED_LIT_ALERTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-308") {
        createIndex(indexName: "IX_ARCHIVED_SCA", tableName: "VALIDATED_ARCHIVED_SCA", unique: "true") {
            column(name: "ARCHIVED_SCA_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "ARCHIVED_SCA_ID, VALIDATED_SIGNAL_ID", forIndexName: "IX_ARCHIVED_SCA", tableName: "VALIDATED_ARCHIVED_SCA")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-309") {
        createIndex(indexName: "IX_ARCHIVED_EVDAS_ALERT", tableName: "VALIDATED_ARCH_EVDAS_ALERTS", unique: "true") {
            column(name: "ARCHIVED_EVDAS_ALERT_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "ARCHIVED_EVDAS_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "IX_ARCHIVED_EVDAS_ALERT", tableName: "VALIDATED_ARCH_EVDAS_ALERTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-310") {
        createIndex(indexName: "IX_VALIDATED_EVDAS_ALERTS", tableName: "VALIDATED_EVDAS_ALERTS", unique: "true") {
            column(name: "EVDAS_ALERT_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "EVDAS_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "IX_VALIDATED_EVDAS_ALERTS", tableName: "VALIDATED_EVDAS_ALERTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-311") {
        createIndex(indexName: "IX_VALIDATED_LITERATURE_ALERTS", tableName: "VALIDATED_LITERATURE_ALERTS", unique: "true") {
            column(name: "LITERATURE_ALERT_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "LITERATURE_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "IX_VALIDATED_LITERATURE_ALERTS", tableName: "VALIDATED_LITERATURE_ALERTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-312") {
        createIndex(indexName: "IX_VALIDATED_SINGLE_ALERTS", tableName: "VALIDATED_SINGLE_ALERTS", unique: "true") {
            column(name: "SINGLE_ALERT_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "SINGLE_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "IX_VALIDATED_SINGLE_ALERTS", tableName: "VALIDATED_SINGLE_ALERTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-313") {
        createIndex(indexName: "IX_pvuser_safety_groups", tableName: "pvuser_safety_groups", unique: "true") {
            column(name: "user_id")

            column(name: "safety_group_id")
        }

        addPrimaryKey(columnNames: "user_id, safety_group_id", forIndexName: "IX_pvuser_safety_groups", tableName: "pvuser_safety_groups")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-314") {
        createIndex(indexName: "IX_user_group_s", tableName: "user_group_s", unique: "true") {
            column(name: "user_id")

            column(name: "group_id")
        }

        addPrimaryKey(columnNames: "user_id, group_id", forIndexName: "IX_user_group_s", tableName: "user_group_s")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-315") {
        createIndex(indexName: "IX_AGG_ALERTPK", tableName: "AGG_ALERT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "AGG_ALERTPK", forIndexName: "IX_AGG_ALERTPK", tableName: "AGG_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-316") {
        createIndex(indexName: "IX_AGG_ON_DEMAND_ALERT", tableName: "AGG_ON_DEMAND_ALERT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "AGG_ON_DEMAND_ALERTPK", forIndexName: "IX_AGG_ON_DEMAND_ALERT", tableName: "AGG_ON_DEMAND_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-317") {
        createIndex(indexName: "IX_CLL_TEMPLTPK", tableName: "CLL_TEMPLT", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "CLL_TEMPLTPK", forIndexName: "IX_CLL_TEMPLTPK", tableName: "CLL_TEMPLT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-318") {
        createIndex(indexName: "IX_DTAB_TEMPLTPK", tableName: "DTAB_TEMPLT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "DTAB_TEMPLTPK", forIndexName: "IX_DTAB_TEMPLTPK", tableName: "DTAB_TEMPLT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-319") {
        createIndex(indexName: "IX_EVDAS_ALERTPK", tableName: "EVDAS_ALERT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EVDAS_ALERTPK", forIndexName: "IX_EVDAS_ALERTPK", tableName: "EVDAS_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-320") {
        createIndex(indexName: "IX_EVDAS_CONFIGPK", tableName: "EVDAS_CONFIG", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EVDAS_CONFIGPK", forIndexName: "IX_EVDAS_CONFIGPK", tableName: "EVDAS_CONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-321") {
        createIndex(indexName: "IX_EVDAS_ON_DEMAND_ALERT", tableName: "EVDAS_ON_DEMAND_ALERT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EVDAS_ON_DEMAND_ALERTPK", forIndexName: "IX_EVDAS_ON_DEMAND_ALERT", tableName: "EVDAS_ON_DEMAND_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-322") {
        createIndex(indexName: "IX_EX_CLL_TEMPLTPK", tableName: "EX_CLL_TEMPLT", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "EX_CLL_TEMPLTPK", forIndexName: "IX_EX_CLL_TEMPLTPK", tableName: "EX_CLL_TEMPLT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-323") {
        createIndex(indexName: "IX_EX_CUSTOM_SQL_TEMPLTPK", tableName: "EX_CUSTOM_SQL_TEMPLT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_CUSTOM_SQL_TEMPLTPK", forIndexName: "IX_EX_CUSTOM_SQL_TEMPLTPK", tableName: "EX_CUSTOM_SQL_TEMPLT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-324") {
        createIndex(indexName: "IX_EX_DTAB_TEMPLTPK", tableName: "EX_DTAB_TEMPLT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_DTAB_TEMPLTPK", forIndexName: "IX_EX_DTAB_TEMPLTPK", tableName: "EX_DTAB_TEMPLT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-325") {
        createIndex(indexName: "IX_EX_EVDAS_CONFIGPK", tableName: "EX_EVDAS_CONFIG", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_EVDAS_CONFIGPK", forIndexName: "IX_EX_EVDAS_CONFIGPK", tableName: "EX_EVDAS_CONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-326") {
        createIndex(indexName: "IX_EX_EVDAS_CONFIG_ACTIVITIES", tableName: "EX_EVDAS_CONFIG_ACTIVITIES", unique: "true") {
            column(name: "EX_EVDAS_CONFIG_ID")

            column(name: "ACTIVITY_ID")
        }

        addPrimaryKey(columnNames: "EX_EVDAS_CONFIG_ID, ACTIVITY_ID", constraintName: "EX_EVDAS_CONFIG_ACTIVITIES", forIndexName: "IX_EX_EVDAS_CONFIG_ACTIVITIES", tableName: "EX_EVDAS_CONFIG_ACTIVITIES")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-327") {
        createIndex(indexName: "IX_EX_NCASE_SQL_TEMPLTPK", tableName: "EX_NCASE_SQL_TEMPLT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_NCASE_SQL_TEMPLTPK", forIndexName: "IX_EX_NCASE_SQL_TEMPLTPK", tableName: "EX_NCASE_SQL_TEMPLT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-328") {
        createIndex(indexName: "IX_EX_QUERY_EXPPK", tableName: "EX_QUERY_EXP", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "EX_QUERY_EXPPK", forIndexName: "IX_EX_QUERY_EXPPK", tableName: "EX_QUERY_EXP")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-329") {
        createIndex(indexName: "IX_EX_QUERY_VALUEPK", tableName: "EX_QUERY_VALUE", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_QUERY_VALUEPK", forIndexName: "IX_EX_QUERY_VALUEPK", tableName: "EX_QUERY_VALUE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-330") {
        createIndex(indexName: "IX_EX_RCONFIGPK", tableName: "EX_RCONFIG", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_RCONFIGPK", forIndexName: "IX_EX_RCONFIGPK", tableName: "EX_RCONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-331") {
        createIndex(indexName: "IX_EX_SQL_VALUEPK", tableName: "EX_SQL_VALUE", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "EX_SQL_VALUEPK", forIndexName: "IX_EX_SQL_VALUEPK", tableName: "EX_SQL_VALUE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-332") {
        createIndex(indexName: "IX_EX_TEMPLT_VALUEPK", tableName: "EX_TEMPLT_VALUE", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_TEMPLT_VALUEPK", forIndexName: "IX_EX_TEMPLT_VALUEPK", tableName: "EX_TEMPLT_VALUE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-333") {
        createIndex(indexName: "IX_MISC_CONFIGPK", tableName: "MISC_CONFIG", unique: "true") {
            column(name: "KEY_1")
        }

        addPrimaryKey(columnNames: "KEY_1", constraintName: "MISC_CONFIGPK", forIndexName: "IX_MISC_CONFIGPK", tableName: "MISC_CONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-334") {
        createIndex(indexName: "IX_NONCASE_SQL_TEMPLTPK", tableName: "NONCASE_SQL_TEMPLT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "NONCASE_SQL_TEMPLTPK", forIndexName: "IX_NONCASE_SQL_TEMPLTPK", tableName: "NONCASE_SQL_TEMPLT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-335") {
        createIndex(indexName: "IX_PVUSERS_ROLESPK", tableName: "PVUSERS_ROLES", unique: "true") {
            column(name: "role_id")

            column(name: "user_id")
        }

        addPrimaryKey(columnNames: "role_id, user_id", constraintName: "PVUSERS_ROLESPK", forIndexName: "IX_PVUSERS_ROLESPK", tableName: "PVUSERS_ROLES")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-336") {
        createIndex(indexName: "IX_QUERY_EXP_VALUEPK", tableName: "QUERY_EXP_VALUE", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "QUERY_EXP_VALUEPK", forIndexName: "IX_QUERY_EXP_VALUEPK", tableName: "QUERY_EXP_VALUE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-337") {
        createIndex(indexName: "IX_QUERY_VALUEPK", tableName: "QUERY_VALUE", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "QUERY_VALUEPK", forIndexName: "IX_QUERY_VALUEPK", tableName: "QUERY_VALUE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-338") {
        createIndex(indexName: "IX_RCONFIGPK", tableName: "RCONFIG", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "RCONFIGPK", forIndexName: "IX_RCONFIGPK", tableName: "RCONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-339") {
        createIndex(indexName: "IX_RPT_FIELD_GROUPPK", tableName: "RPT_FIELD_GROUP", unique: "true") {
            column(name: "NAME")
        }

        addPrimaryKey(columnNames: "NAME", constraintName: "RPT_FIELD_GROUPPK", forIndexName: "IX_RPT_FIELD_GROUPPK", tableName: "RPT_FIELD_GROUP")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-340") {
        createIndex(indexName: "IX_SINGLE_CASE_ALERTPK", tableName: "SINGLE_CASE_ALERT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "SINGLE_CASE_ALERTPK", forIndexName: "IX_SINGLE_CASE_ALERTPK", tableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-846") {
        createIndex(indexName: "idx_single_alert_group", tableName: "SINGLE_CASE_ALERT", unique: "false") {
            column(name: "ASSIGNED_TO_GROUP_ID")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-847") {
        createIndex(indexName: "idx_single_alert_assignedto", tableName: "SINGLE_CASE_ALERT", unique: "false") {
            column(name: "ASSIGNED_TO_ID")
        }
    }

    changeSet(author: "amit (generated)", id: "1594893043821-341") {
        createIndex(indexName: "IX_SINGLE_ON_DEMAND_ALERT", tableName: "SINGLE_ON_DEMAND_ALERT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "SINGLE_ON_DEMAND_ALERTPK", forIndexName: "IX_SINGLE_ON_DEMAND_ALERT", tableName: "SINGLE_ON_DEMAND_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-342") {
        createIndex(indexName: "IX_SOURCE_COLUMN_MASTERPK", tableName: "SOURCE_COLUMN_MASTER", unique: "true") {
            column(name: "REPORT_ITEM")
        }

        addPrimaryKey(columnNames: "REPORT_ITEM", constraintName: "SOURCE_COLUMN_MASTERPK", forIndexName: "IX_SOURCE_COLUMN_MASTERPK", tableName: "SOURCE_COLUMN_MASTER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-343") {
        createIndex(indexName: "IX_SOURCE_TABLE_MASTERPK", tableName: "SOURCE_TABLE_MASTER", unique: "true") {
            column(name: "TABLE_NAME")
        }

        addPrimaryKey(columnNames: "TABLE_NAME", constraintName: "SOURCE_TABLE_MASTERPK", forIndexName: "IX_SOURCE_TABLE_MASTERPK", tableName: "SOURCE_TABLE_MASTER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-344") {
        createIndex(indexName: "IX_SQL_TEMPLTPK", tableName: "SQL_TEMPLT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "SQL_TEMPLTPK", forIndexName: "IX_SQL_TEMPLTPK", tableName: "SQL_TEMPLT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-345") {
        createIndex(indexName: "IX_SQL_TEMPLT_VALUEPK", tableName: "SQL_TEMPLT_VALUE", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "SQL_TEMPLT_VALUEPK", forIndexName: "IX_SQL_TEMPLT_VALUEPK", tableName: "SQL_TEMPLT_VALUE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-346") {
        createIndex(indexName: "IX_SQL_VALUEPK", tableName: "SQL_VALUE", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "SQL_VALUEPK", forIndexName: "IX_SQL_VALUEPK", tableName: "SQL_VALUE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-347") {
        createIndex(indexName: "IX_TEMPLT_VALUEPK", tableName: "TEMPLT_VALUE", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "TEMPLT_VALUEPK", forIndexName: "IX_TEMPLT_VALUEPK", tableName: "TEMPLT_VALUE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-348") {
        createIndex(indexName: "IX_VALIDATED_ALERT_COMMENTSPK", tableName: "VALIDATED_ALERT_COMMENTS", unique: "true") {
            column(name: "VALIDATED_SIGNAL_ID")

            column(name: "COMMENT_ID")
        }

        addPrimaryKey(columnNames: "VALIDATED_SIGNAL_ID, COMMENT_ID", constraintName: "VALIDATED_ALERT_COMMENTSPK", forIndexName: "IX_VALIDATED_ALERT_COMMENTSPK", tableName: "VALIDATED_ALERT_COMMENTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-349") {
        createIndex(indexName: "IX_VALIDATED_ALERT_DOCUMENTSPK", tableName: "VALIDATED_ALERT_DOCUMENTS", unique: "true") {
            column(name: "VALIDATED_SIGNAL_ID")

            column(name: "ALERT_DOCUMENT_ID")
        }

        addPrimaryKey(columnNames: "VALIDATED_SIGNAL_ID, ALERT_DOCUMENT_ID", constraintName: "VALIDATED_ALERT_DOCUMENTSPK", forIndexName: "IX_VALIDATED_ALERT_DOCUMENTSPK", tableName: "VALIDATED_ALERT_DOCUMENTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-350") {
        createIndex(indexName: "IX_ex_rconfig_activitiesPK", tableName: "ex_rconfig_activities", unique: "true") {
            column(name: "EX_CONFIG_ACTIVITIES_ID")

            column(name: "ACTIVITY_ID")
        }

        addPrimaryKey(columnNames: "EX_CONFIG_ACTIVITIES_ID, ACTIVITY_ID", constraintName: "ex_rconfig_activitiesPK", forIndexName: "IX_ex_rconfig_activitiesPK", tableName: "ex_rconfig_activities")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-351") {
        createIndex(indexName: "IX_test_user_rolePK", tableName: "test_user_role", unique: "true") {
            column(name: "role_id")

            column(name: "user_id")
        }

        addPrimaryKey(columnNames: "role_id, user_id", constraintName: "test_user_rolePK", forIndexName: "IX_test_user_rolePK", tableName: "test_user_role")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-352") {
        addUniqueConstraint(columnNames: "LDAP_GROUP_NAME", constraintName: "UC_AC_GRP_LDAP_GRP_NAME_COL", tableName: "ACCESS_CONTROL_GROUP")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-353") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_AC_GRPNAME_COL", tableName: "ACCESS_CONTROL_GROUP")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-354") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_ACTION_TEMPLATENAME_COL", tableName: "ACTION_TEMPLATE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-355") {
        addUniqueConstraint(columnNames: "value", constraintName: "UC_AEVAL_TYPEVALUE_COL", tableName: "AEVAL_TYPE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-356") {
        addUniqueConstraint(columnNames: "chronicle_id", constraintName: "UC_ALERT_DOC_CHRONICLE_ID_COL", tableName: "ALERT_DOCUMENT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-357") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_ALERT_TAGNAME_COL", tableName: "ALERT_TAG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-358") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_CATEGORYNAME_COL", tableName: "CATEGORY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-359") {
        addUniqueConstraint(columnNames: "display_name", constraintName: "UC_DISPOSITIONDISPLAY_NAME_COL", tableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-360") {
        addUniqueConstraint(columnNames: "value", constraintName: "UC_DISPOSITIONVALUE_COL", tableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-361") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_DISPOSITION_RULESNAME_COL", tableName: "DISPOSITION_RULES")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-362") {
        addUniqueConstraint(columnNames: "SCHEDULE_NAME", constraintName: "UC_ETL_SCHEDULE_NAME_COL", tableName: "ETL_SCHEDULE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-363") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_EVAL_REF_TYPENAME_COL", tableName: "EVAL_REF_TYPE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-364") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_GROUPSNAME_COL", tableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-365") {
        addUniqueConstraint(columnNames: "display_name", constraintName: "UC_PRIORITYDISPLAY_NAME_COL", tableName: "PRIORITY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-366") {
        addUniqueConstraint(columnNames: "value", constraintName: "UC_PRIORITYVALUE_COL", tableName: "PRIORITY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-367") {
        addUniqueConstraint(columnNames: "group_name", constraintName: "UC_PRODUCT_GROUPGROUP_NAME_COL", tableName: "product_group")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-368") {
        addUniqueConstraint(columnNames: "value", constraintName: "UC_PVS_STATEVALUE_COL", tableName: "PVS_STATE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-369") {
        addUniqueConstraint(columnNames: "USERNAME", constraintName: "UC_PVUSERUSERNAME_COL", tableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-370") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_PV_CONCEPTNAME_COL", tableName: "PV_CONCEPT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-371") {
        addUniqueConstraint(columnNames: "report_name", constraintName: "UC_RPT_HIST_RPT_NAME_COL", tableName: "report_history")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-372") {
        addUniqueConstraint(columnNames: "AUTHORITY", constraintName: "UC_ROLEAUTHORITY_COL", tableName: "ROLE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-373") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_RPT_FIELDNAME_COL", tableName: "RPT_FIELD")
    }

    /*changeSet(author: "sandeep (generated)", id: "1573557515281-326") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_RPT_FIELD_GROUPNAME_COL", tableName: "RPT_FIELD_GROUP")
    }*/

    changeSet(author: "amit (generated)", id: "1594893043821-375") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_SAFETY_GROUPNAME_COL", tableName: "safety_group")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-376") {
        addUniqueConstraint(columnNames: "token", constraintName: "UC_SPOTFIRE_SESSIONTOKEN_COL", tableName: "spotfire_session")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-377") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_TAGNAME_COL", tableName: "TAG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-378") {
        addUniqueConstraint(columnNames: "authority", constraintName: "UC_TEST_ROLEAUTHORITY_COL", tableName: "test_role")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-379") {
        addUniqueConstraint(columnNames: "email", constraintName: "UC_TEST_SAML_USEREMAIL_COL", tableName: "test_saml_user")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-380") {
        addUniqueConstraint(columnNames: "username", constraintName: "UC_TEST_SAML_USERUSERNAME_COL", tableName: "test_saml_user")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-381") {
        addUniqueConstraint(columnNames: "department_name", constraintName: "UC_USER_DEP_NAME_COL", tableName: "user_department")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-382") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_VALIDATED_SIGNALNAME_COL", tableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-383") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_WORK_FLOW_RULESNAME_COL", tableName: "WORK_FLOW_RULES")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-384") {
        addUniqueConstraint(columnNames: "user_id, alert_type, default_view_instance_id", constraintName: "UK210f34b0cfe5b5a551cca85a2fb5", tableName: "default_view_mapping")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-385") {
        addUniqueConstraint(columnNames: "alert_type, user_id, name", constraintName: "UK7285101c782d3bb42a19d76eade0", tableName: "view_instance")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-387") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "USER_GROUP_MAPPING", constraintName: "FK1egn4hip9c2px7kmifks81ilj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-388") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "signal_history", constraintName: "FK1gksukhgbnjjbm6tq7sergnf3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-389") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "ARCHIVED_LITERATURE_ALERT", constraintName: "FK1iienwc8wr9cid8iy4byqp0mn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-390") {
        addForeignKeyConstraint(baseColumnNames: "recipients_id", baseTableName: "spotfire_notification_pvuser", constraintName: "FK1lbgy2e2cp3bspkxb7ay9xsk7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "spotfire_notification_query")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-391") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_CATEGORY_ID", baseTableName: "DISPO_RULES_TOPIC_CATEGORY", constraintName: "FK1lqlnx26rn2mthsyqoajgfwtr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC_CATEGORY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-392") {
        addForeignKeyConstraint(baseColumnNames: "EX_CONFIG_ACTIVITIES_ID", baseTableName: "ex_rconfig_activities", constraintName: "FK1lvnr30ngjjrcqtmugj4o7xxx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-393") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP", baseTableName: "EX_RCONFIG", constraintName: "FK1mah9k2g3a2vog2rc5l3oyhap", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-394") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_GLOBAL_ID", baseTableName: "SINGLE_GLOBAL_TAG_MAPPING", constraintName: "FK1pciu4e4nwseetvpojr0hrh3q", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALL_TAG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-395") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "EVDAS_SIGNAL_CONCEPTS", constraintName: "FK1pvjhvvvh16fitf3f9aepcrx3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-396") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "AGG_ALERT_ACTIONS", constraintName: "FK1spm3adg2morgiavbiypkcowf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-397") {
        addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES", constraintName: "FK1tom8u04lfcjhkpwup2d8arxa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_QUERY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-398") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "product_event_history", constraintName: "FK1vu272crmaxuyfixksks1ij0g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-399") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "default_view_mapping", constraintName: "FK212psev5e8n5368qm45hxdim9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-400") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_CASE_ALERT_ID", baseTableName: "SINGLE_ALERT_ACTIONS", constraintName: "FK21hx2fdb61yflsa58y8ykjut8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-401") {
        addForeignKeyConstraint(baseColumnNames: "literature_configuration_id", baseTableName: "EX_LITERATURE_DATE_RANGE", constraintName: "FK23j99y5cju9o2h64tv6c4xq1f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_CONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-402") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ARCHIVED_SCA", constraintName: "FK254hw7jn9lektmkp3q2rg54xd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-403") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_ID", baseTableName: "WORKFLOWRULES_ACTION_TEMPLATES", constraintName: "FK25gco89k379c8yngl2ixneunj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "WORK_FLOW_RULES")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-404") {
        addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES", constraintName: "FK28ogh0ec7qa0wls7dxduf1ee1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_QUERY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-405") {
        addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "EX_LITERATURE_CONFIG", constraintName: "FK2alihh3mnbowd4bubkjigrt2q", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-406") {
        addForeignKeyConstraint(baseColumnNames: "exec_config_id", baseTableName: "ALERTS", constraintName: "FK2asghyg8brvh2hd9ihjye9d8a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-407") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_ALERT_ID", baseTableName: "SINGLE_CASE_ALERT_TAGS", constraintName: "FK2jg2pehpoo65k30r8hp0ynaia", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-408") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "TAG", constraintName: "FK2jyfx1eu2cdnehuj8dqwrd6pi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-409") {
        addForeignKeyConstraint(baseColumnNames: "current_priority_id", baseTableName: "LITERATURE_HISTORY", constraintName: "FK2mesh43trl6soolkyvph22b2m", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-410") {
        addForeignKeyConstraint(baseColumnNames: "ADHOC_ALERT_ID", baseTableName: "VALIDATED_ADHOC_ALERTS", constraintName: "FK2mrmhl81t6ttn1ga2kcbl8nc5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-411") {
        addForeignKeyConstraint(baseColumnNames: "GROUP_ID", baseTableName: "SHARE_WITH_GROUP_FILTER", constraintName: "FK2no3an617yrnom00t5jooj7ux", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-412") {
        addForeignKeyConstraint(baseColumnNames: "DATE_RANGE_ID", baseTableName: "TEMPLT_QUERY", constraintName: "FK2skfidis7c3uhb5n3pablyibp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DATE_RANGE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-413") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "AGG_ON_DEMAND_ALERT", constraintName: "FK2vgu56trc9yw9pxi6qn8d7upj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-414") {
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "ARCHIVED_ACA_ACTIONS", constraintName: "FK2vwgh7yejh3h4vol9806scbc3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-415") {
        addForeignKeyConstraint(baseColumnNames: "topic_id", baseTableName: "SIGNAL_CHART", constraintName: "FK2vyvpc8474sber4f5uljnhef0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-416") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "priority_disposition_config", constraintName: "FK30cc3a899ljc1c1mwuvb4gojk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-417") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FK3277qdq090x04o2i21gcksi92", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-418") {
        addForeignKeyConstraint(baseColumnNames: "PVS_ALERT_TAG_ID", baseTableName: "ARCHIVED_SCA_TAGS", constraintName: "FK36oexbhattm8op3hnya3tcgoy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "pvs_alert_tag")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-419") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "evdas_history", constraintName: "FK373ar2qi8f0e9w43cus7umvfm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-420") {
        addForeignKeyConstraint(baseColumnNames: "DISPOSITION_ID", baseTableName: "DISPO_RULES_USER_GROUP", constraintName: "FK37lt1se9b4d25ksatot1whkt7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION_RULES")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-421") {
        addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "test_user_role", constraintName: "FK3g03ml77a0pr2nsyno5k23e0f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "test_role")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-422") {
        addForeignKeyConstraint(baseColumnNames: "current_assigned_to_group_id", baseTableName: "LITERATURE_HISTORY", constraintName: "FK3h89qxc4n54ltjr00fh3dfe5x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-423") {
        addForeignKeyConstraint(baseColumnNames: "default_quali_disposition_id", baseTableName: "GROUPS", constraintName: "FK3hsd6jq62pwevxuymr7c47v4o", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-424") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FK3i2ij0470itt2oyfll08nuagc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-425") {
        addForeignKeyConstraint(baseColumnNames: "MEETING_ID", baseTableName: "MEETING_ATTACHMENTS", constraintName: "FK3kly0ngl8jptbdmurwpdhn15e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEETING")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-426") {
        addForeignKeyConstraint(baseColumnNames: "EX_DATE_RANGE_INFO_ID", baseTableName: "EX_TEMPLT_QUERY", constraintName: "FK3ocntqp0fggudmyw65r9ghtth", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_DATE_RANGE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-427") {
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_LIT_ALERT_ID", baseTableName: "ARCHIVED_LIT_CASE_ALERT_TAGS", constraintName: "FK3tqo8ii2855i6p1mreo00bsms", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_LITERATURE_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-428") {
        addForeignKeyConstraint(baseColumnNames: "lnk_id", baseTableName: "attachment", constraintName: "FK417wyfahv01xv8s9ypukvvpt6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "attachment_link")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-429") {
        addForeignKeyConstraint(baseColumnNames: "business_configuration_id", baseTableName: "RULE_INFORMATION", constraintName: "FK46deuafhfurfdbrewcoueiogp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "BUSINESS_CONFIGURATION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-430") {
        addForeignKeyConstraint(baseColumnNames: "RCONFIG_ID", baseTableName: "TEMPLT_QUERY", constraintName: "FK46gb5ngvp56xrv2j0d91iuqf2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-431") {
        addForeignKeyConstraint(baseColumnNames: "ACTIVITY_ID", baseTableName: "VALIDATED_ALERT_ACTIVITIES", constraintName: "FK4e8ln2cbe20xp8afdh1y7sa9f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-432") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "signal_history", constraintName: "FK4he7y9hi5lwrs2d8tn16rsxew", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-433") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "priority_disposition_config", constraintName: "FK4iba2hmpj3ow92lvk695o6cmr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-434") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_CASE_ALERT_ID", baseTableName: "SINGLE_SIGNAL_CONCEPTS", constraintName: "FK4jhhis76bsysx4xm6i6ioykpc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-435") {
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "ARCHIVED_LIT_ALERT_ACTIONS", constraintName: "FK4k66uoy690sd2sqgt1whsv0ke", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-436") {
        addForeignKeyConstraint(baseColumnNames: "strategy_id", baseTableName: "TOPIC", constraintName: "FK4kh9et74csyh0sxnpty9v5bwf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STRATEGY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-437") {
        addForeignKeyConstraint(baseColumnNames: "RF_INFO_LIST_ID", baseTableName: "RPT_FIELD_INFO", constraintName: "FK4u8v80bl2vqgbw7bs11nm3qr2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-438") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_COMMENTS", constraintName: "FK4xwtik25kkjo898vri20gjn8h", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-439") {
        addForeignKeyConstraint(baseColumnNames: "initial_disposition_id", baseTableName: "AGG_ALERT", constraintName: "FK50yuip9o1motpj4seu0cxi61d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-440") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "LITERATURE_ALERT", constraintName: "FK51t9uul1wlam3muhfsdeni535", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-441") {
        addForeignKeyConstraint(baseColumnNames: "LITERATURE_ALERT_ID", baseTableName: "VALIDATED_LITERATURE_ALERTS", constraintName: "FK530mahbw0a4gx3tet1q3w2g0l", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-442") {
        addForeignKeyConstraint(baseColumnNames: "PVS_ALERT_TAG_ID", baseTableName: "SINGLE_CASE_ALERT_TAGS", constraintName: "FK5395n1fvj46kencfit7o01ycm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "pvs_alert_tag")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-443") {
        addForeignKeyConstraint(baseColumnNames: "USER_ID", baseTableName: "SHARE_WITH_USER_FILTER", constraintName: "FK54h8y7xtiv357hvjskdlhv9nh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-444") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "TOPIC", constraintName: "FK557wy3htik1yrmw2ldt3lujx9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-445") {
        addForeignKeyConstraint(baseColumnNames: "MAP_TABLE_NAME_ATM_ID", baseTableName: "CASE_COLUMN_JOIN_MAPPING", constraintName: "FK568vr26l2ip7hmy80k1umjtw9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "SOURCE_TABLE_MASTER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-446") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "AD_HOC_TOPIC_CONCEPTS", constraintName: "FK58riu8smk8hl4hwwx89gr2v4w", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-447") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ACTIVITIES", constraintName: "FK5967su0w6fjlm4na1gewgy3ts", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-448") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "ARCHIVED_SINGLE_CASE_ALERT", constraintName: "FK5b51h27a8nwqul2cqg130od6x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-449") {
        addForeignKeyConstraint(baseColumnNames: "AD_HOC_ALERT_ID", baseTableName: "AD_HOC_SIGNAL_CONCEPTS", constraintName: "FK5bcrnlc25y3lkkm5d8xsqrb83", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-450") {
        addForeignKeyConstraint(baseColumnNames: "PVS_ALERT_TAG_ID", baseTableName: "LITERATURE_CASE_ALERT_TAGS", constraintName: "FK5lld0tglw6bwtcm3q9v72afnw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "pvs_alert_tag")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-451") {
        addForeignKeyConstraint(baseColumnNames: "EVDAS_CONFIG_ID", baseTableName: "VS_EVDAS_CONFIG", constraintName: "FK5qse9kfhl8x3c7v4yl99yy5ha", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_CONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-452") {
        addForeignKeyConstraint(baseColumnNames: "commented_by_id", baseTableName: "COMMENTS", constraintName: "FK5r4u2uxs7lo6xkmrbmio08003", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-453") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "view_instance", constraintName: "FK61t29rs90yvo8nrrjuy8hnrpp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-454") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "evdas_history", constraintName: "FK67y86bymxk3lnavaj4ax1fmmm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-455") {
        addForeignKeyConstraint(baseColumnNames: "default_adhoc_disposition_id", baseTableName: "GROUPS", constraintName: "FK6aecm7dwqlvw1fubghjflo9wk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-456") {
        addForeignKeyConstraint(baseColumnNames: "ALERT_ID", baseTableName: "ADHOC_ALERT_ACTIONS", constraintName: "FK6b0na7al8svxgc22f6rmb8fyr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-457") {
        addForeignKeyConstraint(baseColumnNames: "RPT_FIELD_ID", baseTableName: "RPT_FIELD_INFO", constraintName: "FK6d5e73n3n8q05npuax4fknirw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-458") {
        addForeignKeyConstraint(baseColumnNames: "SOURCE_COLUMN_MASTER_ID", baseTableName: "RPT_FIELD", constraintName: "FK6hfxx9xfelgaomred8dn99iig", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "REPORT_ITEM", referencedTableName: "SOURCE_COLUMN_MASTER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-459") {
        addForeignKeyConstraint(baseColumnNames: "RPT_RESULT_ID", baseTableName: "RPT_ERROR", constraintName: "FK6m8eikuwcfty44ymm36rxip6c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_RESULT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-460") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_SIGNAL_GROUP", constraintName: "FK6o7whfp7w45byxkfkg3ajgy8d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-461") {
        addForeignKeyConstraint(baseColumnNames: "config_id", baseTableName: "ACTIONS", constraintName: "FK6qnyouq2pj8noqbbgr6blr0fa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTION_CONFIGURATIONS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-462") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP", baseTableName: "EVDAS_CONFIG", constraintName: "FK6sm0nv9c76tc7xx351jufvlo5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-463") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "LITERATURE_CONFIG", constraintName: "FK6vwyki46c40l23phcl2jsjovv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-464") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "SINGLE_SIGNAL_CONCEPTS", constraintName: "FK6xlkiqggquuh3unim09gl7s5k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-465") {
        addForeignKeyConstraint(baseColumnNames: "topic_actions_id", baseTableName: "topic_actions", constraintName: "FK6xwerddtt6xmysbwpqm0thskw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-466") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "ARCHIVED_AGG_ALERT", constraintName: "FK71rkprlwc9p2db3fbch29cpfs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-467") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_SINGLE_ALERTS", constraintName: "FK73rmou5rxoc3xenxf07klxnvl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-468") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ACTIONS", constraintName: "FK795v9wlhwg7olj50atxgreqp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-469") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP_ID", baseTableName: "DISPO_RULES_WORKFLOW_GROUP", constraintName: "FK7e6rwklblhyoo34rnjvek8fin", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-470") {
        addForeignKeyConstraint(baseColumnNames: "workflow_state_id", baseTableName: "TOPIC", constraintName: "FK7fprf6132fra32pjukedntqyl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-471") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "EX_RCONFIG", constraintName: "FK7jnrbgs0ar70viidp7mwns3sc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-472") {
        addForeignKeyConstraint(baseColumnNames: "auto_route_disposition_id", baseTableName: "GROUPS", constraintName: "FK7jp5xha2vlmovt0uxbyjki99", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-473") {
        addForeignKeyConstraint(baseColumnNames: "current_disposition_id", baseTableName: "LITERATURE_HISTORY", constraintName: "FK7lc0bubx7p9yabs83kcq36ps", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-474") {
        addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_ID", baseTableName: "DELIVERIES_SHARED_WITHS", constraintName: "FK7ofjwn1n4eyo0b9ho6nqthf83", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-475") {
        addForeignKeyConstraint(baseColumnNames: "PARAM_ID", baseTableName: "VALUES_PARAMS", constraintName: "FK7p44v7rjuor7a67e1uuhh00ki", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PARAM")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-476") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "AGG_ALERT", constraintName: "FK7uqp71w2q1kumo5txv4soekoi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-477") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "AGG_SIGNAL_CONCEPTS", constraintName: "FK7uu820jqwt2ggpbwt5q1n3irg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-478") {
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_USERID", baseTableName: "SHARE_WITH_USER_ADHOC", constraintName: "FK7vrrx66aotafbdx6uc56kbqvu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-479") {
        addForeignKeyConstraint(baseColumnNames: "GLOBAL_CASE_ID", baseTableName: "SINGLE_GLOBAL_TAGS", constraintName: "FK7xpvynkdusubvmx9lc02em2i3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalCaseId", referencedTableName: "global_case")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-480") {
        addForeignKeyConstraint(baseColumnNames: "EX_RCONFIG_ID", baseTableName: "SHARED_WITH", constraintName: "FK7yoqaegsqnf7fv4hmpsxc0d5u", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-481") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "TOPIC", constraintName: "FK81ax335qvxywo42jwjpk2uj70", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-482") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FK81iwl1ty36fpnf3hbv07ge0e7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-483") {
        addForeignKeyConstraint(baseColumnNames: "initial_disposition_id", baseTableName: "ALERTS", constraintName: "FK81yjfpvqj3gom68hkqv4s3o0w", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-484") {
        addForeignKeyConstraint(baseColumnNames: "TEMPLT_VALUE_ID", baseTableName: "TEMPLT_QRS_TEMPLT_VALUES", constraintName: "FK82nwedxu2puppkbvcn5xj3m15", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TEMPLT_VALUE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-485") {
        addForeignKeyConstraint(baseColumnNames: "initial_disposition_id", baseTableName: "EVDAS_ALERT", constraintName: "FK82sv7r2pavn03feadmrvxov2r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-486") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ALERT_DOCUMENTS", constraintName: "FK82uvw0fbs7p68ocv156ous0ho", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-487") {
        addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "RULE_INFORMATION", constraintName: "FK82y4skeo5y3n493ropl4b6ior", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTION_TEMPLATE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-488") {
        addForeignKeyConstraint(baseColumnNames: "ha_signal_status_id", baseTableName: "ALERTS", constraintName: "FK831h6rebhwqcu11l3bmv3m9gt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-489") {
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_USERID", baseTableName: "SHARE_WITH_USER_LITR_CONFIG", constraintName: "FK86n78knlffb2f3yg8c9jhhlqj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-490") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VAL_SIGNAL_TOPIC_CATEGORY", constraintName: "FK86r3dj8pbqyfn9p1d3cphcxwd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-491") {
        addForeignKeyConstraint(baseColumnNames: "ex_lit_search_config_id", baseTableName: "ARCHIVED_LITERATURE_ALERT", constraintName: "FK87vw6p2kttreaxshoagh8nhxy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_LITERATURE_CONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-492") {
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_GROUPID", baseTableName: "SHARE_WITH_GROUP_CONFIG", constraintName: "FK88ek3tuwyqh526ly1ij7f84as", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-493") {
        addForeignKeyConstraint(baseColumnNames: "PV_USER_ID", baseTableName: "RPT_TEMPLT", constraintName: "FK88o1kq939jqa3ujho728e9hj6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-494") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "EVDAS_ON_DEMAND_ALERT", constraintName: "FK89l5ihyquqm5cgq9mlooxfyx9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_CONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-495") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FK8afup0gx565jdbopkd4b9xalg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-496") {
        addForeignKeyConstraint(baseColumnNames: "income_state_id", baseTableName: "WORK_FLOW_RULES", constraintName: "FK8d35ldteverucbekx4ueliwxo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-497") {
        addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "topic_actions", constraintName: "FK8ewu1k8gy0uup9i3l7edt5u0g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }
//todo check
    changeSet(author: "amit (generated)", id: "1594893043821-498") {
        addForeignKeyConstraint(baseColumnNames: "GLOBAL_PRODUCT_EVENT_ID", baseTableName: "AGG_GLOBAL_TAGS", constraintName: "FK8fru5jqrjb81756xvmoh203uw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalProductEventId", referencedTableName: "global_product_event")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-499") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_GROUP", constraintName: "FK8i6w6mah3h65hjsr0xiwwj247", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-500") {
        addForeignKeyConstraint(baseColumnNames: "global_identity_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FK8ic0iqi8eynbxkkwxroc6io1r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalCaseId", referencedTableName: "global_case")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-501") {
        addForeignKeyConstraint(baseColumnNames: "COMMENT_ID", baseTableName: "VALIDATED_ALERT_COMMENTS", constraintName: "FK8iktftscl552215n7hl8xr46m", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_COMMENT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-502") {
        addForeignKeyConstraint(baseColumnNames: "EX_QUERY_VALUE_ID", baseTableName: "EX_ALERT_QUERY_VALUES", constraintName: "FK8ipmwav0ngw8l0nwefligk05i", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_QUERY_VALUE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-503") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "EX_LITERATURE_CONFIG", constraintName: "FK8jqoqf0lhagmub5an0yxoma3g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-504") {
        addForeignKeyConstraint(baseColumnNames: "TABLE_NAME_ATM_ID", baseTableName: "CASE_COLUMN_JOIN_MAPPING", constraintName: "FK8l9hjbep49n1p5jgt1lwktf1x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "SOURCE_TABLE_MASTER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-505") {
        addForeignKeyConstraint(baseColumnNames: "activity_id", baseTableName: "meeting_activities", constraintName: "FK8o8dfpgm739ed1u78v767vsvt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-506") {
        addForeignKeyConstraint(baseColumnNames: "ROW_COLS_RF_INFO_LIST_ID", baseTableName: "CLL_TEMPLT", constraintName: "FK8p9endljdo1ex5s0am5h174cw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-507") {
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_USERID", baseTableName: "SHARE_WITH_USER_SIGNAL", constraintName: "FK8r3bv1bet4qwkng62b9umxu85", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-508") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP", baseTableName: "VALIDATED_SIGNAL", constraintName: "FK8rwroyawlknqh2oxffgfixhfe", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-509") {
        addForeignKeyConstraint(baseColumnNames: "REPORT_FIELD_ID", baseTableName: "QUERY_EXP_VALUE", constraintName: "FK8v8dg0fy3u8lq3l3u4c4u0mcu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-510") {
        addForeignKeyConstraint(baseColumnNames: "SQL_TEMPLT_ID", baseTableName: "SQL_TEMPLTS_SQL_VALUES", constraintName: "FK8wopocxfmf15ej1v33p2twnc6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SQL_TEMPLT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-511") {
        addForeignKeyConstraint(baseColumnNames: "ACTIVITY_ID", baseTableName: "TOPIC_ACTIVITIES", constraintName: "FK923jql3sdm5jk6ht1an9nn7ov", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-512") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "TOPIC", constraintName: "FK94byri2xuxp9jqxhjmvb4a8x2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-513") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "AD_HOC_SIGNAL_CONCEPTS", constraintName: "FK993e2h8qmb5k5m9d2oo2vh3i8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-514") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FK99lpraxpe9sb9vixs14twpoi9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-515") {
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_EVDAS_ALERT_ID", baseTableName: "ARCHIVED_EVDAS_ALERT_ACTIONS", constraintName: "FK9k0mrfmtad3s0gfgy7uru5v6x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_EVDAS_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-516") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "STRATEGY_MEDICAL_CONCEPTS", constraintName: "FK9k5fv2tt7vtl5d0rce4f46ws", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-517") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ADHOC_ALERTS", constraintName: "FK9lmmhn29xffgygx6iaduvqmqb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-518") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "USER_GROUPS", constraintName: "FK9nm9nx0a6hcur0207kh6k5dn9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-519") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ALERTS", constraintName: "FK9nmky66wnq1cefm2snii3dsn6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-520") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_AGG_ALERTS", constraintName: "FK9q2pi8025jxe8bbgtyldlp6tp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-521") {
        addForeignKeyConstraint(baseColumnNames: "date_range_information_id", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FK9tk3chwgfb3ci8vloblnxd53b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_EVDAS_DATE_RANGE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-522") {
        addForeignKeyConstraint(baseColumnNames: "DISPOSITION_ID", baseTableName: "RCONFIG_DISPOSITION", constraintName: "FK9v26sfjmamkwv5h7yyh7k8vop", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-523") {
        addForeignKeyConstraint(baseColumnNames: "EVDAS_ALERT_ID", baseTableName: "EVDAS_SIGNAL_CONCEPTS", constraintName: "FK9vmxhc2kgh0i6pcw4jqcdm819", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-524") {
        addForeignKeyConstraint(baseColumnNames: "global_identity_id", baseTableName: "AGG_ALERT", constraintName: "FK9wophmgrppuk00kumrk81waqb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalProductEventId", referencedTableName: "global_product_event")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-525") {
        addForeignKeyConstraint(baseColumnNames: "initial_disposition_id", baseTableName: "ARCHIVED_SINGLE_CASE_ALERT", constraintName: "FK9x6gb9gkqb0s4m4kmc2rbnbu8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-526") {
        addForeignKeyConstraint(baseColumnNames: "TEMPLT_QUERY_ID", baseTableName: "RPT_RESULT", constraintName: "FK9xhb2aa0o52td9v65k84gpvb1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TEMPLT_QUERY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-527") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP", baseTableName: "RCONFIG", constraintName: "FKa2kbegt02usojx08bka5tlcqt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-528") {
        addForeignKeyConstraint(baseColumnNames: "COMMENT_ID", baseTableName: "TOPIC_COMMENTS", constraintName: "FKa3cqvwnj1j7q2yi5coqlp2br4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_COMMENT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-529") {
        addForeignKeyConstraint(baseColumnNames: "type_id", baseTableName: "ACTIONS", constraintName: "FKa3rycbytmp5ea3n5dxc5hiaxo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTION_TYPES")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-530") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "LITERATURE_CONFIG", constraintName: "FKa5gt5n32yb0s455yd36wvutkd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-531") {
        addForeignKeyConstraint(baseColumnNames: "advanced_filter_id", baseTableName: "view_instance", constraintName: "FKa5uu337r7cfmxb18x68709tf1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ADVANCED_FILTER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-532") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "EVDAS_ALERT", constraintName: "FKa87y6kvqdbxkl5gef3rft9cl0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-533") {
        addForeignKeyConstraint(baseColumnNames: "date_range_information_id", baseTableName: "LITERATURE_CONFIG", constraintName: "FKa8rlw0012wytmrfayjlx3bw0e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_DATE_RANGE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-534") {
        addForeignKeyConstraint(baseColumnNames: "EX_RCONFIG_ID", baseTableName: "RPT_ERROR", constraintName: "FKaa2wqfteyjdl906bs3t05k0s8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-535") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "test_user_role", constraintName: "FKacxggj2twirtdb9ysv9g5q2sa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "test_saml_user")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-536") {
        addForeignKeyConstraint(baseColumnNames: "EX_EVDAS_CONFIG_ID", baseTableName: "EX_EVDAS_CONFIG_ACTIVITIES", constraintName: "FKahisevpydu0thicdyjm49v5w9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_EVDAS_CONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-537") {
        addForeignKeyConstraint(baseColumnNames: "GROUP_ID", baseTableName: "TOPIC_GROUP", constraintName: "FKaijamtneohj273nu1ncurw295", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-538") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "LITERATURE_ALERT", constraintName: "FKaims0i1suspmoa1et17ll8g86", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-539") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_CATEGORY_ID", baseTableName: "VAL_SIGNAL_TOPIC_CATEGORY", constraintName: "FKaj0rnca6k5rfil9h32x5m6pbu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC_CATEGORY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-540") {
        addForeignKeyConstraint(baseColumnNames: "GRP_ALERT_DISP_ID", baseTableName: "GRP_ALERT_DISP", constraintName: "FKajsw0c9d9gu2wlqqxvi1h1a30", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-541") {
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_SCA_ID", baseTableName: "ARCHIVED_SCA_ACTIONS", constraintName: "FKakgfpbd6u2xm53cbtnadny890", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-542") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ALERT_ACTIVITIES", constraintName: "FKalinqp5i92dk44sa1l98og467", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-543") {
        addForeignKeyConstraint(baseColumnNames: "GROUP_ID", baseTableName: "VALIDATED_SIGNAL_GROUP", constraintName: "FKant7fchwn6ya4i1yvhgqt8djc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-544") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ARCH_EVDAS_ALERTS", constraintName: "FKaucsybjvph30abcvy71hafod5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-545") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "AGG_ALERT_TAGS", constraintName: "FKayc7ooteg4uqk1y8oth5gi85w", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-546") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FKb004pyaimrqt9rvj6i1vcu382", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-547") {
        addForeignKeyConstraint(baseColumnNames: "type_id", baseTableName: "LITERATURE_ACTIVITY", constraintName: "FKb1h9h6gsjltr4to435axmlqba", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITY_TYPE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-548") {
        addForeignKeyConstraint(baseColumnNames: "SIGNAL_STRATEGY_ID", baseTableName: "STRATEGY_MEDICAL_CONCEPTS", constraintName: "FKb309drlgw2wgk40cmpq4iqqmo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STRATEGY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-549") {
        addForeignKeyConstraint(baseColumnNames: "EX_ALERT_DATE_RANGE_ID", baseTableName: "EX_RCONFIG", constraintName: "FKb5kjpnalh2rbc3x34i1aw9073", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_ALERT_DATE_RANGE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-550") {
        addForeignKeyConstraint(baseColumnNames: "COLUMNS_RFI_LIST_ID", baseTableName: "DTAB_COLUMN_MEASURE", constraintName: "FKb61pwfq5cj72hri56yej80i0m", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-551") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "evdas_file_process_log", constraintName: "FKb63thlw61wkcwi0uebhwttj0c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-552") {
        addForeignKeyConstraint(baseColumnNames: "EVDAS_ALERT_ID", baseTableName: "VALIDATED_EVDAS_ALERTS", constraintName: "FKb790uhowictwtiygfm6n9wsrs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-553") {
        addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_ID", baseTableName: "EX_STATUSES_SHARED_WITHS", constraintName: "FKb83gyva8nj5ctoykqkov8ongn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-554") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "WORKFLOWRULES_GROUPS", constraintName: "FKbaasjf1q5jdh8yb9dgo2e5c5x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-555") {
        addForeignKeyConstraint(baseColumnNames: "LM_TABLE_NAME_ATM_ID", baseTableName: "SOURCE_COLUMN_MASTER", constraintName: "FKbbo1adbmuh5tbw11h9j1m54rg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "SOURCE_TABLE_MASTER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-556") {
        addForeignKeyConstraint(baseColumnNames: "EX_RCONFIG_ID", baseTableName: "EX_TEMPLT_QUERY", constraintName: "FKbbsstpmcsh6gpgf7v3rxr5tn6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-557") {
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_GROUPID", baseTableName: "SHARE_WITH_GROUP_SIGNAL", constraintName: "FKbc9bxnvjr82pma735xaxwpt1e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-558") {
        addForeignKeyConstraint(baseColumnNames: "GROUP_ID", baseTableName: "SHARE_WITH_GROUP_VIEW", constraintName: "FKbdy37vtjaj0m7xoigaclxb4on", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-559") {
        addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "LITERATURE_CONFIG", constraintName: "FKbh94j1hyx8woow3tuhfpw771n", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-560") {
        addForeignKeyConstraint(baseColumnNames: "PROD_GRP_ID", baseTableName: "RCONFIGS_PROD_GRP", constraintName: "FKbhhfbf7noaxk5m7dv8fs6vhry", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "product_group")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-561") {
        addForeignKeyConstraint(baseColumnNames: "SCHEDULED_PVUSER_ID", baseTableName: "RPT_RESULT", constraintName: "FKbmfelgea61vxim4d9lodgmw3r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-562") {
        addForeignKeyConstraint(baseColumnNames: "shared_with_id", baseTableName: "ALERTS", constraintName: "FKboxvx12d3pno2w2v4vqcwr3y3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-563") {
        addForeignKeyConstraint(baseColumnNames: "strategy_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKbwna09ojl0fcs9fpgv65fnc14", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STRATEGY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-564") {
        addForeignKeyConstraint(baseColumnNames: "LITERATURE_ALERT_ID", baseTableName: "LIT_ALERT_ACTIONS", constraintName: "FKbwscav6fgh7pp95mn2p7joyr6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-565") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "pvuser_safety_groups", constraintName: "FKc046tqmqedqp9141fa2soxx7x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-566") {
        addForeignKeyConstraint(baseColumnNames: "exec_configuration_id", baseTableName: "ARCHIVED_AGG_ALERT", constraintName: "FKc2c8tbtsjri2vwelqffpwsib2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-567") {
        addForeignKeyConstraint(baseColumnNames: "default_evdas_disposition_id", baseTableName: "GROUPS", constraintName: "FKc31v2f5sfqjsgixxp1ffhxtjk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-568") {
        addForeignKeyConstraint(baseColumnNames: "USER_GROUP_ID", baseTableName: "DISPO_RULES_USER_GROUP", constraintName: "FKc9jts755p0p0fmxn0ppc276aw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-569") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "LITERATURE_ALERT", constraintName: "FKcf0w313ha302l1ybaf3vy5d5d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-570") {
        addForeignKeyConstraint(baseColumnNames: "NONCASE_SQL_TEMPLT_ID", baseTableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES", constraintName: "FKci1t92wr5cug7k5o1y9u2e9v5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "NONCASE_SQL_TEMPLT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-571") {
        addForeignKeyConstraint(baseColumnNames: "target_disposition_id", baseTableName: "DISPOSITION_RULES", constraintName: "FKcms27yedf7sdog3u36hlqg9iu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-572") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_group_s", constraintName: "FKcrqry31n9mka1oh51bhsn8r99", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-573") {
        addForeignKeyConstraint(baseColumnNames: "ALERT_TAG_ID", baseTableName: "SINGLE_ALERT_TAGS", constraintName: "FKcuf5j28ivfuw9jkg1mlthxkbn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_TAG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-574") {
        addForeignKeyConstraint(baseColumnNames: "incoming_disposition_id", baseTableName: "DISPOSITION_RULES", constraintName: "FKcwneoxfjkmjy0q8v73phwc63n", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-575") {
        addForeignKeyConstraint(baseColumnNames: "workflow_rule_id", baseTableName: "WORKFLOWRULES_GROUPS", constraintName: "FKd2i9nh4hjfnjt0d3hf56kml3h", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "WORK_FLOW_RULES")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-576") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKd6owieejvfmafwh8dyofkenut", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-577") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "TOPIC_AGG_ALERTS", constraintName: "FKd74cpexfn1jrxsjhy23g3kqjb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-578") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_ALERT_ID", baseTableName: "VALIDATED_SINGLE_ALERTS", constraintName: "FKd9ws3gvh9jkokbmx8qej0nt7q", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-579") {
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "AGG_ALERT_ACTIONS", constraintName: "FKdaa6kaucnowbyqhb0salrcwsl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-580") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "ARCHIVED_LITERATURE_ALERT", constraintName: "FKdg7m9of1e7mxdseyjpxvfwdii", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-581") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "ARCHIVED_AGG_ALERT", constraintName: "FKdgp2hsdsukq904rr3ln0yugwh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-582") {
        addForeignKeyConstraint(baseColumnNames: "current_assigned_to_id", baseTableName: "CASE_HISTORY", constraintName: "FKdhqqeepqe5rbesr70u9gw7ktm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-583") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_ACTIVITIES", constraintName: "FKdk2ms0bbvlg9wt85bl3h172c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-584") {
        addForeignKeyConstraint(baseColumnNames: "QUERY_VALUE_ID", baseTableName: "TEMPLT_QRS_QUERY_VALUES", constraintName: "FKdr8517c590a2tx7da58eb0med", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY_VALUE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-585") {
        addForeignKeyConstraint(baseColumnNames: "ATTACHMENTS_ID", baseTableName: "MEETING_ATTACHMENTS", constraintName: "FKdujjes8cvidp8xttfhe9yltmx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "FILE_ATTACHMENTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-586") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_AGG_ALERTS", constraintName: "FKdumjbc9ixhbyvwdgivdcqbcgp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-587") {
        addForeignKeyConstraint(baseColumnNames: "exec_configuration_id", baseTableName: "EVDAS_ALERT", constraintName: "FKdvuy2bffdvtxtcyne7yvm2ufl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_EVDAS_CONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-588") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VS_EVAL_METHOD", constraintName: "FKe0ksh1e8gfwkuuc38rs6xbavk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-589") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "ACTIONS", constraintName: "FKe2e0g80hg3uajb4mb48d1k92y", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-590") {
        addForeignKeyConstraint(baseColumnNames: "AD_HOC_ALERT_ID", baseTableName: "AD_HOC_TOPIC_CONCEPTS", constraintName: "FKe3ogu1iyyv0d8jxiu1m5nu1dc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-591") {
        addForeignKeyConstraint(baseColumnNames: "lit_search_config_id", baseTableName: "LITERATURE_ALERT", constraintName: "FKe4a3tfxg6bd0wg0b7pvfmp0pk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_CONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-592") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "ALERTS", constraintName: "FKe4dcmnjtoep5k5c638sllylqu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-593") {
        addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "EX_RCONFIG", constraintName: "FKe9j0svwa1r5mwvgm3bf5m6eq1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-594") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "ACTIVITIES", constraintName: "FKeaneople79bqyu7x49t5reovi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-596") {
        addForeignKeyConstraint(baseColumnNames: "global_identity_id", baseTableName: "ARCHIVED_AGG_ALERT", constraintName: "FKedhgc4ecfee0ta6ixe5syha4e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalProductEventId", referencedTableName: "global_product_event")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-597") {
        addForeignKeyConstraint(baseColumnNames: "topic_id", baseTableName: "MEETING", constraintName: "FKef6s7ftgt9xb8fvpsp5lbuxrc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-598") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_SIGNAL_RCONFIG", constraintName: "FKehg8x540mv0qpy5plqyf7ktqt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-599") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_EVDAS_ALERTS", constraintName: "FKek8566hlirn551hgcxfl9fbpl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-600") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "ALERTS", constraintName: "FKel2n4td5vn9pt0x6tmxjkq1o0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-601") {
        addForeignKeyConstraint(baseColumnNames: "exec_config_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FKenxk6e969lecixm4isf1svn0p", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-602") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "VALIDATED_AGG_ALERTS", constraintName: "FKeq4e569rluxwh2bh0fil7efp8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-603") {
        addForeignKeyConstraint(baseColumnNames: "default_view_instance_id", baseTableName: "default_view_mapping", constraintName: "FKf0xb0id8sbbaribi0u1vlb407", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "view_instance")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-604") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VS_EVDAS_CONFIG", constraintName: "FKf1ryj8ajrpjphnyv68dq5bll2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-605") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "RCONFIG", constraintName: "FKf4d2b2hthl4s6fvbvq0uas5av", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-606") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "ARCHIVED_SINGLE_CASE_ALERT", constraintName: "FKf67kp89dp9wnb9at46cfyg4eg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-607") {
        addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "work_flow_rules_actions", constraintName: "FKf7jgah5sul11a45tsbuggvek4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-608") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_SINGLE_ALERTS", constraintName: "FKf8kqtv3nu1fgrdlcikfp4nfcm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-609") {
        addForeignKeyConstraint(baseColumnNames: "meeting_actions_id", baseTableName: "meeting_actions", constraintName: "FKf8ro1wl6geleojkypcnjercbp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEETING")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-610") {
        addForeignKeyConstraint(baseColumnNames: "EVDAS_ALERT_ID", baseTableName: "EVDAS_ALERT_ACTIONS", constraintName: "FKfajq9c8l8as3u0p8h65m522ua", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-611") {
        addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_ID", baseTableName: "EX_DELIVERIES_SHARED_WITHS", constraintName: "FKffoorb58n2mlt1sjcg481t97u", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-612") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "report_history", constraintName: "FKffw3i3fbs6jyf8kqojxola39s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-613") {
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_GROUPID", baseTableName: "SHARE_WITH_GROUP_LITR_CONFIG", constraintName: "FKfk7kmeqebxjcxsgq733o3uh0d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-614") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "user_group_s", constraintName: "FKfpfe7d6ea5bnivbe7gnc03622", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-615") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_ID", baseTableName: "WORKFLOWRULES_SIGNAL_CATEGORY", constraintName: "FKfubdtk7gwoa8s8919uafl2jj3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "WORK_FLOW_RULES")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-616") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "LITERATURE_CONFIG", constraintName: "FKfvp89jp3dlfpaymistjk1tedp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-617") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_CASE_ALERT_ID", baseTableName: "SINGLE_TOPIC_CONCEPTS", constraintName: "FKfwtfcguybm93tkxxgtbkf3yn0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-618") {
        addForeignKeyConstraint(baseColumnNames: "SIGNAL_CATEGORY_ID", baseTableName: "WORKFLOWRULES_SIGNAL_CATEGORY", constraintName: "FKg049m72c3c0vmmyyg47jky5w5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC_CATEGORY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-619") {
        addForeignKeyConstraint(baseColumnNames: "TEMPLT_QUERY_ID", baseTableName: "TEMPLT_QRS_TEMPLT_VALUES", constraintName: "FKg1yueio2f2400ouiv8y29wrji", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TEMPLT_QUERY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-620") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "signal_history", constraintName: "FKg2etf80lqk7p2chk6848imhyj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-621") {
        addForeignKeyConstraint(baseColumnNames: "ACTION_TEMPLATE_ID", baseTableName: "WORKFLOWRULES_ACTION_TEMPLATES", constraintName: "FKg7jny08nmnya6p6b4o7fxxpyq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTION_TEMPLATE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-622") {
        addForeignKeyConstraint(baseColumnNames: "CATEGORY_ID", baseTableName: "RPT_TEMPLT", constraintName: "FKgcan6gm4h4l0i5xg5qppa4247", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "CATEGORY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-623") {
        addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "meeting_actions", constraintName: "FKgd1k2pcfls85gbq25kdko2lm6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-624") {
        addForeignKeyConstraint(baseColumnNames: "global_identity_id", baseTableName: "LITERATURE_ALERT", constraintName: "FKgiwl95uuujn62d7d50ay6oye2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalArticleId", referencedTableName: "global_article")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-625") {
        addForeignKeyConstraint(baseColumnNames: "USER_ID", baseTableName: "SHARE_WITH_USER_VIEW", constraintName: "FKgixjyq9jo9w2uahbvmpxbn3j0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-626") {
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_EVDAS_ALERT_ID", baseTableName: "VALIDATED_ARCH_EVDAS_ALERTS", constraintName: "FKgpnn8ylkort0al0ldjjsatkun", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_EVDAS_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-627") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FKgqh0iu7w66xqgio4jv38b5hmf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-628") {
        addForeignKeyConstraint(baseColumnNames: "global_identity_id", baseTableName: "ARCHIVED_LITERATURE_ALERT", constraintName: "FKgr8oc3jgwcv7ytkh2hea9ktvy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalArticleId", referencedTableName: "global_article")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-629") {
        addForeignKeyConstraint(baseColumnNames: "ALERT_DOCUMENT_ID", baseTableName: "VALIDATED_ALERT_DOCUMENTS", constraintName: "FKguaxlxc330u6by2rdbpa7o15s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_DOCUMENT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-630") {
        addForeignKeyConstraint(baseColumnNames: "ALERT_DATA_RANGE_ID", baseTableName: "RCONFIG", constraintName: "FKguyscui10qub13216q7omwk6r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_DATE_RANGE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-631") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "ARCHIVED_SINGLE_CASE_ALERT", constraintName: "FKhebdw8tpyc76x8vf04mlyty8x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-632") {
        addForeignKeyConstraint(baseColumnNames: "attachment_id", baseTableName: "ATTACHMENT_DESCRIPTION", constraintName: "FKhf1vmfljes8qfvmkksj0r56gr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "attachment")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-633") {
        addForeignKeyConstraint(baseColumnNames: "ACTIVITY_ID", baseTableName: "EX_EVDAS_CONFIG_ACTIVITIES", constraintName: "FKhfddlxpqvfaocje2iwp6umh63", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-634") {
        addForeignKeyConstraint(baseColumnNames: "guest_attendee_id", baseTableName: "meeting_guest_attendee", constraintName: "FKhgeofgkdx1rjj5s2yglnetx6y", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GUEST_ATTENDEE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-635") {
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "LIT_ALERT_ACTIONS", constraintName: "FKhhg10vfuqjejwgwe05cngrij5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-636") {
        addForeignKeyConstraint(baseColumnNames: "safety_group_id", baseTableName: "PRODUCT_DICTIONARY_CACHE", constraintName: "FKhhjqmaurc06u0g2sd8pk0puw3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "safety_group")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-637") {
        addForeignKeyConstraint(baseColumnNames: "meeting_owner_id", baseTableName: "MEETING", constraintName: "FKhipac92y3mafijg8snr452hkx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-638") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKhjr0k31x2dckpqrnrq4u8hffp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-639") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "AGG_ALERT", constraintName: "FKhp8lobw060slxq44dpqk23aq7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-640") {
        addForeignKeyConstraint(baseColumnNames: "GROUP_ID", baseTableName: "EVDAS_CONFIGURATION_GROUPS", constraintName: "FKhqjqjxidasmpsaf9jsa1m6fkf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-641") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "ADVANCED_FILTER", constraintName: "FKhrs1not8xdn6i0qofrejeridc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-642") {
        addForeignKeyConstraint(baseColumnNames: "QUERY_VALUE_ID", baseTableName: "ALERT_QUERY_VALUES", constraintName: "FKhv09gbc0ogxdov8llx7jt5961", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY_VALUE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-643") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "ARCHIVED_AGG_ALERT", constraintName: "FKhxyvly9aoqmq5w44fdkmajp2f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-644") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_ALERT_ID", baseTableName: "TOPIC_SINGLE_ALERTS", constraintName: "FKhycn6c1tko90ojyv3m9q92b4b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-645") {
        addForeignKeyConstraint(baseColumnNames: "audit_trail_id", baseTableName: "audit_child_log", constraintName: "FKi2lq3l44yxa3o6afk12xj36ip", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "audit_log")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-646") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_LITERATURE_ALERTS", constraintName: "FKid2pii4l2042xd6m3si3yav58", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-647") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "ARCHIVED_EVDAS_ALERT", constraintName: "FKidddx5vd67thtokgpa8h3t17g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-648") {
        addForeignKeyConstraint(baseColumnNames: "EX_QUERY_VALUE_ID", baseTableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES", constraintName: "FKig3y20w0203iai5v122a4em5e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_QUERY_VALUE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-649") {
        addForeignKeyConstraint(baseColumnNames: "signal_strategy_id", baseTableName: "PV_CONCEPT", constraintName: "FKii9w7n9su0kelhwdy64fnoh4a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STRATEGY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-650") {
        addForeignKeyConstraint(baseColumnNames: "parent_id", baseTableName: "COMMENTS", constraintName: "FKipm49nd1i0adasdsh4c4pkc88", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "COMMENTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-651") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "product_event_history", constraintName: "FKit1j3spl8apjv7adws38jn99a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-652") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "LITERATURE_ALERT", constraintName: "FKitf2xvh5yq2hykg0o1ff2yi6k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-653") {
        addForeignKeyConstraint(baseColumnNames: "validated_signal_id", baseTableName: "SIGNAL_CHART", constraintName: "FKiwek5gp0875l735x1kt69iaq7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-654") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "ALERTS", constraintName: "FKixthhci8k983scn1rxj2vrtar", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-655") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "AGG_CASE_ALERT_TAGS", constraintName: "FKiy6dx1s4p9odra4pxi597sr1s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-656") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "evdas_history", constraintName: "FKj0e4kr6h7ba4h1j66a0qsvwx6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-657") {
        addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "RPT_RESULT", constraintName: "FKj0jkrus3o90ljhg1b6ebuttyj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_QUERY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-658") {
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_GROUPID", baseTableName: "SHARE_WITH_GROUP_ADHOC", constraintName: "FKj293s15eb37wlr6gn0yjjpprh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-659") {
        addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "RPT_TEMPLTS_TAGS", constraintName: "FKj3em09hnn5j4v4jmtva0y7x4t", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TAG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-660") {
        addForeignKeyConstraint(baseColumnNames: "PVS_ALERT_TAG_ID", baseTableName: "AGG_CASE_ALERT_TAGS", constraintName: "FKj3x5c42aklughf7f6acvddvk5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "pvs_alert_tag")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-661") {
        addForeignKeyConstraint(baseColumnNames: "DISPOSITION_ID", baseTableName: "DISPO_RULES_TOPIC_CATEGORY", constraintName: "FKj5pmuktmewe6h1pbfljto0p4u", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION_RULES")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-662") {
        addForeignKeyConstraint(baseColumnNames: "default_quant_disposition_id", baseTableName: "GROUPS", constraintName: "FKjco7ijx982a1bsoh3xb115sjf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-663") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "EVDAS_ALERT", constraintName: "FKjdbvyyl4pc5gyonimroq4q9vp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_CONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-664") {
        addForeignKeyConstraint(baseColumnNames: "target_state_id", baseTableName: "WORK_FLOW_RULES", constraintName: "FKjf5iat84x47a3wdllmpj3qa8r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-665") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ARCHIVED_SINGLE_CASE_ALERT", constraintName: "FKjfclvqbnyup3jp6ao1tawmmqe", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-666") {
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_ACA_ID", baseTableName: "ARCHIVED_ACA_ACTIONS", constraintName: "FKjl7ycps7whx6d55nn8k2sbgdi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_AGG_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-667") {
        addForeignKeyConstraint(baseColumnNames: "RPT_USER_ID", baseTableName: "SHARED_WITH", constraintName: "FKjpnhmuynt25rwtymb8c6qbcu2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-668") {
        addForeignKeyConstraint(baseColumnNames: "SIG_STATUS_HISTORY_ID", baseTableName: "SIGNAL_SIG_STATUS_HISTORY", constraintName: "FKjpwbsduge17rl18csfbwj2rog", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STATUS_HISTORY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-669") {
        addForeignKeyConstraint(baseColumnNames: "default_signal_disposition_id", baseTableName: "GROUPS", constraintName: "FKjq3undeff3wy615xd3cjxb13l", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-670") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FKjqg0j3akc948s35kbuqglqjqi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-671") {
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_LIT_ALERT_ID", baseTableName: "ARCHIVED_LIT_ALERT_ACTIONS", constraintName: "FKjuw50f35plt46bw5w8fv5yguy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_LITERATURE_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-672") {
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_USERID", baseTableName: "SHARE_WITH_USER_EVDAS_CONFIG", constraintName: "FKjwr7h59vtbhc7yycaiixb3d30", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-673") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_ALERT_ID", baseTableName: "SINGLE_GLOBAL_TAG_MAPPING", constraintName: "FKk07u3gx8x9cfv56t566onjcfs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-674") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "EVDAS_TOPIC_CONCEPTS", constraintName: "FKk2v2qa969crmd21cb8ey8eykl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-675") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "USER_GROUPS", constraintName: "FKk93ggugty4vt6eff2xct44qvs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-676") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FKkdxec3jbhkxo588a4kpg4h9l4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-677") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "ARCHIVED_EVDAS_ALERT", constraintName: "FKkkp4tovjgrhx31e6ejnly4tvu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_CONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-678") {
        addForeignKeyConstraint(baseColumnNames: "current_assigned_to_id", baseTableName: "LITERATURE_HISTORY", constraintName: "FKkla5tymin5rsewsqnvh6p2o4n", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-679") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ARCHIVED_EVDAS_ALERT", constraintName: "FKkokn9baeqvmps0m1bmcc9wi7x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-680") {
        addForeignKeyConstraint(baseColumnNames: "PVS_ALERT_TAG_ID", baseTableName: "ARCHIVED_LIT_CASE_ALERT_TAGS", constraintName: "FKkqdk7pluq7eeq58ca93llsq9x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "pvs_alert_tag")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-681") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "EX_RCONFIG", constraintName: "FKktk446ckoiyq9ccb0k4k1fa0d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-682") {
        addForeignKeyConstraint(baseColumnNames: "TEMPLT_QUERY_ID", baseTableName: "TEMPLT_QRS_QUERY_VALUES", constraintName: "FKkwhfmdea1iw8ad05voiu1c88x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TEMPLT_QUERY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-683") {
        addForeignKeyConstraint(baseColumnNames: "initial_disposition_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FKkwj9luxaslft1a450w0blw0xv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-684") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ARCHIVED_LIT_ALERTS", constraintName: "FKl11led0pu7l1g5k3ux1h0pwq7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-685") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "ARCHIVED_AGG_CASE_ALERT_TAGS", constraintName: "FKl48xrhxc36pgh6e24mg0wy87k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_AGG_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-686") {
        addForeignKeyConstraint(baseColumnNames: "strategy_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FKl5ki9ms4pq7oyr4wrne2wejf1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STRATEGY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-687") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "evdas_history", constraintName: "FKl751la1u7svv60tmydb5q6rfo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-688") {
        addForeignKeyConstraint(baseColumnNames: "initial_disposition_id", baseTableName: "ARCHIVED_AGG_ALERT", constraintName: "FKl8pwosqr9w21qo798onrflf41", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-689") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "ARCHIVED_AGG_ALERT", constraintName: "FKleyd7fd58inli39s2hn65hlan", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-690") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "RULE_INFORMATION", constraintName: "FKlfungwscmq9bwj56xlirfguy1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-691") {
        addForeignKeyConstraint(baseColumnNames: "date_range_information_id", baseTableName: "EX_LITERATURE_CONFIG", constraintName: "FKlhyif0bp06538q34cbg7o6ni", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_LITERATURE_DATE_RANGE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-692") {
        addForeignKeyConstraint(baseColumnNames: "LITERATURE_ALERT_ID", baseTableName: "LITERATURE_CASE_ALERT_TAGS", constraintName: "FKlialwhjla8foxw2pk52iolbt2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-693") {
        addForeignKeyConstraint(baseColumnNames: "validated_signal_actions_id", baseTableName: "validated_signal_actions", constraintName: "FKlqeb869hd13sihhd6a99xq2he", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-694") {
        addForeignKeyConstraint(baseColumnNames: "performed_by_id", baseTableName: "ACTIVITIES", constraintName: "FKls2t8mye4vopk1sjq1tgy9xoc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-695") {
        addForeignKeyConstraint(baseColumnNames: "initial_disposition_id", baseTableName: "ARCHIVED_EVDAS_ALERT", constraintName: "FKlsj8tckw1pfcf7pxfx0q4xqsk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-696") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "signal_history", constraintName: "FKludcb63lf92hniiym7xpdxhfa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-697") {
        addForeignKeyConstraint(baseColumnNames: "meeting_activities_id", baseTableName: "meeting_activities", constraintName: "FKlxb7shjcemeke6qig312nemnv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEETING")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-698") {
        addForeignKeyConstraint(baseColumnNames: "RPT_RESULT_DATA_ID", baseTableName: "RPT_RESULT", constraintName: "FKly7i8dpff5f3c218d99g8tcyj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_RESULT_DATA")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-699") {
        addForeignKeyConstraint(baseColumnNames: "meeting_guest_attendee_id", baseTableName: "meeting_guest_attendee", constraintName: "FKm139ii2baukit0klasjx4oglf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEETING")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-700") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "AGG_ALERT", constraintName: "FKm2082jiexg6r0a70a2r34utlj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-701") {
        addForeignKeyConstraint(baseColumnNames: "RPT_FIELD_GRPNAME", baseTableName: "RPT_FIELD", constraintName: "FKmahmpsyseeqtjochaf36fxyap", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "NAME", referencedTableName: "RPT_FIELD_GROUP")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-702") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FKmci0hn62phky4qlig0uy8walr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-703") {
        addForeignKeyConstraint(baseColumnNames: "type_id", baseTableName: "ACTIVITIES", constraintName: "FKmeoolraf0sivqb157mg1xxxhn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITY_TYPE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-704") {
        addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "RPT_ERROR", constraintName: "FKmkyr5aidjfp14wd8ytgv48xdn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_QUERY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-705") {
        addForeignKeyConstraint(baseColumnNames: "COLUMN_MEASURE_ID", baseTableName: "DTAB_TEMPLTS_COL_MEAS", constraintName: "FKmop226wsllt5fkw8ldhbio60u", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DTAB_COLUMN_MEASURE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-706") {
        addForeignKeyConstraint(baseColumnNames: "MEASURE_ID", baseTableName: "DTAB_COL_MEAS_MEASURES", constraintName: "FKmowrqjxvrwjdeg7wh33rdsi7e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DTAB_MEASURE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-707") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "EX_LITERATURE_CONFIG", constraintName: "FKmq50wqqf7s1y3ec4mdllb6ueu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-708") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKmv3t9bke7ovuq4xhntvupq5b4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-709") {
        addForeignKeyConstraint(baseColumnNames: "exec_configuration_id", baseTableName: "AGG_ON_DEMAND_ALERT", constraintName: "FKmvf233chim9ujyk92n0fjedw4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-710") {
        addForeignKeyConstraint(baseColumnNames: "ALERT_TAG_ID", baseTableName: "AGG_ALERT_TAGS", constraintName: "FKmw7ferd2w1sq8d034q2a2dare", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_TAG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-711") {
        addForeignKeyConstraint(baseColumnNames: "COMMENT_ID", baseTableName: "ADHOC_ALERT_COMMENTS", constraintName: "FKmx3gs9bdqe5we8t8dyfwlnu57", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_COMMENT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-712") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FKn52svyiofwkw5lpse0bi7tiup", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-713") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FKn6ofry9re33lb76h4npt3cjr0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-714") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "PRODUCT_DICTIONARY_CACHE", constraintName: "FKn6vycx5pad3o4yv5lqgyg9b0k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-715") {
        addForeignKeyConstraint(baseColumnNames: "date_range_information_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKncksxdxhk9k1wg170uk6hxo01", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_DATE_RANGE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-716") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_ADHOC_ALERTS", constraintName: "FKneg18gip2c5vrkf5o5d6hjvp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-717") {
        addForeignKeyConstraint(baseColumnNames: "ADHOC_ALERT_ID", baseTableName: "TOPIC_ADHOC_ALERTS", constraintName: "FKnf31xnc5tocj5toxf64f4n5bo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-718") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "AGG_SIGNAL_CONCEPTS", constraintName: "FKnfgumbuu5ryminkxap7nlgc5p", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-719") {
        addForeignKeyConstraint(baseColumnNames: "PREFERENCE_ID", baseTableName: "PVUSER", constraintName: "FKnjdqpgxtqa9kv92hc0h5a08nh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PREFERENCE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-720") {
        addForeignKeyConstraint(baseColumnNames: "PVS_ALERT_TAG_ID", baseTableName: "ARCHIVED_AGG_CASE_ALERT_TAGS", constraintName: "FKnksubyeucaerikj09ay4ghone", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "pvs_alert_tag")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-721") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "AGG_ALERT", constraintName: "FKnpwnuktj5h2nepfrniywuihe2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-722") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "meeting_pvuser", constraintName: "FKnrjk7qtd13c4vbk8cf2fng3c2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-723") {
        addForeignKeyConstraint(baseColumnNames: "performed_by_id", baseTableName: "LITERATURE_ACTIVITY", constraintName: "FKnu30tvrow0gcfq8mug780icdi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-724") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "RCONFIG", constraintName: "FKnwf5n98xlybm9cmlufdikkb31", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-725") {
        addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_VALUE_ID", baseTableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES", constraintName: "FKnwuo5xfk46w6axjqsd3so9ta4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_VALUE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-726") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "RCONFIG", constraintName: "FKnxr5jgx0rmqc0qwpvfb1hxedb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-727") {
        addForeignKeyConstraint(baseColumnNames: "GLOBAL_ARTICLE_ID", baseTableName: "LITERAURE_GLOBAL_TAGS", constraintName: "FKo0pbdms4a7hhafpsdgorey0ik", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalArticleId", referencedTableName: "global_article")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-728") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "EVDAS_ALERT", constraintName: "FKo3yoly6j5mslepff4epm8i3un", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-729") {
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "EVDAS_ALERT_ACTIONS", constraintName: "FKo4mw5tnngdjph3v8iouiqmkt4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-730") {
        addForeignKeyConstraint(baseColumnNames: "executed_configuration_id", baseTableName: "LITERATURE_ACTIVITY", constraintName: "FKoamgni17duaoqjg3twyy362ue", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_LITERATURE_CONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-731") {
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_GROUPID", baseTableName: "SHARE_WITH_GROUP_EVDAS_CONFIG", constraintName: "FKob80eqdx25087jypm0j5rehhh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-732") {
        addForeignKeyConstraint(baseColumnNames: "RCONFIG_ID", baseTableName: "RPT_ERROR", constraintName: "FKodukke9wren1g3gjo55qi9tgp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-733") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "ARCHIVED_LITERATURE_ALERT", constraintName: "FKoftb386ohtlewbs7o84428801", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-734") {
        addForeignKeyConstraint(baseColumnNames: "user_department_id", baseTableName: "pvuser_user_department", constraintName: "FKohxfvcmtidnanlyvlj3qq9hyt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user_department")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-735") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "SINGLE_TOPIC_CONCEPTS", constraintName: "FKop4ektpb6ulll4ubcyvxd2352", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-736") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_ALERT_ID", baseTableName: "SINGLE_ALERT_TAGS", constraintName: "FKopm5twbskg380tovcrs4r2gxs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-737") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "product_event_history", constraintName: "FKoqw80p3b38v6stbo0ic2u08vf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-738") {
        addForeignKeyConstraint(baseColumnNames: "exec_configuration_id", baseTableName: "AGG_ALERT", constraintName: "FKorl7uf55p01t8h6cb6jvuc4r0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-739") {
        addForeignKeyConstraint(baseColumnNames: "alert_id", baseTableName: "ALERT_DOCUMENT", constraintName: "FKou8nl9rcuc35kn5yuucm1jfso", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-740") {
        addForeignKeyConstraint(baseColumnNames: "ALERT_TAG_ID", baseTableName: "LITERATURE_ALERT_TAGS", constraintName: "FKoxf0kmlxo6jbssodqw9a20l72", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_TAG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-741") {
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_LIT_ALERT_ID", baseTableName: "VALIDATED_ARCHIVED_LIT_ALERTS", constraintName: "FKp4w6jrqpfd4ypvab6bqyatk9g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_LITERATURE_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-742") {
        addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "PVUSERS_ROLES", constraintName: "FKp6qbm114tnjnrpkfb5lpvp82k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ROLE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-743") {
        addForeignKeyConstraint(baseColumnNames: "lit_search_config_id", baseTableName: "ARCHIVED_LITERATURE_ALERT", constraintName: "FKp9mfpvd6hok6q5iu14tsanlk5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_CONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-744") {
        addForeignKeyConstraint(baseColumnNames: "EVDAS_ALERT_ID", baseTableName: "EVDAS_TOPIC_CONCEPTS", constraintName: "FKpaayw5smfn63cqc92ncfgtcwk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-745") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "EVDAS_ALERT", constraintName: "FKpb61fes9spi2lu1n9k4oq9m05", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-746") {
        addForeignKeyConstraint(baseColumnNames: "alert_id", baseTableName: "ACTIVITIES", constraintName: "FKpb7op1gsmnueani37t07swp18", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-747") {
        addForeignKeyConstraint(baseColumnNames: "validated_signal_id", baseTableName: "signal_history", constraintName: "FKpc3meb0lkklinhdq8yvhcc094", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-748") {
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_USERID", baseTableName: "SHARE_WITH_USER_CONFIG", constraintName: "FKpcpkomkegxwykoq2am8py9ayt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-749") {
        addForeignKeyConstraint(baseColumnNames: "workflow_group_id", baseTableName: "EX_LITERATURE_CONFIG", constraintName: "FKpigynbh2vwpt1xx49uypi3ddo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-750") {
        addForeignKeyConstraint(baseColumnNames: "PROD_GRP_ID", baseTableName: "EX_RCONFIGS_PROD_GRP", constraintName: "FKpiydv7gsh3jat1r6upba3jfsl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "product_group")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-751") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "AGG_ALERT", constraintName: "FKpj612eglsaa2cqi8ds4c3hs3c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-752") {
        addForeignKeyConstraint(baseColumnNames: "SQL_VALUE_ID", baseTableName: "SQL_TEMPLTS_SQL_VALUES", constraintName: "FKpjt02j3shpuy0tenek9p1ol40", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SQL_VALUE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-753") {
        addForeignKeyConstraint(baseColumnNames: "validated_signal_id", baseTableName: "MEETING", constraintName: "FKpkleruks8op5rxe14p0u28dun", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-754") {
        addForeignKeyConstraint(baseColumnNames: "DISPOSITION_ID", baseTableName: "DISPO_RULES_WORKFLOW_GROUP", constraintName: "FKpr2qa0uljoummmnormrftubha", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION_RULES")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-755") {
        addForeignKeyConstraint(baseColumnNames: "CONFIG_ID", baseTableName: "VALIDATED_SIGNAL_RCONFIG", constraintName: "FKptphnx7oype1403pwd2wk9963", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-756") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "EX_STATUS", constraintName: "FKptxxpv688i41soevklto2001n", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-757") {
        addForeignKeyConstraint(baseColumnNames: "EVDAS_CONFIG_ID", baseTableName: "EVDAS_CONFIGURATION_GROUPS", constraintName: "FKpw1bhalfblwu0e31df1oib0cr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_CONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-758") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ALERT_COMMENTS", constraintName: "FKq24b4km13myw6pxki435bulke", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-759") {
        addForeignKeyConstraint(baseColumnNames: "exec_config_id", baseTableName: "ARCHIVED_SINGLE_CASE_ALERT", constraintName: "FKq2nl1q0ibqqlvvsykkw40vwir", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-760") {
        addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "validated_signal_actions", constraintName: "FKq4785d5esdy86x6prso7o75el", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-761") {
        addForeignKeyConstraint(baseColumnNames: "LITERATURE_ALERT_ID", baseTableName: "LITERATURE_ALERT_TAGS", constraintName: "FKq8lv3dxpesh2l167udqs606vn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-762") {
        addForeignKeyConstraint(baseColumnNames: "ACTIVITY_ID", baseTableName: "ex_rconfig_activities", constraintName: "FKqaijpt3i6wpk4o7f4j9iofrqj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-763") {
        addForeignKeyConstraint(baseColumnNames: "global_identity_id", baseTableName: "ARCHIVED_SINGLE_CASE_ALERT", constraintName: "FKqaxchl4ofg9634tb244aw63e5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalCaseId", referencedTableName: "global_case")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-764") {
        addForeignKeyConstraint(baseColumnNames: "DISPOSITION_ID", baseTableName: "GRP_ALERT_DISP", constraintName: "FKqeqpmqe77w39we8m3l33b48kt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-765") {
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "ADHOC_ALERT_ACTIONS", constraintName: "FKqf474sj5dkn95xc433napf95b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-766") {
        addForeignKeyConstraint(baseColumnNames: "current_disposition_id", baseTableName: "CASE_HISTORY", constraintName: "FKqh491q7pp8sjet2ep7jy7ilda", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-767") {
        addForeignKeyConstraint(baseColumnNames: "meeting_attendees_id", baseTableName: "meeting_pvuser", constraintName: "FKqiltr5xajp4m5fu0je6set5g3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEETING")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-768") {
        addForeignKeyConstraint(baseColumnNames: "ADHOC_ALERT_ID", baseTableName: "ADHOC_ALERT_COMMENTS", constraintName: "FKqj7ub0xptnbuhouvowxamuj26", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-769") {
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_SCA_ID", baseTableName: "VALIDATED_ARCHIVED_SCA", constraintName: "FKqk25jtv031p4xd9rjn5p24cse", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-770") {
        addForeignKeyConstraint(baseColumnNames: "log_id", baseTableName: "IMPORT_DETAIL", constraintName: "FKqkcg7dewuxqjklxa4xuo3yb6w", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "IMPORT_LOG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-771") {
        addForeignKeyConstraint(baseColumnNames: "exec_configuration_id", baseTableName: "ARCHIVED_EVDAS_ALERT", constraintName: "FKqop2dkci70s0amrbl03k9tqpv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_EVDAS_CONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-772") {
        addForeignKeyConstraint(baseColumnNames: "current_priority_id", baseTableName: "CASE_HISTORY", constraintName: "FKqqc5icyo2ky7wkegjktf5inwd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-773") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FKqry992h07weddj9bprvrhjb3j", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-774") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "LITERATURE_ACTIVITY", constraintName: "FKqtymlypfnndkl92xpmlyn6wgi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-775") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "AGG_TOPIC_CONCEPTS", constraintName: "FKr2ep7rrgudpgn2aeip674gffo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-776") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "LITERATURE_ACTIVITY", constraintName: "FKr2gmw2pc2lgkyxy708wuv8u5b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-777") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "ALERTS", constraintName: "FKr339y18wo70qkphso8k28p4n5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-778") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "WkFL_RUL_DISPOSITIONS", constraintName: "FKr4dvi6dx2akkxj6sg1edfu4y8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-779") {
        addForeignKeyConstraint(baseColumnNames: "product_dictionary_cache_id", baseTableName: "ALLOWED_DICTIONARY_CACHE", constraintName: "FKr4nitfmbmp9dht2pk246d68e9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRODUCT_DICTIONARY_CACHE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-780") {
        addForeignKeyConstraint(baseColumnNames: "exec_configuration_id", baseTableName: "EVDAS_ON_DEMAND_ALERT", constraintName: "FKr4pfuk8ly0nuynw87d5jg8cct", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_EVDAS_CONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-781") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "ARCHIVED_SINGLE_CASE_ALERT", constraintName: "FKr9n8pkn4fmv9fp6fo3exb8pbd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-783") {
        addForeignKeyConstraint(baseColumnNames: "USER_ID", baseTableName: "spotfire_notification_pvuser", constraintName: "FKrdeku7oiw606oc27n077l4agf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-784") {
        addForeignKeyConstraint(baseColumnNames: "SIGNAL_OUTCOME_ID", baseTableName: "VALIDATED_SIGNAL_OUTCOMES", constraintName: "FKre3rort06h65skghf2j23ad7p", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_OUTCOME")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-785") {
        addForeignKeyConstraint(baseColumnNames: "COLUMNS_RF_INFO_LIST_ID", baseTableName: "CLL_TEMPLT", constraintName: "FKrepui5ibyqyi0ylkhdmrggn9x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-786") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "ARCHIVED_EVDAS_ALERT", constraintName: "FKriardutnvjukg5o5ae4i1t23o", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-787") {
        addForeignKeyConstraint(baseColumnNames: "safety_group_id", baseTableName: "pvuser_safety_groups", constraintName: "FKriwpg5obw3hvucl89nm7cyfg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "safety_group")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-788") {
        addForeignKeyConstraint(baseColumnNames: "workflow_group_id", baseTableName: "LITERATURE_CONFIG", constraintName: "FKrnu8n3wqjbeb1qdcgs36odomn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-789") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ARCHIVED_LITERATURE_ALERT", constraintName: "FKrnwq5b3qaj65u7w57lm3o7gy6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-790") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "ALERT_TAG", constraintName: "FKrrbo69cgkt2oof0qend38karl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-791") {
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "ARCHIVED_EVDAS_ALERT_ACTIONS", constraintName: "FKruapp3pulwttybjnqt0ih5cej", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-792") {
        addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "RCONFIG", constraintName: "FKrwhfg2lf7qokjhuat482craww", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-793") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "EVDAS_ALERT", constraintName: "FKrx1ksfdkal8plsauewx9wcoje", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-794") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_ALERT_ID", baseTableName: "ARCHIVED_SCA_TAGS", constraintName: "FKs4oc9hkhlcen6gnfwlmmp7gwb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-795") {
        addForeignKeyConstraint(baseColumnNames: "current_assigned_to_group_id", baseTableName: "CASE_HISTORY", constraintName: "FKs60gq6t7tjj3hsunqhur4w35a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-796") {
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "ARCHIVED_SCA_ACTIONS", constraintName: "FKs63kxguooseokrhs0j33gfwq6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-797") {
        addForeignKeyConstraint(baseColumnNames: "ha_signal_status_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FKs64fr0mhkeoburd4i5hvvtmxy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-798") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "PVUSERS_ROLES", constraintName: "FKs6fht51c12chh1glukanj1vuu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-799") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "EX_RCONFIG", constraintName: "FKscrg3o7ey1xr3ac0oldqvq1cg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-800") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "ALERTS", constraintName: "FKsg3649m44b1j9j7kg3saeacwx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-801") {
        addForeignKeyConstraint(baseColumnNames: "TEMPLATE_ID", baseTableName: "RCONFIG", constraintName: "FKsh216m1mb3g41hm0h6w12wh12", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_TEMPLT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-802") {
        addForeignKeyConstraint(baseColumnNames: "default_lit_disposition_id", baseTableName: "GROUPS", constraintName: "FKsh4dl71unjyng5oae37810ugs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-803") {
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "SINGLE_ALERT_ACTIONS", constraintName: "FKshd0nte1n4d4v5cp5dav55ic0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-804") {
        addForeignKeyConstraint(baseColumnNames: "ex_lit_search_config_id", baseTableName: "LITERATURE_ALERT", constraintName: "FKsm5fds0flxc0po7eqgc5gqgpj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_LITERATURE_CONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-805") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "USER_GROUP_MAPPING", constraintName: "FKsp010pdhbqal79g9q1y3kl5v3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-806") {
        addForeignKeyConstraint(baseColumnNames: "SQL_VALUE_ID", baseTableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES", constraintName: "FKsqppqusike0nnjet4x4elrkom", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SQL_VALUE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-807") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "AGG_TOPIC_CONCEPTS", constraintName: "FKsy5o19eqrc5385sfoidonp43l", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-808") {
        addForeignKeyConstraint(baseColumnNames: "GROUPING_RF_INFO_LIST_ID", baseTableName: "CLL_TEMPLT", constraintName: "FKsysb0klybb1kk1o3f5auerjx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-809") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "ARCHIVED_EVDAS_ALERT", constraintName: "FKt093cjln518o54qj1rv0u5r4r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-810") {
        addForeignKeyConstraint(baseColumnNames: "TABLE_NAME_ATM_ID", baseTableName: "SOURCE_COLUMN_MASTER", constraintName: "FKt2b7h0ovgst4owagdj3bhj57t", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "SOURCE_TABLE_MASTER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-811") {
        addForeignKeyConstraint(baseColumnNames: "validated_signal_id", baseTableName: "validated_signal_action_taken", constraintName: "FKt5h2kt8e2kf76xpxs11tw98ek", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-812") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ARCHIVED_ACA", constraintName: "FKt6fm3gafi66mftqot7fu2qs9f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-813") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ARCHIVED_AGG_ALERT", constraintName: "FKt6os25wyok9pdgy2suadx9vvf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-814") {
        addForeignKeyConstraint(baseColumnNames: "LINKED_SIGNAL_ID", baseTableName: "SIGNAL_LINKED_SIGNALS", constraintName: "FKt7saax9k5yl609iwcsu4x1gr7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-815") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "SINGLE_ON_DEMAND_ALERT", constraintName: "FKt89eni1sgfgosaee42phw0j0a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-816") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "ACTIONS", constraintName: "FKt8jxq56nworpeaop4oarjkmpy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-817") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP", baseTableName: "ALERTS", constraintName: "FKtawq2r3by3tlx2bx8ohm6ssdb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-818") {
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_ACA_ID", baseTableName: "VALIDATED_ARCHIVED_ACA", constraintName: "FKtdy0p6crwvybnpn4fr5m4r547", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_AGG_ALERT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-819") {
        addForeignKeyConstraint(baseColumnNames: "ADHOC_ALERT_ID", baseTableName: "ADHOC_ALERT_ACTION_TAKEN", constraintName: "FKte7ljx7lhewxd219su74acrd7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-820") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKtirjstq25k3s016xchl6imgv8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-821") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FKtlr3lhis3k57s65e2wrfm6tpx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-822") {
        addForeignKeyConstraint(baseColumnNames: "user_user_departments_id", baseTableName: "pvuser_user_department", constraintName: "FKtmsfftkx8df5gdlnxgjyub76b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-823") {
        addForeignKeyConstraint(baseColumnNames: "exec_config_id", baseTableName: "SINGLE_ON_DEMAND_ALERT", constraintName: "FKtphrpv7ktdcanh8loeb1e5qcv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-824") {
        addForeignKeyConstraint(baseColumnNames: "ROWS_RF_INFO_LIST_ID", baseTableName: "DTAB_TEMPLT", constraintName: "FKxuj1f2muxnhq2fgcqbxuycjb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-825") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "product_event_history", constraintName: "FKy156nyus7pkpemc1lk9wuy73", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-826") {
        dropTable(tableName: "HT_CLL_TEMPLT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-827") {
        dropTable(tableName: "HT_DTAB_TEMPLT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-828") {
        dropTable(tableName: "HT_EX_CLL_TEMPLT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-829") {
        dropTable(tableName: "HT_EX_CUSTOM_SQL_TEMPLT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-830") {
        dropTable(tableName: "HT_EX_DTAB_TEMPLT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-831") {
        dropTable(tableName: "HT_EX_NCASE_SQL_TEMPLT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-832") {
        dropTable(tableName: "HT_EX_QUERY_EXP")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-833") {
        dropTable(tableName: "HT_EX_QUERY_VALUE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-834") {
        dropTable(tableName: "HT_EX_SQL_VALUE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-835") {
        dropTable(tableName: "HT_EX_TEMPLT_VALUE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-836") {
        dropTable(tableName: "HT_NONCASE_SQL_TEMPLT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-837") {
        dropTable(tableName: "HT_PARAM")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-838") {
        dropTable(tableName: "HT_QUERY_EXP_VALUE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-839") {
        dropTable(tableName: "HT_QUERY_VALUE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-840") {
        dropTable(tableName: "HT_RPT_TEMPLT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-841") {
        dropTable(tableName: "HT_SQL_TEMPLT")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-842") {
        dropTable(tableName: "HT_SQL_TEMPLT_VALUE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-843") {
        dropTable(tableName: "HT_SQL_VALUE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-844") {
        dropTable(tableName: "HT_TEMPLT_VALUE")
    }

    changeSet(author: "amit (generated)", id: "1594893043821-845") {
        dropTable(tableName: "HT_VALUE")
    }

    changeSet(author: "ankit (generated)", id: "1595253951672-1") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'attachment', columnName: 'NAME')
        }
        sql("alter table attachment modify NAME VARCHAR2(4000 CHAR);")
    }

    changeSet(author: "ankit (generated)", id: "1595253951672-2") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ATTACHMENT_DESCRIPTION', columnName: 'DESCRIPTION')
        }
        sql("alter table ATTACHMENT_DESCRIPTION modify DESCRIPTION VARCHAR2(4000 CHAR);")
    }
}
