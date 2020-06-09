package com.sale.readmanga.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.sale.readmanga.CheckConnection
import com.sale.readmanga.R

/**
 * Проверка подключения к инету
 */
class ConnectionProblemsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_connection_problems, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        if (CheckConnection.NetworkManager.isNetworkAvailable(requireActivity())) {
//            findNavController().navigate(ConnectionProblemsFragmentDirections.actionConnectionProblemsFragmentToListOfManga())
//        }
    }

}
