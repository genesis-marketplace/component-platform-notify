package global.genesis.notify.gateway.sendgrid

import com.sendgrid.Request
import com.sendgrid.SendGrid
import global.genesis.db.rx.entity.multi.AsyncEntityDb
import global.genesis.gen.dao.Notify
import global.genesis.gen.view.entity.EmailDistributionRoute
import global.genesis.gen.view.entity.EmailUserRoute
import global.genesis.notify.gateway.Gateway
import global.genesis.notify.gateway.common.RouteUtils
import global.genesis.notify.gateway.common.RouteUtils.getEmailDistribution
import global.genesis.notify.gateway.common.RouteUtils.getMatchedTopicRoutes
import global.genesis.notify.message.common.EmailDistribution
import global.genesis.notify.message.event.EmailDirectRoutingData
import global.genesis.notify.router.NotifyRoutingData
import global.genesis.notify.router.UserNameResolver
import org.slf4j.LoggerFactory
import java.io.IOException
import javax.inject.Inject

/**
 * Implementation of [Gateway] for sending email messages to a SendGrid server.
 *
 * Two route types are supported:
 * EMAIL_USER_ROUTE
 * EMAIL_DISTRIBUTION_ROUTE
 *
 * A user route is used for sending to one or many users defined in the USER table within
 * the Genesis Application.
 *
 * A distribution route is used to send to an arbitrary list of email addresses that are not
 * tied to specific users within the system.
 *
 */
class SendGridGateway(
    private val config: SendGridGatewayConfig,
    db: AsyncEntityDb,
    private val userNameResolver: UserNameResolver,
    private val emailBuilder: SendGridEmailBuilder,
    private val sendGrid: SendGrid
) : Gateway {

    @Inject
    constructor(
        config: SendGridGatewayConfig,
        db: AsyncEntityDb,
        userNameResolver: UserNameResolver,
        emailBuilder: SendGridEmailBuilder
    ) : this(
        config,
        db,
        userNameResolver,
        emailBuilder,
        SendGrid(config.apiKey)
    )

    private val staticDistribution = config.staticDistribution
    private val emailDistributionRouteCache = RouteUtils.getEmailDistRouteCache(db)
    private val emailUserRouteCache = RouteUtils.getEmailUserRouteCache(db)

    override suspend fun sendViaTopic(message: Notify, routeData: NotifyRoutingData.Topic) {
        val matchedRoutes = routeData.getMatchedTopicRoutes(config.id)

        if (matchedRoutes.isEmpty()) return

        val distributionRoutes: Set<EmailDistributionRoute> = emailDistributionRouteCache.getNotifyRoutes(matchedRoutes)
        val fullDistribution = distributionRoutes.fold(staticDistribution) { acc, emailRoute ->
            acc + emailRoute.getEmailDistribution()
        }
        if (fullDistribution.hasRecipients()) {
            sendViaSendGrid(fullDistribution, message)
        }

        val userRoutes: Set<EmailUserRoute> = emailUserRouteCache.getNotifyRoutes(matchedRoutes)
        val userNames = RouteUtils.resolveUserNames(message, userRoutes, userNameResolver)
        userNames.forEach {
            sendToUserNameViaSendGrid(it, message)
        }

        RouteUtils.warnUnknownRoutes(matchedRoutes, distributionRoutes, userRoutes)
    }

    override suspend fun sendDirect(message: Notify, routingData: NotifyRoutingData.Direct) {
        when (val directRoutingData = routingData.directRoutingData) {
            is EmailDirectRoutingData -> {
                val emailDistribution = directRoutingData.emailDistribution
                if (emailDistribution != null) {
                    sendViaSendGrid(emailDistribution, message)
                }
                val users = directRoutingData.users
                if (!users.isNullOrEmpty()) {
                    users.forEach {
                        sendToUserNameViaSendGrid(it, message)
                    }
                }
            }
            else -> {
                LOG.debug("Direct routing data on notification ${message.notifyId} is of incompatible type for gateway ${config.id}, ignoring")
            }
        }
    }

    private suspend fun sendViaSendGrid(emailDistribution: EmailDistribution, message: Notify) {
        send(emailBuilder.buildEmail(emailDistribution, message))
    }

    private suspend fun sendToUserNameViaSendGrid(recipientUserName: String?, message: Notify) {
        try {
            send(emailBuilder.buildEmail(recipientUserName, message))
        } catch (e: Exception) {
            LOG.warn("Error while building email for user $recipientUserName", e)
        }
    }

    private fun send(email: Request) {
        try {
            val response = sendGrid.api(email)

            LOG.info(
                "Received {} response from SendGrid{}",
                response.statusCode,
                if (response.body.isEmpty()) "" else ": " + response.body
            )

            if (response.statusCode > 299) {
                LOG.error(
                    "Error sending email confirmation:\n\"" + email.body + "\"\nReceived " +
                        response.statusCode + " response with body: " + response.body
                )
            }
        } catch (e: IOException) {
            LOG.error("Error while sending SendGrid email $email", e)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(SendGridGateway::class.java)
    }
}
