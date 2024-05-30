package global.genesis.notify.gateway.sendgrid

import com.sendgrid.Request
import com.sendgrid.Response
import com.sendgrid.SendGrid
import global.genesis.db.rx.entity.multi.AsyncEntityDb
import global.genesis.db.updatequeue.Bulk
import global.genesis.gen.dao.Notify
import global.genesis.gen.dao.NotifyRoute
import global.genesis.gen.dao.enums.NotifySeverity
import global.genesis.gen.view.entity.EmailDistributionRoute
import global.genesis.gen.view.entity.EmailUserRoute
import global.genesis.notify.message.common.EmailDistribution
import global.genesis.notify.router.NotifyRoutingData
import global.genesis.notify.router.UserNameResolver
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.apache.commons.collections4.CollectionUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SendGridGatewayTest {
    private lateinit var gateway: SendGridGateway
    private val mockMailer = mock<SendGrid> {
        on { api(any()) } doReturn Response()
    }
    private val mockEmail = mock<Request> {}
    private val mockEmailBuilder = mock<SendGridEmailBuilder> {
        on { runBlocking { buildEmail(any<String>(), any()) } } doReturn mockEmail
        on { runBlocking { buildEmail(any<EmailDistribution>(), any<Notify>()) } } doReturn mockEmail
    }
    private val mockUserNameResolver = mock<UserNameResolver> {
        onBlocking { getMatchedGatewayUsers<EmailUserRoute>(any(), any(), any(), any()) } doReturn emptySet()
    }
    private val mockDb = mock<AsyncEntityDb> {
        on {
            this.bulkSubscribe(index = EmailDistributionRoute.ById, backwardJoins = true)
        } doReturn flowOf(
            Bulk.Prime.Record(
                EmailDistributionRoute(
                    notifyRouteId = "COMPLETE",
                    topicMatch = "topic",
                    emailTo = "John Smith<john@email.com>,bob@email.com",
                    emailCc = "carol@email.com",
                    emailBcc = "susan@email.com",
                    gatewayId = "email1"
                )
            ),
            Bulk.Prime.Record(
                EmailDistributionRoute(
                    notifyRouteId = "INCOMPLETE",
                    topicMatch = "topic",
                    gatewayId = "email1"
                )
            ),
            Bulk.Prime.Completed
        )
        on {
            this.bulkSubscribe(index = EmailUserRoute.ById, backwardJoins = true)
        } doReturn flowOf(
            Bulk.Prime.Completed
        )
    }

    @BeforeEach
    fun setUp() {
        val config = SendGridGatewayConfig("email1")
        config.defaultSender = "default@email.com"
        gateway = SendGridGateway(
            config = config,
            db = mockDb,
            userNameResolver = mockUserNameResolver,
            emailBuilder = mockEmailBuilder,
            sendGrid = mockMailer
        )
    }

    @Test
    fun `email is sent successfully when there is a matching distribution route with sufficient information - using system defaults`() =
        runBlocking {
            val matchedRoute = NotifyRoute {
                notifyRouteId = "COMPLETE"
                topicMatch = "topic"
                gatewayId = "email1"
            }

            val message = Notify {
                header = "HEADER"
                body = "NOT_MUCH_HERE"
                notifySeverity = NotifySeverity.Information
                topic = "topic"
            }

            gateway.sendViaTopic(message, NotifyRoutingData.Topic(setOf(matchedRoute)))

            val captor = argumentCaptor<EmailDistribution>()

            verify(mockEmailBuilder, times(1)).buildEmail(captor.capture(), any<Notify>())
            verify(mockMailer, atLeastOnce()).api(any())

            assertEquals(1, captor.allValues.size)
            val emailDistribution = captor.firstValue
            assertTrue(
                CollectionUtils.isEqualCollection(
                    emailDistribution.to,
                    listOf("John Smith<john@email.com>", "bob@email.com")
                )
            )
            assertTrue(CollectionUtils.isEqualCollection(emailDistribution.cc, listOf("carol@email.com")))
            assertTrue(CollectionUtils.isEqualCollection(emailDistribution.bcc, listOf("susan@email.com")))
        }

    @Test
    fun `email is sent successfully when there is a matching distribution route with sufficient information - using sender details`() =
        runBlocking {
            val matchedRoute = NotifyRoute {
                notifyRouteId = "COMPLETE"
                topicMatch = "topic"
                gatewayId = "email1"
            }

            val message = Notify {
                header = "HEADER"
                body = "NOT_MUCH_HERE"
                notifySeverity = NotifySeverity.Information
                topic = "topic"
                sender = "user1"
            }

            gateway.sendViaTopic(message, NotifyRoutingData.Topic(setOf(matchedRoute)))

            val captor = argumentCaptor<EmailDistribution>()

            verify(mockEmailBuilder, times(1)).buildEmail(captor.capture(), any<Notify>())
            verify(mockMailer, atLeastOnce()).api(any())

            assertEquals(1, captor.allValues.size)
            val emailDistribution = captor.firstValue
            assertTrue(
                CollectionUtils.isEqualCollection(
                    emailDistribution.to,
                    listOf("John Smith<john@email.com>", "bob@email.com")
                )
            )
            assertTrue(CollectionUtils.isEqualCollection(emailDistribution.cc, listOf("carol@email.com")))
            assertTrue(CollectionUtils.isEqualCollection(emailDistribution.bcc, listOf("susan@email.com")))
        }

    @Test
    fun `email is not sent when there is a matching distribution route with no recipients`() = runBlocking<Unit> {
        val matchedRoute = NotifyRoute {
            notifyRouteId = "INCOMPLETE"
            topicMatch = "topic"
            gatewayId = "email1"
        }

        val message = Notify {
            header = "HEADER"
            body = "NOT_MUCH_HERE"
            notifySeverity = NotifySeverity.Information
            topic = "topic"
            sender = "user1"
        }

        gateway.sendViaTopic(message, NotifyRoutingData.Topic(setOf(matchedRoute)))

        verify(mockMailer, never()).api(any())
    }

    @Test
    fun `email is not sent when there is no matching distribution route`() = runBlocking<Unit> {
        val matchedRoute = NotifyRoute {
            notifyRouteId = "INCOMPLETE"
            topicMatch = "topic"
            gatewayId = "email1"
        }

        val message = Notify {
            header = "HEADER"
            body = "NOT_MUCH_HERE"
            notifySeverity = NotifySeverity.Information
            topic = "topic"
            sender = "user1"
        }

        gateway.sendViaTopic(message, NotifyRoutingData.Topic(setOf(matchedRoute)))

        verify(mockMailer, never()).api(any())
    }
}
