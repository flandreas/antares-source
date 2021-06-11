package ch.scorpion.antares.model

import ch.scorpion.antares.model.gate.AbstractDigitalGate
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

class TestCalculatingVertice(
	calculator: VerticeCalculator<AbstractDigitalGate>,
	inputCount: InputCount = InputCount.TWO
) : AbstractDigitalGate(calculator, inputCount) {

	override val type: String get() = "TestVertice"
	override val typeDesc: String? get() = null
}