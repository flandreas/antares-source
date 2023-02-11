package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawableListener
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.polyline.Polyline
import ch.scorpion.jabbah.draw.polyline.PolylineShape
import ch.scorpion.jabbah.edit.SnapManager
import ch.scorpion.jabbah.edit.Snapper
import ch.scorpion.jabbah.edit.model.text.description.Describable
import ch.scorpion.jabbah.edit.model.text.description.Description
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.connect.EdgeToPortConnector
import ch.scorpion.jabbah.graph.view.net.edge.EdgeEndpointView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewStyling
import ch.scorpion.jabbah.graph.view.net.node.NodeView

/**
 * An [EdgeView] is a part of a [NetView] that connects a [Port] of an origin [VerticeView]
 * with the [Port] of a destination [VerticeView], where both the origin and the destination can be unset.
 * An [EdgeView] is expected to be a [Polyline], without explicitly implementing that interface.
 *
 * An [EdgeView] is typically a [DrawableListener] of the [VerticeView] to which it is connected, and
 * reacts to [DrawableListener.drawableUpdated] by initiating a re-layout of itself.
 *
 * @param <T> the type of signal
 *
 * TODO Refactor: Extract the read-only part of the [Polyline] interface and let [EdgeView] implement it
 */
interface EdgeView<T: Any> : NetViewElement<T>, Describable, ActorView {

	companion object {

		const val BASE_KEY_ARROW = "graph.property.edgeView.arrow"

		/** The name of the [Int] property in [Properties] containing the minimum length of interactively created [EdgeView]s*/
		const val PROP_MIN_EDGE_VIEW_LENGTH = "ch.scorpion.jabbah.graph.view.EdgeView.minLength"

		/** The size of the rectangular area defining whether a point is "inside" this [EdgeView]. Used for snapping.*/
		const val containsSize: Int = 4

		/** The maximum distance for regarding a snap point as being on a corner of this [EdgeView]. Used for snapping.*/
		const val edgeCornerDistance: Int = 15
	}

	override var description: Description
		get() = model.description
		set(value) { model.description = value }

	/** The [Connection] at the origin of this [EdgeView] (i.e. at the [Polyline]'s first point). */
	val origin: Connection<T>?

	/** The [Connection] at the destination of this [EdgeView] (i.e. at the [Polyline]'s last point). */
	val destination: Connection<T>?

    /** Returns the number of segment points of this [EdgeView].*/
    val segmentPointCount: Int

    /**
     * Returns the width of segments of this [EdgeView]. Depends on the [EdgeViewStyling] of this [EdgeView]
     * and is typically the width of the [Stroke] used for drawing this [EdgeView].
     */
    val width: Int

    /**
     * An [EdgeView] is degenerated if it contains less than two [Point2D]s, or all of its [Point2D]s are
     * at the same location.
     */
    val isDegenerated: Boolean

	/**
	 * An [EdgeView] is sufficiently defined if its geometry contributes to a [GraphView] such that it
	 * absence would be recognized by the user. Used while interactive composition of an [EdgeView]
	 * to determine if the created [EdgeView] can be thrown away because its end points are not
	 * sufficiently separated, e.g. if the user creates an [EdgeView] with begin and end points
	 * nearly at the same location.
	 */
	val isSufficientlyLarge: Boolean

    /** Encapsulates all aspects of an [EdgeView] that is related with its layout.*/
	val layout: EdgeViewLayout

    /** Determines whether this [EdgeView] displays an arrow head at its destination. */
    var isArrow: Boolean

    /** Returns the destination [EdgeEndpointView] of this [EdgeView].*/
    val originEndpointView: EdgeEndpointView

    /** Returns the destination [EdgeEndpointView] of this [EdgeView]. */
    val destinationEndpointView: EdgeEndpointView

    /** Holds the [Point2D]s (in absolute coordinates) that define the segments of this [EdgeView].*/
    val polyline: PolylineShape

	val edgeToPortConnectorSupplier: () -> EdgeToPortConnector

