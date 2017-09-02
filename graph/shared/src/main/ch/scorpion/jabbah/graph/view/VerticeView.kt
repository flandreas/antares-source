package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.port.PortView

/**
 * Represents a graphical representation of a [Vertice].
 * After completion of construction, a [VerticeView] must have at least one [PortView], because features
 * like snapping try to determine connection points, which are derived from [PortView].
 * @param T the type of [Vertice] that this [VerticeView] graphically represents.
 */
interface VerticeView<T : Vertice> : GraphElementView<T>, ConnectableView, ActorView {

    val vertice: Vertice get() = model as T

    /**
     * Returns a short description that explains the purpose of this {@link Vertice}.
     * Example: "Inverts the signal arriving at its single input isPort and provides the inverted signal at its single output"
     * @return a translated short description of this [VerticeView]
     */
    val shortDescription: String?

    /** Returns the number of [PortView]s of this [VerticeView].*/
    val portViewCount: Int

    /** Determines whether this [VerticeView] shows its [PortView]s or not.*/
    var isShowPortViews: Boolean

    /** Adds the specified [PortView] to this [VerticeView]. */
    fun addPortView(portView: PortView<*>)

    /** Removes the specified [PortView] from this [VerticeView]. */
    fun removePortView(portView: PortView<*>)

    /** Returns the [PortView]s of this [VerticeView].*/
    fun getPortViews(): ImmutableList<PortView<*>>

    /**
     * Returns the [PortView] at the specified absolute location.
     * @return the [PortView] at `(x, y)`, if any.
     */
    fun getPortViewAt(x: Double, y: Double): PortView<*>?
}