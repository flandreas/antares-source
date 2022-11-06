package ch.scorpion.jabbah.graph.repository

import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice

/**
 * Checks whether a [Graph] structure recursively contains a [SubGraphVertice] with a particular [UUID].
 * Checking involves following all [SubGraphVertice] references to other [Graph]s.
 */
class SubGraphVerticeLocator(
	private val graph: Graph,
	private val repository: MetaGraphRepository = LibraryModule.libraryHolder
) {
	private var contains: Boolean = false

	/**
	 * Checks whether [graph] contains a [SubGraphVertice] with [UUID] [uuid].
	 */
	fun contains(uuid: UUID): Boolean {
		graph.bind(true, repository)
		graph.accept(object : EmptyHierarchyVisitor() {
			override fun visitEnter(node: Any): Boolean {
				if (node is SubGraphVertice) {
					if (node.graphUUID == uuid) {
						contains = true
						return false
					}
				}
				return true
			}
		})
		return contains
	}
}