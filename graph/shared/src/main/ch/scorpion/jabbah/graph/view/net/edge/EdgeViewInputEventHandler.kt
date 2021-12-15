package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.select.Handle
import ch.scorpion.jabbah.graph.view.EdgeView

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