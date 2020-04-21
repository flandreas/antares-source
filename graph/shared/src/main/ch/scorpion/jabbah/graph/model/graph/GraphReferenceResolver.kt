package ch.scorpion.jabbah.graph.model.graph

import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.io.Reference
import ch.scorpion.jabbah.io.ReferenceResolver
import ch.scorpion.jabbah.io.Storable

/**
 * Resolves references of [Storable]s to [GraphElement]s of an existing [Graph].
 */
class GraphReferenceResolver(val graph: Graph) : ReferenceResolver {

    override fun addStorable(globalId: Int, storable: Storable) {
        throw UnsupportedOperationException("inappropriate method call")
    }

    override fun <T: Storable> getStorable(globalId: Int): T? {
        return graph.withStorableId<GraphElement>(globalId) as T?
    }

    override fun requestResolution(requester: Storable, reference: Reference) {
        throw UnsupportedOperationException("inappropriate method call")
    }

    override fun resolveReferences() {
        throw UnsupportedOperationException("inappropriate method call")
    }

    override fun resolveReferences(referenceResolver: ReferenceResolver) {
        throw UnsupportedOperationException("inappropriate method call")
    }
}