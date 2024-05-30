package global.genesis.notify.router

import global.genesis.gen.dao.enums.EntityIdType

data class UserRouteData(
    val routeId: String,
    val entityIdType: EntityIdType,
    val entityId: String?,
    val excludeSender: Boolean
)

data class UserPermissionData(
    val rightCode: String? = null,
    val authCacheName: String? = null
)
