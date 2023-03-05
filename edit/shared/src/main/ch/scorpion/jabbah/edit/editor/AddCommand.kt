package ch.scorpion.jabbah.edit.editor

import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.command.AbstractCommand

/**
 * A [Command] for adding a [Component] to a [Drawing].
 */
class AddCommand(
    private val drawingView: DrawingView<Drawing<in Component>>,
    val component: Component,
    private val componentCustomizer: (Component, Drawing<*>) -> Unit = { _,_ -> }
) : AbstractCommand("edit.command.add", null), Undoable {

    constructor(editor: Editor, component: Component): this(editor.view, component)

	var addedComponentId: Int = 0
		private set

    override fun execute() {
	    val clone = component.doClone()
        drawingView.drawing.add(clone)
	    componentCustomizer.invoke(clone, drawingView.drawing)
	    addedComponentId = clone.id
    }

	override fun undo() {
		drawingView.drawing.remove(drawingView.drawing.getWithId(addedComponentId) as Component)
	}
}