package ch.scorpion.jabbah.graph.model.nonvolatile

import ch.scorpion.jabbah.graph.model.Vertice

/**
 * A [Vertice] whose state can potentially be preserved across simulation runs.
 * [Vertice]s implementing this interface basically support this feature.
 * The user can typically choose whether he wants this behaviour by setting the property.
 */
interface NonVolatile : Vertice {

    /**
     * Set to `true` if this [NonVolatile] should preserve is state across simulation runs.
     */
    var nonVolatile: Boolean
}