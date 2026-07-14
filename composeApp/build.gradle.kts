import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) load(file.inputStream())
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.paparazzi)
}

sqldelight {
    databases {
        create("LocalMindDb") {
            packageName.set("com.markduenas.localmind.data.local")
        }
    }
    linkSqlite = false
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.all {
            linkerOpts("-framework", "CoreML")
            linkerOpts("-framework", "Accelerate")
            linkerOpts("-framework", "AVFoundation")
            linkerOpts("-framework", "Speech")
        }
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.sqldelight.android.driver)
            implementation(libs.sqlcipher.android)
            implementation(libs.androidx.security.crypto)
            implementation(libs.androidx.work.runtime)
            implementation(libs.androidx.glance.appwidget)
            implementation(libs.billing.client)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation.compose)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // SQLDelight
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)

            // Kotlin extensions
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)

            // UUID
            implementation(libs.uuid)

            // Cactus AI (on-device LLM + STT)
            implementation(libs.cactus)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.turbine)
        }
    }
}

android {
    namespace = "com.markduenas.localmind"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.markduenas.localmind"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 46
        versionName = "1.0.40"
    }

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties.getProperty("storeFile", ""))
            storePassword = keystoreProperties.getProperty("storePassword", "")
            keyAlias = keystoreProperties.getProperty("keyAlias", "")
            keyPassword = keystoreProperties.getProperty("keyPassword", "")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

tasks.withType<Test>().configureEach {
    testLogging {
        showStandardStreams = true
        events("passed", "failed", "skipped")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest>().configureEach {
    val runBenchmark = project.findProperty("localmindRunLlmBenchmark")?.toString().orEmpty()
    val periodic = project.findProperty("localmindBenchPeriodic")?.toString().orEmpty()
    val thirdModel = project.findProperty("localmindBenchThirdModel")?.toString().orEmpty()

    environment("LOCALMIND_RUN_LLM_BENCHMARK", runBenchmark)
    environment("LOCALMIND_BENCH_PERIODIC", periodic)
    if (thirdModel.isNotBlank()) {
        environment("LOCALMIND_BENCH_THIRD_MODEL", thirdModel)
    }

    if (runBenchmark.isNotBlank()) {
        args = args + "LOCALMIND_RUN_LLM_BENCHMARK=$runBenchmark"
    }
    if (periodic.isNotBlank()) {
        args = args + "LOCALMIND_BENCH_PERIODIC=$periodic"
    }
    if (thirdModel.isNotBlank()) {
        args = args + "LOCALMIND_BENCH_THIRD_MODEL=$thirdModel"
    }
}
