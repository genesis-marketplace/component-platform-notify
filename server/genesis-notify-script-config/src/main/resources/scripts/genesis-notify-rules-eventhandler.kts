import global.genesis.criteria.util.EvaluatorExpressionWriter
import global.genesis.session.RightSummaryCache
import global.genesis.evaluator.DynamicRuleFactory
import global.genesis.notify.message.event.NotificationRuleInsert
import global.genesis.notify.message.event.NotificationRuleSubscribe
import global.genesis.notify.message.event.NotificationRuleSubscribeContext
import global.genesis.notify.message.event.NotificationRuleTemplateInsert
import global.genesis.notify.message.event.NotificationRuleTemplateUpdate
import global.genesis.notify.message.event.NotificationRuleUpdate
import global.genesis.criteria.model.Assignment
import global.genesis.criteria.model.Expression
import global.genesis.criteria.model.ResultExpression
import global.genesis.db.DbRecord
import global.genesis.db.DbUtil
import global.genesis.db.rx.RxDb
import global.genesis.db.updatequeue.UpdateType
import global.genesis.notify.message.common.NOTIFY_MANAGER_SERVICE

val dynamicRuleFactory = inject<DynamicRuleFactory>()
val cache = inject<RightSummaryCache>()
val dictionary = inject<RxDb>().dictionary

fun addMandatoryFieldsForRule(
    ruleTable: String,
    resultExpression: ResultExpression,
    userName: String
): ResultExpression {
    val assignments = addMandatoryFields(ruleTable, resultExpression)
    assignments.add(Assignment(
        Expression.SingleExpression.Field("SENDER"),
        Expression.SingleExpression.StringValue(userName)
    ))
    return ResultExpression(assignments)
}

fun addMandatoryFieldsForTemplate(
    ruleTable: String,
    resultExpression: ResultExpression
): ResultExpression {
    val assignments = addMandatoryFields(ruleTable, resultExpression)
    assignments.add(Assignment(
        Expression.SingleExpression.Field("SENDER"),
        Expression.SingleExpression.PlaceHolder("USER")
    ))
    return ResultExpression(assignments)
}

fun addMandatoryFields(
    ruleTable: String,
    resultExpression: ResultExpression
): MutableList<Assignment> {
    val assignments = resultExpression.assignments.toMutableList()

    assignments.add(Assignment(
        Expression.SingleExpression.Field("TABLE_NAME"),
        Expression.SingleExpression.StringValue(ruleTable)
    ))

    val primaryKey = DbUtil.getPrimaryKey(dictionary, DbRecord(ruleTable))

    if (primaryKey != null && primaryKey.fields.size == 1) {
        assignments.add(Assignment(
            Expression.SingleExpression.Field("TABLE_ENTITY_ID"),
            Expression.SingleExpression.Field(primaryKey.fields[0].name)
        ))
    }

    return assignments
}

