package com.sale.readmanga.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MangaVolume(val volName: String, val volLink: String) : Parcelable