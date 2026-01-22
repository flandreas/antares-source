package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.Bean
import ch.scorpion.jabbah.draw.DrawableEvent
import ch.scorpion.jabbah.draw.DrawableListener
import ch.scorpion.jabbah.graph.view.ConnectableView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.EdgeViewLayout
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import kotlin.math.max
import kotlin.math.min

class EdgeViewLayoutImpl(
	private val edgeView: EdgeView<*>,
	type: LayoutType = LayoutType.ORTHOGONAL
) : EdgeViewLayout, Bean {

	companion object {
		private val LOG by logger(EdgeViewLayoutImpl::class)
	}

	/** ---- [DrawableListener] */

	override fun drawableInvalidated(event: DrawableEvent) {}

	override fun drawableRequestRedraw(event: DrawableEvent) {}

	/**
	 * Listens for geometry updates of the [ConnectableView]s to which this [EdgeView] is connected and
	 * initiates a re-layout when they are changed.
	 */
	override fun drawableUpdated(event: DrawableEvent) {
		if (edgeView.parent == null) {
			// No need to do any layouts while EdgeView is being loaded from persistent storage
			return
		}
		LOG.trace("VerticeView updated")
		if (event.source == edgeView.origin?.connectableView && !suspendOriginLayout) {
			layoutOrigin()
		}
		if (event.source == edgeView.destination?.connectableView && !suspendDestinationLayout) {
			layoutDestination()
		}

		edgeView.origin?.portView?.edgeViewUpdated(edgeView, edgeView.createConnectionGeometry(edgeView.origin!!))
		edgeView.destination?.portView?.edgeViewUpdated(edgeView, edgeView.createConnectionGeometry(edgeView.destination!!))
	}

	/** ---- [EdgeViewLayout] */

	override var isAdjusted: Boolean = false

	override var suspendOriginLayout: Boolean = false

	override var suspendDestinationLayout: Boolean = false

	override var type = type
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
		isAdjusted = if (edgeView.origin?.connectableView is NodeView<*> || edgeView.destination?.connectableView is NodeView<*>) {
			isAdjusted && edgeView.polyline.pointsCount > 2
		} else {
			isAdjusted && edgeView.polyline.pointsCount > 3
		}
	}

	override fun layoutOrigin() {
		layoutOrigin(null)
	}

	override fun layoutOrigin(direction: Direction?) {
		LOG.trace("layoutOrigin")
		if (!isAdjusted) {
			layoutAll(direction, null)
			return
		}
		val destPointIndex = min(2, edgeView.polyline.pointsCount - 1)
		layoutOriginImpl(destPointIndex, getLayoutOriginPoint(), compact = true, direction?.let { setOf(it) }, null)

		updateAdjusted()
	}

	override fun adjustOrigin(layoutDestIndex: Int, origLocation: Point2D, origDirs: Set<Direction>?, destDirs: Set<Direction>?) {
		val destPointIndex = min(layoutDestIndex, edgeView.polyline.pointsCount - 1)
		layoutOriginImpl(destPointIndex, origLocation, compact = false, origDirs, destDirs)
		isAdjusted = true
	}

	private fun layoutOriginImpl(destPointIndex: Int, origLocation: Point2D?, compact: Boolean, origDirs: Set<Direction>?, destDirs: Set<Direction>?) {
		if (origLocation != null) {
			// Fixed BUG #963: destPointIndex outOfBounds
			if (destPointIndex < 0 || destPointIndex >= edgeView.polyline.pointsCount) {
				return
			}

			val destLocation = Point2D(edgeView.polyline.getPointAt(destPointIndex))
			val effOrigDirs = if (origDirs == null) getOriginDirections(destLocation) else origDirs
			val destDir = type.getSegmentDirection(edgeView, destPointIndex - 1)
			val effDestDirs = destDirs ?: destDir?.let { setOf(destDir) } ?: Direction.ALL

			type.layoutOrigin(
				edgeView,
				edgeView.parent as GraphView,
				LayoutBoundary(
					point = origLocation,
					directions = effOrigDirs,
					isPort = edgeView.origin != null || origDirs != null),
				LayoutBoundary(
					point = destLocation,
					directions = effDestDirs,
					isPort = edgeView.destination != null && destPointIndex == edgeView.polyline.pointsCount - 1),
				destPointIndex,
				compact)
		}
	}

	override fun layoutDestination() {
		layoutDestination(null)
	}

	override fun layoutDestination(direction: Direction?) {
		LOG.trace("layoutDestination")
		if (!isAdjusted) {
			layoutAll(null, direction)
			return
		}
		val origPointIndex = max(0, edgeView.polyline.pointsCount - 3)
		layoutDestinationImpl(origPointIndex, getLayoutDestinationPoint(), compact = true, null, direction?.let { setOf(it) })
		updateAdjusted()
	}

	override fun adjustDestination(layoutOrigIndex: Int, destLocation: Point2D, origDirs: Set<Direction>?, destDirs: Set<Direction>?) {
		layoutDestinationImpl(layoutOrigIndex, destLocation, compact = false, origDirs, destDirs)
		isAdjusted = true
	}

	private fun layoutDestinationImpl(origPointIndex: Int, destLocation: Point2D?, compact: Boolean, origDirs: Set<Direction>?, destDirs: Set<Direction>?) {
		if (destLocation != null) {
			// Fixed BUG #963: origPointIndex outOfBounds
			if (origPointIndex < 0 || origPointIndex >= edgeView.polyline.pointsCount) {
				return
			}

			val origLocation = Point2D(edgeView.polyline.getPointAt(origPointIndex))
			val effDestDirs = destDirs ?: getDestinationDirections(origLocation)
			val origDir = type.getSegmentDirection(edgeView, origPointIndex)
			val effOrigDirs = origDirs ?: origDir?.let { setOf(origDir) } ?: Direction.ALL

			type.layoutDestination(
				edgeView,
				edgeView.parent as GraphView,
				LayoutBoundary(
					point = origLocation,
					directions = effOrigDirs,
					isPort = edgeView.origin != null && origPointIndex == 0),
				LayoutBoundary(
					point = destLocation,
					directions = effDestDirs,
					isPort = edgeView.destination != null || destDirs != null),
				origPointIndex,
				compact)
		}
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

			type.layoutAll(
				edgeView,
				edgeView.parent as GraphView,
				LayoutBoundary(
					point = originPoint,
					directions = originDirs,
					isPort = edgeView.origin != null || originDir != null),
				LayoutBoundary(
					point = destPoint,
					directions = destDirs,
					isPort = edgeView.destination != null || destDir != null))
		}
	}

	private fun getLayoutOriginPoint(): Point2D? {
		if (edgeView.origin != null) {
			return edgeView.origin!!.portConnectionPoint
		}
		if (edgeView.polyline.pointsCount > 0) {
			return Point2D(edgeView.polyline.getPointAt(0))
		}
		return null
	}

	private fun getOriginDirections(refPoint: Point2D): Set<Direction> {
		if (edgeView.origin != null) {
			return edgeView.origin!!.getPortConnectionLayoutDirections(edgeView, refPoint)
		}
		return Direction.ALL
	}

	private fun getLayoutDestinationPoint(): Point2D? {
		if (edgeView.destination != null) {
			return edgeView.destination!!.portConnectionPoint
		}
		if (edgeView.polyline.pointsCount >= 2) {
			return Point2D(edgeView.polyline.getPointAt(edgeView.polyline.pointsCount - 1))
		}
		return null
	}

	private fun getDestinationDirections(refPoint: Point2D): Set<Direction> {
		if (edgeView.destination != null) {
			return Direction.oppositeSet(edgeView.destination!!.getPortConnectionLayoutDirections(edgeView, refPoint))
		}
		return Direction.ALL
	}
}