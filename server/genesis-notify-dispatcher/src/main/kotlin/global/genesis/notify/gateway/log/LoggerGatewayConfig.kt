package global.genesis.notify.gateway.log

import com.google.inject.Injector
import global.genesis.notify.gateway.Gateway
import global.genesis.notify.message.request.RouteInfo
import global.genesis.notify.pal.GatewayConfig
import org.slf4j.event.Level

class LoggerGatewayConfig(
    override val id: String
) : GatewayConfig {
    var defaultLevel: Level = Level.INFO

    override fun validate(): List<String> {
        return emptyList()
    }

    override fun build(injector: Injector): Gateway {
        return LoggerGateway(defaultLevel)
    }

    override val routeInfoSet = setOf(
        RouteInfo(
            dataServerHandler = "ALL_LOG_ROUTES",
            createEventHandler = "LOG_ROUTE_CREATE",
            updateEventHandler = "LOG_ROUTE_UPDATE",
            deleteEventHandler = "LOG_ROUTE_DELETE",
            displayName = "Log Notification"
        )
    )
}
