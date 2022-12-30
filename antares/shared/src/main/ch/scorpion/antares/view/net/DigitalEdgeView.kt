package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.DigitalNet
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.connect.DragEdgeViewDestinationConnector
import ch.scorpion.jabbah.graph.view.connect.DragEdgeViewOriginConnector
import ch.scorpion.jabbah.graph.view.connect.EdgeToPortConnector
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
	edgeToPortConnectorSupplier: () -> EdgeToPortConnector = DEF_EDGE_TO_PORT_CONNECTOR_SUPPLIER,
	origEndpointConnectorSupplier: () -> DragEdgeViewOriginConnector = DEF_ORIG_ENDPOINT_CONNECTOR_SUPPLIER,
	destEndpointConnectorSupplier: () -> DragEdgeViewDestinationConnector = DEF_DEST_ENDPOINT_CONNECTOR_SUPPLIER,
	net: DigitalNet = DigitalNet()
) : EdgeViewImpl<DigitalSignal>(
	styleProvider,
	edgeToPortConnectorSupplier,
	origEndpointConnectorSupplier,
	destEndpointConnectorSupplier,
	net
) {

	companion object {

		/** The name of the [Boolean] property in [Properties] defining whether [DigitalEdgeView] with wide [BitWidth] should use a wider stroke. */
		const val PROP_WIDE_BUS_STROKE = "antares.DigitalEdgeView.wideBusStroke"
	}

	private val wideBus: Boolean get() = (model as DigitalNet).bitWidth.width > 1 && BaseModule.properties.getBoolean(PROP_WIDE_BUS_STROKE)

	override val executionStroke: Stroke get() =
		if (wideBus) {
			(style as EdgeStyle).busStroke
		} else {
			(style as EdgeStyle).executionStroke
		}

	override fun draw(context: DrawContext) {
		val oldColor = context.g.color
		val oldCompositeColor = context.color
		val graphAppContext = context.castedAppContext<GraphApplicationContext>()!!

		context.color = if (graphAppContext.showNetState) {
			if (model.isError) {
				Themes.get<AntaresTheme>().error
			} else {
				val signalColor = model.signal!!.color
				if (styling.isArea) {
					CompositeColor(signalColor.foregroundColor, Themes.get<AntaresTheme>().word.backgroundColor)
				} else {
					signalColor
				}
			}
		} else {
			context.choose(color)
		}

		context.g.stroke = if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			executionStroke
		} else {
			if (wideBus) {
				(style as EdgeStyle).busStroke
			} else {
				stroke
			}
		}

		super.draw(context)

		context.color = oldCompositeColor
		context.g.color = oldColor
	}

	override fun collectSelectBuddies(drawing: Drawing<Component>, buddies: MutableSet<Component>) {
		super.collectSelectBuddies(drawing, buddies)

		netView?.collectConnectedVerticeViews(TunnelView::class, buddies)
		buddies
			.filterIsInstance<TunnelView>()
			.forEach { it.collectSelectBuddies(drawing, buddies) }
	}
}