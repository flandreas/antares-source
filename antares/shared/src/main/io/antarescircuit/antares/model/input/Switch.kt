package io.antarescircuit.antares.model.input

import io.antarescircuit.antares.model.port.DigitalPortImpl
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.GraphActorData
import io.antarescircuit.jabbah.graph.model.vertice.InteractableVertice
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * Represents an interactive switch that can toggle between two states.
 */
class Switch : AbstractSwitch<Switch>(CALCULATOR) {

	companion object {

		val DEF_PROP_DELAY get() = CurrentSwitchPropagationDelay.value

		private const val BASE_RESOURCE_KEY = "library.element.Toggle"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private val CALCULATOR = Calculator()

		private class Calculator : AbstractSwitch.Companion.AbstractSwitchCalculator<Switch>() {
			override fun calculate(vertice: Switch, data: GraphActorData, signalHandler: SignalHandler) {
				super.calculate(vertice, data, signalHandler)
				val output = vertice.getOutput<DigitalSignal>()
				output.setOutgoingSignalBuffered(DigitalSignalFactory.of(vertice.isOn), signalHandler)
			}
		}
	}

	private var switchedOnAt: Long = 0
	var minOnTime: Long = 0

	init {
		addPort(DigitalPortImpl.createOutput())
		propagationDelay = DEF_PROP_DELAY
		minOnTime = 0
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	/** ---- [AbstractSwitch] */

	override fun on(signalHandler: SignalHandler) {
		switchedOnAt = signalHandler.executionTime
		super.on(signalHandler)
	}

	override fun off(signalHandler: SignalHandler) {
		val passedNs = signalHandler.executionTime - switchedOnAt
		if (passedNs < minOnTime) {
			super.delayedOff(signalHandler, minOnTime - passedNs)
		} else {
			super.off(signalHandler)
		}
	}

	/** ---- [InteractableVertice] interface */

	override var interactivePropagationDelay: Long = propagationDelay.value
		set(value) {
			propagationDelay = LongValueImpl(value)
		}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (minOnTime != 0L) {
			writer.writeLong("minOnTime", minOnTime)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("minOnTime")) {
			minOnTime = reader.readLong("minOnTime")
		}
	}
}