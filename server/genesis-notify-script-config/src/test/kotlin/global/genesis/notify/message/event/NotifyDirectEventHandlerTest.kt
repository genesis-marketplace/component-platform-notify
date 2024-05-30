package global.genesis.notify.message.event

import global.genesis.commons.model.GenesisSet
import global.genesis.gen.dao.Notify
import global.genesis.gen.dao.enums.NotifySeverity
import global.genesis.gen.dao.enums.RoutingType
import global.genesis.jackson.core.GenesisJacksonMapper.Companion.toJsonString
import global.genesis.message.core.event.EventReply
import global.genesis.notify.message.common.EmailDistribution
import global.genesis.testsupport.AbstractGenesisTestSupport
import global.genesis.testsupport.GenesisTestConfig
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class NotifyDirectEventHandlerTest : AbstractGenesisTestSupport<GenesisSet>(
    GenesisTestConfig {
        packageName = "global.genesis.eventhandler.pal"
        genesisHome = "/GenesisHome/"
        scriptFileName = "genesis-notify-eventhandler.kts"
        parser = { it }
        useTempClassloader = true
    }
) {

    override fun systemDefinition(): Map<String, Any> = mapOf(
        "IS_SCRIPT" to "true"
    )

    @Test
    fun `invoking NOTIFY_EMAIL_DIRECT creates correct db record`() = runBlocking {
        val emailDirectRoutingData = EmailDirectRoutingData(
            gatewayId = "testGateway",
            emailDistribution = EmailDistribution(
                to = listOf("to@genesis.global"),
                cc = listOf("cc@genesis.global"),
                bcc = listOf("bcc@genesis.global")
            )
        )
        sendEvent(
            details = NotifyEmailDirect(
                header = "testHeader",
                body = "testBody",
                notifySeverity = NotifySeverity.Information,
                tableName = "testTable",
                tableEntityId = "testEntityId",
                permissioningEntityId = "testPermsId",
                notifyAttachments = emptySet(),
                routingData = emailDirectRoutingData
            ),
            userName = "JohnDoe",
            messageType = "EVENT_NOTIFY_EMAIL_DIRECT"
        ).assertedCast<EventReply.EventAck>()

        val notifications = entityDb.getBulk<Notify>().toList()
        assertEquals(1, notifications.size)
        val notification = notifications[0]
        assertEquals(notification.notifySeverity, NotifySeverity.Information)
        assertEquals(notification.sender, "JohnDoe")
        assertEquals(notification.header, "testHeader")
        assertEquals(notification.tableName, "testTable")
        assertEquals(notification.tableEntityId, "testEntityId")
        assertEquals(notification.permissioningEntityId, "testPermsId")
        assertEquals(notification.routingType, RoutingType.DIRECT)
        assertEquals(notification.routingData, emailDirectRoutingData.toJsonString())
    }

    @Test
    fun `invoking NOTIFY_SCREEN_DIRECT creates correct db record`() = runBlocking {
        val screenDirectRoutingData = ScreenDirectRoutingData(
            gatewayId = "testGateway",
            users = setOf("JohnDoe", "JaneDoe")
        )
        sendEvent(
            details = NotifyScreenDirect(
                header = "testHeader",
                body = "testBody",
                notifySeverity = NotifySeverity.Information,
                tableName = "testTable",
                tableEntityId = "testEntityId",
                permissioningEntityId = "testPermsId",
                routingData = screenDirectRoutingData
            ),
            userName = "JohnDoe",
            messageType = "EVENT_NOTIFY_SCREEN_DIRECT"
        ).assertedCast<EventReply.EventAck>()

        val notifications = entityDb.getBulk<Notify>().toList()
        assertEquals(1, notifications.size)
        val notification = notifications[0]
        assertEquals(notification.notifySeverity, NotifySeverity.Information)
        assertEquals(notification.sender, "JohnDoe")
        assertEquals(notification.header, "testHeader")
        assertEquals(notification.tableName, "testTable")
        assertEquals(notification.tableEntityId, "testEntityId")
        assertEquals(notification.permissioningEntityId, "testPermsId")
        assertEquals(notification.routingType, RoutingType.DIRECT)
        assertEquals(notification.routingData, screenDirectRoutingData.toJsonString())
    }
}
