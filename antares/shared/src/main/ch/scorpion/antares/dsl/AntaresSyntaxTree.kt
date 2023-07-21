package ch.scorpion.antares.dsl

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.dsl.*
import ch.scorpion.jabbah.base.parser.TextLocation
import ch.scorpion.jabbah.base.parser.Token

class RaisedInput(location: TextLocation, var variable: Variable) : AbstractNode(location) {
	override fun toString(): String = "^"

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			variable.accept(visitor)
		}
		return visitor.visitLeave(this)
	}
}

class BitAccess(location: TextLocation, token: Token<String>, val index: Node): Variable(location, token) {
	override fun toString(): String = "${super.toString()}@"

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			index.accept(visitor)
		}
		return visitor.visitLeave(this)
	}
}