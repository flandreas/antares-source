package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.*
import ch.scorpion.antares.model.signal.Word
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
	narrowPortCount: PortCount = PortCount.ONE,
	bitWidth: BitWidth = BitWidth.BW_2,
	narrowBitWidth: BitWidth = BitWidth.BW_1
) : AbstractSplitter(CALCULATOR) {

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.WireTap"
		private val TYPE get() = Translations.getString("${BASE_RESOURCE_KEY}.name")
		private val TYPE_DESC get() = Translations.getOptionalString("${BASE_RESOURCE_KEY}.desc")

		private const val MAX_TAP_COUNT = 8

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

	override var bitWidth: BitWidth = bitWidth
		set(value) {
			if (field != value) {
				field = value
				updatePorts()
				resetTapPositions()
				stateChanged()
			}
		}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	override var narrowSideBitWidth: BitWidth = narrowBitWidth
		set(value) {
			if (field != value) {
				field = value
				updatePorts()
				stateChanged()
			}
		}

	private var tapPositions = MutableList(narrowPortCount.count) { it }

	val tapCount: PortCount get() = PortCount.of(tapPositions.size)

	init {
		propagationDelay = 0
		createPorts(narrowPortCount)
	}

	private fun createPorts(outputCount: PortCount) {
		clearPorts()
		tapPositions.clear()

		addWidePort()
		addNarrowPort(outputCount)
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
				outputBits.add(signal.bitAt(tapPositions[portId - 2] + bitIndex))
			}
			(port as DigitalPort).setOutgoingSignalBuffered(DigitalSignalFactory.ofBits(outputBits), signalHandler)
		}
	}

	override fun concentrate(signalHandler: SignalHandler) {
		val bits = Word.createListWithBit(bitWidth, Bit.Undefined).toMutableList()
		for (portId in 2 until 2 + tapCount.count) {
			val signal = getInput<DigitalSignal>(portId).getIncomingSignal()!!
			for (bitIndex in 0 until narrowSideBitWidth.width) {
				bits[tapPositions[portId - 2] + bitIndex] = signal.bitAt(bitIndex)
			}
		}
		(getPort<DigitalSignal>(1) as DigitalPort).setOutgoingSignalBuffered(DigitalSignalFactory.ofBits(bits), signalHandler)
	}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeInt("tapCount", tapCount.count)
		writer.writeInt("wideBitWidth", bitWidth.width)
		writer.writeInt("narrowBitWidth", narrowSideBitWidth.width)
		writer.writeIntegers("positions", tapPositions)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		createPorts(PortCount.of(reader.readInt("tapCount")))
		bitWidth = BitWidth.read("wideBitWidth", reader)
		narrowSideBitWidth = BitWidth.read("narrowBitWidth", reader)
		tapPositions = mutableListOf(*reader.readIntegers("positions").toTypedArray())
		updateNarrowPorts()
	}

	override fun removePort(port: Port<*>) {
		tapPositions.removeAt(port.portId - 2)
		super.removePort(port)
	}

	/** ---- [WireTap] */

	fun getTapPosition(index: Int): Int = tapPositions[index]

	fun setTapPosition(index: Int, pos: Int) {
		require(pos < bitWidth.width) { "Position must be between 0 and ${bitWidth.width - 1}" }
		tapPositions[index] = pos
		updateNarrowPort(getPort<DigitalSignal>(index + 2) as DigitalPort, index)
	}

	fun setTapPositions(pos: List<Int>) {
		require(pos.size == tapCount.count) { "Number of tap positions must be same as tapCount"}
		require(pos.all { it < bitWidth.width }) { "Every positions must be between 0 and ${bitWidth.width - 1}"}
		tapPositions = pos.toMutableList()
		updateNarrowPorts()
	}

	private fun resetTapPositions() {
		tapPositions = MutableList(tapPositions.size) { 0 }
		updateNarrowPorts()
	}

	/** ---- Dynamic Port management */

	/** Also used by application service to increase [tapCount]. */
	fun addOutputPort(tapPosition: Int): DigitalPort {
		if (tapPosition >= MAX_TAP_COUNT) {
			throw IllegalArgumentException("Max. $MAX_TAP_COUNT output ports allowed in WireTap")
		}
		val port = DigitalPortImpl(PortType.INOUT, bitWidth = narrowSideBitWidth)
		addPort(port)
		tapPositions.add(tapPosition)
		updateNarrowPort(port, tapPositions.size - 1)
		return port
	}

	private fun addWidePort() {
		addPort(DigitalPortImpl(PortType.INOUT, bitWidth = bitWidth))
	}

	private fun addNarrowPort(outputCount: PortCount) {
		for (i in 0 until outputCount.count) {
			addOutputPort(i * narrowSideBitWidth.width)
		}
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
		val pos = tapPositions[i]
		return if (narrowSideBitWidth == BitWidth.BW_1) {
			"$pos"
		} else {
			"$pos..${pos + narrowSideBitWidth.width - 1}"
		}
	}
}