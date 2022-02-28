package ch.scorpion.antares.model.truthtable

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.graph.library.LibraryItem
import ch.scorpion.jabbah.graph.library.AbstractLibraryItem
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.io.Reference
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.ReferenceResolver
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Represents a request to initiate opening the [TruthTable] of a [TruthTableLibraryItem].
 * This is used to establish the [TruthTableSavable] as new application [Savable].
 */
data class OpenTruthTableItemRequest(val item: TruthTableLibraryItem)

/**
 * Represents a request to show the [TruthTable] of a [TruthTableLibraryItem].
 * This is used after the [TruthTableSavable] has been established as new application [Savable],
 * and the second request is needed to avoid recursive handling of the first request.
 */
data class ShowTruthTableItemRequest(val item: TruthTableLibraryItem)

/**
 * A [LibraryItem] that contains a [TruthTable].
 */
class TruthTableLibraryItem(
	truthTable: TruthTable = TruthTable()
) : AbstractLibraryItem(TranslatableText(Translations.getString("library.element.truthTable.name")), iconPath = "/img/openInPopup-20.png") {

	var truthTable: TruthTable = truthTable
		private set

	/** ---- [LibraryItem] interface */

	override var name: Name
		get() = truthTable.name
		set(value) { truthTable.name = value }

	override val isFixed: Boolean get() = false

	override fun open(eventBus: EventBus) {
		eventBus.post(OpenTruthTableItemRequest(this))
	}

	override fun accept(visitor: HierarchyVisitor): Boolean = visitor.visit(this)

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		writer.writeStorable("truthTable", truthTable)
	}

	override fun read(reader: StoreReader) {
		truthTable = reader.readStorable("truthTable")
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }
}