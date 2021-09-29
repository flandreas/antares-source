package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.dsl.ExternalFunctionSymbol
import ch.scorpion.jabbah.base.dsl.SymbolTable
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.graph.dsl.AbstractExternalFunctions
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * Defines the external functions used by DSL scripts that adjust a [SubGraphVerticeView]'s symbol
 * during execution.
 */
object DrawExecSymbolFunctions : AbstractExternalFunctions() {

	private lateinit var view: SubGraphVerticeView<*>
	private lateinit var context: DrawContext

	fun bind(view: SubGraphVerticeView<*>, context: DrawContext) {
		this.view = view
		this.context = context
	}

	override fun defineIn(symbolTable: SymbolTable) {
		symbolTable.define(ExternalFunctionSymbol("drawDataFlow", 2, ::drawDataFlowImpl))
	}

	/** Maps to [VerticeView.drawDataFlow]. */
	private fun drawDataFlowImpl(params: List<Any>): Any {
		drawDataFlow(
			stringParam(0, params),
			stringParam(1, params))
		return 0L
	}

	/**
	 * Draws a line in the correct signal color from an input pin to an output pin.
	 * @param inputName the name of the input pin
	 * @param outputName the name of the output pin
	 */
	private fun drawDataFlow(inputName: String, outputName: String) {
		view.drawDataFlow(inputName, outputName, context)
	}
}