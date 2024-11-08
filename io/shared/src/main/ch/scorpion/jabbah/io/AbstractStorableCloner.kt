package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.base.logger

abstract class AbstractStorableCloner {

	companion object {
		private val LOG by logger(AbstractStorableCloner::class)
	}

	/** Used to store an intermediate [ByteArray] while cloning. */
	protected class Buffer(
		val data: ByteArray,
		val length: Int
	)

	protected abstract fun <T: Storable> serializeImpl(storable: T, identityProvider: GlobalIdentityProvider): Buffer

	protected abstract fun <T: Storable> deserializeImpl(s: Buffer, referenceResolver: ReferenceResolver): T

	fun <T: Storable> clone(storable: T): T {
		return newClone(storable, GlobalIdentityCreator())
	}

	fun <T: Storable> clonePreservingIdentities(storable: T): T {
		return newClone(storable, GlobalIdentityCreator())
	}

	fun <T: Storable> clone(
		storable: T,
		identityProvider: GlobalIdentityProvider,
		referenceResolver: ReferenceResolver
	): T {
		try {
			val data = serializeImpl(storable, identityProvider)
			return deserializeImpl(data, referenceResolver)
		} catch (x: Throwable) {
			LOG.error("Error while cloning Storable: ${x.message}")
			throw x
		}
	}

	fun <T: Storable> newClone(
		storable: T,
		identityProvider: GlobalIdentityProvider
	): T {
		try {
			val data = serializeImpl(storable, identityProvider)
			return deserializeImpl(data, ReferenceResolverImpl(identityProvider))
		} catch (x: Throwable) {
			LOG.error("Error while cloning Storable: ${x.message}")
			throw x
		}
	}
}