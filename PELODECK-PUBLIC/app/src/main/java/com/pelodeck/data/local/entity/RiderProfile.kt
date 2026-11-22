package com.pelodeck.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rider_profiles")
data class RiderProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val weightKg: Double = 70.0,
    val profileImageUri: String? = null,
    val preferredSkin: String = "winamp",
    val preferredUnits: String = "IMPERIAL",
    val createdAt: Long = System.currentTimeMillis()
)
