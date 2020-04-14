package com.programmersbox.smashinfo

import org.jsoup.Jsoup

class SpiritApi private constructor() {

    companion object {
        private val siteUrl = "https://smashultimatespirits.com/"
        private val baseUrl = "$siteUrl/actions/load_more.php?offset="
        fun getSpirits(loadPage: Int) = SpiritApi().spirits("$baseUrl$loadPage")
    }

    private fun spirits(url: String): List<Spirit> = Jsoup.connect(url).get()
        .select("div.spirit").map {
            Spirit(
                it.attr("id").toInt(),
                it.select("h5").text(),
                "$siteUrl${it.select("div.imageContainer").select("img").attr("src")}",
                "$siteUrl${it.select("p:has(img)").select("img").attr("src").removePrefix("./")}",
                it.select("p")[3].text()
            )
        }

}

data class Spirit(val id: Int, val name: String, val imageUrl: String, val iconUrl: String, val description: String)
