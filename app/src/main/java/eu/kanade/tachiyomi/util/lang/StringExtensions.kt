package eu.kanade.tachiyomi.util.lang

import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorInt
import kotlin.math.floor

/**
 * Replaces the given string to have at most [count] characters using [replacement] at its end.
 * If [replacement] is longer than [count] an exception will be thrown when `length > count`.
 */
fun String.chop(count: Int, replacement: String = "..."): String {
    return if (length > count) {
        take(count - replacement.length) + replacement
    } else {
        this
    }
}

fun String.removeArticles(): String {
    return when {
        startsWith("a ", true) -> substring(2)
        startsWith("an ", true) -> substring(3)
        startsWith("the ", true) -> substring(4)
        else -> this
    }
}

val String.sqLite: String
    get() = replace("'", "''")

fun String.trimOrNull(): String? {
    val trimmed = trim()
    return if (trimmed.isBlank()) null else trimmed
}

/**
 * Replaces the given string to have at most [count] characters using [replacement] near the center.
 * If [replacement] is longer than [count] an exception will be thrown when `length > count`.
 */
fun String.truncateCenter(count: Int, replacement: String = "..."): String {
    if (length <= count) {
        return this
    }

    val pieceLength: Int = floor((count - replacement.length).div(2.0)).toInt()

    return "${take(pieceLength)}$replacement${takeLast(pieceLength)}"
}

fun String.capitalizeWords(): String {
    val firstReplace = split(" ").joinToString(" ") { it.capitalize() }
    return firstReplace.split("-").joinToString("-") { it.capitalize() }
}

/**
 * Case-insensitive natural comparator for strings.
 */
fun String.compareToCaseInsensitiveNaturalOrder(other: String): Int {
    return String.CASE_INSENSITIVE_ORDER.then(naturalOrder()).compare(this, other)
}

fun CharSequence.tintText(@ColorInt color: Int): Spanned {
    val s = SpannableString(this)
    s.setSpan(ForegroundColorSpan(color), 0, this.length, 0)
    return s
}

fun String.highlightText(highlight: String, @ColorInt color: Int): Spanned {
    val wordToSpan: Spannable = SpannableString(this)
    indexesOf(highlight).forEach {
        wordToSpan.setSpan(BackgroundColorSpan(color), it, it + highlight.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    return wordToSpan
}

fun String.indexesOf(substr: String, ignoreCase: Boolean = true): List<Int> {
    val list = mutableListOf<Int>()
    if (substr.isBlank()) return list

    var i = -1
    while (true) {
        i = indexOf(substr, i + 1, ignoreCase)
        when (i) {
            -1 -> return list
            else -> list.add(i)
        }
    }
}
