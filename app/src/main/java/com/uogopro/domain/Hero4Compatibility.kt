package com.uogopro.domain

data class VideoCompatibility(
    val resolution: VideoResolution,
    val frameRates: List<FrameRate>,
    val fovs: List<FieldOfView>,
)

object Hero4Compatibility {
    fun optionsFor(model: CameraModel): List<VideoCompatibility> = when (model) {
        CameraModel.Hero4Black -> blackOptions
        CameraModel.Hero4Silver -> silverOptions
    }

    fun supportedResolutions(model: CameraModel): List<VideoResolution> =
        optionsFor(model).map { it.resolution }

    fun frameRatesFor(model: CameraModel, resolution: VideoResolution): List<FrameRate> =
        optionsFor(model).firstOrNull { it.resolution.id == resolution.id }?.frameRates.orEmpty()

    fun fovsFor(model: CameraModel, resolution: VideoResolution): List<FieldOfView> =
        optionsFor(model).firstOrNull { it.resolution.id == resolution.id }?.fovs.orEmpty()

    fun coerceSettings(model: CameraModel, settings: VideoSettings): VideoSettings {
        val resolution = supportedResolutions(model).firstOrNull { it.id == settings.resolution.id }
            ?: supportedResolutions(model).first()
        val frameRate = frameRatesFor(model, resolution).firstOrNull { it.id == settings.frameRate.id }
            ?: frameRatesFor(model, resolution).first()
        val fov = fovsFor(model, resolution).firstOrNull { it.id == settings.fov.id }
            ?: fovsFor(model, resolution).first()
        return settings.copy(resolution = resolution, frameRate = frameRate, fov = fov)
    }

    private val wideOnly = listOf(FieldOfView.Wide)
    private val wideMedium = listOf(FieldOfView.Wide, FieldOfView.Medium)
    private val wideMediumNarrow = listOf(FieldOfView.Wide, FieldOfView.Medium, FieldOfView.Narrow)

    private val blackOptions = listOf(
        VideoCompatibility(VideoResolution.FourK, listOf(FrameRate.F30, FrameRate.F25, FrameRate.F24), wideOnly),
        VideoCompatibility(VideoResolution.FourKSuperView, listOf(FrameRate.F24), wideOnly),
        VideoCompatibility(
            VideoResolution.TwoPoint7K,
            listOf(FrameRate.F60, FrameRate.F50, FrameRate.F48, FrameRate.F30, FrameRate.F25, FrameRate.F24),
            wideMedium,
        ),
        VideoCompatibility(VideoResolution.TwoPoint7KSuperView, listOf(FrameRate.F30, FrameRate.F25), wideOnly),
        VideoCompatibility(VideoResolution.TwoPoint7KFourThree, listOf(FrameRate.F30, FrameRate.F25), wideOnly),
        VideoCompatibility(
            VideoResolution.P1440,
            listOf(FrameRate.F80, FrameRate.F60, FrameRate.F50, FrameRate.F48, FrameRate.F30, FrameRate.F25, FrameRate.F24),
            wideOnly,
        ),
        VideoCompatibility(
            VideoResolution.P1080,
            listOf(FrameRate.F120, FrameRate.F90, FrameRate.F60, FrameRate.F50, FrameRate.F48, FrameRate.F30, FrameRate.F25, FrameRate.F24),
            wideMediumNarrow,
        ),
        VideoCompatibility(
            VideoResolution.P1080SuperView,
            listOf(FrameRate.F80, FrameRate.F60, FrameRate.F50, FrameRate.F48, FrameRate.F30, FrameRate.F25, FrameRate.F24),
            wideOnly,
        ),
        VideoCompatibility(VideoResolution.P960, listOf(FrameRate.F120, FrameRate.F60, FrameRate.F50), wideOnly),
        VideoCompatibility(
            VideoResolution.P720,
            listOf(FrameRate.F240, FrameRate.F120, FrameRate.F60, FrameRate.F50, FrameRate.F30, FrameRate.F25),
            wideMediumNarrow,
        ),
        VideoCompatibility(VideoResolution.P720SuperView, listOf(FrameRate.F120, FrameRate.F60, FrameRate.F50), wideOnly),
        VideoCompatibility(VideoResolution.Wvga, listOf(FrameRate.F240), wideOnly),
    )

    private val silverOptions = listOf(
        VideoCompatibility(VideoResolution.FourK, listOf(FrameRate.F15, FrameRate.F12_5), wideOnly),
        VideoCompatibility(VideoResolution.TwoPoint7K, listOf(FrameRate.F30, FrameRate.F25, FrameRate.F24), wideMedium),
        VideoCompatibility(VideoResolution.P1440, listOf(FrameRate.F48, FrameRate.F30, FrameRate.F25, FrameRate.F24), wideOnly),
        VideoCompatibility(
            VideoResolution.P1080,
            listOf(FrameRate.F60, FrameRate.F50, FrameRate.F48, FrameRate.F30, FrameRate.F25, FrameRate.F24),
            wideMediumNarrow,
        ),
        VideoCompatibility(
            VideoResolution.P1080SuperView,
            listOf(FrameRate.F60, FrameRate.F50, FrameRate.F48, FrameRate.F30, FrameRate.F25, FrameRate.F24),
            wideOnly,
        ),
        VideoCompatibility(VideoResolution.P960, listOf(FrameRate.F100, FrameRate.F60, FrameRate.F50), wideOnly),
        VideoCompatibility(
            VideoResolution.P720,
            listOf(FrameRate.F120, FrameRate.F60, FrameRate.F50, FrameRate.F30, FrameRate.F25),
            wideMediumNarrow,
        ),
        VideoCompatibility(VideoResolution.P720SuperView, listOf(FrameRate.F100, FrameRate.F60, FrameRate.F50), wideOnly),
        VideoCompatibility(VideoResolution.Wvga, listOf(FrameRate.F240), wideOnly),
    )
}
