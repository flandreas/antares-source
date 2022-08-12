package ch.scorpion.jabbah.graph.model.element

import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCreator

/**
 * A [ContainerLibraryElementCollector] recursively traverses a [Graph] and collects the [UUID]s of
 * all its [ContainerLibraryElement]s.
 */
class ContainerLibraryElementCollector(
	private val repository: MetaGraphRepository = LibraryModule.libraryHolder,
	private val storableCreator: StorableCreator = IOModule.storableCreator
) {

	/** Contains all collected [UUID]s.*/
	private val uuids = mutableSetOf<UUID>()

	/** Collects the [Set] of  [UUID] all recursively reachable [Graph]s of `graph`.*/
	fun collect(graph: Graph): Set<UUID> {
		uuids.clear()
		graph.bind(true, repository, storableCreator)
		graph.accept(GraphVisitor())
		return uuids
	}

	private inner class GraphVisitor : EmptyHierarchyVisitor() {
		override fun visitEnter(node: Any): Boolean {
			if (node is SubGraphVerticeRef) {
				uuids.add(node.graphUUID!!)
			}
			return true
		}
	}
}