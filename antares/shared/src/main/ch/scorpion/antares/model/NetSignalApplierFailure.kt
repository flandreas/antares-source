package ch.scorpion.antares.model

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.graph.model.Net

/**
 * Posted by [DigitalGraph] on [EventBus] if setting [DigitalGraph.netSignalApplierStrategy]
 * was rejected by the specified [nets].
 */
data class NetSignalApplierFailure(
    val nets: Set<Net<*>>
)