package com.sale.readmanga.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sale.readmanga.data.MangaHistory


@Database(entities = [MangaHistory::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): MangaHistoryDao?
}