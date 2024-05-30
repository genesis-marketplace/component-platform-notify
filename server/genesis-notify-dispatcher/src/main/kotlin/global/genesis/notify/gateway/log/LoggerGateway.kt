package global.genesis.notify.gateway.log

import global.genesis.gen.dao.Notify
import global.genesis.notify.gateway.Gateway
import global.genesis.notify.router.NotifyRoutingData
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

class LoggerGateway(defaultLevel: Level) : Gateway {
    private val log: (String) -> Unit = when (defaultLevel) {
        Level.DEBUG -> { msg -> LOG.debug(msg) }
        Level.ERROR -> { msg -> LOG.error(msg) }
        Level.WARN -> { msg -> LOG.warn(msg) }
        Level.INFO -> { msg -> LOG.info(msg) }
        Level.TRACE -> { msg -> LOG.trace(msg) }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(LoggerGateway::class.java)
    }

    override suspend fun sendDirect(message: Notify, routeData: NotifyRoutingData.Direct) {
        log(routeData, message)
    }

    override suspend fun sendViaTopic(message: Notify, routeData: NotifyRoutingData.Topic) {
        log(routeData, message)
    }

    private fun log(routingData: NotifyRoutingData, message: Notify) {
        val logMessage = "LogGateway: message: $message routingData: $routingData"
        log(logMessage)
    }
}
