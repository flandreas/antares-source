package ch.scorpion.antares.model.testcase.parser

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.net.Probe
import ch.scorpion.antares.model.testcase.Value
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.Node
import ch.scorpion.jabbah.base.dsl.SemanticAnalyser
import ch.scorpion.jabbah.base.dsl.SemanticError
import ch.scorpion.jabbah.base.parser.TextLocation
import ch.scorpion.jabbah.graph.model.GraphPort

class TestcaseAnalyser(
	private val circuit: DigitalGraph
) : SemanticAnalyser {

	override fun analyse(program: Node) {
		val script = program as TestScript
		val portNames = script.portNames.names.map { it.name.value!! }

		ensurePortsExistAndIsUnique(portNames)
		ensureAtLeastOneInput(portNames)
		ensureAtLeastOneOutput(portNames)
		for (child in script.children) {
			when (child) {
				is RunNode -> {
					ensureValueCount(portNames.size, child.children)
					ensureNoDontCareInRunBlock(child)
				}
				is TestVectorNode -> {
					ensureValueCount(portNames.size, listOf(child))
					ensureAtMostOneClock(child)
				}
				else -> {}
			}
		}
	}

	private fun throwSemanticError(key: String, vararg params: Any) {
		throw SemanticError(TextLocation.UNDEFINED, Translations.getString(key, *params))
	}

	private fun ensurePortsExistAndIsUnique(portNames: List<String>) {
		for (name in portNames) {
			val elements = circuit.elements.filter { it is GraphPort<*> && it.name == name || it is Probe && it.name == name }
			if (elements.isEmpty()) {
				throwSemanticError("antares.testcase.error.unknownPort", name)
			} else if (elements.size > 1) {
				throwSemanticError("antares.testcase.error.ambiguousOutputName", name)
			}
		}
	}

	private fun ensureAtLeastOneInput(portNames: List<String>) {
		val elements = circuit.elements.filter {
			it is GraphPort<*> && portNames.contains(it.name) && it.portType.isInput
		}
		if (elements.isEmpty()) {
			throwSemanticError("antares.testcase.error.noInput")
		}
	}

	private fun ensureAtLeastOneOutput(portNames: List<String>) {
		val elements = circuit.elements.filter {
			it is GraphPort<*> && portNames.contains(it.name) && it.portType.isOutput
				|| it is Probe && portNames.contains(it.name)
		}
		if (elements.isEmpty()) {
			throwSemanticError("antares.testcase.error.noOutput")
		}
	}

	private fun ensureValueCount(portNameCount: Int, testVectors: List<TestVectorNode>) {
		testVectors.firstOrNull { it.values.size > portNameCount }?.let {
			throwSemanticError("antares.testcase.error.tooManyValues", it.location.row)
		}
		testVectors.firstOrNull { it.values.size < portNameCount }?.let {
			throwSemanticError("antares.testcase.error.tooFewValues", it.location.row)
		}
	}

	private fun ensureNoDontCareInRunBlock(node: RunNode) {
		node.children.firstOrNull { vectorNode -> vectorNode.values.any { it.value.type == Value.Type.DONT_CARE } }?.let {
			throwSemanticError("antares.testcase.error.dontCareInRunBlock", it.location.row)
		}
	}

	private fun ensureAtMostOneClock(node: TestVectorNode) {
		if (node.values.count { it.value.type == Value.Type.CLOCKED } > 1) {
			throwSemanticError("antares.testcase.error.moreThanOneClock", node.location.row)
		}
	}
}