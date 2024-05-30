package global.genesis.notify.message.event

import global.genesis.criteria.model.ResultExpression
import global.genesis.criteria.model.RuleExpression
import global.genesis.db.updatequeue.UpdateType
import global.genesis.gen.dao.DynamicRule
import global.genesis.gen.dao.DynamicRuleTemplateParams
import global.genesis.gen.dao.enums.ParamSourceType
import global.genesis.gen.dao.enums.ParamType

data class NotificationRuleSubscribe(
    val dynamicRuleId: String,
    val parameterDetails: Map<String, String>
)

data class NotificationRuleSubscribeContext(
    val dynamicRuleTemplate: DynamicRule,
    val templateParameters: List<DynamicRuleTemplateParams>
)

data class NotificationRuleTemplateInsert(
    val ruleName: String,
    val ruleDescription: String = "",
    val ruleTable: String,
    val ruleExpression: RuleExpression? = null,
    val rawRuleExpression: String? = null,
    val resultExpression: ResultExpression,
    val parameterDetails: Map<String, NotificationRuleTemplateParameters>,
    val tableOperations: Set<UpdateType> = setOf(UpdateType.INSERT)
)

data class NotificationRuleTemplateParameters(
    val paramSource: String,
    val paramSourceType: ParamSourceType,
    val paramType: ParamType,
    val paramLabel: String,
    val paramOperator: String
)

data class NotificationRuleTemplateUpdate(
    val dynamicRuleId: String,
    val ruleName: String,
    val ruleDescription: String = "",
    val ruleTable: String,
    val ruleExpression: RuleExpression? = null,
    val rawRuleExpression: String? = null,
    val resultExpression: ResultExpression,
    val parameterDetails: Map<String, NotificationRuleTemplateParameters>,
    val tableOperations: Set<UpdateType> = setOf(UpdateType.INSERT)
)

data class NotificationRuleInsert(
    val ruleName: String,
    val ruleDescription: String = "",
    val ruleTable: String,
    val ruleExpression: RuleExpression? = null,
    val rawRuleExpression: String? = null,
    val resultExpression: ResultExpression,
    val tableOperations: Set<UpdateType> = setOf(UpdateType.INSERT)
)

data class NotificationRuleUpdate(
    val dynamicRuleId: String,
    val ruleName: String,
    val ruleDescription: String = "",
    val ruleTable: String,
    val ruleExpression: RuleExpression? = null,
    val rawRuleExpression: String? = null,
    val resultExpression: ResultExpression,
    val tableOperations: Set<UpdateType> = setOf(UpdateType.INSERT)
)
