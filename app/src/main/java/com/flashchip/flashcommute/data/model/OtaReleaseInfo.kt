// Version 1.00 - FlashCommute
// Developed for FlashChip by WoRFReD
package com.flashchip.flashcommute.data.model

data class OtaReleaseInfo(
    val tagName: String,
    val title: String,
    val releaseNotes: String,
    val publishedAt: String,
    val downloadUrl: String,
    val apkFileName: String,
    val apkSizeBytes: Long,
    val isNewer: Boolean
)
