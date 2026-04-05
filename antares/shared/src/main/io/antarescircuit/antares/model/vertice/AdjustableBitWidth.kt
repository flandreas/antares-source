package io.antarescircuit.antares.model.vertice

import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.signal.BitWidth

/**
 * A [Vertice] having [DigitalPort] and the awareness that they have a [BitWidth] that can change
 */
interface AdjustableBitWidth : Vertice {

    /**
     * Asks this [AdjustableBitWidth] to change the [BitWidth] of the specified [DigitalPort]
     * to [bitWidth] while the [DigitalPort] is about to be connected to a [DigitalNet] with an
     * already established [BitWidth]
     *
     * @return `true` if the [BitWidth] was changed
     */
    fun adjustBitWidth(portId: Int, bitWidth: BitWidth): Boolean
}