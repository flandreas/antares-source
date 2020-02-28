package ch.scorpion.antares.model

import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

class TestCalculatingVertice(
	calculator: VerticeCalculator<CalculatingVertice>
) : CalculatingVertice(calculator) {

	override val type: String get() = "TestVertice"
	override val typeDesc: String? get() = null
}