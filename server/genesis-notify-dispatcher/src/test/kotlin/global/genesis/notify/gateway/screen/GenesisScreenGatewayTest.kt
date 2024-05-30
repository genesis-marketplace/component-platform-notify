package global.genesis.notify.gateway.screen

import global.genesis.db.entity.InsertResult
import global.genesis.db.rx.entity.multi.AsyncEntityDb
import global.genesis.db.updatequeue.Bulk
import global.genesis.gen.dao.Notify
import global.genesis.gen.dao.NotifyAlert
import global.genesis.gen.dao.NotifyRoute
import global.genesis.gen.dao.enums.EntityIdType
import global.genesis.gen.dao.enums.NotifySeverity
import global.genesis.gen.dao.enums.TtlTimeUnit
import global.genesis.gen.view.entity.ScreenRoute
import global.genesis.notify.gateway.Gateway
import global.genesis.notify.router.NotifyRoutingData
import global.genesis.notify.router.UserNameResolver
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GenesisScreenGatewayTest {
    lateinit var mockDb: AsyncEntityDb
    lateinit var genesisScreenGateway: Gateway
    lateinit var mockUserNameResolver: UserNameResolver

    @BeforeEach
    fun setup() {
        mockDb = mock {
            on {
                this.bulkSubscribe(index = ScreenRoute.ById, backwardJoins = true)
            } doReturn flowOf(
                Bulk.Prime.Record(
                    ScreenRoute(
                        notifyRouteId = "1",
                        gatewayId = "Screen",
                        topicMatch = "topic",
                        entityId = "sid",
                        entityIdType = EntityIdType.USER_NAME,
                        ttlTimeUnit = TtlTimeUnit.MINUTES,
                        ttl = 1,
                        excludeSender = false
                    )
                ),
                Bulk.Prime.Record(
                    ScreenRoute(
                        notifyRouteId = "2",
                        gatewayId = "Screen",
                        topicMatch = "topic",
                        entityId = "fred",
                        entityIdType = EntityIdType.USER_NAME,
                        ttlTimeUnit = TtlTimeUnit.MINUTES,
                        ttl = 1,
                        excludeSender = false
                    )
                ),
                Bulk.Prime.Completed
            )
            on {
                this.bulkSubscribe<NotifyAlert>(backwardJoins = false)
            } doReturn flowOf(
                Bulk.Prime.Record(
                    NotifyAlert {
                        alertId = "1"
                        message = "someMessage"
                        userName = "sid"
                    }
                ),
                Bulk.Prime.Completed
            )
        }
        mockUserNameResolver = mock {
            onBlocking { getMatchedGatewayUsers(any<Notify>(), any<Set<ScreenRoute>>(), any(), any()) } doAnswer { invocationOnMock ->
                val routes = invocationOnMock.arguments[1] as Set<ScreenRoute>
                routes.mapNotNull { it.entityId }.toSet()
            }
        }

        val mockPrimed = CompletableDeferred(Unit)
        val screenAlertExpiryManager = mock<ScreenAlertExpiryManager> {
            on {
                calculateExpiry(any())
            } doReturn DateTime(70_000)
            on {
                primed
            } doReturn mockPrimed
        }
        genesisScreenGateway = GenesisScreenGateway(
            config = ScreenGatewayConfig("Screen"),
            db = mockDb,
            userNameResolver = mockUserNameResolver,
            screenAlertExpiryManager = screenAlertExpiryManager
        )
    }

    @Test
    fun givenNotifyMessage_insertsWithCorrectDbRecord() = runBlocking {
        val alertMessage = "someMessage"
        val notifyMessage = Notify {
            header = "HEADER"
            body = alertMessage
            notifySeverity = NotifySeverity.Information
            documentId = "docId"
            topic = "topic"
        }
        val sidRoute =
            NotifyRoute {
                notifyRouteId = "1"
                gatewayId = "Screen"
                topicMatch = "sid"
            }

        val fredRoute =
            NotifyRoute {
                notifyRouteId = "2"
                gatewayId = "Screen"
                topicMatch = "fred"
            }

        whenever(mockDb.insert(any<NotifyAlert>())).thenReturn(InsertResult(NotifyAlert {}))
        val routingData = NotifyRoutingData.Topic(setOf(sidRoute, fredRoute))
        genesisScreenGateway.sendViaTopic(notifyMessage, routingData)

        val users = setOf("sid", "fred")
        val dbCaptor = argumentCaptor<NotifyAlert>()
        verify(mockDb, times(2)).insert(dbCaptor.capture())
        val alertsByUser = dbCaptor.allValues.associateBy { it.userName }
        for (user in users) {
            val alert = alertsByUser[user]
            assertNotNull(alert)
            assertEquals(user, alert.userName)
            assertEquals(NotifySeverity.Information, alert.notifySeverity)
            assertEquals(alertMessage, alert.message)
            assertEquals("HEADER", alert.header)
        }
    }
}
