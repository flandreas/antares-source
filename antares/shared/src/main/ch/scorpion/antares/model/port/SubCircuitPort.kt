package ch.scorpion.antares.model.port

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.OutputAnnotation
import ch.scorpion.antares.model.Trigger
import ch.scorpion.antares.model.inout.DigitalCircuitInOut
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.jabbah.base.StringUtils
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

	/** ---- [SubGraphPort] interface */

	override fun handleGraphPortChanged(graphPort: GraphPort<*>) {
		if (graphPort is DigitalCircuitInOut) {
			// Bug #803 Cast failed for analog circuits
			if (bitWidth.width != graphPort.bitWidth.width) {
				bitWidth = BitWidth.of(graphPort.bitWidth.width)
			}
		}
	}

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

	override var isReading: Boolean = false

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
		if (customCanBeUndefined) {
			writer.writeBoolean("canBeUndefined", customCanBeUndefined)
		}
		if (unconnectedStartValue != null) {
			writer.writeULong("startValue", unconnectedStartValue!!.getValue())
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
		if (reader.hasAttribute("canBeUndefined")) {
			customCanBeUndefined = reader.readBoolean("canBeUndefined")
		}
		if (reader.hasAttribute("startValue")) {
			unconnectedStartValue = DigitalSignalFactory.of(bitWidth, reader.readULong("startValue"))
		}
	}
}