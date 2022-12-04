package ch.scorpion.jabbah.base.time

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

	private lateinit var handler: ActionListenerJvm

	/** Tuning: Avoid [javax.swing.Timer.isRunning] which is costly when called often. */
	private var isRunning: Boolean = false

	private var repeats: Boolean = false

    override fun initialize(interval: Int, repeats: Boolean, handler: TimerHandler): Timer {
        if (timer != null) {
            throw IllegalStateException("already initialized")
        }
	    this.repeats = repeats
	    this.handler = TimerListener(handler)

        timer = javax.swing.Timer(interval, this.handler).also {
        	it.isRepeats = repeats
        }

	    return this
    }

    override fun start() = timer?.let {
	    isRunning = true
	    it.start()
    } ?: throw IllegalStateException("not yet initialized")

    override fun stop() = timer?.let {
		isRunning = false
	    it.stop()
    } ?: throw IllegalStateException("not yet initialized")

    override fun isRunning(): Boolean = isRunning

	private inner class TimerListener(handler: TimerHandler) : ActionListenerJvm(handler) {
		override fun actionPerformed(e: java.awt.event.ActionEvent?) {
			super.actionPerformed(e)
			if (!repeats) {
				isRunning = false
			}
		}
	}
}