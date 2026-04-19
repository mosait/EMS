package com.mosait.ems.core.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Transforms raw digit input (e.g. "01012000") into date format "01.01.2000".
 * Dots are inserted after position 2 and 4 automatically.
 */
class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val out = buildString {
            for (i in raw.indices) {
                if (i == 2 || i == 4) append('.')
                append(raw[i])
            }
        }
        return TransformedText(AnnotatedString(out), DateOffsetMapping(raw.length))
    }

    private class DateOffsetMapping(private val rawLength: Int) : OffsetMapping {
        // raw "DDMMYYYY" → visual "DD.MM.YYYY"
        override fun originalToTransformed(offset: Int): Int = when {
            offset <= 2 -> offset
            offset <= 4 -> offset + 1
            else -> offset + 2
        }.coerceAtMost(if (rawLength <= 2) rawLength else if (rawLength <= 4) rawLength + 1 else rawLength + 2)

        override fun transformedToOriginal(offset: Int): Int = when {
            offset <= 2 -> offset
            offset <= 5 -> offset - 1
            else -> offset - 2
        }.coerceIn(0, rawLength)
    }
}
