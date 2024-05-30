package global.genesis.notify.gateway.screen

import global.genesis.db.rx.entity.multi.AsyncEntityDb
import global.genesis.db.rx.entity.multi.AsyncMultiEntityReadWriteGenericSupport
import global.genesis.db.updatequeue.Bulk
import global.genesis.gen.dao.NotifyAlert
import global.genesis.gen.dao.enums.EntityIdType
import global.genesis.gen.dao.enums.TtlTimeUnit
import global.genesis.gen.view.entity.ScreenRoute
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.awaitility.Awaitility
import org.joda.time.DateTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.time.Clock
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class ScreenAlertExpiryManagerTest {
    lateinit var mockDb: AsyncEntityDb
    lateinit var mockAuditedDb: AsyncMultiEntityReadWriteGenericSupport
    private val clock: Clock = Clock.systemUTC()
    private lateinit var notifyAlertFlow: MutableSharedFlow<Bulk<NotifyAlert>>

    @BeforeEach
    fun setup() = runBlocking {
        notifyAlertFlow = MutableSharedFlow()

        mockAuditedDb = mock {}
        mockDb = mock {
            on {
                this.bulkSubscribe<NotifyAlert>(backwardJoins = false)
            } doReturn notifyAlertFlow
            on { audited(any(), any(), any()) } doReturn mockAuditedDb
        }
    }

    @Test
    fun `prime is successful when bulk prime record is received`(): Unit = runTest(UnconfinedTestDispatcher()) {
        val screenAlertExpiryManager = ScreenAlertExpiryManager(mockDb, this.backgroundScope, clock = clock)
        prime()
        Awaitility.waitAtMost(10, TimeUnit.SECONDS).until {
            runBlocking {
                screenAlertExpiryManager.primed.await()
                return@runBlocking true
            }
        }
    }

    @Test
    fun `alert with expiry time is successfully scheduled for expiry`(): Unit = runTest(UnconfinedTestDispatcher()) {
        val record = NotifyAlert {
            alertId = "1"
            userName = "alert1"
            message = "some message"
            expiry = DateTime(clock.millis().minus(1 * 60 * 1000))
        }

        ScreenAlertExpiryManager(mockDb, this.backgroundScope, clock)
        prime()
        notifyAlertFlow.emit(Bulk.Update.Insert(record, "test"))
        verify(mockAuditedDb, times(1)).modify(record)
    }

    @Test
    fun `alert with expiry time is not scheduled for expiry`(): Unit = runTest(UnconfinedTestDispatcher()) {
        val record = NotifyAlert {
            alertId = "1"
            userName = "alert1"
            message = "some message"
            expiry = DateTime(clock.millis().plus(1 * 60 * 1000))
        }

        ScreenAlertExpiryManager(mockDb, this.backgroundScope, clock)
        prime()
        notifyAlertFlow.emit(Bulk.Update.Insert(record, "test"))
        verify(mockAuditedDb, times(0)).delete(record)
    }

    @Test
    fun `alert without expiry time is not scheduled for expiry`(): Unit = runTest(UnconfinedTestDispatcher()) {
        val record = NotifyAlert {
            alertId = "1"
            userName = "alert1"
            message = "some message"
            expiry = null
        }

        ScreenAlertExpiryManager(mockDb, this.backgroundScope, clock)
        prime()
        notifyAlertFlow.emit(Bulk.Update.Insert(record, "test"))
        verify(mockAuditedDb, times(0)).delete(record)
    }

    @Test
    fun `screen route with a valid ttl returns an expiry`(): Unit = runTest(UnconfinedTestDispatcher()) {
        val screenRoute = ScreenRoute(
            notifyRouteId = "1",
            gatewayId = "",
            topicMatch = "topic",
            entityId = "sid",
            entityIdType = EntityIdType.USER_NAME,
            ttlTimeUnit = TtlTimeUnit.MINUTES,
            ttl = 20,
            excludeSender = false
        )

        val screenRoutes = setOf(screenRoute)
        val expectedExpiry = DateTime(clock.millis().plus(20 * 60 * 1000))
        val screenAlertExpiryManager = ScreenAlertExpiryManager(mockDb, this.backgroundScope, clock)
        prime()
        assertEquals(expectedExpiry.minuteOfDay(), screenAlertExpiryManager.calculateExpiry(screenRoutes)?.minuteOfDay())
    }

    @Test
    fun `screen route with a null ttl returns a null expiry`(): Unit = runTest(UnconfinedTestDispatcher()) {
        val screenRoute = ScreenRoute(
            notifyRouteId = "1",
            gatewayId = "",
            topicMatch = "topic",
            entityId = "sid",
            entityIdType = EntityIdType.USER_NAME,
            ttlTimeUnit = TtlTimeUnit.NONE,
            ttl = null,
            excludeSender = false
        )

        val screenRoutes = setOf(screenRoute)
        val expectedExpiry = null
        val screenAlertExpiryManager = ScreenAlertExpiryManager(mockDb, this.backgroundScope, clock)
        prime()
        assertEquals(expectedExpiry, screenAlertExpiryManager.calculateExpiry(screenRoutes))
    }

    @Test
    fun `two screen routes with valid ttl will return the higher expiry`(): Unit = runTest(UnconfinedTestDispatcher()) {
        val firstScreenRoute = ScreenRoute(
            notifyRouteId = "1",
            gatewayId = "",
            topicMatch = "topic",
            entityId = "sid",
            entityIdType = EntityIdType.USER_NAME,
            ttlTimeUnit = TtlTimeUnit.MINUTES,
            ttl = 10,
            excludeSender = false
        )
        val secondScreenRoute = ScreenRoute(
            notifyRouteId = "1",
            gatewayId = "",
            topicMatch = "topic",
            entityId = "sid",
            entityIdType = EntityIdType.USER_NAME,
            ttlTimeUnit = TtlTimeUnit.MINUTES,
            ttl = 7,
            excludeSender = false
        )

        val screenRoutes = setOf(firstScreenRoute, secondScreenRoute)
        val expectedExpiry = DateTime(clock.millis().plus(10 * 60 * 1000))
        val screenAlertExpiryManager = ScreenAlertExpiryManager(mockDb, this.backgroundScope, clock)
        prime()
        assertEquals(expectedExpiry.minuteOfDay(), screenAlertExpiryManager.calculateExpiry(screenRoutes)?.minuteOfDay())
    }

    @Test
    fun `two screen routes with one null and one valid ttl will return null expiry`(): Unit = runTest(UnconfinedTestDispatcher()) {
        val firstScreenRoute = ScreenRoute(
            notifyRouteId = "1",
            gatewayId = "",
            topicMatch = "topic",
            entityId = "sid",
            entityIdType = EntityIdType.USER_NAME,
            ttlTimeUnit = TtlTimeUnit.NONE,
            ttl = null,
            excludeSender = false
        )
        val secondScreenRoute = ScreenRoute(
            notifyRouteId = "1",
            gatewayId = "",
            topicMatch = "topic",
            entityId = "sid",
            entityIdType = EntityIdType.USER_NAME,
            ttlTimeUnit = TtlTimeUnit.MINUTES,
            ttl = 7,
            excludeSender = false
        )

        val screenRoutes = setOf(firstScreenRoute, secondScreenRoute)
        val expectedExpiry = null
        val screenAlertExpiryManager = ScreenAlertExpiryManager(mockDb, this.backgroundScope, clock)
        prime()
        assertEquals(expectedExpiry, screenAlertExpiryManager.calculateExpiry(screenRoutes))
    }

    private suspend fun prime() {
        notifyAlertFlow.emit(
            Bulk.Prime.Record(
                NotifyAlert {
                    alertId = "1"
                    message = "someMessage"
                    userName = "sid"
                }
            )
        )
        notifyAlertFlow.emit(Bulk.Prime.Completed)
    }
}
