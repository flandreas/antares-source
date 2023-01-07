package ch.scorpion.antares.model.analog

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

class LightBulb : AbstractAnalogTwoPortVertice<LightBulb>(CALCULATOR, "library.element.LightBulb") {

	companion object {
		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<LightBulb> {
			override fun calculate(vertice: LightBulb, data: GraphActorData, signalHandler: SignalHandler) {
				// TODO
			}
		}
	}
}