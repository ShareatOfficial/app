import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.koin.compiler)
}

kotlin {
    jvm(); iosArm64(); iosSimulatorArm64()
    js { browser() }
    @OptIn(ExperimentalWasmDsl::class) wasmJs { browser() }
    android {
        namespace = "org.shareat.feature.menu.ui"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions { jvmTarget = JvmTarget.JVM_11 }
        androidResources { enable = true }
    }
    sourceSets {
        androidMain.dependencies { implementation(libs.compose.uiTooling) }
        commonMain.dependencies {
            implementation(project(":feature:menu:domain"))
            implementation(project(":shared:designsystem"))
            implementation(project(":shared:domain"))
            implementation(project(":shared:navigation"))
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.annotations)
            implementation(libs.jetbrains.navigation3.ui)
            implementation(libs.filekit.dialogs.compose)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies { implementation(libs.kotlin.test); implementation(libs.kotlinx.coroutines.test) }
        webMain.dependencies { implementation(libs.wrappers.browser) }
    }
}

dependencies { androidRuntimeClasspath(libs.compose.uiTooling) }
