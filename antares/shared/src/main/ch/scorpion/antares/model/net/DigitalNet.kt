package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.*
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.DesignError
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.net.NetImpl

/**
 * Extends [NetImpl] to add the following responsibilities:
 * - convert `null`signals to [Bit.False]
 * - identify and propagate a design error if this [Net] connects [Port]s with incompatible [BitWidth]
 */
open class DigitalNet : NetImpl<DigitalSignal>() {

	private val portPropertyListener = PortPropertyListener()

	/** ---- [NetImpl] */

	override val signal: DigitalSignal?
		get() = super.signal ?: Word.allOf(bitWidth, Bit.Undefined)

	override var signalBuffer: DigitalSignal?
		get() = super.signalBuffer ?: Word.allOf(bitWidth, Bit.Undefined)
		set(value) {
			super.signalBuffer = value
		}

	/** ---- [GraphElement] */

	override val designError: DesignError?
		get() {
			val occurringBitWidths = getOccurringBitWidths()
			if (occurringBitWidths.size <= 1) {
				return null
			}
			val bitWidthNames = occurringBitWidths.map { it.customName }.toSet()
			return DesignError(Translations.getString("digitalnet.designError.text", bitWidthNames.joinToString(separator = ",")))
		}

	override val isError: Boolean
		get() = super.isError || isDesignError

	private val isDesignError: Boolean get() = getOccurringBitWidths().size > 1

	private fun getOccurringBitWidths(): Set<BitWidth> {
		return ports.map { it as DigitalPort }.filter { !it.isAdaptive }.map { it.bitWidth }.toSet()
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

	override fun connect(port: Port<DigitalSignal>) {
		super.connect(port)
		port.addPropertyChangeListener(portPropertyListener)
	}

	override fun unconnect(port: Port<*>) {
		super.unconnect(port)
		port.removePropertyChangeListener(portPropertyListener)
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