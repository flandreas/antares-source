package ch.scorpion.antares.dsl

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.dsl.*
import ch.scorpion.jabbah.base.dsl.TokenType.*
import ch.scorpion.jabbah.graph.model.GraphActorData

class AntaresInterpreter(
	node: Node,
	memory: Memory = Memory()
) : Interpreter(node, memory) {

	constructor(parser: AntaresParser): this(parser.parse())
	constructor(program: String): this(AntaresParser(program))

	private val data: GraphActorData? get() = if (params is GraphActorData) params as GraphActorData else null

	override fun interpret(node: Node): Any {
		return when (node) {
			is RaisedInput -> raisedInput(node)
			else -> super.interpret(node)
		}
	}

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

	private fun raisedInput(node: RaisedInput): Any {
		val portName = node.variable.token.value as String
		return data?.let {
			if (it.changedPort?.name == portName
				&& it.getSignal<DigitalSignal>(it.changedPort!!.portId)?.bitAt(0)?.isSet == true
			) 1L else 0L
		} ?: 0L
	}
}