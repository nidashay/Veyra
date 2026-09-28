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
        versionCode = 1
        versionName = "1.0.0" // This is what BuildConfig.VERSION_NAME reads!
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }

    // 🔥 THIS IS THE FIX: Explicitly enable BuildConfig generation
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Coil for lightweight image loading
    implementation("io.coil-kt:coil:2.5.0")
}