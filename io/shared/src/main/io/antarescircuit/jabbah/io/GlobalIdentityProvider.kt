package io.antarescircuit.jabbah.io

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

	private val registry = mutableListOf<Storable>()

	override fun provideIdentity(storable: Storable): Int {
		if (!registry.contains(storable)) {
			registry.add(storable)
		}
		return registry.indexOf(storable)
	}

	override fun getIdentity(storable: Storable): Int {
		if (!registry.contains(storable)) {
			throw IllegalArgumentException("storable ${storable::class.simpleName} not available")
		}
		return registry.indexOf(storable)
	}

	override fun getStorableWithIdentity(globalId: Int): Storable? {
		if (globalId in (0 until registry.size)) {
			return registry[globalId]
		}
		return null
	}
}