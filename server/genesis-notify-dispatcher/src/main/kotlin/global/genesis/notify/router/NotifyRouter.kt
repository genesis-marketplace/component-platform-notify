package global.genesis.notify.router

import global.genesis.gen.dao.Notify

interface NotifyRouter {
    fun getRoutingData(message: Notify): NotifyRoutingData
}
