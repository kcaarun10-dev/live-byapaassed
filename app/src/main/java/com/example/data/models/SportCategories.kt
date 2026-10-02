package com.example.data.models

data class SportCategory(
    val id: String,
    val name: String,
    val apiSlug: String,
    val iconName: String = "sports"
) {
    companion object {
        val ALL = SportCategory("all", "ALL", "all", "sports")
        val FOOTBALL = SportCategory("football", "FOOTBALL", "football", "sports_soccer")
        val CRICKET = SportCategory("cricket", "CRICKET", "cricket", "sports_cricket")

        val CATEGORIES = listOf(
            ALL,
            FOOTBALL,
            CRICKET
        )

        /**
         * Checks if a given category string or slug corresponds to Football or Cricket
         */
        fun isAllowedSport(category: String?): Boolean {
            if (category.isNullOrBlank()) return false
            val lower = category.lowercase().trim()
            return lower.contains("football") ||
                    lower.contains("soccer") ||
                    lower.contains("cricket") ||
                    lower == "football" ||
                    lower == "cricket" ||
                    lower == "soccer"
        }

        fun findBySlug(slug: String): SportCategory {
            return CATEGORIES.firstOrNull {
                it.apiSlug.equals(slug, ignoreCase = true) ||
                        it.id.equals(slug, ignoreCase = true) ||
                        it.name.equals(slug, ignoreCase = true)
            } ?: if (slug.contains("cricket", ignoreCase = true)) CRICKET else FOOTBALL
        }
    }
}
