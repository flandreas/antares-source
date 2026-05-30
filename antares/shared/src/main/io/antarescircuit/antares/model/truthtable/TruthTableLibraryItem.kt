package io.antarescircuit.antares.model.truthtable

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
 * Represents a request to initiate opening the [TruthTable] of a [TruthTableLibraryItem].
 * This is used to establish the [TruthTableSavable] as new application [Savable].
 */
data class OpenTruthTableItemRequest(val item: TruthTableLibraryItem)

/**
 * A [LibraryItem] that contains a [TruthTable].
 */
class TruthTableLibraryItem(
	truthTable: TruthTable = TruthTable()
) : AbstractLibraryItem(
	TranslatableText(Translations.getString("library.element.truthTable.name")),
	iconPath = "/img/truth-table.png"
), UndoableStateLibraryItem<TruthTable> {

	var uuid: UUID = System.createUUID()
		private set

	override var storable: TruthTable = truthTable
		private set

	override val activeIconPath: String get() = "/img/truth-table-active.png"

	/** ---- [LibraryItem] interface */

	override var name: Name
		get() = storable.name
		set(value) { storable.name = value }

	override val isFixed: Boolean get() = false

	override fun open(eventBus: EventBus) {
		eventBus.post(OpenTruthTableItemRequest(this))
	}

	override fun accept(visitor: HierarchyVisitor): Boolean = visitor.visit(this)

	/** ---- [UndoableStateLibraryItem] */

	override fun updateStorable(storable: TruthTable) {
		this.storable = storable
	}

	override fun createSavable(): Savable = TruthTableSavable(this)

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		writer.writeString("uuid", uuid.id)
		writer.writeStorable("truthTable", storable)
	}

	override fun read(reader: StoreReader) {
		if (reader.hasAttribute("uuid")) {
			// Backward compatability: Former version didn't have a UUID
			uuid = UUID(reader.readString("uuid"))
		}
		storable = reader.readStorable("truthTable")
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }
}