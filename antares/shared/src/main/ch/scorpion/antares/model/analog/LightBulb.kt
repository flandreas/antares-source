package ch.scorpion.antares.model.analog

import ch.scorpion.jabbah.graph.model.vertice.EmptyVerticeCalculator

/**
 * Treating [LightBulb] as element with constant resistance, i.e. independent of changing
 * temperature when the current increases.
 */
class LightBulb : AbstractResistingAnalogVertice<LightBulb>(DEF_RESISTANCE, EmptyVerticeCalculator, "library.element.LightBulb") {

	companion object {
		private const val DEF_RESISTANCE = 20.0
	}
}