package ch.scorpion.antares.dsl

import ch.scorpion.antares.dsl.TokenType.*

/**
 * Interprets an AST according to the grammar parsed by [Parser].
 */
class Interpreter(private val node: Node) {

	constructor(parser: Parser): this(parser.parse())
	constructor(text: String): this(Parser(text))

	private val globalScope = mutableMapOf<String, Int>()

	fun interpret(): Int {
		return interpret(node)
	}

	private fun interpret(node: Node): Int {
		return when (node) {
			is Compound -> compound(node)
			is NoOp -> 0
			is UnaryOperation -> unaryOperation(node)
			is BinaryOperation -> binaryOperation(node)
			is Number -> number(node)
			is Assignment -> assignment(node)
			is Variable -> variable(node)
			is Declaration -> declaration(node)
			else -> throw SyntaxError(node.location, "Unknown AST node '${node::class.simpleName}'")
		}
	}

	private fun compound(node: Compound): Int {
		var result = 0
		node.children.forEach { result = interpret(it) }
		return result
	}

	private fun binaryOperation(node: BinaryOperation): Int {
		return when (node.op.type) {
			PLUS -> interpret(node.left) + interpret(node.right)
			MINUS -> interpret(node.left) - interpret(node.right)
			MULTIPLY -> interpret(node.left) * interpret(node.right)
			DIVIDE -> interpret(node.left) / interpret(node.right)
			else -> throw SyntaxError(node.location, "Unknown binary operation '${node.op.type.name}'")
		}
	}

	private fun number(node: Number): Int = node.token.value!!

	private fun unaryOperation(node: UnaryOperation): Int {
		return when (node.op.type) {
			PLUS -> +interpret(node.expr)
			MINUS -> -interpret(node.expr)
			else -> throw SyntaxError(node.location, "Unknown unary operation '${node.op.type.name}'")
		}
	}

	private fun assignment(node: Assignment): Int {
		val name = node.left.token.value!!
		val value = interpret(node.right)
		globalScope[name] = value
		return value
	}

	private fun variable(node: Variable): Int {
		val name = node.token.value!!
		return globalScope[name] ?: throw RuntimeError(node.location, "Variable '$name' not found")
	}

	private fun declaration(node: Declaration): Int {
		val name = node.left.token.value!!
		val value = node.right?.let { interpret(it) } ?: 0
		globalScope[name] = value
		return value
	}
}