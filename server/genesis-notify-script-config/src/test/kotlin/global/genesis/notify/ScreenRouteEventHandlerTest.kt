package global.genesis.notify

import global.genesis.commons.model.GenesisSet
import global.genesis.gen.dao.enums.EntityIdType
import global.genesis.gen.dao.enums.TtlTimeUnit
import global.genesis.gen.view.entity.ScreenRoute
import global.genesis.message.core.event.EventReply
import global.genesis.notify.message.event.ScreenRouteCreate
import global.genesis.testsupport.AbstractGenesisTestSupport
import global.genesis.testsupport.GenesisTestConfig
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ScreenRouteEventHandlerTest : AbstractGenesisTestSupport<GenesisSet>(
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
    fun `test create screen route - with permission`(): Unit = runBlocking {
        sendEvent(
            ScreenRouteCreate(
                gatewayId = "gateway1",
                topicMatch = "SCREEN",
                entityId = "JohnDoe",
                entityIdType = EntityIdType.USER_NAME
            ),
            userName = "JohnDoe",
            messageType = "EVENT_SCREEN_NOTIFY_ROUTE_CREATE"
        ).assertedCast<EventReply.EventAck>()
    }

    @Test
    fun `test update screen route - with permission`(): Unit = runBlocking {
        sendEvent(
            ScreenRoute(
                notifyRouteId = "1",
                gatewayId = "gateway1",
                topicMatch = "SCREEN",
                entityId = "JaneDee",
                entityIdType = EntityIdType.USER_NAME,
                ttl = 1,
                ttlTimeUnit = TtlTimeUnit.SECONDS,
                excludeSender = false
            ),
            userName = "JohnDoe",
            messageType = "EVENT_SCREEN_NOTIFY_ROUTE_UPDATE"
        ).assertedCast<EventReply.EventAck>()

        assertEquals("JaneDee", entityDb.get(ScreenRoute.ById("1"))!!.entityId)
    }

    @Test
    fun `test delete screen route - with permission`(): Unit = runBlocking {
        sendEvent(
            ScreenRoute.ById("1"),
            userName = "JohnDoe",
            messageType = "EVENT_SCREEN_NOTIFY_ROUTE_DELETE"
        ).assertedCast<EventReply.EventAck>()
    }

    @Test
    fun `test create screen route - without permission`(): Unit = runBlocking {
        sendEvent(
            ScreenRouteCreate(
                gatewayId = "gateway1",
                topicMatch = "SCREEN",
                entityId = "JohnDoe",
                entityIdType = EntityIdType.USER_NAME
            ),
            userName = "JaneDee",
            messageType = "EVENT_SCREEN_NOTIFY_ROUTE_CREATE"
        ).assertedCast<EventReply.EventNack>()
    }

    @Test
    fun `test update screen route - without permission`(): Unit = runBlocking {
        sendEvent(
            ScreenRoute(
                notifyRouteId = "1",
                gatewayId = "gateway1",
                topicMatch = "SCREEN",
                entityId = "JaneDee",
                entityIdType = EntityIdType.USER_NAME,
                ttl = 1,
                ttlTimeUnit = TtlTimeUnit.SECONDS,
                excludeSender = false
            ),
            userName = "JaneDee",
            messageType = "EVENT_SCREEN_NOTIFY_ROUTE_UPDATE"
        ).assertedCast<EventReply.EventNack>()
    }

    @Test
    fun `test delete screen route - without permission`(): Unit = runBlocking {
        sendEvent(
            ScreenRoute.ById("1"),
            userName = "JaneDee",
            messageType = "EVENT_SCREEN_NOTIFY_ROUTE_DELETE"
        ).assertedCast<EventReply.EventNack>()
    }
}
