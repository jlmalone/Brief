package com.techventus.wikipedianews.model.domain

enum class NewsSourceType {
    CURRENT_EVENTS,      // Portal:Current_events
    IN_THE_NEWS,         // Main_Page - "In the news" section
    ON_THIS_DAY,         // Main_Page - "On this day" section
    RECENT_DEATHS,       // Portal:Current_events - "Recent deaths" only
    ONGOING_EVENTS       // Portal:Current_events - "Ongoing" only
}

enum class WikipediaLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    SPANISH("es", "Español"),
    FRENCH("fr", "Français"),
    GERMAN("de", "Deutsch"),
    JAPANESE("ja", "日本語"),
    CHINESE("zh", "中文"),
    RUSSIAN("ru", "Русский"),
    PORTUGUESE("pt", "Português"),
    ITALIAN("it", "Italiano"),
    ARABIC("ar", "العربية")
}

data class NewsSource(
    val type: NewsSourceType,
    val language: WikipediaLanguage,
    val isEnabled: Boolean = true
) {
    val displayName: String
        get() = "${type.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }} (${language.displayName})"

    val url: String
        get() = when (type) {
            NewsSourceType.CURRENT_EVENTS -> "https://${language.code}.wikipedia.org/wiki/Portal:Current_events"
            NewsSourceType.IN_THE_NEWS -> "https://${language.code}.wikipedia.org/wiki/Main_Page"
            NewsSourceType.ON_THIS_DAY -> "https://${language.code}.wikipedia.org/wiki/Main_Page"
            NewsSourceType.RECENT_DEATHS -> "https://${language.code}.wikipedia.org/wiki/Portal:Current_events"
            NewsSourceType.ONGOING_EVENTS -> "https://${language.code}.wikipedia.org/wiki/Portal:Current_events"
        }
}
