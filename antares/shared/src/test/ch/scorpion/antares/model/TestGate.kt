package ch.scorpion.antares.model

import ch.scorpion.antares.model.gate.*

class TestGate(
	gateType: LogicGateType
) : AbstractLogicGate(gateType, PortCount.TWO) {

	override val type: String get() = "TestVertice"
	override val typeDesc: String? get() = null
}
