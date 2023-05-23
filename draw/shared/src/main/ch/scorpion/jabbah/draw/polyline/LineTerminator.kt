package ch.scorpion.jabbah.draw.polyline

import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.base.geom.Point2D

/**
 * Graphical object that can be added at both ends of a [Polyline].
 *
 * Examples of line terminators are arrow heads or aggregation indicators of associations between classes in UML
 * diagrams.
 */
interface LineTerminator : Drawable {

	/** The size of this [LineTerminator] in the direction of the last [Polyline] segment to which it is attached.*/
	val size: Int

	/**
	 * Sets the location of this [LineTerminator] while leaving the orientation unchanged.
	 *
	 * This method can be used for moving an [LineTerminator] when the [Polyline] to which this
	 * [LineTerminator] is connected moves.
	 * @param location the new location of this [LineTerminator].
	 */
	fun setLocation(location: Point2D)

	/**
	 * Sets the location and the orientation of this [LineTerminator].
	 *
	 * The location is defined as the last point of the polyline segment to which this [LineTerminator] is
	 * connected.
	 *
	 * The two points represent a vector that defines the orientation of this [LineTerminator].
	 * [LineTerminator]s are expected to rotate around their location accordingly. Typically, the location and the
	 * orientation points are the end points of the last polyline segment to which this [LineTerminator] is
	 * connected.

	 * @param location the new location of this [LineTerminator].
	 * @param orientation the point defining the orientation.
	 */
	fun setLocation(location: Point2D, orientation: Point2D)

	/**
	 * Returns the location at which the polyline segment to which this [LineTerminator] is connected is supposed
	 * to end.
	 *
	 * If a [LineTerminator] is not painted opaque, it would be disturbing if the polyline's last segment would be
	 * painted to the location of this [LineTerminator]s. Instead, a [Polyline] is supposed to paint its
	 * last segment to the point returned by this method.
	 * @return the point at which the connected polyline segment should end.
	 */
	val lineEnd: Point2D
}