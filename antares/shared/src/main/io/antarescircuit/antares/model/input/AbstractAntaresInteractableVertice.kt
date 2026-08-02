package io.antarescircuit.antares.model.input

import io.antarescircuit.jabbah.graph.model.vertice.AbstractInteractableVertice
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

abstract class AbstractAntaresInteractableVertice<S: Any>(
    calculator: VerticeCalculator<*>,
) : AbstractInteractableVertice<S>(calculator) {

    companion object {
        val DEF_PROP_DELAY get() = CurrentSwitchPropagationDelay.value
    }

    override var interactivePropagationDelay: Long = DEF_PROP_DELAY.value

    /** ---- [Storable] interface */

    override fun read(reader: StoreReader) {
        super.read(reader)
        if (reader.hasAttribute("interactivePropagationDelay")) {
            interactivePropagationDelay = reader.readLong("interactivePropagationDelay")
        }
    }

    override fun write(writer: StoreWriter) {
        super.write(writer)
        if (interactivePropagationDelay != DEF_PROP_DELAY.value) {
            writer.writeLong("interactivePropagationDelay", interactivePropagationDelay)
        }
    }
}