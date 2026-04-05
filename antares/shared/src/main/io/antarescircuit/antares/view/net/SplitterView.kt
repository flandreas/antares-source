package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.model.net.Splitter
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.port.AbstractAntaresPortView
import io.antarescircuit.antares.view.port.DigitalPortView
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.view.port.PortLabelPosition

/**
 * A view of a [Splitter].
 */
class SplitterView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: Splitter = Splitter()
) : AbstractBranchCountSplitterView<Splitter>(styleProvider, model) {

	override fun createWideSidePortView(height: Int): DigitalPortView {
		val portView = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getInput(),
			direction = Direction.WEST,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = 0,
			y = (height / 2))
		portView.setLocation(wideSidePortViewX, 0)
		return portView
	}

	override val wideSidePortViewX: Int get() = AbstractAntaresPortView.LENGTH
	override val narrowSidePortViewX: Int get() = AbstractAntaresPortView.LENGTH + WIDTH

	override fun createNarrowSidePortView(port: DigitalPort, y: Int): DigitalPortView {
		val portView = DigitalPortView(
			styleProvider = styleProvider,
			port = port as Port<DigitalSignal>,
			direction = Direction.EAST,
			portLabelPosition = PortLabelPosition.EXTERNAL)
		portView.setLocation(narrowSidePortViewX, y)
		return portView
	}

	override fun createBodyBounds(height: Int): RectangularShape =
		Rectangle2D(AbstractAntaresPortView.LENGTH, -height / 2, WIDTH, height)
}