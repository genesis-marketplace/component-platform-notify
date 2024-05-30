package global.genesis.notify

import global.genesis.commons.model.GenesisSet
import global.genesis.commons.standards.MessageType
import global.genesis.net.channel.GenesisChannel
import global.genesis.net.handler.MessageListener
import global.genesis.testsupport.AbstractGenesisTestSupport
import global.genesis.testsupport.EventResponse
import global.genesis.testsupport.GenesisTestConfig
import org.awaitility.Awaitility
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MetaDataTest : AbstractGenesisTestSupport<EventResponse>(
    GenesisTestConfig {
        packageName = "global.genesis.eventhandler.pal"
        genesisHome = "/GenesisHome/"
        initialDataFile = "notify-data.csv"
        scriptFileName = "genesis-notify-eventhandler.kts"
        useTempClassloader = true
    }
) {

    override fun systemDefinition(): Map<String, Any> = mapOf(
        "IS_SCRIPT" to "true",
        "MESSAGE_CLIENT_PROCESS_NAME" to "GENESIS_ROUTER",
        "DOCUMENT_STORE_BASEDIR" to "src/test/resources/genesisHome/site-specific/incoming",
        "LOCAL_STORAGE_FOLDER" to "LOCAL_STORAGE",
        "STORAGE_STRATEGY" to "LOCAL"
    )

    @Test
    fun test() {
        var metaAck: GenesisSet? = null
        var featureAck: GenesisSet? = null

        messageClient.handler.addListener(
            MessageListener { set: GenesisSet, _: GenesisChannel? ->
                println(set)
                if (metaAck == null) {
                    metaAck = set
                } else {
                    featureAck = set
                }
            }
        )

        val metaRequest = GenesisSet()
        metaRequest.setString(MessageType.MESSAGE_TYPE, "META_REQUEST")
        metaRequest.setGenesisSet("DETAILS", GenesisSet())

        messageClient.sendMessage(metaRequest)

        Awaitility.await().until { metaAck != null }

        assertEquals("META_ACK", metaAck?.getString(MessageType.MESSAGE_TYPE))
        val features: List<GenesisSet?>? = metaAck?.getArray("FEATURE")
        assertEquals(19, features?.size)
    }
}
