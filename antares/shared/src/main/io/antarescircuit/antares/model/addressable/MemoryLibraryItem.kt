package io.antarescircuit.antares.model.addressable

import io.antarescircuit.jabbah.app.Savable
import io.antarescircuit.jabbah.base.HierarchyVisitor
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.graph.library.AbstractLibraryItem
import io.antarescircuit.jabbah.graph.library.LibraryItem
import io.antarescircuit.jabbah.graph.library.UndoableStateLibraryItem
import io.antarescircuit.jabbah.io.*

/**
 * A request to initiate opening the [MemoryStorable] of a [MemoryLibraryItem].
 * This is used to establish the [MemorySavable] as new application [Savable].
 */
data class OpenMemoryLibraryItemRequest(val item: MemoryLibraryItem)

class MemoryLibraryItem(
    memoryStorable: MemoryStorable = MemoryStorable()
) : AbstractLibraryItem(
    TranslatableText(Translations.getString("library.element.memory.name")),
    iconPath = "/img/memory-storable.png"
), UndoableStateLibraryItem<MemoryStorable> {

    var uuid: UUID = System.createUUID()
        private set

    override var storable: MemoryStorable = memoryStorable
        private set

    /** ---- [LibraryItem] interface */

    override var name: Name
        get() = storable.name
        set(value) { storable.name = value }

    override val activeIconPath: String get() = "/img/memory-storable-active.png"

    override val isFixed: Boolean get() = false

    override fun accept(visitor: HierarchyVisitor): Boolean = visitor.visit(this)

    override fun open(eventBus: EventBus) {
        eventBus.post(OpenMemoryLibraryItemRequest(this))
    }

    /** ---- [UndoableStateLibraryItem] */

    override fun updateStorable(storable: MemoryStorable) {
        this.storable = storable
    }

    override fun createSavable(): Savable = MemorySavable(this)

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        writer.writeString("uuid", uuid.toString())
        writer.writeStorable("memory", storable)
    }

    override fun read(reader: StoreReader) {
        uuid = UUID(reader.readString("uuid"))
        storable = reader.readStorable("memory")
    }

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {}
}