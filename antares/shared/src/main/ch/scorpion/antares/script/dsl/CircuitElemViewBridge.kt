package ch.scorpion.antares.script.dsl

import ch.scorpion.antares.script.AntaresScriptGateway
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.script.Script
import ch.scorpion.jabbah.graph.view.VerticeView

class CircuitElementViewBridge(
	script: Script,
	private val verticeView: VerticeView<*>,
	signalHandler: SignalHandler?,
	private val drawContext: DrawContext? = null,
	store: AntaresScriptGateway.Store
) : CircuitElemModelBridge(script, verticeView.model, signalHandler, null, store) {

	/** Returns the ID of the circuit element view.*/
	override fun id(): Int = verticeView.id

	/** Draws a data flow line from the [InputPort] with name [input] to the [OutputPort] with name [output].*/
	@Suppress("unused")
	fun drawDataFlow(input: String, output: String) {
		verticeView.drawDataFlow(input, output, drawContext!!)
	}
}