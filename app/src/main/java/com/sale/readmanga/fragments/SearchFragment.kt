package com.sale.readmanga.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.sale.readmanga.R
import com.sale.readmanga.adapters.MangaListRVAdapter
import com.sale.readmanga.data.MangaList
import com.sale.readmanga.network.SiteContent
import com.sale.readmanga.tools.addOnScrolledToEnd
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.item_progress_bottom_bar.*
import kotlinx.android.synthetic.main.recycler_view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class SearchFragment : Fragment(R.layout.fragment_search), CoroutineScope {

    //Coroutines
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    private val adapter = MangaListRVAdapter()
    private var query = ""
    private var listManga: MutableList<MangaList> = mutableListOf()
    private var paramOffsetSearch = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        searchRequest()
        if(listManga.isEmpty()) bottom_pb?.visibility = View.GONE
    }

    private fun searchRequest() {

        /*Почему-то OnQueryTextListener срабатывал дважды. searchView.clearFocus() решил проблему
        * И вот ответ почему -> Это касается только физических клавиатур
        * the action that user presses search key on keyboard produces two key-event ACTION_DOWN and ACTION_UP,
        * and some device would react to both these two message(supposed to react to ACTION_UP only),
        *  it turns out to be a bug of SDK or Device themselves that can't be controled by us developers.
        *  */
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean = false
            override fun onQueryTextSubmit(queryString: String): Boolean {
                paramOffsetSearch = 0
                query = queryString
                updateList()
                searchView.clearFocus()
                return false
            }
        })
    }

    private fun setAdapter() {

        val searchOk = getString(R.string.some_result_is_not_available)
        val searchNotOk = getString(R.string.no_results)

        job = launch {
            if (listManga.isEmpty()) {
                Toast.makeText(activity, searchNotOk, Toast.LENGTH_SHORT).show()
                bottom_pb?.visibility = View.GONE
            } else {
                Toast.makeText(activity, searchOk, Toast.LENGTH_SHORT).show()
                adapter.set(listManga)
                bottom_pb?.visibility = View.GONE
            }
        }
    }

    private fun initRecyclerView() {
        //Определение ширины экрана и подсчета колонн
        val metrics = resources.displayMetrics
        val spanCount = (metrics.widthPixels / (115 * metrics.scaledDensity)).toInt()
        val mLayoutManager = GridLayoutManager(activity, spanCount)

        rv_list_of_manga.removeAllViews()
        rv_list_of_manga.adapter = adapter
        rv_list_of_manga.layoutManager = mLayoutManager
        rv_list_of_manga.addOnScrolledToEnd { updateList() }
    }

    private fun updateList() {
        bottom_pb?.visibility = View.VISIBLE
        launch(Dispatchers.Default) {
            listManga.clear()
            listManga.addAll(SiteContent.searchManga(paramOffsetSearch, query))
            setAdapter()
            paramOffsetSearch += 50
        }
    }

}