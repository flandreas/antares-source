package ch.scorpion.antares.script.dsl

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.view.VerticeView

class CircuitElementViewBridge(
	private val verticeView: VerticeView<*>,
	signalHandler: SignalHandler?,
	private val drawContext: DrawContext? = null
) : CircuitElemModelBridge(verticeView.model!!, signalHandler, null, null) {

	/** Returns the ID of the circuit element view.*/
	override fun id(): Int = verticeView.id

	/** Draws a data flow line from the [InputPort] with name [input] to the [OutputPort] with name [output].*/
	@Suppress("unused")
	fun drawDataFlow(input: String, output: String) {
		verticeView.drawDataFlow(input, output, drawContext!!)
	}
}