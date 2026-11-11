import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.krstech.chesshelp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.krstech.chesshelp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Release builds are signed with the debug key so `assembleRelease` produces an
            // installable APK out of the box. Replace with your own signingConfig before publishing.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE*",
                "META-INF/NOTICE*",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1",
                "META-INF/*.kotlin_module",
                "META-INF/INDEX.LIST",
                "META-INF/io.netty.versions.properties",
            )
        }
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            // The UI tests run on Robolectric, which needs the merged resources and manifest.
            isIncludeAndroidResources = true
            all { test ->
                // Optional mirror for the Android runtime jars Robolectric downloads on first use:
                // gradle :app:testDebugUnitTest -ProbolectricRepoUrl=https://...
                providers.gradleProperty("robolectricRepoUrl").orNull?.let {
                    test.systemProperty("robolectric.dependency.repo.url", it)
                }
            }
        }
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    // PHASE2: the integration step adds implementation(project(":core")), (":vision"), (":claude") here.

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test.junit)
    testImplementation("org.robolectric:robolectric:4.16.1")
    testImplementation("androidx.test:core:1.7.0")
}
