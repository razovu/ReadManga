package com.sale.readmanga.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.sale.readmanga.R
import com.sale.readmanga.adapters.MangaListRVAdapter
import com.sale.readmanga.app.App
import com.sale.readmanga.data.MangaHistory
import com.sale.readmanga.data.MangaList
import com.sale.readmanga.database.MangaHistoryDao
import kotlinx.android.synthetic.main.fragment_favorites.*
import kotlinx.android.synthetic.main.recycler_view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class FavoritesFragment : Fragment(R.layout.fragment_favorites), CoroutineScope {

    //Coroutine
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    //Room
    private lateinit var title: MangaHistory
    private lateinit var historyDao: MangaHistoryDao
    private lateinit var dbHistory: App

    //SharedPreferences
    private lateinit var sharedPreferences: SharedPreferences
    private val titleNamesList = mutableListOf<String>()

    private val adapter = MangaListRVAdapter()
    private val historyList = mutableListOf<MangaList>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleNamesList.clear()
        dbHistory = App.instance!!
        historyDao = dbHistory.database?.historyDao()!!
        sharedPreferences = requireActivity().getSharedPreferences(FAVORITES, Context.MODE_PRIVATE)
        sharedPreferences.all.forEach {
            titleNamesList.add(it.toString().substringBefore("="))
        }


        initRecyclerView()
        dataBaseManipulations()

    }

    private fun dataBaseManipulations() {
        historyList.clear()
        job = launch(Dispatchers.Default) {
            for (i in titleNamesList) {
                title = historyDao.getByMangaName(i)!!
                historyList.add(MangaList(title.mangaImg, title.mangaName, title.mangaLink))
            }
            setAdapter()
        }
    }

    private fun setAdapter(): Job {
        job = launch {
            if (titleNamesList.isEmpty()) {
                txt_empty_favorites.visibility = View.VISIBLE
            } else {
                txt_empty_favorites.visibility = View.GONE
                adapter.set(historyList)
            }
        }
        return job
    }

    private fun initRecyclerView() {
        //Определение ширины экрана и подсчета колонн
        val metrics = resources.displayMetrics
        val spanCount = (metrics.widthPixels / (115 * metrics.scaledDensity)).toInt()
        val mLayoutManager = GridLayoutManager(activity, spanCount)

        rv_list_of_manga.adapter = adapter
        rv_list_of_manga.layoutManager = mLayoutManager

    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    companion object {
        const val FAVORITES = "Favorites"
    }

}