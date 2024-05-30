package global.genesis.notify.router

import com.google.inject.Inject
import global.genesis.commons.annotation.Module
import global.genesis.config.system.SystemDefinitionService
import global.genesis.db.DbRecord
import global.genesis.db.entity.ViewEntity
import global.genesis.db.rx.RxDb
import global.genesis.db.rx.entity.multi.AsyncEntityDb
import global.genesis.gen.dao.Notify
import global.genesis.gen.dao.User
import global.genesis.gen.dao.enums.EntityIdType
import global.genesis.session.AuthCacheFactory
import global.genesis.session.ProfileUserCache
import global.genesis.session.RightSummaryCache
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.rx3.await
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private const val PERMS_FIELD_KEY = "ADMIN_PERMISSION_ENTITY_FIELD"
private const val PERMS_TABLE_KEY = "ADMIN_PERMISSION_ENTITY_TABLE"

/**
 * This class is for use with route tables that target single or groups of users
 * within the Genesis system. The entity ID and entity ID type fields are used to define
 * all, or a subset of, users in the system.
 *
 * The class then applies permissioning information present on the rules to filter the
 * list of users by either right code, or visibility on an entity linked to the notification
 * by ID.
 *
 */
@Module
class UserNameResolver @Inject constructor(
    private val profileUserCache: ProfileUserCache,
    private val entityDb: AsyncEntityDb,
    private val rxDb: RxDb,
    private val rightSummaryCache: RightSummaryCache,
    private val authCacheFactory: AuthCacheFactory,
    systemDefinitionService: SystemDefinitionService
) {
    private val permsField = systemDefinitionService[PERMS_FIELD_KEY].orElse(null)
    private val permsTable = systemDefinitionService[PERMS_TABLE_KEY].orElse(null)
    private val tableName = "USER_${permsTable}_MAP"
    private val indexName = "USER_${permsTable}_MAP_BY_$permsField"

    suspend fun <V : ViewEntity> getMatchedGatewayUsers(
        notify: Notify,
        routes: Set<V>,
        routeToUserData: (V) -> UserRouteData,
        routeToPermissionData: (V) -> UserPermissionData
    ): Set<String> =
        routes.flatMap {
            getAndFilterUsers(notify, routeToUserData(it), routeToPermissionData(it))
        }.toSet()

    private suspend fun getAndFilterUsers(
        notify: Notify,
        routeData: UserRouteData,
        permissionData: UserPermissionData
    ): Set<String> {
        val users = getUsers(notify, routeData, permissionData)
        return users.filter {
            !(it == notify.sender && routeData.excludeSender) &&
                userHasRights(permissionData, it) &&
                userHasVisibility(it, routeData.entityIdType, permissionData.authCacheName, notify.permissioningEntityId)
        }.toSet()
    }

    /**
     * Check to see if the user has visibility on the given entity ID in the specified auth cache.
     *
     * If the entity ID type is ALL and the auth cache name is given, there is no need to filter here,
     * as all users with permission have already been obtained from the auth cache to avoid a getBulk call.
     */
    private fun userHasVisibility(user: String, entityIdType: EntityIdType, authCacheName: String?, permissioningEntityId: String?): Boolean {
        return authCacheName == null ||
            entityIdType == EntityIdType.ALL ||
            authCacheFactory.newReader(authCacheName).isAuthorised(permissioningEntityId, user)
    }

    private fun userHasRights(permissionData: UserPermissionData, user: String) =
        permissionData.rightCode == null || rightSummaryCache.userHasRight(user, permissionData.rightCode)

    private suspend fun getUsers(
        notify: Notify,
        routeData: UserRouteData,
        permissionData: UserPermissionData
    ): Set<String> {
        val entityIdType = routeData.entityIdType
        val entityId = routeData.entityId
        val routeId = routeData.routeId
        val authCacheName = permissionData.authCacheName
        return when {
            entityIdType == EntityIdType.USER_NAME -> {
                if (entityId != null) {
                    setOf(entityId)
                } else {
                    LOG.warn("Route $routeId with entityId type of USER_NAME was matched but the route does not specify a USER_NAME")
                    emptySet()
                }
            }

            entityIdType == EntityIdType.PROFILE_NAME -> {
                if (entityId != null) {
                    profileUserCache.getUsers(entityId)
                } else {
                    LOG.warn("Route $routeId with entityId type of PROFILE_NAME was matched but the route does not specify a PROFILE_NAME")
                    emptySet()
                }
            }

            entityIdType == EntityIdType.SELF -> {
                val sender = notify.sender
                if (sender != null) {
                    setOf(sender)
                } else {
                    LOG.warn("Route $routeId with entityId type of SELF was matched but the notification did not specify a sender")
                    emptySet()
                }
            }

            entityIdType == EntityIdType.ALL && authCacheName != null -> {
                if (notify.permissioningEntityId != null) {
                    val authCache = authCacheFactory.newReader(mapName = authCacheName)
                    authCache.getAuthorisedUsers(notify.permissioningEntityId)
                } else {
                    LOG.warn(
                        "Route $routeId with entityId type of ALL was matched, filtered using auth cache $authCacheName but " +
                            "the notification did not specify an entity ID for permission checking"
                    )
                    emptySet()
                }
            }

            entityIdType == EntityIdType.ALL -> {
                return entityDb.getBulk<User>()
                    .filter { it.status == "ENABLED" }
                    .map { it.userName }
                    .toSet()
            }

            entityIdType.toString() == permsField -> {
                val searchRecord = DbRecord(tableName)
                searchRecord.setString(permsField, entityId)
                rxDb.getRange(searchRecord, indexName, 1)
                    .toList()
                    .await()
                    .mapNotNull { it.getString("USER_NAME") }
                    .toSet()
            }

            else -> emptySet()
        }
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(UserNameResolver::class.java)
    }
}
