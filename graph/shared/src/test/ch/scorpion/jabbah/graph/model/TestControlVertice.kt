package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.port.PortImpl
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/**
 * A [Vertice] implementation to be used in [ch.scorpion.jabbah.graph] integration tests.
 * [TestControlVertice] uses [Boolean] signals and has a single [InputPort].
 */
class TestControlVertice : CalculatingVertice(CALCULATOR) {

	companion object {
		private val CALCULATOR = object : VerticeCalculator<TestControlVertice> {
			override fun calculate(vertice: TestControlVertice, data: GraphActorData, signalHandler: SignalHandler) {
				val signal = data.getSignal(1) ?: false
				if (signal != vertice.signal) {
					vertice.signal = signal
					vertice.stateChanged(signalHandler)
				}
			}
		}
	}

	var signal: Boolean = false
		private set

	init {
		propagationDelay = 0
		addPort(PortImpl.createInput(Boolean::class))
	}

	override val type: String get() = "Test"
	override val typeDesc: String? get() = null
}