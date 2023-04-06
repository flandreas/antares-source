package ch.scorpion.antares.model.input

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.AbstractInteractableVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

abstract class AbstractSwitch<T : AbstractSwitch<T>>(
	calculator: VerticeCalculator<T>
) : AbstractInteractableVertice(calculator) {

	companion object {
		open class AbstractSwitchCalculator<T : AbstractSwitch<T>> : VerticeCalculator<T> {
			override fun calculate(vertice: T, data: GraphActorData, signalHandler: SignalHandler) {
				if (vertice.isRememberRelease) {
					vertice.requestSetState(signalHandler, false)
					vertice.isRememberRelease = false
				} else {
					vertice.completeSetState(signalHandler)
				}
			}
		}
	}

	var isOn: Boolean = false
		protected set(value) {
			field = value
			stateChanged()
			isRememberRelease = false
		}

	/** Captures a state change to delay it until propagation delay is over.*/
	private var delayedState: Boolean = false

	/**
	 * Used to support view implementations with a non-toggle behaviour, i.e. switches that change to "on" when
	 * the user clicks the mouse button, and to "off" when he releases the mouse button. Since the change to "off"
	 * would be missed because the [Switch] is not enabled at that time, it is remembered in this flag and applied
	 * when the [Switch] has been scheduled the next time for calculation.
	 */
	var isRememberRelease: Boolean = false
		private set

	/** ---- [Actor] interface */

	override fun executionInitialize(signalHandler: SignalHandler) {
		super.executionInitialize(signalHandler)
		isOn = false
	}

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		requestSetState(signalHandler, false)
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		isOn = false
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
			requestSetState(signalHandler, true)
		}
	}

	fun off(signalHandler: SignalHandler) {
		if (enabled && isOn) {
			requestSetState(signalHandler, false)
		}
	}

	fun rememberRelease(signalHandler: SignalHandler) {
		isRememberRelease = true
		requestSetState(signalHandler, false)
	}

	protected open fun requestSetState(signalHandler: SignalHandler, state: Boolean) {
		delayedState = state
		setInteractionEnabled(false, signalHandler)
		requestActingAfter(signalHandler, propagationDelay, createActorData(null))
	}

	private fun completeSetState(signalHandler: SignalHandler) {
		isOn = delayedState
		setInteractionEnabled(true, signalHandler)
	}
}