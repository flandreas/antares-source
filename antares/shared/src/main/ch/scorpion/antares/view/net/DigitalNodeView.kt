package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.DigitalNet
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle
import ch.scorpion.jabbah.graph.view.net.node.NodeViewImpl


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
