ext.set("localDaogenVersion", "GENESIS_NOTIFY")

plugins {
    kotlin("jvm") version "1.9.22"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
    id("org.jetbrains.kotlinx.kover") version "0.8.1"
    id("org.gradle.test-retry") version "1.5.8"
    id("com.jfrog.artifactory") version "5.2.0"
    id("org.sonarqube") version "4.4.1.3373"
    `maven-publish`
    id("global.genesis.build")
}

val testProperties = listOf(
    "DbLayer",
    "MqLayer",
    "DbHost",
    "DbUsername",
    "DbPassword",
    "AliasSource",
    "ClusterMode",
    "DictionarySource",
    "DbNamespace",
    "DbMode",
    "DbThreadsMax",
    "DbThreadsMin",
    "DbThreadKeepAliveSeconds",
    "DbSqlConnectionPoolSize",
    "DbQuotedIdentifiers",
    "SqlMaxParametersPerRequest"
)
val isCiServer = System.getenv().containsKey("CI")
val os = org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem()

sonarqube {
    properties {
        property("sonar.projectKey", "genesislcap_genesis-notify")
        property("sonar.projectName", "pbc-notify-server")
        property("sonar.organization", "genesislcap")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.sourceEncoding", "UTF-8")
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.gradle.maven-publish")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "com.jfrog.artifactory")
    apply(plugin = "org.gradle.test-retry")

    dependencies {
        implementation(platform("global.genesis:genesis-bom:${properties["genesisVersion"]}"))
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.10")
        constraints {
            // define versions of your dependencies here so that submodules do not have to define versions
            api("org.simplejavamail:simple-java-mail:8.8.4")
            api("global.genesis:file-server-api:${properties["fileServerVersion"]}")
            api("com.sendgrid:sendgrid-java:4.10.2")
            testImplementation("com.icegreen:greenmail:2.0.1")
        }
    }
    tasks {
        val java = "17"
        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
                jvmTarget = java
            }
        }

        test {
            maxHeapSize = "2g"
            val testProperties = listOf(
                "DbLayer",
                "MqLayer",
                "DbHost",
                "DbUsername",
                "DbPassword",
                "AliasSource",
                "ClusterMode",
                "DictionarySource",
                "DbNamespace",
                "DbMode",
                "DbThreadsMax",
                "DbThreadsMin",
                "DbThreadKeepAliveSeconds",
                "DbSqlConnectionPoolSize",
                "DbQuotedIdentifiers"
            )
            val properties = System.getProperties()
            for (property in testProperties) {
                val value = properties.getProperty(property)
                    ?: ext.properties[property]?.toString()
                if (value != null) {
                    inputs.property(property, value)
                    systemProperty(property, value)
                }
            }
        }

        compileKotlin {
            kotlinOptions { jvmTarget = java }
        }
    }

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        filter {
            exclude { element -> element.file.path.contains("generated") }
        }
    }
}

tasks {
    assemble {
        for (subproject in subprojects) {
            dependsOn(subproject.tasks.named("assemble"))
        }
        finalizedBy("copyUserNpmrc")
    }
    build {
        for (subproject in subprojects) {
            dependsOn(subproject.tasks.named("build"))
        }
    }
    clean {
        for (subproject in subprojects) {
            dependsOn(subproject.tasks.named("clean"))
        }
    }
    test {
        for (subproject in subprojects) {
            dependsOn(subproject.tasks.named("test"))
        }
    }
    testClasses {
        for (subproject in subprojects) {
            dependsOn(subproject.tasks.named("testClasses"))
        }
    }
    ktlintFormat {
        for (subproject in subprojects) {
            dependsOn(subproject.tasks.named("ktlintFormat"))
        }
    }
    ktlintCheck {
        for (subproject in subprojects) {
            dependsOn(subproject.tasks.named("ktlintCheck"))
        }
    }
    publishToMavenLocal {
        for (subproject in subprojects) {
            dependsOn(subproject.tasks.named("publishToMavenLocal"))
        }
    }
    task("copyUserNpmrc") {
        copy {
            file(project.gradle.gradleUserHomeDir.parent).listFiles()
                ?.let { from(it.filter { it.name.equals(".npmrc") }).into("$projectDir/../../client") }
        }
    }
}

