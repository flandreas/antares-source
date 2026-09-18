package io.antarescircuit.antares.model.input

import io.antarescircuit.antares.model.port.DigitalPortImpl
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.GraphActorData
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter
import kotlin.random.Random

/**
 * Represents an interactive switch that can toggle between two states.
 */
class Switch : AbstractSwitch<Switch>(CALCULATOR) {

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.Toggle"
		private const val DEF_BOUNCE_DURATION = 100L
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private val CALCULATOR = Calculator()

		private class Calculator : AbstractSwitch.Companion.AbstractSwitchCalculator<Switch>() {
			override fun calculate(vertice: Switch, data: GraphActorData, signalHandler: SignalHandler) {
				super.calculate(vertice, data, signalHandler)
				val output = vertice.getOutput<DigitalSignal>()
				output.setOutgoingSignalBuffered(DigitalSignalFactory.of(vertice.isOn), signalHandler)
				vertice.continueBounce(signalHandler)
			}
		}
	}

	private var switchedOnAt: Long = 0
	private var bouncing = false
	private var bounceTransitionsRemaining = 0
	private var bounceDelay = 0L
	private var bounceTarget = false
	var minOnTime: Long = 0
	var bounce: Boolean = false
	var bounceDuration: Long = DEF_BOUNCE_DURATION

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
		if (bounce) {
			startBounce(true, signalHandler)
		} else {
			super.on(signalHandler)
		}
	}

	override fun off(signalHandler: SignalHandler) {
		val passedNs = signalHandler.executionTime - switchedOnAt
		if (passedNs < minOnTime) {
			super.delayedOff(signalHandler, minOnTime - passedNs)
		} else if (bounce) {
			startBounce(false, signalHandler)
		} else {
			super.off(signalHandler)
		}
	}

	private fun startBounce(target: Boolean, signalHandler: SignalHandler) {
		if (enabled && isOn != target) {
			bouncing = true
			bounceTarget = target
			// Each bounce consists of a transition away from, and back to, the target state.
			bounceTransitionsRemaining = Random.nextInt(1, 6) * 2
			// Use a fixed duration for realistic, equally wide bounces. For very short
			// propagation delays, shorten it enough to finish before the switch settles.
			val availableDuration = propagationDelay.value - 1
			bounceDelay = if (availableDuration > 0) {
				minOf(bounceDuration.coerceAtLeast(0L), availableDuration / bounceTransitionsRemaining)
			} else {
				0
			}
			requestSetSignal(target, signalHandler)
		}
	}

	private fun continueBounce(signalHandler: SignalHandler) {
		if (!bouncing) {
			return
		}
		if (bounceTransitionsRemaining > 0) {
			bounceTransitionsRemaining--
			requestSetSignalAfter(!isOn, signalHandler, bounceDelay)
		} else {
			bouncing = false
		}
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (minOnTime != 0L) {
			writer.writeLong("minOnTime", minOnTime)
		}
		if (bounce) {
			writer.writeBoolean("bounce", bounce)
		}
		if (bounceDuration != DEF_BOUNCE_DURATION) {
			writer.writeLong("bounceDuration", bounceDuration)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("minOnTime")) {
			minOnTime = reader.readLong("minOnTime")
		}
		if (reader.hasAttribute("bounce")) {
			bounce = reader.readBoolean("bounce")
		}
		if (reader.hasAttribute("bounceDuration")) {
			bounceDuration = reader.readLong("bounceDuration")
		}
	}
}
