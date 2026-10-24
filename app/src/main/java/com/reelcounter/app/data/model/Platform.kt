package com.reelcounter.app.data.model

enum class Platform(
    val displayName: String,
    val iconEmoji: String,
    val brandHexColor: Long,
    val packageNames: List<String>
) {
    INSTAGRAM(
        displayName = "Instagram",
        iconEmoji = "\uD83D\uDCF8",
        brandHexColor = 0xFFE1306C,
        packageNames = listOf("com.instagram.android")
    ),
    TIKTOK(
        displayName = "TikTok",
        iconEmoji = "\uD83C\uDFB5",
        brandHexColor = 0xFF00F2FE,
        packageNames = listOf("com.zhiliaoapp.musically", "com.ss.android.ugc.trill")
    ),
    YOUTUBE_SHORTS(
        displayName = "YouTube Shorts",
        iconEmoji = "\u25B6\uFE0F",
        brandHexColor = 0xFFFF0000,
        packageNames = listOf("com.google.android.youtube")
    ),
    FACEBOOK(
        displayName = "Facebook",
        iconEmoji = "\uD83D\uDCD8",
        brandHexColor = 0xFF1877F2,
        packageNames = listOf("com.facebook.katana", "com.facebook.lite")
    ),
    SNAPCHAT(
        displayName = "Snapchat Spotlight",
        iconEmoji = "\uD83D\uDC7B",
        brandHexColor = 0xFFFFFC00,
        packageNames = listOf("com.snapchat.android")
    ),
    REDDIT(
        displayName = "Reddit",
        iconEmoji = "\uD83E\uDD16",
        brandHexColor = 0xFFFF4500,
        packageNames = listOf("com.reddit.frontpage")
    ),
    OTHER(
        displayName = "Shorts",
        iconEmoji = "\uD83C\uDFAC",
        brandHexColor = 0xFF9C27B0,
        packageNames = emptyList()
    );

    companion object {
        fun fromPackageName(pkg: String): Platform {
            return entries.firstOrNull { it.packageNames.contains(pkg) } ?: OTHER
        }
    }
}
