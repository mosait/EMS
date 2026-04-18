package com.mosait.ems.core.ui.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateTimeUtil {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    fun formatDate(date: LocalDate?): String = date?.format(dateFormatter) ?: ""
    fun formatTime(dateTime: LocalDateTime?): String = dateTime?.format(timeFormatter) ?: ""
    fun formatDateTime(dateTime: LocalDateTime?): String = dateTime?.format(dateTimeFormatter) ?: ""

    fun parseDate(text: String): LocalDate? = try {
        LocalDate.parse(text, dateFormatter)
    } catch (_: Exception) {
        null
    }
}
