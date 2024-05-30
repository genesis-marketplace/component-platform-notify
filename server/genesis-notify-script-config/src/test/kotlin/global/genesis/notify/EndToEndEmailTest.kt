package global.genesis.notify

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetup
import global.genesis.gen.dao.Notify
import global.genesis.gen.dao.enums.RoutingType
import global.genesis.jackson.core.GenesisJacksonMapper.Companion.toJsonString
import global.genesis.notify.message.common.EmailDistribution
import global.genesis.notify.message.event.EmailDirectRoutingData
import global.genesis.testsupport.AbstractGenesisTestSupport
import global.genesis.testsupport.EventResponse
import global.genesis.testsupport.GenesisTestConfig
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.event.Level
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EndToEndEmailTest : AbstractGenesisTestSupport<EventResponse>(
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
        rootLogLevel = Level.DEBUG
    }
) {
    private lateinit var greenMail: GreenMail

    @BeforeEach
    fun setUp() {
        val serverSetup = ServerSetup.SMTP.port(10000)
        serverSetup.serverStartupTimeout = 10000
        greenMail = GreenMail(serverSetup)
        greenMail.setUser("default@genesis.global", "xxxxxxxxxxx")
        greenMail.setUser("JohnDoe@genesis.global", "xxxxxxxxxxx")
        greenMail.setUser("to.address@genesis.global", "xxxxxxxxxxx")
        greenMail.setUser("cc.address@genesis.global", "xxxxxxxxxxx")
        greenMail.setUser("bcc.address@genesis.global", "xxxxxxxxxxx")
        greenMail.start()
    }

    @AfterEach
    override fun tearDown() {
        greenMail.stop()
    }

    override fun systemDefinition(): Map<String, Any> {
        return mutableMapOf(
            "DOCUMENT_STORE_BASEDIR" to "src/test/resources/GenesisHome/incoming",
            "SYMPHONY_ENABLED_FOR_TESTING" to "true",
            "LOCAL_STORAGE_FOLDER" to "LOCAL_STORAGE",
            "STORAGE_STRATEGY" to "LOCAL",
            "IS_SCRIPT" to "true"
        )
    }

    @Test
    fun `email is sent correctly when routed via TOPIC`() = runBlocking<Unit> {
        entityDb.insert(
            Notify {
                sender = "JohnDoe"
                header = "Test Header"
                body = "Test Message"
                routingType = RoutingType.TOPIC
                routingData = "USER_EMAIL"
            }
        )

        greenMail.waitForIncomingEmail(10000L, 1)
        val message = greenMail.receivedMessages[0]
        assertNotNull(message)
        assertTrue(message.getHeader("To").toList().contains("JohnDoe <johndoe@genesis.global>"))
    }

    @Test
    fun `email is sent correctly when routed via TOPIC - legacy API`() = runBlocking {
        entityDb.insert(
            Notify {
                sender = "JohnDoe"
                header = "Test Header"
                body = "Test Message"
                topic = "USER_EMAIL"
            }
        )

        greenMail.waitForIncomingEmail(10000L, 1)
        val message = greenMail.receivedMessages[0]
        assertNotNull(message)
        assertTrue(message.getHeader("To").toList().contains("JohnDoe <johndoe@genesis.global>"))
    }

    @Test
    fun `email is sent correctly when routed with strategy DIRECT`() = runBlocking {
        entityDb.insert(
            Notify {
                sender = "JohnDoe"
                header = "Test Header"
                body = "Test Message"
                routingType = RoutingType.DIRECT
                routingData = EmailDirectRoutingData(
                    gatewayId = "email1",
                    emailDistribution = EmailDistribution(
                        to = listOf("to.address@genesis.global"),
                        cc = listOf("cc.address@genesis.global"),
                        bcc = listOf("bcc.address@genesis.global")
                    ),
                    users = setOf("JohnDoe")
                ).toJsonString()
            }
        )

        greenMail.waitForIncomingEmail(10000L, 2)
        val messages = greenMail.receivedMessages

        var message = messages[0]
        assertNotNull(message)
        assertTrue(message.getHeader("To").toList().contains("JohnDoe <johndoe@genesis.global>"))

        message = messages[1]
        assertNotNull(message)
        assertTrue(message.getHeader("To").toList().contains("<to.address@genesis.global>"))
        assertTrue(message.getHeader("Cc").toList().contains("<cc.address@genesis.global>"))

        /*
         BCC addresses never appear on the message using this library, so to verify we need
         to get the specific SMTP mailbox for the BCC user and check it received the message
         */
        val users = greenMail.managers.userManager.getUser("bcc.address@genesis.global")
        val inbox = greenMail.managers.imapHostManager.getInbox(users)
        message = inbox.messages.first().mimeMessage
        assertNotNull(message)
        assertTrue(message.getHeader("To").toList().contains("<to.address@genesis.global>"))
        assertTrue(message.getHeader("Cc").toList().contains("<cc.address@genesis.global>"))
    }

    @Test
    fun `email is sent correctly when routed using BASIC codes`() = runBlocking {
        entityDb.insert(
            Notify {
                sender = "JohnDoe"
                header = "Test Header"
                body = "Test Message"
                routingType = RoutingType.BASIC
                routingData = "MY_EMAIL"
            }
        )

        greenMail.waitForIncomingEmail(10000L, 1)
        val message = greenMail.receivedMessages[0]
        assertNotNull(message)
        assertTrue(message.getHeader("To").toList().contains("JohnDoe <johndoe@genesis.global>"))
    }
}
