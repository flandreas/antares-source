package ch.scorpion.jabbah.base.swing

import javax.swing.event.AncestorEvent
import javax.swing.event.AncestorListener

/**
 * Convenience class to request focus on a component used in a [javax.swing.JDialog]
 * such as [javax.swing.JOptionPane].
 */
class RequestFocusListener(
    private val removeListener: Boolean = true
) : AncestorListener {

    override fun ancestorAdded(event: AncestorEvent?) {
        val component = event?.component ?: return
        component.requestFocusInWindow()
        if (removeListener) {
            component.removeAncestorListener(this)
        }
    }

    override fun ancestorRemoved(event: AncestorEvent?) {}

    override fun ancestorMoved(event: AncestorEvent?) {}
}