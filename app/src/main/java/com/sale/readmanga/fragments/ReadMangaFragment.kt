package com.sale.readmanga.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.loader.glide.GlideImageLoader
import com.sale.readmanga.R
import com.sale.readmanga.adapters.ReadMangaViewPagerAdapter
import kotlinx.android.synthetic.main.fragment_read_manga.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.jsoup.Jsoup
import java.io.IOException
import kotlin.coroutines.CoroutineContext

//TODO навигация << < pageNumber > >>
class ReadMangaFragment : Fragment(), CoroutineScope {

    private val imageList = mutableListOf<String>()
    private val chaptersList = mutableListOf<String>()

    private lateinit var sharedPref: SharedPreferences
    private val mSettings = "SETTINGS"
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    private var mangaTitle: String =""
    private var chaptersLine: String? = ""
    private var chapterNum: Int = -1
    private val baseUrl = ListOfMangaFragment.baseUrl

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedPref = requireActivity().getSharedPreferences(mSettings, Context.MODE_PRIVATE)

        getArgs()
        // Инициализация BigImageView
        BigImageViewer.initialize(GlideImageLoader.with(activity))
        return inflater.inflate(R.layout.fragment_read_manga, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //Парсим пикчи в BigImageView и заполняем ViewPager
        launch(Dispatchers.Default) {
            initBtmNav()
            tryToParse()
        }

    }

    //Получаем аргументы навигации
    private fun getArgs() {

        mangaTitle = arguments?.getString("mangaTitle").toString()

        //Получаем стрингу с кучей ссылок и добавляем в list
        chaptersLine = arguments?.getString("chaptersUrls")
        chaptersLine?.split("\n")?.forEach { chaptersList.add(it) }

        //Получаем индекс и составляем ссылку на главу. "?mtr=1" - подтверждение возрастных ограничений
        chapterNum = arguments?.getString("chapterNum")!!.toInt().minus(1)


    }

    private fun initBtmNav() {

        btnPrevVol.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.btn_prev_anim))

            if (chapterNum > 0) {
                chapterNum++
                sharedPref
                    .edit()
                    .clear()
                    .putString(mangaTitle, chapterNum.plus(1).toString())
                    .apply()

                Log.e("sharedPref", sharedPref.getString(mangaTitle, "").toString())
                launch(Dispatchers.Default) {
                    tryToParse()
                }
            }
        }

        btnNextVol.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.btn_next_anim))

            if (chapterNum > 0) {
                chapterNum--
                sharedPref
                    .edit()
                    .clear()
                    .putString(mangaTitle, chapterNum.plus(1).toString())
                    .apply()

                Log.e("sharedPref", sharedPref.getString(mangaTitle, "").toString())

                launch(Dispatchers.Default) {
                    tryToParse()
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun tryToParse() {
        try {
            getChaptersLink()
        } catch (e: IOException) {
            Log.e("TEST", e.message.toString())
        }
    }

    //Парсим данные? Пришлось добавить аннотацию, чтобы не бесил warning
    @SuppressLint("SetTextI18n")
    private fun getChaptersLink() {

        val chaptersUrl = baseUrl + chaptersList[chapterNum] + "?mtr=1"
        val doc = Jsoup.connect(chaptersUrl).get()
        val lineLinks = doc.data()
            .substringAfter("rm_h.init( ")
            .substringBefore(", 0, false);")
            .replace("manga/", "")


        imageList.clear()
        // Получили 3 эелемента (2 сервера и 1 ссылка на пикчу)
        for (index in 0 until JSONArray(lineLinks).length()) {
            val tempList = JSONArray(lineLinks).getJSONArray(index)
            val link = tempList.get(0).toString() + tempList.get(2).toString()

            imageList.add(link)

        }

        //лютейший говнокод, но пока пусть будет так
        //может быть убрать with{}?
        //TODO вынести листенер в отдельный файл, по человечески
        job = launch {
            with(viewPager) {
                removeAllViews()
                offscreenPageLimit = 4
                adapter = ReadMangaViewPagerAdapter(requireContext(), imageList)
                pageCounter.text = "1 | ${imageList.size}"
                addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

                    override fun onPageScrolled(pos: Int, posOffset: Float, posOffsetPx: Int) {}
                    override fun onPageScrollStateChanged(state: Int) {}
                    override fun onPageSelected(pos: Int) {
                        pageCounter.text = "${pos + 1} | ${imageList.size}"
                    }
                })
            }

        }


    }

}
