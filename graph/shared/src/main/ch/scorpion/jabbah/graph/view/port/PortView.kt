package ch.scorpion.jabbah.graph.view.port

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.edit.SnappableX
import ch.scorpion.jabbah.edit.SnappableY
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.io.Storable

/**
 * A part of a [VerticeView] that represents a graphical representation of a [Port].
 * @param T the type of signal that this [PortView]'s [Port] can consume or produce.
 */
interface PortView<T: Any> : Drawable, Storable, SnappableX, SnappableY {

    companion object {

        /**
         * The name of the property in [Properties] that designates the size of the sensitive area around the
         * connection point of a [PortView]. Used for interactive connecting.
         */
        val PROP_SENSITIVE_AREA = "graph.view.portView.sensitiveArea"

        /**
         * The name of the property in [Properties] that designates the object to be used for highlighting the
         * connection point of a [PortView] while interactively connecting.
         */
        val PROP_HIGHLIGHT = "graph.view.portView.hightlight"
    }

    /** The [Port] that this [PortView] displays. Must be one of the [Port]s of the [owner]'s [Vertice].*/
    var port: Port<T>

    /**
     * The [VerticeView] that owns this [PortView]. Needed for rotation behaviour and for determining
     * the connection point while connecting.
     */
    var owner: VerticeView<*>?

    /**
     * Determines whether an [EdgeView] can be connected to this [PortView]. This will generally be `true`,
     * but there are cases when you want to design a [VerticeView] with invisible [PortView]s, such as the
     * probes of an oscilloscope that connect only on the model layer, but not on the view layer.
     */
    val connectable: Boolean

    /** The relative location of this [PortView] within the owning [VerticeView].*/
    var location: Point2D

    val locationX: Double get() = location.x

    val locationY: Double get() = location.y

    /** The absolute {@link Direction} into which this {@link PortView} points when not being rotated. */
    var direction: Direction

    /** Returns the [direction] rotated by [ownerRotation].*/
    val relativeDirection: Direction get() = ownerRotation.rotateDirection(direction)

    /**
     * The {@link Rotation} around the relative location of this {@link PortView} within the owning {@link VerticeView}.
     * Is the same as the `rotation` property of the owning [VerticeView]. Modelled as variable so that value updates
     * from [VerticeView] can lead to an update of this [PortView].
     */
    var ownerRotation: Rotation

    /**
     * Returns the [Point2D] where [EdgeView]s are connected relative to the owner's location.
     * This point may vary depending on whether the [Port] is connected to an [EdgeView] or not.
     */
    val connectionPoint: Point2D

    /**
     * Returns the [Point2D] where [EdgeView]s are connected if the [Port] is not yet connected.
     * Returns the same value as [connectionPoint] if not yet connected, Might return a different one if connected.
     */
    val unconnectedConnectionPoint: Point2D

    /**
     * Returns the current distance (in [direction]) between the origin and the connection point of this
     * [PortView].
     */
    val length: Int

    /**
     * Returns the distance (in [direction]) between the origin and the connection point of this
     * [PortView] when this [PortView] is not connected to an [EdgeView].
     */
    val unconnectedLength: Int

    /**
     * Returns the minimum length of the first or last segment of a [EdgeView] connected to this [PortView].
     * This property is used to control [EdgeView] layouting algorithms. Not used for unconnected [PortView]s.
     */
    val minSegmentLength: Int

    /** Disposes this [PortView] by detaching it from its model and releasing all resources.*/
    fun dispose()

    /** Sets the relative location of this [PortView] within the owning [VerticeView].*/
    fun setLocation(x: Double, y: Double)

    fun setLocation(x: Int, y: Int) = setLocation(x.toDouble(), y.toDouble())

    /**
     * Determines whether a given location is contained in the sensitive area of this [PortView]'s connection point.
     * @param x the x-coordinate of the [Point2D] relative to the owning {@link VerticeView}.
     * @param y the x-coordinate of the [Point2D] relative to the owning {@link VerticeView}.
     * @return `true` if (x,y) is contained in the sensitive area of this [PortView]'s connection point.
     */
    fun containsConnectionPoint(x: Double, y: Double): Boolean

    /** Sets the name of the [Port] that this [PortView] displays.*/
    fun setPortName(name: String)

    /**
     * Notifies this [PortView] that an [EdgeView] has connected its [Net] to the [Port] of this
     * [PortView]. As a reaction, this [PortView] could update its geometry, if necessary.
     */
    fun handleConnect(edgeView: EdgeView<T>)

    /**
     * Notifies this [PortView] that an [EdgeView] has disconnected its [Net] from the [Port] of
     * this [PortView]. As a reaction, this [PortView] could update its geometry, if necessary.
     * This method also supports unconnecting when the [EdgeView] is not known, which is used e.g.
     * during copy/paste. This is needed in order to reset the length to the unconnected length.
     * However, this is an exceptional use case; normally, you should provide the [EdgeView] from which
     * to unconnect this [PortView].
     */
    fun handleUnconnect(edgeView: EdgeView<T>?)

    /**
     * Reuse properties from another [PortView].
     *
     * Primarily used when a [VerticeView] re-initializes itself after a heavy change by recreating all
     * [PortView]s, and the newly created [PortView] must reuse properties from the corresponding old one,
     * most significantly connection information like the length of the connection line.
     */
    fun reuseFrom(portView: PortView<*>)

    /**
     * Returns a short description of this [PortView] to be displayed as a tool tip during execution.
     * @param x the x-coordinate of the mouse position
     * @param y the y-coordinate of the mouse position
     * @param width the width in pixels to which multi-line text is to be formatted, or `null` if to width is to be enforced
     * @return the tool tip text of this [PortView].
     */
    fun getExecutionToolTipText(x: Double, y: Double, width: Int?): String?

}