package com.sale.readmanga.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.sale.readmanga.R
import com.sale.readmanga.adapters.HistoryRVAdapter
import com.sale.readmanga.app.App
import com.sale.readmanga.data.MangaHistory
import com.sale.readmanga.data.MangaListForHistory
import com.sale.readmanga.database.MangaHistoryDao
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.android.synthetic.main.recycler_view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.DateFormat
import kotlin.coroutines.CoroutineContext

class HistoryFragment : Fragment(R.layout.fragment_history), CoroutineScope {

    //Coroutine
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    //Room
    private lateinit var allTitles: List<MangaHistory>
    private lateinit var historyDao: MangaHistoryDao
    private lateinit var dbHistory: App

    private val adapter = HistoryRVAdapter()
    private val historyList = mutableListOf<MangaListForHistory>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHistory = App.instance!!
        historyDao = dbHistory.database?.historyDao()!!

        historyList.clear()
        initRecyclerView()
        dataBaseManipulations()
    }

    private fun dataBaseManipulations() {

        //Извлекаем из таблицы и добавляем данные о каждом тайтле в historyList
        job = launch(Dispatchers.Default) {
            allTitles = historyDao.getAll() as List<MangaHistory>
            for (i in allTitles) {
                if (i.mangaHistory != "0"){
                    historyList.add(MangaListForHistory(
                        i.mangaImg,
                        i.mangaName,
                        i.mangaLink,
                        i.lastTime,
                        i.lastVol)
                    )
                }
            }
            gettingJob()
        }
    }

    private fun gettingJob(): Job {
        //Для удобства пользователя мы сортируем лист истории по timeInMillis и конвертируем в нормальные дату и время.
        historyList.sortByDescending { it.lastTime }
        historyList.map { it.lastTime = DateFormat.getInstance().format(it.lastTime.toLong()) }

        job = launch {
            if (historyList.isEmpty()) {
                txt_empty_history.visibility = View.VISIBLE
            } else {
                txt_empty_history.visibility = View.GONE
                adapter.set(historyList)
            }
        }
        return job
    }

    private fun initRecyclerView() {
        val mLayoutManager = LinearLayoutManager(activity)
        rv_list_of_manga.adapter = adapter
        rv_list_of_manga.layoutManager = mLayoutManager

    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

}