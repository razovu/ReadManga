package com.sale.readmanga.network

import com.sale.readmanga.data.MangaDesc
import com.sale.readmanga.data.MangaList
import com.sale.readmanga.data.MangaVolume
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class SiteContent {

    companion object {

        private const val baseUrl: String = "https://readmanga.live"
        private const val adultPrefix: String = "?mtr=1"
        private const val subSearch: String = "/search"
        private const val subGenres: String = "/genres"

        private val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .callTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        private fun getDoc(url: String): Document {
            return try {

                val req = Request.Builder().url(url).build()
                val call = client.newCall(req)
                val response = call.execute()

                Jsoup.parse(response.body?.string())

            } catch (e: SocketTimeoutException){
                getDoc(url)
            }
        }


        //Оставлю незаюзанным. Пока нет желания браться за ресайклер внутри ресайклера
        fun loadRecommendCategory(): MutableList<String> {
            val category = mutableListOf<String>()

            val url = "$baseUrl/list/presentation"
            val doc = Jsoup.connect(url).get()
            val element = doc.select("pageBlock container")

            element.select("h3").forEach { category.add(it.text().replace(" далее", "")) }
            category.removeAt(category.lastIndex)

            return category
        }

        fun loadRecommend(): MutableList<MutableList<MangaList>> {

            /*
            Тут есть сложность для меня.. Я получил список категорий + Все эелементы каждой категории(по 8шт в каждой)
            Полагается наверное, что мне нужно создать rv итемом которого будет другой rv. Звучит запарно)
            */

            val listManga = mutableListOf<MutableList<MangaList>>()

            val url = "$baseUrl/list/presentation"
            val doc = Jsoup.connect(url).get()
            val element = doc.select("pageBlock container")

            element.select(".row.tiles-row.long").forEach {
                val manga = mutableListOf<MangaList>()
                it.select(".simple-tile").select("a").forEach { x ->
                    val ttl = x.select("img[src]").attr("alt").substringBefore("(")
                    val linkImage = x.select("img[src]").attr("data-original")
                    val linkManga = x.attr("href")
                    manga.add(MangaList(linkImage, ttl, linkManga))
                }
                listManga.add(manga)
                listManga.removeAt(listManga.lastIndex)
            }

            return listManga
        }


        fun loadMangaList(url: String): MutableList<MangaList> {

            val listManga = mutableListOf<MangaList>()

            val doc = getDoc(url)
            val element = doc.select("div[class=tile col-sm-6 ]")

            for (i in 0 until element.size) {

                val ttl = element.select("img[src]").eq(i).attr("alt").substringBefore("(")
                val linkImage = element.select("img[src]").eq(i).attr("data-original")
                val linkManga = element.select("h3").select("a").eq(i).attr("href")
                listManga.add(MangaList(linkImage, ttl, linkManga))
            }
            return listManga
        }

        fun loadMangaDescription(mangaLink: String): MangaDesc {

            val url = baseUrl + mangaLink
            val doc = getDoc(url)
            val element = doc.select(".expandable")

            val mangaImg = element.select("img[src]").attr("src")
            val mangaInfo = doc.selectFirst(".manga-description").text()
            val mangaDesc = doc.select(".subject-meta.col-sm-7").select("p").eachText()
            val mangaTitle = doc.selectFirst(".name").text()

            return MangaDesc(mangaImg, mangaTitle, mangaDesc.joinToString("\n"), mangaInfo)
        }

        fun loadMangaVolumeList(mangaLink: String): MutableList<MangaVolume> {

            val url = baseUrl + mangaLink
            val mangaVolumeList = mutableListOf<MangaVolume>()
            val volNames = mutableListOf<String>()
            val volLinks = mutableListOf<String>()

            val doc = getDoc(url)
            val mangaTitle = doc.selectFirst(".name").text()
            val element = doc.select(".expandable.chapters-link")

            element.select("a[href]")
                .forEach { volNames.add(it.text()) }

            element.select("a")
                .eachAttr("href")
                .map { volLinks.add(it.toString()) }

            //Сразу укоротим, убрав название тайтла перед номером главы
            for (i in 0 until volNames.size) {
                mangaVolumeList.add(
                    MangaVolume(
                        volNames[i].substringAfter(mangaTitle).trim(),
                        volLinks[i]
                    )
                )
            }
            return mangaVolumeList
        }


        fun loadMangaPages(volLink: String): MutableList<String> {
            val imageList = mutableListOf<String>()
            val url = baseUrl + volLink + adultPrefix
            val doc = getDoc(url)

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
            return imageList
        }

        //На вход подаются параметры post запроса.
        fun searchManga(paramOffsetSearch: Int, query: String): MutableList<MangaList> {
            val listManga = mutableListOf<MangaList>()

            val body = FormBody.Builder()
                .add("q", query)
                .add("offset", paramOffsetSearch.toString())
                .build()
            val req = Request.Builder().url(baseUrl + subSearch).post(body).build()
            val response = client.newCall(req).execute()
            val doc = Jsoup.parse(response.body?.string())


            try {
                val element = doc.select("div[class=tile col-sm-6 ]")

                listManga.clear()
                for (i in 0 until element.size) {

                    val ttl = element.select("img[src]").eq(i).attr("alt").substringBefore("(")
                    val linkImage = element.select("img[src]").eq(i).attr("data-original")
                    val linkManga = element.select("h3").select("a").eq(i).attr("href")
                    if (ttl.isNotBlank()) listManga.add(MangaList(linkImage, ttl, linkManga))
                }

                return listManga

            } catch (e: NullPointerException) {
                return listManga
            }
        }
    }
}