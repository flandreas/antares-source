package ch.scorpion.jabbah.edit.editor

import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.app.ComponentCustomizer
import ch.scorpion.jabbah.edit.command.AbstractDrawingViewCommand

/**
 * A [Command] for adding a [Component] to a [Drawing].
 */
class AddCommand(
    drawingView: DrawingView<Drawing<in Component>>,
    val component: Component,
    private val componentCustomizer: ComponentCustomizer? = null
) : AbstractDrawingViewCommand("edit.command.add", drawingView), Undoable {

    constructor(editor: Editor, component: Component): this(editor.view, component)

	var addedComponentId: Int = 0
		private set

	override fun getDetailedDescription(): String =
		"${super.getDetailedDescription()} ${component::class.simpleName} $addedComponentId"

    override fun execute() {
	    val clone = component.doClone()
		(view as DrawingView<Drawing<in Component>>).drawing.add(clone)
	    componentCustomizer?.customizeAddedComponent(clone, view.drawing)
	    addedComponentId = clone.id
    }

	override fun undo() {
		view.drawing.remove(view.drawing.getWithId(addedComponentId) as Component)
	}
}