package global.genesis.notify.pal

import global.genesis.notify.gateway.email.EmailGatewayConfig
import global.genesis.notify.gateway.log.LoggerGatewayConfig
import global.genesis.notify.gateway.screen.ScreenGatewayConfig
import global.genesis.notify.gateway.sendgrid.SendGridGatewayConfig
import global.genesis.notify.gateway.teams.TeamsGatewayConfig

interface GatewaysBuilder {
    @NotifyMarker
    fun log(id: String, init: LoggerGatewayConfig.() -> Unit = {})

    @NotifyMarker
    fun email(id: String, init: EmailGatewayConfig.() -> Unit)

    @NotifyMarker
    fun screen(id: String, init: ScreenGatewayConfig.() -> Unit = {})

    @NotifyMarker
    fun teams(id: String, init: TeamsGatewayConfig.() -> Unit = {})

    @NotifyMarker
    fun sendGrid(id: String, init: SendGridGatewayConfig.() -> Unit = {})

    fun registerGateway(id: String, config: GatewayConfig)
}
