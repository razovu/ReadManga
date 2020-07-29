package com.sale.readmanga.app

import android.app.Application
import androidx.room.Room
import com.sale.readmanga.database.AppDatabase


class App : Application() {
    var database: AppDatabase? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = Room.databaseBuilder(this, AppDatabase::class.java, "history")
            .build()


    }

    companion object {
        var instance: App? = null
    }
}