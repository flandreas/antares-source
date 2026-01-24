package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.polyline.LineTerminator
import ch.scorpion.jabbah.draw.polyline.Polyline
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.connect.AbstractDragEdgeViewEndpointConnector
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.PortView

/**
 * Represents the two possible types of endpoints of an [EdgeView].
 */
enum class EdgeViewEndpointType {

    ORIGIN {

        override val dragConnector: AbstractDragEdgeViewEndpointConnector get() =
            GraphViewModule.dragEdgeViewOriginConnector

        override fun canConnectTo(port: Port<out Any>): Boolean {
            return port.portType.isOutput
        }

        override fun moveTo(edgeView: EdgeView<*>, point: Point2D) {
            edgeView.moveOriginEndPoint(point.x, point.y)
        }

        override fun layout(edgeView: EdgeView<*>, dirs: Set<Direction>?) {
            edgeView.layout.layoutOrigin(dirs)
        }

        override fun getEndpoint(edgeView: EdgeView<*>): EdgeEndpointView {
            return edgeView.originEndpointView
        }

        override fun getDirection(edgeView: EdgeView<*>): Direction? {
            return edgeView.getSegmentDirection(0)
        }

        override fun getLocation(edgeView: EdgeView<*>): Point2D {
            return edgeView.polyline.getFirstPoint()
        }

        override fun getDirectionForPortView(portView: PortView<*>): Direction {
            return portView.relativeDirection
        }

	    override fun adjustTo(edgeView: EdgeView<*>, layoutIndex: Int, location: Point2D, origDirs: Set<Direction>?, destDirs: Set<Direction>?) {
		    edgeView.layout.adjustOrigin(layoutIndex, location, origDirs, destDirs)
	    }

	    override fun remove(edgeView: EdgeView<*>) {
		    edgeView.removeSegmentPoint(0)
	    }

	    override fun getLineTerminator(edgeView: EdgeView<*>): LineTerminator? =
			edgeView.polyline.beginLineTerminator
    },

    DESTINATION {

        override val dragConnector: AbstractDragEdgeViewEndpointConnector get() =
            GraphViewModule.dragEdgeViewDestinationConnector

        override fun canConnectTo(port: Port<out Any>): Boolean {
            return port.portType.isInput
        }

        override fun moveTo(edgeView: EdgeView<*>, point: Point2D) {
            edgeView.moveDestinationEndPoint(point.x, point.y)
        }

        override fun layout(edgeView: EdgeView<*>, dirs: Set<Direction>?) {
            edgeView.layout.layoutDestination(dirs)
        }

        override fun getEndpoint(edgeView: EdgeView<*>): EdgeEndpointView {
            return edgeView.destinationEndpointView
        }

        override fun getDirection(edgeView: EdgeView<*>): Direction? {
            return edgeView.getSegmentDirection(edgeView.segmentPointCount - 2)
        }

        override fun getLocation(edgeView: EdgeView<*>): Point2D {
            return edgeView.polyline.getLastPoint()
        }

        override fun getDirectionForPortView(portView: PortView<*>): Direction {
            return portView.relativeDirection.opposite()
        }

	    override fun adjustTo(edgeView: EdgeView<*>, layoutIndex: Int, location: Point2D, origDirs: Set<Direction>?, destDirs: Set<Direction>?) {
		    edgeView.layout.adjustDestination(layoutIndex, location, origDirs, destDirs)
	    }

	    override fun remove(edgeView: EdgeView<*>) {
		    edgeView.removeSegmentPoint(edgeView.segmentPointCount - 1)
	    }

	    override fun getLineTerminator(edgeView: EdgeView<*>): LineTerminator? =
			edgeView.polyline.endLineTerminator
    };

	val opposite: EdgeViewEndpointType get() =
		when (this) {
			ORIGIN -> DESTINATION
			DESTINATION -> ORIGIN
		}

    abstract val dragConnector: AbstractDragEdgeViewEndpointConnector

    /**
     * Determines whether an endpoint of this type can connect to the specified [Port].
     * @param port the [Port] to connect to
     * @param net the [Net] to which [port] is supposed to be connected, if already existing
     */
    fun canConnectTo(port: Port<out Any>, net: Net<out Any>?, graphView: GraphView): Boolean =
        port.portType.isInput || (port is OutputPort && net != null && port.canConnectToNet(net, graphView))

    abstract fun canConnectTo(port: Port<out Any>): Boolean

	/**
	 * Determines whether an endpoint of this type can connect to the specified [Net]
	 * @param destNet the [Net] to connect to
	 * @param origPort the [Port] to which [destNet] is supposed to be connected
	 */
	fun canConnectTo(destNet: Net<out Any>, origPort: Port<out Any>?, graphView: GraphView): Boolean =
		origPort == null
			|| (origPort.portType.isInput)
			|| (origPort is OutputPort<*>) && origPort.canConnectToNet(destNet, graphView)

    /** Moves this endpoint of an [EdgeView] to the specified location. */
    abstract fun moveTo(edgeView: EdgeView<*>, point: Point2D)

	/** Adjusts this endpoint of an [EdgeView] to the specified location, restricting layout the [EdgeView] point with index [layoutIndex].*/
	abstract fun adjustTo(edgeView: EdgeView<*>, layoutIndex: Int, location: Point2D, origDirs: Set<Direction>?, destDir: Set<Direction>?)

    /** Layouts the [EdgeView] at this endpoint with the preferred [Direction]s at the endpoint. */
    abstract fun layout(edgeView: EdgeView<*>, dirs: Set<Direction>?)

    abstract fun getEndpoint(edgeView: EdgeView<*>): EdgeEndpointView

    /**
     * Returns the [Direction] of the segment at the corresponding endpoint.
     * Only available for orthogonal [EdgeView]s.
     */
    abstract fun getDirection(edgeView: EdgeView<*>): Direction?

    /**
     * Returns the desired [Direction] of the segment at the corresponding endpoint
     * that fits the [Direction] of the specified [PortView]. This is used for layout.
     * Example: The origin segment connected to a [PortView] facing [Direction.EAST] should also
     * face [Direction.EAST], while a destination segment connected to the same [PortView]
     * should face [Direction.WEST].
     */
    abstract fun getDirectionForPortView(portView: PortView<*>): Direction

	/** Returns the location of the corresponding [EdgeView] endpoint. */
    abstract fun getLocation(edgeView: EdgeView<*>): Point2D

	/** Removes the corresponding endpoint from [EdgeView] and makes the second last point the new endpoint. */
	abstract fun remove(edgeView: EdgeView<*>)

	/** Returns the [LineTerminator] at the corresponding endpoint of the [EdgeView]'s [Polyline]. */
	abstract fun getLineTerminator(edgeView: EdgeView<*>): LineTerminator?
}