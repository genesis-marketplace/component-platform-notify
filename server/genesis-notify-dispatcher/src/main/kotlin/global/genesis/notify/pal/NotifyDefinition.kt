package global.genesis.notify.pal

class NotifyDefinition : NotifyBuilder {
    val gatewayConfigs = GatewaysDefinition()

    override fun gateways(init: GatewaysBuilder.() -> Unit) {
        gatewayConfigs.init()
    }
}
