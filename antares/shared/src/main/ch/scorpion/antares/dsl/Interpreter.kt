package ch.scorpion.antares.dsl

import ch.scorpion.antares.dsl.TokenType.*

/**
 * Interprets an AST according to the grammar parsed by [Parser].
 */
class Interpreter(private val node: Node) {

	constructor(parser: Parser): this(parser.parse())
	constructor(text: String): this(Parser(text))

	private val memory = Memory()

	fun interpret(): Long {
		return interpret(node)
	}

	private fun interpret(node: Node): Long {
		return when (node) {
			is Block -> block(node)
			is Compound -> compound(node)
			is NoOp -> 0
			is UnaryOperation -> unaryOperation(node)
			is BinaryOperation -> binaryOperation(node)
			is Number -> number(node)
			is Assignment -> assignment(node)
			is Variable -> variable(node)
			is Declaration -> declaration(node)
			is IfStatement -> ifStatement(node)
			else -> throw SyntaxError(node.location, "Unknown AST node '${node::class.simpleName}'")
		}
	}

	private fun compound(node: Compound): Long {
		var result = 0L
		node.children.forEach { result = interpret(it) }
		return result
	}

	private fun block(node: Block): Long {
		memory.enterScope("block")
		val result = compound(node)
		memory.exitScope()
		return result
	}

	private fun binaryOperation(node: BinaryOperation): Long {
		return when (node.op.type) {
			PLUS -> interpret(node.left) + interpret(node.right)
			MINUS -> interpret(node.left) - interpret(node.right)
			MULTIPLY -> interpret(node.left) * interpret(node.right)
			DIVIDE -> interpret(node.left) / interpret(node.right)
			EQUAL -> if (interpret(node.left) == interpret(node.right)) 1 else 0
			DIFF -> if (interpret(node.left) != interpret(node.right)) 1 else 0
			AND -> interpret(node.left).and(interpret(node.right))
			OR -> interpret(node.left).or(interpret(node.right))
			SMALLER -> if (interpret(node.left) < interpret(node.right)) 1 else 0
			GREATER -> if (interpret(node.left) > interpret(node.right)) 1 else 0
			SMALLER_EQUAL -> if (interpret(node.left) <= interpret(node.right)) 1 else 0
			GREATER_EQUAL -> if (interpret(node.left) >= interpret(node.right)) 1 else 0
			SHIFT_LEFT -> interpret(node.left).shl(interpret(node.right).toInt())
			SHIFT_RIGHT -> interpret(node.left).shr(interpret(node.right).toInt())
			MOD -> interpret(node.left).mod(interpret(node.right))
			else -> throw SyntaxError(node.location, "Unknown binary operation '${node.op.type.name}'")
		}
	}

	private fun number(node: Number): Long = node.token.value!!

	private fun unaryOperation(node: UnaryOperation): Long {
		return when (node.op.type) {
			PLUS -> +interpret(node.expr)
			MINUS -> -interpret(node.expr)
			NOT -> interpret(node.expr).inv()
			else -> throw SyntaxError(node.location, "Unknown unary operation '${node.op.type.name}'")
		}
	}

	/** Also supports implicit declaration. */
	private fun assignment(node: Assignment): Long {
		if (!memory.isDefined(node.left)) {
			memory.define(node.left)
		}
		val value = interpret(node.right)
		memory.setValue(node.left, value)
		return value
	}

	private fun variable(node: Variable): Long =
		memory.getValue(node) as Long

	private fun declaration(node: Declaration): Long {
		if (!memory.isLocallyDefined(node.left)) {
			memory.define(node.left)
		}
		val value = node.right?.let { interpret(it) }
		value?.let { memory.setValue(node.left, value) }
		return value ?: 0
	}

	private fun ifStatement(node: IfStatement): Long {
		if (interpret(node.condition) != 0L) {
			return interpret(node.thenStatement)
		}
		return node.elseStatement?.let { interpret(it) } ?: 0L
	}
}