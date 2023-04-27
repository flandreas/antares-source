package ch.scorpion.antares.view.analog

import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.antares.model.analog.AnalogTwoPortVertice
import ch.scorpion.antares.model.analog.AnalogVertice

/**
 * An analog [VerticeView] that is part of an [AnalogCircuitBranch] by producing the same
 * amount of current at one [AnalogPortView] it consumes at another [AnalogPortView].
 *
 * These are typically [VerticeView]s of [AnalogTwoPortVertice]s, but could also be
 * [VerticeView]s of models with 3 ports, such as a voltage-controlled MOSFET transistor
 * whose gate doesn't consume current, and whose source-gate path acts like a resistor.
 */
interface AnalogBranchVerticeView<T : AnalogVertice> : VerticeView<T> {

	/**
	 * Returns the branch [PortView] opposite to [portView], or `null` if
	 * [portView] isn't part of an [AnalogCircuitBranch].
	 */
	fun getOppositeBranchPortView(portView: AnalogPortView): AnalogPortView? =
		getPortViews().first { it !== portView } as AnalogPortView
}