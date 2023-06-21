package ch.scorpion.antares.model

import ch.scorpion.antares.model.gate.AbstractDigitalGate
import ch.scorpion.antares.model.gate.AbstractDigitalGateCalculator

class TestGate(
	calculator: AbstractDigitalGateCalculator
) : AbstractDigitalGate(calculator, PortCount.TWO) {

	override val type: String get() = "TestVertice"
	override val typeDesc: String? get() = null
}
