plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

apply(from = "generator.gradle.kts")

android {
    namespace = "com.lxy.molweightcalculator"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lxy.molweightcalculator"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        @Suppress("UNCHECKED_CAST")
        val parseElementData = extra["parseElementData"] as () -> Pair<String, String>
        val (ordinals, weights) = parseElementData()
        buildConfigField("int[]", "ELEMENT_ORDINALS", ordinals)
        buildConfigField("double[]", "ELEMENT_WEIGHTS", weights)
    }

    signingConfigs.register("config") {
        keyAlias = "lxy"
        keyPassword = "123456"
        storeFile = file("key.jks")
        storePassword = "123456"
        enableV2Signing = true
        enableV2Signing = true
        enableV4Signing = true
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("config")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("config")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
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

androidComponents {
    onVariants(selector().withBuildType("release")) {
        val excludes = it.packaging.resources.excludes
        excludes.add("/DebugProbesKt.bin")
        excludes.add("META-INF/*.version")
        excludes.add("/kotlin/*.kotlin_builtins")
        excludes.add("/kotlin/**/*.kotlin_builtins")
        excludes.add("/kotlin-tooling-metadata.json")
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.adaptive)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.timber)
    implementation(platform(libs.androidx.compose.bom))
}