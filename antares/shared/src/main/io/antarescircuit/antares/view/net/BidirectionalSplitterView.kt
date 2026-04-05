package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.model.net.BidirectionalSplitter
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.antares.view.port.AbstractAntaresPortView
import io.antarescircuit.antares.view.port.DigitalPortView
import io.antarescircuit.antares.view.style.AntaresTheme
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.view.port.PortLabelPosition

class BidirectionalSplitterView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: BidirectionalSplitter = BidirectionalSplitter()
) : AbstractBranchCountSplitterView<BidirectionalSplitter>(styleProvider, model) {

	companion object {
		private const val DIR_PATH_WIDTH_HALF = Look.SCALE / 2.0
		private val DIR_PATH = System.createPath()
			.moveTo(0, 0)
			.lineTo(-DIR_PATH_WIDTH_HALF, -DIR_PATH_HEIGHT_HALF.toDouble())
			.lineTo(-DIR_PATH_WIDTH, 0)
			.lineTo(-DIR_PATH_WIDTH_HALF, DIR_PATH_HEIGHT_HALF.toDouble())
			.close()
	}

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

	override fun drawDirectionAnnotation(context: DrawContext) {
		context.g.stroke = Themes.get<AntaresTheme>().annotation.stroke
		context.translated(bounds.minX + 0.75 * bounds.width, 0.0) {
			it.g.draw(DIR_PATH)
		}
	}
}