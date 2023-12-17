package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogPort
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.container.InternalLabelOrientation
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.style.GraphStyleType
import ch.scorpion.jabbah.graph.view.style.GraphTheme

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
) : AbstractAntaresPortView<AnalogSignal>(styleProvider, port, x, y, direction, portLabelPosition, internalLabelOrientation, length ?: LENGTH, customUnconnectedLength) {

	/** The electrical current (in A) flowing through this [AnalogPortView] during simulation. */
	var current: Double = 0.0

	/** ---- [PortView] interface */

	override val connectedLength: Int get() = 0

	override val unconnectedLength: Int get() = customUnconnectedLength ?: LENGTH

	/** ---- [AbstractAntaresPortView] */

	override fun setupColor(context: DrawContext) {
		val appContext = context.castedAppContext<GraphApplicationContext>()!!
		if (appContext.showNetState) {
			context.g.color = port.net?.signal?.color?.foregroundColor ?: context.choose(styleProvider.getStyle(GraphStyleType.EDGE).color).foregroundColor
		} else {
			context.g.color = context.choose(styleProvider.getStyle(GraphStyleType.EDGE).color).foregroundColor
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
		super.drawAccess(context)
		drawPossibleCoincidenceWarning(context)
	}
}