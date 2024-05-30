package global.genesis.notify

import global.genesis.commons.model.GenesisSet
import global.genesis.gen.dao.enums.EntityIdType
import global.genesis.gen.view.entity.EmailUserRoute
import global.genesis.message.core.event.EventReply
import global.genesis.notify.message.event.EmailUserRouteCreate
import global.genesis.testsupport.AbstractGenesisTestSupport
import global.genesis.testsupport.GenesisTestConfig
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EmailUserRouteEventHandlerTest : AbstractGenesisTestSupport<GenesisSet>(
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
    fun `test create email user route - with permission`(): Unit = runBlocking {
        insertRoute().assertedCast<EventReply.EventAck>()
    }

    @Test
    fun `test update email user route - with permission`(): Unit = runBlocking {
        val list = getRoutes()
        assertEquals(1, list.size)

        sendEvent(
            EmailUserRoute(
                notifyRouteId = list[0].notifyRouteId,
                gatewayId = "gateway1",
                topicMatch = "EMAIL",
                entityId = "JaneDee",
                entityIdType = EntityIdType.USER_NAME,
                excludeSender = false
            ),
            userName = "JohnDoe",
            messageType = "EVENT_EMAIL_USER_ROUTE_UPDATE"
        ).assertedCast<EventReply.EventAck>()

        assertEquals("JaneDee", entityDb.get(EmailUserRoute.ById(list[0].notifyRouteId))!!.entityId)
    }

    @Test
    fun `test delete email user route - with permission`(): Unit = runBlocking {
        val list = getRoutes()
        assertEquals(1, list.size)

        sendEvent(
            details = EmailUserRoute.ById(list[0].notifyRouteId),
            userName = "JohnDoe",
            messageType = "EVENT_EMAIL_USER_ROUTE_DELETE"
        ).assertedCast<EventReply.EventAck>()
    }

    @Test
    fun `test create email user route - without permission`(): Unit = runBlocking {
        sendEvent(
            EmailUserRouteCreate(
                gatewayId = "gateway1",
                topicMatch = "EMAIL",
                entityId = "JohnDoe",
                entityIdType = EntityIdType.USER_NAME
            ),
            userName = "JaneDee",
            messageType = "EVENT_EMAIL_USER_ROUTE_CREATE"
        ).assertedCast<EventReply.EventNack>()
    }

    @Test
    fun `test update email user route - without permission`(): Unit = runBlocking {
        sendEvent(
            EmailUserRoute(
                notifyRouteId = "1",
                gatewayId = "gateway1",
                topicMatch = "EMAIL",
                entityId = "JaneDee",
                entityIdType = EntityIdType.USER_NAME,
                excludeSender = false
            ),
            userName = "JaneDee",
            messageType = "EVENT_EMAIL_USER_ROUTE_UPDATE"
        ).assertedCast<EventReply.EventNack>()
    }

    @Test
    fun `test delete email user route - without permission`(): Unit = runBlocking {
        sendEvent(
            EmailUserRoute.ById("1"),
            userName = "JaneDee",
            messageType = "EVENT_EMAIL_USER_ROUTE_DELETE"
        ).assertedCast<EventReply.EventNack>()
    }

    private suspend fun EmailUserRouteEventHandlerTest.insertRoute() = sendEvent(
        EmailUserRouteCreate(
            gatewayId = "gateway1",
            topicMatch = "EMAIL",
            entityId = "JohnDoe",
            entityIdType = EntityIdType.USER_NAME
        ),
        userName = "JohnDoe",
        messageType = "EVENT_EMAIL_USER_ROUTE_CREATE"
    )

    private suspend fun EmailUserRouteEventHandlerTest.getRoutes() = entityDb.getBulk<EmailUserRoute>().toList()
}
