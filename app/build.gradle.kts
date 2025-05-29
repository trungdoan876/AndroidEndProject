plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "hcmute.edu.vn.projectfinalandroid"
    compileSdk = 35

    defaultConfig {
        applicationId = "hcmute.edu.vn.projectfinalandroid"
        minSdk = 24
        targetSdk = 34
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

    packaging {
        resources {
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Thêm ML Kit Text Recognition
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation("com.google.mlkit:text-recognition-chinese:16.0.0")
    implementation("com.google.mlkit:text-recognition-devanagari:16.0.0")
    implementation("com.google.mlkit:translate:17.0.2")
    implementation("com.google.mlkit:language-id:16.0.0")
    // tess-two
    implementation("com.rmtheis:tess-two:9.1.0")
    // CameraX
    implementation("androidx.camera:camera-core:1.4.2")
    implementation("androidx.camera:camera-camera2:1.4.2")
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    implementation("androidx.camera:camera-view:1.4.2")
    // Room database
    implementation("androidx.room:room-runtime:2.5.2")
    annotationProcessor("androidx.room:room-compiler:2.5.2")
    // Google Cloud Vision
    implementation ("com.google.cloud:google-cloud-vision:3.39.0")
    implementation ("com.google.auth:google-auth-library-oauth2-http:1.23.0")
    // Để gọi http
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    //json
    implementation ("com.google.code.gson:gson:2.10.1")

    // glide để tải ảnh từ internet
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

}