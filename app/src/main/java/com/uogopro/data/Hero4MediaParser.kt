package com.uogopro.data

import com.uogopro.domain.MediaFile
import com.uogopro.domain.MediaType
import org.json.JSONObject

object Hero4MediaParser {
    fun parse(body: String, sourceUrl: (folder: String, name: String) -> String): List<MediaFile> {
        val root = JSONObject(body)
        val media = root.optJSONArray("media") ?: return emptyList()
        val files = mutableListOf<MediaFile>()

        for (mediaIndex in 0 until media.length()) {
            val directory = media.optJSONObject(mediaIndex) ?: continue
            val folder = directory.optString("d").takeIf { it.isNotBlank() } ?: continue
            val folderFiles = directory.optJSONArray("fs") ?: continue

            for (fileIndex in 0 until folderFiles.length()) {
                val file = folderFiles.optJSONObject(fileIndex) ?: continue
                val name = file.optString("n").takeIf { it.isNotBlank() } ?: continue
                val type = mediaTypeFor(name) ?: continue
                files += MediaFile(
                    folder = folder,
                    name = name,
                    modifiedAtEpochSeconds = file.optString("mod").toLongOrNull(),
                    sizeBytes = file.optString("s").toLongOrNull(),
                    type = type,
                    sourceUrl = sourceUrl(folder, name),
                )
            }
        }

        return files.sortedWith(
            compareByDescending<MediaFile> { it.modifiedAtEpochSeconds ?: Long.MIN_VALUE }
                .thenByDescending { it.name },
        )
    }

    private fun mediaTypeFor(name: String): MediaType? =
        when (name.substringAfterLast('.', "").uppercase()) {
            "JPG", "JPEG" -> MediaType.Photo
            "MP4" -> MediaType.Video
            else -> null
        }
}
