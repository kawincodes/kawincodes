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
}
