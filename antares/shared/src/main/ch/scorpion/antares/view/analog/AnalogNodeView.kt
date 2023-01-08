package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogNet
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.net.node.NodeViewImpl

class AnalogNodeView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	net: Net<AnalogSignal> = AnalogNet()
) : NodeViewImpl<AnalogSignal>(styleProvider, net) {

	override fun draw(context: DrawContext) {
		val graphAppContext = context.castedAppContext<GraphApplicationContext>()!!

		context.color = if (graphAppContext.showNetState) {
			if (model.isError) {
				Themes.get<AntaresTheme>().error
			} else {
				model.signal!!.color
			}
		} else {
			context.choose(color)
		}

		super.draw(context)
	}
}