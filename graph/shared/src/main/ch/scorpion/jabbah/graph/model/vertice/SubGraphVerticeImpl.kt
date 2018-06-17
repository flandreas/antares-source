package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StorableCreator
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.*

/**
 * A [SubGraphVertice] implementation that is used as the model class of a [ContainerDrawing].
 */
class SubGraphVerticeImpl : CalculatingVertice(EmptyVerticeCalculator), SubGraphVertice {

    companion object {
        private val LOG by logger(SubGraphVerticeImpl::class)
    }

    // TODO Not used any more?
    private val graph: Graph? = null

    /* ---- [SubGraphVertice] */

    // TODO Not used any more?
    override var graphUUID: UUID? = null

    override fun getGraphIfPresent(): Graph? {
        return graph
    }

    override fun getGraph(repository: MetaGraphRepository, storableCreator: StorableCreator): Graph {
        return graph!!
    }

    override fun <T : Any> propagateOutput(outputPort: SubGraphOutputPort<T>, signal: T, signalHandler: SignalHandler) {
        // Not needed here
    }

    /** ---- [Storable] */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeString("uuid", graphUUID.toString())
        writer.writeStorables("ports", getSubGraphPorts().iterator())
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        graphUUID = UUID(reader.readString("uuid"))

        for (port in reader.readStorables("ports").map { it as SubGraphPort<Any> }) {
            LOG.debug("SubGraphVerticeImpl: reading and adding SubCircuitPort $port")
            // Legacy file support. In new files, portId has always to be there!
            if (port.portId > 0) {
                addPort(port, port.portId)
            } else {
                addPort(port)
            }
        }
    }

    override fun getStorableChildren(): Iterator<Storable> {
        val list = mutableListOf<Storable>()
        list.addAll(getSubGraphPorts())
        return list.iterator()
    }

    /** ---- [SubGraphVerticeImpl] */

    private fun getSubGraphPorts(): ImmutableList<SubGraphPort<Any>> {
        return getPorts().map { it as SubGraphPort<Any>}.toImmutableList()
    }
}