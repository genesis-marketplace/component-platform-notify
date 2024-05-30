package global.genesis.notify.router

import global.genesis.gen.dao.NotifyRoute
import global.genesis.notify.message.event.DirectRoutingData

sealed class NotifyRoutingData {

    data class Topic(
        val routes: Set<NotifyRoute>
    ) : NotifyRoutingData()

    data class Direct(
        val directRoutingData: DirectRoutingData
    ) : NotifyRoutingData()
}
