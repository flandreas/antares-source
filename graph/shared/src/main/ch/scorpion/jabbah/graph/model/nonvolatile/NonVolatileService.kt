package ch.scorpion.jabbah.graph.model.nonvolatile

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.MetaGraph

/**
 * Loads and stores [NonVolatileStorable] from and to persistent store to share
 * state across multiple execution runs.
 */
interface NonVolatileService {

    /**
     * Loads non-volatile data for the root [MetaGraph] with the specified [UUID].
     * @return `null` if no data is available
     */
    fun load(rootMetaGraphId: UUID): NonVolatileStorable?

    /**
     * Persistently stores non-volatile data for the root [MetaGraph] with the specified [UUID].
     */
    fun store(rootMetaGraphId: UUID, nonVolatileStorable: NonVolatileStorable)

    /**
     * Deletes potentially stored non-volatile data for the root [MetaGraph] with the specified [UUID].
     * This might occur if the [MetaGraph] has been changed since the last execution run such that there
     * is no data to store anymore.
     */
    fun delete(rootMetaGraphId: UUID)
}

class UnimplementedNonVolatileService : NonVolatileService {
    override fun load(rootMetaGraphId: UUID): NonVolatileStorable? {
        throw UnsupportedOperationException("not implemented")
    }

    override fun store(rootMetaGraphId: UUID, nonVolatileStorable: NonVolatileStorable) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun delete(rootMetaGraphId: UUID) {
        throw UnsupportedOperationException("not implemented")
    }
}

class EmptyNonVolatileService : NonVolatileService {

    override fun load(rootMetaGraphId: UUID): NonVolatileStorable? = null

    override fun store(rootMetaGraphId: UUID, nonVolatileStorable: NonVolatileStorable) {}

    override fun delete(rootMetaGraphId: UUID) {}
}