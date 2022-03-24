package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.Concentrator
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.Handedness
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
    model: Concentrator = Concentrator(),
    handedness: Handedness = Handedness.RIGHT
) : AbstractSplitterView<Concentrator>(styleProvider, model, handedness) {

	override fun createWideSidePortView(height: Int): DigitalPortView {
		val portView = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getOutput(),
			direction = Direction.EAST,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = 0,
			y = (height / 2))
		portView.setLocation(wideSidePortViewX, 0)
		return portView
	}

	override val wideSidePortViewX: Int get() = -DigitalPortView.LENGTH
	override val narrowSidePortViewX: Int get() = -DigitalPortView.LENGTH - WIDTH

	override fun createNarrowSidePortView(port: DigitalPort, y: Int): DigitalPortView {
		val portView = DigitalPortView(
			styleProvider = styleProvider,
			port = port as Port<DigitalSignal>,
			direction = Direction.WEST,
			portLabelPosition = PortLabelPosition.EXTERNAL)
		portView.setLocation(narrowSidePortViewX, y)
		return portView
	}

	override fun createBodyBounds(height: Int): RectangularShape =
		Rectangle2D(-DigitalPortView.LENGTH - WIDTH, -height / 2, WIDTH, height)
}