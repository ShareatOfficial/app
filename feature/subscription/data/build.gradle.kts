import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.buildkonfig)
}

// Android includes its build type in the requested Gradle task; Xcode exports CONFIGURATION.
val requestedTasks = gradle.startParameter.taskNames
val isIosReleaseBuild =
    providers.environmentVariable("CONFIGURATION").orNull.equals("Release", ignoreCase = true) ||
        requestedTasks.any {
            it.contains("ios", ignoreCase = true) && it.contains("release", ignoreCase = true)
        }
val isAndroidReleaseBuild = requestedTasks.any {
    it.contains("release", ignoreCase = true) && !it.contains("ios", ignoreCase = true)
}
val isReleaseBuild = isAndroidReleaseBuild || isIosReleaseBuild
val revenueCatEnvironment = providers.gradleProperty("shareat.environment")
    .orElse(if (isReleaseBuild) "production" else "development")
    .map { it.lowercase() }
    .get()

require(revenueCatEnvironment in setOf("development", "production")) {
    "shareat.environment must be either 'development' or 'production'"
}

val revenueCatTestStoreApiKey = "test_zurEuEMpYKqdazDPfluDhydYgxy"

fun revenueCatApiKey(
    platform: String,
    productionPrefix: String,
    requiredForCurrentBuild: Boolean,
): String {
    val environmentProperty = "shareat.revenuecat.$revenueCatEnvironment.${platform}ApiKey"
    val legacyProperty = "shareat.revenuecat.${platform}ApiKey"
    val apiKey = providers.gradleProperty(environmentProperty)
        .orElse(providers.gradleProperty(legacyProperty))
        .orElse(if (revenueCatEnvironment == "development") revenueCatTestStoreApiKey else "")
        .get()

    if (revenueCatEnvironment == "production" && (requiredForCurrentBuild || apiKey.isNotEmpty())) {
        require(apiKey.startsWith(productionPrefix)) {
            "Production builds require $environmentProperty to use a $productionPrefix RevenueCat public SDK key"
        }
    }

    return apiKey
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
            revenueCatApiKey(
                platform = "android",
                productionPrefix = "goog_",
                requiredForCurrentBuild = isAndroidReleaseBuild || !isIosReleaseBuild,
            ),
        )
        buildConfigField(
            STRING,
            "REVENUECAT_IOS_API_KEY",
            revenueCatApiKey(
                platform = "ios",
                productionPrefix = "appl_",
                requiredForCurrentBuild = isIosReleaseBuild || !isAndroidReleaseBuild,
            ),
        )
    }
}
