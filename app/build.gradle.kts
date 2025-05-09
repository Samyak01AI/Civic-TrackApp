plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.civic_trackapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.civic_trackapplication"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding=true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.storage.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.google.firebase:firebase-firestore-ktx:24.10.0")
    implementation("com.google.firebase:firebase-auth-ktx:22.3.0")
    implementation("com.facebook.android:facebook-android-sdk:16.3.0")
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.cloudinary:cloudinary-android:3.0.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("org.json:json:20220924")
}