package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.*
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.LongValueImpl
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class WireTap(
	bitWidth: BitWidth = BitWidth.BW_2,
	narrowBitWidth: BitWidth = BitWidth.BW_1,
	narrowPortCount: PortCount = PortCount.ONE,
) : AbstractSplitter(CALCULATOR) {

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.WireTap"
		private val TYPE get() = Translations.getString("${BASE_RESOURCE_KEY}.name")
		private val TYPE_DESC get() = Translations.getOptionalString("${BASE_RESOURCE_KEY}.desc")

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<AbstractSplitter> {
			override fun calculate(vertice: AbstractSplitter, data: GraphActorData, signalHandler: SignalHandler) {
				val changedPortId = data.changedPort!!.portId
				if (changedPortId == 1) {
					vertice.split(data.getSignal(1)!!, signalHandler)
				} else {
					vertice.concentrate(signalHandler)
				}
			}
		}
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	private var config: WireTapConfig = WireTapConfig(bitWidth, narrowBitWidth, narrowPortCount)

	override var bitWidth: BitWidth
		get() = config.wideSideBitWidth
		set(value) {
			if (config.wideSideBitWidth != value) {
				config = config.withWideSideBitWidth(value)
				updatePorts()
				stateChanged()
			}
		}
	override var narrowSideBitWidth: BitWidth
		get() = config.narrowSideBitWidth
		set(value) {
			if (config.narrowSideBitWidth != value) {
				config = config.withNarrowSideBitWidth(value)
				updatePorts()
				stateChanged()
			}
		}

	val tapCount: PortCount get() = config.narrowPortCount

	init {
		propagationDelay = LongValueImpl.ZERO
		createPorts()
	}

	private fun createPorts() {
		clearPorts()

		addPort(DigitalPortImpl(PortType.INOUT, bitWidth = bitWidth))
		for (i in 0 until config.narrowPortCount.count) {
			val port = DigitalPortImpl(PortType.INOUT, bitWidth = narrowSideBitWidth)
			addPort(port)
		}
		updatePorts()
	}

	/** ---- [AbstractSplitter] */

	override val wideSidePort: DigitalPort get() = getPort<DigitalPort>(1) as DigitalPort

	override val narrowSidePorts: List<DigitalPort> get() = getPorts()
		.filterIndexed { index, _ ->  index > 0}
		.map { it as DigitalPort }

	/** ---- [Vertice] */

	override val requireUniquePortNames: Boolean get() = false

	override fun split(signal: DigitalSignal, signalHandler: SignalHandler) {
		for (portId in 2 until 2 + tapCount.count) {
			val port = getOutput<DigitalSignal>(portId)
			val outputBits = mutableListOf<Bit>()
			for (bitIndex in 0 until narrowSideBitWidth.width) {
				outputBits.add(signal.bitAt(config.tapPositions[portId - 2] + bitIndex))
			}
			(port as DigitalPort).setOutgoingSignalBuffered(DigitalSignalFactory.ofBits(outputBits), signalHandler)
		}
	}

	override fun concentrate(signalHandler: SignalHandler) {
		val bits = Word.createListWithBit(bitWidth, Bit.Undefined).toMutableList()
		for (portId in 2 until 2 + tapCount.count) {
			val signal = getInput<DigitalSignal>(portId).getIncomingSignal()!!
			for (bitIndex in 0 until narrowSideBitWidth.width) {
				bits[config.tapPositions[portId - 2] + bitIndex] = signal.bitAt(bitIndex)
			}
		}
		(getPort<DigitalSignal>(1) as DigitalPort).setOutgoingSignalBuffered(DigitalSignalFactory.ofBits(bits), signalHandler)
	}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeStorable("config", config)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		config = reader.readStorable("config")
		createPorts()
		updateNarrowPorts()
	}

	override fun removePort(port: Port<*>) {
		if (port.portId != portsCount) {
			throw IllegalArgumentException("Can only remove last Port from WireTap")
		}
		super.removePort(port)
		config = config.withRemovedNarrowPort()
	}

	/** ---- [WireTap] */

	fun getTapPosition(index: Int): Int = config.tapPositions[index]

	fun setTapPosition(index: Int, pos: Int) {
		config = config.withTapPosition(index, pos)
		updateNarrowPort(getPort<Any>(index + 2) as DigitalPort, index)
	}

	/** ---- Dynamic Port management */

	fun addNarrowPorts(count: Int): List<DigitalPort> {
		val ports = mutableListOf<DigitalPort>()
		config = config.withAddedNarrowPorts(count)
		for (i in 0 until count) {
			DigitalPortImpl(PortType.INOUT, bitWidth = narrowSideBitWidth).also {
				addPort(it)
				ports.add(it)
			}
		}
		updatePorts()
		return ports
	}

	private fun updatePorts() {
		updateWidePort()
		updateNarrowPorts()
	}

	private fun updateWidePort() {
		(getPort<DigitalSignal>(1) as DigitalPort).bitWidth = bitWidth
	}

	private fun updateNarrowPorts() {
		for (i in 0 until tapCount.count) {
			updateNarrowPort((getPort<DigitalSignal>(i + 2) as DigitalPort), i)
		}
	}

	private fun updateNarrowPort(port: DigitalPort, index: Int) {
		port.apply {
			bitWidth = narrowSideBitWidth
			name = createNarrowPortName(index)
		}
	}

	private fun createNarrowPortName(i: Int): String {
		val pos = config.tapPositions[i]
		return if (narrowSideBitWidth == BitWidth.BW_1) {
			"$pos"
		} else {
			"$pos..${pos + narrowSideBitWidth.width - 1}"
		}
	}
}