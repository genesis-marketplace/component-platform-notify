package global.genesis.notify.gateway.screen

import global.genesis.notify.message.request.RouteInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ScreenGatewayConfigTest {
    private val routeInfoSet = setOf(
        RouteInfo(
            dataServerHandler = "ALL_SCREEN_ROUTES",
            createEventHandler = "SCREEN_NOTIFY_ROUTE_CREATE",
            updateEventHandler = "SCREEN_NOTIFY_ROUTE_UPDATE",
            deleteEventHandler = "SCREEN_NOTIFY_ROUTE_DELETE",
            displayName = "Screen Notification"
        )
    )

    @Test
    fun `hard coded route info is accurate`() {
        val screenGatewayConfig = ScreenGatewayConfig("Screen")
        assertEquals(routeInfoSet, screenGatewayConfig.routeInfoSet)
    }
}
