package io.antarescircuit.jabbah.edit.app

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.PropertyChangeEvent
import io.antarescircuit.jabbah.base.event.PropertyChangeListener
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.Grid

/**
 * An [Action] for toggling the [Grid] snap functionality for the given [Editor].
 */
class GridSnapAction(private val editor: Editor) : AbstractAction("edit.action.grid.snap") {

	companion object {
		private val LOG by logger(GridSnapAction::class)
	}

	private val propertyChangeHandler = EditorPropertyListener()

	init {
		updateState()
		editor.addPropertyChangeListener(propertyChangeHandler)
	}

	override fun dispose() {
		super.dispose()
		editor.removePropertyChangeListener(propertyChangeHandler)
	}

	override fun execute(event: ActionEvent) {
		editor.gridSnap = !editor.gridSnap
		LOG.userTrail("Grid snap set to ${editor.gridSnap}")
	}

	private fun updateState() {
		selected = editor.gridSnap
	}

	private inner class EditorPropertyListener : PropertyChangeListener<Any> {
		override fun propertyChanged(e: PropertyChangeEvent<Any>) {
			if (e.name == Editor.PROP_GRID_SNAP) {
				updateState()
			} else if (e.name == Editor.PROP_ACTIVE) {
				enabled = editor.active
			}
		}
	}
}