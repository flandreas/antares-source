package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.testcase.Value.State.*
import ch.scorpion.antares.model.testcase.Value.State.NORMAL
import ch.scorpion.antares.model.testcase.Value.Type.*
import ch.scorpion.jabbah.edit.Cloneable

open class Value(
	val value: ULong,
	val type: Type = Type.NORMAL
) : Cloneable<Value> {

	constructor(signal: DigitalSignal): this(
		signal.toLong() ?: 0UL,
		if (signal.isPartiallyUndefined) UNDEFINED else Type.NORMAL
	)

	companion object {
		val X = Value(0UL, DONT_CARE)
	}

	enum class Type {
		NORMAL,
		UNDEFINED,
		DONT_CARE
	}

	enum class State {
		NORMAL,
		PASSED,
		FAILED
	}

	override fun doClone(): Value = Value(value, type)

	override fun toString(): String =
		when (type) {
			Type.NORMAL -> value.toString()
			UNDEFINED -> "Z"
			DONT_CARE -> "X"
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
	actual: DigitalSignal
) : Value(actual) {

	private val isPassed: Boolean get() = this == expected

	override val state: State get() = if (isPassed) PASSED else FAILED
}