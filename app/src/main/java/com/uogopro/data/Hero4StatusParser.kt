package com.uogopro.data

import com.uogopro.domain.BatteryLevel
import com.uogopro.domain.CameraState
import com.uogopro.domain.CaptureMode
import com.uogopro.domain.EvCompensation
import com.uogopro.domain.FieldOfView
import com.uogopro.domain.FrameRate
import com.uogopro.domain.IsoLimit
import com.uogopro.domain.SdCardState
import com.uogopro.domain.Sharpness
import com.uogopro.domain.VideoResolution
import com.uogopro.domain.VideoSettings
import com.uogopro.domain.WhiteBalance
import org.json.JSONObject

object Hero4StatusParser {
    fun parse(rawJson: String): CameraState {
        val root = JSONObject(rawJson)
        val status = root.optJSONObject("status") ?: JSONObject()
        val settings = root.optJSONObject("settings") ?: JSONObject()

        return CameraState(
            connected = true,
            batteryAvailable = status.optIntOrNull("1") == 1,
            batteryLevel = BatteryLevel.fromCode(status.optIntOrNull("2")),
            isRecording = status.optIntOrNull("8") == 1,
            recordingDurationSeconds = status.optIntOrNull("13") ?: 0,
            clientsConnected = status.optIntOrNull("31") ?: 0,
            streaming = status.optIntOrNull("32") == 1,
            sdCardState = SdCardState.fromCode(status.optIntOrNull("33")),
            remainingPhotos = status.optIntOrNull("34"),
            remainingVideoSeconds = status.optIntOrNull("35"),
            mode = CaptureMode.fromCode(status.optIntOrNull("43")),
            subMode = status.optIntOrNull("44") ?: 0,
            freeSpaceBytes = status.optLongOrNull("54"),
            videoSettings = VideoSettings(
                resolution = VideoResolution.fromId(settings.optIntOrNull("2")),
                frameRate = FrameRate.fromId(settings.optIntOrNull("3")),
                fov = FieldOfView.fromId(settings.optIntOrNull("4")),
                lowLight = settings.optIntOrNull("8") == 1,
                spotMeter = settings.optIntOrNull("9") == 1,
                protune = settings.optIntOrNull("10") == 1,
                whiteBalance = WhiteBalance.fromId(settings.optIntOrNull("11")),
                isoLimit = IsoLimit.fromId(settings.optIntOrNull("13")),
                sharpness = Sharpness.fromId(settings.optIntOrNull("14")),
                evCompensation = EvCompensation.fromId(settings.optIntOrNull("15")),
            ),
        )
    }

    private fun JSONObject.optIntOrNull(key: String): Int? =
        if (has(key) && !isNull(key)) optInt(key) else null

    private fun JSONObject.optLongOrNull(key: String): Long? =
        if (has(key) && !isNull(key)) optLong(key) else null
}
