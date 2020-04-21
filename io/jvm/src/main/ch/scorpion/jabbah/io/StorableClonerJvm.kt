package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.base.logger
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * A [StorableCloner] implementation for the JVM target.
 */
actual object StorableCloner {

	private val LOG by logger(StorableCloner::class)

	actual fun serialize(storable: Storable): String {
		return serializeImpl(storable, GlobalIdentityCreator())
	}

	actual fun deserialize(s: String): Storable {
		return deserializeImpl(s, IOModule.storableCreator, ReferenceResolverImpl())
	}

	actual fun <T: Storable> clone(storable: T): T {
		return clone(storable, GlobalIdentityCreator(), IOModule.storableCreator, ReferenceResolverImpl())
	}

	actual fun <T: Storable> clonePreservingIdentities(storable: T, storableCreator: StorableCreator): T {
		return clone(storable, GlobalIdentityReflector(), storableCreator, ReferenceResolverImpl())
	}

	actual fun <T: Storable> cloneUsingCreator(storable: T, storableCreator: StorableCreator): T {
		return clone(storable, GlobalIdentityCreator(), storableCreator, ReferenceResolverImpl())
	}

	actual fun <T: Storable> clone(
		storable: T,
		identityProvider: GlobalIdentityProvider,
		storableCreator: StorableCreator,
		referenceResolver: ReferenceResolver
	): T {
		try {
			val data = serializeImpl(storable, identityProvider)
			LOG.debug(data)
			return deserializeImpl(data, storableCreator, referenceResolver)
		} catch (x: Throwable) {
			LOG.error("Error while cloning Storable: ${x.message}")
			throw x
		}
	}

	private fun <T: Storable> serializeImpl(storable: T, identityProvider: GlobalIdentityProvider): String {
		val buffer = ByteArrayOutputStream()
		val xmlWriter = ElectricXmlWriter(buffer)
		val writer = StoreXmlWriter(xmlWriter, IOModule.typeMap, identityProvider)
		writer.writeStorable(storable)
		return buffer.toString()
	}

	private fun <T: Storable> deserializeImpl(s: String, storableCreator: StorableCreator, referenceResolver: ReferenceResolver): T {
		val xmlReader = ElectricXmlReader(ByteArrayInputStream(s.toByteArray()))
		val reader = StoreXmlReader(xmlReader, IOModule.typeMap, storableCreator, referenceResolver)
		return reader.readStorable() as T
	}
}