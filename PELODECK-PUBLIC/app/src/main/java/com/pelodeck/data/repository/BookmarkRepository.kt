package com.pelodeck.data.repository

import com.pelodeck.data.local.dao.BookmarkDao
import com.pelodeck.data.local.entity.Bookmark
import kotlinx.coroutines.flow.Flow

class BookmarkRepository(
    private val bookmarkDao: BookmarkDao
) {
    val allBookmarks: Flow<List<Bookmark>> = bookmarkDao.getAllBookmarks()

    suspend fun addBookmark(title: String, url: String) {
        val count = bookmarkDao.getCount()
        bookmarkDao.insert(
            Bookmark(
                title = title,
                url = url,
                orderIndex = count,
                isDefault = false
            )
        )
    }

    suspend fun deleteBookmark(bookmark: Bookmark) {
        bookmarkDao.delete(bookmark)
    }
}
