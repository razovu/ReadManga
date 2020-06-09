package com.sale.readmanga.fragments

import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.sale.readmanga.R
import kotlinx.android.synthetic.main.fragment_manga_desc.*
import kotlinx.android.synthetic.main.item_progress_bar.*
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import kotlin.coroutines.CoroutineContext

//TODO сохранить состояние этого фрагмента в onStop или OnDestroy
class MangaDescriptionFragment : Fragment(), CoroutineScope {

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    private val chaptersUrls = mutableListOf<String>()
    private val choiceList = mutableListOf<String>()
    private var chapterNum = "chapter Url"
    private val baseUrl = ListOfMangaFragment().BASE_URL
    private var mangaTitle = "manga name"
    var mangaLink: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        retainInstance = true
        return inflater.inflate(R.layout.fragment_manga_desc, container, false)

    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mangaLink = arguments?.getString("link").toString()

        Toast.makeText(activity, mangaLink, Toast.LENGTH_LONG).show()
        launch(Dispatchers.Default) {
            getMangaDescription()
            job.isCompleted
        }

        //Кнопка "назад" в тулбаре
        tlbr_desc.setNavigationOnClickListener { activity?.onBackPressed() }

        //Кнопка "читать"
        readbtn.setOnClickListener {
//            it.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.searchbtn_anim))
            basicAlert()
        }
    }



    //Диалоговое окно с выбором главы
    private fun basicAlert(){
        val choiceArr = choiceList.map { it.substringAfter(mangaTitle).trim() }.toList().toTypedArray()
        val builder = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.AlertDialogCustom))

        builder.setTitle("Выбор главы")

        builder.setItems(choiceArr) { dialog, which ->
            chapterNum = chaptersUrls[which]
            Toast.makeText(activity, choiceList[which], Toast.LENGTH_SHORT).show()
            findNavController().navigate(MangaDescriptionFragmentDirections
                .actionMangaDescFragmentToReadThisFragment().setChapterNum(chapterNum).setLink(mangaLink))
        }

        //Выбор первой главы
        builder.setPositiveButton("Первая глава") { dialog, which ->

            chapterNum = chaptersUrls.last()
            Log.e("CHAPTER", chapterNum)
            findNavController().navigate(MangaDescriptionFragmentDirections
                .actionMangaDescFragmentToReadThisFragment().setChapterNum(chapterNum).setLink(mangaLink))
        }


        builder.setNegativeButton("Отмена") { dialog, which ->
            Toast.makeText(activity, "OK", Toast.LENGTH_SHORT).show()
        }

        builder.show()

    }



    //Парсинг описания манги
    private fun getMangaDescription() {
        pb.visibility = ProgressBar.VISIBLE
        val doc = Jsoup.connect(baseUrl + mangaLink).get()
        val element = doc.select(".expandable")

        val titleImg = element.select("img[src]").attr("src")
        val descTxt = doc.selectFirst(".manga-description").text()
        val itemsDesc = doc.select(".subject-meta.col-sm-7").select("p").eachText()

        //Название манги + извлечение списка глав в choiceList
        mangaTitle = doc.selectFirst(".name").text()

        //список глав
        doc.select(".expandable.chapters-link")
           .select("a[href]")
           .forEach { choiceList.add(it.text()) }

        //список ссылок на главы
        doc.select(".expandable.chapters-link")
            .select("a")
            .eachAttr("href")
            .map { chaptersUrls.add(it.toString()) }


//        val a = itemsDesc.map{ SpannableStringBuilder().bold { append(it.substringBefore(":")) } }
//        //TODO выделить жирным шрифтом элементы описания

        launch {
            ctlbr_desc.title = mangaTitle
            Glide.with(title_img_full).load(titleImg).into(title_img_full)
            description_txt.text = descTxt
            item_desc_txt.text = itemsDesc.joinToString("\n")
            pb.visibility = ProgressBar.GONE
        }

    }

}
