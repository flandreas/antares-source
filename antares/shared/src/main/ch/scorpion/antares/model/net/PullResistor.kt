package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.net.PullDirection.HIGH
import ch.scorpion.antares.model.net.PullDirection.LOW
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class PullResistor(
	bitWidth: BitWidth = BitWidth.BW_1,
	pullDirection: PullDirection = LOW
) : CalculatingVertice(CALCULATOR), WeakOutputPortBehaviour<DigitalSignal> {

	companion object{

		private const val BASE_RESOURCE_KEY = "library.element.PullResistor"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_LOW_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.low.desc")
		private val TYPE_HIGH_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.high.desc")
		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<PullResistor> {
			override fun calculate(vertice: PullResistor, data: GraphActorData, signalHandler: SignalHandler) {
				if (signalHandler.executionTime == vertice.propagationDelay) {
					vertice.getOutput<DigitalSignal>().net?.let {
						if (it.signal?.isFullyUndefined == true) {
							vertice.getOutput<DigitalSignal>().setOutgoingSignal(vertice.preferredOutputSignal, signalHandler)
						}
					}
				}
			}
		}

		fun getPreferredSignal(bitWidth: BitWidth, pullDirection: PullDirection): DigitalSignal =
			when(pullDirection) {
				LOW -> DigitalSignalFactory.allOf(bitWidth, Bit.False)
				HIGH -> DigitalSignalFactory.allOf(bitWidth, Bit.True)
			}

		fun getPreferredBit(pullDirection: PullDirection): Bit =
			when (pullDirection) {
				LOW -> Bit.False
				HIGH -> Bit.True
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

	private val preferredOutputSignal: DigitalSignal get() =
		getPreferredSignal(bitWidth, pullDirection)

	init {
		addPort(DigitalPortImpl(
			portType = PortType.OUTPUT,
			bitWidth = bitWidth,
			canBeUndefined = true,
			weakBehaviour = this
		))
	}

	fun getOutputPort(): DigitalPort = getOutput<DigitalSignal>() as DigitalPort

	/** ---- [WeakOutputPortBehaviour] interface */

	override fun withdrawWeakOutput(netSignal: DigitalSignal?, port: OutputPort<DigitalSignal>, signalHandler: SignalHandler) {
		val weakSignal = preferredOutputSignal.replaceBy(Bit.Undefined) { index, _ -> netSignal?.bitAt(index)?.isDefined ?: true }
		port.setOutgoingSignalBuffered(weakSignal, signalHandler)
		stateChanged(signalHandler)
	}

	override fun activateWeakOutput(netSignal: DigitalSignal?, port: OutputPort<DigitalSignal>, signalHandler: SignalHandler): DigitalSignal {
		val weakSignal = netSignal?.replaceBy(getPreferredBit(pullDirection)) { _, bit -> bit == Bit.Undefined }
			?: getPreferredSignal(bitWidth, pullDirection)

		withdrawWeakOutput(netSignal, port, signalHandler)

		return weakSignal
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

	override fun executionInitialize(signalHandler: SignalHandler) {
		super.executionInitialize(signalHandler)
		getOutput<DigitalSignal>().setOutgoingSignalBuffered(null, signalHandler)
	}

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		requestActingAfter(signalHandler, propagationDelay, GraphActorDataImpl(null, preferredOutputSignal))
	}
}