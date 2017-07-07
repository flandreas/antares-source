package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.EdgeView


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
            edgeView.layoutOrigin()
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
    },

    DESTINATION {

        override fun canConnectTo(portType: PortType): Boolean {
            return portType.isInput
        }

        override fun moveTo(edgeView: EdgeView<*>, point: Point2D) {
            edgeView.moveDestinationEndPoint(point.x, point.y)
        }

        override fun layout(edgeView: EdgeView<*>, direction: Direction?) {
            edgeView.layoutDestination(direction)
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

    };

    /** Determines whether and endpoint of this type can connect to a [Port] of the specified [PortType].*/
    abstract fun canConnectTo(portType: PortType): Boolean

    /** Moves this endpoint of an [EdgeView] to the specified location. */
    abstract fun moveTo(edgeView: EdgeView<*>, point: Point2D)

    /** Layouts the [EdgeView] at this endpoint with the preferred [Direction] at the endpoint. */
    abstract fun layout(edgeView: EdgeView<*>, direction: Direction?)

    abstract fun getEndpoint(edgeView: EdgeView<*>): EdgeEndpointView

    /**
     * Returns the [Direction] of the segment at the corresponding endpoint.
     * Only available for orthogonal [EdgeView]s.
     */
    abstract fun getDirection(edgeView: EdgeView<*>): Direction?

    abstract fun getLocation(edgeView: EdgeView<*>): Point2D
}