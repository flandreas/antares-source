package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.*
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.net.NetImpl

/**
 * Extends [NetImpl] to add the following responsibilities:
 * - convert `null`signals to [Bit.False]
 * - identify and propagate a design error if this [Net] connects [Port]s with incompatible [BitWidth]
 */
open class DigitalNet : NetImpl<DigitalSignal>() {

	companion object {
		private val LOG by logger(DigitalNet::class)
	}

	private val portPropertyListener = PortPropertyListener()

	/** ---- [NetImpl] */

	override val signal: DigitalSignal?
		get() = super.signal ?: DigitalSignalFactory.allOf(bitWidth, Bit.Undefined)

	override var signalBuffer: DigitalSignal?
		get() = super.signalBuffer ?: DigitalSignalFactory.allOf(bitWidth, Bit.Undefined)
		set(value) {
			super.signalBuffer = value
		}

	override fun cloneEmpty(): Net<DigitalSignal> = DigitalNet()

	/** ---- [GraphElement] */

	override val designError: DesignError?
		get() {
			super.designError?.let { return it }

			val occurringBitWidths = getOccurringBitWidths()
			if (occurringBitWidths.size <= 1) {
				return null
			}
			val bitWidthNames = occurringBitWidths.map { it.toString() }.toSet()
			return DesignError(Translations.getString("digitalnet.designError.text", bitWidthNames.joinToString(separator = ",")))
		}

	override val isError: Boolean
		get() = super.isError || isDesignError

	private val isDesignError: Boolean get() = getOccurringBitWidths().size > 1

	private fun getOccurringBitWidths(): Set<Int> {
		return ports.map { it as DigitalPort }.filter { !it.isAdaptive }.map { it.bitWidth.width }.toSet()
	}

	/** ---- [Net] */

	override val signalDescription: String? get() {
		if (signal == null) {
			return ""
		}
		return if (ports.size >= 1) {
			CurrentDigitalSignalNotation.notation.notate(signal!!, (ports.first() as DigitalPort).signalRepresentation)
		} else {
			CurrentDigitalSignalNotation.notation.notate(signal!!, DigitalSignalRepresentation.HEXADECIMAL)
		}
	}

	override fun setSignal(signal: DigitalSignal?, origin: OutputPort<DigitalSignal>, immediatePort: OutputPort<DigitalSignal>, signalHandler: SignalHandler, force: Boolean) {
		if (signal?.hasError == true) {
			raiseError(signal, origin, signalHandler)
		} else {
			super.setSignal(signal, origin, immediatePort, signalHandler, force)
		}
	}

	override fun connect(port: Port<DigitalSignal>) {
		super.connect(port)
		port.addPropertyChangeListener(portPropertyListener)
	}

	override fun unconnect(port: Port<*>) {
		super.unconnect(port)
		port.removePropertyChangeListener(portPropertyListener)
	}

	private fun raiseError(signal: DigitalSignal, origin: OutputPort<DigitalSignal>, signalHandler: SignalHandler) {
		val error = SignalError(origin, signalHandler.executionTime)
		val logMsg = "Error signal $signal from port ${origin.portId} in ${origin.owner?.id}"
		LOG.trace(logMsg)
		executionError = error
		signalHandler.deferExecutionError(error)
	}

	/** ---- [DigitalNet] */

	val bitWidth: BitWidth
		get() {
			if (portsCount == 0) {
				return BitWidth.BW_1
			}
			return ports.map { it as DigitalPort }.first().bitWidth
		}

	private inner class PortPropertyListener : PropertyChangeListener<Any> {
		override fun propertyChanged(e: PropertyChangeEvent<Any>) {
			stateChanged()
		}
	}
}