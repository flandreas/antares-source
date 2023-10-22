package ch.scorpion.antares.model.testcase.parser

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.Node
import ch.scorpion.jabbah.base.dsl.SemanticAnalyser
import ch.scorpion.jabbah.base.dsl.SemanticError
import ch.scorpion.jabbah.base.parser.TextLocation

class TestcaseAnalyser(
	private val circuit: DigitalGraph
) : SemanticAnalyser {

	override fun analyse(program: Node) {
		val script = program as TestScript
		val portNames = script.portNames.names.map { it.value!! }

		ensurePortsExist(portNames)
		ensureAtLeastOneInput(portNames)
		ensureAtLeastOneOutput(portNames)
		ensureValueCount(portNames.size, script.testVectors.children)
	}

	private fun throwSemanticError(key: String, vararg params: Any) {
		throw SemanticError(TextLocation.UNDEFINED, Translations.getString(key, *params))
	}

	private fun ensurePortsExist(portNames: List<String>) {
		portNames.firstOrNull { circuit.getGraphPort<DigitalSignal>(it) == null }?.let {
			throwSemanticError("antares.testcase.error.unknownPort", it)
		}
	}

	private fun ensureAtLeastOneInput(portNames: List<String>) {
		if (portNames.map { circuit.getGraphPort<DigitalSignal>(it)!! }.none { it.portType.isInput }) {
			throwSemanticError("antares.testcase.error.noInput")
		}
	}

	private fun ensureAtLeastOneOutput(portNames: List<String>) {
		if (portNames.map { circuit.getGraphPort<DigitalSignal>(it)!! }.none { it.portType.isOutput }) {
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
}