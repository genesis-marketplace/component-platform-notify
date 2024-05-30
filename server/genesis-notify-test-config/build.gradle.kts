dependencies {
    compileOnly("global.genesis:genesis-dictionary")
    compileOnly("global.genesis:genesis-process")
    compileOnly("global.genesis:genesis-pal-execution")
    compileOnly(project(path = ":genesis-notify-test-dictionary-cache", configuration = "codeGen"))
    implementation(project(":genesis-notify-config"))
}

description = "genesis-notify-test-config"

tasks {
    copyDependencies {
        enabled = false
    }
}
