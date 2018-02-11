package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.snap.ComponentSnapper

/**
 * An [Action] for toggling the [ComponentSnapper] functionality for the given [Editor].
 */
class ComponentSnapAction(private val editor: Editor) : AbstractAction("edit.tool.align") {

	init {
		updateState()
		editor.addPropertyChangeListener(object : PropertyChangeListener<Any> {
			override fun propertyChanged(e: PropertyChangeEvent<Any>) {
				if (e.name == Editor.PROP_COMPONENT_SNAP) {
					updateState()
				}
			}
		})
	}

	override fun execute(event: ActionEvent) {
		editor.componentSnap = !editor.componentSnap
	}

	private fun updateState() {
		selected = editor.componentSnap
	}
}