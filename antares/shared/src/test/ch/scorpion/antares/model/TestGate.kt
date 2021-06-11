package ch.scorpion.antares.model

import ch.scorpion.antares.model.gate.AbstractDigitalGate
import ch.scorpion.antares.model.gate.AbstractDigitalGateCalculator
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

class TestGate(
	calculator: AbstractDigitalGateCalculator
) : AbstractDigitalGate(calculator, InputCount.TWO) {

	override val type: String get() = "TestVertice"
	override val typeDesc: String? get() = null
}

class TestCalculatingVertice<T : CalculatingVertice>(
	calculator: VerticeCalculator<T>
) : CalculatingVertice(calculator) {

	override val type: String get() = "TestVertice"
	override val typeDesc: String? get() = null
}