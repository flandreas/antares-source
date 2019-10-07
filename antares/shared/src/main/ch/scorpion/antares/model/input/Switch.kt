package ch.scorpion.antares.model.input

import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.GraphActorDataImpl
import ch.scorpion.jabbah.graph.model.vertice.InteractableVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/**
 * Represents an interactive switch that can toggle between two states.
 */
class Switch : InteractableVertice("library.element.Switch", CALCULATOR) {

    var isOn: Boolean = false
        private set

	private var delayedOff: Boolean = false

    init {
        addPort(DigitalPortImpl.createOutput())
        propagationDelay = 1000
    }

    companion object {
        val CALCULATOR = object : VerticeCalculator<Switch> {
            override fun calculate(vertice: Switch, data: GraphActorData, signalHandler: SignalHandler) {
                val output = vertice.getOutput<DigitalSignal>()
                output.setOutgoingSignalBuffered(data.getSignal(1), signalHandler)

	            if (vertice.delayedOff) {
		            vertice.delayedOff = false
		            vertice.setState(signalHandler, false)
	            } else {
		            vertice.enabled = true
	            }
            }
        }
    }

    /** ---- [Actor] interface */

    override fun executionStarted(signalHandler: SignalHandler) {
        super.executionStarted(signalHandler)
	    setState(signalHandler, false)
    }

    override fun executionStopped(signalHandler: SignalHandler) {
        super.executionStopped(signalHandler)
        isOn = false
	    enabled = true
    }

    /** ---- [Switch] */

    fun toggle(signalHandler: SignalHandler) {
	    if (isOn) {
		    off(signalHandler)
	    } else {
		    on(signalHandler)
	    }
    }

    fun on(signalHandler: SignalHandler) {
	    if (enabled && !isOn) {
		    setState(signalHandler, true)
	    }
    }

	fun off(signalHandler: SignalHandler) {
		if (isOn) {
			if (enabled) {
				setState(signalHandler, false)
			} else {
				delayedOff = true
			}
		}
	}

	private fun setState(signalHandler: SignalHandler, on: Boolean) {
		isOn = on
		enabled = false
		requestActingAfter(signalHandler, propagationDelay, GraphActorDataImpl(null, Word.of(isOn)))
	}
}