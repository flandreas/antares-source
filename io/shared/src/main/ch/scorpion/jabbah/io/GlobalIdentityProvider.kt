package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.base.logger

/**
 * Provides globally unique identities for [Storable]s when writing them to persistent store.
 */
interface GlobalIdentityProvider {

    /**
     * Registers a [Storable] that is being written to persistent storage in order to be accessable
     * by calls of method [provideIdentity].*/
    fun register(storable: Storable)

    /**
     * Provides an identity of a [Storable] that has previously been registered using [register].
     */
    fun provideIdentity(storable: Storable): Int
}

/** A [GlobalIdentityProvider] that creates a new ID for every registered [Storable].*/
class GlobalIdentityCreator : GlobalIdentityProvider {

    private val LOG by logger(GlobalIdentityCreator::class)

    private val registry = mutableListOf<Storable>()

    override fun register(storable: Storable) {
        registry.add(storable)
    }

    override fun provideIdentity(storable: Storable): Int {
        val id = registry.indexOf(storable)
        if (id < 0) {
            LOG.warn("GlobalIdentityCreator: providing ID $id smaller than 0 for storable $storable")
        }
        return id
    }
}

/**
 * A [GlobalIdentityProvider] that reflects the identity of the registered [Storable]s that they already
 * possess.
 */
class GlobalIdentityReflector : GlobalIdentityProvider {

    override fun register(storable: Storable) {
        // empty
    }

    override fun provideIdentity(storable: Storable): Int {
        return storable.storableId
    }
}