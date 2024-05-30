childProjects.values.forEach { project ->
    project.tasks {
        artifactoryPublish {
            enabled = false
        }
    }
}

// Add your genesis config dependencies here
dependencies {
}

description = "genesis-notify-test-dictionary-cache"
