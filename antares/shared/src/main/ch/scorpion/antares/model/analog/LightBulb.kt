package ch.scorpion.antares.model.analog

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/**
 * Treating [LightBulb] as element with constant resistance, i.e. independent of changing
 * temperature when the current increases.
 */
class LightBulb : AbstractResistingAnalogVertice<LightBulb>(DEF_RESISTANCE, CALCULATOR, "library.element.LightBulb") {

	companion object {
		private const val DEF_RESISTANCE = 20.0
		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<LightBulb> {
			override fun calculate(vertice: LightBulb, data: GraphActorData, signalHandler: SignalHandler) {
				// TODO
			}
		}
	}
}