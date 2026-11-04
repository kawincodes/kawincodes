plugins {
    id("com.android.library")
    id("app.cash.paparazzi")
}
android {
    namespace = "net.venussolutions.venusmicrofindailyweeklycollection.clone"
    compileSdk = 36
    defaultConfig { minSdk = 28 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    lint { abortOnError = false; checkReleaseBuilds = false }
}
tasks.withType<Test>().configureEach { maxHeapSize = "5g"; jvmArgs("-Xss4m"); systemProperty("render.filter", System.getProperty("render.filter") ?: ""); ignoreFailures = true }
dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.14.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    testImplementation("junit:junit:4.13.2")
}
