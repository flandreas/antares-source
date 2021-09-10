package ch.scorpion.antares.dsl

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.antares.dsl.TokenType.*

class InterpreterError(msg: String) : Throwable(msg)

/**
 * Interprets an AST according to the grammar parsed by [Parser].
 * TODO: Implement using [HierarchyVisitor]
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
			is Compound -> interpret(node)
			is NoOp -> 0
			is UnaryOperation -> interpret(node)
			is BinaryOperation -> interpret(node)
			is Number -> interpret(node)
			is Assignment -> interpret(node)
			is Variable -> interpret(node)
			is Declaration -> interpret(node)
			else -> throw SyntaxError("Unknown AST node '${node::class.simpleName}'")
		}
	}

	private fun interpret(node: Compound): Int {
		var result = 0
		node.children.forEach { result = interpret(it) }
		return result
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

	private fun interpret(node: Assignment): Int {
		val name = node.left.token.value!!
		val value = interpret(node.right)
		globalScope[name] = value
		return value
	}

	private fun interpret(node: Variable): Int {
		val name = node.token.value!!
		return globalScope[name] ?: throw InterpreterError("Variable '$name' not found")
	}

	private fun interpret(node: Declaration): Int {
		val name = node.left.token.value!!
		val value = node.right?.let { interpret(it) } ?: 0

		if (globalScope[name] != null) {
			throw InterpreterError("Variable '$name' already defined")
		}

		globalScope[name] = value
		return value
	}
}