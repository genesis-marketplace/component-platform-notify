package global.genesis.notify.gateway.teams

import com.google.inject.Inject
import global.genesis.db.rx.entity.multi.AsyncEntityDb
import global.genesis.gen.dao.Notify
import global.genesis.gen.view.entity.MsTeamsRoute
import global.genesis.notify.gateway.Gateway
import global.genesis.notify.gateway.common.RouteUtils.getMatchedTopicRoutes
import global.genesis.notify.router.NotifyRouteCache
import global.genesis.notify.router.NotifyRoutingData
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class MicrosoftTeamsChannelGateway @Inject constructor(
    val config: TeamsGatewayConfig,
    db: AsyncEntityDb
) : Gateway {

    private val httpClient = HttpClient.newHttpClient()

    private val msTeamsRouteCache = NotifyRouteCache(
        name = "MsTeamsRouteCache",
        subscriber = db.bulkSubscribe(
            index = MsTeamsRoute.ById,
            backwardJoins = true
        ),
        keyFunction = { it.notifyRouteId }
    )

    // see https://docs.microsoft.com/en-us/microsoftteams/platform/webhooks-and-connectors/how-to/connectors-using#:~:text=Setting%20up%20a%20custom%20incoming%20webhook,-Follow%20these%20steps&text=In%20Microsoft%20Teams%2C%20choose%20More,the%20webhook%2C%20and%20choose%20Create.

    override suspend fun sendViaTopic(message: Notify, routeData: NotifyRoutingData.Topic) {
        val matchedRoutes = routeData.getMatchedTopicRoutes(config.id)

        val msTeamsRoutes: Set<MsTeamsRoute> =
            msTeamsRouteCache.getNotifyRoutes(routesToFind = matchedRoutes, logWarningOnNotFound = true)
        msTeamsRoutes.forEach { route ->
            sendMessage(route.url, message)
        }
    }

    override suspend fun sendDirect(message: Notify, routeData: NotifyRoutingData.Direct) {
        LOG.trace("Direct routing is not currently implemented for Teams gateways, ignoring message")
    }

    private fun sendMessage(url: String, message: Notify) {
        val httpRequest = HttpRequest.newBuilder(URI.create(url)).POST(
            HttpRequest.BodyPublishers.ofString(
                """
            {
              "text": "${message.header} [${message.notifySeverity}]</br>${message.body}"
            }
                """.trimIndent()
            )
        ).build()

        httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding())
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(MicrosoftTeamsChannelGateway::class.java)
    }
}
