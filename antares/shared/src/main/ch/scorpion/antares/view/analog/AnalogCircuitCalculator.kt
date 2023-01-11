package ch.scorpion.antares.view.analog

import ch.scorpion.jabbah.execution.SignalHandler

/**
 * Performs voltage/current calculation for an [AnalogGraphView].
 */
interface AnalogCircuitCalculator {

	fun calculate(circuitView: AnalogGraphView, signalHandler: SignalHandler)
}