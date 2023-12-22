package ch.scorpion.antares.model.input

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.InteractableVertice
import ch.scorpion.jabbah.graph.model.vertice.AbstractInteractableVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.graph.view.GraphView

abstract class AbstractSwitch<T : AbstractSwitch<T>>(
	calculator: VerticeCalculator<T>
) : AbstractInteractableVertice<Boolean>(calculator) {

	companion object {
		open class AbstractSwitchCalculator<T : AbstractSwitch<T>> : VerticeCalculator<T> {
			override fun calculate(vertice: T, data: GraphActorData, signalHandler: SignalHandler) {
				vertice.calculate(signalHandler)
			}
		}
	}

	val isOn: Boolean get() = signal ?: false

	/** ---- [InteractableVertice] interface */

	override val interactivePropagationDelay: Long get() = Switch.DEF_PROP_DELAY

	/** ---- [Actor] interface */

	override fun executionInitialize(signalHandler: SignalHandler) {
		super.executionInitialize(signalHandler)
		setSignal(false, signalHandler)
	}

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		requestSetSignal(false, signalHandler)
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		setSignal(false, signalHandler)
		setInteractionEnabled(true, signalHandler)
	}

	/** ---- [AbstractSwitch] */

	fun toggle(signalHandler: SignalHandler) {
		if (isOn) {
			off(signalHandler)
		} else {
			on(signalHandler)
		}
	}

	fun on(signalHandler: SignalHandler) {
		if (enabled && !isOn) {
			requestSetSignal(true, signalHandler)
		}
	}

	fun off(signalHandler: SignalHandler) {
		if (enabled && isOn) {
			requestSetSignal(false, signalHandler)
		}
	}
}