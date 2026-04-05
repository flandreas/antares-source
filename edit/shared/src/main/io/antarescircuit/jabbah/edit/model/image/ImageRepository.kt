package io.antarescircuit.jabbah.edit.model.image

import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.draw.graphics.Image
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.model.text.description.Name

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

    /** Returns all [ImageIdentification]s sorted ascending by name. */
    fun getAllImageIds(): List<ImageIdentification>
}

data class ImageData(
    val image: Image,
    val name: Name
)