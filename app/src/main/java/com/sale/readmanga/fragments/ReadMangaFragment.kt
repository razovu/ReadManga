package com.sale.readmanga.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.loader.glide.GlideImageLoader
import com.sale.readmanga.R
import com.sale.readmanga.adapters.ReadMangaViewPagerAdapter
import kotlinx.android.synthetic.main.fragment_read_manga.*
import kotlinx.android.synthetic.main.item_progress_bar.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.jsoup.Jsoup
import java.io.IOException
import kotlin.coroutines.CoroutineContext

//TODO навигация << < pageNumber > >>
class ReadMangaFragment : Fragment(), CoroutineScope{

    private val imageList = mutableListOf<String>()

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    private var chapterNum = "номер главы"
    private var chaptersUrl = "ссылка на конкретную главу"
    private val baseUrl = ListOfMangaFragment().BASE_URL

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Инициализация BigImageView
        BigImageViewer.initialize(GlideImageLoader.with(activity))
        return inflater.inflate(R.layout.fragment_read_manga, container, false)

    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //Получаем аргументы навигации(ссылку на главу)
        chapterNum = arguments?.getString("chapterNum").toString()
        chaptersUrl = "$baseUrl$chapterNum?mtr=1"
        Log.e("TEST", chaptersUrl)

        //Парсим пикчи в BigImageView и заполняем ViewPager
        launch(Dispatchers.Default) { tryToParse() }

    }


    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun tryToParse() {
        try {
            getChaptersLink(chaptersUrl)
        } catch (e: IOException) {
            Log.e("TEST", e.message.toString())
        }
    }

    //Парсим данные
    private fun getChaptersLink(chaptersUrl: String) {

        //Прогрессбар располагается за пикчами. Что логично, если пикча еще не загрузилась - виден прогрессбар
        //Из очевидных минусов - ресурсы и информативность. Пока не знаю насколько это тяжелое решение
        pb.visibility = ProgressBar.VISIBLE

        val doc = Jsoup.connect(chaptersUrl).get()
        val lineLinks = doc.data()
            .substringAfter("rm_h.init( ")
            .substringBefore(", 0, false);")
            .replace("manga/", "")


        // Получили 3 эелемента (2 сервера и 1 ссылка на пикчу)
        for (index in 0 until JSONArray(lineLinks).length()) {
            val tempList = JSONArray(lineLinks).getJSONArray(index)
            val link = tempList.get(0).toString() + tempList.get(2).toString()

            imageList.add(link)
        }

        launch {
            viewPager.adapter = ReadMangaViewPagerAdapter(requireContext(), imageList)
        }

    }

}
