package ch.scorpion.jabbah.edit.tool

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.edit.Editor

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