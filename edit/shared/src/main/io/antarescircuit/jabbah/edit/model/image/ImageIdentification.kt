package io.antarescircuit.jabbah.edit.model.image

import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.draw.graphics.ImageType
import io.antarescircuit.jabbah.base.Bean
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.description.Namable
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.edit.model.text.description.observableName
import io.antarescircuit.jabbah.io.*

class ImageIdentification(
    uuid: UUID = UUID("undefined"),
    imageType: ImageType = ImageType.SVG,
    name: Name = Name(TranslatableText())
): AbstractStorable(), Bean, Namable {

    var uuid: UUID = uuid
        private set

    var imageType: ImageType = imageType
        private set

    override var name: Name by observableName(name)

    /** ---- [Storable] */

    override fun read(reader: StoreReader) {
        uuid = UUID(reader.readString("uuid"))
        imageType = ImageType.withName(reader.readString("type"))
        name = Name.read("name", reader)
    }

    override fun write(writer: StoreWriter) {
        writer.writeString("uuid", uuid.toString())
        writer.writeString("type", imageType.customName)
        name.write("name", writer)
    }

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
        // No references
    }

    /** ---- [Any] */

    override fun toString(): String = name.value

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ImageIdentification

        if (uuid != other.uuid) return false
        if (imageType != other.imageType) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + imageType.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}