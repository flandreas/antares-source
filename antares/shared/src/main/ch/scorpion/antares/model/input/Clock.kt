package ch.scorpion.antares.model.input

import ch.scorpion.antares.model.input.PeriodOrFrequencyUnit.Nanosecond
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.LongValueImpl
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A digital component that produces a periodically changing [DigitalSignal].
 */
class Clock(name: String? = null) : CalculatingVertice(CALCULATOR, name) {

	companion object {

		const val BASE_RESOURCE_KEY = "library.element.Clock"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<Clock> {
			override fun calculate(vertice: Clock, data: GraphActorData, signalHandler: SignalHandler) {
				if (vertice.isEnabled) {
					vertice.setOn(signalHandler, !vertice.isOn)
				}
			}
		}
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	/** The real system time (ms) when execution has been started.*/
	var realStartTime: Long = 0
		private set

	/** The number of cycles since execution start. Can be used to calculate effective frequency.*/
	var cycleCount: Long = 0
		private set

	var isOn: Boolean = false
		private set

	var isEnabled: Boolean = true
		set(value) {
			if (field != value) {
				field = value
				stateChanged()
			}
		}

	var periodOrFrequency: PeriodOrFrequency = PeriodOrFrequency(1_000_000_000, Nanosecond)
		set(value) {
			// Set propagationDelay even if periodOrFrequency hasn't changed to restore
			// propagationDelay that might have changed during simulation
			propagationDelay = LongValueImpl(value.asNanoseconds.value)
			if (field != value) {
				field = value
				stateChanged()
			}
		}

	/** The percentage of [periodOrFrequency] during which the output is OFF.*/
	var offPercentage: Double = 50.0
		set(value) {
			if (field != value) {
				require(value in 0.0..100.0) { "Percentage must be between 0 and 100" }
				field = value
				stateChanged()
			}
		}

	/** Used for restoring [periodOrFrequency] after the simulation has ended. */
	private lateinit var periodOrFrequencyBuffer: PeriodOrFrequency

	init {
		propagationDelay = LongValueImpl(periodOrFrequency.asNanoseconds.value)
		addPort(DigitalPortImpl.createOutput())
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (!isEnabled) {
			writer.writeBoolean("enabled", false)
		}
		if (periodOrFrequency.unit != Nanosecond) {
			writer.writeString("unit", periodOrFrequency.unit.customName)
		}
		writer.writeDouble("offPercentage", offPercentage)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("enabled")) {
			isEnabled = reader.readBoolean("enabled")
		}
		periodOrFrequency = if (reader.hasAttribute("unit")) {
			PeriodOrFrequency.fromNanoseconds(
				propagationDelay.value,
				PeriodOrFrequencyUnit.withName(reader.readString("unit")))
		} else {
			PeriodOrFrequency.fromNanoseconds(
				propagationDelay.value,
				Nanosecond)
		}
		if (reader.hasAttribute("offPercentage")) {
			offPercentage = reader.readDouble("offPercentage")
		}
	}

	/** ---- [Actor] interface */

	override fun executionInitialize(signalHandler: SignalHandler) {
		super.executionInitialize(signalHandler)
		realStartTime = System.currentTimeMillis()
		cycleCount = 0
		periodOrFrequencyBuffer = periodOrFrequency.copy()
		isOn = false
		getOutput<DigitalSignal>().setOutgoingSignalBuffered(DigitalSignalFactory.of(isOn), signalHandler)
	}

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		if (offPercentage < 100) {
			requestActingAfter(signalHandler, offTime, createActorData(null))
		}
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		realStartTime = 0
		cycleCount = 0
		super.executionStopped(signalHandler)
		periodOrFrequency = periodOrFrequencyBuffer
	}

	/** ---- [Clock] */

	private val offTime: Long get() = (propagationDelay.value / 100.0 * offPercentage).toLong()

	private val onTime: Long get() = propagationDelay.value / 100 * (100.0 - offPercentage).toLong()

	fun toggle(signalHandler: SignalHandler) {
		if (!isEnabled) {
			setOn(signalHandler, !isOn, true)
		}
	}

	private fun setOn(signalHandler: SignalHandler, on: Boolean, isToggled: Boolean = false) {
		if (isOn != on) {
			cycleCount++
			isOn = on
			getOutput<DigitalSignal>().setOutgoingSignalBuffered(DigitalSignalFactory.of(isOn), signalHandler)
			stateChanged(signalHandler)
			if (isEnabled || isToggled) {
				if (isOn && offPercentage > 0.0 || !isOn && offPercentage < 100.0) {
					val delay = if (isToggled) {1
					} else {
						if (isOn) onTime else offTime
					}
					requestActingAfter(signalHandler, delay, createActorData(null))
				}
			}
		}
	}
}