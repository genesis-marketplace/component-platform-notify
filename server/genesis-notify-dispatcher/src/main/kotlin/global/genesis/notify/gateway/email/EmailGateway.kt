package global.genesis.notify.gateway.email

import com.google.inject.Inject
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
import org.simplejavamail.api.email.Email
import org.simplejavamail.api.mailer.Mailer
import org.simplejavamail.mailer.MailerBuilder
import org.slf4j.LoggerFactory

/**
 * Implementation of [Gateway] for sending email messages to an SMTP server.
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
class EmailGateway(
    private val config: EmailGatewayConfig,
    private val userNameResolver: UserNameResolver,
    private val emailBuilder: EmailBuilder,
    private val mailer: Mailer,
    db: AsyncEntityDb
) : Gateway {

    @Inject
    constructor(
        config: EmailGatewayConfig,
        db: AsyncEntityDb,
        userNameResolver: UserNameResolver,
        emailBuilder: EmailBuilder
    ) : this(
        config,
        userNameResolver,
        emailBuilder,
        MailerBuilder
            .withSMTPServer(config.smtpHost, config.smtpPort, config.smtpUser, config.smtpPw)
            .withTransportStrategy(config.smtpProtocol)
            .buildMailer(),
        db
    )

    private val staticDistribution = config.staticDistribution
    private val emailDistributionRouteCache = RouteUtils.getEmailDistRouteCache(db)
    private val emailUserRouteCache = RouteUtils.getEmailUserRouteCache(db)

    override suspend fun sendDirect(message: Notify, routeData: NotifyRoutingData.Direct) {
        when (val directRoutingData = routeData.directRoutingData) {
            is EmailDirectRoutingData -> {
                val emailDistribution = directRoutingData.emailDistribution
                if (emailDistribution != null) {
                    sendToEmail(emailDistribution, message)
                }
                val users = directRoutingData.users
                if (!users.isNullOrEmpty()) {
                    users.forEach {
                        sendToUserName(it, message)
                    }
                }
            }
            else -> {
                LOG.warn("Direct routing data on notification ${message.notifyId} is of incompatible type for gateway ${config.id}, ignoring")
            }
        }
    }

    override suspend fun sendViaTopic(message: Notify, routeData: NotifyRoutingData.Topic) {
        val matchedRoutes = routeData.getMatchedTopicRoutes(config.id)

        if (matchedRoutes.isNotEmpty()) {
            val distributionRoutes: Set<EmailDistributionRoute> = emailDistributionRouteCache.getNotifyRoutes(matchedRoutes)
            val fullDistribution = distributionRoutes.fold(staticDistribution) { acc, emailRoute ->
                acc + emailRoute.getEmailDistribution()
            }
            if (fullDistribution.hasRecipients()) {
                sendToEmail(fullDistribution, message)
            }

            val userRoutes: Set<EmailUserRoute> = emailUserRouteCache.getNotifyRoutes(matchedRoutes)
            val userNames = RouteUtils.resolveUserNames(message, userRoutes, userNameResolver)
            userNames.forEach {
                sendToUserName(it, message)
            }

            RouteUtils.warnUnknownRoutes(matchedRoutes, distributionRoutes, userRoutes)
        } else {
            LOG.warn("No topic routes matched gateway ${config.id} for notify ${message.notifyId}, ignoring")
        }
    }

    private suspend fun sendToEmail(emailDistribution: EmailDistribution, message: Notify) {
        send(emailBuilder.buildEmail(emailDistribution, message))
    }

    private suspend fun sendToUserName(recipientUserName: String?, message: Notify) {
        try {
            send(emailBuilder.buildEmail(recipientUserName, message))
        } catch (e: Exception) {
            LOG.warn("Error while building email for user $recipientUserName", e)
        }
    }

    private fun send(email: Email) {
        try {
            mailer.sendMail(email)
                .whenComplete { _, t: Throwable? ->
                    if (t != null) {
                        LOG.warn("Error while sending email $email", t)
                    } else {
                        LOG.debug("Successfully sent email {}", email)
                    }
                }
        } catch (e: Exception) {
            LOG.error("Error while sending email $email", e)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(EmailGateway::class.java)
    }
}
