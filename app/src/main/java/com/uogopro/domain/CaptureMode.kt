package com.uogopro.domain

enum class CaptureMode(val code: Int, val label: String) {
    Video(0, "Video"),
    Photo(1, "Photo"),
    MultiShot(2, "MultiShot");

    companion object {
        fun fromCode(code: Int?): CaptureMode = entries.firstOrNull { it.code == code } ?: Video
    }
}

enum class CameraModel(val label: String) {
    Hero4Black("HERO4 Black"),
    Hero4Silver("HERO4 Silver"),
}

enum class BatteryLevel(val label: String) {
    Unknown("Unknown"),
    Empty("Empty"),
    Low("Low"),
    Half("Half"),
    Full("Full"),
    Charging("Charging");

    companion object {
        fun fromCode(code: Int?): BatteryLevel = when (code) {
            0 -> Empty
            1 -> Low
            2 -> Half
            3 -> Full
            4 -> Charging
            else -> Unknown
        }
    }
}

enum class SdCardState(val label: String) {
    Inserted("Inserted"),
    Missing("Missing"),
    Unknown("Unknown");

    companion object {
        fun fromCode(code: Int?): SdCardState = when (code) {
            0 -> Inserted
            2 -> Missing
            else -> Unknown
        }
    }
}
