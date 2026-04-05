package io.antarescircuit.jabbah.graph.model.graph

import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.GraphElement
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.io.GlobalIdentityCreator
import io.antarescircuit.jabbah.io.GlobalIdentityProvider
import io.antarescircuit.jabbah.io.Storable

/**
 * Redirects identity providing requests for a [GraphView]'s [Graph]'s objects to
 * those of another [Graph], which is used when opening (cloning) a new [GraphView]
 * on an existing [Graph].
 */
class GraphGlobalIdentityProvider(
	private val otherGraph: Graph,
	private val backend: GlobalIdentityProvider = GlobalIdentityCreator()
) : GlobalIdentityProvider {

	override fun provideIdentity(storable: Storable): Int {
		// Should always exist, but there are files out there with "modelId=-1"
		return otherStorable(storable)?.let { backend.provideIdentity(it) } ?: -1
	}

	override fun getIdentity(storable: Storable): Int =
		backend.getIdentity(storable)

	override fun getStorableWithIdentity(globalId: Int): Storable? =
		backend.getStorableWithIdentity(globalId)

	private fun otherStorable(storable: Storable): Storable? {
		return when (storable) {
			is GraphElement -> getOtherGraphElement(storable)
			else -> storable
		}
	}

	private fun getOtherGraphElement(graphElement: GraphElement): GraphElement? =
		otherGraph.withId(graphElement.id)
}