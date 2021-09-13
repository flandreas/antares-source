package ch.scorpion.antares.dsl

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.dsl.AbstractNode
import ch.scorpion.jabbah.base.dsl.CodeLocation
import ch.scorpion.jabbah.base.dsl.Variable

class RaisedInput(location: CodeLocation, var variable: Variable) : AbstractNode(location) {
	override fun toString(): String = "^"

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			variable.accept(visitor)
		}
		return visitor.visitLeave(this)
	}
}