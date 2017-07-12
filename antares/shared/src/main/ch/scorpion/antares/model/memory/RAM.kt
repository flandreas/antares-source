package ch.scorpion.antares.model.memory

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.Trigger
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter


/**
 * Represents a random access (i.e. writable) memory whose address width and data width can be specified.
 */
class RAM(hasClock: Boolean = true) : CalculatingVertice(RAMCalculator()) {

    companion object {
        val ADDRESS_PORT_NAME = "A"
        val CHIP_SELECT_PORT_NAME = "CS"
        val DATA_PORT_NAME = "D"
        val WRITE_PORT_NAME = "WR"
        val CLEAR_PORT_NAME = "CLR"
        val CLOCK_PORT_NAME = "CLK"

    }

    val memory = Memory()

    /** Determines whether this [RAM] has a clock input. */
    var hasClock: Boolean = false
        set(value) {
            if (value != field) {
                field = value
                if (value) {
                    val clockPort = DigitalPortImpl.createInput(CLOCK_PORT_NAME)
                    clockPort.trigger = Trigger.EDGE
                    addPort(clockPort)
                } else {
                    removePort(getPort<DigitalSignal>(CLOCK_PORT_NAME))
                }
            }
        }

    init {
        addPort(DigitalPortImpl.createInput(Logic.POSITIVE, ADDRESS_PORT_NAME, BitWidth.BW_8))
        addPort(DigitalPortImpl.createInput(CHIP_SELECT_PORT_NAME))
        addPort(DigitalPortImpl.createInput(WRITE_PORT_NAME))
        addPort(DigitalPortImpl.createInput(CLEAR_PORT_NAME))
        addPort(DigitalPortImpl.createInOut(Logic.POSITIVE, DATA_PORT_NAME, BitWidth.BW_8))
        this.hasClock = hasClock
    }

    /** ---- [Storable] */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeString("addressBitWidth", getAddressWidth().customName)
        writer.writeString("dataBitWidth", getDataWidth().customName)
        writer.writeBoolean("clock", hasClock)
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        setAddressWidth(BitWidth.withName(reader.readString("addressBitWidth")))
        setDataWidth(BitWidth.withName(reader.readString("dataBitWidth")))
        hasClock = reader.readBoolean("clock")
    }

    /** ---- [Actor] interface */

    override fun executionStarted(signalHandler: SignalHandler) {
        super.executionStarted(signalHandler)
        getDataPort().setOutgoingSignal(Word.undefined(getDataWidth()), signalHandler)
    }

    /** ---- [RAM] */

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
        getDataPort().bitWidth = bitWidth
        stateChanged()
    }

    fun getAddressInput(): DigitalPort {
        val addressInput = getPort<DigitalSignal>(ADDRESS_PORT_NAME)
        return addressInput as DigitalPort
    }

    fun getChipSelectInput(): DigitalPort {
        val addressInput = getPort<DigitalSignal>(CHIP_SELECT_PORT_NAME)
        return addressInput as DigitalPort
    }

    fun getDataPort(): DigitalPort {
        val dataOutput = getPort<DigitalSignal>(DATA_PORT_NAME)
        return dataOutput as DigitalPort
    }

    fun getWriteInput(): DigitalPort {
        val writePort = getPort<DigitalSignal>(WRITE_PORT_NAME)
        return writePort as DigitalPort
    }

    fun getClearInput(): DigitalPort {
        val clearPort = getPort<DigitalSignal>(CLEAR_PORT_NAME)
        return clearPort as DigitalPort
    }

    fun getClockInput(): DigitalPort? {
        if (!hasClock) {
            return null
        }
        return getPort<DigitalSignal>(CLOCK_PORT_NAME) as DigitalPort
    }

    fun getAddressWidth(): BitWidth {
        return getAddressInput().bitWidth
    }

    fun getDataWidth(): BitWidth {
        return getDataPort().bitWidth
    }

    fun read(address: Int): Long? {
        return memory.read(address)
    }

    fun write(address: Int, value: Long) {
        memory.write(address, value)
    }
}