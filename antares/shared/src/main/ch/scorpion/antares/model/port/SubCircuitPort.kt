package ch.scorpion.antares.model.port

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.Trigger
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.collection.EmptyIterator
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphInput
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.SubGraphInputPort
import ch.scorpion.jabbah.graph.model.SubGraphOutputPort
import ch.scorpion.jabbah.io.*

class SubCircuitPort(
    portType: PortType = PortType.INPUT,
    name: String? = null
) : DigitalPortImpl(portType, name), Storable, SubGraphInputPort<DigitalSignal>, SubGraphOutputPort<DigitalSignal> {

    private val LOG by logger(SubCircuitPort::class)

    init {
        LOG.debug("SubCircuitPort: new instance $this portType=$portType name=$name")
    }

    /** ---- [SubGraphInputPort] */

    /** Holds the link to the [GraphInput] of the inner [Graph]. Is explicitly set during the execution binding process. */
    override var graphInput: GraphInput<DigitalSignal>? = null

    /** ---- [PortImpl] */

    override fun setOutgoingSignalBuffered(signal: DigitalSignal?, signalHandler: SignalHandler) {
        super.setOutgoingSignalBuffered(signal, signalHandler)
        owner!!.outputChanged(this, signalHandler)
    }

    /** ---- [Storable] interface */

    override var storableId: Int = 0

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
        // empty
    }

    override fun write(writer: StoreWriter) {
        writer.writeInt("portId", portId)
        if (StringUtils.isNotEmpty(name)) {
            writer.writeString("name", name!!)
        }
        writer.writeString("logic", logic.customName)
        writer.writeInt("bitWidth", bitWidth.width)
        writer.writeString("trigger", trigger.customName)
        writer.writeString("type", portType.customName)
        writer.writeString("representation", signalRepresentation.customName)
    }

    override fun read(reader: StoreReader) {
        if (reader.hasAttribute("portId")) {
            // TODO Legacy file support. In new files, portId has always to be there!
            portId = reader.readInt("portId")
        }
        if (reader.hasAttribute("name")) {
            name = reader.readString("name")
        }
        logic = Logic.withName(reader.readString("logic"))
        if (reader.hasAttribute("bitWidth")) {
            bitWidth = BitWidth.of(reader.readInt("bitWidth"))
        }
        if (reader.hasAttribute("trigger")) {
            trigger = Trigger.withName(reader.readString("trigger"))
        }
        portType = PortType.withName(reader.readString("type"))
        if (reader.hasAttribute("representation")) {
            // TODO Legacy file support. In new files, portId has always to be there!
            signalRepresentation = DigitalSignalRepresentation.withName(reader.readString("representation"))
        }
    }

    override fun getStorableChildren(): Iterator<Storable> = EmptyIterator()
}