package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.AnalogNet
import io.antarescircuit.antares.model.analog.AnalogSignal
import io.antarescircuit.antares.view.style.AntaresTheme
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.model.Net
import io.antarescircuit.jabbah.graph.view.net.node.NodeViewImpl

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