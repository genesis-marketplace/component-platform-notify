package global.genesis.notify.pal

import com.google.inject.Injector
import global.genesis.notify.gateway.Gateway
import global.genesis.notify.message.request.RouteInfo

interface GatewayConfig {
    val id: String
    fun validate(): List<String>
    fun build(injector: Injector): Gateway

    val routeInfoSet: Set<RouteInfo>
}
