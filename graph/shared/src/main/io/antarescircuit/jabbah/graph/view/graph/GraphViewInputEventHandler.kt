package io.antarescircuit.jabbah.graph.view.graph

import io.antarescircuit.jabbah.base.event.MouseEvent
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.InputEventHandler
import io.antarescircuit.jabbah.draw.container.DrawableBagInputEventHandler
import io.antarescircuit.jabbah.edit.EditInputEventContext
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.graph.view.connect.*
import io.antarescircuit.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule

/**
 * Intercepts [MouseEvent][MouseEvent] on [VerticeView] in order to forward them to the injected connectors.
 *
 * This reliefs [VerticeView] implementations from the burden of providing constructor injection parameters
 * for all kinds of connectors and possibly lots of other objects to inject.
 */
class GraphViewInputEventHandler<T : GraphElementView<*>>(
	private val dslOutputToInputOrEdgeConnector: OutputToInputOrEdgeConnector = GraphViewModule.outputToInputOrEdgeConnector,
	private val dslInputToOutputOrEdgeConnector: InputToOutputOrEdgeConnector = GraphViewModule.inputToOutputOrEdgeConnector,
	private val reconnectOriginConnector: ReconnectOriginConnector = GraphViewModule.reconnectOriginConnector,
	private val reconnectDestinationConnector: ReconnectDestinationConnector = GraphViewModule.reconnectDestinationConnector
) : DrawableBagInputEventHandler<T, EditInputEventContext>() {

	fun graphElementViewRemoved() {
		ConnectionPointHighlighter.removePortViewHighlight()
	}

	override fun handlerOfDrawable(drawable: Drawable, context: EditInputEventContext): InputEventHandler<EditInputEventContext> {

		if (!context.view.canvas.hasFocus) {
			// Bug #1082: Connectors can lead to errors when requesting focus first leads to loosing focus by another component.
			// If the other component is e.g. a property editor, an interfering state change might occur.
			return super.handlerOfDrawable(drawable, context)
		}

		if (drawable is VerticeView<*>) {
			drawable.getPortViewAtConnectionPoint(context.x, context.y)?.let { portView ->
				if (portView.port.isConnected && context.mouseEvent?.isAltDown == true) {
					val edgeView = (drawableBag as GraphView).getEdgeView(portView.port)
					if (edgeView?.origin?.portView === portView) {
						reconnectOriginConnector.useFor(edgeView, context)
						return reconnectOriginConnector.handler
					} else if (edgeView?.destination?.portView === portView) {
						reconnectDestinationConnector.useFor(edgeView, context)
						return reconnectDestinationConnector.handler
					}
				} else {
					if (portView.port.portType.isOutput) {
						dslOutputToInputOrEdgeConnector.useFor(drawable, context)
						return dslOutputToInputOrEdgeConnector.handler
					} else if (portView.port.portType.isInput) {
						dslInputToOutputOrEdgeConnector.useFor(drawable, context)
						return dslInputToOutputOrEdgeConnector.handler
					}
				}
			}
		}

		return super.handlerOfDrawable(drawable, context)
	}
}