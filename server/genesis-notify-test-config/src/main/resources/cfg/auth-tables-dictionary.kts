tables {
    table(name = "PROFILE", id = 1002, audit = details(1053, "PR")) {
        NAME
        DESCRIPTION
        STATUS
        primaryKey {
            NAME
        }
    }

    table(name = "PROFILE_USER", id = 1003, audit = details(1051, "PA")) {
        PROFILE_NAME
        USER_NAME
        primaryKey {
            PROFILE_NAME
            USER_NAME
        }
        indices {
            nonUnique {
                USER_NAME
                PROFILE_NAME
            }
        }
    }

    val permissionsField = SysDef["ADMIN_PERMISSION_ENTITY_FIELD"]
    val permissionsTable = SysDef["ADMIN_PERMISSION_ENTITY_TABLE"]

    if (permissionsTable != null && permissionsField != null) {
        val multiEntity = SysDef["ADMIN_PERMISSION_MULTI_ENTITY_FIELD"]

        table(name = "USER_${permissionsTable}_MAP", id = 1012) {
            USER_NAME
            Fields[permissionsField]
            if (multiEntity != null) Fields[multiEntity]
            primaryKey {
                USER_NAME
                if (multiEntity != null) Fields[permissionsField]
            }
            indices {
                nonUnique(name = "USER_${permissionsTable}_MAP_BY_${permissionsField}") {
                    Fields[permissionsField]
                    if (multiEntity != null) Fields[multiEntity]
                }
            }
        }
    }

    // Only used for unit tests
    table("COUNTERPARTY", 10190) {
        COUNTERPARTY_ID
        primaryKey {
            COUNTERPARTY_ID
        }
    }
}
