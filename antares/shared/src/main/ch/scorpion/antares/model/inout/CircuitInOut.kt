package ch.scorpion.antares.model.inout

import ch.scorpion.antares.model.input.Switch
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.vertice.InteractableVertice
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * A [Vertice] that can feed a signal into a circuit [Graph] and forward it to the outside of a [Graph].
 *
 * On a conceptual level, a [CircuitInOut] can be seen as a [Port] of an entire [Graph], although it
 * is a [Vertice] that contains a [Port] with a particular [PortType].
 *
 * [CircuitInOut]s are potentially bi-directional. Concrete implementations might allow the user
 * tho choose which signal directions (input, output, input-output) are to be supported.
 *
 * [CircuitInOut]s are interactable, i.e. the user can click on their views to change the
 * current signal. This is only allowed for top-level [CircuitInOut], because otherwise
 * the user's input would interfere with signals arriving from surrounding circuits.
 *
 * @param T type of the signal
 */
interface CircuitInOut<T : Any> : BidirectionalGraphPort<T>, InteractableVertice<T> {

	/**
	 * Determines whether this [CircuitInOut] belongs to a top-level [Graph]. Manually setting the input
	 * value is only allowed for top-level [CircuitInOut]s.
	 */
	val isToplevel: Boolean

	/**
	 * Sets the new signal entered manually by the user.
	 * This method is typically used by the UI and should use a propagation delay that is similar to the one
	 * used by a [Switch].
	 */
	fun setSignalManually(signal: T, signalHandler: SignalHandler)
}
