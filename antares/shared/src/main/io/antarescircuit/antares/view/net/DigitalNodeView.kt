package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.model.net.DigitalNet
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.graph.model.Net
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.net.netview.NetViewStyle
import io.antarescircuit.jabbah.graph.view.net.node.NodeViewImpl


/** Overwritten to draw in signal-specific color. */
class DigitalNodeView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	net: Net<DigitalSignal> = DigitalNet(),
	netViewStyle: NetViewStyle? = null
) : NodeViewImpl<DigitalSignal>(styleProvider, net, netViewStyle) {

	override fun draw(context: DrawContext) {
		val oldColor = context.g.color
		val oldCompositeColor = context.color

		GraphViewModule.getTypedNetViewElementColorProvider<DigitalSignal>().setColor(context, this)

		super.draw(context)

		context.color = oldCompositeColor
		context.g.color = oldColor
	}
}
