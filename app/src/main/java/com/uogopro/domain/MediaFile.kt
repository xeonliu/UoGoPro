package com.uogopro.domain

enum class MediaType {
    Photo,
    Video,
}

data class MediaFile(
    val folder: String,
    val name: String,
    val modifiedAtEpochSeconds: Long?,
    val sizeBytes: Long?,
    val type: MediaType,
    val sourceUrl: String,
) {
    val path: String = "$folder/$name"
}
