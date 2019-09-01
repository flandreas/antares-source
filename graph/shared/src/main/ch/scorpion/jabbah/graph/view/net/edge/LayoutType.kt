package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * Represents the supported types for laying out the segments of an [EdgeView].
 */
enum class LayoutType(val customName: String, inputEventHandler: EdgeViewInputEventHandler) {

	STRAIGHT("straight", EdgeViewInputEventHandler()) {
		override fun layout(edgeView: EdgeView<*>, graphView: GraphView<*>, begin: LayoutBoundary, end: LayoutBoundary): List<Point2D> {
			return StraightEdgeViewLayouter.layout(edgeView, graphView, begin, end)
		}

		override fun getSegmentDirection(edgeView: EdgeView<*>, segmentIndex: Int): Direction? {
			return null
		}
	},

	ORTHOGONAL("ortho", DragEdgeSegmentHandler()) {
		override fun layout(edgeView: EdgeView<*>, graphView: GraphView<*>, begin: LayoutBoundary, end: LayoutBoundary): List<Point2D> {
			return OrthoEdgeViewLayouter.layout(edgeView, graphView, begin, end)
		}
	},

	NONE("none", DragEdgePointHandler()) {
		override fun layout(edgeView: EdgeView<*>, graphView: GraphView<*>, begin: LayoutBoundary, end: LayoutBoundary): List<Point2D> {
			return NoneEdgeViewLayouter.layout(edgeView, graphView, begin, end)
		}
	};

	companion object {

		val LOG by logger(LayoutType::class)

		fun withName(customName: String): LayoutType {
			for (i in 0 until LayoutType.values().size) {
				if (values()[i].customName == customName) {
					return values()[i]
				}
			}
			LOG.error("Unknown Layout $customName")
			throw IllegalArgumentException("Unknown Layout $customName")
		}
	}

	private val _inputEventHandler: EdgeViewInputEventHandler = inputEventHandler

	/**
	 * Returns an [InputEventHandler] that handles input events for the specified [EdgeView].
	 *
	 * This method is used for selected [EdgeView]s as well as for unselected ones.
	 * The specified `alternative` is to be returned if the events are not to be handled by special components
	 * like [EdgeEndpointView], but the specified alternative. Selected [EdgeView]s will used the default, which
	 * forwards events to the [EdgeView] itself. Unselected [EdgeView]s will rather return an empty [InputEventHandler],
	 * which results in involving the selection tool to select the [EdgeView] upon a click, which will finally result
	 * in events being handled by the [EdgeView]'s [InputEventHandler].
	 *
	 * TODO This is independent of layout and should therefore moved away from here
	 */
	fun <T : InputEventContext> getInputEventHandler(
		edgeView: EdgeView<*>,
		context: T,
		alternative: () -> InputEventHandler<T> = {
			_inputEventHandler.edgeView = edgeView
			_inputEventHandler as InputEventHandler<T>
		}
	): InputEventHandler<T> {

		if (context.mouseEvent != null) {
			if (edgeView.destination == null && edgeView.destinationEndpointView.contains(context.x, context.y)) {
				return edgeView.destinationEndpointView.getInputEventHandler(context)
			}
			if (edgeView.origin == null && edgeView.originEndpointView.contains(context.x, context.y)) {
				return edgeView.originEndpointView.getInputEventHandler(context)
			}
			if (context.mouseEvent!!.isAltDown) {
				edgeView.edgeToPortConnectorSupplier.invoke().useFor(edgeView)
				return edgeView.edgeToPortConnectorSupplier.invoke().handler as InputEventHandler<T>
			}
		}

		return alternative.invoke()
	}

	abstract fun layout(edgeView: EdgeView<*>, graphView: GraphView<*>, begin: LayoutBoundary, end: LayoutBoundary): List<Point2D>

	override fun toString(): String {
		return when (this) {
			STRAIGHT -> Translations.getString("graph.property.edgeView.layout.straight.name")
			ORTHOGONAL -> Translations.getString("graph.property.edgeView.layout.orthogonal.name")
			NONE -> Translations.getString("graph.property.edgeView.layout.none.name")
		}
	}

	open fun getSegmentDirection(edgeView: EdgeView<*>, segmentIndex: Int): Direction? {
		if (edgeView.isDegenerated) {
			return null
		}
		if (!edgeView.polyline.isSegmentOrthogonal(segmentIndex)) {
			return null
		}

		return Direction.optionalOf(
			Point2D(edgeView.getSegmentPoint(segmentIndex)),
			Point2D(edgeView.getSegmentPoint(segmentIndex + 1)))
	}
}

/** Represents a boundary of a region of an [EdgeView] that is to be layouted. */
data class LayoutBoundary(val point: Point2D, val directions: Set<Direction>, val isPort: Boolean)