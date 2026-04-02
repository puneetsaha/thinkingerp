import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget()

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.sqldelight.driver.android)
            implementation(libs.koin.android)
            // Barcode scanning (Android)
            implementation(libs.mlkit.barcode.scanning)
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.navigation.compose)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Ktor (Claude API)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)

            // SQLDelight
            implementation(libs.sqldelight.coroutines)

            // Koin DI
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.java)
            implementation(libs.sqldelight.driver.jvm)
            // Barcode scanning (Desktop)
            implementation(libs.zxing.core)
        }
    }
}

android {
    namespace = "com.thinkingerp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.thinkingerp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

compose.desktop {
    application {
        mainClass = "com.thinkingerp.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Msi)
            packageName = "ThinkingERP"
            packageVersion = "1.0.0"
            windows {
                menuGroup = "ThinkingERP"
                upgradeUuid = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
            }
        }
    }
}

sqldelight {
    databases {
        create("ThinkingERPDatabase") {
            packageName.set("com.thinkingerp.data.database")
            srcDirs.setFrom("src/commonMain/sqldelight")
        }
    }
}
