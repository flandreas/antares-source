package ch.scorpion.antares.dsl

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.*
import ch.scorpion.jabbah.base.dsl.TokenType.*
import ch.scorpion.jabbah.graph.dsl.GraphDslInterpreter
import ch.scorpion.jabbah.graph.model.GraphActorData

class AntaresInterpreter(
	node: Node,
	memory: Memory = Memory()
) : GraphDslInterpreter(node, memory) {

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
			{ l, r -> if (l.toLong() == r.toLong()) 1L else 0L },
			{ l, r -> if (l.toLong() == r.toULong()) 1L else 0L },
			{ l, r -> if (l == r.getValue().toLong()) 1L else 0L })
		as Long
	}

	override fun storeValue(variable: Variable, value: Any): Any {
		return when (variable) {
			is BitAccess -> setBit(variable, value)
			else -> super.storeValue(variable, value)
		}
	}

	override fun loadValue(variable: Variable): Any =
		when (variable) {
			is BitAccess -> getBit(variable)
			else -> super.loadValue(variable)
		}

	override fun interpretAssocArrayKey(variable: AssocArray): Long {
		val key = interpret(variable.key)
		return when (key) {
			is DigitalSignal -> key.toLong()?.toLong() ?: throw RuntimeError(variable.location, Translations.getString("antares.dsl.arrayIndexNotFullyDefined.msg"))
			else -> super.interpretAssocArrayKey(variable)
		}
	}

	private fun getBit(bitAccess: BitAccess): Any {
		val value = memory.getValue(bitAccess)
		val index = getBitAccessIndex(bitAccess)
		return when (value) {
			is DigitalSignal -> {
				if (index >= value.bitWidth.width) {
					DigitalSignalFactory.of(Bit.False)
				} else {
					DigitalSignalFactory.of(value.bitAt(index))
				}
			}
			is Long -> value.shr(index).mod(2).toLong()
			else -> throw RuntimeError(bitAccess.location, Translations.getString("antares.dsl.bitAccessNotSupportedByType.msg"))
		}
	}

	private fun setBit(bitAccess: BitAccess, value: Any): Any {
		val index = getBitAccessIndex(bitAccess)
		val bitToSet = getBitAccessSetValue(bitAccess, value)
		val oldValue = super.loadValue(bitAccess)
		val newValue = when (oldValue) {
			is DigitalSignal -> {
				if (index >= oldValue.bitWidth.width) {
					throw RuntimeError(bitAccess.location, Translations.getString("antares.dsl.indexOutOfRange.msg"))
				}
				oldValue.withBit(index, Bit.of(bitToSet))
			}
			is Long -> {
				BitOperation.setBitAt(oldValue.toULong(), bitToSet, index).toLong()
			}
			else -> throw RuntimeError(bitAccess.location, Translations.getString("antares.dsl.bitAccessNotSupportedByType.msg"))
		}
		memory.setValue(bitAccess, newValue)
		return newValue
	}

	private fun getBitAccessIndex(bitAccess: BitAccess): Int {
		val index = interpret(bitAccess.index)
		return when (index) {
			is Long -> index.toInt()
			is DigitalSignal -> {
				index.toLong()?.toInt() ?: throw RuntimeError(bitAccess.location, Translations.getString("antares.dsl.bitAccessNotSupportedByType.msg"))
			}
			else -> throw RuntimeError(bitAccess.location, Translations.getString("antares.dsl.bitAccessNotSupportedByType.msg"))
		}
	}

	private fun getBitAccessSetValue(bitAccess: BitAccess, value: Any): Int {
		return when (value) {
			is DigitalSignal -> if (value.bitAt(0).isSet) 1 else 0
			is Long -> value.mod(2)
			else -> throw RuntimeError(bitAccess.location, Translations.getString("antares.dsl.bitAccessNotSupportedByType.msg"))
		}
	}

	private fun signalToLong(signal: DigitalSignal): Long =
		signal.toLong()?.toLong() ?: throw RuntimeError(rootNode.location, Translations.getString("antares.dsl.undefinedSignal.msg"))

	override fun typedBinaryOp(node: BinaryOperation): Any {
		return when (node.op.type) {
			EQUAL -> evaluateEqualCondition(node, interpret(node.left), interpret(node.right))
			AND -> binaryOp(node,
				{ l, r -> l.and(r) },
				{ l, r -> l.and(r)},
				{ l, r -> l.and(r.toULong()) },
				{ l, r -> l.and(signalToLong(r)) })
			OR -> binaryOp(node,
				{ l, r -> l.or(r) },
				{ l, r -> l.or(r) },
				{ l, r -> l.or(r.toULong()) },
				{ l, r -> l.or(signalToLong(r)) })
			PLUS -> binaryOp(node,
				{ l, r -> l + r},
				{ l, r -> l.add(r) },
				{ l, r -> l.add(r.toUInt()) },
				{ l, r -> l.plus(signalToLong(r)) })
			MINUS -> binaryOp(node,
				{ l, r -> l - r },
				{ l, r -> l.subtract(r)},
				{ l, r -> l.subtract(r.toUInt()) },
				{ l, r -> l.minus(signalToLong(r)) })
			MULTIPLY -> binaryOp(node,
				{ l, r -> l * r},
				{ l, r -> l.multiply(r) },
				{ l, r -> l.multiply(r.toUInt()) },
				{ l, r -> l * signalToLong(r) })
			DIVIDE -> binaryOp(node,
				{ l, r -> l.div(r)},
				{ l, r -> l.divide(r) },
				{ l, r -> l.divide(r.toUInt()) },
				{ l, r -> l.div(signalToLong(r)) })
			DIFF -> binaryOp(node,
				{ l, r -> if (l != r) 1L else 0L},
				{ l, r -> if (l != r) 1L else 0L },
				{ l, r -> if (l.toLong() != r.toULong()) 1L else 0L },
				{ l, r -> if (l != signalToLong(r)) 1L else 0L })
			MOD -> binaryOp(node,
				{ l, r -> l.mod(r) },
				{ l, r -> l.mod(r) },
				{ l, r -> l.mod(r.toULong()) },
				{ l, r -> l.mod(signalToLong(r)) })
			GREATER -> binaryOp(node,
				{ l, r -> if (l > r) 1L else 0L },
				{ l, r -> if (l.isGreaterThan(r)) 1L else 0L },
				{ l, r -> if (l.isGreaterThan(r.toULong())) 1L else 0L },
				{ l, r -> if (l > signalToLong(r)) 1L else 0L })
			GREATER_EQUAL -> binaryOp(node,
				{ l, r -> if (l >= r) 1L else 0L },
				{ l, r -> if (l.isGreaterEqualThan(r)) 1L else 0L },
				{ l, r -> if (l.isGreaterEqualThan(r.toULong())) 1L else 0L },
				{ l, r -> if (l >= signalToLong(r)) 1L else 0L })
			SMALLER -> binaryOp(node,
				{ l, r -> if (l < r) 1L else 0L },
				{ l, r -> if (l.isSmallerThan(r)) 1L else 0L },
				{ l, r -> if (l.isSmallerThan(r.toULong())) 1L else 0L },
				{ l, r -> if (l < signalToLong(r)) 1L else 0L})
			SMALLER_EQUAL -> binaryOp(node,
				{ l, r -> if (l <= r) 1L else 0L },
				{ l, r -> if (l.isSmallerEqualThan(r)) 1L else 0L },
				{ l, r -> if (l.isSmallerEqualThan(r.toULong())) 1L else 0L },
				{ l, r -> if (l <= signalToLong(r)) 1L else 0L })
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
		mixedOp1: (DigitalSignal, Long) -> Any,
		mixedOp2: (Long, DigitalSignal) -> Any
	): Any {
		val left = interpret(node.left)
		val right = interpret(node.right)
		return binaryOpInterpreted(node, node.op.type, left, right, longOp, signalOp, mixedOp1, mixedOp2)
	}

	private fun binaryOpInterpreted(
		node: Node,
		op: TokenType,
		left: Any,
		right: Any,
		longOp: (Long, Long) -> Any,
		signalOp: (DigitalSignal, DigitalSignal) -> Any,
		mixedOp1: (DigitalSignal, Long) -> Any,
		mixedOp2: (Long, DigitalSignal) -> Any
	): Any {
		try {
			return if (left is Long && right is Long) {
				longOp(left, right)
			} else if (left is DigitalSignal && right is DigitalSignal) {
				signalOp(left, right)
			} else if (left is DigitalSignal && right is Long) {
				mixedOp1(left, right)
			} else if (left is Long && right is DigitalSignal) {
				mixedOp2(left, right)
			} else {
				throw RuntimeError(node.location, Translations.getString("base.dsl.incompatibleTypes.msg", op.id))
			}
		} catch (e: Throwable) {
			if (e.message != null) {
				throw RuntimeError(node.location, Translations.getString("antares.dsl.operationExecutionErrorMsg.msg", e.message!!))
			} else {
				throw RuntimeError(node.location, Translations.getString("antares.dsl.operationExecutionError.msg"))
			}
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
			else -> throw RuntimeError(node.location, Translations.getString("base.dsl.incompatibleTypes.msg", node.op.type.id))
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
			else -> throw RuntimeError(node.location, Translations.getString("base.dsl.incompatibleTypes.msg", node.op.type.id))
		}
	}

	private fun raisedInput(node: RaisedInput): Any {
		val portName = node.variable.token.value as String
		return data?.let {
			if (it.changedPort?.name == portName
				&& (it.changedPort as DigitalPort).logic.evaluate(it.getSignal<DigitalSignal>(it.changedPort!!.portId)!!.bitAt(0)).isSet
			) 1L else 0L
		} ?: 0L
	}
}