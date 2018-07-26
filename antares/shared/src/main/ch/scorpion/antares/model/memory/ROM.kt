package ch.scorpion.antares.model.memory

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.*
import ch.scorpion.jabbah.base.Math
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter


/**
 * A read-only memory whose address width and data width can be specified.
 */
class ROM : CalculatingVertice(CALCULATOR), Addressable {

    companion object {

        const val ADDRESS_PORT_NAME = "A"
        const val CHIP_SELECT_PORT_NAME = "CS"
        const val DATA_PORT_NAME = "D"

        val CALCULATOR = object : VerticeCalculator<ROM> {
            override fun calculate(vertice: ROM, data: GraphActorData, signalHandler: SignalHandler) {
                if (vertice.getChipSelectInput().getIncomingSignal() == Word.of(true)) {
                    val address = vertice.getAddressInput().getIncomingSignal()
                    val addressInt = address!!.toInt()
                    if (addressInt == null) {
                        vertice.getDataOutput().setOutgoingSignalBuffered(Word.error(vertice.dataWidth), signalHandler)
                    } else {
	                    vertice.currentSelectedAddress = addressInt
                        vertice.getDataOutput().setOutgoingSignalBuffered(Word.of(vertice.dataWidth, vertice.read(addressInt)), signalHandler)
                    }
                } else {
                    vertice.getDataOutput().setOutgoingSignalBuffered(Word.undefined(vertice.dataWidth), signalHandler)
                }
            }
        }
    }

    val memory = Memory()

    /**
     * A newline-separated list of [Disassembler] configurations consisting of operations in the form "regex=op".
     */
    var disassemblerConfig: String = ""
        set(value) {
            field = value
            resetDisassembler()
            disassembleAll()
        }

	/**
	 * Represents the last value of the address input, but gets only updated when the chip is selected (CS).
	 * Can be used for displaying the "current" (i.e. last) selected address.
	 */
	var currentSelectedAddress: Int = 0

	private val disassembler = Disassembler()

    /** Maps a memory cell address to its disassembly string.*/
    private val disassembly = mutableMapOf<Int,String>()

    /** Contains the number of characters of the longest disassembly entry.*/
    private var _disassemblyWidth: Int = 0

    init {
        addPort(DigitalPortImpl.createInput(Logic.POSITIVE, ADDRESS_PORT_NAME, BitWidth.BW_8))
        addPort(DigitalPortImpl.createInput(CHIP_SELECT_PORT_NAME))
        addPort(DigitalPortImpl.createOutput(Logic.POSITIVE, DATA_PORT_NAME, BitWidth.BW_8,
                DigitalSignalRepresentation.HEXADECIMAL))
    }

    /** ---- [Addressable] interface */

    override val currentAddress: Int get() = currentSelectedAddress

    override val maxAddress: Int get() = getAddressInput().bitWidth.power() - 1

    override val data: Long
        get() {
            val address = currentAddress
            if (address >= 0) {
                return memory.read(address)
            }
            return 0
        }

    override val addressWidth: BitWidth get() = getAddressInput().bitWidth

    override val dataWidth: BitWidth get() = getDataOutput().bitWidth

    override val disassemblyWidth: Int get() = _disassemblyWidth

    override fun dataAt(address: Int): Long = memory.read(address)

    override fun disassemblyAt(address: Int): String = disassembly.getOrElse(address) { "" }

	override fun commentAt(address: Int): String? = memory.readComment(address)

	/** ---- [Storable] */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeString("addressBitWidth", addressWidth.customName)
        writer.writeString("dataBitWidth", dataWidth.customName)
        writer.writeString("content", CompressedMemoryDump.write(memory, dataWidth))
        if (StringUtils.isNotEmpty(disassemblerConfig)) {
            writer.writeString("disassembler", disassemblerConfig)
        }
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        setAddressWidth(BitWidth.withName(reader.readString("addressBitWidth")))
		setDataWidth(BitWidth.withName(reader.readString("dataBitWidth")))
        CompressedMemoryDump.read(memory, reader.readString("content"))
        disassemblerConfig = reader.readOptionalString("disassembler") ?: ""
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

    fun read(address: Int): Long = memory.read(address)

    fun write(address: Int, value: Long) {
        memory.write(address, value)
        disassembleCell(address)
    }

    private fun resetDisassembler() {
        disassembler.reset()
        resetDisassembly()
    }

    private fun resetDisassembly() {
        _disassemblyWidth = 0
        disassembly.clear()
    }

    private fun disassembleAll() {
        resetDisassembly()
        if (disassemblerConfig.isBlank()) {
            return
        }
        disassembler.operations(disassemblerConfig)
        memory.getNonZeroCells().forEach { disassembleCell(it.address) }
    }

    private fun disassembleCell(address: Int) {
        val value = BitOperation.longToHex(memory.read(address)).padStart(dataWidth.width / 4, '0')
        val d = disassembler.disassemble(value)
        disassembly[address] = d
        _disassemblyWidth = Math.max(_disassemblyWidth, disassembly[address]!!.length)
    }
}