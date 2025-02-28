package ch.scorpion.antares.model.truthtable

import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.graph.library.AbstractLibraryItem
import ch.scorpion.jabbah.graph.library.LibraryItem
import ch.scorpion.jabbah.graph.library.UndoableStateLibraryItem
import ch.scorpion.jabbah.io.*

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
		writer.writeStorable("truthTable", storable)
	}

	override fun read(reader: StoreReader) {
		storable = reader.readStorable("truthTable")
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }
}