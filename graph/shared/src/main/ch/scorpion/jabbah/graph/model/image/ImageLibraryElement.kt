package ch.scorpion.jabbah.graph.model.image

import ch.scorpion.jabbah.draw.graphics.Image
import ch.scorpion.jabbah.edit.model.image.ImageComponent
import ch.scorpion.jabbah.edit.model.image.ImageIdentification
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.library.LibraryItem
import ch.scorpion.jabbah.graph.library.UndoableStateLibraryItem
import ch.scorpion.jabbah.graph.model.GenericGraphType
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.GraphType
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphElementViewWrapper
import ch.scorpion.jabbah.io.*

/**
 * A request to initiate opening the [ImageIdentification] of a [ImageLibraryElement].
 * This is used to establish the [ImageIdentificationSavable] as new application [Savable].
 */
data class OpenImageLibraryElementRequest(val element: ImageLibraryElement)

/**
 * A [LibraryItem] referencing an image resource.
 */
class ImageLibraryElement(
    imageId: ImageIdentification = ImageIdentification()
) : LibraryElement(
    imageId.name.translation,
    iconPath = "/img/image.png"
), UndoableStateLibraryItem<ImageIdentification> {

    /** Lazily initialized instance of the referenced [Image]. */
    var image: Image? = null

    /** ---- [LibraryItem] interface */

    override var name: Name
        get() = storable.name
        set(value) { storable.name = value }

    /** ---- [LibraryElement] */

    override val isFixed: Boolean get() = false

    override val graphType: GraphType get() = GenericGraphType

    override fun open(eventBus: EventBus) {
        eventBus.post(OpenImageLibraryElementRequest(this))
    }

    override fun <T : GraphElement> getNewInstance(): GraphElementView<T> {
        library!!.libraryService.getImage(library!!, this)
        return GraphElementViewWrapper(ImageComponent(storable.uuid)) as GraphElementView<T>
    }

    /** ---- [UndoableStateLibraryItem] */

    override var storable: ImageIdentification = imageId
        private set

    override fun updateStorable(storable: ImageIdentification) {
        this.storable = storable
    }

    override fun createSavable(): Savable = ImageIdentificationSavable(this)

    /** ---- [Storable] */

    override fun write(writer: StoreWriter) {
        writer.writeStorable("imageId", storable)
    }

    override fun read(reader: StoreReader) {
        storable = reader.readStorable("imageId")
    }

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {}
}