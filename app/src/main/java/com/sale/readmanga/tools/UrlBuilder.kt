package com.sale.readmanga.tools

/** Только не говорите, что все url'ки надо вынести в strings. ДА ВЫ ЧТО!! БОЖЕ УПАСИ!
 *  P.S. лень было юзать FormBody.Builder() поэтому ручками, да да. Так и проще */

class UrlBuilder {
    companion object {
        //sort in url always on 1st place!
        fun build(filter: Int = 0, sort: Int = 0, category: Int= 0, genres: Int = 0) : String {
            val baseUrl = "https://readmanga.live/list"
            val prefixFilter = getFilterPrefix(filter)
            val prefixSort = getSortPrefix(sort)
            var url = ""

            when {
                category == 0 && genres == 0 -> url = baseUrl + prefixSort + prefixFilter
                genres != 0 -> url = baseUrl + getGenresPrefix(genres) + prefixSort + prefixFilter
                category != 0 -> url = baseUrl + getCategoryPrefix(category) + prefixSort + prefixFilter
            }

            return url
        }

        private fun getGenresPrefix(genres: Int): String {
            return when(genres) {
                1 -> "/genre/art"
                2 -> "/genre/action"
                3 -> "/genre/martial_arts"
                4 -> "/genre/vampires"
                5 -> "/genre/harem"
                6 -> "/genre/gender_intriga"
                7 -> "/genre/heroic_fantasy"
                8 -> "/genre/detective"
                9 -> "/genre/josei"
                10 -> "/genre/doujinshi"
                11 -> "/genre/drama"
                12 -> "/genre/game"
                13 -> "/genre/historical"
                14 -> "/genre/cyberpunk"
                15 -> "/genre/codomo"
                16 -> "/genre/comedy"
                17 -> "/genre/maho_shoujo"
                18 -> "/genre/mecha"
                19 -> "/genre/sci_fi"
                20 -> "/genre/natural"
                21 -> "/genre/postapocalypse"
                22 -> "/genre/adventure"
                23 -> "/genre/psychological"
                24 -> "/genre/romance"
                25 -> "/genre/samurai"
                26 -> "/genre/supernatural"
                27 -> "/genre/shoujo"
                28 -> "/genre/shoujo_ai"
                29 -> "/genre/shounen"
                30 -> "/genre/shounen_ai"
                31 -> "/genre/sports"
                32 -> "/genre/seinen"
                33 -> "/genre/tragedy"
                34 -> "/genre/thriller"
                35 -> "/genre/horror"
                36 -> "/genre/fantasy"
                37 -> "/genre/school"
                38 -> "/genre/ecchi"
                39 -> "/genre/yuri"
                else -> ""
            }
        }

        private fun getFilterPrefix(filter: Int): String {
            return when(filter) {
                1 -> "&filter=high_rate"
                2 -> "&filter=single"
                3 -> "&filter=mature"
                4 -> "&filter=completed"
                5 -> "&filter=translated"
                6 -> "&filter=many_chapters"
                7 -> "&filter=wait_upload"
                else -> ""
            }
        }

        private fun getSortPrefix(sort: Int): String {
            return when(sort) {
                1 -> "?sortType=name"
                2 -> "?sortType=votes"
                3 -> "?sortType=created"
                4 -> "?sortType=updated"
                else -> "?sortType=rate"
            }
        }

        private fun getCategoryPrefix(category: Int): String {
            return when(category) {
                1 -> "/category/yonkoma"
                2 -> "/category/comix"
                3 -> "/category/manhwa"
                4 -> "/category/manhua"
                5 -> "/tag/color"
                6 -> "/tag/web"
                7 -> "/tag/stopped"
                else -> ""
            }

        }


    }

}