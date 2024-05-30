package global.genesis.notify.router

import global.genesis.db.rx.AbstractEntityBulkTableSubscriber
import global.genesis.db.rx.entity.multi.AsyncEntityDb
import global.genesis.gen.dao.Notify
import global.genesis.gen.dao.NotifyRoute
import global.genesis.gen.dao.description.NotifyRouteDescription
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TopicMatchingNotifyRouter @Inject constructor(
    db: AsyncEntityDb
) : NotifyRouter,
    AbstractEntityBulkTableSubscriber<NotifyRoute>(
        db,
        NotifyRouteDescription
    ) {

    private val routes: MutableMap<String, NotifyRoute> = ConcurrentHashMap()

    override fun getRoutingData(message: Notify): NotifyRoutingData {
        val topic = message.topic ?: message.routingData!!
        return routes.values
            .filter { it.matchRoute(topic) }
            .toSet()
            .let { NotifyRoutingData.Topic(it) }
    }

    private fun NotifyRoute.matchRoute(topic: String): Boolean {
        return when (val match = topicMatch) {
            "*" -> true
            else -> topic.startsWith(match)
        }
    }

    override fun onPrime(record: NotifyRoute) {
        routes[record.notifyRouteId] = record
    }

    override fun onInsert(record: NotifyRoute) {
        routes[record.notifyRouteId] = record
    }

    override fun onDelete(record: NotifyRoute) {
        routes.remove(record.notifyRouteId)
    }

    override fun onModify(newRecord: NotifyRoute, oldRecord: NotifyRoute, modifiedFields: List<String>) {
        routes.remove(oldRecord.notifyRouteId)
        routes[newRecord.notifyRouteId] = newRecord
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(TopicMatchingNotifyRouter::class.java)
    }
}
