plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.tagangbiblia.app"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Two editions built from the same code. Each has its own database asset
    // (src/<flavor>/assets), strings and launcher icon, and installs side by side.
    flavorDimensions += "edition"
    productFlavors {
        create("angbiblia") {          // Tagalog Ang Biblia (1905) - keeps the original app id
            dimension = "edition"
            applicationId = "com.tagangbiblia.app"
            buildConfigField("String", "DB_ASSET", "\"TagAngBiblia.db\"")
            buildConfigField("String", "TABLE_PREFIX", "\"TagAngBiblia\"")
        }
        create("sefer") {              // The Sefer - American Standard Version (1901)
            dimension = "edition"
            applicationId = "com.tagangbiblia.sefer"
            buildConfigField("String", "DB_ASSET", "\"Sefer.db\"")
            buildConfigField("String", "TABLE_PREFIX", "\"ASV\"")
        }
    }
    // Keep the bundled Bible database uncompressed-friendly and untouched
    androidResources { noCompress += "db" }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
}
