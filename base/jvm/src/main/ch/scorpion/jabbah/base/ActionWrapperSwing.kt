package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import java.awt.event.ActionEvent
import javax.swing.KeyStroke

/**
 * Wraps the platform-neutral [Action] in a JVM Swing [Action].
 * Create an instance of this class for every [Action] to be wrapped and use it when creating menu items
 * or buttons. No need for subclassing.
 */
class ActionWrapperSwing(private val action: ch.scorpion.jabbah.base.Action) : javax.swing.AbstractAction() {

	init {
		update()
		action.addPropertyChangeListener(object : PropertyChangeListener<Any> {
			override fun propertyChanged(e: PropertyChangeEvent<Any>) {
				when (e.name) {
					Action.PROP_NAME -> putValue(javax.swing.Action.NAME, e.newValue)
					Action.PROP_DESCRIPTION -> putValue(javax.swing.Action.SHORT_DESCRIPTION, e.newValue)
					Action.PROP_ACCELERATOR -> if (e.newValue != null) putValue(javax.swing.Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(e.newValue as String))
					Action.PROP_ENABLED -> isEnabled = e.newValue as Boolean
					Action.PROP_SELECTED -> putValue(javax.swing.Action.SELECTED_KEY, e.newValue)
				}
			}
		})
	}

	private fun update() {
		putValue(javax.swing.Action.NAME, action.name)
		putValue(javax.swing.Action.SHORT_DESCRIPTION, action.description)
		if (action.accelerator != null) {
			putValue(javax.swing.Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(action.accelerator))
		}
		isEnabled = action.enabled
		putValue(javax.swing.Action.SELECTED_KEY, action.selected)
	}

	override fun actionPerformed(e: ActionEvent?) {
		action.execute(ch.scorpion.jabbah.base.event.ActionEvent(
			event = e,
			source = e!!.source,
			modifiers = e.modifiers,
			action = e.actionCommand ?: "",
			time = e.`when`
		))
	}
}