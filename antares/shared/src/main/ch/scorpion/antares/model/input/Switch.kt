package ch.scorpion.antares.model.input

import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData

/**
 * Represents an interactive switch that can toggle between two states.
 */
class Switch : AbstractSwitch<Switch>(CALCULATOR) {

	companion object {

		const val PROP_DEFAULT_DELAY = "ch.scorpion.antares.model.input.Switch.defaultPropDelay"
		val DEF_PROP_DELAY get() = BaseModule.properties.getInt(PROP_DEFAULT_DELAY).toLong()
		private const val BASE_RESOURCE_KEY = "library.element.Toggle"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private val CALCULATOR = Calculator()

		private class Calculator : AbstractSwitch.Companion.AbstractSwitchCalculator<Switch>() {
			override fun calculate(vertice: Switch, data: GraphActorData, signalHandler: SignalHandler) {
				val output = vertice.getOutput<DigitalSignal>()
				output.setOutgoingSignalBuffered(DigitalSignalFactory.of(vertice.isOn), signalHandler)

				super.calculate(vertice, data, signalHandler)
			}
		}
	}

	init {
		addPort(DigitalPortImpl.createOutput())
		propagationDelay = DEF_PROP_DELAY
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

}