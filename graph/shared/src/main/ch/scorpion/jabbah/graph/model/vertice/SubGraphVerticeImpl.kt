package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.base.collection.ConcatIterator
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.SubGraphInputPort
import ch.scorpion.jabbah.graph.model.SubGraphOutputPort
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StorableCreator
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.base.UUID

/**
 * A [SubGraphVertice] implementation that is used as the model class of a [ContainerDrawing].
 */
class SubGraphVerticeImpl : CalculatingVertice(EmptyVerticeCalculator), SubGraphVertice {

    // TODO Not used any more?
    private val graph: Graph? = null

    /* ---- [SubGraphVertice] */

    // TODO Not used any more?
    override var graphUUID: UUID? = null

    override fun getGraphIfPresent(): Graph? {
        return graph
    }

    override fun getGraph(library: Library, storableCreator: StorableCreator): Graph {
        return graph!!
    }

    /** ---- [Storable] */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeString("uuid", graphUUID.toString());
        writer.writeStorables("inputPorts", getSubGraphInputPorts().iterator());
        writer.writeStorables("outputPorts", getSubGraphOutputPorts().iterator());
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        graphUUID = UUID(reader.readString("uuid"))
        for (inputPort in reader.readStorables("inputPorts").map { it as SubGraphInputPort<Any> }) {
            // Legacy file support. In new files, portId has always to be there!
            if (inputPort.portId > 0) {
                addPort(inputPort, inputPort.portId)
            } else {
                addPort(inputPort)
            }
        }
        for (outputPort in reader.readStorables("outputPorts").map { it as SubGraphOutputPort<Any> }) {
            // Legacy file support. In new files, portId has always to be there!
            if (outputPort.portType == PortType.OUTPUT) {
                // Exclude InOuts (old bug for which persistent files still exists)
                if (outputPort.portId > 0) {
                    addPort(outputPort, outputPort.portId)
                } else {
                    addPort(outputPort)
                }
            }
        }
    }

    override fun getStorableChildren(): Iterator<Storable> {
        val list = mutableListOf<Storable>()
        list.addAll(getSubGraphInputPorts())
        list.addAll(getSubGraphOutputPorts())
        return list.iterator()
    }

    /** ---- [SubGraphVerticeImpl] */

    private fun getSubGraphInputPorts(): ImmutableList<SubGraphInputPort<Any>> {
        return getInputs().map { it as SubGraphInputPort<Any> }.toImmutableList()
    }

    private fun getSubGraphOutputPorts(): ImmutableList<SubGraphOutputPort<Any>> {
        return getOutputs().map { it as SubGraphOutputPort<Any> }.toImmutableList()
    }
}