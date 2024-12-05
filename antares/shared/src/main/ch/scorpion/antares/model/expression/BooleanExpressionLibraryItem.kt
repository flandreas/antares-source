package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.graph.library.AbstractLibraryItem
import ch.scorpion.jabbah.graph.library.LibraryItem
import ch.scorpion.jabbah.graph.library.UndoableStateLibraryItem
import ch.scorpion.jabbah.io.*

data class OpenBooleanExpressionItemRequest(val item: BooleanExpressionLibraryItem)

class BooleanExpressionLibraryItem(
	initialName: TranslatableText = TranslatableText(),
	expressions: String = "",
	singleCharIdentifier: Boolean = BaseModule.properties.getBoolean(BooleanExpressionNotation.PROP_OMIT_AND)
) : AbstractLibraryItem(
	initialName,
	iconPath = "/img/expression.png"
), UndoableStateLibraryItem<BooleanExpressionStorable> {

	override var storable = BooleanExpressionStorable(initialName, expressions, singleCharIdentifier)
		private set

	override val activeIconPath: String get() = "/img/expression-active.png"

	override val isFixed: Boolean = false

	override fun open(eventBus: EventBus) {
		eventBus.post(OpenBooleanExpressionItemRequest(this))
	}

	override fun accept(visitor: HierarchyVisitor): Boolean = visitor.visit(this)

	/** ---- [LibraryItem] interface */

	override var name: Name
		get() = storable.name
		set(value) { storable.name = value }

	/** ---- [Storable] interface */

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		writer.writeStorable("expressions", storable)
	}

	override fun read(reader: StoreReader) {
		storable = reader.readStorable("expressions")
		if (reader.hasElement("name")) {
			// Backward compatibility: Name was stored here, but is now in BooleanExpressionStorable
			storable.name = Name.read("name", reader)
		}
	}

	override fun updateStorable(storable: BooleanExpressionStorable) {
		this.storable = storable
	}
}