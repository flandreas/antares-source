package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.io.module.DomXmlWriter

/**
 * A [StorableCloner] implementation for the JavaScript platform.
 */
actual object StorableCloner : AbstractStorableCloner() {

	override fun <T: Storable> serializeImpl(storable: T, identityProvider: GlobalIdentityProvider): String {
		var buffer = ""
		val xmlWriter = DomXmlWriter { buffer = it }
		val writer = StoreXmlWriter(xmlWriter, IOModule.typeMap, identityProvider)
		writer.writeStorable(storable)
		return buffer
	}

	override fun <T: Storable> deserializeImpl(s: String, storableCreator: StorableCreator, referenceResolver: ReferenceResolver): T {
		val xmlReader = DomXmlReader(s)
		val reader = StoreXmlReader(xmlReader, IOModule.typeMap, storableCreator, referenceResolver)
		return reader.readStorable() as T
	}
}