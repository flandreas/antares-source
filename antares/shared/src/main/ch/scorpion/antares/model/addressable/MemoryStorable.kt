package ch.scorpion.antares.model.addressable

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.edit.model.text.description.Namable
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.edit.model.text.description.observableName
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.io.*

/** A [Memory] wrapped in a [Storable].*/
class MemoryStorable(
    initialName: String = "",
    addressWidth: BitWidth = BitWidth.BW_8,
    dataWidth: BitWidth = BitWidth.BW_8
) : AbstractStorable(), Namable, Addressable {

    /** ---- [Addressable] interface */

    override val memory = Memory()

    override var dataWidth: BitWidth = dataWidth

    override var addressWidth: BitWidth = addressWidth

    override val currentAddress: Int get() = 0

    override val maxAddress: Int get() = addressWidth.maxValue.toInt()

    override val data: ULong get() = 0UL

    override val disassemblyWidth: Int get() = 0

    override val isSelected: Boolean get() = false

    override val storesCells: Boolean get() = true

    override var dataSource: String? = null

    override fun addDataListener(listener: AddressableDataListener) {
        // Not needed?
    }

    override fun removeDataListener(listener: AddressableDataListener) {
        // Not needed?
    }

    override fun clear() {
        memory.clear()
    }

    override fun update() {
        // TODO
    }

    override fun dataAt(address: Int): ULong = memory.read(address)

    override fun setDataAt(address: Int, value: ULong, signalHandler: SignalHandler?) {
        memory.write(address, value)
    }

    override fun commentAt(address: Int): String? = memory.readComment(address)

    override fun setCommentAt(address: Int, value: String?, signalHandler: SignalHandler?) {
        memory.writeComment(address, value)
    }

    override fun disassemblyAt(address: Int): String = ""

    /** ---- [Namable] interface */

    override var name: Name by observableName(Name(initialName))

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        name.write("name", writer)
        writer.writeString("dataBitWidth", dataWidth.customName)
        writer.writeString("addressBitWidth", addressWidth.customName)
        writer.writeString("content", CompressedMemoryDump.write(memory, dataWidth))
    }

    override fun read(reader: StoreReader) {
        name = Name.read("name", reader)
        dataWidth = BitWidth.withName(reader.readString("dataBitWidth"))
        addressWidth = BitWidth.withName(reader.readString("addressBitWidth"))
        CompressedMemoryDump.read(memory, reader.readString("content"))
    }

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {}
}