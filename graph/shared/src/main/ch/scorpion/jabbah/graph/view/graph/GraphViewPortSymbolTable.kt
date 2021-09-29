package ch.scorpion.jabbah.graph.view.graph

import ch.scorpion.jabbah.base.dsl.Symbol
import ch.scorpion.jabbah.base.dsl.SymbolTable
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * Implicitly and dynamically defines the names of all [GraphPort]s as variable names.
 * This relieves [GraphView] from the burden to update its [SymbolTable] whenever
 * [GraphPort]s are added or removed, or when their names change.
 */
class GraphViewPortSymbolTable(
	private val graphView: GraphView,
	override val scopeLevel: Int = 0
) : SymbolTable {

	override val enclosingScope: SymbolTable? get() = null

	override val symbolsCount: Int get() = graphView.graph!!.graphPorts.size

	override fun define(symbol: Symbol) {
		throw UnsupportedOperationException("Cannot define additional symbols in GraphView SymbolTable")
	}

	override fun hasSymbol(name: String, currentScopeOnly: Boolean): Boolean =
		graphView.graph!!.graphPorts.any { it.name == name }

	override fun lookup(name: String, currentScopeOnly: Boolean): Symbol? =
		graphView.graph!!.graphPorts.firstOrNull { it.name == name }?.let { Symbol(it.name!!) }
}