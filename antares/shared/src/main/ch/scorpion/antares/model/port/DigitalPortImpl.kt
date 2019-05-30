package ch.scorpion.antares.model.port

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.OutputAnnotation
import ch.scorpion.antares.model.Trigger
import ch.scorpion.antares.model.port.DigitalPort.Companion.PROP_BIT_WIDTH
import ch.scorpion.antares.model.port.DigitalPort.Companion.PROP_LOGIC
import ch.scorpion.antares.model.port.DigitalPort.Companion.PROP_OUTPUT_ANNOTATION
import ch.scorpion.antares.model.port.DigitalPort.Companion.PROP_SIGNAL_REPRESENTATION
import ch.scorpion.antares.model.port.DigitalPort.Companion.PROP_TRIGGER
import ch.scorpion.antares.model.signal.*
import ch.scorpion.jabbah.edit.model.text.description.Describable
import ch.scorpion.jabbah.edit.model.text.description.DescribableImpl
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.port.PortImpl

/**
 * A standard implementation of the [DigitalPort] interface.
 */
open class DigitalPortImpl(
	portType: PortType,
	name: String? = null,
	logic: Logic = Logic.POSITIVE,
	trigger: Trigger = Trigger.LEVEL,
	bitWidth: BitWidth = BitWidth.BW_1,
	signalRepresentation: DigitalSignalRepresentation =
		if (bitWidth.width > 4) DigitalSignalRepresentation.HEXADECIMAL else DigitalSignalRepresentation.BINARY,
	describable: Describable = DescribableImpl()
) : PortImpl<DigitalSignal>(portType, DigitalSignal::class, name, describable), DigitalPort {

	companion object {

		fun createPort(portType: PortType): DigitalPort {
			return DigitalPortImpl(portType)
		}

		fun createInput(): DigitalPort {
			return createPort(PortType.INPUT)
		}

		fun createInput(name: String?): DigitalPort {
			return DigitalPortImpl(PortType.INPUT, name)
		}

		fun createInput(logic: Logic, name: String?, bitWidth: BitWidth): DigitalPort {
			return DigitalPortImpl(PortType.INPUT, name, logic, bitWidth = bitWidth)
		}

		fun createInput(trigger: Trigger, name: String?, bitWidth: BitWidth): DigitalPort {
			return DigitalPortImpl(PortType.INPUT, name, trigger = trigger, bitWidth = bitWidth)
		}

		fun createOutput(): DigitalPort {
			return createPort(PortType.OUTPUT)
		}

		fun createOutput(name: String?): DigitalPort {
			return DigitalPortImpl(PortType.OUTPUT, name)
		}

		fun createOutput(logic: Logic): DigitalPort {
			return DigitalPortImpl(PortType.OUTPUT, null, logic)
		}

		fun createOutput(logic: Logic, name: String?, bitWidth: BitWidth): DigitalPort {
			return DigitalPortImpl(PortType.OUTPUT, name, logic, bitWidth = bitWidth)
		}

		fun createTriStateOutput(logic: Logic, name: String?, bitWidth: BitWidth): DigitalPort {
			val port = DigitalPortImpl(PortType.OUTPUT, name, logic, bitWidth = bitWidth)
			port.defaultBit = Bit.Undefined
			return port
		}

		fun createOutput(logic: Logic, name: String?, bitWidth: BitWidth, signalRepresentation: DigitalSignalRepresentation): DigitalPort {
			return DigitalPortImpl(PortType.OUTPUT, name, logic, bitWidth = bitWidth, signalRepresentation = signalRepresentation)
		}

		fun createInOut(): DigitalPort {
			val port = DigitalPortImpl(PortType.INOUT)
			port.defaultBit = Bit.Undefined
			return port
		}

		fun createInOut(logic: Logic, name: String?, bitWidth: BitWidth): DigitalPort {
			val port = DigitalPortImpl(PortType.INOUT, name, logic, bitWidth = bitWidth)
			port.defaultBit = Bit.Undefined
			return port
		}
	}

	/**
	 * Explicitly set default {@link Bit}. If not set, [defaultDigitalSignal] calculates the default signal based
	 * on the [PortType].
	 */
	private var defaultBit: Bit? = null

	override fun toString(): String {
		return super.toString() + " BitWidth=$bitWidth"
	}

	/** ---- [DigitalPort] interface */

	override var bitWidth: BitWidth = bitWidth
		set(value) {
			if (field != value) {
				clear()
				val oldValue = field
				field = value
				changeSupport.fire(PROP_BIT_WIDTH, oldValue, field)
			}
		}

	override var logic: Logic = logic
		set(value) {
			if (field != value) {
				val oldValue = field
				field = value
				changeSupport.fire(PROP_LOGIC, oldValue, field)
			}
		}

	override var trigger: Trigger = trigger
		set(value) {
			if (field != value) {
				val oldValue = field
				field = value
				changeSupport.fire(PROP_TRIGGER, oldValue, field)
			}
		}

	override var outputAnnotation: OutputAnnotation = OutputAnnotation.NONE
		set(value) {
			if (field != value) {
				val oldValue = field
				field = value
				changeSupport.fire(PROP_OUTPUT_ANNOTATION, oldValue, field)
			}
		}

	override var signalRepresentation: DigitalSignalRepresentation = signalRepresentation
		set(value) {
			val oldValue = field
			field = value
			changeSupport.fire(PROP_SIGNAL_REPRESENTATION, oldValue, field)
		}

	override fun getDefaultSignal(): DigitalSignal? {
		return defaultDigitalSignal
	}

	override var isOutputDominant: Boolean = true

	override val dominantSignal: DigitalSignal
		get() {
			return if (isOutputDominant) {
				getOutgoingSignal()!!
			} else {
				getIncomingSignal()!!
			}
		}

	override var isAdaptive: Boolean = false

	/** ---- [Port] interface */

	override val defaultDigitalSignal: DigitalSignal
		get() {
			if (defaultBit != null) {
				return Word.allOf(bitWidth, defaultBit!!)
			}
			if (portType == PortType.INOUT) {
				return Word.undefined(bitWidth)
			}
			// Needed for power-on behaviour, especially for bistable circuits ('Undefined' would not work)
			return Word.allOf(bitWidth, Bit.False)
		}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		storeIncomingSignal(defaultDigitalSignal)
		storeOutgoingSignal(defaultDigitalSignal)
	}

	/** ---- [OutputPort] */

	override val isOutputUndefined: Boolean
		get() = getOutgoingSignal() == null || (getOutgoingSignal() as Word).isAllOf(Bit.Undefined)
}