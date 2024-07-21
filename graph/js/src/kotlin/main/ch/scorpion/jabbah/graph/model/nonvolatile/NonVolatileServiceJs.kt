package ch.scorpion.jabbah.graph.model.nonvolatile

import ch.scorpion.jabbah.base.UUID

/**
 * Not yet implemented on JS platform.
 */
class NonVolatileServiceJs : NonVolatileService {

    override fun load(rootMetaGraphId: UUID): NonVolatileStorable? = null

    override fun store(rootMetaGraphId: UUID, nonVolatileStorable: NonVolatileStorable) {
        // empty
    }

    override fun delete(rootMetaGraphId: UUID) {
        // empty
    }
}