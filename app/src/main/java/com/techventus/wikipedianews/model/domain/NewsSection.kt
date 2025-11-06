package com.techventus.wikipedianews.model.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Domain model representing a section of news articles.
 *
 * Examples: "Topics in the News", "Ongoing Events", "Recent Deaths", etc.
 */
@Parcelize
data class NewsSection(
    val header: String,
    val articles: List<NewsArticle>
) : Parcelable
