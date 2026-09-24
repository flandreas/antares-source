package io.antarescircuit.antares.dsl

import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.Word
import io.antarescircuit.jabbah.base.dsl.ActivationRecord
import io.antarescircuit.jabbah.base.dsl.StoringActivationRecord
import io.antarescircuit.jabbah.base.dsl.Variable
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.model.graph.GraphActivationRecord

class AntaresStoringActivationRecord(
	name: String,
	parent: ActivationRecord?
) : StoringActivationRecord(name, parent) {

	private val graphActivationRecord: GraphActivationRecord? get() {
		var p = parent
		if (p is GraphActivationRecord) {
			return p
		}
		while (p != null && p is StoringActivationRecord) {
			p = p.parent
			if (p is GraphActivationRecord) {
				return p
			}
		}
		return null
	}

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
							is DigitalSignal -> {
								if (graphActivationRecord?.graph?.getGraphOutput<DigitalSignal>(variable.token.value!!)?.portType == PortType.OUTPUT) {
									// For Circuit outputs, undefined bits must NOT be replaced
									super.store(variable, value)
								} else {
									super.store(variable, useIfDefined(presentValue, value))
								}
							}
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