package io.antarescircuit.jabbah.graph.container

import io.antarescircuit.jabbah.base.dsl.*
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * Defines the external functions used by DSL scripts that adjust a [SubGraphVerticeView]'s symbol
 * during execution.
 */
object DrawExecSymbolFunctions : DslExternalFunctions {

	private lateinit var view: SubGraphVerticeView<*>
	private lateinit var context: DrawContext

	fun bind(view: SubGraphVerticeView<*>, context: DrawContext) {
		this.view = view
		this.context = context
	}

	override fun defineIn(symbolTable: SymbolTable) {
		symbolTable.define(ExternalFunctionSymbol("drawDataFlow", 2, ::drawDataFlowImpl))
		symbolTable.define(ExternalFunctionSymbol("setLabel", 1, ::setLabelImpl))
	}

	/** Maps to [VerticeView.drawDataFlow]. */
	private fun drawDataFlowImpl(params: List<Any>, @Suppress("UNUSED_PARAMETER") context: Any? = null): Any {
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

	private fun setLabelImpl(params: List<Any>, @Suppress("UNUSED_PARAMETER") context: Any? = null): Any {
		setLabel(
			stringParam(0, params))
		return 0L
	}

	/**
	 * Sets the label of the symbol during execution. Only applicable if the symbol contains a label component.
	 * @param label the current label text
	 */
	private fun setLabel(label: String) {
		view.executionLabel = TranslatableText(label)
	}
}