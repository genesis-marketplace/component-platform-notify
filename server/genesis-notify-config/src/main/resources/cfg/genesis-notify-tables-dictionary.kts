/**
 * System              : Genesis Business Library
 * Sub-System          : multi-pro-code-test Configuration
 * Version             : 1.0
 * Copyright           : (c) Genesis
 * Date                : 2022-03-18
 * Function : Provide table definition config for multi-pro-code-test.
 *
 * Modification History
 */
tables {
    table(name = "NOTIFY", id = 21, audit = details(24, "NT")) {
        sequence(NOTIFY_ID, "NT")
        SENDER
        TOPIC
        HEADER not null
        BODY not null
        NOTIFY_COMPRESSION_TYPE
        NOTIFY_SEVERITY
        DOCUMENT_ID
        TEMPLATE_REF
        TABLE_NAME
        TABLE_ENTITY_ID
        PERMISSIONING_ENTITY_ID
        ROUTING_TYPE
        ROUTING_DATA
        primaryKey {
            NOTIFY_ID
        }
    }

    table(name = "NOTIFY_ATTACHMENT", id = 47, audit = details(48, "NA")) {
        NOTIFY_ID
        FILE_STORAGE_ID
        primaryKey {
            NOTIFY_ID
            FILE_STORAGE_ID
        }
        indices {
            nonUnique {
                FILE_STORAGE_ID
            }
        }
    }

    table(name = "NOTIFY_ROUTE", id = 22, audit = details(25, "NR")) {
        sequence(NOTIFY_ROUTE_ID, "NR")
        TOPIC_MATCH not null
        GATEWAY_ID not null
        primaryKey {
            NOTIFY_ROUTE_ID
        }
    }
    table(name = "SCREEN_NOTIFY_ROUTE_EXT", id = 32, audit = details(33, "SN")) {
        NOTIFY_ROUTE_ID
        ENTITY_ID
        ENTITY_ID_TYPE not null
        RIGHT_CODE
        AUTH_CACHE_NAME
        TTL
        TTL_TIME_UNIT
        EXCLUDE_SENDER
        primaryKey {
            NOTIFY_ROUTE_ID
        }
    }
    table(name = "EMAIL_DIST_NOTIFY_ROUTE_EXT", id = 36, audit = details(37, "ED")) {
        NOTIFY_ROUTE_ID
        EMAIL_TO
        EMAIL_CC
        EMAIL_BCC
        primaryKey {
            NOTIFY_ROUTE_ID
        }
    }
    table(name = "EMAIL_USER_NOTIFY_ROUTE_EXT", id = 38, audit = details(39, "UE")) {
        NOTIFY_ROUTE_ID
        ENTITY_ID
        ENTITY_ID_TYPE not null
        RIGHT_CODE
        AUTH_CACHE_NAME
        EXCLUDE_SENDER
        primaryKey {
            NOTIFY_ROUTE_ID
        }
    }
    table(name = "MS_TEAMS_NOTIFY_ROUTE_EXT", id = 40, audit = details(41, "MT")) {
        NOTIFY_ROUTE_ID
        URL not null
        primaryKey {
            NOTIFY_ROUTE_ID
        }
    }
    table(name = "LOG_NOTIFY_ROUTE_EXT", id = 42, audit = details(43, "LN")) {
        NOTIFY_ROUTE_ID
        primaryKey {
            NOTIFY_ROUTE_ID
        }
    }
    table(name = "NOTIFY_ALERT", id = 31, audit = details(44, "NA")) {
        sequence(ALERT_ID, "AL")
        ALERT_ID
        MESSAGE
        HEADER
        USER_NAME
        EXPIRY
        NOTIFY_SEVERITY
        CREATED_AT
        TABLE_ENTITY_ID
        TOPIC
        ALERT_STATUS
        primaryKey {
            ALERT_ID
        }
    }
}
