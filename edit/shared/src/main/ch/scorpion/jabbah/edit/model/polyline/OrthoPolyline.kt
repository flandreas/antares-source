package ch.scorpion.jabbah.edit.model.polyline

import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.Geometry
import ch.scorpion.jabbah.base.Math
import ch.scorpion.jabbah.base.MathClass

/**
 * An [OrthoPolyline] is a sequence of [Point2D]s that keeps only those points that form a sequence of
 * orthogonal segments.
 */
class OrthoPolyline(points: List<Point2D>) {

    constructor(): this(listOf())

    companion object {
        /** The maximal angle between two line segment that leads to compaction. */
        val COMPACT_ANGLE = 0.1
    }

    val points: ImmutableList<Point2D> get() = _points.toImmutableList()
    val size: Int get() = _points.size
    val boundingBox: Rectangle2D get() = Rectangle2D(_boundingBox)
    val isDegenerated: Boolean get() = size < 2 || _points[0] == _points[1]

    private val _points = mutableListOf<Point2D>()

    private val _boundingBox = Rectangle2D()

    init {
        add(points)
    }

    /** ---- [Any] */

    override fun toString(): String {
        val s = StringBuilder()
        _points.forEach { s.append("(${it.x},${it.y})") }
        return s.toString()
    }

    /** ---- [OrthoPolyline] */

    fun add(point: Point2D): OrthoPolyline {
        add(listOf(point))
        return this
    }

    fun add(points: List<Point2D>): OrthoPolyline {
        if (points.isNotEmpty()) {
            _points.addAll(points)
            compact()
            updateBoundingBox()
        }
        return this
    }

    fun get(index: Int): Point2D {
        return _points[index]
    }

    fun getSegmentDirection(segmentIndex: Int): Direction? {
        if (isDegenerated) {
            return null
        }
        return Direction.of(_points[segmentIndex], _points[segmentIndex + 1])
    }

    /** Removes all intermediate points that lie between two collinear segments */
    fun compact() {
        var i = 1
        while (i < _points.size) {
            if (canCompactPoint(i)) {
                _points.removeAt(i)
            } else {
                i++
            }
        }
    }

    /** Updates the bounding box according to the current points. */
    private fun updateBoundingBox() {
        if (_points.size > 0) {
            _boundingBox.setFrame(_points[0].x, _points[0].y, 0.0, 0.0)
        }
        _points.forEach { _boundingBox.add(it) }
        _boundingBox.setFrame(
                _boundingBox.x, _boundingBox.y,
                _boundingBox.width, _boundingBox.height)
    }

    /**
     * Checks whether the [Point2D] at a specific index can be compacted because it is an intermediate point of two
     * collinear segments.
     * @param i the index of the [Point2D] to check
     * @return `true` if the [Point2D] at index `i` can be removed.
     */
    private fun canCompactPoint(i: Int): Boolean {
        if (_points[i] == _points[i - 1]) {
            return true
        }
        if (i > _points.size - 2) {
            return false
        }

        val angle1 = Geometry.angle(
                Point2D(),
                Geometry.normal(
                        _points[i - 1].x, _points[i - 1].y,
                        _points[i].x, _points[i].y))

        val angle2 = Geometry.angle(
                Point2D(),
                Geometry.normal(
                        _points[i].x, points[i].y,
                        _points[i + 1].x, _points[i + 1].y))

        return Math.abs(angle1 - angle2) <= COMPACT_ANGLE || Math.abs(Math.abs(angle1 - angle2) - MathClass.PI) < COMPACT_ANGLE
    }
}
