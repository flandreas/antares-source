package ch.scorpion.jabbah.graph.view.net.node

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.graph.view.ConnectableView
import ch.scorpion.jabbah.graph.view.NetViewElement
import ch.scorpion.jabbah.graph.view.EdgeView

/**
 * A graphical representation of a junction of [EdgeView]s.
 */
interface NodeView<T: Any> : NetViewElement<T>, ConnectableView {

    /**
     * Returns the [EdgeView]s that have this [NodeView] as their origin. .
     * @return the outgoing [EdgeView]s of this [NodeView].
     */
    fun getOutgoingEdgeViews(): List<EdgeView<T>>

    /**
     * Returns all [EdgeView]s that are connected to this [NodeView].
     * @return the [EdgeView]'s of this [NodeView].
     */
    fun getEdgeViews(): List<EdgeView<T>>

    /**
     * Returns the [EdgeView] (if any) that leaves this [NodeView] at the specified [Direction],
     * independent of whether the [EdgeView] is an incoming or an outgoing [EdgeView] (in terms of
     * [EdgeView.getOrigin] and [EdgeView.getDestination]).
     */
    fun getEdgeView(direction: Direction): EdgeView<T>?

    /**
     * Determines whether any incoming or outgoing [EdgeView] connected to this [NodeView] contains the
     * specified point.
     *
     * @param x the x-coordinate of the specific point.
     * @param y the y-coordinate of the specific point.
     * @param excludedEdgeView an optional [EdgeView] to exclude from consideration.
     * @return `true` if any of the incoming or outgoing [EdgeView] exluding `excludedEdgeView`
     *      contains the point `(x,y)`.
     */
    fun anyEdgeViewContainsPoint(x: Double, y: Double, excludedEdgeView: EdgeView<*>?): Boolean

    /** Returns the [EdgeView] (if any) that has this [NodeView] as its destination.*/
    fun getIncomingEdgeView(): EdgeView<T>?
}