package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.net.CombinedNet
import ch.scorpion.jabbah.graph.model.net.NetCombiner
import ch.scorpion.jabbah.graph.model.net.SignalConverter
import ch.scorpion.jabbah.graph.model.net.SignalPropagationChain
import ch.scorpion.jabbah.graph.model.port.PortImpl
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/**
 * A test [Vertice] with a pluggable [SignalConverter] that defines how an output signal
 * is calculated from an input signal.
 */
class TestVerticeString(
	outputIsInOut: Boolean = false,
	name: String? = null,
	private val converter: SignalConverter<String>? = null
): CalculatingVertice(CALCULATOR, name), NetCombiner<String> {

	companion object {
		const val TYPE = "Test"
		const val TYPE_DESC = "TestDescription"

		val CALCULATOR = object : VerticeCalculator<TestVerticeString> {
			override fun calculate(vertice: TestVerticeString, data: GraphActorData, signalHandler: SignalHandler) {
				val signal = data.getSignal<String>(1)
				vertice.getOutput<String>().setOutgoingSignalBuffered(vertice.converter?.convert(signal) ?: signal, signalHandler)
			}
		}
	}

	override val type: String get() = TestVertice.TYPE
	override val typeDesc: String get() = TestVertice.TYPE_DESC

	init {
		addPort(PortImpl.createInput(String::class))
		addPort(if (outputIsInOut) PortImpl.createInOut(String::class) else PortImpl.createOutput(String::class))
	}

	override fun getSignalPropagationChains(inputPort: InputPort<String>): Collection<SignalPropagationChain<String>> {
		return if (inputPort === getInput<String>(1)) {
			val outputPort = getOutput<String>(2)
			val chains = CombinedNet.fromOutputPort(outputPort).chains
			chains.forEach { it.extendHead(converter, inputPort, outputPort) }
			chains
		} else {
			emptyList()
		}
	}
}