package io.antarescircuit.jabbah.edit.editor

import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.edit.app.ComponentCustomizer
import io.antarescircuit.jabbah.edit.command.AbstractDrawingViewCommand

/**
 * A [Command] for adding a [Component] to a [Drawing].
 */
class AddCommand(
    drawingView: DrawingView<Component, Drawing<in Component>>,
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
		@Suppress("UNCHECKED_CAST") // DrawingView type ensured by constructor
		(view as DrawingView<Component, Drawing<in Component>>).drawing.add(clone)

		componentCustomizer?.customizeAddedComponent(clone, view.drawing)
	    addedComponentId = clone.id
    }

	override fun undo() {
		view.drawing.remove(view.drawing.getWithId(addedComponentId) as Component)
	}
}