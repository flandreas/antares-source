package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Common base class for [Splitter] and [Concentrator].
 *
 * An [AbstractSplitter] has two sides: The "wide side" has one [DigitalPort] with a wide [BitWidth],
 * and the "narrow side" has multiple [DigitalPort]s each with a narrower [BitWidth] that depends on the
 * value of [branchCount]. For example, an [AbstractSplitter] with a wide side [BitWidth] of 8 and
 * a [branchCount] of 2 has 2 narrow side [DigitalPort]s each with 4 bits.
 */
abstract class AbstractSplitter(
	bitWidth: BitWidth,
	branchCount: BranchCount,
	calculator: VerticeCalculator<AbstractSplitter>
) : CalculatingVertice(calculator) {

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

	val narrowSideBitWidth: BitWidth get() = BitWidth.of(bitWidth.width / branchCount.count)

	init {
		propagationDelay = 0
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

	override fun executionStarted(signalHandler: SignalHandler) {
		super.executionStarted(signalHandler)
		getOutputs().forEach { it.flush(signalHandler) }
	}

	/** ---- [AbstractSplitter] */

	protected abstract fun createWideSidePort(): DigitalPort

	protected abstract fun createNarrowSidePort(index: Int): DigitalPort

	abstract val wideSidePort: DigitalPort

	abstract val narrowSidePorts: List<DigitalPort>

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
		addPort(createWideSidePort())

		for (index in 0 until bitWidth.width step narrowSideBitWidth.width) {
			addPort(createNarrowSidePort(index))
		}
	}
}