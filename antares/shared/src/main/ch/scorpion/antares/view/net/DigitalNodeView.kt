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
	currentSystemSpeedCategory: CurrentSystemSpeedCategory = ExecutionModule.currentSystemSpeedCategory,
	net: Net<DigitalSignal> = DigitalNet(),
	netViewStyle: NetViewStyle? = null
) : NodeViewImpl<DigitalSignal>(styleProvider, currentSystemSpeedCategory, net, netViewStyle) {

	override fun draw(context: DrawContext) {
		val oldColor = context.g.color
		val oldCompositeColor = context.color

		if (context.castedAppContext<GraphApplicationContext>()!!.isExecute && showNetState()) {
			if (!model.isError) {
				val signalColor = model.signal!!.getColor()
				context.color = if (styling.isArea) {
					CompositeColor(signalColor.foregroundColor, Themes.get<AntaresTheme>().word.backgroundColor)
				} else {
					signalColor
				}
			}
		} else {
			context.color = context.choose(color)
		}

		super.draw(context)

		context.color = oldCompositeColor
		context.g.color = oldColor
	}
}

class DigitalNodeViewFactory(
	private val styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	private val currentSystemSpeedCategory: CurrentSystemSpeedCategory = ExecutionModule.currentSystemSpeedCategory
) : NodeViewFactory<DigitalSignal> {

	override fun create(): NodeView<DigitalSignal> {
		return DigitalNodeView(styleProvider, currentSystemSpeedCategory)
	}

	override fun create(net: Net<DigitalSignal>): NodeView<DigitalSignal> {
		return DigitalNodeView(styleProvider, currentSystemSpeedCategory, net)
	}
}