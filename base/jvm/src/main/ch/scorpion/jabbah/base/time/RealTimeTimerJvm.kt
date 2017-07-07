package ch.scorpion.jabbah.base.time

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.ActionListener
import ch.scorpion.jabbah.base.event.ActionListenerJvm

/**
 * A [Timer] implementation on the JVM that provides time timing events in real time.
 */
class RealTimeTimerJvm : Timer {

    private var timer: javax.swing.Timer? = null

    override fun initialize(interval: Int, handler: (ActionEvent) -> Unit) {
        if (timer != null) {
            throw IllegalStateException("already initialized")
        }
        timer = javax.swing.Timer(interval, ActionListenerJvm(handler))
    }

    override fun start() {
        if (timer == null) {
            throw IllegalStateException("not yet initialized")
        }
        timer!!.start()
    }

    override fun stop() {
        if (timer == null) {
            throw IllegalStateException("not yet initialized")
        }
        timer!!.stop()
    }

    override fun isRunning(): Boolean {
        if (timer == null) {
            throw IllegalStateException("not yet initialized")
        }
        return timer!!.isRunning
    }
}