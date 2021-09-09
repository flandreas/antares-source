package ch.scorpion.antares.dsl

interface Node

data class UnaryOperation(
	val op: Token<Any>,
	val expr: Node
) : Node

data class BinaryOperation(
	val left: Node,
	val op: Token<Any>,
	val right: Node
) : Node

data class Number(val token: Token<Int>) : Node

class NoOp : Node

class Compound(val children: List<Node>): Node

data class Variable(val token: Token<String>) : Node

data class Assignment(val left: Variable, val op: Token<Assignment>, val right: Node) : Node