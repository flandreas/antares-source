package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.io.AbstractStorable
import ch.scorpion.jabbah.io.Reference
import ch.scorpion.jabbah.io.ReferenceResolver
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

enum class DocumentType(override val customName: String) : EnumProperty<DocumentType> {
    Markdown("markdown");

    companion object {
        const val BASE_KEY = "element.property.documentType"

        fun withName(customName: String): DocumentType =
            DocumentType.entries.find { it.customName == customName }
                ?: throw IllegalArgumentException("unknown DocumentType $customName)")
    }

    override fun toString(): String =
        when (this) {
            Markdown -> Translations.getString("$BASE_KEY.markdown")
        }
}

class Document(
    type: DocumentType = DocumentType.Markdown,
    text: String = ""
) : AbstractStorable() {

    var type: DocumentType = type
        private set

    var text: String = text
        private set

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

    override fun write(writer: StoreWriter) {
        writer.writeString("type", type.customName)
        writer.writeText("text", text)
    }

    override fun read(reader: StoreReader) {
        type = DocumentType.withName(reader.readString("type"))
        text = reader.readText("text")
    }

    fun withText(text: String): Document = Document(type, text)
}