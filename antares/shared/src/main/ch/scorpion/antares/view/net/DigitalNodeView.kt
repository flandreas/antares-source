package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.DigitalNet
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
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.net.node.NodeViewFactory
import ch.scorpion.jabbah.graph.view.net.node.NodeViewImpl


/**
 * Overwritten in order to draw in signal-specific color.
 *
 * TODO Refactor: Similar code for determining color as in [DigitalEdgeView]. Why sublassing?
 * Try to inject the aspect of varying color and stroke into [EdgeView] and [NodeView].
 */
class DigitalNodeView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	net: Net<DigitalSignal> = DigitalNet(),
	netViewStyle: NetViewStyle? = null
) : NodeViewImpl<DigitalSignal>(styleProvider, net, netViewStyle) {

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

		super.draw(context)

		context.color = oldCompositeColor
		context.g.color = oldColor
	}
}

class DigitalNodeViewFactory(
	private val styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : NodeViewFactory<DigitalSignal> {

	override fun create(netView: NetView<DigitalSignal>): NodeView<DigitalSignal> =
		DigitalNodeView(styleProvider, netView.net, netView.style)
}