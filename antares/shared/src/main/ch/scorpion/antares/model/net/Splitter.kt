package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.io.Storable

/**
 * Splits a multi-bit [Net] into multiple [Net]s with smaller [BitWidth].
 */
class Splitter(
	bitWidth: BitWidth = BitWidth.BW_8,
	branchCount: BranchCount = BranchCount.BC_4
) : CalculatingVertice("library.element.Splitter", CALCULATOR) {

	companion object {

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<Splitter> {
			override fun calculate(vertice: Splitter, data: GraphActorData, signalHandler: SignalHandler) {
				val signal = data.getSignal<DigitalSignal>(1)
				for ((index, output) in vertice.getOutputs().withIndex()) {
					val digitalPort = output as DigitalPort
					if (signal == null) {
						digitalPort.setOutgoingSignalBuffered(Word.undefined(vertice.getOutputBitWidth()), signalHandler)
					} else {
						digitalPort.setOutgoingSignalBuffered(signal.getSubword(vertice.getOutputBitWidth(), index), signalHandler)
					}
				}
			}
		}
	}

	private var _bitWidth: BitWidth = bitWidth
	var bitWidth: BitWidth
		get() = _bitWidth
		set(value) {
			if (_bitWidth != value) {
				setSplitting(value, BranchCount.forBitWidth(value).first())
			}
		}
	private var _branchCount: BranchCount = branchCount
	var branchCount: BranchCount
		get() = _branchCount
		set(value) {
			if (_branchCount != value) {
				setSplitting(bitWidth, value)
			}
		}

	val supportedBranchCounts: List<BranchCount> get() = BranchCount.forBitWidth(bitWidth)

	init {
		setSplitting(bitWidth, branchCount)
	}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeInt("bitWidth", bitWidth.width)
		writer.writeInt("branchCount", branchCount.count)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		setSplitting(BitWidth.of(reader.readInt("bitWidth")), BranchCount.withCount(reader.readInt("branchCount")))
	}

	/** ---- [Splitter] */

	fun getOutputBitWidth(): BitWidth {
		return BitWidth.of(bitWidth.width / branchCount.count)
	}

	private fun setSplitting(bitWidth: BitWidth, branchCount: BranchCount) {
		if (branchCount < BranchCount.BC_2 || branchCount.count > bitWidth.width) {
			return
		}
		if (!BranchCount.forBitWidth(bitWidth).contains(branchCount)) {
			return
		}
		_bitWidth = bitWidth
		_branchCount = branchCount
		updateSplitting()
	}

	private fun updateSplitting() {
		clearPorts()
		addPort(DigitalPortImpl.createInput(Logic.POSITIVE, null, this.bitWidth))

		val outputBitWidth = getOutputBitWidth()
		for (i in 0 until bitWidth.width step outputBitWidth.width) {
			val label = i.toString()
			// Code disabled: Show only the start index of the bit range
			// if (outputBitWidth.getWidth() > 1) {
			// label = label + "-" + Integer.toString(i + outputBitWidth.getWidth() - 1);
			// }
			addPort(DigitalPortImpl.createOutput(Logic.POSITIVE, label, outputBitWidth))
		}
	}
}