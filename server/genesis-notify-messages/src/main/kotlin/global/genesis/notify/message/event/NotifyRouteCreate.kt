package global.genesis.notify.message.event

import global.genesis.gen.dao.enums.EntityIdType
import global.genesis.gen.dao.enums.TtlTimeUnit

interface RouteCreate {
    val gatewayId: String
    val topicMatch: String
}

data class EmailDistributionRouteCreate(
    override val gatewayId: String,
    override val topicMatch: String,
    val emailTo: Set<String> = emptySet(),
    val emailCc: Set<String> = emptySet(),
    val emailBcc: Set<String> = emptySet()
) : RouteCreate

data class EmailDistributionRouteUpdate(
    val notifyRouteId: String,
    override val gatewayId: String,
    override val topicMatch: String,
    val emailTo: Set<String> = emptySet(),
    val emailCc: Set<String> = emptySet(),
    val emailBcc: Set<String> = emptySet()
) : RouteCreate

data class EmailUserRouteCreate(
    override val gatewayId: String,
    override val topicMatch: String,
    val entityId: String? = null,
    val entityIdType: EntityIdType,
    val rightCode: String? = null,
    val authCacheName: String? = null,
    val excludeSender: Boolean = false
) : RouteCreate

data class LogRouteCreate(
    override val gatewayId: String,
    override val topicMatch: String
) : RouteCreate

data class MsTeamsRouteCreate(
    override val gatewayId: String,
    override val topicMatch: String,
    val url: String
) : RouteCreate

data class ScreenRouteCreate(
    override val gatewayId: String,
    override val topicMatch: String,
    val entityId: String? = null,
    val entityIdType: EntityIdType,
    val ttl: Long? = null,
    val ttlTimeUnit: TtlTimeUnit = TtlTimeUnit.NONE,
    val rightCode: String? = null,
    val authCacheName: String? = null,
    val excludeSender: Boolean = false
) : RouteCreate
