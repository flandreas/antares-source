package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.BidirectionalSplitter
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition

class BidirectionalSplitterView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: BidirectionalSplitter = BidirectionalSplitter()
) : AbstractSplitterView<BidirectionalSplitter>(styleProvider, model) {

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
		portView.setLocation(DigitalPortView.LENGTH, 0)
		return portView
	}

	override fun createNarrowSidePortView(port: DigitalPort, y: Int): DigitalPortView {
		val portView = DigitalPortView(
			styleProvider = styleProvider,
			port = port as Port<DigitalSignal>,
			direction = Direction.EAST,
			portLabelPosition = PortLabelPosition.EXTERNAL)
		portView.setLocation(DigitalPortView.LENGTH + WIDTH, y)
		return portView
	}

	override fun createBodyBounds(height: Int): RectangularShape =
		Rectangle2D(DigitalPortView.LENGTH, -height / 2, WIDTH, height)

	override fun drawDirectionAnnotation(context: DrawContext) {
		context.g.stroke = Themes.get<AntaresTheme>().annotation.stroke
		context.g.translate(bounds.minX + 0.75 * bounds.width, 0.0)
		context.g.draw(DIR_PATH)
		context.g.translate(-(bounds.minX + 0.75 * bounds.width), 0.0)
	}
}