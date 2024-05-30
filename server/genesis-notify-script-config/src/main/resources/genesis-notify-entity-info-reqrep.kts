import global.genesis.cluster.modules.sysinfo.SystemEntityRequest
import global.genesis.cluster.modules.sysinfo.SystemEntityResponse
import global.genesis.config.system.SystemDefinitionService
import global.genesis.db.dictionary.util.getAllEntityNames
import global.genesis.db.rx.RxDb

val dictionary = inject<RxDb>().dictionary
val notifyEntityList = inject<SystemDefinitionService>().getItem("NOTIFY_ENTITY_LIST") as? List<String>

requestReplies {
    requestReply<SystemEntityRequest, SystemEntityResponse>(name = "NOTIFY_SYSTEM_ENTITY") {
        replyList { request ->
            val regex = request.entityName.replace("*", ".*").toRegex()
            val entityNames = getAllEntityNames(dictionary).let {
                if (!notifyEntityList.isNullOrEmpty()) {
                    it.filter { it in notifyEntityList }
                } else {
                    it
                }
            }.filter { it.matches(regex) }
            entityNames.map {
                SystemEntityResponse(entityName = it)
            }
        }
    }

}
