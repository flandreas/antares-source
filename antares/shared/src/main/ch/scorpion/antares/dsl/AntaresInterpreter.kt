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

	override fun interpret(node: Node): Any =
		when (node) {
			is RaisedInput -> raisedInput(node)
			else -> super.interpret(node)
		}

	override fun evaluateTrueCondition(value: Any): Boolean =
		when (value) {
			is DigitalSignal -> value.toLong() != null && value.toLong() != 0UL
			else -> super.evaluateTrueCondition(value)
		}

	override fun evaluateEqualCondition(node: Node, left: Any, right: Any): Long {
		return binaryOpInterpreted(
			node,
			EQUAL,
			left,
			right,
			{ l, r -> if (l == r) 1L else 0L},
			{ l, r -> if (l == r) 1L else 0L },
			{ l, r -> if (l.toLong() == r.toULong()) 1L else 0L })
		as Long
	}

	override fun typedBinaryOp(node: BinaryOperation): Any {
		return when (node.op.type) {
			AND -> binaryOp(node, { l, r -> l.and(r) }, { l, r -> l.and(r)}, { l, r -> l.and(r.toULong()) })
			OR -> binaryOp(node, { l, r -> l.or(r) }, { l, r -> l.or(r) }, { l, r -> l.or(r.toULong()) })
			PLUS -> binaryOp(node, { l, r -> l + r}, { l, r -> l.add(r) }, { l, r -> l.add(r.toUInt()) })
			EQUAL -> evaluateEqualCondition(node, interpret(node.left), interpret(node.right))
			DIFF -> binaryOp(node,
				{ l, r -> if (l != r) 1L else 0L},
				{ l, r -> if (l != r) 1L else 0L },
				{ l, r -> if (l.toLong() != r.toULong()) 1L else 0L })
			MOD -> binaryOp(node, { l, r -> l.mod(r) }, { l, r -> l.mod(r) }, { l, r -> l.mod(r.toULong()) })
			GREATER -> binaryOp(node,
				{ l, r -> if (l > r) 1L else 0L },
				{ l, r -> if (l.isGreaterThan(r)) 1L else 0L },
				{ l, r -> if (l.isGreaterThan(r.toULong())) 1L else 0L })
			GREATER_EQUAL -> binaryOp(node,
				{ l, r -> if (l >= r) 1L else 0L },
				{ l, r -> if (l.isGreaterEqualThan(r)) 1L else 0L },
				{ l, r -> if (l.isGreaterEqualThan(r.toULong())) 1L else 0L })
			SMALLER -> binaryOp(node,
				{ l, r -> if (l < r) 1L else 0L },
				{ l, r -> if (l.isSmallerThan(r)) 1L else 0L },
				{ l, r -> if (l.isSmallerThan(r.toULong())) 1L else 0L })
			SMALLER_EQUAL -> binaryOp(node,
				{ l, r -> if (l <= r) 1L else 0L },
				{ l, r -> if (l.isSmallerEqualThan(r)) 1L else 0L },
				{ l, r -> if (l.isSmallerEqualThan(r.toULong())) 1L else 0L })
			else -> super.typedBinaryOp(node)
		}
	}

	override fun typedBinaryOpWithRightInt(node: BinaryOperation): Any =
		when (node.op.type) {
			SHIFT_LEFT -> binaryOpWithRightInt(node, { l, r -> l.shl(r) }, { l, r -> l.shiftLeft(r) })
			SHIFT_RIGHT -> binaryOpWithRightInt(node, { l, r -> l.shr(r) }, { l, r -> l.shiftRight(r) })
			else -> super.typedBinaryOpWithRightInt(node)
		}

	private fun binaryOp(
		node: BinaryOperation,
		longOp: (Long, Long) -> Any,
		signalOp: (DigitalSignal, DigitalSignal) -> Any,
		mixedOp: (DigitalSignal, Long) -> Any
	): Any {
		val left = interpret(node.left)
		val right = interpret(node.right)
		return binaryOpInterpreted(node, node.op.type, left, right, longOp, signalOp, mixedOp)
	}

	private fun binaryOpInterpreted(
		node: Node,
		op: TokenType,
		left: Any,
		right: Any,
		longOp: (Long, Long) -> Any,
		signalOp: (DigitalSignal, DigitalSignal) -> Any,
		mixedOp: (DigitalSignal, Long) -> Any
	): Any {
		return if (left is Long && right is Long) {
			longOp(left, right)
		} else if (left is DigitalSignal && right is DigitalSignal) {
			signalOp(left, right)
		} else if (left is DigitalSignal && right is Long) {
			mixedOp(left, right)
		} else {
			throw RuntimeError(node.location, "Incompatible types for '${op}'")
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

	override fun typedUnaryOp(node: UnaryOperation): Any =
		when (node.op.type) {
			NOT -> unaryOp(node, { it.inv() }, { it.not() })
			else -> super.typedUnaryOp(node)
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