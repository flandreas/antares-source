package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.Logic
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
 * Concentrates multiple [Net]s into one [Net] with a larger [BitWidth].
 */
class Concentrator(
    bitWidth: BitWidth = BitWidth.BW_8,
    branchCount: BranchCount = BranchCount.BC_4
) : CalculatingVertice("library.element.Concentrator", CALCULATOR){

    companion object {
        val CALCULATOR = object : VerticeCalculator<Concentrator> {
            override fun calculate(vertice: Concentrator, data: GraphActorData, signalHandler: SignalHandler) {
                val words = mutableListOf<Word>()
                vertice.getInputs().forEach { words.add(data.getSignal(it.portId)!!) }
                vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(Word.of(words), signalHandler)
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

    /** ---- [Concentrator] */

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
        addPort(DigitalPortImpl.createOutput(Logic.POSITIVE, null, this.bitWidth))
        val portBitWidth = bitWidth.width / branchCount.count
        for (i in 0 until bitWidth.width step portBitWidth) {
            val label = i.toString()
            // Code disabled: Show only the start index of the bit range
            // if (portBitWidth > 1) {
            // label = label + "-" + Integer.toString(i + portBitWidth - 1);
            // }
            addPort(DigitalPortImpl.createInput(Logic.POSITIVE, label, BitWidth.of(portBitWidth)))
        }
    }
}