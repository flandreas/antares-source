package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.base.logger

abstract class AbstractStorableCloner {

	companion object {
		private val LOG by logger(AbstractStorableCloner::class)
	}

	protected abstract fun <T: Storable> serializeImpl(storable: T, identityProvider: GlobalIdentityProvider): String

	protected abstract fun <T: Storable> deserializeImpl(s: String, storableCreator: StorableCreator, referenceResolver: ReferenceResolver): T

	fun serialize(storable: Storable): String {
		return serializeImpl(storable, GlobalIdentityCreator())
	}

	fun deserialize(s: String): Storable {
		return deserializeImpl(s, IOModule.storableCreator, ReferenceResolverImpl())
	}

	fun <T: Storable> clone(storable: T): T {
		return newClone(storable)
	}

	fun <T: Storable> clonePreservingIdentities(storable: T, storableCreator: StorableCreator): T {
		return newClone(storable)
	}

	fun <T: Storable> cloneUsingCreator(storable: T, storableCreator: StorableCreator): T {
		return newClone(storable)
	}

	fun <T: Storable> clone(
		storable: T,
		identityProvider: GlobalIdentityProvider,
		storableCreator: StorableCreator,
		referenceResolver: ReferenceResolver
	): T {
		try {
			val data = serializeImpl(storable, identityProvider)
			return deserializeImpl(data, storableCreator, referenceResolver)
		} catch (x: Throwable) {
			LOG.error("Error while cloning Storable: ${x.message}")
			throw x
		}
	}

	fun <T: Storable> newClone(
		storable: T,
		identityProvider: GlobalIdentityProvider = GlobalIdentityCreator()
	): T {
		try {
			val data = serializeImpl(storable, identityProvider)
			//println(data)
			return deserializeImpl(data, IOModule.storableCreator, ReferenceResolverImpl(identityProvider))
		} catch (x: Throwable) {
			LOG.error("Error while cloning Storable: ${x.message}")
			throw x
		}
	}
}