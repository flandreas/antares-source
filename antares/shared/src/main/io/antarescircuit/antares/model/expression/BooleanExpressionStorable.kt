package io.antarescircuit.antares.model.expression

import io.antarescircuit.jabbah.base.Bean
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.description.Namable
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.edit.model.text.description.observableName
import io.antarescircuit.jabbah.io.*

class BooleanExpressionStorable(
	initialName: TranslatableText = TranslatableText(),
	initialExpression: String = "",
	singleCharIdentifier: Boolean = false
) : AbstractStorable(), Namable, Bean {

	var expressions: String = initialExpression

	var singleCharIdentifier: Boolean = singleCharIdentifier

	/** ---  [Any] */

	override fun toString(): String = name.getTranslation()

	/** ---- [Namable] interface */

	override var name: Name by observableName(Name(initialName))

	/** --- [Storable] interface */

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {}

	override fun write(writer: StoreWriter) {
		name.write("name", writer)
		writer.writeString("expressions", expressions)
		if (singleCharIdentifier) {
			writer.writeBoolean("singleCharId", singleCharIdentifier)
		}
	}

	override fun read(reader: StoreReader) {
		if (reader.hasElement("name")) {
			// Backward compatibility: Name was previously stores in LibraryItem
			name = Name.read("name", reader)
		}
		expressions = reader.readString("expressions")
		if (reader.hasAttribute("singleCharId")) {
			singleCharIdentifier = reader.readBoolean("singleCharId")
		}
	}
}

