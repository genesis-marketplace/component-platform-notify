package global.genesis.notify.request

import global.genesis.commons.model.GenesisSet
import global.genesis.criteria.model.Assignment
import global.genesis.criteria.model.Condition
import global.genesis.criteria.model.Expression
import global.genesis.criteria.model.Operation
import global.genesis.criteria.model.ResultExpression
import global.genesis.criteria.model.RuleExpression
import global.genesis.gen.dao.DynamicRule
import global.genesis.gen.dao.NotifyRoute
import global.genesis.gen.dao.enums.ParamSourceType
import global.genesis.gen.dao.enums.ParamType
import global.genesis.gen.dao.enums.RuleExecutionStrategy
import global.genesis.message.core.request.Request
import global.genesis.message.core.workflow.message.RequestReplyWorkflow
import global.genesis.message.core.workflow.message.requestReplyWorkflowBuilder
import global.genesis.notify.message.request.NotificationRuleTemplateReply
import global.genesis.notify.message.request.NotifyRouteTopicsReply
import global.genesis.testsupport.AbstractGenesisTestSupport
import global.genesis.testsupport.GenesisTestConfig
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import kotlin.test.assertNotNull

class NotificationRuleTemplateRequestReplyTest : AbstractGenesisTestSupport<GenesisSet>(
    GenesisTestConfig {
        packageName = "global.genesis.requestreply.pal"
        genesisHome = "/GenesisHome/"
        initialDataFile = "notification-rule-template-data.csv"
        scriptFileName = "genesis-notify-rules-reqrep.kts"
        parser = { it }
        useTempClassloader = true
    }
) {

    object NotificationRuleTemplateFlow : RequestReplyWorkflow<DynamicRule.ById, DynamicRule> by requestReplyWorkflowBuilder("NOTIFICATION_RULE_TEMPLATE")
    object NotificationRuleTemplateDetailsFlow : RequestReplyWorkflow<DynamicRule.ById, NotificationRuleTemplateReply> by requestReplyWorkflowBuilder("NOTIFICATION_RULE_TEMPLATE_DETAILS")
    object NotificationRuleFlow : RequestReplyWorkflow<DynamicRule.ById, DynamicRule> by requestReplyWorkflowBuilder("NOTIFICATION_RULE")
    object NotifyRouteFlow : RequestReplyWorkflow<NotifyRoute.ById, NotifyRouteTopicsReply> by requestReplyWorkflowBuilder("NOTIFY_ROUTE_TOPICS")

    @Test
    fun `NOTIFICATION_RULE_TEMPLATE_DETAILS should reply with all related details for a dynamic rule id`(): Unit = runBlocking {
        val req = Request(
            request = DynamicRule.byId("DR-1234"),
            messageType = "NOTIFICATION_RULE_TEMPLATE_DETAILS",
            userName = "JohnDoe"
        )

        val expectedRuleExpression = RuleExpression(
            listOf(
                Condition(
                    Expression.SingleExpression.Field("QUANTITY"),
                    Operation.GREATER_THAN,
                    Expression.SingleExpression.NumericValue(BigDecimal.valueOf(1000))
                )
            )
        )

        val expectedResultExpression = ResultExpression(
            listOf(
                Assignment(
                    Expression.SingleExpression.Field("TOPIC"),
                    Expression.SingleExpression.StringValue("Topic")
                )
            )
        )

        val reply = sendRequest(NotificationRuleTemplateDetailsFlow, req)
        assertEquals(1, reply.size)
        val ruleTemplateReply = reply[0]

        assertEquals("name-update", ruleTemplateReply.ruleName)
        assertEquals("description-update", ruleTemplateReply.ruleDescription)
        assertEquals("table_name-update", ruleTemplateReply.ruleTable)
        assertEquals(RuleExecutionStrategy.UNLIMITED, ruleTemplateReply.ruleExecutionStrategy)
        assertEquals(expectedRuleExpression, ruleTemplateReply.ruleExpression)
        assertEquals(expectedResultExpression, ruleTemplateReply.resultExpression)

        val parameter1 = ruleTemplateReply.parameters["parameter_name-update"]
        assertNotNull(parameter1)
        assertEquals("REQ_TEST_UPDATE", parameter1.paramSource)
        assertEquals(ParamSourceType.REQ_REP, parameter1.paramSourceType)
        assertEquals(ParamType.STRING, parameter1.paramType)
        assertEquals("PARAM_KEY_UPDATE", parameter1.paramLabel)
        assertEquals(">", parameter1.paramOperator)

        val parameter2 = ruleTemplateReply.parameters["parameter_name-update-two"]
        assertNotNull(parameter2)
        assertEquals("REQ_TEST_UPDATE", parameter2.paramSource)
        assertEquals(ParamSourceType.REQ_REP, parameter2.paramSourceType)
        assertEquals(ParamType.STRING, parameter2.paramType)
        assertEquals("PARAM_KEY_UPDATE", parameter2.paramLabel)
        assertEquals(">", parameter2.paramOperator)
    }

    @Test
    fun `NOTIFICATION_RULE_TEMPLATE_DETAILS should fail when requested without permission`(): Unit = runBlocking {
        val req = Request(
            request = DynamicRule.byId("DR-1234"),
            messageType = "NOTIFICATION_RULE_TEMPLATE_DETAILS",
            userName = "JohnyDoe"
        )

        val exception: Exception = assertThrows<IllegalArgumentException> {
            sendRequest(NotificationRuleTemplateDetailsFlow, req)
        }

        exception.message?.let { assertTrue(it.contains("User JohnyDoe lacks sufficient permissions")) }
    }

    @Test
    fun `NOTIFICATION_RULE_TEMPLATE should return all dynamic rules which are templates and type NOTIFY`(): Unit = runBlocking {
        val req = Request(
            request = DynamicRule.byId("*"),
            messageType = "NOTIFICATION_RULE_TEMPLATE",
            userName = "JohnDoe"
        )

        val reply = sendRequest(NotificationRuleTemplateFlow, req)

        // db also contains a record in DYNAMIC_RULE with RULE_TYPE="NOT_NOTIFY" and another with IS_TEMPLATE=false
        assertEquals(2, reply.size)
        val ruleTemplateReply = reply.filter { it.name == "name-update" }.first()

        assertEquals("name-update", ruleTemplateReply.name)
        assertEquals("description-update", ruleTemplateReply.description)
        assertEquals("table_name-update", ruleTemplateReply.ruleTable)
        assertEquals(RuleExecutionStrategy.UNLIMITED, ruleTemplateReply.ruleExecutionStrategy)
        assertEquals("((QUANTITY > 1000))", ruleTemplateReply.ruleExpression)
        assertEquals("((TOPIC = \"Topic\"))", ruleTemplateReply.resultExpression)
    }

    @Test
    fun `NOTIFICATION_RULE_TEMPLATE should fail when requested without permission`(): Unit = runBlocking {
        val req = Request(
            request = DynamicRule.byId("*"),
            messageType = "NOTIFICATION_RULE_TEMPLATE",
            userName = "JohnyDoe"
        )

        val exception: Exception = assertThrows<IllegalArgumentException> {
            sendRequest(NotificationRuleTemplateDetailsFlow, req)
        }

        exception.message?.let { assertTrue(it.contains("User JohnyDoe lacks sufficient permissions")) }
    }

    @Test
    fun `NOTIFICATION_RULE should return all dynamic rules which are NOT templates and type NOTIFY and for that user`(): Unit = runBlocking {
        val req = Request(
            request = DynamicRule.byId("*"),
            messageType = "NOTIFICATION_RULE",
            userName = "JohnDoe"
        )

        val reply = sendRequest(NotificationRuleFlow, req)

        // db also contains 3 records not matching the criteria
        assertEquals(1, reply.size)
        val ruleTemplateReply = reply[0]
        println(reply)

        assertEquals("name_custom", ruleTemplateReply.name)
        assertEquals("description_custom", ruleTemplateReply.description)
        assertEquals("table_name_custom", ruleTemplateReply.ruleTable)
        assertEquals(RuleExecutionStrategy.UNLIMITED, ruleTemplateReply.ruleExecutionStrategy)
        assertEquals("((COST > 400))", ruleTemplateReply.ruleExpression)
        assertEquals("((TOPIC = \"Topic\"))", ruleTemplateReply.resultExpression)
    }

    @Test
    fun `NOTIFICATION_RULE should fail when requested without permission`(): Unit = runBlocking {
        val req = Request(
            request = DynamicRule.byId("*"),
            messageType = "NOTIFICATION_RULE",
            userName = "JohnyDoe"
        )

        val exception: Exception = assertThrows<IllegalArgumentException> {
            sendRequest(NotificationRuleFlow, req)
        }

        exception.message?.let { assertTrue(it.contains("User JohnyDoe lacks sufficient permissions")) }
    }

    @Test
    fun `NOTIFY_ROUTE_TOPICS should return all topics`(): Unit = runBlocking {
        val req = Request(
            request = NotifyRoute.byId("*"),
            messageType = "NOTIFY_ROUTE_TOPICS",
            userName = "JohnDoe"
        )

        val reply = sendRequest(NotifyRouteFlow, req)
        assertTrue(reply[0].topics.containsAll(listOf("topic1", "topic2", "topic3")))
    }

    @Test
    fun `NOTIFY_ROUTE_TOPICS should fail when requested without permission`(): Unit = runBlocking {
        val req = Request(
            request = NotifyRoute.byId("*"),
            messageType = "NOTIFY_ROUTE_TOPICS",
            userName = "JaneDoe"
        )

        val exception: Exception = assertThrows<IllegalArgumentException> {
            sendRequest(NotifyRouteFlow, req)
        }

        exception.message?.let { assertTrue(it.contains("User JaneDoe lacks sufficient permissions")) }
    }
}
