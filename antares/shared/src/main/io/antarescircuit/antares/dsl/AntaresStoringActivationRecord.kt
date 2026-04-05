package io.antarescircuit.antares.dsl

import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.Word
import io.antarescircuit.jabbah.base.dsl.ActivationRecord
import io.antarescircuit.jabbah.base.dsl.StoringActivationRecord
import io.antarescircuit.jabbah.base.dsl.Variable

class AntaresStoringActivationRecord(
	name: String,
	parent: ActivationRecord?
) : StoringActivationRecord(name, parent) {

	override fun store(variable: Variable, value: Any) {
		when (value) {
			is DigitalSignal -> {
				if (!value.isPartiallyUndefined) {
					super.store(variable, value)
				} else {
					val presentValue = getOptionalValue(variable)
					if (presentValue == null) {
						super.store(variable, value)
					} else {
						when (presentValue) {
							is DigitalSignal -> super.store(variable, useIfDefined(presentValue, value))
							is ULong -> super.store(variable, value.or(presentValue))
							else -> super.store(variable, value)
						}
					}
				}

			}
			else -> super.store(variable, value)
		}
	}

	private fun useIfDefined(present: DigitalSignal, other: DigitalSignal): DigitalSignal =
		Word((0 until present.bitWidth.width).map {
			if (it < other.bitWidth.width) {
				if (other.bitAt(it).isDefined) {
					other.bitAt(it)
				} else {
					present.bitAt(it)
				}
			} else {
				present.bitAt(it)
			}
		})
}