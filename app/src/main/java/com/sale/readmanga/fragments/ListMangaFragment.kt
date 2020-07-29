package com.sale.readmanga.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.sale.readmanga.R
import com.sale.readmanga.adapters.MangaListRVAdapter
import com.sale.readmanga.data.MangaList
import com.sale.readmanga.network.CheckConnection
import com.sale.readmanga.network.SiteContent
import com.sale.readmanga.tools.UrlBuilder
import com.sale.readmanga.tools.addOnScrolledToEnd
import kotlinx.android.synthetic.main.fragment_list_of_manga.*
import kotlinx.android.synthetic.main.item_dialog_filter.view.*
import kotlinx.android.synthetic.main.item_progress_bottom_bar.*
import kotlinx.android.synthetic.main.recycler_view.*
import kotlinx.coroutines.*
import java.io.IOException
import kotlin.coroutines.CoroutineContext


class ListMangaFragment : Fragment(R.layout.fragment_list_of_manga), CoroutineScope {


    //Coroutine
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    private var networkManager = true
    private var listManga: MutableList<MangaList> = mutableListOf()
    private val adapter = MangaListRVAdapter()

    //Url Builder
    private var url = "https://readmanga.live/list?sortType=rate"
    private var paramOffset = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()

        //Первичная загрузка данных
        if(listManga.isEmpty()) { goAhead() } else bottom_pb?.visibility = View.GONE

        sort_btn.setOnClickListener { dialogFilter() }

        swipeContainer.setOnRefreshListener { goAhead() }

        //spinner genres????
        genres_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                url = UrlBuilder.build(genres = genres_spinner.selectedItemPosition)
                Log.e("url", url)

                listManga.clear()
                paramOffset = 0
                goAhead()
            }

        }

    }

    private fun dialogFilter(){
        val nullParent: ViewGroup? = null
        val dialog = LayoutInflater.from(activity).inflate(R.layout.item_dialog_filter, nullParent)
        val builder = AlertDialog.Builder(activity, R.style.AlertDialogCustom)
            .setView(dialog)
            .setPositiveButton(getString(R.string.apply)) { _, _ ->
                val filter = dialog.filter_spinner.selectedItemPosition
                val sort = dialog.sort_spinner.selectedItemPosition
                val category = dialog.categories_spinner.selectedItemPosition
                url = UrlBuilder.build(filter, sort, category)
                Log.e("url", url)

                listManga.clear()
                paramOffset = 0
                goAhead()
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->  }
            .setTitle(getString(R.string.filter_title))

        builder.show()
    }


    // RecyclerView
    private fun initRecyclerView() {
        //Определение ширины экрана и подсчета колонн
        val metrics = resources.displayMetrics
        val spanCount = (metrics.widthPixels / (115 * metrics.scaledDensity)).toInt()
        val mLayoutManager = GridLayoutManager(activity, spanCount)

        rv_list_of_manga.adapter = adapter
        rv_list_of_manga.layoutManager = mLayoutManager
        rv_list_of_manga.addOnScrolledToEnd { goAhead() }
    }

    private fun goAhead() = launch { launchDataLoad() }

    private suspend fun bottomProgressBar(show: Boolean?) = withContext(Dispatchers.Main) {
        when(show) {
            true -> {
                bottom_pb?.visibility = View.VISIBLE
                txt_loading_pb?.text = getString(R.string.loading)
            }
            false -> {
                bottom_pb?.visibility = View.GONE
            }
            null -> {
                bottom_pb?.visibility = View.VISIBLE
                txt_loading_pb?.text = getString(R.string.network_notification)
            }
        }

    }

    private tailrec suspend fun launchDataLoad() {
        networkManager = CheckConnection.NetworkManager.isNetworkAvailable(activity)
        if (networkManager) {
            bottomProgressBar(show = true)
            update()
        } else {
            bottomProgressBar(show = null)
            delay(2000)
            launchDataLoad()
        }

    }

    private suspend fun update() = withContext(Dispatchers.Default)  {

        try {
            listManga.addAll(SiteContent.loadMangaList("$url&offset=$paramOffset"))
            setAdapter()
            paramOffset += 70
        } catch (e: IOException){
            delay(2000)
            launchDataLoad()
        }

    }

    private suspend fun setAdapter() = withContext(Dispatchers.Main) {
        adapter.set(listManga)
        bottomProgressBar(show = false)
        swipeContainer.isRefreshing = false
    }


    override fun onDestroyView() {
        super.onDestroyView()
        job.cancel()
    }

}
