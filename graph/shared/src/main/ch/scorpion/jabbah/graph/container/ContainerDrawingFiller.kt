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
import kotlin.math.abs
import kotlin.math.ceil
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
		private const val GRID = SCALE
		private const val LENGTH = 2 * SCALE
		private const val MIN_HEIGHT = 8 * SCALE
		private const val PIN_INSET = 2 * SCALE
		private const val PIN_DIST = 4 * SCALE
		private const val MIN_WIDTH = 8.0 * SCALE
		private const val LABEL_INSET_X = SCALE
	}

	fun fill() {
		// Remove default OriginIndicator and rectangle
		containerDrawing.drawables.toList().forEach {
			containerDrawing.remove(it)
		}

		createLayout()
	}

	private fun createLayout() {
		val inputPortViews = graphView.getGraphPortViews().filter { it.model.portType.isInput }.reversed()
		val outputPortViews = graphView.getGraphPortViews().filter { !inputPortViews.contains(it) }.reversed()

		val height = max(MIN_HEIGHT, max(2 * PIN_INSET + PIN_DIST * (inputPortViews.size - 1), 2 * PIN_INSET + PIN_DIST * (outputPortViews.size - 1)))

		val inputs = mutableListOf<PortViewComponent<*>>()
		val outputs = mutableListOf<PortViewComponent<*>>()

		var maxInputWidth = 0.0
		for (pin in inputPortViews) {
			val portView = portViewFactory.createPortView(portFactory.createSubGraphPort(pin.model), Direction.WEST)
			maxInputWidth = max(maxInputWidth, abs(portView.boundingBox.maxX - portView.location.x))
			inputs.add(portViewFactory.createPortViewComponent(portView))
		}

		var maxOutputWidth = 0.0
		for (pin in outputPortViews) {
			val portView = portViewFactory.createPortView(portFactory.createSubGraphPort(pin.model), Direction.EAST)
			maxOutputWidth = max(maxOutputWidth, abs(portView.location.x - portView.boundingBox.minX))
			outputs.add(portViewFactory.createPortViewComponent(portView))
		}

		// TODO Add label
		val labelWidth = 0

		val widthRaw = max(
			MIN_WIDTH, maxInputWidth + LABEL_INSET_X + labelWidth + LABEL_INSET_X + maxOutputWidth)

		// Snap width to grid
		val width = ceil(widthRaw / GRID) * GRID

		containerDrawing.add(RectangleComponent(0.0, -PIN_INSET.toDouble(), width, height.toDouble()))

		var pinY = 0
		for (input in inputs) {
			input.location = Point2D(0, pinY)
			containerDrawing.add(input)
			pinY += PIN_DIST
		}

		pinY = 0
		for (output in outputs) {
			output.location = Point2D(width, pinY.toDouble())
			containerDrawing.add(output)
			pinY += PIN_DIST
		}

		containerDrawing.add(OriginIndicator(x = -LENGTH.toDouble(), y = 0.0))
	}
}