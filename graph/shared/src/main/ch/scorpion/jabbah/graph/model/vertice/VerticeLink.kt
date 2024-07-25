package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.Vertice

/**
 * A link to a [Vertice] supporting late binding. Used in conjunction with undo/redo, which must avoid
 * object references, because those can change due to undo/redo snapshot exchanges.
 */
interface VerticeLink {

    /**
     * Returns the [Vertice] that this [VerticeLink] is pointing to, starting with the specified [Graph].
     * @param startGraph the [Graph] where resolving is started
     * @throws IllegalArgumentException if any of the [Vertice]s in the referencing path cannot be resolved
     */
    fun getLinkedVertice(startGraph: Graph): Vertice
}

class ImmediateVerticeLink(val id: Int) : VerticeLink {

    override fun getLinkedVertice(startGraph: Graph): Vertice = startGraph.withId(id) as Vertice
}