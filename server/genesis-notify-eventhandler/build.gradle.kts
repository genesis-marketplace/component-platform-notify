dependencies {
    implementation("global.genesis:genesis-pal-eventhandler")
    implementation("global.genesis:genesis-pal-dataserver")
    implementation("global.genesis:genesis-pal-requestserver")
    implementation("global.genesis:genesis-pal-execution")
    implementation("global.genesis:genesis-eventhandler")
    implementation("global.genesis:genesis-evaluator")
    implementation(project(":genesis-notify-messages"))
    api("global.genesis:genesis-db")
    compileOnly(project(":genesis-notify-config"))
    compileOnly(project(path = ":genesis-notify-dictionary-cache", configuration = "codeGen"))
    testImplementation("global.genesis:genesis-dbtest")
    testImplementation("global.genesis:genesis-testsupport")
    testImplementation(project(path = ":genesis-notify-dictionary-cache", configuration = "codeGen"))
}

description = "genesis-notify-eventhandler"
