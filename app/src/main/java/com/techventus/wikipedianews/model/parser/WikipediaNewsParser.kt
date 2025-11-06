package com.techventus.wikipedianews.model.parser

import com.techventus.wikipedianews.model.domain.NewsArticle
import com.techventus.wikipedianews.model.domain.NewsSection
import org.jsoup.Jsoup
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parser for Wikipedia's Current Events HTML content.
 *
 * Extracts structured news data from Wikipedia's Portal:Current_events page.
 * Following android-template pattern for clean separation of parsing logic.
 */
@Singleton
class WikipediaNewsParser @Inject constructor() {

    /**
     * Parse HTML content from Wikipedia Current Events portal.
     *
     * @param html Raw HTML content from the Wikipedia page
     * @return List of news sections with articles
     */
    fun parse(html: String): List<NewsSection> {
        try {
            if (html.isBlank()) {
                Timber.w("Empty HTML content provided to parser")
                return emptyList()
            }

            val doc = Jsoup.parse(html)
            val sections = mutableListOf<NewsSection>()

            // Parse Topics in the News
            parseSection(
                doc = doc,
                sectionLabel = "Topics_in_the_news",
                headerTitle = "Topics in the News"
            )?.let { sections.add(it) }

            // Parse Ongoing Events
            parseSection(
                doc = doc,
                sectionLabel = "Ongoing_events",
                headerTitle = "Ongoing"
            )?.let { sections.add(it) }

            // Parse Recent Deaths
            parseSection(
                doc = doc,
                sectionLabel = "Recent_deaths",
                headerTitle = "Recent Deaths"
            )?.let { sections.add(it) }

            // Parse "Current events of" sections (daily events)
            parseDailySections(doc)?.let { sections.addAll(it) }

            Timber.d("Successfully parsed ${sections.size} sections with ${sections.sumOf { it.articles.size }} total articles")

            return sections
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse Wikipedia HTML")
            return emptyList()
        }
    }

    /**
     * Parse a standard section (Topics, Ongoing, Recent Deaths).
     */
    private fun parseSection(
        doc: org.jsoup.nodes.Document,
        sectionLabel: String,
        headerTitle: String
    ): NewsSection? {
        return try {
            val section = doc.selectFirst("div[aria-labelledby=$sectionLabel]")
            if (section == null) {
                Timber.w("Section '$headerTitle' not found")
                return null
            }

            val list = section.selectFirst("ul")
            if (list == null) {
                Timber.w("List items under '$headerTitle' not found")
                return null
            }

            val articles = mutableListOf<NewsArticle>()
            val items = list.select("li")

            for (item in items) {
                val htmlContent = fixRelativeUrls(item.html().trim())
                val text = item.text().trim()

                if (htmlContent.isNotEmpty()) {
                    articles.add(
                        NewsArticle(
                            id = generateArticleId(headerTitle, text),
                            title = text,
                            htmlContent = htmlContent,
                            url = extractFirstUrl(htmlContent),
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }

            if (articles.isEmpty()) {
                Timber.w("No articles found in section '$headerTitle'")
                return null
            }

            Timber.d("Parsed section '$headerTitle' with ${articles.size} articles")
            NewsSection(header = headerTitle, articles = articles)
        } catch (e: Exception) {
            Timber.e(e, "Error parsing section '$headerTitle'")
            null
        }
    }

    /**
     * Parse daily "Current events of" sections.
     */
    private fun parseDailySections(doc: org.jsoup.nodes.Document): List<NewsSection>? {
        return try {
            val dayHeaders = doc.select("div.current-events-heading")
            if (dayHeaders.isEmpty()) {
                Timber.w("'Current events of' sections not found")
                return null
            }

            val sections = mutableListOf<NewsSection>()

            for (dayHeader in dayHeaders) {
                val titleElement = dayHeader.selectFirst("span.summary")
                if (titleElement == null) {
                    Timber.w("Title element not found in daily section")
                    continue
                }

                // Remove day-of-week suffix like " (Saturday)"
                val headerText = titleElement.text()
                    .replace(Regex(" \\(.*?\\)"), "")
                    .trim()

                val dayList = dayHeader.parent()?.selectFirst("div.current-events-content > ul")
                if (dayList == null) {
                    Timber.w("List items under 'Current events of $headerText' not found")
                    continue
                }

                val articles = mutableListOf<NewsArticle>()
                val dayItems = dayList.select("li")

                for (item in dayItems) {
                    val htmlContent = fixRelativeUrls(item.html().trim())
                    val text = item.text().trim()

                    if (htmlContent.isNotEmpty()) {
                        articles.add(
                            NewsArticle(
                                id = generateArticleId(headerText, text),
                                title = text,
                                htmlContent = htmlContent,
                                url = extractFirstUrl(htmlContent),
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }

                if (articles.isNotEmpty()) {
                    sections.add(NewsSection(header = headerText, articles = articles))
                    Timber.d("Parsed daily section '$headerText' with ${articles.size} articles")
                }
            }

            sections.ifEmpty { null }
        } catch (e: Exception) {
            Timber.e(e, "Error parsing 'Current events of' sections")
            null
        }
    }

    /**
     * Fix relative Wikipedia URLs to absolute URLs.
     */
    private fun fixRelativeUrls(html: String): String {
        return html.replace(
            "a href=\"/",
            "a href=\"https://en.m.wikipedia.org/"
        )
    }

    /**
     * Extract the first URL from HTML content.
     */
    private fun extractFirstUrl(html: String): String {
        val urlRegex = """href="([^"]+)"""".toRegex()
        val matchResult = urlRegex.find(html)
        return matchResult?.groupValues?.getOrNull(1) ?: ""
    }

    /**
     * Generate a unique article ID based on section and content.
     */
    private fun generateArticleId(section: String, content: String): String {
        // Use a hash of section + content for stable IDs
        val combined = "$section-$content"
        return UUID.nameUUIDFromBytes(combined.toByteArray()).toString()
    }
}
