package global.genesis.notify.gateway.teams

import global.genesis.notify.message.request.RouteInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TeamsGatewayConfigTest {
    private val routeInfoSet = setOf(
        RouteInfo(
            dataServerHandler = "ALL_MS_TEAMS_ROUTES",
            createEventHandler = "MS_TEAMS_ROUTE_CREATE",
            updateEventHandler = "MS_TEAMS_ROUTE_UPDATE",
            deleteEventHandler = "MS_TEAMS_ROUTE_DELETE",
            displayName = "MS Teams Notification"
        )
    )

    @Test
    fun `hard coded route info is accurate`() {
        val teamsGatewayConfig = TeamsGatewayConfig("Teams")
        assertEquals(routeInfoSet, teamsGatewayConfig.routeInfoSet)
    }
}
