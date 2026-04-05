package io.antarescircuit.antares.model.output

import io.antarescircuit.antares.model.Logic
import io.antarescircuit.antares.model.gate.AbstractLogicGate
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.port.DigitalPortImpl
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.sound.Tone
import io.antarescircuit.jabbah.base.sound.ToneFactory
import io.antarescircuit.jabbah.base.sound.ToneParams
import io.antarescircuit.jabbah.base.sound.WaveformType
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.Translation
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.actor.Actor
import io.antarescircuit.jabbah.graph.model.GraphActorData
import io.antarescircuit.jabbah.graph.model.vertice.CalculatingVertice
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

class Buzzer : CalculatingVertice(CALCULATOR) {

	companion object {
		const val BASE_RESOURCE_KEY = "library.element.Buzzer"

		private val VOLUME_PORT_DESC get() = TranslatableText(Translation.ofStaticKey("antares.buzzer.volume.desc"))
		private val FREQUENCY_PORT_DESC get() = TranslatableText(Translation.ofStaticKey("antares.buzzer.frequency.desc"))
		private val ENABLE_PORT_DESC get() = TranslatableText(Translation.ofStaticKey("antares.buzzer.enable.desc"))

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
		addPort(DigitalPortImpl.createInput("EN", description = ENABLE_PORT_DESC))
		addPort(DigitalPortImpl.createInput(Logic.POSITIVE, "F", BitWidth.BW_12, FREQUENCY_PORT_DESC))
		addPort(DigitalPortImpl.createInput(Logic.POSITIVE, "V", BitWidth.BW_8, VOLUME_PORT_DESC))
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