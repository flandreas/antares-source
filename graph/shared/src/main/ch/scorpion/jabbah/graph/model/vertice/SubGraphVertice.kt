package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.edit.model.text.description.Describable
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.*

/**
 * A [Vertice] that contains an inner [Graph], thus enabling recursively nested [Graph] structures.
 *
 * A [SubGraphVertice] doesn't perform any calculations by itself. Its [InputPort]s are
 * [SubGraphInputPort]s that forward an arriving signal immediately to the corresponding [GraphInput] of the
 * contained [Graph]. Symmetrically, its [OutputPort]s are [SubGraphOutputPort]s that receive their
 * signals from the corresponding [GraphOutput]s of the contained [Graph], which they forward to the outer
 * [Graph] through the connected [Net]s.
 */
interface SubGraphVertice : Vertice, Describable {

    /** Holds the [UUID] of the [Graph] that this [SubGraphVertice] contains.*/
    var graphUUID: UUID?

	/** Contains the displayable name of the [Graph] represented by this [SubGraphVertice] as various translations.*/
	var graphName: Name

    /**
     * Returns the [Graph] that this [SubGraphVertice] contains, if already present.
     * The [Graph] is not present before [GraphElement.bind]
     */
    fun getGraphIfPresent(): Graph?

    /** Returns the contained [Graph] by loading and copying it from the specified [MetaGraphRepository] if not already loaded.*/
    fun getGraph(repository: MetaGraphRepository): Graph

    fun <T: Any> propagateOutput(outputPort: SubGraphOutputPort<T>, signal: T, signalHandler: SignalHandler)
}