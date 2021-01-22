package ch.scorpion.jabbah.io

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * A [StorableCloner] implementation for the JVM target.
 */
actual object StorableCloner : AbstractStorableCloner() {

	override fun <T: Storable> serializeImpl(storable: T, identityProvider: GlobalIdentityProvider): String {
		val buffer = ByteArrayOutputStream()
		val xmlWriter = ElectricXmlWriter(buffer)
		val writer = StoreXmlWriter(xmlWriter, IOModule.typeMap, identityProvider)
		writer.writeStorable(storable)
		return buffer.toString()
	}

	override fun <T: Storable> deserializeImpl(s: String, storableCreator: StorableCreator, referenceResolver: ReferenceResolver): T {
		val xmlReader = ElectricXmlReader(ByteArrayInputStream(s.toByteArray()))
		val reader = StoreXmlReader(xmlReader, IOModule.typeMap, storableCreator, referenceResolver)
		return reader.readStorable() as T
	}
}