plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.navigation.safeargs)
}

android {
    namespace = "com.twinbrother.fruitsort"
    compileSdk = 35

    // --- Signing configuration ---
    val keystorePathProp: String? = (project.findProperty("RELEASE_STORE_FILE") as String?)
    val keystorePasswordProp: String? = (project.findProperty("RELEASE_STORE_PASSWORD") as String?)
    val keyAliasProp: String? = (project.findProperty("RELEASE_KEY_ALIAS") as String?)
    val keyPasswordProp: String? = (project.findProperty("RELEASE_KEY_PASSWORD") as String?)

    signingConfigs {
        create("release") {
            if (keystorePathProp != null && keystorePasswordProp != null && 
                keyAliasProp != null && keyPasswordProp != null) {
                storeFile = file(keystorePathProp)
                storePassword = keystorePasswordProp
                keyAlias = keyAliasProp
                keyPassword = keyPasswordProp
            }
        }
    }

    defaultConfig {
        applicationId = "com.twinbrother.fruitsort"
        minSdk = 30
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Chỉ áp dụng signingConfig nếu storeFile đã được thiết lập để tránh NullPointerException
            if (keystorePathProp != null) {
                signingConfig = signingConfigs.getByName("release")
            }
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.play.services.ads)
    implementation("com.android.billingclient:billing-ktx:7.1.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
