package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.model.text.LabelComponent
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.graph.GraphNameAbbreviator
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.port.PortFactory
import ch.scorpion.jabbah.graph.view.GraphPortView
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
abstract class AbstractContainerDrawingFiller(
	protected val graphView: GraphView,
	protected val containerDrawing: ContainerDrawing,
	protected val addLabel: Boolean,
	protected val portFactory: PortFactory,
	protected val portViewFactory: PortViewFactory
) {

	companion object {
		const val SCALE = 7
		const val GRID = SCALE
		private const val LENGTH = 2 * SCALE
		const val PIN_INSET = 2 * SCALE
		const val PIN_DIST = 4 * SCALE
		const val MIN_WIDTH = 8.0 * SCALE
		const val LABEL_INSET_X = SCALE
	}

	fun fill() {
		containerDrawing.drawables.toList().forEach {
			containerDrawing.remove(it)
		}
		createLayout()
	}

	protected abstract val pinStartY: Int

	protected abstract val minHeight: Int

	open fun calculateHeight(
		inputPortViews: List<GraphPortView<*>>,
		outputPortViews: List<GraphPortView<*>>
	): Int =
		max(minHeight, max(2 * PIN_INSET + PIN_DIST * (inputPortViews.size - 1), 2 * PIN_INSET + PIN_DIST * (outputPortViews.size - 1)))

	protected abstract fun createLabel(): LabelComponent?

	protected abstract fun calculateWidth(maxInputWidth: Double, maxOutputWidth: Double, labelComponent: LabelComponent?): Double

	protected abstract fun positionLabel(label: LabelComponent, rectangle: RectangularShape)

	protected fun calculateWidthWithoutLabel(maxInputWidth: Double, maxOutputWidth: Double): Double =
		max(MIN_WIDTH, maxInputWidth + LABEL_INSET_X + 2 * LABEL_INSET_X + maxOutputWidth)

	private fun createLayout() {
		val inputPortViews = graphView.getGraphPortViews().filter { it.model.portType.isInput }.reversed()
		val outputPortViews = graphView.getGraphPortViews().filter { !inputPortViews.contains(it) }.reversed()

		val height = snap(calculateHeight(inputPortViews, outputPortViews).toDouble())

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

		val label = createLabel()

		val width = snap(calculateWidth(maxInputWidth, maxOutputWidth, label))

		val rectangle = RectangleComponent(0.0, -PIN_INSET.toDouble(), width, height.toDouble())
		containerDrawing.add(rectangle)

		var pinY = pinStartY
		for (input in inputs) {
			input.location = Point2D(0, pinY)
			containerDrawing.add(input)
			pinY += PIN_DIST
		}

		pinY = pinStartY
		for (output in outputs) {
			output.location = Point2D(width, pinY.toDouble())
			containerDrawing.add(output)
			pinY += PIN_DIST
		}

		if (label != null) {
			positionLabel(label, rectangle)
			containerDrawing.add(label)
		}

		containerDrawing.add(OriginIndicator(x = -LENGTH.toDouble(), y = pinStartY.toDouble()))
	}

	private fun snap(value: Double): Double = ceil(value / GRID) * GRID
}

class NarrowContainerDrawingFiller(
	graphView: GraphView,
	containerDrawing: ContainerDrawing,
	addLabel: Boolean = false,
	portFactory: PortFactory = GraphModelModule.portFactory,
	portViewFactory: PortViewFactory = GraphViewModule.portViewFactory
) : AbstractContainerDrawingFiller(graphView, containerDrawing, addLabel, portFactory, portViewFactory) {

	companion object {
		private const val MIN_HEIGHT = 8 * SCALE
		private const val LABEL_INSET_Y = 2 * SCALE
	}

	override val pinStartY: Int get() = 0

	override val minHeight: Int get() = MIN_HEIGHT

	override fun createLabel(): LabelComponent? =
		if (addLabel) {
			LabelComponent(GraphNameAbbreviator.abbreviate(graphView.name))
		} else {
			null
		}

	override fun calculateWidth(maxInputWidth: Double, maxOutputWidth: Double, label: LabelComponent?): Double =
		if (label != null) {
			val labelWidth = label.boundingBox.widthInt
			max(MIN_WIDTH, 2 * max(maxInputWidth, maxOutputWidth) + labelWidth + 2 * LABEL_INSET_X)
		} else {
			calculateWidthWithoutLabel(maxInputWidth, maxOutputWidth)
		}

	override fun positionLabel(label: LabelComponent, rectangle: RectangularShape) {
		label.location = Point2D(rectangle.centerX, LABEL_INSET_Y.toDouble())
	}
}

class WideContainerDrawingFiller(
	graphView: GraphView,
	containerDrawing: ContainerDrawing,
	portFactory: PortFactory = GraphModelModule.portFactory,
	portViewFactory: PortViewFactory = GraphViewModule.portViewFactory
) : AbstractContainerDrawingFiller(graphView, containerDrawing, addLabel = true, portFactory, portViewFactory) {

	companion object {
		private const val MIN_HEIGHT = 4 * SCALE
		private const val LABEL_INSET_Y = 3
	}

	private val label = LabelComponent(graphView.name, inverse = true)

	private val labelHeight: Int get() = label.boundingBox.height.toInt() + 2 * LABEL_INSET_Y

	override val pinStartY: Int get() = 0

	override val minHeight: Int get() = MIN_HEIGHT

	override fun calculateHeight(
		inputPortViews: List<GraphPortView<*>>,
		outputPortViews: List<GraphPortView<*>>
	): Int = super.calculateHeight(inputPortViews, outputPortViews) + labelHeight

	override fun createLabel(): LabelComponent = label

	override fun calculateWidth(maxInputWidth: Double, maxOutputWidth: Double, labelComponent: LabelComponent?): Double =
		max(super.calculateWidthWithoutLabel(maxInputWidth, maxOutputWidth), label.label.boundingBox.width + 2 * LABEL_INSET_X)

	override fun positionLabel(label: LabelComponent, rectangle: RectangularShape) {
		// CAUTION: labelHeight is derived from the label's boundingBox, which in turn depends on the dimension, if set.
		// Therefore, first request the pure labelHeight (without dimension), then set the dimension using labelHeight
		val height = labelHeight
		label.dimension = Dimension2D(rectangle.width, height.toDouble())
		label.location = Point2D(rectangle.centerX, rectangle.maxY - height / 2)
	}
}