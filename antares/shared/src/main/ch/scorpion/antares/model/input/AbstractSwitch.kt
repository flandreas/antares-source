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

	override var interactivePropagationDelay: Long = Switch.DEF_PROP_DELAY.value

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

	open fun toggle(signalHandler: SignalHandler) {
		if (isOn) {
			off(signalHandler)
		} else {
			on(signalHandler)
		}
	}

	open fun on(signalHandler: SignalHandler) {
		if (enabled && !isOn) {
			requestSetSignal(true, signalHandler)
		}
	}

	open fun off(signalHandler: SignalHandler) {
		if (enabled && isOn) {
			requestSetSignal(false, signalHandler)
		}
	}

	protected fun delayedOff(signalHandler: SignalHandler, delayedBy: Long) {
		if (enabled && isOn) {
			requestSetSignalAfter(false, signalHandler, delayedBy)
		}
	}
}