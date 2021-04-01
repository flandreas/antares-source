package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.base.collection.EmptyIterator

class TestStorable(
	private val resolver: (Int) -> Unit = {}
) : Storable {

	override var storableId: Int = 0

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		resolver(storableId)
	}

	override fun write(writer: StoreWriter) {
		// empty
	}

	override fun read(reader: StoreReader) {
		// empty
	}

	override fun getStorableChildren(): Iterator<Storable> {
		return EmptyIterator()
	}
}