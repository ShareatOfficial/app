package org.shareat.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform