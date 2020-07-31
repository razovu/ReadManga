package com.sale.readmanga.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.loader.glide.GlideImageLoader
import com.sale.readmanga.R
import com.sale.readmanga.adapters.ReadMangaViewPagerAdapter
import com.sale.readmanga.app.App
import com.sale.readmanga.data.MangaHistory
import com.sale.readmanga.data.MangaVolume
import com.sale.readmanga.database.MangaHistoryDao
import com.sale.readmanga.network.CheckConnection
import com.sale.readmanga.network.SiteContent
import kotlinx.android.synthetic.main.fragment_read_manga.*
import kotlinx.android.synthetic.main.item_progress_bar.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class ReadMangaFragment : Fragment(R.layout.fragment_read_manga), CoroutineScope {

    private var imageList = mutableListOf<String>()

    //Coroutines
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    //Room
    private lateinit var dbHistory: App
    private lateinit var historyDao: MangaHistoryDao

    //args
    private var networkManager = true
    private var currentVol: Int = 0
    private lateinit var mangaImg: String
    private lateinit var mangaTitle: String
    private lateinit var mangaLink: String
    private lateinit var volList: Array<Parcelable>
    private lateinit var currentMangaVol: MangaVolume


    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //BigImageView
        BigImageViewer.initialize(GlideImageLoader.with(activity))
        
        reader_layout.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION


        //Room
        dbHistory = App.instance!!
        historyDao = dbHistory.database?.historyDao()!!

        //args
        mangaTitle = arguments?.getString("mangaTitle").toString()
        volList = arguments?.getParcelableArray("volList")!!
        currentVol = arguments?.getString("chapterNum")?.toInt() ?: 0
        mangaImg = arguments?.getString("mangaImg").toString()
        mangaLink = arguments?.getString("mangaLink").toString()


        pb_layout.visibility = View.VISIBLE

        //Парсим пикчи в BigImageView и заполняем ViewPager
        update()

        initVolumesNavigation()
    }


    private fun initVolumesNavigation() {
        //помним, что currentVol это индекс в массиве, где 0 это первая глава
        btnNextVol.setOnClickListener {
            if (currentVol >= 0 && currentVol < volList.lastIndex) {
                job.cancel()
                currentVol++
                update()
            }
        }

        btnPrevVol.setOnClickListener {
            if (currentVol > 0 && currentVol <= volList.lastIndex) {
                job.cancel()
                currentVol--
                update()

            }
        }
    }

    private fun update() {
        networkManager = CheckConnection.NetworkManager.isNetworkAvailable(activity)
        if (networkManager) {
            job = launch(Dispatchers.Default) {
                getChaptersLink()
                historyEdit()
            }
        }
    }



    private fun historyEdit() {

        val prevValue = historyDao.getByMangaName(mangaTitle) as MangaHistory
        val newValue = "${currentVol}, ${prevValue.mangaHistory}"
        val currentTime = System.currentTimeMillis().toString()
        //Если в таблице не содержится значение конкретной главы(в нашем случае это индекс), то добавляем
        if (!prevValue.mangaHistory.contains(currentVol.toString())) {
            historyDao.update(
                MangaHistory(
                    mangaTitle, mangaImg, mangaLink, newValue, currentTime, currentMangaVol.volName
                )
            )
        }
        Log.e("historyDao", historyDao.getByMangaName(mangaTitle).toString())
    }


    private fun getChaptersLink() {
        currentMangaVol = volList[currentVol] as MangaVolume
        var counterText = ""
        imageList.clear()
        imageList = SiteContent.loadMangaPages(currentMangaVol.volLink)
        val vpAdapter = ReadMangaViewPagerAdapter(requireContext())

        job = launch {
            with(viewPager) {
                removeAllViews()
                offscreenPageLimit = 3
                adapter = vpAdapter
                vpAdapter.set(imageList)
                counterText = "1 | ${imageList.size}"
                pageCounter.text = counterText
                addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

                    override fun onPageScrolled(pos: Int, posOffset: Float, posOffsetPx: Int) {}
                    override fun onPageScrollStateChanged(state: Int) {}
                    override fun onPageSelected(pos: Int) {
                        counterText = "${pos + 1} | ${imageList.size}"
                        pageCounter.text = counterText
                    }
                })
            }
            pb_layout.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

}
