package ch.scorpion.jabbah.graph.model.element

import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.collection.DirectedGraph
import ch.scorpion.jabbah.base.collection.Stack
import ch.scorpion.jabbah.base.collection.TopologicalSort
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef

/**
 * A [ContainerLibraryElementCollector] recursively traverses a [Graph] and collects the [UUID]s of
 * all its [ContainerLibraryElement]s.
 */
class ContainerLibraryElementCollector(
	private val repository: MetaGraphRepository = LibraryModule.libraryHolder
) {

	/** Contains all collected [UUID]s.*/
	private val uuids = mutableSetOf<UUID>()

	private val dependencies = DirectedGraph<UUID>()

	/** Collects the [Set] of  [UUID] all recursively reachable [Graph]s of `graph`.*/
	fun collect(graph: Graph): ContainerLibraryElementCollector {
		uuids.clear()
		graph.bind(true, repository)
		graph.accept(GraphVisitor())
		return this
	}

	fun asUuids(): Set<UUID> = uuids

	fun asSortedDependencies(): List<UUID> =
		TopologicalSort
			.sort(dependencies)
			.toList()

	private inner class GraphVisitor() : EmptyHierarchyVisitor() {

		private val stack = Stack<Graph>()

		override fun visitEnter(node: Any): Boolean {
			if (node is Graph) {
				stack.push(node)
				dependencies.addNode(node.uuid)
			}
			if (node is SubGraphVerticeRef) {
				dependencies.addNode(node.graphUUID!!)
				dependencies.addEdge(stack.peek().uuid, node.graphUUID!!)
				uuids.add(node.graphUUID!!)
			}
			return true
		}

		override fun visitLeave(node: Any): Boolean {
			if (node is Graph) {
				stack.pop()
			}
			return true
		}
	}
}