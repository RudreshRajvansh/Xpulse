package com.rudresh.xpulse.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun QrCode(seed: String, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(220.dp)) {
        val n = 21
        val cell = size.width / n
        var h = 2166136261L
        for (c in seed) {
            h = (h xor c.code.toLong()) * 16777619L and 0xffffffffL
        }
        fun rand(): Double {
            h = (h * 1103515245L + 12345L) and 0x7fffffffL
            return h.toDouble() / 0x7fffffffL
        }
        fun isFinder(x: Int, y: Int) =
            (x < 6 && y < 6) || (x > n - 7 && y < 6) || (x < 6 && y > n - 7)

        for (y in 0 until n) {
            for (x in 0 until n) {
                if (!isFinder(x, y) && rand() > 0.55) {
                    drawRect(Color.Black, Offset(x * cell, y * cell), Size(cell, cell))
                }
            }
        }

        fun finder(fx: Int, fy: Int) {
            drawRect(Color.Black, Offset(fx * cell, fy * cell), Size(6 * cell, 6 * cell))
            drawRect(Color.White, Offset((fx + 1) * cell, (fy + 1) * cell), Size(4 * cell, 4 * cell))
            drawRect(Color.Black, Offset((fx + 2) * cell, (fy + 2) * cell), Size(2 * cell, 2 * cell))
        }
        finder(0, 0)
        finder(n - 6, 0)
        finder(0, n - 6)
    }
}
