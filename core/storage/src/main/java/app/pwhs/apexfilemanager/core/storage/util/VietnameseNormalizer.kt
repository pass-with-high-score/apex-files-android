package app.pwhs.apexfilemanager.core.storage.util

import java.text.Normalizer
import java.util.regex.Pattern

object VietnameseNormalizer {
    private val DIACRITICAL_MARKS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")

    fun removeAccents(text: String): String {
        if (text.isEmpty()) return ""
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
        val withoutMarks = DIACRITICAL_MARKS_PATTERN.matcher(normalized).replaceAll("")
        return withoutMarks
            .replace('đ', 'd')
            .replace('Đ', 'D')
    }

    fun containsIgnoreCaseAndAccents(source: String, query: String): Boolean {
        if (query.isEmpty()) return true
        val normalizedSource = removeAccents(source).lowercase()
        val normalizedQuery = removeAccents(query).lowercase()
        return normalizedSource.contains(normalizedQuery)
    }
}
