package com.uogopro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uogopro.data.GoProRepository
import com.uogopro.data.Hero4CommandCatalog
import com.uogopro.domain.CameraModel
import com.uogopro.domain.CameraState
import com.uogopro.domain.CaptureMode
import com.uogopro.domain.EvCompensation
import com.uogopro.domain.FieldOfView
import com.uogopro.domain.FrameRate
import com.uogopro.domain.Hero4Compatibility
import com.uogopro.domain.IsoLimit
import com.uogopro.domain.MediaFile
import com.uogopro.domain.Sharpness
import com.uogopro.domain.StreamBitRate
import com.uogopro.domain.StreamWindowSize
import com.uogopro.domain.VideoResolution
import com.uogopro.domain.WhiteBalance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class CameraUiState(
    val host: String = Hero4CommandCatalog.DEFAULT_HOST,
    val pairingPin: String = "",
    val model: CameraModel = CameraModel.Hero4Black,
    val cameraState: CameraState = CameraState(),
    val busy: Boolean = false,
    val locating: Boolean = false,
    val previewActive: Boolean = false,
    val previewUri: String = "",
    val streamBitRate: StreamBitRate = StreamBitRate.M1,
    val streamWindowSize: StreamWindowSize = StreamWindowSize.Default,
    val mediaFiles: List<MediaFile> = emptyList(),
    val mediaLoading: Boolean = false,
    val selectedMediaIndex: Int? = null,
    val error: String? = null,
    val message: String? = null,
)

