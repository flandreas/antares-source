package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Splits a multi-bit [Net] into multiple [Net]s with smaller [BitWidth].
 */
class Splitter(
	bitWidth: BitWidth = BitWidth.BW_8,
	branchCount: BranchCount = BranchCount.BC_4
) : CalculatingVertice(CALCULATOR) {

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.Splitter"
		private val TYPE = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<Splitter> {
			override fun calculate(vertice: Splitter, data: GraphActorData, signalHandler: SignalHandler) {
				val signal = data.getSignal<DigitalSignal>(1)
				for ((index, output) in vertice.getOutputs().withIndex()) {
					val digitalPort = output as DigitalPort
					if (signal == null) {
						digitalPort.setOutgoingSignalBuffered(Word.undefined(vertice.outputBitWidth), signalHandler)
					} else {
						digitalPort.setOutgoingSignalBuffered(signal.getSubword(vertice.outputBitWidth, index), signalHandler)
					}
				}
			}
		}
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	var bitWidth: BitWidth = bitWidth
		set(value) {
			if (field != value) {
				// calling setSplitting() would cause infinite recursion
				field = value
				branchCount =  BranchCount.forBitWidth(value).first()
				updatePorts()
			}
		}

	var branchCount: BranchCount = branchCount
		set(value) {
			if (field != value) {
				// calling setSplitting() would cause infinite recursion
				if (isSplittingSupported(bitWidth, value)) {
					field = value
					updatePorts()
				}
			}
		}

	var signalRepresentation = DigitalSignalRepresentation.BINARY
		set(value) {
			if (field != value) {
				field = value
				getOutputs().map { it as DigitalPort }.forEach { it.signalRepresentation = field }
			}
		}

	val supportedBranchCounts: List<BranchCount> get() = BranchCount.forBitWidth(bitWidth)

	init {
		if (isSplittingSupported(bitWidth, branchCount)) {
			setSplitting(bitWidth, branchCount)
		} else {
			throw IllegalArgumentException("Splitting with bitWidth $bitWidth and branchCount $branchCount not supported")
		}
	}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeInt("bitWidth", bitWidth.width)
		writer.writeInt("branchCount", branchCount.count)
		writer.writeString("representation", signalRepresentation.customName)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		setSplitting(BitWidth.of(reader.readInt("bitWidth")), BranchCount.withCount(reader.readInt("branchCount")))
		if (reader.hasAttribute("representation")) {
			// Legacy file support: in new files, 'representation' is always there
			signalRepresentation = DigitalSignalRepresentation.withName(reader.readString("representation"))
		}
	}

	/** ---- [Splitter] */

	private val outputBitWidth: BitWidth get() = BitWidth.of(bitWidth.width / branchCount.count)

	private fun isSplittingSupported(bitWidth: BitWidth, branchCount: BranchCount): Boolean {
		if (branchCount < BranchCount.BC_2 || branchCount.count > bitWidth.width) {
			return false
		}
		if (!BranchCount.forBitWidth(bitWidth).contains(branchCount)) {
			return false
		}
		return true
	}

	private fun setSplitting(bitWidth: BitWidth, branchCount: BranchCount) {
		if (!isSplittingSupported(bitWidth, branchCount)) {
			return
		}

		this.bitWidth = bitWidth
		this.branchCount = branchCount

		updatePorts()
	}

	private fun updatePorts() {
		clearPorts()
		addPort(DigitalPortImpl.createInput(Logic.POSITIVE, null, this.bitWidth))

		val outputBitWidth = outputBitWidth
		for (i in 0 until bitWidth.width step outputBitWidth.width) {
			addPort(DigitalPortImpl.createOutput(Logic.POSITIVE, i.toString(), outputBitWidth, signalRepresentation))
		}
	}
}