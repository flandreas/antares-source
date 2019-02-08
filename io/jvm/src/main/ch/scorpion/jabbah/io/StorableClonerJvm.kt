package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.base.logger
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * A [StorableCloner] implementation for the JVM target.
 */
class StorableClonerJvm(val typeMap: TypeMap = IOModule.typeMap) : StorableCloner {

	companion object {
		private val LOG by logger(StorableClonerJvm::class)
	}

	/** ---- [StorableCloner] interface */

	override fun serialize(storable: Storable): String {
		return serializeImpl(storable, GlobalIdentityCreator())
	}

	override fun deserialize(s: String): Storable {
		return deserializeImpl(s, IOModule.storableCreator, ReferenceResolverImpl())
	}

	override fun clone(storable: Storable): Storable {
		return clone(storable, GlobalIdentityCreator(), IOModule.storableCreator, ReferenceResolverImpl())
	}

	override fun clonePreservingIdentities(storable: Storable, storableCreator: StorableCreator): Storable {
		return clone(storable, GlobalIdentityReflector(), storableCreator, ReferenceResolverImpl())
	}

	override fun cloneUsingCreator(storable: Storable, storableCreator: StorableCreator): Storable {
		return clone(storable, GlobalIdentityCreator(), storableCreator, ReferenceResolverImpl())
	}

	override fun clone(
		storable: Storable,
		identityProvider: GlobalIdentityProvider,
		storableCreator: StorableCreator,
		referenceResolver: ReferenceResolver
	): Storable {
		try {
			val data = serializeImpl(storable, identityProvider)
			LOG.debug(data)
			return deserializeImpl(data, storableCreator, referenceResolver)
		} catch (x: Throwable) {
			LOG.error("Error while cloning Storable: ${x.message}")
			throw x
		}
	}

	/** ---- [StorableClonerJvm] */

	private fun serializeImpl(storable: Storable, identityProvider: GlobalIdentityProvider): String {
		val buffer = ByteArrayOutputStream()
		val xmlWriter = ElectricXmlWriter(buffer)
		val writer = StoreXmlWriter(xmlWriter, typeMap, identityProvider)
		writer.writeStorable(storable)
		return buffer.toString()
	}

	private fun deserializeImpl(s: String, storableCreator: StorableCreator, referenceResolver: ReferenceResolver): Storable {
		val xmlReader = ElectricXmlReader(ByteArrayInputStream(s.toByteArray()))
		val reader = StoreXmlReader(xmlReader, typeMap, storableCreator, referenceResolver)
		return reader.readStorable()
	}
}