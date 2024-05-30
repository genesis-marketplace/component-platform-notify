package global.genesis.notify

import global.genesis.commons.model.GenesisSet
import global.genesis.gen.view.entity.MsTeamsRoute
import global.genesis.message.core.event.EventReply
import global.genesis.notify.message.event.MsTeamsRouteCreate
import global.genesis.testsupport.AbstractGenesisTestSupport
import global.genesis.testsupport.GenesisTestConfig
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MSTeamsRouteEventHandlerTest : AbstractGenesisTestSupport<GenesisSet>(
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
    fun `test create ms teams route - with permission`(): Unit = runBlocking {
        insertRoute().assertedCast<EventReply.EventAck>()
    }

    @Test
    fun `test update ms teams route - with permission`(): Unit = runBlocking {
        insertRoute().assertedCast<EventReply.EventAck>()
        val list = getRoutes()
        assertEquals(1, list.size)

        sendEvent(
            MsTeamsRoute(
                notifyRouteId = list[0].notifyRouteId,
                gatewayId = "gateway2",
                topicMatch = "LOG",
                url = "http://ms-teams/test"
            ),
            userName = "JohnDoe",
            messageType = "EVENT_MS_TEAMS_ROUTE_UPDATE"
        ).assertedCast<EventReply.EventAck>()

        assertEquals("gateway2", entityDb.get(MsTeamsRoute.ById(list[0].notifyRouteId))!!.gatewayId)
    }

    @Test
    fun `test delete ms teams route - with permission`(): Unit = runBlocking {
        insertRoute()
        val list = getRoutes()
        assertEquals(1, list.size)

        sendEvent(
            details = MsTeamsRoute.ById(list[0].notifyRouteId),
            userName = "JohnDoe",
            messageType = "EVENT_MS_TEAMS_ROUTE_DELETE"
        ).assertedCast<EventReply.EventAck>()
    }

    @Test
    fun `test create ms teams route - without permission`(): Unit = runBlocking {
        sendEvent(
            MsTeamsRouteCreate(
                gatewayId = "gateway1",
                topicMatch = "LOG",
                url = "http://ms-teams/test"
            ),
            userName = "JaneDee",
            messageType = "EVENT_MS_TEAMS_ROUTE_CREATE"
        ).assertedCast<EventReply.EventNack>()
    }

    @Test
    fun `test update ms teams route - without permission`(): Unit = runBlocking {
        sendEvent(
            MsTeamsRoute(
                notifyRouteId = "1",
                gatewayId = "gateway1",
                topicMatch = "LOG",
                url = "http://ms-teams/test"
            ),
            userName = "JaneDee",
            messageType = "EVENT_MS_TEAMS_ROUTE_UPDATE"
        ).assertedCast<EventReply.EventNack>()
    }

    @Test
    fun `test delete ms teams route - without permission`(): Unit = runBlocking {
        sendEvent(
            MsTeamsRoute.ById("1"),
            userName = "JaneDee",
            messageType = "EVENT_MS_TEAMS_ROUTE_DELETE"
        ).assertedCast<EventReply.EventNack>()
    }

    private suspend fun MSTeamsRouteEventHandlerTest.insertRoute() = sendEvent(
        MsTeamsRouteCreate(
            gatewayId = "gateway1",
            topicMatch = "LOG",
            url = "http://ms-teams/test"
        ),
        userName = "JohnDoe",
        messageType = "EVENT_MS_TEAMS_ROUTE_CREATE"
    )

    private suspend fun MSTeamsRouteEventHandlerTest.getRoutes() = entityDb.getBulk<MsTeamsRoute>().toList()
}
