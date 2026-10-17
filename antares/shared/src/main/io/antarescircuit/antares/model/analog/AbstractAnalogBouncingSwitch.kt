package io.antarescircuit.antares.model.analog

import io.antarescircuit.antares.model.input.AbstractSwitch
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter
import kotlin.random.Random


/** An analog switch that can randomly bounce before settling in its requested state. */
abstract class AbstractAnalogBouncingSwitch<T : AbstractAnalogBouncingSwitch<T>>(
	calculator: VerticeCalculator<T>
) : AbstractSwitch<T>(calculator) {

	companion object {
		private const val DEF_BOUNCE_DURATION = 100L
	}

	/** Set to `true` while the switch is bouncing.*/
	private var bouncing = false

	/** Controls how many individual state transitions happen during bouncing. */
	private var bounceTransitionsRemaining = 0

	/**
	 * The state (on/off) in which the switch settles after bouncing. This is also the state requested by the user
	 * when clicking on the switch.
	 */
	private var bounceTarget = false

	/** Determines whether the switch bounces before settling in its requested state.*/
	var bounce: Boolean = false

	/** The duration of the bounce period in nanoseconds. */
	var bounceDuration: Long = DEF_BOUNCE_DURATION

	override val isOnForView: Boolean
		get() = if (bouncing) bounceTarget else isOn

	/** ---- [AbstractSwitch] */

	override fun on(signalHandler: SignalHandler) {
		if (bounce) {
			startBounce(true, signalHandler)
		} else {
			super.on(signalHandler)
		}
	}

	override fun off(signalHandler: SignalHandler) {
		if (bounce) {
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
			requestSetSignalAfter(target, signalHandler, effectivePropagationDelay)
		}
	}

	protected fun continueBounce(signalHandler: SignalHandler) {
		if (!bouncing) {
			return
		}
		if (bounceTransitionsRemaining > 0) {
			bounceTransitionsRemaining--
			val nextState = if (bounceTransitionsRemaining == 0) bounceTarget else !isOn
			requestSetSignalAfter(nextState, signalHandler, randomBounceDelay())
		} else {
			bouncing = false
		}
	}

	private fun randomBounceDelay(): Long {
		val nominalDuration = bounceDuration.coerceAtLeast(1L)
		val minimumDuration = maxOf(1L, nominalDuration / 2)
		val maximumDuration = nominalDuration + nominalDuration / 2
		return if (minimumDuration == maximumDuration) {
			nominalDuration
		} else {
			Random.nextLong(minimumDuration, maximumDuration + 1)
		}
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (bounce) {
			writer.writeBoolean("bounce", bounce)
		}
		if (bounceDuration != DEF_BOUNCE_DURATION) {
			writer.writeLong("bounceDuration", bounceDuration)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("bounce")) {
			bounce = reader.readBoolean("bounce")
		}
		if (reader.hasAttribute("bounceDuration")) {
			bounceDuration = reader.readLong("bounceDuration")
		}
	}
}
