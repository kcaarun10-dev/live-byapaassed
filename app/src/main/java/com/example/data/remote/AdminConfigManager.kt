package com.example.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import com.example.data.api.ApiClient
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.Request

class AdminConfigManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ar_sports_admin_config", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _configFlow = MutableStateFlow<AdminConfig?>(null)
    val configFlow: StateFlow<AdminConfig?> = _configFlow.asStateFlow()

    private val _announcementFlow = MutableStateFlow<AdminAnnouncement?>(null)
    val announcementFlow: StateFlow<AdminAnnouncement?> = _announcementFlow.asStateFlow()

    private val _updateAvailableFlow = MutableStateFlow(false)
    val updateAvailableFlow: StateFlow<Boolean> = _updateAvailableFlow.asStateFlow()

    companion object {
        // Default Firebase Realtime Database / Remote Config JSON endpoint
        // You can point this to your Firebase Realtime DB URL (e.g. https://your-app-default-rtdb.firebaseio.com/app_config.json)
        // or any GitHub raw / hosting JSON URL.
        const val DEFAULT_FIREBASE_CONFIG_URL = "https://raw.githubusercontent.com/streamed-sports/config/main/app_config.json"
    }

    init {
        loadCachedConfig()
    }

    private fun loadCachedConfig() {
        val cachedJson = prefs.getString("cached_admin_config", null)
        if (!cachedJson.isNullOrBlank()) {
            try {
                val config = gson.fromJson(cachedJson, AdminConfig::class.java)
                _configFlow.value = config
                applyConfig(config)
            } catch (_: Exception) {}
        }
    }

    /**
     * Gets the current installed APK version code.
     */
    fun getInstalledVersionCode(): Int {
        return try {
            val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode
            }
        } catch (_: Exception) {
            1
        }
    }

    /**
     * Gets the current installed APK version name.
     */
    fun getInstalledVersionName(): String {
        return try {
            val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            pInfo.versionName ?: "1.0.0"
        } catch (_: Exception) {
            "1.0.0"
        }
    }

    /**
     * Fetches latest configuration from Firebase / Remote Config URL.
     */
    suspend fun fetchRemoteConfig(customUrl: String? = null): Result<AdminConfig> = withContext(Dispatchers.IO) {
        val targetUrl = customUrl ?: prefs.getString("custom_firebase_url", null) ?: DEFAULT_FIREBASE_CONFIG_URL

        try {
            val request = Request.Builder()
                .url(targetUrl)
                .header("Accept", "application/json")
                .header("User-Agent", "ARSports-AndroidApp")
                .build()

            val response = ApiClient.okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string()
                if (!bodyStr.isNullOrBlank()) {
                    val config = gson.fromJson(bodyStr, AdminConfig::class.java)
                    _configFlow.value = config
                    prefs.edit().putString("cached_admin_config", bodyStr).apply()
                    applyConfig(config)
                    return@withContext Result.success(config)
                }
            }
            Result.failure(Exception("Failed to fetch remote config: HTTP ${response.code}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun applyConfig(config: AdminConfig) {
        // 1. Check version update
        val currentCode = getInstalledVersionCode()
        val updateNeeded = config.latestVersionCode > currentCode && config.downloadUrl.isNotBlank()
        _updateAvailableFlow.value = updateNeeded

        // 2. Check announcement
        val ann = config.announcement
        if (ann != null && ann.enabled && ann.message.isNotBlank()) {
            val dismissedId = prefs.getString("dismissed_announcement_id", null)
            if (dismissedId != ann.id) {
                _announcementFlow.value = ann
            } else {
                _announcementFlow.value = null
            }
        } else {
            _announcementFlow.value = null
        }
    }

    fun dismissAnnouncement(announcementId: String) {
        prefs.edit().putString("dismissed_announcement_id", announcementId).apply()
        _announcementFlow.value = null
    }

    fun setCustomFirebaseUrl(url: String) {
        prefs.edit().putString("custom_firebase_url", url).apply()
    }
}
