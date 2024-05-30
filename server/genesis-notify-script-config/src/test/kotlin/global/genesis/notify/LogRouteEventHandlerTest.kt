package global.genesis.notify

import global.genesis.commons.model.GenesisSet
import global.genesis.gen.view.entity.LogRoute
import global.genesis.message.core.event.EventReply
import global.genesis.notify.message.event.LogRouteCreate
import global.genesis.testsupport.AbstractGenesisTestSupport
import global.genesis.testsupport.GenesisTestConfig
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LogRouteEventHandlerTest : AbstractGenesisTestSupport<GenesisSet>(
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
    fun `test create log route - with permission`(): Unit = runBlocking {
        insertRoute().assertedCast<EventReply.EventAck>()
    }

    @Test
    fun `test update log route - with permission`(): Unit = runBlocking {
        insertRoute().assertedCast<EventReply.EventAck>()
        val list = getRoutes()
        assertEquals(1, list.size)

        sendEvent(
            LogRoute(
                notifyRouteId = list[0].notifyRouteId,
                gatewayId = "gateway2",
                topicMatch = "LOG"
            ),
            userName = "JohnDoe",
            messageType = "EVENT_LOG_ROUTE_UPDATE"
        ).assertedCast<EventReply.EventAck>()

        assertEquals("gateway2", entityDb.get(LogRoute.ById(list[0].notifyRouteId))!!.gatewayId)
    }

    @Test
    fun `test delete log route - with permission`(): Unit = runBlocking {
        insertRoute()
        val list = getRoutes()
        assertEquals(1, list.size)

        sendEvent(
            details = LogRoute.ById(list[0].notifyRouteId),
            userName = "JohnDoe",
            messageType = "EVENT_LOG_ROUTE_DELETE"
        ).assertedCast<EventReply.EventAck>()
    }

    @Test
    fun `test create log route - without permission`(): Unit = runBlocking {
        sendEvent(
            LogRouteCreate(
                gatewayId = "gateway1",
                topicMatch = "LOG"
            ),
            userName = "JaneDee",
            messageType = "EVENT_LOG_ROUTE_CREATE"
        ).assertedCast<EventReply.EventNack>()
    }

    @Test
    fun `test update log route - without permission`(): Unit = runBlocking {
        sendEvent(
            LogRoute(
                notifyRouteId = "1",
                gatewayId = "gateway1",
                topicMatch = "LOG"
            ),
            userName = "JaneDee",
            messageType = "EVENT_LOG_ROUTE_UPDATE"
        ).assertedCast<EventReply.EventNack>()
    }

    @Test
    fun `test delete log route - without permission`(): Unit = runBlocking {
        sendEvent(
            LogRoute.ById("1"),
            userName = "JaneDee",
            messageType = "EVENT_LOG_ROUTE_DELETE"
        ).assertedCast<EventReply.EventNack>()
    }

    private suspend fun LogRouteEventHandlerTest.insertRoute() = sendEvent(
        LogRouteCreate(
            gatewayId = "gateway1",
            topicMatch = "LOG"
        ),
        userName = "JohnDoe",
        messageType = "EVENT_LOG_ROUTE_CREATE"
    )

    private suspend fun LogRouteEventHandlerTest.getRoutes() = entityDb.getBulk<LogRoute>().toList()
}
