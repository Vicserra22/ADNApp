plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.adnapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.adnapp"
        minSdk = 26
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
        viewBinding = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.google.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.core.splashscreen.v100)
    implementation(libs.picasso)
    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom.v33130))

    // When using the BoM, don't specify versions in Firebase dependencies
    implementation(libs.google.firebase.analytics)
    implementation(libs.firebase.auth.ktx)

    // Retrofit
    implementation (libs.converter.gson)
    implementation(libs.retrofit)
    implementation (libs.gson)

//// Glide (para cargar imágenes en el RecyclerView)
//    implementation ('com.github.bumptech.glide:glide:4.16.0')
//    kapt 'com.github.bumptech.glide:compiler:4.16.0'

}