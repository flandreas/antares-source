package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.port.PortView


/**
 * Represents the two possible types of endpoints of an [EdgeView].
 */
enum class EdgeViewEndpointType {

    ORIGIN {

        override fun canConnectTo(portType: PortType): Boolean {
            return portType.isOutput
        }

        override fun moveTo(edgeView: EdgeView<*>, point: Point2D) {
            edgeView.moveOriginEndPoint(point.x, point.y)
        }

        override fun layout(edgeView: EdgeView<*>, direction: Direction?) {
            edgeView.layout.layoutOrigin(direction)
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

	    override fun adjustTo(edgeView: EdgeView<*>, layoutIndex: Int, direction: Direction?, location: Point2D) {
		    edgeView.layout.adjustOrigin(layoutIndex, direction, location)
	    }

	    override fun remove(edgeView: EdgeView<*>) {
		    edgeView.removeSegmentPoint(0)
	    }
    },

    DESTINATION {

        override fun canConnectTo(portType: PortType): Boolean {
            return portType.isInput
        }

        override fun moveTo(edgeView: EdgeView<*>, point: Point2D) {
            edgeView.moveDestinationEndPoint(point.x, point.y)
        }

        override fun layout(edgeView: EdgeView<*>, direction: Direction?) {
            edgeView.layout.layoutDestination(direction)
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

	    override fun adjustTo(edgeView: EdgeView<*>, layoutIndex: Int, direction: Direction?, location: Point2D) {
		    edgeView.layout.adjustDestination(layoutIndex, direction, location)
	    }

	    override fun remove(edgeView: EdgeView<*>) {
		    edgeView.removeSegmentPoint(edgeView.segmentPointCount - 1)
	    }
    };

    /** Determines whether and endpoint of this type can connect to a [Port] of the specified [PortType].*/
    abstract fun canConnectTo(portType: PortType): Boolean

    /** Moves this endpoint of an [EdgeView] to the specified location. */
    abstract fun moveTo(edgeView: EdgeView<*>, point: Point2D)

	/** Adjusts this endpoint of an [EdgeView] to the specified location, restricting layout the [EdgeView] point with index [layoutIndex].*/
	abstract fun adjustTo(edgeView: EdgeView<*>, layoutIndex: Int, direction: Direction? = null, location: Point2D)

    /** Layouts the [EdgeView] at this endpoint with the preferred [Direction] at the endpoint. */
    abstract fun layout(edgeView: EdgeView<*>, direction: Direction?)

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
}