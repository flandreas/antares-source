package ch.scorpion.jabbah.graph.model.image

import ch.scorpion.jabbah.draw.graphics.Image
import ch.scorpion.jabbah.edit.model.image.ImageComponent
import ch.scorpion.jabbah.edit.model.image.ImageIdentification
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.library.LibraryItem
import ch.scorpion.jabbah.graph.model.GenericGraphType
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.GraphType
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphElementViewWrapper
import ch.scorpion.jabbah.io.*

/**
 * A [LibraryItem] referencing an image resource.
 */
class ImageLibraryElement(
    imageId: ImageIdentification = ImageIdentification()
) : LibraryElement(imageId.name.translation, iconPath = "/img/image.png") {

    var imageId: ImageIdentification = imageId
        private set

    /** Lazily initialized instance of the referenced [Image]. */
    var image: Image? = null

    /** ---- [LibraryElement] */

    override val isFixed: Boolean get() = false

    override val graphType: GraphType get() = GenericGraphType

    override fun <T : GraphElement> getNewInstance(): GraphElementView<T> {
        library!!.libraryService.getImage(library!!, this)
        return GraphElementViewWrapper(ImageComponent(imageId.uuid)) as GraphElementView<T>
    }

    /** ---- [Storable] */

    override fun write(writer: StoreWriter) {
        writer.writeStorable("imageId", imageId)
    }

    override fun read(reader: StoreReader) {
        imageId = reader.readStorable("imageId")
        name = imageId.name
    }

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {}
}