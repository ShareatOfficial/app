import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    jvm()
    iosArm64()
    iosSimulatorArm64()

    js { browser() }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }

    android {
        namespace = "org.shareat.feature.subscription.data"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions { jvmTarget = JvmTarget.JVM_11 }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":feature:subscription:domain"))
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val mobileMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.purchases.kmp.core)
            }
        }
        androidMain.get().dependsOn(mobileMain)
        iosMain.get().apply {
            dependsOn(mobileMain)
        }
        named { it.lowercase().startsWith("ios") }.configureEach {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

buildkonfig {
    packageName = "org.shareat.feature.subscription.data.config"
    objectName = "RevenueCatBuildConfig"

    defaultConfigs {
        buildConfigField(
            STRING,
            "REVENUECAT_ANDROID_API_KEY",
            providers.gradleProperty("shareat.revenuecat.androidApiKey")
                .orElse("test_zurEuEMpYKqdazDPfluDhydYgxy")
                .get(),
        )
        buildConfigField(
            STRING,
            "REVENUECAT_IOS_API_KEY",
            providers.gradleProperty("shareat.revenuecat.iosApiKey")
                .orElse("test_zurEuEMpYKqdazDPfluDhydYgxy")
                .get(),
        )
    }
}
