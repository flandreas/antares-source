package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.signal.*
import ch.scorpion.antares.model.testcase.Value.State.*
import ch.scorpion.antares.model.testcase.Value.State.NORMAL
import ch.scorpion.antares.model.testcase.Value.Type.*
import ch.scorpion.jabbah.edit.Cloneable

open class Value(
	val value: DigitalSignal,
	val type: Type = Type.NORMAL,
	val representation: DigitalSignalRepresentation = DigitalSignalRepresentation.DECIMAL
) : Cloneable<Value> {

	constructor(
		signal: DigitalSignal,
		representation: DigitalSignalRepresentation = DigitalSignalRepresentation.DECIMAL
	): this(
		signal,
		if (signal.isFullyUndefined) UNDEFINED else Type.NORMAL,
		representation
	)

	companion object {
		val X = Value(DigitalSignalFactory.of(Bit.False), DONT_CARE)
		val Z = Value(DigitalSignalFactory.of(Bit.Undefined), UNDEFINED)
	}

	enum class Type {
		NORMAL,
		UNDEFINED,
		DONT_CARE,
		CLOCKED,
	}

	enum class State {
		NORMAL,
		PASSED,
		FAILED
	}

	open fun withValue(value: DigitalSignal): Value =
		Value(value = value, type = type, representation = representation)

	override fun doClone(): Value = Value(value, type, representation)

	override fun toString(): String =
		when (type) {
			Type.NORMAL -> CurrentDigitalSignalNotation.notation.notate(value, representation)
			UNDEFINED -> "Z"
			DONT_CARE -> "X"
			CLOCKED -> "^$value"
		}

	open val state: State get() = NORMAL

	override fun equals(other: Any?): Boolean {
		if (other == null) {
			return false
		}
		if (this === other) {
			return true
		}
		if (other !is Value) {
			return false
		}
		if (this.type == DONT_CARE || other.type == DONT_CARE) {
			return true
		}
		if (this.type != other.type) {
			return false
		}
		if (type == UNDEFINED) {
			return true
		}
		return this.value == other.value
	}

	override fun hashCode(): Int {
		var result = value.hashCode()
		result = 31 * result + type.hashCode()
		return result
	}
}

class MatchedValue(
	val expected: Value,
	actual: DigitalSignal,
) : Value(actual, representation = expected.representation) {

	private val isPassed: Boolean = this == expected

	override val state: State = if (isPassed) PASSED else FAILED

	override fun withValue(value: DigitalSignal): Value =
		MatchedValue(expected, value)
}