subprojects {
    afterEvaluate {
        val copyDependencies = tasks.findByName("copyDependencies") ?: return@afterEvaluate
        tasks.withType<Jar> {
            dependsOn(copyDependencies)
        }
    }
    tasks {
        test {
            useJUnitPlatform()

            val testProperties = listOf(
                "DbLayer",
                "MqLayer",
                "DbHost",
                "DbUsername",
                "DbPassword",
                "AliasSource",
                "ClusterMode",
                "DictionarySource",
                "DbNamespace",
                "DbMode",
                "DbThreadsMax",
                "DbThreadsMin",
                "DbThreadKeepAliveSeconds",
                "DbSqlConnectionPoolSize",
                "DbQuotedIdentifiers",
                "SqlMaxParametersPerRequest"
            )
            // Add exports and opens so ChronicleQueue can continue working in JDK 17.
            // More info in: https://chronicle.software/chronicle-support-java-17/
            jvmArgs = jvmArgs!! + listOf(
                "--add-exports=java.base/jdk.internal.ref=ALL-UNNAMED",
                "--add-exports=java.base/sun.nio.ch=ALL-UNNAMED",
                "--add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED",
                "--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
                "--add-opens=jdk.compiler/com.sun.tools.javac=ALL-UNNAMED",
                "--add-opens=java.base/java.lang=ALL-UNNAMED",
                "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
                "--add-opens=java.base/java.io=ALL-UNNAMED",
                "--add-opens=java.base/java.util=ALL-UNNAMED",
                "--add-opens=java.base/java.nio=ALL-UNNAMED" // this one is opened for LMDB
            )
            val properties = System.getProperties()
            for (property in testProperties) {
                val value = properties.getProperty(property)
                    ?: ext.properties[property]?.toString()

                if (value != null) {
                    inputs.property(property, value)
                    systemProperty(property, value)
                }
            }
            if (os.isMacOsX) {
                // Needed to guarantee FDB java bindings will work as expected in MacOS
                environment("DYLD_LIBRARY_PATH", "/usr/local/lib")
            }
            // UK Locale changed in recent Java versions and the abbreviation for September is now Sept instead of Sep.
            // This cases our DumpTableFormattedTest.test dump table formatted to fail. Setting to COMPAT mode allows
            // same behaviour as Java 8. We should deal with this at some point.
            // More info here: https://bugs.openjdk.org/browse/JDK-8256837
            // And here: https://bugs.openjdk.org/browse/JDK-8273437
            systemProperty("java.locale.providers", "COMPAT")
            if (!isCiServer) {
                systemProperty("kotlinx.coroutines.debug", "")
            }
        }
    }
}

allprojects {

    group = "global.genesis"

    kotlin {
        jvmToolchain {
            (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
    tasks.withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.WARN
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    repositories {
        mavenLocal {
            // VERY IMPORTANT!!! EXCLUDE AGRONA AS IT IS A POM DEPENDENCY AND DOES NOT PLAY NICELY WITH MAVEN LOCAL!
            content {
                excludeGroup("org.agrona")
            }
        }
        mavenCentral()
        maven {
            url = uri("https://genesisglobal.jfrog.io/genesisglobal/dev-repo")
            credentials {
                username = properties["genesisArtifactoryUser"].toString()
                password = properties["genesisArtifactoryPassword"].toString()
            }
        }
        val repoKey = buildRepoKey()
        maven {
            url = uri("https://genesisglobal.jfrog.io/genesisglobal/$repoKey")
            credentials {
                username = properties["genesisArtifactoryUser"].toString()
                password = properties["genesisArtifactoryPassword"].toString()
            }
        }
    }

    publishing {
        publications.create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

artifactory {
    setContextUrl("https://genesisglobal.jfrog.io/genesisglobal")

    publish {
        repository {
            setRepoKey(buildRepoKey())
            setUsername(property("genesisArtifactoryUser").toString())
            setPassword(property("genesisArtifactoryPassword").toString())
        }
        defaults {
            publications("ALL_PUBLICATIONS")
            setPublishArtifacts(true)
            setPublishPom(true)
            isPublishBuildInfo = false
        }
    }
}

fun buildRepoKey(): String {
    val buildTag = buildTagFor(project.version.toString())

    val repoKey = if (shouldDeployToClientRepo(buildTag)) {
        "libs-$buildTag-client"
    } else {
        "libs-$buildTag-local"
    }

    return repoKey
}

fun buildTagFor(version: String): String =
    when (version.substringAfterLast('-')) {
        "SNAPSHOT" -> "snapshot"
        in Regex("""M\d+[a-z]*$""") -> "milestone"
        else -> "release"
    }

fun shouldDeployToClientRepo(buildTag: String) = properties["deployToClientRepo"] == "true" && buildTag != "snapshot"

operator fun Regex.contains(s: String) = matches(s)
