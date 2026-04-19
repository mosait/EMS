package com.mosait.ems.core.ui.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

object DateTimeUtil {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    // Strict formatter rejects invalid days/months (e.g. 32.13.2000)
    private val strictDateFormatter = DateTimeFormatter.ofPattern("dd.MM.uuuu")
        .withResolverStyle(ResolverStyle.STRICT)

    fun formatDate(date: LocalDate?): String = date?.format(dateFormatter) ?: ""
    fun formatTime(dateTime: LocalDateTime?): String = dateTime?.format(timeFormatter) ?: ""
    fun formatDateTime(dateTime: LocalDateTime?): String = dateTime?.format(dateTimeFormatter) ?: ""

    fun parseDate(text: String): LocalDate? = try {
        LocalDate.parse(text, dateFormatter)
    } catch (_: Exception) {
        null
    }

    /**
     * Parses a date from 8 raw digits (DDMMYYYY).
     * Returns null if incomplete, invalid date, or in the future.
     */
    fun parseDateFromDigits(text: String): LocalDate? {
        if (text.length != 8) return null
        return try {
            val formatted = "${text.substring(0, 2)}.${text.substring(2, 4)}.${text.substring(4, 8)}"
            val date = LocalDate.parse(formatted, strictDateFormatter)
            if (date.isAfter(LocalDate.now())) null else date
        } catch (_: Exception) {
            null
        }
    }

    /** Formats a LocalDate to 8 raw digits (DDMMYYYY) for use with DateVisualTransformation. */
    fun formatDateToDigits(date: LocalDate?): String =
        date?.format(dateFormatter)?.replace(".", "") ?: ""
}
