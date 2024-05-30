package global.genesis.notify.router

import global.genesis.gen.dao.Notify
import global.genesis.jackson.core.GenesisJacksonMapper.Companion.jsonToObject
import global.genesis.notify.message.event.DirectRoutingData

/**
 * Implementation of [NotifyRouter] that routes directly to specific
 * gateways using the routing information passed in the ROUTING_INFORMATION
 * field on the notify record.
 *
 * Direct routing allows the users to bypass topic routing and create their
 * own recipient lists programatically, for more complex use cases.
 *
 * @author tgosling
 */
class DirectNotifyRouter : NotifyRouter {
    override fun getRoutingData(message: Notify): NotifyRoutingData {
        val directRoutingData = message.routingData!!.jsonToObject<DirectRoutingData>()
        return NotifyRoutingData.Direct(
            directRoutingData
        )
    }
}
