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
include(":feature:restaurantHome")
include(":feature:review")
include(":feature:lastActivity")
include(":feature:home:domain")
include(":feature:home:ui")
include(":feature:restaurant:domain")
include(":feature:restaurant:ui")
include(":feature:settings:domain")
include(":feature:settings:ui")
include(":feature:login:ui")
include(":feature:login:domain")
include(":shared:domain")
include(":shared:data")
include(":shared:navigation")
include(":shared:designsystem")
include(":shared:ui")
include(":webApp")
