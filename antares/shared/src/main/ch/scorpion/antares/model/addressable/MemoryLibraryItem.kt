package ch.scorpion.antares.model.addressable

import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.graph.library.AbstractLibraryItem
import ch.scorpion.jabbah.graph.library.LibraryItem
import ch.scorpion.jabbah.graph.library.UndoableStateLibraryItem
import ch.scorpion.jabbah.io.*

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