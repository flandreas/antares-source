package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.Vertice

/**
 * A link to object supporting late binding. Used in conjunction with undo/redo, which must avoid
 * object references, because those can change due to undo/redo snapshot exchanges.
 */
interface ObjectLink<out T: Any> {

    /**
     * Returns the object that this [ObjectLink] is pointing to, starting with the specified [Graph].
     * @param startGraph the [Graph] where resolving is started. `null` if the linked objects are not part of a [Graph]
     * @throws IllegalArgumentException if any of the objects in the referencing path cannot be resolved
     */
    fun getLinkedObject(startGraph: Graph?): T
}

interface VerticeLink : ObjectLink<Vertice>

class ImmediateVerticeLink(val id: Int) : VerticeLink {

    override fun getLinkedObject(startGraph: Graph?): Vertice = startGraph!!.withId(id) as Vertice
}