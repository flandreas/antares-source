package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * An [AbstractSplitter] with a [BranchCount] property.
 *
 * The [BitWidth] of the narrow side depends on the value of [branchCount]. For example,
 * an [AbstractSplitter] with a wide side [BitWidth] of 8 and a [branchCount] of 2 has
 * 2 narrow side [DigitalPort]s each with 4 bits.
 */
abstract class AbstractBranchCountSplitter(
	bitWidth: BitWidth,
	branchCount: BranchCount,
	calculator: VerticeCalculator<AbstractSplitter>
) : AbstractSplitter(calculator) {

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

	val supportedBranchCounts: List<BranchCount> get() = BranchCount.forBitWidth(bitWidth)

	override var bitWidth: BitWidth = bitWidth
		set(value) {
			if (field != value) {
				field = value
				branchCount =  BranchCount.forBitWidth(value).first()
				updatePorts()
				stateChanged()
			}
		}

	init {
		if (isSplittingSupported(bitWidth, branchCount)) {
			setSplitting(bitWidth, branchCount)
		} else {
			throw IllegalArgumentException("Splitting with bitWidth $bitWidth and branchCount $branchCount not supported")
		}
	}

	/** ---- [AbstractSplitter] */

	override val narrowSideBitWidth: BitWidth get() = BitWidth.of(bitWidth.width / branchCount.count)

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeInt("branchCount", branchCount.count)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		setSplitting(BitWidth.read("bitWidth", reader), BranchCount.withCount(reader.readInt("branchCount")))
	}

	/** ---- [AbstractBranchCountSplitter] */

	protected abstract fun createWideSidePort(): DigitalPort

	protected abstract fun createNarrowSidePort(index: Int): DigitalPort

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

	protected open fun updatePorts() {
		clearPorts()
		addPort(createWideSidePort())

		for (index in 0 until bitWidth.width step narrowSideBitWidth.width) {
			addPort(createNarrowSidePort(index))
		}
	}
}