package io.antarescircuit.antares.model.analog

import io.antarescircuit.antares.view.analog.engine.AnalogElementMixin
import io.antarescircuit.jabbah.graph.model.vertice.CalculatingVertice
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator

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