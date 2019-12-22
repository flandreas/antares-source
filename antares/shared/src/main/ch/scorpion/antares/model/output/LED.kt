package ch.scorpion.antares.model.output

import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/**
 * A light emitting [Vertice] that turns on with [Bit.True].
 */
class LED() : CalculatingVertice("library.element.LED", CALCULATOR) {

	companion object {

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<LED> {
			override fun calculate(vertice: LED, data: GraphActorData, signalHandler: SignalHandler) {
				vertice.isOn = (data.getSignal<DigitalSignal>(1) as Word).bitAt(0) == Bit.True
			}
		}
	}

	var isOn: Boolean = false
		set(value) {
			if (field != value) {
				field = value
				stateChanged()
			}
		}

	init {
		addPort(DigitalPortImpl.createInput())
		propagationDelay = 0
	}

	/** ---- [Actor] */

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		isOn = false
	}
}