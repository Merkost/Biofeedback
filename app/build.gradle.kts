plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.merkost.biofeedback"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.merkost.biofeedback"
        minSdk = 29
        targetSdk = 34
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

composeCompiler {
    enableStrongSkippingMode = true

    reportsDestination = layout.buildDirectory.dir("compose_compiler")
}

dependencies {
    implementation(project(":healbe"))

    implementation(libs.coil.compose)
    implementation(libs.coil.svg)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.lottie.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    implementation("com.patrykandpatrick.vico:compose:2.0.0-alpha.21")
    implementation("com.patrykandpatrick.vico:compose-m3:2.0.0-alpha.21")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Logging
    api("com.jakewharton.timber:timber:5.0.1")

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-rx2:1.5.2")


    // HEALBE SDK
    api("com.healbe:healbesdk:1.5.0")

    // rxJava
    api("io.reactivex.rxjava2:rxjava:2.2.21")
    api("io.reactivex.rxjava2:rxandroid:2.1.1")
}