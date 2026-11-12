package io.antarescircuit.jabbah.io

import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.collection.DirectedGraph
import io.antarescircuit.jabbah.base.collection.IdentityKey
import io.antarescircuit.jabbah.base.collection.TopologicalSort
import io.antarescircuit.jabbah.base.logger

/**
 * This implementation of [ReferenceResolver] does a topological sort of the references before resolving them,
 * so that leaf references are resolved before references which reference other references.
 */
class ReferenceResolverImpl(
	private val identityProvider: GlobalIdentityProvider? = null
) : ReferenceResolver {

	companion object {
		private val LOG by logger(ReferenceResolverImpl::class)
	}

	/** Maps global persistent IDs to object references.*/
	private val map = mutableMapOf<Int, Storable>()

	/** Maps object references to the first global persistent ID they were added with, reversing [map]. */
	private val globalIds = HashMap<IdentityKey<Storable>, Int>()

	/**
	 * Maps [Storable]s to the [Reference]s they wish to resolve after all [Storable]s have been instantiated.
	 * Its insertion order maintains the order of [Storable]s in which they have requested a reference resolution.
	 * This is for avoiding requests for resolving unestablished references with cross-layer references.
	 */
	private val resolutionRequestMap = LinkedHashMap<Storable, MutableList<Reference>>()

	/** ---- [ReferenceResolver] */

	override fun addStorable(globalId: Int, storable: Storable) {
		if (LOG.isTraceEnabled()) {
			LOG.trace("add storable $globalId")
		}
		map[globalId] = storable
		globalIds.getOrPut(IdentityKey(storable)) { globalId }
	}

	override fun <T: Storable> getStorable(globalId: Int): T? {
		if (LOG.isTraceEnabled()) {
			LOG.trace("getStorable for id $globalId")
		}

		@Suppress("UNCHECKED_CAST")
		return map[globalId] as T? ?: identityProvider?.getStorableWithIdentity(globalId) as T?
	}

	override fun getGlobalId(storable: Storable): Int {
		return findGlobalId(storable)
			?: identityProvider?.getIdentity(storable)
			?: throw IllegalArgumentException("storable ${storable::class.simpleName} not available")
	}

	override fun requestResolution(requester: Storable, reference: Reference) {
		val list = resolutionRequestMap.getOrPut(requester) { mutableListOf() }
		if (!list.contains(reference)) {
			list.add(reference)
		}
	}

	override fun resolveReferences() {
		resolveReferences(this)
	}

	override fun resolveReferences(referenceResolver: ReferenceResolver) {
		val graph = buildGraph()
		val list = TopologicalSort.sort(graph)

		for (storable in list) {
			for (reference in resolutionRequestMap[storable]!!) {
				if (!reference.dummy) {
					try {
						storable.resolve(reference, referenceResolver)
					} catch (e: Throwable) {
						LOG.error("error while resolving class '${System.getClassName(storable)}': ${e.message}")
						throw e
					}
				}
			}
		}

		list.forEach {
			it.resolutionDone()
		}

		list.forEach {
			it.allResolutionDone()
		}
	}

	/** ---- [ReferenceResolverImpl] */

	/** Returns the first global ID still mapped to [storable] in [map], or `null` if there is none. */
	private fun findGlobalId(storable: Storable): Int? {
		val globalId = globalIds[IdentityKey(storable)] ?: return null
		if (map[globalId] === storable) {
			return globalId
		}
		// The first ID has been reassigned to another Storable (duplicate IDs), so fall back to a full scan
		return map.entries.firstOrNull { it.value === storable }?.key
	}

	/**
	 * Add dummy [Reference]s for [Storable]s that occur as targets of "resolve after" to make sure that
	 * they occur as a graph node even if they didn't request a resolution by themselves.
	 */
	private fun addDummyReferences() {
		val set = mutableSetOf<Storable>()
		for (references in resolutionRequestMap.values) {
			for (reference in references) {
				reference.resolveAfter?.forEach {
					val storable: Storable? = getStorable(it)
					if (storable != null && storable !in resolutionRequestMap) {
						set.add(storable)
					}
				}
			}
		}
		set.forEach {
			requestResolution(it, Reference(name = "dummy", dummy = true))
		}
	}

	/**
	 * Builds a dependency graph of all [Reference]s, which allows to perform a topological sort of these
	 * References.
	 * @return the [DirectedGraph] representing the dependency graph of all [Reference]s.
	 */
	private fun buildGraph(): DirectedGraph<Storable> {
		val graph = DirectedGraph<Storable>()

		addDummyReferences()

		// Add nodes
		resolutionRequestMap.keys.forEach { graph.addNode(it) }

		// Add edges
		for (storable in resolutionRequestMap.keys) {
			for (reference in resolutionRequestMap[storable]!!) {
				reference.resolveAfter?.forEach {
					val predecessor: Storable? = getStorable(it)
					if (predecessor != null) {
						graph.addEdge(predecessor, storable)
					}
				}
			}
		}

		return graph
	}
}