package global.genesis.notify

import global.genesis.commons.model.GenesisSet.Companion.genesisSet
import global.genesis.commons.standards.MessageType.MESSAGE_TYPE
import global.genesis.gen.dao.Notify
import global.genesis.gen.dao.enums.NotifySeverity
import global.genesis.message.core.event.EventReply
import global.genesis.notify.message.event.NotifyInsert
import global.genesis.testsupport.AbstractGenesisTestSupport
import global.genesis.testsupport.EventResponse
import global.genesis.testsupport.GenesisTestConfig
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class NotifyInsertTest : AbstractGenesisTestSupport<EventResponse>(
    GenesisTestConfig {
        packageNames = listOf(
            "global.genesis.eventhandler.pal",
            "global.genesis.notify",
            "global.genesis.file.storage.provider"
        ).toMutableList()
        genesisHome = "/GenesisHome/"
        initialDataFile = "notify-data.csv"
        scriptFileName = "genesis-notify-eventhandler.kts,another-notify.kts"
        useTempClassloader = true
        parser = EventResponse
    }
) {

    override fun systemDefinition(): Map<String, Any> {
        return mutableMapOf(
            "DOCUMENT_STORE_BASEDIR" to "src/test/resources/GenesisHome/incoming",
            "SYMPHONY_ENABLED_FOR_TESTING" to "true",
            "LOCAL_STORAGE_FOLDER" to "LOCAL_STORAGE",
            "STORAGE_STRATEGY" to "LOCAL",
            "IS_SCRIPT" to "true"
        )
    }

    private val notifyWithDocId = Notify {
        this.topic = "deals"
        this.header = "Genesis Confirmation Service"
        this.sender = "JohnDoe"
        this.body =
            "<br/>Please find enclosed your confirmation statements containing 29 trades on date 3rd November 2020.<br/>Best regards<br/>Genesis Operations"
        this.notifySeverity = NotifySeverity.Information
        this.documentId = "00001"
    }

    private val notifyNoDocId = Notify {
        this.topic = "deals"
        this.header = "Genesis Confirmation Service"
        this.sender = "JohnDoe"
        this.body =
            "<br/>Please find enclosed your confirmation statements containing 29 trades on date 3rd November 2020.<br/>Best regards<br/>Genesis Operations"
        this.notifySeverity = NotifySeverity.Information
    }

    @Test
    fun `test insert notify with integer table entity ID`() = runBlocking {
        val message = genesisSet {
            MESSAGE_TYPE with "EVENT_NOTIFY_INSERT"
            "USER_NAME" with "JohnDoe"
            "DETAILS" with genesisSet {
                "TOPIC" with "DEALS"
                "HEADER" with "Genesis Confirmation Service"
                "SENDER" with "JohnDoe"
                "BODY" with "<br/>Please find enclosed your confirmation statements containing 29 trades on date 3rd November 2020.<br/>Best regards<br/>Genesis Operations"
                "NOTIFY_SEVERITY" with "Information"
                "TABLE_ENTITY_ID" with 1
            }
        }

        val response = messageClient.suspendRequest(message)
        assertEquals("EVENT_ACK", response!!.getString("MESSAGE_TYPE"))
    }

    @Test
    fun `can send email with document id`(): Unit = runBlocking {
        sendEvent(notifyWithDocId, userName = "JohnDoe", messageType = "EVENT_NOTIFY_INSERT").assertedCast<EventReply.EventAck>()
    }

    @Test
    fun `cannot send email as another user`(): Unit = runBlocking {
        sendEvent(
            notifyWithDocId,
            userName = "JaneDee",
            messageType = "EVENT_NOTIFY_INSERT"
        ).assertedCast<EventReply.EventNack>()
    }

    @Test
    fun `can send email with multiple attachments`(): Unit = runBlocking {
        val notIns = NotifyInsert(setOf("00001", "00002"))
        notIns.notify = notifyNoDocId

        sendEvent(
            notIns,
            userName = "JohnDoe",
            messageType = "EVENT_NOTIFY_INSERT"
        ).assertedCast<EventReply.EventAck>()
    }
}
