package ch.scorpion.jabbah.graph.model.oscilloscope

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.vertice.AbstractVertice
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.PortFactory
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader

/**
 * A [Vertice] that probes signals in a [Graph] in order to be tracked and displayed
 * on the view layer.
 *
 * TODO Refactoring: Split PortFactory into model and view parts to avoid dependency on GraphViewModule!
 */
class OscilloscopeProbeVertice<T: Any>(
        name: String? = null,
        portFactory: PortFactory = GraphViewModule.portFactory
) : AbstractVertice("graph.component.oscilloscope.port") {

    init {
        val port = portFactory.createPort<T>(PortType.INPUT)
        port.name = name
        addPort(portFactory.createOscilloscopeProbePort<T>(name))
    }

    /** ---- [Vertice] interface */

    override fun inputChanged(input: InputPort<*>, signalHandler: SignalHandler) {
        stateChanged(signalHandler)
    }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeString("name", getPort<T>().name!!)
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        getPort<T>().name = reader.readString("name")
    }
}