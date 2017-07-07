package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCreator
import ch.scorpion.jabbah.base.UUID

/**
 * Checks whether a [Graph] structure recursively contains a [SubGraphVertice] with a particular {@link UUID}.
 * Checking involves following all [SzbGraphVertice] references to other [Graphs].
 */
class SubGraphVerticeLocator(
    private val graph: Graph,
    private val library: Library,
    private val storableCreator: StorableCreator
) {
    constructor(graph: Graph): this(graph, LibraryModule.libraryHolder.library, IOModule.storableCreator)

    private var contains: Boolean = false

    /**
     * Checks whether [graph] contains a [SubGraphVerticeView] with [UUID] [uuid].
     */
    fun contains(uuid: UUID): Boolean {
        graph.bind(library, storableCreator)
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