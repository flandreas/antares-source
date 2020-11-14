package ch.scorpion.jabbah.graph.model.port

import ch.scorpion.jabbah.base.collection.EmptyIterator
import ch.scorpion.jabbah.edit.model.text.description.Description
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphInput
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.SubGraphInputPort
import ch.scorpion.jabbah.graph.model.SubGraphOutputPort
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.io.*
import kotlin.reflect.KClass

/**
 * A [SubGraphPortImpl] is used as a [Port] in [SubGraphVertice]s and maintains a named link to the
 * [GraphPort] of the referenced [Graph].
 * @param <T> the type of the signal that is processed by this [SubGraphPortImpl].
 */
class SubGraphPortImpl<T: Any>(
	portType: PortType = PortType.INPUT,
	signalClass: KClass<T>? = null,
	name: String? = null
) : PortImpl<T>(portType, signalClass, name), SubGraphInputPort<T>, SubGraphOutputPort<T> {

    /** ---- [SubGraphInputPort] */

    override var graphInput: GraphInput<T>? = null

    /** ---- [SubGraphOutputPort] */

    override fun propagateSignal(signal: T, signalHandler: SignalHandler) {
        setOutgoingSignalBuffered(signal, signalHandler)
        if (owner is SubGraphVertice) {
            (owner as SubGraphVertice).propagateOutput(this, signal, signalHandler)
        }
    }

    /** ---- [PortImpl] */

    override fun setIncomingSignal(signal: T?, signalHandler: SignalHandler) {
        super.setIncomingSignal(signal, signalHandler)
        graphInput?.setIncomingSignal(signal, signalHandler)
    }

    // TODO Is this override necessary? Wasn't part of the guugen version
    override fun setOutgoingSignal(signal: T?, signalHandler: SignalHandler) {
        super.setOutgoingSignal(signal, signalHandler)
        owner?.outputChanged(this, signalHandler)
    }

    override fun setOutgoingSignalBuffered(signal: T?, signalHandler: SignalHandler) {
        super.setOutgoingSignalBuffered(signal, signalHandler)
        owner?.outputChanged(this, signalHandler)
    }

    /** ---- [Storable] interface */

    override var storableId: Int = 0

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
        // empty
    }

    override fun write(writer: StoreWriter) {
        writer.writeString("name", name!!)
        writer.writeString("type", portType.customName)
	    description.write("desc", writer)
    }

    override fun read(reader: StoreReader) {
        name = reader.readString("name")
        portType = PortType.withName(reader.readString("type"))
	    description = Description.read("desc", reader)
    }

    override fun getStorableChildren(): Iterator<Storable> = EmptyIterator()
}