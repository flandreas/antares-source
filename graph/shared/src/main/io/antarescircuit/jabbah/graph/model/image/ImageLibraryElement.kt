package io.antarescircuit.jabbah.graph.model.image

import io.antarescircuit.jabbah.draw.graphics.Image
import io.antarescircuit.jabbah.edit.model.image.ImageComponent
import io.antarescircuit.jabbah.edit.model.image.ImageIdentification
import io.antarescircuit.jabbah.app.Savable
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.graph.library.LibraryElement
import io.antarescircuit.jabbah.graph.library.LibraryItem
import io.antarescircuit.jabbah.graph.library.UndoableStateLibraryItem
import io.antarescircuit.jabbah.graph.model.GenericGraphType
import io.antarescircuit.jabbah.graph.model.GraphElement
import io.antarescircuit.jabbah.graph.model.GraphType
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphElementViewWrapper
import io.antarescircuit.jabbah.io.*

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