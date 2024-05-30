package global.genesis.notify.gateway.screen

import com.google.inject.Inject
import global.genesis.db.rx.entity.multi.AsyncEntityDb
import global.genesis.db.updatequeue.Bulk
import global.genesis.gen.dao.NotifyAlert
import global.genesis.gen.dao.enums.AlertStatus
import global.genesis.gen.dao.enums.TtlTimeUnit
import global.genesis.gen.view.entity.ScreenRoute
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.joda.time.DateTime
import java.time.Clock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit

class ScreenAlertExpiryManager(
    private val entityDb: AsyncEntityDb,
    private val coroutineScope: CoroutineScope,
    private val clock: Clock = Clock.systemUTC()
) : CoroutineScope by coroutineScope {

    @Inject
    constructor(
        entityDb: AsyncEntityDb
    ) : this(entityDb, CoroutineScope(Dispatchers.IO) + SupervisorJob())

    private val auditedDb = entityDb.audited("ScreenAlertExpiryManager", "ALERT_EXPIRED", "Alert deleted due to being expired")
    val jobs: ConcurrentMap<String, Job> = ConcurrentHashMap()
    val primed = CompletableDeferred<Unit>()

    init {
        launch {
            entityDb.bulkSubscribe<NotifyAlert>(backwardJoins = false).collect {
                when (it) {
                    is Bulk.Prime.Completed -> {
                        primed.complete(Unit)
                    }

                    is Bulk.Prime.Record -> {
                        scheduleExpiryIfNecessary(it.record)
                    }

                    is Bulk.Update.Insert -> {
                        scheduleExpiryIfNecessary(it.record)
                    }

                    is Bulk.Update.Modify -> {
                        // Cancel the job if the expiry time has been modified
                        if (it.newRecord.expiry != it.oldRecord.expiry) {
                            val job = jobs.remove(it.newRecord.alertId)
                            job?.cancel()
                            scheduleExpiryIfNecessary(it.newRecord)
                        }
                    }

                    is Bulk.Update.Delete -> {
                        jobs[it.record.alertId]?.cancel()
                    }
                }
            }
        }
    }

    private fun scheduleExpiryIfNecessary(record: NotifyAlert) {
        val expiryTime: DateTime? = record.expiry
        if (expiryTime != null) {
            val alertId = record.alertId
            // Calculate the delay until the record should be deleted
            val delayMillis = expiryTime.minus(clock.millis()).millis

            // Launch a coroutine to delete the record after the delay
            // Store the job in the map for possible cancellation
            jobs[alertId] = launch {
                delay(delayMillis)
                expireRecord(record)
            }
        }
    }

    private suspend fun expireRecord(record: NotifyAlert) {
        record.alertStatus = AlertStatus.EXPIRED
        auditedDb.modify(record)
    }

    fun calculateExpiry(screenRoutes: Set<ScreenRoute>): DateTime? {
        return if (screenRoutes.any { it.ttl == null }) {
            null
        } else {
            screenRoutes
                .mapNotNull { getTtlAsMillis(it) }
                .maxOfOrNull { clock.millis() + it }
                ?.let { DateTime(it) }
        }
    }

    private fun getTtlAsMillis(route: ScreenRoute): Long? {
        val ttl = route.ttl
        return ttl?.let {
            when (route.ttlTimeUnit) {
                TtlTimeUnit.NONE -> null
                else -> TimeUnit.valueOf(route.ttlTimeUnit.toString()).toMillis(it)
            }
        }
    }
}
