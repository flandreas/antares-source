package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.antares.view.analog.engine.AnalogCircuitAnalysis
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/** An [AnalogVertice] with an electrical resistance. */
interface ResistingAnalogVertice : AnalogVertice {
	val resistance: Double
}

/**
 * A base implementation of [ResistingAnalogVertice].
 */
abstract class AbstractResistingAnalogVertice<T: CalculatingVertice>(
	resistance: Double,
	calculator: VerticeCalculator<T>,
	baseResourceKey: String
) : AbstractAnalogTwoPortVertice<T>(calculator, baseResourceKey), ResistingAnalogVertice {

	override var resistance: Double = resistance
		set(value) {
			if (field != value) {
				field = value
				stateChanged(reason = MAIN_PROPERTY_STATE)
			}
		}

	/** ---- [AnalogElement] */

	override fun stamp(analysis: AnalogCircuitAnalysis) {
		analysis.stampResistor(analogElem.nodes[0], analogElem.nodes[1], resistance)
	}
}