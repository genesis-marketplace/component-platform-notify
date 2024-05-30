import global.genesis.criteria.model.RuleExpression
import global.genesis.criteria.model.ResultExpression
import global.genesis.criteria.util.EvaluatorExpressionParser
import global.genesis.criteria.util.InvalidExpression
import global.genesis.criteria.util.InvalidResultExpression
import global.genesis.criteria.util.ValidComplexExpression
import global.genesis.criteria.util.ValidResultExpression
import global.genesis.criteria.util.ValidSimpleExpression
import global.genesis.db.updatequeue.UpdateType
import global.genesis.notify.message.request.NotificationRuleReply
import global.genesis.notify.message.request.NotificationRuleTemplateReply
import global.genesis.notify.message.request.NotifyRouteTopicsReply
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

fun getRuleExpressions(dynamicRule: DynamicRule): Pair<RuleExpression?, String?> {
    val ruleExpressionString = dynamicRule.ruleExpression
    val ruleExpressionResponse = EvaluatorExpressionParser.readRuleExpression(ruleExpressionString ?: "")
    return when (ruleExpressionResponse) {
        is InvalidExpression -> null to null
        is ValidComplexExpression -> null to ruleExpressionResponse.rawExpression
        is ValidSimpleExpression -> ruleExpressionResponse.ruleExpression to null
    }
}

fun resultExpression(dynamicRule: DynamicRule): ResultExpression? {
    val resultExpressionParseResponse =
        EvaluatorExpressionParser.readResultExpression(dynamicRule.resultExpression ?: "")
    val resultExpression = when (resultExpressionParseResponse) {
        is InvalidResultExpression -> null
        is ValidResultExpression -> resultExpressionParseResponse.resultExpression
    }
    return resultExpression
}

requestReplies {
    requestReply("NOTIFICATION_RULE_TEMPLATE", DYNAMIC_RULE_VIEW) {
        permissioning {
            permissionCodes = listOf("NotificationRuleTemplateView")
        }
        where { row, _ ->
            row.isTemplate && row.ruleType == "NOTIFY"
        }
    }

    requestReply<DynamicRule.ById, NotificationRuleTemplateReply>(name = "NOTIFICATION_RULE_TEMPLATE_DETAILS") {
        permissioning {
            permissionCodes = listOf("NotificationRuleTemplateView")
        }
        replySingle { request ->
            val dynamicRule = db.get(request)
            require(dynamicRule != null) { "Could not find dynamic rule record for id: $request" }
            val dynamicRuleParams = db.getRange(DynamicRuleTemplateParams.byDynamicRuleId(dynamicRule.id))
                .toList()
                .associateBy { it.paramName }

            val (ruleExpression, rawRuleExpression) = getRuleExpressions(dynamicRule)
            val resultExpression = resultExpression(dynamicRule)

            NotificationRuleTemplateReply(
                dynamicRuleId = dynamicRule.id,
                ruleName = dynamicRule.name,
                ruleDescription = dynamicRule.description,
                ruleTable = dynamicRule.ruleTable ?: "",
                ruleExecutionStrategy = dynamicRule.ruleExecutionStrategy,
                ruleExpression = ruleExpression,
                rawRuleExpression = rawRuleExpression,
                resultExpression = resultExpression,
                parameters = dynamicRuleParams,
                tableOperations = dynamicRule.tableOperation.split("|").map {
                    UpdateType.valueOf(it)
                }.toSet()
            )
        }
    }

    requestReply("NOTIFICATION_RULE", DYNAMIC_RULE_VIEW) {
        permissioning {
            auth {
                where { user ->
                    user == this.userName
                }
            }
            permissionCodes = listOf("NotificationRuleView")
        }
        where { row, _ ->
            !row.isTemplate && row.ruleType == "NOTIFY"
        }
    }

    requestReply<DynamicRule.ById, NotificationRuleReply>(name = "NOTIFICATION_RULE_DETAILS") {
        permissioning {
            permissionCodes = listOf("NotificationRuleView")
        }
        replySingle { request ->
            val dynamicRule = db.get(request)
            require(dynamicRule != null) { "Could not find dynamic rule record for id: $request" }

            val (ruleExpression, rawRuleExpression) = getRuleExpressions(dynamicRule)
            val resultExpression = resultExpression(dynamicRule)

            NotificationRuleReply(
                dynamicRuleId = dynamicRule.id,
                ruleName = dynamicRule.name,
                ruleDescription = dynamicRule.description,
                ruleTable = dynamicRule.ruleTable ?: "",
                ruleExecutionStrategy = dynamicRule.ruleExecutionStrategy,
                ruleExpression = ruleExpression,
                rawRuleExpression = rawRuleExpression,
                resultExpression = resultExpression,
                tableOperations = dynamicRule.tableOperation.split("|").map {
                    UpdateType.valueOf(it)
                }.toSet()
            )
        }
    }

    requestReply<NotifyRoute.ById, NotifyRouteTopicsReply>("NOTIFY_ROUTE_TOPICS") {
        permissioning {
            permissionCodes = listOf("NotificationRouteTopicsView")
        }
        replySingle {
            val list: List<String> = db.getBulk(NotifyRoute::class)
                .map { it.topicMatch }
                .distinct()
                .toList()

            NotifyRouteTopicsReply(
                list
            )
        }
    }

    requestReply("NOTIFY_ALERT", NOTIFY_ALERT_AUDIT) {
        permissioning {
            auth {
                where { user ->
                    user == this.userName
                }
            }
        }
    }
}
