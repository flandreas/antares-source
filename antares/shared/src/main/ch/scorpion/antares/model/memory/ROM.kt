package ch.scorpion.antares.model.memory

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter


/**
 * A read-only memory whose address width and data width can be specified.
 */
class ROM : CalculatingVertice(CALCULATOR) {

    companion object {

        val ADDRESS_PORT_NAME = "A"
        val CHIP_SELECT_PORT_NAME = "CS"
        val DATA_PORT_NAME = "D"

        val CALCULATOR = object : VerticeCalculator<ROM> {
            override fun calculate(vertice: ROM, data: GraphActorData, signalHandler: SignalHandler) {
                if (vertice.getChipSelectInput().getIncomingSignal() == Word.of(true)) {
                    val address = vertice.getAddressInput().getIncomingSignal()
                    val addressInt = address!!.toInt()
                    if (addressInt == null) {
                        vertice.getDataOutput().setOutgoingSignalBuffered(Word.error(vertice.getDataWidth()), signalHandler)
                    } else {
                        val value = vertice.read(addressInt)
                        vertice.getDataOutput().setOutgoingSignalBuffered(Word.of(vertice.getDataWidth(), value), signalHandler)
                    }
                } else {
                    vertice.getDataOutput().setOutgoingSignalBuffered(Word.undefined(vertice.getDataWidth()), signalHandler)
                }
            }
        }
    }

    val memory = Memory()

    init {
        addPort(DigitalPortImpl.createInput(Logic.POSITIVE, ADDRESS_PORT_NAME, BitWidth.BW_8))
        addPort(DigitalPortImpl.createInput(CHIP_SELECT_PORT_NAME))
        addPort(DigitalPortImpl.createOutput(Logic.POSITIVE, DATA_PORT_NAME, BitWidth.BW_8,
                DigitalSignalRepresentation.HEXADECIMAL));
    }

    /** ---- [Storable] */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeString("addressBitWidth", getAddressWidth().customName)
        writer.writeString("dataBitWidth", getDataWidth().customName)
        writer.writeString("content", CompressedMemoryDump.write(memory, getDataWidth()))
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        setAddressWidth(BitWidth.withName(reader.readString("addressBitWidth")))
		setDataWidth(BitWidth.withName(reader.readString("dataBitWidth")))
        CompressedMemoryDump.read(memory, reader.readString("content"))
    }

    /** ---- [ROM]  */

    fun clear() {
        memory.clear()
        stateChanged()
    }

    fun setAddressWidth(bitWidth: BitWidth) {
        checkNotNull(bitWidth)
        getAddressInput().bitWidth = bitWidth
        stateChanged()
    }

    fun setDataWidth(bitWidth: BitWidth) {
        checkNotNull(bitWidth)
        getDataOutput().bitWidth = bitWidth
        stateChanged()
    }

    fun getAddressInput(): DigitalPort {
        val addressInput = getInput<DigitalSignal>(ADDRESS_PORT_NAME)
        return addressInput as DigitalPort
    }

    fun getChipSelectInput(): DigitalPort {
        val addressInput = getInput<DigitalSignal>(CHIP_SELECT_PORT_NAME)
        return addressInput as DigitalPort
    }

    fun getDataOutput(): DigitalPort {
        val dataOutput = getOutput<DigitalSignal>(DATA_PORT_NAME)
        return dataOutput as DigitalPort
    }

    fun getAddressWidth(): BitWidth {
        return getAddressInput().bitWidth
    }

    fun getDataWidth(): BitWidth {
        return getDataOutput().bitWidth
    }

    fun read(address: Int): Long {
        return memory.read(address)
    }

    fun write(address: Int, value: Long) {
        memory.write(address, value)
    }
}