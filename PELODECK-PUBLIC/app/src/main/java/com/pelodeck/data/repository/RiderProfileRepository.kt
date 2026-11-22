package com.pelodeck.data.repository

import com.pelodeck.data.local.dao.RiderProfileDao
import com.pelodeck.data.local.entity.RiderProfile
import kotlinx.coroutines.flow.Flow

class RiderProfileRepository(
    private val riderProfileDao: RiderProfileDao
) {
    val allProfiles: Flow<List<RiderProfile>> = riderProfileDao.getAllProfiles()

    suspend fun getProfileById(id: Long): RiderProfile? = riderProfileDao.getProfileById(id)

    suspend fun getProfileByName(name: String): RiderProfile? = riderProfileDao.getProfileByName(name)

    suspend fun createProfile(profile: RiderProfile): Long = riderProfileDao.insert(profile)

    suspend fun updateProfile(profile: RiderProfile) = riderProfileDao.update(profile)

    suspend fun deleteProfile(profile: RiderProfile) = riderProfileDao.delete(profile)

    suspend fun getCount(): Int = riderProfileDao.getCount()
}
