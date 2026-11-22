package com.pelodeck.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pelodeck.data.local.entity.RiderProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface RiderProfileDao {

    @Query("SELECT * FROM rider_profiles ORDER BY createdAt DESC")
    fun getAllProfiles(): Flow<List<RiderProfile>>

    @Query("SELECT * FROM rider_profiles")
    suspend fun getAllProfilesList(): List<RiderProfile>

    @Query("SELECT * FROM rider_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): RiderProfile?

    @Query("SELECT * FROM rider_profiles WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getProfileByName(name: String): RiderProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: RiderProfile): Long

    @Update
    suspend fun update(profile: RiderProfile)

    @Delete
    suspend fun delete(profile: RiderProfile)

    @Query("SELECT COUNT(*) FROM rider_profiles")
    suspend fun getCount(): Int
}
