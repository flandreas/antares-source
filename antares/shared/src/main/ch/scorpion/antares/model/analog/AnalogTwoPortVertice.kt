package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.engine.AnalogElementMixin
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/**
 * An [AnalogVertice] with exactly two [AnalogPort]s.
 */
interface AnalogTwoPortVertice : AnalogVertice

abstract class AbstractAnalogTwoPortVertice<T: CalculatingVertice>(
	calculator: VerticeCalculator<T>,
	baseResourceKey: String,
	analogElement: AnalogElementMixin = AnalogElementMixin()
) : AbstractAnalogVertice<T>(calculator, baseResourceKey, analogElement), AnalogTwoPortVertice {

	init {
		addPort(AnalogPort())
		addPort(AnalogPort())
	}
}