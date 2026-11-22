package com.pelodeck.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pelodeck.data.local.entity.RideSession
import kotlinx.coroutines.flow.Flow

@Dao
interface RideSessionDao {

    @Query("SELECT * FROM ride_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<RideSession>>

    @Query("SELECT * FROM ride_sessions WHERE riderProfileId = :profileId ORDER BY startTime DESC")
    fun getSessionsForProfile(profileId: Long): Flow<List<RideSession>>

    @Query("SELECT * FROM ride_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Long): RideSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: RideSession): Long

    @Update
    suspend fun update(session: RideSession)

    @Query("DELETE FROM ride_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
