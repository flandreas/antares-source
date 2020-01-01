package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.ConnectableView
import ch.scorpion.jabbah.graph.view.EdgeView

/**
 * A utility object for splitting and joining [EdgeView]s.
 */
object EdgeViewSplitterJoiner {

	fun <T: Any> split(
		edgeView: EdgeView<T>,
		index: Int, splitLocation: Point2D,
		edgeViewCreator: (Net<T>) -> EdgeView<T>
	): EdgeView<T> {

		val tail = edgeViewCreator.invoke(edgeView.model)
		tail.clear()

		if (edgeView.isArrow) {
			tail.isArrow = true
			edgeView.isArrow = false
		}
		while (edgeView.segmentPointCount - 1 > index) {
			tail.addSegmentPoint(0, edgeView.getSegmentPoint(edgeView.segmentPointCount - 1))
			edgeView.polyline.removePoint(edgeView.segmentPointCount - 1)
		}

		if (splitLocation != edgeView.polyline.getLastPoint()) {
			edgeView.addSegmentPoint(Point2D(Point2D(splitLocation)))
		}
		if (splitLocation != tail.getSegmentPoint(0)) {
			tail.addSegmentPoint(0, Point2D(splitLocation))
		}

		tail.layout.type = edgeView.layout.type
		tail.layout.isAdjusted = edgeView.layout.isAdjusted
		tail.isArrow = edgeView.isArrow

		val oldDestination = edgeView.destination

		edgeView.unconnectFromDestination()

		if (oldDestination != null) {
			tail.connectToDestination(oldDestination)
		}

		return tail
	}

	fun <T: Any> join(edgeView: EdgeView<T>, other: EdgeView<T>): EdgeView<*> {
		return when {
			edgeView.polyline.getLastPoint() == other.polyline.getFirstPoint() -> joinOtherHeadWithTail(edgeView, other)
			edgeView.polyline.getFirstPoint() == other.polyline.getLastPoint() -> joinOtherTailWithHead(edgeView, other)
			edgeView.polyline.getFirstPoint() == other.polyline.getFirstPoint() -> joinOtherHeadWithHead(edgeView, other)
			else -> throw IllegalArgumentException("joined EdgeView is not adjacent")
		}
	}

	/**
	 * Checks whether the origin segment should be split because it is to be moved and the originating
	 * [ConnectableView] requires a minimum segment length, and adds a new [Point2D] if necessary.
	 * @return `true` if the origin segment has been split and a new [Point2D] has been added
	 */
	fun splitOriginSegmentForMove(edgeView: EdgeView<*>): Boolean {
		if (edgeView.origin?.port == null) {
			return false
		}

		val portView = edgeView.origin!!.portView
		if (portView!!.minSegmentLength == 0) {
			return false
		}

		val newPoint = Point2D(
			edgeView.polyline.getFirstPoint().x + portView.relativeDirection.dx * portView.minSegmentLength,
			edgeView.polyline.getFirstPoint().y + portView.relativeDirection.dy * portView.minSegmentLength)

		edgeView.polyline.addPointAt(1, newPoint.x, newPoint.y)
		return true
	}

	/**
	 * Checks whether the destination segment should be split because it is to be moved and the destination
	 * [ConnectableView] requires a minimum segment length, and adds a new [Point2D] if necessary.
	 * @return `true` if the destination segment has been split and a new [Point2D] has been added
	 */
	fun splitDestinationSegmentForMove(edgeView: EdgeView<*>): Boolean {
		if (edgeView.destination?.port == null) {
			return false
		}

		val portView = edgeView.destination!!.portView
		if (portView!!.minSegmentLength == 0) {
			return false
		}

		val newPoint = Point2D(
			edgeView.polyline.getLastPoint().x + portView.relativeDirection.dx * portView.minSegmentLength,
			edgeView.polyline.getLastPoint().y + portView.relativeDirection.dy * portView.minSegmentLength)

		edgeView.polyline.addPointAt(edgeView.polyline.pointsCount - 1, newPoint.x, newPoint.y)
		return true
	}

	private fun <T: Any> joinOtherHeadWithHead(edgeView: EdgeView<T>, other: EdgeView<T>): EdgeView<*> {
		for (i in 0 until other.segmentPointCount) {
			if (edgeView.polyline.getFirstPoint() != other.getSegmentPoint(i)) {
				edgeView.addSegmentPoint(0, other.getSegmentPoint(i))
			}
		}
		edgeView.compact()
		val origin = other.destination

		unconnect(other)
		origin?.let { edgeView.connectToOrigin(it) }

		return edgeView
	}

	private fun <T: Any> joinOtherHeadWithTail(edgeView: EdgeView<T>, other: EdgeView<T>): EdgeView<*> {
		for (i in 0 until other.segmentPointCount) {
			if (edgeView.polyline.getLastPoint() != other.getSegmentPoint(i)) {
				edgeView.addSegmentPoint(other.getSegmentPoint(i))
			}
		}
		edgeView.compact()
		val destination = other.destination

		unconnect(other)
		destination?.let { edgeView.connectToDestination(it) }

		return edgeView
	}

	private fun <T: Any> joinOtherTailWithHead(edgeView: EdgeView<T>, other: EdgeView<T>): EdgeView<*> {
		for (i in other.segmentPointCount - 1 downTo 0) {
			if (edgeView.polyline.getFirstPoint() != other.getSegmentPoint(i)) {
				edgeView.addSegmentPoint(0, other.getSegmentPoint(i))
			}
		}
		edgeView.compact()
		val origin = other.origin

		unconnect(other)
		origin?.let { edgeView.connectToOrigin(it) }

		return edgeView
	}

	private fun <T: Any> unconnect(edgeView: EdgeView<T>) {
		edgeView.origin?.let { edgeView.unconnectFromOrigin() }
		edgeView.destination?.let { edgeView.unconnectFromDestination() }
	}
}