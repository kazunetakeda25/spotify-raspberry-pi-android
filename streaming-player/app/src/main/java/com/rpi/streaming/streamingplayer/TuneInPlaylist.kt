package com.rpi.streaming.streamingplayer

import java.io.Serializable

data class TuneInPlaylist(
    var status: Int,
    var image: String,
    var title: String,
    var subImage: String,
    var subTitle: String,
    var href: String,
    var url: String
) : Serializable