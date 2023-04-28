package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.animation.PointRange
import ch.scorpion.jabbah.animation.Sequence
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.EdgeView

/**
 * Represents the [Sequence] of the [Point2D]s of which an [EdgeView] consists.
 */
class EdgeViewPointSequence(
	private val edgeView: EdgeView<*>,
	private val isReverse: Boolean = false,
	private val returnSequenceEndPoint: Boolean = edgeView.segmentPointCount == 2,
	offset: Double = 0.0
) : Sequence<Point2D> {

	private var currSegmentPointIndex: Int = if (isReverse) {
		edgeView.segmentPointCount - 1
	} else {
		0
	}

	/** Creation depends on [currSegmentPointIndex] already set. */
	private var currPointRange: PointRange = createCurrentPointRange(returnSequenceEndPoint, offset)

	/** ---- [Sequence] interface */

	/** Interpret the [size] as overall [EdgeView] size, because that determines the "speed" on the first segment.*/
	override val size: Double = edgeView.calculateMaximumNetLength(isReverse)

	override fun getCurrent(): Point2D = currPointRange.getCurrent()

	override fun hasNext(): Boolean = currPointRange.hasNext() || hasNextSegmentPoint()

	override fun getNext(distance: Double): Point2D {
		if (currPointRange.hasNext()) {
			return currPointRange.getNext(distance)
		}
		if (hasNextSegmentPoint()) {
			nextSegment()
			return currPointRange.getNext(distance)
		}
		throw NoSuchElementException()
	}

	private fun nextSegment() {
		nextSegmentPoint()
		val returnEndPoint = !hasNextSegmentPoint() && returnSequenceEndPoint
		currPointRange = createCurrentPointRange(
			returnEndPoint = returnEndPoint,
			offset = currPointRange.remainder)
	}

	/** ---- [EdgeViewPointSequence] sequencing API for avoiding [Point2D] instantiation */

	fun forEach(distance: Double, handler: (x: Double, y: Double) -> Unit) {
		while (hasNext()) {
			if (currPointRange.hasNext()) {
				currPointRange.forEach(distance, handler)
			} else {
				nextSegment()
			}
		}
	}

	/** ---- [EdgeViewPointSequence] */

	val remainder: Double get() = currPointRange.remainder

	private fun createCurrentPointRange(returnEndPoint: Boolean, offset: Double): PointRange =
		if (isReverse) {
			PointRange(
				edgeView.getSegmentPoint(currSegmentPointIndex),
				edgeView.getSegmentPoint(currSegmentPointIndex - 1),
				returnEndPoint,
				offset)
		} else {
			PointRange(
				edgeView.getSegmentPoint(currSegmentPointIndex),
				edgeView.getSegmentPoint(currSegmentPointIndex + 1),
				returnEndPoint,
				offset)
		}

	private fun hasNextSegmentPoint(): Boolean =
		if (isReverse) {
			currSegmentPointIndex > 1
		} else {
			currSegmentPointIndex < edgeView.segmentPointCount - 2
		}

	private fun nextSegmentPoint() {
		if (isReverse) {
			currSegmentPointIndex--
		} else {
			currSegmentPointIndex++
		}
	}
}