package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.dsl.CodeLocation
import ch.scorpion.jabbah.base.dsl.ExternalFunctionSymbol
import ch.scorpion.jabbah.base.dsl.RuntimeError
import ch.scorpion.jabbah.base.dsl.ScopedSymbolTable
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * Defines the external functions used by DSL scripts that adjust a [SubGraphVerticeView]'s symbol
 * during execution.
 */
object DrawExecSymbolFunctions {

	private var view: SubGraphVerticeView<*>? = null
	private var context: DrawContext? = null

	fun bind(view: SubGraphVerticeView<*>, context: DrawContext) {
		this.view = view
		this.context = context
	}

	fun defineIn(symbolTable: ScopedSymbolTable) {
		symbolTable.define(ExternalFunctionSymbol("drawDataFlow", 2, ::drawDataFlow))
	}

	/** Maps to [VerticeView.drawDataFlow]*/
	private fun drawDataFlow(params: List<Any>): Any {
		checkBinding()
		view!!.drawDataFlow(
			stringParam(0, params),
			stringParam(1, params),
			context!!)
		return 0L
	}

	private fun checkBinding() {
		if (view == null || context == null) {
			throw IllegalStateException("not bound")
		}
	}

	private fun longParam(index: Int, params: List<Any>): Long {
		if (index >= params.size) {
			throw RuntimeError(CodeLocation.UNDEFINED, "Not enough parameters")
		}
		val param = params[index]
		if (param !is Long) {
			throw RuntimeError(CodeLocation.UNDEFINED, "Expected number in parameter $index")
		}
		return param
	}

	private fun stringParam(index: Int, params: List<Any>): String {
		if (index >= params.size) {
			throw RuntimeError(CodeLocation.UNDEFINED, "Not enough parameters")
		}
		val param = params[index]
		if (param !is String) {
			throw RuntimeError(CodeLocation.UNDEFINED, "Expected string in parameter $index")
		}
		return param
	}
}