rootProject.name = "genesisproduct-genesis-notify"

pluginManagement {
    pluginManagement {
        val genesisVersion: String by settings
        plugins {
            id("global.genesis.build") version genesisVersion
            id("global.genesis.packagescan") version genesisVersion
            id("global.genesis.deploy") version genesisVersion
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
        gradlePluginPortal()
        maven {
            url = uri("https://genesisglobal.jfrog.io/genesisglobal/dev-repo")
            credentials {
                username = extra.properties["genesisArtifactoryUser"].toString()
                password = extra.properties["genesisArtifactoryPassword"].toString()
            }
        }
    }
}

include("genesis-notify-config")
include("genesis-notify-messages")
include("genesis-notify-eventhandler")
include("genesis-notify-dispatcher")
include("genesis-notify-script-config")
include("genesis-notify-distribution")
include("genesis-notify-dictionary-cache")
include("genesis-notify-dictionary-cache:genesis-notify-generated-sysdef")
include("genesis-notify-dictionary-cache:genesis-notify-generated-fields")
include("genesis-notify-dictionary-cache:genesis-notify-generated-dao")
include("genesis-notify-dictionary-cache:genesis-notify-generated-hft")
include("genesis-notify-dictionary-cache:genesis-notify-generated-view")
include("genesis-notify-test-config")
include("genesis-notify-test-dictionary-cache")
include("genesis-notify-test-dictionary-cache:genesis-notify-test-generated-sysdef")
include("genesis-notify-test-dictionary-cache:genesis-notify-test-generated-fields")
include("genesis-notify-test-dictionary-cache:genesis-notify-test-generated-dao")
include("genesis-notify-test-dictionary-cache:genesis-notify-test-generated-hft")
include("genesis-notify-test-dictionary-cache:genesis-notify-test-generated-view")
