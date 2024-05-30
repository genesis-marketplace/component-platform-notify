import global.genesis.gradle.plugin.pbc.task.GenxTemplateTask

plugins {
    id("global.genesis.pbc")
}

tasks.register("sonar") { }

tasks {
    val tasks = listOf("clean", "assemble", "check", "build", "sonar")
    for(taskName in tasks){
        named(taskName){
            gradle.includedBuilds.forEach {
                dependsOn(it.task(":$taskName"))
            }
        }
    }

    withType<GenxTemplateTask> {
        this.context.add(
            GenxTemplateTask.ExtractorInput(
                "fileServerVersion",
                project.file("server/gradle.properties"),
                "fileServerVersion\\s*=\\s*([\\d.]+[-\\w]*)"
            )
        )
    }
}
