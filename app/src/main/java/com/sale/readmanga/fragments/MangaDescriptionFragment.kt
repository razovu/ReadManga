package com.sale.readmanga.fragments

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import kotlin.coroutines.CoroutineContext

//TODO сохранить состояние этого фрагмента в onStop или OnDestroy
class MangaDescriptionFragment : Fragment(), CoroutineScope {

    private val job: Job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + job


    private val mSettings = "SETTINGS"
    private lateinit var sharedPref: SharedPreferences
    private val chaptersUrls = mutableListOf<String>()
    private val titleList = mutableListOf<String>()
    private val baseUrl = ListOfMangaFragment.baseUrl
    private var mangaTitle = "manga name"
    private var mangaLink: String = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        retainInstance = true
        sharedPref = requireActivity().getSharedPreferences(mSettings, MODE_PRIVATE)
        mangaLink = arguments?.getString("link").toString()

        return inflater.inflate(R.layout.fragment_manga_desc, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        launch(Dispatchers.Default) { getMangaDescription() }


        //Кнопка "назад" в тулбаре
        tlbr_desc.setNavigationOnClickListener { activity?.onBackPressed() }

        //Кнопка "читать"
        readbtn.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.searchbtn_anim))
            basicAlert()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }


    //Диалоговое окно с выбором главы
    private fun basicAlert() {
        //Тк алерт билдер принимает только array<T> пришлось конвертнуть mutableList
        //И заодно удалить название тайтла перед номером главы


        val builder = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.AlertDialogCustom))
        val choiceArr = titleList
            .map { it.substringAfter(mangaTitle).trim() }
            .toList()
            .toTypedArray()

        with(builder) {
            setTitle("Выбор главы")

            // Сетим список глав и передаем ссылку выбранной главы
            setItems(choiceArr) { dialog, which ->
                sharedPref.edit().putString(mangaTitle, which.toString()).apply()
                navigateToReader(which.toString())
            }

            setPositiveButton("сначала") { dialog, which ->
                navigateToReader(chaptersUrls.size.toString())
            }

            if (sharedPref.contains(mangaTitle)) {
                setNeutralButton("Продолжить") { dialog, which ->
                    val lastPicked = sharedPref.getString(mangaTitle, "last picked chapter")
                    navigateToReader(lastPicked.toString())
                }
            }

            setNegativeButton("Отмена") { dialog, which ->
                Toast.makeText(activity, "OK", Toast.LENGTH_SHORT).show()
            }

            show()
        }
    }


    private fun navigateToReader(selected: String) {
        //Передаем в аргументы индекс выбранной главы и list ссылок.
        //не знаю почему я не использовал ArrayOf<Strings>, так что я просто привел к стрингу
        val action = MangaDescriptionFragmentDirections.actionMangaDescFragmentToReadThisFragment()
        findNavController().navigate(
            action
                .setChapterNum(selected)
                .setChaptersUrls(chaptersUrls.joinToString("\n"))
                .setMangaTitle(mangaTitle)
        )
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
            .forEach { titleList.add(it.text()) }

        //список ссылок на главы
        doc.select(".expandable.chapters-link")
            .select("a")
            .eachAttr("href")
            .map { chaptersUrls.add(it.toString()) }


//        val a = itemsDesc.map{ SpannableStringBuilder().bold { append(it.substringBefore(":")) } }
//        //TODO выделить жирным шрифтом элементы описания

        launch {
            ctlbr_desc.title = mangaTitle
            Glide.with(title_img_full)
                .load(titleImg)
                .into(title_img_full)
            description_txt.text = descTxt
            item_desc_txt.text = itemsDesc.joinToString("\n")
            pb.visibility = ProgressBar.GONE
        }

    }

}
