package global.genesis.notify.message.event

import global.genesis.jackson.core.GenesisJacksonMapper.Companion.jsonToObject
import global.genesis.jackson.core.GenesisJacksonMapper.Companion.toPrettyJsonString
import global.genesis.notify.message.common.EmailDistribution
import org.junit.jupiter.api.Test
import kotlin.test.assertIs

class DirectRoutingDataTest {

    @Test
    fun `EmailDirectRoutingData can be correctly deserialised using embedded type info`() {
        val data = EmailDirectRoutingData(
            gatewayId = "TEST",
            emailDistribution = EmailDistribution(
                to = listOf("test@test.com"),
                cc = listOf("test@test.com"),
                bcc = listOf("test@test.com")
            ),
            users = setOf("TEST")
        )

        val encoded = data.toPrettyJsonString()
        println(encoded)
        val decoded = encoded.jsonToObject<DirectRoutingData>()
        assertIs<EmailDirectRoutingData>(decoded)
    }

    @Test
    fun `ScreenDirectRoutingData can be correctly deserialised using embedded type info`() {
        val data = ScreenDirectRoutingData(
            gatewayId = "TEST",
            users = setOf("TEST")
        )

        val encoded = data.toPrettyJsonString()
        println(encoded)
        val decoded = encoded.jsonToObject<DirectRoutingData>()
        assertIs<ScreenDirectRoutingData>(decoded)
    }
}
