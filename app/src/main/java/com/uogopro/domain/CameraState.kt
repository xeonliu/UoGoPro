package com.uogopro.domain

data class CameraState(
    val connected: Boolean = false,
    val batteryAvailable: Boolean = false,
    val batteryLevel: BatteryLevel = BatteryLevel.Unknown,
    val isRecording: Boolean = false,
    val recordingDurationSeconds: Int = 0,
    val mode: CaptureMode = CaptureMode.Video,
    val subMode: Int = 0,
    val clientsConnected: Int = 0,
    val streaming: Boolean = false,
    val sdCardState: SdCardState = SdCardState.Unknown,
    val remainingPhotos: Int? = null,
    val remainingVideoSeconds: Int? = null,
    val freeSpaceBytes: Long? = null,
    val videoSettings: VideoSettings = VideoSettings(),
)

data class VideoSettings(
    val resolution: VideoResolution = VideoResolution.P1080,
    val frameRate: FrameRate = FrameRate.F30,
    val fov: FieldOfView = FieldOfView.Wide,
    val lowLight: Boolean = false,
    val spotMeter: Boolean = false,
    val protune: Boolean = false,
    val whiteBalance: WhiteBalance = WhiteBalance.Auto,
    val isoLimit: IsoLimit = IsoLimit.Iso1600,
    val sharpness: Sharpness = Sharpness.High,
    val evCompensation: EvCompensation = EvCompensation.Zero,
)
