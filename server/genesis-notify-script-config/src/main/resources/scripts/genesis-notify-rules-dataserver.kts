dataServer {
    query("ALL_DYNAMIC_NOTIFY_RULES", DYNAMIC_RULE) {
        permissioning {
            permissionCodes = listOf("NotificationRuleView")
        }
        where { dynamicRule ->
            !dynamicRule.isTemplate && dynamicRule.ruleType == "NOTIFY"
        }
    }
    query("ALL_NOTIFY_ALERT_RECORDS", NOTIFY_ALERT) {
        permissioning {
            auth {
                where { user ->
                    user == this.userName
                }
            }
        }
    }
    query("ALL_NOTIFY_RECORDS", NOTIFY) {
        permissioning {
            permissionCodes = listOf("NotificationView")
        }
    }
    query("ALL_SCREEN_ROUTES", SCREEN_ROUTE) {
        permissioning {
            permissionCodes = listOf("NotificationRouteView")
        }
    }
    query("ALL_EMAIL_USER_ROUTES", EMAIL_USER_ROUTE) {
        permissioning {
            permissionCodes = listOf("NotificationRouteView")
        }
    }
    query("ALL_EMAIL_DISTRIBUTION_ROUTES", EMAIL_DISTRIBUTION_ROUTE) {
        permissioning {
            permissionCodes = listOf("NotificationRouteView")
        }
    }
    query("ALL_LOG_ROUTES", LOG_ROUTE) {
        permissioning {
            permissionCodes = listOf("NotificationRouteView")
        }
    }
    query("ALL_MS_TEAMS_ROUTES", MS_TEAMS_ROUTE) {
        permissioning {
            permissionCodes = listOf("NotificationRouteView")
        }
    }
    query("ALL_RULE_TEMPLATES", DYNAMIC_RULE) {
        permissioning {
            permissionCodes = listOf("NotificationRuleTemplateView")
        }
        where { dynamicRule ->
            dynamicRule.isTemplate && dynamicRule.ruleType == "NOTIFY"
        }
    }
}
