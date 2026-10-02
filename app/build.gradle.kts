plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.veyra.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.veyra.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "1.0.9"
    }

    // 🔥 SIGNING CONFIGURATION
    signingConfigs {
        create("release") {
            storeFile = file("../veyra-release.keystore")
            storePassword = "clin200812345678" // ⚠️ REPLACE THIS!
            keyAlias = "veyra"
            keyPassword = "clin200812345678" // ⚠️ REPLACE THIS! (usually same as keystore password)
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release") // 🔥 Use the release signing config
        }
        debug {
            signingConfig = signingConfigs.getByName("release") // 🔥 Sign debug builds too!
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("io.coil-kt:coil:2.5.0")
}