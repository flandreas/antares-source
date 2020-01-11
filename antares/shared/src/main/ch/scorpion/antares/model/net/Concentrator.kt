package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.io.Storable

/**
 * Concentrates multiple [Net]s into one [Net] with a larger [BitWidth].
 */
class Concentrator(
	bitWidth: BitWidth = BitWidth.BW_8,
	branchCount: BranchCount = BranchCount.BC_4
) : CalculatingVertice("library.element.Concentrator", CALCULATOR) {

	companion object {

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<Concentrator> {
			override fun calculate(vertice: Concentrator, data: GraphActorData, signalHandler: SignalHandler) {
				val words = mutableListOf<Word>()
				vertice.getInputs().forEach { words.add(data.getSignal(it.portId)!!) }
				vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(Word.of(words), signalHandler)
			}
		}
	}

	var bitWidth: BitWidth = bitWidth
		set(value) {
			if (field != value) {
				// calling setConcentration() would cause infinite recursion
				field = value
				branchCount = BranchCount.forBitWidth(value).first()
				updatePorts()
			}
		}

	var branchCount: BranchCount = branchCount
		set(value) {
			if (field != value) {
				// calling setConcentration() would cause infinite recursion
				if (isConcentrationSupported(bitWidth, value)) {
					field = value
					updatePorts()
				}
			}
		}

	var signalRepresentation: DigitalSignalRepresentation = DigitalSignalRepresentation.BINARY
		set(value) {
			if (field != value) {
				field = value
				(getOutput<DigitalSignal>() as DigitalPort).signalRepresentation = field
			}
		}

	val supportedBranchCounts: List<BranchCount> get() = BranchCount.forBitWidth(bitWidth)

	init {
		if (isConcentrationSupported(bitWidth, branchCount)) {
			setConcentration(bitWidth, branchCount)
		} else {
			throw IllegalArgumentException("Concentration with bitWidth $bitWidth and branchCount $branchCount not supported")
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
		if (reader.hasAttribute("representation")) {
			// Legacy file support: in new files, 'representation' is always there
			signalRepresentation = DigitalSignalRepresentation.withName(reader.readString("representation"))
		}
		setConcentration(BitWidth.of(reader.readInt("bitWidth")), BranchCount.withCount(reader.readInt("branchCount")))
	}

	/** ---- [Concentrator] */

	private fun isConcentrationSupported(bitWidth: BitWidth, branchCount: BranchCount): Boolean {
		if (branchCount < BranchCount.BC_2 || branchCount.count > bitWidth.width) {
			return false
		}
		if (!BranchCount.forBitWidth(bitWidth).contains(branchCount)) {
			return false
		}
		return true
	}

	private fun setConcentration(bitWidth: BitWidth, branchCount: BranchCount) {
		if (!isConcentrationSupported(bitWidth, branchCount)) {
			return
		}
		this.bitWidth = bitWidth
		this.branchCount = branchCount

		updatePorts()
	}

	private fun updatePorts() {
		clearPorts()
		addPort(DigitalPortImpl.createOutput(Logic.POSITIVE, null, bitWidth, signalRepresentation))

		val portBitWidth = bitWidth.width / branchCount.count
		for (i in 0 until bitWidth.width step portBitWidth) {
			addPort(DigitalPortImpl.createInput(Logic.POSITIVE, i.toString(), BitWidth.of(portBitWidth)))
		}
	}
}