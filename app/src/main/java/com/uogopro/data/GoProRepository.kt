package com.uogopro.data

import com.uogopro.domain.CaptureMode
import com.uogopro.domain.EvCompensation
import com.uogopro.domain.FieldOfView
import com.uogopro.domain.FrameRate
import com.uogopro.domain.IsoLimit
import com.uogopro.domain.Sharpness
import com.uogopro.domain.VideoResolution
import com.uogopro.domain.WhiteBalance

class GoProRepository(
    private val httpClient: GoProHttpClient = GoProHttpClient(),
) {
    suspend fun getStatus(host: String) =
        Hero4StatusParser.parse(httpClient.get(host, Hero4CommandCatalog.STATUS))

    suspend fun pair(host: String, pin: String) {
        val normalizedPin = pin.trim()
        require(normalizedPin.length == 4 && normalizedPin.all { it.isDigit() }) {
            "Pairing PIN must be 4 digits"
        }
        httpClient.get(host, Hero4CommandCatalog.pairingStart(normalizedPin), secure = true)
        httpClient.get(host, Hero4CommandCatalog.pairingFinish(normalizedPin), secure = true)
    }

    suspend fun setMode(host: String, mode: CaptureMode) = command(host, Hero4CommandCatalog.mode(mode))
    suspend fun setSubMode(host: String, mode: CaptureMode, subMode: Int) = command(host, Hero4CommandCatalog.subMode(mode, subMode))
    suspend fun shutter(host: String, recording: Boolean) = command(host, if (recording) Hero4CommandCatalog.SHUTTER_ON else Hero4CommandCatalog.SHUTTER_OFF)
    suspend fun tagMoment(host: String) = command(host, Hero4CommandCatalog.TAG_MOMENT)
    suspend fun locate(host: String, enabled: Boolean) = command(host, if (enabled) Hero4CommandCatalog.LOCATE_ON else Hero4CommandCatalog.LOCATE_OFF)
    suspend fun powerOff(host: String) = command(host, Hero4CommandCatalog.POWER_OFF)
    suspend fun startPreview(host: String) = command(host, Hero4CommandCatalog.LIVE_STREAM_START)
    suspend fun stopPreview(host: String) = command(host, Hero4CommandCatalog.LIVE_STREAM_STOP)

    fun previewUri(host: String): String = Hero4CommandCatalog.previewUri(host)

    suspend fun setVideoResolution(host: String, value: VideoResolution) = command(host, Hero4CommandCatalog.videoResolution(value))
    suspend fun setFrameRate(host: String, value: FrameRate) = command(host, Hero4CommandCatalog.frameRate(value))
    suspend fun setFov(host: String, value: FieldOfView) = command(host, Hero4CommandCatalog.fov(value))
    suspend fun setLowLight(host: String, enabled: Boolean) = command(host, Hero4CommandCatalog.lowLight(enabled))
    suspend fun setSpotMeter(host: String, enabled: Boolean) = command(host, Hero4CommandCatalog.spotMeter(enabled))
    suspend fun setProtune(host: String, enabled: Boolean) = command(host, Hero4CommandCatalog.protune(enabled))
    suspend fun setWhiteBalance(host: String, value: WhiteBalance) = command(host, Hero4CommandCatalog.whiteBalance(value))
    suspend fun setIsoLimit(host: String, value: IsoLimit) = command(host, Hero4CommandCatalog.isoLimit(value))
    suspend fun setSharpness(host: String, value: Sharpness) = command(host, Hero4CommandCatalog.sharpness(value))
    suspend fun setEvCompensation(host: String, value: EvCompensation) = command(host, Hero4CommandCatalog.evCompensation(value))

    private suspend fun command(host: String, path: String) {
        httpClient.get(host, path)
    }
}
