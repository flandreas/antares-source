package io.antarescircuit.jabbah.io

import io.antarescircuit.jabbah.base.collection.IdentityKey

/**
 * Provides globally unique identities for [Storable]s when writing them to persistent store.
 */
interface GlobalIdentityProvider {

	/**
	 * Provides an identity of a [Storable] that has previously been registered using [register].
	 * Used when writing
	 * @return the globally unique identity of [storable] (starting with 0), or -1 if [storable]
	 * has not been registered previously.
	 */
    fun provideIdentity(storable: Storable): Int

	/**
	 * Used when reading [Storable]s upon cloning only a part of a [Storable].
	 * Throws an exception if [Storable] doesn't exist.
	 */
	fun getIdentity(storable: Storable): Int

	fun getStorableWithIdentity(globalId: Int): Storable?
}

class GlobalIdentityCreator : GlobalIdentityProvider {

	/** Holds the registered [Storable]s, with their index being their identity. */
	private val registry = mutableListOf<Storable>()

	/** Maps registered [Storable]s by object identity to their index in [registry]. */
	private val identities = HashMap<IdentityKey<Storable>, Int>()

	override fun provideIdentity(storable: Storable): Int =
		identities.getOrPut(IdentityKey(storable)) {
			registry.add(storable)
			registry.size - 1
		}

	override fun getIdentity(storable: Storable): Int =
		identities[IdentityKey(storable)]
			?: throw IllegalArgumentException("storable ${storable::class.simpleName} not available")

	override fun getStorableWithIdentity(globalId: Int): Storable? = registry.getOrNull(globalId)
}
