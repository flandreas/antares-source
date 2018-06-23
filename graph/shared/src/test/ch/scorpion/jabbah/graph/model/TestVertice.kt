package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.port.PortImpl
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/**
 * A [Vertice] implementation to be used in [ch.scorpion.jabbah.graph] integration tests.
 * [TestVertice] uses [Boolean] signals and has a single [InputPort] and a single [OutputPort].
 */
class TestVertice : CalculatingVertice(CALCULATOR) {

    companion object {
        val CALCULATOR = object : VerticeCalculator<TestVertice> {
            override fun calculate(vertice: TestVertice, data: GraphActorData, signalHandler: SignalHandler) {
                vertice.getOutput<Boolean>().setOutgoingSignalBuffered(data.getSignal(1), signalHandler)
            }
        }
    }

    init {
        addPort(PortImpl.createInput(Boolean::class))
        addPort(PortImpl.createOutput(Boolean::class))
    }
}