package global.genesis.notify.message.event

import global.genesis.commons.model.GenesisSet
import global.genesis.criteria.model.Assignment
import global.genesis.criteria.model.Condition
import global.genesis.criteria.model.Expression
import global.genesis.criteria.model.Operation
import global.genesis.criteria.model.ResultExpression
import global.genesis.criteria.model.RuleExpression
import global.genesis.db.updatequeue.UpdateType
import global.genesis.gen.dao.DynamicRule
import global.genesis.gen.dao.DynamicRuleTemplateParams
import global.genesis.gen.dao.enums.ParamSourceType
import global.genesis.gen.dao.enums.ParamType
import global.genesis.gen.dao.enums.RuleExecutionStrategy
import global.genesis.message.core.event.EventReply
import global.genesis.testsupport.AbstractGenesisTestSupport
import global.genesis.testsupport.GenesisTestConfig
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NotificationRuleTemplateEventHandlerTest : AbstractGenesisTestSupport<GenesisSet>(
    GenesisTestConfig {
        packageName = "global.genesis.eventhandler.pal"
        genesisHome = "/GenesisHome/"
        initialDataFile = "notification-rule-template-data.csv"
        scriptFileName = "genesis-notify-rules-eventhandler.kts"
        parser = { it }
        useTempClassloader = true
    }
) {
    private val ruleExpression = RuleExpression(
        listOf(
            Condition(
                Expression.SingleExpression.Field("QUANTITY"),
                Operation.GREATER_THAN,
                Expression.SingleExpression.NumericValue(BigDecimal.valueOf(500L))
            ),
            Condition(
                Expression.SingleExpression.Field("ACCOUNT"),
                Operation.EQUALS,
                Expression.SingleExpression.StringValue("TEST_ACCOUNT")
            ),
            Condition(
                Expression.SingleExpression.Field("PNL"),
                Operation.LESS_THAN,
                Expression.BinaryExpression(
                    Expression.SingleExpression.Field("COST"),
                    Operation.PLUS,
                    Expression.SingleExpression.Field("FEES")
                )
            )
        )
    )

    private val resultExpression = ResultExpression(
        listOf(
            Assignment(
                Expression.SingleExpression.Field("TOPIC"),
                Expression.SingleExpression.StringValue("New Topic")
            )
        )
    )

    private val parameterDetail = NotificationRuleTemplateParameters(
        paramSource = "REQ_TEST",
        paramSourceType = ParamSourceType.REQ_REP,
        paramType = ParamType.STRING,
        paramLabel = "PARAM_LABEL",
        paramOperator = ">"
    )

    private val newParameterDetail = NotificationRuleTemplateParameters(
        paramSource = "",
        paramSourceType = ParamSourceType.USER_TEXT,
        paramType = ParamType.NUMBER,
        paramLabel = "PARAM_KEY-new",
        paramOperator = "<"
    )

    override fun systemDefinition(): Map<String, Any> = mapOf(
        "IS_SCRIPT" to "true"
    )

    @Test
    fun `insert notification rule template + params without permission should fail`(): Unit = runBlocking {
        val result = sendEvent(
            NotificationRuleTemplateInsert(
                ruleName = "name",
                ruleDescription = "description",
                ruleTable = "table_name",
                ruleExpression = ruleExpression,
                resultExpression = ResultExpression(emptyList()),
                parameterDetails = mapOf("parameter_name" to parameterDetail)
            ),
            userName = "JohnyDoe",
            messageType = "EVENT_NOTIFICATION_RULE_TEMPLATE_CREATE"
        ).assertedCast<EventReply.EventNack>()

        assertTrue(result.error[0].text == "User JohnyDoe lacks sufficient permissions")
    }

    @Test
    fun `test insert notification rule template + params`(): Unit = runBlocking {
        sendEvent(
            NotificationRuleTemplateInsert(
                ruleName = "name",
                ruleDescription = "description",
                ruleTable = "table_name",
                ruleExpression = ruleExpression,
                resultExpression = resultExpression,
                parameterDetails = mapOf("parameter_name" to parameterDetail),
                tableOperations = setOf(UpdateType.INSERT, UpdateType.MODIFY)
            ),
            userName = "JohnDoe",
            messageType = "EVENT_NOTIFICATION_RULE_TEMPLATE_CREATE"
        ).assertedCast<EventReply.EventAck>()

        val dynamicRules = entityDb.getRange(DynamicRule.byName("name")).toList()
        assertEquals(1, dynamicRules.size)

        val dynamicRule = dynamicRules[0]
        assertEquals("name", dynamicRule.name)
        assertEquals("description", dynamicRule.description)
        assertEquals("table_name", dynamicRule.ruleTable)
        assertEquals(RuleExecutionStrategy.UNLIMITED, dynamicRule.ruleExecutionStrategy)
        assertTrue(dynamicRule.tableOperation == "INSERT|MODIFY" || dynamicRule.tableOperation == "MODIFY|INSERT")
        assertEquals("((QUANTITY > 500) && (ACCOUNT == \"TEST_ACCOUNT\") && (PNL < (COST + FEES)))", dynamicRule.ruleExpression)
        assertEquals("((TOPIC = \"New Topic\") && (TABLE_NAME = \"table_name\") && (SENDER = {{USER}}))", dynamicRule.resultExpression)

        val dynamicRuleParams = entityDb.getRange(DynamicRuleTemplateParams.byDynamicRuleId(dynamicRule.id)).toList()
        assertEquals(1, dynamicRuleParams.size)

        assertEquals(dynamicRule.id, dynamicRuleParams[0].dynamicRuleId)
        assertEquals("parameter_name", dynamicRuleParams[0].paramName)
        assertEquals("REQ_TEST", dynamicRuleParams[0].paramSource)
        assertEquals(ParamSourceType.REQ_REP, dynamicRuleParams[0].paramSourceType)
        assertEquals(ParamType.STRING, dynamicRuleParams[0].paramType)
        assertEquals("PARAM_LABEL", dynamicRuleParams[0].paramLabel)
        assertEquals(">", dynamicRuleParams[0].paramOperator)
    }

    @Test
    fun `update notification rule template + params without permission should fail`() {
        runBlocking {
            val result = sendEvent(
                NotificationRuleTemplateUpdate(
                    dynamicRuleId = "DR-1234",
                    ruleName = "name-new",
                    ruleDescription = "description-new",
                    ruleTable = "table_name-new",
                    ruleExpression = ruleExpression,
                    resultExpression = ResultExpression(emptyList()),
                    parameterDetails = mapOf("parameter_name-new" to newParameterDetail)
                ),
                userName = "JohnyDoe",
                messageType = "EVENT_NOTIFICATION_RULE_TEMPLATE_UPDATE"
            ).assertedCast<EventReply.EventNack>()

            assertTrue(result.error[0].text == "User JohnyDoe lacks sufficient permissions")
        }
    }

    @Test
    fun `test update notification rule template + params`(): Unit = runBlocking {
        val dynamicRule = entityDb.get(DynamicRule.byId("DR-1234"))
        assertNotNull(dynamicRule)

        sendEvent(
            NotificationRuleTemplateUpdate(
                dynamicRuleId = "DR-1234",
                ruleName = "name-new",
                ruleDescription = "description-new",
                ruleTable = "table_name-new",
                ruleExpression = ruleExpression,
                resultExpression = resultExpression,
                parameterDetails = mapOf("parameter_name-new" to newParameterDetail)
            ),
            userName = "JohnDoe",
            messageType = "EVENT_NOTIFICATION_RULE_TEMPLATE_UPDATE"
        ).assertedCast<EventReply.EventAck>()

        val dynamicRuleNew = entityDb.get(DynamicRule.byId(dynamicRule.id))
        assertNotNull(dynamicRuleNew)

        assertEquals("name-new", dynamicRuleNew.name)
        assertEquals("description-new", dynamicRuleNew.description)
        assertEquals("table_name-new", dynamicRuleNew.ruleTable)
        assertEquals(RuleExecutionStrategy.UNLIMITED, dynamicRuleNew.ruleExecutionStrategy)
        assertEquals("INSERT", dynamicRule.tableOperation)
        assertEquals("((QUANTITY > 500) && (ACCOUNT == \"TEST_ACCOUNT\") && (PNL < (COST + FEES)))", dynamicRuleNew.ruleExpression)
        assertEquals("((TOPIC = \"New Topic\") && (TABLE_NAME = \"table_name-new\") && (SENDER = {{USER}}))", dynamicRuleNew.resultExpression)

        val dynamicRuleParams = entityDb.getRange(DynamicRuleTemplateParams.byDynamicRuleId(dynamicRuleNew.id)).toList()

        assertEquals(1, dynamicRuleParams.size)

        assertEquals(dynamicRule.id, dynamicRuleParams[0].dynamicRuleId)
        assertEquals("parameter_name-new", dynamicRuleParams[0].paramName)
        assertTrue(dynamicRuleParams[0].paramSource.isNullOrBlank())
        assertEquals(ParamSourceType.USER_TEXT, dynamicRuleParams[0].paramSourceType)
        assertEquals(ParamType.NUMBER, dynamicRuleParams[0].paramType)
        assertEquals("PARAM_KEY-new", dynamicRuleParams[0].paramLabel)
        assertEquals("<", dynamicRuleParams[0].paramOperator)
    }

    @Test
    fun `update notification rule template + params that doesn't exist should fail`(): Unit = runBlocking {
        val dynamicRule = entityDb.get(DynamicRule.byId("DR-XXXX"))
        kotlin.test.assertNull(dynamicRule)

        val result = sendEvent(
            NotificationRuleTemplateUpdate(
                dynamicRuleId = "DR-XXXX",
                ruleName = "name-new",
                ruleDescription = "description-new",
                ruleTable = "table_name-new",
                ruleExpression = ruleExpression,
                resultExpression = resultExpression,
                parameterDetails = mapOf("parameter_name-new" to newParameterDetail)
            ),
            userName = "JohnDoe",
            messageType = "EVENT_NOTIFICATION_RULE_TEMPLATE_UPDATE"
        ).assertedCast<EventReply.EventNack>()

        assertTrue(result.error[0].text == "Dynamic Rule of ID DR-XXXX doesn't exist!")
    }

    @Test
    fun `super user should be able to update notification rule template + params of another user`(): Unit = runBlocking {
        val dynamicRule = entityDb.get(DynamicRule.byId("DR-1234"))
        assertNotNull(dynamicRule)

        sendEvent(
            NotificationRuleTemplateUpdate(
                dynamicRuleId = "DR-1234",
                ruleName = "name-new",
                ruleDescription = "description-new",
                ruleTable = "table_name-new",
                ruleExpression = ruleExpression,
                resultExpression = resultExpression,
                parameterDetails = mapOf("parameter_name-new" to newParameterDetail)
            ),
            userName = "JaneDoe",
            messageType = "EVENT_NOTIFICATION_RULE_TEMPLATE_UPDATE"
        ).assertedCast<EventReply.EventAck>()

        val dynamicRuleNew = entityDb.get(DynamicRule.byId(dynamicRule.id))
        assertNotNull(dynamicRuleNew)

        assertEquals("name-new", dynamicRuleNew.name)
        assertEquals("description-new", dynamicRuleNew.description)
        assertEquals("table_name-new", dynamicRuleNew.ruleTable)
        assertEquals(RuleExecutionStrategy.UNLIMITED, dynamicRuleNew.ruleExecutionStrategy)
        assertEquals("INSERT", dynamicRule.tableOperation)
        assertEquals("((QUANTITY > 500) && (ACCOUNT == \"TEST_ACCOUNT\") && (PNL < (COST + FEES)))", dynamicRuleNew.ruleExpression)
        assertEquals("((TOPIC = \"New Topic\") && (TABLE_NAME = \"table_name-new\") && (SENDER = {{USER}}))", dynamicRuleNew.resultExpression)

        val dynamicRuleParams = entityDb.getRange(DynamicRuleTemplateParams.byDynamicRuleId(dynamicRuleNew.id)).toList()

        assertEquals(1, dynamicRuleParams.size)

        assertEquals(dynamicRule.id, dynamicRuleParams[0].dynamicRuleId)
        assertEquals("parameter_name-new", dynamicRuleParams[0].paramName)
        assertTrue(dynamicRuleParams[0].paramSource.isNullOrBlank())
        assertEquals(ParamSourceType.USER_TEXT, dynamicRuleParams[0].paramSourceType)
        assertEquals(ParamType.NUMBER, dynamicRuleParams[0].paramType)
        assertEquals("PARAM_KEY-new", dynamicRuleParams[0].paramLabel)
        assertEquals("<", dynamicRuleParams[0].paramOperator)
    }

    @Test
    fun `non super user should not be able to update notification rule template + params of another user`(): Unit = runBlocking {
        val dynamicRule = entityDb.get(DynamicRule.byId("DR-1234"))
        assertNotNull(dynamicRule)

        val result = sendEvent(
            NotificationRuleTemplateUpdate(
                dynamicRuleId = "DR-1234",
                ruleName = "name-new",
                ruleDescription = "description-new",
                ruleTable = "table_name-new",
                ruleExpression = ruleExpression,
                resultExpression = ResultExpression(emptyList()),
                parameterDetails = mapOf("parameter_name-new" to newParameterDetail)
            ),
            userName = "JanieDoe",
            messageType = "EVENT_NOTIFICATION_RULE_TEMPLATE_UPDATE"
        ).assertedCast<EventReply.EventNack>()

        assertTrue(result.error[0].text == "User JanieDoe lacks sufficient permissions")

        val dynamicRuleNew = entityDb.get(DynamicRule.byId(dynamicRule.id))
        assertNotNull(dynamicRuleNew)

        assertEquals("name-update", dynamicRuleNew.name)
        assertEquals("description-update", dynamicRuleNew.description)
        assertEquals("table_name-update", dynamicRuleNew.ruleTable)
        assertEquals(RuleExecutionStrategy.UNLIMITED, dynamicRuleNew.ruleExecutionStrategy)
        assertEquals("((QUANTITY > 1000))", dynamicRuleNew.ruleExpression)
        assertEquals("((TOPIC = \"Topic\"))", dynamicRuleNew.resultExpression)

        val dynamicRuleParams = entityDb.getRange(DynamicRuleTemplateParams.byDynamicRuleId(dynamicRuleNew.id)).toList().sorted()

        assertEquals(2, dynamicRuleParams.size)

        assertEquals(dynamicRule.id, dynamicRuleParams[0].dynamicRuleId)
        assertEquals("parameter_name-update", dynamicRuleParams[0].paramName)
        assertEquals("parameter_name-update-two", dynamicRuleParams[1].paramName)
    }

    @Test
    fun `delete notification rule template + params without permission should fail`(): Unit = runBlocking {
        val result = sendEvent(
            DynamicRule.byId("DR-1234"),
            userName = "JohnyDoe",
            messageType = "EVENT_NOTIFICATION_RULE_TEMPLATE_DELETE"
        ).assertedCast<EventReply.EventNack>()

        assertTrue(result.error[0].text == "User JohnyDoe lacks sufficient permissions")
    }

    @Test
    fun `test delete notification rule template + params`(): Unit = runBlocking {
        val dynamicRule = entityDb.get(DynamicRule.byId("DR-1234"))
        assertNotNull(dynamicRule)

        sendEvent(
            DynamicRule.byId("DR-1234"),
            userName = "JohnDoe",
            messageType = "EVENT_NOTIFICATION_RULE_TEMPLATE_DELETE"
        ).assertedCast<EventReply.EventAck>()

        assertNull(entityDb.get(DynamicRule.byId("DR-1234")))
        assertEquals(0, entityDb.getRange(DynamicRuleTemplateParams.byDynamicRuleId("DR-1234")).toList().size)
    }

    @Test
    fun `delete notification rule + params that doesn't exist should fail`(): Unit = runBlocking {
        val dynamicRule = entityDb.get(DynamicRule.byId("DR-XXXX"))
        kotlin.test.assertNull(dynamicRule)

        val result = sendEvent(
            DynamicRule.byId("DR-XXXX"),
            userName = "JohnDoe",
            messageType = "EVENT_NOTIFICATION_RULE_TEMPLATE_DELETE"
        ).assertedCast<EventReply.EventNack>()

        assertTrue(result.error[0].text == "Dynamic Rule of ID DR-XXXX doesn't exist!")
    }

    @Test
    fun `super user should be able to delete another users notification rule template + params`(): Unit = runBlocking {
        val dynamicRule = entityDb.get(DynamicRule.byId("DR-1234"))
        assertNotNull(dynamicRule)

        sendEvent(
            DynamicRule.byId("DR-1234"),
            userName = "JaneDoe",
            messageType = "EVENT_NOTIFICATION_RULE_TEMPLATE_DELETE"
        ).assertedCast<EventReply.EventAck>()

        assertNull(entityDb.get(DynamicRule.byId("DR-1234")))
        assertEquals(0, entityDb.getRange(DynamicRuleTemplateParams.byDynamicRuleId("DR-1234")).toList().size)
    }

    @Test
    fun `non super user should not be able to delete another users notification rule template + params`(): Unit = runBlocking {
        val dynamicRule = entityDb.get(DynamicRule.byId("DR-1234"))
        assertNotNull(dynamicRule)

        val result = sendEvent(
            DynamicRule.byId("DR-1234"),
            userName = "JanieDoe",
            messageType = "EVENT_NOTIFICATION_RULE_TEMPLATE_DELETE"
        ).assertedCast<EventReply.EventNack>()

        assertTrue(result.error[0].text == "User JanieDoe lacks sufficient permissions")

        assertNotNull(entityDb.get(DynamicRule.byId("DR-1234")))
        assertEquals(2, entityDb.getRange(DynamicRuleTemplateParams.byDynamicRuleId("DR-1234")).toList().size)
    }

    @Test
    fun `test insert notification rule template with existing table`(): Unit = runBlocking {
        // NOTIFY table certain exists because I'm inside the module that defines it (see genesis-notify-tables-dictionary.kts)
        sendEvent(
            NotificationRuleTemplateInsert(
                ruleName = "NOTIFY Template",
                ruleDescription = "NOTIFY Description",
                ruleTable = "NOTIFY",
                ruleExpression = ruleExpression,
                resultExpression = ResultExpression(resultExpression.assignments),
                parameterDetails = mapOf("parameter_name" to parameterDetail)
            ),
            userName = "JohnDoe",
            messageType = "EVENT_NOTIFICATION_RULE_TEMPLATE_CREATE"
        ).assertedCast<EventReply.EventAck>()

        val dynamicRules = entityDb.getRange(DynamicRule.byName("NOTIFY Template")).toList()
        assertEquals(
            "((TOPIC = \"New Topic\") && (TABLE_NAME = \"NOTIFY\") && (TABLE_ENTITY_ID = NOTIFY_ID) && (SENDER = {{USER}}))",
            dynamicRules[0].resultExpression
        )
    }

    @Test
    fun `test modify notification rule template with existing table`(): Unit = runBlocking {
        // NOTIFY table certain exists because I'm inside the module that defines it (see genesis-notify-tables-dictionary.kts)
        sendEvent(
            NotificationRuleTemplateUpdate(
                dynamicRuleId = "DR-1234",
                ruleName = "NOTIFY Template",
                ruleDescription = "NOTIFY Description",
                ruleTable = "NOTIFY",
                ruleExpression = ruleExpression,
                resultExpression = ResultExpression(resultExpression.assignments),
                parameterDetails = mapOf("parameter_name-new" to newParameterDetail)
            ),
            userName = "JohnDoe",
            messageType = "EVENT_NOTIFICATION_RULE_TEMPLATE_UPDATE"
        ).assertedCast<EventReply.EventAck>()

        val dynamicRules = entityDb.getRange(DynamicRule.byName("NOTIFY Template")).toList()
        assertEquals(
            "((TOPIC = \"New Topic\") && (TABLE_NAME = \"NOTIFY\") && (TABLE_ENTITY_ID = NOTIFY_ID) && (SENDER = {{USER}}))",
            dynamicRules[0].resultExpression
        )
    }

    @Test
    fun `test subscribe to notification rule - with permission`(): Unit = runBlocking {
        val dynamicRuleTemplate = entityDb.get(DynamicRule.byId("DR-0000"))
        assertNotNull(dynamicRuleTemplate)

        sendEvent(
            NotificationRuleSubscribe(
                dynamicRuleId = "DR-0000",
                parameterDetails = mapOf("COST" to "400")
            ),
            userName = "JohnDoe",
            messageType = "EVENT_NOTIFICATION_RULE_SUBSCRIBE"
        ).assertedCast<EventReply.EventAck>()

        val (ruleTemplates, dynamicRules) = entityDb.getRange(DynamicRule.byName("template")).toList().partition {
            it.isTemplate
        }
        assertEquals(1, ruleTemplates.size)
        assertEquals(1, dynamicRules.size)
        assertEquals("JohnDoe", dynamicRules[0].userName)
        assertEquals("((COST > \"400\"))", dynamicRules[0].ruleExpression)
    }

    @Test
    fun `test subscribe to two notification rules with same parameterDetails has unique ids - with permission`(): Unit = runBlocking {
        val dynamicRuleTemplate = entityDb.get(DynamicRule.byId("DR-0000"))
        assertNotNull(dynamicRuleTemplate)

        sendEvent(
            NotificationRuleSubscribe(
                dynamicRuleId = "DR-0000",
                parameterDetails = mapOf("COST" to "400")
            ),
            userName = "JohnDoe",
            messageType = "EVENT_NOTIFICATION_RULE_SUBSCRIBE"
        ).assertedCast<EventReply.EventAck>()

        sendEvent(
            NotificationRuleSubscribe(
                dynamicRuleId = "DR-0000",
                parameterDetails = mapOf("COST" to "400")
            ),
            userName = "JohnDoe",
            messageType = "EVENT_NOTIFICATION_RULE_SUBSCRIBE"
        ).assertedCast<EventReply.EventAck>()

        val dynamicRules = entityDb.getRange(DynamicRule.byName("template")).toList()
        assertEquals(3, dynamicRules.size)
        assertNotEquals(dynamicRules[0].id, dynamicRules[1].id, dynamicRules[2].id)
    }

    @Test
    fun `test unsubscribe from own notification rule - with permission`(): Unit = runBlocking {
        val dynamicRule = entityDb.get(DynamicRule.byId("DR-0001"))
        assertNotNull(dynamicRule)

        sendEvent(
            DynamicRule.byId("DR-0001"),
            userName = "JohnDoe",
            messageType = "EVENT_NOTIFICATION_RULE_UNSUBSCRIBE"
        ).assertedCast<EventReply.EventAck>()

        assertNull(entityDb.get(DynamicRule.byId("DR-0001")))
    }

    @Test
    fun `unsubscribing from the notification rule of another user should fail - without super permission`(): Unit = runBlocking {
        val result = sendEvent(
            DynamicRule.byId("DR-0001"),
            userName = "JaneDoe",
            messageType = "EVENT_NOTIFICATION_RULE_UNSUBSCRIBE"
        ).assertedCast<EventReply.EventNack>()

        assertTrue(result.error[0].text == "User JaneDoe lacks sufficient permissions")
        assertNotNull(entityDb.get(DynamicRule.byId("DR-0001")))
    }

    @Test
    fun `unsubscribing the notification rule of another user should work - with super permission`(): Unit = runBlocking {
        sendEvent(
            DynamicRule.byId("DR-0001"),
            userName = "JohnyDoe",
            messageType = "EVENT_NOTIFICATION_RULE_UNSUBSCRIBE"
        ).assertedCast<EventReply.EventAck>()

        assertNull(entityDb.get(DynamicRule.byId("DR-0001")))
    }

    @Test
    fun `test unsubscribe from notification rule - without permission`(): Unit = runBlocking {
        val result = sendEvent(
            DynamicRule.byId("DR-0002"),
            userName = "JanieDoe",
            messageType = "EVENT_NOTIFICATION_RULE_UNSUBSCRIBE"
        ).assertedCast<EventReply.EventNack>()

        assertTrue(result.error[0].text == "User JanieDoe lacks sufficient permissions")

        val dynamicRule = entityDb.get(DynamicRule.byId("DR-0002"))
        assertNotNull(dynamicRule)
    }

    @Test
    fun `test unsubscribe from notification rule that doesn't exists - with permission`(): Unit = runBlocking {
        val dynamicRule = entityDb.get(DynamicRule.byId("DR-0003"))
        assertNull(dynamicRule)

        val result = sendEvent(
            DynamicRule.byId("DR-0003"),
            userName = "JohnDoe",
            messageType = "EVENT_NOTIFICATION_RULE_UNSUBSCRIBE"
        ).assertedCast<EventReply.EventNack>()

        assertTrue(result.error[0].text == "No record found for Dynamic Rule ID: DR-0003")
    }

    @Test
    fun `sender is set correctly in result expression when template is created and subsequently subscribed to`() = runBlocking {
        sendEvent(
            NotificationRuleTemplateInsert(
                ruleName = "NOTIFY Template",
                ruleDescription = "NOTIFY Description",
                ruleTable = "NOTIFY",
                ruleExpression = ruleExpression,
                resultExpression = ResultExpression(resultExpression.assignments),
                parameterDetails = emptyMap()
            ),
            userName = "JohnDoe",
            messageType = "EVENT_NOTIFICATION_RULE_TEMPLATE_CREATE"
        ).assertedCast<EventReply.EventAck>()

        var dynamicRules = entityDb.getRange(DynamicRule.byName("NOTIFY Template")).toList()
        assertEquals(1, dynamicRules.size)
        var dynamicRule = dynamicRules.first()

        assertEquals("((TOPIC = \"New Topic\") && (TABLE_NAME = \"NOTIFY\") && (TABLE_ENTITY_ID = NOTIFY_ID) && (SENDER = {{USER}}))", dynamicRule.resultExpression)

        sendEvent(
            details = NotificationRuleSubscribe(
                dynamicRuleId = dynamicRule.id,
                parameterDetails = emptyMap()
            ),
            userName = "JaneDoe",
            messageType = "EVENT_NOTIFICATION_RULE_SUBSCRIBE"
        ).assertedCast<EventReply.EventAck>()

        dynamicRules = entityDb.getRange(DynamicRule.byName("NOTIFY Template")).toList()
        assertEquals(2, dynamicRules.size)
        dynamicRules = dynamicRules.filter { !it.isTemplate }.toList()
        assertEquals(1, dynamicRules.size)
        dynamicRule = dynamicRules.first()

        assertEquals("((TOPIC = \"New Topic\") && (TABLE_NAME = \"NOTIFY\") && (TABLE_ENTITY_ID = NOTIFY_ID) && (SENDER = \"JaneDoe\"))", dynamicRule.resultExpression)
    }
}
