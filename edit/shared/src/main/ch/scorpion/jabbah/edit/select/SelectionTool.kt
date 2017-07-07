package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Tool

/**
 * A [Tool] for selecting [Component]s in a [Drawing].
 */
interface SelectionTool : Tool {
    // empty
}

/** A factory that creates a [SelectionTool] for an [Editor].*/
interface SelectionToolFactory {
    fun create(editor: Editor): SelectionTool
}