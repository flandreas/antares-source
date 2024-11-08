package ch.scorpion.jabbah.io

class StringStorable(
    var content: String = ""
) : AbstractStorable() {

    override val isReferencable: Boolean get() = false

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {}

    override fun write(writer: StoreWriter) {
        writer.writeString("content", content)
    }

    override fun read(reader: StoreReader) {
        content = reader.readString("content")
    }
}