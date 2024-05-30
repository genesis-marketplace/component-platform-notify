package global.genesis.notify.router

import global.genesis.db.entity.SingleCardinalityDbEntity
import global.genesis.db.updatequeue.Bulk
import global.genesis.gen.dao.NotifyRoute
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

@OptIn(DelicateCoroutinesApi::class)
class NotifyRouteCache<T : SingleCardinalityDbEntity>(
    val name: String,
    private val subscriber: Flow<Bulk<T>>,
    private val keyFunction: (T) -> String
) {
    val primed = CompletableDeferred<Unit>()
    private val notifyRoutes = ConcurrentHashMap<String, T>()

    init {
        GlobalScope.launch {
            subscriber.collect { recordUpdate ->
                when (recordUpdate) {
                    is Bulk.Prime.Completed -> {
                        primed.complete(Unit)
                        LOG.info("Initialisation completed for $name cache, listening to updates")
                    }

                    is Bulk.Prime.Record -> {
                        LOG.debug("Prime record {}", recordUpdate)
                        notifyRoutes[keyFunction(recordUpdate.record)] = recordUpdate.record
                    }

                    is Bulk.Update.Insert -> {
                        LOG.debug("Insert record {}", recordUpdate)
                        notifyRoutes[keyFunction(recordUpdate.record)] = recordUpdate.record
                    }

                    is Bulk.Update.Modify -> {
                        LOG.debug("Modify record {}", recordUpdate)
                        notifyRoutes[keyFunction(recordUpdate.record)] = recordUpdate.newRecord
                    }

                    is Bulk.Update.Delete -> {
                        LOG.debug("Delete record {}", recordUpdate)
                        notifyRoutes.remove(keyFunction(recordUpdate.record))
                    }
                }
            }
        }
        runBlocking {
            LOG.info("Waiting on {} priming to finish initialization", name)
            primed.await()
            LOG.info("{} primed, completing initialization", name)
        }
    }

    fun getNotifyRoutes(
        routesToFind: Set<NotifyRoute>,
        logWarningOnNotFound: Boolean = false
    ): Set<T> =
        routesToFind.map { routeToFind ->
            routeToFind to notifyRoutes[routeToFind.notifyRouteId]
        }.onEach { (routeToFind, foundRoute) ->
            if (foundRoute == null && logWarningOnNotFound) {
                LOG.warn("Could not find route with id ${routeToFind.notifyRouteId} in cache $name")
            }
        }.mapNotNull { it.second }.toSet()

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(NotifyRouteCache::class.java)
    }
}
