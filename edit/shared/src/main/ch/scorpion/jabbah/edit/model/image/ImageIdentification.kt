package ch.scorpion.jabbah.edit.model.image

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.draw.graphics.ImageType
import ch.scorpion.jabbah.base.Bean
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Namable
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.edit.model.text.description.observableName
import ch.scorpion.jabbah.io.*

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