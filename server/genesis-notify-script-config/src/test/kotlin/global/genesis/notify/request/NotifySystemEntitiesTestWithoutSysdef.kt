package global.genesis.notify.request

import global.genesis.cluster.modules.sysinfo.SystemEntityRequest
import global.genesis.cluster.modules.sysinfo.SystemEntityResponse
import global.genesis.commons.model.GenesisSet
import global.genesis.message.core.request.Request
import global.genesis.message.core.workflow.message.RequestReplyWorkflow
import global.genesis.message.core.workflow.message.requestReplyWorkflowBuilder
import global.genesis.testsupport.AbstractGenesisTestSupport
import global.genesis.testsupport.GenesisTestConfig
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class NotifySystemEntitiesTestWithoutSysdef : AbstractGenesisTestSupport<GenesisSet>(
    GenesisTestConfig {
        packageNames = mutableListOf("global.genesis.requestreply.pal")
        genesisHome = "/GenesisHome/"
        initialDataFile = "notify-data.csv"
        scriptFileName = "genesis-notify-entity-info-reqrep.kts"
        useTempClassloader = true
    }
) {

    object NotifySystemEntitiesWorkflow : RequestReplyWorkflow<SystemEntityRequest, SystemEntityResponse> by requestReplyWorkflowBuilder("NOTIFY_SYSTEM_ENTITY")

    override fun systemDefinition(): Map<String, Any> {
        return emptyMap()
    }

    @Test
    fun testFilterReply() = runBlocking {
        val req = Request(
            request = SystemEntityRequest("*"),
            messageType = "NOTIFY_SYSTEM_ENTITY",
            userName = "JohnDoe"
        )
        val reply = sendRequest(NotifySystemEntitiesWorkflow, req)
        assertEquals(58, reply.size)
    }
}
