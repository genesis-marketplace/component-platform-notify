/**
 * System              : Genesis Business Library
 * Sub-System          : multi-pro-code-test Configuration
 * Version             : 1.0
 * Copyright           : (c) Genesis
 * Date                : 2022-03-18
 * Function : Provide view config for multi-pro-code-test.
 *
 * Modification History
 */
views {
    view("SCREEN_ROUTE", NOTIFY_ROUTE) {
        joins {
            joining(SCREEN_NOTIFY_ROUTE_EXT, joinType = JoinType.INNER, backwardsJoin = true) {
                on(NOTIFY_ROUTE.NOTIFY_ROUTE_ID to SCREEN_NOTIFY_ROUTE_EXT.NOTIFY_ROUTE_ID)
            }
        }
        fields {
            NOTIFY_ROUTE.except(listOf(NOTIFY_ROUTE.RECORD_ID, NOTIFY_ROUTE.TIMESTAMP))
            SCREEN_NOTIFY_ROUTE_EXT.except(
                listOf(
                    SCREEN_NOTIFY_ROUTE_EXT.NOTIFY_ROUTE_ID,
                    SCREEN_NOTIFY_ROUTE_EXT.RECORD_ID,
                    SCREEN_NOTIFY_ROUTE_EXT.TIMESTAMP
                )
            )
        }
    }
    view("EMAIL_DISTRIBUTION_ROUTE", NOTIFY_ROUTE) {
        joins {
            joining(EMAIL_DIST_NOTIFY_ROUTE_EXT, joinType = JoinType.INNER, backwardsJoin = true) {
                on(NOTIFY_ROUTE.NOTIFY_ROUTE_ID to EMAIL_DIST_NOTIFY_ROUTE_EXT.NOTIFY_ROUTE_ID)
            }
        }
        fields {
            NOTIFY_ROUTE.except(listOf(NOTIFY_ROUTE.RECORD_ID, NOTIFY_ROUTE.TIMESTAMP))
            EMAIL_DIST_NOTIFY_ROUTE_EXT.except(
                listOf(
                    EMAIL_DIST_NOTIFY_ROUTE_EXT.NOTIFY_ROUTE_ID,
                    EMAIL_DIST_NOTIFY_ROUTE_EXT.RECORD_ID,
                    EMAIL_DIST_NOTIFY_ROUTE_EXT.TIMESTAMP
                )
            )
        }
    }
    view("EMAIL_USER_ROUTE", NOTIFY_ROUTE) {
        joins {
            joining(EMAIL_USER_NOTIFY_ROUTE_EXT, joinType = JoinType.INNER, backwardsJoin = true) {
                on(NOTIFY_ROUTE.NOTIFY_ROUTE_ID to EMAIL_USER_NOTIFY_ROUTE_EXT.NOTIFY_ROUTE_ID)
            }
        }
        fields {
            NOTIFY_ROUTE.except(listOf(NOTIFY_ROUTE.RECORD_ID, NOTIFY_ROUTE.TIMESTAMP))
            EMAIL_USER_NOTIFY_ROUTE_EXT.except(
                listOf(
                    EMAIL_USER_NOTIFY_ROUTE_EXT.NOTIFY_ROUTE_ID,
                    EMAIL_USER_NOTIFY_ROUTE_EXT.RECORD_ID,
                    EMAIL_USER_NOTIFY_ROUTE_EXT.TIMESTAMP
                )
            )
        }
    }
    view("MS_TEAMS_ROUTE", NOTIFY_ROUTE) {
        joins {
            joining(MS_TEAMS_NOTIFY_ROUTE_EXT, joinType = JoinType.INNER, backwardsJoin = true) {
                on(NOTIFY_ROUTE.NOTIFY_ROUTE_ID to MS_TEAMS_NOTIFY_ROUTE_EXT.NOTIFY_ROUTE_ID)
            }
        }
        fields {
            NOTIFY_ROUTE.except(listOf(NOTIFY_ROUTE.RECORD_ID, NOTIFY_ROUTE.TIMESTAMP))
            MS_TEAMS_NOTIFY_ROUTE_EXT.except(
                listOf(
                    MS_TEAMS_NOTIFY_ROUTE_EXT.NOTIFY_ROUTE_ID,
                    MS_TEAMS_NOTIFY_ROUTE_EXT.RECORD_ID,
                    MS_TEAMS_NOTIFY_ROUTE_EXT.TIMESTAMP
                )
            )
        }
    }
    view("LOG_ROUTE", NOTIFY_ROUTE) {
        joins {
            joining(LOG_NOTIFY_ROUTE_EXT, joinType = JoinType.INNER, backwardsJoin = true) {
                on(NOTIFY_ROUTE.NOTIFY_ROUTE_ID to LOG_NOTIFY_ROUTE_EXT.NOTIFY_ROUTE_ID)
            }
        }
        fields {
            NOTIFY_ROUTE.except(listOf(NOTIFY_ROUTE.RECORD_ID, NOTIFY_ROUTE.TIMESTAMP))
            LOG_NOTIFY_ROUTE_EXT.except(
                listOf(
                    LOG_NOTIFY_ROUTE_EXT.NOTIFY_ROUTE_ID,
                    LOG_NOTIFY_ROUTE_EXT.RECORD_ID,
                    LOG_NOTIFY_ROUTE_EXT.TIMESTAMP
                )
            )
        }
    }
    view("DYNAMIC_RULE_VIEW", DYNAMIC_RULE) {
        fields {
            DYNAMIC_RULE.allFields()
            derivedField("DATETIME", LONG) {
                withInput(DYNAMIC_RULE.TIMESTAMP) { timestamp ->
                    timestamp!!.shr(22)
                }
            }
        }
    }
}
