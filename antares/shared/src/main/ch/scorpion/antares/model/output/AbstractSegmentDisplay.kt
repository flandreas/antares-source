package ch.scorpion.antares.model.output

import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

abstract class AbstractSegmentDisplay<T : Vertice>(
	calculator: VerticeCalculator<T>
) : CalculatingVertice(calculator) {

	abstract fun inputValueOf(bitName: String): Boolean
}