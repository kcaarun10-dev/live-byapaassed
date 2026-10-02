package com.example.data.remote

data class AdminConfig(
    val apiBaseUrl: String? = null,
    val latestVersionCode: Int = 1,
    val latestVersionName: String = "a.r.u.n.1",
    val downloadUrl: String = "",
    val isForceUpdate: Boolean = false,
    val updateMessage: String = "A new version of AR SPORTS is available. Please update to enjoy the latest features and uninterrupted streaming.",
    val announcement: AdminAnnouncement? = null
)

data class AdminAnnouncement(
    val id: String = "announcement_default",
    val title: String = "",
    val message: String = "",
    val actionUrl: String? = null,
    val enabled: Boolean = false
)
