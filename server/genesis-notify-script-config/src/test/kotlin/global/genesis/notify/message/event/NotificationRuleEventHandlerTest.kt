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
import global.genesis.gen.dao.enums.RuleExecutionStrategy
import global.genesis.gen.dao.enums.RuleStatus
import global.genesis.message.core.event.EventReply
import global.genesis.testsupport.AbstractGenesisTestSupport
import global.genesis.testsupport.GenesisTestConfig
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NotificationRuleEventHandlerTest : AbstractGenesisTestSupport<GenesisSet>(
    GenesisTestConfig {
        packageName = "global.genesis.eventhandler.pal"
        genesisHome = "/GenesisHome/"
        initialDataFile = "notification-rule-data.csv"
        scriptFileName = "genesis-notify-rules-eventhandler.kts"
        parser = { it }
        useTempClassloader = true
    }
) {

    override fun systemDefinition(): Map<String, Any> = mapOf(
        "IS_SCRIPT" to "true"
    )

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

    @Test
    fun `test create notification rule - with permission`(): Unit = runBlocking {
        sendEvent(
            NotificationRuleInsert(
                ruleName = "name-new",
                ruleDescription = "description-new",
                ruleTable = "table_name-new",
                ruleExpression = ruleExpression,
                resultExpression = resultExpression
            ),
            userName = "JohnDoe",
            messageType = "EVENT_NOTIFICATION_RULE_CREATE"
        ).assertedCast<EventReply.EventAck>()

        val dynamicRules = entityDb.getRange(DynamicRule.byName("name-new")).toList()
        assertEquals(1, dynamicRules.size)

        val dynamicRule = dynamicRules[0]
        assertEquals("name-new", dynamicRule.name)
        assertEquals("description-new", dynamicRule.description)
        assertEquals("table_name-new", dynamicRule.ruleTable)
        assertEquals(RuleExecutionStrategy.UNLIMITED, dynamicRule.ruleExecutionStrategy)
        assertEquals("INSERT", dynamicRule.tableOperation)
        assertEquals("((QUANTITY > 500) && (ACCOUNT == \"TEST_ACCOUNT\") && (PNL < (COST + FEES)))", dynamicRule.ruleExpression)
        assertEquals("((TOPIC = \"New Topic\") && (TABLE_NAME = \"table_name-new\") && (SENDER = \"JohnDoe\"))", dynamicRule.resultExpression)
    }

    @Test
    fun `create notification rule without permission should fail`(): Unit = runBlocking {
        val result = sendEvent(
            NotificationRuleInsert(
                ruleName = "name-new",
                ruleDescription = "description-new",
                ruleTable = "table_name-new",
                ruleExpression = ruleExpression,
                resultExpression = resultExpression
            ),
            userName = "JanieDoe",
            messageType = "EVENT_NOTIFICATION_RULE_CREATE"
        ).assertedCast<EventReply.EventNack>()

        Assertions.assertTrue(result.error[0].text == "User JanieDoe lacks sufficient permissions")
    }

    @Test
    fun `test update notification rule - with permission`(): Unit = runBlocking {
        val dynamicRule = entityDb.get(DynamicRule.byId("DR-1234"))
        assertNotNull(dynamicRule)

        sendEvent(
            NotificationRuleUpdate(
                dynamicRuleId = "DR-1234",
                ruleName = "name-new",
                ruleDescription = "description-new",
                ruleTable = "table_name-new",
                ruleExpression = ruleExpression,
                resultExpression = resultExpression,
                tableOperations = setOf(UpdateType.INSERT, UpdateType.MODIFY)
            ),
            userName = "JohnDoe",
            messageType = "EVENT_NOTIFICATION_RULE_UPDATE"
        ).assertedCast<EventReply.EventAck>()

        val dynamicRuleNew = entityDb.get(DynamicRule.byId(dynamicRule.id))
        assertNotNull(dynamicRuleNew)

        assertEquals("name-new", dynamicRuleNew.name)
        assertEquals("description-new", dynamicRuleNew.description)
        assertEquals("table_name-new", dynamicRuleNew.ruleTable)
        assertEquals(RuleExecutionStrategy.UNLIMITED, dynamicRuleNew.ruleExecutionStrategy)
        assertTrue(dynamicRuleNew.tableOperation == "INSERT|MODIFY" || dynamicRuleNew.tableOperation == "MODIFY|INSERT")
        assertEquals("((QUANTITY > 500) && (ACCOUNT == \"TEST_ACCOUNT\") && (PNL < (COST + FEES)))", dynamicRuleNew.ruleExpression)
        assertEquals("((TOPIC = \"New Topic\") && (TABLE_NAME = \"table_name-new\") && (SENDER = \"JohnDoe\"))", dynamicRuleNew.resultExpression)
    }

    @Test
    fun `update notification rule without permission should fail`(): Unit = runBlocking {
        val dynamicRule = entityDb.get(DynamicRule.byId("DR-1234"))
        assertNotNull(dynamicRule)

        val result = sendEvent(
            NotificationRuleUpdate(
                dynamicRuleId = "DR-1234",
                ruleName = "name-new",
                ruleDescription = "description-new",
                ruleTable = "table_name-new",
                ruleExpression = ruleExpression,
                resultExpression = resultExpression
            ),
            userName = "JanieDoe",
            messageType = "EVENT_NOTIFICATION_RULE_UPDATE"
        ).assertedCast<EventReply.EventNack>()

        Assertions.assertTrue(result.error[0].text == "User JanieDoe lacks sufficient permissions")
    }

    @Test
    fun `update notification rule that doesn't exist should fail`(): Unit = runBlocking {
        val dynamicRule = entityDb.get(DynamicRule.byId("DR-XXXX"))
        assertNull(dynamicRule)

        val result = sendEvent(
            NotificationRuleUpdate(
                dynamicRuleId = "DR-XXXX",
                ruleName = "name-new",
                ruleDescription = "description-new",
                ruleTable = "table_name-new",
                ruleExpression = ruleExpression,
                resultExpression = resultExpression
            ),
            userName = "JohnDoe",
            messageType = "EVENT_NOTIFICATION_RULE_UPDATE"
        ).assertedCast<EventReply.EventNack>()

        Assertions.assertTrue(result.error[0].text == "Dynamic Rule of ID DR-XXXX doesn't exist!")
    }

    @Test
    fun `super user should be able to update notification rule of another user`(): Unit = runBlocking {
        val dynamicRule = entityDb.get(DynamicRule.byId("DR-1234"))
        assertNotNull(dynamicRule)

        sendEvent(
            NotificationRuleUpdate(
                dynamicRuleId = "DR-1234",
                ruleName = "name-new",
                ruleDescription = "description-new",
                ruleTable = "table_name-new",
                ruleExpression = ruleExpression,
                resultExpression = resultExpression
            ),
            userName = "JohnyDoe",
            messageType = "EVENT_NOTIFICATION_RULE_UPDATE"
        ).assertedCast<EventReply.EventAck>()

        val dynamicRuleNew = entityDb.get(DynamicRule.byId(dynamicRule.id))
        assertNotNull(dynamicRuleNew)

        assertEquals("name-new", dynamicRuleNew.name)
        assertEquals("description-new", dynamicRuleNew.description)
        assertEquals("table_name-new", dynamicRuleNew.ruleTable)
        assertEquals(RuleExecutionStrategy.UNLIMITED, dynamicRuleNew.ruleExecutionStrategy)
        assertEquals("((QUANTITY > 500) && (ACCOUNT == \"TEST_ACCOUNT\") && (PNL < (COST + FEES)))", dynamicRuleNew.ruleExpression)
        assertEquals("((TOPIC = \"New Topic\") && (TABLE_NAME = \"table_name-new\") && (SENDER = \"JohnyDoe\"))", dynamicRuleNew.resultExpression)
    }

    @Test
    fun `non super user should not be able to update notification rule of another user`(): Unit = runBlocking {
        val dynamicRule = entityDb.get(DynamicRule.byId("DR-1234"))
        assertNotNull(dynamicRule)

        val result = sendEvent(
            NotificationRuleUpdate(
                dynamicRuleId = "DR-1234",
                ruleName = "name-new",
                ruleDescription = "description-new",
                ruleTable = "table_name-new",
                ruleExpression = ruleExpression,
                resultExpression = ResultExpression(emptyList())
            ),
            userName = "JanieDoe",
            messageType = "EVENT_NOTIFICATION_RULE_UPDATE"
        ).assertedCast<EventReply.EventNack>()

        Assertions.assertTrue(result.error[0].text == "User JanieDoe lacks sufficient permissions")

        val dynamicRuleNew = entityDb.get(DynamicRule.byId(dynamicRule.id))
        assertNotNull(dynamicRuleNew)

        assertEquals("name-update", dynamicRuleNew.name)
        assertEquals("description-update", dynamicRuleNew.description)
        assertEquals("table_name-update", dynamicRuleNew.ruleTable)
        assertEquals(RuleExecutionStrategy.UNLIMITED, dynamicRuleNew.ruleExecutionStrategy)
        assertEquals("((QUANTITY > 1000))", dynamicRuleNew.ruleExpression)
        assertEquals("((TOPIC = \"Topic\"))", dynamicRuleNew.resultExpression)
    }

    @Test
    fun `test delete notification rule - with permission`(): Unit = runBlocking {
        val dynamicRule = entityDb.get(DynamicRule.byId("DR-1234"))
        assertNotNull(dynamicRule)

        sendEvent(
            DynamicRule.byId("DR-1234"),
            userName = "JohnDoe",
            messageType = "EVENT_NOTIFICATION_RULE_DELETE"
        ).assertedCast<EventReply.EventAck>()

        Assertions.assertNull(entityDb.get(DynamicRule.byId("DR-1234")))
    }

    @Test
    fun `delete notification rule without permission should fail`(): Unit = runBlocking {
        val dynamicRule = entityDb.get(DynamicRule.byId("DR-1234"))
        assertNotNull(dynamicRule)

        val result = sendEvent(
            DynamicRule.byId("DR-1234"),
            userName = "JanieDoe",
            messageType = "EVENT_NOTIFICATION_RULE_DELETE"
        ).assertedCast<EventReply.EventNack>()

        Assertions.assertTrue(result.error[0].text == "User JanieDoe lacks sufficient permissions")
    }

    @Test
    fun `delete notification rule that doesn't exist should fail`(): Unit = runBlocking {
        val dynamicRule = entityDb.get(DynamicRule.byId("DR-XXXX"))
        assertNull(dynamicRule)

        val result = sendEvent(
            DynamicRule.byId("DR-XXXX"),
            userName = "JohnDoe",
            messageType = "EVENT_NOTIFICATION_RULE_DELETE"
        ).assertedCast<EventReply.EventNack>()

        Assertions.assertTrue(result.error[0].text == "Dynamic Rule of ID DR-XXXX doesn't exist!")
    }

    @Test
    fun `super user should be able to delete another users notification rule`(): Unit = runBlocking {
        val dynamicRule = entityDb.get(DynamicRule.byId("DR-1234"))
        assertNotNull(dynamicRule)

        sendEvent(
            DynamicRule.byId("DR-1234"),
            userName = "JohnyDoe",
            messageType = "EVENT_NOTIFICATION_RULE_DELETE"
        ).assertedCast<EventReply.EventAck>()

        Assertions.assertNull(entityDb.get(DynamicRule.byId("DR-1234")))
    }

    @Test
    fun `non super user should not be able to delete another users notification rule`(): Unit = runBlocking {
        val dynamicRule = entityDb.get(DynamicRule.byId("DR-1234"))
        assertNotNull(dynamicRule)

        val result = sendEvent(
            DynamicRule.byId("DR-1234"),
            userName = "JaneDoe",
            messageType = "EVENT_NOTIFICATION_RULE_DELETE"
        ).assertedCast<EventReply.EventNack>()

        Assertions.assertTrue(result.error[0].text == "User JaneDoe lacks sufficient permissions")

        assertNotNull(entityDb.get(DynamicRule.byId("DR-1234")))
    }

    @Test
    fun `test disable notification rule - with permission`(): Unit = runBlocking {
        sendEvent(
            DynamicRule.byId("DR-1234"),
            userName = "JohnDoe",
            messageType = "EVENT_NOTIFICATION_RULE_DISABLE"
        ).assertedCast<EventReply.EventAck>()

        val dynamicRule = entityDb.get(DynamicRule.byId("DR-1234"))
        assertNotNull(dynamicRule)
        assertEquals(RuleStatus.DISABLED, dynamicRule.ruleStatus)
    }

    @Test
    fun `test disable notification rule - without permission`(): Unit = runBlocking {
        val result = sendEvent(
            DynamicRule.byId("DR-1234"),
            userName = "JanieDoe",
            messageType = "EVENT_NOTIFICATION_RULE_DISABLE"
        ).assertedCast<EventReply.EventNack>()

        Assertions.assertTrue(result.error[0].text == "User JanieDoe lacks sufficient permissions")

        val dynamicRule = entityDb.get(DynamicRule.byId("DR-1234"))
        assertNotNull(dynamicRule)
        assertEquals(RuleStatus.ENABLED, dynamicRule.ruleStatus)
    }

    @Test
    fun `disabling notification rule of another user should fail - without super permission`(): Unit = runBlocking {
        val result = sendEvent(
            DynamicRule.byId("DR-1234"),
            userName = "JaneDoe",
            messageType = "EVENT_NOTIFICATION_RULE_DISABLE"
        ).assertedCast<EventReply.EventNack>()

        Assertions.assertTrue(result.error[0].text == "User JaneDoe lacks sufficient permissions")

        val dynamicRule = entityDb.get(DynamicRule.byId("DR-1234"))
        assertNotNull(dynamicRule)
        assertEquals(RuleStatus.ENABLED, dynamicRule.ruleStatus)
    }

    @Test
    fun `disabling notification rule of another user should work - with super permission`(): Unit = runBlocking {
        sendEvent(
            DynamicRule.byId("DR-1234"),
            userName = "JohnyDoe",
            messageType = "EVENT_NOTIFICATION_RULE_DISABLE"
        ).assertedCast<EventReply.EventAck>()

        val dynamicRule = entityDb.get(DynamicRule.byId("DR-1234"))
        assertNotNull(dynamicRule)
        assertEquals(RuleStatus.DISABLED, dynamicRule.ruleStatus)
    }

    @Test
    fun `test enable notification rule - with permission`(): Unit = runBlocking {
        sendEvent(
            DynamicRule.byId("DR-1235"),
            userName = "JohnDoe",
            messageType = "EVENT_NOTIFICATION_RULE_ENABLE"
        ).assertedCast<EventReply.EventAck>()

        val dynamicRule = entityDb.get(DynamicRule.byId("DR-1235"))
        assertNotNull(dynamicRule)
        assertEquals(RuleStatus.ENABLED, dynamicRule.ruleStatus)
    }

    @Test
    fun `test enable notification rule - without permission`(): Unit = runBlocking {
        val result = sendEvent(
            DynamicRule.byId("DR-1235"),
            userName = "JanieDoe",
            messageType = "EVENT_NOTIFICATION_RULE_ENABLE"
        ).assertedCast<EventReply.EventNack>()

        Assertions.assertTrue(result.error[0].text == "User JanieDoe lacks sufficient permissions")

        val dynamicRule = entityDb.get(DynamicRule.byId("DR-1235"))
        assertNotNull(dynamicRule)
        assertEquals(RuleStatus.DISABLED, dynamicRule.ruleStatus)
    }

    @Test
    fun `enabling notification rule of another user should fail - without super permission`(): Unit = runBlocking {
        val result = sendEvent(
            DynamicRule.byId("DR-1235"),
            userName = "JaneDoe",
            messageType = "EVENT_NOTIFICATION_RULE_ENABLE"
        ).assertedCast<EventReply.EventNack>()

        Assertions.assertTrue(result.error[0].text == "User JaneDoe lacks sufficient permissions")

        val dynamicRule = entityDb.get(DynamicRule.byId("DR-1235"))
        assertNotNull(dynamicRule)
        assertEquals(RuleStatus.DISABLED, dynamicRule.ruleStatus)
    }

    @Test
    fun `enabling notification rule of another user should work - with super permission`(): Unit = runBlocking {
        sendEvent(
            DynamicRule.byId("DR-1235"),
            userName = "JohnyDoe",
            messageType = "EVENT_NOTIFICATION_RULE_ENABLE"
        ).assertedCast<EventReply.EventAck>()

        val dynamicRule = entityDb.get(DynamicRule.byId("DR-1235"))
        assertNotNull(dynamicRule)
        assertEquals(RuleStatus.ENABLED, dynamicRule.ruleStatus)
    }
}
