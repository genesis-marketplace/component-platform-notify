import global.genesis.jackson.core.GenesisJacksonMapper.Companion.toJsonString
import global.genesis.notify.message.event.*

fun Collection<String>.toNotifyAttachments(notifyId: String): List<NotifyAttachment> = map { fileStorageId ->
    NotifyAttachment {
        this.notifyId = notifyId
        this.fileStorageId = fileStorageId
    }
}

//For now, sender must be user on event where populated
fun validateSender(event: Event<*>, sender: String?) {
    require(sender.isNullOrEmpty() || sender == event.userName) {
        "User not permitted to send on behalf of another"
    }
}

eventHandler {

    eventHandler<ScreenRouteCreate>(name = "SCREEN_NOTIFY_ROUTE_CREATE", transactional = true) {
        permissioning {
            permissionCodes = listOf("NotificationRouteCreate")
        }
        onCommit { event ->
            val details = event.details
            val notifyRoute = NotifyRoute {
                this.gatewayId = details.gatewayId
                this.topicMatch = details.topicMatch
            }
            val insertResult = entityDb.insert(notifyRoute)
            val screenNotifyRoute = ScreenNotifyRouteExt {
                this.notifyRouteId = insertResult.record.notifyRouteId
                this.entityId = details.entityId
                this.entityIdType = details.entityIdType
                this.ttl = details.ttl
                this.ttlTimeUnit = details.ttlTimeUnit
                this.authCacheName = details.authCacheName
                this.rightCode = details.rightCode
                this.excludeSender = details.excludeSender
            }
            entityDb.insert(screenNotifyRoute)
            ack()
        }
    }

    eventHandler<ScreenRoute>(name = "SCREEN_NOTIFY_ROUTE_UPDATE", transactional = true) {
        permissioning {
            permissionCodes = listOf("NotificationRouteUpdate")
        }
        onCommit { event ->
            val details = event.details
            val notifyRoute = NotifyRoute {
                this.notifyRouteId = details.notifyRouteId
                this.gatewayId = details.gatewayId
                this.topicMatch = details.topicMatch
            }
            val screenNotifyRoute = ScreenNotifyRouteExt {
                this.notifyRouteId = details.notifyRouteId
                this.entityId = details.entityId
                this.entityIdType = details.entityIdType
                this.ttl = details.ttl
                this.ttlTimeUnit = details.ttlTimeUnit
                this.authCacheName = details.authCacheName
                this.rightCode = details.rightCode
                this.excludeSender = details.excludeSender
            }
            entityDb.modify(notifyRoute)
            entityDb.modify(screenNotifyRoute)
            ack()
        }
    }

    eventHandler<ScreenRoute.ById>(name = "SCREEN_NOTIFY_ROUTE_DELETE", transactional = true) {
        permissioning {
            permissionCodes = listOf("NotificationRouteDelete")
        }
        onCommit { event ->
            val details = event.details
            val notifyRoute = NotifyRoute.ById(details.notifyRouteId)
            val screenNotifyRoute = ScreenNotifyRouteExt.byNotifyRouteId(details.notifyRouteId)
            entityDb.delete(notifyRoute)
            entityDb.delete(screenNotifyRoute)
            ack()
        }
    }

    eventHandler<MsTeamsRouteCreate>(name = "MS_TEAMS_ROUTE_CREATE", transactional = true) {
        permissioning {
            permissionCodes = listOf("NotificationRouteCreate")
        }
        onCommit { event ->
            val details = event.details
            val notifyRoute = NotifyRoute {
                this.gatewayId = details.gatewayId
                this.topicMatch = details.topicMatch
            }
            val insertResult = entityDb.insert(notifyRoute)
            val msTeamsNotifyRoute = MsTeamsNotifyRouteExt {
                this.notifyRouteId = insertResult.record.notifyRouteId
                this.url = details.url
            }
            entityDb.insert(msTeamsNotifyRoute)
            ack()
        }
    }

    eventHandler<MsTeamsRoute>(name = "MS_TEAMS_ROUTE_UPDATE", transactional = true) {
        permissioning {
            permissionCodes = listOf("NotificationRouteUpdate")
        }
        onCommit { event ->
            val details = event.details
            val notifyRoute = NotifyRoute {
                this.notifyRouteId = details.notifyRouteId
                this.gatewayId = details.gatewayId
                this.topicMatch = details.topicMatch
            }
            val msTeamsNotifyRoute = MsTeamsNotifyRouteExt {
                this.notifyRouteId = details.notifyRouteId
                this.url = details.url
            }
            entityDb.modify(notifyRoute)
            entityDb.modify(msTeamsNotifyRoute)
            ack()
        }
    }

    eventHandler<MsTeamsRoute.ById>(name = "MS_TEAMS_ROUTE_DELETE", transactional = true) {
        permissioning {
            permissionCodes = listOf("NotificationRouteDelete")
        }
        onCommit { event ->
            val details = event.details
            val notifyRoute = NotifyRoute.ById(details.notifyRouteId)
            val msTeamsNotifyRoute = MsTeamsNotifyRouteExt.ByNotifyRouteId(details.notifyRouteId)
            entityDb.delete(notifyRoute)
            entityDb.delete(msTeamsNotifyRoute)
            ack()
        }
    }

    eventHandler<EmailDistributionRouteCreate>(name = "EMAIL_DISTRIBUTION_ROUTE_CREATE", transactional = true) {
        permissioning {
            permissionCodes = listOf("NotificationRouteCreate")
        }
        onCommit { event ->
            val details = event.details
            val notifyRoute = NotifyRoute {
                this.gatewayId = details.gatewayId
                this.topicMatch = details.topicMatch
            }
            val insertResult = entityDb.insert(notifyRoute)
            val emailDistNotifyRoute = EmailDistNotifyRouteExt {
                this.notifyRouteId = insertResult.record.notifyRouteId
                this.emailTo = details.emailTo.joinToString(",")
                this.emailCc = details.emailCc.joinToString(",")
                this.emailBcc = details.emailBcc.joinToString(",")
            }
            entityDb.insert(emailDistNotifyRoute)
            ack()
        }
    }

    eventHandler<EmailDistributionRouteUpdate>(name = "EMAIL_DISTRIBUTION_ROUTE_UPDATE", transactional = true) {
        permissioning {
            permissionCodes = listOf("NotificationRouteUpdate")
        }
        onCommit { event ->
            val details = event.details
            val notifyRoute = NotifyRoute {
                this.notifyRouteId = details.notifyRouteId
                this.gatewayId = details.gatewayId
                this.topicMatch = details.topicMatch
            }
            val emailDistNotifyRoute = EmailDistNotifyRouteExt {
                this.notifyRouteId = details.notifyRouteId
                this.emailTo = details.emailTo.joinToString(",")
                this.emailCc = details.emailCc.joinToString(",")
                this.emailBcc = details.emailBcc.joinToString(",")
            }
            entityDb.modify(notifyRoute)
            entityDb.modify(emailDistNotifyRoute)
            ack()
        }
    }

    eventHandler<EmailDistributionRoute.ById>(name = "EMAIL_DISTRIBUTION_ROUTE_DELETE", transactional = true) {
        permissioning {
            permissionCodes = listOf("NotificationRouteDelete")
        }
        onCommit { event ->
            val details = event.details
            val notifyRoute = NotifyRoute.ById(details.notifyRouteId)
            val emailDistNotifyRoute = EmailDistNotifyRouteExt.ByNotifyRouteId(details.notifyRouteId)
            entityDb.delete(notifyRoute)
            entityDb.delete(emailDistNotifyRoute)
            ack()
        }
    }

    eventHandler<EmailUserRouteCreate>(name = "EMAIL_USER_ROUTE_CREATE", transactional = true) {
        permissioning {
            permissionCodes = listOf("NotificationRouteCreate")
        }
        onCommit { event ->
            val details = event.details
            val notifyRoute = NotifyRoute {
                this.gatewayId = details.gatewayId
                this.topicMatch = details.topicMatch
            }
            val insertResult = entityDb.insert(notifyRoute)
            val emailUserNotifyRoute = EmailUserNotifyRouteExt {
                this.notifyRouteId = insertResult.record.notifyRouteId
                this.entityId = details.entityId
                this.entityIdType = details.entityIdType
                this.excludeSender = details.excludeSender
            }
            entityDb.insert(emailUserNotifyRoute)
            ack()
        }
    }

    eventHandler<EmailUserRoute>(name = "EMAIL_USER_ROUTE_UPDATE", transactional = true) {
        permissioning {
            permissionCodes = listOf("NotificationRouteUpdate")
        }
        onCommit { event ->
            val details = event.details
            val notifyRoute = NotifyRoute {
                this.notifyRouteId = details.notifyRouteId
                this.gatewayId = details.gatewayId
                this.topicMatch = details.topicMatch
            }
            val emailUserNotifyRoute = EmailUserNotifyRouteExt {
                this.notifyRouteId = details.notifyRouteId
                this.entityId = details.entityId
                this.entityIdType = details.entityIdType
                this.excludeSender = details.excludeSender
            }
            entityDb.modify(notifyRoute)
            entityDb.modify(emailUserNotifyRoute)
            ack()
        }
    }

    eventHandler<EmailUserRoute.ById>(name = "EMAIL_USER_ROUTE_DELETE", transactional = true) {
        permissioning {
            permissionCodes = listOf("NotificationRouteDelete")
        }
        onCommit { event ->
            val details = event.details
            val notifyRoute = NotifyRoute.ById(details.notifyRouteId)
            val emailUserNotifyRoute = EmailUserNotifyRouteExt.ByNotifyRouteId(details.notifyRouteId)
            entityDb.delete(notifyRoute)
            entityDb.delete(emailUserNotifyRoute)
            ack()
        }
    }

    eventHandler<LogRouteCreate>(name = "LOG_ROUTE_CREATE", transactional = true) {
        permissioning {
            permissionCodes = listOf("NotificationRouteCreate")
        }
        onCommit { event ->
            val details = event.details
            val notifyRoute = NotifyRoute {
                this.gatewayId = details.gatewayId
                this.topicMatch = details.topicMatch
            }
            val insertResult = entityDb.insert(notifyRoute)
            val logRoute = LogNotifyRouteExt {
                this.notifyRouteId = insertResult.record.notifyRouteId
            }
            entityDb.insert(logRoute)
            ack()
        }
    }

    eventHandler<LogRoute>(name = "LOG_ROUTE_UPDATE", transactional = true) {
        permissioning {
            permissionCodes = listOf("NotificationRouteUpdate")
        }
        onCommit { event ->
            val details = event.details
            val notifyRoute = NotifyRoute {
                this.notifyRouteId = details.notifyRouteId
                this.gatewayId = details.gatewayId
                this.topicMatch = details.topicMatch
            }
            val logRoute = LogNotifyRouteExt {
                this.notifyRouteId = details.notifyRouteId
            }
            entityDb.modify(notifyRoute)
            entityDb.modify(logRoute)
            ack()
        }
    }

    eventHandler<LogRoute.ById>(name = "LOG_ROUTE_DELETE", transactional = true) {
        permissioning {
            permissionCodes = listOf("NotificationRouteDelete")
        }
        onCommit { event ->
            val details = event.details
            val notifyRoute = NotifyRoute.ById(details.notifyRouteId)
            val logRoute = LogNotifyRouteExt.ByNotifyRouteId(details.notifyRouteId)
            entityDb.delete(notifyRoute)
            entityDb.delete(logRoute)
            ack()
        }
    }

    contextEventHandler<NotifyAlert.ByAlertId, NotifyAlert>(name = "DISMISS_NOTIFY_ALERT") {
        onValidate { event ->
            val notifyAlert = entityDb.get(event.details)
            if (notifyAlert == null) {
                validationResult(nack("Alert with ID ${event.details.alertId} does not exist"))
            } else if (notifyAlert.userName != event.userName) {
                validationResult(nack("Dismissing an alert targeted at another user is forbidden"))
            } else {
                validationResult(ack(), notifyAlert)
            }
        }
        onCommit { event, notifyAlert ->
            notifyAlert!!.alertStatus = AlertStatus.DISMISSED
            entityDb.modify(notifyAlert)
            ack()
        }
    }

    eventHandler<NotifyInsert>(name = "NOTIFY_INSERT", transactional = true) {
        schemaValidation = false

        onValidate { event ->
            validateSender(event, event.details.notify.sender)
            ack()
        }
        onCommit { event ->
            //Insert Notify
            val record = event.details.notify
            record.tableEntityId = event.details.tableEntityId?.toString()
            val insertedNotifyRec = entityDb.insert(record).record
            LOG.info("insertedNotifyRec = {}", insertedNotifyRec)

            //Insert Attachments
            if (!event.details.notifyAttachments.isNullOrEmpty()) {
                val notifyAttachments = event.details.notifyAttachments!!.toNotifyAttachments(insertedNotifyRec.notifyId)
                entityDb.insertAll(notifyAttachments)
            }

            ack()
        }
    }

    eventHandler<NotifyEmailDirect> {
        schemaValidation = false
        onValidate { event ->
            ack()
        }

        onCommit { event ->
            val notify = Notify {
                sender = event.userName
                header = event.details.header
                body = event.details.body
                notifySeverity = event.details.notifySeverity
                tableName = event.details.tableName
                tableEntityId = event.details.tableEntityId.toString()
                permissioningEntityId = event.details.permissioningEntityId
                routingType = RoutingType.DIRECT
                routingData = event.details.routingData.toJsonString()
            }
            val insertedNotifyRec = entityDb.insert(notify).record
            LOG.info("insertedNotifyRec = {}", insertedNotifyRec)

            //Insert Attachments
            if (!event.details.notifyAttachments.isNullOrEmpty()) {
                val notifyAttachments = event.details.notifyAttachments!!.toNotifyAttachments(insertedNotifyRec.notifyId)
                entityDb.insertAll(notifyAttachments)
            }
            ack()
        }
    }

    eventHandler<NotifyScreenDirect> {
        schemaValidation = false
        onValidate { event ->
            ack()
        }

        onCommit { event ->
            val notify = Notify {
                sender = event.userName
                header = event.details.header
                body = event.details.body
                notifySeverity = event.details.notifySeverity
                tableName = event.details.tableName
                tableEntityId = event.details.tableEntityId.toString()
                permissioningEntityId = event.details.permissioningEntityId
                routingType = RoutingType.DIRECT
                routingData = event.details.routingData.toJsonString()
            }
            val insertedNotifyRec = entityDb.insert(notify).record
            LOG.info("insertedNotifyRec = {}", insertedNotifyRec)
            ack()
        }
    }
}
