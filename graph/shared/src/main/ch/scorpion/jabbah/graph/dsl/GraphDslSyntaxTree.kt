package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.dsl.*

class InitStatement(location: CodeLocation, val block: Block) : AbstractNode(location) {
	override fun toString(): String = "init"

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			block.accept(visitor)
		}
		return visitor.visitLeave(this)
	}
}

class Property(location: CodeLocation, val id: Literal, val name: Variable) : AbstractNode(location) {
	override fun toString(): String = TokenType.HASH.id

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			id.accept(visitor)
			name.accept(visitor)
		}
		return visitor.visitLeave(this)
	}
}