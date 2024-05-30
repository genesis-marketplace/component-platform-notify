package global.genesis.notify

import global.genesis.commons.model.GenesisSet
import global.genesis.gen.dao.NotifyAlert
import global.genesis.gen.dao.enums.AlertStatus
import global.genesis.message.core.event.EventReply
import global.genesis.testsupport.AbstractGenesisTestSupport
import global.genesis.testsupport.GenesisTestConfig
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AlertDismissalTest : AbstractGenesisTestSupport<GenesisSet>(
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
    fun `cannot dismiss alert that does not exist`() = runBlocking {
        val eventNack = sendEvent(
            NotifyAlert.ByAlertId("Test1"),
            userName = "JohnDoe",
            messageType = "EVENT_DISMISS_NOTIFY_ALERT"
        ).assertedCast<EventReply.EventNack>()
        assertEquals(1, eventNack.error.size)
    }

    @Test
    fun `cannot dismiss alert targeted at another user`() = runBlocking {
        entityDb.insert(
            NotifyAlert {
                userName = "JaneDoe"
                message = "Test"
                alertId = "TestAlertId1"
            }
        )

        val eventNack = sendEvent(
            NotifyAlert.ByAlertId("TestAlertId1"),
            userName = "JohnDoe",
            messageType = "EVENT_DISMISS_NOTIFY_ALERT"
        ).assertedCast<EventReply.EventNack>()
        assertEquals(1, eventNack.error.size)
    }

    @Test
    fun `alert is successfully dismissed`() = runBlocking {
        entityDb.insert(
            NotifyAlert {
                userName = "JohnDoe"
                message = "Test"
                alertId = "TestAlertId1"
            }
        )
        sendEvent(
            NotifyAlert.ByAlertId("TestAlertId1"),
            userName = "JohnDoe",
            messageType = "EVENT_DISMISS_NOTIFY_ALERT"
        ).assertedCast<EventReply.EventAck>()
        val notifyAlert = entityDb.get(NotifyAlert.byAlertId("TestAlertId1"))
        assertNotNull(notifyAlert)
        assertEquals(AlertStatus.DISMISSED, notifyAlert.alertStatus)
    }
}
