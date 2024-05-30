package global.genesis.notify.pal

import global.genesis.commons.config.GenesisConfigurationException
import global.genesis.notify.gateway.email.EmailGatewayConfig
import global.genesis.notify.gateway.log.LoggerGatewayConfig
import global.genesis.notify.gateway.screen.ScreenGatewayConfig
import global.genesis.notify.gateway.sendgrid.SendGridGatewayConfig
import global.genesis.notify.gateway.teams.TeamsGatewayConfig
import global.genesis.notify.message.request.RouteInfo

class GatewaysDefinition : GatewaysBuilder {
    val configById = mutableMapOf<String, GatewayConfig>()
    val errorList = mutableListOf<String>()
    val gatewayIdsByRouteInfo = mutableMapOf<RouteInfo, MutableSet<String>>()

    override fun log(id: String, init: LoggerGatewayConfig.() -> Unit) {
        val loggerConfig = LoggerGatewayConfig(id)
        loggerConfig.init()
        registerGateway(id, loggerConfig)
    }

    override fun email(id: String, init: EmailGatewayConfig.() -> Unit) {
        val emailConfig = EmailGatewayConfig(id)
        emailConfig.init()
        registerGateway(id, emailConfig)
    }

    override fun screen(id: String, init: ScreenGatewayConfig.() -> Unit) {
        val screenConfig = ScreenGatewayConfig(id)
        screenConfig.init()
        registerGateway(id, screenConfig)
    }

    override fun teams(id: String, init: TeamsGatewayConfig.() -> Unit) {
        val teamsConfig = TeamsGatewayConfig(id)
        teamsConfig.init()
        registerGateway(id, teamsConfig)
    }

    override fun sendGrid(id: String, init: SendGridGatewayConfig.() -> Unit) {
        val sendGridConfig = SendGridGatewayConfig(id)
        sendGridConfig.init()
        registerGateway(id, sendGridConfig)
    }

    override fun registerGateway(id: String, config: GatewayConfig) {
        val previousGatewayConfig = configById.putIfAbsent(id, config)
        previousGatewayConfig?.let {
            errorList.add("Detected duplicate gateway name: $id")
        }
        config.routeInfoSet.forEach {
            if (gatewayIdsByRouteInfo.containsKey(it)) {
                gatewayIdsByRouteInfo[it]?.add(id)
            } else {
                gatewayIdsByRouteInfo[it] = mutableSetOf(id)
            }
        }
    }

    fun validate() {
        for (entry in configById.entries) {
            entry.value.validate().forEach {
                errorList.add("${entry.key}; $it")
            }
        }
        if (errorList.isNotEmpty()) {
            val e = GenesisConfigurationException("Validation errors encountered while processing notify definition")
            errorList
                .map { GenesisConfigurationException(it) }
                .forEach { e.addSuppressed(it) }
            throw e
        }
    }
}
