package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.base.io.ByteArrayOutputStreamWithBufferAccess
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

/**
 * A [StorableCloner] implementation for the JVM target.
 */
actual object StorableCloner : AbstractStorableCloner() {

	actual fun serialize(storable: Storable): String =
		serializeImpl(storable, GlobalIdentityCreator()).data.toString(StandardCharsets.UTF_8)

	actual fun deserialize(s: String): Storable =
		deserializeImpl(Buffer(s.toByteArray(StandardCharsets.UTF_8), s.length), IOModule.storableCreator, ReferenceResolverImpl())

	override fun <T : Storable> serializeImpl(storable: T, identityProvider: GlobalIdentityProvider): Buffer {
		val out = ByteArrayOutputStreamWithBufferAccess(256)
		val xmlWriter = ElectricXmlWriter(out)
		val writer = StoreXmlWriter(xmlWriter, IOModule.typeMap, identityProvider)
		writer.writeStorable(storable)
		return Buffer(out.buffer, out.size())
	}

	override fun <T : Storable> deserializeImpl(
		s: Buffer,
		storableCreator: StorableCreator,
		referenceResolver: ReferenceResolver
	): T {
		val xmlReader = ElectricXmlReader(ByteArrayInputStream(s.data, 0, s.length))
		val reader = StoreXmlReader(xmlReader, IOModule.typeMap, storableCreator, referenceResolver)
		return reader.readStorable() as T
	}
}