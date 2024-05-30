package global.genesis.notify.gateway.email

import com.google.inject.Binder
import com.google.inject.Injector
import com.google.inject.Module
import global.genesis.notify.gateway.Gateway
import global.genesis.notify.message.common.EmailDistribution
import global.genesis.notify.pal.GatewayConfig
import global.genesis.notify.utils.ConfigValidator
import global.genesis.pal.shared.inject
import org.simplejavamail.api.mailer.config.TransportStrategy

class EmailGatewayConfig(
    override val id: String
) : GatewayConfig {
    var smtpPort: Int = 587
    var smtpHost: String? = null
    var smtpUser: String? = null
    var smtpPw: String? = null
    var smtpProtocol: TransportStrategy = TransportStrategy.SMTP
    var systemDefaultUserName: String? = null
    var systemDefaultEmail: String? = null
    var sendFromUserAddress: Boolean = false
    internal val staticDistribution = EmailDistribution()

    override fun validate(): List<String> {
        return ConfigValidator.validateRequiredFields(EmailGatewayConfig::class, this)
    }

    override fun build(injector: Injector): Gateway {
        val childInjector = injector.createChildInjector(object : Module {
            override fun configure(binder: Binder?) {
                binder?.bind(EmailGatewayConfig::class.java)?.toInstance(this@EmailGatewayConfig)
            }
        })
        return childInjector.inject<EmailGateway>()
    }

    override val routeInfoSet = setOf(EMAIL_USER_ROUTE_INFO, EMAIL_DISTRIBUTION_ROUTE_INFO)

    fun staticDistribution(init: EmailDistribution.() -> Unit) {
        staticDistribution.init()
    }
}
