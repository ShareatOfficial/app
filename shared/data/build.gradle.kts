import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.buildkonfig)
}

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
            providers.gradleProperty("shareat.supabase.url")
                .orElse("https://zgqurfalmblcjdelmyff.supabase.co")
                .get(),
        )
        buildConfigField(
            STRING,
            "SUPABASE_PUBLISHABLE_KEY",
            providers.gradleProperty("shareat.supabase.publishableKey")
                .orElse("sb_publishable_-z55CLLxCmsulMWxnxRM9g_kyqoGcuU")
                .get(),
        )
    }
}
