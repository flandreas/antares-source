package ch.scorpion.antares.dsl

import ch.scorpion.antares.dsl.TokenType.*

/**
 * Interprets an AST according to the grammar parsed by [Parser].
 */
class Interpreter(private val node: Node) {

	constructor(parser: Parser): this(parser.parse())
	constructor(text: String): this(Parser(text))

	fun interpret(): Int {
		return interpret(node)
	}

	private fun interpret(node: Node): Int {
		return when (node) {
			is UnaryOperation -> interpret(node)
			is BinaryOperation -> interpret(node)
			is Number -> interpret(node)
			else -> throw SyntaxError("Unknown node '${node::class.simpleName}'")
		}
	}

	private fun interpret(node: BinaryOperation): Int {
		return when (node.op.type) {
			PLUS -> interpret(node.left) + interpret(node.right)
			MINUS -> interpret(node.left) - interpret(node.right)
			MULTIPLY -> interpret(node.left) * interpret(node.right)
			DIVIDE -> interpret(node.left) / interpret(node.right)
			else -> throw SyntaxError("Unknown binary operation '${node.op.type.name}'")
		}
	}

	private fun interpret(node: Number): Int = node.token.value!!

	private fun interpret(node: UnaryOperation): Int {
		return when (node.op.type) {
			PLUS -> +interpret(node.expr)
			MINUS -> -interpret(node.expr)
			else -> throw SyntaxError("Unknown unary operation '${node.op.type.name}'")
		}
	}
}