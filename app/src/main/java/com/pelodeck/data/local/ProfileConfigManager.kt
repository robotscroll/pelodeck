package com.pelodeck.data.local

import android.content.Context
import android.os.Environment
import android.util.Log
import com.pelodeck.data.local.entity.RiderProfile
import com.pelodeck.data.local.preferences.UserPreferencesRepository
import com.pelodeck.data.repository.RiderProfileRepository
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONArray
import java.io.File
import java.io.InputStream

object ProfileConfigManager {

    private const val TAG = "ProfileConfigManager"
    const val PROFILES_JSON_FILENAME = "profiles.json"

    /**
     * Resolves the profiles.json file content.
     * Checks:
     * 1. External files directory: /sdcard/Android/data/com.pelodeck.debug/files/profiles.json
     * 2. Shared storage: /sdcard/PeloDeck/profiles.json
     * 3. Bundled asset: assets/profiles.json
     */
    fun loadProfilesJsonString(context: Context): String {
        // 1. App external files dir (accessible without special root permissions via ADB)
        val externalFile = File(context.getExternalFilesDir(null), PROFILES_JSON_FILENAME)
        if (externalFile.exists() && externalFile.canRead()) {
            try {
                val content = externalFile.readText()
                if (content.isNotBlank()) {
                    Log.i(TAG, "Loaded profiles from external app file: ${externalFile.absolutePath}")
                    return content
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed reading external profiles file", e)
            }
        }

        // 2. Common storage directory (/sdcard/PeloDeck/profiles.json)
        val commonStorageFile = File(Environment.getExternalStorageDirectory(), "PeloDeck/$PROFILES_JSON_FILENAME")
        if (commonStorageFile.exists() && commonStorageFile.canRead()) {
            try {
                val content = commonStorageFile.readText()
                if (content.isNotBlank()) {
                    Log.i(TAG, "Loaded profiles from common storage: ${commonStorageFile.absolutePath}")
                    return content
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed reading common storage profiles file", e)
            }
        }

        // 3. Fallback to bundled asset & write starter copy to external files dir
        return try {
            val stream: InputStream = context.assets.open(PROFILES_JSON_FILENAME)
            val content = stream.bufferedReader().use { it.readText() }
            try {
                externalFile.writeText(content)
                Log.i(TAG, "Exported starter copy of profiles.json to ${externalFile.absolutePath}")
            } catch (e: Exception) {
                Log.d(TAG, "Could not write starter copy: ${e.message}")
            }
            content
        } catch (e: Exception) {
            Log.e(TAG, "Failed loading profiles from assets", e)
            "[]"
        }
    }

    /**
     * Parses the JSON string into a list of RiderProfile models.
     */
    fun parseProfiles(jsonString: String): List<RiderProfile> {
        val list = mutableListOf<RiderProfile>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val name = obj.optString("name", "Rider")
                val weightKg = obj.optDouble("weightKg", 75.0)
                val profileImageUri = obj.optString("profileImageUri").takeIf { it.isNotBlank() }
                val preferredSkin = obj.optString("preferredSkin", "winamp")
                val preferredUnits = obj.optString("preferredUnits", "IMPERIAL")

                list.add(
                    RiderProfile(
                        name = name,
                        weightKg = weightKg,
                        profileImageUri = profileImageUri,
                        preferredSkin = preferredSkin,
                        preferredUnits = preferredUnits
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing profiles JSON", e)
        }
        return list
    }

    /**
     * Synchronizes profiles from JSON into the Room database.
     * Updates existing profiles with matching names (allowing instant updates of photo URLs, weight, skins),
     * or inserts new profiles.
     */
    suspend fun syncProfiles(
        context: Context,
        profileRepo: RiderProfileRepository,
        prefRepo: UserPreferencesRepository? = null
    ) {
        val json = loadProfilesJsonString(context)
        val parsedProfiles = parseProfiles(json)

        for (profile in parsedProfiles) {
            val existing = profileRepo.getProfileByName(profile.name)
            if (existing != null) {
                // Update properties while preserving database primary key ID and createdAt timestamp
                val updated = existing.copy(
                    weightKg = profile.weightKg,
                    profileImageUri = profile.profileImageUri,
                    preferredSkin = profile.preferredSkin,
                    preferredUnits = profile.preferredUnits
                )
                profileRepo.updateProfile(updated)
            } else {
                val newId = profileRepo.createProfile(profile)
                if (prefRepo != null && prefRepo.activeProfileId.firstOrNull() == null) {
                    prefRepo.setActiveProfileId(newId)
                }
            }
        }
    }
}
