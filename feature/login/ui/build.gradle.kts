import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.File

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.koin.compiler)
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    android {
        namespace = "org.shareat.feature.login.ui"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        // Required for the compose resources in this module to be packaged as Android assets.
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":feature:login:domain"))
            implementation(project(":shared:domain"))
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.jetbrains.material3.adaptive)
            implementation(libs.compose.ui)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.components.resources)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.annotations)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.compose.ui.test)
        }
    }
}

// Pinned rather than inferred from the project path so the `Res` import stays stable if the
// module ever moves.
compose.resources {
    packageOfResClass = "shareat.feature.login.ui.generated.resources"
}

// Configure wasmJs browser tests to use Playwright's Chrome if available.
// WHY: This machine has no /Applications/Google Chrome.app and it cannot be installed,
// so Karma cannot find Chrome. We glob Playwright's cache for the highest-versioned
// chromium-* directory that contains an executable Chrome binary, and set CHROME_BIN so
// Karma uses it. No-ops silently on CI/Linux where CHROME_BIN is already set in the
// environment or Chrome is available on PATH.
val resolvedChromeBin = providers.environmentVariable("CHROME_BIN")
    .orElse(
        providers.environmentVariable("HOME").map { home ->
            File("$home/Library/Caches/ms-playwright")
                .listFiles { f -> f.isDirectory && f.name.startsWith("chromium-") }
                ?.sortedByDescending { it.name.removePrefix("chromium-").toIntOrNull() ?: 0 }
                ?.firstNotNullOfOrNull { chromiumDir ->
                    chromiumDir.walk().firstOrNull { f ->
                        f.isFile && f.canExecute() && f.name.contains("Google Chrome")
                    }?.absolutePath
                }.orEmpty()
        }
    )

tasks.withType<org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest>().configureEach {
    val bin = resolvedChromeBin.orNull
    if (!bin.isNullOrEmpty()) {
        environment("CHROME_BIN", bin)
    }
}
