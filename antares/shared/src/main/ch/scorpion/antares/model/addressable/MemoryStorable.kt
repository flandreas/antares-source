package ch.scorpion.antares.model.addressable

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.edit.model.text.description.Namable
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.edit.model.text.description.observableName
import ch.scorpion.jabbah.io.*

/** A [Memory] wrapped in a [Storable].*/
class MemoryStorable(
    initialName: String = "",
    dataWidth: BitWidth = BitWidth.BW_8
) : AbstractStorable(), Namable {

    val memory = Memory()

    var dataWidth: BitWidth = dataWidth

    /** ---- [Namable] interface */

    override var name: Name by observableName(Name(initialName))

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        name.write("name", writer)
        writer.writeString("dataBitWidth", dataWidth.customName)
        writer.writeString("content", CompressedMemoryDump.write(memory, dataWidth))
    }

    override fun read(reader: StoreReader) {
        name = Name.read("name", reader)
        dataWidth = BitWidth.withName(reader.readString("dataBitWidth"))
        CompressedMemoryDump.read(memory, reader.readString("content"))
    }

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {}
}