package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.Concentrator
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition

/**
 * A view of a [Concentrator].
 */
class ConcentratorView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: Concentrator = Concentrator()
) : AbstractSplitterView<Concentrator>(styleProvider, model) {

	override fun createWideSidePortView(height: Int): DigitalPortView {
		val portView = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getOutput(),
			direction = Direction.EAST,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = 0,
			y = (height / 2))
		portView.setLocation(-DigitalPortView.LENGTH, 0)
		return portView
	}

	override fun createNarrowSidePortView(port: DigitalPort, y: Int): DigitalPortView {
		val portView = DigitalPortView(
			styleProvider = styleProvider,
			port = port as Port<DigitalSignal>,
			direction = Direction.WEST,
			portLabelPosition = PortLabelPosition.EXTERNAL)
		portView.setLocation(-DigitalPortView.LENGTH - WIDTH, y)
		return portView
	}

	override fun createBodyBounds(height: Int): RectangularShape =
		Rectangle2D(-DigitalPortView.LENGTH - WIDTH, -height / 2, WIDTH, height)
}