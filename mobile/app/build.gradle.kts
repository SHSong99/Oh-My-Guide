import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

android {
    namespace = "com.ohmyguide.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ohmyguide.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["NAVER_MAP_CLIENT_ID"] =
            localProperties.getProperty("NAVER_MAP_CLIENT_ID", "")

        buildConfigField("String", "NAVER_MAP_CLIENT_ID", "\"${localProperties.getProperty("NAVER_MAP_CLIENT_ID", "")}\"")
        buildConfigField("String", "BASE_URL", "\"${localProperties.getProperty("BASE_URL", "")}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${localProperties.getProperty("GOOGLE_WEB_CLIENT_ID", "")}\"")
        buildConfigField("String", "ODSAY_API_KEY", "\"${localProperties.getProperty("ODSAY_API_KEY", "")}\"")
        buildConfigField("String", "BUSAN_BIMS_SERVICE_KEY", "\"${localProperties.getProperty("BUSAN_BIMS_SERVICE_KEY", "")}\"")
        buildConfigField("String", "NAVER_MAP_CLIENT_SECRET", "\"${localProperties.getProperty("NAVER_MAP_CLIENT_SECRET", "")}\"")
        buildConfigField("String", "TMAP_APP_KEY", "\"${localProperties.getProperty("TMAP_APP_KEY", "")}\"")
        buildConfigField("String", "GOOGLE_CLOUD_TTS_KEY", "\"${localProperties.getProperty("GOOGLE_CLOUD_TTS_KEY", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.okhttp.sse)

    // Coroutines
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.play.services)

    // ViewModel
    implementation(libs.lifecycle.viewmodel.compose)

    // Navigation
    implementation(libs.navigation.compose)

    // Naver Map
    implementation(libs.naver.map)
    implementation(libs.naver.map.compose)

    // Location
    implementation(libs.play.services.location)

    // Credential Manager (Google Sign-In)
    implementation(libs.credentials)
    implementation(libs.credentials.play)
    implementation(libs.googleid)

    // Encrypted Storage
    implementation(libs.security.crypto)

    // Google Sign-In (Authorization API for access token)
    implementation(libs.play.services.auth)

    // Image Loading
    implementation(libs.coil.compose)

    // Video Player
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
