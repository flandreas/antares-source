package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Tool

/** An [Action] for selecting an [Editor]'s current [Tool].*/
class ToolAction(
	baseName: String,
	private val tool: Tool,
	private val editor: Editor,
	imagePath: String?
) : AbstractAction(baseName) {

	private val editorListener = EditorListener()

	init {
		selected = tool == editor.currentTool
		this.imagePath = imagePath
		editor.addPropertyChangeListener(editorListener)
	}

	override fun execute(event: ActionEvent) {
		editor.currentTool = tool
	}

	override fun dispose() {
		editor.removePropertyChangeListener(editorListener)
	}

	private inner class EditorListener : PropertyChangeListener<Any> {
		override fun propertyChanged(e: PropertyChangeEvent<Any>) {
			if (e.name == Editor.PROP_CURRENT_TOOL) {
				selected = editor.currentTool == tool
			}
		}
	}
}