	/**
	 * Can be set for [EdgeView]s during interactive creation by the user, especially to avoid
	 * that the [EdgeView] "under construction" gets deleted while being created, which would lead to
	 * all kind of difficult state problems. Not persistent.
	 */
	var underConstruction: Boolean

	/**
	 * Returns the [Stroke] this [EdgeView] uses in execution mode.
	 * Used by other objects that have to draw [EdgeView] alike overlays.
	 */
	val executionStroke: Stroke

	val isConnectedWithNodeView: Boolean get() =
		origin?.connectableView is NodeView<*> || destination?.connectableView is NodeView<*>

    /**
     * Returns the [Connection] that corresponds with the specified [Port], which can be either the
     * [origin] or the [destination] [Connection].
     */
    fun getConnection(port: Port<*>): Connection<T>?

	/** Returns the [Connection] at [EdgeViewEndpointType]. */
	fun getConnection(endpointType: EdgeViewEndpointType): Connection<T>?

	/** Returns the [Connection] connected with [connectableView]. */
	fun getConnection(connectableView: ConnectableView): Connection<T>?

	fun getOppositeConnection(port: Port<*>): Connection<T>?

	fun getOppositeConnection(connection: Connection<*>): Connection<T>? =
		if (origin?.connectableView === connection.connectableView) destination else origin

	/**
	 * Returns the [EdgeViewEndpointType] that represents the specified [Connection], or `null` if
	 * neither [origin] nor [destination] equals [connection].
	 */
	fun getConnectionEndpointType(connection: Connection<*>): EdgeViewEndpointType?

	fun getConnectionEndpointType(connectableView: ConnectableView): EdgeViewEndpointType?

	fun getEndpointType(edgeEndpointView: EdgeEndpointView): EdgeViewEndpointType?

	/**
	 * Returns the [EdgeEndpointView] of this [EdgeView] with the specified [EdgeViewEndpointType]
	 * if the corresponding end is unconnected (open), or `null` otherwise.
	 */
	fun getOpenEndpointView(type: EdgeViewEndpointType): EdgeEndpointView?

	/** Returns the start [Point2D] of the segment with the specified index.*/
    fun getSegmentPoint(index: Int): Point2D

    /**
     * Adds the specified [Point2D] at the end of this [EdgeView].
     * @return this [EdgeView] to support method chaining
     */
    fun addSegmentPoint(point: Point2D): EdgeView<T>

    /**
     * Adds the specified [Point2D] at the given index.
     * @returns this [EdgeView] to support method chaining
     */
    fun addSegmentPoint(index: Int, point: Point2D): EdgeView<T>

	/** Removes the [Point2D] at the given index.*/
	fun removeSegmentPoint(index: Int): EdgeView<T>

	/** Removes all segment points from this [EdgeView]. */
	fun clear(): EdgeView<T>

	/**
     * Compacts this [EdgeView] by removing [Point2D]s that are at the same location as their predecessor
     * [Point2D], or that are intermediate points of two parallel segments.
     */
    fun compact()

	/** Consumes the [Point2D]s produced by an [EdgeViewLayout] without initiating another layout cycle. */
	fun setLaidOutPoints(points: List<Point2D>, compact: Boolean)

    /**
     * Connects this [EdgeView] to the [Port] of the origin [ConnectableView].
     * Note that this method does **NOT** establish the connection on the model layer.
     */
    fun connectToOrigin(connection: Connection<T>)

	fun unconnectFromOrigin()

    /**
     * Connects this [EdgeView] to the [Port] of the destination [ConnectableView].
     * Note that this method does **NOT** establish the connection on the model layer.
     */
    fun connectToDestination(connection: Connection<T>)

	fun unconnectFromDestination(lockEndpoint: Boolean = false)

    /** Moves the point with the specified index to a new location. */
    fun movePoint(index: Int, x: Double, y: Double)

    /**
     * Moves the origin end point of an open [EdgeView] to the specified location while interactively connecting
     * two [Vertice]s.
     * @param x the x coordinate of the new end point location.
     * @param y the y coordinate of the new end point location.
     */
    fun moveOriginEndPoint(x: Double, y: Double)

