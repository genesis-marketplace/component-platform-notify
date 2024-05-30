package global.genesis.notify.router

import com.google.inject.Inject
import com.google.inject.Singleton
import global.genesis.gen.dao.Notify
import global.genesis.gen.dao.enums.RoutingType

@Singleton
class NotifyRouterProvider @Inject constructor(
    private val basicNotifyRouter: BasicNotifyRouter,
    private val topicMatchingNotifyRouter: TopicMatchingNotifyRouter,
    private val directNotifyRouter: DirectNotifyRouter
) {

    fun getRouter(notify: Notify): NotifyRouter {
        return if (notify.topic != null) {
            topicMatchingNotifyRouter
        } else {
            when (notify.routingType) {
                RoutingType.BASIC -> basicNotifyRouter
                RoutingType.TOPIC -> topicMatchingNotifyRouter
                RoutingType.DIRECT -> directNotifyRouter
            }
        }
    }
}
