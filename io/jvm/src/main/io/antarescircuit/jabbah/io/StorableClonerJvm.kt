package io.antarescircuit.jabbah.io

import io.antarescircuit.jabbah.base.io.ByteArrayOutputStreamWithBufferAccess
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

/**
 * A [StorableCloner] implementation for the JVM target.
 */
actual object StorableCloner : AbstractStorableCloner() {

	actual fun serialize(storable: Storable): String =
		serializeImpl(storable, GlobalIdentityCreator()).data.toString(StandardCharsets.UTF_8)

	actual fun deserialize(s: String): Storable {
		val array = s.toByteArray(StandardCharsets.UTF_8)
		return deserializeImpl(Buffer(array, array.size), ReferenceResolverImpl())
	}

	override fun <T : Storable> serializeImpl(storable: T, identityProvider: GlobalIdentityProvider): Buffer {
		val out = ByteArrayOutputStreamWithBufferAccess(256)
		val xmlWriter = ElectricXmlWriter(out)
		val writer = StoreXmlWriter(xmlWriter, IOModule.typeMap, identityProvider)
		writer.writeStorable(storable)
		return Buffer(out.buffer, out.size())
	}

	override fun <T : Storable> deserializeImpl(
		s: Buffer,
		referenceResolver: ReferenceResolver
	): T {
		val xmlReader = ElectricXmlReader(ByteArrayInputStream(s.data, 0, s.length))
		val reader = StoreXmlReader(xmlReader, IOModule.typeMap, referenceResolver)
		return reader.readStorable() as T
	}
}