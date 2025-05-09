package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.net.PullDirection.HIGH
import ch.scorpion.antares.model.net.PullDirection.LOW
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.*
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
		private const val PULL_UP_BASE_RESOURCE_KEY = "library.element.PullUpResistor"
		private const val PULL_DOWN_BASE_RESOURCE_KEY = "library.element.PullDownResistor"
		private val TYPE_LOW_DESC get() = Translations.getString("$BASE_RESOURCE_KEY.low.desc")
		private val TYPE_HIGH_DESC get() = Translations.getString("$BASE_RESOURCE_KEY.high.desc")
		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<PullResistor> {
			override fun calculate(vertice: PullResistor, data: GraphActorData, signalHandler: SignalHandler) {
				if (signalHandler.executionTime == vertice.propagationDelay.value) {
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

	override val type: String get() = when (pullDirection) {
		LOW -> Translations.getString("$PULL_DOWN_BASE_RESOURCE_KEY.name")
		HIGH -> Translations.getString("$PULL_UP_BASE_RESOURCE_KEY.name")
	}

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

	/** ---- [GraphElement] */

	override fun graphParamsChanged(graph: Graph) {
		super.graphParamsChanged(graph)
		(bitWidth as? BitWidthExpression)?.let { it.evaluateIn(graph)?.let { bw -> bitWidth = bw } }
	}

	/** ---- [WeakOutputPortBehaviour] interface */

	override val isWeekOutputPortBehaviour: Boolean get() = true

	override fun withdrawWeakOutput(netSignal: DigitalSignal?, port: OutputPort<DigitalSignal>, signalHandler: SignalHandler) {
		val weakSignal = preferredOutputSignal.replaceBy(Bit.Undefined) { index, _ ->
			if (netSignal != null && index < netSignal.bitWidth.width) {
				netSignal.bitAt(index).isDefined
			} else {
				true
			}
		}
		port.setOutgoingSignalBuffered(weakSignal, signalHandler)
		stateChanged(signalHandler)
	}

	override fun activateWeakOutput(netSignal: DigitalSignal?, port: OutputPort<DigitalSignal>, signalHandler: SignalHandler): DigitalSignal {
		val weakSignal = netSignal?.replaceBy(getPreferredBit(pullDirection)) { _, bit -> bit == Bit.Undefined }
			?: getPreferredSignal(bitWidth, pullDirection)

		withdrawWeakOutput(netSignal, port, signalHandler)

		return weakSignal
	}

	override fun handleNetChanged(signalHandler: SignalHandler) {
		stateChanged(signalHandler)
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		bitWidth.write("bitWidth", writer)
		writer.writeString("pullDir", pullDirection.customName)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		bitWidth = BitWidth.read("bitWidth", reader)
		pullDirection = PullDirection.withName(reader.readString("pullDir"))
	}

	/** ---- [Actor] */

	override fun executionInitialize(signalHandler: SignalHandler) {
		super.executionInitialize(signalHandler)
		getOutput<DigitalSignal>().setOutgoingSignalBuffered(null, signalHandler)
	}

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		requestActingAfter(signalHandler, propagationDelay.value, createActorData(null))
	}
}