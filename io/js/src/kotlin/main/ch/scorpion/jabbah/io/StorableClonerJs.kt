package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.io.module.DomXmlWriter

/**
 * A [StorableCloner] implementation for the JavaScript platform.
 */
actual object StorableCloner : AbstractStorableCloner() {

	actual fun serialize(storable: Storable): String =
		serializeImpl(storable, GlobalIdentityCreator()).data.toString()

	actual fun deserialize(s: String): Storable {
		val byteArray = s.encodeToByteArray()
		return deserializeImpl(Buffer(byteArray, byteArray.size), IOModule.storableCreator, ReferenceResolverImpl())
	}

	override fun <T : Storable> serializeImpl(storable: T, identityProvider: GlobalIdentityProvider): Buffer {
		var s = ""
		val xmlWriter = DomXmlWriter { s = it }
		val writer = StoreXmlWriter(xmlWriter, IOModule.typeMap, identityProvider)
		writer.writeStorable(storable)
		val byteArray = s.encodeToByteArray()
		return Buffer(byteArray, byteArray.size)
	}

	override fun <T : Storable> deserializeImpl(
		s: Buffer,
		storableCreator: StorableCreator,
		referenceResolver: ReferenceResolver
	): T {
		val xmlReader = DomXmlReader(s.data.toString())
		val reader = StoreXmlReader(xmlReader, IOModule.typeMap, storableCreator, referenceResolver)
		return reader.readStorable() as T
	}
}