package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.io.*

class BooleanExpressionStorable(
	initialExpression: String = ""
) : AbstractStorable() {

	var expressions: String = initialExpression
		private set

	/** --- [Storable] interface */

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {}

	override fun write(writer: StoreWriter) {
		writer.writeString("expressions", expressions)
	}

	override fun read(reader: StoreReader) {
		expressions = reader.readString("expressions")
	}
}

