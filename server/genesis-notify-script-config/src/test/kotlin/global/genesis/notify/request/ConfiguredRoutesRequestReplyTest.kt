package global.genesis.notify.request

import global.genesis.commons.model.GenesisSet
import global.genesis.message.core.request.Request
import global.genesis.message.core.workflow.message.RequestReplyWorkflow
import global.genesis.message.core.workflow.message.requestReplyWorkflowBuilder
import global.genesis.notify.message.request.ConfiguredRoute
import global.genesis.notify.message.request.EmptyRequest
import global.genesis.notify.message.request.NotifyRoutesReply
import global.genesis.notify.pal.NotifyDefinition
import global.genesis.testsupport.AbstractGenesisTestSupport
import global.genesis.testsupport.GenesisTestConfig
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import javax.inject.Inject
import kotlin.test.assertEquals

class ConfiguredRoutesRequestReplyTest : AbstractGenesisTestSupport<GenesisSet>(
    GenesisTestConfig {
        addPackageName("global.genesis.notify")
        addPackageName("global.genesis.eventhandler.pal")
        addPackageName("global.genesis.symphony")
        addPackageName("global.genesis.requestreply.pal")
        addPackageName("global.genesis.file.storage.provider")
        genesisHome = "/GenesisHome/"
        initialDataFile = "notification-rule-template-data.csv"
        scriptFileName = "another-notify.kts,genesis-notify-routes-reqrep.kts"
        parser = { it }
        useTempClassloader = true
    }
) {

    override fun systemDefinition(): Map<String, Any> = mapOf(
        "LOCAL_STORAGE_FOLDER" to "LOCAL_STORAGE",
        "STORAGE_STRATEGY" to "LOCAL"
    )

    @Inject
    private lateinit var notifyDefinition: NotifyDefinition

    object ConfiguredRoutesWorkflow : RequestReplyWorkflow<EmptyRequest, NotifyRoutesReply> by requestReplyWorkflowBuilder("CONFIGURED_NOTIFY_ROUTES")

    @Test
    fun `CONFIGURED_NOTIFY_ROUTES should reply with all configured gateway routes`() = runBlocking {
        val request = Request(
            request = EmptyRequest(),
            messageType = "CONFIGURED_NOTIFY_ROUTES",
            userName = "JohnDoe"
        )

        val reply = sendRequest(ConfiguredRoutesWorkflow, request)[0]
        val expected = NotifyRoutesReply(
            configuredRoutes = notifyDefinition.gatewayConfigs.gatewayIdsByRouteInfo.map { ConfiguredRoute(it.key, it.value) }.toSet()
        )
        assertEquals(expected, reply)
    }
}
