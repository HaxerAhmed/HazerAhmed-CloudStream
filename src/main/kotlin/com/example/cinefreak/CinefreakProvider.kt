package com.example.cinefreak

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.api.MovieSearchResponse
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.*

/**
 * Simple example provider for the movie site Cinefreak.
 *
 * This class extends the CloudStream MainAPI and implements search
 * functionality by scraping the cinefreak.net search page.  It needs
 * additional methods (getMainPage, load) to fully integrate with the
 * CloudStream framework.  Use this file as a starting point and fill
 * in the missing parts following the official CloudStream provider docs.
 */
class CinefreakProvider : MainAPI() {
    override var mainUrl = "https://cinefreak.net"
    override var name = "Cinefreak"
    override val hasMainPage = true
    override val supportedTypes = setOf(TvType.Movie)

    /**
     * Search the Cinefreak site by building a query URL and parsing the
     * returned HTML.  The site uses `?s=your+query` for searches.
     */
    override suspend fun search(query: String, page: Int): List<SearchResponse> {
        val url = "$mainUrl/?s=${query.encodeURLParameter()}"
        val doc = app.get(url).document
        return doc.select("h2.entry-title a").map {
            val title = it.text()
            val href = it.attr("href")
            MovieSearchResponse(
                title = title,
                url = href,
                apiName = name,
                type = TvType.Movie
            )
        }
    }

    // TODO: Implement getMainPage() to show latest releases and categories.
    // TODO: Implement load() to extract video links from a movie page.
}
