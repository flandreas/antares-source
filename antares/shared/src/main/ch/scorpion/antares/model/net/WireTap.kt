package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class WireTap(
	inputBitWidth: BitWidth = BitWidth.BW_2,
	outputBitWidth: BitWidth = BitWidth.BW_1,
	tapPositions: List<Int> = listOf(0, 1)
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

	private var tapPositions = mutableListOf(*tapPositions.toTypedArray())

	val tapCount: Int get() = tapPositions.size

	init {
		propagationDelay = 0
		createPorts()
	}

	private fun createPorts() {
		clearPorts()
		addInputPort()
		addOutputPorts()
		updatePorts()
	}

	private fun addInputPort() {
		addPort(DigitalPortImpl(PortType.INOUT, bitWidth = inputBitWidth))
	}

	private fun addOutputPorts() {
		for (i in 0 until tapCount) {
			addPort(DigitalPortImpl(PortType.INOUT, name, bitWidth = outputBitWidth))
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
		for (i in 0 until tapCount) {
			(getPort<DigitalSignal>(i + 2) as DigitalPort).apply {
				bitWidth = outputBitWidth
				name = createOutputPortName(i)
			}
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

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeInt("inputWidth", inputBitWidth.width)
		writer.writeInt("outputWidth", outputBitWidth.width)
		writer.writeIntegers("positions", tapPositions)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		inputBitWidth = BitWidth.read("inputWidth", reader)
		outputBitWidth = BitWidth.read("outputWidth", reader)
		tapPositions = mutableListOf(*reader.readIntegers("positions").toTypedArray())
	}
}