package ch.scorpion.jabbah.base.dsl

import ch.scorpion.jabbah.base.HierarchyVisitor

interface Node {
	val location: CodeLocation
	fun accept(visitor: HierarchyVisitor): Boolean
}

abstract class AbstractNode(override val location: CodeLocation) : Node {
	override fun accept(visitor: HierarchyVisitor): Boolean {
		return visitor.visit(this)
	}
}

class UnaryOperation(
	location: CodeLocation,
	val op: Token<Any>,
	val expr: Node
) : AbstractNode(location) {

	override fun toString(): String {
		return when (op.type) {
			TokenType.PLUS -> "Unary +"
			TokenType.MINUS -> "Unary -"
			TokenType.NOT -> "not"
			else -> throw IllegalStateException("unsupported unary op ${op.type}")
		}
	}

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			expr.accept(visitor)
		}
		return visitor.visitLeave(this)
	}
}

class BinaryOperation(
	location: CodeLocation,
	val left: Node,
	val op: Token<Any>,
	val right: Node
) : AbstractNode(location) {

	override fun toString(): String {
		return when (op.type) {
			TokenType.PLUS -> "+"
			TokenType.MINUS -> "-"
			TokenType.MULTIPLY -> "*"
			TokenType.DIVIDE -> "/"
			TokenType.EQUAL -> "=="
			TokenType.DIFF -> "!="
			TokenType.AND -> "and"
			TokenType.OR -> "or"
			else -> throw IllegalStateException("unsupported binary op ${op.type}")
		}
	}

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			left.accept(visitor)
			right.accept(visitor)
		}
		return visitor.visitLeave(this)
	}
}

class Number(location: CodeLocation, val token: Token<Long>) : AbstractNode(location) {
	override fun toString(): String = token.value!!.toString()
}

class NoOp(location: CodeLocation) : AbstractNode(location) {
	override fun toString(): String = "NoOp"
}

open class Compound(location: CodeLocation, val children: List<Node>) : AbstractNode(location) {

	override fun toString(): String = "Compound"

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			for (child in children) {
				if (!child.accept(visitor)) {
					break
				}
			}
		}
		return visitor.visitLeave(this)
	}
}

class Block(location: CodeLocation, children: List<Node>) : Compound(location, children) {
	override fun toString(): String = "Block"
}

open class Variable(location: CodeLocation, val token: Token<String>) : AbstractNode(location) {
	override fun toString(): String = token.value!!

	override fun accept(visitor: HierarchyVisitor): Boolean = visitor.visit(this)
}

class AssocArray(location: CodeLocation, token: Token<String>, val key: Node): Variable(location, token) {
	override fun toString(): String = "${super.toString()}[]"

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			key.accept(visitor)
		}
		return visitor.visitLeave(this)
	}
}

class Assignment(location: CodeLocation, val left: Variable, val op: Token<Assignment>, val right: Node) : AbstractNode(location) {

	override fun toString(): String = "="

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			left.accept(visitor)
			right.accept(visitor)
		}
		return visitor.visitLeave(this)
	}
}

class Declaration(location: CodeLocation, val left: Variable, val right: Node?, val store: Boolean) : AbstractNode(location) {
	override fun toString(): String = if (store) "store" else "var"

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			left.accept(visitor)
			right?.accept(visitor)
		}
		return visitor.visitLeave(this)
	}
}

class IfStatement(location: CodeLocation, val condition: Node, val thenStatement: Node, val elseStatement: Node?) : AbstractNode(location) {
	override fun toString(): String = "if"

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			condition.accept(visitor)
			thenStatement.accept(visitor)
		}
		return visitor.visitLeave(this)
	}
}

/**
 * @property condition `null` only for 'else' case
 */
class WhenClause(location: CodeLocation, val condition: Node?, val then: Node) : AbstractNode(location) {
	override fun toString(): String = condition?.let { ":" } ?: "else"

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			condition?.accept(visitor)
			then.accept(visitor)
		}
		return visitor.visitLeave(this)
	}
}

class WhenStatement(location: CodeLocation, val expression: Node, val clauses: List<WhenClause>): AbstractNode(location) {
	override fun toString(): String = "when"

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			expression.accept(visitor)
			for (clause in clauses) {
				if (!clause.accept(visitor)) {
					break
				}
			}
		}
		return visitor.visitLeave(this)
	}
}

class ForStatement(location: CodeLocation, val variable: Variable, val inExpr: Node, val toExpr: Node, val statement: Node): AbstractNode(location) {
	override fun toString(): String = "for"

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			variable.accept(visitor)
			inExpr.accept(visitor)
			toExpr.accept(visitor)
			statement.accept(visitor)
		}
		return visitor.visitLeave(this)
	}
}