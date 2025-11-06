package com.techventus.wikipedianews.ui.compose.component

import android.text.Html
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.techventus.wikipedianews.model.domain.NewsArticle

/**
 * News article card component.
 * Displays a single news article with formatted HTML content.
 */
@Composable
fun NewsArticleCard(
    article: NewsArticle,
    onArticleClick: (NewsArticle) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onArticleClick(article) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Parse HTML and display as formatted text
            val formattedText = remember(article.htmlContent) {
                parseHtmlToAnnotatedString(article.htmlContent)
            }

            Text(
                text = formattedText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Parse HTML content to AnnotatedString for Compose.
 * This is a simplified parser - for production, consider using a library.
 */
private fun parseHtmlToAnnotatedString(html: String): AnnotatedString {
    // Remove HTML from string for simple display
    // In production, use a proper HTML parser or library like Accompanist
    val plainText = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()

    return buildAnnotatedString {
        append(plainText)

        // Check if text contains links (basic detection)
        if (html.contains("<a ")) {
            // Style as clickable text
            addStyle(
                style = SpanStyle(
                    color = androidx.compose.ui.graphics.Color.Blue,
                    textDecoration = TextDecoration.Underline
                ),
                start = 0,
                end = plainText.length
            )
        }
    }
}
