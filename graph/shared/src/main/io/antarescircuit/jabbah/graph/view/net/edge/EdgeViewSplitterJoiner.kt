package io.antarescircuit.jabbah.graph.view.net.edge

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.graph.view.ConnectableView
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.NetView
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewEndpointType.DESTINATION
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewEndpointType.ORIGIN

/**
 * A utility object for splitting and joining [EdgeView]s.
 */
object EdgeViewSplitterJoiner {

	fun <T: Any> split(
		edgeView: EdgeView<T>,
		index: Int,
		splitLocation: Point2D,
		edgeViewCreator: (NetView<T>) -> EdgeView<T>
	): EdgeView<T> {

		val tail = edgeViewCreator.invoke(edgeView.netView!!)
		tail.clear()

		if (edgeView.isArrow) {
			tail.isArrow = true
			edgeView.isArrow = false
		}

		var lastPoint: Point2D? = null
		if (edgeView.segmentPointCount > 0) {
			lastPoint = edgeView.polyline.getLastPoint()
		}

		while (edgeView.segmentPointCount - 1 > index) {
			tail.addSegmentPoint(0, edgeView.getSegmentPoint(edgeView.segmentPointCount - 1))
			edgeView.polyline.removePoint(edgeView.segmentPointCount - 1)
		}

		// Bug #988
		if (tail.segmentPointCount == 0 && lastPoint != null) {
			tail.addSegmentPoint(lastPoint)
		}

		if (splitLocation != edgeView.polyline.getLastPoint()) {
			edgeView.addSegmentPoint(Point2D(Point2D(splitLocation)))
		}
		if (splitLocation != tail.getSegmentPoint(0)) {
			tail.addSegmentPoint(0, Point2D(splitLocation))
		}

		// Bug #988
		if (tail.segmentPointCount == 1) {
			tail.addSegmentPoint(splitLocation)
		}

		tail.layout.type = edgeView.layout.type
		tail.layout.isAdjusted = edgeView.layout.isAdjusted
		tail.isArrow = edgeView.isArrow

		val oldDestination = edgeView.destination
		edgeView.unconnectFromDestination(lockEndpoint = true)

		if (oldDestination != null) {
			tail.connectToDestination(oldDestination)
		}

		return tail
	}

	fun <T: Any> join(edgeView: EdgeView<T>, endpointType: EdgeViewEndpointType, other: EdgeView<T>, otherEndpointType: EdgeViewEndpointType): EdgeView<*> {
		return when (endpointType) {
            ORIGIN -> {
				when (otherEndpointType) {
                    ORIGIN -> joinOtherHeadWithHead(edgeView, other)
                    DESTINATION -> joinOtherTailWithHead(edgeView, other)
                }
			}
            DESTINATION -> when (otherEndpointType) {
				ORIGIN -> joinOtherHeadWithTail(edgeView, other)
				DESTINATION -> joinOtherTailWithTail(edgeView, other)
			}
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

		// Intentionally operate only on view level. Other systems rely on model connections.
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

		// Intentionally operate only on view level. Other systems rely on model connections.
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

		// Intentionally operate only on view level. Other systems rely on model connections.
		unconnect(other)
		origin?.let { edgeView.connectToOrigin(it) }

		return edgeView
	}

	private fun <T: Any> joinOtherTailWithTail(edgeView: EdgeView<T>, other: EdgeView<T>): EdgeView<*> {
		for (i in other.segmentPointCount - 1 downTo 0) {
			if (edgeView.polyline.getFirstPoint() != other.getSegmentPoint(i)) {
				edgeView.addSegmentPoint(other.getSegmentPoint(i))
			}
		}
		edgeView.compact()
		val destination = other.origin

		// Intentionally operate only on view level. Other systems rely on model connections.
		unconnect(other)
		destination?.let { edgeView.connectToDestination(it) }

		return edgeView
	}

	private fun <T: Any> unconnect(edgeView: EdgeView<T>) {
		edgeView.origin?.let { edgeView.unconnectFromOrigin() }
		edgeView.destination?.let { edgeView.unconnectFromDestination() }
	}
}