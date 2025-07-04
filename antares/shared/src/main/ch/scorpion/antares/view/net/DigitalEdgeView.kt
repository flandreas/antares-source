package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.DigitalNet
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.net.tunnel.TunnelView
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.connect.DragEdgeViewDestinationConnector
import ch.scorpion.jabbah.graph.view.connect.DragEdgeViewOriginConnector
import ch.scorpion.jabbah.graph.view.connect.EdgeToPortOrEdgeConnector
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewImpl
import ch.scorpion.jabbah.graph.view.style.EdgeStyle


/**
 * Extends [EdgeViewImpl] in order to apply a digital-specific [EdgeView] rendering, i.e. colors that show the
 * value of the [DigitalSignal], and painting large [BitWidth]s with wider strokes.
 */
class DigitalEdgeView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	edgeToPortOrEdgeConnectorSupplier: () -> EdgeToPortOrEdgeConnector = DEF_EDGE_TO_PORT_CONNECTOR_SUPPLIER,
	origEndpointConnectorSupplier: () -> DragEdgeViewOriginConnector = DEF_ORIG_ENDPOINT_CONNECTOR_SUPPLIER,
	destEndpointConnectorSupplier: () -> DragEdgeViewDestinationConnector = DEF_DEST_ENDPOINT_CONNECTOR_SUPPLIER,
	net: DigitalNet = DigitalNet()
) : EdgeViewImpl<DigitalSignal>(
	styleProvider,
	edgeToPortOrEdgeConnectorSupplier,
	origEndpointConnectorSupplier,
	destEndpointConnectorSupplier,
	net
) {

	companion object {

		/** The name of the [Boolean] property in [Properties] defining whether [DigitalEdgeView] with wide [BitWidth] should use a wider stroke. */
		const val PROP_WIDE_BUS_STROKE = "antares.DigitalEdgeView.wideBusStroke"

		fun getStroke(style: EdgeStyle, bitWidth: BitWidth, isExecution: Boolean): Stroke =
			if (isExecution) {
				getExecutionStroke(style, bitWidth)
			} else if (isWideBus(bitWidth)) {
				style.busStroke
			} else {
				style.stroke
			}

		private fun isWideBus(bitWidth: BitWidth): Boolean =
			bitWidth.width > 1 && BaseModule.properties.getBoolean(PROP_WIDE_BUS_STROKE)

		private fun getExecutionStroke(style: EdgeStyle, bitWidth: BitWidth): Stroke {
			return if (isWideBus(bitWidth)) {
				style.busStroke
			} else {
				style.executionStroke
			}
		}
	}

	override fun getEffectiveStroke(isExecution: Boolean): Stroke =
        getStroke(style as EdgeStyle, (model as DigitalNet).bitWidth, isExecution)

	override val executionStroke: Stroke get() =
		getExecutionStroke(style as EdgeStyle, (model as DigitalNet).bitWidth)

	override fun draw(context: DrawContext) {
		val oldColor = context.g.color
		val oldCompositeColor = context.color
		val graphAppContext = context.castedAppContext<GraphApplicationContext>()!!

		GraphViewModule.getTypedNetViewElementColorProvider<DigitalSignal>().setColor(context, this)

		context.g.stroke = getEffectiveStroke(context.castedAppContext<GraphApplicationContext>()!!.isExecute)

		super.draw(context)

		if (DrawModule.debugGfx && !graphAppContext.showNetState) {
			drawBeginConnectionAnnotation(context)
		}

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