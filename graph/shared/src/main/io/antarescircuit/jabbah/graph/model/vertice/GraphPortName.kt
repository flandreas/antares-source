package io.antarescircuit.jabbah.graph.model.vertice

import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.GraphPort
import io.antarescircuit.jabbah.graph.model.PortType

/**
 * Utility for dealing with names of [GraphPort]s.
 */
object GraphPortName {

	// DOT_MATCHES_ALL: names may contain line breaks, which would otherwise make matchEntire fail
	private val STRUCTURE_REGEX = "^(.*?)(\\d*)$".toRegex(RegexOption.DOT_MATCHES_ALL)

	fun defaultName(name: String?, portType: PortType): String {
		if (StringUtils.isNotEmpty(name)) {
			return name!!
		}
		return when (portType) {
			PortType.INPUT -> "I1"
			PortType.OUTPUT -> "O1"
			PortType.INOUT -> "IO1"
		}
	}

	fun createPastedName(name: String, graph: Graph): String =
		createUniqueName(name) { isTaken(it, graph) }

	/**
	 * Returns [name] unless [isTaken], otherwise the next free name with the same stem:
	 * a trailing number is counted up ("A3" -> "A4"), a name without one gets a number appended ("Clk" -> "Clk2").
	 */
	fun createUniqueName(name: String, isTaken: (String) -> Boolean): String {
		if (!isTaken(name)) {
			return name
		}
		val structure = createStructure(name)
		// A name without number counts as the first one, e.g. "I" is followed by "I2"
		var count = if (structure.number < 0) 1 else structure.number
		var newName: String
		do {
			count++
			newName = "${structure.text}$count"
		} while (isTaken(newName))
		return newName
	}

	/**
	 * Shifts the trailing numbers of the pasted [graphPorts] that clash with names in [graph] by a common
	 * offset per stem, so that a pasted group keeps its relative order, e.g. pasting D0, D1 into a [graph]
	 * containing D0..D3 yields D4, D5. Clashes that cannot be resolved this way are left to [createPastedName].
	 */
	fun renumberPasted(graphPorts: List<GraphPort<*>>, graph: Graph) {
		val existingStructures = graph.graphPorts.mapNotNull { it.name }.map { createStructure(it) }

		graphPorts
			.mapNotNull { port -> port.name?.let { port to createStructure(it) } }
			.filter { (_, structure) -> structure.number >= 0 }
			.groupBy { (_, structure) -> structure.text }
			.forEach { (stem, group) ->
				if (group.none { (port, _) -> isTaken(port.name!!, graph) }) {
					return@forEach
				}
				val maxExisting = existingStructures.filter { it.text == stem }.maxOfOrNull { it.number }
					?: return@forEach
				val delta = maxExisting - group.minOf { (_, structure) -> structure.number } + 1
				val newNames = group.map { (port, structure) -> port to "$stem${structure.number + delta}" }
				val distinct = newNames.map { it.second }.toSet().size == newNames.size
				// All ports of the group are renamed, so their original names cannot clash
				if (distinct && newNames.none { (_, name) -> isTaken(name, graph) }) {
					newNames.forEach { (port, name) -> port.name = name }
				}
			}
	}

	/** Splits [name] into its text and its trailing number, which is -1 if there is none. */
	fun createStructure(name: String): GraphPortNameStructure {
		val result = STRUCTURE_REGEX.matchEntire(name)!!
		return result.groupValues[2].toIntOrNull()
			?.let { GraphPortNameStructure(result.groupValues[1], it) }
			?: GraphPortNameStructure(name, -1)
	}

	/** Returns `true` if [name] is used by a [GraphPort] or a parameter of [graph]. */
	private fun isTaken(name: String, graph: Graph): Boolean =
		graph.graphPorts.any { it.name == name } || graph.parameterDefinitions.contains(name)
}

data class GraphPortNameStructure(
	val text: String,
	val number: Int,
)
