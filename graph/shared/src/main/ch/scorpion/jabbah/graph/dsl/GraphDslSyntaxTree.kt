package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.dsl.AbstractNode
import ch.scorpion.jabbah.base.dsl.Block
import ch.scorpion.jabbah.base.dsl.CodeLocation

class InitStatement(location: CodeLocation, val block: Block) : AbstractNode(location) {
	override fun toString(): String = "init"

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			block.accept(visitor)
		}
		return visitor.visitLeave(this)
	}
}