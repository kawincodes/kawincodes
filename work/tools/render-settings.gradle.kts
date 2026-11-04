pluginManagement {
    repositories {
        google()
        maven("https://maven-central.storage-download.googleapis.com/maven2/")
        maven("https://repo1.maven.org/maven2/")
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "app.cash.paparazzi") {
                useModule("app.cash.paparazzi:paparazzi-gradle-plugin:${requested.version}")
            }
        }
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        maven("https://maven-central.storage-download.googleapis.com/maven2/")
        maven("https://repo1.maven.org/maven2/")
    }
}
rootProject.name = "finsoft-render"
include(":screens")
