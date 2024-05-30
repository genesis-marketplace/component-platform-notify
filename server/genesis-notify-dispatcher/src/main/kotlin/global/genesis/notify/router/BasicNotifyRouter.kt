package global.genesis.notify.router

import global.genesis.commons.annotation.Module
import global.genesis.gen.dao.Notify
import global.genesis.notify.gateway.email.EmailGatewayConfig
import global.genesis.notify.gateway.screen.ScreenGatewayConfig
import global.genesis.notify.gateway.sendgrid.SendGridGatewayConfig
import global.genesis.notify.message.event.EmailDirectRoutingData
import global.genesis.notify.message.event.ScreenDirectRoutingData
import global.genesis.notify.pal.NotifyDefinition
import javax.inject.Inject

/**
 * Implementation of [NotifyRouter] that uses a small list
 * of hardcoded destination codes, for common notification use
 * cases, like 'send this to my screen' or 'send this to my email
 * address'.
 *
 * Basic routing prevents the need to have any additional
 * runtime configuration setup in order to use the notification system.
 *
 * @author tgosling
 */
@Module
class BasicNotifyRouter @Inject constructor(
    definition: NotifyDefinition
) : NotifyRouter {

    private val defaultScreenGatewayId: String?
    private val defaultEmailGatewayId: String?

    init {
        defaultScreenGatewayId = definition.gatewayConfigs.configById.filter {
            it.value is ScreenGatewayConfig
        }.map { it.key }
            .firstOrNull()

        defaultEmailGatewayId = definition.gatewayConfigs.configById.filter {
            it.value is EmailGatewayConfig || it.value is SendGridGatewayConfig
        }.map { it.key }
            .firstOrNull()
    }

    override fun getRoutingData(message: Notify): NotifyRoutingData {
        val sender = message.sender
        require(sender != null) { "Cannot use basic routing to send a notification to a user when the notification does not have a sender" }

        return when (message.routingData) {
            MY_SCREEN -> {
                require(defaultScreenGatewayId != null) { "Cannot use basic routing to send a notification to a user screen when no screen gateway has been configured." }
                val directRoutingData = ScreenDirectRoutingData(defaultScreenGatewayId, setOf(sender))
                NotifyRoutingData.Direct(directRoutingData)
            }

            MY_EMAIL -> {
                require(defaultEmailGatewayId != null) { "Cannot use basic routing to send a notification to a user email when no email gateway has been configured." }
                val directRoutingData = EmailDirectRoutingData(gatewayId = defaultEmailGatewayId, users = setOf(sender))
                NotifyRoutingData.Direct(directRoutingData)
            }

            else -> {
                throw IllegalArgumentException("BASIC routing type was specified but ROUTING_DATA did not contain a valid code. Received ${message.routingData}, expected one of $SUPPORTED_CODES")
            }
        }
    }

    companion object {
        private const val MY_SCREEN = "MY_SCREEN"
        private const val MY_EMAIL = "MY_EMAIL"
        private val SUPPORTED_CODES = setOf(MY_EMAIL, MY_SCREEN)
    }
}
