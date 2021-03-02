package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class PullResistor(
	pullDirection: PullDirection = PullDirection.LOW
) : CalculatingVertice(CALCULATOR) {

	companion object{

		private const val BASE_RESOURCE_KEY = "library.element.PullResistor"
		private val TYPE = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")
		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<PullResistor> {
			override fun calculate(vertice: PullResistor, data: GraphActorData, signalHandler: SignalHandler) {
				val input = data.getSignal<Word>(1)
				val output = if (input?.isAllOf(Bit.Undefined) != false) {
					when(vertice.pullDirection) {
						PullDirection.LOW -> Word.allOf(vertice.bitWidth, Bit.False)
						PullDirection.HIGH -> Word.allOf(vertice.bitWidth, Bit.True)
					}
				} else {
					input
				}
				vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(output, signalHandler)
			}
		}
	}

	init {
		addPort(DigitalPortImpl.createInOut())
	}

	override val type: String get() = TYPE

	override val typeDesc: String? get() = TYPE_DESC

	var bitWidth: BitWidth
		get() = (getInput<DigitalSignal>() as DigitalPort).bitWidth
		set(value) {
			if (value != bitWidth) {
				(getInput<DigitalSignal>() as DigitalPort).bitWidth = value
				stateChanged()
			}
		}

	var pullDirection: PullDirection = pullDirection
		set(value) {
			if (field != value) {
				field = value
				stateChanged()
			}
		}

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
		getOutput<DigitalSignal>().setOutgoingSignal(Word.allOf(bitWidth, Bit.Undefined), signalHandler)
	}
}