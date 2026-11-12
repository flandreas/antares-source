package io.antarescircuit.antares.hdl

import io.antarescircuit.antares.model.testcase.parser.PortNameType
import io.antarescircuit.antares.model.testcase.parser.TestScript
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.dsl.SemanticError
import io.antarescircuit.jabbah.base.parser.TextLocation
import io.antarescircuit.jabbah.graph.model.PortType

/** What a test bench does with a test script column. */
enum class TestBenchRole { DRIVE, CHECK, IGNORE }

/** A test script column resolved to the [port] of the circuit under test. */
class TestBenchColumn(val port: HDLPort, val role: TestBenchRole)

/**
 * Resolves the columns of [testScript] to ports of [circuit]. Explicit markers (">A", "<A") decide the role;
 * otherwise inputs are driven and outputs are checked. Pins on in/out nets use their own pin type like the
 * simulator's test runner, and are ignored if they are bidirectional themselves.
 *
 * @throws SemanticError if a column has no corresponding port
 */
fun resolveTestBenchColumns(testScript: TestScript, circuit: HDLCircuit, renaming: HDLRenaming): List<TestBenchColumn> =
	testScript.portNames.names.map { portName ->
		val saveName = renaming.checkName(portName.name.value!!)
		val port = circuit.getPort(saveName, ignoreCase = !renaming.isCaseSensitive)
			?: throw SemanticError(TextLocation.UNDEFINED, Translations.getString("antares.vhdl.portNameNotFound.error.text", saveName))
		val role = when (portName.type) {
			PortNameType.INPUT -> TestBenchRole.DRIVE
			PortNameType.OUTPUT -> TestBenchRole.CHECK
			PortNameType.DEFAULT -> when (port.direction) {
				// Seen from inside the circuit, inputs define and outputs read their value
				HDLPort.Direction.OUT -> TestBenchRole.DRIVE
				HDLPort.Direction.IN -> TestBenchRole.CHECK
				HDLPort.Direction.INOUT -> when (circuit.circuit.graphPorts.firstOrNull { it.name == portName.name.value }?.portType) {
					PortType.INPUT -> TestBenchRole.DRIVE
					PortType.OUTPUT -> TestBenchRole.CHECK
					else -> TestBenchRole.IGNORE
				}
			}
		}
		TestBenchColumn(port, role)
	}
