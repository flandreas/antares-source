package io.antarescircuit.jabbah.graph.model

import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.port.PortImpl
import io.antarescircuit.jabbah.graph.model.vertice.CalculatingVertice
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator

/**
 * A [Vertice] implementation to be used in [io.antarescircuit.jabbah.graph] integration tests.
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
		propagationDelay = LongValueImpl.Companion.ZERO
		addPort(PortImpl.Companion.createInput())
	}

	override val type: String get() = "Test"
	override val typeDesc: String? get() = null
}