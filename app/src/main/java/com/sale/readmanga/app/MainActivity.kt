package com.sale.readmanga.app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.sale.readmanga.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initbnv()
    }


    private fun initbnv() {
        val navController = findNavController(R.id.fragment)
        NavigationUI.setupWithNavController(bnv, navController)
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if (destination.id == R.id.readThisFragment) {
                bnv.visibility = View.GONE
            } else {
                bnv.visibility = View.VISIBLE
            }
        }
    }



}




