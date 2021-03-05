package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.net.PullDirection.HIGH
import ch.scorpion.antares.model.net.PullDirection.LOW
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.vertice.AbstractVertice
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class PullResistor(
	bitWidth: BitWidth = BitWidth.BW_1,
	pullDirection: PullDirection = LOW
) : AbstractVertice() {

	companion object{

		private const val BASE_RESOURCE_KEY = "library.element.PullResistor"
		private val TYPE = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_LOW_DESC = Translations.getOptionalString("$BASE_RESOURCE_KEY.low.desc")
		private val TYPE_HIGH_DESC = Translations.getOptionalString("$BASE_RESOURCE_KEY.high.desc")

		fun getPreferredSignal(bitWidth: BitWidth, pullDirection: PullDirection): DigitalSignal =
			when(pullDirection) {
				LOW -> Word.allOf(bitWidth, Bit.False)
				HIGH -> Word.allOf(bitWidth, Bit.True)
			}
	}

	override val type: String get() = TYPE

	override val typeDesc: String? get() = when (pullDirection) {
		LOW -> TYPE_LOW_DESC
		HIGH -> TYPE_HIGH_DESC
	}

	var bitWidth: BitWidth
		get() = getOutputPort().bitWidth
		set(value) {
			if (value != bitWidth) {
				getOutputPort().bitWidth = value
				getOutputPort().weakSignal = getPreferredSignal(value, pullDirection)
				stateChanged()
			}
		}

	var pullDirection: PullDirection = pullDirection
		set(value) {
			if (field != value) {
				field = value
				getOutputPort().weakSignal = getPreferredSignal(bitWidth, value)
				stateChanged()
			}
		}

	private val preferredOutputSignal: DigitalSignal get() =
		getPreferredSignal(bitWidth, pullDirection)

	init {
		addPort(DigitalPortImpl(
			portType = PortType.OUTPUT,
			bitWidth = bitWidth,
			canBeUndefined = true,
			weakSignal = getPreferredSignal(bitWidth, pullDirection)
		))
	}

	fun getOutputPort(): DigitalPort = getOutput<DigitalSignal>() as DigitalPort

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeInt("bitWidth", bitWidth.width)
		writer.writeString("pullDir", pullDirection.customName)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		bitWidth = BitWidth.of(reader.readInt("bitWidth"))
		pullDirection = PullDirection.withName(reader.readString("pullDir"))
	}

	/** ---- [Actor] */

	override fun executionStarted(signalHandler: SignalHandler) {
		super.executionStarted(signalHandler)
		getOutput<DigitalSignal>().setOutgoingSignal(preferredOutputSignal, signalHandler)
	}
}