codeGen {
    configModuleFilter = setOf("genesis-notify-config")
    useCleanerTask.set(((properties["useCleanerTask"] ?: "true") == "true"))
}

description = "genesis-generated-view"
