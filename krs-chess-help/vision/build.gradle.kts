import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
}

// This module ships inside the Android app, so it is compiled against the JDK 17 API surface
// (-Xjdk-release) to keep newer JDK-only members (e.g. List.removeLast) out of the bytecode.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xjdk-release=17")
    }
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test.junit)
}

tasks.withType<Test>().configureEach {
    systemProperty("java.awt.headless", "true")
    testLogging { events("failed") }

    // RealFixturesTest and RandomScenesTest are steered by environment variables. Gradle does not track
    // those by itself and would report the task as up to date (or restore it from the build cache)
    // instead of running the tests against the screenshots that were just pointed to.
    val fixtures = providers.environmentVariable("KRS_VISION_FIXTURES").orNull
    inputs.property("krsVisionFixtures", fixtures ?: "")
    inputs.property("krsVisionRandomScenes", providers.environmentVariable("KRS_VISION_RANDOM_SCENES").orNull ?: "")
    if (!fixtures.isNullOrBlank()) {
        // the screenshots live outside the project, so their content is not a tracked input either
        outputs.upToDateWhen { false }
        outputs.cacheIf { false }
    }
}
