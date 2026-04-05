package io.antarescircuit.antares.model.analog

import io.antarescircuit.antares.model.input.AbstractSwitch
import io.antarescircuit.antares.view.analog.engine.AnalogCircuitAnalysis
import io.antarescircuit.antares.view.analog.engine.AnalogElement
import io.antarescircuit.antares.view.analog.engine.AnalogElementMixin
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.GraphActorData
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.model.vertice.InteractableVertice

/**
 * An interactive switch with three bidirectional [AnalogPort]s.
 *
 * [Port] 1 is the single [Port] at one side, and [Ports][Port] 2 and 3 are the two on the other side.
 * Interprets property [AbstractSwitch.isOn] as `true` if [Port] 2 is active, and as `false`
 * if [Port] 3 is active.
 */
class AnalogDoubleThrowSwitch(
    private val analogElement: AnalogElementMixin = AnalogElementMixin(postCount = 3)
) : AbstractSwitch<AnalogDoubleThrowSwitch>(CALCULATOR),
    AnalogVertice,
    AnalogElement by analogElement
{
    companion object {
        private const val BASE_RESOURCE_KEY = "library.element.AnalogDoubleThrowSwitch"

        private val CALCULATOR = Calculator()

        private class Calculator : AbstractSwitch.Companion.AbstractSwitchCalculator<AnalogDoubleThrowSwitch>() {
            override fun calculate(vertice: AnalogDoubleThrowSwitch, data: GraphActorData, signalHandler: SignalHandler) {
                super.calculate(vertice, data, signalHandler)
                vertice.requestAnalogGraphReanalization(signalHandler)
            }
        }
    }

    private val logic = AnalogDoubleThrowSwitchLogic(this, 0, ::isOn)

    override val type: String get() = Translations.getString("${BASE_RESOURCE_KEY}.name")

    override val typeDesc: String? get() = Translations.getOptionalString("${BASE_RESOURCE_KEY}.desc")

    init {
        analogElement.bindAnalogElement(this)
        addPort(AnalogPort())
        addPort(AnalogPort())
        addPort(AnalogPort())
        propagationDelay = LongValueImpl.ZERO
    }

    /** ---- [InteractableVertice] interface */

    override var interactivePropagationDelay: Long = propagationDelay.value
        set(value) {
            propagationDelay = LongValueImpl(value)
        }

    /** ---- [AnalogDoubleThrowSwitch] */

    private fun requestAnalogGraphReanalization(signalHandler: SignalHandler) {
        stateChanged(signalHandler, AbstractAnalogVertice.REQUEST_REANALYZE)
    }

    /** ---- [AnalogElement] */

    override val voltageSourceCount: Int get() = logic.voltageSourceCount

    override fun stamp(analysis: AnalogCircuitAnalysis) {
        logic.stamp(analysis)
    }

    override fun calculateCurrent() { }
}