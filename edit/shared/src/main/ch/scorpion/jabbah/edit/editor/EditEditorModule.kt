package ch.scorpion.jabbah.edit.editor

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.drag.DragManagerImpl
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.select.EditSelectModule

/**
 * Module definitions for the [ch.scorpion.jabbah.edit.editor] module.
 */
object EditEditorModule : AbstractModule() {

	var dragManagerFactory: DragManagerFactory = { editor -> DragManagerImpl(editor) }

    override fun initialize() { }

    override fun resetDependencies() { }

    @Suppress("unused")
    fun createEditor(name: String, view: DrawingView<Drawing<Component>>): Editor =
	    EditorImpl(view, EditModule.commandManager, EditSelectModule.selectionToolFactory, name)
}