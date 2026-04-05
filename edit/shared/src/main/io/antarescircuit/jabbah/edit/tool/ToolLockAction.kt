package io.antarescircuit.jabbah.edit.tool

import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.edit.Editor

/**
 * An [Action] for toggling the [Editor.toolLock] property.
 */
class ToolLockAction(
    private val editor: Editor
) : AbstractAction("edit.action.toolLock", imagePath = "/img/lock-24.png") {

    init {
        selected = editor.toolLock
    }

    override fun execute(event: ActionEvent) {
        editor.toolLock = !editor.toolLock
    }
}