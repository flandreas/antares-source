package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.draw.DrawableContainer
import ch.scorpion.jabbah.draw.container.DrawableContainerImpl
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.ComponentContainer
import ch.scorpion.jabbah.io.*

/**
 * A standard implementation of a [ComponentContainer].
 */
open class ComponentContainerImpl<T: Component> : DrawableContainerImpl<T>(), ComponentContainer<T> {

    override  var storableId: Int = 0

    /** Determines whether this [Storable] is currently being read from persistent store */
    protected var readingFromStore: Boolean = false

    override fun add(drawable: T, index: Int): DrawableContainer<T> {
        if (!contains(drawable)) {
            if (!readingFromStore) {
                drawable.id = getMaxId() + 1
            }
            return super.add(drawable, index)
        }
        return this
    }

    /** ---- [ComponentContainer] interface */

    override fun getWidthId(id: Int): T? {
        return getDrawables().filter { it.id == id }.firstOrNull()
    }

    /** ---- [Storable] interface */

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
        if (reference.name == "component") {
            readingFromStore = true
            add(reference.additionalInfo as T)
            readingFromStore = false
        }
    }

    override fun write(writer: StoreWriter) {
        writer.writeStorables("components", backToFrontIterator())
    }

    override fun read(reader: StoreReader) {
        clear()
        for (storable in reader.readStorables("components")) {
            reader.requestResolution(this, Reference(
                name = "component",
                additionalInfo = storable,
                resolveAfter = listOf(storable.storableId)
            ))
        }
    }

    override fun getStorableChildren(): Iterator<Storable> {
        return frontToBackIterator()
    }

    /** ---- [ComponentContainerImpl] */

    /** Returns the maximum of the identifications of all contained [Component]s.*/
    private fun getMaxId(): Int {
        if (drawablesCount == 0) {
            return 0
        }
        return getDrawables().maxBy { it.id }!!.id
    }
}