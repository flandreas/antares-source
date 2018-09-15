package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * A [VerticeView] that is created by a [ControlViewSource] and to be used to add to a [SubGraphVerticeView].
 * @param <T> the type of the model
 */
interface ControlView<T : Vertice> : VerticeView<T> {

    val controlId: String?

    /**
     * Binds this [ControlView] to the corresponding [Vertice] of the [Graph] that is contained in the
     * [SubGraphVerticeView] that owns this [ControlView]. Used for establishing a process to update this [ControlView]
     * whenever the corresponding [Vertice] changes.
     */
    fun bindToModel(model: T)
}