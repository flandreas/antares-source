package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.graph.library.AbstractLibraryItem
import ch.scorpion.jabbah.graph.library.UndoableStateLibraryItem
import ch.scorpion.jabbah.io.*

data class OpenBooleanExpressionItemRequest(val item: BooleanExpressionLibraryItem)
data class ShowBooleanExpressionItemRequest(val item: BooleanExpressionLibraryItem)

class BooleanExpressionLibraryItem(
	initialName: TranslatableText = TranslatableText(),
	expressions: String = "",
	singleCharIdentifier: Boolean = BaseModule.properties.getBoolean(BooleanExpressionNotation.PROP_OMIT_AND)
) : AbstractLibraryItem(
	initialName,
	iconPath = "/img/expression.png"
), UndoableStateLibraryItem<BooleanExpressionStorable> {

	var expressions = BooleanExpressionStorable(expressions, singleCharIdentifier)

	override val activeIconPath: String get() = "/img/expression-active.png"

	override val isFixed: Boolean = false

	override fun open(eventBus: EventBus) {
		eventBus.post(OpenBooleanExpressionItemRequest(this))
	}

	override fun accept(visitor: HierarchyVisitor): Boolean = visitor.visit(this)

	/** ---- [Storable] interface */

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		name.write("name", writer)
		writer.writeStorable("expressions", expressions)
	}

	override fun read(reader: StoreReader) {
		name = Name.read("name", reader)
		expressions = reader.readStorable("expressions")
	}

	override fun updateStorable(storable: BooleanExpressionStorable) {
		expressions = storable
	}
}