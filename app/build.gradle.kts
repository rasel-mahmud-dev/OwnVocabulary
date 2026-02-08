import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.rs.myvocabulary"
    compileSdk = 35

    val properties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        properties.load(FileInputStream(localPropertiesFile))
    }

    defaultConfig {
        applicationId = "com.rs.myvocabulary"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "IMAGEKIT_PUBLIC_KEY", "\"${properties.getProperty("IMAGEKIT_PUBLIC_KEY", "")}\"")
        buildConfigField("String", "API_KEY", "\"${properties.getProperty("API_KEY", "")}\"")
        buildConfigField("String", "GEMINI_API_KEYS", "\"${properties.getProperty("GEMINI_API_KEYS", "")}\"")
        buildConfigField("String", "GROQ_API_KEYS", "\"${properties.getProperty("GROQ_API_KEYS", "")}\"")

        buildConfigField(
                "String",
                "API_BASE_URL",
                "\"${properties.getProperty("API_BASE_URL", "")}\""
        )
        buildConfigField(
                "String",
                "API_SECRET_KEY",
                "\"${properties.getProperty("API_SECRET_KEY", "")}\""
        )
    }

    signingConfigs {
        create("testConfig") {
            storeFile = file("./test-key.jks")
            storePassword = "123456"
            keyAlias = "key0"
            keyPassword = "123456"
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("testConfig")
            isDebuggable = true
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }

        release {
            isMinifyEnabled = true
            isDebuggable = false
            signingConfig = signingConfigs.getByName("testConfig")
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
    kotlinOptions { jvmTarget = "11" }
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
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.media3.exoplayer)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("io.coil-kt:coil-compose:2.4.0") // If using Coil v2
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.google.accompanist:accompanist-coil:0.15.0")

    implementation("androidx.lifecycle:lifecycle-runtime:2.9.2")
    implementation("androidx.compose.ui:ui-android:1.9.0")

    implementation("androidx.compose.foundation:foundation:1.9.0") // or latest version
    implementation("androidx.compose.ui:ui-text:1.9.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.2")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    implementation("androidx.work:work-runtime-ktx:2.10.3")

    implementation("androidx.navigation:navigation-compose:2.9.3")

}
