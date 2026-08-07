package org.shareat.app

// I leave this here as example of interface to be implemented in each platform
interface Platform {
    val name: String
}

expect fun getPlatform(): Platform