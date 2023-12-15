package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.*
import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Vertice

/**
 * Used as source of constituent equations when building the equation system during simulation.
 */
interface AnalogVertice : Vertice, AnalogElement {

	fun handleAnalogPortChanged(port: AnalogPort, signalHandler: SignalHandler) {
		// empty
	}
}

/**
 * Posted by [AnalogVertice] on the system's [EventBus] to indicate that an
 * [AnalogGraphView] containing [source] should recalculate. Needed because
 * some [AnalogVertice] are triggered by low-level model methods and don't
 * have access to [AnalogGraphView] at that moment.
 */
data class AnalogCalculationRequest(
	val source: AnalogVertice,
	val signalHandler: SignalHandler
)

