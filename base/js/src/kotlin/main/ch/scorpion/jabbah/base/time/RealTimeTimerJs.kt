package ch.scorpion.jabbah.base.time

import ch.scorpion.jabbah.base.event.ActionEvent
import kotlin.js.Date

/**
 * Implements the [Timer] interface on the JavaScript platform.
 */
class RealTimeTimerJs : Timer {

    /** The ID returned from `window.setInterval()`.*/
    private var id: Int? = null

    override var interval: Int = 0

    private var handler: ((ActionEvent) -> Unit)? = null

	private var repeats: Boolean = true

    /** ---- [Timer] interface */

    override fun initialize(interval: Int, repeats: Boolean, handler: (ActionEvent) -> Unit) {
        if (this.interval > 0) {
            throw IllegalStateException("already initialized")
        }
        this.interval = interval
        this.handler = handler
	    this.repeats = repeats
    }

    override fun start() {
        if (interval == 0) {
            throw IllegalStateException("not yet initialized")
        }
	    if (id != null) {
	    	// already running
	    	return
	    }
	    id = if (repeats) {
		    kotlinx.browser.window.setInterval({
			    handler!!(ActionEvent(null, kotlinx.browser.window, 0, "timer", Date().getTime().toLong()))
		    }, interval)
	    } else {
	    	kotlinx.browser.window.setTimeout({
			    handler!!(ActionEvent(null, kotlinx.browser.window, 0, "timer", Date().getTime().toLong()))
		    }, interval)
	    }
    }

    override fun stop() {
        if (interval == 0) {
            throw IllegalStateException("not yet initialized")
        }
	    id?.let {
	    	if (repeats) {
			    kotlinx.browser.window.clearInterval(it)
		    } else {
		        kotlinx.browser.window.clearTimeout(it)
		    }
		    id = null
	    }
    }

    override fun isRunning(): Boolean = id != null
}