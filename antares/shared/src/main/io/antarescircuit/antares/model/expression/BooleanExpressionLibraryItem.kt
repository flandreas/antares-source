package io.antarescircuit.antares.model.expression

import io.antarescircuit.jabbah.app.Savable
import io.antarescircuit.jabbah.base.HierarchyVisitor
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.graph.library.AbstractLibraryItem
import io.antarescircuit.jabbah.graph.library.LibraryItem
import io.antarescircuit.jabbah.graph.library.UndoableStateLibraryItem
import io.antarescircuit.jabbah.io.*

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

	/** ---- [UndoableStateLibraryItem] */

	override fun updateStorable(storable: BooleanExpressionStorable) {
		this.storable = storable
	}

	override fun createSavable(): Savable = BooleanExpressionSavable(this)
}