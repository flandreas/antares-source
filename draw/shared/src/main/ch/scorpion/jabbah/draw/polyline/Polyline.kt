package ch.scorpion.jabbah.draw.polyline

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.drawable.RotationDirection

/**
 * A geometric figure consisting of a sequence of straight line segments.
 */
interface Polyline {

	companion object {
		const val CONTAINS_SIZE = 4
		const val OVERLAPS_SIZE = 2
	}

    /** Holds the number of points of this [Polyline].*/
    val pointsCount: Int

    /** Holds the [LineTerminator] that is connected to the first point of this [Polyline].*/
    var beginLineTerminator: LineTerminator?

    /** Holds the [LineTerminator] that is connected to the last point of this [Polyline].*/
    var endLineTerminator: LineTerminator?

	/** Returns the geometrical length of this [Polyline], which is the sum of all segment lengths.*/
	val length: Double

	fun getPointList(): List<Point2D>

	/** Removes all points from this [Polyline].*/
    fun clear()

    /**
     * Adds a new point with the specified coordinates to the end of this [Polyline].
     * @param x the x coordinate of the new point
     * @param y the y coordinate of the new point
     */
    fun addPoint(x: Double, y: Double): Polyline

    /**
     * Adds a new point with the specified coordinates to the end of this [Polyline].
     */
    fun addPoint(x: Int, y: Int): Polyline = addPoint(x.toDouble(), y.toDouble())

    /**
     * Adds a new point the specified index.
     * @param index the index at which the new point is to be added, where `0` is the index of the first point.
     * @param x the x coordinate of the point to set
     * @param y the y coordinate of the point to set
     */
    fun addPointAt(index: Int, x: Double, y: Double): Polyline

    /**
     * Removes the point at the specified index.
     * @param index the index of the point to be removed, where `0` is the index of the first point, and
     *      `getPointsCount() - 1` is the index of the last point
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    fun removePoint(index: Int): Polyline

    /**
     * Returns the point at the specified index.
     * @param index the index of the requested point, where `0` is the index of the first point, and
     *      `getPointsCount() - 1` is the index of the last point
     * @return the point at the specified index
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    fun getPointAt(index: Int): Point2D

    /**
     * Returns the first [Point2D].
     * @throws IndexOutOfBoundsException if there is no [Point2D]
     */
    fun getFirstPoint(): Point2D = getPointAt(0)

    /**
     * Returns the last [Point2D].
     * @throws IndexOutOfBoundsException if there is no [Point2D]
     */
    fun getLastPoint(): Point2D = getPointAt(pointsCount - 1)

    /**
     * Sets the coordinates of the point at the specified index.
     * @param index the index of the points whose coordinates are to be set, where `0` is the index of the first
     *      point, and `getPointsCount() - 1` is the index of the last point. If index is
     *      `getPointsCount()`, a new point with the specified coordinates is added.
     * @param x the x coordinate of the point to set
     * @param y the y coordinate of the point to set
     */
    fun setPointAt(index: Int, x: Double, y: Double): Polyline

    /**
     * Replaces the point of this [Polyline] with the points in the specified list.
     * @param points the list of new points for this Polyline.
     */
    fun setPoints(points: List<Point2D>): Polyline

    /**
     * Moves the entire [Polyline] to a new location.
     *
     * The location of an [Polyline] is defined a the location of the first point. Moves all points of this
     * [Polyline] by the same offset.
     * @param x the x coordinate of the new location
     * @param y the y coordinate of the new location
     */
    fun setLocation(x: Double, y: Double): Polyline

    /** Returns the width of the [Polyline]'s segment. This width influences the calculation of the bounding box. */
    fun getLineWidth(): Double

    /**
     * Finds the index of the [Polyline] segment that contains the specified location while respecting the
     * specified sensitive area.
     * @param x the x coordinate of the location.
     * @param y the y coordinate of the location.
     * @param area the sensitive area.
     * @return the index of the found segment, where 0 is the index of the first segment.
     */
    fun findSegment(x: Double, y: Double, area: Int = CONTAINS_SIZE): Int?

    /**
     * Finds the index of the [Point2D] that contains the specified location while respecting the specified
     * sensitive area.
     * @param x the x coordinate of the location.
     * @param y the y coordinate of the location.
     * @param area the sensitive area.
     * @return the index of the found segment [Point2D], where 0 is the index of the first [Point2D].
     */
    fun findPoint(x: Double, y: Double, area: Int = CONTAINS_SIZE): Int?

    /** Returns the center of the segment with the specified index. */
    fun getCenterOfSegment(index: Int): Point2D

    /**
     * Compacts this [Polyline] by removing [Point2D]s that are at the same location as their preceding
     * [Point2D], unless the [Polyline] contains only two [Point2D]s.
     * @return `true` if the [Polyline] has been changed while compacting
     */
    fun compact(): Boolean

    /**
     * Returns the geometrical length of the specified segment.
     * @param index the index of the segment
     */
    fun getSegmentLength(index: Int): Double

    /**
     * Returns the [Point2D]s of this [Polyline] between a startIndex, inclusive, and an end index, exclusive.
     * @param startIndex the index to start with
     * @param endIndex the end index, exclusive
     * @return the [List] containing the requested [Point2D]s
     */
    fun getPoints(startIndex: Int, endIndex: Int): List<Point2D>

	fun isSegmentHorizontal(index: Int): Boolean

	fun isSegmentVertical(index: Int): Boolean

    /**
     * Determines whether the specified segment is orthogonal, i.e. is horizontal or vertical.
     * @param index the index of the segment in question, where 0 is the index of the first segment.
     * @return `true` if segment `index` is orthogonal.
     */
    fun isSegmentOrthogonal(index: Int): Boolean = isSegmentHorizontal(index) || isSegmentVertical(index)

	fun isSegmentOrthogonalTo(index: Int, otherIndex: Int, other: List<Point2D>): Boolean

    fun mirrorHorizontally(x: Double)

    fun mirrorVertically(y: Double)

	/**
	 * Reverses the order of the points of this [Polyline], making the formerly first point now the last point,
	 * and vice versa.
	 */
	fun reverse()

	/** Rotates by 90 degrees to the specified [RotationDirection]. */
	fun rotate(direction: RotationDirection, pivot: Point2D?)

	fun overlapsOrthogonallyWith(otherIndex: Int, other: List<Point2D>): Boolean
}

/**
 * The number of interferences of an orthogonal [Polyline] with other orthogonal [Polyline]s.
 * @property intersectionCount the number of intersections of any orthogonal segments
 * @property overlappingCount the number of overlaps of segments with parallel segments
 */
data class PolylineInterference(
	val intersectionCount: Int,
	val overlappingCount: Int
) {
	companion object {
		val ZERO = PolylineInterference(0, 0)
	}
}