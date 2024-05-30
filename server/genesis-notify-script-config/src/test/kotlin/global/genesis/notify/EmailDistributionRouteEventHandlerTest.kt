package global.genesis.notify

import global.genesis.commons.model.GenesisSet
import global.genesis.gen.view.entity.EmailDistributionRoute
import global.genesis.message.core.event.EventReply
import global.genesis.notify.message.event.EmailDistributionRouteCreate
import global.genesis.notify.message.event.EmailDistributionRouteUpdate
import global.genesis.testsupport.AbstractGenesisTestSupport
import global.genesis.testsupport.GenesisTestConfig
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EmailDistributionRouteEventHandlerTest : AbstractGenesisTestSupport<GenesisSet>(
    GenesisTestConfig {
        packageName = "global.genesis.eventhandler.pal"
        genesisHome = "/GenesisHome/"
        initialDataFile = "notify-data.csv"
        scriptFileName = "genesis-notify-eventhandler.kts"
        parser = { it }
        useTempClassloader = true
    }
) {
    override fun systemDefinition(): Map<String, Any> = mapOf(
        "IS_SCRIPT" to "true",
        "LOCAL_STORAGE_FOLDER" to "LOCAL_STORAGE",
        "STORAGE_STRATEGY" to "LOCAL"
    )

    @Test
    fun `test create email distribution route - with permission`(): Unit = runBlocking {
        insertRoute().assertedCast<EventReply.EventAck>()
    }

    @Test
    fun `test update email distribution route - with permission`(): Unit = runBlocking {
        insertRoute().assertedCast<EventReply.EventAck>()
        val list = getRoutes()
        assertEquals(1, list.size)

        sendEvent(
            EmailDistributionRouteUpdate(
                notifyRouteId = list[0].notifyRouteId,
                gatewayId = "gateway2",
                topicMatch = "EMAIL",
                emailTo = setOf("John.Doe@genesis.global"),
                emailCc = setOf("Jane.Doe@genesis.global"),
                emailBcc = setOf("Jamie.Doe@genesis.global")
            ),
            userName = "JohnDoe",
            messageType = "EVENT_EMAIL_DISTRIBUTION_ROUTE_UPDATE"
        ).assertedCast<EventReply.EventAck>()

        assertEquals("gateway2", entityDb.get(EmailDistributionRoute.ById(list[0].notifyRouteId))!!.gatewayId)
    }

    @Test
    fun `test delete email distribution route - with permission`(): Unit = runBlocking {
        insertRoute()
        val list = getRoutes()
        assertEquals(1, list.size)

        sendEvent(
            details = EmailDistributionRoute.ById(list[0].notifyRouteId),
            userName = "JohnDoe",
            messageType = "EVENT_EMAIL_DISTRIBUTION_ROUTE_DELETE"
        ).assertedCast<EventReply.EventAck>()
    }

    @Test
    fun `test create email distribution route - without permission`(): Unit = runBlocking {
        sendEvent(
            EmailDistributionRouteCreate(
                gatewayId = "gateway1",
                topicMatch = "EMAIL",
                emailTo = setOf("John.Doe@genesis.global"),
                emailCc = setOf("Jane.Doe@genesis.global"),
                emailBcc = setOf("Jamie.Doe@genesis.global")
            ),
            userName = "JaneDee",
            messageType = "EVENT_EMAIL_DISTRIBUTION_ROUTE_CREATE"
        ).assertedCast<EventReply.EventNack>()
    }

    @Test
    fun `test update email distribution route - without permission`(): Unit = runBlocking {
        sendEvent(
            EmailDistributionRouteUpdate(
                notifyRouteId = "1",
                gatewayId = "gateway1",
                topicMatch = "EMAIL",
                emailTo = setOf("John.Doe@genesis.global"),
                emailCc = setOf("Jane.Doe@genesis.global"),
                emailBcc = setOf("Jamie.Doe@genesis.global")
            ),
            userName = "JaneDee",
            messageType = "EVENT_EMAIL_DISTRIBUTION_ROUTE_UPDATE"
        ).assertedCast<EventReply.EventNack>()
    }

    @Test
    fun `test delete email distribution route - without permission`(): Unit = runBlocking {
        sendEvent(
            EmailDistributionRoute.ById("1"),
            userName = "JaneDee",
            messageType = "EVENT_EMAIL_DISTRIBUTION_ROUTE_DELETE"
        ).assertedCast<EventReply.EventNack>()
    }

    private suspend fun EmailDistributionRouteEventHandlerTest.insertRoute() = sendEvent(
        EmailDistributionRouteCreate(
            gatewayId = "gateway1",
            topicMatch = "EMAIL",
            emailTo = setOf("John.Doe@genesis.global"),
            emailCc = setOf("Jane.Doe@genesis.global"),
            emailBcc = setOf("Jamie.Doe@genesis.global")
        ),
        userName = "JohnDoe",
        messageType = "EVENT_EMAIL_DISTRIBUTION_ROUTE_CREATE"
    )

    private suspend fun EmailDistributionRouteEventHandlerTest.getRoutes() = entityDb.getBulk<EmailDistributionRoute>().toList()
}
