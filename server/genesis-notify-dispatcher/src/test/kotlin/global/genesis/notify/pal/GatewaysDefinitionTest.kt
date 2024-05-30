package global.genesis.notify.pal

import com.google.inject.Injector
import global.genesis.gen.dao.Notify
import global.genesis.notify.gateway.Gateway
import global.genesis.notify.gateway.email.EmailGatewayConfig
import global.genesis.notify.gateway.log.LoggerGatewayConfig
import global.genesis.notify.gateway.screen.ScreenGatewayConfig
import global.genesis.notify.gateway.teams.TeamsGatewayConfig
import global.genesis.notify.message.request.RouteInfo
import global.genesis.notify.router.NotifyRoutingData
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GatewaysDefinitionTest {

    @Test
    fun `multiple EMAIL gateways types - correctly tracks supported gateways ids for a given route`() {
        val gatewaysDefinition = GatewaysDefinition()
        val emailGatewaySupportedRoutes = EmailGatewayConfig("Email").routeInfoSet

        gatewaysDefinition.email("email1") {}
        gatewaysDefinition.email("email2") {}

        emailGatewaySupportedRoutes.forEach {
            val gatewayIds = gatewaysDefinition.gatewayIdsByRouteInfo[it]!!
            assertEquals(setOf("email1", "email2"), gatewayIds)
        }
    }

    @Test
    fun `multiple LOGGER gateways types - correctly tracks supported gateways ids for a given route`() {
        val gatewaysDefinition = GatewaysDefinition()
        val loggerGatewaySupportedRoutes = LoggerGatewayConfig("Log").routeInfoSet

        gatewaysDefinition.log("log1") {}
        gatewaysDefinition.log("log2") {}

        loggerGatewaySupportedRoutes.forEach {
            val gatewayIds = gatewaysDefinition.gatewayIdsByRouteInfo[it]!!
            assertEquals(setOf("log1", "log2"), gatewayIds)
        }
    }

    @Test
    fun `multiple SCREEN gateways types - correctly tracks supported gateways ids for a given route`() {
        val gatewaysDefinition = GatewaysDefinition()
        val screenGatewaySupportedRoutes = ScreenGatewayConfig("Screen").routeInfoSet

        gatewaysDefinition.screen("screen1") {}
        gatewaysDefinition.screen("screen2") {}

        screenGatewaySupportedRoutes.forEach {
            val gatewayIds = gatewaysDefinition.gatewayIdsByRouteInfo[it]!!
            assertEquals(setOf("screen1", "screen2"), gatewayIds)
        }
    }

    @Test
    fun `multiple TEAMS gateways types - correctly tracks supported gateways ids for a given route`() {
        val gatewaysDefinition = GatewaysDefinition()
        val teamsGatewaySupportedRoutes = TeamsGatewayConfig("Teams").routeInfoSet

        gatewaysDefinition.teams("teams1") {}
        gatewaysDefinition.teams("teams2") {}

        teamsGatewaySupportedRoutes.forEach {
            val gatewayIds = gatewaysDefinition.gatewayIdsByRouteInfo[it]!!
            assertEquals(setOf("teams1", "teams2"), gatewayIds)
        }
    }

    @Test
    fun `multiple DEVELOPER extended gateways types - correctly tracks supported gateways ids for a given route`() {
        val gatewaysDefinition = GatewaysDefinition()
        val customGateway = object : Gateway {
            override suspend fun sendDirect(message: Notify, routeData: NotifyRoutingData.Direct) {
            }

            override suspend fun sendViaTopic(message: Notify, routeData: NotifyRoutingData.Topic) {
            }
        }
        val customGatewayConfig = object : GatewayConfig {
            override val id: String
                get() = "Custom"

            override fun validate() = listOf("")

            override fun build(injector: Injector) = customGateway

            override val routeInfoSet = setOf(
                RouteInfo(
                    dataServerHandler = "SOME_ROUTE",
                    createEventHandler = "SOME_ROUTE_CREATE",
                    updateEventHandler = "SOME_ROUTE_UPDATE",
                    deleteEventHandler = "SOME_ROUTE_DELETE",
                    displayName = "MS Teams Notification"
                )
            )
        }
        val customGatewaySupportedRoutes = customGatewayConfig.routeInfoSet

        gatewaysDefinition.registerGateway("extended1", customGatewayConfig)
        gatewaysDefinition.registerGateway("extended2", customGatewayConfig)

        customGatewaySupportedRoutes.forEach {
            val gatewayIds = gatewaysDefinition.gatewayIdsByRouteInfo[it]!!
            assertEquals(setOf("extended1", "extended2"), gatewayIds)
        }
    }

    @Test
    fun `different gateway types with same route info - correctly tracks supported gateways ids for a given route`() {
        val gatewaysDefinition = GatewaysDefinition()
        val emailGatewaySupportedRoutes = EmailGatewayConfig("email1").routeInfoSet

        gatewaysDefinition.email("email1") {}
        gatewaysDefinition.sendGrid("send-grid")

        emailGatewaySupportedRoutes.forEach {
            val gatewayIds = gatewaysDefinition.gatewayIdsByRouteInfo[it]!!
            assertEquals(setOf("email1", "send-grid"), gatewayIds)
            assertEquals(2, gatewaysDefinition.gatewayIdsByRouteInfo.keys.size)
        }
    }
}
