package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.SelectionModel
import ch.scorpion.jabbah.edit.select.SelectedColorSelectionModel
import ch.scorpion.jabbah.graph.view.EdgeView

/**
 * A [SelectionModel] that renders a selected [EdgeView] in the selection color and provides an
 * [InputEventHandler] depending of the [EdgeView]'s [Layout].
 */
class EdgeViewReplaceSelectionModel<T: EdgeView<*>>(component: T) : SelectedColorSelectionModel<T>(component) {

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
		return component.layout.type.getInputEventHandler(component, context)
	}

	override fun notifyRemoved(view: DrawingView<*>) {
		component.layout.type.edgeViewInputEventHandler.dismiss(view)
	}
}