import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.krstech.chesshelp.capture"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    lint {
        abortOnError = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

val robolectricAndroidAll: Configuration by configurations.creating {
    isTransitive = false
}

dependencies {
    api(project(":vision"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    // Mocks the (final, constructor-less) platform classes so the capture lifecycle can run on the JVM.
    testImplementation("org.mockito:mockito-core:5.14.2")
    // Real Skia on the JVM (native graphics mode), so the images sent to Claude are actually rendered in a test.
    testImplementation("org.robolectric:robolectric:4.16.1")
    robolectricAndroidAll("org.robolectric:android-all-instrumented:15-robolectric-13954326-i7")
}

// Robolectric would download its Android runtime jar by itself while the tests run, bypassing Gradle's
// repositories, cache and offline mode. Resolve it as an ordinary dependency instead and point Robolectric
// at the copy (its documented offline setup). The version is the SDK 35 runtime of Robolectric 4.16.1.
val robolectricRuntimeDir = layout.buildDirectory.dir("robolectric-runtime")
val copyRobolectricRuntime by tasks.registering(Copy::class) {
    from(robolectricAndroidAll)
    into(robolectricRuntimeDir)
}

tasks.withType<Test>().configureEach {
    dependsOn(copyRobolectricRuntime)
    systemProperty("robolectric.offline", "true")
    systemProperty("robolectric.dependency.dir", robolectricRuntimeDir.get().asFile.absolutePath)
}
