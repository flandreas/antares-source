package io.antarescircuit.antares.model.analog

import io.antarescircuit.antares.view.analog.engine.AnalogElementMixin
import io.antarescircuit.antares.view.analog.engine.AnalogCircuitAnalysis
import io.antarescircuit.jabbah.graph.model.vertice.EmptyVerticeCalculator

class AnalogGround : AbstractAnalogVertice<AnalogGround>(
	EmptyVerticeCalculator,
	"library.element.AnalogGround",
	AnalogElementMixin(postCount = 1)
) {

	init {
		addPort(AnalogPort())
	}

	override val voltageSourceCount: Int get() = 1

	override fun stamp(analysis: AnalogCircuitAnalysis) {
		analysis.stampVoltageSource(0, analogElem.nodes[0], analogElem.voltageSource, 0.0)
	}
}