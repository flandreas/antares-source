package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Tool
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.Command

/**
 * A [Tool] for selecting [Component]s in a [Drawing] and for moving selected [Component]s around.
 * A [SelectionTool] (like every [Tool]) is owned and controlled by an [Editor], which calls this [Tool]'s
 * event handling methods whenever it is the current [Tool] of the [Editor]. A [SelectionTool] is responsible
 * for creating and registering appropriate [Command]s when moving [Component]s.
 */
interface SelectionTool : Tool {
    // empty
}

/** A factory that creates a [SelectionTool] for an [Editor].*/
interface SelectionToolFactory {
    fun create(editor: Editor): SelectionTool
}