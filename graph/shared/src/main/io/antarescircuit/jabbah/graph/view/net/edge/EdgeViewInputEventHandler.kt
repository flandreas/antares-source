package io.antarescircuit.jabbah.graph.view.net.edge

import io.antarescircuit.jabbah.draw.InputEventHandlerAdapter
import io.antarescircuit.jabbah.draw.InputEventHandler
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.EditInputEventContext
import io.antarescircuit.jabbah.edit.select.Handle
import io.antarescircuit.jabbah.graph.view.EdgeView

/**
 * Base class for implementing [InputEventHandler]s for [EdgeView]s depending on their [LayoutType].
 */
open class EdgeViewInputEventHandler(
	var edgeView: EdgeView<*>? = null
) : InputEventHandlerAdapter<EditInputEventContext>() {

	/**
	 * Notifies this [EdgeViewInputEventHandler] that it is currently not used any more.
	 * Implementations should cleanup and remove anything from the [DrawingView] they have
	 * temporarily added, such as [Handles][Handle].
	 */
	open fun dismiss(view: DrawingView<*>) {}
}