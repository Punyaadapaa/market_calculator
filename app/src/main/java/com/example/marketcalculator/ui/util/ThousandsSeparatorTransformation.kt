package com.example.marketcalculator.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Nampilin angka mentah (misal "1500000") jadi "1.500.000" di layar,
 * tapi yang kesimpen di state tetap angka polos. Jadi kalkulasi di belakang
 * gak perlu ribet parsing titik lagi.
 */
class ThousandsSeparatorTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        if (original.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val n = original.length
        val toTransformed = IntArray(n + 1)
        val builder = StringBuilder()
        var idx = 0

        for (i in 0 until n) {
            toTransformed[i] = idx
            val sisaDariKanan = n - i
            if (i != 0 && sisaDariKanan % 3 == 0) {
                builder.append('.')
                idx++
            }
            builder.append(original[i])
            idx++
        }
        toTransformed[n] = idx

        val transformedText = builder.toString()

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                toTransformed[offset.coerceIn(0, n)]

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, transformedText.length)
                var dots = 0
                for (i in 0 until clamped) {
                    if (transformedText[i] == '.') dots++
                }
                return (clamped - dots).coerceIn(0, n)
            }
        }

        return TransformedText(AnnotatedString(transformedText), mapping)
    }
}
