package com.uogopro.data

import com.uogopro.domain.CaptureMode
import com.uogopro.domain.EvCompensation
import com.uogopro.domain.FieldOfView
import com.uogopro.domain.FrameRate
import com.uogopro.domain.IsoLimit
import com.uogopro.domain.Sharpness
import com.uogopro.domain.VideoResolution
import com.uogopro.domain.WhiteBalance

object Hero4CommandCatalog {
    const val DEFAULT_HOST = "10.5.5.9"

    const val STATUS = "/gp/gpControl/status"
    const val SHUTTER_ON = "/gp/gpControl/command/shutter?p=1"
    const val SHUTTER_OFF = "/gp/gpControl/command/shutter?p=0"
    const val TAG_MOMENT = "/gp/gpControl/command/storage/tag_moment"
    const val LOCATE_ON = "/gp/gpControl/command/system/locate?p=1"
    const val LOCATE_OFF = "/gp/gpControl/command/system/locate?p=0"
    const val POWER_OFF = "/gp/gpControl/command/system/sleep"
    const val LIVE_STREAM_START = "/gp/gpControl/execute?p1=gpStream&a1=proto_v2&c1=restart"
    const val LIVE_STREAM_STOP = "/gp/gpControl/execute?p1=gpStream&c1=stop"

    fun mode(mode: CaptureMode): String = "/gp/gpControl/command/mode?p=${mode.code}"

    fun subMode(mode: CaptureMode, subMode: Int): String =
        "/gp/gpControl/command/sub_mode?mode=${mode.code}&sub_mode=$subMode"

    fun videoResolution(value: VideoResolution): String = setting(2, value.id)
    fun frameRate(value: FrameRate): String = setting(3, value.id)
    fun fov(value: FieldOfView): String = setting(4, value.id)
    fun lowLight(enabled: Boolean): String = setting(8, enabled.asHero4Flag())
    fun spotMeter(enabled: Boolean): String = setting(9, enabled.asHero4Flag())
    fun protune(enabled: Boolean): String = setting(10, enabled.asHero4Flag())
    fun whiteBalance(value: WhiteBalance): String = setting(11, value.id)
    fun isoLimit(value: IsoLimit): String = setting(13, value.id)
    fun sharpness(value: Sharpness): String = setting(14, value.id)
    fun evCompensation(value: EvCompensation): String = setting(15, value.id)

    fun pairingStart(pin: String): String = "/gpPair?c=start&pin=$pin&mode=0"
    fun pairingFinish(pin: String): String = "/gpPair?c=finish&pin=$pin&mode=0"

    fun previewUri(host: String): String {
        val normalizedHost = host.trim()
            .removePrefix("http://")
            .removePrefix("https://")
            .trimEnd('/')
            .substringBefore(':')
        return "udp://$normalizedHost:8554"
    }

    private fun setting(settingId: Int, value: Int): String =
        "/gp/gpControl/setting/$settingId/$value"

    private fun Boolean.asHero4Flag(): Int = if (this) 1 else 0
}
