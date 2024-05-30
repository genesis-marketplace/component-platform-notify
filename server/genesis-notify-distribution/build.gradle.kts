import org.apache.tools.ant.filters.FixCrLfFilter

plugins {
    distribution
}

dependencies {
    implementation(project(":genesis-notify-config"))
    implementation(project(":genesis-notify-dictionary-cache"))
    implementation(project(":genesis-notify-eventhandler"))
    implementation(project(":genesis-notify-messages"))
    implementation(project(":genesis-notify-script-config"))
    implementation(project(":genesis-notify-dispatcher"))
}

description = "genesis-notify-distribution"

distributions {
    main {
        contents {
            // Octal conversion for file permissions
            val libPermissions = "600".toInt(8)
            val scriptPermissions = "700".toInt(8)
            into("genesis-notify/bin") {
                from(configurations.runtimeClasspath)
                exclude("genesis-notify-config*.jar")
                exclude("genesis-notify-distribution*.jar")
                include("genesis-notify-*.jar")
            }
            into("genesis-notify/lib") {
                from(project.rootProject.layout.buildDirectory.dir("dependencies"))
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE

                exclude("genesis-*.jar")
                exclude("genesis-notify-*.jar")
                exclude("*.zip")

                fileMode = libPermissions
            }
            includeCfg(this)
            includeData(this)
            includeScripts(this, scriptPermissions)
            // Removes intermediate folder called with the same name as the zip archive.
            into("/")
        }
    }
}

val packageConfigFiles: TaskProvider<Jar> = tasks.register<Jar>("packConfigFiles")

val distribution by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

// To give custom name to the distribution package
tasks {
    distZip {
        archiveBaseName.set("genesisproduct-genesis-notify")
        archiveClassifier.set("bin")
        archiveExtension.set("zip")
    }

    packageConfigFiles {
        dependsOn("createProductDetails", "createManifest")
        archiveClassifier.set("minimal")
        archiveExtension.set("zip")
        includeCfg(this)
        includeData(this)
        includeScripts(this, "700".toInt(8))
        exclude("**/*.java", "**/*.jar")
    }

    copyDependencies {
        enabled = false
    }
}

artifacts {
    val distzip = tasks.distZip.get()
    add("distribution", distzip.archiveFile) {
        builtBy(distzip)
    }
}

publishing {
    publications {
        create<MavenPublication>("genesis-notifyServerDistribution") {
            artifact(tasks.distZip.get())
        }
        create<MavenPublication>("genesis-notifyMinimalDistribution") {
            artifact(packageConfigFiles.get())
        }
    }
}

description = "genesis-notify-distribution"

fun includeScripts(copySpec: CopySpec, scriptPermissions: Int) {
    copySpec.into("genesis-notify/scripts") {
        from("${project.rootProject.projectDir}/genesis-notify-config/src/main/resources/scripts")
        from("${project.rootProject.projectDir}/genesis-notify-script-config/src/main/resources/scripts")
        filter(
            org.apache.tools.ant.filters.FixCrLfFilter::class,
            "eol" to FixCrLfFilter.CrLf.newInstance("lf")
        )
        fileMode = scriptPermissions
    }
}

fun includeCfg(copySpec: CopySpec) {
    copySpec.into("genesis-notify/cfg") {
        from("${project.rootProject.projectDir}/genesis-notify-config/src/main/resources/cfg")
        from(project.layout.buildDirectory.dir("generated/product-details"))
        filter(
            org.apache.tools.ant.filters.FixCrLfFilter::class,
            "eol" to FixCrLfFilter.CrLf.newInstance("lf")
        )
    }
}

fun includeData(copySpec: CopySpec) {
    copySpec.into("genesis-notify/data") {
        from("${project.rootProject.projectDir}/genesis-notify-config/src/main/resources/data")
        filter(
            org.apache.tools.ant.filters.FixCrLfFilter::class,
            "eol" to FixCrLfFilter.CrLf.newInstance("lf")
        )
    }
}
