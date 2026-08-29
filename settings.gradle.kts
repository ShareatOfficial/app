rootProject.name = "shareat"

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":androidApp")
include(":feature:home:domain")
include(":feature:home:ui")
include(":feature:profile:domain")
include(":feature:profile:ui")
include(":feature:login:ui")
include(":feature:login:domain")
include(":shared:domain")
include(":shared:data")
include(":shared:navigation")
include(":shared:ui")
include(":webApp")
