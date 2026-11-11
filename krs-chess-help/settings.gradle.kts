pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "krs-chess-help"

include(":app")     // Android application: capture service, overlay, settings UI
include(":core")    // Pure Kotlin: chess rules, FEN, move generation, engine, move tracking
include(":vision")  // Pure Kotlin: board location, square sampling, change detection
include(":claude")  // Pure Kotlin: Claude API client (Anthropic Java SDK), prompts, tool loop
include(":brain")   // Pure Kotlin: analysis coordinator (capture -> locate -> read -> track -> advise)
include(":capture") // Android library: MediaProjection screen capture and board image encoding
