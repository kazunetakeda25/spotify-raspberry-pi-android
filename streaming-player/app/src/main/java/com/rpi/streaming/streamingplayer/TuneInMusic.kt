package com.rpi.streaming.streamingplayer

import java.io.Serializable

class TuneInMusic(is_selected: Boolean, image: String, title: String, subImage: String, subtitle: String, href: String): Serializable {
    var bSelected: Boolean? = is_selected
    var image: String? = image
    var title: String? = title
    var subImage: String? = subImage
    var subTitle: String? = subtitle
    var href: String? = href

}