package ch.scorpion.jabbah.edit

/**
 * A [Tool] for selecting [Component]s in a [Drawing] and for moving selected [Component]s around.
 *
 * A [SelectionTool] (like every [Tool]) is owned and controlled by an [Editor], which calls this [Tool]'s
 * event handling methods whenever it is the current [Tool] of the [Editor]. A [SelectionTool] is responsible
 * for creating and registering appropriate [Command]s when moving [Component]s.
 *
 * A [SelectionTool] posts [ContextActionRequest]s when user clicks the right mouse button on selected
 * [Component]s.
 */
interface SelectionTool : Tool {
    // empty
}

/** A factory that creates a [SelectionTool] for an [Editor].*/
interface SelectionToolFactory {
    fun create(editor: Editor): SelectionTool
}

/**
 * Posted by [SelectionTool] on [EventBus] to request the UI layer to display context actions
 * for the currently selected [Component]s.
 */
data class ContextActionRequest(val editor: Editor)