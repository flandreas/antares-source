package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Grid

/**
 * An [Action] for toggling the [Grid] snap functionality for the given [Editor].
 */
class GridSnapAction(private val editor: Editor) : AbstractAction("edit.action.grid.snap") {

	init {
		updateState()
		editor.addPropertyChangeListener(object : PropertyChangeListener<Any> {
			override fun propertyChanged(e: PropertyChangeEvent<Any>) {
				if (e.name == Editor.PROP_GRID_SNAP) {
					updateState()
				}
			}
		})
	}

	override fun execute(event: ActionEvent) {
		editor.gridSnap = !editor.gridSnap
	}

	private fun updateState() {
		selected = editor.gridSnap
	}
}