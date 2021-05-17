package ch.scorpion.jabbah.graph.view.graph

import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.container.DrawableContainerInputEventHandler
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.connect.InputToOutputOrEdgeConnector
import ch.scorpion.jabbah.graph.view.connect.OutputToInputConnector
import ch.scorpion.jabbah.graph.view.connect.ReconnectDestinationConnector
import ch.scorpion.jabbah.graph.view.connect.ReconnectOriginConnector
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

/**
 * Intercepts [MouseEvent][MouseEvent] on [VerticeView] in order to forward them to the injected connectors.
 *
 * This relieves [VerticeView] implementations from the burden of providing constructor injection parameters
 * for all kinds of connectors and possibly lots of other objects to inject.
 */
class GraphViewInputEventHandler<T : GraphElementView<*>>(
	private val dslOutputToInputConnector: OutputToInputConnector = GraphViewModule.outputToInputConnector,
	private val dslInputToOutputOrEdgeConnector: InputToOutputOrEdgeConnector = GraphViewModule.inputToOutputOrEdgeConnector,
	private val reconnectOriginConnector: ReconnectOriginConnector = GraphViewModule.reconnectOriginConnector,
	private val reconnectDestinationConnector: ReconnectDestinationConnector = GraphViewModule.reconnectDestinationConnector
) : DrawableContainerInputEventHandler<T, EditInputEventContext>() {

	companion object {
		private val LOG by logger(GraphViewInputEventHandler::class)
	}

	override fun handlerOfDrawable(drawable: Drawable, context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
		if (drawable is VerticeView<*>) {
			val portView = drawable.getPortViewAtConnectionPoint(context.x, context.y)
			if (portView != null && portView.connectable) {
				if (portView.port.portType.isOutput) {
					return if (portView.port.isConnected && context.mouseEvent?.isAltDown == true) {
						LOG.debug("delegating mouseMoved to ReconnectOriginConnector")
						reconnectOriginConnector.useFor((container as GraphView).getEdgeView(portView.port)!!, context)
						reconnectOriginConnector.handler
					} else {
						LOG.debug("delegating mouseMoved to OutputToInputConnector")
						dslOutputToInputConnector.useFor(drawable, context)
						dslOutputToInputConnector.handler
					}
				} else if (portView.port.portType.isInput) {
					return if (portView.port.isConnected && context.mouseEvent?.isAltDown == true) {
						LOG.debug("delegating mouseMoved to ReconnectDestinationConnector")
						reconnectDestinationConnector.useFor((container as GraphView).getEdgeView(portView.port)!!, context)
						reconnectDestinationConnector.handler
					} else {
						LOG.debug("delegating mouseMoved to InputToOutputOrEdgeConnector")
						dslInputToOutputOrEdgeConnector.useFor(drawable, context)
						dslInputToOutputOrEdgeConnector.handler
					}
				}
			}
		}

		return super.handlerOfDrawable(drawable, context)
	}
}