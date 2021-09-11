package ch.scorpion.antares.dsl

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
			else -> throw IllegalStateException("unsupported unary op ${op.type}")
		}
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

class Number(location: CodeLocation, val token: Token<Int>) : AbstractNode(location) {
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

class Variable(location: CodeLocation, val token: Token<String>) : AbstractNode(location) {
	override fun toString(): String = token.value!!

	override fun accept(visitor: HierarchyVisitor): Boolean = visitor.visit(this)
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

class Declaration(location: CodeLocation, val left: Variable, val right: Node?) : AbstractNode(location) {
	override fun toString(): String = "Var"

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			left.accept(visitor)
			right?.accept(visitor)
		}
		return visitor.visitLeave(this)
	}
}

class IfStatement(location: CodeLocation, val condition: Node, val thenStatement: Node, val elseStatement: Node?) : AbstractNode(location) {
	override fun toString(): String = "If"

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			condition.accept(visitor)
			thenStatement.accept(visitor)
		}
		return visitor.visitLeave(this)
	}
}