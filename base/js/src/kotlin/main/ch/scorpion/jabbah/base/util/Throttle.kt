package ch.scorpion.jabbah.base.util

import kotlinx.browser.window

/**
 * Throttles calls of [function] to not more than once in [interval].
 */
class Throttle(
    private val function: () -> Unit,
    private val interval: Int,
) {
    private var timer: Int? = null

    fun invokeFirst() {
        if (timer == null) {
            function.invoke()
            timer = window.setTimeout({ timer = null }, interval)
        }
    }

    fun invokeLast() {
        if (timer == null) {
            timer = window.setTimeout(
                {
                    function.invoke()
                    timer = null
                },
                interval)
        }
    }
}