package io.antarescircuit.antares.model

import io.antarescircuit.jabbah.graph.model.InputPort
import io.antarescircuit.antares.model.gate.AbstractLogicGate
import io.antarescircuit.jabbah.base.EnumProperty
import io.antarescircuit.jabbah.base.Translations

/**
 * Identifies a particular [InputPort] of an [AbstractLogicGate].
 */
enum class InputPortNumber(val id: Int) : EnumProperty<InputPortNumber> {
	NONE(0),
	ONE(1),
	TWO(2),
	THREE(3),
	FOUR(4),
	FIVE(5),
	SIX(6),
	SEVEN(7),
	EIGHT(8);

	companion object {

		fun of(value: Int): InputPortNumber {
			return InputPortNumber.values().first { it.id == value }
		}

		fun withId(id: Int): InputPortNumber {
			for (inputPortNumber in InputPortNumber.values()) {
				if (inputPortNumber.id == id) {
					return inputPortNumber
				}
			}
			throw IllegalArgumentException("Unknown InputPortNumber $id")
		}
	}

	override val customName: String get() = id.toString()

	override fun toString(): String {
		return when (id) {
			0 -> Translations.getString("element.property.InputPortNumber.none")
			else -> id.toString()
		}
	}
}