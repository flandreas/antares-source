package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.inout.DigitalCircuitInOut
import ch.scorpion.antares.model.net.Probe
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.testcase.parser.PortName
import ch.scorpion.antares.model.testcase.parser.PortNameType
import ch.scorpion.antares.model.testcase.parser.TestScript
import ch.scorpion.jabbah.graph.model.PortType
import kotlin.math.max

abstract class AbstractTestcaseRunner(
	protected val testName: String,
	protected val testScript: TestScript,
	protected val circuit: DigitalGraph
) {

	protected lateinit var portNames: List<PortName>
	protected lateinit var currentTestVector: TestVector

	abstract fun run(): TestRunResult

	abstract fun processInputChanged(context: Any?): Long

	abstract fun dispose()

	protected abstract fun setInput(input: DigitalCircuitInOut, signal: DigitalSignal)

	protected abstract fun readOutput(output: DigitalCircuitInOut): DigitalSignal?

	protected abstract fun readOutput(output: Probe): DigitalSignal?

	protected fun determineIsOutput(): List<Boolean> = portNames.map { isDigitalCircuitOutput(it) || isProbeOutput(it) }.toList()

	private fun isDigitalCircuitInput(portName: PortName): Boolean {
		val port = circuit.getGraphPort<DigitalSignal>(portName.name.value!!)
		return portName.type == PortNameType.INPUT || port is DigitalCircuitInOut && port.portType == PortType.INPUT
	}

	private fun isDigitalCircuitOutput(portName: PortName): Boolean {
		val port = circuit.getGraphPort<DigitalSignal>(portName.name.value!!)
		return portName.type == PortNameType.OUTPUT || port is DigitalCircuitInOut && port.portType == PortType.OUTPUT
	}

	private fun isProbeOutput(portName: PortName): Boolean {
		return circuit.elements.any { it is Probe && it.name == portName.name.value }
	}

	@Suppress("UNUSED_PARAMETER")
	protected fun setInputs(context: Any?): Long =
		max(
			setInputsFiltered(context) { it != Value.Type.CLOCKED },
			setInputsFiltered(context) { it == Value.Type.CLOCKED }
		)

	private fun setInputsFiltered(context: Any?, filter: (Value.Type) -> Boolean): Long {
		var inputSet = false
		portNames.forEachIndexed { index, portName ->
			val port = circuit.getGraphPort<DigitalSignal>(portName.name.value!!)
			if (isDigitalCircuitInput(portName)) {
				val value = currentTestVector.getValue(index)
				if (filter(value.type)) {
					val signal = value.value.ofWidth((port as DigitalCircuitInOut).bitWidth)
					setInput(port, signal)
					inputSet = true
				}
			}
		}
		return if (inputSet) {
			processInputChanged(context)
		} else {
			0
		}
	}

	@Suppress("UNUSED_PARAMETER")
	protected fun readOutputs(context: Any?) {
		portNames.forEachIndexed { index, portName ->
			var outputValue: DigitalSignal? = null
			var bitWidth: BitWidth? = null

			if (isDigitalCircuitOutput(portName)) {
				val port = circuit.getGraphPort<DigitalSignal>(portName.name.value!!)
				outputValue = readOutput(port as DigitalCircuitInOut)
				bitWidth = port.bitWidth
			} else if (isProbeOutput(portName)) {
				val probe = circuit.elements.first { it is Probe && it.name == portName.name.value }
				outputValue = readOutput(probe as Probe)
				bitWidth = probe.bitWidth
			} else {
				null
			}

			if (outputValue != null && bitWidth != null) {
				val expected = currentTestVector.getValue(index)

				val matchedValue = MatchedValue(
					expected.withValue(expected.value.ofWidth(bitWidth)),
					outputValue.ofWidth(bitWidth)
				)
				currentTestVector.setValue(index, matchedValue)
			}
		}
	}
}