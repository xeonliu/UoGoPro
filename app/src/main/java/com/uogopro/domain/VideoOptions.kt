package com.uogopro.domain

data class VideoResolution(
    val id: Int,
    val label: String,
    val dimensions: String,
) {
    companion object {
        val FourK = VideoResolution(1, "4K", "3840x2160")
        val FourKSuperView = VideoResolution(2, "4K SuperView", "3840x2160")
        val TwoPoint7K = VideoResolution(4, "2.7K", "2704x1520")
        val TwoPoint7KSuperView = VideoResolution(5, "2.7K SuperView", "2704x1520")
        val TwoPoint7KFourThree = VideoResolution(6, "2.7K 4:3", "2704x2028")
        val P1440 = VideoResolution(7, "1440p", "1920x1440")
        val P1080SuperView = VideoResolution(8, "1080p SuperView", "1920x1080")
        val P1080 = VideoResolution(9, "1080p", "1920x1080")
        val P960 = VideoResolution(10, "960p", "1280x960")
        val P720SuperView = VideoResolution(11, "720p SuperView", "1280x720")
        val P720 = VideoResolution(12, "720p", "1280x720")
        val Wvga = VideoResolution(13, "WVGA", "848x480")

        val all = listOf(
            FourK,
            FourKSuperView,
            TwoPoint7K,
            TwoPoint7KSuperView,
            TwoPoint7KFourThree,
            P1440,
            P1080SuperView,
            P1080,
            P960,
            P720SuperView,
            P720,
            Wvga,
        )

        fun fromId(id: Int?): VideoResolution = all.firstOrNull { it.id == id } ?: P1080
    }
}

data class FrameRate(
    val id: Int,
    val label: String,
    val value: Double,
) {
    companion object {
        val F240 = FrameRate(0, "240 fps", 240.0)
        val F120 = FrameRate(1, "120 fps", 120.0)
        val F100 = FrameRate(2, "100 fps", 100.0)
        val F90 = FrameRate(3, "90 fps", 90.0)
        val F80 = FrameRate(4, "80 fps", 80.0)
        val F60 = FrameRate(5, "60 fps", 60.0)
        val F50 = FrameRate(6, "50 fps", 50.0)
        val F48 = FrameRate(7, "48 fps", 48.0)
        val F30 = FrameRate(8, "30 fps", 30.0)
        val F25 = FrameRate(9, "25 fps", 25.0)
        val F24 = FrameRate(10, "24 fps", 24.0)
        val F15 = FrameRate(11, "15 fps", 15.0)
        val F12_5 = FrameRate(12, "12.5 fps", 12.5)

        val all = listOf(F240, F120, F100, F90, F80, F60, F50, F48, F30, F25, F24, F15, F12_5)

        fun fromId(id: Int?): FrameRate = all.firstOrNull { it.id == id } ?: F30
    }
}

enum class FieldOfView(val id: Int, val label: String) {
    Wide(0, "Wide"),
    Medium(1, "Medium"),
    Narrow(2, "Narrow"),
    Linear(4, "Linear");

    companion object {
        fun fromId(id: Int?): FieldOfView = entries.firstOrNull { it.id == id } ?: Wide
    }
}

enum class WhiteBalance(val id: Int, val label: String) {
    Auto(0, "Auto"),
    K3000(1, "3000K"),
    K5500(2, "5500K"),
    K6500(3, "6500K"),
    Native(4, "Native"),
    K4000(5, "4000K"),
    K4800(6, "4800K"),
    K6000(7, "6000K");

    companion object {
        fun fromId(id: Int?): WhiteBalance = entries.firstOrNull { it.id == id } ?: Auto
    }
}

enum class IsoLimit(val id: Int, val label: String) {
    Iso6400(0, "6400"),
    Iso1600(1, "1600"),
    Iso400(2, "400"),
    Iso3200(3, "3200"),
    Iso800(4, "800"),
    Iso200(7, "200"),
    Iso100(8, "100");

    companion object {
        fun fromId(id: Int?): IsoLimit = entries.firstOrNull { it.id == id } ?: Iso1600
    }
}

enum class Sharpness(val id: Int, val label: String) {
    High(0, "High"),
    Medium(1, "Medium"),
    Low(2, "Low");

    companion object {
        fun fromId(id: Int?): Sharpness = entries.firstOrNull { it.id == id } ?: High
    }
}

enum class EvCompensation(val id: Int, val label: String) {
    Plus2(0, "+2.0"),
    Plus1_5(1, "+1.5"),
    Plus1(2, "+1.0"),
    Plus0_5(3, "+0.5"),
    Zero(4, "0.0"),
    Minus0_5(5, "-0.5"),
    Minus1(6, "-1.0"),
    Minus1_5(7, "-1.5"),
    Minus2(8, "-2.0");

    companion object {
        fun fromId(id: Int?): EvCompensation = entries.firstOrNull { it.id == id } ?: Zero
    }
}

data class StreamBitRate(
    val id: Int,
    val label: String,
) {
    companion object {
        val K250 = StreamBitRate(250_000, "250 Kbps")
        val K400 = StreamBitRate(400_000, "400 Kbps")
        val K600 = StreamBitRate(600_000, "600 Kbps")
        val K700 = StreamBitRate(700_000, "700 Kbps")
        val K800 = StreamBitRate(800_000, "800 Kbps")
        val M1 = StreamBitRate(1_000_000, "1 Mbps")
        val M1_2 = StreamBitRate(1_200_000, "1.2 Mbps")
        val M1_6 = StreamBitRate(1_600_000, "1.6 Mbps")
        val M2 = StreamBitRate(2_000_000, "2 Mbps")
        val M2_4 = StreamBitRate(2_400_000, "2.4 Mbps")

        val all = listOf(K250, K400, K600, K700, K800, M1, M1_2, M1_6, M2, M2_4)
    }
}

enum class StreamWindowSize(val id: Int, val label: String) {
    Default(0, "Default"),
    P240(1, "240"),
    P240ThreeFour(2, "240 3:4"),
    P240Half(3, "240 1:2"),
    P480(4, "480"),
    P480ThreeFour(5, "480 3:4"),
    P480Half(6, "480 1:2");
}
