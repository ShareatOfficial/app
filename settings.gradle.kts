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
include(":feature:home")
include(":feature:profile:domain")
include(":feature:profile:ui")
include(":feature:login:ui")
include(":feature:login:domain")
include(":feature:menu:domain")
include(":feature:menu:ui")
include(":shared:domain")
include(":shared:data")
include(":shared:navigation")
include(":shared:ui")
include(":webApp")
