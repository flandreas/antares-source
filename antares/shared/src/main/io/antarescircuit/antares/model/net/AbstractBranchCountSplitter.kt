package io.antarescircuit.antares.model.net

import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

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

	override fun split(signal: DigitalSignal, signalHandler: SignalHandler) {
		for (portId in 2..portsCount) {
			val outputPort = getPort<DigitalSignal>(portId) as DigitalPort
			outputPort.setOutgoingSignalBuffered(signal.getSubword(narrowSideBitWidth, portId - 2), signalHandler)
		}
	}

	override fun concentrate(signalHandler: SignalHandler) {
		val words = mutableListOf<DigitalSignal>()
		for (portId in 2..portsCount) {
			val signal = (getPort<DigitalPort>(portId) as DigitalPort).getIncomingSignal() as DigitalSignal
			words.add(signal)
		}
		val output = DigitalSignalFactory.of(words)
		(getPort<DigitalSignal>(1) as DigitalPort).setOutgoingSignalBuffered(output, signalHandler)
	}

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
		if (branchCount.count < BranchCount.BC_2.count || branchCount.count > bitWidth.width) {
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