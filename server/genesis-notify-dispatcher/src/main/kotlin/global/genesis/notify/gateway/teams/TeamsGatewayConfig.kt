package global.genesis.notify.gateway.teams

import com.google.inject.Binder
import com.google.inject.Injector
import com.google.inject.Module
import global.genesis.notify.gateway.Gateway
import global.genesis.notify.message.request.RouteInfo
import global.genesis.notify.pal.GatewayConfig
import global.genesis.pal.shared.inject

class TeamsGatewayConfig(
    override val id: String
) : GatewayConfig {
    @Deprecated(message = "Gateway level URL is deprecated. The webHook URL must be specified on the Teams Route")
    var url: String? = null

    override fun validate(): List<String> = emptyList()

    override fun build(injector: Injector): Gateway {
        val childInjector = injector.createChildInjector(object : Module {
            override fun configure(binder: Binder?) {
                binder?.bind(TeamsGatewayConfig::class.java)?.toInstance(this@TeamsGatewayConfig)
            }
        })
        return childInjector.inject<MicrosoftTeamsChannelGateway>()
    }

    override val routeInfoSet = setOf(
        RouteInfo(
            dataServerHandler = "ALL_MS_TEAMS_ROUTES",
            createEventHandler = "MS_TEAMS_ROUTE_CREATE",
            updateEventHandler = "MS_TEAMS_ROUTE_UPDATE",
            deleteEventHandler = "MS_TEAMS_ROUTE_DELETE",
            displayName = "MS Teams Notification"
        )
    )
}
