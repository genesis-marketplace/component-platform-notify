package global.genesis.notify.gateway.sendgrid

import com.google.inject.Binder
import com.google.inject.Injector
import com.google.inject.Module
import global.genesis.notify.gateway.Gateway
import global.genesis.notify.gateway.email.EMAIL_DISTRIBUTION_ROUTE_INFO
import global.genesis.notify.gateway.email.EMAIL_USER_ROUTE_INFO
import global.genesis.notify.message.common.EmailDistribution
import global.genesis.notify.pal.GatewayConfig
import global.genesis.notify.utils.ConfigValidator
import global.genesis.pal.shared.inject

class SendGridGatewayConfig(
    override val id: String
) : GatewayConfig {

    var apiKey: String? = null
    var defaultSender: String? = null

    internal val staticDistribution = EmailDistribution()

    override fun validate(): List<String> {
        return ConfigValidator.validateRequiredFields(SendGridGatewayConfig::class, this)
    }

    override fun build(injector: Injector): Gateway {
        val childInjector = injector.createChildInjector(object : Module {
            override fun configure(binder: Binder?) {
                binder?.bind(SendGridGatewayConfig::class.java)?.toInstance(this@SendGridGatewayConfig)
            }
        })
        return childInjector.inject<SendGridGateway>()
    }

    override val routeInfoSet = setOf(EMAIL_USER_ROUTE_INFO, EMAIL_DISTRIBUTION_ROUTE_INFO)

    fun staticDistribution(init: EmailDistribution.() -> Unit) {
        staticDistribution.init()
    }
}
