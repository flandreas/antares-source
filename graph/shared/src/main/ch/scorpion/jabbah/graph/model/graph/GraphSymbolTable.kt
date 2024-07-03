package ch.scorpion.jabbah.graph.model.graph

import ch.scorpion.jabbah.base.dsl.Symbol
import ch.scorpion.jabbah.base.dsl.SymbolTable
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.param.GraphParamDefinition

/**
 * Implicitly and dynamically defines the names of all [GraphPort]s
 * and [GraphParamDefinition]s as variable names.
 *
 * This reliefs [Graph] from the burden to update its [SymbolTable] whenever
 * [GraphPort]s are added or removed, or when their names change.
 */
class GraphSymbolTable(
	private val graph: Graph,
	override val scopeLevel: Int = 0
) : SymbolTable {

	override val enclosingScope: SymbolTable? get() = null

	override val symbolsCount: Int get() = graph.graphPorts.size + graph.parameterDefinitions.size

	override fun names(): Iterator<String> {
		val names = mutableListOf<String>()
		names.addAll(graph.graphPorts.mapNotNull { it.name })
		names.addAll(graph.parameterDefinitions.map { it.name })
		return names.iterator()
	}

	override fun define(symbol: Symbol) {
		throw UnsupportedOperationException("Cannot define additional symbols in GraphView SymbolTable")
	}

	override fun hasSymbol(name: String, currentScopeOnly: Boolean): Boolean =
		graph.graphPorts.any { it.name == name } ||
			graph.parameterDefinitions.contains(name)

	override fun lookup(name: String, currentScopeOnly: Boolean): Symbol? =
		graph.graphPorts.firstOrNull { it.name == name }?.let { Symbol(it.name!!) }
			?: graph.parameterDefinitions.get(name)?.let { Symbol(it.name) }

	override fun canWrite(name: String): Boolean =
		graph.getGraphPort<Any>(name)?.portType?.isOutput ?: false
}