package ch.scorpion.jabbah.edit.model.image

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.draw.graphics.ImageType
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.io.*

class ImageIdentification(
    uuid: UUID = UUID("undefined"),
    imageType: ImageType = ImageType.SVG,
    name: Name = Name(TranslatableText())
): AbstractStorable() {

    var uuid: UUID = uuid
        private set

    var imageType: ImageType = imageType
        private set

    var name: Name = name
        private set

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
}