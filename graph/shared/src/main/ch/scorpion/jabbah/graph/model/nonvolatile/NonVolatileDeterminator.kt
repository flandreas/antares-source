package ch.scorpion.jabbah.graph.model.nonvolatile

import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import ch.scorpion.jabbah.graph.model.Graph

class NonVolatileDeterminator : EmptyHierarchyVisitor() {

    private var result: Boolean = false

    fun hasNonVolatileData(graph: Graph): Boolean {
        graph.accept(this)
        return result
    }

    override fun visit(node: Any): Boolean {
        if (node is NonVolatile && node.nonVolatile) {
            result = true
            return false
        }
        return true
    }
}