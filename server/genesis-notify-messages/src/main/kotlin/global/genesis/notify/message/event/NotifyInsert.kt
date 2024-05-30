package global.genesis.notify.message.event

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonUnwrapped
import global.genesis.gen.dao.Notify
import global.genesis.gen.dao.enums.NotifySeverity

data class NotifyInsert(
    val notifyAttachments: Set<String> = emptySet()
) {
    @JsonUnwrapped
    @JsonIgnoreProperties("TABLE_ENTITY_ID")
    lateinit var notify: Notify

    var tableEntityId: Any? = null
}

data class NotifyEmailDirect(
    val header: String,
    val body: String,
    val notifySeverity: NotifySeverity = NotifySeverity.Information,
    val tableName: String? = null,
    val tableEntityId: Any? = null,
    val permissioningEntityId: String? = null,
    val notifyAttachments: Set<String>? = null,
    val routingData: EmailDirectRoutingData
)

data class NotifyScreenDirect(
    val header: String,
    val body: String,
    val notifySeverity: NotifySeverity = NotifySeverity.Information,
    val tableName: String? = null,
    val tableEntityId: Any? = null,
    val permissioningEntityId: String? = null,
    val routingData: ScreenDirectRoutingData
)
