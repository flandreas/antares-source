package io.antarescircuit.jabbah.base.ui

import kotlin.math.max

object DisplayDuration {

    private const val MIN_HOLD_DURATION_MS = 2_000

    private const val CHARACTERS_PER_SECOND = 15

    fun calculateMilliseconds(text: String): Int {
        return max(MIN_HOLD_DURATION_MS, (text.length / CHARACTERS_PER_SECOND) * 1000)
    }
}