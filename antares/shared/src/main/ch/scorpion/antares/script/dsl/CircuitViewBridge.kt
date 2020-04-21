package ch.scorpion.antares.script.dsl

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphInput
import ch.scorpion.jabbah.graph.model.GraphOutput
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Scenario
import ch.scorpion.jabbah.graph.view.VerticeView

open class CircuitViewBridge(
	private val view: DrawingView<GraphView>,
	private val signalHandler: SignalHandler?
) {

	companion object {
		private val LOG by logger(CircuitViewBridge::class)
	}

	/** Returns the name of the circuit model.*/
	fun name(): String = view.drawing.graph!!.name.value

	/** Returns the signal of the [GraphInput] with the specified name as a [String].*/
	fun input(name: String): String = view.drawing.graph!!.getGraphInput<DigitalSignal>(name)!!.getOutput<DigitalSignal>().getOutgoingSignal().toString()

	/** Returns the signal of the [GraphOutput] with the specified name as a [String].*/
	fun output(name: String): String = view.drawing.graph!!.getGraphOutput<DigitalSignal>(name)!!.getInput<DigitalSignal>().getIncomingSignal().toString()

	/** Returns the circuit element with the specified ID.*/
	@Suppress("unused")
	fun elem(id: Int): CircuitElementViewBridge = CircuitElementViewBridge(
		view.drawing.getWithId(id)!! as VerticeView, signalHandler)

	/** Returns the ID of the current [Scenario], or an empty [String] if none is active.*/
	@Suppress("unused")
	fun scenario(): String {
		val scenario = view.drawing.currentScenario
		return scenario?.id.toString()
	}

	/** Highlights the circuit elements with the specified IDs.*/
	@Suppress("unused")
	fun highlight(vararg ids: Int) {
		view.highlighter.highlight(*ids)
	}

	/** Removes all highlights from the circuit.*/
	@Suppress("unused")
	fun unhighlight() {
		view.highlighter.unhighlightAll()
	}
}