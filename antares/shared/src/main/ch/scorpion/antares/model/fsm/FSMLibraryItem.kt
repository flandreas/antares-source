package ch.scorpion.antares.model.fsm

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

data class OpenFSMLibraryItemRequest(val item: FSMLibraryItem)

class FSMLibraryItem(
    initialName: TranslatableText = TranslatableText(Translations.getString("antares.fsm.initialName")),
) : AbstractLibraryItem(
    initialName,
    // TODO Icon Janis
    iconPath = "/img/expression.png"
), UndoableStateLibraryItem<FSMDrawing> {

    var uuid: UUID = System.createUUID()
        private set

    /** ---- [LibraryItem] interface */

    override var storable: FSMDrawing = FSMDrawing(initialName)
        private set

    // TODO Icon Janis
    override val activeIconPath: String get() = "/img/expression-active.png"

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