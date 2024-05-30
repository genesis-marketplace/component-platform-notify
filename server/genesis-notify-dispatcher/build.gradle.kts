plugins {
    id("global.genesis.packagescan")
}

storeClasses {
    scanPackage(name = "global.genesis.notify")
}

dependencies {
    api(project(":genesis-notify-messages"))
    api("global.genesis:genesis-net")
    api("global.genesis:genesis-messages")
    api("global.genesis:genesis-pal-execution")
    api("global.genesis:genesis-jackson")
    api("global.genesis:genesis-db-server")
    api("global.genesis:genesis-eventhandler")
    api("global.genesis:genesis-evaluator")
    api("global.genesis:genesis-pal-eventhandler")
    api("global.genesis:genesis-pal-dataserver")
    api("global.genesis:genesis-pal-requestserver")
    api("global.genesis:genesis-process")
    api("global.genesis:genesis-metrics")
    api("org.slf4j:slf4j-api")
    api("org.simplejavamail:simple-java-mail")
    api("org.apache.commons:commons-compress")
    api("com.sendgrid:sendgrid-java")
    api("global.genesis:file-server-api")
    testImplementation("global.genesis:genesis-testsupport")
    testImplementation("global.genesis:genesis-messages")
    testImplementation("org.awaitility:awaitility")
    testImplementation("org.mockito.kotlin:mockito-kotlin")
    testImplementation("org.awaitility:awaitility-kotlin")
    // Jose: Is compileOnly equivalent to "provided"?
    compileOnly(project(path = ":genesis-notify-dictionary-cache", configuration = "codeGen"))
    testImplementation(project(path = ":genesis-notify-test-dictionary-cache", configuration = "codeGen"))
    testImplementation("junit:junit")
    testImplementation("org.mockito.kotlin:mockito-kotlin")
    testImplementation("org.awaitility:awaitility-kotlin")
    testImplementation("org.hamcrest:hamcrest-library")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("org.apache.pdfbox:pdfbox:3.0.2")
}

tasks {
    test {
        maxHeapSize = "4g"
        minHeapSize = "256m"
    }
}

description = "genesis-notify-dispatcher"
