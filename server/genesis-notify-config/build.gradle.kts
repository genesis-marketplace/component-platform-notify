dependencies {
    compileOnly("global.genesis:genesis-dictionary")
    compileOnly("global.genesis:genesis-process")
    compileOnly("global.genesis:genesis-pal-execution")
    compileOnly(project(path = ":genesis-notify-dictionary-cache", configuration = "codeGen"))
}

description = "genesis-notify-config"
