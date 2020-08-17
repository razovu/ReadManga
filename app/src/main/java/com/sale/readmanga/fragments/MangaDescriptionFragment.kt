package com.sale.readmanga.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.sale.readmanga.R
import com.sale.readmanga.adapters.MangaVolumesRVAdapter
import com.sale.readmanga.app.App
import com.sale.readmanga.data.MangaHistory
import com.sale.readmanga.data.MangaVolume
import com.sale.readmanga.database.MangaHistoryDao
import com.sale.readmanga.network.CheckConnection
import com.sale.readmanga.network.SiteContent
import kotlinx.android.synthetic.main.fragment_manga_desc.*
import kotlinx.android.synthetic.main.item_progress_bar.*
import kotlinx.android.synthetic.main.item_progress_bottom_bar.*
import kotlinx.android.synthetic.main.item_recycler_volume.*
import kotlinx.coroutines.*
import java.net.SocketTimeoutException
import kotlin.coroutines.CoroutineContext

class MangaDescriptionFragment : Fragment(R.layout.fragment_manga_desc), CoroutineScope {

    //Coroutines
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    //Room
    private lateinit var thisTitle: MangaHistory
    private lateinit var historyDao: MangaHistoryDao
    private lateinit var dbHistory: App

    //args
    private lateinit var mangaTitle: String
    private lateinit var mangaLink: String
    private lateinit var mangaImg: String
    private lateinit var sharedPreferences: SharedPreferences

    private val adapter = MangaVolumesRVAdapter()
    private var historyList = mutableListOf<String>()
    private var volList = mutableListOf<MangaVolume>()
    private var networkManager = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //shared preferences init
        sharedPreferences = requireActivity().getSharedPreferences(
            FavoritesFragment.FAVORITES,
            Context.MODE_PRIVATE
        )

        //Room
        dbHistory = App.instance!!
        historyDao = dbHistory.database?.historyDao()!!

        //Args
        mangaLink = arguments?.getString("link").toString()
        mangaTitle = arguments?.getString("title").toString()

        //Recycler
        fab_read.visibility = View.GONE
        lv_vol.adapter = adapter
        lv_vol.layoutManager = LinearLayoutManager(activity)

        //Read first volume button
        readFirstVol.setOnClickListener { navigateToReader("0") }

        //Favorites button
        initFavoritesBtn()

        //Back button
        tlbr_desc.setNavigationOnClickListener { activity?.onBackPressed() }

        //update tables n network
        goAhead()


    }

    private fun goAhead() = launch { launchDataLoad() }

    private suspend fun launchDataLoad() {
        networkManager = CheckConnection.NetworkManager.isNetworkAvailable(activity)
        if (networkManager) {
            try {
                initHistoryDao()
            } catch (e: SocketTimeoutException){
                initHistoryDao()
            }

        } else {
            delay(2000)
            launchDataLoad()
        }
    }

    private suspend fun initHistoryDao() = withContext(Dispatchers.Default) {

        //Пробуем извлечь данные с таблицы. Если таковой не существует, то ловим TypeCastException
        try {
            thisTitle = historyDao.getByMangaName(mangaTitle) as MangaHistory
            if (thisTitle.mangaHistory != "0") {
                initFabRead(thisTitle.mangaHistory.substringBefore(","))
                historyList.addAll(thisTitle.mangaHistory.split(", "))
            } else {
                initFabRead("0")
            }
            setMangaDesc()

        } catch (e: TypeCastException) {
            setMangaDesc()
            historyDao.insert(MangaHistory(mangaTitle, mangaImg, mangaLink, "0", ""))
            initFabRead("0")
            Log.e("room", "there is no such table")
        }

    }

    private fun initFavoritesBtn() {

        if (sharedPreferences.contains(mangaTitle)) {
            favoritesBtnStateChange(red = true)
        } else {
            favoritesBtnStateChange(red = false)
        }

        btn_favorites.setOnClickListener {
            if (btn_favorites.text != getText(R.string.in_favorites)) {
                favoritesBtnStateChange(red = true)
                sharedPreferences.edit().putString(mangaTitle, mangaTitle).apply()
            } else {
                favoritesBtnStateChange(red = false)
                sharedPreferences.edit().remove(mangaTitle).apply()
            }
        }
    }

    private fun favoritesBtnStateChange(red: Boolean) {
        if (red) {
            btn_favorites.text = getText(R.string.in_favorites)
            btn_favorites.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.favorites_red, 0, 0)
        } else {
            btn_favorites.text = getText(R.string.not_in_favorites)
            btn_favorites.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.favorites, 0, 0)
        }
    }

    private fun initFabRead(num: String) {

        if (num.toInt() != 0) fab_read.text = getString(R.string.continueReadingShort)
        fab_read.setOnClickListener { navigateToReader(num) }
    }

    private fun navigateToReader(volNum: String) {

        val bundle: Bundle = bundleOf(
            "volList" to volList.reversed().toTypedArray(),
            "mangaLink" to mangaLink,
            "mangaImg" to mangaImg,
            "chapterNum" to volNum,
            "mangaTitle" to mangaTitle
        )
        if (volList.isNotEmpty()) findNavController().navigate(R.id.readThisFragment, bundle)
    }

    //Парсинг описания манги
    private fun setMangaDesc() {
        pb_layout.visibility = View.VISIBLE

        val listMangaInfo = SiteContent.loadMangaDescription(mangaLink)
        mangaImg = listMangaInfo.mangaImg

        volList.clear()
        volList = SiteContent.loadMangaVolumeList(mangaLink)
        if (volList.isEmpty()) {
            launch {
                cv_vol.visibility = View.GONE
                fab_read.text = getString(R.string.vol_list_is_empty)
            }
        }

        //inflate views
        job = launch {

            ctlbr_desc.title = listMangaInfo.mangaTitle
            Glide.with(title_img_full)
                .load(listMangaInfo.mangaImg)
                .error(R.drawable.failure_img)
                .into(title_img_full)
            description_txt.text = listMangaInfo.mangaInfo
            item_desc_txt.text = listMangaInfo.mangaDesc

            adapter.set(volList, mangaTitle, mangaImg, mangaLink, historyList)

            pb_layout.visibility = View.GONE
            fab_read.visibility = View.VISIBLE
            bottom_pb?.visibility = View.GONE
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }


}
