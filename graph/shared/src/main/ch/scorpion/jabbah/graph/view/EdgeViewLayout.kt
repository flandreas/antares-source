package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawableListener
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewImpl
import ch.scorpion.jabbah.graph.view.net.edge.LayoutType

/**
 * Encapsulates all aspects of an [EdgeView] that is related with its layout.
 * Listens for geometry updates of the [ConnectableView]s to which this [EdgeView] is connected and
 * initiates a re-layout when they are changed.
 */
interface EdgeViewLayout : DrawableListener {

	/** Determines how the segments of the [EdgeView] are automatically laid out, e.g. orthogonally.*/
	var type: LayoutType

	/**
	 * An [EdgeView] is adjusted if the user has moved one of its segments after layout.
	 * Moving a [VerticeView] connected to one end of an adjusted [VerticeView] only updates the layout
	 * of the connected segment, not the entire [EdgeView].
	 */
	var isAdjusted: Boolean

	/** Indicates that this [EdgeViewImpl] should not perform an origin layout.  */
	var suspendOriginLayout: Boolean

	/** Indicates that this [EdgeViewImpl] should not perform a destination layout.  */
	var suspendDestinationLayout: Boolean

	/** Resets the [isAdjusted] property if there are not enough points in the [EdgeView] to justify adjustment.*/
	fun updateAdjusted()

	fun layoutOrigin()

	/**
	 * Layouts the origin part of this [EdgeView] by using the specified origin [Direction].
	 * This is useful when the [EdgeView] has not yet been connected to a origin [Port], but should
	 * use a determined origin [Direction], for example while interactively dragging the origin point
	 * and snapping to a origin [Port].
	 */
	fun layoutOrigin(direction: Direction?)

	fun layoutDestination()

	/**
	 * Layouts the destination part of this [EdgeView] by using the specified destination [Direction].
	 * This is useful when the [EdgeView] has not yet been connected to a destination [Port], but should
	 * use a determined destination [Direction], for example while interactively dragging the destination point
	 * and snapping to a destination [Port].
	 */
	fun layoutDestination(direction: Direction?)

	/**
	 * Adjusts the destination of the [EdgeView] to [origLocation] and updates the layout to the point at [layoutDestIndex].
	 */
	fun adjustOrigin(layoutDestIndex: Int, origDirection: Direction? = null, origLocation: Point2D)

	/**
	 * Adjusts the destination of the [EdgeView] to [destLocation] and updates the layout from the point at [layoutOrigIndex].
	 */
	fun adjustDestination(layoutOrigIndex: Int, destDir: Direction? = null, destLocation: Point2D)

}