package ch.scorpion.antares.dsl

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.dsl.ActivationRecord
import ch.scorpion.jabbah.base.dsl.StoringActivationRecord
import ch.scorpion.jabbah.base.dsl.Variable

class AntaresStoringActivationRecord(
	name: String,
	parent: ActivationRecord?
) : StoringActivationRecord(name, parent) {

	override fun store(variable: Variable, value: Any) {
		when (value) {
			is DigitalSignal -> {
				// Don't store partially undefined DigitalSignals
				if (!value.isPartiallyUndefined) {
					super.store(variable, value)
				}
			}
			else -> super.store(variable, value)
		}
	}
}