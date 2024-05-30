package global.genesis.notify.gateway.screen

import global.genesis.db.rx.entity.multi.AsyncEntityDb
import global.genesis.gen.dao.Notify
import global.genesis.gen.dao.NotifyAlert
import global.genesis.gen.view.entity.ScreenRoute
import global.genesis.notify.gateway.Gateway
import global.genesis.notify.gateway.common.RouteUtils.getMatchedTopicRoutes
import global.genesis.notify.message.event.ScreenDirectRoutingData
import global.genesis.notify.router.NotifyRouteCache
import global.genesis.notify.router.NotifyRoutingData
import global.genesis.notify.router.UserNameResolver
import global.genesis.notify.router.UserPermissionData
import global.genesis.notify.router.UserRouteData
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.slf4j.LoggerFactory
import javax.inject.Inject

class GenesisScreenGateway @Inject constructor(
    private val config: ScreenGatewayConfig,
    private val db: AsyncEntityDb,
    private val userNameResolver: UserNameResolver,
    private val screenAlertExpiryManager: ScreenAlertExpiryManager
) : Gateway {

    private val screenRouteCache = NotifyRouteCache(
        name = "ScreenRouteCache",
        subscriber = db.bulkSubscribe(
            index = ScreenRoute.ById,
            backwardJoins = true
        ),
        keyFunction = { it.notifyRouteId }
    )

    init {
        runBlocking {
            LOG.info("Waiting for screenRouteCache to be primed")
            screenRouteCache.primed.await()
            LOG.info("Waiting for screenAlertExpiryManager to be primed")
            screenAlertExpiryManager.primed.await()
            LOG.info("screenRouteCache primed")
            LOG.info("screenAlertExpiryManager primed")
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(GenesisScreenGateway::class.java)
    }

    override suspend fun sendDirect(message: Notify, routeData: NotifyRoutingData.Direct) {
        val directRoutingData = routeData.directRoutingData
        when (directRoutingData) {
            is ScreenDirectRoutingData -> {
                if (!directRoutingData.users.isNullOrEmpty()) {
                    directRoutingData.users!!.forEach {
                        insertAlert(message, it, null)
                    }
                }
            }
            else -> {
                LOG.debug("Direct routing data on notification ${message.notifyId} is of incompatible type for gateway ${config.id}, ignoring")
            }
        }
    }

    override suspend fun sendViaTopic(message: Notify, routeData: NotifyRoutingData.Topic) {
        val matchedRoutes = routeData.getMatchedTopicRoutes(config.id)

        LOG.debug("GenesisScreenGateway message: {}", message)
        val screenRoutes: Set<ScreenRoute> =
            screenRouteCache.getNotifyRoutes(routesToFind = matchedRoutes, logWarningOnNotFound = true)
        LOG.debug("GenesisScreenGateway screenRoutes: {}", screenRoutes)
        val expiry = screenAlertExpiryManager.calculateExpiry(screenRoutes)
        val userNames: Set<String> = resolveUserNames(message, screenRoutes)
        if (userNames.isEmpty()) {
            LOG.trace(
                "Found matching routes $matchedRoutes for screen alerts, but no valid users matched" +
                    " configuration. Consider reviewing the routing records."
            )
        }
        userNames.forEach {
            insertAlert(message, it, expiry)
        }
    }

    private suspend fun resolveUserNames(message: Notify, matchedRoutes: Set<ScreenRoute>): Set<String> {
        return userNameResolver.getMatchedGatewayUsers(
            notify = message,
            routes = matchedRoutes,
            routeToUserData = { UserRouteData(it.notifyRouteId, it.entityIdType, it.entityId, it.excludeSender) },
            routeToPermissionData = { UserPermissionData(it.rightCode, it.authCacheName) }
        )
    }

    private suspend fun insertAlert(message: Notify, userName: String, expiry: DateTime?) {
        LOG.info("insertAlert message: {}", message)
        val alert = NotifyAlert {
            this.message = message.body
            this.userName = userName
            this.expiry = expiry
            notifySeverity = message.notifySeverity
            createdAt = DateTime.now().withZone(DateTimeZone.UTC)
            header = message.header
            topic = message.topic
            tableEntityId = message.tableEntityId
        }
        val insertResult = db.insert(alert)
        LOG.trace("insert result is {}", insertResult)
    }
}
