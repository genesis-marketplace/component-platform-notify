package global.genesis.notify.gateway.screen

import com.google.inject.Binder
import com.google.inject.Injector
import com.google.inject.Module
import global.genesis.notify.gateway.Gateway
import global.genesis.notify.message.request.RouteInfo
import global.genesis.notify.pal.GatewayConfig
import global.genesis.pal.shared.inject

class ScreenGatewayConfig(
    override val id: String
) : GatewayConfig {
    override fun validate(): List<String> {
        return emptyList()
    }

    override fun build(injector: Injector): Gateway {
        val childInjector = injector.createChildInjector(object : Module {
            override fun configure(binder: Binder?) {
                binder?.bind(ScreenGatewayConfig::class.java)?.toInstance(this@ScreenGatewayConfig)
            }
        })
        return childInjector.inject<GenesisScreenGateway>()
    }

    override val routeInfoSet = setOf(
        RouteInfo(
            dataServerHandler = "ALL_SCREEN_ROUTES",
            createEventHandler = "SCREEN_NOTIFY_ROUTE_CREATE",
            updateEventHandler = "SCREEN_NOTIFY_ROUTE_UPDATE",
            deleteEventHandler = "SCREEN_NOTIFY_ROUTE_DELETE",
            displayName = "Screen Notification"
        )
    )
}
