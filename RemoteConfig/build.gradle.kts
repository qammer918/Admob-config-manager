plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.module.remoteconfig"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "app_open_splash_id", "ca-app-pub-3940256099942544/9257395921")
            resValue("string", "interstitial_splash_id", "ca-app-pub-3940256099942544/4411468910")
            resValue("string", "app_open_in_app_id", "ca-app-pub-3940256099942544/9257395921")
            resValue("string", "banner_splash_id", "ca-app-pub-3940256099942544/9214589741")
            resValue("string", "interstitial_function_id", "ca-app-pub-3940256099942544/4411468910")
            resValue("string", "native_onboarding_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "native_onboarding_id2", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "native_exit_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "native_functions_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "native_home_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "native_language_id", "ca-app-pub-3940256099942544/2247696110")

        }


        debug {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "app_open_splash_id", "ca-app-pub-3940256099942544/9257395921")
            resValue("string", "interstitial_splash_id", "ca-app-pub-3940256099942544/4411468910")
            resValue("string", "app_open_in_app_id", "ca-app-pub-3940256099942544/9257395921")
            resValue("string", "banner_splash_id", "ca-app-pub-3940256099942544/9214589741")
            resValue("string", "interstitial_function_id", "ca-app-pub-3940256099942544/4411468910")
            resValue("string", "native_onboarding_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "native_onboarding_id2", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "native_exit_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "native_functions_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "native_home_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "native_language_id", "ca-app-pub-3940256099942544/2247696110")


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
        buildConfig = true
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation (platform(libs.firebase.bom))
    implementation (libs.firebase.analytics)
    implementation (libs.firebase.config)

    implementation(libs.gson)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)



}