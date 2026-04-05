package io.antarescircuit.antares.model.fsm

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

data class OpenFSMLibraryItemRequest(val item: FSMLibraryItem)

class FSMLibraryItem(
    initialName: TranslatableText = TranslatableText(Translations.getString("antares.fsm.initialName")),
) : AbstractLibraryItem(
    initialName,
    iconPath = "/img/fsm.png"
), UndoableStateLibraryItem<FSMDrawing> {

    var uuid: UUID = System.createUUID()
        private set

    /** ---- [LibraryItem] interface */

    override var storable: FSMDrawing = FSMDrawing(initialName)
        private set

    override val activeIconPath: String get() = "/img/fsm-active.png"

    override val isFixed: Boolean = false

    override fun open(eventBus: EventBus) {
        eventBus.post(OpenFSMLibraryItemRequest(this))
    }

    override fun accept(visitor: HierarchyVisitor): Boolean = visitor.visit(this)

    override var name: Name
        get() = storable.name
        set(value) { storable.name = value }

    /** ---- [Storable] interface */

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

    override fun write(writer: StoreWriter) {
        writer.writeStorable("fsm", storable)
        writer.writeString("uuid", uuid.id)
    }

    override fun read(reader: StoreReader) {
        storable = reader.readStorable("fsm")
        uuid = UUID(reader.readString("uuid"))
    }

    /** ---- [UndoableStateLibraryItem] */

    override fun updateStorable(storable: FSMDrawing) {
        this.storable = storable
    }

    override fun createSavable(): Savable = FSMSavable(this)
}