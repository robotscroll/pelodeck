package com.pelodeck.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pelodeck.data.local.dao.BookmarkDao
import com.pelodeck.data.local.dao.RideSessionDao
import com.pelodeck.data.local.dao.RiderProfileDao
import com.pelodeck.data.local.entity.Bookmark
import com.pelodeck.data.local.entity.RideSession
import com.pelodeck.data.local.entity.RiderProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        RiderProfile::class,
        RideSession::class,
        Bookmark::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PeloDeckDatabase : RoomDatabase() {

    abstract fun riderProfileDao(): RiderProfileDao
    abstract fun rideSessionDao(): RideSessionDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @Volatile
        private var instance: PeloDeckDatabase? = null

        fun getInstance(context: Context): PeloDeckDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PeloDeckDatabase::class.java,
                    "pelodeck_database.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                instance?.let { database ->
                    // Seed default rider profile
                    database.riderProfileDao().insert(
                        RiderProfile(
                            name = "Rider 1",
                            weightKg = 75.0,
                            profileImageUri = "asset:///avatars/avatar_1.png",
                            preferredSkin = "winamp",
                            preferredUnits = "IMPERIAL"
                        )
                    )

                    // Seed Cinnamoroll profile
                    database.riderProfileDao().insert(
                        RiderProfile(
                            name = "Rider 2",
                            weightKg = 45.0,
                            profileImageUri = "asset:///avatars/avatar_cinnamoroll.png",
                            preferredSkin = "cinnamoroll",
                            preferredUnits = "IMPERIAL"
                        )
                    )

                    database.riderProfileDao().insert(
                        RiderProfile(
                            name = "Rider 3",
                            weightKg = 75.0,
                            profileImageUri = "asset:///avatars/avatar_1.png",
                            preferredSkin = "winamp",
                            preferredUnits = "IMPERIAL"
                        )
                    )

                    // Seed default browser bookmarks
                    database.bookmarkDao().insertAll(
                        listOf(
                            Bookmark(title = "YouTube", url = "https://www.youtube.com", orderIndex = 0, isDefault = true),
                            Bookmark(title = "Netflix", url = "https://www.netflix.com", orderIndex = 1, isDefault = true),
                            Bookmark(title = "Disney+", url = "https://www.disneyplus.com", orderIndex = 2, isDefault = true),
                            Bookmark(title = "Hulu", url = "https://www.hulu.com", orderIndex = 3, isDefault = true),
                            Bookmark(title = "Twitch", url = "https://www.twitch.tv", orderIndex = 4, isDefault = true)
                        )
                    )
                }
            }
        }
    }
}
