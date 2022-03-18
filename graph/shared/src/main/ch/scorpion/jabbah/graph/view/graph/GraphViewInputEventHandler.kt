package ch.scorpion.jabbah.graph.view.graph

import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.container.DrawableBagInputEventHandler
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.connect.*
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

/**
 * Intercepts [MouseEvent][MouseEvent] on [VerticeView] in order to forward them to the injected connectors.
 *
 * This relieves [VerticeView] implementations from the burden of providing constructor injection parameters
 * for all kinds of connectors and possibly lots of other objects to inject.
 */
class GraphViewInputEventHandler<T : GraphElementView<*>>(
	private val dslOutputToInputOrEdgeConnector: OutputToInputOrEdgeConnector = GraphViewModule.outputToInputOrEdgeConnector,
	private val dslInputToOutputOrEdgeConnector: InputToOutputOrEdgeConnector = GraphViewModule.inputToOutputOrEdgeConnector,
	private val reconnectOriginConnector: ReconnectOriginConnector = GraphViewModule.reconnectOriginConnector,
	private val reconnectDestinationConnector: ReconnectDestinationConnector = GraphViewModule.reconnectDestinationConnector
) : DrawableBagInputEventHandler<T, EditInputEventContext>() {

	fun graphElementViewRemoved(graphElementView: GraphElementView<*>) {
		ConnectionPointHighlighter.removePortViewHighlight()
	}

	override fun handlerOfDrawable(drawable: Drawable, context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
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