    /**
     * Moves the destination end point of an open [EdgeView] to the specified location while interactively
     * connecting two [Vertice]s.
     * @param x the x coordinate of the new end point location.
     * @param y the y coordinate of the new end point location.
     */
    fun moveDestinationEndPoint(x: Double, y: Double)

    /**
     * Moves a segment of this [EdgeView] and returns information about the segment moving.
     * @param segmentIndex the index of the segment that is to be moved.
     * @param from the start location of the displacement vector
     * @param to the end location of the displacement vector.
     * @return the information about the segment moving.
     */
    fun moveSegment(segmentIndex: Int, from: Point2D, to: Point2D): MoveEdgeSegmentInfo

    /**
     * Moves a segment of this [EdgeView] and returns information about the segment moving.
     * @param segmentIndex the index of the segment that is to be moved.
     * @param offset the offset of the moving.
     * @return the information about the segment moving.
     */
    fun moveSegment(segmentIndex: Int, offset: Double): MoveEdgeSegmentInfo

    /**
     * Returns the [Direction] of the segment with the specified index, if it is not degenerated.
     * @param segmentIndex the index of the segment
     * @return the [Direction] of the segment with index `segmentIndex`
     */
    fun getSegmentDirection(segmentIndex: Int): Direction?

    /**
     * Calculates the length of the longest [EdgeView] path starting with this [EdgeView].
     * Traverses the entire net built by [NodeView] and their outgoing [EdgeView] that is reachable by this
     * [EdgeView].
     * @param reverse `false` if the [EdgeView] should be traversed from origin to destination, `true`
     * if it should be traversed from destination to origin
     * @return the length of the longest subnet starting with this [EdgeView].
     */
    fun calculateMaximumNetLength(reverse: Boolean): Double

    /**
     * Splits this [EdgeView] at a particular segment and a particular (x,y) location.
     *
     * Trims the tail of this [EdgeView] at the specified segment and location, and returns the trimmed tail as a
     * new [EdgeView].
     * @param index the index of the segment where the splitting occurs
     * @param splitLocation the location where the splitting occurs
     * @param edgeViewCreator used to create the new [EdgeView] tail instance.
     * @return the newly created [EdgeView] that represents the tail part that has been spit apart from this [EdgeView].
     */
    fun split(index: Int, splitLocation: Point2D, edgeViewCreator: (NetView<T>) -> EdgeView<T>): EdgeView<T>

    /**
     * Joins this [EdgeView] with another adjacent [EdgeView].
     * Adds all segment point of the adjacent [EdgeView] either at the head or the tail of this [EdgeView].
     * Connects itself to the [ConnectableView] to the corresponding end of the adjacent [EdgeView]
     * @param edgeView the [EdgeView] to join. Must be adjacent.
     * @return the remaining [EdgeView]
     * @throws IllegalArgumentException if `edgeView` is not adjacent
     */
    fun join(edgeView: EdgeView<T>): EdgeView<*>

	/**
	 * Snaps the point given by [x] and [y] to the location on this [EdgeView] that is nearest to any of
	 * the snapping coordinates defined by the specified [Snapper].
	 * @return a [EdgeViewSnapLocatorResult] or `null` if no suitable location was found, for example if too far
	 * away from the [EdgeView], or not on a perpendicular segment.
	 */
	fun snap(x: Double, y: Double, snapManager: SnapManager? = null): EdgeViewSnapLocatorResult?
}

data class EdgeViewSnapLocatorResult(val segmentIndex: Int, val x: Double, val y: Double) {
	val location: Point2D get() = Point2D(x, y)
}

/**
 * Contains information being used while interactively dragging a segment of an [EdgeView].
 * @property segmentIndex The new index of the segment being moved. Might change during move because of re-layouts
 * @property offset The number of pixels that the segment has been moved perpendicular to the segment
 */
data class MoveEdgeSegmentInfo(val segmentIndex: Int, val offset: Double)

