package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.io.module.DomXmlWriter

/**
 * A [StorableCloner] implementation for the JavaScript platform.
 * TODO Refactor: Extract methods common to JVM implementation into base class.
 */
actual object StorableCloner {

	private val LOG by logger(StorableCloner::class)

	/** ---- [StorableCloner] interface */

	actual fun serialize(storable: Storable): String {
		throw UnsupportedOperationException("not implemented")
	}

	actual fun deserialize(s: String): Storable {
		throw UnsupportedOperationException("not implemented")
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

	actual fun <T: Storable> clone(storable: T, identityProvider: GlobalIdentityProvider, storableCreator: StorableCreator, referenceResolver: ReferenceResolver): T {
		try {
			var buffer: String? = null
			val xmlWriter = DomXmlWriter { buffer = it }
			val writer = StoreXmlWriter(xmlWriter, IOModule.typeMap, identityProvider)
			writer.writeStorable(storable)

			LOG.debug(buffer ?: "empty")

			val xmlReader = DomXmlReader(buffer!!)
			val reader = StoreXmlReader(xmlReader, IOModule.typeMap, storableCreator, referenceResolver)
			return reader.readStorable() as T
		} catch (x: Throwable) {
			LOG.error("Error while cloning Storable: ${x.message}")
			throw x
		}
	}
}