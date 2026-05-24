plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.textrecognizer"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.textrecognizer"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // CameraX core library
    implementation("androidx.camera:camera-core:1.3.1")
    // CameraX Camera2 library
    implementation("androidx.camera:camera-camera2:1.3.1")
    // CameraX Lifecycle library
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    // CameraX View class (This fixes the red PreviewView!)
    implementation("androidx.camera:camera-view:1.3.1")
    // Google ML Kit Text Recognition
    implementation("com.google.mlkit:text-recognition:16.0.0")

    implementation("androidx.core:core-splashscreen:1.2.0")
}