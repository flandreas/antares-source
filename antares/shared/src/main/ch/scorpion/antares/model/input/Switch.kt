package ch.scorpion.antares.model.input

import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.GraphActorDataImpl
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/**
 * Represents an interactive switch that can toggle between two states.
 */
class Switch : CalculatingVertice("library.element.Switch", CALCULATOR) {

    var isOn: Boolean = false
        private set

    init {
        addPort(DigitalPortImpl.createOutput())
        propagationDelay = 1000
    }

    companion object {
        val CALCULATOR = object : VerticeCalculator<Switch> {
            override fun calculate(vertice: Switch, data: GraphActorData, signalHandler: SignalHandler) {
                val output = vertice.getOutput<DigitalSignal>()
                output.setOutgoingSignalBuffered(data.getSignal(1), signalHandler)
                vertice.stateChanged()
            }
        }
    }

    /** ---- [Actor] interface */

    override fun executionStarted(signalHandler: SignalHandler) {
        super.executionStarted(signalHandler)
        setOn(signalHandler, false)
    }

    override fun executionStopped(signalHandler: SignalHandler) {
        super.executionStopped(signalHandler)
        isOn = false
        stateChanged()
    }

    /** ---- [Switch] */

    fun toggle(signalHandler: SignalHandler) {
        setOn(signalHandler, !isOn)
    }

    fun setOn(signalHandler: SignalHandler, on: Boolean) {
        this.isOn = on
        stateChanged()
        requestActingAfter(signalHandler, propagationDelay, GraphActorDataImpl(null, Word.of(isOn)))
    }
}