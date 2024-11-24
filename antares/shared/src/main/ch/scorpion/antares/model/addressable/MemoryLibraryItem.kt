package ch.scorpion.antares.model.addressable

import ch.scorpion.antares.view.addressable.MemoryStorable
import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.graph.library.LibraryItem
import ch.scorpion.jabbah.graph.library.AbstractLibraryItem
import ch.scorpion.jabbah.graph.library.UndoableStateLibraryItem
import ch.scorpion.jabbah.io.Reference
import ch.scorpion.jabbah.io.ReferenceResolver
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A request to initiate opening the [MemoryStorable] of a [MemoryLibraryItem].
 * This is used to establish the [MemorySavable] as new application [Savable].
 */
data class OpenMemoryLibraryItemRequest(val item: MemoryLibraryItem)

/**
 * A request to show the [MemoryStorable] of a [MemoryLibraryItem].
 * This is used after the [MemorySavable] has been established as new application [Savable],
 * and the second request is needed to avoid recursive handling of the first request.
 */
data class ShowMemoryLibraryItemRequest(val item: MemoryLibraryItem)

// TODO: Icon
class MemoryLibraryItem(
    memoryStorable: MemoryStorable = MemoryStorable()
) : AbstractLibraryItem(
    TranslatableText(Translations.getString("library.element.memory.name")),
    iconPath = "/img/truth-table.png"
), UndoableStateLibraryItem<MemoryStorable> {

    var memoryStorable: MemoryStorable = memoryStorable
        private set

    /** ---- [LibraryItem] interface */

    override var name: Name
        get() = memoryStorable.name
        set(value) { memoryStorable.name = value }

    override val activeIconPath: String get() = "/img/truth-table-active.png"

    override val isFixed: Boolean get() = false

    override fun accept(visitor: HierarchyVisitor): Boolean = visitor.visit(this)

    override fun open(eventBus: EventBus) {
        eventBus.post(OpenMemoryLibraryItemRequest(this))
    }


    /** ---- [UndoableStateLibraryItem] */

    override fun updateStorable(storable: MemoryStorable) {
        memoryStorable = storable
    }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        writer.writeStorable("memory", memoryStorable)
    }

    override fun read(reader: StoreReader) {
        memoryStorable = reader.readStorable("memory")
    }

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {}
}