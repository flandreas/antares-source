package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class WireTap(
	outputCount: PortCount = PortCount.ONE,
	inputBitWidth: BitWidth = BitWidth.BW_2,
	outputBitWidth: BitWidth = BitWidth.BW_1
) : CalculatingVertice(CALCULATOR) {

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.WireTap"
		private val TYPE get() = Translations.getString("${BASE_RESOURCE_KEY}.name")
		private val TYPE_DESC get() = Translations.getOptionalString("${BASE_RESOURCE_KEY}.desc")

		private const val MAX_TAP_COUNT = 8

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<WireTap> {
			override fun calculate(vertice: WireTap, data: GraphActorData, signalHandler: SignalHandler) {
				TODO()
			}
		}
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	var inputBitWidth: BitWidth = inputBitWidth
		set(value) {
			if (field != value) {
				field = value
				resetTapPositions()
				updatePorts()
				stateChanged()
			}
		}

	var outputBitWidth: BitWidth = outputBitWidth
		set(value) {
			if (field != value) {
				field = value
				updatePorts()
				stateChanged()
			}
		}

	private var tapPositions = mutableListOf<Int>()

	val tapCount: PortCount get() = PortCount.of(tapPositions.size)

	init {
		propagationDelay = 0
		createPorts(outputCount)
	}

	private fun createPorts(outputCount: PortCount) {
		clearPorts()
		tapPositions.clear()

		addInputPort()
		addOutputPorts(outputCount)
		updatePorts()
	}

	/** ---- [Vertice] */

	override val requireUniquePortNames: Boolean get() = false

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeInt("outputCount", tapCount.count)
		writer.writeInt("inputWidth", inputBitWidth.width)
		writer.writeInt("outputWidth", outputBitWidth.width)
		writer.writeIntegers("positions", tapPositions)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		createPorts(PortCount.of(reader.readInt("outputCount")))
		inputBitWidth = BitWidth.read("inputWidth", reader)
		outputBitWidth = BitWidth.read("outputWidth", reader)
		tapPositions = mutableListOf(*reader.readIntegers("positions").toTypedArray())
		updateOutputPorts()
	}

	override fun removePort(port: Port<*>) {
		tapPositions.removeAt(port.portId - 2)
		super.removePort(port)
	}

	/** ---- [WireTap] */

	fun getTapPosition(index: Int): Int = tapPositions[index]

	fun setTapPosition(index: Int, pos: Int) {
		require(pos < inputBitWidth.width) { "Position must be between 0 and ${inputBitWidth.width}" }
		tapPositions[index] = pos
		updateOutputPort(getPort<DigitalSignal>(index + 2) as DigitalPort, index)
	}

	private fun resetTapPositions() {
		tapPositions = MutableList(tapPositions.size) { 0 }
		updateOutputPorts()
	}

	/** ---- Dynamic Port management */

	/** Also used by application service to increase [tapCount]. */
	fun addOutputPort(tapPosition: Int): DigitalPort {
		if (tapPosition >= MAX_TAP_COUNT) {
			throw IllegalArgumentException("Max. $MAX_TAP_COUNT output ports allowed in WireTap")
		}
		val port = DigitalPortImpl(PortType.INOUT, bitWidth = outputBitWidth)
		addPort(port)
		tapPositions.add(tapPosition)
		updateOutputPort(port, tapPositions.size - 1)
		return port
	}

	private fun addInputPort() {
		addPort(DigitalPortImpl(PortType.INOUT, bitWidth = inputBitWidth))
	}

	private fun addOutputPorts(outputCount: PortCount) {
		for (i in 0 until outputCount.count) {
			addOutputPort(i * outputBitWidth.width)
		}
	}

	private fun updatePorts() {
		updateInputPort()
		updateOutputPorts()
	}

	private fun updateInputPort() {
		(getPort<DigitalSignal>(1) as DigitalPort).bitWidth = inputBitWidth
	}

	private fun updateOutputPorts() {
		for (i in 0 until tapCount.count) {
			updateOutputPort((getPort<DigitalSignal>(i + 2) as DigitalPort), i)
		}
	}

	private fun updateOutputPort(port: DigitalPort, index: Int) {
		port.apply {
			bitWidth = outputBitWidth
			name = createOutputPortName(index)
		}
	}

	private fun createOutputPortName(i: Int): String {
		val pos = tapPositions[i]
		return if (outputBitWidth == BitWidth.BW_1) {
			"$pos"
		} else {
			"$pos..${pos + outputBitWidth.width - 1}"
		}
	}
}