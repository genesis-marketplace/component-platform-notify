dependencies {
    implementation("global.genesis:genesis-messages")
    api("global.genesis:genesis-db")
    api("global.genesis:genesis-criteria")
    compileOnly(project(path = ":genesis-notify-dictionary-cache", configuration = "codeGen"))
}

description = "genesis-notify-messages"
