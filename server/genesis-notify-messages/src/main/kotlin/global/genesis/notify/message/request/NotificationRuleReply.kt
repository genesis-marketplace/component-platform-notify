package global.genesis.notify.message.request

import global.genesis.criteria.model.ResultExpression
import global.genesis.criteria.model.RuleExpression
import global.genesis.db.updatequeue.UpdateType
import global.genesis.gen.dao.enums.RuleExecutionStrategy

data class NotificationRuleReply(
    val dynamicRuleId: String,
    val ruleName: String,
    val ruleDescription: String?,
    val ruleTable: String,
    val ruleExecutionStrategy: RuleExecutionStrategy,
    val ruleExpression: RuleExpression?,
    val rawRuleExpression: String?,
    val resultExpression: ResultExpression?,
    val tableOperations: Set<UpdateType>
)
