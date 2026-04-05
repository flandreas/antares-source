package io.antarescircuit.antares.dsl

import io.antarescircuit.antares.model.analog.AnalogSignal
import io.antarescircuit.antares.model.gate.CurrentUndefinedGateInputBehavior
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.signal.*
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.dsl.*
import io.antarescircuit.jabbah.base.dsl.DslTokenType.*
import io.antarescircuit.jabbah.base.parser.TextLocation
import io.antarescircuit.jabbah.graph.dsl.GraphDslInterpreter
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphFunctionContext
import kotlin.math.pow

class AntaresInterpreter(
	node: Node,
	memory: Memory = Memory()
) : GraphDslInterpreter(node, memory) {

	constructor(parser: AntaresParser): this(parser.parse())
	constructor(parser: AntaresParser, memory: Memory): this(parser.parse(), memory)
	constructor(program: String): this(AntaresParser(program))

	private val context: SubGraphFunctionContext? get() = params as? SubGraphFunctionContext

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

	override fun storeValue(variable: Variable, value: Any): Any {
		return when (variable) {
			is BitAccess -> setBit(variable, value)
			else -> super.storeValue(variable, value)
		}
	}

	override fun loadValue(variable: Variable): Any =
		when (variable) {
			is BitAccess -> getBit(variable)
			is LengthCast -> getLengthCastedValue(variable)
			else -> super.loadValue(variable)
		}

	override fun interpretAssocArrayKey(variable: AssocArray): Long {
		val key = interpret(variable.key)
		return when (key) {
			is DigitalSignal -> key.toLong()?.toLong() ?: throw RuntimeError(variable.location, Translations.getString("antares.dsl.arrayIndexNotFullyDefined.msg"))
			else -> super.interpretAssocArrayKey(variable)
		}
	}

	private fun getLengthCastedValue(lengthCast: LengthCast): Any {
		val value = memory.getValue(lengthCast)
		val length = getLengthCastLength(lengthCast)
		if (length > BitWidth.MAX) {
			throw RuntimeError(lengthCast.location, Translations.getString("antares.dsl.lengthCastLengthTooLarge.msg", BitWidth.MAX.toString()))
		}
		return when (value) {
			is DigitalSignal -> {
				value.ofWidth(BitWidth.of(length))
			}
			is Long -> value // nothing to cast
			else ->  throw RuntimeError(lengthCast.location, Translations.getString("antares.dsl.lengthCastNotSupportedByType.msg"))
		}
	}

	private fun getLengthCastLength(lengthCast : LengthCast): Int {
		val length = interpret(lengthCast.length)
		return when (length) {
			is Long -> length.toInt()
			is DigitalSignal -> {
				signalToLong(length).toInt()
			}
			else -> throw RuntimeError(lengthCast.location, Translations.getString("antares.dsl.lengthCastNotSupportedByType.msg"))
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
		val newValue: Any = when (oldValue) {
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
				signalToLong(index).toInt()
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
		signal.toLong()?.toLong() ?: CurrentUndefinedGateInputBehavior.value.definedValue(signal.bitWidth).toLong()!!.toLong()

	override fun binaryOperation(node: BinaryOperation): Any {
		var left = interpret(node.left)
		var right = interpret(node.right)

		if (left is AnalogSignal) left = left.voltage.toFloat()
		if (right is AnalogSignal) right = right.voltage.toFloat()

		return binaryOpInterpreted(node.location, node.op.type, left, right)
	}

	override fun addL(l: Any, r: Any, loc: TextLocation): Any =
		when (l) {
			is DigitalSignal -> addR(l, r, loc)
			else -> super.addL(l, r, loc)
		}

	private fun addR(l: DigitalSignal, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> l.add(signalToLong(r).toUInt())
			is Long -> l.add(r.toUInt())
			else -> throwIncompatibleTypes(loc, PLUS)
		}

	override fun addR(l: Long, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> l + signalToLong(r)
			else -> super.addR(l, r, loc)
		}

	override fun addR(l: Float, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> addR(l, signalToLong(r), loc)
			else -> super.addR(l, r, loc)
		}

	override fun subtractL(l: Any, r: Any, loc: TextLocation): Any =
		when (l) {
			is DigitalSignal -> subtractR(l, r, loc)
			else -> super.subtractL(l, r, loc)
		}

	private fun subtractR(l: DigitalSignal, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> l.subtract(signalToLong(r).toUInt())
			is Long -> l.subtract(r.toUInt())
			else -> throwIncompatibleTypes(loc, MINUS)
		}

	override fun subtractR(l: Long, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> l - signalToLong(r)
			else -> super.subtractR(l, r, loc)
		}

	override fun subtractR(l: Float, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> subtractR(l, signalToLong(r), loc)
			else -> super.subtractR(l, r, loc)
		}

	override fun multiplyL(l: Any, r: Any, loc: TextLocation): Any =
		when (l) {
			is DigitalSignal -> multiplyR(l, r, loc)
			else -> super.multiplyL(l, r, loc)
		}

	private fun multiplyR(l: DigitalSignal, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> l.multiply(signalToLong(r).toUInt())
			is Long -> l.multiply(r.toUInt())
			else -> throwIncompatibleTypes(loc, MULTIPLY)
		}

	override fun multiplyR(l: Long, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> l * signalToLong(r)
			else -> super.multiplyR(l, r, loc)
		}

	override fun multiplyR(l: Float, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> multiplyR(l, signalToLong(r), loc)
			else -> super.multiplyR(l, r, loc)
		}

	override fun divideL(l: Any, r: Any, loc: TextLocation): Any =
		when (l) {
			is DigitalSignal -> divideR(l, r, loc)
			is Long -> divideR(l, r, loc)
			is Float -> divideR(l, r, loc)
			else -> super.divideL(l, r, loc)
		}

	private fun divideR(l: DigitalSignal, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> signalToLong(r).let { if (it == 0L) l else l.divide(it.toULong()) }
			is Long -> l.divide(r.toULong())
			else -> throwIncompatibleTypes(loc, DIVIDE)
		}

	override fun divideR(l: Long, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> signalToLong(r).let { if (it == 0L) l else l.div(it) }
			is Long -> if (r == 0L) l else l.div(r)
			is Float -> if (r == 0.0F) l else l.div(r)
			else -> super.divideR(l, r, loc)
		}

	override fun divideR(l: Float, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> divideR(l, signalToLong(r), loc)
			else -> super.divideR(l, r, loc)
		}

	override fun powerL(l: Any, r: Any, loc: TextLocation): Any =
		when (l) {
			is DigitalSignal -> powerR(l, r, loc)
			else -> super.powerL(l, r, loc)
		}

	private fun powerR(l: DigitalSignal, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> l.power(signalToLong(r).toByte())
			is Long -> l.power(r.toByte())
			else -> throwIncompatibleTypes(loc, CARET)
		}

	override fun powerR(l: Long, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> l.toDouble().pow(signalToLong(r).toInt()).toLong()
			else -> super.powerR(l, r, loc)
		}

	override fun powerR(l: Float, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> powerR(l, signalToLong(r), loc)
			else -> super.powerR(l, r, loc)
		}

	override fun equalL(l: Any, r: Any, loc: TextLocation): Any =
		when (l) {
			is DigitalSignal -> equalR(l, r, loc)
			else -> super.equalL(l, r, loc)
		}

	private fun equalR(l: DigitalSignal, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> if (l.toLong() == r.toLong()) 1L else 0L
			is Long -> if (signalToLong(l) == r) 1L else 0L
			else -> throwIncompatibleTypes(loc, EQUAL)
		}

	override fun equalR(l: Long, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> if (l == signalToLong(r)) 1L else 0L
			else -> super.equalR(l, r, loc)
		}

	override fun equalR(l: Float, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> if (l == signalToLong(r).toFloat()) 1L else 0L
			else -> super.equalR(l, r, loc)
		}

	override fun smallerL(l: Any, r: Any, loc: TextLocation): Any =
		when (l) {
			is DigitalSignal -> smallerR(l, r, loc)
			else -> super.smallerL(l, r, loc)
		}

	private fun smallerR(l: DigitalSignal, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> if (signalToLong(l) < signalToLong(r)) 1L else 0L
			is Long -> if (signalToLong(l) < r) 1L else 0L
			else -> throwIncompatibleTypes(loc, SMALLER)
		}

	override fun smallerR(l: Long, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> if (l < signalToLong(r)) 1L else 0L
			else -> super.smallerR(l, r, loc)
		}

	override fun smallerR(l: Float, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> if (l < signalToLong(r).toFloat()) 1L else 0L
			else -> super.smallerR(l, r, loc)
		}

	override fun greaterL(l: Any, r: Any, loc: TextLocation): Any =
		when (l) {
			is DigitalSignal -> greaterR(l, r, loc)
			else -> super.greaterL(l, r, loc)
		}

	private fun greaterR(l: DigitalSignal, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> if (signalToLong(l) > signalToLong(r)) 1L else 0L
			is Long -> if (signalToLong(l) > r) 1L else 0L
			else -> throwIncompatibleTypes(loc, GREATER)
		}

	override fun greaterR(l: Long, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> if (l > signalToLong(r)) 1L else 0L
			else -> super.greaterR(l, r, loc)
		}

	override fun greaterR(l: Float, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> if (l > signalToLong(r).toFloat()) 1L else 0L
			else -> super.greaterR(l, r, loc)
		}

	override fun andL(l: Any, r: Any, loc: TextLocation): Any =
		when (l) {
			is DigitalSignal -> andR(l, r, loc)
			else -> super.andL(l, r, loc)
		}

	private fun andR(l: DigitalSignal, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> l.and(r)
			is Long -> l.and(r.toULong())
			else -> throwIncompatibleTypes(loc, AND)
		}

	override fun andR(l: Long, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> l.and(signalToLong(r))
			else -> super.andR(l, r, loc)
		}

	override fun orL(l: Any, r: Any, loc: TextLocation): Any =
		when (l) {
			is DigitalSignal -> orR(l, r, loc)
			else -> super.orL(l, r, loc)
		}

	private fun orR(l: DigitalSignal, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> l.or(r)
			is Long -> l.or(r.toULong())
			else -> throwIncompatibleTypes(loc, OR)
		}

	override fun orR(l: Long, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> l.or(signalToLong(r))
			else -> super.orR(l, r, loc)
		}

	override fun shiftLeftL(l: Any, r: Any, loc: TextLocation): Any =
		when (l) {
			is DigitalSignal -> shiftLeftR(l, r, loc)
			else -> super.shiftLeftL(l, r, loc)
		}

	private fun shiftLeftR(l: DigitalSignal, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> l.shiftLeft(signalToLong(r).toInt())
			is Long -> l.shiftLeft(r.toInt())
			else -> throwIncompatibleTypes(loc, SHIFT_LEFT)
		}

	override fun shiftLeftR(l: Long, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> l.shl(signalToLong(r).toInt())
			else -> super.shiftLeftR(l, r, loc)
		}

	override fun shiftRightL(l: Any, r: Any, loc: TextLocation): Any =
		when (l) {
			is DigitalSignal -> shiftRightR(l, r, loc)
			else -> super.shiftRightL(l, r, loc)
		}

	private fun shiftRightR(l: DigitalSignal, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> l.shiftRight(signalToLong(r).toInt())
			is Long -> l.shiftRight(r.toInt())
			else -> throwIncompatibleTypes(loc, SHIFT_RIGHT)
		}

	override fun shiftRightR(l: Long, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> l.shr(signalToLong(r).toInt())
			else -> super.shiftRightR(l, r, loc)
		}

	override fun modL(l: Any, r: Any, loc: TextLocation): Any =
		when (l) {
			is DigitalSignal -> modR(l, r, loc)
			else -> super.modL(l, r, loc)
		}

	private fun modR(l: DigitalSignal, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> l.mod(signalToLong(r).toULong())
			is Long -> l.mod(r.toULong())
			else -> throwIncompatibleTypes(loc, MOD)
		}

	override fun modR(l: Long, r: Any, loc: TextLocation): Any =
		when (r) {
			is DigitalSignal -> l.mod(signalToLong(r).toInt()).toLong()
			else -> super.modR(l, r, loc)
		}

	override fun not(value: Any, loc: TextLocation): Any =
		when (value) {
			is DigitalSignal -> {
				value.not()
			}
			is Long -> {
				// This does a signed bit inversion, resulting in "not 0" equals to "-1"
				// value.toULong().inv()
                when (value) {
                    0L -> 1L
                    1L -> 0L
                    else -> DigitalSignalFactory.ofMinimalBitWidth(value.toULong()).not().getValue().toLong()
                }
			}
			else -> {
				super.not(value, loc)
			}
		}

	override fun plus(value: Any, loc: TextLocation): Any =
		when (value) {
			is DigitalSignal -> value
			else -> super.plus(value, loc)
		}

	private fun raisedInput(node: RaisedInput): Any {
		val portName = node.variable.token.value as String
		return context?.let {
			if (it.data.changedPort?.name == portName
				&& (it.data.changedPort as DigitalPort).logic.evaluate(it.data.getSignal<DigitalSignal>(it.data.changedPort!!.portId)!!.bitAt(0)).isSet
			) 1L else 0L
		} ?: 0L
	}
}