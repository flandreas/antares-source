package ch.scorpion.jabbah.base.invocation

import ch.scorpion.jabbah.base.swing.StateBar
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.base.logger
import java.awt.Component
import java.awt.Cursor
import java.awt.event.ActionListener
import java.awt.event.KeyAdapter
import java.awt.event.MouseAdapter
import javax.swing.RootPaneContainer
import javax.swing.SwingUtilities
import javax.swing.Timer

/**
 * A [BusyHandler] graphically informs the user that the user interface is busy waiting until a background
 * activity has been finished.
 *
 * During the background activity the user interface is blocked using the glass pane and the waiting
 * cursor is used (UI busy). When the background activity is finished, the glass pane and the cursor are not reset
 * immediately, to avoid flickering if another background activity starts shortly afterwards.
 *
 * The [busyCounter] keeps track of the current number of running background activities. Whenever it changes from
 * 0 to 1, the UI is switched to busy synchronously. Therefore, no new events are fired by buttons afterwards. When the
 * busyCounter changes from 1 to 0, switching the UI from busy to normal is triggered with some delay. This is
 * implemented by using a [Timer]. The [Timer] is started when the busyCounter changes from 1 to 0 and cancelled
 * when the counter changes form 0 to 1. Whenever the timer fires, the UI is switched to normal.
 *
 * @property rootPaneContainer the [RootPaneContainer] whose busyness is handled by this [BusyHandler]
 * @property stateBar the optional [StateBar] that contains additional busyness indications
 */
class BusyHandler(val rootPaneContainer: RootPaneContainer, val stateBar: StateBar?) {

    companion object {
        val LOG by logger(BusyHandler.Companion::class)

        /** Stores all registered [BusyHandler]s.*/
        private val HANDLERS: MutableMap<RootPaneContainer, BusyHandler> = mutableMapOf()

        /** Creates a new [BusyHandler] and for the specified [RootPaneContainer] with optional [StateBar], and registers it.*/
        fun register(container: RootPaneContainer, stateBar: StateBar?): BusyHandler {
            return register(BusyHandler(container, stateBar))
        }

        /** Registers the specified [BusyHandler] and returns it */
        fun register(busyHandler: BusyHandler): BusyHandler {
            synchronized(HANDLERS) {
	            LOG.trace("BusyHandler: registering for ${busyHandler.rootPaneContainer::class.simpleName}")
                HANDLERS.put(busyHandler.rootPaneContainer, busyHandler)
            }
            return busyHandler
        }

        /** Unregisters the specified [RootPaneContainer] by removing its [BusyHandler].*/
        fun deregister(container: RootPaneContainer) {
            synchronized(HANDLERS) {
	            LOG.trace("BusyHandler: registering for ${container::class.simpleName}")
                HANDLERS[container]?.dispose()
                HANDLERS.remove(container)
            }
        }

        /** Unregisters the specified [BusyHandler].*/
        @Suppress("unused")
        fun deregister(busyHandler: BusyHandler) {
            deregister(busyHandler.rootPaneContainer)
        }

        /** Increments the busy counter for all registered [BusyHandler]s.*/
        fun increment() {
            for (busyHandler in copyBusyHandlers()) {
                busyHandler.incrementBusyCounter()
            }
        }

        /** Decrements the busy counter for all registered [BusyHandler]s.*/
        fun decrement() {
            for (busyHandler in copyBusyHandlers()) {
                busyHandler.decrementBusyCounter()
            }
        }


        /** Set the visibility of the state bar of all registered busy handlers */
        @Suppress("unused")
        fun setStateBarsVisible(visible: Boolean) {
            for (busyHandler in copyBusyHandlers()) {
                busyHandler.showStateBar = visible
            }
        }

        private fun copyBusyHandlers(): List<BusyHandler> {
            synchronized(HANDLERS) {
                val result = mutableListOf<BusyHandler>()
                result.addAll(HANDLERS.values)
                return result
            }
        }
    }

    /** The busy counter represents the current state driven by starting and stopping background activities. */
    private var busyCounter: Int = 0

    /** Contains the current state of the user interface.*/
    private var uiBusy: Boolean = false

    private var showStateBar: Boolean = false

    private val switchUIToNormalTimer: Timer

    private val mouseHandler = object : MouseAdapter() {}

    private val keyHandler = object : KeyAdapter() {}

    init {
        rootPaneContainer.glassPane.addMouseListener(mouseHandler)
        rootPaneContainer.glassPane.addKeyListener(keyHandler)

        switchUIToNormalTimer = Timer(100, ActionListener {
            if (busyCounter == 0 && uiBusy) {
                // switch UI to normal
                LOG.trace("Switch UI to normal")
                uiBusy = false
                (rootPaneContainer as Component).cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
                rootPaneContainer.glassPane.isVisible = false
                stateBar?.netState = StateBar.NetState.NET_INACTIVE
            }
        })
        switchUIToNormalTimer.isRepeats = false
    }

    fun dispose() {
        rootPaneContainer.glassPane.removeMouseListener(mouseHandler)
        rootPaneContainer.glassPane.removeKeyListener(keyHandler)
    }

    /**
     * Increments the busy counter. To be called when starting a background activity.
     * Can be called on any thread.
     */
    fun incrementBusyCounter() {
        LOG.trace("incrementBusyCounter")
        var counter = 0
        synchronized(this) {
            busyCounter++
            counter = busyCounter
        }
        if (counter == 1) {
            busyStateChanged()
        }
    }

    /**
     * Decrements the busy counter. To be called when a background acivity completed.
     * Can be called on any thread.
     */
    fun decrementBusyCounter() {
        LOG.trace("decrementBusyCounter")
        var counter = 0
        synchronized(this) {
            if (busyCounter == 0) {
                return
            }
            busyCounter--
            counter = busyCounter
        }

        if (counter == 0) {
            busyStateChanged()
        }
    }


    /**
     * Called whenever the busy state changed. It has to be called after every state change, but the state can have
     * changed already due to multithreading. If the state changes to busy, change the UI synchronously. If the state
     * changes to normal, just start the timer and return immediately. Can be called from any thread.
     */
    private fun busyStateChanged() {
        if (busyCounter <= 0) {
            // asynchronous switch to normal
            switchUIToNormalTimer.start()
            return
        }

        // synchronous switch to busy
        try {
            val runnable = Runnable {
                synchronized(this) {
                    if (busyCounter > 0 && !uiBusy) {
                        LOG.trace("switch UI to busy")
                        uiBusy = true
                        switchUIToNormalTimer.stop()
                        (rootPaneContainer as Component).cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
                        rootPaneContainer.glassPane.isVisible = true
                        if (stateBar != null && showStateBar) {
                            stateBar.netState = StateBar.NetState.NET_ACTIVE
                        }
                    }
                }
            }
            if (SwingUtilities.isEventDispatchThread()) {
                runnable.run()
            } else {
                UiUtil.invokeAndWaitThrowing(runnable)
            }
        } catch (e: Throwable) {
            LOG.error("Exception in changing busy state: ${e.message}")
            throw RuntimeException(e)
        }
    }
}