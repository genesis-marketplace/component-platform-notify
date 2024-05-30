package global.genesis.notify.router

import global.genesis.gen.dao.Notify
import global.genesis.gen.dao.enums.RoutingType
import global.genesis.notify.gateway.email.EmailGatewayConfig
import global.genesis.notify.gateway.screen.ScreenGatewayConfig
import global.genesis.notify.message.event.EmailDirectRoutingData
import global.genesis.notify.message.event.ScreenDirectRoutingData
import global.genesis.notify.pal.NotifyDefinition
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BasicNotifyRouterTest {

    @Test
    fun `code MY_SCREEN is a valid basic routing code`() {
        val message = Notify {
            header = "Test"
            body = "Test message"
            routingType = RoutingType.BASIC
            routingData = "MY_SCREEN"
            sender = "JohnDoe"
        }
        val definition = NotifyDefinition()
        definition.gatewayConfigs.configById["Screen"] = ScreenGatewayConfig("Screen")
        definition.gatewayConfigs.configById["Email"] = EmailGatewayConfig("Email")
        val router = BasicNotifyRouter(definition)
        val routingData = router.getRoutingData(message)

        assertIs<NotifyRoutingData.Direct>(routingData)
        val directRoutingData = routingData.directRoutingData
        assertEquals("Screen", directRoutingData.gatewayId)
        assertIs<ScreenDirectRoutingData>(directRoutingData)
        assertTrue(directRoutingData.users!!.contains("JohnDoe"))
    }

    @Test
    fun `code MY_EMAIL is a valid basic routing code`() {
        val message = Notify {
            header = "Test"
            body = "Test message"
            routingType = RoutingType.BASIC
            routingData = "MY_EMAIL"
            sender = "JohnDoe"
        }
        val definition = NotifyDefinition()
        definition.gatewayConfigs.configById["Screen"] = ScreenGatewayConfig("Screen")
        definition.gatewayConfigs.configById["Email"] = EmailGatewayConfig("Email")
        val router = BasicNotifyRouter(definition)
        val routingData = router.getRoutingData(message)

        assertIs<NotifyRoutingData.Direct>(routingData)
        val directRoutingData = routingData.directRoutingData
        assertEquals("Email", directRoutingData.gatewayId)
        assertIs<EmailDirectRoutingData>(directRoutingData)
        assertTrue(directRoutingData.users!!.contains("JohnDoe"))
    }
}
