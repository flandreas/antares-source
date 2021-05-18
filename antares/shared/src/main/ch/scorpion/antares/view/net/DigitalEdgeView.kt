package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.DigitalNet
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.NetView
import ch.scorpion.jabbah.graph.view.connect.DragEdgeViewDestinationConnector
import ch.scorpion.jabbah.graph.view.connect.DragEdgeViewOriginConnector
import ch.scorpion.jabbah.graph.view.connect.EdgeToPortConnector
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewFactory
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewImpl
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.style.EdgeStyle


/**
 * Extends [EdgeViewImpl] in order to apply a digital-specific [EdgeView] rendering, i.e. colors that show the
 * value of the [DigitalSignal], and painting large [BitWidth]s with wider strokes.
 *
 * TODO Refactor: Similar code for determining color as in [DigitalNodeView]. Why subclassing?
 * Try to inject the aspect of varying color and stroke into [EdgeView] and [NodeView].
 */
class DigitalEdgeView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	edgeToPortConnectorSupplier: () -> EdgeToPortConnector = { GraphViewModule.edgeToPortConnector },
	origEndpointConnectorSupplier: () -> DragEdgeViewOriginConnector = { GraphViewModule.dragEdgeViewOriginConnector },
	destEndpointConnectorSupplier: () -> DragEdgeViewDestinationConnector = { GraphViewModule.dragEdgeViewDestinationConnector },
	currentSystemSpeedCategory: CurrentSystemSpeedCategory = ExecutionModule.currentSystemSpeedCategory,
	net: Net<DigitalSignal> = DigitalNet()
) : EdgeViewImpl<DigitalSignal>(
	styleProvider,
	edgeToPortConnectorSupplier,
	origEndpointConnectorSupplier,
	destEndpointConnectorSupplier,
	currentSystemSpeedCategory,
	net
) {

	override fun draw(context: DrawContext) {
		val oldColor = context.g.color
		val oldCompositeColor = context.color
		val graphAppContext = context.castedAppContext<GraphApplicationContext>()!!

		context.color = if (graphAppContext.isExecute && showNetState(graphAppContext.isPausing)) {
			if (model.isError) {
				Themes.get<AntaresTheme>().error
			} else {
				val signalColor = model.signal!!.getColor()
				if (styling.isArea) {
					CompositeColor(signalColor.foregroundColor, Themes.get<AntaresTheme>().word.backgroundColor)
				} else {
					signalColor
				}
			}
		} else {
			context.choose(color)
		}

		if ((model as DigitalNet).bitWidth.width > 1) {
			context.g.stroke = (style as EdgeStyle).busStroke
		} else {
			context.g.stroke = if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
				(style as EdgeStyle).executionStroke
			} else {
				stroke
			}
		}

		super.draw(context)

		context.color = oldCompositeColor
		context.g.color = oldColor
	}
}

class DigitalEdgeViewFactory(
	private val styleProvider: StyleProvider,
	private val edgeToPortConnectorSupplier: () -> EdgeToPortConnector,
	private val origEndpointConnectorSupplier: () -> DragEdgeViewOriginConnector,
	private val destEndpointConnectorSupplier: () -> DragEdgeViewDestinationConnector,
	private val currentSystemSpeedCategory: CurrentSystemSpeedCategory
) : EdgeViewFactory<DigitalSignal> {

	override fun createEdgeView(): EdgeView<DigitalSignal> {
		return DigitalEdgeView(styleProvider, edgeToPortConnectorSupplier, origEndpointConnectorSupplier,
			destEndpointConnectorSupplier, currentSystemSpeedCategory)
	}

	override fun createEdgeView(netView: NetView<DigitalSignal>): EdgeView<DigitalSignal> {
		return DigitalEdgeView(styleProvider, edgeToPortConnectorSupplier, origEndpointConnectorSupplier,
			destEndpointConnectorSupplier, currentSystemSpeedCategory, netView.net)
	}
}