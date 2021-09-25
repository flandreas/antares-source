package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.dsl.ExternalFunctionSymbol
import ch.scorpion.jabbah.base.dsl.ScopedSymbolTable
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

	override fun defineIn(symbolTable: ScopedSymbolTable) {
		symbolTable.define(ExternalFunctionSymbol("drawDataFlow", 2, ::drawDataFlow))
	}

	/** Maps to [VerticeView.drawDataFlow]. */
	private fun drawDataFlow(params: List<Any>): Any {
		view.drawDataFlow(
			stringParam(0, params),
			stringParam(1, params),
			context)
		return 0L
	}
}