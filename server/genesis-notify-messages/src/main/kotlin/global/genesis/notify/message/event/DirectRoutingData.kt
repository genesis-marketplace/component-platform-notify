package global.genesis.notify.message.event

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.As
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id
import global.genesis.notify.message.common.EmailDistribution

/**
 * These classes are used for the direct routing strategy.
 * They contain complex routing info that has to be calculated programatically
 * and cannot be easily expressed through the topic routing system.
 *
 * These objects are serialised as data in the ROUTING_DATA column in the notify table.
 * In this way, the NOTIFY_MANAGER can communicate this complex data to the NOTIFY_DISPATCHER.
 */
@JsonTypeInfo(
    use = Id.CLASS,
    include = As.PROPERTY,
    property = "TYPE",
    visible = true
)
abstract class DirectRoutingData {
    abstract var gatewayId: String?
}

class EmailDirectRoutingData(
    override var gatewayId: String? = null,
    var emailDistribution: EmailDistribution? = null,
    var users: Set<String>? = null
) : DirectRoutingData()

class ScreenDirectRoutingData(
    override var gatewayId: String? = null,
    var users: Set<String>? = null
) : DirectRoutingData()
