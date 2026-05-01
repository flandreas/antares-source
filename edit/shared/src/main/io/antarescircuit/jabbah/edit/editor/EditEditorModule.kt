package io.antarescircuit.jabbah.edit.editor

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.edit.drag.DragManagerImpl
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.edit.select.EditSelectModule

/**
 * Module definitions for the [io.antarescircuit.jabbah.edit.editor] module.
 */
object EditEditorModule : AbstractModule() {

	var dragManagerFactory: DragManagerFactory = { editor -> DragManagerImpl(editor) }

    override fun initialize() { }

    override fun resetDependencies() { }

    @Suppress("unused")
    fun createEditor(name: String, view: DrawingView<Component, Drawing<Component>>): Editor =
	    EditorImpl(view, EditModule.commandManager, EditSelectModule.selectionToolFactory, name)
}