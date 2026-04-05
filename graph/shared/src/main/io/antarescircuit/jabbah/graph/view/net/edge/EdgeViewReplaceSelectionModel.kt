package io.antarescircuit.jabbah.graph.view.net.edge

import io.antarescircuit.jabbah.draw.InputEventContext
import io.antarescircuit.jabbah.draw.InputEventHandler
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.SelectionModel
import io.antarescircuit.jabbah.edit.select.SelectedColorSelectionModel
import io.antarescircuit.jabbah.graph.view.EdgeView

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