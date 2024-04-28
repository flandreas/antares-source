package ch.scorpion.jabbah.edit.model.image

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.draw.graphics.Image
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.model.text.description.Name

/**
 * Defines a repository of [Image]s identified with an [UUID].
 *
 * [Component]s that display an [Image] can only keep the [UUID] and get the [Image] from the
 * [ImageRepository] when needed.
 *
 * An [ImageRepository] is responsible for loading an [Image] from external storage, and it might
 * cache [Image]s once they are loaded.
 */
interface ImageRepository {

    fun getImage(uuid: UUID): ImageData?

    fun getAllImageIds(): List<ImageIdentification>
}


data class ImageData(
    val image: Image,
    val name: Name
)