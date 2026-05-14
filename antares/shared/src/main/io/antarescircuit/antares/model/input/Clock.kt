package io.antarescircuit.antares.model.input

import io.antarescircuit.antares.model.port.DigitalPortImpl
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.base.LongValue
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.actor.Actor
import io.antarescircuit.jabbah.graph.model.GraphActorData
import io.antarescircuit.jabbah.graph.model.vertice.AbstractVertice
import io.antarescircuit.jabbah.graph.model.vertice.CalculatingVertice
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * A digital component that produces a periodically changing [DigitalSignal].
 */
class Clock(name: String? = null) : CalculatingVertice(CALCULATOR, name ?: DEF_NAME) {

	companion object {

		const val BASE_RESOURCE_KEY = "library.element.Clock"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")
		private const val DEF_NAME = "CLK"

		private val DEF_PERIOD_OR_FREQUENCY = MagnitudeValue(1, Magnitude.One, SIUnit.Second)

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

	var cycleCountStartTime: Long = 0
		private set

	/** The system speed for which [cycleCount] is valid. [cycleCount] is reset if the speed changes.*/
	private var cycleCountSpeed: Int = 0

	var isOn: Boolean = false
		private set

	var isEnabled: Boolean = true
		set(value) {
			if (field != value) {
				field = value
				stateChanged()
			}
		}

	var periodOrFrequency: MagnitudeValue = DEF_PERIOD_OR_FREQUENCY
		set(value) {
			// Set propagationDelay even if periodOrFrequency hasn't changed to restore
			// propagationDelay that might have changed during simulation
			propagationDelay = calculatePropagationDelay(value)
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
	private lateinit var periodOrFrequencyBuffer: MagnitudeValue

	override var propagationDelay: LongValue
		get() = super.propagationDelay
		set(value) {
			super.propagationDelay = value
			resetCycleCount()
		}

	init {
		propagationDelay = calculatePropagationDelay(periodOrFrequency)
		addPort(DigitalPortImpl.createOutput(super.name))
	}

	private fun calculatePropagationDelay(periodOrFrequency: MagnitudeValue): LongValue {
		return when (periodOrFrequency.unit) {
            SIUnit.Second -> LongValueImpl(periodOrFrequency.baseValueInMagnitude(Magnitude.Nano).toLong())
            SIUnit.Hertz -> LongValueImpl((1 / periodOrFrequency.baseValue * Magnitude.Nano.factor).toLong())
            else -> throw IllegalArgumentException("Unit '${periodOrFrequency.unit.customName}' not supported in clock")
        }
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (!isEnabled) {
			writer.writeBoolean("enabled", false)
		}
		periodOrFrequency.write("periodOrFrequency", writer, true)
		writer.writeDouble("offPercentage", offPercentage)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("enabled")) {
			isEnabled = reader.readBoolean("enabled")
		}

		if (reader.hasAttribute("unit")) {
			// Backward compatability before MagnitudeValue was introduced
			periodOrFrequency = when (reader.readString("unit")) {
				"s" -> MagnitudeValue(propagationDelay.value / Magnitude.Nano.factor, Magnitude.One, SIUnit.Second)
				"ms" -> MagnitudeValue(propagationDelay.value * (Magnitude.Milli.factor.toDouble() / Magnitude.Nano.factor), Magnitude.Milli, SIUnit.Second)
				"us" -> { MagnitudeValue(propagationDelay.value * (Magnitude.Micro.factor.toDouble() / Magnitude.Nano.factor), Magnitude.Micro, SIUnit.Second) }
				"ns" -> MagnitudeValue(propagationDelay.value, Magnitude.Nano, SIUnit.Second)
				"Hz"-> MagnitudeValue(Magnitude.Nano.factor / propagationDelay.value.toDouble(), Magnitude.One, SIUnit.Hertz)
				"kHz" -> MagnitudeValue(Magnitude.Nano.factor / propagationDelay.value.toDouble() / Magnitude.Kilo.factor, Magnitude.Kilo, SIUnit.Hertz)
				"MHz" -> MagnitudeValue(Magnitude.Nano.factor / propagationDelay.value.toDouble() / Magnitude.Mega.factor, Magnitude.Mega, SIUnit.Hertz)
				"GHz" -> MagnitudeValue(Magnitude.Nano.factor / propagationDelay.value.toDouble() / Magnitude.Giga.factor, Magnitude.Giga, SIUnit.Hertz)
				// else interpret as Nanoseconds
				else -> MagnitudeValue(propagationDelay.value / Magnitude.Nano.factor * Magnitude.Nano.factor, Magnitude.Nano, SIUnit.Second)
			}
		} else if (reader.hasAttribute("periodOrFrequency${MagnitudeValue.MAGNITUDE_VALUE_EXT}")) {
			periodOrFrequency = MagnitudeValue.readWithUnit("periodOrFrequency", reader)
		} else {
			// Backward compatability: Interpret as Nanoseconds
			periodOrFrequency = MagnitudeValue(propagationDelay.value, Magnitude.Nano, SIUnit.Second).normalize()
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
		cycleCountSpeed = signalHandler.systemSpeedCategory.systemSpeed.speed
		resetCycleCount()
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		realStartTime = 0
		super.executionStopped(signalHandler)
		periodOrFrequency = periodOrFrequencyBuffer
	}

	/** ---- [AbstractVertice] */

	override var name: String?
		get() = super.name
		set(value) {
			super.name = value
			getPort<DigitalSignal>().name = value
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
			if (signalHandler.systemSpeedCategory.systemSpeed.speed != cycleCountSpeed) {
				cycleCountSpeed = signalHandler.systemSpeedCategory.systemSpeed.speed
				resetCycleCount()
			}
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

	private fun resetCycleCount() {
		cycleCount = 0
		cycleCountStartTime = System.currentTimeMillis()
	}
}