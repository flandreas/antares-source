package ch.scorpion.antares.view.container

import ch.scorpion.antares.view.Look.SCALE
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.container.OriginIndicator
import ch.scorpion.jabbah.graph.container.PortViewComponent
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.port.PortFactory
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.PortViewFactory
import kotlin.math.max

/**
 * Fills a [ContainerDrawing] with a standard symbol drawing containing [PortViewComponent]s
 * for all input and output [GraphPort]s.
 */
class ContainerDrawingFiller(
	private val metaGraph: MetaGraph,
	private val portFactory: PortFactory = GraphModelModule.portFactory,
	private val portViewFactory: PortViewFactory = GraphViewModule.portViewFactory
) {

	companion object {
		private const val MIN_HEIGHT = 8 * SCALE
		private const val PIN_INSET = 2 * SCALE
		private const val PIN_DIST = 4 * SCALE
		private const val WIDTH = 8 * SCALE
	}

	fun fill() {
		// Remove default OriginIndicator and rectangle
		metaGraph.containerDrawing.drawables.toList().forEach {
			metaGraph.containerDrawing.remove(it)
		}

		createLayout()
	}

	private fun createLayout() {
		val inputs = metaGraph.graph.graphView.getGraphPortViews().filter { it.model.portType.isInput }.reversed()
		val outputs = metaGraph.graph.graphView.getGraphPortViews().filter { !inputs.contains(it) }.reversed()

		val height = max(MIN_HEIGHT, max(2 * PIN_INSET + PIN_DIST * (inputs.size - 1), 2 * PIN_INSET + PIN_DIST * (outputs.size - 1)))

		metaGraph.containerDrawing.add(RectangleComponent(0.0, -PIN_INSET.toDouble(), WIDTH.toDouble(), height.toDouble()))

		var pinY = 0
		for (pin in inputs) {
			val pvc = portViewFactory.createPortViewComponent(portViewFactory.createPortView(portFactory.createSubGraphPort(pin.model)))
			pvc.location = Point2D(0, pinY)
			pvc.direction = Direction.WEST
			metaGraph.containerDrawing.add(pvc)
			pinY += PIN_DIST
		}

		pinY = 0
		for (pin in outputs) {
			val pvc = portViewFactory.createPortViewComponent(portViewFactory.createPortView(portFactory.createSubGraphPort(pin.model)))
			pvc.location = Point2D(WIDTH, pinY)
			pvc.direction = Direction.EAST
			metaGraph.containerDrawing.add(pvc)
			pinY += PIN_DIST
		}

		metaGraph.containerDrawing.add(OriginIndicator(x = -DigitalPortView.LENGTH.toDouble(), y = 0.0))
	}
}