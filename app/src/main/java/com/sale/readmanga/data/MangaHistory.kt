package com.sale.readmanga.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MangaHistory(

    @PrimaryKey
    var mangaName: String = "0",

    var mangaImg: String = "",

    var mangaLink: String = "",

    var mangaHistory: String = "",

    var lastTime: String = "",

    var lastVol: String = ""
)