package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.inout.DigitalCircuitInOut
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.testcase.parser.TestScript

abstract class AbstractTestcaseRunner(
	protected val testName: String,
	protected val testScript: TestScript,
	protected val circuit: DigitalGraph
) {

	protected lateinit var portNames: List<String>
	protected lateinit var currentTestVector: TestVector

	abstract fun run(): TestRunResult

	abstract fun processInputChanged()

	abstract fun dispose()

	protected abstract fun setInput(port: DigitalCircuitInOut, signal: DigitalSignal)

	protected abstract fun readOutput(port: DigitalCircuitInOut): DigitalSignal?

	protected fun determineIsOutput(portNames: List<String>): List<Boolean> =
		portNames.map {
			val port = circuit.getGraphPort<DigitalSignal>(it)
			port is DigitalCircuitInOut && port.portType.isOutput
		}.toList()

	@Suppress("UNUSED_PARAMETER")
	protected fun setInputs(context: Any?) {
		setInputsFiltered { it != Value.Type.CLOCKED }
		setInputsFiltered { it == Value.Type.CLOCKED }
	}

	private fun setInputsFiltered(filter: (Value.Type) -> Boolean) {
		portNames.forEachIndexed { index, portName ->
			val port = circuit.getGraphPort<DigitalSignal>(portName)
			if (port is DigitalCircuitInOut && port.portType.isInput) {
				val value = currentTestVector.getValue(index)
				if (filter(value.type)) {
					val signal = DigitalSignalFactory.of(port.bitWidth, value.value)
					setInput(port, signal)
					processInputChanged()
				}
			}
		}
	}

	@Suppress("UNUSED_PARAMETER")
	protected fun readOutputs(context: Any?) {
		portNames.forEachIndexed { index, portName ->
			val port = circuit.getGraphPort<DigitalSignal>(portName)
			if (port is DigitalCircuitInOut && port.portType.isOutput) {
				val expected = currentTestVector.getValue(index)
				val value = readOutput(port)
				val matchedValue = MatchedValue(expected, value!!)
				currentTestVector.setValue(index, matchedValue)
			}
		}
	}
}