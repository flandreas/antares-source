package ch.scorpion.jabbah.base.time

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.ActionListener
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.logger

/**
 * A timer abstraction to be used for implementing various types of timers for simulation processes,
 * such as real-time timers or test timers that allow precise control over time.
 *
 * A [Timer] repeatedly fires action events at the specified intervals.
 */
interface Timer {

    /** The time in milliseconds between two ticks.*/
    var interval: Int

    /**
     * Initializes this [Timer] with an interval and the one and only [ActionListener] to which
     * timing events are sent.
     *
     * @param interval the interval in milliseconds
     * @param handler the handler of an [ActionEvent] to be called after every timing interval
     */
    fun initialize(interval: Int, handler: (ActionEvent) -> Unit)

    fun start()

    fun stop()

    /**
     * Determines whether this [Timer] is currently running, i.e. whether it has been started and not yet stopped.
     */
    fun isRunning(): Boolean
}

/** A [Timer] whose timing events can be controlled by a [ControlledTimeService].*/
class ControlledTimer(val timeService: ControlledTimeService) : Timer {

    override var interval: Int = 0

    /** The interval between two timer ticks in nanoseconds.*/
    private val intervalNs: Int get() = interval * 1_000_000

    /** Determines whether this [Timer] is currently running.*/
    private var running: Boolean = false

    /** The time in nanoseconds at which the last timing events had been sent.*/
    private var lastTimerTime: Long = 0

    //private var listener: ActionListener? = null
    private var handler: ((ActionEvent) -> Unit)? = null

    init {
        timeService.addPropertyChangeListener(object : PropertyChangeListener<Long> {
            override fun propertyChanged(e: PropertyChangeEvent<Long>) {
                timeChanged()
            }
        })
    }

    /** ---- [Timer] interface */

    override fun initialize(interval: Int, handler: (ActionEvent) -> Unit) {
        ensureUninitialized()
        this.interval = interval
        this.handler = handler
    }

    override fun start() {
        ensureInitialized()
        if (running) {
            return
        }
        running = true
    }

    override fun stop() {
        ensureInitialized()
        if (!running) {
            return
        }
        running = false
    }

    override fun isRunning(): Boolean {
        return running
    }

    /** ---- [ControlledTimer] */

    private fun ensureUninitialized() {
        if (handler != null) {
            throw IllegalStateException("already initialized")
        }
    }

    private fun ensureInitialized() {
        if (handler == null) {
            throw IllegalStateException("not yet initialized")
        }
    }

    private fun timeChanged() {
        if (!running) {
            return
        }

        val now = timeService.nowNanos()
        if (now >= lastTimerTime + intervalNs) {
            lastTimerTime = now
            fireActionPerformed(now)
        }
    }

    private fun fireActionPerformed(now: Long) {
        handler!!(ActionEvent(source = this, modifiers = 0, action = "timer", time = now))
    }
}
