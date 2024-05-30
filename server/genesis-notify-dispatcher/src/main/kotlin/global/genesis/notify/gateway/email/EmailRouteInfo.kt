package global.genesis.notify.gateway.email

import global.genesis.notify.message.request.RouteInfo

val EMAIL_USER_ROUTE_INFO = RouteInfo(
    dataServerHandler = "ALL_EMAIL_USER_ROUTES",
    createEventHandler = "EMAIL_USER_ROUTE_CREATE",
    updateEventHandler = "EMAIL_USER_ROUTE_UPDATE",
    deleteEventHandler = "EMAIL_USER_ROUTE_DELETE",
    displayName = "Email User Notification"
)
val EMAIL_DISTRIBUTION_ROUTE_INFO = RouteInfo(
    dataServerHandler = "ALL_EMAIL_DISTRIBUTION_ROUTES",
    createEventHandler = "EMAIL_DISTRIBUTION_ROUTE_CREATE",
    updateEventHandler = "EMAIL_DISTRIBUTION_ROUTE_UPDATE",
    deleteEventHandler = "EMAIL_DISTRIBUTION_ROUTE_DELETE",
    displayName = "Email Distribution Notification"
)
