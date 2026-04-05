package io.antarescircuit.antares.model

import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.graph.model.Net

/**
 * Posted by [DigitalGraph] on [EventBus] if setting [DigitalGraph.netSignalApplierStrategy]
 * was rejected by the specified [nets].
 */
data class NetSignalApplierFailure(
    val nets: Set<Net<*>>
)