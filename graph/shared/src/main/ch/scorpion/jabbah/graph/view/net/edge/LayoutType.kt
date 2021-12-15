package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.connect.EdgeToPortConnector

/**
 * Represents the supported types for laying out the segments of an [EdgeView].
 */
enum class LayoutType(
	override val customName: String,
	inputEventHandler: EdgeViewInputEventHandler
) : EnumProperty<LayoutType> {

	STRAIGHT("straight", EdgeViewInputEventHandler()) {

		override fun layoutOrigin(edgeView: EdgeView<*>, graphView: GraphView, begin: LayoutBoundary, end: LayoutBoundary, destPointIndex: Int, compact: Boolean) {
			StraightEdgeViewLayouter.layoutOrigin(edgeView, graphView, begin, end, destPointIndex, compact)
		}

		override fun layoutDestination(edgeView: EdgeView<*>, graphView: GraphView, begin: LayoutBoundary, end: LayoutBoundary, origPointIndex: Int, compact: Boolean) {
			StraightEdgeViewLayouter.layoutDestination(edgeView, graphView, begin, end, origPointIndex, compact)
		}

		override fun layoutAll(edgeView: EdgeView<*>, graphView: GraphView, begin: LayoutBoundary, end: LayoutBoundary) {
			StraightEdgeViewLayouter.layoutAll(edgeView, graphView, begin, end)
		}

		override fun getSegmentDirection(edgeView: EdgeView<*>, segmentIndex: Int): Direction? {
			return null
		}
	},

	ORTHOGONAL("ortho", DragEdgeSegmentHandler()) {

		override fun layoutOrigin(edgeView: EdgeView<*>, graphView: GraphView, begin: LayoutBoundary, end: LayoutBoundary, destPointIndex: Int, compact: Boolean) {
			OrthoEdgeViewLayouter.layoutOrigin(edgeView, graphView, begin, end, destPointIndex, compact)
		}

		override fun layoutDestination(edgeView: EdgeView<*>, graphView: GraphView, begin: LayoutBoundary, end: LayoutBoundary, origPointIndex: Int, compact: Boolean) {
			OrthoEdgeViewLayouter.layoutDestination(edgeView, graphView, begin, end, origPointIndex, compact)
		}

		override fun layoutAll(edgeView: EdgeView<*>, graphView: GraphView, begin: LayoutBoundary, end: LayoutBoundary) {
			OrthoEdgeViewLayouter.layoutAll(edgeView, graphView, begin, end)
		}
	},

	NONE("none", DragEdgePointHandler()) {

		override fun layoutOrigin(edgeView: EdgeView<*>, graphView: GraphView, begin: LayoutBoundary, end: LayoutBoundary, destPointIndex: Int, compact: Boolean) {
			NoneEdgeViewLayouter.layoutOrigin(edgeView, graphView, begin, end, destPointIndex, compact)
		}

		override fun layoutDestination(edgeView: EdgeView<*>, graphView: GraphView, begin: LayoutBoundary, end: LayoutBoundary, origPointIndex: Int, compact: Boolean) {
			NoneEdgeViewLayouter.layoutDestination(edgeView, graphView, begin, end, origPointIndex, compact)
		}

		override fun layoutAll(edgeView: EdgeView<*>, graphView: GraphView, begin: LayoutBoundary, end: LayoutBoundary) {
			NoneEdgeViewLayouter.layoutAll(edgeView, graphView, begin, end)
		}
	};

	companion object {

		val LOG by logger(LayoutType::class)
		const val BASE_KEY = "graph.property.edgeView.layout"

		fun withName(customName: String): LayoutType {
			for (i in 0 until values().size) {
				if (values()[i].customName == customName) {
					return values()[i]
				}
			}
			LOG.error("Unknown Layout $customName")
			throw IllegalArgumentException("Unknown Layout $customName")
		}
	}

	val edgeViewInputEventHandler: EdgeViewInputEventHandler = inputEventHandler

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
			edgeViewInputEventHandler.edgeView = edgeView
			edgeViewInputEventHandler as InputEventHandler<T>
		}
	): InputEventHandler<T> {

		if (context.mouseEvent != null) {
			if (edgeView.destination == null && edgeView.destinationEndpointView.contains(context.x, context.y)) {
				return edgeView.destinationEndpointView.getInputEventHandler(context)
			}
			if (edgeView.origin == null && edgeView.originEndpointView.contains(context.x, context.y)) {
				return edgeView.originEndpointView.getInputEventHandler(context)
			}
			if (context.mouseEvent?.hasModifier(EdgeToPortConnector.SPLIT_EDGE_VIEW_MODIFIER) == true) {
				edgeView.edgeToPortConnectorSupplier.invoke().useFor(edgeView, context as EditInputEventContext)
				return edgeView.edgeToPortConnectorSupplier.invoke().handler as InputEventHandler<T>
			}
		}

		return alternative.invoke()
	}

	abstract fun layoutOrigin(edgeView: EdgeView<*>, graphView: GraphView, begin: LayoutBoundary, end: LayoutBoundary, destPointIndex: Int, compact: Boolean)

	abstract fun layoutDestination(edgeView: EdgeView<*>, graphView: GraphView, begin: LayoutBoundary, end: LayoutBoundary, origPointIndex: Int, compact: Boolean)

	abstract fun layoutAll(edgeView: EdgeView<*>, graphView: GraphView, begin: LayoutBoundary, end: LayoutBoundary)

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

/** Represents a boundary of a region of an [EdgeView] that is to be laid out. */
data class LayoutBoundary(val point: Point2D, val directions: Set<Direction>, val isPort: Boolean)