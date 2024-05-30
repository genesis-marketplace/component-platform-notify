package global.genesis.notify.gateway

import global.genesis.gen.dao.Notify
import global.genesis.notify.router.NotifyRoutingData
import java.io.Closeable

interface Gateway : Closeable {

    suspend fun sendDirect(message: Notify, routeData: NotifyRoutingData.Direct)

    suspend fun sendViaTopic(message: Notify, routeData: NotifyRoutingData.Topic)

    override fun close() {}
}
