package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.AnalogPort
import io.antarescircuit.antares.model.analog.AnalogSignal
import io.antarescircuit.antares.view.port.AbstractAntaresPortView
import io.antarescircuit.antares.view.port.ExternalPortLabelDistance
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.container.InternalLabelOrientation
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.view.port.PortLabelPosition
import io.antarescircuit.jabbah.graph.view.port.PortView
import io.antarescircuit.jabbah.graph.view.style.GraphStyleType
import io.antarescircuit.jabbah.graph.view.style.GraphTheme

class AnalogPortView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	port: Port<AnalogSignal> = AnalogPort(),
	x: Int = 0,
	y: Int = 0,
	direction: Direction = Direction.EAST,
	portLabelPosition: PortLabelPosition = PortLabelPosition.INTERNAL,
	internalLabelOrientation: InternalLabelOrientation = InternalLabelOrientation.Horizontal,
	length: Int? = null,
	customUnconnectedLength: Int? = null,
	horizontalExternalLabel: Boolean = false,
	externalPortLabelDistance: ExternalPortLabelDistance = ExternalPortLabelDistance.Small
) : AbstractAntaresPortView<AnalogSignal>(
	styleProvider,
	port,
	x,
	y,
	direction,
	portLabelPosition,
	internalLabelOrientation,
	length ?: LENGTH,
	customUnconnectedLength,
	horizontalExternalLabel,
	externalPortLabelDistance
) {

	/** ---- [PortView] interface */

	override val connectedLength: Int get() = 0

	override val unconnectedLength: Int get() = customUnconnectedLength ?: LENGTH

	/** ---- [AbstractAntaresPortView] */

	override fun setupColor(context: DrawContext) {
		val appContext = context.castedAppContext<GraphApplicationContext>()!!
		context.g.color = if (appContext.showNetState) {
			port.net?.signal?.color?.foregroundColor ?: context.choose(styleProvider.getStyle(GraphStyleType.EDGE).color).foregroundColor
		} else {
			context.choose(owner!!.getEditPortViewColor(styleProvider)).foregroundColor
		}
	}

	override fun setupStroke(context: DrawContext) {
		context.g.stroke = if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			Themes.get<GraphTheme>().edge.executionStroke
		} else {
			Themes.get<GraphTheme>().edge.stroke
		}
	}

	override fun drawAccess(context: DrawContext) {
		prepareConnectionDrawContext(context)
		super.drawAccess(context)
		drawPossibleCoincidenceWarning(context)
	}
}