package ch.scorpion.antares.dsl

import ch.scorpion.jabbah.base.HierarchyVisitor

interface Node {
	fun accept(visitor: HierarchyVisitor): Boolean
}

abstract class AbstractNode : Node {
	override fun accept(visitor: HierarchyVisitor): Boolean {
		return visitor.visit(this)
	}
}

data class UnaryOperation(
	val op: Token<Any>,
	val expr: Node
) : AbstractNode() {

	override fun toString(): String {
		return when (op.type) {
			TokenType.PLUS -> "Unary +"
			TokenType.MINUS -> "Unary -"
			else -> throw IllegalStateException("unsupported unary op ${op.type}")
		}
	}
}

data class BinaryOperation(
	val left: Node,
	val op: Token<Any>,
	val right: Node
) : AbstractNode() {

	override fun toString(): String {
		return when (op.type) {
			TokenType.PLUS -> "+"
			TokenType.MINUS -> "-"
			TokenType.MULTIPLY -> "*"
			TokenType.DIVIDE -> "/"
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

data class Number(val token: Token<Int>) : AbstractNode() {
	override fun toString(): String = token.value!!.toString()
}

class NoOp : AbstractNode() {
	override fun toString(): String = "NoOp"
}

class Compound(val children: List<Node>): AbstractNode() {

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

data class Variable(val token: Token<String>) : AbstractNode() {
	override fun toString(): String = token.value!!
}

data class Assignment(val left: Variable, val op: Token<Assignment>, val right: Node) : AbstractNode() {

	override fun toString(): String = "="

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			left.accept(visitor)
			right.accept(visitor)
		}
		return visitor.visitLeave(this)
	}
}