package ch.scorpion.jabbah.edit.editor

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.select.EditSelectModule

/**
 * Module definitions for the [ch.scorpion.jabbah.edit.editor] module.
 */
object EditEditorModule : AbstractModule() {

	var dragManagerFactory: DragManagerFactory = { editor -> DragManagerImpl(editor) }

    override fun initialize() {
        // empty
    }

    @Suppress("unused")
    fun createEditor(view: DrawingView<Drawing<Component>>): Editor =
	    EditorImpl(view, EditModule.commandManager, EditSelectModule.selectionToolFactory)
}