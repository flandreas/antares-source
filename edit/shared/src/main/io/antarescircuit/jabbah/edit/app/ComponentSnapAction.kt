package io.antarescircuit.jabbah.edit.app

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.PropertyChangeEvent
import io.antarescircuit.jabbah.base.event.PropertyChangeListener
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.snap.ComponentSnapper

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
				} else if (e.name == Editor.PROP_ACTIVE) {
					enabled = editor.active
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