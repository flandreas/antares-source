package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.draw.DrawableListener
import ch.scorpion.jabbah.draw.polyline.Polyline
import ch.scorpion.jabbah.draw.polyline.PolylineShape
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Snapper
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.view.connect.EdgeToPortConnector
import ch.scorpion.jabbah.graph.view.net.edge.*
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
interface EdgeView<T: Any> : NetViewElement<T> {

	companion object {

		/** The size of the rectangular area defining whether a point is "inside" this [EdgeView]. Used for snapping.*/
		const val containsSize: Int = 4

		/** The maximum distance for regarding a snap point as being on a corner of this [EdgeView]. Used for snapping.*/
		const val edgeCornerDistance: Int = 15
	}

	/** The [ConnectableView] from which this [EdgeView] originates.*/
    var origin: ConnectableView?

    /** The [Port] of the model of [origin] where this [EdgeView] originates.*/
    var originPort: Port<T>?

    /** The [ConnectableView] that is the destination of this [EdgeView].*/
    var destination: ConnectableView?

    /** The [Port] of the model of [destination] that is the destination of [EdgeView].*/
    var destinationPort: Port<T>?

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

    /** Determines how the segments of this [EdgeView] are automatically laid out, e.g. orthogonally.*/
    //var layoutType: LayoutType
	val layout: EdgeViewLayout

    /** Determines whether this [EdgeView] displays an arrow head at its destination. */
    var isArrow: Boolean

    /** Returns the destination [EdgeEndpointView] of this [EdgeView].*/
    val originEndpointView: EdgeEndpointView

    /** Returns the destination [EdgeEndpointView] of this [EdgeView]. */
    val destinationEndpointView: EdgeEndpointView

    /** Holds the [Point2D]s (in absolute coordinates) that define the segments of this [EdgeView].*/
    val polyline: PolylineShape

    /** Indicates the combination of [Port]s an [EdgeView] is connected with.*/
    val connectionState: EdgeViewConnectionState

	val edgeToPortConnectorSupplier: () -> EdgeToPortConnector

    /**
     * Returns the [ConnectableView] that corresponds with the specified [Port], which can be either the
     * [origin] or the [destination] [ConnectableView].
     */
    fun getConnectableView(port: Port<T>): ConnectableView?

    /** Returns the start [Point2D] of the segment with the specified index.*/
    fun getSegmentPoint(index: Int): Point2D

    /**
     * Adds the specified [Point2D] at the end of this [EdgeView].
     * @return this [EdgeView] to support method chaining
     */
    fun addSegmentPoint(point: Point2D): EdgeView<T>

    /**
     * Adds the specified [Point2D] at the the given index.
     * @returns this [EdgeView] to support method chaining
     */
    fun addSegmentPoint(index: Int, point: Point2D): EdgeView<T>

    /**
     * Compacts this [EdgeView] by removing [Point2D]s that are at the same location as their predecessor
     * [Point2D], or that are intermediate points of two parallel segments.
     */
    fun compact()

	fun setLaidOutPoints(points: List<Point2D>)

    /**
     * Connects this [EdgeView] to the [Port] of the origin [ConnectableView].
     * Note that this method does **NOT** establish the connection on the model layer.
     * @param origin the [ConnectableView] from which this [EdgeView] originates, or `null` to
     * disconnect this [EdgeView] from its former origin [ConnectableView].
     */
    fun connectToOrigin(origin: ConnectableView?, port: Port<T>?)

    /**
     * Connects this [EdgeView] to the [Port] of the destination [ConnectableView].
     * Note that this method does **NOT** establish the connection on the model layer.
     * @param destination the [ConnectableView] that is the destination of this [EdgeView], or `null`
     * to disconnect this [EdgeView] fro its former destination [ConnectableView].
     */
    fun connectToDestination(destination: ConnectableView?, port: Port<T>?)

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
    fun split(index: Int, splitLocation: Point2D, edgeViewCreator: (Net<T>) -> EdgeView<T>): EdgeView<T>

    /**
     * Joins this [EdgeView] with another adjacent [EdgeView].
     * Adds all segment point of the adjacent [EdgeView] either at the head or the tail of this [EdgeView].
     * Connects itself to the [ConnectableView] to the corresponding end of the adjacent [EdgeView]
     * @param edgeView the [EdgeView] to join. Must be adjacent.
     * @return the remaining [EdgeView]
     * @throws IllegalArgumentException if `edgeView` is not adjacent
     */
    fun join(edgeView: EdgeView<T>): EdgeView<*>

	fun snap(x: Double, y: Double, backgroundSnapper: Snapper? = null): EdgeViewSnapLocatorResult?
}

data class EdgeViewSnapLocatorResult(val segmentIndex: Int, val x: Double, val y: Double)

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

	fun updateAdjusted()

	fun handleOriginChanged()

	fun handleDestinationChanged()

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
}

/**
 * Indicates the combination of [Port]s an [EdgeView] is connected with.
 * Connections to [NodeView] are ignored, because they don't have [Port]s.
 */
enum class EdgeViewConnectionState {
    /** None of both ends is connected*/
    Unconnected,
    /** One end is connected with an [InputPort], the other end is open.*/
    Input,
    /** One end is connected with an [OutputPort], the other end is open.*/
    Output,
    /** One end is connected with an [InputPort], the other end is connected with an [OutputPort].*/
    InputOutput,
    /** Both ends are connected with [InputPort]s, which can result from deleting a segment leading to a [NodeView].*/
    InputInput,
    /** Both ends are connected with [OutputPort]s, which can result from deleting a segment leading to a [NodeView].*/
    OutputOutput
}

/**
 * Contains information being used while interactively dragging a segment of an [EdgeView].
 * @property segmentIndex The new index of the segment being moved. Might change during move because of re-layouts
 * @property offset The number of pixels that the segment has been moved perpendicular to the segment
 */
data class MoveEdgeSegmentInfo(val segmentIndex: Int, val offset: Double)

