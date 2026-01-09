import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class CineroticProvider : MainAPI() {

    override var name = "Cinerotic"
    override var mainUrl = "https://cinerotic.net"
    override val supportedTypes = setOf(TvType.NSFW)
    override val hasMainPage = true

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {

        val doc = app.get(mainUrl).document

        val items = doc.select("article").mapNotNull {
            val a = it.selectFirst("a") ?: return@mapNotNull null
            val title = a.text()
            val link = a.attr("href")
            val poster = it.selectFirst("img")?.attr("src")

            newMovieSearchResponse(title, fixUrl(link), TvType.NSFW) {
                posterUrl = poster
            }
        }

        return HomePageResponse(
            listOf(HomePageList("Latest", items)),
            false
        )
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val doc = app.get("$mainUrl/?s=$query").document

        return doc.select("article").mapNotNull {
            val a = it.selectFirst("a") ?: return@mapNotNull null
            newMovieSearchResponse(a.text(), fixUrl(a.attr("href")), TvType.NSFW)
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val title = doc.selectFirst("h1")?.text() ?: "Cinerotic"

        val video = doc.selectFirst("video source")
            ?.attr("src")
            ?: throw ErrorLoadingException("Video not found")

        return newMovieLoadResponse(title, url, TvType.NSFW, video)
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        callback(
            ExtractorLink(
                name,
                name,
                data,
                mainUrl,
                Qualities.Unknown.value,
                false
            )
        )
        return true
    }
}