class CameraViewModel(
    private val repository: GoProRepository = GoProRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState
    private var previewKeepAliveJob: Job? = null

    fun updateHost(host: String) {
        _uiState.update { it.copy(host = host, error = null, message = null) }
    }

    fun updatePairingPin(pin: String) {
        _uiState.update { it.copy(pairingPin = pin.filter(Char::isDigit).take(4), error = null, message = null) }
    }

    fun updateModel(model: CameraModel) {
        _uiState.update {
            it.copy(
                model = model,
                cameraState = it.cameraState.copy(
                    videoSettings = Hero4Compatibility.coerceSettings(model, it.cameraState.videoSettings),
                ),
                error = null,
                message = null,
            )
        }
    }

    fun refreshStatus() = runCameraAction(successMessage = "Status refreshed") {
        val state = repository.getStatus(currentHost())
        _uiState.update { it.copy(cameraState = state) }
    }

    fun pair() = runCameraAction(successMessage = "Pairing completed") {
        repository.pair(currentHost(), _uiState.value.pairingPin)
        val state = repository.getStatus(currentHost())
        _uiState.update { it.copy(cameraState = state) }
    }

    fun setMode(mode: CaptureMode) = runCameraAction(successMessage = "Mode set to ${mode.label}") {
        repository.setMode(currentHost(), mode)
        refreshStateAfterCommand()
    }

    fun shutter() {
        val shouldStartRecording = !_uiState.value.cameraState.isRecording
        runCameraAction(successMessage = if (shouldStartRecording) "Shutter started" else "Shutter stopped") {
            repository.shutter(currentHost(), shouldStartRecording)
            refreshStateAfterCommand()
        }
    }

    fun tagMoment() = runCameraAction(successMessage = "Moment tagged") {
        repository.tagMoment(currentHost())
    }

    fun toggleLocate() {
        val enabled = !_uiState.value.locating
        runCameraAction(successMessage = if (enabled) "Locate enabled" else "Locate disabled") {
            repository.locate(currentHost(), enabled)
            _uiState.update { it.copy(locating = enabled) }
        }
    }

    fun powerOff() = runCameraAction(successMessage = "Power off command sent") {
        if (_uiState.value.previewActive) {
            stopPreviewKeepAlive()
            repository.stopPreview(currentHost())
        }
        repository.powerOff(currentHost())
        _uiState.update {
            it.copy(
                cameraState = CameraState(),
                previewActive = false,
                previewUri = "",
            )
        }
    }

    fun startPreview() = runCameraAction(successMessage = "Preview started") {
        repository.setStreamBitRate(currentHost(), _uiState.value.streamBitRate)
        repository.setStreamWindowSize(currentHost(), _uiState.value.streamWindowSize)
        _uiState.update {
            it.copy(
                previewActive = true,
                previewUri = repository.previewUri(),
            )
        }
        delay(600)
        repository.startPreview(currentHost())
        repository.sendPreviewKeepAlive(currentHost())
        startPreviewKeepAlive()
        refreshStateAfterCommand()
    }

    fun stopPreview() = runCameraAction(successMessage = "Preview stopped") {
        stopPreviewKeepAlive()
        _uiState.update { it.copy(previewActive = false, previewUri = "") }
        delay(250)
        repository.stopPreview(currentHost())
        refreshStateAfterCommand()
    }

    fun setStreamBitRate(value: StreamBitRate) = runCameraAction(successMessage = "Preview bitrate set to ${value.label}") {
        _uiState.update { it.copy(streamBitRate = value) }
        repository.setStreamBitRate(currentHost(), value)
        restartPreviewIfActive()
    }

    fun setStreamWindowSize(value: StreamWindowSize) = runCameraAction(successMessage = "Preview window set to ${value.label}") {
        _uiState.update { it.copy(streamWindowSize = value) }
        repository.setStreamWindowSize(currentHost(), value)
        restartPreviewIfActive()
    }

    fun loadMedia() {
        viewModelScope.launch {
            _uiState.update { it.copy(mediaLoading = true, error = null, message = null) }
            runCatching { repository.getMediaFiles(currentHost()) }
                .onSuccess { files ->
                    _uiState.update {
                        it.copy(
                            mediaFiles = files,
                            mediaLoading = false,
                            selectedMediaIndex = it.selectedMediaIndex?.takeIf { index -> index in files.indices },
                            message = "Loaded ${files.size} media files",
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            mediaLoading = false,
                            error = throwable.message ?: throwable.javaClass.simpleName,
                        )
                    }
                }
        }
    }

    fun openMedia(index: Int) {
        _uiState.update {
            it.copy(selectedMediaIndex = index.takeIf { selected -> selected in it.mediaFiles.indices })
        }
    }

    fun closeMedia() {
        _uiState.update { it.copy(selectedMediaIndex = null) }
    }

    fun setResolution(resolution: VideoResolution) = runCameraAction(successMessage = "Resolution set to ${resolution.label}") {
        val coerced = Hero4Compatibility.coerceSettings(
            _uiState.value.model,
            _uiState.value.cameraState.videoSettings.copy(resolution = resolution),
        )
        repository.setVideoResolution(currentHost(), coerced.resolution)
        repository.setFrameRate(currentHost(), coerced.frameRate)
        repository.setFov(currentHost(), coerced.fov)
        refreshStateAfterCommand()
    }

    fun setFrameRate(frameRate: FrameRate) = runCameraAction(successMessage = "Frame rate set to ${frameRate.label}") {
        repository.setFrameRate(currentHost(), frameRate)
        refreshStateAfterCommand()
    }

    fun setFov(fov: FieldOfView) = runCameraAction(successMessage = "FOV set to ${fov.label}") {
        repository.setFov(currentHost(), fov)
        refreshStateAfterCommand()
    }

    fun setLowLight(enabled: Boolean) = runCameraAction(successMessage = "Low light ${enabled.onOff()}") {
        repository.setLowLight(currentHost(), enabled)
        refreshStateAfterCommand()
    }

    fun setSpotMeter(enabled: Boolean) = runCameraAction(successMessage = "Spot meter ${enabled.onOff()}") {
        repository.setSpotMeter(currentHost(), enabled)
        refreshStateAfterCommand()
    }

    fun setProtune(enabled: Boolean) = runCameraAction(successMessage = "Protune ${enabled.onOff()}") {
        repository.setProtune(currentHost(), enabled)
        refreshStateAfterCommand()
    }

    fun setWhiteBalance(value: WhiteBalance) = runCameraAction(successMessage = "White balance set to ${value.label}") {
        repository.setWhiteBalance(currentHost(), value)
        refreshStateAfterCommand()
    }

    fun setIsoLimit(value: IsoLimit) = runCameraAction(successMessage = "ISO limit set to ${value.label}") {
        repository.setIsoLimit(currentHost(), value)
        refreshStateAfterCommand()
    }

    fun setSharpness(value: Sharpness) = runCameraAction(successMessage = "Sharpness set to ${value.label}") {
        repository.setSharpness(currentHost(), value)
        refreshStateAfterCommand()
    }

    fun setEvCompensation(value: EvCompensation) = runCameraAction(successMessage = "EV set to ${value.label}") {
        repository.setEvCompensation(currentHost(), value)
        refreshStateAfterCommand()
    }

    private fun runCameraAction(successMessage: String, action: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(busy = true, error = null, message = null) }
            runCatching { action() }
                .onSuccess {
                    _uiState.update { it.copy(busy = false, message = successMessage) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            busy = false,
                            error = throwable.message ?: throwable.javaClass.simpleName,
                        )
                    }
                }
        }
    }

    private suspend fun refreshStateAfterCommand() {
        val state = repository.getStatus(currentHost())
        _uiState.update { it.copy(cameraState = state) }
    }

    private suspend fun restartPreviewIfActive() {
        if (_uiState.value.previewActive) {
            repository.restartPreview(currentHost())
            repository.sendPreviewKeepAlive(currentHost())
        }
    }

    private fun currentHost(): String = _uiState.value.host.ifBlank { Hero4CommandCatalog.DEFAULT_HOST }

    private fun Boolean.onOff(): String = if (this) "on" else "off"

    private fun startPreviewKeepAlive() {
        previewKeepAliveJob?.cancel()
        previewKeepAliveJob = viewModelScope.launch {
            while (isActive) {
                runCatching { repository.sendPreviewKeepAlive(currentHost()) }
                delay(2_500)
            }
        }
    }

    private fun stopPreviewKeepAlive() {
        previewKeepAliveJob?.cancel()
        previewKeepAliveJob = null
    }

    override fun onCleared() {
        stopPreviewKeepAlive()
        super.onCleared()
    }
}
