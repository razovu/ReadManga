package com.sale.readmanga.database

import androidx.room.*

import com.sale.readmanga.data.MangaHistory

@Dao
interface MangaHistoryDao {

    @Query("SELECT * FROM MangaHistory")
    fun getAll(): List<MangaHistory>?

    @Query("SELECT * FROM MangaHistory WHERE mangaName = :mangaName")
    fun getByMangaName(mangaName: String): MangaHistory?

    @Insert
    fun insert(history: MangaHistory?)

    @Update
    fun update(history: MangaHistory?)

    @Delete
    fun delete(history: MangaHistory?)
}