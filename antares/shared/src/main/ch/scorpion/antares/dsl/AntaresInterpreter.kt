package ch.scorpion.antares.dsl

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.dsl.*
import ch.scorpion.jabbah.base.dsl.TokenType.*

class AntaresInterpreter(node: Node) : Interpreter(node) {

	constructor(parser: AntaresParser): this(parser.parse())
	constructor(program: String): this(AntaresParser(program))

	override fun typedBinaryOp(node: BinaryOperation): Any {
		return when (node.op.type) {
			AND -> binaryOp(node, { l, r -> l.and(r) }, { l, r -> l.and(r) })
			OR -> binaryOp(node, { l, r -> l.or(r) }, { l, r -> l.or(r) })
			else -> super.typedBinaryOp(node)
		}
	}

	override fun typedBinaryOpWithRightInt(node: BinaryOperation): Any {
		return when (node.op.type) {
			SHIFT_LEFT -> binaryOpWithRightInt(node, { l, r -> l.shl(r) }, { l, r -> l.shiftLeft(r) })
			SHIFT_RIGHT -> binaryOpWithRightInt(node, { l, r -> l.shr(r) }, { l, r -> l.shiftRight(r) })
			else -> super.typedBinaryOpWithRightInt(node)
		}
	}

	private fun binaryOp(
		node: BinaryOperation,
		longOp: (Long, Long) -> Long,
		signalOp: (DigitalSignal, DigitalSignal) -> DigitalSignal
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

	override fun typedUnaryOp(node: UnaryOperation): Any {
		return when (node.op.type) {
			NOT -> unaryOp(node, { it.inv() }, { it.not() })
			else -> super.typedUnaryOp(node)
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
}