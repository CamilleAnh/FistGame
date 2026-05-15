plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.navigation.safeargs)
}

android {
    namespace = "com.twinbrother.fruitsort"
    compileSdk = 35
    // --- Signing configuration (reads from gradle properties or local.properties) ---
    // Provide these properties in your local.properties (do NOT commit):
    //   RELEASE_STORE_FILE=/absolute/path/to/release-keystore.jks
    //   RELEASE_STORE_PASSWORD=your_store_password
    //   RELEASE_KEY_ALIAS=your_key_alias
    //   RELEASE_KEY_PASSWORD=your_key_password
    //
    // Or set them in ~/.gradle/gradle.properties.
    val keystorePathProp: String? = (project.findProperty("RELEASE_STORE_FILE") as String?) ?: (project.findProperty("keystorePath") as String?)
    val keystorePasswordProp: String? = (project.findProperty("RELEASE_STORE_PASSWORD") as String?) ?: (project.findProperty("keystorePassword") as String?)
    val keyAliasProp: String? = (project.findProperty("RELEASE_KEY_ALIAS") as String?) ?: (project.findProperty("keyAlias") as String?)
    val keyPasswordProp: String? = (project.findProperty("RELEASE_KEY_PASSWORD") as String?) ?: (project.findProperty("keyPassword") as String?)

    signingConfigs {
        create("release") {
            keystorePathProp?.let { storeFile = file(it) }
            keystorePasswordProp?.let { storePassword = it }
            keyAliasProp?.let { keyAlias = it }
            keyPasswordProp?.let { keyPassword = it }
        }
    }

    defaultConfig {
        applicationId = "com.twinbrother.fruitsort"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use release signing config if properties provided locally
            signingConfig = signingConfigs.findByName("release")
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
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.play.services.ads)
    implementation("com.android.billingclient:billing-ktx:7.1.1")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
