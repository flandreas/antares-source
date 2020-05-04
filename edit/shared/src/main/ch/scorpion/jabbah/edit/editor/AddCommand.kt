package ch.scorpion.jabbah.edit.editor

import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.io.StorableCloner

/**
 * A [Command] for adding a [Component] to a [Drawing].
 */
class AddCommand(
    private val drawingView: DrawingView<Drawing<in Component>>,
    val component: Component
) : AbstractCommand("edit.command.add", null), Undoable {

    constructor(editor: Editor, component: Component): this(editor.view, component)

	var addedComponentId: Int = 0
		private set

    override fun execute() {
	    val clone = StorableCloner.clone(component)
        drawingView.drawing.add(clone)
	    addedComponentId = clone.id
    }

	override fun undo() {
		drawingView.drawing.remove(drawingView.drawing.getWithId(addedComponentId) as Component)
	}
}