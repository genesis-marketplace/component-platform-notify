import global.genesis.config.dsl.systemDefinition

systemDefinition {

    global {
        // Symphony
        item(name = "DOCUMENT_STORE_BASEDIR", value = "/home/gem/run/site-specific/symphony/incoming")
        item("STORAGE_STRATEGY", "LOCAL")
        item("LOCAL_STORAGE_FOLDER", "LOCAL_STORAGE")
    }
}
