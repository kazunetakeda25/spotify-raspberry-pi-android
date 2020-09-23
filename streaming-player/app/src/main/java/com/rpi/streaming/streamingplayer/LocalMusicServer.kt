package com.rpi.streaming.streamingplayer

import java.io.Serializable

data class LocalMusicServer(
    var username: String? = null,
    var password: String? = null,
    var hostName: String? = null,
    var sharedRootDirectory: String? = null
): Serializable
