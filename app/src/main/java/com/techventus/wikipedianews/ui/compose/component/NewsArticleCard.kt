package com.techventus.wikipedianews.ui.compose.component

import android.content.Intent
import android.text.Html
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.techventus.wikipedianews.model.domain.NewsArticle

/**
 * News article card component.
 * Displays a single news article with formatted HTML content and action buttons.
 */
@Composable
fun NewsArticleCard(
    article: NewsArticle,
    onArticleClick: (NewsArticle) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clickable { onArticleClick(article) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Share button
                IconButton(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, article.title)
                            putExtra(Intent.EXTRA_TEXT, "${article.title}\n\n${article.url}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share article"))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share article",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Open in browser button
                IconButton(
                    onClick = { onArticleClick(article) }
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = "Open in browser",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
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
