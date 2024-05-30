package global.genesis.notify.pal.parse

import global.genesis.notify.gateway.email.EmailGatewayConfig
import global.genesis.notify.pal.NotifyFileReader
import global.genesis.testsupport.AbstractGenesisTestSupport
import global.genesis.testsupport.EventResponse
import global.genesis.testsupport.GenesisTestConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NotifyFileReaderTest : AbstractGenesisTestSupport<EventResponse>(
    GenesisTestConfig {
        packageNames = listOf("global.genesis.notify", "global.genesis.file.storage.provider").toMutableList()
        genesisHome = "/genesisHome/"
        scriptFileName = "another-notify.kts"
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
        val reader = bootstrap.injector.getInstance(NotifyFileReader::class.java)

        val emailConfig = reader.get().gatewayConfigs.configById.get("email1") as EmailGatewayConfig
        assertThat(emailConfig.smtpUser).isEqualTo("default@genesis.global")
    }
}
