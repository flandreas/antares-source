package ch.scorpion.jabbah.base.event

import java.awt.event.ActionEvent

/**
 * An [ActionListener] adapter for the JVM.
 */
class ActionListenerJvm(val handler: (ch.scorpion.jabbah.base.event.ActionEvent) -> Unit) : java.awt.event.ActionListener {

    /** ---- [java.awt.event.ActionListener] interface */

    override fun actionPerformed(e: ActionEvent?) {
        if (e != null) {
            handler(ActionEvent(e, e.source, e.modifiers, e.actionCommand ?: "", e.`when`))
        }
    }
}