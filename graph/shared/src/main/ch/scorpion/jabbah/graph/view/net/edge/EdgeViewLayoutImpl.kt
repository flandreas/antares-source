package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.DrawableEvent
import ch.scorpion.jabbah.draw.DrawableListener
import ch.scorpion.jabbah.graph.view.ConnectableView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.EdgeViewLayout
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.net.node.NodeView

class EdgeViewLayoutImpl(private val edgeView: EdgeView<*>) : EdgeViewLayout {

	companion object {
		private val LOG by logger(EdgeViewLayoutImpl::class)
	}

	/** ---- [DrawableListener] */

	override fun drawableInvalidated(event: DrawableEvent) { }

	override fun drawableRequestRedraw(event: DrawableEvent) { }

	/**
	 * Listens for geometry updates of the [ConnectableView]s to which this [EdgeView] is connected and
	 * initiates a re-layout when they are changed.
	 */
	override fun drawableUpdated(event: DrawableEvent) {
		if (edgeView.parent == null) {
			// No need to do any layouts while EdgeView is being loaded from persistant storage
			return
		}
		LOG.debug("VerticeView updated")
		if (event.source == edgeView.origin && !suspendOriginLayout) {
			layoutOrigin()
		}
		if (event.source == edgeView.destination && !suspendDestinationLayout) {
			layoutDestination()
		}
	}

	/** ---- [EdgeViewLayout] */

	override var isAdjusted: Boolean = false

	override var suspendOriginLayout: Boolean = false

	override var suspendDestinationLayout: Boolean = false

	override var type = LayoutType.ORTHOGONAL
		set(value) {
			if (value == field) {
				return
			}
			field = value
			if (edgeView.parent != null) {
				layoutAll(null, null)
			}
		}

	override fun updateAdjusted() {
		isAdjusted = if (edgeView.origin is NodeView<*> || edgeView.destination is NodeView<*>) {
			isAdjusted && edgeView.polyline.pointsCount > 2
		} else {
			isAdjusted && edgeView.polyline.pointsCount > 3
		}
	}

	override fun handleOriginChanged() {
		throw UnsupportedOperationException("not implemented")
	}

	override fun handleDestinationChanged() {
		throw UnsupportedOperationException("not implemented")
	}

	override fun layoutOrigin() {
		layoutOrigin(null)
	}

	override fun layoutOrigin(direction: Direction?) {
		LOG.debug("layoutOrigin")
		if (!isAdjusted) {
			layoutAll(direction, null)
			return
		}
		val originPoint = getLayoutOriginPoint()
		if (originPoint != null) {
			val destPointIndex = Math.min(2, edgeView.polyline.pointsCount - 1)
			val destPoint = Point2D(edgeView.polyline.getPointAt(destPointIndex))
			val originDirs = if (direction == null) getOriginDirections(destPoint) else setOf(direction)
			val destDir = type.getSegmentDirection(edgeView, destPointIndex - 1)
			val list = mutableListOf<Point2D>()
			list.addAll(type.layout(
				edgeView,
				edgeView.parent as GraphView<*>,
				LayoutBoundary(
					point = originPoint,
					directions = originDirs,
					isPort = edgeView.origin != null || direction != null),
				LayoutBoundary(
					point = destPoint,
					directions = destDir?.let { setOf(destDir) } ?: setOf(),
					isPort = edgeView.destination != null && destPointIndex == edgeView.polyline.pointsCount - 1)))

			list.addAll(edgeView.polyline.getPoints(destPointIndex, edgeView.polyline.pointsCount))

			edgeView.setLaidOutPoints(list)
		}

		updateAdjusted()
	}

	override fun layoutDestination() {
		layoutDestination(null)
	}

	override fun layoutDestination(direction: Direction?) {
		LOG.debug("layoutDestination")
		if (!isAdjusted) {
			layoutAll(null, direction)
			return
		}
		val destPoint = getLayoutDestinationPoint()
		if (destPoint != null) {
			val origPointIndex = Math.max(0, edgeView.polyline.pointsCount - 3)
			val origPoint = Point2D(edgeView.polyline.getPointAt(origPointIndex))
			val destDirs = if (direction == null) getDestinationDirections(origPoint) else setOf(direction)
			val origDir = type.getSegmentDirection(edgeView, origPointIndex)
			val list = mutableListOf<Point2D>()
			list.addAll(type.layout(
				edgeView,
				edgeView.parent as GraphView<*>,
				LayoutBoundary(
					point = origPoint,
					directions = origDir?.let { setOf(origDir) } ?: setOf(),
					isPort = edgeView.origin != null && origPointIndex == 0),
				LayoutBoundary(
					point = destPoint,
					directions = destDirs,
					isPort = edgeView.destination != null || direction != null)))
			list.addAll(0, edgeView.polyline.getPoints(0, origPointIndex))

			edgeView.setLaidOutPoints(list)
		}

		updateAdjusted()
	}

	private fun layoutAll(originDir: Direction?, destDir: Direction?) {
		if (LOG.isTraceEnabled()) {
			LOG.trace("layoutAll, originDir=$originDir, destDir=$destDir")
		}
		val originPoint = getLayoutOriginPoint()
		val originDirs: Set<Direction>
		val destPoint = getLayoutDestinationPoint()
		val destDirs: Set<Direction>

		if (originPoint != null && destPoint != null) {
			originDirs = if (originDir != null) setOf(originDir) else getOriginDirections(destPoint)
			destDirs = if (destDir != null) setOf(destDir) else getDestinationDirections(originPoint)

			val laidOutPoints = type.layout(
				edgeView,
				edgeView.parent as GraphView<*>,
				LayoutBoundary(
					point = originPoint,
					directions = originDirs,
					isPort = edgeView.origin != null || originDir != null),
				LayoutBoundary(
					point = destPoint,
					directions = destDirs,
					isPort = edgeView.destination != null || destDir != null))


			edgeView.setLaidOutPoints(laidOutPoints)
		}
	}

	private fun getLayoutOriginPoint(): Point2D? {
		if (edgeView.origin != null) {
			return edgeView.origin!!.getPortConnectionPoint(edgeView.originPort)
		}
		if (edgeView.polyline.pointsCount > 0) {
			return Point2D(edgeView.polyline.getPointAt(0))
		}
		return null
	}

	private fun getOriginDirections(refPoint: Point2D): Set<Direction> {
		if (edgeView.origin != null) {
			return edgeView.origin!!.getPortConnectionLayoutDirections(edgeView, edgeView.originPort, refPoint)
		}
		return setOf(Direction.EAST)
	}

	private fun getLayoutDestinationPoint(): Point2D? {
		if (edgeView.destination != null) {
			return edgeView.destination!!.getPortConnectionPoint(edgeView.destinationPort)
		}
		if (edgeView.polyline.pointsCount >= 2) {
			return Point2D(edgeView.polyline.getPointAt(edgeView.polyline.pointsCount - 1))
		}
		return null
	}

	private fun getDestinationDirections(refPoint: Point2D): Set<Direction> {
		if (edgeView.destination != null) {
			return Direction.oppositeSet(edgeView.destination!!.getPortConnectionLayoutDirections(edgeView, edgeView.destinationPort, refPoint))
		}
		return Direction.ALL
	}
}