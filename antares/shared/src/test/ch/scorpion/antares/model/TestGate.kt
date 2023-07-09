package ch.scorpion.antares.model

import ch.scorpion.antares.model.gate.AbstractLogicGate
import ch.scorpion.antares.model.gate.AbstractLogicGateCalculator

class TestGate(
	calculator: AbstractLogicGateCalculator
) : AbstractLogicGate(calculator, PortCount.TWO) {

	override val type: String get() = "TestVertice"
	override val typeDesc: String? get() = null
}
