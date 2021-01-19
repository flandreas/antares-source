package ch.scorpion.antares.model.port

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.OutputAnnotation
import ch.scorpion.antares.model.Trigger
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.collection.EmptyIterator
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.io.*

/**
 * TODO Code duplication with SubGraphPortImpl.
 */
class SubCircuitPort(
	portType: PortType = PortType.INPUT,
	name: String? = null
) : DigitalPortImpl(portType, name), Storable, SubGraphInputPort<DigitalSignal>, SubGraphOutputPort<DigitalSignal> {

	/** ---- [SubGraphInputPort] interface */

	/** Holds the link to the [GraphInput] of the inner [Graph]. Is explicitly set during the execution binding process. */
	override var graphInput: GraphInput<DigitalSignal>? = null

	/** ---- [SubGraphOutputPort] interface */

	override fun propagateSignal(signal: DigitalSignal, signalHandler: SignalHandler) {
		setOutgoingSignalBuffered(signal, signalHandler)
		if (owner is SubGraphVertice) {
			(owner as SubGraphVertice).propagateOutput(this, signal, signalHandler)
		}
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
		if (outputAnnotation != OutputAnnotation.NONE) {
			writer.writeString("outputAnnotation", outputAnnotation.customName)
		}
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
		if (reader.hasAttribute("outputAnnotation")) {
			outputAnnotation = OutputAnnotation.withName(reader.readString("outputAnnotation"))
		}
	}

	override fun getStorableChildren(): Iterator<Storable> = EmptyIterator()
}