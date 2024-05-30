package global.genesis.notify.message.request

data class RouteInfo(
    val dataServerHandler: String,
    val createEventHandler: String,
    val updateEventHandler: String,
    val deleteEventHandler: String,
    val displayName: String
)

data class ConfiguredRoute(
    val routeInfo: RouteInfo,
    val supportedGatewayIds: Set<String>
)

data class NotifyRoutesReply(
    val configuredRoutes: Set<ConfiguredRoute>
)
