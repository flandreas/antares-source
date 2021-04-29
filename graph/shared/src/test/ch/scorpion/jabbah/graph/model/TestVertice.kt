package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.net.*
import ch.scorpion.jabbah.graph.model.port.PortImpl
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/**
 * A [Vertice] implementation to be used in [ch.scorpion.jabbah.graph] integration tests.
 * [TestVertice] uses [Boolean] signals and has a single [InputPort] and a single [OutputPort].
 *
 * @param inOut `true` if the created output [Port] has [PortType.INOUT], `false` if it has [PortType.OUTPUT]
 */
class TestVertice(
	private val inOut: Boolean = false,
	name: String? = null,
	canBeUndefined: Boolean = false
) : CalculatingVertice(CALCULATOR, name), NetCombiner {

    companion object {
	    const val TYPE = "Test"
	    const val TYPE_DESC = "TestDescription"

        val CALCULATOR = object : VerticeCalculator<TestVertice> {
            override fun calculate(vertice: TestVertice, data: GraphActorData, signalHandler: SignalHandler) {
                vertice.getOutput<Boolean>().setOutgoingSignalBuffered(data.getSignal(1), signalHandler)
            }
        }
    }

	override val type: String get() = TYPE
	override val typeDesc: String get() = TYPE_DESC

    init {
        addPort(PortImpl.createInput(Boolean::class))
        addPort(if (inOut) PortImpl.createInOut(Boolean::class, canBeUndefined = true) else PortImpl.createOutput(Boolean::class, canBeUndefined = canBeUndefined))
    }

	override fun <T : Any> createCombinedNetsFor(outputPort: OutputPort<T>, inputPort: InputPort<T>, signalHandler: SignalHandler): Collection<CombinedNet<T>> {
		return if (inputPort === getInput<Boolean>(1)) {
			CombinedNet.createFor(getOutput(2), signalHandler)
		} else {
			emptyList()
		}
	}

	override fun requiresCombinedNets(signalHandler: SignalHandler): Boolean = true
}