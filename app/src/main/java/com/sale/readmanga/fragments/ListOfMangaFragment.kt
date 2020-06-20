package com.sale.readmanga.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.sale.readmanga.R
import com.sale.readmanga.adapters.MangaListAdapter
import com.sale.readmanga.data.Manga
import com.sale.readmanga.noname.addOnScrolledToEnd
import kotlinx.android.synthetic.main.fragment_list_of_manga.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import kotlin.coroutines.CoroutineContext


//TODO реализовать множество recycler'ов с категориями + сортировка
class ListOfMangaFragment : Fragment(), CoroutineScope {

    companion object {
        const val baseUrl: String = "https://readmanga.me"
    }


    private var job: Job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + job


    private var offCount = 0
    private var offCountSearch = 0
    private val listManga = mutableListOf<Manga>()
    private val adapter = MangaListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_of_manga, container, false)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        searchRequest()
        logoBtn.setOnClickListener {
            listManga.clear()
            offCount = 0
            update()
        }

        //Первичная загрузка данных
        if(listManga.isEmpty()) { update() }


    }

    private fun searchRequest() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(queryString: String): Boolean {
                offCountSearch = 0
                launch(Dispatchers.Default) {
                    searchManga(searchView.query.toString())
                    gettingJob()
                    offCountSearch += 50
                }
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
    }

    private fun gettingJob(): Job {
        job = launch { adapter.set(listManga) }
        return job
    }

    private fun searchManga(query: String) {
        val url = "https://readmanga.me/search"
        val client = OkHttpClient()
        val body = FormBody.Builder()
            .add("q", query)
            .add("offset", offCountSearch.toString())
            .build()
        val req = Request.Builder().url(url).post(body).build()
        val response = client.newCall(req).execute()
        val jj = Jsoup.parse(response.body?.string())


        try {
            val result = jj.selectFirst("#mangaResults").selectFirst("h3").text()
            val element = jj.select("div[class=tile col-sm-6 ]")

            showToast(result.substringBefore(")") + ")")
            listManga.clear()
            for (i in 0 until element.size) {

                val ttl = element.select("img[src]").eq(i).attr("alt").substringBefore("(")
                val linkImage = element.select("img[src]").eq(i).attr("data-original")
                val linkManga = element.select("h3").select("a").eq(i).attr("href")
                listManga.add(Manga(linkImage, ttl, linkManga))
            }
        } catch(e: NullPointerException) { showToast("Не найдено") }
    }


    private fun showToast(txt: String) {
        launch(Dispatchers.Main) { Toast.makeText(activity, txt, Toast.LENGTH_LONG).show() }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }


    // RecyclerView
    private fun initRecyclerView() {
        //Определение ширины экрана и подсчета колонн
        val metrics = resources.displayMetrics
        val spanCount = (metrics.widthPixels / (115 * metrics.scaledDensity)).toInt()
        val mLayoutManager = GridLayoutManager(activity, spanCount)


        rv_list_of_manga.adapter = adapter
        rv_list_of_manga.layoutManager = mLayoutManager


        //Загрузка при прокрутке
        rv_list_of_manga.addOnScrolledToEnd { update() }
    }

    //Парсинг данных в listManga
    //TODO вынести весь парсинг в отдельный файл "ContentProvider"
    private fun loadMangaList() {

        val url = "$baseUrl/list?sortType=rate&offset=$offCount"
        val doc = Jsoup.connect(url).get()
        val element = doc.select("div[class=tile col-sm-6 ]")

        for (i in 0 until element.size) {

            val ttl = element.select("img[src]").eq(i).attr("alt").substringBefore("(")
            val linkImage = element.select("img[src]").eq(i).attr("data-original")
            val linkManga = element.select("h3").select("a").eq(i).attr("href")
            listManga.add(Manga(linkImage, ttl, linkManga))
        }

    }

    private fun update() {
        launch(Dispatchers.Default) {
            loadMangaList()
            gettingJob()
            offCount += 70
        }
    }


}
