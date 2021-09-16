package ch.scorpion.antares.dsl

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.dsl.*

class RaisedInput(location: CodeLocation, var variable: Variable) : AbstractNode(location) {
	override fun toString(): String = "^"

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			variable.accept(visitor)
		}
		return visitor.visitLeave(this)
	}
}

class BitAccess(location: CodeLocation, token: Token<String>, val index: Node): Variable(location, token) {
	override fun toString(): String = "${super.toString()}@"

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			index.accept(visitor)
		}
		return visitor.visitLeave(this)
	}
}