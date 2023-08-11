package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.PortType

/**
 * Utility for dealing with names of [GraphPort]s.
 */
object GraphPortName {

	private val STRUCTURE_REGEX = "^(\\D+)(\\d+)$".toRegex()

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
		createStructure(name)
			?.let { createStructuredPastedName(name, it, graph) }
			?: createStandardPastedName(name, graph)

	private fun createStandardPastedName(name: String, graph: Graph): String {
		var count = 1
		var newName = name
		while (graph.graphPorts.any { it.name == newName }) {
			count++
			newName = "$name ($count)"
		}
		return newName
	}

	private fun createStructuredPastedName(name: String, structure: GraphPortNameStructure, graph: Graph): String {
		var count = structure.number
		var newName = name
		while (graph.graphPorts.any { it.name == newName }) {
			count++
			newName = "${structure.text}$count"
		}
		return newName
	}

	fun createStructure(name: String): GraphPortNameStructure? {
		val result = STRUCTURE_REGEX.matchEntire(name)
		return if (result != null && result.groupValues.size == 3) {
			GraphPortNameStructure(result.groupValues[1], result.groupValues[2].toInt())
		} else {
			null
		}
	}
}

data class GraphPortNameStructure(
	val text: String,
	val number: Int
)