package global.genesis.notify.gateway.common

import global.genesis.db.rx.entity.multi.AsyncEntityDb
import global.genesis.gen.dao.Notify
import global.genesis.gen.dao.NotifyRoute
import global.genesis.gen.view.entity.EmailDistributionRoute
import global.genesis.gen.view.entity.EmailUserRoute
import global.genesis.notify.message.common.EmailDistribution
import global.genesis.notify.router.NotifyRouteCache
import global.genesis.notify.router.NotifyRoutingData
import global.genesis.notify.router.UserNameResolver
import global.genesis.notify.router.UserPermissionData
import global.genesis.notify.router.UserRouteData
import org.slf4j.LoggerFactory

object RouteUtils {
    private val LOG = LoggerFactory.getLogger(RouteUtils::class.java)

    fun NotifyRoutingData.Topic.getMatchedTopicRoutes(gatewayId: String) = this
        .routes.filter { it.gatewayId == gatewayId }
        .toSet()

    fun warnUnknownRoutes(
        matchedRoutes: Set<NotifyRoute>,
        distributionRoutes: Set<EmailDistributionRoute>,
        userRoutes: Set<EmailUserRoute>
    ) {
        val matchedRouteIds = distributionRoutes.map { it.notifyRouteId }.toSet() +
            userRoutes.map { it.notifyRouteId }.toSet()
        val unknownRouteIds = matchedRoutes.map { it.notifyRouteId }.toSet() - matchedRouteIds
        unknownRouteIds.forEach {
            LOG.warn("Unable to find compatible routing data for matched route $it")
        }
    }

    fun getEmailUserRouteCache(db: AsyncEntityDb): NotifyRouteCache<EmailUserRoute> {
        return NotifyRouteCache(
            name = "EmailUserRouteCache",
            subscriber = db.bulkSubscribe(
                index = EmailUserRoute.ById,
                backwardJoins = true
            ),
            keyFunction = { it.notifyRouteId }
        )
    }

    fun getEmailDistRouteCache(db: AsyncEntityDb): NotifyRouteCache<EmailDistributionRoute> {
        return NotifyRouteCache(
            name = "EmailDistributionRouteCache",
            subscriber = db.bulkSubscribe(
                index = EmailDistributionRoute.ById,
                backwardJoins = true
            ),
            keyFunction = { it.notifyRouteId }
        )
    }

    suspend fun resolveUserNames(message: Notify, matchedRoutes: Set<EmailUserRoute>, userNameResolver: UserNameResolver): Set<String> {
        return userNameResolver.getMatchedGatewayUsers(
            notify = message,
            routes = matchedRoutes,
            routeToUserData = { UserRouteData(it.notifyRouteId, it.entityIdType, it.entityId, it.excludeSender) },
            routeToPermissionData = { UserPermissionData(it.rightCode, it.authCacheName) }
        )
    }

    fun EmailDistributionRoute.getEmailDistribution(): EmailDistribution {
        return EmailDistribution(
            to = getEmailList(this.emailTo),
            cc = getEmailList(this.emailCc),
            bcc = getEmailList(this.emailBcc)
        )
    }

    private fun getEmailList(email: String?): List<String> {
        return if (email.isNullOrEmpty()) {
            emptyList()
        } else {
            email.split(",")
        }
    }
}
