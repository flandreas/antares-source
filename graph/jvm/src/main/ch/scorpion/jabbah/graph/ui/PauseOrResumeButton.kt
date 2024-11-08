package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.execution.PROP_PAUSE_OR_RESUME_ACTION_IN_BREAKPOINT
import ch.scorpion.jabbah.execution.PauseOrResumeAction
import javax.swing.JToggleButton

/**
 * A [JToggleButton] for [PauseOrResumeAction].
 * Uses a different icon depending on [PauseOrResumeAction.inBreakpoint].
 */
class PauseOrResumeButton(
    private val action: PauseOrResumeAction
) : JToggleButton(ActionWrapperSwing(action)) {

    companion object {
        private const val INACTIVE_ICON = "/img/pause24.png"
        private const val ACTIVE_ICON = "/img/pause-active24.png"
    }

    private val inBreakpointListener = PropertyChangeListener<Any> { e ->
        if (e.name == PROP_PAUSE_OR_RESUME_ACTION_IN_BREAKPOINT) {
            action.imagePath = if (action.inBreakpoint) {
                ACTIVE_ICON
            } else {
                INACTIVE_ICON
            }
        }
    }

    init {
        text = null
        action.imagePath = INACTIVE_ICON
        action.addPropertyChangeListener(inBreakpointListener)
    }

    fun dispose() {
        action.removePropertyChangeListener(inBreakpointListener)
        (super.getAction() as ActionWrapperSwing).dispose()
    }
}