import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.androidxRoom)
    alias(libs.plugins.ksp)
}

val gitCommitCount = 2//getGitCount()
val gitCommitHash = "1"//getGitHash()

val appName = "c001apk-KMP"
val pkgName = "com.example.c001apk.kmp"

kotlin {
    androidTarget()

    jvm("desktop")

    /*listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = appName
            isStatic = true
        }
    }*/

    sourceSets {
        /*androidMain.dependencies {
            implementation(libs.androidx.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.koin.android)
        }*/
        /*iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }*/
        val desktopMain by getting {
            kotlin {
                srcDir("build/generated/ksp/metadata")
            }
            dependencies {
                implementation(compose.desktop.common)
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.ktor.client.cio)
                implementation(libs.slf4j)
            }
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.materialIconsExtended)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.androidx.navigation.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.kotlinx.coroutines.core)

            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.serialization.gson)
            implementation(libs.ktor.client.logging)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            // Room
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)

            // DataStore
            implementation(libs.atomicfu)
            implementation(libs.androidx.datastore.core.okio)
            implementation(libs.androidx.datastore.preferences.core)

            // Decompose
            implementation(libs.decompose.decompose)
            implementation(libs.essenty.lifecycle)
            implementation(libs.decompose.extensionsComposeJetbrains)

            implementation(libs.kamel)
            implementation(libs.zoomable)
            implementation(libs.material.kolor)
            implementation(libs.jbcrypt)
            implementation(libs.jsoup)
            implementation(libs.dokar3.sonner)
            implementation(libs.htmlconverter.jvm)
            implementation(libs.constraintlayout.compose.multiplatform)
            implementation(libs.material3.windowsizeclass.multiplatform)
            // api(libs.compose.webview.multiplatform)

        }
    }
    /*configurations.all {
        resolutionStrategy {
            force("androidx.compose.material:material-ripple:1.7.0-alpha05")
        }
    }*/
}

android {
    namespace = pkgName
    compileSdk = 35
    defaultConfig {
        applicationId = pkgName
        minSdk = 21
        targetSdk = 35
        versionName = gitCommitHash
        versionCode = gitCommitCount
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    packaging {
        resources {
            excludes += "/kotlin**"
            excludes += "/META-INF/**"
            excludes += "/META-INF/**/**"
            excludes += "/kotlin/**"
            excludes += "/okhttp3/**"
            excludes += "/DebugProbesKt.bin"
        }
    }
    applicationVariants.all {
        outputs.all {
            (this as BaseVariantOutputImpl).outputFileName =
                "$appName-$versionName($versionCode)-$name.apk"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = appName
            packageVersion = "1.0.$gitCommitCount"
            modules("jdk.unsupported")

            description = "c001apk-KMP"
            copyright = "© 2024 bggRGjQaUbCoE and open source contributors. All rights reserved."
            licenseFile.set(project.file("../LICENSE"))

            linux { iconFile = file("src/desktopMain/resources/linux/Icon.png") }
            macOS { iconFile = file("src/desktopMain/resources/macOS/Icon.icns") }
            windows { iconFile = file("src/desktopMain/resources/windows/Icon.ico") }
        }
    }
}



buildConfig {
    packageName("c001apk_kmp.composeapp")
    buildConfigField("VERSION_NAME", provider { gitCommitHash })
    buildConfigField("VERSION_CODE", provider { gitCommitCount.toString() })
}

fun getGitCount(): Int {
    val out = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-list", "--count", "HEAD")
        standardOutput = out
    }
    return out.toString().trim().toInt()
}

fun getGitHash(): String {
    val out = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "--verify", "--short", "HEAD")
        standardOutput = out
    }
    return out.toString().trim()
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspCommonMainMetadata", libs.androidx.room.compiler)
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}
