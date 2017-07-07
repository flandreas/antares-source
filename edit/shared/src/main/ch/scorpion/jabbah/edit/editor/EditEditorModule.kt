package ch.scorpion.jabbah.edit.editor

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.CommandManagerImpl
import ch.scorpion.jabbah.edit.select.EditSelectModule

/**
 * Module definitions for the [ch.scorpion.jabbah.edit.editor] module.
 */
object EditEditorModule : AbstractModule() {

    override fun initialize() {
        // empty
    }

    @Suppress("unused")
    fun createEditor(view: DrawingView<Drawing<Component>>): Editor {
        return EditorImpl(view, CommandManagerImpl(), EditSelectModule.selectionToolFactory)
    }
}