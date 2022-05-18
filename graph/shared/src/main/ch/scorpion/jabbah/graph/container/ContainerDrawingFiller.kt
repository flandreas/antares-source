package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.port.PortFactory
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.PortViewFactory
import kotlin.math.max

/**
 * Fills a [ContainerDrawing] with a standard symbol drawing containing [PortViewComponent]s
 * for all input and output [GraphPort]s.
 */
class ContainerDrawingFiller(
	private val graphView: GraphView,
	private val containerDrawing: ContainerDrawing,
	private val portFactory: PortFactory = GraphModelModule.portFactory,
	private val portViewFactory: PortViewFactory = GraphViewModule.portViewFactory
) {

	companion object {
		private const val SCALE = 7
		private const val LENGTH = 2 * SCALE
		private const val MIN_HEIGHT = 8 * SCALE
		private const val PIN_INSET = 2 * SCALE
		private const val PIN_DIST = 4 * SCALE
		private const val WIDTH = 8 * SCALE
	}

	fun fill() {
		// Remove default OriginIndicator and rectangle
		containerDrawing.drawables.toList().forEach {
			containerDrawing.remove(it)
		}

		createLayout()
	}

	private fun createLayout() {
		val inputs = graphView.getGraphPortViews().filter { it.model.portType.isInput }.reversed()
		val outputs = graphView.getGraphPortViews().filter { !inputs.contains(it) }.reversed()

		val height = max(MIN_HEIGHT, max(2 * PIN_INSET + PIN_DIST * (inputs.size - 1), 2 * PIN_INSET + PIN_DIST * (outputs.size - 1)))

		containerDrawing.add(RectangleComponent(0.0, -PIN_INSET.toDouble(), WIDTH.toDouble(), height.toDouble()))

		var pinY = 0
		for (pin in inputs) {
			val pvc = portViewFactory.createPortViewComponent(portViewFactory.createPortView(portFactory.createSubGraphPort(pin.model)))
			pvc.location = Point2D(0, pinY)
			pvc.direction = Direction.WEST
			containerDrawing.add(pvc)
			pinY += PIN_DIST
		}

		pinY = 0
		for (pin in outputs) {
			val pvc = portViewFactory.createPortViewComponent(portViewFactory.createPortView(portFactory.createSubGraphPort(pin.model)))
			pvc.location = Point2D(WIDTH, pinY)
			pvc.direction = Direction.EAST
			containerDrawing.add(pvc)
			pinY += PIN_DIST
		}

		containerDrawing.add(OriginIndicator(x = -LENGTH.toDouble(), y = 0.0))
	}
}