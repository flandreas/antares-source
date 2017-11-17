package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.io.StorableCreator
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.SubGraphOutputPort

/**
 * A [Vertice] that contains an inner [Graph], thus enabling recursively nested [Graph] structures.
 *
 * A [SubGraphVertice] doesn't perform any calculations by itself. Its [InputPort]s are
 * [SubGraphInputPort]s that forward an arriving signal immediately to the corresponding [GraphInput] of the
 * contained [Graph]. Symmetrically, its [OutputPort]s are [SubGraphOutputPort]s that receive their
 * signals from the corresponding [GraphOutput]s of the contained [Graph], which they forward to the outer
 * [Graph] through the connected [Net]s.
 */
interface SubGraphVertice : Vertice {

    /** Holds the [UUID] of the [Graph] that this [SubGraphVertice] contains.*/
    var graphUUID: UUID?

    /**
     * Returns the [Graph] that this [SubGraphVertice] contains, if already present.
     * The [Graph] is not present before [GraphElement.bind]
     */
    fun getGraphIfPresent(): Graph?

    /** Returns the contained [Graph] by loading and copying it from the specified [Library] if not already loaded.*/
    fun getGraph(library: Library, storableCreator: StorableCreator): Graph

    fun <T: Any> propagateOutput(outputPort: SubGraphOutputPort<T>, signal: T, signalHandler: SignalHandler)
}