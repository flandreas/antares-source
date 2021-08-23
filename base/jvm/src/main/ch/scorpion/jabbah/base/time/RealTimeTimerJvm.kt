package ch.scorpion.jabbah.base.time

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.ActionListenerJvm

/**
 * A [Timer] implementation on the JVM that provides time timing events in real time.
 */
class RealTimeTimerJvm : Timer {

    override var interval: Int
        get() = timer?.delay ?: throw IllegalStateException("not yet initialized")
        set(value) {
            timer?.initialDelay = value
            timer?.delay = value
        }

    private var timer: javax.swing.Timer? = null

    override fun initialize(interval: Int, repeats: Boolean, handler: (ActionEvent) -> Unit) {
        if (timer != null) {
            throw IllegalStateException("already initialized")
        }
        timer = javax.swing.Timer(interval, ActionListenerJvm(handler)).also {
        	it.isRepeats = repeats
        }
    }

    override fun start() = timer?.start() ?: throw IllegalStateException("not yet initialized")

    override fun stop() = timer?.stop() ?: throw IllegalStateException("not yet initialized")

    override fun isRunning(): Boolean = timer?.isRunning ?: throw IllegalStateException("not yet initialized")
}