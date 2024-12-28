package ch.scorpion.antares.model.analog

import ch.scorpion.antares.model.input.AbstractSwitch
import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.antares.view.analog.engine.AnalogElementMixin
import ch.scorpion.antares.view.analog.engine.AnalogCircuitAnalysis
import ch.scorpion.jabbah.base.LongValueImpl
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.InteractableVertice

/**
 * The electrical [resistance] of [AnalogSwitch] depends on the state of [isOn].
 */
class AnalogSwitch(
	private val analogElement: AnalogElementMixin = AnalogElementMixin()
) : AbstractSwitch<AnalogSwitch>(CALCULATOR),
	AnalogVertice,
	AnalogTwoPortVertice,
	AnalogElement by analogElement
{
	companion object {
		private const val BASE_RESOURCE_KEY = "library.element.AnalogSwitch"

		private val CALCULATOR = Calculator()

		private class Calculator : AbstractSwitch.Companion.AbstractSwitchCalculator<AnalogSwitch>() {
			override fun calculate(vertice: AnalogSwitch, data: GraphActorData, signalHandler: SignalHandler) {
				super.calculate(vertice, data, signalHandler)
				vertice.requestAnalogGraphReanalization(signalHandler)
			}
		}
	}

	private val logic = AnalogSwitchLogic(this, 0, ::isOn)

	override val type: String get() = Translations.getString("${BASE_RESOURCE_KEY}.name")

	override val typeDesc: String? get() = Translations.getOptionalString("${BASE_RESOURCE_KEY}.desc")

	init {
		analogElement.bindAnalogElement(this)
		addPort(AnalogPort())
		addPort(AnalogPort())
		propagationDelay = LongValueImpl.ZERO
	}

	private fun requestAnalogGraphReanalization(signalHandler: SignalHandler) {
		stateChanged(signalHandler, AbstractAnalogVertice.REQUEST_REANALYZE)
	}

	/** ---- [InteractableVertice] interface */

	override var interactivePropagationDelay: Long = propagationDelay.value
		set(value) {
			propagationDelay = LongValueImpl(value)
		}

	/** ---- [AnalogElement] */

	override val voltageSourceCount: Int get() = logic.voltageSourceCount

	override fun stamp(analysis: AnalogCircuitAnalysis) {
		logic.stamp(analysis)
	}

	override fun calculateCurrent() { }
}