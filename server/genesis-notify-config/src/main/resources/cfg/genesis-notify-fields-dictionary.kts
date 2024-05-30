/**
 * System              : Genesis Business Library
 * Sub-System          : multi-pro-code-test Configuration
 * Version             : 1.0
 * Copyright           : (c) Genesis
 * Date                : 2022-03-18
 * Function : Provide fields config for multi-pro-code-test.
 *
 * Modification History
 */

fields {
    field(name = "FILE_STORAGE_ID", type = STRING)
    field(name = "NOTIFY_ID", type = STRING)
    field(name = "SENDER", type = STRING)
    field(name = "TOPIC", type = STRING, nullable = true)
    field(name = "TOPIC_MATCH", type = STRING, maxSize = 255)
    field(name = "INCOMING_TOPIC", type = STRING)
    field(name = "EXPIRY", type = DATETIME)
    field(name = "HEADER", type = STRING, maxSize = dbMaxSize)
    field(name = "BODY", type = STRING, maxSize = dbMaxSize)
    field(name = "NOTIFY_COMPRESSION_TYPE", type = STRING)
    field(name = "NOTIFY_ROUTE_ID", type = STRING)
    field(
        name = "NOTIFY_SEVERITY",
        type = ENUM("Information", "Warning", "Serious", "Critical", default = "Information")
    )
    field(name = "GATEWAY_ID", type = STRING, maxSize = 255)
    field(name = "ALERT_ID", type = STRING)
    field(name = "ALERT_STATUS", type = ENUM("NEW", "DISMISSED", "EXPIRED", default = "NEW"))
    field(name = "MESSAGE", type = STRING, maxSize = dbMaxSize)
    field(name = "DYNAMIC_NOTIFY_RULE_ID", type = STRING)
    field(name = "DYNAMIC_RULE_ID", type = STRING)
    field(name = "DOCUMENT_ID", type = STRING)
    field(name = "TEMPLATE_REF", type = STRING)
    field(name = "TABLE_NAME", type = STRING)
    field(name = "TABLE_ENTITY_ID", type = STRING)
    field(name = "PERMISSIONING_ENTITY_ID", type = STRING)
    field(name = "EMAIL_TO", type = STRING, maxSize = dbMaxSize)
    field(name = "EMAIL_CC", type = STRING, maxSize = dbMaxSize)
    field(name = "EMAIL_BCC", type = STRING, maxSize = dbMaxSize)
    field(name = "URL", type = STRING, maxSize = dbMaxSize)
    field(name = "TTL", type = LONG)
    field(name = "TTL_TIME_UNIT", type = ENUM("NONE", "SECONDS", "MINUTES", "HOURS", "DAYS", default = "NONE"))
    field(name = "RIGHT_CODE", type = STRING)
    field(name = "AUTH_CACHE_NAME", type = STRING)
    field(name = "EXCLUDE_SENDER", type = BOOLEAN, nullable = false)
    field(name = "ROUTING_TYPE", type = ENUM("TOPIC", "DIRECT", "BASIC", default = "TOPIC"))
    field(name = "ROUTING_DATA", type = STRING, maxSize = dbMaxSize, nullable = true)
    val permissionsField = SysDef["ADMIN_PERMISSION_ENTITY_FIELD"]
    if (permissionsField == null) {
        field(
            name = "ENTITY_ID_TYPE",
            type = ENUM("USER_NAME", "PROFILE_NAME", "ALL", "SELF", default = "USER_NAME")
        )
    } else {
        field(
            name = "ENTITY_ID_TYPE",
            type = ENUM("USER_NAME", "PROFILE_NAME", "ALL", "SELF", permissionsField, default = "USER_NAME")
        )
    }
}

