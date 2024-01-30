package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.swing.UiUtil
import java.awt.event.ActionEvent
import java.awt.event.MouseEvent
import javax.swing.KeyStroke

/**
 * Wraps the platform-neutral [Action] in a JVM Swing [Action].
 * Create an instance of this class for every [Action] to be wrapped and use it when creating menu items
 * or buttons. No need for subclassing.
 */
class ActionWrapperSwing(private val action: Action) : javax.swing.AbstractAction() {

	companion object {
		fun toJabbahActionEvent(e: ActionEvent): ch.scorpion.jabbah.base.event.ActionEvent =
			ch.scorpion.jabbah.base.event.ActionEvent(
				event = e,
				source = e.source,
				modifiers = e.modifiers,
				action = e.actionCommand ?: "",
				time = e.`when`)

		fun toJabbahActionEvent(e: MouseEvent): ch.scorpion.jabbah.base.event.ActionEvent =
			ch.scorpion.jabbah.base.event.ActionEvent(
				event = e,
				source = e.source,
				modifiers = e.modifiersEx,
				action = "",
				time = e.`when`)
	}

	private val actionPropertyListener = PropertyChangeListener<Any> { e ->
		when (e.name) {
			ActionProperty.PROP_NAME -> putValue(javax.swing.Action.NAME, e.newValue)
			ActionProperty.PROP_DESCRIPTION -> putValue(javax.swing.Action.SHORT_DESCRIPTION, e.newValue)
			ActionProperty.PROP_ACCELERATOR -> if (e.newValue != null) putValue(javax.swing.Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(e.newValue as String))
			ActionProperty.PROP_ENABLED -> isEnabled = e.newValue as Boolean
			ActionProperty.PROP_SELECTED -> putValue(javax.swing.Action.SELECTED_KEY, e.newValue)
			ActionProperty.PROP_IMAGE_PATH -> if (e.newValue != null) putValue(javax.swing.Action.LARGE_ICON_KEY, UiUtil.themedIcon(e.newValue as String))
		}
	}

	init {
		update()
		action.addPropertyChangeListener(actionPropertyListener)
		addPropertyChangeListener {
			when (it.propertyName) {
				javax.swing.Action.SELECTED_KEY -> action.selected = it.newValue as Boolean
			}
		}
	}

	fun dispose() {
		action.removePropertyChangeListener(actionPropertyListener)
	}

	private fun update() {
		putValue(javax.swing.Action.NAME, action.name)
		putValue(javax.swing.Action.SHORT_DESCRIPTION, action.description)
		if (action.accelerator != null) {
			putValue(javax.swing.Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(action.accelerator))
		}
		if (action.imagePath != null) {
			putValue(javax.swing.Action.LARGE_ICON_KEY, UiUtil.themedIcon(action.imagePath!!))
		}
		isEnabled = action.enabled
		putValue(javax.swing.Action.SELECTED_KEY, action.selected)
	}

	override fun actionPerformed(e: ActionEvent?) {
		action.execute(toJabbahActionEvent(e!!))
	}
}