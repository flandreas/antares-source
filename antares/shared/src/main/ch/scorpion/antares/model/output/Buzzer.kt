package ch.scorpion.antares.model.output

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.gate.AbstractLogicGate
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.sound.Tone
import ch.scorpion.jabbah.base.sound.ToneFactory
import ch.scorpion.jabbah.base.sound.ToneParams
import ch.scorpion.jabbah.base.sound.WaveformType
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class Buzzer : CalculatingVertice(CALCULATOR) {

	companion object {
		const val BASE_RESOURCE_KEY = "library.element.Buzzer"
		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<Buzzer> {
			override fun calculate(vertice: Buzzer, data: GraphActorData, signalHandler: SignalHandler) {
				if (data.changedPort === vertice.enablePort) {
					if (data.getSignal<DigitalSignal>(data.changedPort!!.portId)!!.bitAt(0).isSet) {
						vertice.playSound()
					} else {
						vertice.stopSound()
					}
				} else {
					if (vertice.isEnabled) {
						vertice.playSound()
					}
				}
			}
		}
	}

	override val type: String get() = Translations.getString("$BASE_RESOURCE_KEY.name")
	override val typeDesc: String get() = Translations.getString("$BASE_RESOURCE_KEY.desc")

	val enablePort: DigitalPort get() = getInput<DigitalPort>(1) as DigitalPort
	val frequencyPort: DigitalPort get() = getInput<DigitalPort>(2) as DigitalPort
	val volumePort: DigitalPort get() = getInput<DigitalPort>(3) as DigitalPort

	val isEnabled: Boolean get() = enablePort.getIncomingSignal()?.bitAt(0)?.isSet == true

	var waveformType: WaveformType = WaveformType.Sine

	private var tone: Tone? = null

	init {
		propagationDelay = AbstractLogicGate.DEFAULT_PROPAGATION_DELAY
		addPort(DigitalPortImpl.createInput("EN"))
		addPort(DigitalPortImpl.createInput(Logic.POSITIVE, "F", BitWidth.BW_12))
		addPort(DigitalPortImpl.createInput(Logic.POSITIVE, "V", BitWidth.BW_8))
	}

	/** ---- [Actor] interface */

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		tone = null
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		tone?.stop()
		tone = null
	}

	/** ---- [Storable] interface */

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("waveform")) {
			waveformType = WaveformType.withName(reader.readString("waveform"))
		}
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("waveform", waveformType.customName)
	}

	/** ---- [Buzzer] */

	private fun playSound() {
		val params = createToneParams()
		if (params != null) {
			if (tone == null) {
				tone = ToneFactory.create(params)
				tone!!.play()
			} else {
				tone!!.update(params)
			}
		} else {
			stopSound()
		}
	}

	private fun stopSound() {
		tone?.stop()
		tone = null
	}

	private fun createToneParams(): ToneParams? {
		val frequency = frequencyPort.getIncomingSignal()?.toInt()

		if (frequency != null) {
			if (frequency <= 20 || frequency >= 20_000) {
				return null
			}
			return ToneParams(frequency, getVolume(), waveformType)
		}
		return null
	}

	private fun getVolume(): Double {
		val volumeWidth = volumePort.bitWidth
		val value = volumePort.getIncomingSignal()?.toInt()
		return if (value == null) {
			0.5
		} else {
			(value.toLong().and(0xffffffffL) * 32767).toDouble() / volumeWidth.maxValue.toDouble()
		}
	}
}