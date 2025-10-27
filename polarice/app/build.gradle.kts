plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.polaricemobile"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.polaricemobile"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    
    // HTTP client for API calls
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    
    // JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Supabase client
    implementation("io.github.jan-tennert.supabase:supabase-kt:2.0.4")
    implementation("io.github.jan-tennert.supabase:auth-kt:2.0.4")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.0.4")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.0.4")
    
    // Coroutines for async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}