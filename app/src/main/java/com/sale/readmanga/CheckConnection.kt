package com.sale.readmanga

import android.content.Context
import android.net.ConnectivityManager


class CheckConnection {

    object NetworkManager {

        fun isNetworkAvailable(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = cm.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }
}