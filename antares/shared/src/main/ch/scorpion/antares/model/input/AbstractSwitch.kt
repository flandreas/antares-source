package ch.scorpion.antares.model.input

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.AbstractInteractableVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.graph.view.GraphView

abstract class AbstractSwitch<T : AbstractSwitch<T>>(
	calculator: VerticeCalculator<T>
) : AbstractInteractableVertice(calculator) {

	companion object {
		open class AbstractSwitchCalculator<T : AbstractSwitch<T>> : VerticeCalculator<T> {
			override fun calculate(vertice: T, data: GraphActorData, signalHandler: SignalHandler) {
				if (vertice.delayedOff) {
					vertice.delayedOff = false
					vertice.setState(signalHandler, false, data.graphView)
				} else {
					vertice.setInteractionEnabled(true, signalHandler)
				}
			}
		}
	}

	var isOn: Boolean = false
		protected set

	/**
	 * Used to support view implementations with a non-toggle behaviour, i.e. switches that change to "on" when
	 * the user clicks the mouse button, and to "off" when he releases the mouse button. Since the change to "off"
	 * would be missed because the [Switch] is not enabled at that time, it is remembered in this flag and applied
	 * when the [Switch] has been scheduled the next time for calculation.
	 */
	protected var delayedOff: Boolean = false

	/** ---- [Actor] interface */

	override fun executionInitialize(signalHandler: SignalHandler) {
		super.executionInitialize(signalHandler)
		isOn = false
	}

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		setState(signalHandler, false, graphView = null)
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		isOn = false
		setInteractionEnabled(true, signalHandler)
	}

	fun toggle(signalHandler: SignalHandler, graphView: GraphView?) {
		if (isOn) {
			off(signalHandler, graphView)
		} else {
			on(signalHandler, graphView)
		}
	}

	fun on(signalHandler: SignalHandler, graphView: GraphView?) {
		if (enabled && !isOn) {
			setState(signalHandler, true, graphView)
		}
	}

	fun off(signalHandler: SignalHandler, graphView: GraphView?) {
		if (isOn) {
			if (enabled) {
				setState(signalHandler, false, graphView)
			} else {
				delayedOff = true
			}
		}
	}

	protected open fun setState(signalHandler: SignalHandler, on: Boolean, graphView: GraphView?) {
		isOn = on
		setInteractionEnabled(false, signalHandler)
		requestActingAfter(signalHandler, propagationDelay, createActorData(null, graphView = graphView))
	}
}