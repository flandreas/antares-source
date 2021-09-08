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