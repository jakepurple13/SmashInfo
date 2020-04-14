package com.programmersbox.smashinfo

import com.programmersbox.gsonutils.getJsonApi
import org.jsoup.Jsoup

class SpiritApi private constructor() {

    companion object {
        private const val siteUrl = "https://smashultimatespirits.com"
        private const val baseUrl = "$siteUrl/actions/load_more.php?offset="
        private const val searchUrl = "$siteUrl/actions/search.php?q="
        private const val getAllUrl = "$siteUrl/api/spirits/GET_all.php"
        fun getSpirits(loadPage: Int) = SpiritApi().spirits("$baseUrl$loadPage")
        fun getAllSpirits() = SpiritApi().getAll(getAllUrl)
    }

    private fun spirits(url: String): List<Spirit> = Jsoup.connect(url).get()
        .select("div.spirit").map {
            Spirit(
                it.attr("id").toInt(),
                it.select("h5").text(),
                "$siteUrl${it.select("div.imageContainer").select("img").attr("src")}",
                "$siteUrl${it.select("p:has(img)").select("img").attr("src").removePrefix("./")}",
                it.select("p")[3].text(),
                it.select("p")[2].text()
            )
        }

    private fun getAll(url: String) = getJsonApi<Base>(url)?.records

}

data class Base(val records: List<Spirits>?)

data class Spirits(val id: String?, val name: String?, val game: String?, val series: String?, val description: String?) {
    fun toSpirit() = Spirit(
        id?.toInt() ?: 0, name ?: "", game = series ?: "", description = description ?: "",
        imageUrl = "https://smashultimatespirits.com/img/spiritImages/$id.png",
        iconUrl = "https://smashultimatespirits.com/img/seriesIcons/$series.png"
    )
}

data class Spirit(val id: Int, val name: String, val imageUrl: String, val iconUrl: String, val description: String, val game: String)
