import global.genesis.notify.message.request.ConfiguredRoute
import global.genesis.notify.message.request.EmptyRequest
import global.genesis.notify.message.request.NotifyRoutesReply
import global.genesis.notify.pal.NotifyDefinition

val gatewayIdsByRouteInfo = inject<NotifyDefinition>().gatewayConfigs.gatewayIdsByRouteInfo

requestReplies {
    requestReply<EmptyRequest, NotifyRoutesReply>(name = "CONFIGURED_NOTIFY_ROUTES") {
        replySingle {
            NotifyRoutesReply(
                configuredRoutes = gatewayIdsByRouteInfo.map { ConfiguredRoute(it.key, it.value) }.toSet()
            )
        }
    }
}