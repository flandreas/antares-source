package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPort
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
 * Splits a multi-bit [Net] into multiple [Net]s with smaller [BitWidth].
 */
class Splitter(
    bitWidth: BitWidth,
    branchCount: Int
) : CalculatingVertice(CALCULATOR) {

    constructor(): this(BitWidth.BW_8, 4)

    companion object {
        val CALCULATOR = object : VerticeCalculator<Splitter> {
            override fun calculate(vertice: Splitter, data: GraphActorData, signalHandler: SignalHandler) {
                val signal = data.getSignal<DigitalSignal>(1)
                var index = 0
                for (output in vertice.getOutputs()) {
                    val digitalPort = output as DigitalPort
                    if (signal == null) {
                        digitalPort.setOutgoingSignalBuffered(Word.undefined(vertice.getOutputBitWidth()), signalHandler)
                    } else {
                        digitalPort.setOutgoingSignalBuffered(signal.getSubword(vertice.getOutputBitWidth(), index), signalHandler)
                    }
                    index++
                }
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

    /** ---- [Splitter] */

    fun getOutputBitWidth(): BitWidth {
        return BitWidth.of(bitWidth.width / branchCount)
    }

    private fun setSplitting(bitWidth: BitWidth, branchCount: Int) {
        checkArgument(branchCount >= 2 && branchCount <= bitWidth.width, "branchCount must be between 2 and bitWidth")
        checkArgument(bitWidth.width % branchCount == 0, "bitWidth must be divisible by branchCount without remainder")
        _bitWidth = bitWidth
        _branchCount = branchCount
        updateSplitting()
    }

    private fun updateSplitting() {
        clearPorts()
        addPort(DigitalPortImpl.createInput(Logic.POSITIVE, null, this.bitWidth));

        val outputBitWidth = getOutputBitWidth()
        for (i in 0..bitWidth.width - 1 step outputBitWidth.width) {
            val label = i.toString()
            // Code disabled: Show only the start index of the bit range
            // if (outputBitWidth.getWidth() > 1) {
            // label = label + "-" + Integer.toString(i + outputBitWidth.getWidth() - 1);
            // }
            addPort(DigitalPortImpl.createOutput(Logic.POSITIVE, label, outputBitWidth))
        }
    }
}