import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.buildkonfig)
}

// Android includes its build type in the requested Gradle task; Xcode exports CONFIGURATION.
val requestedTasks = gradle.startParameter.taskNames.map { it.substringAfterLast(':') }
val isReleaseBuild = requestedTasks.any { it.contains("release", ignoreCase = true) } ||
    providers.environmentVariable("CONFIGURATION").orNull.equals("Release", ignoreCase = true)
val supabaseEnvironment = providers.gradleProperty("shareat.environment")
    .orElse(if (isReleaseBuild) "production" else "development")
    .map { it.lowercase() }
    .get()

require(supabaseEnvironment in setOf("development", "production")) {
    "shareat.environment must be either 'development' or 'production'"
}

val (defaultSupabaseUrl, defaultSupabasePublishableKey) = when (supabaseEnvironment) {
    "production" -> "https://eeolozlmnfzcdognlezy.supabase.co" to
        "sb_publishable_gDxtUHd6zy7pvrf7s5g_LA_Td-Vp1xJ"
    else -> "https://zgqurfalmblcjdelmyff.supabase.co" to
        "sb_publishable_-z55CLLxCmsulMWxnxRM9g_kyqoGcuU"
}

fun supabaseProperty(name: String, fallback: String): String =
    providers.gradleProperty("shareat.supabase.$supabaseEnvironment.$name")
        .orElse(providers.gradleProperty("shareat.supabase.$name"))
        .orElse(fallback)
        .get()

kotlin {
    jvm()
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
        namespace = "org.shareat.shared.data"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        enableCoreLibraryDesugaring = true

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        withHostTest {}
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.ktor.client.android)
        }
        commonMain.dependencies {
            api(project(":shared:domain"))

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.supabase.auth)
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.storage)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.koin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.kvault)
        }
        jsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}

dependencies {
    add("coreLibraryDesugaring", libs.android.desugarJdkLibs)
}

buildkonfig {
    packageName = "org.shareat.app.data.config"
    objectName = "SupabaseBuildConfig"

    defaultConfigs {
        buildConfigField(
            STRING,
            "SUPABASE_URL",
            supabaseProperty("url", defaultSupabaseUrl),
        )
        buildConfigField(
            STRING,
            "SUPABASE_PUBLISHABLE_KEY",
            supabaseProperty("publishableKey", defaultSupabasePublishableKey),
        )
    }
}
