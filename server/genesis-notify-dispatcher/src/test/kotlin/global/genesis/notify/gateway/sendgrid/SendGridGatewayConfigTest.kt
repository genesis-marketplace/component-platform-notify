package global.genesis.notify.gateway.sendgrid

import global.genesis.notify.message.request.RouteInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SendGridGatewayConfigTest {
    private val routeInfoSet = setOf(
        RouteInfo(
            dataServerHandler = "ALL_EMAIL_DISTRIBUTION_ROUTES",
            createEventHandler = "EMAIL_DISTRIBUTION_ROUTE_CREATE",
            updateEventHandler = "EMAIL_DISTRIBUTION_ROUTE_UPDATE",
            deleteEventHandler = "EMAIL_DISTRIBUTION_ROUTE_DELETE",
            displayName = "Email Distribution Notification"
        ),
        RouteInfo(
            dataServerHandler = "ALL_EMAIL_USER_ROUTES",
            createEventHandler = "EMAIL_USER_ROUTE_CREATE",
            updateEventHandler = "EMAIL_USER_ROUTE_UPDATE",
            deleteEventHandler = "EMAIL_USER_ROUTE_DELETE",
            displayName = "Email User Notification"
        )
    )

    @Test
    fun `hard coded route info is accurate`() {
        val sendGridGatewayConfig = SendGridGatewayConfig("SendGrid")
        assertEquals(routeInfoSet, sendGridGatewayConfig.routeInfoSet)
    }
}
