package ch.scorpion.jabbah.io

class TestStorable(
	private val resolver: (Int) -> Unit = {}
) : Storable {

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		resolver(referenceResolver.getGlobalId(this))
	}

	override fun write(writer: StoreWriter) { }

	override fun read(reader: StoreReader) { }
}