eventHandler {
    eventHandler<NotificationRuleTemplateInsert>(name = "NOTIFICATION_RULE_TEMPLATE_CREATE", transactional = true) {
        permissioning {
            permissionCodes = listOf("NotificationRuleTemplateCreate")
        }
        onValidate { event ->
            require(!event.details.parameterDetails.keys.contains("USER")) {
                "USER is a reserved keyword for rule creation, please choose another key for your parameter"
            }
            ack()
        }
        onCommit { event ->
            val details = event.details

            val ruleExpressionString = if (details.ruleExpression != null) {
                EvaluatorExpressionWriter.writeRuleExpression(details.ruleExpression!!)
            } else {
                details.rawRuleExpression!!
            }

            val dynamicRule = DynamicRule {
                userName = event.userName
                name = details.ruleName
                description = details.ruleDescription
                ruleTable = details.ruleTable
                ruleExecutionStrategy = RuleExecutionStrategy.UNLIMITED
                ruleExpression = ruleExpressionString
                resultExpression = EvaluatorExpressionWriter.writeResultExpression(
                    addMandatoryFieldsForTemplate(details.ruleTable, details.resultExpression)
                )
                ruleStatus = RuleStatus.ENABLED
                processName = NOTIFY_MANAGER_SERVICE
                messageType = "EVENT_NOTIFY_INSERT"
                isTemplate = true
                ruleType = "NOTIFY"
                tableOperation = details.tableOperations.joinToString("|")
            }

            val result = entityDb.insert(dynamicRule)

            val parameterRecords = details.parameterDetails.map {
                DynamicRuleTemplateParams {
                    dynamicRuleId = result.record.id
                    paramName = it.key
                    paramSource = it.value.paramSource
                    paramSourceType = it.value.paramSourceType
                    paramType = it.value.paramType
                    paramLabel = it.value.paramLabel
                    paramOperator = it.value.paramOperator
                }
            }

            entityDb.insertAll(parameterRecords)

            ack()
        }
    }

    eventHandler<NotificationRuleTemplateUpdate>(name = "NOTIFICATION_RULE_TEMPLATE_UPDATE", transactional = true) {
        permissioning {
            permissionCodes = listOf("NotificationRuleTemplateUpdate")
        }
        onValidate { event ->
            val record = entityDb.get(DynamicRule.byId(event.details.dynamicRuleId))
            require(record != null) {
                "Dynamic Rule of ID ${event.details.dynamicRuleId} doesn't exist!"
            }
            require(record.userName == event.userName || cache.userHasRight(event.userName, "NotificationAdminAction")) {
                "User ${event.userName} lacks sufficient permissions"
            }
            require(!event.details.parameterDetails.keys.contains("USER")) {
                "USER is a reserved keyword for rule creation, please choose another key for your parameter"
            }
            ack()
        }
        onCommit { event ->
            val details = event.details

            val ruleExpressionString = if (details.ruleExpression != null) {
                EvaluatorExpressionWriter.writeRuleExpression(details.ruleExpression!!)
            } else {
                details.rawRuleExpression!!
            }

            val dynamicRule = DynamicRule {
                id = details.dynamicRuleId
                userName = event.userName
                name = details.ruleName
                description = details.ruleDescription
                ruleTable = details.ruleTable
                ruleExecutionStrategy = RuleExecutionStrategy.UNLIMITED
                ruleExpression = ruleExpressionString
                resultExpression = EvaluatorExpressionWriter.writeResultExpression(
                    addMandatoryFieldsForTemplate(details.ruleTable, details.resultExpression)
                )
                ruleStatus = RuleStatus.ENABLED
                processName = NOTIFY_MANAGER_SERVICE
                messageType = "EVENT_NOTIFY_INSERT"
                isTemplate = true
                ruleType = "NOTIFY"
                tableOperation = details.tableOperations.joinToString("|")
            }

            entityDb.modify(dynamicRule)

            val existingParameters = entityDb.getRange(DynamicRuleTemplateParams.byDynamicRuleId(dynamicRule.id)).toList()
            val existingParamNames = existingParameters
                .map { it.paramName }
                .toSet()

            val recordsToUpsert = details.parameterDetails.map { entry ->
                DynamicRuleTemplateParams {
                    dynamicRuleId = dynamicRule.id
                    paramName = entry.key
                    paramSource = entry.value.paramSource
                    paramSourceType = entry.value.paramSourceType
                    paramType = entry.value.paramType
                    paramLabel = entry.value.paramLabel
                    paramOperator = entry.value.paramOperator
                }
            }.toList()

            entityDb.upsertAll(recordsToUpsert.map { EntityModifyDetails(it) })

            val paramNamesInserted = recordsToUpsert.map { it.paramName }.toSet()
            val namesToDelete = existingParamNames - paramNamesInserted
            val recordsToDelete = existingParameters.filter { it.paramName in namesToDelete }

            entityDb.deleteAll(recordsToDelete)

            ack()
        }
    }

    eventHandler<DynamicRule.ById>(name = "NOTIFICATION_RULE_TEMPLATE_DELETE", transactional = true) {
        permissioning {
            permissionCodes = listOf("NotificationRuleTemplateDelete")
        }
        onValidate { event ->
            val record = entityDb.get(DynamicRule.byId(event.details.id))
            require(record != null) {
                "Dynamic Rule of ID ${event.details.id} doesn't exist!"
            }
            require(record.userName == event.userName || cache.userHasRight(event.userName, "NotificationAdminAction")) {
                "User ${event.userName} lacks sufficient permissions"
            }
            ack()
        }
        onCommit { event ->
            val details = event.details
            entityDb.delete(DynamicRule.byId(details.id))
            entityDb.deleteAll(entityDb.getRange(DynamicRuleTemplateParams.byDynamicRuleId(details.id)))
            ack()
        }
    }

    eventHandler<NotificationRuleInsert>(name = "NOTIFICATION_RULE_CREATE", transactional = true) {
        permissioning {
            permissionCodes = listOf("NotificationRuleCreate")
        }
        onCommit {event ->
            val details = event.details

            val ruleExpressionString = if (details.ruleExpression != null) {
                EvaluatorExpressionWriter.writeRuleExpression(details.ruleExpression!!)
            } else {
                details.rawRuleExpression!!
            }

            val dynamicRule = DynamicRule {
                userName = event.userName
                name = details.ruleName
                description = details.ruleDescription
                ruleTable = details.ruleTable
                ruleExecutionStrategy = RuleExecutionStrategy.UNLIMITED
                ruleExpression = ruleExpressionString
                resultExpression = EvaluatorExpressionWriter.writeResultExpression(
                    addMandatoryFieldsForRule(details.ruleTable, details.resultExpression, event.userName)
                )
                ruleStatus = RuleStatus.ENABLED
                processName = NOTIFY_MANAGER_SERVICE
                messageType = "EVENT_NOTIFY_INSERT"
                isTemplate = false
                ruleType = "NOTIFY"
                tableOperation = details.tableOperations.joinToString("|")
            }

            entityDb.insert(dynamicRule)

            ack()
        }
    }

    eventHandler<NotificationRuleUpdate>(name = "NOTIFICATION_RULE_UPDATE", transactional = true) {
        permissioning {
            permissionCodes = listOf("NotificationRuleUpdate")
        }
        onValidate { event ->
            val record = entityDb.get(DynamicRule.byId(event.details.dynamicRuleId))
            require(record != null) {
                "Dynamic Rule of ID ${event.details.dynamicRuleId} doesn't exist!"
            }
            require(record.userName == event.userName || cache.userHasRight(event.userName, "NotificationAdminAction")) {
                "User ${event.userName} lacks sufficient permissions"
            }
            ack()
        }
        onCommit { event ->
            val details = event.details

            val ruleExpressionString = if (details.ruleExpression != null) {
            EvaluatorExpressionWriter.writeRuleExpression(details.ruleExpression!!)
            } else {
                details.rawRuleExpression!!
            }

            val dynamicRule = DynamicRule {
                id = details.dynamicRuleId
                userName = event.userName
                name = details.ruleName
                description = details.ruleDescription
                ruleTable = details.ruleTable
                ruleExecutionStrategy = RuleExecutionStrategy.UNLIMITED
                ruleExpression = ruleExpressionString
                resultExpression = EvaluatorExpressionWriter.writeResultExpression(
                    addMandatoryFieldsForRule(details.ruleTable, details.resultExpression, event.userName)
                )
                ruleStatus = RuleStatus.ENABLED
                processName = NOTIFY_MANAGER_SERVICE
                messageType = "EVENT_NOTIFY_INSERT"
                ruleType = "NOTIFY"
                tableOperation = details.tableOperations.joinToString("|")
            }

            entityDb.modify(dynamicRule)

            ack()
        }
    }

    eventHandler<DynamicRule.ById>(name = "NOTIFICATION_RULE_DELETE", transactional = true) {
        permissioning {
            permissionCodes = listOf("NotificationRuleDelete")
        }
        onValidate { event ->
            val record = entityDb.get(DynamicRule.byId(event.details.id))
            require(record != null) {
                "Dynamic Rule of ID ${event.details.id} doesn't exist!"
            }
            require(record.userName == event.userName || cache.userHasRight(event.userName, "NotificationAdminAction")) {
                "User ${event.userName} lacks sufficient permissions"
            }
            ack()
        }
        onCommit { event ->
            val details = event.details
            entityDb.delete(DynamicRule.byId(details.id))
            ack()
        }
    }

    contextEventHandler<NotificationRuleSubscribe, NotificationRuleSubscribeContext>(name = "NOTIFICATION_RULE_SUBSCRIBE", transactional = true) {
        permissioning {
            permissionCodes = listOf("NotificationRuleSubscribe")
        }
        onValidate { event ->
            val templateId = event.details.dynamicRuleId
            val dynamicRuleTemplate: DynamicRule? = entityDb.get(DynamicRule.byId(templateId))
            val templateParameters = entityDb.getRange(DynamicRuleTemplateParams.byDynamicRuleId(templateId)).toList()
            if (dynamicRuleTemplate == null) {
                validationResult(nack("Unable to find dynamic rule template with id $templateId"))
            } else {
                dynamicRuleFactory.validateTemplateParameters(
                    dynamicRuleTemplate.id,
                    event.details.parameterDetails,
                    templateParameters
                )
                validationResult(ack(), NotificationRuleSubscribeContext(dynamicRuleTemplate, templateParameters))
            }
        }
        onCommit { event, context ->
            val details = event.details
            val dynamicRuleCustom = dynamicRuleFactory.createDynamicRuleFromTemplate(
                event.userName,
                context!!.dynamicRuleTemplate,
                details.parameterDetails,
                context.templateParameters
            )
            dynamicRuleCustom.ruleType = "NOTIFY"
            val result = entityDb.insert(dynamicRuleCustom)
            ack()
        }
    }

    eventHandler<DynamicRule.ById>(name = "NOTIFICATION_RULE_UNSUBSCRIBE", transactional = true) {
        permissioning {
            permissionCodes = listOf("NotificationRuleUnsubscribe")
        }
        onValidate { event ->
            val record = entityDb.get(DynamicRule.byId(event.details.id))
            require(record != null) {
                "No record found for Dynamic Rule ID: ${event.details.id}"
            }
            require(record.userName == event.userName || cache.userHasRight(event.userName, "NotificationAdminAction")) {
                "User ${event.userName} lacks sufficient permissions"
            }
            ack()
        }
        onCommit { event ->
            val details = event.details
            entityDb.delete(DynamicRule.byId(details.id))
            ack()
        }
    }

    eventHandler<DynamicRule.ById>(name = "NOTIFICATION_RULE_DISABLE", transactional = true) {
        permissioning {
            permissionCodes = listOf("NotificationRuleDisable")
        }
        onValidate { event ->
            val record = entityDb.get(DynamicRule.byId(event.details.id))
            require(record?.userName == event.userName || cache.userHasRight(event.userName, "NotificationAdminAction")) {
                "User ${event.userName} lacks sufficient permissions"
            }
            ack()
        }
        onCommit { event ->
            val details = event.details
            val dynamicRule: DynamicRule? = entityDb.get(DynamicRule.byId(details.id))
            if (dynamicRule != null) {
                dynamicRule.ruleStatus = RuleStatus.DISABLED
                entityDb.modify(dynamicRule)
                ack()
            } else {
                nack("Cannot find Dynamic Rule for ID: " + details.id)
            }
        }
    }

    eventHandler<DynamicRule.ById>(name = "NOTIFICATION_RULE_ENABLE", transactional = true) {
        permissioning {
            permissionCodes = listOf("NotificationRuleEnable")
        }
        onValidate { event ->
            val record = entityDb.get(DynamicRule.byId(event.details.id))
            require(record?.userName == event.userName || cache.userHasRight(event.userName, "NotificationAdminAction")) {
                "User ${event.userName} lacks sufficient permissions"
            }
            ack()
        }
        onCommit { event ->
            val details = event.details
            val dynamicRule: DynamicRule? = entityDb.get(DynamicRule.byId(details.id))
            if (dynamicRule != null) {
                dynamicRule.ruleStatus = RuleStatus.ENABLED
                entityDb.modify(dynamicRule)
                ack()
            } else {
                nack("Cannot find Dynamic Rule for ID: " + details.id)
            }
        }
    }
}
