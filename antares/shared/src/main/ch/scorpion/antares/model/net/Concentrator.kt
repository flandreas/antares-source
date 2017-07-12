package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.checkArgument
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Concentrates multiple [Net]s into one [Net] with a larger [BitWidth].
 */
class Concentrator(
    bitWidth: BitWidth = BitWidth.BW_8,
    branchCount: Int = 4
) : CalculatingVertice(CALCULATOR){

    companion object {
        val CALCULATOR = object : VerticeCalculator<Concentrator> {
            override fun calculate(vertice: Concentrator, data: GraphActorData, signalHandler: SignalHandler) {
                val words = mutableListOf<Word>()
                vertice.getInputs().forEach { words.add(data.getSignal<Word>(it.portId)!!) }
                vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(Word.of(words), signalHandler)
            }
        }
    }

    var _bitWidth: BitWidth = bitWidth
    var bitWidth: BitWidth
        get() = _bitWidth
        set(value) {
            if (_bitWidth != value) {
                setSplitting(value, 2)
            }
        }
    var _branchCount: Int = branchCount
    var branchCount: Int
        get() = _branchCount
        set(value) {
            if (_branchCount != value) {
                setSplitting(bitWidth, value)
            }
        }

    init {
        setSplitting(bitWidth, branchCount)
    }

    /** ---- [Storable] */

    override fun write(writer: StoreWriter) {
        super.write(writer);
        writer.writeInt("bitWidth", bitWidth.width);
        writer.writeInt("branchCount", branchCount);
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        setSplitting(BitWidth.of(reader.readInt("bitWidth")), reader.readInt("branchCount"))
    }

    /** ---- [Concentrator] */

    private fun setSplitting(bitWidth: BitWidth, branchCount: Int) {
        checkArgument(branchCount >= 2 && branchCount <= bitWidth.width, "branchCount must be between 2 and bitWidth")
        checkArgument(bitWidth.width % branchCount == 0, "bitWidth must be divisible by branchCount without remainder")
        _bitWidth = bitWidth
        _branchCount = branchCount
        updateSplitting()
    }

    private fun updateSplitting() {
        clearPorts()
        addPort(DigitalPortImpl.createOutput(Logic.POSITIVE, null, this.bitWidth))
        val portBitWidth = bitWidth.width / branchCount
        for (i in 0..bitWidth.width-1 step portBitWidth) {
            val label = i.toString()
            // Code disabled: Show only the start index of the bit range
            // if (portBitWidth > 1) {
            // label = label + "-" + Integer.toString(i + portBitWidth - 1);
            // }
            addPort(DigitalPortImpl.createInput(Logic.POSITIVE, label, BitWidth.of(portBitWidth)))
        }
    }
}