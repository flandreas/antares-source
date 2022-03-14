package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.io.*

class BooleanExpressionStorable(
	initialExpression: String = "",
	singleCharIdentifier: Boolean = false
) : AbstractStorable() {

	var expressions: String = initialExpression
		private set

	var singleCharIdentifier: Boolean = singleCharIdentifier

	/** --- [Storable] interface */

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {}

	override fun write(writer: StoreWriter) {
		writer.writeString("expressions", expressions)
		if (singleCharIdentifier) {
			writer.writeBoolean("singleCharId", singleCharIdentifier)
		}
	}

	override fun read(reader: StoreReader) {
		expressions = reader.readString("expressions")
		if (reader.hasAttribute("singleCharId")) {
			singleCharIdentifier = reader.readBoolean("singleCharId")
		}
	}
}

