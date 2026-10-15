plugins {
    id("com.android.application") version "8.2.2"
    id("org.jetbrains.kotlin.android") version "1.9.22"
}

// Override with -P, e.g. ./gradlew installDebug -PcoinflowMerchantId=your-merchant-id
fun exampleProperty(name: String, default: String): String = providers.gradleProperty(name).getOrElse(default)

android {
    namespace = "cash.coinflow.fraud.example"
    compileSdk = 34

    defaultConfig {
        applicationId = "cash.coinflow.fraud.example.android"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "MERCHANT_ID", "\"${exampleProperty("coinflowMerchantId", "coinflow")}\"")
        buildConfigField("String", "ENV", "\"${exampleProperty("coinflowEnv", "sandbox")}\"")
        // Optional. When set, "Send request" POSTs here with the x-device-id header attached.
        buildConfigField("String", "CHARGE_URL", "\"${exampleProperty("coinflowChargeUrl", "")}\"")
        // For scripted runs: collect once after launch and log the headers.
        buildConfigField("boolean", "AUTORUN", "${exampleProperty("coinflowFraudAutorun", "0") == "1"}")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("cash.coinflow:coinflow-fraud:0.1.1")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
