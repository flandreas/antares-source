package io.antarescircuit.jabbah.graph.model

import io.antarescircuit.jabbah.base.EnumProperty
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.io.AbstractStorable
import io.antarescircuit.jabbah.io.Reference
import io.antarescircuit.jabbah.io.ReferenceResolver
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

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