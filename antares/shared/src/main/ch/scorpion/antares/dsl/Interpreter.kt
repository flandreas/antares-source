package ch.scorpion.antares.dsl

import ch.scorpion.antares.dsl.TokenType.*
import ch.scorpion.antares.model.signal.DigitalSignal

/**
 * Interprets an AST according to the grammar parsed by [Parser].
 */
class Interpreter(private val node: Node) {

	constructor(parser: Parser): this(parser.parse())
	constructor(text: String): this(Parser(text))

	val memory = Memory()

	fun interpret(): Any = interpret(node)

	private fun interpret(node: Node): Any {
		return when (node) {
			is Block -> block(node)
			is Compound -> compound(node)
			is NoOp -> 0L
			is UnaryOperation -> unaryOperation(node)
			is BinaryOperation -> binaryOperation(node)
			is Number -> number(node)
			is Assignment -> assignment(node)
			is Variable -> variable(node)
			is Declaration -> declaration(node)
			is IfStatement -> ifStatement(node)
			is WhenStatement -> whenStatement(node)
			is ForStatement -> forStatement(node)
			else -> throw SyntaxError(node.location, "Unknown AST node '${node::class.simpleName}'")
		}
	}

	private fun interpretAsLong(node: Node): Long {
		val result = interpret(node)
		if (result !is Long) {
			throw RuntimeError(node.location, "Not a number")
		}
		return result
	}

	private fun compound(node: Compound): Any {
		var result: Any = 0L
		node.children.forEach { result = interpret(it) }
		return result
	}

	private fun block(node: Block): Any {
		memory.enterScope("block")
		val result = compound(node)
		memory.exitScope()
		return result
	}

	private fun binaryOperation(node: BinaryOperation): Any {
		return when (node.op.type) {
			PLUS -> interpretAsLong(node.left) + interpretAsLong(node.right)
			MINUS -> interpretAsLong(node.left) - interpretAsLong(node.right)
			MULTIPLY -> interpretAsLong(node.left) * interpretAsLong(node.right)
			DIVIDE -> interpretAsLong(node.left) / interpretAsLong(node.right)
			EQUAL -> if (interpret(node.left) == interpret(node.right)) 1L else 0L
			DIFF -> if (interpret(node.left) != interpret(node.right)) 1L else 0L
			AND -> binaryOp(node, { l, r -> l.and(r) }, { l, r -> l.and(r) })
			OR -> binaryOp(node, { l, r -> l.or(r) }, { l, r -> l.or(r) })
			SMALLER -> if (interpretAsLong(node.left) < interpretAsLong(node.right)) 1L else 0L
			GREATER -> if (interpretAsLong(node.left) > interpretAsLong(node.right)) 1L else 0L
			SMALLER_EQUAL -> if (interpretAsLong(node.left) <= interpretAsLong(node.right)) 1L else 0L
			GREATER_EQUAL -> if (interpretAsLong(node.left) >= interpretAsLong(node.right)) 1L else 0L
			SHIFT_LEFT -> binaryOpWithRightInt(node, { l, r -> l.shl(r) }, { l, r -> l.shiftLeft(r) })
			SHIFT_RIGHT -> binaryOpWithRightInt(node, { l, r -> l.shr(r) }, { l, r -> l.shiftRight(r) })
			MOD -> interpretAsLong(node.left).mod(interpretAsLong(node.right))
			else -> throw SyntaxError(node.location, "Unknown binary operation '${node.op.type.name}'")
		}
	}

	private fun binaryOp(
		node: BinaryOperation,
		longOp: (Long, Long) -> Long,
		signalOp: (DigitalSignal,DigitalSignal) -> DigitalSignal
	): Any {
		val left = interpret(node.left)
		val right = interpret(node.right)
		return if (left is Long && right is Long) {
			longOp(left, right)
		} else if (left is DigitalSignal && right is DigitalSignal) {
			signalOp(left, right)
		} else {
			throw RuntimeError(node.location, "Incompatible types for '${node.op.type}'")
		}
	}

	private fun binaryOpWithRightInt(
		node: BinaryOperation,
		longOp: (Long, Int) -> Long,
		signalOp: (DigitalSignal, Int) -> DigitalSignal
	): Any {
		val left = interpret(node.left)
		val right = interpretAsLong(node.right)
		return when (left) {
			is Long -> longOp(left, right.toInt())
			is DigitalSignal -> signalOp(left, right.toInt())
			else -> throw RuntimeError(node.location, "Incompatible type for '${node.op.type}'")
		}
	}

	private fun number(node: Number): Long = node.token.value!!

	private fun unaryOperation(node: UnaryOperation): Any {
		return when (node.op.type) {
			PLUS -> +interpretAsLong(node.expr)
			MINUS -> -interpretAsLong(node.expr)
			NOT -> unaryOp(node, { it.inv() }, { it.not() })
			else -> throw SyntaxError(node.location, "Unknown unary operation '${node.op.type.name}'")
		}
	}

	private fun unaryOp(
		node: UnaryOperation,
		longOp: (Long) -> Long,
		signalOp: (DigitalSignal) -> DigitalSignal
	): Any {
		val value = interpret(node.expr)
		return when (value) {
			is Long -> longOp(value)
			is DigitalSignal -> signalOp(value)
			else -> throw RuntimeError(node.location, "Incompatible type for '${node.op.type}'")
		}
	}

	/** Also supports implicit declaration. */
	private fun assignment(node: Assignment): Any {
		if (!memory.isDefined(node.left)) {
			memory.define(node.left)
		}
		val value = interpret(node.right)
		memory.setValue(node.left, value)
		return value
	}

	private fun variable(node: Variable): Any =
		memory.getValue(node)

	private fun declaration(node: Declaration): Any {
		if (!memory.isLocallyDefined(node.left)) {
			memory.define(node.left)
		}
		val value = node.right?.let { interpret(it) }
		value?.let { memory.setValue(node.left, value) }
		return value ?: 0L
	}

	private fun ifStatement(node: IfStatement): Any {
		if (interpret(node.condition) != 0L) {
			return interpret(node.thenStatement)
		}
		return node.elseStatement?.let { interpret(it) } ?: 0L
	}

	private fun whenStatement(node: WhenStatement): Any {
		val expr = interpret(node.expression)
		for (clause in node.clauses) {
			if (clause.condition == null || expr == interpret(clause.condition)) {
				return interpret(clause.then)
			}
		}
		return 0L
	}

	private fun forStatement(node: ForStatement): Any {
		var startValue = interpretAsLong(node.inExpr)
		val endValue = interpretAsLong(node.toExpr)

		memory.enterScope("for")
		memory.define(node.variable)

		if (startValue <= endValue) {
			for (value in startValue..endValue) {
				memory.setValue(node.variable, value)
				interpret(node.statement)
			}
		} else {
			for (value in startValue downTo endValue) {
				memory.setValue(node.variable, value)
				interpret(node.statement)
			}
		}

		memory.exitScope()
		return 0L
	}
}