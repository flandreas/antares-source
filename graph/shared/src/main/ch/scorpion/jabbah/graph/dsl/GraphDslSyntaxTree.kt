package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.dsl.*
import ch.scorpion.jabbah.base.parser.TextLocation

class InitStatement(location: TextLocation, val block: Block) : AbstractNode(location) {
	override fun toString(): String = "init"

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			block.accept(visitor)
		}
		return visitor.visitLeave(this)
	}
}

class PropertyPortName(location: TextLocation, val elemId: Literal, val portName: Variable) : AbstractNode(location) {
	override fun toString(): String = DslTokenType.HASH.id

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			elemId.accept(visitor)
			portName.accept(visitor)
		}
		return visitor.visitLeave(this)
	}
}

class PropertyPortId(location: TextLocation, val elemId: Literal, val portId: Literal) : AbstractNode(location) {
	override fun toString(): String = DslTokenType.HASH.id

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			elemId.accept(visitor)
			portId.accept(visitor)
		}
		return visitor.visitLeave(this)
	}
}