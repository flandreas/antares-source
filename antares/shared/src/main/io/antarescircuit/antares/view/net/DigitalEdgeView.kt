package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.model.net.DigitalNet
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.net.tunnel.TunnelView
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.Stroke
import io.antarescircuit.jabbah.draw.module.DrawModule
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewImpl
import io.antarescircuit.jabbah.graph.view.style.EdgeStyle


/**
 * Extends [EdgeViewImpl] in order to apply a digital-specific [EdgeView] rendering, i.e. colors that show the
 * value of the [DigitalSignal], and painting large [BitWidth]s with wider strokes.
 */
class DigitalEdgeView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	net: DigitalNet = DigitalNet()
) : EdgeViewImpl<DigitalSignal>(styleProvider, net) {

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