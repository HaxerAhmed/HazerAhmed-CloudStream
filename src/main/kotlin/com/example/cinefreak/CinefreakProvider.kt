package com.example.cinefreak

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element
import java.net.URLEncoder

class CinefreakProvider : MainAPI() {
    override var mainUrl = "https://cinefreak.net"
    override var name = "CineFreak"
    override var lang = "en"
    override val hasMainPage = true
    override val hasDownloadSupport = true
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override val mainPage = mainPageOf(
        "/" to "Latest",
        "/category/bangla-dubbed/" to "Bangla Dubbed",
        "/category/hindi-movies/" to "Hindi Movies",
        "/category/english-movies/" to "English Movies",
        "/category/dual-audio/" to "Dual Audio",
        "/category/bangla-movies/" to "Bangla Movies",
        "/category/web-series/" to "Web Series",
        "/category/horror/" to "Horror"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val path = request.data
        val url = when (path) {
            "/", "" -> if (page == 1) mainUrl else "$mainUrl/page/$page/"
            else -> if (page == 1) "$mainUrl$path" else "$mainUrl$path/page/$page/"
        }
        val document = app.get(url).document
        val cards = document.select("article").ifEmpty { document.select("div.post") }
        val results = cards.mapNotNull { toSearchResult(it) }
        return newHomePageResponse(request.name, results, results.isNotEmpty())
    }

    private fun toSearchResult(el: Element): SearchResponse? {
        val link = el.selectFirst(".entry-title a")?.absUrl("href")
            ?: el.selectFirst("h2 a")?.absUrl("href")
            ?: el.selectFirst("a[href]")?.absUrl("href")
            ?: return null
        val title = el.selectFirst(".entry-title a")?.text()
            ?: el.selectFirst("h2 a")?.text()
            ?: el.selectFirst("a")?.attr("title")
            ?: ""
        if (title.isBlank()) return null
        var poster = el.selectFirst("img[src]")?.attr("src") ?: ""
        if (poster.isBlank()) {
            poster = el.selectFirst("img[data-src]")?.attr("data-src") ?: ""
        }
        return newMovieSearchResponse(title, link, TvType.Movie) {
            this.posterUrl = poster
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val url = "$mainUrl/?s=$encoded"
        val document = app.get(url).document
        val cards = document.select("article").ifEmpty { document.select("div.post") }
        return cards.mapNotNull { toSearchResult(it) }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val title = document.selectFirst("h1.entry-title")?.text()
            ?: document.selectFirst("title")?.text()
            ?: url
        var poster = document.selectFirst(".post-thumbnail img[src]")?.attr("src") ?: ""
        if (poster.isBlank()) {
            poster = document.selectFirst(".post-thumbnail img[data-src]")?.attr("data-src") ?: ""
        }
        val linkButtons = document.select("a:matchesOwn(?i)(Download Links|Watch Online|Download|Watch)")
        val links = linkButtons.mapNotNull { it.absUrl("href") }.distinct()
        val data = links.joinToString("+")
        return newMovieLoadResponse(title, url, TvType.Movie, data) {
            posterUrl = poster
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val parts = data.split("+").filter { it.isNotBlank() }
        for (part in parts) {
            callback(
                newExtractorLink(
                    this.name,
                    this.name,
                    part
                )
            )
        }
        return true
    }
}
