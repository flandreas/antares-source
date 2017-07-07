package ch.scorpion.jabbah.base.time

import ch.scorpion.jabbah.base.event.ActionEvent
import kotlin.js.Date

/**
 * Implements the [Timer] interface on the JavaScript platform.
 */
class RealTimeTimerJs : Timer {

    /** The ID returned from `window.setInterval()`.*/
    private var id: Int? = null

    private var interval: Int? = null

    private var handler: ((ActionEvent) -> Unit)? = null

    /** ---- [Timer] interface */

    override fun initialize(interval: Int, handler: (ActionEvent) -> Unit) {
        if (this.interval != null) {
            throw IllegalStateException("already initialized")
        }
        this.interval = interval
        this.handler = handler
    }

    override fun start() {
        if (interval == null) {
            throw IllegalStateException("not yet initialized")
        }
        id = kotlin.browser.window.setInterval({
            handler!!(ActionEvent(kotlin.browser.window, 0, "timer", Date().getTime().toLong()))
        }, interval!!)
    }

    override fun stop() {
        if (interval == null) {
            throw IllegalStateException("not yet initialized")
        }
        kotlin.browser.window.clearInterval(id!!)
        id = null
    }

    override fun isRunning(): Boolean {
        return id != null
    }
}