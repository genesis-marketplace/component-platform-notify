package global.genesis.notify.gateway.email

import global.genesis.file.message.common.FileContentReply
import global.genesis.gen.dao.Notify
import global.genesis.gen.dao.enums.NotifySeverity
import global.genesis.notify.documents.FileStorageClientWrapper
import global.genesis.notify.documents.GeneratedDocument
import global.genesis.notify.message.common.EmailDistribution
import global.genesis.session.UserEmailCache
import global.genesis.testsupport.AbstractGenesisTestSupport
import global.genesis.testsupport.EventResponse
import global.genesis.testsupport.GenesisTestConfig
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private const val NOTIFY = "NOTIFY"
private const val NOTIFY_ID = "NOTIFY_ID"

private const val EMAIL_HEADER = "Email generation service"
private const val EMAIL_BODY = "This is a simple email body."

private const val CRITICAL_NOTIFY_HEADER = "Critical notification"
private const val CRITICAL_NOTIFY_BODY = "This email contains critical information."

private const val TOPIC = "TOPIC"

private const val JANE = "Jane <jane.doe@email.com>"
private const val ALICE = "Alice <alice@email.com>"
private const val BOB = "<bob@email.com>"

private val TEST_FILE_CONTENT = "test file content".toByteArray()

class EmailBuilderTest : AbstractGenesisTestSupport<EventResponse> (
    GenesisTestConfig {
        addPackageName("global.genesis.notify")
        addPackageName("global.genesis.file.storage.provider")
        genesisHome = "/genesisHome/"
        scriptFileName = "another-notify.kts"
        useTempClassloader = true
    }
) {
    private lateinit var emailBuilder: EmailBuilder
    private lateinit var emailCache: UserEmailCache
    private lateinit var config: EmailGatewayConfig
    private lateinit var fileStorageClient: FileStorageClientWrapper

    override fun systemDefinition(): Map<String, Any> = mapOf(
        "IS_SCRIPT" to "true",
        "MESSAGE_CLIENT_PROCESS_NAME" to "GENESIS_ROUTER",
        "DOCUMENT_STORE_BASEDIR" to "src/test/resources/genesisHome/site-specific/incoming",
        "LOCAL_STORAGE_FOLDER" to createTempDirectory("LOCAL_STORAGE_FOLDER"),
        "STORAGE_STRATEGY" to "LOCAL"
    )

    @BeforeEach
    fun setUp() {
        config = EmailGatewayConfig("Email")
        config.systemDefaultEmail = "default@email.com"
        config.systemDefaultUserName = "Default Sender"

        emailCache = mock<UserEmailCache> {}

        fileStorageClient = mock<FileStorageClientWrapper> {
            onBlocking { getFileContents(any()) } doReturn null
            onBlocking { getAllFileContents(any()) } doReturn emptyList()
            onBlocking { generateDocumentContent(any()) } doAnswer {
                val notify = it.arguments[0] as Notify
                GeneratedDocument(notify.body, emptyList())
            }
        }

        emailBuilder = EmailBuilder(
            config,
            emailCache,
            entityDb,
            fileStorageClient
        )
    }

    @Test
    fun `test basic email`() = runBlocking {
        whenever(emailCache.userEmailAddress(JANE)) doReturn "jane.doe@email.com"

        val notify = Notify {
            this.notifyId = NOTIFY_ID
            this.body = EMAIL_BODY
            this.header = EMAIL_HEADER
            this.notifySeverity = NotifySeverity.Information
            this.topic = TOPIC
        }

        val email = emailBuilder.buildEmail(JANE, notify)

        assertTrue(email.recipients.size == 1)
        assertEquals(email.subject, EMAIL_HEADER)
        assertEquals(email.htmlText, EMAIL_BODY)
    }

    @Test
    fun `test basic email with email distribution`() = runBlocking {
        val emailDistribution = EmailDistribution(
            to = listOf(JANE, ALICE),
            cc = listOf(BOB),
            bcc = emptyList()
        )
        whenever(emailCache.userEmailAddress(JANE)) doReturn "jane.doe@email.com"
        whenever(emailCache.userEmailAddress(ALICE)) doReturn "alice@email.com"
        whenever(emailCache.userEmailAddress(BOB)) doReturn "bob@email.com"

        val notify = Notify {
            this.notifyId = NOTIFY_ID
            this.body = EMAIL_BODY
            this.header = EMAIL_HEADER
            this.notifySeverity = NotifySeverity.Information
            this.topic = TOPIC
        }

        val email = emailBuilder.buildEmail(emailDistribution, notify)

        assertTrue(email.recipients.size == 3)
        assertTrue(email.recipients.elementAt(0).address == "jane.doe@email.com")
        assertTrue(email.recipients.elementAt(1).address == "alice@email.com")
        assertTrue(email.recipients.elementAt(2).address == "bob@email.com")

        assertEquals(email.subject, EMAIL_HEADER)
        assertEquals(email.htmlText, EMAIL_BODY)
    }

    @Test
    fun `test basic email with attachments`() = runBlocking {
        whenever(emailCache.userEmailAddress(JANE)) doReturn "jane.doe@email.com"

        whenever(fileStorageClient.getFileContents(eq("valid_id"))) doReturn FileContentReply(
            fileStorageId = "valid_id",
            fileName = "notify-data.csv",
            fileContent = TEST_FILE_CONTENT
        )

        val notify = Notify {
            this.notifyId = NOTIFY_ID
            this.body = EMAIL_BODY
            this.header = EMAIL_HEADER
            this.documentId = "valid_id"
            this.notifySeverity = NotifySeverity.Information
            this.topic = TOPIC
        }

        val email = emailBuilder.buildEmail(JANE, notify)

        assertTrue(email.recipients.size == 1)
        assertEquals(email.subject, EMAIL_HEADER)
        assertEquals(email.htmlText, EMAIL_BODY)
        assertEquals(email.attachments.size, 1)
        assertEquals(email.attachments[0].name, "notify-data.csv")
    }

    @Test
    fun `test basic email - send without attachment if document ID is not found`() = runBlocking {
        whenever(emailCache.userEmailAddress(JANE)) doReturn "jane.doe@email.com"

        val notify = Notify {
            this.notifyId = NOTIFY_ID
            this.body = EMAIL_BODY
            this.header = EMAIL_HEADER
            this.documentId = "non_existent"
            this.notifySeverity = NotifySeverity.Information
            this.topic = TOPIC
        }

        val email = emailBuilder.buildEmail(JANE, notify)

        assertTrue(email.recipients.size == 1)
        assertEquals(email.subject, EMAIL_HEADER)
        assertEquals(email.htmlText, EMAIL_BODY)
        assertEquals(email.attachments.size, 0)
    }

    @Test
    fun `test simple email template - get template by ID`(): Unit = runBlocking {
        whenever(emailCache.userEmailAddress(JANE)) doReturn "jane.doe@email.com"

        val templateFile = File("src/test/resources/simple-email-template.html")
        whenever(fileStorageClient.generateDocumentContent(any())) doReturn GeneratedDocument(
            templateFile.readText(),
            emptyList()
        )

        val notify = Notify {
            this.notifyId = NOTIFY_ID
            this.body = EMAIL_BODY
            this.header = EMAIL_HEADER
            this.notifySeverity = NotifySeverity.Information
            this.templateRef = "valid_id"
            this.topic = TOPIC
        }

        val email = emailBuilder.buildEmail(JANE, notify)
        assertTrue(email.recipients.size == 1)
        assertEquals(email.subject, EMAIL_HEADER)

        val text = email.htmlText
        assertNotNull(text)
        assertTrue(text.contains("A Blue Heading"))
        assertTrue(text.contains("A red paragraph."))
    }

    @Test
    fun `test simple email template - get template by file name`(): Unit = runBlocking {
        whenever(emailCache.userEmailAddress(JANE)) doReturn "jane.doe@email.com"

        val templateFile = File("src/test/resources/simple-email-template.html")
        whenever(fileStorageClient.generateDocumentContent(any())) doReturn GeneratedDocument(
            templateFile.readText(),
            emptyList()
        )

        val notify = Notify {
            this.notifyId = NOTIFY_ID
            this.topic = TOPIC
            this.header = EMAIL_HEADER
            this.body = EMAIL_BODY
            this.notifySeverity = NotifySeverity.Information
            this.templateRef = "simple-email-template.html"
        }

        val email = emailBuilder.buildEmail(JANE, notify)
        assertTrue(email.recipients.size == 1)
        assertEquals(email.subject, EMAIL_HEADER)

        val text = email.htmlText
        assertNotNull(text)
        assertTrue(text.contains("A Blue Heading"))
        assertTrue(text.contains("A red paragraph."))
    }

    @Test
    fun `test email template with linked entity`(): Unit = runBlocking {
        whenever(emailCache.userEmailAddress(JANE)) doReturn "jane.doe@email.com"

        val generatedFile = File("src/test/resources/notify-report.html")
        whenever(fileStorageClient.generateDocumentContent(any())) doReturn GeneratedDocument(
            generatedFile.readText(),
            emptyList()
        )

        val entityId = entityDb.insert(
            Notify {
                body = CRITICAL_NOTIFY_BODY
                header = CRITICAL_NOTIFY_HEADER
                notifySeverity = NotifySeverity.Critical
                topic = TOPIC
            }
        ).record.notifyId

        val notify = Notify {
            this.notifyId = NOTIFY_ID
            this.body = EMAIL_BODY
            this.header = EMAIL_HEADER
            this.notifySeverity = NotifySeverity.Information
            this.tableEntityId = entityId
            this.tableName = NOTIFY
            this.templateRef = "valid_id"
            this.topic = TOPIC
        }

        val email = emailBuilder.buildEmail(JANE, notify)
        assertTrue(email.recipients.size == 1)
        assertEquals(email.subject, EMAIL_HEADER)

        val text = email.htmlText
        assertNotNull(text)
        assertTrue(text.contains("ATTENTION!"))
        assertTrue(text.contains("Please find below information regarding a critical notification."))

        assertTrue(text.contains("<td>This email contains critical information.</td>"))
        assertTrue(text.contains("<td>Critical notification</td>"))
        assertTrue(text.contains("<td>Critical</td>"))
        assertTrue(text.contains("<td>TOPIC</td>"))
    }

    @Test
    fun `test email template with linked entity, assets & data`(): Unit = runBlocking {
        whenever(emailCache.userEmailAddress(JANE)) doReturn "jane.doe@email.com"

        val generatedFile = File("src/test/resources/notify-report-with-assets.html")
        val cssFile = File("src/test/resources/notify-report-with-assets.css")
        val imageFile = File("src/test/resources/genesis-logo.png")
        whenever(fileStorageClient.generateDocumentContent(any())) doReturn GeneratedDocument(
            generatedFile.readText(),
            listOf(
                FileContentReply("1", "notify-report-with-assets.css", cssFile.inputStream().readAllBytes()),
                FileContentReply("2", "genesis-logo.png", imageFile.inputStream().readAllBytes())
            )
        )

        val entityId = entityDb.insert(
            Notify {
                body = CRITICAL_NOTIFY_BODY
                header = CRITICAL_NOTIFY_HEADER
                notifySeverity = NotifySeverity.Critical
                topic = TOPIC
            }
        ).record.notifyId

        val notify = Notify {
            this.notifyId = NOTIFY_ID
            this.body = EMAIL_BODY
            this.header = EMAIL_HEADER
            this.notifySeverity = NotifySeverity.Information
            this.tableEntityId = entityId
            this.tableName = NOTIFY
            this.templateRef = "valid_id"
            this.topic = TOPIC
        }

        val email = emailBuilder.buildEmail(JANE, notify)
        assertTrue(email.recipients.size == 1)
        assertEquals(email.subject, EMAIL_HEADER)

        val text = email.htmlText
        assertNotNull(text)
        assertTrue(text.contains("ATTENTION!"))
        assertTrue(text.contains("Please find below information regarding a critical notification and their respective assets."))

        assertTrue(text.contains("<td>This email contains critical information.</td>"))
        assertTrue(text.contains("<td>Critical notification</td>"))
        assertTrue(text.contains("<td>Critical</td>"))
        assertTrue(text.contains("<td>TOPIC</td>"))
    }

    @Test
    fun `test missing template uses notify body`(): Unit = runBlocking {
        whenever(emailCache.userEmailAddress(JANE)) doReturn "jane.doe@email.com"

        val notify = Notify {
            this.notifyId = NOTIFY_ID
            this.topic = TOPIC
            this.header = EMAIL_HEADER
            this.body = EMAIL_BODY
            this.notifySeverity = NotifySeverity.Information
            this.templateRef = "non_existent"
        }

        val email = emailBuilder.buildEmail(JANE, notify)
        assertEquals(email.htmlText, EMAIL_BODY)
    }

    @Test
    fun `test missing email cache throws exception`(): Unit = runBlocking {
        val notify = Notify {
            this.notifyId = NOTIFY_ID
            this.topic = TOPIC
            this.header = EMAIL_HEADER
            this.body = EMAIL_BODY
            this.notifySeverity = NotifySeverity.Information
        }

        val exception = assertThrows<RuntimeException> {
            emailBuilder.buildEmail(JANE, notify)
        }
        assertEquals("Could not obtain email address for user Jane <jane.doe@email.com>", exception.message)
    }
}
