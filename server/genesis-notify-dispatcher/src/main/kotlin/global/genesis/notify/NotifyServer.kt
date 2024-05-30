package global.genesis.notify

import com.google.inject.Injector
import global.genesis.commons.annotation.Module
import global.genesis.db.rx.AbstractEntityBulkTableSubscriber
import global.genesis.db.rx.entity.multi.AsyncEntityDb
import global.genesis.gen.dao.Notify
import global.genesis.gen.dao.description.NotifyDescription
import global.genesis.notify.gateway.Gateway
import global.genesis.notify.pal.NotifyDefinition
import global.genesis.notify.router.NotifyRouterProvider
import global.genesis.notify.router.NotifyRoutingData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.util.HashMap
import javax.inject.Inject

@Module
class NotifyServer(
    db: AsyncEntityDb,
    private val routerProvider: NotifyRouterProvider,
    definition: NotifyDefinition,
    private val injector: Injector,
    private val scope: CoroutineScope
) : AbstractEntityBulkTableSubscriber<Notify>(
    entityDb = db,
    entityDescription = NotifyDescription,
    scope = scope
) {

    val gateways: Map<String, Gateway>

    @OptIn(DelicateCoroutinesApi::class)
    @Inject
    constructor(
        db: AsyncEntityDb,
        routerProvider: NotifyRouterProvider,
        definition: NotifyDefinition,
        injector: Injector
    ) : this(db, routerProvider, definition, injector, GlobalScope)

    init {
        definition.gatewayConfigs.validate()
        gateways = HashMap()
        definition.gatewayConfigs.configById.forEach {
            gateways[it.key] = it.value.build(injector)
        }
        initialise()
    }

    override fun onInsert(record: Notify) {
        try {
            scope.launch {
                val router = routerProvider.getRouter(record)
                val routingData = router.getRoutingData(record)
                send(routingData, record)
            }
        } catch (e: Exception) {
            LOG.warn("Error while sending {}", record, e)
        }
    }

    suspend fun send(routingData: NotifyRoutingData, message: Notify) {
        when (routingData) {
            is NotifyRoutingData.Direct -> {
                sendDirect(routingData, message)
            }
            is NotifyRoutingData.Topic -> {
                sendViaTopic(message, routingData)
            }
        }
    }

    private suspend fun sendViaTopic(
        message: Notify,
        routeData: NotifyRoutingData.Topic
    ) {
        val gatewayIds = routeData.routes.map { it.gatewayId }.toSet()
        gatewayIds.forEach {
            val gateway = gateways[it]
            LOG.debug("Sending messageId {} to gatewayId: {}", message.notifyId, it)
            try {
                require(gateway != null) { "gateway with ID $it is not configured" }
                gateway.sendViaTopic(message, routeData)
            } catch (e: Exception) {
                LOG.error("Can't send message to gateway {}: ", message.notifyId, e)
            }
        }
    }

    private suspend fun sendDirect(
        routeData: NotifyRoutingData.Direct,
        message: Notify
    ) {
        val gatewayId = routeData.directRoutingData.gatewayId
        val gateway = gateways[gatewayId]
        if (gateway == null) {
            LOG.warn("Received direct routing request for gateway $gatewayId, which does not exist. Ignoring notification")
        } else {
            gateway.sendDirect(message, routeData)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(NotifyServer::class.java)
    }
}
