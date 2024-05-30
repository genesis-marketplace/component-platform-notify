package global.genesis.notify.gateway.log

import global.genesis.notify.message.request.RouteInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LoggerGatewayConfigTest {

    private val routeInfoSet = setOf(
        RouteInfo(
            dataServerHandler = "ALL_LOG_ROUTES",
            createEventHandler = "LOG_ROUTE_CREATE",
            updateEventHandler = "LOG_ROUTE_UPDATE",
            deleteEventHandler = "LOG_ROUTE_DELETE",
            displayName = "Log Notification"
        )
    )

    @Test
    fun `hard coded route info is accurate`() {
        val loggerGatewayConfig = LoggerGatewayConfig("Log")
        assertEquals(routeInfoSet, loggerGatewayConfig.routeInfoSet)
    }
}
