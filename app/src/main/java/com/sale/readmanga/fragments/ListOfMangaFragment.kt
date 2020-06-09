package com.sale.readmanga.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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
import org.jsoup.Jsoup
import kotlin.coroutines.CoroutineContext


//TODO реализовать множество recycler'ов с категориями + сортировка
class ListOfMangaFragment : Fragment(), CoroutineScope {

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    val BASE_URL = "https://readmanga.me"
    var offCount = 0
    val listManga = mutableListOf<Manga>()
    private val adapter = MangaListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_of_manga, container, false)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Кнопка поиска и ее анимация
        searchbtn.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.searchbtn_anim))
        }

        initRecyclerView()


        //Первичная загрузка данных
        //TODO чтобы при возвращении на экран данные не подгружались снова
        launch(Dispatchers.Default) {
            loadMangaList()
            gettingJob()
            offCount += 70
        }


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
        rv_list_of_manga.addOnScrolledToEnd {
            launch(Dispatchers.Default) {
                loadMangaList()
                gettingJob()
                offCount += 70
            }
        }
    }

    //Парсинг данных в listManga
    private fun loadMangaList() {

        //https://readmanga.me/list?sortType=rate&offset=70
        val url = "$BASE_URL/list?sortType=rate&offset=$offCount"
        val doc = Jsoup.connect(url).get()
        val element = doc.select("div[class=tile col-sm-6 ]")

        for (i in 0 until element.size) {

            val ttl = element.select("img[src]").eq(i).attr("alt").substringBefore("(")
            val linkImage = element.select("img[src]").eq(i).attr("data-original")
            val linkManga = element.select("h3").select("a").eq(i).attr("href")
            listManga.add(Manga(linkImage, ttl, linkManga))
        }

    }

    private fun gettingJob(): Job {
        job = launch { adapter.set(listManga) }
        return job
    }

}
