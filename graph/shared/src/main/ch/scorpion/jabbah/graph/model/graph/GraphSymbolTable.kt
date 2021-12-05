package ch.scorpion.jabbah.graph.model.graph

import ch.scorpion.jabbah.base.dsl.Symbol
import ch.scorpion.jabbah.base.dsl.SymbolTable
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * Implicitly and dynamically defines the names of all [GraphPort]s as variable names.
 * This relieves [Graph] from the burden to update its [SymbolTable] whenever
 * [GraphPort]s are added or removed, or when their names change.
 */
class GraphSymbolTable(
	private val graph: Graph,
	override val scopeLevel: Int = 0
) : SymbolTable {

	override val enclosingScope: SymbolTable? get() = null

	override val symbolsCount: Int get() = graph.graphPorts.size

	override fun define(symbol: Symbol) {
		throw UnsupportedOperationException("Cannot define additional symbols in GraphView SymbolTable")
	}

	override fun hasSymbol(name: String, currentScopeOnly: Boolean): Boolean =
		graph.graphPorts.any { it.name == name } ||
			graph.parameterDefinitions.contains(name)

	override fun lookup(name: String, currentScopeOnly: Boolean): Symbol? =
		graph.graphPorts.firstOrNull { it.name == name }?.let { Symbol(it.name!!) }
			?: graph.parameterDefinitions.withName(name)?.let { Symbol(it.name) }
}