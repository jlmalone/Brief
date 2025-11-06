package com.techventus.wikipedianews.model.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Domain model representing a news article from Wikipedia's Current Events portal.
 *
 * This is the clean domain model used throughout the app's business logic.
 * It is independent of data layer implementation details.
 */
@Parcelize
data class NewsArticle(
    val id: String,
    val title: String,
    val htmlContent: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable
