plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.motherload"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.motherload"
        minSdk = 24
        targetSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    val room_version  = "2.5.0"

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("androidx.preference:preference:1.2.1")
    implementation("org.osmdroid:osmdroid-android:6.1.17")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("com.google.android.gms:play-services-location:17.1.0")
    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    testImplementation("junit:junit:4.13.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    kapt ("androidx.room:room-compiler:$room_version")
    kapt ("android.arch.persistence.room:compiler:1.1.1")
    implementation("androidx.room:room-common:$room_version")
}