package ch.scorpion.antares.view.analog

import ch.scorpion.jabbah.execution.SignalHandler

/**
 * Performs voltage/current calculation for an [AnalogGraphView].
 */
interface AnalogCircuitCalculator<T : AnalogCircuitAnalysis> {

	/**
	 * Analyses the structure of an [AnalogGraphView] and returns all information needed for
	 * calculating electrical currents and voltages depending on the actual resistances in the circuit.
	 *
	 * @throws IllegalStateException in case of an invalid circuit
	 * */
	fun analyse(circuitView: AnalogGraphView): T

	/**
	 * Calculates electrical currents and voltages in an [AnalogGraphView]. Prior to calculation,
	 * [analysis] must be called, typically at the start of the simulation.
	 */
	fun calculate(analysis: T, signalHandler: SignalHandler)
}

interface AnalogCircuitCalculatorFactory {
	fun <T : AnalogCircuitAnalysis> create(): AnalogCircuitCalculator<T>
}