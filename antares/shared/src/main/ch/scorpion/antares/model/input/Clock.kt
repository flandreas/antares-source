package ch.scorpion.antares.model.input

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.gate.AbstractDigitalGate
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A digital component that produces a periodically changing [DigitalSignal].
 */
class Clock : AbstractDigitalGate(CALCULATOR, InputCount.ZERO) {

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

	/** Used for restoring [propagationDelay] after the simulation has ended. */
	private var propagationDelayBuffer: Long = 0

	init {
		propagationDelay = 1_000_000_000
	}

	/** ---- [AbstractDigitalGate] */

	override val minInputCount: InputCount get() = InputCount.ZERO

	override val maxInputCount: InputCount get() = InputCount.ZERO

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (!isEnabled) {
			writer.writeBoolean("enabled", false)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("enabled")) {
			isEnabled = reader.readBoolean("enabled")
		}
	}

	/** ---- [Actor] interface */

	override fun executionInitialize(signalHandler: SignalHandler) {
		super.executionInitialize(signalHandler)
		realStartTime = System.currentTimeMillis()
		cycleCount = 0
		propagationDelayBuffer = propagationDelay
		isOn = false
		getOutput<DigitalSignal>().setOutgoingSignalBuffered(Word.of(this.isOn), signalHandler)
	}

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		setOn(signalHandler, false)
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		realStartTime = 0
		cycleCount = 0
		super.executionStopped(signalHandler)
		propagationDelay = propagationDelayBuffer
	}

	/** ---- [Clock] */

	fun setOn(signalHandler: SignalHandler, on: Boolean) {
		if (this.isOn != on) {
			cycleCount++
			this.isOn = on
			getOutput<DigitalSignal>().setOutgoingSignalBuffered(Word.of(this.isOn), signalHandler)
			stateChanged()
			if (isEnabled) {
				requestActingAfter(signalHandler, propagationDelay / 2, createActorData(null))
			}
		}
	}
}